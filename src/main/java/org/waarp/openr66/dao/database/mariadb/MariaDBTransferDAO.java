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

package org.waarp.openr66.dao.database.mariadb;

import org.waarp.openr66.dao.database.DBTransferDAO;
import org.waarp.openr66.dao.exception.DAOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MariaDBTransferDAO extends DBTransferDAO {

  protected static String SQL_GET_ID = "SELECT seq FROM Sequences " +
                                       "WHERE name='RUNSEQ' FOR UPDATE";
  private static String SQL_UPDATE_ID = "UPDATE Sequences SET seq = ? " +
                                        "WHERE name='RUNSEQ'";

  public MariaDBTransferDAO(Connection con) throws DAOException {
    super(con);
  }

  @Override
  protected long getNextId() throws DAOException {
    PreparedStatement ps = null;
    PreparedStatement ps2 = null;
    try {
      ps = connection.prepareStatement(SQL_GET_ID);
      ResultSet rs = ps.executeQuery();
      long res;
      if (rs.next()) {
        res = rs.getLong(1);
        ps2 = connection.prepareStatement(SQL_UPDATE_ID);
        ps2.setLong(1, res + 1);
        ps2.executeUpdate();
        return res;
      } else {
        throw new DAOException(
            "Error no id available, you should purge the database.");
      }
    } catch (SQLException e) {
      throw new DAOException(e);
    } finally {
      closeStatement(ps);
      closeStatement(ps2);
    }
  }
}