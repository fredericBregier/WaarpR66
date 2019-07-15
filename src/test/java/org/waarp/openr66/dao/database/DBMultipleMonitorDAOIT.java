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

package org.waarp.openr66.dao.database;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.waarp.openr66.dao.Filter;
import org.waarp.openr66.dao.MultipleMonitorDAO;
import org.waarp.openr66.pojo.MultipleMonitor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public abstract class DBMultipleMonitorDAOIT {

  private Connection con;

  public void runScript(String script) {
    try {
      ScriptRunner runner = new ScriptRunner(con, false, true);
      URL url =
          Thread.currentThread().getContextClassLoader().getResource(script);
      runner.runScript(new BufferedReader(new FileReader(url.getPath())));
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Before
  public void setUp() {
    try {
      con = getConnection();
      initDB();
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public abstract Connection getConnection() throws SQLException;

  public abstract void initDB() throws SQLException;

  @After
  public void wrapUp() {
    try {
      cleanDB();
      con.close();
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  public abstract void cleanDB() throws SQLException;

  @Test
  public void testDeleteAll() {
    try {
      MultipleMonitorDAO dao = new DBMultipleMonitorDAO(getConnection());
      dao.deleteAll();

      ResultSet res = con.createStatement()
                         .executeQuery("SELECT * FROM multiplemonitor");
      assertEquals(false, res.next());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testDelete() {
    try {
      MultipleMonitorDAO dao = new DBMultipleMonitorDAO(getConnection());
      dao.delete(new MultipleMonitor("server1", 0, 0, 0));

      ResultSet res = con.createStatement()
                         .executeQuery(
                             "SELECT * FROM multiplemonitor where hostid = 'server1'");
      assertEquals(false, res.next());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testGetAll() {
    try {
      MultipleMonitorDAO dao = new DBMultipleMonitorDAO(getConnection());
      assertEquals(4, dao.getAll().size());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testSelect() {
    try {
      MultipleMonitorDAO dao = new DBMultipleMonitorDAO(getConnection());
      MultipleMonitor multiple = dao.select("server1");
      MultipleMonitor multiple2 = dao.select("ghost");

      assertEquals("server1", multiple.getHostid());
      assertEquals(11, multiple.getCountConfig());
      assertEquals(29, multiple.getCountRule());
      assertEquals(18, multiple.getCountHost());
      assertEquals(null, multiple2);
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testExist() {
    try {
      MultipleMonitorDAO dao = new DBMultipleMonitorDAO(getConnection());
      assertEquals(true, dao.exist("server1"));
      assertEquals(false, dao.exist("ghost"));
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }


  @Test
  public void testInsert() {
    try {
      MultipleMonitorDAO dao = new DBMultipleMonitorDAO(getConnection());
      dao.insert(new MultipleMonitor("chacha", 31, 19, 98));

      ResultSet res = con.createStatement()
                         .executeQuery(
                             "SELECT COUNT(1) as count FROM multiplemonitor");
      res.next();
      assertEquals(5, res.getInt("count"));

      ResultSet res2 = con.createStatement()
                          .executeQuery(
                              "SELECT * FROM multiplemonitor WHERE hostid = 'chacha'");
      res2.next();
      assertEquals("chacha", res2.getString("hostid"));
      assertEquals(98, res2.getInt("countRule"));
      assertEquals(19, res2.getInt("countHost"));
      assertEquals(31, res2.getInt("countConfig"));
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testUpdate() {
    try {
      MultipleMonitorDAO dao = new DBMultipleMonitorDAO(getConnection());
      dao.update(new MultipleMonitor("server2", 31, 19, 98));

      ResultSet res = con.createStatement()
                         .executeQuery(
                             "SELECT * FROM multiplemonitor WHERE hostid = 'server2'");
      res.next();
      assertEquals("server2", res.getString("hostid"));
      assertEquals(98, res.getInt("countRule"));
      assertEquals(19, res.getInt("countHost"));
      assertEquals(31, res.getInt("countConfig"));
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }


  @Test
  public void testFind() {
    ArrayList<Filter> map = new ArrayList<Filter>();
    map.add(new Filter(DBMultipleMonitorDAO.COUNT_CONFIG_FIELD, "=", 0));
    try {
      MultipleMonitorDAO dao = new DBMultipleMonitorDAO(getConnection());
      assertEquals(2, dao.find(map).size());
    } catch (Exception e) {
      fail(e.getMessage());
    }
  }
}

