package fi.seco.longlucene;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.BytesRef;

import fi.seco.util.ByteArrayLongUtil;

public final class SingleThreeLongsTokenStream extends TokenStream {

	public SingleThreeLongsTokenStream(long s, long p, long o) {
		super(new SingleThreeLongsAttributeFactory(s, p, o));
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

	private static final class SingleThreeLongsAttributeFactory extends AttributeFactory {
		private final SingleThreeLongsAttribute i;

		public SingleThreeLongsAttributeFactory(long s, long p, long o) {
			this.i = new SingleThreeLongsAttribute(s, p, o);
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

	private static final class SingleThreeLongsAttribute extends AttributeImpl implements TermToBytesRefAttribute {

		private final BytesRef termBytes;
		private final long s;
		private final long p;
		private final long o;

		public SingleThreeLongsAttribute(long s, long p, long o) {
			this.s = s;
			this.p = p;
			this.o = o;
			byte[] bb1 = new byte[24];
			ByteArrayLongUtil.longToByteArray(s, bb1);
			ByteArrayLongUtil.longToByteArray(p, bb1, 8);
			ByteArrayLongUtil.longToByteArray(o, bb1, 16);
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
			return (int) ((s ^ (s >>> 32)) + 3 * (p ^ (p >>> 32)) + 7 * (o ^ (o >>> 32)));
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof SingleThreeLongsAttribute)) return false;
			return s == ((SingleThreeLongsAttribute) other).s && p == ((SingleThreeLongsAttribute) other).p && o == ((SingleThreeLongsAttribute) other).o;
		}

		@Override
		public void copyTo(AttributeImpl target) {
			throw new UnsupportedOperationException("TermToBytesRef cannot be copied");
		}

		@Override
		public String toString() {
			return s + "," + p + "," + o;
		}

	}

}
