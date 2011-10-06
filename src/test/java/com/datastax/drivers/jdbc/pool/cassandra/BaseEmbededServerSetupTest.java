package com.datastax.drivers.jdbc.pool.cassandra;

import java.io.IOException;

import me.prettyprint.hector.testutils.EmbeddedServerHelper;

import org.apache.cassandra.config.ConfigurationException;
import org.apache.thrift.transport.TTransportException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.drivers.jdbc.pool.cassandra.connection.CassandraHostConfigurator;
import com.datastax.drivers.jdbc.pool.cassandra.connection.HConnectionManager;

/**
 * Base class for test cases that need access to EmbeddedServerHelper
 *
 */
public abstract class BaseEmbededServerSetupTest {
	
  private static Logger log = LoggerFactory.getLogger(BaseEmbededServerSetupTest.class);
  private static EmbeddedServerHelper embedded;

  protected HConnectionManager connectionManager;
  protected CassandraHostConfigurator cassandraHostConfigurator;
  protected String clusterName = "TestCluster";

  /**
   * Set embedded cassandra up and spawn it in a new thread.
   *
   * @throws TTransportException
   * @throws IOException
   * @throws InterruptedException
   */
  @BeforeClass
  public static void beforeClassSetup() throws TTransportException, IOException, InterruptedException, ConfigurationException {
    log.info("in setup of BaseEmbedded.Test");
    embedded = new EmbeddedServerHelper();
    embedded.setup();
  }

  @AfterClass
  public static void teardown() throws IOException {
    embedded.teardown();
    embedded = null;
  }

  protected void setupClient() {
    cassandraHostConfigurator = new CassandraHostConfigurator("127.0.0.1:9170");
    connectionManager = new HConnectionManager(clusterName, cassandraHostConfigurator);
  }

}
