package services;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;

public class DatabaseConnectionService {
    private final String url = "jdbc:sqlserver://${dbServer};databaseName=${dbName};user=${user};password=${pass}";

    private Connection connection = null;

    private String databaseName;
    private String serverName;

    private String username;
    
    public DatabaseConnectionService(String serverName, String databaseName) {
        this.serverName = serverName;
        this.databaseName = databaseName;
    }
    
    public DatabaseConnectionService(String propertiesFileName) throws IOException {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword("827CJIXWp73P11No9cO5p9NGrL8mLG2k");
        encryptor.setAlgorithm("PBEWithMD5AndDES");
        
        Properties properties = new EncryptableProperties(encryptor);
        properties.load(new FileInputStream(propertiesFileName));
        
        this.serverName   = properties.getProperty("server");
        this.databaseName = properties.getProperty("database");
        String username   = properties.getProperty("username");
        String password   = properties.getProperty("password");
        
        connect(username, password);
    }

    public boolean connect(String username, String password) {
        this.username = username;
        String finalURL = url
                .replace("${dbServer}", this.serverName)
                .replace("${dbName}", this.databaseName)
                .replace("${user}", username)
                .replace("${pass}", password);
        try {
            this.connection = DriverManager.getConnection(finalURL);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String getUsername() {
        return this.username;
    }
    
    public String getDatabaseName() {
        return this.databaseName;
    }


    public Connection getConnection() {
        return this.connection;
    }

    public void closeConnection() {
        try {
            this.connection.close();
            System.out.println("Connection Closed");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}