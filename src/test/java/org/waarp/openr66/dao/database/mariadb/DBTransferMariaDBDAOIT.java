package org.waarp.openr66.dao.database.mariadb;

import org.junit.ClassRule;
import org.testcontainers.containers.MariaDBContainer;
import org.waarp.openr66.dao.database.DBTransferDAO;
import org.waarp.openr66.dao.database.DBTransferDAOIT;
import org.waarp.openr66.dao.exception.DAOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBTransferMariaDBDAOIT extends DBTransferDAOIT {

    @ClassRule
    public static MariaDBContainer db = new MariaDBContainer();
    private String createScript = "mysql/create.sql";
    private String populateScript = "mysql/populate.sql";
    private String cleanScript = "mysql/clean.sql";

    @Override
    public DBTransferDAO getDAO(Connection con) throws DAOException {
        return new MariaDBTransferDAO(con);
    }


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

