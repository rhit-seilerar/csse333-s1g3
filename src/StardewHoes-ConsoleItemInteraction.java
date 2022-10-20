package databasesConnectivity;
import java.io.FileReader;
import java.io.Reader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.Iterator;
import java.util.Scanner;


public class StardewHoes {
   public static void main(String[] args) throws Exception
   {
      String url = "jdbc:sqlserver://${dbServer};databaseName=${dbName};user=${user};password={${pass}}";
      
      String defaultServer   = (args.length >= 1) ? args[0] : "titan.csse.rose-hulman.edu";
      String defaultDatabase = (args.length >= 2) ? args[1] : "StardewHoes10";
      String password = "";
      String username = "";
      Scanner scanner = null;
      if(args.length >= 3) {
         username = args[2];
      } else {
         System.out.print("What is your username?\n> ");
         scanner = new Scanner(System.in);
         username = scanner.nextLine();
      }
      
      if(args.length >= 4) {
         password = args[3];
      } else {
         System.out.print("What is your password?\n> ");
         while(scanner.hasNextLine()) {
        	 password = scanner.nextLine();
        	 break;
         }
      }
      
      url = url.replace("${dbServer}", defaultServer).replace("${dbName}", defaultDatabase).replace("${user}", username).replace("${pass}", password);
      Connection connection = DriverManager.getConnection(url);
      
      System.out.print("Would you like to insert, delete, or retrieve an item\n");
      System.out.print("Please type 'insert' for insert, 'delete' for delete, and 'retrieve' for retrieval\n>");
      String option = null;
      while(scanner.hasNextLine()) {
     	 option = scanner.nextLine();
     	 break;
      }
      
      if(option.contains("insert")) {
    	  String name = "";
    	  String quality = "";
    	  String baseprice = "";
    	  System.out.println("Insert selected\nPlease provide the item name:\n>");
    	  while(scanner.hasNextLine()) {
    		  name = scanner.nextLine();
    	      break;
    	  }
    	  System.out.println("Please provide the item quality:\n>");
    	  while(scanner.hasNextLine()) {
    		  quality = scanner.nextLine();
    	      break;
    	  }
    	  System.out.println("Please provide the item base price:\n>");
    	  while(scanner.hasNextLine()) {
    		  baseprice = scanner.nextLine();
    	      break;
    	  }
    	  int basePrice = Integer.parseInt(baseprice);
    	  int qual = Integer.parseInt(quality);
    	  String query = "{? = call insert_Item(?, ?, ?)}";
    	  CallableStatement statement = connection.prepareCall(query);
    	  statement.registerOutParameter(1, Types.INTEGER);
    	  statement.setString(2, name);
    	  statement.setInt(3, qual);
    	  statement.setInt(4, basePrice);
    	  statement.execute();
    	  int result = statement.getInt(1);
    	  System.out.println("Item inserted with result: " + result);
    	  connection.close();
    	  
      } else if(option.contains("delete")) {
    	  String id = "";
    	  System.out.println("Delete selected\nPlease provide the item ID:\n>");
    	  while(scanner.hasNextLine()) {
    		  id = scanner.nextLine();
    	      break;
    	  }
    	  int ID = Integer.parseInt(id);
    	  String query = "{? = call delete_Item(?)}";
    	  CallableStatement statement = connection.prepareCall(query);
    	  statement.registerOutParameter(1, Types.INTEGER);
    	  statement.setInt(2, ID);
    	  statement.execute();
    	  int result = statement.getInt(1);
    	  System.out.println("Item with ID " + id + " was deleted");
    	  connection.close();
    	  
      } else if(option.contains("retrieve")) {
    	  String id = "";
    	  System.out.println("Retrieval selected\nPlease provide the item ID:\n>");
    	  while(scanner.hasNextLine()) {
    		  id = scanner.nextLine();
    	      break;
    	  }
    	  int ID = Integer.parseInt(id);
    	  String query = "{? = call get_Item(?)}";
    	  CallableStatement statement = connection.prepareCall(query);
    	  statement.registerOutParameter(1, Types.INTEGER);
    	  statement.setInt(2, ID);
    	  ResultSet result = statement.executeQuery();
    	  System.out.println("Item with ID " + id + " was retrieved");
    	  connection.close();
    	  
      } else {
    	  System.out.println("Option selection not recognized...");
    	  connection.close();
      }
   }
}