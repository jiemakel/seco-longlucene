package fi.seco.longlucene;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.Version;
import org.junit.Test;

public class TestAttributeCloningCachingTokenFilter {

	@Test
	public void testCaching() throws IOException {
		TokenStream f1 = new StandardAnalyzer(Version.LUCENE_46).tokenStream("foo", new StringReader("testing testing, what will come out of this?"));
		TokenStream t1 = AttributeCloningCachingTokenFilter.getCachedTokenStream(f1);
		TokenStream t2 = AttributeCloningCachingTokenFilter.getCachedTokenStream(t1);
		try {
			while (t1.incrementToken()) {
				Iterator<AttributeImpl> ai1 = t1.getAttributeImplsIterator();
				Iterator<AttributeImpl> ai2 = t2.getAttributeImplsIterator();
				while (ai1.hasNext()) {
					AttributeImpl cai = ai1.next();
					AttributeImpl cai2 = ai2.next();
					if (cai == cai2) fail("Attribute has been already used!");
				}
			}
		} catch (IOException e) {

		}
	}

}
