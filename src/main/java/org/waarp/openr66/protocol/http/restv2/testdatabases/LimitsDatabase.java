/*
 * This file is part of Waarp Project (named also Waarp or GG).
 *
 * Copyright 2009, Waarp SAS, and individual contributors by the @author
 * tags. See the COPYRIGHT.txt in the distribution for a full listing of
 * individual contributors.
 *
 * All Waarp Project is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Waarp . If not, see <http://www.gnu.org/licenses/>.
 */

package org.waarp.openr66.protocol.http.restv2.testdatabases;

import org.waarp.openr66.protocol.http.restv2.data.Limit;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public final class LimitsDatabase {

    public static List<Limit> limitsDb = initLimitsDb();

    private static List<Limit> initLimitsDb() {
        List<Limit> limits = new ArrayList<Limit>();
        Limit limit = new Limit();
        limit.upGlobalLimit = 1000000;
        limit.downGlobalLimit = 1000000;
        limit.upSessionLimit = 1000000;
        limit.downSessionLimit = 1000000;
        limit.delayLimit = 1000;

        limits.add(limit);
        return limits;
    }

    public static Limit select(String id) {
        for(Limit limit : limitsDb) {
            if(limit.hostID.equals(id)) {
                return limit;
            }
        }
        return null;
    }

    public static boolean insert(Limit limit) {
        if(select(limit.hostID) != null) {
            return false;
        } else {
            limitsDb.add(limit);
            return true;
        }
    }

    public static boolean delete(String id) {
        Limit deleted = select(id);
        if(deleted == null) {
            return false;
        } else {
            limitsDb.remove(deleted);
            return true;
        }
    }

    public static boolean modify(String id, Limit limit) {
        Limit old = select(id);
        if(old == null) {
            return false;
        } else {
            limitsDb.remove(old);
            limitsDb.add(limit);
            return true;
        }
    }
}
