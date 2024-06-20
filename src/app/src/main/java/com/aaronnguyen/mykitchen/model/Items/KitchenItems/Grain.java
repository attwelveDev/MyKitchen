package com.aaronnguyen.mykitchen.model.Items.KitchenItems;

import com.aaronnguyen.mykitchen.model.user.User;

import java.util.Date;
import java.util.List;

public class Grain extends Item {
    public Grain(String name, Date expiryDate, Date boughtDate, User associatedUser, int quantity, String storageLocation, List<ItemUseSchedule> schedule) {
        super(name, expiryDate, boughtDate, associatedUser, quantity, storageLocation, schedule);
    }
}