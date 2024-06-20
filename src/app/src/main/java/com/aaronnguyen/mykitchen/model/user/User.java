package com.aaronnguyen.mykitchen.model.user;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.aaronnguyen.mykitchen.DAO.UserDaoFirebase;
import com.aaronnguyen.mykitchen.Service.ExpiryCheckService;
import com.aaronnguyen.mykitchen.model.kitchen.KitchenObserver;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class User implements KitchenObserver, Serializable {
    private String userName = null;
    private String email;
    private List<String> kitchenIDs;
    private final String uid;
    private Uri profilePicture;
    private String FCM;

    /**
     * @deprecated This constructor is deprecated as we can always fetch from cloud. Consider using the constructor that only uses user id
     * @param email The email of the user
     * @param uid The ID for the user
     */
    @Deprecated
    public User(String email, String uid) {
        this.email = email;
        this.uid = uid;

        kitchenIDs = new ArrayList<>();
    }

    /**
     * <p>
     *  This constructor should be used carefully since th[e field {@code FCM} will not be initialised.
     *  <STRONG>
     *  This will cause this user not be able to send any notifications.
     *  </STRONG>
     *  Therefore, it is suitable if this user is only a fake user and would not need the full functionality as a normal user object.
     *  </p>
     *  <p>
     *  An example usage of this constructor is the sign up since the login will have no information about the user.
     *  However, this should be pretty much the only case where you would want to use this constructor.
     * </p>
     * @param uid The user id
     * @param userName The user name
     * @param email The user email
     * @param kitchenIDs The kitchens of the user
     */
    public User(String uid, String userName, String email, List<String> kitchenIDs) {
        this.uid = uid;
        this.userName = userName;
        this.email = email;
        this.kitchenIDs = kitchenIDs;
    }

    public User(String uid, String userName, String email, List<String> kitchenIDs, String FCM) {
        this.uid = uid;
        this.userName = userName;
        this.email = email;
        this.kitchenIDs = kitchenIDs;
        this.FCM = FCM;
    }

    /**
     * @deprecated
     * This construct the user by fetching the data online.
     * @param uid The user id
     */
    @Deprecated
    public User(String uid) {
        this.uid = uid;
//        fetchUserInfo(uid, this);
    }

    public static void startExpiryCheckService(Context context, String id) {
        Intent serviceIntent = new Intent(context, ExpiryCheckService.class);
        serviceIntent.putExtra(ExpiryCheckService.EXTRA_KITCHEN_ID, id);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }

    /**
     * Notifies the user that an item is close to expiring.
     * Author: Chengbo Yan
     *
     */
    public void notifyCloseToExpire(String kitchenName) {
        if(FCM == null)
            return;
        sendNotificationToDevice(FCM,  kitchenName,"CloseToExpire");
    }

    /**
     * Handles the action when an item has expired.
     * Author: Chengbo Yan
     *
     */
    @Override
    public void onExpired(String kitchenName) {
        if(FCM == null)
            return;
        sendNotificationToDevice(FCM, kitchenName,"OnExpired");
    }

    /**
     * Notifies the user that some items have expired and some are close to expiring.
     * Author: Chengbo Yan
     *
     */
    @Override
    public void notifyCloseToExpireAndExpired(String kichenName){
        if(FCM == null)
            return;
        sendNotificationToDevice(FCM, kichenName, "ExpiredAndCloseToExpire");
    }

    /**
     * Sends a chat notification to the user.
     * Author: Chengbo Yan
     *
     * @param message The chat message.
     * @param userName The name of the user sending the message.
     */
    @Override
    public void chatNotify(String message,String userName) {
        if(FCM == null)
            return;
        sendChatNotification(FCM, userName, message);
    }

    /**
     * Notifies the user about adding an item.
     * Author: Chengbo Yan
     *
     * @param message The notification message.
     */
    @Override
    public void notifyAddingItem(String message,String kichenName) {
        if(FCM == null)
            return;
        sendNotificationToDevice(FCM,kichenName, message);
    }


    /**
     * Sends a chat notification to the user's device using Firebase Cloud Messaging (FCM).
     * Author: Chengbo Yan
     * @param token The FCM token of the device.
     * @param userNameSendNotification The name of the user sending the notification.
     * @param chatMessage The chat message to be sent.
     */
    private void sendChatNotification(String token, String userNameSendNotification, String chatMessage) {
        Log.i("GroupChat","group");
        String title = "Group Chat";
        String message = userNameSendNotification +" send "  + chatMessage;

        String finalTitle = title;
        String finalMessage = message;
        String finalChannelId = "KitchenChat";
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


                try (OutputStream os = conn.getOutputStream()) {
                    os.write(messageJSON.toString().getBytes(StandardCharsets.UTF_8));
                }

                int responseCode = conn.getResponseCode();
                Log.i("FCM Chat", "Response Code : " + responseCode);
                Log.i("FCM Chat", "Response Message : " + conn.getResponseMessage());

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        Log.i("FCM Chat Response", response.toString());
                    }
                } else {
                    Log.e("FCM", "Failed to send notification");
                }
            } catch (Exception e) {
                Log.e("FCM", "Error sending notification", e);
            }
        });
    }

    /**
     * Sends a notification to the user's device using Firebase Cloud Messaging (FCM).
     * Author: Chengbo Yan
     *
     * @param token The FCM token of the device.
     * @param type The type of notification (e.g., "CloseToExpire", "OnExpired").
     */
    private void sendNotificationToDevice(String token,String kitchenName, String type) {
        String title = null;
        String message = null;
        String channelId = null;

        Log.i("send notification",   " device");
        if(type.equals("CloseToExpire")){
            title =  "Close To Expiry Alert!";
            message = "Check your kitchen "+ kitchenName + ", some items are going to expire ";
            channelId = "ExpiryKitchenNotify";
        }
        else if (type.equals("OnExpired")) {
            title = "Expiry Alert!";
            message = "Check your kitchen "+ kitchenName + ", some items have expired ";
            channelId = "ExpiryKitchenNotify";

        }
        else if (type.equals("ExpiredAndCloseToExpire"))
        {
            title = "Expiry Alert!";
            message = "Check your kitchen "+ kitchenName + ", some items have " +
                    "expired and some items are going to expire";
            channelId = "ExpiryKitchenNotify"; // Default channel ID
        }
        else {
            title = "Adding Item";
            message = type + " in kitchen " + kitchenName;
            channelId = "addingItemNotification";
        }
        String finalTitle = title;
        String finalMessage = message;
        String finalChannelId = channelId;
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
                messageJSON.put("data", new JSONObject()
                        .put("type", finalChannelId));

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

    // Getters and setters for email, kitchens, and uid
    public String getEmail() {
        return email;
    }

    public List<String> getKitchenIDs() {
        return kitchenIDs;
    }

    public String getUid() {
        return uid;
    }

    public void setProfilePicture(Uri profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Uri getProfilePicture() {
        return profilePicture;
    }

    public String getUserName() {
        if(userName != null)
            return userName;
        return this.uid;
    }
    public String getFCM() {
        return FCM;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setKitchenIDs(List<String> kitchenIDs) {
        this.kitchenIDs = kitchenIDs;
    }
    public void setFCM(String FCM) {
        this.FCM = FCM;
    }

    public Map<String, Object> toJSONObject() {
        Map<String, Object> jsonObject = new HashMap<>();

        jsonObject.put(UserDaoFirebase.KITCHENS_FIELD_NAME, kitchenIDs);
        if(email != null)
            jsonObject.put(UserDaoFirebase.EMAIL_FIELD_NAME, email);
        if(userName != null)
            jsonObject.put(UserDaoFirebase.USER_NAME_FIELD_NAME, userName);
        if(FCM != null)
            jsonObject.put(UserDaoFirebase.FCM_TOKEN_FIELD_NAME, FCM);

        return jsonObject;
    }
}