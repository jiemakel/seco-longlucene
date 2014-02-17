/**
 * 
 */
package fi.seco.longlucene;

import fi.seco.longlucene.ILongIndexReader.ILongCollectorFactory;

/**
 * @author jiemakel
 * 
 */
public class SingletonLongCollectorFactory implements ILongCollectorFactory {

	private final ILongCollector singleton;

	public SingletonLongCollectorFactory(ILongCollector singleton) {
		this.singleton = singleton;
	}

	@Override
	public ILongCollector get() {
		return singleton;
	}

}
