package fi.seco.longlucene;

import java.util.List;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

public interface ILongIndexReader {

	public long numDocs();

	public long numDeletedDocs();

	public void search(Query query, ILongCollectorFactory factory);

	public ILongBinaryDocValues getBinaryDocValues(String field);

	public ILongNumericDocValues getNumericDocValues(String field);

	public long docFreq(Term t);

	public List<AtomicReaderContext>[] getAtomicReaderContexts();

	public void setParallel(boolean parallel);

	public void runPrefixQueries(String field, String text, final ILongPrefixCollector pc);

	public static interface ILongPrefixCollector {
		public void collect(long doc, String currentKeyword);
	}

	public Document document(long doc);

	public static interface ILongCollectorFactory {
		public ILongCollector get();
	}

}
