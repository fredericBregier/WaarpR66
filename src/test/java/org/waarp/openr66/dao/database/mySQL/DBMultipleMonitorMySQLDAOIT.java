package org.waarp.openr66.dao.database.mySQL;

import org.junit.ClassRule;
import org.testcontainers.containers.MySQLContainer;
import org.waarp.openr66.dao.database.DBMultipleMonitorDAOIT;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBMultipleMonitorMySQLDAOIT extends DBMultipleMonitorDAOIT {

    @ClassRule
    public static MySQLContainer db = new MySQLContainer();
    private String createScript = "mysql/create.sql";
    private String populateScript = "mysql/populate.sql";
    private String cleanScript = "mysql/clean.sql";

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                db.getJdbcUrl(),
                db.getUsername(),
                db.getPassword());
    }

    @Override
    public void initDB() {
        runScript(createScript);
        runScript(populateScript);
    }

    @Override
    public void cleanDB() {
        runScript(cleanScript);
    }
}

