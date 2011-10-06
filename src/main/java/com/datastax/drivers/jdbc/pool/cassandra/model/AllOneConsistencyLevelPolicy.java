package com.datastax.drivers.jdbc.pool.cassandra.model;

import com.datastax.drivers.jdbc.pool.cassandra.service.OperationType;

/**
 * A simple implementation of {@link ConsistencyLevelPolicy} which returns ONE as the desired
 * consistency level for both reads and writes.
 *
 * @author Ran Tavory
 *
 */
public final class AllOneConsistencyLevelPolicy implements ConsistencyLevelPolicy {

  @Override
  public HConsistencyLevel get(OperationType op) {
    return HConsistencyLevel.ONE;
  }

  @Override
  public HConsistencyLevel get(OperationType op, String cfName) {
    return HConsistencyLevel.ONE;
  }

}
