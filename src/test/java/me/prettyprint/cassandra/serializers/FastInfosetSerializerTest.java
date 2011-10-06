package me.prettyprint.cassandra.serializers;

import com.datastax.drivers.jdbc.pool.cassandra.Serializer;
import com.datastax.drivers.jdbc.pool.cassandra.serializers.FastInfosetSerializer;

/**
 * @author shuzhang0@gmail.com
 * 
 */
public class FastInfosetSerializerTest extends JaxbSerializerTest {

  @Override
  protected Serializer<Object> getSerializer() {
    return new FastInfosetSerializer(JaxbString.class);
  }

}
