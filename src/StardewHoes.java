import java.io.FileReader;
import java.io.Reader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;

import org.json.JSONObject;

public class StardewHoes {
   public static void main(String[] args) throws Exception
   {
      String url = "jdbc:sqlserver://${dbServer};databaseName=${dbName};user=${user};password={${pass}}";
      
      String defaultServer   = (args.length >= 1) ? args[0] : "titan.csse.rose-hulman.edu";
      String defaultDatabase = (args.length >= 2) ? args[1] : "StardewHoes10";
      
      Scanner scanner = new Scanner(System.in);
      String username, password;
      if(args.length >= 3) {
         username = args[2];
      } else {
         System.out.print("What is your username?\n> ");
         username = scanner.nextLine();
         scanner.close();
      }
      
      if(args.length >= 4) {
         password = args[3];
      } else {
         System.out.print("What is your password?\n> ");
         password = scanner.nextLine();
         scanner.close();
      }
      
      url = url.replace("${dbServer}", defaultServer).replace("${dbName}", defaultDatabase).replace("${user}", username).replace("${pass}", password);
      Connection connection = DriverManager.getConnection(url);
      
      boolean loop = true;
      while(loop) {
         System.out.print("What action would you like to perform? (type h for help)\n> ");
         char mode = scanner.nextLine().strip().charAt(0);
         
         switch(mode) {
            // Quit / Exit
            case 'q':
            case 'x': {
               loop = false;
            } break;
            
            // Populate
            case 'p': {
               populateDatabase(connection);
            } break;
            
            // Get
            case 'g': {
               
            } break;
            
            // Insert
            case 'i': {
               
            } break;
            
            // Update
            case 'u': {
               
            } break;
            
            // Delete
            case 'd': {
               
            } break;
            
            // Help
            case 'h':
            case '?':
            default: {
               System.out.println("q or x: Exit");
               System.out.println("p: Populate the database");
               System.out.println("g: Retrieve data from the database");
               System.out.println("i: Insert new data into the database");
               System.out.println("u: Update data in the database");
               System.out.println("d: Delete data from the database");
               System.out.println("h or ?: Show this help menu");
            }
         }
      }
      
      connection.close();
   }
   
   @SuppressWarnings("unchecked")
   public static void populateDatabase(Connection connection) throws Exception
   {
      Reader reader = new FileReader("data/Data/Crops.json");
      JSONObject cropsRoot = new JSONObject(reader.read());
      reader.close();
      JSONObject cropsContent = cropsRoot.getJSONObject("content");
      
      reader = new FileReader("data/Data/ObjectInformation.json");
      JSONObject objsRoot = new JSONObject(reader.read());
      reader.close();
      JSONObject objsContent = objsRoot.getJSONObject("content");
      
      reader = new FileReader("data/Data/FarmAnimals.json");
      JSONObject animalsRoot = new JSONObject(reader.read());
      reader.close();
      JSONObject animalsContent = animalsRoot.getJSONObject("content");
      
      HashMap<String, Integer> IdMap = new HashMap<>();
      
      Iterator<String> keys = objsContent.keys();
      while(keys.hasNext()) {
         String itemId = keys.next();
         String[] values = objsContent.getString(itemId).split("/");
         
         String name = values[0];
         int price = Integer.valueOf(values[1]);
         
         //TODO: Handle quality
         String[] types = values[3].split(" ");
         int itemDBId;
         if(types[0].equals("Seeds")) {
            String season = cropsContent.getString(itemId).split("/")[1];
            season = season.replace("spring", "Spring").replace("summer", "Summer").replace("fall", "Fall").replace(" ", "/");
            if(season.equals("Spring/Summer/Fall")) season = "All";
            if(!season.contains("Spring") && !season.contains("Summer") && !season.contains("Fall")) season = "None";
            
            itemDBId = insertSeed(connection, name, 0, price, season);
         } else if(types[0].equals("Fish")) {
            itemDBId = insertFish(connection, name, 0, price);
         } else if(types[0].equals("Cooking")) {
            itemDBId = insertFood(connection, name, 0, price);
         } else if(types[0].equals("Basic")) {
            if(types.length > 1) {
               int category = Integer.valueOf(types[1]);
               switch(category) {
                  case -5:
                  case -6:
                  case -14:
                  case -18: {
                     itemDBId = insertAnimalProduct(connection, name, 0, price);
                  } break;
                  
                  case 17: {
                     if(itemId.equals("417")) itemDBId = insertPlantProduct(connection, name, 0, price, "Fruit");
                     else if(itemId.equals("430")) itemDBId = insertAnimalProduct(connection, name, 0, price);
                     else itemDBId = insertItem(connection, name, 0, price);
                  } break;
                  
                  case -26:
                  case -27: {
                     double multiplier = 0; // TODO: Handle this
                     itemDBId = insertArtisanGood(connection, name, 0, price, multiplier);
                  } break;
                  
                  case -74: itemDBId = insertPlantProduct(connection, name, 0, price, "Vegetable"); break;
                  case -79: itemDBId = insertPlantProduct(connection, name, 0, price, "Fruit"); break;
                  case -80: itemDBId = insertPlantProduct(connection, name, 0, price, "Flower"); break;
                  case -81: itemDBId = insertPlantProduct(connection, name, 0, price, "Forage"); break;
                  
                  default: itemDBId = insertItem(connection, name, 0, price);
               }
            } else {
               itemDBId = insertProduce(connection, name, 0, price);
            }
         } else {
            // Minerals, Quest, asdf, Crafting, Arch, Ring
            itemDBId = insertItem(connection, name, 0, price);
         }
         
         IdMap.put(itemId, itemDBId);
      }
      
      keys = animalsContent.keys();
      while(keys.hasNext()) {
         String name = keys.next();
         String[] values = animalsContent.getString(name).split("/");
         
         Integer produceDBId1 = IdMap.get(values[2]);
         Integer produceDBId2 = IdMap.get(values[3]);
         
         int price = 0; // TODO: Get price working
         
         int animalDBId = insertAnimal(connection, name, price);
         IdMap.put(name, animalDBId);
         
         if(produceDBId1 != null) insertProduces(connection, animalDBId, produceDBId1);
         if(produceDBId2 != null) insertProduces(connection, animalDBId, produceDBId2);
      }
   }
   
   public static void insertProduces(Connection connection, int animalID, int produceID) throws Exception
   {
      String query = "{? = call insert_Produces(?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, animalID);
      statement.setInt(3, produceID);
      statement.execute();
      int result = statement.getInt(1);
   }
   
   public static int insertSeed(Connection connection, String name, int quality, int basePrice, String season) throws Exception
   {
      String query = "{? = call insert_Seed(?, ?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      statement.setInt(3, quality);
      statement.setInt(4, basePrice);
      statement.setString(5, season);
      statement.registerOutParameter(6, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(6);
      
      return id;
   }
   
   public static int insertArtisanGood(Connection connection, String name, int quality, int basePrice, double multiplier) throws Exception
   {
      String query = "{? = call insert_ArtisanGood(?, ?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      statement.setInt(3, quality);
      statement.setInt(4, basePrice);
      statement.setDouble(5, multiplier);
      statement.registerOutParameter(6, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(6);
      
      return id;
   }
   
   public static int insertPlantProduct(Connection connection, String name, int quality, int basePrice, String type) throws Exception
   {
      String query = "{? = call insert_PlantProduct(?, ?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      statement.setInt(3, quality);
      statement.setInt(4, basePrice);
      statement.setString(5, type);
      statement.registerOutParameter(6, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(6);
      
      return id;
   }
   
   public static int insertAnimal(Connection connection, String name, int basePrice) throws Exception
   {
      String query = "{? = call insert_Animal(?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      statement.setInt(3, basePrice);
      statement.registerOutParameter(4, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(4);
      
      return id;
   }
   
   public static int insertAnimalProduct(Connection connection, String name, int quality, int basePrice) throws Exception
   {
      String query = "{? = call insert_AnimalProduct(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      statement.setInt(3, quality);
      statement.setInt(4, basePrice);
      statement.registerOutParameter(5, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(5);
      
      return id;
   }
   
   public static int insertProduce(Connection connection, String name, int quality, int basePrice) throws Exception
   {
      String query = "{? = call insert_Produce(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      statement.setInt(3, quality);
      statement.setInt(4, basePrice);
      statement.registerOutParameter(5, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(5);
      
      return id;
   }
   
   public static int insertFish(Connection connection, String name, int quality, int basePrice) throws Exception
   {
      String query = "{? = call insert_Fish(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      statement.setInt(3, quality);
      statement.setInt(4, basePrice);
      statement.registerOutParameter(5, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(5);
      
      return id;
   }
   
   public static int insertFood(Connection connection, String name, int quality, int basePrice) throws Exception
   {
      String query = "{? = call insert_Food(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      statement.setInt(3, quality);
      statement.setInt(4, basePrice);
      statement.registerOutParameter(5, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(5);
      
      return id;
   }
   
   public static int insertItem(Connection connection, String name, int quality, int basePrice) throws Exception
   {
      String query = "{? = call insert_Item(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      statement.setInt(3, quality);
      statement.setInt(4, basePrice);
      statement.registerOutParameter(5, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(5);
      
      return id;
   }
}