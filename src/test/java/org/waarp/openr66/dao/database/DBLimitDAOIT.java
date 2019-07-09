package org.waarp.openr66.dao.database;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.waarp.openr66.dao.Filter;
import org.waarp.openr66.dao.LimitDAO;
import org.waarp.openr66.pojo.Limit;
import org.waarp.openr66.pojo.UpdatedInfo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.*;

public abstract class DBLimitDAOIT {

    private Connection con;

    public abstract Connection getConnection() throws SQLException;

    public abstract void initDB() throws SQLException;

    public abstract void cleanDB() throws SQLException;

    public void runScript(String script) {
        try {
            ScriptRunner runner = new ScriptRunner(con, false, true);
            URL url = Thread.currentThread().getContextClassLoader().getResource(script);
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

    @After
    public void wrapUp() {
        try {
            cleanDB();
            con.close();
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testDeleteAll() {
        try {
            LimitDAO dao = new DBLimitDAO(getConnection());
            dao.deleteAll();

            ResultSet res = con.createStatement()
                               .executeQuery("SELECT * FROM configuration");
            assertEquals(false, res.next());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testDelete() {
        try {
            LimitDAO dao = new DBLimitDAO(getConnection());
            dao.delete(new Limit("server1", 0l));

            ResultSet res = con.createStatement()
                               .executeQuery("SELECT * FROM configuration where hostid = 'server1'");
            assertEquals(false, res.next());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetAll() {
        try {
            LimitDAO dao = new DBLimitDAO(getConnection());
            assertEquals(3, dao.getAll().size());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSelect() {
        try {
            LimitDAO dao = new DBLimitDAO(getConnection());
            Limit limit = dao.select("server1");
            Limit limit2 = dao.select("ghost");

            assertEquals("server1", limit.getHostid());
            assertEquals(1, limit.getReadGlobalLimit());
            assertEquals(2, limit.getWriteGlobalLimit());
            assertEquals(3, limit.getReadSessionLimit());
            assertEquals(4, limit.getWriteSessionLimit());
            assertEquals(5, limit.getDelayLimit());
            assertEquals(UpdatedInfo.NOTUPDATED, limit.getUpdatedInfo());
            assertEquals(null, limit2);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testExist() {
        try {
            LimitDAO dao = new DBLimitDAO(getConnection());
            assertEquals(true, dao.exist("server1"));
            assertEquals(false, dao.exist("ghost"));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


    @Test
    public void testInsert() {
        try {
            LimitDAO dao = new DBLimitDAO(getConnection());
            dao.insert(new Limit("chacha", 4l,
                                 1l, 5l, 13l, 12,
                                 UpdatedInfo.TOSUBMIT));

            ResultSet res = con.createStatement()
                               .executeQuery("SELECT COUNT(1) as count FROM configuration");
            res.next();
            assertEquals(4, res.getInt("count"));

            ResultSet res2 = con.createStatement()
                                .executeQuery("SELECT * FROM configuration WHERE hostid = 'chacha'");
            res2.next();
            assertEquals("chacha", res2.getString("hostid"));
            assertEquals(4, res2.getLong("delaylimit"));
            assertEquals(1, res2.getLong("readGlobalLimit"));
            assertEquals(5, res2.getLong("writeGlobalLimit"));
            assertEquals(13, res2.getLong("readSessionLimit"));
            assertEquals(12, res2.getLong("writeSessionLimit"));
            assertEquals(UpdatedInfo.TOSUBMIT.ordinal(), res2.getInt("updatedInfo"));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testUpdate() {
        try {
            LimitDAO dao = new DBLimitDAO(getConnection());
            dao.update(new Limit("server2", 4l,
                                 1l, 5l, 13l, 12l,
                                 UpdatedInfo.RUNNING));

            ResultSet res = con.createStatement()
                               .executeQuery("SELECT * FROM configuration WHERE hostid = 'server2'");
            res.next();
            assertEquals("server2", res.getString("hostid"));
            assertEquals(4, res.getLong("delaylimit"));
            assertEquals(1, res.getLong("readGlobalLimit"));
            assertEquals(5, res.getLong("writeGlobalLimit"));
            assertEquals(13, res.getLong("readSessionLimit"));
            assertEquals(12, res.getLong("writeSessionLimit"));
            assertEquals(UpdatedInfo.RUNNING.ordinal(), res.getInt("updatedInfo"));
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }


    @Test
    public void testFind() {
        ArrayList<Filter> map = new ArrayList<Filter>();
        map.add(new Filter(DBLimitDAO.READ_SESSION_LIMIT_FIELD, ">", 2));
        try {
            LimitDAO dao = new DBLimitDAO(getConnection());
            assertEquals(2, dao.find(map).size());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}

