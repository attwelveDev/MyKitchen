package com.example.lib;

/**
 * Relevant item information from app/src/main/res/raw/australia_grocery_2022sep.csv
 */
public class ItemCSV {
    String name;
    String type;
    int pantryExpiryDays;
    int fridgeExpiryDays;
    int freezerExpiryDays;
    int defaultExpiryDays;
    static String[] categories = new String[]{"Dairy", "Fruit", "Grain", "Protein", "Vegetable", "OtherItem"};

    public ItemCSV(String name, String type, int pantryExpiryDays, int fridgeExpiryDays, int freezerExpiryDays, int defaultExpiryDays) {
        this.name = name;
        this.type = type;
        this.pantryExpiryDays = pantryExpiryDays;
        this.fridgeExpiryDays = fridgeExpiryDays;
        this.freezerExpiryDays = freezerExpiryDays;
        this.defaultExpiryDays = defaultExpiryDays;
    }
}
