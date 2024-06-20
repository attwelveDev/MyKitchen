package com.aaronnguyen.mykitchen.Service;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.aaronnguyen.mykitchen.DAO.JSONObjectParser;
import com.aaronnguyen.mykitchen.DAO.KitchenFirebaseDAO;
import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Item;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemFactory;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemUseSchedule;
import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.model.kitchen.KitchenObserver;
import com.aaronnguyen.mykitchen.model.notification.NotificationFactory;
import com.aaronnguyen.mykitchen.model.user.User;
import com.aaronnguyen.mykitchen.ui.main.Overview.OverviewViewModel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
/**
 * Service for checking the expiry of items in a kitchen.
 * Author: Chengbo Yan
 *
 */

public class ExpiryCheckService extends Service {

    public static final String EXTRA_KITCHEN_ID ="extra_kitchen_id";
    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "expiry_notification_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        scheduleAlarm();
        createNotificationChannel();
        Log.i("Service","start checking service");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return Service.START_REDELIVER_INTENT;
        }

        boolean isAlarmTrigger = intent.getBooleanExtra("isAlarmTrigger", false);
        if (isAlarmTrigger) {
            final String kitchenId = intent.getStringExtra(EXTRA_KITCHEN_ID);
            if (kitchenId != null && !kitchenId.isEmpty()) {
                checkSingleKitchen(kitchenId);
                Log.i("ExpiryCheckService", "Alarm triggered check for kitchen: " + kitchenId);
            } else {
                Log.e("ExpiryCheckService", "No kitchen ID provided with alarm trigger.");
            }
        } else {
            Log.i("ExpiryCheckService", "Service started without alarm trigger.");
        }
        final String kitchenId = intent.getStringExtra(EXTRA_KITCHEN_ID);
        checkSingleKitchen(kitchenId);
        Notification notification = createNotification();
        startForeground(NOTIFICATION_ID, notification);
        return START_STICKY;
    }

    private Notification createNotification() {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Expiry Check Running")
                .setContentText("Checking kitchen items...")
                .setSmallIcon(R.drawable.ic_kitchen)
                .setContentIntent(pendingIntent)
                .setTicker("Ticker text")
                .setWhen(System.currentTimeMillis());

        return builder.build();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkSingleKitchen(String kitchenId) {
        Log.i("checkKitchen","check"+kitchenId);

        if (kitchenId == null || kitchenId.isEmpty()) {
            Log.e("ExpiryCheckService", "Invalid kitchen ID provided.");
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(KitchenFirebaseDAO.KITCHEN_COLLECTION_NAME).document(kitchenId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                Kitchen kitchen = new Kitchen(kitchenId, document.getString("name"));
                List<KitchenObserver> observers = new ArrayList<>();

                if(document.contains(KitchenFirebaseDAO.ITEMS_FIELD_NAME)) {
                    updateAllItemList(document, kitchen);
                }


                if (document.get(KitchenFirebaseDAO.NOTIFICATION_FIELD_NAME) != null) {
                    updateAllNotificationList(document, kitchen);
                }

                if (document.get(KitchenFirebaseDAO.OWNER_FIELD_NAME) != null) {
                    Log.i("DEBUG", "Start fetching members in kitchen");
                    List<String> members = (List<String>) document.get(KitchenFirebaseDAO.RESIDENTS_FIELD_NAME);
                    for (String memberId : members) {
                        Log.i("Member ID", memberId);
                        observers.add(new User(memberId));
                    }
                    kitchen.observers = observers;
                }
                kitchen.allKindsNotification();

            } else {
                Log.e("ExpiryCheckService", "Failed to fetch kitchen: ", task.getException());
            }
        });
    }

    private void updateAllNotificationList(DocumentSnapshot document, Kitchen kitchen) {
        Log.i("DEBUG", "Start fetching notification in kitchen");
        ArrayList<com.aaronnguyen.mykitchen.model.notification.Notification> notifications = new ArrayList<>();
        List<Map<String, Object>> NotificationFromFireBase =
                (List<Map<String, Object>>) document.get(KitchenFirebaseDAO.NOTIFICATION_FIELD_NAME);
        for (var jsonObject : NotificationFromFireBase) {
            if (jsonObject.containsKey(com.aaronnguyen.mykitchen.model.notification.Notification.MESSAGE)
                    && jsonObject.containsKey(com.aaronnguyen.mykitchen.model.notification.Notification.IMPORTANCE)) {
                notifications.add(NotificationFactory.createNotification(
                        jsonObject.get(com.aaronnguyen.mykitchen.model.notification.Notification.MESSAGE).toString(),
                        Integer.parseInt(jsonObject.get(com.aaronnguyen.mykitchen.model.notification.Notification.IMPORTANCE).toString()))
                );
            }
        }
        Log.i("get Notification list",notifications.toString());
        kitchen.setNotifications(notifications);
    }

    private void updateAllItemList(DocumentSnapshot document, Kitchen kitchen) {
        ArrayList<Item> itemList = new ArrayList<>();
        List<Map<String, Object>> itemsFromFireBase = (List<Map<String, Object>>) document.get(KitchenFirebaseDAO.ITEMS_FIELD_NAME);
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
                    List<ItemUseSchedule> schedule = JSONObjectParser.parseJSONScheduleList(scheduleList);

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
                    Log.e("ExpiryCheckService", "Failed to parse item details", e);
                }
            }
            Log.i("ExpiryCheckService", "Successfully checked and parsed items: " + itemList.size());
        }

//        kitchen.itemList = itemList;
        kitchen.setItemList(itemList);
        Log.i("checkService",kitchen.getItemList().toString());
    }


    public static class ExpiryCheckReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent serviceIntent = new Intent(context, ExpiryCheckService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }

    public void scheduleAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, ExpiryCheckReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 48);
        calendar.set(Calendar.SECOND, 30);

        if (Calendar.getInstance().after(calendar)) {
            calendar.add(Calendar.DATE, 1);
        }

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
        Log.i("ExpiryCheckService", "Alarm scheduled successfully");
    }


}
