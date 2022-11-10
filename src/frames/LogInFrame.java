package frames;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import services.DatabaseConnectionService;

public class LogInFrame extends JFrame implements ActionListener {
	// Set this to '7' (or any non-null number really) to skip login.
	public static Integer permissions = null;
	
	//Connectivity Services
	private DatabaseConnectionService dbcs;
	
	//User Data
	private static String username;
	private static String password;
	public static Random random = new SecureRandom();
	
	//Components
	private JPanel userInfo;
	private JTextField user;
	private JTextField passw;
	private JButton login;
	private JButton register;
	
	public LogInFrame(DatabaseConnectionService dbcs) {
		//Connectivity Set Up
		this.dbcs = dbcs;
		
		//Component Set Up
		this.userInfo = new JPanel();
		JLabel users = new JLabel("Username: ");
		this.user = new JTextField(15);
		JLabel pass = new JLabel("Password: ");
		this.passw = new JTextField(15);
		this.login = new JButton("Log In");
		this.login.addActionListener(this);
		this.register = new JButton("Register User");
		this.register.addActionListener(this);

		//Component Combination
		this.add(userInfo);
		this.userInfo.add(users);
		this.userInfo.add(this.user);
		this.userInfo.add(Box.createRigidArea(new Dimension(10, 0)));
		this.userInfo.add(pass);
		this.userInfo.add(this.passw);
		this.userInfo.add(Box.createRigidArea(new Dimension(10, 0)));
		this.userInfo.add(this.login);
		this.userInfo.add(Box.createRigidArea(new Dimension(10, 0)));
		this.userInfo.add(this.register);
		
		
		//Formatting
		this.userInfo.setLayout(new BoxLayout(this.userInfo, BoxLayout.X_AXIS));
		this.userInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.setSize(800, 100);
		this.setVisible(true);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Boolean isManager = false;
		Boolean managerExists;
        Integer type = null;
        Integer farmId = null;
		username = this.user.getText();
		password = this.passw.getText();
		if(e.getSource() == this.login) {
        	try {
				CallableStatement statement = this.dbcs.getConnection().prepareCall("{? = call get_Login(?)}");
				statement.registerOutParameter(1, Types.INTEGER);
				statement.setString(2, username);
				ResultSet resultSet = statement.executeQuery();
			       
				if(resultSet.isClosed() || !resultSet.next()) {
					JOptionPane warning = new JOptionPane();
					JOptionPane.showMessageDialog(new LogInFrame(this.dbcs), "Incorrect Log-In Information");
					warning.setVisible(true);
					this.dispose();
				} else {
					permissions = resultSet.getInt("Type");
					byte[] storedHash = resultSet.getBytes("Hash");
			   		byte[] storedSalt = resultSet.getBytes("Salt");
			   		byte[] givenHash = hashPassword(password, storedSalt);
			         
			   		int i = 0;
			   		for(; i < 16; i++) {
			       		if(storedHash[i] != givenHash[i]) {
							break;
						}
			   		}
			   		if(i == 16) {
			       		System.out.printf("Successfully logged in %s\n", username);
						HomeFrame hf = new HomeFrame(this.dbcs, permissions);
						this.dispose();
			   		} else {
						JOptionPane warning = new JOptionPane();
						JOptionPane.showMessageDialog(new LogInFrame(this.dbcs), "Incorrect Log-In Information");
						warning.setVisible(true);
						this.dispose();
						permissions = null;
					}
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} else if (e.getSource() == this.register) {
			managerExists = null;
			try {
				CallableStatement statement = this.dbcs.getConnection().prepareCall("{? = call get_Login(?, ?)}");
				statement.registerOutParameter(1, Types.INTEGER);
         		statement.setNull(2, Types.VARCHAR);
         		statement.setInt(3, 7);
         		ResultSet resultSet = statement.executeQuery();
         		managerExists = !resultSet.isClosed() && resultSet.next();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            if(type == null) {
                if(!managerExists) {
                    isManager = null;
                    while(isManager == null) {
						JOptionPane managerial = new JOptionPane();
						int yn = JOptionPane.showOptionDialog(this, "Is This Acocunt A Manager?", "Manager Decision", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, 0);
						managerial.setVisible(true);
						if(yn == JOptionPane.YES_OPTION) {
							isManager = true;
						} else if(yn == JOptionPane.NO_OPTION) {
							isManager = false;
						}
                    }
            	}
				JOptionPane vsfSelection = new JOptionPane();
				String[] options = {"Villager", "Shopkeeper", "Farmer"};
				int selected = JOptionPane.showOptionDialog(this, "Villager, Shopkeeper, or Farmer?", "Villager, Shopkeeper, or Farmer?", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
				vsfSelection.setVisible(true);
				if(selected == 0) {
					type = 0;
					permissions = 0;
				} else if(selected == 1) {
					type = 1;
					permissions = 1;
				} else {
					if(farmId == null) {
						JOptionPane farmOptionPane = new JOptionPane();
						String farmName = farmOptionPane.showInputDialog(this, "What farm do you work on?");
						farmOptionPane.setVisible(true);
						try {
							CallableStatement statement = this.dbcs.getConnection().prepareCall("? = call get_Farm(?, ?)");
							statement.registerOutParameter(1, Types.INTEGER);
							statement.setNull(2, Types.INTEGER);
							statement.setString(3, farmName);
							ResultSet resultSet = statement.executeQuery();
							if(resultSet.isClosed() || !resultSet.next()) {
								System.out.println("That farm doesn't exist.");
							} else {
								farmId = resultSet.getInt("ID");
								permissions = 2;
							}
						} catch (SQLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
                }
				int result = 1;
				try {
					byte[] salt = new byte[16];
					random.nextBytes(salt);
					byte[] hash = hashPassword(password, salt);
				
					if(isManager) permissions = 7;
					
					CallableStatement statement = this.dbcs.getConnection().prepareCall("{? = call insert_Login(?, ?, ?, ?)}");
					statement.registerOutParameter(1, Types.INTEGER);
					statement.setString(2, username);
					statement.setBytes(3, hash);
					statement.setBytes(4, salt);
					statement.setInt(5, permissions);
					statement.execute();
					result = statement.getInt(1);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				if(result != 0) {
				   System.out.printf("ERROR: Failed to register %s\n", username);
				   permissions = null;
				} else {
				   System.out.printf("Successfully registered %s\n", username);
				   try {
				   if(type == 0) insertVillager(this.dbcs.getConnection(), username);
				   if(type == 1) insertShopkeeper(this.dbcs.getConnection(), username);
				   if(type == 2) insertFarmer(this.dbcs.getConnection(), username, farmId);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					HomeFrame hf = new HomeFrame(this.dbcs, permissions);
					this.dispose();
        		}
			}
        }

	public static byte[] hashPassword(String password, byte[] salt) throws Exception {
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		return keyFactory.generateSecret(spec).getEncoded();
	}

	public static void insertFarmer(Connection connection, String name, int farmid) throws Exception {
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
	 }

	 public static void insertShopkeeper(Connection connection, String name) throws Exception {
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
	 }

	 public static void insertVillager(Connection connection, String name) throws Exception {
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
	 }
}
