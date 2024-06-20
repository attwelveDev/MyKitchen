package com.aaronnguyen.mykitchen.model.Search;
/**
 * The Product class represents a product with its name, type, and expiry days
 * for different storage conditions (pantry, fridge, freezer, and default).
 * @author u7517596 Chengbo Yan
 */
public class Product {
    private String name;

    private String type;

    private int pantryExpiryDays;

    private int fridgeExpiryDays;


    private int freezerExpiryDays;

    private int defaultExpiryDays;


    public Product(String name,String type, int pantryExpiryDays,
                   int fridgeExpiryDays, int freezerExpiryDays, int defaultExpiryDays) {
        this.name = name;
        this.type = type;
        this.pantryExpiryDays = pantryExpiryDays;
        this.fridgeExpiryDays = fridgeExpiryDays;
        this.freezerExpiryDays = freezerExpiryDays;
        this.defaultExpiryDays = defaultExpiryDays;
    }

    public String getType() {
        return type;
    }

    public int getPantryExpiryDays() {
        return pantryExpiryDays;
    }

    public int getFridgeExpiryDays() {
        return fridgeExpiryDays;
    }

    public int getFreezerExpiryDays() {
        return freezerExpiryDays;
    }

    public int getDefaultExpiryDays() {
        return defaultExpiryDays;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.type + this.name;
    }

    public void setPantryExpiryDays(int newDays) {
        pantryExpiryDays = newDays;
    }

    public void setFridgeExpiryDays(int newDays) {
        fridgeExpiryDays = newDays;
    }

    public void setFreezerExpiryDays(int newDays) {
        freezerExpiryDays = newDays;
    }

    public void setDefaultExpiryDays(int newDays) {
        defaultExpiryDays = newDays;
    }
}

