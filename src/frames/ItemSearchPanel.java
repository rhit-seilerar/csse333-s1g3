package frames;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import services.SearchService;

public class ItemSearchPanel extends JPanel {

    private SearchService searchService = null;
    private JButton filterButton = null;
    private JTable table;
    private JTextField nameTextBox = null;

    public ItemSearchPanel(SearchService searchService) throws SQLException {
        this.searchService = searchService;
        JPanel filterPanel = generateFilterUiItems();
        this.setLayout(new BorderLayout());
        this.add(filterPanel, BorderLayout.NORTH);
        JScrollPane tablePane = generateAppointmentTable();
        this.add(tablePane, BorderLayout.CENTER);
    }

    private JScrollPane generateAppointmentTable() throws SQLException {
        this.table = new JTable(this.search());

        TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer() {

            SimpleDateFormat f = new SimpleDateFormat("MM/dd/yy hh:mm");

            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                if (value instanceof Timestamp) {
                    value = f.format(value);
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        };

        this.table.getColumnModel().getColumn(3).setCellRenderer(tableCellRenderer);

        JScrollPane scrollPane = new JScrollPane(this.table);
        this.table.setFillsViewportHeight(true);
        return scrollPane;

    }

    private JPanel generateFilterUiItems() {
        JPanel filterPanel = new JPanel();
        FlowLayout layout = new FlowLayout();
        layout.setHgap(15);
        this.nameTextBox = new JTextField(10);

        filterPanel.setLayout(layout);
        filterPanel.add(new JLabel("Name:"));
        filterPanel.add(this.nameTextBox);


        this.filterButton = new JButton("Search");
        filterPanel.add(filterButton);
        this.filterButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    refresh();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
        return filterPanel;
    }

    public void refresh() throws SQLException {
        table.setModel(search());
    }

    private ItemTableModel search() throws SQLException {
        String name = this.nameTextBox.getText();

        ArrayList<Item> data = this.searchService.getItem(name);
        return new ItemTableModel(data);
    }
}