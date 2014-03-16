/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hive.streaming;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.LockRequest;
import org.apache.hadoop.hive.metastore.api.LockResponse;
import org.apache.hadoop.hive.metastore.api.LockState;
import org.apache.hadoop.hive.metastore.api.NoSuchTxnException;
import org.apache.hadoop.hive.metastore.api.TxnAbortedException;
import org.apache.hadoop.hive.ql.io.AcidOutputFormat;
import org.apache.hadoop.hive.ql.io.RecordUpdater;
import org.apache.hadoop.hive.ql.io.orc.OrcOutputFormat;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorFactory;
import org.apache.thrift.TException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Represents a set of Transactions returned by Hive. Supports opening, writing to
 * and commiting/aborting each transaction. The interface is designed to ensure
 * transactions in a batch are used up sequentially. Multiple transaction batches can be
 * used (initialized with separate RecordWriters) for concurrent streaming
 *
 */
public interface TransactionBatch  {
  public enum TxnState {INACTIVE, OPEN, COMMITTED, ABORTED }

  /**
   * Activate the next available transaction in the current transaction batch
   * @throws StreamingException
   */
  public void beginNextTransaction() throws StreamingException, InterruptedException;

  /**
   * Get Id of currently open transaction
   * @return transaction id
   */
  public Long getCurrentTxnId();

  /**
   * get state of current transaction
   */
  public TxnState getCurrentTransactionState();

  /**
   * Commit the currently open transaction
   * @throws StreamingException
   */
  public void commit() throws StreamingException, InterruptedException;

  /**
   * Abort the currently open transaction
   * @throws StreamingException
   */
  public void abort() throws StreamingException, InterruptedException;

  /**
   * Remaining transactions are the ones that are not committed or aborted or open.
   * Currently open transaction is not considered part of remaining txns.
   * @return number of transactions remaining this batch.
   */
  public int remainingTransactions();


  /**
   *  Write record using RecordWriter
   * @param record  the data to be written
   * @throws ConnectionError
   * @throws IOException
   * @throws StreamingException
   */
  public void write(byte[] record) throws StreamingException, InterruptedException;
  /**
   *  Write records using RecordWriter
   * @param records collection of rows to be written
   * @throws ConnectionError
   * @throws IOException
   * @throws StreamingException
   */
  public void write(Collection<byte[]> records) throws StreamingException, InterruptedException;

  /**
   * Close the TransactionBatch
   * @throws StreamingException
   */
  public void close() throws StreamingException, InterruptedException;
}
