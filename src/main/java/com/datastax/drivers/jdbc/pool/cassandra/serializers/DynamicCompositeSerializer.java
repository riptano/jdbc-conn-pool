/**
 * 
 */
package com.datastax.drivers.jdbc.pool.cassandra.serializers;

import static com.datastax.drivers.jdbc.pool.cassandra.ddl.ComparatorType.DYNAMICCOMPOSITETYPE;

import java.nio.ByteBuffer;

import com.datastax.drivers.jdbc.pool.cassandra.ddl.ComparatorType;


/**
 * @author Todd Nine
 * 
 */
public class DynamicCompositeSerializer extends
		AbstractSerializer<DynamicComposite> {

	private static final DynamicCompositeSerializer instance = new DynamicCompositeSerializer();

	public static DynamicCompositeSerializer get() {
		return instance;
	}

	@Override
	public ByteBuffer toByteBuffer(DynamicComposite obj) {

		return obj.serialize();
	}

	@Override
	public DynamicComposite fromByteBuffer(ByteBuffer byteBuffer) {

		return DynamicComposite.fromByteBuffer(byteBuffer);

	}

	@Override
	public ComparatorType getComparatorType() {
		return DYNAMICCOMPOSITETYPE;
	}

}
