package fi.seco.longlucene;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.BytesRef;

import fi.seco.util.ByteArrayLongUtil;

public final class SingleLongTokenStream extends TokenStream {

	public SingleLongTokenStream(long i) {
		super(new SingleLongAttributeFactory(i));
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

	private static final class SingleLongAttributeFactory extends AttributeFactory {
		private final SingleLongAttribute i;

		public SingleLongAttributeFactory(long i) {
			this.i = new SingleLongAttribute(i);
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

	private static final class SingleLongAttribute extends AttributeImpl implements TermToBytesRefAttribute {

		private final BytesRef termBytes;
		private final long i;

		public SingleLongAttribute(long i) {
			this.i = i;
			byte[] bb1 = new byte[8];
			ByteArrayLongUtil.longToByteArray(i, bb1);
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
			return (int) (i ^ (i >>> 32));
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof SingleLongAttribute)) return false;
			return i == ((SingleLongAttribute) other).i;
		}

		@Override
		public void copyTo(AttributeImpl target) {
			throw new UnsupportedOperationException("TermToBytesRef cannot be copied");
		}

		@Override
		public String toString() {
			return Long.toString(i);
		}

	}

}
