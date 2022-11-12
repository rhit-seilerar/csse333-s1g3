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
import services.SearchService;

public class HomeFrame extends JFrame implements ActionListener {

    //Connectivity Services
	private DatabaseConnectionService dbcs;
    private SearchService search;

    private int permissions;

    private JPanel homePanel;

    //Everyone Permission Items
    private JButton viewItems;
    private ItemSearchFrame itemSearchFrame;
    private JButton viewShopSells;
    private JButton viewShopBuys;
    private JButton editNeeds;
    private JButton settings;
    private JButton logOut;

    //Permission Level 1 Items
    private JButton editShopSells;
    private JButton editShopBuys;
    private JButton viewFarmSells;

    //Permission Level 2 Items
    private JButton editFarmSells;
    private JButton editProfessions;
    private JButton editHasProfession;

    //Permission Level 7 Items
    private JButton populate;
    private JButton editItems;
    private JButton editFarms;
    private JButton editVillagers;
    private JButton editLogins;
    private JButton editShops;

    

    public HomeFrame(DatabaseConnectionService dbcs, int permissions) {
        this.dbcs = dbcs;
        this.permissions = permissions;
        this.search = new SearchService(this.dbcs);
        //Component Set Up
        this.homePanel = new JPanel();
        this.viewItems = new JButton("Search Items");
        viewItems.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    itemSearchFrame = new ItemSearchFrame(search);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
        this.viewShopSells = new JButton("View Shop Goods For Sale");
        this.viewShopBuys = new JButton("View Goods Shops Are Buying");
        this.editNeeds = new JButton("Need Something?");
        this.settings = new JButton("Account Settings");
        this.logOut = new JButton("Log Out");

        //Component Combination
        this.add(this.homePanel);
        this.homePanel.add(this.logOut);
        this.homePanel.add(Box.createRigidArea(new Dimension(5, 10)));
        this.homePanel.add(this.settings);
        this.homePanel.add(Box.createRigidArea(new Dimension(5, 10)));
        this.homePanel.add(this.viewItems);
        this.homePanel.add(Box.createRigidArea(new Dimension(5, 10)));
        this.homePanel.add(this.viewShopSells);
        this.homePanel.add(Box.createRigidArea(new Dimension(5, 10)));
        this.homePanel.add(this.viewShopBuys);
        this.homePanel.add(Box.createRigidArea(new Dimension(5, 10)));
        this.homePanel.add(this.editNeeds);
        this.homePanel.add(Box.createRigidArea(new Dimension(5, 10)));

        if((permissions & 1) != 0) {
            this.editShopSells = new JButton("Edit What Your Shop Is Selling");
            this.editShopBuys = new JButton("Edit What Your Shop Is Buying");
            this.viewFarmSells = new JButton("View What Farms Are Selling");
            this.homePanel.add(this.editShopSells);
            this.homePanel.add(Box.createRigidArea(new Dimension(5, 10)));
            this.homePanel.add(this.editShopBuys);
            this.homePanel.add(Box.createRigidArea(new Dimension(5, 10)));
            this.homePanel.add(this.viewFarmSells);
            this.homePanel.add(Box.createRigidArea(new Dimension(5, 10)));
        }

        if((permissions & 2) != 0) {
            this.editFarmSells = new JButton("Edit What Your Farm Is Selling");
            this.editProfessions = new JButton("Edit Possible Professions");
            this.editHasProfession = new JButton("Edit Who Has What Professions");
            this.homePanel.add(this.editFarmSells);
            this.homePanel.add(Box.createRigidArea(new Dimension(5, 10)));
            this.homePanel.add(this.editProfessions);
            this.homePanel.add(Box.createRigidArea(new Dimension(5, 10)));
            this.homePanel.add(this.editHasProfession);
            this.homePanel.add(Box.createRigidArea(new Dimension(5, 10)));
        }

        if((permissions & 4) != 0) {
            this.populate = new JButton("Populate");
            this.editItems = new JButton("Edit Items");
            this.editFarms = new JButton("Edit Farms");
            this.editVillagers = new JButton("Edit Villagers");
            this.editLogins = new JButton("Edit Logins");
            this.editShops = new JButton("Edit Shops");
            this.homePanel.add(this.populate);
            this.homePanel.add(Box.createRigidArea(new Dimension(5, 10)));
            this.homePanel.add(this.editItems);
            this.homePanel.add(Box.createRigidArea(new Dimension(5, 10)));
            this.homePanel.add(this.editFarms);
            this.homePanel.add(Box.createRigidArea(new Dimension(5, 10)));
            this.homePanel.add(this.editVillagers);
            this.homePanel.add(Box.createRigidArea(new Dimension(5, 10)));
            this.homePanel.add(this.editLogins);
            this.homePanel.add(Box.createRigidArea(new Dimension(5, 10)));
            this.homePanel.add(this.editShops);
        }

        //Formatting
        this.homePanel.setLayout(new BoxLayout(this.homePanel, BoxLayout.Y_AXIS));
		this.homePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		this.setSize(200, 1000);
		this.setVisible(true);
		this.setResizable(false);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // TODO Auto-generated method stub
        
    }
    
}
