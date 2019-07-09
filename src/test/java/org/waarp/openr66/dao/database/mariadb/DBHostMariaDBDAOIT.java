package org.waarp.openr66.dao.database.mariadb;

import org.junit.ClassRule;
import org.testcontainers.containers.MariaDBContainer;
import org.waarp.openr66.dao.database.DBHostDAOIT;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBHostMariaDBDAOIT extends DBHostDAOIT {

    @ClassRule
    public static MariaDBContainer db = new MariaDBContainer();
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

