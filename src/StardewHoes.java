import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;
import org.json.JSONObject;

import frames.LogInFrame;
import services.DatabaseConnectionService;

public class StardewHoes {
   // If false, the backup command-line UI will be used
   public static boolean useGUI = false;
   
   // Set this to '7' (or any non-null number really) to skip login.
   public static Integer permissions = null;
   
   public static Random random = new SecureRandom();
   
   public static String nextLine(Scanner scanner) throws Exception {
      while (!scanner.hasNextLine()) Thread.sleep(1);
      return scanner.nextLine();
   }
   
   public static void main(String[] args) throws Exception {
      StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
      encryptor.setPassword("827CJIXWp73P11No9cO5p9NGrL8mLG2k");
      encryptor.setAlgorithm("PBEWithMD5AndDES");
      
      Properties properties = new EncryptableProperties(encryptor);
      properties.load(new FileInputStream(".properties"));
      
      String server      = properties.getProperty("server");
      String database    = properties.getProperty("database");
      String appUsername = properties.getProperty("username");
      String appPassword = properties.getProperty("password");
      
      if(server == null || database == null || appUsername == null || appPassword == null) {
         System.out.println("ERROR: Not enough information to establish a database connection.");
         return;
      }
      
      DatabaseConnectionService dbcs = new DatabaseConnectionService(server, database);
      
      if(!dbcs.connect(appUsername, appPassword)) {
         System.out.println("ERROR: Could not connect to the database.");
         return;
      }
      
      if(useGUI) {
         new LogInFrame(dbcs);
         return;
      }
      
      Scanner scanner = new Scanner(System.in);
      Connection connection = dbcs.getConnection();
      
      boolean loop = true;
      String username = null;
      while(permissions == null && loop) {
         System.out.print("What action would you like to perform? (type h for help)\n> ");
         String modeStr = nextLine(scanner);
         char mode = (modeStr.length() == 1) ? modeStr.charAt(0) : '\n';
         
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
               username = nextLine(scanner);
               
               String password = new String(System.console().readPassword("Please provide your password:\n> "));
               
               CallableStatement statement = connection.prepareCall("{? = call get_Login(?)}");
               statement.registerOutParameter(1, Types.INTEGER);
               statement.setString(2, username);
               ResultSet resultSet = statement.executeQuery();
               
               if(resultSet.isClosed() || !resultSet.next()) {
                  System.out.printf("ERROR: Failed to login %s\n", username);
               } else {
                  permissions = resultSet.getInt("Type");
                  byte[] storedHash = resultSet.getBytes("Hash");
                  byte[] storedSalt = resultSet.getBytes("Salt");
                  byte[] givenHash = hashPassword(password, storedSalt);
                  
                  int i = 0;
                  for(; i < 16; i++) {
                     if(storedHash[i] != givenHash[i]) break;
                  }
                  
                  if(i == 16) {
                     System.out.printf("Successfully logged in %s\n", username);
                     loop = false;
                  } else {
                     System.out.printf("ERROR: Failed to login %s\n", username);
                     permissions = null;
                  }
               }
            } break;
            
            // Register
            case 'r': {
               System.out.println("Registration selected.");
               
               System.out.print("Please provide a username:\n> ");
               username = nextLine(scanner);
               
               String password = new String(System.console().readPassword("Please provide a password:\n> "));
               
               CallableStatement statement = connection.prepareCall("{? = call get_Login(?, ?)}");
               statement.registerOutParameter(1, Types.INTEGER);
               statement.setNull(2, Types.VARCHAR);
               statement.setInt(3, 7);
               ResultSet resultSet = statement.executeQuery();
               boolean managerExists = !resultSet.isClosed() && resultSet.next();
               
               Boolean isManager = false;
               Integer type = null;
               Integer farmId = null;
               while(type == null) {
                  if(!managerExists) {
                     isManager = null;
                     while(isManager == null) {
                        System.out.print("Is this a managerial account? (Y/n)\n> ");
                        String response = nextLine(scanner);
                        if(response.length() == 0 || response.equalsIgnoreCase("y")) {
                           isManager = true;
                        } else if(response.equalsIgnoreCase("n"))
                           isManager = false;
                        else
                           System.out.println("Invalid response.");
                     }
                  }
                  
                  System.out.print("Are you a villager (v), a shopkeeper (s), or a farmer (f)?\n> ");
                  char typeChar = nextLine(scanner).charAt(0);
                  switch(typeChar) {
                     case 'v': type = 0; permissions = 0; break;
                     case 's': type = 1; permissions = 1; break;
                     case 'f': {
                        type = 2;
                        permissions = 2;
                        
                        while(farmId == null) {
                           System.out.print("Which farm do you work on?:\n> ");
                           String farmName = nextLine(scanner);
                           
                           statement = connection.prepareCall("? = call get_Farm(?, ?)");
                           statement.registerOutParameter(1, Types.INTEGER);
                           statement.setNull(2, Types.INTEGER);
                           statement.setString(3, farmName);
                           resultSet = statement.executeQuery();
                           if(resultSet.isClosed() || !resultSet.next()) {
                              System.out.println("That farm doesn't exist.");
                           } else {
                              farmId = resultSet.getInt("ID");
                           }
                        }
                     } break;
                     default: {
                        System.out.println("Unrecognized account type.");
                     }
                  }
               }
               
               byte[] salt = new byte[16];
               random.nextBytes(salt);
               byte[] hash = hashPassword(password, salt);
               
               if(isManager) permissions = 7;
               
               statement = connection.prepareCall("{? = call insert_Login(?, ?, ?, ?)}");
               statement.registerOutParameter(1, Types.INTEGER);
               statement.setString(2, username);
               statement.setBytes(3, hash);
               statement.setBytes(4, salt);
               statement.setInt(5, permissions);
               statement.execute();
               int result = statement.getInt(1);
               
               if(result != 0) {
                  System.out.printf("ERROR: Failed to register %s\n", username);
                  permissions = null;
               } else {
                  System.out.printf("Successfully registered %s\n", username);
                  
                  if(type == 0) insertVillager(connection, username);
                  if(type == 1) insertShopkeeper(connection, username);
                  if(type == 2) insertFarmer(connection, username, farmId);
                  
                  System.out.print("Would you like to login with this account? [Y/n]\n> ");
                  String response = scanner.nextLine();
                  if(!response.equals("n")) {
                     System.out.printf("Successfully logged in %s\n", username);
                     loop = false;
                  } else {
                     permissions = null;
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
      while(permissions != null && loop) {
         System.out.print("What action would you like to perform? (type h for help)\n> ");
         String modeStr = nextLine(scanner);
         char mode = (modeStr.length() == 1) ? modeStr.charAt(0) : '\n';
         
         switch(mode) {
            // Quit / Exit
            case 'q':
            case 'x': {
               System.out.println("Exiting");
               loop = false;
            } break;
            
            // Populate
            case 'p': {
               if((permissions & 4) == 0)
                  printMainHelp(true);
               else {
                  System.out.println("Populate selected");
                  populateDatabase(connection);
               }
            } break;
            
            // Get
            case 'g': {
               System.out.print("Get selected\nWhat would you like to retrieve? (type h for help)\n> ");
               String item = nextLine(scanner);
               
               switch(item) {
                  case "q":
                  case "x": {
                     System.out.println("Exiting");
                     loop = false;
                  } break;
                  case "animal": {
                     System.out.println("Get method does not exist, please use item");
                  } break;
                  case "animalProduct": {
                     System.out.println("Get method does not exist, please use item");
                  } break;
                  case "artisanGood": {
                     System.out.println("Get method does not exist, please use item");
                  } break;
                  case "fish": {
                     System.out.println("Get method does not exist, please use item");
                  } break;
                  case "food": {
                     System.out.println("Get method does not exist, please use item");
                  } break;
                  case "item": {
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
                  case "plantProduct": {
                     System.out.print("Please provide the plant product's ID (leave empty for null):\n> ");
                     String idStr = nextLine(scanner);
                     
                     System.out.print("Please provide the plant product's name (leave empty for null):\n> ");
                     String nameStr = nextLine(scanner);
                     
                     System.out.print("Please provide the plant product's quality (leave empty for null):\n> ");
                     String qualityStr = nextLine(scanner);
                     
                     System.out.print("Please provide the plant product's price (leave empty for null):\n> ");
                     String priceStr = nextLine(scanner);
                     
                     String query = "{? = call get_PlantProduct(?, ?, ?, ?)}";
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
                        System.out.printf("Successfully retrieved plantProduct with ID %s\n", idStr);
                     } else {
                        System.out.printf("ERROR in getPlantProduct: Failed with error code %d\n", result);
                     }
                  } break;
                  case "produce": {
                     System.out.print("Please provide the produce's ID (leave empty for null):\n> ");
                     String idStr = nextLine(scanner);
                     
                     System.out.print("Please provide the produce's name (leave empty for null):\n> ");
                     String nameStr = nextLine(scanner);
                     
                     System.out.print("Please provide the produce's quality (leave empty for null):\n> ");
                     String qualityStr = nextLine(scanner);
                     
                     System.out.print("Please provide the produce's price (leave empty for null):\n> ");
                     String priceStr = nextLine(scanner);
                     
                     String query = "{? = call get_Produce(?, ?, ?, ?)}";
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
                        System.out.printf("Successfully retrieved Produce with ID %s\n", idStr);
                     } else {
                        System.out.printf("ERROR in getProduce: Failed with error code %d\n", result);
                     }
                  } break;
                  
                  case "seed": {
                     System.out.println("Get method does not exist, please use item");
                  }
                  
                  default:
                     System.out.println("Unknown option. Here are the recognized options:");
                  case "h":
                  case "?": {
                     System.out.println("q or x: Exit");
                     System.out.println("animal: get animal");
                     System.out.println("animalProdouct: get animal product");
                     System.out.println("artisanGood: get artisan good");
                     System.out.println("fish: get fish");
                     System.out.println("food: get food");
                     System.out.println("item: get item");
                     System.out.println("plantProduct: get plantProduct");
                     System.out.println("produce: get produce");
                     System.out.println("seed: get seed");
                     System.out.println("h or ?: Show this help menu");
                  } break;
               }
            } break;
            
            // Insert
            case 'i': {
               System.out.print("Insert selected\nWhat would you like to insert? (type h for help)\n> ");
               String item = nextLine(scanner);
               char insertMode = (item.length() > 0) ? item.charAt(0) : '\n';
               
               switch(insertMode) {
                  case 'q':
                  case 'x': {
                     System.out.println("Exiting");
                     loop = false;
                  } break;
                  
                  case 'h':
                  case '?': {
                     printInsertHelp(false);
                  } break;
                  
                  default: {
                     boolean foundCase = false;
                     
                     // Managerial
                     if((permissions & 4) != 0) {
                        foundCase = true;
                        
                        if(item.equalsIgnoreCase("animal")) {
                           System.out.print("Please provide the animal's name:\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the animal's base price:\n> ");
                           String baseprice = nextLine(scanner);
                           
                           int basePrice = Integer.parseInt(baseprice);
                           
                           insertAnimal(connection, name, basePrice);
                        } else if(item.equalsIgnoreCase("animalProduct")) {
                           System.out.print("Please provide the animal product's name:\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the animal product's quality (0 for normal, 3 for iridium):\n> ");
                           String quality = nextLine(scanner);
                           
                           System.out.print("Please provide the animal product's base price:\n> ");
                           String baseprice = nextLine(scanner);
                           
                           int basePrice = Integer.parseInt(baseprice);
                           int qual = Integer.parseInt(quality);
                           
                           insertAnimalProduct(connection, name, qual, basePrice);
                        } else if(item.equalsIgnoreCase("artisanGood")) {
                           System.out.print("Please provide the artisan good's name:\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the artisan good's quality (0 for normal, 3 for iridium):\n> ");
                           String quality = nextLine(scanner);
                           
                           System.out.print("Please provide the artisan good's base price:\n> ");
                           String baseprice = nextLine(scanner);
                           
                           System.out.print("Please provide the artisan good's multiplier:\n> ");
                           String multiplier = nextLine(scanner);
                           
                           int basePrice = Integer.parseInt(baseprice);
                           int qual = Integer.parseInt(quality);
                           double multi = Double.parseDouble(multiplier);
                           
                           insertArtisanGood(connection, name, qual, basePrice, multi);
                        } else if(item.equalsIgnoreCase("farm")) {
                           System.out.print("Please provide the farm's name:\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the farm's season\n> ");
                           String season = nextLine(scanner);
                           
                           insertFarm(connection, name, season);
                        } else if(item.equalsIgnoreCase("farmer")) {
                           System.out.print("Please provide the farmer's name:\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the farmer's farmID\n> ");
                           String id = nextLine(scanner);
                           
                           int farmid = Integer.parseInt(id);
                           
                           insertFarmer(connection, name, farmid);
                        } else if(item.equalsIgnoreCase("fish")) {
                           System.out.print("Please provide the fish's name:\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the fish's quality (0 for normal, 3 for iridium):\\n> ");
                           String quality = nextLine(scanner);
                           
                           System.out.print("Please provide the fish's base price:\n> ");
                           String baseprice = nextLine(scanner);
                           
                           int basePrice = Integer.parseInt(baseprice);
                           int qual = Integer.parseInt(quality);
                           
                           insertFish(connection, name, qual, basePrice);
                        } else if(item.equalsIgnoreCase("food")) {
                           System.out.print("Please provide the food's name:\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the food's base price:\n> ");
                           String baseprice = nextLine(scanner);
                           
                           int basePrice = Integer.parseInt(baseprice);
                           
                           insertFood(connection, name, basePrice);
                        } else if(item.equalsIgnoreCase("generates")) {
                           System.out.print("Please provide the produceId:\n> ");
                           String produceId = nextLine(scanner);
                           
                           System.out.print("Please provide the productId:\n> ");
                           String productId = nextLine(scanner);
                           
                           int pceId = Integer.parseInt(produceId);
                           int pctId = Integer.parseInt(productId);
                           
                           insertGenerates(connection, pceId, pctId);
                        } else if(item.equalsIgnoreCase("hasIngredient")) {
                           System.out.print("Please provide the ingredientId:\n> ");
                           String ingredientId = nextLine(scanner);
                           
                           System.out.print("Please provide the foodId:\n> ");
                           String foodId = nextLine(scanner);
                           
                           int iId = Integer.parseInt(ingredientId);
                           int fId = Integer.parseInt(foodId);
                           
                           insertHasIngredient(connection, iId, fId);
                        } else if(item.equalsIgnoreCase("item")) {
                           System.out.print("Please provide the item's name:\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the item's quality (0 for normal, 3 for iridium):\n> ");
                           String quality = nextLine(scanner);
                           
                           System.out.print("Please provide the item's base price:\n> ");
                           String baseprice = nextLine(scanner);
                           
                           int basePrice = Integer.parseInt(baseprice);
                           Integer qual = (quality.length() == 0) ? null : Integer.parseInt(quality);
                           
                           insertItem(connection, name, qual, basePrice);
                        } else if(item.equalsIgnoreCase("plantProduct")) {
                           System.out.print("Please provide the plantProduct's name:\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the plantProduct's quality (0 for normal, 3 for iridium):\n> ");
                           String quality = nextLine(scanner);
                           
                           System.out.print("Please provide the plantProduct's base price:\n> ");
                           String basePrice = nextLine(scanner);
                           
                           System.out.print("Please provide the plantProduct's type (Fruit, Vegetable, Forage, or Flower):\n> ");
                           String type = nextLine(scanner);
                           
                           int qual = Integer.parseInt(quality);
                           int price = Integer.parseInt(basePrice);
                           
                           insertPlantProduct(connection, name, qual, price, type);
                        } else if(item.equalsIgnoreCase("produce")) {
                           System.out.print("Please provide the Produce's name:\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the Produce's quality (0 for normal, 3 for iridium):\n> ");
                           String quality = nextLine(scanner);
                           
                           System.out.print("Please provide the Produce's base price:\n> ");
                           String basePrice = nextLine(scanner);
                           
                           int qual = Integer.parseInt(quality);
                           int price = Integer.parseInt(basePrice);
                           
                           insertProduce(connection, name, qual, price);
                        } else if(item.equalsIgnoreCase("produces")) {
                           System.out.print("Please provide the animalId:\n> ");
                           String animalId = nextLine(scanner);
                           
                           System.out.print("Please provide the productId:\n> ");
                           String productId = nextLine(scanner);
                           
                           int aId = Integer.parseInt(animalId);
                           int pId = Integer.parseInt(productId);
                           
                           insertProduces(connection, aId, pId);
                        } else if(item.equalsIgnoreCase("profession")) {
                           System.out.print("Please provide the profession's boost category:\n> ");
                           String bCat = nextLine(scanner);
                           
                           System.out.print("Please provide the profession's boost multiplier:\n> ");
                           String bMult = nextLine(scanner);
                           
                           double mult = Double.parseDouble(bMult);
                           
                           insertProfession(connection, bCat, mult);
                        } else if(item.equalsIgnoreCase("seed")) {
                           System.out.print("Please provide the Seed's name:\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the Seed's base price:\n> ");
                           String basePrice = nextLine(scanner);
                           
                           System.out.print("Please provide the Seed's season (Spring, Summer, Fall, Spring/Summer, Spring/Fall, Summer/Fall, All, or None):\n> ");
                           String season = nextLine(scanner);
                           
                           int price = Integer.parseInt(basePrice);
                           
                           insertSeed(connection, name, price, season);
                        } else if(item.equalsIgnoreCase("shop")) {
                           System.out.print("Please provide the Shop's ownerId:\n> ");
                           String ownerId = nextLine(scanner);
                           
                           System.out.print("Please provide the Shop's name:\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the Shop's address:\n> ");
                           String address = nextLine(scanner);
                           
                           System.out.print("Please provide the Shop's schedule:\n> ");
                           String schedule = nextLine(scanner);
                           
                           int oId = Integer.parseInt(ownerId);
                           
                           insertShop(connection, name, address, schedule, oId);
                        } else if(item.equalsIgnoreCase("shopkeeper")) {
                           System.out.print("Please provide the Shopkeeper's name:\n> ");
                           String name = nextLine(scanner);
                           
                           insertShopkeeper(connection, name);
                        } else if(item.equalsIgnoreCase("villager")) {
                           System.out.print("Please provide the Villager's name:\n> ");
                           String name = nextLine(scanner);
                           
                           insertVillager(connection, name);
                        } else {
                           foundCase = false;
                        }
                        
                        if(foundCase) break;
                     }
                     
                     // Farmer
                     if((permissions & 2) != 0) {
                        foundCase = true;
                        
                        if(item.equalsIgnoreCase("hasProfession")) {
                           System.out.print("Please provide the professionId:\n> ");
                           String professionId = nextLine(scanner);
                           
                           System.out.print("Please provide the farmerId:\n> ");
                           String farmerId = nextLine(scanner);
                           
                           int pId = Integer.parseInt(professionId);
                           int fId = Integer.parseInt(farmerId);
                           
                           insertHasProfession(connection, pId, fId);
                        } else if(item.equalsIgnoreCase("farmSells")) {
                           //TODO: This needs to be implemented
                           foundCase = false;
                        } else {
                           foundCase = false;
                        }
                        
                        if(foundCase) break;
                     }
                     
                     // Shopkeeper
                     if((permissions & 1) != 0) {
                        foundCase = true;
                        
                        if(item.equalsIgnoreCase("shopSells")) {
                           System.out.print("Please provide the shopId:\n> ");
                           String shopId = nextLine(scanner);
                           
                           System.out.print("Please provide the itemId:\n> ");
                           String itemId = nextLine(scanner);
                           
                           int sId = Integer.parseInt(shopId);
                           int iId = Integer.parseInt(itemId);
                           
                           insertShopSells(connection, sId, iId);
                        } else if(item.equalsIgnoreCase("shopBuys")) {
                           System.out.print("Please provide the shopId:\n> ");
                           String shopId = nextLine(scanner);
                           
                           System.out.print("Please provide the itemId:\n> ");
                           String itemId = nextLine(scanner);
                           
                           int sId = Integer.parseInt(shopId);
                           int iId = Integer.parseInt(itemId);
                           
                           insertShopBuys(connection, sId, iId);
                        } else {
                           foundCase = false;
                        }
                        
                        if(foundCase) break;
                     }
                     
                     // Villager
                     if(item.equalsIgnoreCase("needs")) {
                        System.out.print("Please provide the villagerId:\n> ");
                        String villagerId = nextLine(scanner);
                        
                        System.out.print("Please provide the itemId:\n> ");
                        String itemId = nextLine(scanner);
                        
                        System.out.print("Please provide the reward:\n> ");
                        String reward = nextLine(scanner);
                        
                        System.out.print("Please provide the quantity:\n> ");
                        String quantity = nextLine(scanner);
                        
                        int vId = Integer.parseInt(villagerId);
                        int iId = Integer.parseInt(itemId);
                        int r = Integer.parseInt(reward);
                        int quant = Integer.parseInt(quantity);
                        
                        insertNeeds(connection, vId, iId, r, quant);
                     } else {
                        printInsertHelp(true);
                     }
                  }
               }
            } break;
            
            // Update
            case 'u': {
               System.out.print("Update selected\nWhat would you like to update? (type h for help)\n> ");
               String item = nextLine(scanner);
               char updateMode = (item.length() == 1) ? item.charAt(0) : ' ';
               
               switch(updateMode) {
                  case 'q':
                  case 'x': {
                     System.out.println("Exiting");
                     loop = false;
                  } break;
                  
                  case 'h':
                  case '?': {
                     printUpdateHelp(false);
                  } break;
                  
                  default: {
                     boolean foundCase = false;
                     
                     if((permissions & 4) != 0) {
                        foundCase = true;
                        
                        if(item.equalsIgnoreCase("animal")) {
                           System.out.print("Please provide the animal's id:\n> ");
                           String animalId = nextLine(scanner);
                           
                           System.out.print("Please provide the animal's name (leave empty for null):\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the animal's quality (0 for normal, 3 for iridium):\n> ");
                           String quality = nextLine(scanner);
                         
                           System.out.print("Please provide the animal's base price (leave empty for null):\n> ");
                           String baseprice = nextLine(scanner);
                         
                           Integer qual;
                           Integer basePrice;
                           int aId = Integer.parseInt(animalId);
                           if(name.length() == 0) {
                              name = null;
                           }
                           if(quality.length() == 0) {
                              qual = null;
                           }
                           else {
                              qual = Integer.valueOf(quality);
                           }
                           if(baseprice.length() == 0) {
                              basePrice = null;
                           } else {
                              basePrice = Integer.valueOf(baseprice);
                           }
                         
                           updateAnimal(connection, aId, name, qual, basePrice);
                        } else if(item.equalsIgnoreCase("animalProduct")) {
                           System.out.print("Please provide the animal product's id:\n> ");
                           String animalId = nextLine(scanner);
                           
                           System.out.print("Please provide the animal product's name (leave empty for null):\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the animal product's quality (0 for normal, 3 for iridium):\n> ");
                           String quality = nextLine(scanner);
                         
                           System.out.print("Please provide the animal produt's base price (leave empty for null):\n> ");
                           String baseprice = nextLine(scanner);
                         
                           Integer qual;
                           Integer basePrice;
                           int aId = Integer.parseInt(animalId);
                           if(name.length() == 0) {
                              name = null;
                           }
                           if(quality.length() == 0) {
                              qual = null;
                           }
                           else {
                              qual = Integer.valueOf(quality);
                           }
                           if(baseprice.length() == 0) {
                              basePrice = null;
                           } else {
                              basePrice = Integer.valueOf(baseprice);
                           }
                           
                           updateAnimalProduct(connection, aId, name, qual, basePrice);
                        } else if(item.equalsIgnoreCase("artisanGood")) {
                           System.out.print("Please provide the artisan good's id:\n> ");
                           String artisanId = nextLine(scanner);
                           
                           System.out.print("Please provide the artisan good's name (leave empty for null):\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the artisan good's quality (0 for normal, 3 for iridium):\n> ");
                           String quality = nextLine(scanner);
                           
                           System.out.print("Please provide the artisan good's base price (leave empty for null):\n> ");
                           String baseprice = nextLine(scanner);
                           
                           System.out.print("Please provide the artisan good's multiplier (leave empty for null):\n> ");
                           String multiplier = nextLine(scanner);
                           
                           Integer qual;
                           Integer basePrice;
                           Double multi;
                           int aId = Integer.parseInt(artisanId);
                           if(name.length() == 0) {
                              name = null;
                           }
                           if(quality.length() == 0) {
                              qual = null;
                           }
                           else {
                              qual = Integer.valueOf(quality);
                           }
                           if(baseprice.length() == 0) {
                              basePrice = null;
                           } else {
                              basePrice = Integer.valueOf(baseprice);
                           }
                           if(multiplier.length() == 0) {
                              multi = null;
                           } else {
                              multi = Double.valueOf(multiplier);
                           }
                           
                           updateArtisanGood(connection, aId, name, qual, basePrice, multi);
                        } else if(item.equalsIgnoreCase("fish")) {
                           System.out.print("Please provide the fish's id:\n> ");
                           String fishId = nextLine(scanner);
                           
                           System.out.print("Please provide the fish's name (leave empty for null):\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the fish's quality (0 for normal, 3 for iridium):\n> ");
                           String quality = nextLine(scanner);
                           
                           System.out.print("Please provide the fish's base price (leave empty for null):\n> ");
                           String baseprice = nextLine(scanner);
                           
                           Integer qual;
                           Integer basePrice;
                           int fId = Integer.parseInt(fishId);
                           if(name.length() == 0) {
                              name = null;
                           }
                           if(quality.length() == 0) {
                              qual = null;
                           }
                           else {
                              qual = Integer.valueOf(quality);
                           }
                           if(baseprice.length() == 0) {
                              basePrice = null;
                           } else {
                              basePrice = Integer.valueOf(baseprice);
                           }
                           
                           updateFish(connection, fId, name, qual, basePrice);
                        } else if(item.equalsIgnoreCase("food")) {
                           System.out.print("Please provide the food's id:\n> ");
                           String foodId = nextLine(scanner);
                           
                           System.out.print("Please provide the food's name (leave empty for null):\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the food's quality (0 for normal, 3 for iridium):\n> ");
                           String quality = nextLine(scanner);
                           
                           System.out.print("Please provide the food's base price (leave empty for null):\n> ");
                           String baseprice = nextLine(scanner);
                           
                           Integer qual;
                           Integer basePrice;
                           int fId = Integer.parseInt(foodId);
                           if(name.length() == 0) {
                              name = null;
                           }
                           if(quality.length() == 0) {
                              qual = null;
                           }
                           else {
                              qual = Integer.valueOf(quality);
                           }
                           if(baseprice.length() == 0) {
                              basePrice = null;
                           } else {
                              basePrice = Integer.valueOf(baseprice);
                           }
                           
                           updateFood(connection, fId, name, qual, basePrice);
                        } else if(item.equalsIgnoreCase("item")) {
                           System.out.print("Please provide the item's id:\n> ");
                           String itemId = nextLine(scanner);
                           
                           System.out.print("Please provide the item's name (leave empty for null):\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the item's quality (0 for normal, 3 for iridium):\n> ");
                           String quality = nextLine(scanner);
                           
                           System.out.print("Please provide the item's base price (leave empty for null):\n> ");
                           String baseprice = nextLine(scanner);
                           
                           Integer qual;
                           Integer basePrice;
                           int iId = Integer.parseInt(itemId);
                           if(name.length() == 0) {
                              name = null;
                           }
                           if(quality.length() == 0) {
                              qual = null;
                           }
                           else {
                              qual = Integer.valueOf(quality);
                           }
                           if(baseprice.length() == 0) {
                              basePrice = null;
                           } else {
                              basePrice = Integer.valueOf(baseprice);
                           }
                           
                           updateItem(connection, iId, name, qual, basePrice);
                        } else if(item.equalsIgnoreCase("plantProduct")) {
                           System.out.print("Please provide the plant product's id:\n> ");
                           String plantprodId = nextLine(scanner);
                           
                           System.out.print("Please provide the plant product's name (leave empty for null):\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the plant product's quality (0 for normal, 3 for iridium) (leave empty for null):\n> ");
                           String quality = nextLine(scanner);
                           
                           System.out.print("Please provide the plant product's base price (leave empty for null):\n> ");
                           String baseprice = nextLine(scanner);
                           
                           System.out.print("Please provide the plant product's type (Fruit, Vegetable, Forage, or Flower) (leave empty for null):\n> ");
                           String type = nextLine(scanner);
                           
                           Integer qual;
                           Integer basePrice;
                           int ppId = Integer.parseInt(plantprodId);
                           if(name.length() == 0) {
                              name = null;
                           }
                           if(quality.length() == 0) {
                              qual = null;
                           }
                           else {
                              qual = Integer.valueOf(quality);
                           }
                           if(baseprice.length() == 0) {
                              basePrice = null;
                           } else {
                              basePrice = Integer.valueOf(baseprice);
                           }
                           
                           updatePlantProduct(connection, ppId, name, qual, basePrice, type);
                        } else if(item.equalsIgnoreCase("produce")) {
                           System.out.print("Please provide the produce's id:\n> ");
                           String produceId = nextLine(scanner);
                           
                           System.out.print("Please provide the produce's name (leave empty for null):\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the produce's quality (0 for normal, 3 for iridium):\n> ");
                           String quality = nextLine(scanner);
                           
                           System.out.print("Please provide the produce's base price (leave empty for null):\n> ");
                           String baseprice = nextLine(scanner);
                           
                           Integer qual;
                           Integer basePrice;
                           int pId = Integer.parseInt(produceId);
                           if(name.length() == 0) {
                              name = null;
                           }
                           if(quality.length() == 0) {
                              qual = null;
                           }
                           else {
                              qual = Integer.valueOf(quality);
                           }
                           if(baseprice.length() == 0) {
                              basePrice = null;
                           } else {
                              basePrice = Integer.valueOf(baseprice);
                           }
                           
                           updateProduce(connection, pId, name, qual, basePrice);
                        } else if(item.equalsIgnoreCase("seed")) {
                           System.out.print("Please provide the seed's id:\n> ");
                           String seedId = nextLine(scanner);
                           
                           System.out.print("Please provide the seed's name (leave empty for null):\n> ");
                           String name = nextLine(scanner);
                           
                           System.out.print("Please provide the seed's quality (0 for normal, 3 for iridium) (leave empty for null):\n> ");
                           String quality = nextLine(scanner);
                           
                           System.out.print("Please provide the seed's base price (leave empty for null):\n> ");
                           String baseprice = nextLine(scanner);
                           
                           System.out.print("Please provide the seed's season (Spring, Summer, Fall, Spring/Summer, Spring/Fall, Summer/Fall, All, or None) (leave empty for null):\n> ");
                           String type = nextLine(scanner);
                           
                           Integer qual;
                           Integer basePrice;
                           int sId = Integer.parseInt(seedId);
                           if(name.length() == 0) {
                              name = null;
                           }
                           if(quality.length() == 0) {
                              qual = null;
                           }
                           else {
                              qual = Integer.valueOf(quality);
                           }
                           if(baseprice.length() == 0) {
                              basePrice = null;
                           } else {
                              basePrice = Integer.valueOf(baseprice);
                           }
                           
                           updateSeed(connection, sId, name, qual, basePrice, type);
                        } else {
                           foundCase = false;
                        }
                        
                        if(foundCase) break;
                     }
                     
                     if((permissions & 2) != 0) {
                        foundCase = true;
                        
                        //TODO: FarmSells, Farmer
                        {
                           foundCase = false;
                        }
                        
                        if(foundCase) break;
                     }
                     
                     if((permissions & 1) != 0) {
                        foundCase = true;
                        
                        //TODO: ShopSells, ShopBuys, Shop
                        {
                           foundCase = false;
                        }
                        
                        if(foundCase) break;
                     }
                     
                     //TODO: Needs
                     if(item.equalsIgnoreCase("login")) {
                        String password = new String(System.console().readPassword("Please provide a new password:\n> "));
                        byte[] salt = new byte[16];
                        random.nextBytes(salt);
                        byte[] hash = hashPassword(password, salt);
                        CallableStatement statement = connection.prepareCall("{? = call update_Login(?, ?, ?)}");
                        statement.registerOutParameter(1, Types.INTEGER);
                        statement.setString(2, username);
                        statement.setBytes(3, hash);
                        statement.setBytes(4, salt);
                        statement.execute();
                        int result = statement.getInt(1);
                        if(result == 0) {
                           System.out.println("Successfully updated your password.");
                        } else {
                           System.out.println("ERROR in updateLogin: Failed to update your password.");
                        }
                     } else {
                        printUpdateHelp(true);
                     }
                  }
               }
            } break;
            
            // Delete
            case 'd': {
               System.out.print("Delete selected\nWhat would you like to delete? (type h for help)\n> ");
               String item = nextLine(scanner);
               char deleteMode = (item.length() == 1) ? item.charAt(0) : ' ';
               
               switch(deleteMode) {
                  case 'q':
                  case 'x': {
                     System.out.println("Exiting");
                     loop = false;
                  } break;
                  
                  case 'h':
                  case '?': {
                     printDeleteHelp(false);
                  } break;
                  
                  default: {
                     boolean foundCase = false;
                     
                     if((permissions & 4) != 0) {
                        foundCase = true;
                        
                        if(item.equalsIgnoreCase("animal")) {
                           System.out.print("Please provide the animal's id:\n> ");
                           String animalId = nextLine(scanner);
                           
                           int aId = Integer.parseInt(animalId);
                           
                           String query = "{? = call delete_animal(?)}";
                           CallableStatement statement = connection.prepareCall(query);
                           statement.registerOutParameter(1, Types.INTEGER);
                           statement.setInt(2, aId);
                           
                           statement.execute();
                           
                           int result = statement.getInt(1);
                           if(result == 0)
                              System.out.printf("Successfully deleted animal\n");
                           else
                              System.out.printf("ERROR in deleteAnimal: Failed with error code %d\n", result);
                        } else if(item.equalsIgnoreCase("animalProduct")) {
                           System.out.print("Please provide the animal product's id:\n> ");
                           String animalId = nextLine(scanner);
                           
                           int aId = Integer.parseInt(animalId);
                           
                           String query = "{? = call delete_animalproduct(?)}";
                           CallableStatement statement = connection.prepareCall(query);
                           statement.registerOutParameter(1, Types.INTEGER);
                           statement.setInt(2, aId);
                           
                           statement.execute();
                           
                           int result = statement.getInt(1);
                           if(result == 0)
                              System.out.printf("Successfully deleted animal product\n");
                           else
                              System.out.printf("ERROR in deleteAnimalProduct: Failed with error code %d\n", result);
                        } else if(item.equalsIgnoreCase("artisanGood")) {
                           System.out.print("Please provide the artisan good's id:\n> ");
                           String artisanId = nextLine(scanner);
                           
                           int aId = Integer.parseInt(artisanId);
                           
                           String query = "{? = call delete_artisangood(?)}";
                           CallableStatement statement = connection.prepareCall(query);
                           statement.registerOutParameter(1, Types.INTEGER);
                           statement.setInt(2, aId);
                           
                           statement.execute();
                           
                           int result = statement.getInt(1);
                           if(result == 0)
                              System.out.printf("Successfully deleted Artisan Good\n");
                           else
                              System.out.printf("ERROR in deleteArtisanGood: Failed with error code %d\n", result);
                        } else if(item.equalsIgnoreCase("fish")) {
                           System.out.print("Please provide the fish's id:\n> ");
                           String fishId = nextLine(scanner);
                           
                           int fId = Integer.parseInt(fishId);
                           
                           String query = "{? = call delete_fish(?)}";
                           CallableStatement statement = connection.prepareCall(query);
                           statement.registerOutParameter(1, Types.INTEGER);
                           statement.setInt(2, fId);
                           
                           statement.execute();
                           
                           int result = statement.getInt(1);
                           if(result == 0)
                              System.out.printf("Successfully deleted fish\n");
                           else
                              System.out.printf("ERROR in deleteFish: Failed with error code %d\n", result);
                        } else if(item.equalsIgnoreCase("food")) {
                           System.out.print("Please provide the food's id:\n> ");
                           String foodId = nextLine(scanner);
                           
                           int fId = Integer.parseInt(foodId);
                           
                           String query = "{? = call delete_food(?)}";
                           CallableStatement statement = connection.prepareCall(query);
                           statement.registerOutParameter(1, Types.INTEGER);
                           statement.setInt(2, fId);
                           
                           statement.execute();
                           
                           int result = statement.getInt(1);
                           if(result == 0)
                              System.out.printf("Successfully deleted food\n");
                           else
                              System.out.printf("ERROR in deleteFood: Failed with error code %d\n", result);
                        } else if(item.equalsIgnoreCase("item")) {
                           System.out.print("Please provide the item's ID (leave empty for null):\n> ");
                           String idStr = nextLine(scanner);
                           
                           System.out.print("Please provide the item's name (leave empty for null):\n> ");
                           String nameStr = nextLine(scanner);
                           
                           System.out.print("Please provide the item's quality (leave empty for null):\n> ");
                           String qualityStr = nextLine(scanner);
                           
                           System.out.print("Please provide the item's price (leave empty for null):\n> ");
                           String priceStr = nextLine(scanner);
                           
                           String query = "{? = call delete_item(?, ?, ?, ?)}";
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
                        } else if(item.equalsIgnoreCase("plantProduct")) {
                           System.out.print("Please provide the plant product's id:\n> ");
                           String plantprodId = nextLine(scanner);
                           
                           int ppId = Integer.parseInt(plantprodId);
                           
                           String query = "{? = call delete_plantProduct(?)}";
                           CallableStatement statement = connection.prepareCall(query);
                           statement.registerOutParameter(1, Types.INTEGER);
                           statement.setInt(2, ppId);
                           
                           statement.execute();
                           
                           int result = statement.getInt(1);
                           if(result == 0)
                              System.out.printf("Successfully deleted Plant Product\n");
                           else
                              System.out.printf("ERROR in deletePlantProduct: Failed with error code %d\n", result);
                        } else if(item.equalsIgnoreCase("produce")) {
                           System.out.print("Please provide the produce's id:\n> ");
                           String produceId = nextLine(scanner);
                           
                           int pId = Integer.parseInt(produceId);
                           
                           String query = "{? = call delete_produce(?)}";
                           CallableStatement statement = connection.prepareCall(query);
                           statement.registerOutParameter(1, Types.INTEGER);
                           statement.setInt(2, pId);
                           
                           statement.execute();
                           
                           int result = statement.getInt(1);
                           if(result == 0)
                              System.out.printf("Successfully deleted produce\n");
                           else
                              System.out.printf("ERROR in deleteProduce: Failed with error code %d\n", result);
                        } else if(item.equalsIgnoreCase("seed")) {
                           System.out.print("Please provide the seed's id:\n> ");
                           String seedId = nextLine(scanner);
                           
                           int sId = Integer.parseInt(seedId);
                           
                           String query = "{? = call delete_seed(?)}";
                           CallableStatement statement = connection.prepareCall(query);
                           statement.registerOutParameter(1, Types.INTEGER);
                           statement.setInt(2, sId);
                           
                           statement.execute();
                           
                           int result = statement.getInt(1);
                           if(result == 0)
                              System.out.printf("Successfully deleted seed\n");
                           else
                              System.out.printf("ERROR in deleteSeed: Failed with error code %d\n", result);
                        } else {
                           foundCase = false;
                        }
                        
                        if(foundCase) break;
                     }
                     
                     if((permissions & 2) != 0) {
                        foundCase = true;
                        
                        //TODO: FarmSells, HasProfession
                        {
                           foundCase = false;
                        }
                        
                        if(foundCase) break;
                     }
                     
                     if((permissions & 1) != 0) {
                        foundCase = true;
                        
                        //TODO: ShopSells, ShopBuys
                        {
                           foundCase = false;
                        }
                        
                        if(foundCase) break;
                     }
                     
                     //TODO: Needs
                     {
                        printUpdateHelp(true);
                     }
                     
                     printDeleteHelp(true);
                  }
               }
            } break;
               
            // Help
            default:
               printMainHelp(true); break;
            case 'h':
            case '?':
               printMainHelp(false); break;
         }
      }
      
      scanner.close();
      connection.close();
   }
   
   public static void printMainHelp(boolean unknownOption) {
      if(unknownOption)
         System.out.println("Unknown option. Here are the recognized options:");
      
      System.out.println("q or x: Exit");
      if((permissions & 4) != 0) System.out.println("p: Populate the database");
      System.out.println("g: Retrieve data from the database");
      System.out.println("i: Insert new data into the database");
      System.out.println("u: Update data in the database");
      System.out.println("d: Delete data from the database");
      System.out.println("h or ?: Show this help menu");
   }
   
   public static void printInsertHelp(boolean unknownOption) {
      if(unknownOption)
         System.out.println("Unknown option. Here are the recognized options:");
      
      System.out.println("q or x: Exit");
      if((permissions & 4) != 0) {
         System.out.println("animal: insert animal");
         System.out.println("animalProdouct: insert animal product");
         System.out.println("artisanGood: insert artisan good");
         System.out.println("farm: insert farm");
         System.out.println("farmer: insert farmer");
         System.out.println("farmSells: insert farm sells");
         System.out.println("fish: insert fish");
         System.out.println("food: insert food");
         System.out.println("generates: insert generates");
         System.out.println("hasIngredient: insert has ingredient");
         System.out.println("item: insert item");
         System.out.println("plantProduct: insert plantProduct");
         System.out.println("produce: insert produce");
         System.out.println("produces: insert produces");
         System.out.println("profession: insert profession");
         System.out.println("seed: insert seed");
         System.out.println("shop: insert shop");
         System.out.println("shopkeeper: insert shopkeeper");
         System.out.println("villager: insert villager");
      }
      if((permissions & 2) != 0) {
         System.out.println("hasProfession: insert has profession");
         System.out.println("farmSells: insert farmSells record");
      }
      if((permissions & 1) != 0) {
         System.out.println("shopBuys: insert shop buys");
         System.out.println("shopSells: insert shop sells");
      }
      System.out.println("needs: insert needs");
      System.out.println("h or ?: Show this help menu");
   }
   
   public static void printUpdateHelp(boolean unknownOption) {
      if(unknownOption)
         System.out.println("Unknown option. Here are the recognized options:");
      
      System.out.println("q or x: Exit");
      if((permissions & 4) != 0) {
         System.out.println("animal: update animal");
         System.out.println("animalProdouct: update animal product");
         System.out.println("artisanGood: update artisan good");
         System.out.println("fish: update fish");
         System.out.println("food: update food");
         System.out.println("item: update item");
         System.out.println("plantProduct: update plantProduct");
         System.out.println("produce: update produce");
         System.out.println("seed: update seed");
      }
      System.out.println("h or ?: Show this help menu");
   }
   
   public static void printDeleteHelp(boolean unknownOption) {
      if(unknownOption)
         System.out.println("Unknown option. Here are the recognized options:");
      
      System.out.println("q or x: Exit");
      if((permissions & 4) != 0) {
         System.out.println("animal: delete animal");
         System.out.println("animalProdouct: delete animal product");
         System.out.println("artisanGood: delete artisan good");
         System.out.println("fish: delete fish");
         System.out.println("food: delete food");
         System.out.println("item: delete item");
         System.out.println("plantProduct: delete plantProduct");
         System.out.println("produce: delete produce");
         System.out.println("seed: delete seed");
      }
      System.out.println("h or ?: Show this help menu");
   }
   
   public static byte[] hashPassword(String password, byte[] salt) throws Exception {
      KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
      SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      return keyFactory.generateSecret(spec).getEncoded();
   }
   
   @SuppressWarnings("unchecked")
   public static void populateDatabase(Connection connection) throws Exception {
      String fileData = Files.readString((new File("data/Data/Crops.json")).toPath());
      JSONObject cropsRoot = new JSONObject(fileData);
      JSONObject cropsContent = cropsRoot.getJSONObject("content");
      
      fileData = Files.readString((new File("data/Data/ObjectInformation.json")).toPath());
      JSONObject objsRoot = new JSONObject(fileData);
      JSONObject objsContent = objsRoot.getJSONObject("content");
      
      HashMap<String, Integer> idMap = new HashMap<>();
      HashMap<String, Integer> nameMap = new HashMap<>();
      HashMap<String, ArrayList<String>> categoryMap = new HashMap<>();
      
      Iterator<String> keys = objsContent.keys();
      while(keys.hasNext()) {
         String itemId = keys.next();
         String[] values = objsContent.getString(itemId).split("/");
         
         String name = values[0];
         int price = Integer.valueOf(values[1]);
         
         //TODO: Handle quality
         String[] types = values[3].split(" ");
         Integer itemDBId = null;
         
         if(types.length > 1) {
            ArrayList<String> categoryList = categoryMap.get(types[1]);
            if(categoryList == null) {
               categoryList = new ArrayList<>();
               categoryMap.put(types[1], categoryList);
            }
            categoryList.add(itemId);
         }
         
         if(types[0].equals("Seeds")) {
            String season = "All";
            if(cropsContent.has(itemId)) {
               season = cropsContent.getString(itemId).split("/")[1];
               season = season.replace("spring", "Spring").replace("summer", "Summer").replace("fall", "Fall").replace(" ", "/");
               if(season.equals("Spring/Summer/Fall")) season = "All";
               if(!season.contains("Spring") && !season.contains("Summer") && !season.contains("Fall")) season = "None";
            }
            
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
                  
                  case -17: {
                     if(itemId.equals("417")) itemDBId = insertProduce(connection, name, null, price);
                     else if(itemId.equals("430")) itemDBId = insertAnimalProduct(connection, name, null, price);
                     else itemDBId = insertItem(connection, name, null, price);
                  } break;
                  
                  case -26:
                  case -27: {
                     itemDBId = insertArtisanGood(connection, name, null, price, 0.0);
                  } break;
                  
                  case -74: {
                     if(name.equals("Banana Sapling"))           itemDBId = insertSeed(connection, name, price, "Summer");
                     else if(name.equals("Tea Sapling"))         itemDBId = insertSeed(connection, name, price, "All");
                     else if(name.equals("Cherry Sapling"))      itemDBId = insertSeed(connection, name, price, "Spring");
                     else if(name.equals("Apricot Sapling"))     itemDBId = insertSeed(connection, name, price, "Spring");
                     else if(name.equals("Orange Sapling"))      itemDBId = insertSeed(connection, name, price, "Summer");
                     else if(name.equals("Peach Sapling"))       itemDBId = insertSeed(connection, name, price, "Summer");
                     else if(name.equals("Pomegranite Sapling")) itemDBId = insertSeed(connection, name, price, "Fall");
                     else if(name.equals("Apple Sapling"))       itemDBId = insertSeed(connection, name, price, "Fall");
                     else if(name.equals("Mango Sapling"))       itemDBId = insertSeed(connection, name, price, "Summer");
                  } break;
                  
                  case -75: itemDBId = insertPlantProduct(connection, name, null, price, "Vegetable"); break;
                  case -79: itemDBId = insertPlantProduct(connection, name, null, price, "Fruit"); break;
                  case -80: itemDBId = insertPlantProduct(connection, name, null, price, "Flower"); break;
                  case -81: itemDBId = insertPlantProduct(connection, name, null, price, "Forage"); break;
                  
                  default: itemDBId = insertItem(connection, name, null, price);
               }
            } else {
               itemDBId = insertItem(connection, name, null, price);
            }
         } else if(types[0].equals("Arch")) {
            if(name.equals("Dinosaur Egg")) {
               itemDBId = insertAnimalProduct(connection, name, null, price);
            } else {
               itemDBId = insertItem(connection, name, null, price);
            }
         } else if(types[0].equals("Crafting")) {
            if(name.equals("Coffee")) {
               itemDBId = insertArtisanGood(connection, name, null, price, 0.0);
            } else {
               itemDBId = insertItem(connection, name, null, price);
            }
         } else {
            // Minerals, Quest, asdf, Ring
            itemDBId = insertItem(connection, name, null, price);
         }
         
         idMap.put(itemId, itemDBId);
         nameMap.put(name, itemDBId);
      }
      
      fileData = Files.readString((new File("data/Data/FarmAnimals.json")).toPath());
      JSONObject animalsRoot = new JSONObject(fileData);
      JSONObject animalsContent = animalsRoot.getJSONObject("content");
      keys = animalsContent.keys();
      while(keys.hasNext()) {
         String name = keys.next();
         if(name.equals("Hog")) continue;
         
         String[] values = animalsContent.getString(name).split("/");
         
         Integer produceDBId1 = idMap.get(values[2]);
         Integer produceDBId2 = idMap.get(values[3]);
         
         Integer price = 0;
         if(name.contains("Chicken")) price = 800;
         else if(name.contains("Duck")) price = 1200;
         else if(name.contains("Rabbit")) price = 8000;
         else if(name.contains("Cow")) price = 1500;
         else if(name.contains("Goat")) price = 4000;
         else if(name.contains("Sheep")) price = 8000;
         else if(name.contains("Pig")) price = 16000;
         
         int animalDBId = insertAnimal(connection, name, price);
         idMap.put(name, animalDBId);
         
         if(produceDBId1 != null)
            insertProduces(connection, animalDBId, produceDBId1);
         if(produceDBId2 != null && produceDBId1 != produceDBId2)
            insertProduces(connection, animalDBId, produceDBId2);
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
         
         Integer resultId = idMap.get(outputs[0]);
         
         for(int i = 0; i < inputs.length; i += 2) {
            ArrayList<String> categoryList = categoryMap.get(inputs[i]);
            
            if(categoryList != null) {
               for(String item : categoryList) {
                  insertHasIngredient(connection, idMap.get(item), resultId);
               }
            } else {
               insertHasIngredient(connection, idMap.get(inputs[i]), resultId);
            }
         }
      }
      
      int mrQiId1 = insertShopkeeper(connection, "MisterQi1");
      int mrQiId2 = insertShopkeeper(connection, "MisterQi2");
      idMap.put("MisterQi1", mrQiId1);
      idMap.put("MisterQi2", mrQiId2);
      insertShop(connection, "WalnutRoom", "Ginger Island 1", "Always", mrQiId1);
      insertShop(connection, "Casino", "Calico Desert 2, in the back of the Oasis", "9a-11:50p", mrQiId2);
      
      String name = "TravelingMerchant";
      int id = insertShopkeeper(connection, name);
      idMap.put(name, id);
      insertShop(connection, "TravelingCart", "Cindersap Forest, north of the upper pond", "6am to 8pm on Fridays and Saturdays, and 5pm to 2am during the Night Market.", id);
      
      name = "DesertTrader";
      id = insertShopkeeper(connection, name);
      idMap.put(name, id);
      insertShop(connection, "TradingHut", "Next to the road through the Calico Desert", "6am to 2am every day, except during the Night Market.", id);
      
      name = "Morris";
      id = insertShopkeeper(connection, name);
      idMap.put(name, id);
      insertShop(connection, "JojaMart", "Pelican Town 3", "Permanently closed.", id);
      
      name = "VolcanicDwarf";
      id = insertShopkeeper(connection, name);
      idMap.put(name, id);
      insertShop(connection, "VolcanoShop", "In level 5 of the Ginger Island Volcano", "Always open.", id);
      
      name = "IslandTrader";
      id = insertShopkeeper(connection, name);
      idMap.put(name, id);
      insertShop(connection, "IslandTradingStand", "On the way to the Ginger Island Volcano", "Always open.", id);
      
      name = "HatMouse";
      id = insertShopkeeper(connection, name);
      idMap.put(name, id);
      insertShop(connection, "HatShop", "Abandoned house in the Cindersap Forest", "Always open.", id);
      
      name = "Bear";
      idMap.put(name, insertVillager(connection, name));
      
      name = "Birdie";
      idMap.put(name, insertVillager(connection, name));
      
      name = "Gil";
      idMap.put(name, insertVillager(connection, name));
      
      name = "Gunther";
      idMap.put(name, insertVillager(connection, name));
      
      // fileData = Files.readString((new File("data/Data/NPCDispositions.json")).toPath());
      // JSONObject npcRoot = new JSONObject(fileData);
      // JSONObject npcContent = npcRoot.getJSONObject("content");
      
      // keys = npcContent.keys();
      // while(keys.hasNext()) {
      //    name = keys.next();
         
      //    if(name.equals("Marlon")) {
      //       id = insertShopkeeper(connection, name);
      //       insertShop(connection, "AdventurerGuild", "Mountain 1", "2pm to 10pm each day, except for festivals.", id);
      //    } else if(name.equals("Clint")) {
      //       id = insertShopkeeper(connection, name);
      //       idMap.put(name, id);
      //       insertShop(connection, "Blacksmith", "Mountain 1", "9am to 4pm each day, except for Winter 16, Fridays, and festivals.", id);
      //    } else if(name.equals("Robin")) {
      //       id = insertShopkeeper(connection, name);
      //       idMap.put(name, id);
      //       insertShop(connection, "CarpentersShop", "24 Mountain Road", "9am to 5pm each day, except for Summer 18, Tuesdays, and festivals", id);
      //    } else if(name.equals("Willy")) {
      //       id = insertShopkeeper(connection, name);
      //       idMap.put(name, id);
      //       insertShop(connection, "FishShop", "Beach 1", "8am to 5pm each day, except for non-rainy Saturdays, 10am to 2am on Spring 9, and festivals.", id);
      //    } else if(name.equals("Harvey")) {
      //       id = insertShopkeeper(connection, name);
      //       idMap.put(name, id);
      //       insertShop(connection, "Clinic", "Pelican Town 1", "9am to 2pm on Tuesdays and Thursdays, and 9am to 12pm on Sundays, Mondays, Wednesdays, and Fridays. Closed for festivals.", id);
      //    } else if(name.equals("Alex")) {
      //       id = insertShopkeeper(connection, name);
      //       idMap.put(name, id);
      //       insertShop(connection, "IceCreamStand", "Near the museum", "1pm to 5pm in the summer, except for Wednesdays, Summer 16, and rainy days.", id);
      //    } else if(name.equals("Marnie")) {
      //       id = insertShopkeeper(connection, name);
      //       idMap.put(name, id);
      //       insertShop(connection, "AnimalRanch", "Cindersap Forest 1", "9am to 4pm, except for Mondays, Tuesdays, Fall 18, Winter 18, and festivals.", id);
      //    } else if(name.equals("Sandy")) {
      //       id = insertShopkeeper(connection, name);
      //       idMap.put(name, id);
      //       insertShop(connection, "Oasis", "Calico Desert 2", "9am to 11:50pm, except for festivals.", id);
      //    } else if(name.equals("Pierre")) {
      //       id = insertShopkeeper(connection, name);
      //       idMap.put(name, id);
      //       insertShop(connection, "GeneralShop", "Pelican Town 2", "9am to 5pm, except for festivals.", id);
      //    } else if(name.equals("Gus")) {
      //       id = insertShopkeeper(connection, name);
      //       idMap.put(name, id);
      //       insertShop(connection, "Saloon", "Pelican Town 3", "12pm to 12am, except for festivals and until 4:30pm on Fall 4.", id);
      //    } else if(name.equals("Wizard")) {
      //       id = insertShopkeeper(connection, name);
      //       idMap.put(name, id);
      //       insertShop(connection, "WizardTower", "Cindersap Forest 2", "6am to 11pm, except for Spring 24 and Winter 8.", id);
      //    } else if(name.equals("Lewis")) {
      //       // id = insertManager(connection, name);
      //       idMap.put(name, id);
      //       insertShop(connection, "MovieTheater", "Pelican Town 3", "9am to 9pm every day.", id);
      //    } else if(name.equals("Dwarf")) {
      //       id = insertShopkeeper(connection, name);
      //       idMap.put(name, id);
      //       insertShop(connection, "MinesShop", "In the Mountain mines", "Always open.", id);
      //    } else if(name.equals("Krobus")) {
      //       id = insertShopkeeper(connection, name);
      //       idMap.put(name, id);
      //       insertShop(connection, "SewerShop", "In the Pelican Town sewers", "Always open.", id);
      //    } else {
      //       id = insertVillager(connection, name);
      //    }
         
      //    idMap.put(name, id);
      // }
      
      insertProfession(connection, "AnimalProduct Price", 1.2);
      insertProfession(connection, "PlantProduct Price", 1.1);
      insertProfession(connection, "ArtisanGood Price", 1.4);
      insertProfession(connection, "MetalBar Price", 1.5);
      insertProfession(connection, "Gem Price", 1.3);
      insertProfession(connection, "Wood Price", 1.25);
      insertProfession(connection, "Syrup Price", 1.25);
      insertProfession(connection, "Fish Price", 1.25);
      insertProfession(connection, "Fish Price", 1.5);
      
      
      insertGenerates(connection, nameMap.get("Tea Leaves"), nameMap.get("Green Tea"));
      insertGenerates(connection, nameMap.get("Coffee Bean"), nameMap.get("Coffee"));
      insertGenerates(connection, nameMap.get("Wool"), nameMap.get("Cloth"));
      
      updateArtisanGood(connection, nameMap.get("Juice"), null, null, 0, 2.25);
      for(String idStr : categoryMap.get("-75")) {
         insertGenerates(connection, idMap.get(idStr), nameMap.get("Juice"));
      }
      
      updateArtisanGood(connection, nameMap.get("Honey"), null, null, null, 2.0);
      for(String idStr : categoryMap.get("-80")) {
         insertGenerates(connection, idMap.get(idStr), nameMap.get("Honey"));
      }
      
      int id0 = insertArtisanGood(connection, "Wine", 0, 0, 3.0*1.00);
      int id1 = insertArtisanGood(connection, "Wine", 1, 0, 3.0*1.25);
      int id2 = insertArtisanGood(connection, "Wine", 2, 0, 3.0*1.50);
      int id3 = insertArtisanGood(connection, "Wine", 3, 0, 3.0*2.00);
      for(String idStr : categoryMap.get("-79")) {
         insertGenerates(connection, idMap.get(idStr), id0);
         insertGenerates(connection, idMap.get(idStr), id1);
         insertGenerates(connection, idMap.get(idStr), id2);
         insertGenerates(connection, idMap.get(idStr), id3);
      }
      
      id0 = insertArtisanGood(connection, "Pale Ale", 0, 300, 0);
      id1 = insertArtisanGood(connection, "Pale Ale", 1, 375, 0);
      id2 = insertArtisanGood(connection, "Pale Ale", 2, 450, 0);
      id3 = insertArtisanGood(connection, "Pale Ale", 3, 600, 0);
      insertGenerates(connection, nameMap.get("Hops"), id0);
      insertGenerates(connection, nameMap.get("Hops"), id1);
      insertGenerates(connection, nameMap.get("Hops"), id2);
      insertGenerates(connection, nameMap.get("Hops"), id3);
      
      id0 = insertArtisanGood(connection, "Beer", 0, 200, 0);
      id1 = insertArtisanGood(connection, "Beer", 1, 250, 0);
      id2 = insertArtisanGood(connection, "Beer", 2, 300, 0);
      id3 = insertArtisanGood(connection, "Beer", 3, 400, 0);
      insertGenerates(connection, nameMap.get("Wheat"), id0);
      insertGenerates(connection, nameMap.get("Wheat"), id1);
      insertGenerates(connection, nameMap.get("Wheat"), id2);
      insertGenerates(connection, nameMap.get("Wheat"), id3);
      
      id0 = insertArtisanGood(connection, "Mead", 0, 200, 0);
      id1 = insertArtisanGood(connection, "Mead", 1, 250, 0);
      id2 = insertArtisanGood(connection, "Mead", 2, 300, 0);
      id3 = insertArtisanGood(connection, "Mead", 3, 400, 0);
      insertGenerates(connection, nameMap.get("Honey"), id0);
      insertGenerates(connection, nameMap.get("Honey"), id1);
      insertGenerates(connection, nameMap.get("Honey"), id2);
      insertGenerates(connection, nameMap.get("Honey"), id3);
      
      id0 = insertArtisanGood(connection, "Cheese", 0, 230, 0);
      id1 = insertArtisanGood(connection, "Cheese", 1, 287, 0);
      id2 = insertArtisanGood(connection, "Cheese", 2, 345, 0);
      id3 = insertArtisanGood(connection, "Cheese", 3, 460, 0);
      insertGenerates(connection, nameMap.get("Milk"), id0);
      insertGenerates(connection, nameMap.get("Milk"), id1);
      insertGenerates(connection, nameMap.get("Milk"), id2);
      insertGenerates(connection, nameMap.get("Milk"), id3);
      insertGenerates(connection, nameMap.get("Large Milk"), id0);
      insertGenerates(connection, nameMap.get("Large Milk"), id1);
      insertGenerates(connection, nameMap.get("Large Milk"), id2);
      insertGenerates(connection, nameMap.get("Large Milk"), id3);
      
      id0 = insertArtisanGood(connection, "Goat Cheese", 0, 400, 0);
      id1 = insertArtisanGood(connection, "Goat Cheese", 1, 500, 0);
      id2 = insertArtisanGood(connection, "Goat Cheese", 2, 600, 0);
      id3 = insertArtisanGood(connection, "Goat Cheese", 3, 800, 0);
      insertGenerates(connection, nameMap.get("Goat Milk"), id0);
      insertGenerates(connection, nameMap.get("Goat Milk"), id1);
      insertGenerates(connection, nameMap.get("Goat Milk"), id2);
      insertGenerates(connection, nameMap.get("Goat Milk"), id3);
      insertGenerates(connection, nameMap.get("L. Goat Milk"), id0);
      insertGenerates(connection, nameMap.get("L. Goat Milk"), id1);
      insertGenerates(connection, nameMap.get("L. Goat Milk"), id2);
      insertGenerates(connection, nameMap.get("L. Goat Milk"), id3);
      
      id0 = insertArtisanGood(connection, "Mayonnaise", 0, 190, 0);
      id1 = insertArtisanGood(connection, "Mayonnaise", 1, 237, 0);
      id2 = insertArtisanGood(connection, "Mayonnaise", 2, 285, 0);
      id3 = insertArtisanGood(connection, "Mayonnaise", 3, 380, 0);
      insertGenerates(connection, nameMap.get("Egg"), id0);
      insertGenerates(connection, nameMap.get("Egg"), id1);
      insertGenerates(connection, nameMap.get("Egg"), id2);
      insertGenerates(connection, nameMap.get("Egg"), id3);
      insertGenerates(connection, nameMap.get("Large Egg"), id0);
      insertGenerates(connection, nameMap.get("Large Egg"), id1);
      insertGenerates(connection, nameMap.get("Large Egg"), id2);
      insertGenerates(connection, nameMap.get("Large Egg"), id3);
      insertGenerates(connection, nameMap.get("Ostrich Egg"), id0);
      insertGenerates(connection, nameMap.get("Ostrich Egg"), id1);
      insertGenerates(connection, nameMap.get("Ostrich Egg"), id2);
      insertGenerates(connection, nameMap.get("Ostrich Egg"), id3);
      insertGenerates(connection, nameMap.get("Golden Egg"), id0);
      insertGenerates(connection, nameMap.get("Golden Egg"), id1);
      insertGenerates(connection, nameMap.get("Golden Egg"), id2);
      insertGenerates(connection, nameMap.get("Golden Egg"), id3);
      
      insertGenerates(connection, nameMap.get("Duck Egg"), nameMap.get("Duck Mayonnaise"));
      insertGenerates(connection, nameMap.get("Void Egg"), nameMap.get("Void Mayonnaise"));
      insertGenerates(connection, nameMap.get("Dinosaur Egg"), nameMap.get("Dinosaur Mayonnaise"));
      
      insertGenerates(connection, nameMap.get("Truffle"), nameMap.get("Truffle Oil"));
      insertGenerates(connection, nameMap.get("Corn"), nameMap.get("Oil"));
      insertGenerates(connection, nameMap.get("Sunflower Seeds"), nameMap.get("Oil"));
      insertGenerates(connection, nameMap.get("Sunflower"), nameMap.get("Oil"));
      
      updateArtisanGood(connection, nameMap.get("Pickles"), null, null, 50, 2.0);
      insertGenerates(connection, nameMap.get("Ginger"), nameMap.get("Pickles"));
      for(String idStr : categoryMap.get("-75")) {
         insertGenerates(connection, idMap.get(idStr), nameMap.get("Pickles"));
      }
      
      updateArtisanGood(connection, nameMap.get("Jelly"), null, null, 50, 2.0);
      for(String idStr : categoryMap.get("-79")) {
         insertGenerates(connection, idMap.get(idStr), nameMap.get("Jelly"));
      }
      
      insertGenerates(connection, nameMap.get("Roe"), nameMap.get("Caviar"));
      
      updateArtisanGood(connection, nameMap.get("Aged Roe"), null, null, 0, 2.0);
      insertGenerates(connection, nameMap.get("Roe"), nameMap.get("Aged Roe"));
   }
   
   public static int insertProfession(Connection connection, String category, double multiplier) throws Exception {
      String query = "{? = call insert_Profession(?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, category);
      statement.setDouble(3, multiplier);
      statement.registerOutParameter(4, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(4);
      
      if (result == 0)
         System.out.printf("Successfully inserted Profession for category %s with boost %f.\n", category, multiplier);
      else
         System.out.printf("ERROR in insertProfesison: Failed with error code %d.\n", result);
      
      return id;
   }
   
   public static int insertHasProfession(Connection connection, int profId, int farmerId) throws Exception {
      String query = "{? = call insert_HasProfession(?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, profId);
      statement.setInt(3, farmerId);
      statement.registerOutParameter(4, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(4);
      
      if (result == 0)
         System.out.printf("Successfully inserted HasProfession for farmer %d with profession %d.\n", farmerId, profId);
      else
         System.out.printf("ERROR in insertProfesison: Failed with error code %d.\n", result);
      
      return id;
   }

   public static void insertShop(Connection connection, String name, String address, String schedule, int shopkeeperId)
         throws Exception {
      String query = "{? = call insert_Shop(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, shopkeeperId);
      statement.setString(3, name);
      statement.setString(4, address);
      statement.setString(5, schedule);
      statement.execute();
      int result = statement.getInt(1);

      if (result == 0)
         System.out.printf("Successfully inserted Shop with name %s, address %s, schedule %s, and shopkeeper %d.\n",
               name, address, schedule, shopkeeperId);
      else
         System.out.printf("ERROR in insertShopkeeper: Failed with error code %d.\n", result);
   }

   public static void insertNeeds(Connection connection, int villagerId, int itemId, int reward, int quantity)
         throws Exception {
      String query = "{? = call insert_Needs(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, villagerId);
      statement.setInt(3, itemId);
      statement.setInt(4, reward);
      statement.setInt(5, quantity);
      statement.execute();
      int result = statement.getInt(1);

      if (result == 0)
         System.out.printf(
               "Successfully inserted Needs with villagerId %d, itemId %d, reward %d, and quantity %d.\n",
               villagerId, itemId, reward, quantity);
      else
         System.out.printf("ERROR in insertNeeds: Failed with error code %d.\n", result);
   }

   public static int insertShopkeeper(Connection connection, String name) throws Exception {
      String query = "{? = call insert_Shopkeeper(?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      statement.registerOutParameter(3, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(3);

      if (result == 0)
         System.out.printf("Successfully inserted Shopkeeper with name %s.\n", name);
      else
         System.out.printf("ERROR in insertShopkeeper: Failed with error code %d.\n", result);
      
      return id;
   }
   
   public static int insertVillager(Connection connection, String name) throws Exception {
      String query = "{? = call insert_Villager(?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      statement.registerOutParameter(3, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(3);
      
      if (result == 0)
         System.out.printf("Successfully inserted Villager with name %s.\n", name);
      else
         System.out.printf("ERROR in insertVillager: Failed with error code %d.\n", result);
      
      return id;
   }

   public static void insertGenerates(Connection connection, int produceID, int productID) throws Exception {
      String query = "{? = call insert_Generates(?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, produceID);
      statement.setInt(3, productID);
      statement.execute();
      int result = statement.getInt(1);

      if (result == 0)
         System.out.printf("Successfully inserted Generates relation for produce %d and product %d.\n", produceID,
               productID);
      else
         System.out.printf("ERROR in insertGenerates: Failed with error code %d.\n", result);
   }

   public static void insertProduces(Connection connection, int animalID, int produceID) throws Exception {
      String query = "{? = call insert_Produces(?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, animalID);
      statement.setInt(3, produceID);
      statement.execute();
      int result = statement.getInt(1);

      if (result == 0)
         System.out.printf("Successfully inserted Produces relation for animal %d and animal product %d.\n",
               animalID, produceID);
      else
         System.out.printf("ERROR in insertProduces: Failed with error code %d.\n", result);
   }

   public static void insertHasIngredient(Connection connection, int ingredientId, int foodId) throws Exception {
      String query = "{? = call insert_HasIngredient(?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, ingredientId);
      statement.setInt(3, foodId);
      statement.execute();
      int result = statement.getInt(1);

      if (result == 0)
         System.out.printf("Successfully inserted HasIngredient relation for ingredient %d and food %d.\n",
               ingredientId, foodId);
      else
         System.out.printf("ERROR in insertHasIngredient: Failed with error code %d.\n", result);
   }

   public static int insertSeed(Connection connection, String name, int basePrice, String season) throws Exception {
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

      if (result == 0)
         System.out.printf("Successfully inserted Seed with name %s, price %d, and season %s.\n", name, basePrice,
               season);
      else
         System.out.printf("ERROR in insertSeed: Failed with error code %d.\n", result);

      return id;
   }

   public static int insertArtisanGood(Connection connection, String name, Integer quality, int basePrice,
         double multiplier) throws Exception {
      String query = "{? = call insert_ArtisanGood(?, ?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      if (quality == null)
         statement.setNull(3, Types.INTEGER);
      else
         statement.setInt(3, quality);
      statement.setInt(4, basePrice);
      statement.setDouble(5, multiplier);
      statement.registerOutParameter(6, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(6);

      if (result == 0)
         System.out.printf(
               "Successfully inserted ArtisanGood with name %s, quality %d, price %d, and multiplier %f.\n", name,
               quality, basePrice, multiplier);
      else
         System.out.printf("ERROR in insertArtisanGood: Failed with error code %d.\n", result);

      return id;
   }

   public static void updateArtisanGood(Connection connection, int id, String name, Integer quality, Integer basePrice,
         Double multiplier) throws Exception {
      String query = "{? = call update_ArtisanGood(?, ?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, id);
      statement.setString(3, name);
      if (quality == null)
         statement.setNull(4, Types.INTEGER);
      else
         statement.setInt(4, quality);
      if (basePrice == null)
         statement.setNull(5, Types.INTEGER);
      else
         statement.setInt(5, basePrice);
      if (multiplier == null)
         statement.setNull(6, Types.DOUBLE);
      else
         statement.setDouble(6, multiplier);
      statement.execute();
      int result = statement.getInt(1);

      if (result == 0)
         System.out.printf("Successfully updated ArtisanGood with ID %d.\n", id);
      else
         System.out.printf("ERROR in updateArtisanGood: Failed with error code %d.\n", result);
   }

   public static int insertPlantProduct(Connection connection, String name, Integer quality, int basePrice,
         String type) throws Exception {
      String query = "{? = call insert_PlantProduct(?, ?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      if (quality == null)
         statement.setNull(3, Types.INTEGER);
      else
         statement.setInt(3, quality);
      statement.setInt(4, basePrice);
      statement.setString(5, type);
      statement.registerOutParameter(6, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(6);

      if (result == 0)
         System.out.printf("Successfully inserted PlantProduct with name %s (%s), quality %d, and price %d.\n", name,
               type, quality, basePrice);
      else
         System.out.printf("ERROR in insertPlantProduct: Failed with error code %d.\n", result);

      return id;
   }

   public static int insertAnimal(Connection connection, String name, Integer basePrice) throws Exception {
      String query = "{? = call insert_Animal(?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      statement.setInt(3, basePrice);
      statement.registerOutParameter(4, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(4);

      if (result == 0)
         System.out.printf("Successfully inserted Animal with name %s and price %d.\n", name, basePrice);
      else
         System.out.printf("ERROR in insertAnimal: Failed with error code %d.\n", result);

      return id;
   }

   public static int insertAnimalProduct(Connection connection, String name, Integer quality, int basePrice)
         throws Exception {
      String query = "{? = call insert_AnimalProduct(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      if (quality == null)
         statement.setNull(3, Types.INTEGER);
      else
         statement.setInt(3, quality);
      statement.setInt(4, basePrice);
      statement.registerOutParameter(5, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(5);

      if (result == 0)
         System.out.printf("Successfully inserted AnimalProduct with name %s, qualiy %d, and price %d.\n", name,
               quality, basePrice);
      else
         System.out.printf("ERROR in insertAnimalProduct: Failed with error code %d.\n", result);

      return id;
   }

   public static int insertProduce(Connection connection, String name, Integer quality, int basePrice)
         throws Exception {
      String query = "{? = call insert_Produce(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      if (quality == null)
         statement.setNull(3, Types.INTEGER);
      else
         statement.setInt(3, quality);
      statement.setInt(4, basePrice);
      statement.registerOutParameter(5, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(5);

      if (result == 0)
         System.out.printf("Successfully inserted Produce with name %s, qualiy %d, and price %d.\n", name, quality,
               basePrice);
      else
         System.out.printf("ERROR in insertProduce: Failed with error code %d.\n", result);

      return id;
   }

   public static int insertFish(Connection connection, String name, Integer quality, int basePrice) throws Exception {
      String query = "{? = call insert_Fish(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      if (quality == null)
         statement.setNull(3, Types.INTEGER);
      else
         statement.setInt(3, quality);
      statement.setInt(4, basePrice);
      statement.registerOutParameter(5, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(5);

      if (result == 0)
         System.out.printf("Successfully inserted Fish with name %s, qualiy %d, and price %d.\n", name, quality,
               basePrice);
      else
         System.out.printf("ERROR in insertFish: Failed with error code %d.\n", result);

      return id;
   }

   public static int insertFood(Connection connection, String name, int basePrice) throws Exception {
      String query = "{? = call insert_Food(?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      statement.setInt(3, basePrice);
      statement.registerOutParameter(4, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(4);

      if (result == 0)
         System.out.printf("Successfully inserted Food with name %s and price %d.\n", name, basePrice);
      else
         System.out.printf("ERROR in insertFood: Failed with error code %d.\n", result);

      return id;
   }

   public static int insertItem(Connection connection, String name, Integer quality, int basePrice) throws Exception {
      String query = "{? = call insert_Item(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      if (quality == null)
         statement.setNull(3, Types.INTEGER);
      else
         statement.setInt(3, quality);
      statement.setInt(4, basePrice);
      statement.registerOutParameter(5, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(5);

      if (result == 0)
         System.out.printf("Successfully inserted Item with name %s, qualiy %d, and price %d.\n", name, quality,
               basePrice);
      else
         System.out.printf("ERROR in insertItem: Failed with error code %d.\n", result);

      return id;
   }

   public static void insertFarm(Connection connection, String name, String season) throws Exception {
      String query = "{? = call insert_Farm(?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      statement.setString(3, season);
      statement.execute();
      int result = statement.getInt(1);

      if (result == 0)
         System.out.printf("Successfully inserted Farm with name %s, and season %s.\n", name, season);
      else
         System.out.printf("ERROR in insertFarm: Failed with error code %d.\n", result);
   }
   
   public static int insertFarmer(Connection connection, String name, int farmid) throws Exception {
      String query = "{? = call insert_Farmer(?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setString(2, name);
      statement.setInt(3, farmid);
      statement.registerOutParameter(5, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(5);

      if (result == 0)
         System.out.printf("Successfully inserted Farmer with name %s, and farmId %d.\n", name, farmid);
      else
         System.out.printf("ERROR in insertFarmer: Failed with error code %d.\n", result);
      
      return id;
   }

   public static void insertShopBuys(Connection connection, int shopId, int itemId) throws Exception {
      String query = "{? = call insert_ShopBuys(?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, shopId);
      statement.setInt(3, itemId);
      statement.execute();
      int result = statement.getInt(1);

      if (result == 0)
         System.out.printf("Successfully inserted ShopBuys with shopId %d, and itemId %d.\n", shopId, itemId);
      else
         System.out.printf("ERROR in insertShopBuys: Failed with error code %d.\n", result);
   }

   public static int insertShopSells(Connection connection, int shopId, int itemId) throws Exception {
      String query = "{? = call insert_ShopSells(?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, shopId);
      statement.setInt(3, itemId);
      statement.registerOutParameter(5, Types.INTEGER);
      statement.execute();
      int result = statement.getInt(1);
      int id = statement.getInt(5);

      if (result == 0)
         System.out.printf("Successfully inserted ShopSells with shopId %d, and itemId %d.\n", shopId, itemId);
      else
         System.out.printf("ERROR in insertShopSells: Failed with error code %d.\n", result);

      return id;
   }

   public static void updateAnimal(Connection connection, int id, String name, Integer quality, Integer basePrice)
         throws Exception {
      String query = "{? = call update_Animal(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, id);
      if (name == null)
         statement.setNull(3, Types.VARCHAR);
      else
         statement.setString(3, name);
      if (quality == null)
         statement.setNull(4, Types.INTEGER);
      else
         statement.setInt(4, quality);
      if (basePrice == null)
         statement.setNull(5, Types.INTEGER);
      else
         statement.setInt(5, basePrice);
      statement.execute();
      int result = statement.getInt(1);

      if (result == 0)
         System.out.printf("Successfully updated Animal with ID %d.\n", id);
      else
         System.out.printf("ERROR in updateAnimal: Failed with error code %d.\n", result);
   }

   public static void updateAnimalProduct(Connection connection, int id, String name, Integer quality,
         Integer basePrice) throws Exception {
      String query = "{? = call update_AnimalProduct(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, id);
      if (name == null)
         statement.setNull(3, Types.VARCHAR);
      else
         statement.setString(3, name);
      if (quality == null)
         statement.setNull(4, Types.INTEGER);
      else
         statement.setInt(4, quality);
      if (basePrice == null)
         statement.setNull(5, Types.INTEGER);
      else
         statement.setInt(5, basePrice);
      statement.execute();
      int result = statement.getInt(1);

      if (result == 0)
         System.out.printf("Successfully updated AnimalProduct with ID %d.\n", id);
      else
         System.out.printf("ERROR in updateAnimalProduct: Failed with error code %d.\n", result);
   }

   public static void updateFish(Connection connection, int id, String name, Integer quality, Integer basePrice)
         throws Exception {
      String query = "{? = call update_Fish(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, id);
      if (name == null)
         statement.setNull(3, Types.VARCHAR);
      else
         statement.setString(3, name);
      if (quality == null)
         statement.setNull(4, Types.INTEGER);
      else
         statement.setInt(4, quality);
      if (basePrice == null)
         statement.setNull(5, Types.INTEGER);
      else
         statement.setInt(5, basePrice);
      statement.execute();
      int result = statement.getInt(1);

      if (result == 0)
         System.out.printf("Successfully updated Fish with ID %d.\n", id);
      else
         System.out.printf("ERROR in updateFish: Failed with error code %d.\n", result);
   }

   public static void updateFood(Connection connection, int id, String name, Integer quality, Integer basePrice)
         throws Exception {
      String query = "{? = call update_Food(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, id);
      if (name == null)
         statement.setNull(3, Types.VARCHAR);
      else
         statement.setString(3, name);
      if (quality == null)
         statement.setNull(4, Types.INTEGER);
      else
         statement.setInt(4, quality);
      if (basePrice == null)
         statement.setNull(5, Types.INTEGER);
      else
         statement.setInt(5, basePrice);
      statement.execute();
      int result = statement.getInt(1);

      if (result == 0)
         System.out.printf("Successfully updated Food with ID %d.\n", id);
      else
         System.out.printf("ERROR in updateFood: Failed with error code %d.\n", result);
   }

   public static void updateItem(Connection connection, int id, String name, Integer quality, Integer basePrice)
         throws Exception {
      String query = "{? = call update_Item(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, id);
      if (name == null)
         statement.setNull(3, Types.VARCHAR);
      else
         statement.setString(3, name);
      if (quality == null)
         statement.setNull(4, Types.INTEGER);
      else
         statement.setInt(4, quality);
      if (basePrice == null)
         statement.setNull(5, Types.INTEGER);
      else
         statement.setInt(5, basePrice);
      statement.execute();
      int result = statement.getInt(1);

      if (result == 0)
         System.out.printf("Successfully updated Item with ID %d.\n", id);
      else
         System.out.printf("ERROR in updateItem: Failed with error code %d.\n", result);
   }

   public static void updatePlantProduct(Connection connection, int id, String name, Integer quality,
         Integer basePrice, String type) throws Exception {
      String query = "{? = call update_PlantProduct(?, ?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, id);
      if (name == null)
         statement.setNull(3, Types.VARCHAR);
      else
         statement.setString(3, name);
      if (quality == null)
         statement.setNull(4, Types.INTEGER);
      else
         statement.setInt(4, quality);
      if (basePrice == null)
         statement.setNull(5, Types.INTEGER);
      else
         statement.setInt(5, basePrice);
      if (type == null)
         statement.setNull(6, Types.VARCHAR);
      else
         statement.setString(6, type);
      statement.execute();
      int result = statement.getInt(1);

      if (result == 0)
         System.out.printf("Successfully updated PlantProduct with ID %d.\n", id);
      else
         System.out.printf("ERROR in updatePlantProduct: Failed with error code %d.\n", result);
   }

   public static void updateProduce(Connection connection, int id, String name, Integer quality, Integer basePrice)
         throws Exception {
      String query = "{? = call update_Produce(?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, id);
      if (name == null)
         statement.setNull(3, Types.VARCHAR);
      else
         statement.setString(3, name);
      if (quality == null)
         statement.setNull(4, Types.INTEGER);
      else
         statement.setInt(4, quality);
      if (basePrice == null)
         statement.setNull(5, Types.INTEGER);
      else
         statement.setInt(5, basePrice);
      statement.execute();
      int result = statement.getInt(1);

      if (result == 0)
         System.out.printf("Successfully updated Produce with ID %d.\n", id);
      else
         System.out.printf("ERROR in updateProduce: Failed with error code %d.\n", result);
   }

   public static void updateSeed(Connection connection, int id, String name, Integer quality, Integer basePrice,
         String season) throws Exception {
      String query = "{? = call update_Seed(?, ?, ?, ?, ?)}";
      CallableStatement statement = connection.prepareCall(query);
      statement.registerOutParameter(1, Types.INTEGER);
      statement.setInt(2, id);
      if (name == null)
         statement.setNull(3, Types.VARCHAR);
      else
         statement.setString(3, name);
      if (quality == null)
         statement.setNull(4, Types.INTEGER);
      else
         statement.setInt(4, quality);
      if (basePrice == null)
         statement.setNull(5, Types.INTEGER);
      else
         statement.setInt(5, basePrice);
      if (season == null)
         statement.setNull(6, Types.VARCHAR);
      else
         statement.setString(6, season);
      statement.execute();
      int result = statement.getInt(1);

      if (result == 0)
         System.out.printf("Successfully updated Seed with ID %d.\n", id);
      else
         System.out.printf("ERROR in updateSeed: Failed with error code %d.\n", result);
   }

}