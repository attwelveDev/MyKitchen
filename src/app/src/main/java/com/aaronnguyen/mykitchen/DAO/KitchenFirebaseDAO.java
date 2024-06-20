package com.aaronnguyen.mykitchen.DAO;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aaronnguyen.mykitchen.CustomExceptions.FetchKitchenException;
import com.aaronnguyen.mykitchen.CustomExceptions.InvalidQuantityException;
import com.aaronnguyen.mykitchen.ScheduleAlarms.CartScheduleAlarm.CartScheduleAlarm;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartItem;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartSchedule;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Item;
import com.aaronnguyen.mykitchen.model.Items.ItemUsage;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemUseSchedule;
import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.model.notification.Notification;
import com.aaronnguyen.mykitchen.ui.main.Overview.OverviewViewModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author u7643339 Isaac Leong, u7517596 ChengBo Yan, u7648367 Ruixian Wu
 */
public class KitchenFirebaseDAO implements KitchenDAO {
    private static KitchenFirebaseDAO instance;
    public static KitchenFirebaseDAO getInstance() {
        if(instance == null)
            instance = new KitchenFirebaseDAO();
        return instance;
    }

    public static final String KITCHEN_COLLECTION_NAME = "kitchens";
    public static final String NAME_FIELD_NAME = "name";
    public static final String OWNER_FIELD_NAME = "owner";
    public static final String RESIDENTS_FIELD_NAME = "members";
    public static final String PENDING_RESIDENTS_FIELD_NAME = "pending users";
    public static final String BANNED_RESIDENTS_FIELD_NAME = "banned users";
    public static final String ITEMS_FIELD_NAME = "items";
    public static final String CHAT_FIELD_NAME = "chat room ID";
    public static final String HISTORY_FIELD_NAME = "history";
    public static final String CART_FIELD_NAME = "cart";
    public static final String NOTIFICATION_FIELD_NAME = "notification";

    public final FirebaseFirestore db;

    /**
     * A data access object that can read, write and sync with a specific kitchen document (specified by the id)
     */
    public KitchenFirebaseDAO() {
        db = FirebaseFirestore.getInstance();
    }

    /**
     * This function will add a kitchen in the dataset
     */
    @Override
    public void createKitchen(String kitchenID, String kitchenName, String chatID, WriteListener writeListener) {
        if(kitchenName == null || kitchenID == null)
            return;

        Map<String, Object> newKitchen = new HashMap<>();
        newKitchen.put(NAME_FIELD_NAME, kitchenName);
        newKitchen.put(OWNER_FIELD_NAME, kitchenID);
        newKitchen.put(RESIDENTS_FIELD_NAME, Collections.singletonList(kitchenID));
        newKitchen.put(ITEMS_FIELD_NAME, Collections.EMPTY_LIST);
        newKitchen.put(HISTORY_FIELD_NAME, Collections.EMPTY_LIST);
        newKitchen.put(CART_FIELD_NAME, Collections.EMPTY_LIST);
        newKitchen.put(NOTIFICATION_FIELD_NAME, Collections.EMPTY_LIST);
        newKitchen.put(CHAT_FIELD_NAME, chatID);
        newKitchen.put(PENDING_RESIDENTS_FIELD_NAME, Collections.EMPTY_LIST);
        newKitchen.put(BANNED_RESIDENTS_FIELD_NAME, Collections.EMPTY_LIST);

        db.collection(KITCHEN_COLLECTION_NAME)
                .add(newKitchen)
                .addOnSuccessListener(documentReference -> writeListener.onWriteSuccess(documentReference.getId()))
                .addOnFailureListener(writeListener::onWriteFailure);
    }

    /**
     * This function rename the kitchen
     * @param kitchenID the kitchen to be renamed, specified by the id of the kitchen
     * @param newName the new name that is wished to be given to the kitchen
     */
    @Override
    public void renameKitchen(String kitchenID, String newName) {
        db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID).update(NAME_FIELD_NAME, newName);
    }

    /**
     * This function wil synchronise the kitchen
     * @param kitchenID This is the kitchen document that we wish to synchronise with
     * @param fetchListener This is the fetch listener / callback every time fetch has success
     */
    @Override
    public void syncKitchen(String kitchenID, FetchListener fetchListener) {
        db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID).addSnapshotListener(((value, error) -> {
            if (value == null) {
                fetchListener.onFetchFailure(new FetchKitchenException());
                return;
            }

            if (!value.exists()) {
                fetchListener.onFetchFailure(new IllegalArgumentException("Kitchen with id " + kitchenID + " does not exist"));
                return;
            }

            if (error != null) {
                fetchListener.onFetchFailure(error);
            }

            String chatRoomID = checkFieldInDocument(value, CHAT_FIELD_NAME) ? (String) value.get(CHAT_FIELD_NAME) : null;
            String kitchenName = checkFieldInDocument(value, NAME_FIELD_NAME) ? (String) value.get(NAME_FIELD_NAME) : null;
            String ownerID = checkFieldInDocument(value, OWNER_FIELD_NAME) ? (String) value.get(OWNER_FIELD_NAME) : null;
            List<String> activeResidents = checkFieldInDocument(value, RESIDENTS_FIELD_NAME) ? (List<String>) value.get(RESIDENTS_FIELD_NAME) : null;

            List<String> pendingResidents = checkFieldInDocument(value, PENDING_RESIDENTS_FIELD_NAME) ?
                    (List<String>) value.get(PENDING_RESIDENTS_FIELD_NAME) : null;

            List<String> bannedResidents = checkFieldInDocument(value, BANNED_RESIDENTS_FIELD_NAME) ?
                    (List<String>) value.get(BANNED_RESIDENTS_FIELD_NAME) : null;

            ArrayList<ItemUsage> history = checkFieldInDocument(value, HISTORY_FIELD_NAME) ?
                    JSONObjectParser.parseHistoryList((List<Map<String, Object>>) value.get(HISTORY_FIELD_NAME)) : null;

            ArrayList<Item> itemList = checkFieldInDocument(value, ITEMS_FIELD_NAME) ?
                    JSONObjectParser.parseItemList((List<Map<String, Object>>) value.get(ITEMS_FIELD_NAME)) : null;

            ArrayList<CartItem> cartItemList = checkFieldInDocument(value, CART_FIELD_NAME) ?
                    JSONObjectParser.parseCartItemList((List<Map<String, Object>>) value.get(CART_FIELD_NAME)) : null;

            ArrayList<Notification> notifications = checkFieldInDocument(value, NOTIFICATION_FIELD_NAME) ?
                    JSONObjectParser.parseNotification((List<Map<String, Object>>) value.get(NOTIFICATION_FIELD_NAME)) : null;

            KitchenData kitchenData = new KitchenData(
                    kitchenID,
                    kitchenName,
                    itemList,
                    history,
                    chatRoomID,
                    ownerID,
                    activeResidents,
                    pendingResidents,
                    bannedResidents,
                    notifications,
                    cartItemList
            );

            fetchListener.onFetchSuccess(kitchenData);
        }));
    }

    /**
     * @param item        The item to be appointed
     * @param newSchedule The schedule date
     * @author u7648367 Ruixian Wu
     */
    @Override
    public void addItemUseSchedule(String kitchenID, Item item, ItemUseSchedule newSchedule) throws InvalidQuantityException {
        if(newSchedule.getScheduledQuantity() <= 0) {
            throw new InvalidQuantityException(InvalidQuantityException.Code.NON_POSITIVE);
        }

        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentReference kitchenDocRef = db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID);
                DocumentSnapshot documentSnapshot = transaction.get(kitchenDocRef);

                List<Map<String, Object>> itemsFromFireBase = (List<Map<String, Object>>) documentSnapshot.get(ITEMS_FIELD_NAME);
                for (int i = 0; i < itemsFromFireBase.size(); i++) {
                    var jsonObject = itemsFromFireBase.get(i);
                    if(jsonObject.containsKey(Item.NAME_FIELD_NAME)
                            && jsonObject.containsKey(Item.TYPE_FIELD_NAME)
                            && jsonObject.containsKey(Item.EXPIRY_DATE_FIELD_NAME)
                            && jsonObject.containsKey(Item.BOUGHT_DATE_FIELD_NAME)
                            && jsonObject.containsKey(Item.QUANTITY_FIELD_NAME)
                            && jsonObject.containsKey(Item.STORAGE_LOCATION_FIELD_NAME)
                            && jsonObject.containsKey(Item.ASSOCIATED_USER_ID_FIELD_NAME)
                            && jsonObject.containsKey(Item.ASSOCIATED_USER_EMAIL_FIELD_NAME)
                            && jsonObject.containsKey(Item.SCHEDULE_FIELD_NAME)) {

                        List<Map<String,Object>> scheduleList = (List <Map<String,Object>>) jsonObject.get(Item.SCHEDULE_FIELD_NAME);
                        List<ItemUseSchedule> schedule = JSONObjectParser.parseJSONScheduleList(scheduleList);

                        DateFormat df = new SimpleDateFormat(OverviewViewModel.DATE_DISPLAY_FORMAT);

                        String name = jsonObject.get(Item.NAME_FIELD_NAME).toString();
                        String eDate = jsonObject.get(Item.EXPIRY_DATE_FIELD_NAME).toString();
                        String bDate = jsonObject.get(Item.BOUGHT_DATE_FIELD_NAME).toString();
                        String type = jsonObject.get(Item.TYPE_FIELD_NAME).toString();

                        String quantity = jsonObject.get(Item.QUANTITY_FIELD_NAME).toString();
                        String location = jsonObject.get(Item.STORAGE_LOCATION_FIELD_NAME).toString();

                        if(name.equals(item.getName())
                                && type.equals(item.getTypeString())
                                && eDate.equals(df.format(item.getExpiryDate()))
                                && bDate.equals(df.format(item.getBoughtDate()))
                                && quantity.equals(String.valueOf(item.getQuantity()))
                                && location.equals(item.getStorageLocation())
                                && Objects.equals(schedule,item.getSchedule())) {
                            scheduleList.add(newSchedule.toJsonObject());
                            jsonObject.put(Item.SCHEDULE_FIELD_NAME, scheduleList);
                            break;
                        }
                    }
                }

                //Delete
                transaction.update(kitchenDocRef, ITEMS_FIELD_NAME, itemsFromFireBase);
                return null;
            }
        });
    }

    /**
     * @param item      The item to remove the appointment from
     * @param schedule  The schedule to remove
     * @author u7648367 Ruixian Wu
     */
    @Override
    public void removeItemUseSchedule(String kitchenID, Item item, ItemUseSchedule schedule) {
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentReference kitchenDocRef = db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID);
                DocumentSnapshot documentSnapshot = transaction.get(kitchenDocRef);

                List<Map<String, Object>> itemsFromFireBase = (List<Map<String, Object>>) documentSnapshot.get(ITEMS_FIELD_NAME);
                for (int i = 0; i < itemsFromFireBase.size(); i++) {
                    var jsonObject = itemsFromFireBase.get(i);

                    if(jsonObject.containsKey(Item.NAME_FIELD_NAME)
                            && jsonObject.containsKey(Item.TYPE_FIELD_NAME)
                            && jsonObject.containsKey(Item.EXPIRY_DATE_FIELD_NAME)
                            && jsonObject.containsKey(Item.BOUGHT_DATE_FIELD_NAME)
                            && jsonObject.containsKey(Item.QUANTITY_FIELD_NAME)
                            && jsonObject.containsKey(Item.STORAGE_LOCATION_FIELD_NAME)
                            && jsonObject.containsKey(Item.ASSOCIATED_USER_ID_FIELD_NAME)
                            && jsonObject.containsKey(Item.ASSOCIATED_USER_EMAIL_FIELD_NAME)
                            && jsonObject.containsKey(Item.SCHEDULE_FIELD_NAME)) {

                        List<Map<String,Object>> scheduleListJson = (List <Map<String,Object>>) jsonObject.get(Item.SCHEDULE_FIELD_NAME);
                        List<ItemUseSchedule> scheduleList = JSONObjectParser.parseJSONScheduleList(scheduleListJson);

                        DateFormat df = new SimpleDateFormat(OverviewViewModel.DATE_DISPLAY_FORMAT);


                        String name = jsonObject.get(Item.NAME_FIELD_NAME).toString();
                        String type = jsonObject.get(Item.TYPE_FIELD_NAME).toString();

                        String eDate = jsonObject.get(Item.EXPIRY_DATE_FIELD_NAME).toString();
                        String bDate = jsonObject.get(Item.BOUGHT_DATE_FIELD_NAME).toString();

                        String quantity = jsonObject.get(Item.QUANTITY_FIELD_NAME).toString();
                        String location = jsonObject.get(Item.STORAGE_LOCATION_FIELD_NAME).toString();


                        if(name.equals(item.getName())
                                && type.equals(item.getTypeString())
                                && eDate.equals(df.format(item.getExpiryDate()))
                                && bDate.equals(df.format(item.getBoughtDate()))
                                && quantity.equals(String.valueOf(item.getQuantity()))
                                && location.equals(item.getStorageLocation())
                                && Objects.equals(scheduleList,item.getSchedule())) {


                            for (int scheduleListIndex = 0; scheduleListIndex < scheduleList.size(); scheduleListIndex++) {
                                if (scheduleList.get(scheduleListIndex).equals(schedule)) {
                                    scheduleListJson.remove(scheduleListIndex);
                                }
                            }

                            jsonObject.put(Item.SCHEDULE_FIELD_NAME, scheduleListJson);
                            break;
                        }
                    }
                }
                transaction.update(kitchenDocRef, ITEMS_FIELD_NAME, itemsFromFireBase);
                return null;
            }
        });
    }

    /**
     * Deletes all itemUse and cart schedules made by the given user
     * @param kitchenID
     * @param userID the user who's schedule we want to remove
     * @author u7648367 Ruixian Wu
     */
    @Override
    public void removeUserFromSchedule(String kitchenID, String userID) {
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentReference kitchenDocRef = db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID);
                DocumentSnapshot documentSnapshot = transaction.get(kitchenDocRef);



                List<Map<String, Object>> itemsFromFireBase = (List<Map<String, Object>>) documentSnapshot.get(ITEMS_FIELD_NAME);
                removeUserFromItemUseSchedule(itemsFromFireBase, userID);

                transaction.update(kitchenDocRef, ITEMS_FIELD_NAME, itemsFromFireBase);


                List<Map<String, Object>> cartItemsFromFireBase = (List<Map<String, Object>>) documentSnapshot.get(CART_FIELD_NAME);
                removeUserFromCartSchedule(cartItemsFromFireBase, userID);

                transaction.update(kitchenDocRef, CART_FIELD_NAME, cartItemsFromFireBase);


                return null;
            }
        });
    }

    /**
     * This function will remove a particular user from all of the item use schedules
     * @param itemsFromFireBase All the items from the firebase (in JSON format)
     * @param userID The user to be removed
     * @author u7648367 Ruixian Wu
     */
    private void removeUserFromItemUseSchedule(List<Map<String, Object>> itemsFromFireBase, String userID) {
        for (int i = 0; i < itemsFromFireBase.size(); i++) {
            var jsonObject = itemsFromFireBase.get(i);

            if (jsonObject.containsKey(Item.NAME_FIELD_NAME)
                    && jsonObject.containsKey(Item.TYPE_FIELD_NAME)
                    && jsonObject.containsKey(Item.EXPIRY_DATE_FIELD_NAME)
                    && jsonObject.containsKey(Item.BOUGHT_DATE_FIELD_NAME)
                    && jsonObject.containsKey(Item.QUANTITY_FIELD_NAME)
                    && jsonObject.containsKey(Item.STORAGE_LOCATION_FIELD_NAME)
                    && jsonObject.containsKey(Item.ASSOCIATED_USER_ID_FIELD_NAME)
                    && jsonObject.containsKey(Item.ASSOCIATED_USER_EMAIL_FIELD_NAME)
                    && jsonObject.containsKey(Item.SCHEDULE_FIELD_NAME)) {

                List<Map<String, Object>> scheduleListJson = (List<Map<String, Object>>) jsonObject.get(Item.SCHEDULE_FIELD_NAME);
                List<Map<String, Object>> newScheduleListJson = new ArrayList<>();

                assert scheduleListJson != null;
                for (Map<String, Object> sched : scheduleListJson) {
                    if (!sched.get(ItemUseSchedule.SCHEDULE_UID_FIELD_NAME).equals(userID)) {
                        newScheduleListJson.add(sched);
                    } else {
                        Kitchen.removeScheduleNotify((String) sched.get(ItemUseSchedule.SCHEDULE_ID_FIELD_NAME), true);
                    }
                }

                jsonObject.put(Item.SCHEDULE_FIELD_NAME, newScheduleListJson);
            }
        }
    }

    /**
     * This function will remove the user from all of the cart item use schedules
     * @param cartItemsFromFireBase The list of cart items (in JSON format)
     * @param userID the user id
     * @author u7648367 Ruixian Wu
     */
    private void removeUserFromCartSchedule(List<Map<String, Object>> cartItemsFromFireBase, String userID) {
        for (int i = 0; i < cartItemsFromFireBase.size(); i++) {
            var jsonObject = cartItemsFromFireBase.get(i);

            if(jsonObject.containsKey(CartItem.NAME_FIELD_NAME)
                    && jsonObject.containsKey(CartItem.TYPE_FIELD_NAME)
                    && jsonObject.containsKey(CartItem.EXPIRY_DAYS_FIELD_NAME)
                    && jsonObject.containsKey(CartItem.QUANTITY_FIELD_NAME)
                    && jsonObject.containsKey(CartItem.STORAGE_LOCATION_FIELD_NAME)
                    && jsonObject.containsKey(CartItem.ASSOCIATED_USER_ID_FIELD_NAME)
                    && jsonObject.containsKey(CartItem.ASSOCIATED_USER_EMAIL_FIELD_NAME)
                    && jsonObject.containsKey(CartItem.SCHEDULE_FIELD_NAME)) {

                List<Map<String,Object>> scheduleListJson = (List <Map<String,Object>>) jsonObject.get(CartItem.SCHEDULE_FIELD_NAME);
                List<Map<String, Object>> newScheduleListJson = new ArrayList<>();


                for (Map<String, Object> sched : scheduleListJson) {
                    if (!sched.get(CartSchedule.SCHEDULE_UID_FIELD_NAME).equals(userID)) {
                        newScheduleListJson.add(sched);
                    } else {
                        Kitchen.removeScheduleNotify((String) sched.get(CartSchedule.SCHEDULE_ID_FIELD_NAME), false);
                    }
                }
                jsonObject.put(CartItem.SCHEDULE_FIELD_NAME, newScheduleListJson);

            }
        }
    }

    /**
     * Writes item to FireStore
     * If an item with the same name, type, storage location, expiry date and bought date exist,
     * add the quantity of the item we are attempting to add to the item that already exists.
     * @param kitchenID
     * @param item The item
     * @author u7648367 Ruixian Wu
     */
    @Override
    public void addItem(String kitchenID, @NonNull Item item) throws InvalidQuantityException {
        if(item.getQuantity() <= 0) {
            throw new InvalidQuantityException(InvalidQuantityException.Code.NON_POSITIVE);
        }
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentReference kitchenDocRef = db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID);
                DocumentSnapshot documentSnapshot = transaction.get(kitchenDocRef);

                List<Map<String, Object>> itemsFromFireBase = (List<Map<String, Object>>) documentSnapshot.get(ITEMS_FIELD_NAME);

                boolean foundIdenticalItem = false;

                for (int i = 0; i < itemsFromFireBase.size(); i++) {
                    var jsonObject = itemsFromFireBase.get(i);

                    if(jsonObject.containsKey(Item.NAME_FIELD_NAME)
                            && jsonObject.containsKey(Item.TYPE_FIELD_NAME)
                            && jsonObject.containsKey(Item.EXPIRY_DATE_FIELD_NAME)
                            && jsonObject.containsKey(Item.BOUGHT_DATE_FIELD_NAME)
                            && jsonObject.containsKey(Item.QUANTITY_FIELD_NAME)
                            && jsonObject.containsKey(Item.STORAGE_LOCATION_FIELD_NAME)
                            && jsonObject.containsKey(Item.ASSOCIATED_USER_ID_FIELD_NAME)
                            && jsonObject.containsKey(Item.ASSOCIATED_USER_EMAIL_FIELD_NAME)
                            && jsonObject.containsKey(Item.SCHEDULE_FIELD_NAME)) {

                        DateFormat df = new SimpleDateFormat(OverviewViewModel.DATE_DISPLAY_FORMAT);

                        String name = jsonObject.get(Item.NAME_FIELD_NAME).toString();
                        String eDate = jsonObject.get(Item.EXPIRY_DATE_FIELD_NAME).toString();
                        String bDate = jsonObject.get(Item.BOUGHT_DATE_FIELD_NAME).toString();
                        String type = jsonObject.get(Item.TYPE_FIELD_NAME).toString();

                        int quantity = Integer.parseInt(jsonObject.get(Item.QUANTITY_FIELD_NAME).toString());
                        String location = jsonObject.get(Item.STORAGE_LOCATION_FIELD_NAME).toString();

                        if(name.equals(item.getName())
                                && eDate.equals(df.format(item.getExpiryDate()))
                                && bDate.equals(df.format(item.getBoughtDate()))
                                && type.equals(item.getTypeString())
                                && location.equals(item.getStorageLocation())) {

                            foundIdenticalItem = true;
                            jsonObject.put(Item.QUANTITY_FIELD_NAME, String.valueOf(quantity + item.getQuantity()));
                            break;
                        }
                    }
                }

                if (!foundIdenticalItem) {
                    itemsFromFireBase.add(item.toJSONObject());
                }

                transaction.update(kitchenDocRef, ITEMS_FIELD_NAME, itemsFromFireBase);
                return null;
            }
        });
    }

    /**
     * This function remove the item from the kitchen document
     * @param kitchenID The kitchen document id
     * @param item The item
     * @author u7643339 Isaac Leong
     */
    @Override
    public void removeItem(String kitchenID, @NonNull Item item) {
        db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID).update(ITEMS_FIELD_NAME, FieldValue.arrayRemove(item.toJSONObject()));
    }

    /**
     *
     * @param kitchenId the id of the kitchen with the item.
     * @param oldItem the item to edit, i.e. to replace.
     * @param newItem the new item.
     * @author u7333216 Aaron Nguyen, u7643339 Isaac Leong
     */
    @Override
    public void editItem(String kitchenId, @NonNull Item oldItem, @NonNull Item newItem) {
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentReference kitchenDocRef = db.collection(KITCHEN_COLLECTION_NAME).document(kitchenId);
                DocumentSnapshot documentSnapshot = transaction.get(kitchenDocRef);

                List<Map<String, Object>> itemsFromFireBase = (List<Map<String, Object>>) documentSnapshot.get(ITEMS_FIELD_NAME);
                for (int i = 0; i < itemsFromFireBase.size(); i++) {
                    var jsonObject = itemsFromFireBase.get(i);

                    if(jsonObject.containsKey(Item.NAME_FIELD_NAME)
                            && jsonObject.containsKey(Item.TYPE_FIELD_NAME)
                            && jsonObject.containsKey(Item.EXPIRY_DATE_FIELD_NAME)
                            && jsonObject.containsKey(Item.BOUGHT_DATE_FIELD_NAME)
                            && jsonObject.containsKey(Item.QUANTITY_FIELD_NAME)
                            && jsonObject.containsKey(Item.STORAGE_LOCATION_FIELD_NAME)
                            && jsonObject.containsKey(Item.ASSOCIATED_USER_ID_FIELD_NAME)
                            && jsonObject.containsKey(Item.ASSOCIATED_USER_EMAIL_FIELD_NAME)
                            && jsonObject.containsKey(Item.SCHEDULE_FIELD_NAME)) {

                        List<Map<String,Object>> scheduleList = (List <Map<String,Object>>) jsonObject.get(Item.SCHEDULE_FIELD_NAME);
                        List<ItemUseSchedule> schedule = JSONObjectParser.parseJSONScheduleList(scheduleList);

                        DateFormat df = new SimpleDateFormat(OverviewViewModel.DATE_DISPLAY_FORMAT);

                        String name = jsonObject.get(Item.NAME_FIELD_NAME).toString();
                        String eDate = jsonObject.get(Item.EXPIRY_DATE_FIELD_NAME).toString();
                        String bDate = jsonObject.get(Item.BOUGHT_DATE_FIELD_NAME).toString();
                        String type = jsonObject.get(Item.TYPE_FIELD_NAME).toString();

                        String quantity = jsonObject.get(Item.QUANTITY_FIELD_NAME).toString();
                        String location = jsonObject.get(Item.STORAGE_LOCATION_FIELD_NAME).toString();

                        if(name.equals(oldItem.getName())
                                && type.equals(oldItem.getTypeString())
                                && eDate.equals(df.format(oldItem.getExpiryDate()))
                                && bDate.equals(df.format(oldItem.getBoughtDate()))
                                && quantity.equals(String.valueOf(oldItem.getQuantity()))
                                && location.equals(oldItem.getStorageLocation())
                                && schedule.equals(oldItem.getSchedule())) {
                            itemsFromFireBase.set(i, newItem.toJSONObject());
                            break;
                        }
                    }
                }

                transaction.update(kitchenDocRef, ITEMS_FIELD_NAME, itemsFromFireBase);
                return null;
            }
        });
    }

    /**
     * This function adds a cart item into the list of cart items
     * @param kitchenID the kitchen associated that is identified by id
     * @param cartItem The cart item to be added to the shopping cart
     * @throws InvalidQuantityException This exception is thrown when you are trying to add cart item with negative quantity
     * @author u7648367 Ruixian Wu
     */
    @Override
    public void addCartItem(String kitchenID, CartItem cartItem) throws InvalidQuantityException {
        if(cartItem.getQuantity() <= 0) {
            throw new InvalidQuantityException(InvalidQuantityException.Code.NON_POSITIVE);
        }
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentReference kitchenDocRef = db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID);
                DocumentSnapshot documentSnapshot = transaction.get(kitchenDocRef);

                List<Map<String, Object>> itemsFromFireBase = (List<Map<String, Object>>) documentSnapshot.get(CART_FIELD_NAME);

                boolean foundIdenticalItem = false;

                for (int i = 0; i < itemsFromFireBase.size(); i++) {
                    var jsonObject = itemsFromFireBase.get(i);

                    if(jsonObject.containsKey(CartItem.NAME_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.TYPE_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.EXPIRY_DAYS_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.QUANTITY_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.STORAGE_LOCATION_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.ASSOCIATED_USER_ID_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.ASSOCIATED_USER_EMAIL_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.SCHEDULE_FIELD_NAME)) {

                        String name = jsonObject.get(CartItem.NAME_FIELD_NAME).toString();
                        String days = jsonObject.get(CartItem.EXPIRY_DAYS_FIELD_NAME).toString();
                        int quantity = Integer.parseInt(jsonObject.get(CartItem.QUANTITY_FIELD_NAME).toString());
                        String location = jsonObject.get(CartItem.STORAGE_LOCATION_FIELD_NAME).toString();
                        String type = jsonObject.get(CartItem.TYPE_FIELD_NAME).toString();

                        if(name.equals(cartItem.getName())
                                && type.equals(cartItem.getTypeString())
                                && Integer.parseInt(days) == cartItem.getExpiryDays()
                                && type.equals(cartItem.getTypeString())
                                && location.equals(cartItem.getStorageLocation())) {

                            foundIdenticalItem = true;
                            jsonObject.put(Item.QUANTITY_FIELD_NAME, String.valueOf(quantity + cartItem.getQuantity()));
                            break;

                        }
                    }
                }

                if (!foundIdenticalItem) {
                    itemsFromFireBase.add(cartItem.toJSONObject());
                }

                transaction.update(kitchenDocRef, CART_FIELD_NAME, itemsFromFireBase);
                return null;
            }
        });
    }

    /**
     * This function remove the cart item from the list
     * @param kitchenID the kitchen associated that is identified by id
     * @param cartItem the cart item
     * @author u7648367 Ruixian
     */
    @Override
    public void removeCartItem(String kitchenID, CartItem cartItem) {
        db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID).update(CART_FIELD_NAME, FieldValue.arrayRemove(cartItem.toJSONObject()));
    }

    /**
     * This function add a cart item schedule
     * @param kitchenID The kitchen id
     * @param item The cart item that the schedule should go in
     * @param cartSchedule the cart schedule to be added in
     * @author u7648367 Ruixian Wu
     */
    @Override
    public void addCartItemSchedule(String kitchenID, CartItem item, CartSchedule cartSchedule) {
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentReference kitchenDocRef = db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID);
                DocumentSnapshot documentSnapshot = transaction.get(kitchenDocRef);

                List<Map<String, Object>> itemsFromFireBase = (List<Map<String, Object>>) documentSnapshot.get(CART_FIELD_NAME);
                for (int i = 0; i < itemsFromFireBase.size(); i++) {
                    var jsonObject = itemsFromFireBase.get(i);

                    if(jsonObject.containsKey(CartItem.NAME_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.TYPE_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.EXPIRY_DAYS_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.QUANTITY_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.STORAGE_LOCATION_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.ASSOCIATED_USER_ID_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.ASSOCIATED_USER_EMAIL_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.SCHEDULE_FIELD_NAME)) {

                        List<Map<String,Object>> scheduleListJson = (List <Map<String,Object>>) jsonObject.get(CartItem.SCHEDULE_FIELD_NAME);
                        List<CartSchedule> scheduleList = JSONObjectParser.parseJSONCartScheduleList(scheduleListJson);


                        String name = jsonObject.get(CartItem.NAME_FIELD_NAME).toString();
                        String days = jsonObject.get(CartItem.EXPIRY_DAYS_FIELD_NAME).toString();
                        String quantity = jsonObject.get(Item.QUANTITY_FIELD_NAME).toString();
                        String location = jsonObject.get(Item.STORAGE_LOCATION_FIELD_NAME).toString();
                        String type = jsonObject.get(CartItem.TYPE_FIELD_NAME).toString();


                        if(name.equals(item.getName())
                                && type.equals(item.getTypeString())
                                && Integer.parseInt(days) == item.getExpiryDays()
                                && quantity.equals(String.valueOf(item.getQuantity()))
                                && location.equals(item.getStorageLocation())
                                && scheduleList.equals(item.getSchedule())) {

                            scheduleListJson.add(cartSchedule.toJsonObject());
                            jsonObject.put(CartItem.SCHEDULE_FIELD_NAME, scheduleListJson);
                            break;
                        }
                    }
                }
                transaction.update(kitchenDocRef, CART_FIELD_NAME, itemsFromFireBase);
                return null;
            }
        });
    }

    /**
     * This function removes a schedule from the cart item
     * @param kitchenID the kitchen id
     * @param cartItem The cart item that the schedule should be removed from
     * @param schedule the cart schedule to be removed
     * @author u7648367 Ruixian Wu
     */
    @Override
    public void removeCartItemSchedule(String kitchenID, CartItem cartItem, CartSchedule schedule) {
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentReference kitchenDocRef = db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID);
                DocumentSnapshot documentSnapshot = transaction.get(kitchenDocRef);

                if(documentSnapshot.get(CART_FIELD_NAME) == null) {
                    throw new FirebaseFirestoreException("Cannot find " + CART_FIELD_NAME + " in the document", FirebaseFirestoreException.Code.ABORTED);
                }

                List<Map<String, Object>> itemsFromFireBase = (List<Map<String, Object>>) documentSnapshot.get(CART_FIELD_NAME);
                for (int i = 0; i < Objects.requireNonNull(itemsFromFireBase).size(); i++) {
                    Map<String, Object> jsonObject = itemsFromFireBase.get(i);

                    if(jsonObject.get(CartItem.NAME_FIELD_NAME) != null
                            && jsonObject.get(CartItem.TYPE_FIELD_NAME) != null
                            && jsonObject.get(CartItem.EXPIRY_DAYS_FIELD_NAME) != null
                            && jsonObject.get(CartItem.QUANTITY_FIELD_NAME) != null
                            && jsonObject.get(CartItem.STORAGE_LOCATION_FIELD_NAME) != null
                            && jsonObject.get(CartItem.ASSOCIATED_USER_ID_FIELD_NAME) != null
                            && jsonObject.get(CartItem.ASSOCIATED_USER_EMAIL_FIELD_NAME) != null
                            && jsonObject.get(CartItem.SCHEDULE_FIELD_NAME) != null) {

                        Log.i("DEBUG", "found well-formatted item");

                        List<Map<String,Object>> scheduleListJson = (List <Map<String,Object>>) jsonObject.get(CartItem.SCHEDULE_FIELD_NAME);
                        List<CartSchedule> scheduleList = JSONObjectParser.parseJSONCartScheduleList(scheduleListJson);


                        String name = Objects.requireNonNull(jsonObject.get(CartItem.NAME_FIELD_NAME)).toString();
                        String days = Objects.requireNonNull(jsonObject.get(CartItem.EXPIRY_DAYS_FIELD_NAME)).toString();
                        String quantity = Objects.requireNonNull(jsonObject.get(CartItem.QUANTITY_FIELD_NAME)).toString();
                        String location = Objects.requireNonNull(jsonObject.get(CartItem.STORAGE_LOCATION_FIELD_NAME)).toString();
                        String type = jsonObject.get(CartItem.TYPE_FIELD_NAME).toString();


                        if(name.equals(cartItem.getName())
                                && type.equals(cartItem.getTypeString())
                                && Integer.parseInt(days) == cartItem.getExpiryDays()
                                && quantity.equals(String.valueOf(cartItem.getQuantity()))
                                && location.equals(cartItem.getStorageLocation())
                                && scheduleList.equals(cartItem.getSchedule())) {

                            Log.i("DEBUG", "found correct item");


                            for (int scheduleListIndex = 0; scheduleListIndex < scheduleList.size(); scheduleListIndex++) {
                                if (scheduleList.get(scheduleListIndex).equals(schedule)) {
                                    scheduleListJson.remove(scheduleListIndex);
                                }
                            }

                            jsonObject.put(CartItem.SCHEDULE_FIELD_NAME, scheduleListJson);
                            break;
                        }
                    }
                }
                transaction.update(kitchenDocRef, CART_FIELD_NAME, itemsFromFireBase);
                return null;
            }
        });
    }

    /**
     *
     * @param item The item to be changed
     * @param newQuantity the new quantity of the item
     * @author u7648367 Ruixian Wu
     */
    @Override
    public void setCartItemQuantity(String kitchenID, CartItem item, int newQuantity) throws InvalidQuantityException {
        writeCartItemField(kitchenID, item, CartItem.QUANTITY_FIELD_NAME, String.valueOf(newQuantity));
    }

    /**
     *
     * @param kitchenID
     * @param item
     * @param storageLocation
     * @author u7648367 Ruixian Wu
     */
    @Override
    public void setCartItemStorageLocation(String kitchenID, CartItem item, String storageLocation) throws InvalidQuantityException {
        writeCartItemField(kitchenID, item, CartItem.STORAGE_LOCATION_FIELD_NAME, storageLocation);
    }

    /**
     *
     * @param kitchenID
     * @param item
     * @param newExpiryDays
     * @author u7648367 Ruixian Wu
     */
    @Override
    public void setCartItemExpiryDays(String kitchenID, CartItem item, int newExpiryDays) throws InvalidQuantityException {
        writeCartItemField(kitchenID, item, CartItem.EXPIRY_DAYS_FIELD_NAME, String.valueOf(newExpiryDays));
    }

    /**
     * Writes Cart Item fields quantity, expiry days, and location
     * @param kitchenID
     * @param item
     * @param fieldName
     * @param fieldValue
     * @author u7648367 Ruixian Wu
     */
    private void writeCartItemField(String kitchenID, CartItem item, String fieldName, String fieldValue) throws InvalidQuantityException {
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentReference kitchenDocRef = db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID);
                DocumentSnapshot documentSnapshot = transaction.get(kitchenDocRef);

                List<Map<String, Object>> itemsFromFireBase = (List<Map<String, Object>>) documentSnapshot.get(CART_FIELD_NAME);
                for (int i = 0; i < itemsFromFireBase.size(); i++) {
                    var jsonObject = itemsFromFireBase.get(i);

                    if(jsonObject.containsKey(CartItem.NAME_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.TYPE_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.EXPIRY_DAYS_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.QUANTITY_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.STORAGE_LOCATION_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.ASSOCIATED_USER_ID_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.ASSOCIATED_USER_EMAIL_FIELD_NAME)
                            && jsonObject.containsKey(CartItem.SCHEDULE_FIELD_NAME)) {

                        List<Map<String,Object>> scheduleList = (List <Map<String,Object>>) jsonObject.get(CartItem.SCHEDULE_FIELD_NAME);
                        List<CartSchedule> schedule = JSONObjectParser.parseJSONCartScheduleList(scheduleList);

                        String name = jsonObject.get(CartItem.NAME_FIELD_NAME).toString();
                        String days = jsonObject.get(CartItem.EXPIRY_DAYS_FIELD_NAME).toString();
                        String quantity = jsonObject.get(Item.QUANTITY_FIELD_NAME).toString();
                        String location = jsonObject.get(Item.STORAGE_LOCATION_FIELD_NAME).toString();
                        String type = jsonObject.get(Item.TYPE_FIELD_NAME).toString();



                        if(name.equals(item.getName())
                                && Integer.parseInt(days) == item.getExpiryDays()
                                && quantity.equals(String.valueOf(item.getQuantity()))
                                && type.equals(item.getTypeString())
                                && location.equals(item.getStorageLocation())
                                && schedule.equals(item.getSchedule())) {

                            if (fieldName.equals(CartItem.EXPIRY_DAYS_FIELD_NAME)) {
                                int expiryDays = Integer.parseInt(fieldName);
                                if (expiryDays > 0) {
                                jsonObject.put(CartItem.EXPIRY_DAYS_FIELD_NAME, String.valueOf(fieldValue));
                                }
                            } else if (fieldName.equals(CartItem.STORAGE_LOCATION_FIELD_NAME)) {
                                jsonObject.put(CartItem.STORAGE_LOCATION_FIELD_NAME, fieldValue);
                            } else if (fieldName.equals(CartItem.QUANTITY_FIELD_NAME)) {
                                int newQuantity = Integer.parseInt(fieldValue);
                                if (schedule.isEmpty() && newQuantity == 0) {
                                    itemsFromFireBase.remove(jsonObject);
                                } else if (newQuantity >= 0) {
                                    jsonObject.put(CartItem.QUANTITY_FIELD_NAME, String.valueOf(fieldValue));
                                }
                            }

                            break;
                        }
                    }
                }

                transaction.update(kitchenDocRef, CART_FIELD_NAME, itemsFromFireBase);
                return null;
            }
        });

    }

    /**
     * This will exercise the use option to an item
     * @param kitchenID The kitchen that the item is stored in
     * @param uid The user id that is using the item
     * @param itemName The name of the item
     * @param scheduleQuantity The scheduled quantity
     * @param scheduleID The schedule id
     * @param uemail The user email
     * @author u7648367 Ruixian
     */
    @Override
    public void executeItemUseSchedule(String kitchenID, String uid, String itemName, int scheduleQuantity, String scheduleID, String uemail) {
        DocumentReference docRef = db.collection("kitchens").document(kitchenID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot ds = task.getResult();
                if (ds.exists()) {
                    String ownerID = (String) ds.get(KitchenFirebaseDAO.OWNER_FIELD_NAME);
                    List<String> memberIDList = (List<String>) ds.get(KitchenFirebaseDAO.RESIDENTS_FIELD_NAME);
                    memberIDList.add(ownerID);
                    if (!memberIDList.contains(uid)) {
                        return;
                    }

                    boolean execute = false;
                    boolean foundSchedule = false;

                    List<Map<String, Object>> itemsFromFireBase = (List<Map<String, Object>>) ds.get(KitchenFirebaseDAO.ITEMS_FIELD_NAME);
                    for (var jsonObject : itemsFromFireBase) {
                        if(jsonObject.containsKey(Item.NAME_FIELD_NAME)
                                && jsonObject.containsKey(Item.TYPE_FIELD_NAME)
                                && jsonObject.containsKey(Item.EXPIRY_DATE_FIELD_NAME)
                                && jsonObject.containsKey(Item.BOUGHT_DATE_FIELD_NAME)
                                && jsonObject.containsKey(Item.QUANTITY_FIELD_NAME)
                                && jsonObject.containsKey(Item.STORAGE_LOCATION_FIELD_NAME)
                                && jsonObject.containsKey(Item.ASSOCIATED_USER_ID_FIELD_NAME)
                                && jsonObject.containsKey(Item.ASSOCIATED_USER_EMAIL_FIELD_NAME)
                                && jsonObject.containsKey(Item.SCHEDULE_FIELD_NAME)
                                && jsonObject.get(Item.NAME_FIELD_NAME).equals(itemName)) {
                            if (!(jsonObject.get(Item.NAME_FIELD_NAME).equals(itemName))) {
                                continue;
                            }

                            int itemQuantity = Integer.parseInt(jsonObject.get(Item.QUANTITY_FIELD_NAME).toString());
                            if (itemQuantity == scheduleQuantity) {
                                execute = true;
                                docRef.update(KitchenFirebaseDAO.ITEMS_FIELD_NAME, FieldValue.arrayRemove(jsonObject));
                            }
                            else {
                                //Checking if the current jsonObject contains the correct schedule
                                List<Map<String,Object>> scheduleList = (List <Map<String,Object>>) jsonObject.get(Item.SCHEDULE_FIELD_NAME);
                                for (Map<String,Object> scheduleJsonObject : scheduleList) {
                                    if (scheduleJsonObject.containsKey(ItemUseSchedule.SCHEDULE_DATE_FIELD_NAME)
                                            && scheduleJsonObject.containsKey(ItemUseSchedule.SCHEDULE_QUANTITY_FIELD_NAME)
                                            && scheduleJsonObject.containsKey(ItemUseSchedule.SCHEDULE_ID_FIELD_NAME)
                                            && scheduleJsonObject.containsKey(ItemUseSchedule.SCHEDULE_UEMAIL_FIELD_NAME)
                                            && scheduleJsonObject.containsKey(ItemUseSchedule.SCHEDULE_UID_FIELD_NAME)

                                            && scheduleJsonObject.get(ItemUseSchedule.SCHEDULE_ID_FIELD_NAME).equals(scheduleID)) {

                                        //Only complete item use according to schedule if there is sufficient item quantity
                                        Log.i("DEBUG", "itemQuantity - scheduleQuantity >= 0" + (itemQuantity - scheduleQuantity >= 0));
                                        if (itemQuantity - scheduleQuantity >= 0) {
                                            Log.i("DEBUG", "execute = true");
                                            execute = true;

                                            jsonObject.put(Item.QUANTITY_FIELD_NAME,itemQuantity - scheduleQuantity);

                                        }
                                        boolean b = scheduleList.remove(scheduleJsonObject);
                                        Log.i("DEBUG", b + "");
                                        jsonObject.put(Item.SCHEDULE_FIELD_NAME, scheduleList);

                                        foundSchedule = true;

                                        docRef.update(KitchenFirebaseDAO.ITEMS_FIELD_NAME, itemsFromFireBase);

                                        break;
                                    }
                                }
                            }
                            //Schedule found but did not execute due to insufficient quantity
                            Log.i("DEBUG", "update history: " + execute);

                            if (execute) {
                                //Update history
                                if (ds.get(KitchenFirebaseDAO.HISTORY_FIELD_NAME) != null) {

                                    Map<String,Object> historyJsonObject = new HashMap<String,Object>();

                                    DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

                                    historyJsonObject.put(ItemUsage.NAME_FIELD_NAME, itemName);
                                    historyJsonObject.put(ItemUsage.USING_DATE, dateFormat.format(Calendar.getInstance().getTimeInMillis()));
                                    historyJsonObject.put(ItemUsage.QUANTITY_FIELD_NAME,scheduleQuantity);
                                    historyJsonObject.put(ItemUsage.ASSOCIATED_USER_ID_FIELD_NAME, uid);
                                    historyJsonObject.put(ItemUsage.ASSOCIATED_USER_EMAIL_FIELD_NAME, uemail);

                                    Log.i("DEBUG", "schedule updating history");
                                    docRef.update(KitchenFirebaseDAO.HISTORY_FIELD_NAME, FieldValue.arrayUnion(historyJsonObject));
                                }
                                break;
                            }
                            if (foundSchedule) {
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    /**
     * This will exercise the option of using an cart item
     * @param kitchenID The kitchen id
     * @param uid The user id
     * @param itemName The name of the item
     * @param scheduleID The id of the schedule
     * @param scheduleDaysReoccurring The recorrding schedule day
     * @param requestCode The request code
     * @param context The context of this application
     * @author u7648367 Ruixian Wu
     */
    public void executeCartSchedule(String kitchenID, String uid, String itemName, String scheduleID, int scheduleDaysReoccurring, int requestCode, Context context) {
        DocumentReference docRef = db.collection("kitchens").document(kitchenID);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot docSnapshot = task.getResult();
                if (docSnapshot.exists()) {
                    try {
                        boolean scheduleFound = false;

                        String ownerID = (String) docSnapshot.get(KitchenFirebaseDAO.OWNER_FIELD_NAME);

                        List<String> memberIDList = (List<String>) docSnapshot.get(KitchenFirebaseDAO.RESIDENTS_FIELD_NAME);
                        memberIDList.add(ownerID);
                        if (!memberIDList.contains(uid)) {

                            CartScheduleAlarm.cancelCartSchedule(requestCode, context);

                            Log.i("DEBUG", "cancelled - missing user");
                        } else {
                            List<Map<String, Object>> itemsFromFireBase = (List<Map<String, Object>>) docSnapshot.get(KitchenFirebaseDAO.CART_FIELD_NAME);
                            for (int i = 0; i < itemsFromFireBase.size(); i++) {
                                var jsonObject = itemsFromFireBase.get(i);

                                if(jsonObject.containsKey(CartItem.NAME_FIELD_NAME)
                                        && jsonObject.containsKey(CartItem.TYPE_FIELD_NAME)
                                        && jsonObject.containsKey(CartItem.EXPIRY_DAYS_FIELD_NAME)
                                        && jsonObject.containsKey(CartItem.QUANTITY_FIELD_NAME)
                                        && jsonObject.containsKey(CartItem.STORAGE_LOCATION_FIELD_NAME)
                                        && jsonObject.containsKey(CartItem.ASSOCIATED_USER_ID_FIELD_NAME)
                                        && jsonObject.containsKey(CartItem.ASSOCIATED_USER_EMAIL_FIELD_NAME)
                                        && jsonObject.containsKey(CartItem.SCHEDULE_FIELD_NAME)

                                        && ((String) jsonObject.get(Item.NAME_FIELD_NAME)).equals(itemName)) {


                                    int itemQuantity = Integer.parseInt(jsonObject.get(CartItem.QUANTITY_FIELD_NAME).toString());

                                    List<Map<String,Object>> scheduleList = (List <Map<String,Object>>) jsonObject.get(CartItem.SCHEDULE_FIELD_NAME);
                                    for (Map<String,Object> scheduleJsonObject : scheduleList) {
                                        if (scheduleJsonObject.containsKey(CartSchedule.SCHEDULE_DATE_FIELD_NAME)
                                                && scheduleJsonObject.containsKey(CartSchedule.SCHEDULE_QUANTITY_FIELD_NAME)
                                                && scheduleJsonObject.containsKey(CartSchedule.SCHEDULE_DAYS_REOCCURRING_FIELD_NAME)
                                                && scheduleJsonObject.containsKey(CartSchedule.SCHEDULE_ID_FIELD_NAME)
                                                && scheduleJsonObject.containsKey(CartSchedule.SCHEDULE_UEMAIL_FIELD_NAME)
                                                && scheduleJsonObject.containsKey(CartSchedule.SCHEDULE_UID_FIELD_NAME)

                                                && (scheduleJsonObject.get(CartSchedule.SCHEDULE_ID_FIELD_NAME).toString()).equals(scheduleID)
                                        ) {


                                            if (scheduleDaysReoccurring == 0) {
                                                scheduleList.remove(scheduleJsonObject);
                                                jsonObject.put(CartItem.SCHEDULE_FIELD_NAME, scheduleList);
                                            } else {
                                                Long currDate = (CartSchedule.df).parse((String) scheduleJsonObject.get(CartSchedule.SCHEDULE_DATE_FIELD_NAME)).getTime();
                                                Long nextDate = currDate + (long) scheduleDaysReoccurring * 24 * 60 * 60 * 1000;

                                                Date date = Calendar.getInstance().getTime();
                                                date.setTime(nextDate);

                                                String nextDateString = (CartSchedule.df).format(date);

                                                scheduleJsonObject.put(CartSchedule.SCHEDULE_DATE_FIELD_NAME, nextDateString);
                                                jsonObject.put(CartItem.SCHEDULE_FIELD_NAME, scheduleList);

                                            }


                                            jsonObject.put(CartItem.QUANTITY_FIELD_NAME,
                                                    String.valueOf(Integer.parseInt(jsonObject.get(CartItem.QUANTITY_FIELD_NAME).toString()) + Integer.parseInt(scheduleJsonObject.get(CartSchedule.SCHEDULE_QUANTITY_FIELD_NAME).toString())));



                                            docRef.update(KitchenFirebaseDAO.CART_FIELD_NAME, itemsFromFireBase);
                                            scheduleFound = true;
                                            break;
                                        }

                                    }

                                    if (scheduleFound) {
                                        break;
                                    }
                                }
                            }


                            if (scheduleDaysReoccurring != 0 && !scheduleFound) {
                                CartScheduleAlarm.cancelCartSchedule(requestCode, context);
                                Log.i("DEBUG", "cancelled - missing schedule");
                            }
                        }
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }


    /**
     *
     * @param kitchenID The kitchen ID of the target kitchen
     * @param itemUsage The history record
     * @author u7517596 ChengBo Yan
     */
    @Override
    public void addItemUsage(String kitchenID, ItemUsage itemUsage) {
        Log.i("userItemEmail",itemUsage.getUser().getEmail());
        db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID).update(HISTORY_FIELD_NAME, FieldValue.arrayUnion(itemUsage.toJSONObject()));
    }

    /**
     *
     * @param kitchenID
     * @param notification The notification
     * @author u7517596 ChengBo Yan
     */
    @Override
    public void addNotification(String kitchenID, Notification notification) {
        db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID).update(NOTIFICATION_FIELD_NAME, FieldValue.arrayUnion(notification.toJSONObject()));
    }

    /**
     *
     * @param kitchenID
     * @param notification The notification to be removed
     * @author u7517596 ChengBo Yan
     */
    @Override
    public void removeNotification(String kitchenID, Notification notification) {
        db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID).update(NOTIFICATION_FIELD_NAME, FieldValue.arrayRemove(notification.toJSONObject()));
    }

    /**
     * This function adds active residents
     * @param kitchenID the kitchen id to specify the particular kitchen document
     * @param userID the user id to be added
     * @author u7643339 Isaac Leong
     */
    @Override
    public void addActiveResidents(String kitchenID, String userID) {
        db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID).update(RESIDENTS_FIELD_NAME, FieldValue.arrayUnion(userID));
    }

    /**
     * This function removes active residents
     * @param kitchenID the kitchen id to specify the particular kitchen document
     * @param userID the user to be removed
     * @author u7643339 Isaac Leong
     */
    @Override
    public void removeActiveResidents(String kitchenID, String userID) {
        db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID).update(RESIDENTS_FIELD_NAME, FieldValue.arrayRemove(userID));
    }

    /**
     * This function bans active residents
     * @param kitchenID the kitchen id to specify the particular kitchen document
     * @param userID the user to be banned
     * @author u7643339 Isaac Leong
     */
    @Override
    public void addBannedResidents(String kitchenID, String userID) {
        db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID).update(BANNED_RESIDENTS_FIELD_NAME, FieldValue.arrayUnion(userID));
    }

    /**
     * This function removes a resident from the banned list
     * @param kitchenID the kitchen id to specify the particular kitchen document
     * @param userID the user to be removed
     * @author u7643339 Isaac Leong
     */
    @Override
    public void removeBannedResidents(String kitchenID, String userID) {
        db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID).update(BANNED_RESIDENTS_FIELD_NAME, FieldValue.arrayRemove(userID));
    }

    /**
     * This function adds the user to the pending residents
     * @param kitchenID The kitchen that the user wants to be added to
     * @param userID the user that raises the request
     * @author u7643339 Isaac Leong
     */
    @Override
    public void addPendingResidents(String kitchenID, String userID) {
        db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID).update(PENDING_RESIDENTS_FIELD_NAME, FieldValue.arrayUnion(userID));
    }

    /**
     * This function remove the user in the pending residents list
     * @param kitchenID the kitchen the user wanted to join
     * @param userID the user to be rejected for joining the kitchen
     * @author u7643339 Isaac Leong
     */
    @Override
    public void removePendingResidents(String kitchenID, String userID) {
        db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID).update(PENDING_RESIDENTS_FIELD_NAME, FieldValue.arrayRemove(userID));
    }

    /**
     * This will delete the kitchen document (only)
     * @param kitchenID the kitchen to be deleted
     * @param writeListener The result of the deletion
     * @author u7643339 Isaac Leong
     */
    @Override
    public void deleteKitchen(String kitchenID, WriteListener writeListener) {
        db.collection(KITCHEN_COLLECTION_NAME).document(kitchenID).delete()
                .addOnSuccessListener(writeListener::onWriteSuccess)
                .addOnFailureListener(writeListener::onWriteFailure);
    }

    /**
     * This function will check if the given document snapshot contains a non-null object of the given field
     * @param value the document snapshot
     * @param fieldName the field name
     * @return true iff the document contains the field and the field has a non-null object
     * @author u7517596 ChengBo Yan
     */
    private boolean checkFieldInDocument(DocumentSnapshot value, String fieldName) {
        return value.contains(fieldName) && value.get(fieldName) != null;
    }
}
