package com.datastax.drivers.jdbc.pool.cassandra.jdbc;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.datastax.drivers.jdbc.pool.cassandra.BaseEmbededServerSetupTest;


public class HCQLDataSourceTest extends BaseEmbededServerSetupTest {

  private final static String cassandraUrl = "localhost:9170";


  private HCQLDataSource factory;

  @Before
  public void setupCase() throws Exception {
    factory = new HCQLDataSource();
  }

  @After
  public void teardownCase() throws IOException {
    factory = null;
  }

  @Test
  public void getObjectInstance() throws Exception {
    Reference resource = new Reference("HCQLDataSource");

    resource.add(new StringRefAddr("hosts", cassandraUrl));
    resource.add(new StringRefAddr("clusterName", clusterName));
    resource.add(new StringRefAddr("keyspace", "Keyspace1"));
    resource.add(new StringRefAddr("user", ""));
    resource.add(new StringRefAddr("password", ""));
    

    Name jndiName = mock(Name.class);
    Context context = new InitialContext();
    Hashtable<String, String> environment = new Hashtable<String, String>();

    DataSource dataSource = (DataSource) factory.getObjectInstance(resource, jndiName, context, environment);

    assertNotNull(dataSource);
    assertNotNull(dataSource.getConnection());
  }
}
