package com.aaronnguyen.mykitchen.DAO;

import android.util.Log;

import com.aaronnguyen.mykitchen.model.Items.CartItems.CartItem;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartItemFactory;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartSchedule;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Item;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemFactory;
import com.aaronnguyen.mykitchen.model.Items.ItemUsage;
import com.aaronnguyen.mykitchen.model.Items.Schedule;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemUseSchedule;
import com.aaronnguyen.mykitchen.model.notification.AppExpiryNotification;
import com.aaronnguyen.mykitchen.model.notification.Notification;
import com.aaronnguyen.mykitchen.model.notification.NotificationFactory;
import com.aaronnguyen.mykitchen.model.user.User;
import com.aaronnguyen.mykitchen.ui.main.Overview.OverviewViewModel;
import com.aaronnguyen.mykitchen.ui.main.ShoppingCart.CartViewModel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This class parses the JSON object that are from Firebase
 * @author u7648367 Ruixian Wu, u7517596 ChengBo Yan, u7643339 Isaac Leong
 */
public class JSONObjectParser {

    /**
     * This will parse the list of notifications in the provided document snapshot
     * @param notificationsFromFirebase The notification list from firebase as a JSON object represented by map
     * @return an array list of notifications
     */
    public static ArrayList<Notification> parseNotification(List<Map<String, Object>> notificationsFromFirebase) {
        ArrayList<Notification> notifications = new ArrayList<>();
        for (var jsonObject : notificationsFromFirebase) {
            if (jsonObject.containsKey(AppExpiryNotification.MESSAGE)
                    && jsonObject.containsKey(AppExpiryNotification.IMPORTANCE)) {
                notifications.add(NotificationFactory.createNotification(
                        jsonObject.get(AppExpiryNotification.MESSAGE).toString(),
                        Integer.parseInt(jsonObject.get(AppExpiryNotification.IMPORTANCE).toString()))
                );
            }
        }
        return notifications;
    }

    /**
     * This will parse the json form of the list of history record given the document snapshot
     * @param historyFromFireBase The history list from firebase as a JSON object represented by map
     * @return The ArrayList of the history records
     */
    public static ArrayList<ItemUsage> parseHistoryList(List<Map<String, Object>> historyFromFireBase) {
        Log.i("DEBUG", "Start fetching history in kitchen");
        ArrayList<ItemUsage> itemUsageList = new ArrayList<>();
        Log.i("c",historyFromFireBase.toString());
        for (var jsonObject : historyFromFireBase) {

            if (jsonObject.containsKey(ItemUsage.NAME_FIELD_NAME)
                    && jsonObject.containsKey(ItemUsage.USING_DATE)
                    && jsonObject.containsKey(ItemUsage.QUANTITY_FIELD_NAME)
                    && jsonObject.containsKey(ItemUsage.ASSOCIATED_USER_EMAIL_FIELD_NAME)
                    &&jsonObject.containsKey(ItemUsage.ASSOCIATED_USER_ID_FIELD_NAME)
            ) {
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    Date date = df.parse(jsonObject.get(ItemUsage.USING_DATE).toString());
                    itemUsageList.add(ItemUsage.createItemUsage(
                            jsonObject.get(ItemUsage.NAME_FIELD_NAME).toString(),
                            date,
                            jsonObject.get(ItemUsage.QUANTITY_FIELD_NAME).toString(),
                            new User(
                                    jsonObject.get(ItemUsage.ASSOCIATED_USER_ID_FIELD_NAME).toString()
                            )
                    ));

                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        Log.i("get ItemUsage list",itemUsageList.toString());
        return itemUsageList;
    }

    /**
     * This will parse the json form of the item list given the document snapshot
     * @param itemsFromFireBase The item list from firebase represented as a JSON object by a map
     * @return The ArrayList of the item
     */
    public static ArrayList<Item> parseItemList(List<Map<String, Object>> itemsFromFireBase) {
        Log.i("DEBUG", "Syncing list of items");
        ArrayList<Item> itemList = new ArrayList<>();
        for (var jsonObject : itemsFromFireBase) {
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

                try {
                    List<Map<String,Object>> scheduleList = (List <Map<String,Object>>) jsonObject.get(Item.SCHEDULE_FIELD_NAME);
                    List<ItemUseSchedule> schedule = parseJSONScheduleList(scheduleList);

                    itemList.add(ItemFactory.createItem(
                            jsonObject.get(Item.NAME_FIELD_NAME).toString(),
                            jsonObject.get(Item.TYPE_FIELD_NAME).toString(),
                            df.parse(jsonObject.get(Item.EXPIRY_DATE_FIELD_NAME).toString()),
                            df.parse(jsonObject.get(Item.BOUGHT_DATE_FIELD_NAME).toString()),
                            new User(jsonObject.get(Item.ASSOCIATED_USER_ID_FIELD_NAME).toString()),
                            Integer.parseInt(jsonObject.get(Item.QUANTITY_FIELD_NAME).toString()),
                            jsonObject.get(Item.STORAGE_LOCATION_FIELD_NAME).toString(),
                            schedule
                    ));
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return itemList;
    }

    /**
     * This function extract the cart item list in the document snapshot
     * @param cartItemsFromFireBase The cart item list from firebase as JSON object represented by a map
     * @return This returns a list of cart item
     */
    public static ArrayList<CartItem> parseCartItemList(List<Map<String, Object>> cartItemsFromFireBase) {
        ArrayList<CartItem> cartItemList = new ArrayList<>();
        for (var jsonObject : cartItemsFromFireBase) {
            if(jsonObject.containsKey(CartItem.NAME_FIELD_NAME)
                    && jsonObject.containsKey(CartItem.TYPE_FIELD_NAME)
                    && jsonObject.containsKey(CartItem.EXPIRY_DAYS_FIELD_NAME)
                    && jsonObject.containsKey(CartItem.QUANTITY_FIELD_NAME)
                    && jsonObject.containsKey(CartItem.STORAGE_LOCATION_FIELD_NAME)
                    && jsonObject.containsKey(CartItem.ASSOCIATED_USER_ID_FIELD_NAME)
                    && jsonObject.containsKey(CartItem.ASSOCIATED_USER_EMAIL_FIELD_NAME)
                    && jsonObject.containsKey(CartItem.SCHEDULE_FIELD_NAME)) {

                List<Map<String,Object>> scheduleList = (List <Map<String,Object>>) jsonObject.get(CartItem.SCHEDULE_FIELD_NAME);
                List<CartSchedule> schedule = parseJSONCartScheduleList(scheduleList);

                cartItemList.add(CartItemFactory.createCartItem(
                        jsonObject.get(CartItem.NAME_FIELD_NAME).toString(),
                        jsonObject.get(CartItem.TYPE_FIELD_NAME).toString(),
                        Integer.parseInt(jsonObject.get(CartItem.EXPIRY_DAYS_FIELD_NAME).toString()),
                        new User(jsonObject.get(CartItem.ASSOCIATED_USER_ID_FIELD_NAME).toString()),
                        Integer.parseInt(jsonObject.get(CartItem.QUANTITY_FIELD_NAME).toString()),
                        jsonObject.get(Item.STORAGE_LOCATION_FIELD_NAME).toString(),
                        schedule
                ));

            }
        }

        return cartItemList;
    }

    /**
     * This parse the schedule list which is stored as a json document
     * @param scheduleList the json representation of the schedule list
     * @return a proper schedule list
     */
    public static List<ItemUseSchedule> parseJSONScheduleList(List<Map<String,Object>> scheduleList) {
        List<ItemUseSchedule> schedule = new ArrayList<>();
        for (var scheduleJsonObject : scheduleList) {
            if (scheduleJsonObject.containsKey(Schedule.SCHEDULE_DATE_FIELD_NAME)
                    && scheduleJsonObject.containsKey(Schedule.SCHEDULE_QUANTITY_FIELD_NAME)
                    && scheduleJsonObject.containsKey(Schedule.SCHEDULE_ID_FIELD_NAME)
                    && scheduleJsonObject.containsKey(Schedule.SCHEDULE_UEMAIL_FIELD_NAME)
                    && scheduleJsonObject.containsKey(Schedule.SCHEDULE_UID_FIELD_NAME)) {

                Date date = Calendar.getInstance().getTime();

                try {
                    date = (ItemUseSchedule.df).parse((String) scheduleJsonObject.get(Schedule.SCHEDULE_DATE_FIELD_NAME));

                } catch(ParseException e) {
                    e.printStackTrace();
                }
                Log.i("DEBUG", "schedule id parsing: " + scheduleJsonObject.get(Schedule.SCHEDULE_ID_FIELD_NAME));
                schedule.add(new ItemUseSchedule(date,
                        Integer.parseInt(scheduleJsonObject.get(Schedule.SCHEDULE_QUANTITY_FIELD_NAME).toString()),
                        (String) scheduleJsonObject.get(Schedule.SCHEDULE_UEMAIL_FIELD_NAME),
                        (String) scheduleJsonObject.get(Schedule.SCHEDULE_UID_FIELD_NAME),
                        (String) scheduleJsonObject.get(Schedule.SCHEDULE_ID_FIELD_NAME)));
            }
        }
        return schedule;
    }

    /**
     * This parse the schedule list which is stored as a json document
     * @param scheduleList the json representation of the schedule list
     * @return a proper schedule list
     */
    public static List<CartSchedule> parseJSONCartScheduleList(List<Map<String,Object>> scheduleList) {
        List<CartSchedule> schedule = new ArrayList<>();
        DateFormat df = new SimpleDateFormat(CartViewModel.DATE_DISPLAY_FORMAT);

        for (var scheduleJsonObject : scheduleList) {
            if (scheduleJsonObject.containsKey(CartSchedule.SCHEDULE_DATE_FIELD_NAME)
                    && scheduleJsonObject.containsKey(CartSchedule.SCHEDULE_QUANTITY_FIELD_NAME)
                    && scheduleJsonObject.containsKey(CartSchedule.SCHEDULE_DAYS_REOCCURRING_FIELD_NAME)
                    && scheduleJsonObject.containsKey(CartSchedule.SCHEDULE_ID_FIELD_NAME)
                    && scheduleJsonObject.containsKey(CartSchedule.SCHEDULE_UEMAIL_FIELD_NAME)
                    && scheduleJsonObject.containsKey(CartSchedule.SCHEDULE_UID_FIELD_NAME)) {

                try {
                    schedule.add(new CartSchedule(
                            df.parse(scheduleJsonObject.get(CartSchedule.SCHEDULE_DATE_FIELD_NAME).toString()),
                            Integer.parseInt(scheduleJsonObject.get(CartSchedule.SCHEDULE_QUANTITY_FIELD_NAME).toString()),
                            Integer.parseInt(scheduleJsonObject.get(CartSchedule.SCHEDULE_DAYS_REOCCURRING_FIELD_NAME).toString()),
                            (String) scheduleJsonObject.get(CartSchedule.SCHEDULE_UEMAIL_FIELD_NAME),
                            (String) scheduleJsonObject.get(CartSchedule.SCHEDULE_UID_FIELD_NAME),
                            scheduleJsonObject.get(CartSchedule.SCHEDULE_ID_FIELD_NAME).toString()));
                } catch (ParseException e) {
                    Log.i("DEBUG","");
                }
            }
        }
        return schedule;
    }
}
