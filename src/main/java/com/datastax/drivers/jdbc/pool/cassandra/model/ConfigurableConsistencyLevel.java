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
package com.datastax.drivers.jdbc.pool.cassandra.model;

import java.util.HashMap;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.drivers.jdbc.pool.cassandra.service.OperationType;

/**
 * Configurable and Runtime adjustable ConsistencyLevelPolicy
 * @author zznate
 */
public class ConfigurableConsistencyLevel implements ConsistencyLevelPolicy {
  private final Logger log = LoggerFactory.getLogger(ConfigurableConsistencyLevel.class);

  private Map<String, HConsistencyLevel> readCfConsistencyLevels = new HashMap<String, HConsistencyLevel>();
  private Map<String, HConsistencyLevel> writeCfConsistencyLevels = new HashMap<String, HConsistencyLevel>();
  private HConsistencyLevel defaultReadConsistencyLevel = HConsistencyLevel.QUORUM;
  private HConsistencyLevel defaultWriteConsistencyLevel = HConsistencyLevel.QUORUM;

  @Override
  public HConsistencyLevel get(OperationType op) {
    return op.equals(OperationType.READ) ? defaultReadConsistencyLevel : defaultWriteConsistencyLevel;
  }

  @Override
  public HConsistencyLevel get(OperationType op, String cfName) {
    if (op.equals(OperationType.READ)) {
      HConsistencyLevel rcf = readCfConsistencyLevels.get(cfName);
      return rcf != null ? rcf : defaultReadConsistencyLevel;
    } else {
      HConsistencyLevel wcf = writeCfConsistencyLevels.get(cfName);
      return wcf != null ? wcf : defaultWriteConsistencyLevel;
    }
  }

  public void setReadCfConsistencyLevels(Map<String, HConsistencyLevel> columnFamilyConsistencyLevels) {
    this.readCfConsistencyLevels = columnFamilyConsistencyLevels;
  }

  public void setWriteCfConsistencyLevels(Map<String, HConsistencyLevel> columnFamilyConsistencyLevels) {
    this.writeCfConsistencyLevels = columnFamilyConsistencyLevels;
  }

  public void setConsistencyLevelForCfOperation(HConsistencyLevel consistencyLevel,
      String columnFamily,
      OperationType operationType) {
    if ( operationType.equals(OperationType.READ)) {
      readCfConsistencyLevels.put(columnFamily, consistencyLevel);
    } else {
      writeCfConsistencyLevels.put(columnFamily, consistencyLevel);
    }
    log.info("{} ConsistencyLevel set to {} for ColumnFamily {}",
        new Object[]{operationType.toString(),consistencyLevel.toString(),columnFamily});
  }

  public void setDefaultReadConsistencyLevel(HConsistencyLevel defaultReadConsistencyLevel) {
    this.defaultReadConsistencyLevel = defaultReadConsistencyLevel;
  }

  public void setDefaultWriteConsistencyLevel(HConsistencyLevel defaultWriteConsistencyLevel) {
    this.defaultWriteConsistencyLevel = defaultWriteConsistencyLevel;
  }



}
