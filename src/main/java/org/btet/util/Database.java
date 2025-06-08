package org.btet.util;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
/**
 * The Database class is responsible for providing methods to connect and disconnect from the database.
 * */
public class Database {
    /**
     * Connects to the database using the database.properties file.
     * @return Connection object
     * @throws SQLException if a database access error occurs
     * @throws IOException if an I/O error occurs while reading from the database.properties file
     * */
    public static Connection connectToDatabase() throws SQLException, IOException {
        try(FileReader fr = new FileReader("database.properties")) {
            Properties prop = new Properties();
            prop.load(fr);

            return DriverManager.getConnection(
                    prop.getProperty("databaseUrl"),
                    prop.getProperty("username"),
                    prop.getProperty("password"));
        }
    }
    /**
     * Closes the connection to the database, has to be called manually if try with resources is not used.
     * */
    public void disconnectFromDatabase(Connection connection) throws SQLException {
        connection.close();
    }
}
