/**
 * 
 */
package com.datastax.drivers.jdbc.pool.cassandra.serializers;

import static com.datastax.drivers.jdbc.pool.cassandra.ddl.ComparatorType.COMPOSITETYPE;

import java.nio.ByteBuffer;

import com.datastax.drivers.jdbc.pool.cassandra.ddl.ComparatorType;


/**
 * @author Todd Nine
 * 
 */
public class CompositeSerializer extends AbstractSerializer<Composite> {

  @Override
  public ByteBuffer toByteBuffer(Composite obj) {

    return obj.serialize();
  }

  @Override
  public Composite fromByteBuffer(ByteBuffer byteBuffer) {

    Composite composite = new Composite();
    composite.deserialize(byteBuffer);

    return composite;

  }

  @Override
  public ComparatorType getComparatorType() {
    return COMPOSITETYPE;
  }

}
