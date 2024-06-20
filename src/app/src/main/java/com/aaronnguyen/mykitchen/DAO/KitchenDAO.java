package com.aaronnguyen.mykitchen.DAO;

import android.content.Context;

import androidx.annotation.NonNull;

import com.aaronnguyen.mykitchen.CustomExceptions.InvalidQuantityException;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartItem;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartSchedule;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Item;
import com.aaronnguyen.mykitchen.model.Items.ItemUsage;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemUseSchedule;
import com.aaronnguyen.mykitchen.model.notification.Notification;

/**
 * This is the KitchenDAO interface.
 * @author u7643339 Isaac Leong
 */
public interface KitchenDAO {

    /**
     * This will create a separate entry in the kitchen dataset
     * @param kitchenID the id of the kitchen
     * @param kitchenName the name of the kitchen
     * @param writeListener The result listener
     */
    void createKitchen(String kitchenID, String kitchenName, String chatID, WriteListener writeListener);

    /**
     * This function will rename the kitchen
     * @param kitchenID the kitchen to be renamed, specified by the id of the kitchen
     * @param newName the new name that is wished to be given to the kitchen
     */
    void renameKitchen(String kitchenID, String newName);

    /**
     * This will establish a synchronised channel between the data and the object.
     * Everytime when there is anything received it is pushed into the kitchenData object in the fetchKitchenListener argument
     */
    void syncKitchen(String kitchenID, FetchListener fetchListener);

    /**
     * This will create a new appointment for a particular item to be used on the given date
     * @param item The item to be appointed
     * @param newSchedule The schedule date
     */
    void addItemUseSchedule(String kitchenID, Item item, ItemUseSchedule newSchedule) throws InvalidQuantityException;

    /**
     * This will remove the appointment
     * @param item The item to remove the appointment from
     * @param schedule The schedule to remove
     */
    void removeItemUseSchedule(String kitchenID, Item item, ItemUseSchedule schedule);

    /**
     * This will remove all the appointments by a specific users
     * @param userID the user
     */
    void removeUserFromSchedule(String kitchenID, String userID);

    /**
     * Add the item to the kitchen
     * @param item The item
     */
    void addItem(String kitchenID, @NonNull Item item) throws InvalidQuantityException;

    /**
     * Remove the item from the kitchen
     * @param item The item
     */
    void removeItem(String kitchenID, @NonNull Item item);

    /**
     * Edit an item from the kitchen.
     *
     * @param kitchenId the id of the kitchen with the item.
     * @param oldItem the item to edit, i.e. to replace.
     * @param newItem the new item.
     */
    void editItem(String kitchenId, @NonNull Item oldItem, @NonNull Item newItem);

    /**
     * This function add a cart item in the cart item list
     * @param kitchenID the kitchen associated that is identified by id
     * @param cartItem The cart item to be added to the shopping cart
     */
    void addCartItem(String kitchenID, CartItem cartItem) throws InvalidQuantityException;

    /**
     * This function removes the cart item in the cart item list
     * @param kitchenID the kitchen associated that is identified by id
     * @param cartItem the cart item
     */
    void removeCartItem(String kitchenID, CartItem cartItem);

    /**
     * This function will add a cart item schedule
     * @param kitchenID The kitchen id
     * @param cartItem The cart item that the schedule should go in
     * @param cartSchedule the cart schedule to be added in
     */
    void addCartItemSchedule(String kitchenID, CartItem cartItem, CartSchedule cartSchedule);

    /**
     * This function will remove a cart item schedule
     * @param kitchenID the kitchen id
     * @param cartItem The cart item that the schedule should be removed from
     * @param schedule the cart schedule to be removed
     */
    void removeCartItemSchedule(String kitchenID, CartItem cartItem, CartSchedule schedule);

    /**
     * Set the quantity of the a cart item
     * @param item The item to be changed
     * @param newQuantity the new quantity of the item
     */
    void setCartItemQuantity(String kitchenID, CartItem item, int newQuantity) throws InvalidQuantityException;

    /**
     * Add the history record to the kitchen
     * @param itemUsage The history record
     */
    void addItemUsage(String kitchenID, ItemUsage itemUsage);

    /**
     * Add the notification regarding important information to the kitchen object
     * @param notification The notification
     */
    void addNotification(String kitchenID, Notification notification);

    /**
     * Remove the notification given the record
     * @param notification The notification to be removed
     */
    void removeNotification(String kitchenID, Notification notification);

    /**
     * Add the user to the active residents list inside the kitchen
     * @param kitchenID the kitchen id to specify the particular kitchen document
     * @param userID the user id to be added
     */
    void addActiveResidents(String kitchenID, String userID);

    /**
     * This function will remove the active residents
     * @param userID the user to be removed
     */
    void removeActiveResidents(String kitchenID, String userID);

    /**
     * This function will ban the given user (it will also remove the access granted to the user)
     * @param userID the user to be banned
     */
    void addBannedResidents(String kitchenID, String userID);

    /**
     * This function will remove a user from the banned list
     * @param userID the user to be removed
     */
    void removeBannedResidents(String kitchenID, String userID);

    /**
     * This function will add the userID to the pending list of the kitchen
     * @param kitchenID The kitchen that the user wants to be added to
     * @param userID the user that raises the request
     */
    void addPendingResidents(String kitchenID, String userID);

    /**
     * This function will remove the user from the pending list as the request is rejected.
     * @param kitchenID the kitchen the user wanted to join
     * @param userID the user to be rejected for joining the kitchen
     */
    void removePendingResidents(String kitchenID, String userID);

    /**
     * This will delete the kitchen
     * @param writeListener The result of the deletion
     */
    void deleteKitchen(String kitchenID, WriteListener writeListener);

    /**
     * This will set the cart item storage location
     * @param kitchenID The kitchen that the cart item belongs to
     * @param item The target cart item for change
     * @param storageLocation The storage location
     * @throws InvalidQuantityException This indicates the item has incorrect quantity
     */
    void setCartItemStorageLocation(String kitchenID, CartItem item, String storageLocation) throws InvalidQuantityException;

    /**
     * This will set the cart item expiry days
     * @param kitchenID The kitchen that the cart item belongs to
     * @param item The target cart item for chagnes
     * @param newExpiryDays The storage location
     * @throws InvalidQuantityException This indicates the item has incorrect quantity
     */
    void setCartItemExpiryDays(String kitchenID, CartItem item, int newExpiryDays) throws InvalidQuantityException;


    /**
     * This will set the cart item expiry days
     * @param kitchenID THe kitchenID
     * @param uid The USer id
     * @param itemName The target cart item for changes
     * @throws InvalidQuantityException This indicates the item has incorrect quantity
     */
    void executeCartSchedule(String kitchenID, String uid, String itemName, String scheduleID, int scheduleDaysReoccurring, int requestCode, Context context);
    void executeItemUseSchedule(String kitchenID, String uid, String itemName, int scheduleQuantity, String scheduleID, String uemail);
}
