package services;

import java.sql.*;
import java.util.ArrayList;

import frames.Item;

public class SearchService {

    private DatabaseConnectionService dbService = null;

    public SearchService(DatabaseConnectionService dbService) {
        this.dbService = dbService;
    }

    public ArrayList<Item> getItem(String name) throws SQLException {
        String query = "{? = call get_Item(?, ?, ?, ?)}";
        CallableStatement statement = dbService.getConnection().prepareCall(query);
        statement.registerOutParameter(1, Types.INTEGER);
        statement.setNull(2, Types.INTEGER);
        statement.setString(3, name);
        statement.setNull(4, Types.TINYINT);
        statement.setNull(5, Types.INTEGER);
        ArrayList<Item> ret = new ArrayList<Item>();
        ResultSet resultSet = statement.executeQuery();
        while(resultSet.next()){
            int id = resultSet.getInt(1);
            String retname = resultSet.getString(2);
            int quality = resultSet.getInt(3);
            int baseprice = resultSet.getInt(4);
            ret.add(new Item(id, retname, quality, baseprice));
        }
        return ret;
    }


}
