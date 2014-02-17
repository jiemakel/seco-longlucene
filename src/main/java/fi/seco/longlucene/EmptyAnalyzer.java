/**
 * 
 */
package fi.seco.longlucene;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.EmptyTokenStream;

/**
 * @author jiemakel
 * 
 */
public class EmptyAnalyzer extends Analyzer {

	private static final TokenStreamComponents empty = new TokenStreamComponents(new EmptyTokenizer(), new EmptyTokenStream());

	@Override
	protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
		return empty;
	}

}
