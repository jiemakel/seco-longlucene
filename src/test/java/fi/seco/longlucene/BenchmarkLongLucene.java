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
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.SingleInstanceLockFactory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import com.google.caliper.Benchmark;
import com.google.caliper.Param;
import com.google.caliper.runner.CaliperMain;

import fi.seco.longlucene.ILongIndexReader.ILongCollectorFactory;
import fi.seco.util.FileUtils;

/**
 * @author jiemakel
 * 
 */
public class BenchmarkLongLucene extends Benchmark {

	private ILongIndexReader lld;
	private IndexSearcher ld;
	private File f1, f2;

	@Param({ "10000", "100000", "500000000" })
	private int indexSize;

	@Param({ "1", "10", "100" })
	private int numCategories;

	@Param({ "10", "1000" })
	private int getCount;

	private BytesRef[] id;

	@Override
	protected void setUp() throws IOException {
		try {
			f1 = File.createTempFile("testlld", "tmp");
			f2 = File.createTempFile("testlld", "tmp");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		f1.delete();
		f2.delete();
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);
		ILongIndexWriter tmp = new LongLuceneDirectory(f1.getAbsolutePath(), analyzer, false, null).writer();
		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_46, analyzer);
		iwc.setRAMBufferSizeMB(512);
		IndexWriter tmp2 = new IndexWriter(new MMapDirectory(f2, new SingleInstanceLockFactory()), iwc);
		Random r = new Random();
		for (int i = 0; i < indexSize; i++)
			for (int j = 0; j < numCategories; j++) {
				long l2 = r.nextInt(indexSize);
				Document d1 = new Document();
				Document d2 = new Document();
				d1.add(new Field("l2", new SingleLongTokenStream(l2), LongLuceneDirectory.pretokenizedField));
				d2.add(new Field("l2", new SingleLongTokenStream(l2), LongLuceneDirectory.pretokenizedField));
				tmp.addDocument(d1);
				tmp2.addDocument(d2);
			}
		tmp.commit();
		tmp2.commit();
		lld = tmp.getReader();
		ld = new IndexSearcher(DirectoryReader.open(tmp2, false));
		id = new BytesRef[getCount];
		for (int i = 0; i < getCount; i++)
			id[i] = new BytesRef(ByteBuffer.allocate(8).putLong(r.nextInt(indexSize)).array());
	}

	@Override
	protected void tearDown() {
		FileUtils.recursivelyDelete(f1);
		FileUtils.recursivelyDelete(f2);
	}

	public long timeLuceneDirectoryGet(int reps) throws IOException {
		final long[] ret = new long[1];
		for (int i = 0; i < reps; i++)
			for (int j = 0; j < getCount; j++)
				ld.search(new TermQuery(new Term("l2", id[j])), new Collector() {

					private int base;

					@Override
					public void setScorer(Scorer scorer) throws IOException {}

					@Override
					public void collect(int doc) throws IOException {
						ret[0] += base + doc;
					}

					@Override
					public void setNextReader(AtomicReaderContext context) throws IOException {
						base = context.docBase;
					}

					@Override
					public boolean acceptsDocsOutOfOrder() {
						return true;
					}

				});
		return ret[0];
	}

	public long timeLongLuceneDirectoryGet(int reps) throws IOException {
		final long[] ret = new long[1];
		for (int i = 0; i < reps; i++)
			for (int j = 0; j < getCount; j++)
				lld.search(new TermQuery(new Term("l2", id[j])), new ILongCollectorFactory() {

					private final ILongCollector lc = new ILongCollector() {

						@Override
						public void collect(long doc, NonExceptingScorer scorer) {
							ret[0] += doc;
						}

					};

					@Override
					public ILongCollector get() {
						return lc;
					}

				});
		return ret[0];
	}

	public static void main(String[] args) {
		CaliperMain.main(BenchmarkLongLucene.class, args);
	}
}
