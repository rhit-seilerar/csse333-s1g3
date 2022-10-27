import java.io.File;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.json.JSONObject;

//import org.json.JSONObject;

public class StardewHoes {
   public static String nextLine(Scanner scanner)
   {
      while(!scanner.hasNextLine());
      return scanner.nextLine();
   }
   
   public static void main(String[] args) throws Exception
   {
      String url = "jdbc:sqlserver://${dbServer};databaseName=${dbName};user=${user};password={${pass}}";
      
      Scanner scanner = new Scanner(System.in);
      Random random = new SecureRandom();
      
      String defaultServer   = (args.length >= 1) ? args[0] : "titan.csse.rose-hulman.edu";
      String defaultDatabase = (args.length >= 2) ? args[1] : "StardewHoes10";
      String appUsername = "StardewHoesapp10";
      String appPassword = "Password1234";
      
      url = url.replace("${dbServer}", defaultServer).replace("${dbName}", defaultDatabase).replace("${user}", appUsername).replace("${pass}", appPassword);
      Connection connection = DriverManager.getConnection(url);
      
      boolean loop = true;
      boolean loggedIn = false;
      while(loop) {
         System.out.print("What action would you like to perform? (type h for help)\n> ");
         String modeStr = nextLine(scanner);
         char mode = (modeStr.length() > 0) ? modeStr.charAt(0) : '\n';
         
         switch(mode) {
            // Quit / Exit
            case 'q':
            case 'x': {
               System.out.println("Exiting");
               loop = false;
            } break;
            
            // Login
            case 'l': {
               System.out.println("Login selected");
               
               System.out.print("Please provide your username:\n> ");
               String username = nextLine(scanner);
               
               System.out.print("Please provide your password:\n> ");
               String password = nextLine(scanner);
               
               CallableStatement statement = connection.prepareCall("{? = call get_Login(?)}");
               statement.registerOutParameter(1, Types.INTEGER);
               statement.setString(2, username);
               ResultSet resultSet = statement.executeQuery();
               
               if(resultSet.isClosed() || !resultSet.next()) {
                  System.out.printf("ERROR: Failed to login %s\n", username);
               } else {
                  byte[] storedHash = resultSet.getBytes(2);
                  byte[] storedSalt = resultSet.getBytes(3);
                  byte[] givenHash = hashPassword(password, storedSalt);
                  
                  if(givenHash.equals(storedHash)) {
                     System.out.printf("ERROR: Failed to login %s\n", username);
                  } else {
                     System.out.printf("Successfully logged in %s\n", username);
                     loop = false;
                     loggedIn = true;
                  }
               }
            } break;
            
            // Register
            case 'r': {
               System.out.println("Registration selected");
               
               System.out.print("Please provide a username:\n> ");
               String username = nextLine(scanner);
               
               System.out.print("Please provide a password:\n> ");
               String password = nextLine(scanner);
               
               byte[] salt = new byte[16];
               random.nextBytes(salt);
               byte[] hash = hashPassword(password, salt);
               
               CallableStatement statement = connection.prepareCall("{? = call insert_Login(?, ?, ?)}");
               statement.registerOutParameter(1, Types.INTEGER);
               statement.setString(2, username);
               statement.setBytes(3, hash);
               statement.setBytes(4, salt);
               statement.execute();
               int result = statement.getInt(1);
               
               if(result != 0) {
                  System.out.printf("ERROR: Failed to register %s\n", username);
               } else {
                  System.out.printf("Successfully registered %s\n", username);
                  
                  System.out.print("Would you like to login with this account? [Y/n]\n> ");
                  String response = scanner.nextLine();
                  if(!response.equals("n")) {
                     System.out.printf("Successfully logged in %s\n", username);
                     loop = false;
                     loggedIn = true;
                  }
               }
            } break;
            
            // Help
            default:
            System.out.println("Unknown option. Here are the recognized options:");
            case 'h':
            case '?': {
               System.out.println("q or x: Exit");
               System.out.println("l: Login");
               System.out.println("r: Register an account");
               System.out.println("h or ?: Show this help menu");
            } break;
         }
      }
      
      loop = true;
      while(loggedIn && loop) {
         System.out.print("What action would you like to perform? (type h for help)\n> ");
         String modeStr = nextLine(scanner);
         char mode = (modeStr.length() > 0) ? modeStr.charAt(0) : '\n';
         
         switch(mode) {
            // Quit / Exit
            case 'q':
            case 'x': {
               System.out.println("Exiting");
               loop = false;
            } break;
            
            // Populate
            case 'p': {
               System.out.println("Populate selected");
               populateDatabase(connection);
            } break;
            
            // Get
            case 'g': {
               System.out.println("Retrieval selected");
               
               System.out.print("Please provide the item's ID (leave empty for null):\n> ");
               String idStr = nextLine(scanner);
               
               System.out.print("Please provide the item's name (leave empty for null):\n> ");
               String nameStr = nextLine(scanner);
               
               System.out.print("Please provide the item's quality (leave empty for null):\n> ");
               String qualityStr = nextLine(scanner);
               
               System.out.print("Please provide the item's price (leave empty for null):\n> ");
               String priceStr = nextLine(scanner);
               
               String query = "{? = call get_Item(?, ?, ?, ?)}";
               CallableStatement statement = connection.prepareCall(query);
               statement.registerOutParameter(1, Types.INTEGER);
               
               if(idStr.length() > 0) statement.setInt(2, Integer.valueOf(idStr));
               else statement.setNull(2, Types.INTEGER);
               
               if(nameStr.length() > 0) statement.setString(3, nameStr);
               else statement.setNull(3, Types.VARCHAR);
               
               if(qualityStr.length() > 0) statement.setInt(4, Integer.valueOf(qualityStr));
               else statement.setNull(4, Types.TINYINT);
               
               if(priceStr.length() > 0) statement.setInt(5, Integer.valueOf(priceStr));
               else statement.setNull(5, Types.INTEGER);
               
               ResultSet resultSet = statement.executeQuery();
               
               System.out.println("        | ID         | Name                                     | Quality | Price");
               int i = 0;
               while(!resultSet.isClosed() && resultSet.next()) {
                  System.out.printf(" %-6d | %-10d | %-40s | %d       | %d\n", i, resultSet.getInt(1), resultSet.getString(2), resultSet.getInt(3), resultSet.getInt(4));
                  i++;
               }
               
               int result = statement.getInt(1);
               if(result == 0) {
                  System.out.printf("Successfully retrieved Item with ID %s\n", idStr);
               } else {
                  System.out.printf("ERROR in getItem: Failed with error code %d\n", result);
               }
            } break;
            
            // Insert
            case 'i': {
               System.out.print("Insert selected\nPlease provide the item's name:\n> ");
               String name = nextLine(scanner);
               
               System.out.print("Please provide the item's quality (0 for normal, 3 for iridium):\n> ");
               String quality = nextLine(scanner);
               
               System.out.print("Please provide the item's base price:\n> ");
               String baseprice = nextLine(scanner);
               
               int basePrice = Integer.parseInt(baseprice);
               int qual = Integer.parseInt(quality);
               
               insertItem(connection, name, qual, basePrice);
            } break;
            
            // Update
            case 'u': {
               System.out.print("Update selected\nPlease provide the item's id:\n> ");
               String id = nextLine(scanner);
               int ID = Integer.parseInt(id);
               System.out.print("Please provide the item's new name:\n> ");
               String name = nextLine(scanner);
               System.out.print("Please provide the item's new quality (0 for normal, 3 for iridium):\n> ");
               String qual = nextLine(scanner);
               Object quality = null;
                System.out.print("Please provide the item's new base price:\n> ");
                String bp = nextLine(scanner);
                Object basePrice = null;
                String query = "{? = call update_Item(?, ?, ?, ?)}";
                CallableStatement statement = connection.prepareCall(query);
                statement.registerOutParameter(1, Types.INTEGER);
                statement.setInt(2, ID);
                
                if(name == "null" || name == "") {
                   name = null;
                  }
                  
                  if(qual == "null" || qual == "") {
                     quality = null;
                  } else {
                     quality = Integer.parseInt(qual);
                  }
                  
                  if(bp == "null" || bp == "") {
                     basePrice = null;
                  } else {
                     basePrice = Integer.parseInt(bp);
                  }
                  if (name != null) {
                     statement.setString(3, name);
                  } else {
                     statement.setNull(3, Types.VARCHAR);
                  }
                  if (basePrice != null) {
                   statement.setInt(5, (int) basePrice);
                } else {
                   statement.setNull(5, Types.INTEGER);
               }
               if(quality != null) {
                  statement.setInt(4, (int) quality);
               }
               else {
                  statement.setInt(4, Types.INTEGER);
               }
               
               statement.execute();
               System.out.println("statement executed with return value " + statement.getInt(1));
               
            } break;
            
            // Delete
            case 'd': {
               System.out.print("Delete selected");
               
               System.out.print("Please provide the item's ID (leave empty for null):\n> ");
               String idStr = nextLine(scanner);
               
               System.out.print("Please provide the item's name (leave empty for null):\n> ");
               String nameStr = nextLine(scanner);
               
               System.out.print("Please provide the item's quality (leave empty for null):\n> ");
               String qualityStr = nextLine(scanner);
               
               System.out.print("Please provide the item's price (leave empty for null):\n> ");
               String priceStr = nextLine(scanner);
               
               String query = "{? = call delete_Item(?, ?, ?, ?)}";
               CallableStatement statement = connection.prepareCall(query);
               statement.registerOutParameter(1, Types.INTEGER);
               
               if(idStr.length() > 0) statement.setInt(2, Integer.valueOf(idStr));
               else statement.setNull(2, Types.INTEGER);
               
               if(nameStr.length() > 0) statement.setString(3, nameStr);
               else statement.setNull(3, Types.VARCHAR);
               
               if(qualityStr.length() > 0) statement.setInt(4, Integer.valueOf(qualityStr));
               else statement.setNull(4, Types.TINYINT);
               
               if(priceStr.length() > 0) statement.setInt(5, Integer.valueOf(priceStr));
               else statement.setNull(5, Types.INTEGER);
               
               statement.execute();
               
               int result = statement.getInt(1);
               if(result == 0)
                  System.out.printf("Successfully deleted items\n");
               else
               System.out.printf("ERROR in deleteItem: Failed with error code %d\n", result);
            } break;
            
            // Help
            default:
               System.out.println("Unknown option. Here are the recognized options:");
               case 'h':
               case '?': {
                  System.out.println("q or x: Exit");
               System.out.println("p: Populate the database");
               System.out.println("g: Retrieve data from the database");
               System.out.println("i: Insert new data into the database");
               System.out.println("u: Update data in the database");
               System.out.println("d: Delete data from the database");
               System.out.println("h or ?: Show this help menu");
            } break;
         }
      }
      
      scanner.close();
      connection.close();
   }
   
   public static byte[] hashPassword(String password, byte[] salt) throws Exception
   {
      KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      byte[] hash = keyFactory.generateSecret(spec).getEncoded();
      return hash;
   }
   
   @SuppressWarnings("unchecked")
   public static void populateDatabase(Connection connection) throws Exception
   {
      String fileData = Files.readString((new File("data/Data/Crops.json")).toPath());
      JSONObject cropsRoot = new JSONObject(fileData);
      JSONObject cropsContent = cropsRoot.getJSONObject("content");
      
      fileData = Files.readString((new File("data/Data/ObjectInformation.json")).toPath());
      JSONObject objsRoot = new JSONObject(fileData);
      JSONObject objsContent = objsRoot.getJSONObject("content");
      
      HashMap<String, Integer> IdMap = new HashMap<>();
      
      Iterator<String> keys = objsContent.keys();
      while(keys.hasNext()) {
         String itemId = keys.next();
         String[] values = objsContent.getString(itemId).split("/");
         
         if(itemId.equals("770")) {
            continue;
         }
         
         if(itemId.equals("906")) {
            System.out.println("hi");
         }
         
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
            
            itemDBId = insertSeed(connection, name, price, season);
         } else if(types[0].equals("Fish")) {
            itemDBId = insertFish(connection, name, null, price);
         } else if(types[0].equals("Cooking")) {
            itemDBId = insertFood(connection, name, price);
         } else if(types[0].equals("Basic")) {
            if(types.length > 1) {
               int category = Integer.valueOf(types[1]);
               switch(category) {
                  case -5:
                  case -6:
                  case -14:
                  case -18: {
                     itemDBId = insertAnimalProduct(connection, name, null, price);
                  } break;
                  
                  case 17: {
                     if(itemId.equals("417")) itemDBId = insertProduce(connection, name, null, price);
                     else if(itemId.equals("430")) itemDBId = insertAnimalProduct(connection, name, null, price);
                     else itemDBId = insertItem(connection, name, null, price);
                  } break;
                  
                  case -26:
                  case -27: {
                     itemDBId = insertArtisanGood(connection, name, null, price, 0.0);
                  } break;
                  
                  case -74: itemDBId = insertPlantProduct(connection, name, null, price, "Vegetable"); break;
                  case -79: itemDBId = insertPlantProduct(connection, name, null, price, "Fruit"); break;
                  case -80: itemDBId = insertPlantProduct(connection, name, null, price, "Flower"); break;
                  case -81: itemDBId = insertPlantProduct(connection, name, null, price, "Forage"); break;
                  
                  default: itemDBId = insertItem(connection, name, null, price);
               }
            } else {
               itemDBId = insertProduce(connection, name, null, price);
            }
         } else {
            // Minerals, Quest, asdf, Crafting, Arch, Ring
            itemDBId = insertItem(connection, name, null, price);
         }
         
         IdMap.put(name, itemDBId);
      }
      
      fileData = Files.readString((new File("data/Data/FarmAnimals.json")).toPath());
      JSONObject animalsRoot = new JSONObject(fileData);
      JSONObject animalsContent = animalsRoot.getJSONObject("content");
      
      return;
      /*
      keys = animalsContent.keys();
      while(keys.hasNext()) {
         String name = keys.next();
         String[] values = animalsContent.getString(name).split("/");
         
         Integer produceDBId1 = IdMap.get(values[2]);
         Integer produceDBId2 = IdMap.get(values[3]);
         
         Integer price = null;
         if(name.contains("Chicken")) price = 800;
         else if(name.contains("Duck")) price = 1200;
         else if(name.contains("Rabbit")) price = 8000;
         else if(name.contains("Cow")) price = 1500;
         else if(name.contains("Goat")) price = 4000;
         else if(name.contains("Sheep")) price = 8000;
         else if(name.contains("Pig")) price = 16000;
         
         int animalDBId = insertAnimal(connection, name, price);
         IdMap.put(name, animalDBId);
         
         if(produceDBId1 != null) insertProduces(connection, animalDBId, produceDBId1);
         if(produceDBId2 != null) insertProduces(connection, animalDBId, produceDBId2);
      }
      
      fileData = Files.readString((new File("data/Data/CookingRecipes.json")).toPath());
      JSONObject cookingRoot = new JSONObject(fileData);
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
      
      fileData = Files.readString((new File("data/Data/NPCDispositions.json")).toPath());
      JSONObject npcRoot = new JSONObject(fileData);
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
      
      insertProfession(connection, "AnimalProduct Price", 1.2);
      insertProfession(connection, "PlantProduct Price", 1.1);
      insertProfession(connection, "ArtisanGood Price", 1.4);
      insertProfession(connection, "MetalBar Price", 1.5);
      insertProfession(connection, "Gem Price", 1.3);
      insertProfession(connection, "Wood Price", 1.25);
      insertProfession(connection, "Syrup Price", 1.25);
      insertProfession(connection, "Fish Price", 1.25);
      insertProfession(connection, "Fish Price", 1.5);
      
      insertGenerates(connection, IdMap.get("Tea Leaves"), IdMap.get("Green Tea"));
      insertGenerates(connection, IdMap.get("Coffee Bean"), IdMap.get("Coffee"));
      insertGenerates(connection, IdMap.get("Wool"), IdMap.get("Cloth"));
      
      updateArtisanGood(connection, IdMap.get("Juice"), null, null, 0, 2.25);
      Statement statement = connection.createStatement();
      ResultSet resultSet = statement.executeQuery("select id from PlantProduct where Type = 'Vegetable'");
      while(resultSet.next()) {
         id = resultSet.getInt("ID");
         insertGenerates(connection, id, IdMap.get("Juice"));
      }
      
      updateArtisanGood(connection, IdMap.get("Honey"), null, null, null, 2.0);
      statement = connection.createStatement();
      resultSet = statement.executeQuery("select id from PlantProduct where Type = 'Flower'");
      while(resultSet.next()) {
         id = resultSet.getInt("ID");
         insertGenerates(connection, id, IdMap.get("Honey"));
      }
      
      int id0 = insertArtisanGood(connection, "Wine", 0, 0, 3.0*1.00);
      int id1 = insertArtisanGood(connection, "Wine", 1, 0, 3.0*1.25);
      int id2 = insertArtisanGood(connection, "Wine", 2, 0, 3.0*1.50);
      int id3 = insertArtisanGood(connection, "Wine", 3, 0, 3.0*2.00);
      statement = connection.createStatement();
      resultSet = statement.executeQuery("select id from PlantProduct where Type = 'Fruit'");
      while(resultSet.next()) {
         id = resultSet.getInt("ID");
         insertGenerates(connection, id, id0);
         insertGenerates(connection, id, id1);
         insertGenerates(connection, id, id2);
         insertGenerates(connection, id, id3);
      }
      
      id0 = insertArtisanGood(connection, "Pale Ale", 0, 300, 0);
      id1 = insertArtisanGood(connection, "Pale Ale", 1, 375, 0);
      id2 = insertArtisanGood(connection, "Pale Ale", 2, 450, 0);
      id3 = insertArtisanGood(connection, "Pale Ale", 3, 600, 0);
      insertGenerates(connection, IdMap.get("Hops"), id0);
      insertGenerates(connection, IdMap.get("Hops"), id1);
      insertGenerates(connection, IdMap.get("Hops"), id2);
      insertGenerates(connection, IdMap.get("Hops"), id3);
      
      id0 = insertArtisanGood(connection, "Beer", 0, 200, 0);
      id1 = insertArtisanGood(connection, "Beer", 1, 250, 0);
      id2 = insertArtisanGood(connection, "Beer", 2, 300, 0);
      id3 = insertArtisanGood(connection, "Beer", 3, 400, 0);
      insertGenerates(connection, IdMap.get("Wheat"), id0);
      insertGenerates(connection, IdMap.get("Wheat"), id1);
      insertGenerates(connection, IdMap.get("Wheat"), id2);
      insertGenerates(connection, IdMap.get("Wheat"), id3);
      
      id0 = insertArtisanGood(connection, "Mead", 0, 200, 0);
      id1 = insertArtisanGood(connection, "Mead", 1, 250, 0);
      id2 = insertArtisanGood(connection, "Mead", 2, 300, 0);
      id3 = insertArtisanGood(connection, "Mead", 3, 400, 0);
      insertGenerates(connection, IdMap.get("Honey"), id0);
      insertGenerates(connection, IdMap.get("Honey"), id1);
      insertGenerates(connection, IdMap.get("Honey"), id2);
      insertGenerates(connection, IdMap.get("Honey"), id3);
      
      id0 = insertArtisanGood(connection, "Cheese", 0, 230, 0);
      id1 = insertArtisanGood(connection, "Cheese", 1, 287, 0);
      id2 = insertArtisanGood(connection, "Cheese", 2, 345, 0);
      id3 = insertArtisanGood(connection, "Cheese", 3, 460, 0);
      insertGenerates(connection, IdMap.get("Milk"), id0);
      insertGenerates(connection, IdMap.get("Milk"), id1);
      insertGenerates(connection, IdMap.get("Milk"), id2);
      insertGenerates(connection, IdMap.get("Milk"), id3);
      insertGenerates(connection, IdMap.get("Large Milk"), id0);
      insertGenerates(connection, IdMap.get("Large Milk"), id1);
      insertGenerates(connection, IdMap.get("Large Milk"), id2);
      insertGenerates(connection, IdMap.get("Large Milk"), id3);
      
      id0 = insertArtisanGood(connection, "Goat Cheese", 0, 400, 0);
      id1 = insertArtisanGood(connection, "Goat Cheese", 1, 500, 0);
      id2 = insertArtisanGood(connection, "Goat Cheese", 2, 600, 0);
      id3 = insertArtisanGood(connection, "Goat Cheese", 3, 800, 0);
      insertGenerates(connection, IdMap.get("Goat Milk"), id0);
      insertGenerates(connection, IdMap.get("Goat Milk"), id1);
      insertGenerates(connection, IdMap.get("Goat Milk"), id2);
      insertGenerates(connection, IdMap.get("Goat Milk"), id3);
      insertGenerates(connection, IdMap.get("L. Goat Milk"), id0);
      insertGenerates(connection, IdMap.get("L. Goat Milk"), id1);
      insertGenerates(connection, IdMap.get("L. Goat Milk"), id2);
      insertGenerates(connection, IdMap.get("L. Goat Milk"), id3);
      
      id0 = insertArtisanGood(connection, "Mayonnaise", 0, 190, 0);
      id1 = insertArtisanGood(connection, "Mayonnaise", 1, 237, 0);
      id2 = insertArtisanGood(connection, "Mayonnaise", 2, 285, 0);
      id3 = insertArtisanGood(connection, "Mayonnaise", 3, 380, 0);
      insertGenerates(connection, IdMap.get("Egg"), id0);
      insertGenerates(connection, IdMap.get("Egg"), id1);
      insertGenerates(connection, IdMap.get("Egg"), id2);
      insertGenerates(connection, IdMap.get("Egg"), id3);
      insertGenerates(connection, IdMap.get("Large Egg"), id0);
      insertGenerates(connection, IdMap.get("Large Egg"), id1);
      insertGenerates(connection, IdMap.get("Large Egg"), id2);
      insertGenerates(connection, IdMap.get("Large Egg"), id3);
      insertGenerates(connection, IdMap.get("Ostrich Egg"), id0);
      insertGenerates(connection, IdMap.get("Ostrich Egg"), id1);
      insertGenerates(connection, IdMap.get("Ostrich Egg"), id2);
      insertGenerates(connection, IdMap.get("Ostrich Egg"), id3);
      insertGenerates(connection, IdMap.get("Golden Egg"), id0);
      insertGenerates(connection, IdMap.get("Golden Egg"), id1);
      insertGenerates(connection, IdMap.get("Golden Egg"), id2);
      insertGenerates(connection, IdMap.get("Golden Egg"), id3);
      
      insertGenerates(connection, IdMap.get("Duck Egg"), IdMap.get("Duck Mayonnaise"));
      insertGenerates(connection, IdMap.get("Void Egg"), IdMap.get("Void Mayonnaise"));
      insertGenerates(connection, IdMap.get("Dinosaur Egg"), IdMap.get("Dinosaur Mayonnaise"));
      
      insertGenerates(connection, IdMap.get("Truffle"), IdMap.get("Truffle Oil"));
      insertGenerates(connection, IdMap.get("Corn"), IdMap.get("Oil"));
      insertGenerates(connection, IdMap.get("Sunflower Seeds"), IdMap.get("Oil"));
      insertGenerates(connection, IdMap.get("Sunflower"), IdMap.get("Oil"));
      
      updateArtisanGood(connection, IdMap.get("Pickles"), null, null, 50, 2.0);
      statement = connection.createStatement();
      resultSet = statement.executeQuery("select id from PlantProduct where Type = 'Vegetable'");
      insertGenerates(connection, IdMap.get("Ginger"), IdMap.get("Pickles"));
      while(resultSet.next()) {
         id = resultSet.getInt("ID");
         insertGenerates(connection, id, IdMap.get("Pickles"));
      }
      
      updateArtisanGood(connection, IdMap.get("Jelly"), null, null, 50, 2.0);
      statement = connection.createStatement();
      resultSet = statement.executeQuery("select id from PlantProduct where Type = 'Fruit'");
      while(resultSet.next()) {
         id = resultSet.getInt("ID");
         insertGenerates(connection, id, IdMap.get("Jelly"));
      }
      
      insertGenerates(connection, IdMap.get("Sturgeon Roe"), IdMap.get("Caviar"));
      
      updateArtisanGood(connection, IdMap.get("Aged Roe"), null, null, 0, 2.0);
      insertGenerates(connection, IdMap.get("Roe"), IdMap.get("Aged Roe"));
      */
   }
   
   public static int insertProfession(Connection connection, String category, double multiplier) throws Exception
   {
      String query = "{? = call insert_Profession(?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, category);
      statement.setDouble(3, multiplier);
      statement.registerOutParameter(4, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(4);
      
      if(result == 0)
         System.out.printf("Successfully inserted Profession for category %s with boost %f.\n", category, multiplier);
         else
         System.out.printf("ERROR in insertProfesison: Failed with error code %d.\n", result);
         
         return id;
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
         
         if(result == 0)
         System.out.printf("Successfully inserted Shop with name %s, address %s, schedule %s, and shopkeeper %d.\n", name, address, schedule, shopkeeperId);
         else
         System.out.printf("ERROR in insertShopkeeper: Failed with error code %d.\n", result);
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
      
      if(result == 0)
      System.out.printf("Successfully inserted Shopkeeper with name %s.\n", name);
      else
      System.out.printf("ERROR in insertShopkeeper: Failed with error code %d.\n", result);
      
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
      
      if(result == 0)
         System.out.printf("Successfully inserted Villager with name %s.\n", name);
      else
         System.out.printf("ERROR in insertVillager: Failed with error code %d.\n", result);
      
      return id;
   }
   
   public static void insertGenerates(Connection connection, int produceID, int productID) throws Exception
   {
      String query = "{? = call insert_Generates(?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, produceID);
      statement.setInt(3, productID);
      statement.execute();
      int result = statement.getInt(1);
      
      if(result == 0)
         System.out.printf("Successfully inserted Generates relation for produce %d and product %d.\n", produceID, productID);
      else
         System.out.printf("ERROR in insertGenerates: Failed with error code %d.\n", result);
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
      
      if(result == 0)
         System.out.printf("Successfully inserted Produces relation for animal %d and animal product %d.\n", animalID, produceID);
      else
         System.out.printf("ERROR in insertProduces: Failed with error code %d.\n", result);
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
      
      if(result == 0)
         System.out.printf("Successfully inserted HasIngredient relation for ingredient %d and food %d.\n", ingredientId, foodId);
      else
         System.out.printf("ERROR in insertHasIngredient: Failed with error code %d.\n", result);
   }
   
   public static int insertSeed(Connection connection, String name, int basePrice, String season) throws Exception
   {
      String query = "{? = call insert_Seed(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      statement.setInt(3, basePrice);
      statement.setString(4, season);
      statement.registerOutParameter(5, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(5);
      
      if(result == 0)
         System.out.printf("Successfully inserted Seed with name %s, price %d, and season %s.\n", name, basePrice, season);
      else
         System.out.printf("ERROR in insertSeed: Failed with error code %d.\n", result);
      
      return id;
   }
   
   public static int insertArtisanGood(Connection connection, String name, Integer quality, int basePrice, double multiplier) throws Exception
   {
      String query = "{? = call insert_ArtisanGood(?, ?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      if(quality == null) statement.setNull(3, Types.INTEGER);
      else statement.setInt(3, quality);
      statement.setInt(4, basePrice);
      statement.setDouble(5, multiplier);
      statement.registerOutParameter(6, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(6);
      
      if(result == 0)
         System.out.printf("Successfully inserted ArtisanGood with name %s, quality %d, price %d, and multiplier %f.\n", name, quality, basePrice, multiplier);
      else
         System.out.printf("ERROR in insertArtisanGood: Failed with error code %d.\n", result);
      
      return id;
   }
   
   public static void updateArtisanGood(Connection connection, int id, String name, Integer quality, Integer basePrice, Double multiplier) throws Exception
   {
      String query = "{? = call update_ArtisanGood(?, ?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, id);
      statement.setString(3, name);
      if(quality == null) statement.setNull(4, Types.INTEGER);
      else statement.setInt(4, quality);
      statement.setInt(5, basePrice);
      statement.setDouble(6, multiplier);
      statement.execute();
      int result = statement.getInt(1);
      
      if(result == 0)
         System.out.printf("Successfully updated ArtisanGood with ID %d.\n", id);
      else
         System.out.printf("ERROR in updateArtisanGood: Failed with error code %d.\n", result);
   }
   
   public static int insertPlantProduct(Connection connection, String name, Integer quality, int basePrice, String type) throws Exception
   {
      String query = "{? = call insert_PlantProduct(?, ?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      if(quality == null) statement.setNull(3, Types.INTEGER);
      else statement.setInt(3, quality);
      statement.setInt(4, basePrice);
      statement.setString(5, type);
      statement.registerOutParameter(6, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(6);
      
      if(result == 0)
         System.out.printf("Successfully inserted PlantProduct with name %s (%s), quality %d, and price %d.\n", name, type, quality, basePrice);
      else
         System.out.printf("ERROR in insertPlantProduct: Failed with error code %d.\n", result);
      
      return id;
   }
   
   public static int insertAnimal(Connection connection, String name, Integer basePrice) throws Exception
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
      
      if(result == 0)
         System.out.printf("Successfully inserted Animal with name %s and price %d.\n", name, basePrice);
      else
         System.out.printf("ERROR in insertAnimal: Failed with error code %d.\n", result);
      
      return id;
   }
   
   public static int insertAnimalProduct(Connection connection, String name, Integer quality, int basePrice) throws Exception
   {
      String query = "{? = call insert_AnimalProduct(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      if(quality == null) statement.setNull(3, Types.INTEGER);
      else statement.setInt(3, quality);
      statement.setInt(4, basePrice);
      statement.registerOutParameter(5, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(5);
      
      if(result == 0)
         System.out.printf("Successfully inserted AnimalProduct with name %s, qualiy %d, and price %d.\n", name, quality, basePrice);
      else
         System.out.printf("ERROR in insertAnimalProduct: Failed with error code %d.\n", result);
      
      return id;
   }
   
   public static int insertProduce(Connection connection, String name, Integer quality, int basePrice) throws Exception
   {
      String query = "{? = call insert_Produce(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      if(quality == null) statement.setNull(3, Types.INTEGER);
      else statement.setInt(3, quality);
      statement.setInt(4, basePrice);
      statement.registerOutParameter(5, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(5);
      
      if(result == 0)
         System.out.printf("Successfully inserted Produce with name %s, qualiy %d, and price %d.\n", name, quality, basePrice);
      else
         System.out.printf("ERROR in insertProduce: Failed with error code %d.\n", result);
      
      return id;
   }
   
   public static int insertFish(Connection connection, String name, Integer quality, int basePrice) throws Exception
   {
      String query = "{? = call insert_Fish(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      if(quality == null) statement.setNull(3, Types.INTEGER);
      else statement.setInt(3, quality);
      statement.setInt(4, basePrice);
      statement.registerOutParameter(5, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(5);
      
      if(result == 0)
         System.out.printf("Successfully inserted Fish with name %s, qualiy %d, and price %d.\n", name, quality, basePrice);
      else
         System.out.printf("ERROR in insertFish: Failed with error code %d.\n", result);
      
      return id;
   }
   
   public static int insertFood(Connection connection, String name, int basePrice) throws Exception
   {
      String query = "{? = call insert_Food(?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      statement.setInt(3, basePrice);
      statement.registerOutParameter(4, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(4);
      
      if(result == 0)
         System.out.printf("Successfully inserted Food with name %s and price %d.\n", name, basePrice);
      else
         System.out.printf("ERROR in insertFood: Failed with error code %d.\n", result);
      
      return id;
   }
   
   public static int insertItem(Connection connection, String name, Integer quality, int basePrice) throws Exception
   {
      String query = "{? = call insert_Item(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      if(quality == null) statement.setNull(3, Types.INTEGER);
      else statement.setInt(3, quality);
      statement.setInt(4, basePrice);
      statement.registerOutParameter(5, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(5);
      
      if(result == 0)
         System.out.printf("Successfully inserted Item with name %s, qualiy %d, and price %d.\n", name, quality, basePrice);
      else
         System.out.printf("ERROR in insertItem: Failed with error code %d.\n", result);
      
      return id;
   }
}