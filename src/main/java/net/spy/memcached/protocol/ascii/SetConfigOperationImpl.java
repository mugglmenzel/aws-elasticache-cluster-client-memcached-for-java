/**
 * Copyright (C) 2012-2012 Amazon.com, Inc. or its affiliates. All Rights Reserved. 
 *
 * Licensed under the Amazon Software License (the "License"). You may not use this 
 * file except in compliance with the License. A copy of the License is located at
 *  http://aws.amazon.com/asl/
 * or in the "license" file accompanying this file. This file is distributed on 
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, express or
 * implied. See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package net.spy.memcached.protocol.ascii;

import java.nio.ByteBuffer;

import net.spy.memcached.KeyUtil;
import net.spy.memcached.ops.ConfigurationType;
import net.spy.memcached.ops.OperationCallback;
import net.spy.memcached.ops.OperationState;
import net.spy.memcached.ops.OperationStatus;
import net.spy.memcached.ops.SetConfigOperation;

/**
 * class for ascii config set operations
 */
class SetConfigOperationImpl extends OperationImpl implements SetConfigOperation{

  private static final String CMD = "config set";
  private static final int OVERHEAD = 32;
  private static final OperationStatus STORED = new OperationStatus(true,
      "STORED");
  protected final ConfigurationType type;
  protected final int flags;
  protected final byte[] data;

  public SetConfigOperationImpl(ConfigurationType type, int f, byte[] d,
      OperationCallback cb) {
    super(cb);
    this.type = type;
    flags = f;
    data = d;
  }

  @Override
  public void handleLine(String line) {
    assert getState() == OperationState.READING : "Read ``" + line
        + "'' when in " + getState() + " state";
    getCallback().receivedStatus(matchStatus(line, STORED));
    transitionState(OperationState.COMPLETE);
  }

  @Override
  public void initialize() {
    ByteBuffer bb = ByteBuffer.allocate(data.length
        + KeyUtil.getKeyBytes(type.getValue()).length + OVERHEAD);
    setArguments(bb, CMD, type.getValue(), flags, data.length);
    assert bb.remaining() >= data.length + 2 : "Not enough room in buffer,"
        + " need another " + (2 + data.length - bb.remaining());
    bb.put(data);
    bb.put(CRLF);
    bb.flip();
    setBuffer(bb);
  }

  @Override
  protected void wasCancelled() {
    getCallback().receivedStatus(CANCELLED);
  }

  public int getFlags() {
    return flags;
  }

  public byte[] getData() {
    return data;
  }

  @Override
  public ConfigurationType getType() {
    return type;
  }
  
  @Override
  public String toString() {
    return "Cmd: " + "config set Type: " + type.getValue() + " Flags: " + flags + " Data Length: " + data.length;
  }
}
