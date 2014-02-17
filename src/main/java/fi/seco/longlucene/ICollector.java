package fi.seco.longlucene;

public interface ICollector {
	public void collect(int doc, NonExceptingScorer scorer);
}
