/**
 * 
 */
package fi.seco.longlucene;

import java.io.IOException;

import org.apache.lucene.search.Scorer;

/**
 * @author jiemakel
 * 
 */
public class NonExceptingScorer {
	private final Scorer scorer;

	public NonExceptingScorer(Scorer scorer) {
		this.scorer = scorer;
	}

	public float score() {
		try {
			return scorer.score();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
