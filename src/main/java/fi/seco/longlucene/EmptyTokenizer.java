/**
 * 
 */
package fi.seco.longlucene;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Tokenizer;

/**
 * @author jiemakel
 * 
 */
public final class EmptyTokenizer extends Tokenizer {

	private static final StringReader sr = new StringReader("");

	public EmptyTokenizer() {
		super(sr);
	}

	@Override
	public final boolean incrementToken() throws IOException {
		return false;
	}

}
