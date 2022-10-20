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
      
      reader = new FileReader("data/Data/FarmAnimals.json");
      JSONObject animalsRoot = new JSONObject(reader.read());
      reader.close();
      JSONObject animalsContent = animalsRoot.getJSONObject("content");
      
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
      
      reader = new FileReader("data/Data/CookingRecipes.json");
      JSONObject cookingRoot = new JSONObject(reader.read());
      reader.close();
      JSONObject cookingContent = cookingRoot.getJSONObject("content");
      
      keys = cookingContent.keys();
      while(keys.hasNext()) {
         String name = keys.next();
         String[] values = cookingContent.getString(name).split("/");
         String[] inputs = values[0].split(" ");
         String[] outputs = values[2].split(" ");
         
         if(outputs.length > 2) {
            System.out.println("Error! Recipe has more than one yield type");
            break;
         }
         
         Integer resultId = IdMap.get(outputs[0]);
         
         for(int i = 0; i < inputs.length; i += 2) {
            Integer ingredientId = IdMap.get(inputs[i]);
            insertHasIngredient(connection, ingredientId, resultId);
         }
      }
      
      int mrQiId1 = insertShopkeeper(connection, "MisterQi1");
      int mrQiId2 = insertShopkeeper(connection, "MisterQi2");
      IdMap.put("MisterQi1", mrQiId1);
      IdMap.put("MisterQi2", mrQiId2);
      insertShop(connection, "WalnutRoom", "Ginger Island 1", "Always", mrQiId1);
      insertShop(connection, "Casino", "Calico Desert 2, in the back of the Oasis", "9a-11:50p", mrQiId2);
      
      String name = "TravelingMerchant";
      int id = insertShopkeeper(connection, name);
      IdMap.put(name, id);
      insertShop(connection, "TravelingCart", "Cindersap Forest, north of the upper pond", "6am to 8pm on Fridays and Saturdays, and 5pm to 2am during the Night Market.", id);
      
      name = "DesertTrader";
      id = insertShopkeeper(connection, name);
      IdMap.put(name, id);
      insertShop(connection, "TradingHut", "Next to the road through the Calico Desert", "6am to 2am every day, except during the Night Market.", id);
      
      name = "Morris";
      id = insertShopkeeper(connection, name);
      IdMap.put(name, id);
      insertShop(connection, "JojaMart", "Pelican Town 3", "Permanently closed.", id);
      
      name = "VolcanicDwarf";
      id = insertShopkeeper(connection, name);
      IdMap.put(name, id);
      insertShop(connection, "VolcanoShop", "In level 5 of the Ginger Island Volcano", "Always open.", id);
      
      name = "IslandTrader";
      id = insertShopkeeper(connection, name);
      IdMap.put(name, id);
      insertShop(connection, "IslandTradingStand", "On the way to the Ginger Island Volcano", "Always open.", id);
      
      name = "HatMouse";
      id = insertShopkeeper(connection, name);
      IdMap.put(name, id);
      insertShop(connection, "HatShop", "Abandoned house in the Cindersap Forest", "Always open.", id);
      
      name = "Bear";
      IdMap.put(name, insertVillager(connection, name));
      
      name = "Birdie";
      IdMap.put(name, insertVillager(connection, name));
      
      name = "Gil";
      IdMap.put(name, insertVillager(connection, name));
      
      name = "Gunther";
      IdMap.put(name, insertVillager(connection, name));
      
      reader = new FileReader("data/Data/NPCDispositions.json");
      JSONObject npcRoot = new JSONObject(reader.read());
      reader.close();
      JSONObject npcContent = npcRoot.getJSONObject("content");
      
      keys = npcContent.keys();
      while(keys.hasNext()) {
         name = keys.next();
         
         if(name.equals("Marlon")) {
            id = insertShopkeeper(connection, name);
            insertShop(connection, "AdventurerGuild", "Mountain 1", "2pm to 10pm each day, except for festivals.", id);
         } else if(name.equals("Flint")) {
            id = insertShopkeeper(connection, name);
            IdMap.put(name, id);
            insertShop(connection, "Blacksmith", "Mountain 1", "9am to 4pm each day, except for Winter 16, Fridays, and festivals.", id);
         } else if(name.equals("Robin")) {
            id = insertShopkeeper(connection, name);
            IdMap.put(name, id);
            insertShop(connection, "CarpentersShop", "24 Mountain Road", "9am to 5pm each day, except for Summer 18, Tuesdays, and festivals", id);
         } else if(name.equals("Willy")) {
            id = insertShopkeeper(connection, name);
            IdMap.put(name, id);
            insertShop(connection, "FishShop", "Beach 1", "8am to 5pm each day, except for non-rainy Saturdays, 10am to 2am on Spring 9, and festivals.", id);
         } else if(name.equals("Harvey")) {
            id = insertShopkeeper(connection, name);
            IdMap.put(name, id);
            insertShop(connection, "Clinic", "Pelican Town 1", "9am to 2pm on Tuesdays and Thursdays, and 9am to 12pm on Sundays, Mondays, Wednesdays, and Fridays. Closed for festivals.", id);
         } else if(name.equals("Alex")) {
            id = insertShopkeeper(connection, name);
            IdMap.put(name, id);
            insertShop(connection, "IceCreamStand", "Near the museum", "1pm to 5pm in the summer, except for Wednesdays, Summer 16, and rainy days.", id);
         } else if(name.equals("Marnie")) {
            id = insertShopkeeper(connection, name);
            IdMap.put(name, id);
            insertShop(connection, "AnimalRanch", "Cindersap Forest 1", "9am to 4pm, except for Mondays, Tuesdays, Fall 18, Winter 18, and festivals.", id);
         } else if(name.equals("Sandy")) {
            id = insertShopkeeper(connection, name);
            IdMap.put(name, id);
            insertShop(connection, "Oasis", "Calico Desert 2", "9am to 11:50pm, except for festivals.", id);
         } else if(name.equals("Pierre")) {
            id = insertShopkeeper(connection, name);
            IdMap.put(name, id);
            insertShop(connection, "GeneralShop", "Pelican Town 2", "9am to 5pm, except for festivals.", id);
         } else if(name.equals("Gus")) {
            id = insertShopkeeper(connection, name);
            IdMap.put(name, id);
            insertShop(connection, "Saloon", "Pelican Town 3", "12pm to 12am, except for festivals and until 4:30pm on Fall 4.", id);
         } else if(name.equals("Wizard")) {
            id = insertShopkeeper(connection, name);
            IdMap.put(name, id);
            insertShop(connection, "WizardTower", "Cindersap Forest 2", "6am to 11pm, except for Spring 24 and Winter 8.", id);
         } else if(name.equals("Lewis")) {
            id = insertShopkeeper(connection, name);
            IdMap.put(name, id);
            insertShop(connection, "MovieTheater", "Pelican Town 3", "9am to 9pm every day.", id);
         } else if(name.equals("Dwarf")) {
            id = insertShopkeeper(connection, name);
            IdMap.put(name, id);
            insertShop(connection, "MinesShop", "In the Mountain mines", "Always open.", id);
         } else if(name.equals("Krobus")) {
            id = insertShopkeeper(connection, name);
            IdMap.put(name, id);
            insertShop(connection, "SewerShop", "In the Pelican Town sewers", "Always open.", id);
         } else {
            id = insertVillager(connection, name);
         }
         
         IdMap.put(name, id);
      }
      
      //TODO: Profession, Generates, multiplier
   }
   
   public static void insertShop(Connection connection, String name, String address, String schedule, int shopkeeperId) throws Exception
   {
      String query = "{? = call insert_Shop(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      statement.setString(3, address);
      statement.setString(4, schedule);
      statement.setInt(5, shopkeeperId);
      statement.execute();
      int result = statement.getInt(1);
   }
   
   public static int insertShopkeeper(Connection connection, String name) throws Exception
   {
      String query = "{? = call insert_Shopkeeper(?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      statement.registerOutParameter(3, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(3);
      
      return id;
   }
   
   public static int insertVillager(Connection connection, String name) throws Exception
   {
      String query = "{? = call insert_Villager(?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      statement.registerOutParameter(3, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(3);
      
      return id;
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
   
   public static void insertHasIngredient(Connection connection, int ingredientId, int foodId) throws Exception
   {
      String query = "{? = call insert_HasIngredient(?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, ingredientId);
      statement.setInt(3, foodId);
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