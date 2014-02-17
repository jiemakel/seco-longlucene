/**
 * 
 */
package fi.seco.longlucene;

import org.apache.lucene.codecs.Codec;
import org.apache.lucene.codecs.DocValuesFormat;
import org.apache.lucene.codecs.FieldInfosFormat;
import org.apache.lucene.codecs.LiveDocsFormat;
import org.apache.lucene.codecs.NormsFormat;
import org.apache.lucene.codecs.PostingsFormat;
import org.apache.lucene.codecs.SegmentInfoFormat;
import org.apache.lucene.codecs.StoredFieldsFormat;
import org.apache.lucene.codecs.TermVectorsFormat;
import org.apache.lucene.codecs.lucene45.Lucene45DocValuesFormat;
import org.apache.lucene.codecs.lucene46.Lucene46FieldInfosFormat;
import org.apache.lucene.codecs.lucene40.Lucene40LiveDocsFormat;
import org.apache.lucene.codecs.lucene42.Lucene42NormsFormat;
import org.apache.lucene.codecs.lucene46.Lucene46SegmentInfoFormat;
import org.apache.lucene.codecs.lucene42.Lucene42TermVectorsFormat;
import org.apache.lucene.codecs.lucene41.Lucene41StoredFieldsFormat;
import org.apache.lucene.codecs.pulsing.Pulsing41PostingsFormat;

/**
 * @author jiemakel
 * 
 */
public class SecoPulsing46Codec extends Codec {

	private final PostingsFormat postingsFormat = new Pulsing41PostingsFormat(5);

	private final StoredFieldsFormat fieldsFormat = new Lucene41StoredFieldsFormat();
	private final TermVectorsFormat vectorsFormat = new Lucene42TermVectorsFormat();
	private final FieldInfosFormat fieldInfosFormat = new Lucene46FieldInfosFormat();
	private final DocValuesFormat docValuesFormat = new Lucene45DocValuesFormat();
	private final SegmentInfoFormat infosFormat = new Lucene46SegmentInfoFormat();
	private final NormsFormat normsFormat = new Lucene42NormsFormat();
	private final LiveDocsFormat liveDocsFormat = new Lucene40LiveDocsFormat();

	public SecoPulsing46Codec() {
		super("SecoPulsing46");
	}

	@Override
	public StoredFieldsFormat storedFieldsFormat() {
		return fieldsFormat;
	}

	@Override
	public TermVectorsFormat termVectorsFormat() {
		return vectorsFormat;
	}

	@Override
	public DocValuesFormat docValuesFormat() {
		return docValuesFormat;
	}

	@Override
	public PostingsFormat postingsFormat() {
		return postingsFormat;
	}

	@Override
	public FieldInfosFormat fieldInfosFormat() {
		return fieldInfosFormat;
	}

	@Override
	public SegmentInfoFormat segmentInfoFormat() {
		return infosFormat;
	}

	@Override
	public NormsFormat normsFormat() {
		return normsFormat;
	}

	@Override
	public LiveDocsFormat liveDocsFormat() {
		return liveDocsFormat;
	}

}
