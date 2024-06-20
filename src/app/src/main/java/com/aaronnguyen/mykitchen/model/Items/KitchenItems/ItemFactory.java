package com.aaronnguyen.mykitchen.model.Items.KitchenItems;

import com.aaronnguyen.mykitchen.model.user.User;

import java.util.Date;
import java.util.List;

public class ItemFactory {

    /**
     * This is the factory of an item
     * @param name The name of the item
     * @param type The type of the item (i.e. the class)
     * @param expectedExpiry The expected expiry date of the item
     * @param boughtDate The bought date of the item
     * @param user The user tha is associated with the item
     * @param quantity The quantity of the item
     * @param storageLocation The storage location
     * @param schedule The schedule of the item
     * @return The created item
     */
    public static Item createItem(String name, String type, Date expectedExpiry, Date boughtDate, User user, int quantity, String storageLocation, List<ItemUseSchedule> schedule) {
        System.out.println(type.trim().toLowerCase());
        return switch (type.trim().toLowerCase()) {
            case "protein" ->
                    new Protein(name, expectedExpiry, boughtDate, user, quantity, storageLocation, schedule);
            case "vegetable" ->
                    new Vegetable(name, expectedExpiry, boughtDate, user, quantity, storageLocation, schedule);
            case "fruit" ->
                    new Fruit(name, expectedExpiry, boughtDate, user, quantity, storageLocation, schedule);
            case "dairy" ->
                    new Dairy(name, expectedExpiry, boughtDate, user, quantity, storageLocation, schedule);
            case "grain" ->
                    new Grain(name, expectedExpiry, boughtDate, user, quantity, storageLocation, schedule);
            case "others", "other", "other items" ->
                    new OtherItem(name, expectedExpiry, boughtDate, user, quantity, storageLocation, schedule);
            default -> throw new IllegalArgumentException("Cannot parse the type of the item");
        };
    }

}
