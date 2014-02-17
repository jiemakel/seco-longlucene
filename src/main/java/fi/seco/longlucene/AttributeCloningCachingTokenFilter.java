package fi.seco.longlucene;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.util.AttributeSource;

public final class AttributeCloningCachingTokenFilter extends TokenStream {
	private final ArrayList<AttributeSource.State> cache = new ArrayList<AttributeSource.State>();
	private Iterator<AttributeSource.State> iterator;
	private AttributeSource.State finalState;

	public AttributeCloningCachingTokenFilter(TokenStream input) {
		super(input.cloneAttributes());
		try {
			input.reset();
			while (input.incrementToken())
				cache.add(input.captureState());
			// capture final state
			input.end();
			finalState = captureState();
			input.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		cache.trimToSize();
		iterator = cache.iterator();
	}

	@Override
	public final boolean incrementToken() throws IOException {
		if (!iterator.hasNext()) // the cache is exhausted, return false
			return false;
		// Since the TokenFilter can be reset, the tokens need to be preserved as immutable.
		restoreState(iterator.next());
		return true;
	}

	@Override
	public final void end() throws IOException {
		if (finalState != null) restoreState(finalState);
	}

	@Override
	public void reset() throws IOException {
		if (cache != null) iterator = cache.iterator();
	}

	public static AttributeCloningCachingTokenFilter getCachedTokenStream(TokenStream tt2) {
		synchronized (tt2) {
			return new AttributeCloningCachingTokenFilter(tt2);
		}
	}

}
