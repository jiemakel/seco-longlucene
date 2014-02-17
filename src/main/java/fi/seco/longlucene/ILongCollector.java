package fi.seco.longlucene;

public interface ILongCollector {
	public void collect(long doc, NonExceptingScorer scorer);
}
