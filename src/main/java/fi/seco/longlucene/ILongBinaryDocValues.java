/**
 * 
 */
package fi.seco.longlucene;

import org.apache.lucene.util.BytesRef;

/**
 * @author jiemakel
 * 
 */
public interface ILongBinaryDocValues {
	public void get(long docId, BytesRef result);
}
