package fi.seco.longlucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;

public interface ILongIndexWriter {

	public void addDocument(Document document);

	public ILongIndexReader getReader();

	public void deleteDocuments(Term term);

	/**
	 * Delete documents by query
	 * 
	 * @param query
	 *            the query to delete by
	 */
	public void deleteDocuments(Query query);

	/**
	 * Delete documents by terms
	 * 
	 * @param terms
	 *            terms to delete by
	 */
	public void deleteDocuments(Term... terms);

	/**
	 * Delete documents by queries
	 * 
	 * @param queries
	 *            queries to delete by
	 */
	public void deleteDocuments(Query... queries);

	public void clearIndex();

	public void close();

	/**
	 * @return maximum document id + 1;
	 */
	public long maxDoc();

	/**
	 * Commit modifications to disk
	 */
	public void commit();

}
