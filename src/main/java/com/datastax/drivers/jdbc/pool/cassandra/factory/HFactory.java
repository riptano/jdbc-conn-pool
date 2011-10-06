package com.datastax.drivers.jdbc.pool.cassandra.factory;

import java.util.HashMap;
import java.util.Map;

import com.datastax.drivers.jdbc.pool.cassandra.connection.CassandraHostConfigurator;
import com.datastax.drivers.jdbc.pool.cassandra.connection.Cluster;
import com.datastax.drivers.jdbc.pool.cassandra.connection.ThriftCluster;
import com.datastax.drivers.jdbc.pool.cassandra.model.ConsistencyLevelPolicy;
import com.datastax.drivers.jdbc.pool.cassandra.model.QuorumAllConsistencyLevelPolicy;



/**
 * A convenience class with bunch of factory static methods to help create a
 * mutator, queries etc.
 * 
 * @author Ran
 * @author zznate
 */
public final class HFactory {

  private static final Map<String, Cluster> clusters = new HashMap<String, Cluster>();

  private static final ConsistencyLevelPolicy DEFAULT_CONSISTENCY_LEVEL_POLICY = new QuorumAllConsistencyLevelPolicy();

  public static Cluster getCluster(String clusterName) {
    synchronized (clusters) {
      return clusters.get(clusterName);
    }
  }

  /**
   * Method tries to create a Cluster instance for an existing Cassandra
   * cluster. If another class already called getOrCreateCluster, the factory
   * returns the cached instance. If the instance doesn't exist in memory, a new
   * ThriftCluster is created and cached.
   * 
   * Example usage for a default installation of Cassandra.
   * 
   * String clusterName = "Test Cluster"; String host = "localhost:9160";
   * Cluster cluster = HFactory.getOrCreateCluster(clusterName, host);
   * 
   * Note the host should be the hostname and port number. It is preferable to
   * use the hostname instead of the IP address.
   * 
   * @param clusterName
   *          The cluster name. This is an identifying string for the cluster,
   *          e.g. "production" or "test" etc. Clusters will be created on
   *          demand per each unique clusterName key.
   * @param hostIp
   *          host:ip format string
   * @return
   */
  public static Cluster getOrCreateCluster(String clusterName, String hostIp) {
    return getOrCreateCluster(clusterName,
        new CassandraHostConfigurator(hostIp));
  }

  /**
   * Method tries to create a Cluster instance for an existing Cassandra
   * cluster. If another class already called getOrCreateCluster, the factory
   * returns the cached instance. If the instance doesn't exist in memory, a new
   * ThriftCluster is created and cached.
   * 
   * Example usage for a default installation of Cassandra.
   * 
   * String clusterName = "Test Cluster"; String host = "localhost:9160";
   * Cluster cluster = HFactory.getOrCreateCluster(clusterName, new
   * CassandraHostConfigurator(host));
   * 
   * @param clusterName
   *          The cluster name. This is an identifying string for the cluster,
   *          e.g. "production" or "test" etc. Clusters will be created on
   *          demand per each unique clusterName key.
   * @param cassandraHostConfigurator
   */
  public static Cluster getOrCreateCluster(String clusterName,
      CassandraHostConfigurator cassandraHostConfigurator) {
    synchronized (clusters) {
      Cluster c = clusters.get(clusterName);
      if (c == null) {
        c = createCluster(clusterName, cassandraHostConfigurator);
        clusters.put(clusterName, c);
      }
      return c;
    }
  }

  /**
   * Method looks in the cache for the cluster by name. If none exists, a new
   * ThriftCluster instance is created.
   * 
   * @param clusterName
   *          The cluster name. This is an identifying string for the cluster,
   *          e.g. "production" or "test" etc. Clusters will be created on
   *          demand per each unique clusterName key.
   * @param cassandraHostConfigurator
   * 
   */
  public static Cluster createCluster(String clusterName,
      CassandraHostConfigurator cassandraHostConfigurator) {
    synchronized (clusters) {
      return clusters.get(clusterName) == null ? new ThriftCluster(clusterName,
          cassandraHostConfigurator) : clusters.get(clusterName);
    }
  }

  /**
   * Method looks in the cache for the cluster by name. If none exists, a new
   * ThriftCluster instance is created.
   * 
   * @param clusterName
   *          The cluster name. This is an identifying string for the cluster,
   *          e.g. "production" or "test" etc. Clusters will be created on
   *          demand per each unique clusterName key.
   * @param cassandraHostConfigurator
   * @param credentials
   */
  public static Cluster createCluster(String clusterName,
      CassandraHostConfigurator cassandraHostConfigurator,
      Map<String, String> credentials) {
    synchronized (clusters) {
      return clusters.get(clusterName) == null ? new ThriftCluster(clusterName,
          cassandraHostConfigurator, credentials) : clusters.get(clusterName);
    }
  }
  
  /**
   * Shutdown this cluster, removing it from the Map. This operation is
   * extremely expensive and should not be done lightly.
   * @param cluster
   */
  public static void shutdownCluster(Cluster cluster) {
    synchronized (clusters) {
      String clusterName = cluster.getName();
      if (clusters.get(clusterName) != null ) {
        cluster.getConnectionManager().shutdown();
        clusters.remove(clusterName);
      }
    }
  }


  public static ConsistencyLevelPolicy createDefaultConsistencyLevelPolicy() {
    return DEFAULT_CONSISTENCY_LEVEL_POLICY;
  }


}
