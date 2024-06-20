package com.aaronnguyen.mykitchen.model.Items;

import com.aaronnguyen.mykitchen.model.user.User;

public class GeneralItem {

    public static final String NAME_FIELD_NAME = "name";
    public static final String TYPE_FIELD_NAME = "field";

    public static final String QUANTITY_FIELD_NAME = "quantity";
    public static final String STORAGE_LOCATION_FIELD_NAME = "storage location";
    public static final String ASSOCIATED_USER_ID_FIELD_NAME = "uid";
    public static final String ASSOCIATED_USER_EMAIL_FIELD_NAME = "uemail";


    protected String name;
    protected User associatedUser;
    protected int quantity;
    protected String storageLocation;

    public GeneralItem(String name, User associatedUser, int quantity, String storageLocation) {
        this.name = name;
        this.associatedUser = associatedUser;
        this.quantity = quantity;
        this.storageLocation = storageLocation;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStorageLocation() {
        return storageLocation;
    }
    public User getAssociatedUser() {
        return associatedUser;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }
}
