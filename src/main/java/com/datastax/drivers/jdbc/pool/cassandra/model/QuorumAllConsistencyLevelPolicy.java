package com.datastax.drivers.jdbc.pool.cassandra.model;

import com.datastax.drivers.jdbc.pool.cassandra.ConsistencyLevelPolicy;
import com.datastax.drivers.jdbc.pool.cassandra.HConsistencyLevel;
import com.datastax.drivers.jdbc.pool.cassandra.service.OperationType;

/**
 * A simple implementation of {@link ConsistencyLevelPolicy} which returns QUORUM as the desired
 * consistency level for both reads and writes.
 *
 * @author Ran Tavory
 *
 */
public final class QuorumAllConsistencyLevelPolicy implements ConsistencyLevelPolicy {

  @Override
  public HConsistencyLevel get(OperationType op) {
    return HConsistencyLevel.QUORUM;
  }

  @Override
  public HConsistencyLevel get(OperationType op, String cfName) {
    return HConsistencyLevel.QUORUM;
  }

}
