package com.aaronnguyen.mykitchen.model.kitchen;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.aaronnguyen.mykitchen.CustomExceptions.InvalidQuantityException;
import com.aaronnguyen.mykitchen.DAO.ChatRoomFirebaseDAO;
import com.aaronnguyen.mykitchen.DAO.FetchListener;
import com.aaronnguyen.mykitchen.DAO.KitchenData;
import com.aaronnguyen.mykitchen.DAO.KitchenFirebaseDAO;
import com.aaronnguyen.mykitchen.DAO.UserDaoFirebase;
import com.aaronnguyen.mykitchen.DAO.WriteListener;
import com.aaronnguyen.mykitchen.ScheduleAlarms.CartScheduleAlarm.CartScheduleAlarm;
import com.aaronnguyen.mykitchen.ScheduleAlarms.ItemUseScheduleAlarm.ItemUseScheduleAlarm;
import com.aaronnguyen.mykitchen.Service.ExpiryCheckService;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartItem;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartSchedule;
import com.aaronnguyen.mykitchen.model.Items.ItemUsage;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Item;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemFactory;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemUseSchedule;
import com.aaronnguyen.mykitchen.model.chat.ChatMessage;
import com.aaronnguyen.mykitchen.model.chat.ChatRoom;
import com.aaronnguyen.mykitchen.model.chat.ChatRoomListener;
import com.aaronnguyen.mykitchen.model.notification.AppExpiryNotification;
import com.aaronnguyen.mykitchen.model.notification.Notification;
import com.aaronnguyen.mykitchen.model.notification.NotificationFactory;
import com.aaronnguyen.mykitchen.model.user.User;
import com.aaronnguyen.mykitchen.ui.main.Overview.KitchenOverviewFragment;
import com.aaronnguyen.mykitchen.ui.main.ShoppingCart.ShoppingCartFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.function.Function;


/**
 * This class provides a single point of access to perform all types of actions that can be performed in a kitchen
 * @author Isaac Leong, Chengbo Yan, u7648367 Ruixian Wu
 */
public class Kitchen implements KitchenSubject, KitchenUserSubject, Serializable {
    private final String kitchenID;
    private String kitchenName;
    public ArrayList<Item> itemList;
    private ArrayList<CartItem> cartItemList;
    private ArrayList<ItemUsage> itemUsageList;
    private String chatRoomID;
    private ChatRoom chatRoom;
    public List<KitchenObserver> observers;
    private String ownerID;
    private List<String> members;
    private List<String> pendingResidents;
    private List<String> bannedResidents;
    private ArrayList<Notification> notifications;
    private boolean shouldFetchUserProfilePictures = false;

    // You should only used this list if you are tolerant with possible delay of information of the users or either you use a listener
    public final HashMap<String, User> users = new HashMap<>();
    private final ArrayList<KitchenUsersObserver> usersObservers = new ArrayList<>();

    private KitchenPropertyListener kitchenPropertyListener;
    private KitchenMemberListener kitchenMemberListener;
    private KitchenItemListener kitchenItemListener;
    private ChatRoomListener chatRoomListener;


    /**
     * Constructs a new Kitchen with a specified ID and name.
     *
     * @param kitchenID   The unique identifier for the kitchen.
     * @param kitchenName The name of the kitchen.
     */
    public Kitchen(String kitchenID, String kitchenName) {
        this.kitchenID = kitchenID;
        this.kitchenName = kitchenName;
        this.itemList = new ArrayList<>();
        this.cartItemList = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.itemUsageList = new ArrayList<>();
        this.pendingResidents = new ArrayList<>();
        this.bannedResidents = new ArrayList<>();
        this.notifications = new ArrayList<>();
    }

    /**
     * This creates a kitchen (which still fetches the database)
     * If you only want to create a kitchen that does not fetch the database, see the constructor that
     * takes in kitchenID and kitchenName
     *
     * @param kitchenID the id of the kitchen
     */
    public Kitchen(String kitchenID) {
        this.kitchenID = kitchenID;
        this.itemList = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.itemUsageList = new ArrayList<>();
        this.pendingResidents = new ArrayList<>();
        this.bannedResidents = new ArrayList<>();
        this.notifications = new ArrayList<>();
        this.cartItemList = new ArrayList<>();
        this.members = new ArrayList<>();
        members.add(ownerID);

        syncKitchenRealTime();
    }

    /**
     * This constructor will cause the kitchen to fetch the database in realtime
     *
     * @param kitchenID               the kitchen ID
     * @param kitchenPropertyListener this will only notify when the kitchen name or the kitchen is deleted
     */
    public Kitchen(String kitchenID, KitchenPropertyListener kitchenPropertyListener) {
        this.kitchenID = kitchenID;
        this.itemList = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.itemUsageList = new ArrayList<>();
        this.pendingResidents = new ArrayList<>();
        this.bannedResidents = new ArrayList<>();
        this.notifications = new ArrayList<>();
        this.cartItemList = new ArrayList<>();
        this.members = new ArrayList<>();
        members.add(ownerID);

        this.kitchenPropertyListener = kitchenPropertyListener;

        syncKitchenRealTime();
    }

    /**
     * This constructor will cause the kitchen to fetch the database in realtime
     *
     * @param kitchenID               the kitchen ID
     * @param kitchenPropertyListener this will notify when the kitchen name or the kitchen is deleted
     * @param kitchenMemberListener   this will notify when the member is changed
     */
    public Kitchen(String kitchenID, KitchenPropertyListener kitchenPropertyListener, KitchenMemberListener kitchenMemberListener) {
        this.kitchenID = kitchenID;
        this.itemList = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.itemUsageList = new ArrayList<>();
        this.pendingResidents = new ArrayList<>();
        this.bannedResidents = new ArrayList<>();
        this.notifications = new ArrayList<>();
        this.cartItemList = new ArrayList<>();
        this.members = new ArrayList<>();
        members.add(ownerID);

        this.kitchenPropertyListener = kitchenPropertyListener;
        this.kitchenMemberListener = kitchenMemberListener;

        syncKitchenRealTime();
    }

    /**
     * This constructor will cause the kitchen to fetch the database in realtime
     * @param kitchenID               the kitchen ID
     * @param kitchenPropertyListener this will notify when the kitchen name or the kitchen is deleted
     * @param kitchenMemberListener   this will notify when the properties of the list of members are changed
     * @param kitchenItemListener     this will notify when the list of items of members are changed
     */
    public Kitchen(String kitchenID, KitchenPropertyListener kitchenPropertyListener, KitchenMemberListener kitchenMemberListener, KitchenItemListener kitchenItemListener, ChatRoomListener chatRoomListener) {
        this.kitchenID = kitchenID;
        this.itemList = new ArrayList<>();
        this.observers = new ArrayList<>();
        this.itemUsageList = new ArrayList<>();
        this.pendingResidents = new ArrayList<>();
        this.bannedResidents = new ArrayList<>();
        this.notifications = new ArrayList<>();
        this.cartItemList = new ArrayList<>();
        this.members = new ArrayList<>();
        members.add(ownerID);

        this.kitchenPropertyListener = kitchenPropertyListener;
        this.kitchenMemberListener = kitchenMemberListener;
        this.kitchenItemListener = kitchenItemListener;
        this.chatRoomListener = chatRoomListener;

        syncKitchenRealTime();
    }

    /**
     * This function will synchronise the kitchen with the kitchen document on firebase
     */
    private void syncKitchenRealTime() {
        KitchenFirebaseDAO.getInstance().syncKitchen(this.kitchenID, new FetchListener() {
            @Override
            public <T> void onFetchSuccess(T data) {
                if (!(data instanceof KitchenData kitchenData))
                    return;

                if (kitchenData.getKitchenName() != null) {
                    String pulledKitchenName = kitchenData.getKitchenName();
                    // Check if it is different to the local name, and if so call the update
                    if (kitchenName == null || !kitchenName.equals(pulledKitchenName)) {
                        kitchenName = pulledKitchenName;
                        if (kitchenPropertyListener != null) {
                            kitchenPropertyListener.onKitchenNameUpdateListener(kitchenName);
                        }
                    }
                }

                if (kitchenData.getChatRoomID() != null) {
                    chatRoomID = kitchenData.getChatRoomID();
                    if (chatRoom == null && chatRoomListener != null) {
                        chatRoom = new ChatRoom(chatRoomID, chatRoomListener);
                        chatRoomListener.onChatRoomEstablishedSuccess();
                    }
                }

                if (kitchenData.getOwnerID() != null) {
                    if (ownerID == null || !ownerID.equals(kitchenData.getOwnerID())) {
                        ownerID = kitchenData.getOwnerID();
                        if (kitchenMemberListener != null) {
                            kitchenMemberListener.onOwnerIDUpdateListener(ownerID);
                        }
                    }
                }

                if (kitchenData.getMembers() != null) {
                    if (members == null || !members.equals(kitchenData.getMembers())) {
                        members = kitchenData.getMembers();
                        fetchUserInfo();
                        if (kitchenMemberListener != null) {
                            kitchenMemberListener.onKitchenActiveMembersUpdateListener(members);
                        }
                    }
                }

                if (kitchenData.getPendingResidents() != null) {
                    if (pendingResidents == null || !pendingResidents.equals(kitchenData.getPendingResidents())) {
                        pendingResidents = kitchenData.getPendingResidents();
                        if (kitchenMemberListener != null) {
                            kitchenMemberListener.onKitchenPendingMembersUpdateListener(pendingResidents);
                        }
                    }
                }

                if (kitchenData.getBannedResidents() != null) {
                    if (bannedResidents == null || !bannedResidents.equals(kitchenData.getBannedResidents())) {
                        bannedResidents = kitchenData.getBannedResidents();
                        if (kitchenMemberListener != null) {
                            kitchenMemberListener.onKitchenBannedMembersUpdateListener(bannedResidents);
                        }
                    }
                }

                if (kitchenData.getItemList() != null) {
                    if (itemList == null || !itemList.equals(kitchenData.getItemList())) {
                        itemList = kitchenData.getItemList();
                        if (kitchenItemListener != null) {
                            kitchenItemListener.onItemListUpdateListener(itemList);
                        }
                    }
                }

                if (kitchenData.getItemUsageList() != null) {
                    if (itemUsageList == null || !itemUsageList.equals(kitchenData.getItemUsageList())) {
                        itemUsageList = kitchenData.getItemUsageList();
                        if (kitchenItemListener != null) {
                            kitchenItemListener.onItemUsageListUpdateListener(itemUsageList);
                        }
                    }
                }

                if (kitchenData.getCart() != null) {

                    if (cartItemList == null || !cartItemList.equals(kitchenData.getCart())) {
                        cartItemList = kitchenData.getCart();

                        for (CartItem c : cartItemList) {
                            Log.i("DEBUG", c.toString());
                        }


                        if (kitchenItemListener != null) {
                            kitchenItemListener.onCartItemListUpdateListener(cartItemList);
                        }
                    }
                }

                if (kitchenData.getNotifications() != null) {
                    if (notifications == null || !notifications.equals(kitchenData.getNotifications())) {
                        notifications = kitchenData.getNotifications();
                        if (kitchenItemListener != null) {
                            kitchenItemListener.onNotificationListener(notifications);
                        }
                    }
                }
            }

            @Override
            public void onFetchFailure(Exception exception) {
                Log.e("DEBUG", exception.toString());
            }
        });
    }

    /**
     * This function will change the name of the current kitchen
     * @param newName the new name
     */
    public void changeKitchenName(String newName) {
        KitchenFirebaseDAO.getInstance().renameKitchen(this.kitchenID, newName);
    }

    /**
     * This function will delete the kitchen (the current kitchen object on firebase)
     */
    public void deleteKitchen() {
        // Store this data before deleting the whole kitchen
        ArrayList<String> userIDs = new ArrayList<>(this.members);
        KitchenFirebaseDAO.getInstance().deleteKitchen(this.kitchenID, new WriteListener() {
            @Override
            public <T> void onWriteSuccess(T data) {
                for (String id : userIDs) {
                    UserDaoFirebase.getInstance().removeKitchen(id, kitchenID);
                }
                ChatRoomFirebaseDAO.getInstance().deleteChatRoom(chatRoomID);

                if (kitchenPropertyListener != null) {
                    kitchenPropertyListener.onKitchenDeleteListener(kitchenID);
                }
            }

            @Override
            public void onWriteFailure(Exception exception) {
                if (kitchenPropertyListener != null) {
                    kitchenPropertyListener.onKitchenDeleteFailureListener(exception);
                }
            }
        });
    }

    public void deleteCartItem(CartItem item) {
        KitchenFirebaseDAO.getInstance().removeCartItem(this.kitchenID, item);
    }
    public void editTextNotification(Item oldItem,Item newItem){

        itemList.remove(oldItem);
        Log.i("removeItem",oldItem.toString());
        itemList.add(newItem);
        Log.i("removeItem",newItem.toString());

        if (kitchenItemListener != null) {
            kitchenItemListener.onItemListUpdateListener(itemList);
        }
        for (Item item : itemList) {
            itemCloseTOExpiry(item);
            itemOutOfExpiry(item);
        }
    }

    /**
     * check all the items, and update the notification, including sending notification to user's phone
     */
    public void allKindsNotification() {
        observers.clear();
        observers.addAll(users.values());
        Log.i("DEBUGObservers",observers.toString());
        int flag =0;
        int flag2 = 0 ;
        for (Item item : itemList) {
            flag += itemCloseTOExpiry(item);
            flag2 += itemOutOfExpiry(item);
        }
        if(flag>0 && flag2>0){
            this.notifyCloseToExpireAndExpired();
        }
        else {
            if (flag > 0) {
                Log.i("Notification", "notify user Item Close");
                this.notifyCloseToExpire();
            }
            if (flag2 > 0) {
                Log.i("Notification", "notify user Item Close");
                this.notifyExpired();
            }
        }
    }


    /**
     * Checks if the given item is close to expiring and adds a notification if it is.
     * @param item The item to check for expiry.
     * @return 1 if the item is close to expiring, otherwise 0.
     */
    public int itemCloseTOExpiry(Item item) {
        if (item.isCloseToExpiry()) {
            if (item.DaysBeforeExpiry() > 0) {
                String days = " day";
                int beforeExpiryDay = item.DaysBeforeExpiry();
                if(beforeExpiryDay == 1) {
                    days = "1" + days;
                }
                else {
                    days =  beforeExpiryDay + " days";
                }
                AppExpiryNotification appExpiryNotification = NotificationFactory.createNotification(item.getName() + " " +
                        "will expire in " + days, 2);
                KitchenFirebaseDAO.getInstance().addNotification(this.kitchenID, appExpiryNotification);
            } else if (item.DaysBeforeExpiry() == 0) {
                AppExpiryNotification appExpiryNotification = NotificationFactory.createNotification
                        (item.getName() + " will expire today", 2);
                KitchenFirebaseDAO.getInstance().addNotification(this.kitchenID, appExpiryNotification);
            }
            return  1;
        }
        return  0;
    }

    /**
     * Checks if the given item has expired and adds a notification if it has.
     * @param item The item to check for expiry.
     * @return 1 if the item has expired, otherwise 0.
     */
    public int itemOutOfExpiry(Item item) {
        if (item.OutOfExpiry()) {
            AppExpiryNotification appExpiryNotification =
                    NotificationFactory.createNotification(item.getName() + " has expired ", 3);
            KitchenFirebaseDAO.getInstance().addNotification(this.kitchenID, appExpiryNotification);
            return 1;
        }
        return  0;
    }

    /**
     * Notifies observers that an item is close to expiring.
     */
    @Override
    public void notifyCloseToExpire() {
        for (KitchenObserver observer : observers) {
            observer.notifyCloseToExpire(kitchenName);
        }
    }

    /**
     * Notifies observers that an item has expired.
     */
    @Override
    public void notifyExpired() {
        for (KitchenObserver observer : observers) {
            observer.onExpired(kitchenName);
        }
    }

    /**
     * Notifies observers that items are both close to expiring and have expired.
     */
    @Override
    public void notifyCloseToExpireAndExpired(){
        for (KitchenObserver observer : observers) {
            observer.notifyCloseToExpireAndExpired(kitchenName);
        }
    }

    @Override
    public void notifyAddingItem(String message) {
        observers.clear();
        observers.addAll(users.values());
        for(KitchenObserver observer:observers){
            observer.notifyAddingItem(message,kitchenName);
        }
    }

    /**
     * This add a new item in the fridge
     *
     * @param item The item to be added. This should be generated by `ItemFactory`
     */
    public void addItem(Item item) throws InvalidQuantityException {
        itemList.add(item);
        KitchenFirebaseDAO.getInstance().addItem(this.kitchenID, item);
    }


    /**
     * This add a new itemUsage in the History
     *
     * @param itemUsage The itemUsage to be added.
     */
    public void addItemUsage(ItemUsage itemUsage) {
        Log.i("DEBUG", itemUsage.toString());
        Log.i("userItem", kitchenID);
        itemUsageList.add(itemUsage);
        KitchenFirebaseDAO.getInstance().addItemUsage(this.kitchenID, itemUsage);
    }

    /**
     * This will use the model. However, depending on the quantity,
     *
     * @param item              The item that will be reduced / used
     * @param demandingQuantity The quantity that the user is trying to use.
     */
    public boolean useItem(Item item, int demandingQuantity, User currentUser) {
        Log.i("SIMULATION", item.toString());
        itemList.forEach(i -> Log.i("DEBUG", i.toString()));

        if (!itemList.contains(item)) {
            Log.i("SIMULATION", "Unfounded item as " + item.toJSONObject());
            itemList.forEach(itemInList -> Log.i("SIMULATION", itemInList.toJSONObject().toString()));
            return false;
        }

        Item itemInList = itemList.get(itemList.indexOf(item));

        if (itemInList == null) {
            Log.i("SIMULATION", "It is actually a null item");
            return false;
        }

        if (itemInList.getQuantity() < demandingQuantity) {
            Log.i("SIMULATION", "You are probably demanding too much");
            return false;
        } else if (itemInList.getQuantity() == demandingQuantity) {
            Date currentDate = new Date();
            ItemUsage itemUsage = new ItemUsage(itemInList.getName(), currentUser, currentDate, demandingQuantity);

            addItemUsage(itemUsage);
            removeItem(item);
        } else if (itemInList.getQuantity() > demandingQuantity) {
            Date currentDate = new Date();
            ItemUsage itemUsage = new ItemUsage(itemInList.getName(), currentUser, currentDate, demandingQuantity);

            addItemUsage(itemUsage);
            return reduceItem(item, demandingQuantity);
        }

        return true;
    }

    /**
     * This remove an item in the fridge
     *
     * @param item The item to be added. This should be generated by `ItemFactory`
     */
    public void removeItem(@NonNull Item item) {
        Log.i("DEBUG", "Remove item with name: " + item.toJSONObject());
        KitchenFirebaseDAO.getInstance().removeItem(this.kitchenID, item);
    }

    /**
     * This function will set the quantity to the new quantity argument
     * @param item the item to be updated
     * @param newQuantity the new quantity of the item
     * @return true iff the function has been called correctly (with positive amount of new quantity)
     */
    public boolean reduceItem(@NonNull Item item, int newQuantity) {
        Log.i("DEBUG", "Reduce item with name: " + item.getName());

        if (!itemList.contains(item))
            Log.e("DEBUG", "Cannot find expected item");

        KitchenFirebaseDAO.getInstance().editItem(kitchenID, item, ItemFactory.createItem(
                item.getName(),
                item.getTypeString(),
                item.getExpiryDate(),
                item.getBoughtDate(),
                item.getAssociatedUser(),
                item.getQuantity() - newQuantity,
                item.getStorageLocation(),
                item.getSchedule())
        );
        return true;
    }

    /**
     * Adds an itemUseSchedule to FireStore and sets the current device to write to FireStore in the future as specified in the schedule
     * @param overviewFragment
     * @param kitchen that holds the item we want to add the schedule to
     * @param item that the schedule is being added to
     * @param newSchedule that is to be added
     * @author u7648367 Ruixian Wu
     */
    public boolean scheduleItemUse(KitchenOverviewFragment overviewFragment, Kitchen kitchen, Item item, ItemUseSchedule newSchedule) {
        new ItemUseScheduleAlarm(overviewFragment, kitchen, item, newSchedule);
        try{
            KitchenFirebaseDAO.getInstance().addItemUseSchedule(this.kitchenID, item, newSchedule);
        } catch (InvalidQuantityException e) {
            return false;
        }
        return true;
    }

    /**
     * Removes the schedule of an item from FireStore and notifies the user who set the schedule to cancel the future write to FireStore as outline by the schedule
     *
     * @param item     the item from whcih we want to remove the schedule
     * @param schedule The use schedule
     */
    public void removeScheduleItemUse(Item item, ItemUseSchedule schedule) {
        KitchenFirebaseDAO.getInstance().removeItemUseSchedule(this.kitchenID, item, schedule);
        removeScheduleNotify(schedule.getScheduleID(), true);
    }


    public void deleteNotification(Notification notification) {
        KitchenFirebaseDAO.getInstance().removeNotification(this.kitchenID, notification);
    }

    public void addCartItem(CartItem cartItem) throws InvalidQuantityException {
        KitchenFirebaseDAO.getInstance().addCartItem(this.kitchenID, cartItem);
    }

    public void sendMessageOnline(ChatMessage chatMessage) {
        chatRoom.sendMessage(chatMessage);
    }

    @Override
    public void notifyChatToOthers(ChatMessage chatMessage, User currentUser) {
        syncKitchenRealTime();
        Log.i("notifyObserver",this.getKitchenName());
        observers.clear();
        observers.addAll(users.values());
        for (KitchenObserver observer : observers) {
            Log.i("notifyObserver",observer.getUid());
            if(observer.getUid().equals(currentUser.getUid())) continue;
            observer.chatNotify(chatMessage.getMessage(),currentUser.getUserName());
        }
    }

    /**
     * Changes the quantity of the given cart item on FireStore
     * @param item whose quantity is to be changed
     * @param newQuantity that will replace the item's current quantity
     * @throws InvalidQuantityException
     */
    public void changeCartItemQuantity(CartItem item, int newQuantity) throws InvalidQuantityException {
        KitchenFirebaseDAO.getInstance().setCartItemQuantity(this.kitchenID, item, newQuantity);
    }

    /**
     * Changes the storage location of the given cart item on FireStore
     * @param item whose storage location is to be changed
     * @param storageLocation The new storage location that we want to change the item's current storage location to
     * @throws InvalidQuantityException
     */
    public void changeCartItemStorageLocation(CartItem item, String storageLocation) throws InvalidQuantityException {
        KitchenFirebaseDAO.getInstance().setCartItemStorageLocation(this.kitchenID, item, storageLocation);
    }

    /**
     * Changes the expiry days of the given cart item on FireStore
     * @param item whose expiry days is to be changed
     * @param newExpiryDays that will replace the item's current expiry days
     * @throws InvalidQuantityException
     */
    public void changeCartItemExpiry(CartItem item, int newExpiryDays) throws InvalidQuantityException {
        KitchenFirebaseDAO.getInstance().setCartItemExpiryDays(this.kitchenID, item, newExpiryDays);
    }

    /**
     * This function will remove a resident from the list of active residents (which have access to the kitchen)
     * @param userID The user to be removed
     * @return true iff removal is successful (i.e. the user to be removed is not the owner of the kitchen)
     */
    public boolean removeActiveResidents(String userID) {
        if(!userID.equals(ownerID)) {
            KitchenFirebaseDAO.getInstance().removeActiveResidents(this.kitchenID, userID);
            UserDaoFirebase.getInstance().removeKitchen(userID, this.kitchenID);
            KitchenFirebaseDAO.getInstance().removeUserFromSchedule(this.kitchenID, userID);
            return true;
        }
        return false;
    }

    /**
     * This function ban an active residents (remove from the authorisation and put the user into the banned list)
     * @param userID The user to be banned
     * @return true iff ban is successful (i.e. the user to be removed is not the owner of the kitchen)
     */
    public boolean banActiveResidents(String userID) {
        if(!userID.equals(ownerID)) {
            KitchenFirebaseDAO.getInstance().removeActiveResidents(this.kitchenID, userID);
            KitchenFirebaseDAO.getInstance().addBannedResidents(this.kitchenID, userID);
            KitchenFirebaseDAO.getInstance().removeUserFromSchedule(this.kitchenID, userID);
            return true;
        }
        return false;
    }

    /**
     * This function will kindly remove a user from the banned list, <STRONG>BUT DO NOT AUTHORISE THE USER</STRONG>
     * @param userID the user to be removed from the banned list
     */
    public void removeBannedResidents(String userID) {
        Log.i("DEBUG", "Removing "  + userID + " from banned list");
        KitchenFirebaseDAO.getInstance().removeBannedResidents(this.kitchenID, userID);
    }

    /**
     * This function will approve a user form the pending list.
     * @param currentUserID The user that is granting access to the kitchen
     * @param userID The user that is being authorised
     * @return true iff the currentUser has the power to grant access
     */
    public boolean approveResidentsRequest(String currentUserID, String userID) {
        if(currentUserID.equals(this.ownerID)) {
            UserDaoFirebase.getInstance().addKitchen(userID, kitchenID, new WriteListener() {
                @Override
                public <T> void onWriteSuccess(T data) {
                    KitchenFirebaseDAO.getInstance().removePendingResidents(kitchenID, userID);
                    KitchenFirebaseDAO.getInstance().addActiveResidents(kitchenID, userID);
                }

                @Override
                public void onWriteFailure(Exception exception) {
                    Log.e("DEBUG", exception.toString());
                }
            });
            return true;
        }
        return false;
    }

    /**
     * This will reject the user request to join the kitchen (i.e. remove the user from the pending list without putting the user into the active memeber list)
     * @param currentUserID The user that is requesting to reject the pending user
     * @param userID The user to be rejected
     * @return True iff this action is possible
     */
    public boolean rejectResidentRequest(String currentUserID, String userID) {
        if(currentUserID.equals(this.ownerID)) {
            KitchenFirebaseDAO.getInstance().removePendingResidents(this.kitchenID, userID);
            return true;
        }
        return false;
    }

    /**
     * Add the given cart schedule to the item specified on FireStore
     * Start alarm on current device to write to FireStore (as outlined in the schedule) in the future
     * @param fragment
     * @param item that we want to add a cart schedule to
     * @param schedule that is to be added
     */
    public void addCartSchedule(ShoppingCartFragment fragment, CartItem item, CartSchedule schedule) {
        CartScheduleAlarm cartScheduleAlarm = new CartScheduleAlarm(fragment, this, item, schedule);
        KitchenFirebaseDAO.getInstance().addCartItemSchedule(this.kitchenID, item, schedule);
    }

    /**
     * Removes cart schedule from FireStore
     * Notifies user that set the cart schedule to cancel alarm that would write to FireStore in the future for the schedule
     * @param kitchenID of the kitchen that contains the schedule we want to remove
     * @param item that contains the schedule we want to remove
     * @param schedule that is to be removed
     */
    public void removeCartSchedule(String kitchenID, CartItem item, CartSchedule schedule) {
        KitchenFirebaseDAO.getInstance().removeCartItemSchedule(kitchenID, item, schedule);
        removeScheduleNotify(schedule.getScheduleID(), false);
    }

    /**
     * This function will remove the set of cart items to the kitchen simulating the idea that the
     * user has finished shopping and a part of of the things inside the shopping cart has been bought.
     *
     * @param cartItems The set of cart items
     * @param user The user that is putting these things in cart to the kitchen
     */
    public void buyCartItems(Set<CartItem> cartItems, User user) throws InvalidQuantityException {
        for (CartItem cartItem : cartItems) {
            KitchenFirebaseDAO.getInstance().addItem(kitchenID, cartItem.buyCartItem(user));
            if (cartItem.noSchedules()) {
                KitchenFirebaseDAO.getInstance().removeCartItem(kitchenID, cartItem);
            } else {
                KitchenFirebaseDAO.getInstance().setCartItemQuantity(kitchenID, cartItem, 0);
            }
        }
    }

    @Override
    public void attach(KitchenUsersObserver observer) {
        usersObservers.add(observer);
        System.out.println(usersObservers.size());
    }

    @Override
    public void detach(KitchenUsersObserver observer) {
        usersObservers.remove(observer);
    }

    /**
     * This function notify all the observers that need all the users' info of this kitchen
     */
    @Override
    public void notifyUserChanges() {
        // First remove any null objects in the array
        usersObservers.removeAll(Collections.singleton(null));

        usersObservers.forEach(observer -> {
            System.out.println("NOTIFIED");
            observer.notify(new HashMap<>(users));
        });
    }

    @Override
    public void fetchUserInfo(Function<HashMap<String, User>, Void> callBack) {
        callBack.apply(new HashMap<>(users));
    }

    /**
     * This function will perform a one-time fetch of the user info whenever the list of member has changed.
     * Therefore, the information about the users may not be very up to date. But this should be fine
     * since user info should not be updated often
     */
    private void fetchUserInfo() {
        for (int i = 0; i < members.size(); i++) {
            Log.i("DEBUGObservers", String.valueOf(i));
            UserDaoFirebase.getInstance().fetchUser(members.get(i), new FetchListener() {
                @Override
                public <T> void onFetchSuccess(T data) {
                    // Only update the value if the value is correct
                    if(data instanceof User userData) {
                        if (shouldFetchUserProfilePictures) {
                            fetchUserProfilePicture(userData);
                        } else {
                            users.put(userData.getUid(), userData);
                            notifyUserChanges();
                        }
                    }
                }

                @Override
                public void onFetchFailure(Exception exception) {
                    // This should not do anything so that this map will sustain the previously stored info
                    System.out.println(exception.toString());
                }
            });
        }
    }

    private void fetchUserProfilePicture(User user) {
        System.out.println("FETCHING PFPS");
        String uid = user.getUid();
        UserDaoFirebase.getInstance().fetchProfilePicture(uid, new FetchListener() {
            @Override
            public <T> void onFetchSuccess(T data) {
                if (data instanceof Uri uri) {
                    user.setProfilePicture(uri);
                    users.put(uid, user);

                    notifyUserChanges();
                }
            }

            @Override
            public void onFetchFailure(Exception exception) {
                System.out.println(exception.toString());
                // This should not do anything so that this map will not contain the value
            }
        });

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
    public void setItemList(ArrayList<Item> itemList) {
        this.itemList = itemList;
    }

    public void setShouldFetchUserProfilePictures(boolean shouldFetchUserProfilePictures) {
        this.shouldFetchUserProfilePictures = shouldFetchUserProfilePictures;

        if (shouldFetchUserProfilePictures) {
            fetchUserInfo();
        }
    }

    public ChatRoom getChatRoom() {
        return chatRoom;
    }
    public String getOwnerID() {
        return ownerID;
    }
    public List<String> getPendingResidents() {
        return pendingResidents;
    }
    public List<String> getBannedResidents() {
        return bannedResidents;
    }
    public ArrayList<ItemUsage> getItemUsageList() {
        return itemUsageList;
    }
    public ArrayList<Notification> getNotifications() {
        if(notifications == null){
            for (Item item : itemList) {
                if (item.OutOfExpiry()) {
                    AppExpiryNotification appExpiryNotification =
                            NotificationFactory.createNotification(item.getName() + " has expired ", 3);
                }
                if (item.DaysBeforeExpiry() > 0) {
                    String plural = item.DaysBeforeExpiry() == 1 ? "" : "s";
                    AppExpiryNotification appExpiryNotification = NotificationFactory.createNotification(item.getName() + " will expire in " +
                            item.DaysBeforeExpiry() + "   day" + plural, 2);
                    KitchenFirebaseDAO.getInstance().addNotification(this.kitchenID, appExpiryNotification);
                } else if (item.DaysBeforeExpiry() == 0) {
                    AppExpiryNotification appExpiryNotification = NotificationFactory.createNotification
                            (item.getName() + " will expire today", 2);
                    KitchenFirebaseDAO.getInstance().addNotification(this.kitchenID, appExpiryNotification);
                }
            }
        }
        return notifications;
    }
    public void setNotifications(ArrayList<Notification> notifications) {
        this.notifications = notifications;
    }
    public ArrayList<CartItem> getCartItemList() {
        return cartItemList;
    }
    public void addNotification(Notification notification) {
        KitchenFirebaseDAO.getInstance().addNotification(this.kitchenID, notification);
        notifications.add(notification);
        this.notifyAddingItem(notification.getText());
    }


    /**
     * Get the FCM of the user that set a schedule that has been deleted, and notify that user
     * @param scheduleID of the schedule that is to be removed
     * @param itemUse True is the schedule is an ItemUseSchedule, false if it is a CartSchedule
     * @author u7648367 Ruixian Wu (adapted from u7515796 Chengbo Yan)
     */
    public static void removeScheduleNotify(String scheduleID, boolean itemUse) {
        Log.i("DEBUG", "removeScheduleNotify");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = scheduleID.split("/")[0];
        String requestCode = scheduleID.split("/")[1];
        DocumentReference userDocRef = db.collection("users").document(userId);
        userDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String token = documentSnapshot.getString("fcmToken");
                if (token != null) {
                    Log.i("DEBUG", token);
                    removeScheduleNotification(token, itemUse, requestCode);
                } else {
                    Log.d("FCM", "No token for user");
                }
            }
        }).addOnFailureListener(e -> Log.e("FCM", "Error getting user document", e));

    }

    /**
     * Notify the user that set a schedule that that schedule has been deleted
     * @param token FCM token of the user that set the schedule
     * @param itemUse True is the schedule is an ItemUseSchedule, false if it is a CartSchedule
     * @param requestCode of the schedule that has been deleted
     * @author u7648367 Ruixian Wu (adapted from u7515796 Chengbo Yan)
     */
    private static void removeScheduleNotification(String token, boolean itemUse, String requestCode) {
        Log.i("DEBUG", "removeScheduleNotification");
        String title = "Schedule";
        String scheduleType = itemUse ? "true" : "false";

        String finalTitle = title;
        String finalMessage = requestCode + "/" + scheduleType;
        String finalChannelId = "Schedule";
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                URL url = new URL("https://fcm.googleapis.com/fcm/send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "key=" + "AAAAZOFPPEM:APA91bGPp2PHDu5TZdgdTEMgR8_-WXym2u0XDPIpOcCg1UaWCr1fQTWIr_h38rx3fLppY5dfH4e3bypniqeNdpXyizFP9AwnwgBkeSD3IA2VsYNXEbS9hT8vexNgpwsjGOES-SFFfEgw");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                JSONObject notificationJSON = new JSONObject();
                notificationJSON.put("title", finalTitle);
                notificationJSON.put("body", finalMessage);

                JSONObject messageJSON = new JSONObject();
                messageJSON.put("notification", notificationJSON);
                messageJSON.put("to", token);
                messageJSON.put("android", new JSONObject().put("notification", new JSONObject().put("channel_id", finalChannelId)));
                JSONObject data = new JSONObject();
                data.put("type", "Schedule");
                messageJSON.put("data", data);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(messageJSON.toString().getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = conn.getResponseCode();
                Log.i("FCM", "Response Code : " + responseCode);
                Log.i("FCM", "Response Message : " + conn.getResponseMessage());

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        Log.i("FCM Response", response.toString());
                    }
                } else {
                    Log.e("FCM", "Failed to send notification");
                }
            } catch (Exception e) {
                Log.e("FCM", "Error sending notification", e);
            }
        });
    }
}