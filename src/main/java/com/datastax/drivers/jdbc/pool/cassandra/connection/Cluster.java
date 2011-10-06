package com.datastax.drivers.jdbc.pool.cassandra.connection;

import java.util.Map;
import java.util.Set;

public interface Cluster {

  Set<CassandraHost> getKnownPoolHosts(boolean refresh);

  HConnectionManager getConnectionManager();


  /**
   * Adds the host to this Cluster. Unless skipApplyConfig is set to true, the settings in
   * the CassandraHostConfigurator will be applied to the provided CassandraHost
   * @param cassandraHost
   * @param skipApplyConfig
   */
  void addHost(CassandraHost cassandraHost, boolean skipApplyConfig);

  /**
   * Descriptive name of the cluster.
   * This name is used to identify the cluster.
   * @return
   */
  String getName();

  Map<String, String> getCredentials();
}