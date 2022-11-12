package frames;

public class Item {

//The Item table has a primary key ID which is a unique integer that cannot be null. Name is a length 20 varchar that cannot be null, Quality is a tinyint that cannot be null, and BasePrice is an integer that cannot be null.
    private int id;
    private String name;
    private int quality;
    private int basePrice;


    public Item(int id, String name, int quality, int basePrice){
        this.id = id;
        this.name = name;
        this.quality = quality;
        this.basePrice = basePrice;
    }


    public Object getValue(String name) {
        switch (name.toUpperCase())
        {
            case "ID":
                return this.getId();
            case "NAME":
                return this.getName();
            case "QUALITY":
                return this.getQuality();
            case "BASE PRICE":
                return this.getBasePrice();
        }
        throw new IllegalArgumentException("Name of " + name + " is not a valid argument.");

    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuality() {
        return quality;
    }

    public int getBasePrice() {
        return basePrice;
    }
}