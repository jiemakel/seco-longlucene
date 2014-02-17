package fi.seco.longlucene;

import java.io.IOException;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.lucene.index.SegmentInfos;

public class NoNormalMergePolicy extends LogByteSizeMergePolicy {

	@Override
	public MergeSpecification findMerges(MergeTrigger mergeTrigger, SegmentInfos arg0) throws CorruptIndexException, IOException {
		return null;
	}

}
