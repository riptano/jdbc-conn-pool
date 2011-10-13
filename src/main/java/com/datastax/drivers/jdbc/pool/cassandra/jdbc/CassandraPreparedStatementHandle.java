/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.drivers.jdbc.pool.cassandra.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicReference;

import com.datastax.drivers.jdbc.pool.cassandra.connection.HConnectionManager;
import com.datastax.drivers.jdbc.pool.cassandra.jdbc.proxy.Recordable;
import com.datastax.drivers.jdbc.pool.cassandra.jdbc.proxy.Recorder;
import com.datastax.drivers.jdbc.pool.cassandra.service.Operation;
import com.datastax.drivers.jdbc.pool.cassandra.service.OperationType;
import com.datastax.drivers.jdbc.pool.cassandra.utils.ReflectionUtils;


/**
 * Wrapper around JDBC PreparedStatement.
 *
 */
public class CassandraPreparedStatementHandle extends CassandraStatementHandle implements PreparedStatement, Recorder {
  
  /** Handle to the real prepared statement. */
  private PreparedStatement internalPreparedStatement;
  private String sql;
  
  public CassandraPreparedStatementHandle(PreparedStatement internalPreparedStatement, HConnectionManager manager, 
      CassandraConnectionHandle cassandraConnectionHandle, String sql) {
    super(internalPreparedStatement, manager, cassandraConnectionHandle);
    this.internalPreparedStatement = internalPreparedStatement;
    this.sql = sql;
  }
  
  /**
   * Handles the Statement replacement during a failover.
   * @param stmRef reference to the current statement
   * @param newConnection a new ConnectionHandle
   * @throws SQLException 
   */
  private void getReadyforFailover(AtomicReference<PreparedStatement> stmRef, CassandraConnectionHandle newConnection) throws SQLException {
    PreparedStatement newStm = newConnection.createInternalPrepareStatement(sql);

    // Apply the recorded methods invocation onto the new statement.
    this.applyInvocationsOn(newStm);
    stmRef.get().close();
    
    internalPreparedStatement = newStm;
    cassandraConnectionHandle = newConnection;
    stmRef.set(newStm);
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#addBatch()
   */
  public void addBatch() throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.addBatch();
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#clearParameters()
   */
  @Override
  public void clearParameters() throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.clearParameters();
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#execute()
   */
  @Override
  public boolean execute() throws SQLException {
    final AtomicReference<PreparedStatement> stmRef = new AtomicReference<PreparedStatement>(this.internalPreparedStatement);
    
    Operation<Boolean> op = new Operation<Boolean>(OperationType.CQL, this) {

      @Override
      public Boolean execute(CassandraConnectionHandle connection) throws SQLException {
        return stmRef.get().execute();
      }
      
      @Override
      public void prepareForFailover(CassandraConnectionHandle newConnection) throws SQLException {
        getReadyforFailover(stmRef, newConnection);
      }
    };

    execute(op);
    return op.getResult();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#executeQuery()
   */
  @Override
  public ResultSet executeQuery() throws SQLException {
    final AtomicReference<PreparedStatement> stmRef = new AtomicReference<PreparedStatement>(this.internalPreparedStatement);
    Operation<ResultSet> op = new Operation<ResultSet>(OperationType.CQL, this) {

      @Override
      public ResultSet execute(CassandraConnectionHandle connection) throws SQLException {
        return stmRef.get().executeQuery();
      }
      
      @Override
      public void prepareForFailover(CassandraConnectionHandle newConnection) throws SQLException {
        getReadyforFailover(stmRef, newConnection);
      }
    };

    execute(op);
    return op.getResult();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#executeUpdate()
   */
  public int executeUpdate() throws SQLException {
    final AtomicReference<PreparedStatement> stmRef = new AtomicReference<PreparedStatement>(this.internalPreparedStatement);
    Operation<Integer> op = new Operation<Integer>(OperationType.CQL, this) {

      @Override
      public Integer execute(CassandraConnectionHandle connection) throws SQLException {
        return  stmRef.get().executeUpdate();
      }
      
      @Override
      public void prepareForFailover(CassandraConnectionHandle newConnection) throws SQLException {
        getReadyforFailover(stmRef, newConnection);
      }
    };

    execute(op);
    return op.getResult();
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#getMetaData()
   */
  public ResultSetMetaData getMetaData() throws SQLException {
    checkClosed();
    try {
      return this.internalPreparedStatement.getMetaData();
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#getParameterMetaData()
   */
  public ParameterMetaData getParameterMetaData() throws SQLException {
    checkClosed();
    try {
      return this.internalPreparedStatement.getParameterMetaData();
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setArray(int, java.sql.Array)
   */
  @Recordable
  public void setArray(int parameterIndex, Array x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setArray(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream)
   */
  @Recordable
  public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setAsciiStream(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, int)
   */
  @Recordable
  public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setAsciiStream(parameterIndex, x, length);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, long)
   */
  @Recordable
  public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setAsciiStream(parameterIndex, x, length);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setBigDecimal(int, java.math.BigDecimal)
   */
  @Recordable
  public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setBigDecimal(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream)
   */
  @Override
  @Recordable
  public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setBinaryStream(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, int)
   */
  @Recordable
  public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setBinaryStream(parameterIndex, x, length);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, long)
   */
  @Recordable
  public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setBinaryStream(parameterIndex, x, length);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setBlob(int, java.sql.Blob)
   */
  @Recordable
  public void setBlob(int parameterIndex, Blob x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setBlob(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream)
   */
  @Recordable
  public void setBlob(int parameterIndex, InputStream x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setBlob(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream, long)
   */
  @Recordable
  public void setBlob(int parameterIndex, InputStream x, long length) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setBlob(parameterIndex, x, length);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setBoolean(int, boolean)
   */
  @Recordable
  public void setBoolean(int parameterIndex, boolean x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setBoolean(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setByte(int, byte)
   */
  @Recordable
  public void setByte(int parameterIndex, byte x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setByte(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setBytes(int, byte[])
   */
  @Recordable
  public void setBytes(int parameterIndex, byte[] x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setBytes(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader)
   */
  @Recordable
  public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setCharacterStream(parameterIndex, reader);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, int)
   */
  @Recordable
  public void setCharacterStream(int parameterIndex, Reader reader, int lenght) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setCharacterStream(parameterIndex, reader, lenght);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, long)
   */
  @Recordable
  public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setCharacterStream(parameterIndex, reader, length);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setClob(int, java.sql.Clob)
   */
  @Recordable
  public void setClob(int parameterIndex, Clob x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setClob(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setClob(int, java.io.Reader)
   */
  @Recordable
  public void setClob(int parameterIndex, Reader reader) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setClob(parameterIndex, reader);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setClob(int, java.io.Reader, long)
   */
  @Recordable
  public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setClob(parameterIndex, reader, length);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setDate(int, java.sql.Date)
   */
  @Recordable
  public void setDate(int parameterIndex, Date x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setDate(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setDate(int, java.sql.Date, java.util.Calendar)
   */
  @Recordable
  public void setDate(int parameterIndex, Date x, Calendar c) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setDate(parameterIndex, x, c);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setDouble(int, double)
   */
  @Recordable
  public void setDouble(int parameterIndex, double x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setDouble(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setFloat(int, float)
   */
  @Recordable
  public void setFloat(int parameterIndex, float x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setFloat(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setInt(int, int)
   */
  @Recordable
  public void setInt(int parameterIndex, int x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setInt(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setLong(int, long)
   */
  @Recordable
  public void setLong(int parameterIndex, long x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setLong(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }
  
  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader)
   */
  @Recordable
  public void setNCharacterStream(int parameterIndex, Reader x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setNCharacterStream(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader, long)
   */
  @Recordable
  public void setNCharacterStream(int parameterIndex, Reader x, long length) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setNCharacterStream(parameterIndex, x, length);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setNClob(int, java.sql.NClob)
   */
  @Recordable
  public void setNClob(int parameterIndex, NClob x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setNClob(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader)
   */
  @Recordable
  public void setNClob(int parameterIndex, Reader x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setNClob(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader, long)
   */
  @Recordable
  public void setNClob(int parameterIndex, Reader x, long length) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setNClob(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setNString(int, java.lang.String)
   */
  @Recordable
  public void setNString(int parameterIndex, String x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setNString(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setNull(int, int)
   */
  @Recordable
  public void setNull(int parameterIndex, int x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setNull(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setNull(int, int, java.lang.String)
   */
  @Recordable
  public void setNull(int parameterIndex, int x, String typeName) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setNull(parameterIndex, x, typeName);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setObject(int, java.lang.Object)
   */
  @Recordable
  public void setObject(int parameterIndex, Object x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setObject(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int)
   */
  @Recordable
  public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setObject(parameterIndex, x, targetSqlType);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int, int)
   */
  @Recordable
  public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setRef(int, java.sql.Ref)
   */
  @Recordable
  public void setRef(int parameterIndex, Ref x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setRef(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setRowId(int, java.sql.RowId)
   */
  @Recordable
  public void setRowId(int parameterIndex, RowId x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setRowId(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setSQLXML(int, java.sql.SQLXML)
   */
  @Recordable
  public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setSQLXML(parameterIndex, xmlObject);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setShort(int, short)
   */
  @Recordable
  public void setShort(int parameterIndex, short x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setShort(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setString(int, java.lang.String)
   */
  @Recordable
  public void setString(int parameterIndex, String x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setString(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setTime(int, java.sql.Time)
   */
  @Recordable
  public void setTime(int parameterIndex, Time x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setTime(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setTime(int, java.sql.Time, java.util.Calendar)
   */
  @Recordable
  public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setTime(parameterIndex, x, cal);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp)
   */
  @Recordable
  public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setTimestamp(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp, java.util.Calendar)
   */
  @Recordable
  public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setTimestamp(parameterIndex, x, cal);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setURL(int, java.net.URL)
   */
  @Recordable
  public void setURL(int parameterIndex, URL x) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setURL(parameterIndex, x);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see java.sql.PreparedStatement#setUnicodeStream(int, java.io.InputStream, int)
   */
  @Recordable
  public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
    checkClosed();
    try {
      this.internalPreparedStatement.setUnicodeStream(parameterIndex, x, length);
    } catch (SQLException e) {
      throw this.cassandraConnectionHandle.markPossiblyBroken(e);
    }
  }

}
