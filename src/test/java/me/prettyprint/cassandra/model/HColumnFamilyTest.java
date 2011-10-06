package me.prettyprint.cassandra.model;

import static com.datastax.drivers.jdbc.pool.cassandra.factory.HFactory.createKeyspace;
import static com.datastax.drivers.jdbc.pool.cassandra.factory.HFactory.getOrCreateCluster;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.NoSuchElementException;
import java.util.UUID;


import org.junit.Before;
import org.junit.Test;

import com.datastax.drivers.jdbc.pool.cassandra.BaseEmbededServerSetupTest;
import com.datastax.drivers.jdbc.pool.cassandra.Keyspace;
import com.datastax.drivers.jdbc.pool.cassandra.connection.Cluster;
import com.datastax.drivers.jdbc.pool.cassandra.factory.HFactory;
import com.datastax.drivers.jdbc.pool.cassandra.serializers.IntegerSerializer;
import com.datastax.drivers.jdbc.pool.cassandra.serializers.LongSerializer;
import com.datastax.drivers.jdbc.pool.cassandra.serializers.StringSerializer;
import com.datastax.drivers.jdbc.pool.cassandra.serializers.UUIDSerializer;
import com.datastax.drivers.jdbc.pool.cassandra.service.HColumnFamilyImpl;
import com.datastax.drivers.jdbc.pool.cassandra.utils.TimeUUIDUtils;
import com.datastax.drivers.jdbc.pool.hector.api.HColumnFamily;
import com.datastax.drivers.jdbc.pool.hector.api.mutation.Mutator;


public class HColumnFamilyTest extends BaseEmbededServerSetupTest {
  
  private Keyspace keyspace;
  private UUID timeUUID;
  @Before
  public void setupLocal() {
    //setupClient();
    Cluster cluster = getOrCreateCluster("MyCluster", "127.0.0.1:9170");
    keyspace = createKeyspace("Keyspace1", cluster);
    
    Mutator<String> mutator = HFactory.createMutator(keyspace, StringSerializer.get());
    mutator.addInsertion("zznate", "Standard1", HFactory.createStringColumn("email", "nate@datastax.com"));
    mutator.addInsertion("zznate", "Standard1", HFactory.createColumn("int", 1, StringSerializer.get(), IntegerSerializer.get()));
    mutator.addInsertion("zznate", "Standard1", HFactory.createColumn("long", 1L, StringSerializer.get(), LongSerializer.get()));
    timeUUID = TimeUUIDUtils.getTimeUUID(System.currentTimeMillis());
    mutator.addInsertion("zznate", "Standard1", HFactory.createColumn("uuid", timeUUID, StringSerializer.get(), UUIDSerializer.get()));
    mutator.execute();
  }
  
  @Test
  public void testColumnFamilySetup() {
    HColumnFamily<String, String> columnFamily = new HColumnFamilyImpl<String,String>(keyspace, "Standard1",StringSerializer.get(), StringSerializer.get());
    columnFamily.addKey("zznate");
    // columnFamily.loadSlice(15); columnFamily.addKey().loadSlice() ~ def. 100
    assertEquals(1,columnFamily.getInt("int"));
    assertEquals("nate@datastax.com",columnFamily.getString("email"));
    assertEquals(1L, columnFamily.getLong("long"));
    assertEquals(timeUUID, columnFamily.getUUID("uuid"));

  }
  
  @Test
  public void testColumnFamilyReadahead() {
    HColumnFamily<String, String> columnFamily = new HColumnFamilyImpl<String,String>(keyspace, "Standard1",StringSerializer.get(), StringSerializer.get());
    columnFamily.addKey("zznate").setCount(10);
    assertEquals(4,columnFamily.getColumns().size());
    assertEquals(1,columnFamily.getInt("int"));
    assertEquals("nate@datastax.com",columnFamily.getString("email"));
    assertEquals(1L, columnFamily.getLong("long"));
    assertEquals(timeUUID, columnFamily.getUUID("uuid"));

  }
  
  @Test
  public void testClearAndRecall() {
    HColumnFamily<String, String> columnFamily = new HColumnFamilyImpl<String,String>(keyspace, "Standard1",StringSerializer.get(), StringSerializer.get());
    columnFamily.addKey("zznate").setCount(10);
    assertEquals(4,columnFamily.getColumns().size());
    assertEquals(1,columnFamily.getInt("int"));
    assertEquals("nate@datastax.com",columnFamily.getString("email"));
    assertEquals(1L, columnFamily.getLong("long"));
    assertEquals(timeUUID, columnFamily.getUUID("uuid"));
    columnFamily.clear();
    assertNull(columnFamily.getUUID("uuid"));
    assertEquals(4,columnFamily.getColumns().size());
    assertEquals(timeUUID, columnFamily.getUUID("uuid"));
  }
  
  @Test
  public void testToggleMultiget() {
    Mutator<String> mutator = HFactory.createMutator(keyspace, StringSerializer.get());
    mutator.addInsertion("patricioe", "Standard1", HFactory.createStringColumn("email", "patricioe@datastax.com"));
    mutator.addInsertion("patricioe", "Standard1", HFactory.createColumn("int", 2, StringSerializer.get(), IntegerSerializer.get()));
    mutator.addInsertion("patricioe", "Standard1", HFactory.createColumn("long", 2L, StringSerializer.get(), LongSerializer.get()));
    timeUUID = TimeUUIDUtils.getTimeUUID(System.currentTimeMillis());
    mutator.addInsertion("patricioe", "Standard1", HFactory.createColumn("uuid", timeUUID, StringSerializer.get(), UUIDSerializer.get()));
    mutator.execute();
    
    HColumnFamilyImpl<String, String> columnFamily = new HColumnFamilyImpl<String,String>(keyspace, "Standard1",StringSerializer.get(), StringSerializer.get());
    columnFamily.addKey("zznate").addKey("patricioe").setCount(10);
    assertEquals("nate@datastax.com",columnFamily.getString("email"));
    assertTrue(columnFamily.hasNext());
    columnFamily.next();
    assertEquals("patricioe@datastax.com",columnFamily.getString("email"));
    assertFalse(columnFamily.hasNext());
    try {
      columnFamily.next();
      fail();
    } catch (NoSuchElementException nsee) {
      assertNotNull(nsee);
    }
  }
}
