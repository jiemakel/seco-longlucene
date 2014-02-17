package fi.seco.longlucene;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.BytesRef;

public final class SingleBytesRefTokenStream extends TokenStream {

	public SingleBytesRefTokenStream(BytesRef b) {
		super(new SingleByteArrayAttributeFactory(b));
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

	private static final class SingleByteArrayAttributeFactory extends AttributeFactory {
		private final SingleBytesRefAttribute i;

		public SingleByteArrayAttributeFactory(BytesRef b) {
			this.i = new SingleBytesRefAttribute(b);
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

	private static final class SingleBytesRefAttribute extends AttributeImpl implements TermToBytesRefAttribute {

		private final BytesRef termBytes;

		public SingleBytesRefAttribute(BytesRef termBytes) {
			this.termBytes = termBytes;
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
			return termBytes.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof SingleBytesRefAttribute)) return false;
			return termBytes.equals(((SingleBytesRefAttribute) other).termBytes);
		}

		@Override
		public void copyTo(AttributeImpl target) {
			throw new UnsupportedOperationException("TermToBytesRef cannot be copied");
		}

		@Override
		public String toString() {
			return termBytes.toString();
		}

	}

}
