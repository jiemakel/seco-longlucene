package fi.seco.longlucene;

import java.io.IOException;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.BytesRef;

public final class EmptyTokenStream extends TokenStream {

	private static final EmptyTokenStream instance = new EmptyTokenStream();

	private EmptyTokenStream() {
		super(new EmptyAttributeFactory());
		addAttribute(TermToBytesRefAttribute.class);
	}

	public static final EmptyTokenStream getInstance() {
		return instance;
	}

	@Override
	public boolean incrementToken() throws IOException {
		return false;
	}

	private static final class EmptyAttributeFactory extends AttributeFactory {
		private final EmptyAttribute i = new EmptyAttribute();

		public EmptyAttributeFactory() {}

		@Override
		public AttributeImpl createAttributeInstance(Class<? extends Attribute> attClass) {
			if (attClass.equals(TermToBytesRefAttribute.class)) return i;
			return AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY.createAttributeInstance(attClass);
		}

	}

	private static final class EmptyAttribute extends AttributeImpl implements TermToBytesRefAttribute {

		private final BytesRef termBytes = new BytesRef();

		@Override
		public int fillBytesRef() {
			return 0;
		}

		@Override
		public BytesRef getBytesRef() {
			return termBytes;
		}

		@Override
		public void clear() {}

		@Override
		public void copyTo(AttributeImpl target) {}

	}
}
