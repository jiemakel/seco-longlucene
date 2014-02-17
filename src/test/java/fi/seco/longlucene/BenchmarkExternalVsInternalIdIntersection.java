/**
 * 
 */
package fi.seco.longlucene;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.runner.CaliperMain;

import fi.seco.longlucene.ILongIndexReader.ILongCollectorFactory;
import fi.seco.longobject.set.IMutableLongSetWithConcurrentAdd;
import fi.seco.longobject.set.ISets;
import fi.seco.util.FileUtils;

/**
 * @author jiemakel
 * 
 */
public class BenchmarkExternalVsInternalIdIntersection extends Benchmark {

	private ILongIndexReader lld1, lld2, lld3;
	private File f1, f2, f3;

	@Param({ "10000", "100000" })
	private int indexSize;

	@Param({ "5", "20", "100" })
	private int numCategories;

	@Param({ "10", "1000" })
	private int getCount;

	private BytesRef[] id;

	@Override
	protected void setUp() throws IOException {
		try {
			f1 = File.createTempFile("testlld", "tmp");
			f2 = File.createTempFile("testlld", "tmp");
			f3 = File.createTempFile("testlld", "tmp");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		f1.delete();
		f2.delete();
		f3.delete();
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_50);
		ILongIndexWriter tmp1 = new LongLuceneDirectory(f1.getAbsolutePath(), analyzer, false, null).writer();
		ILongIndexWriter tmp2 = new LongLuceneDirectory(f2.getAbsolutePath(), analyzer, false, null).writer();
		ILongIndexWriter tmp3 = new LongLuceneDirectory(f3.getAbsolutePath(), analyzer, false, null).writer();
		Random r = new Random();
		for (int i = 0; i < indexSize; i++)
			for (int j = 0; j < numCategories; j++) {
				long l2 = r.nextInt(indexSize);
				Document d1 = new Document();
				Document d2 = new Document();
				Document d3 = new Document();
				d1.add(new Field("l2", new SingleLongTokenStream(l2), LongLuceneDirectory.pretokenizedField));
				d2.add(new Field("l2", new SingleLongTokenStream(l2), LongLuceneDirectory.pretokenizedField));
				d3.add(new Field("l2", new SingleLongTokenStream(l2), LongLuceneDirectory.pretokenizedField));
				d2.add(new NumericDocValuesField("l1", i));
				d3.add(new StoredField("l1", (long) i));
				tmp1.addDocument(d1);
				tmp2.addDocument(d2);
				tmp3.addDocument(d3);
			}
		/*		tmp1.commit();
				tmp2.commit();
				tmp3.commit();*/
		lld1 = tmp1.getReader();
		lld2 = tmp2.getReader();
		lld3 = tmp3.getReader();
		id = new BytesRef[getCount + 1];
		for (int i = 0; i < getCount + 1; i++)
			id[i] = new BytesRef(ByteBuffer.allocate(8).putLong(r.nextInt(indexSize)).array());
	}

	@Override
	protected void tearDown() {
		FileUtils.recursivelyDelete(f1);
		FileUtils.recursivelyDelete(f2);
		FileUtils.recursivelyDelete(f3);
	}

	public long timeInternalIdIntersection(int reps) throws IOException {
		final long[] ret = new long[1];
		for (int i = 0; i < reps; i++)
			for (int j = 0; j < getCount; j++) {
				BooleanQuery bq = new BooleanQuery();
				bq.add(new TermQuery(new Term("l2", id[j])), BooleanClause.Occur.MUST);
				bq.add(new TermQuery(new Term("l2", id[j + 1])), BooleanClause.Occur.MUST);
				lld1.search(bq, new ILongCollectorFactory() {

					private final ILongCollector lc = new ILongCollector() {

						@Override
						public void collect(long doc, NonExceptingScorer scorer) {
							ret[0]++;
						}

					};

					@Override
					public ILongCollector get() {
						return lc;
					}

				});
			}
		return ret[0];
	}

	public long timeCSFIdIntersection(int reps) throws IOException {
		final long[] ret = new long[1];
		final ILongNumericDocValues ls = lld2.getNumericDocValues("l1");
		for (int i = 0; i < reps; i++)
			for (int j = 0; j < getCount; j++) {
				final IMutableLongSetWithConcurrentAdd s1 = ISets.getNewMutableLongSetWithConcurrentAdd(false, false, true);
				final IMutableLongSetWithConcurrentAdd s2 = ISets.getNewMutableLongSetWithConcurrentAdd(false, false, true);

				lld2.search(new TermQuery(new Term("l2", id[j])), new ILongCollectorFactory() {

					private final ILongCollector lc = new ILongCollector() {

						@Override
						public void collect(long doc, NonExceptingScorer scorer) {
							s1.add(ls.get(doc));
						}

					};

					@Override
					public ILongCollector get() {
						return lc;
					}

				});
				lld2.search(new TermQuery(new Term("l2", id[j + 1])), new ILongCollectorFactory() {

					private final ILongCollector lc = new ILongCollector() {

						@Override
						public void collect(long doc, NonExceptingScorer scorer) {
							s2.add(ls.get(doc));
						}

					};

					@Override
					public ILongCollector get() {
						return lc;
					}

				});
				s1.and(s2);
				ret[0] += s1.size();
			}
		return ret[0];
	}

	/*	public long timeStoredIdIntersection(int reps) throws IOException {
			final long[] ret = new long[1];
			for (int i = 0; i < reps; i++)
				for (int j = 0; j < getCount; j++) {
					final IMutableLongSetWithConcurrentAdd s1 = ISets.getNewMutableLongSetWithConcurrentAdd(false, false, true);
					final IMutableLongSetWithConcurrentAdd s2 = ISets.getNewMutableLongSetWithConcurrentAdd(false, false, true);

					lld2.search(new TermQuery(new Term("l2", id[j])), new ILongCollectorFactory() {

						private final ILongCollector lc = new ILongCollector() {

							@Override
							public void collect(long doc, NonExceptingScorer scorer) {
								s1.add(lld3.document(doc).getField("l1").numericValue().longValue());
							}

						};

						@Override
						public ILongCollector get() {
							return lc;
						}

					});
					lld2.search(new TermQuery(new Term("l2", id[j + 1])), new ILongCollectorFactory() {

						private final ILongCollector lc = new ILongCollector() {

							@Override
							public void collect(long doc, NonExceptingScorer scorer) {
								s2.add(lld3.document(doc).getField("l1").numericValue().longValue());
							}

						};

						@Override
						public ILongCollector get() {
							return lc;
						}

					});
					s1.and(s2);
					ret[0] += s1.size();
				}
			return ret[0];
		} */

	public static void main(String[] args) {
		CaliperMain.main(BenchmarkExternalVsInternalIdIntersection.class, args);
	}
}
