package com.datastax.drivers.jdbc.pool.cassandra.connection;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;


public interface LoadBalancingPolicy extends Serializable {
  HClientPool getPool(Collection<HClientPool> pools, Set<CassandraHost> excludeHosts);
  HClientPool createConnection(CassandraHost host) throws SQLException;
}
