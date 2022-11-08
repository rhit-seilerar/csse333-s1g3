
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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