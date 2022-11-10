import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.sql.Statement;
import java.util.Properties;
import java.util.Scanner;

public class MigrationCreator {
   public static String databaseName;
   public static StringBuilder stringBuilder;
   public static DatabaseConnectionService connection;
   
   public static void transferAndExecuteQuery(String fileName) throws Exception {
      Scanner scanner = new Scanner(new File(fileName));
      boolean pastPreamble = false;
      StringBuilder query = new StringBuilder();
      
      while(scanner.hasNextLine()) {
         String line = scanner.nextLine();
         
         if(!pastPreamble) {
            if(line.length() == 0) continue;
            if(line.contains("use ") || line.contains("USE ")) continue;
            if(line.equalsIgnoreCase("go")) continue;
            
            pastPreamble = true;
         }
         
         if(line.equalsIgnoreCase("go")) continue;
         
         line = line.replace("create procedure", "create or alter procedure")
                    .replace("CREATE PROCEDURE", "CREATE OR ALTER PROCEDURE")
                    .replace("StardewHoes10", databaseName);
         
         query.append(line + "\n");
      }
      
      scanner.close();
      stringBuilder.append(query);
      
      Statement statement = connection.getConnection().createStatement();
      statement.execute(query.toString());
      System.out.println("Executed " + fileName + ".");
   }
   
   public static void transferAndExecuteDirectory(String dirName) throws Exception {
      File directory = new File(dirName);
      File[] files = directory.listFiles();
      
      if(files == null) return;
      
      for(File file : files) {
         transferAndExecuteQuery(file.getPath());
      }
   }
   
   public static void connect(String serverName, String databaseName, String username, String password) {
      connection = new DatabaseConnectionService(serverName, databaseName);
      boolean connected = connection.connect(username, password);
      if(!connected) {
         System.out.println("Unable to connect to the database.");
         return;
      }
      System.out.println("Successfully connected to the database.");
   }
   
   public static void main(String[] args) throws Exception {
      Properties properties = new Properties();
      properties.load(new FileInputStream(".properties"));
      String serverName = properties.getProperty("server");
      databaseName = properties.getProperty("database");
      
      Scanner scanner = new Scanner(System.in);
      System.out.print("What is your username?\n> ");
      String username = StardewHoes.nextLine(scanner);
      String password = new String(System.console().readPassword("What is your password?\n> "));
      
      connect(serverName, "master", username, password);
      stringBuilder = new StringBuilder();
      stringBuilder.append("use master\ngo\n\n");
      
      transferAndExecuteQuery("queries/create_database.sql");
      
      connection.closeConnection();
      connect(serverName, databaseName, username, password);
      stringBuilder.append("\nuse " + databaseName + "\ngo\n\n");
      
      transferAndExecuteQuery("queries/create_roles.sql");
      transferAndExecuteQuery("queries/create_table.sql");
      transferAndExecuteQuery("queries/create_trigger.sql");
      transferAndExecuteQuery("queries/functions.sql");
      
      transferAndExecuteDirectory("queries/get");
      transferAndExecuteDirectory("queries/insert");
      transferAndExecuteDirectory("queries/delete");
      transferAndExecuteDirectory("queries/update");
      
      FileWriter writer = new FileWriter("queries/create.sql");
      writer.write(stringBuilder.toString());
      writer.close();
      
      connection.closeConnection();
      scanner.close();
   }
}
