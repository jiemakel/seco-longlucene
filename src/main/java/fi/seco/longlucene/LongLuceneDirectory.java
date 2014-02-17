package fi.seco.longlucene;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LongLuceneDirectory implements ILongLuceneDirectory {

	private static final Logger log = LoggerFactory.getLogger(LongLuceneDirectory.class);

	private ILongIndexReader reader;
	private final ILongIndexWriter writer;

	public static final int bshift = 30;
	public static final int bsize = 1 << bshift;
	public static final int band = (bsize - 1);

	public static final FieldType pretokenizedField = new FieldType(StringField.TYPE_NOT_STORED);
	static {
		pretokenizedField.setTokenized(true);
		pretokenizedField.freeze();
		Codec.setDefault(Codec.forName("SecoBloomBlock46"));
		//Codec.setDefault(Codec.forName("SecoPulsing46"));
	}
	protected final Analyzer analyzer;

	public LongLuceneDirectory(String mpath, final Analyzer analyzer, boolean retainOrderInMerge, ExecutorService es) {
		writer = new LongIndexWriter(mpath, analyzer, retainOrderInMerge, es);
		this.analyzer = analyzer;
		reader = writer.getReader();
	}

	@Override
	public ILongIndexWriter writer() {
		return writer;
	}

	@Override
	public ILongIndexReader reader() {
		return reader;
	}

	@Override
	public Analyzer getAnalyzer() {
		return analyzer;
	}

	@Override
	public List<String> getTokens(String string) {
		List<String> ret = new ArrayList<String>();
		try {
			TokenStream stream = analyzer.tokenStream(null, new StringReader(string));
			stream.reset();
			CharTermAttribute term = stream.getAttribute(CharTermAttribute.class);
			while (stream.incrementToken())
				ret.add(term.toString());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	@Override
	public List<String> getMatches(Collection<String> labels, String query, boolean strict) {
		if (labels == null) return null;
		List<String> ret = new ArrayList<String>();
		if (!strict) {
			Set<String> tokens = new HashSet<String>(getTokens(query));
			for (String label : labels)
				for (String token : getTokens(label))
					if (tokens.contains(token)) ret.add(label);
		} else {
			Collection<String> tokens = getTokens(query);
			for (String label : labels)
				for (String token : getTokens(label))
					for (String t2 : tokens)
						if (token.startsWith(t2)) ret.add(label);
		}
		return ret;
	}

	@Override
	public void ensureFreshReader() {
		reader = writer.getReader();
	}

	private final Map<String, ThreadLocal<NonExceptingQueryParser>> qps = new HashMap<String, ThreadLocal<NonExceptingQueryParser>>();

	@Override
	public NonExceptingQueryParser getQueryParser(final String field) {
		ThreadLocal<NonExceptingQueryParser> dstl = qps.get(field);
		if (dstl == null) {
			dstl = new ThreadLocal<NonExceptingQueryParser>() {
				@Override
				protected NonExceptingQueryParser initialValue() {
					return new NonExceptingQueryParser(new QueryParser(Version.LUCENE_46, field, analyzer));
				};
			};
			qps.put(field, dstl);
		}
		return dstl.get();

	}

}
