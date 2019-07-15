/*******************************************************************************
 * This file is part of Waarp Project (named also Waarp or GG).
 *
 *  Copyright (c) 2019, Waarp SAS, and individual contributors by the @author
 *  tags. See the COPYRIGHT.txt in the distribution for a full listing of
 *  individual contributors.
 *
 *  All Waarp Project is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or (at your
 *  option) any later version.
 *
 *  Waarp is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 *  A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  Waarp . If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.waarp.openr66.pojo;

/**
 * Limit data object
 */
public class Limit {

  private String hostid;

  private long readGlobalLimit;

  private long writeGlobalLimit;

  private long readSessionLimit;

  private long writeSessionLimit;

  private long delayLimit;

  private UpdatedInfo updatedInfo = UpdatedInfo.UNKNOWN;

  /**
   * Empty constructor for compatibility issues
   */
  @Deprecated
  public Limit() {
  }

  public Limit(String hostid, long delayLimit, long readGlobalLimit,
               long writeGlobalLimit, long readSessionLimit,
               long writeSessionLimit, UpdatedInfo updatedInfo) {
    this(hostid, delayLimit, readGlobalLimit, writeGlobalLimit,
         readSessionLimit, writeSessionLimit);
    this.updatedInfo = updatedInfo;
  }

  public Limit(String hostid, long delayLimit, long readGlobalLimit,
               long writeGlobalLimit, long readSessionLimit,
               long writeSessionLimit) {
    this.hostid = hostid;
    this.delayLimit = delayLimit;
    this.readGlobalLimit = readGlobalLimit;
    this.writeGlobalLimit = writeGlobalLimit;
    this.readSessionLimit = readSessionLimit;
    this.writeSessionLimit = writeSessionLimit;
  }

  public Limit(String hostid, long delayLimit) {
    this(hostid, delayLimit, 0, 0, 0, 0);
  }

  public String getHostid() {
    return this.hostid;
  }

  public void setHostid(String hostid) {
    this.hostid = hostid;
  }

  public long getReadGlobalLimit() {
    return this.readGlobalLimit;
  }

  public void setReadGlobalLimit(long readGlobalLimit) {
    this.readGlobalLimit = readGlobalLimit;
  }

  public long getWriteGlobalLimit() {
    return this.writeGlobalLimit;
  }

  public void setWriteGlobalLimit(long writeGlobalLimit) {
    this.writeGlobalLimit = writeGlobalLimit;
  }

  public long getReadSessionLimit() {
    return this.readSessionLimit;
  }

  public void setReadSessionLimit(long readSessionLimit) {
    this.readSessionLimit = readSessionLimit;
  }

  public long getWriteSessionLimit() {
    return this.writeSessionLimit;
  }

  public void setWriteSessionLimit(long writeSessionLimit) {
    this.writeSessionLimit = writeSessionLimit;
  }

  public long getDelayLimit() {
    return this.delayLimit;
  }

  public void setDelayLimit(long delayLimit) {
    this.delayLimit = delayLimit;
  }

  public UpdatedInfo getUpdatedInfo() {
    return this.updatedInfo;
  }

  public void setUpdatedInfo(UpdatedInfo info) {
    this.updatedInfo = info;
  }
}
