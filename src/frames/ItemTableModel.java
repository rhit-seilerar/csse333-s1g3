package frames;

import frames.Item;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class ItemTableModel extends AbstractTableModel{

    private ArrayList<Item> data = null;
    private String[] columnNames = {"ID", "Name", "Quality", "Base Price"};

    public ItemTableModel(ArrayList<Item> data) {
        this.data = data;

    }

    @Override
    public int getRowCount() {
        return this.data.size();
    }

    @Override
    public int getColumnCount() {
        return this.columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return this.data.get(rowIndex).getValue(this.columnNames[columnIndex]);
    }

    @Override
    public String getColumnName(int index) {
        return this.columnNames[index];
    }



}
