package com.aaronnguyen.mykitchen.Service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.ScheduleAlarms.CartScheduleAlarm.CartScheduleAlarm;
import com.aaronnguyen.mykitchen.ScheduleAlarms.ItemUseScheduleAlarm.ItemUseScheduleAlarm;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for handling Firebase Cloud Messaging (FCM) notifications.
 * This class manages the creation of notification channels, handling incoming messages,
 * and sending notifications to the user.
 * Author: Chengbo Yan
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final ConcurrentHashMap<String, List<String>> notificationMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> notificationCount = new ConcurrentHashMap<>();
    private final int NOTIFICATION_THRESHOLD = 4;
    private final int TIME_THRESHOLD_MINUTES = 4;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("ExpiryKitchenNotify", "Expiry Notifications", NotificationManager.IMPORTANCE_HIGH);
            createNotificationChannel("KitchenChat", "Kitchen Chat Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            createNotificationChannel("Schedule", "Schedule Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            createNotificationChannel("addingItemNotification", "Adding Item Notifications", NotificationManager.IMPORTANCE_LOW);

        }

    }

    /**
     * Creates a notification channel with the specified ID, name, and importance level.
     *
     * @param channelId the ID of the notification channel
     * @param channelName the name of the notification channel
     * @param importance the importance level of the notification channel
     */
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        channel.setDescription(channelName);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        String type = remoteMessage.getData().get("type");

        if (type != null) {
            switch (type) {
                case "ExpiryKitchenNotify":
                    sendNotification(title, body, "ExpiryKitchenNotify");
                    break;
                case "GroupChat":
                    sendNotification(title, body, "KitchenChat");
                    break;
                case "Schedule":
                    boolean scheduleType = false;
                    if (body.split("/")[1].equals("true")) {
                        ItemUseScheduleAlarm.cancelItemUseSchedule(Integer.parseInt(body.split("/")[0]), this);
                    } else {
                        CartScheduleAlarm.cancelCartSchedule(Integer.parseInt(body.split("/")[0]), this);
                    }
                    break;
                case "addingItemNotification":
                    storeAddItemNotification(title, body);
                    break;
                default:
                    sendNotification(title, body, "ExpiryKitchenNotify");// Fallback to default channel
                    break;
            }
        }
        else {
            sendNotification(title, body, "ExpiryKitchenNotify");
        }
    }

    /**
     * Stores the "adding item" notification and sends batched notifications when a threshold is reached.
     *
     * @param title the title of the notification
     * @param body the body of the notification
     */
    private void storeAddItemNotification(String title, String body) {
        String key = "addingItemNotification";
        notificationMap.computeIfAbsent(key, k -> new ArrayList<>()).add(title + ": " + body);
        notificationCount.merge(key, 1, Integer::sum);

        if (notificationCount.get(key) >= NOTIFICATION_THRESHOLD) {
            sendBatchedAddItemNotifications();
            notificationCount.put(key, 0);
        }
    }

    private void sendBatchedAddItemNotifications() {
        String key = "addingItemNotification";
        List<String> notifications = notificationMap.get(key);
        if (notifications != null && !notifications.isEmpty()) {
            StringBuilder messageBuilder = new StringBuilder();
            notifications.forEach(notification -> messageBuilder.append(notification).append("\n"));

            String finalMessage = messageBuilder.toString();
            sendNotifications("Add Item Notification", finalMessage, key);
            notifications.clear();
        }
    }

    /**
     * Sends a single notification.
     *
     * @param title the title of the notification
     * @param body the body of the notification
     * @param channelId the ID of the notification channel
     */

    private void sendNotification(String title, String body, String channelId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_kitchen)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(getNotificationId(), builder.build());  // Ensure unique ID for each notification
    }

    /**
     * Sends a batched notification.
     *
     * @param title the title of the notification
     * @param body the body of the notification
     * @param channelId the ID of the notification channel
     */
    private void sendNotifications(String title, String body, String channelId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_kitchen)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(getNotificationId(), builder.build());
    }

    private int getNotificationId() {
        return (int) System.currentTimeMillis() % 10000;  // Ensures a unique ID for each notification

    }



    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("FCM", "The new token: " + token);
        saveTokenToFirebase(token);
    }

    private void saveTokenToFirebase(String token) {
        String userId = getUserId();
        if (userId != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference userDoc = db.collection("users").document(userId);
            userDoc.update("fcmToken", token)
                    .addOnSuccessListener(aVoid -> Log.d("FCM Token", "Token successfully updated"))
                    .addOnFailureListener(e -> Log.e("FCM Token", "Failed to update token", e));
        }
    }

    private String getUserId() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            return mAuth.getCurrentUser().getUid();
        } else {
            return null;
        }
    }

}

