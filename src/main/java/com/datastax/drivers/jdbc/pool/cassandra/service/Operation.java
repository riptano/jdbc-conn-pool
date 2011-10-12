package com.datastax.drivers.jdbc.pool.cassandra.service;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

import com.datastax.drivers.jdbc.pool.cassandra.connection.CassandraHost;
import com.datastax.drivers.jdbc.pool.cassandra.connection.CassandraClientMonitor.Counter;
import com.datastax.drivers.jdbc.pool.cassandra.exceptions.HectorException;
import com.datastax.drivers.jdbc.pool.cassandra.jdbc.CassandraConnectionHandle;
import com.datastax.drivers.jdbc.pool.cassandra.jdbc.CassandraStatementHandle;
import com.datastax.drivers.jdbc.pool.cassandra.model.ConsistencyLevelPolicy;
import com.datastax.drivers.jdbc.pool.cassandra.model.ExecutionResult;


/**
 * Defines an operation performed on cassandra
 *
 * @param <T>
 *          The result type of the operation (if it has a result), such as the
 *          result of get_count or get_column
 *
 *          Oh closures, how I wish you were here...
 */
public abstract class Operation<T> {

  /** Counts failed attempts */
  public final Counter failCounter;

  /** The stopwatch used to measure operation performance */
  public final String stopWatchTagName;

  public FailoverPolicy failoverPolicy = FailoverPolicy.ON_FAIL_TRY_ALL_AVAILABLE;
  public ConsistencyLevelPolicy consistencyLevelPolicy;
  
  public String keyspaceName;

  public Map<String, String> credentials;
  
  protected T result;
  private HectorException exception;
  private CassandraHost cassandraHost;
  protected long execTime;
  public final OperationType operationType;
  public CassandraStatementHandle cassandraStatement;
  
  public Operation(OperationType operationType, CassandraStatementHandle statement) {
    this(operationType);
    this.cassandraStatement = statement;
  }
  
  public Operation(OperationType operationType) {
    this.failCounter = operationType.equals(OperationType.READ) ? Counter.READ_FAIL :
      Counter.WRITE_FAIL;
    this.operationType = operationType;
    this.stopWatchTagName = operationType.name();
  }

  public Operation(OperationType operationType, Map<String, String> credentials) {
    this(operationType, FailoverPolicy.ON_FAIL_TRY_ALL_AVAILABLE, null, credentials);
  }
  
  public Operation(OperationType operationType, FailoverPolicy failoverPolicy, String keyspaceName, Map<String, String> credentials) {
    this(operationType);
    this.failoverPolicy = failoverPolicy;
    this.keyspaceName = keyspaceName;
    this.credentials = Collections.unmodifiableMap(credentials);
  }
  
  
  public void applyConnectionParams(String keyspace, ConsistencyLevelPolicy consistencyLevelPolicy,
      FailoverPolicy failoverPolicy, Map<String,String> credentials) {
    // TODO this is a first step. must be cleaned up.
    this.keyspaceName = keyspace;
    this.consistencyLevelPolicy = consistencyLevelPolicy;
    this.failoverPolicy = failoverPolicy;
    this.credentials = credentials;
  }

  public void setResult(T executionResult) {
    result = executionResult;
  }

  /**
   *
   * @return The result of the operation, if this is an operation that has a
   *         result (such as getColumn etc.
   */
  public T getResult() {
    // TODO remove in favor of getExecutionResult
    return result;
  }
  
  public ExecutionResult<T> getExecutionResult() {
    return new ExecutionResult<T>(result, execTime, cassandraHost);
  }

  /**
   * Performs the operation on the given cassandra instance.
   */
  public abstract T execute(CassandraConnectionHandle connection) throws SQLException;
  
  /**
   * 
   * @param connection
   * @throws SQLException
   */
  public abstract void prepareForFailover(CassandraConnectionHandle newConnection) throws SQLException;

  public void executeAndSetResult(CassandraConnectionHandle connection) throws SQLException {
    final CassandraHost cassandraHost = connection.getCassandraHost();
    this.cassandraHost = cassandraHost;
    long startTime = System.nanoTime();
    setResult(execute(connection));
    execTime = System.nanoTime() - startTime;
  }

  public void setException(HectorException e) {
    exception = e;
  }

  public boolean hasException() {
    return exception != null;
  }

  public HectorException getException() {
    return exception;
  }

  public CassandraHost getCassandraHost() {
    return this.cassandraHost;
  }

  public void setConnection(CassandraConnectionHandle currentConnection) {
    if (cassandraStatement != null)
      cassandraStatement.cassandraConnectionHandle = currentConnection;
  }

  public CassandraConnectionHandle getConnection() {
    if (cassandraStatement != null)
      return cassandraStatement.cassandraConnectionHandle;
    return null;
  }

}

