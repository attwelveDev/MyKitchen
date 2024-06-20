package com.aaronnguyen.mykitchen.DAO;

import com.aaronnguyen.mykitchen.model.Items.CartItems.CartItem;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Item;
import com.aaronnguyen.mykitchen.model.Items.ItemUsage;
import com.aaronnguyen.mykitchen.model.notification.Notification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents all the data that can be synchronised with the database.
 * In other word, this is a parcel for fetching from firebase
 */
public class KitchenData {
    private final String kitchenID;
    private final String kitchenName;
    private final ArrayList<Item> itemList;
    private final ArrayList<ItemUsage> itemUsageList;
    private final ArrayList<CartItem> cart;
    private final String chatRoomID;
    private final String ownerID;
    private final List<String> members;
    private final List<String> pendingResidents;
    private final List<String> bannedResidents;
    private final ArrayList<Notification> notifications;

    public KitchenData(String kitchenID, String kitchenName, ArrayList<Item> itemList, ArrayList<ItemUsage> itemUsageList, String chatRoomID, String ownerID, List<String> members, List<String> pendingResidents, List<String> bannedResidents, ArrayList<Notification> notifications, ArrayList<CartItem> cart) {
        this.kitchenID = kitchenID;
        this.kitchenName = kitchenName;
        this.itemList = itemList;
        this.itemUsageList = itemUsageList;
        this.chatRoomID = chatRoomID;
        this.ownerID = ownerID;
        this.members = members;
        this.pendingResidents = pendingResidents;
        this.bannedResidents = bannedResidents;
        this.notifications = notifications;
        this.cart = cart;
    }

    public String getKitchenID() {
        return kitchenID;
    }

    public String getKitchenName() {
        return kitchenName;
    }

    public ArrayList<Item> getItemList() {
        return itemList;
    }

    public ArrayList<ItemUsage> getItemUsageList() {
        return itemUsageList;
    }

    public String getChatRoomID() {
        return chatRoomID;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public List<String> getMembers() {
        return members;
    }

    public List<String> getPendingResidents() {
        return pendingResidents;
    }

    public List<String> getBannedResidents() {
        return bannedResidents;
    }

    public ArrayList<Notification> getNotifications() {
        return notifications;
    }

    public ArrayList<CartItem> getCart() {
        return cart;
    }

    public Map<String, Object> toJSONObject() {
        Map<String, Object> kitchenJSONObject = new HashMap<>();
        kitchenJSONObject.put(KitchenFirebaseDAO.NAME_FIELD_NAME, getKitchenName());
        kitchenJSONObject.put(KitchenFirebaseDAO.OWNER_FIELD_NAME, getOwnerID());
        kitchenJSONObject.put(KitchenFirebaseDAO.RESIDENTS_FIELD_NAME, getMembers());
        kitchenJSONObject.put(KitchenFirebaseDAO.ITEMS_FIELD_NAME, getItemList());
        kitchenJSONObject.put(KitchenFirebaseDAO.HISTORY_FIELD_NAME, getItemUsageList());
        kitchenJSONObject.put(KitchenFirebaseDAO.CART_FIELD_NAME, getCart());
        kitchenJSONObject.put(KitchenFirebaseDAO.NOTIFICATION_FIELD_NAME, getNotifications());
        kitchenJSONObject.put(KitchenFirebaseDAO.CHAT_FIELD_NAME, getChatRoomID());
        kitchenJSONObject.put(KitchenFirebaseDAO.PENDING_RESIDENTS_FIELD_NAME, getPendingResidents());
        kitchenJSONObject.put(KitchenFirebaseDAO.BANNED_RESIDENTS_FIELD_NAME, getBannedResidents());
        return kitchenJSONObject;
    }
}
