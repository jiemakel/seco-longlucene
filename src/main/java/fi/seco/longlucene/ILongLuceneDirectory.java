package fi.seco.longlucene;

import java.util.Collection;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;

/**
 * A wrapper wrapping multiple Lucene directories so that more than
 * Integer.MAX_VALUE documents may be indexed and queried.
 * 
 * @author jiemakel
 * 
 */
public interface ILongLuceneDirectory {

	public ILongIndexWriter writer();

	public ILongIndexReader reader();

	public Analyzer getAnalyzer();

	public List<String> getTokens(String string);

	public List<String> getMatches(Collection<String> labels, String query, boolean strict);

	public void ensureFreshReader();

	public NonExceptingQueryParser getQueryParser(String field);

}