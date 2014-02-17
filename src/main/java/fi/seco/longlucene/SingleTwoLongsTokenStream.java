package fi.seco.longlucene;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.BytesRef;

import fi.seco.util.ByteArrayLongUtil;

public final class SingleTwoLongsTokenStream extends TokenStream {

	public SingleTwoLongsTokenStream(long s, long p) {
		super(new SingleTwoLongsAttributeFactory(s, p));
		addAttribute(TermToBytesRefAttribute.class);
	}

	private boolean advanced = false;

	@Override
	public final boolean incrementToken() throws IOException {
		if (!advanced) {
			advanced = true;
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return getAttributeFactory().toString();
	}

	private static final class SingleTwoLongsAttributeFactory extends AttributeFactory {
		private final SingleTwoLongsAttribute i;

		public SingleTwoLongsAttributeFactory(long s, long p) {
			this.i = new SingleTwoLongsAttribute(s, p);
		}

		@Override
		public AttributeImpl createAttributeInstance(Class<? extends Attribute> attClass) {
			if (attClass.equals(TermToBytesRefAttribute.class)) return i;
			return AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY.createAttributeInstance(attClass);
		}

		@Override
		public String toString() {
			return i.toString();
		}
	}

	private static final class SingleTwoLongsAttribute extends AttributeImpl implements TermToBytesRefAttribute {

		private final BytesRef termBytes;
		private final long s;
		private final long p;

		public SingleTwoLongsAttribute(long s, long p) {
			this.s = s;
			this.p = p;
			byte[] bb1 = new byte[16];
			ByteArrayLongUtil.longToByteArray(s, bb1);
			ByteArrayLongUtil.longToByteArray(p, bb1, 8);
			termBytes = new BytesRef(bb1);
		}

		@Override
		public int fillBytesRef() {
			return termBytes.hashCode();
		}

		@Override
		public BytesRef getBytesRef() {
			return termBytes;
		}

		@Override
		public void clear() {}

		@Override
		public int hashCode() {
			return (int) ((s ^ (s >>> 32)) + 3 * (p ^ (p >>> 32)));
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof SingleTwoLongsAttribute)) return false;
			return s == ((SingleTwoLongsAttribute) other).s && p == ((SingleTwoLongsAttribute) other).p;
		}

		@Override
		public void copyTo(AttributeImpl target) {
			throw new UnsupportedOperationException("TermToBytesRef cannot be copied");
		}

		@Override
		public String toString() {
			return s + "," + p;
		}

	}

}
