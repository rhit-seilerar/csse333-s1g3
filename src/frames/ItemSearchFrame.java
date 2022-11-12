package frames;

import java.awt.BorderLayout;
import java.sql.SQLException;
import javax.swing.JFrame;

import services.SearchService;

public class ItemSearchFrame extends JFrame {

    private ItemSearchPanel panel;


    public ItemSearchFrame(SearchService searchService) throws SQLException {
        super();
        this.setSize(1000, 750);
        this.setResizable(false);
        this.setTitle("Exam App");
        this.panel = new ItemSearchPanel(searchService);

        this.add(this.panel, BorderLayout.CENTER);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setVisible(true);
        this.pack();
    }


}