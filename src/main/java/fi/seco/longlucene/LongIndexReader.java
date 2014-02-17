package fi.seco.longlucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.BinaryDocValues;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.DocsEnum;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.index.ReaderUtil;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixTermsEnum;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LongIndexReader implements ILongIndexReader {

	private static final Logger log = LoggerFactory.getLogger(LongIndexReader.class);

	private final List<AtomicReaderContext>[] atomicContexts;
	private final int[][] starts;
	private final DirectoryReader[] readers;

	private final IndexSearcher[] searchers;
	private final ExecutorService es;

	@SuppressWarnings("unchecked")
	public LongIndexReader(DirectoryReader[] readers, ExecutorService es) {
		this.readers = readers;
		this.es = es;
		atomicContexts = new List[readers.length];
		starts = new int[readers.length][];
		searchers = new IndexSearcher[readers.length];
		for (int i = 0; i < readers.length; i++) {
			List<AtomicReaderContext> ac = readers[i].leaves();
			atomicContexts[i] = ac;
			int[] mstarts = new int[ac.size()];
			for (int j = 0; j < ac.size(); j++)
				mstarts[j] = ac.get(j).docBase;
			starts[i] = mstarts;
			searchers[i] = new IndexSearcher(readers[i], es);
		}
	}

	@Override
	public long numDocs() {
		long count = 0;
		for (int i = 0; i < atomicContexts.length; i++) {
			List<AtomicReaderContext> c = atomicContexts[i];
			for (int j = 0; j < c.size(); j++)
				count += c.get(j).reader().numDocs();
		}
		return count;
	}

	@Override
	public long numDeletedDocs() {
		long count = 0;
		for (int i = 0; i < atomicContexts.length; i++) {
			List<AtomicReaderContext> c = atomicContexts[i];
			for (int j = 0; j < c.size(); j++)
				count += c.get(j).reader().numDeletedDocs();
		}
		return count;
	}

	private static final class LongCollectorCallable implements Callable<Object> {

		private final long base;
		private final AtomicReaderContext ac;
		private final ILongCollectorFactory lsf;
		private final Weight w;

		private LongCollectorCallable(long base, Weight w, AtomicReaderContext ac, ILongCollectorFactory lsf) {
			this.base = base + ac.docBase;
			this.lsf = lsf;
			this.ac = ac;
			this.w = w;
		}

		@Override
		public Object call() {
			ILongCollector ls = lsf.get();
			try {
				Scorer sc = w.scorer(ac, !false, true, ac.reader().getLiveDocs());
				if (sc == null) return null;
				NonExceptingScorer nsc = new NonExceptingScorer(sc);
				int doc;
				while ((doc = sc.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS)
					ls.collect(base + doc, nsc);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return null;
		}

	}

	private boolean parallel = true;

	@Override
	public void setParallel(boolean parallel) {
		this.parallel = parallel;
	}

	@Override
	public void search(Query query, ILongCollectorFactory collector) {
		if (false)
			try {
				long base = 0;
				List<Future<Object>> futures = new ArrayList<Future<Object>>();
				for (int i = 0; i < atomicContexts.length; i++) {
					List<AtomicReaderContext> c = atomicContexts[i];
					Weight w = searchers[i].createNormalizedWeight(query);
					for (int j = 0; j < c.size(); j++)
						futures.add(es.submit(new LongCollectorCallable(base, w, c.get(j), collector)));
					base += LongLuceneDirectory.bsize;
				}
				for (Future<Object> f : futures)
					try {
						f.get();
					} catch (ExecutionException e) {
						if (e.getCause() instanceof Error) throw (Error) e.getCause();
						if (e.getCause() instanceof RuntimeException) throw (RuntimeException) e.getCause();
						throw new RuntimeException(e.getCause());
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		else {
			final long[] base = new long[1];
			final ILongCollector ilc = collector.get();
			for (int i = 0; i < searchers.length; i++) {
				try {
					searchers[i].search(query, new Collector() {

						private NonExceptingScorer nsc;
						private long mbase;

						@Override
						public void setScorer(Scorer scorer) throws IOException {
							nsc = new NonExceptingScorer(scorer);
						}

						@Override
						public void setNextReader(AtomicReaderContext context) throws IOException {
							mbase = base[0] + context.docBase;
						}

						@Override
						public void collect(int doc) throws IOException {
							ilc.collect(mbase + doc, nsc);
						}

						@Override
						public boolean acceptsDocsOutOfOrder() {
							return true;
						}
					});
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				base[0] += LongLuceneDirectory.bsize;
			}
		}
	}

	private final Map<String, ThreadLocal<ILongNumericDocValues>> dsnCache = new HashMap<String, ThreadLocal<ILongNumericDocValues>>();

	public ILongNumericDocValues getNumericDocValues(final String field) {
		ThreadLocal<ILongNumericDocValues> dstl = dsnCache.get(field);
		if (dstl == null) {
			dstl = new ThreadLocal<ILongNumericDocValues>() {
				@Override
				protected ILongNumericDocValues initialValue() {
					final NumericDocValues[][] sources = new NumericDocValues[atomicContexts.length][];

					for (int i = 0; i < sources.length; i++) {
						List<AtomicReaderContext> c = atomicContexts[i];
						NumericDocValues[] msources = new NumericDocValues[c.size()];
						for (int j = 0; j < c.size(); j++)
							try {
								NumericDocValues t = c.get(j).reader().getNumericDocValues(field);
								if (t != null) msources[j] = t;
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						sources[i] = msources;
					}
					return new ILongNumericDocValues() {

						private long cstart = -1;
						private long cend = -1;
						private NumericDocValues csrc;

						@Override
						public long get(long docId) {
							if (docId >= cstart && docId < cend) return csrc.get((int) (docId - cstart));
							ensureSource(docId);
							return csrc.get((int) (docId - cstart));
						}

						private final void ensureSource(long docId) {
							int sind1 = (int) (docId >>> LongLuceneDirectory.bshift);
							int sind2 = (int) (docId & LongLuceneDirectory.band);
							int[] mstarts = starts[sind1];
							int subInd = ReaderUtil.subIndex(sind2, mstarts);
							cstart = sind1;
							cstart <<= LongLuceneDirectory.bshift;
							cstart += mstarts[subInd];
							cend = cstart + atomicContexts[sind1].get(subInd).reader().maxDoc();
							csrc = sources[sind1][subInd];
						}

						/*						@Override
												public BytesRef getBytes(long docId, BytesRef ref) {
													if (docId >= cstart && docId < cend) return csrc.getBytes((int) (docId - cstart), ref);
													ensureSource(docId);
													return csrc.getBytes((int) (docId - cstart), ref);
												} */

					};
				}
			};
			dsnCache.put(field, dstl);
		}
		return dstl.get();
	}

	private final Map<String, ThreadLocal<ILongBinaryDocValues>> dsbCache = new HashMap<String, ThreadLocal<ILongBinaryDocValues>>();

	public ILongBinaryDocValues getBinaryDocValues(final String field) {
		ThreadLocal<ILongBinaryDocValues> dstl = dsbCache.get(field);
		if (dstl == null) {
			dstl = new ThreadLocal<ILongBinaryDocValues>() {
				@Override
				protected ILongBinaryDocValues initialValue() {
					final BinaryDocValues[][] sources = new BinaryDocValues[atomicContexts.length][];

					for (int i = 0; i < sources.length; i++) {
						List<AtomicReaderContext> c = atomicContexts[i];
						BinaryDocValues[] msources = new BinaryDocValues[c.size()];
						for (int j = 0; j < c.size(); j++)
							try {
								BinaryDocValues t = c.get(j).reader().getBinaryDocValues(field);
								if (t != null) msources[j] = t;
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						sources[i] = msources;
					}
					return new ILongBinaryDocValues() {

						private long cstart = -1;
						private long cend = -1;
						private BinaryDocValues csrc;

						private final void ensureSource(long docId) {
							int sind1 = (int) (docId >>> LongLuceneDirectory.bshift);
							int sind2 = (int) (docId & LongLuceneDirectory.band);
							int[] mstarts = starts[sind1];
							int subInd = ReaderUtil.subIndex(sind2, mstarts);
							cstart = sind1;
							cstart <<= LongLuceneDirectory.bshift;
							cstart += mstarts[subInd];
							cend = cstart + atomicContexts[sind1].get(subInd).reader().maxDoc();
							csrc = sources[sind1][subInd];
						}

						@Override
						public void get(long docId, BytesRef ref) {
							if (docId >= cstart && docId < cend)
								csrc.get((int) (docId - cstart), ref);
							else {
								ensureSource(docId);
								csrc.get((int) (docId - cstart), ref);
							}
						}

					};
				}
			};
			dsbCache.put(field, dstl);
		}
		return dstl.get();
	}

	@Override
	public Document document(long docId) {
		int sind1 = (int) (docId >>> LongLuceneDirectory.bshift);
		int sind2 = (int) (docId & LongLuceneDirectory.band);
		try {
			return readers[sind1].document(sind2);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long docFreq(Term t) {
		long count = 0;
		for (int i = 0; i < atomicContexts.length; i++) {
			List<AtomicReaderContext> c = atomicContexts[i];
			for (int j = 0; j < c.size(); j++)
				try {
					count += c.get(j).reader().docFreq(t);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		}
		return count;
	}

	@Override
	public List<AtomicReaderContext>[] getAtomicReaderContexts() {
		return atomicContexts;
	}

	@Override
	public void runPrefixQueries(String field, String text, final ILongPrefixCollector pc) {
		long base = 0;
		TermsEnum ter = null;
		BytesRef prefix = new BytesRef(text);
		try {
			for (int i = 0; i < atomicContexts.length; i++) {
				List<AtomicReaderContext> c = atomicContexts[i];
				for (int j = 0; j < c.size(); j++) {
					final AtomicReader reader = c.get(j).reader();
					Terms t = reader.terms(field);
					if (t == null) continue;
					ter = t.iterator(ter);
					final TermsEnum termsEnum = new PrefixTermsEnum(ter, prefix);
					if (termsEnum == null || termsEnum.next() == null) return;
					final Bits delDocs = reader.getLiveDocs();
					DocsEnum docsEnum = null;
					do {
						docsEnum = termsEnum.docs(delDocs, docsEnum, 0);
						String cterm = termsEnum.term().utf8ToString();
						while (true) {
							int d = docsEnum.nextDoc();
							if (d == DocIdSetIterator.NO_MORE_DOCS) break;
							pc.collect(base + d, cterm);
						}
					} while (termsEnum.next() != null);
				}
				base += LongLuceneDirectory.bsize;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
