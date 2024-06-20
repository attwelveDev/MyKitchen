package com.aaronnguyen.mykitchen.model.Items.CartItems;

import com.aaronnguyen.mykitchen.model.user.User;

import java.util.List;

/**
 * Creates instances of CartItem
 * @author u7648367 Ruixian Wu (adapted from u7643339 Isaac Leong)
 */
public class CartItemFactory {

    public static CartItem createCartItem(String name, String type, int expiryDays, User associatedUser, int quantity, String storageLocation, List<CartSchedule> schedule) {
        switch(type.trim().toLowerCase()) {
            case "protein":
                return new CartProtein(name, expiryDays, associatedUser, quantity, storageLocation, schedule);
            case "vegetable":
                return new CartVegetable(name, expiryDays, associatedUser, quantity, storageLocation, schedule);
            case "fruit":
                return new CartFruit(name, expiryDays, associatedUser, quantity, storageLocation, schedule);
            case "dairy":
                return new CartDairy(name, expiryDays, associatedUser, quantity, storageLocation, schedule);
            case "grain":
                return new CartGrain(name, expiryDays, associatedUser, quantity, storageLocation, schedule);
            case "others", "other", "other items":
                return new CartOtherItem(name, expiryDays, associatedUser, quantity, storageLocation, schedule);
            default:
                throw new IllegalArgumentException("Cannot parse the type of the item");
        }
    }
}
