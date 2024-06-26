package com.aaronnguyen.mykitchen.model.Items.CartItems;

import com.aaronnguyen.mykitchen.model.user.User;

import java.util.List;

/**
 * CartItem of type Vegetable
 * @author u7648367 Ruixian Wu (adapted from u7643339 Isaac Leong)
 */
public class CartVegetable extends CartItem {
    public CartVegetable(String name, int expiryDays, User associatedUser, int quantity, String storageLocation, List<CartSchedule> schedule) {
        super(name, expiryDays, associatedUser, quantity, storageLocation, schedule);
    }
}
