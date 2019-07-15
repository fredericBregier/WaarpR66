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

import org.waarp.common.database.data.AbstractDbData;

import java.util.HashMap;
import java.util.Map;

public enum UpdatedInfo {
  /**
   * Unknown run status
   */
  UNKNOWN(0),
  /**
   * Not updated run status
   */
  NOTUPDATED(1),
  /**
   * Interrupted status (stop or cancel)
   */
  INTERRUPTED(2),
  /**
   * Updated run status meaning ready to be submitted
   */
  TOSUBMIT(3),
  /**
   * In error run status
   */
  INERROR(4),
  /**
   * Running status
   */
  RUNNING(5),
  /**
   * All done run status
   */
  DONE(6);

  private static Map<Integer, UpdatedInfo> map
      = new HashMap<Integer, UpdatedInfo>();

  static {
    for (UpdatedInfo updatedInfo : UpdatedInfo.values()) {
      map.put(updatedInfo.id, updatedInfo);
    }
  }

  private int id;

  UpdatedInfo(final int updatedInfo) {
    id = updatedInfo;
  }

  public static UpdatedInfo valueOf(int updatedInfo) {
    if (!map.containsKey(updatedInfo)) {
      return UNKNOWN;
    }
    return map.get(updatedInfo);
  }

  public static UpdatedInfo fromLegacy(AbstractDbData.UpdatedInfo info) {
    return valueOf(info.name());
  }

  public boolean equals(AbstractDbData.UpdatedInfo legacy) {
    return this.ordinal() == legacy.ordinal();
  }

  public AbstractDbData.UpdatedInfo getLegacy() {
    return AbstractDbData.UpdatedInfo.valueOf(this.name());
  }
}
