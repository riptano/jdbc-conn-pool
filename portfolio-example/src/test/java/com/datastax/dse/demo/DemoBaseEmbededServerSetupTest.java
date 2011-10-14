package com.datastax.dse.demo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.prettyprint.hector.testutils.EmbeddedServerHelper;

import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.KSMetaData;
import org.apache.cassandra.config.Schema;
import org.apache.cassandra.db.ColumnFamilyType;
import org.apache.cassandra.db.marshal.DoubleType;
import org.apache.cassandra.db.marshal.LongType;
import org.apache.cassandra.db.marshal.UTF8Type;
import org.apache.cassandra.locator.SimpleStrategy;
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
public abstract class DemoBaseEmbededServerSetupTest {
	
  private static Logger log = LoggerFactory.getLogger(DemoBaseEmbededServerSetupTest.class);
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
    loadSchema();
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

  
  private static void loadSchema() {
    List<KSMetaData> schema = new ArrayList<KSMetaData>();
    Map<String, String> opts = new HashMap<String, String>();
    opts.put("replication_factor", Integer.toString(1));
    
    CFMetaData cfmd = new CFMetaData("PortfolioDemo", "Portfolios", ColumnFamilyType.Standard, UTF8Type.instance, null)
      .keyValidator(UTF8Type.instance)
      .defaultValidator(LongType.instance);
    
    CFMetaData stocks = new CFMetaData("PortfolioDemo", "Stocks", ColumnFamilyType.Standard, UTF8Type.instance, null)
    .keyValidator(UTF8Type.instance)
    .defaultValidator(DoubleType.instance);
    
    CFMetaData stockHist = new CFMetaData("PortfolioDemo", "StockHist", ColumnFamilyType.Standard, UTF8Type.instance, null)
    .keyValidator(UTF8Type.instance)
    .defaultValidator(DoubleType.instance);
    
    CFMetaData histLoss = new CFMetaData("PortfolioDemo", "HistLoss", ColumnFamilyType.Standard, UTF8Type.instance, null)
    .keyValidator(UTF8Type.instance)
    .defaultValidator(UTF8Type.instance);
    
    KSMetaData ks = new KSMetaData("PortfolioDemo", SimpleStrategy.class, opts, cfmd, stocks, stockHist, histLoss);
     
    Schema.instance.load(Arrays.asList(ks), Schema.instance.getVersion());
    
  }
}
