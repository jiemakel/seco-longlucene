package fi.seco.longlucene;

import java.util.Locale;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DateTools.Resolution;
import org.apache.lucene.queryparser.classic.CharStream;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.queryparser.classic.QueryParserTokenManager;
import org.apache.lucene.queryparser.classic.Token;
import org.apache.lucene.search.MultiTermQuery.RewriteMethod;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

public class NonExceptingQueryParser {

	private final QueryParser qp;

	public NonExceptingQueryParser(QueryParser qp) {
		this.qp = qp;
	}

	@Override
	public int hashCode() {
		return qp.hashCode();
	}

	public void init(Version matchVersion, String f, Analyzer a) {
		qp.init(matchVersion, f, a);
	}

	@Override
	public boolean equals(Object obj) {
		return qp.equals(obj);
	}

	public final int Conjunction() {
		try {
			return qp.Conjunction();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public Query parse(String query) {
		try {
			return qp.parse(query);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public final int Modifiers() {
		try {
			return qp.Modifiers();
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public Analyzer getAnalyzer() {
		return qp.getAnalyzer();
	}

	public String getField() {
		return qp.getField();
	}

	public final Query TopLevelQuery(String field) {
		try {
			return qp.TopLevelQuery(field);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public final boolean getAutoGeneratePhraseQueries() {
		return qp.getAutoGeneratePhraseQueries();
	}

	public final void setAutoGeneratePhraseQueries(boolean value) {
		qp.setAutoGeneratePhraseQueries(value);
	}

	public final Query Query(String field) {
		try {
			return qp.Query(field);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	public float getFuzzyMinSim() {
		return qp.getFuzzyMinSim();
	}

	public void setFuzzyMinSim(float fuzzyMinSim) {
		qp.setFuzzyMinSim(fuzzyMinSim);
	}

	public int getFuzzyPrefixLength() {
		return qp.getFuzzyPrefixLength();
	}

	public void setFuzzyPrefixLength(int fuzzyPrefixLength) {
		qp.setFuzzyPrefixLength(fuzzyPrefixLength);
	}

	public final Query Clause(String field) {
		try {
			return qp.Clause(field);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

	}

	public int getPhraseSlop() {
		return qp.getPhraseSlop();
	}

	public void setAllowLeadingWildcard(boolean allowLeadingWildcard) {
		qp.setAllowLeadingWildcard(allowLeadingWildcard);
	}

	public boolean getAllowLeadingWildcard() {
		return qp.getAllowLeadingWildcard();
	}

	public void setEnablePositionIncrements(boolean enable) {
		qp.setEnablePositionIncrements(enable);
	}

	public boolean getEnablePositionIncrements() {
		return qp.getEnablePositionIncrements();
	}

	public void setDefaultOperator(Operator op) {
		qp.setDefaultOperator(op);
	}

	public Operator getDefaultOperator() {
		return qp.getDefaultOperator();
	}

	public void setLowercaseExpandedTerms(boolean lowercaseExpandedTerms) {
		qp.setLowercaseExpandedTerms(lowercaseExpandedTerms);
	}

	public boolean getLowercaseExpandedTerms() {
		return qp.getLowercaseExpandedTerms();
	}

	public RewriteMethod getMultiTermRewriteMethod() {
		return qp.getMultiTermRewriteMethod();
	}

	public void setLocale(Locale locale) {
		qp.setLocale(locale);
	}

	public Locale getLocale() {
		return qp.getLocale();
	}

	public void setDateResolution(Resolution dateResolution) {
		qp.setDateResolution(dateResolution);
	}

	public void setDateResolution(String fieldName, Resolution dateResolution) {
		qp.setDateResolution(fieldName, dateResolution);
	}

	public Resolution getDateResolution(String fieldName) {
		return qp.getDateResolution(fieldName);
	}

	public void setAnalyzeRangeTerms(boolean analyzeRangeTerms) {
		qp.setAnalyzeRangeTerms(analyzeRangeTerms);
	}

	public boolean getAnalyzeRangeTerms() {
		return qp.getAnalyzeRangeTerms();
	}

	public void ReInit(CharStream stream) {
		qp.ReInit(stream);
	}

	public void ReInit(QueryParserTokenManager tm) {
		qp.ReInit(tm);
	}

	public final Token getNextToken() {
		return qp.getNextToken();
	}

	public final Token getToken(int index) {
		return qp.getToken(index);
	}

	public ParseException generateParseException() {
		return qp.generateParseException();
	}

	public final void enable_tracing() {
		qp.enable_tracing();
	}

	public final void disable_tracing() {
		qp.disable_tracing();
	}

	public void setMultiTermRewriteMethod(RewriteMethod method) {
		qp.setMultiTermRewriteMethod(method);
	}

	public void setPhraseSlop(int phraseSlop) {
		qp.setPhraseSlop(phraseSlop);
	}

	public final Query Term(String field) {
		try {
			return qp.Term(field);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		return qp.toString();
	}
}
