package com.datastax.drivers.jdbc.pool.cassandra;

import com.datastax.drivers.jdbc.pool.cassandra.exceptions.HectorException;
import com.datastax.drivers.jdbc.pool.cassandra.model.ConsistencyLevelPolicy;
import com.datastax.drivers.jdbc.pool.cassandra.service.Operation;


/**
 *
 * @author Ran Tavory
 *
 */
public interface Keyspace {

  public static final String KEYSPACE_SYSTEM = "system";
  
  void setConsistencyLevelPolicy(ConsistencyLevelPolicy cp);

  String getKeyspaceName();
  
  long createClock();

}