import java.io.FileReader;
import java.io.Reader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Types;
import java.util.Iterator;
import java.util.Scanner;

import org.json.JSONObject;
import org.json.JSONString;


public class StardewHoes {
   public static void main(String[] args) throws Exception
   {
      String url = "jdbc:sqlserver://${dbServer};databaseName=${dbName};user=${user};password={${pass}}";
      
      String defaultServer   = (args.length >= 1) ? args[0] : "titan.csse.rose-hulman.edu";
      String defaultDatabase = (args.length >= 2) ? args[1] : "StardewHoes10";
      
      String username, password;
      if(args.length >= 3) {
         username = args[2];
      } else {
         System.out.print("What is your username?\n> ");
         Scanner scanner = new Scanner(System.in);
         username = scanner.nextLine();
         scanner.close();
      }
      
      if(args.length >= 4) {
         password = args[3];
      } else {
         System.out.print("What is your password?\n> ");
         Scanner scanner = new Scanner(System.in);
         password = scanner.nextLine();
         scanner.close();
      }
      
      url = url.replace("${dbServer}", defaultServer).replace("${dbName}", defaultDatabase).replace("${user}", username).replace("${pass}", password);
      Connection connection = DriverManager.getConnection(url);
      
      // Reader reader = new FileReader("data/Data/Crops.json");
      // JSONObject root = new JSONObject(reader.read());
      // JSONObject content = root.getJSONObject("content");
      // Iterator<String> keys = content.keys();
      // while(keys.hasNext()) {
      //    String key = keys.next();
      // }
      // reader.close();
      
      String query = "{? = call insert_PlantProduct(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, "Stone");
      statement.setInt(3, 0);
      statement.setInt(4, 0);
      statement.setString(5, "Fruit");
      statement.execute();
      int result = statement.getInt(1);
      
      System.out.println("Executed query with a result code of " + result + ".");
      
      connection.close();
   }
}