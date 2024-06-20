package com.aaronnguyen.mykitchen.ui.main.Notification;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.model.notification.AppExpiryNotification;
import com.aaronnguyen.mykitchen.model.notification.Notification;
import com.aaronnguyen.mykitchen.model.user.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

/**
 * Adapter for managing the notifications in a kitchen.
 * @author u7515796 ChengboYan
 */
public class NotificationAdapter extends ArrayAdapter<Notification> {
    private Context context;
    private ArrayList<Notification> notifications;
    private  NotificationViewModel notificationViewModel;
    private  KitchenNotificationFragment notificationFragment;
    private Set<Notification> removedNotifications = new HashSet<>();
    private User currentUser;
    private Kitchen currentKitchen;


    public NotificationAdapter(Context context, ArrayList<Notification> notifications,
                               KitchenNotificationFragment kitchenNotificationFragment) {
        super(context, 0);
        this.notifications = notifications;
        this.notificationFragment = kitchenNotificationFragment;
        this.context = context;
        this.currentUser = kitchenNotificationFragment.notificationViewModel.currentUser;
        this.currentKitchen = kitchenNotificationFragment.notificationViewModel.currentKitchen;

        loadRemovedNotifications();
        for(Notification removedNotification: removedNotifications){
            notifications.remove(removedNotification);
            saveRemovedNotificationsToFile(removedNotification);
            notificationFragment.deleteNotification(removedNotification);
            notifyDataSetChanged();
        }
    }

    private void loadRemovedNotifications() {
        String fileName = currentUser.getUid() + "_" +
                currentKitchen.getKitchenID() + "_removed_notifications.json";
        File file = new File(context.getFilesDir(), fileName);
        if (file.exists()) {
            try (FileReader fileReader = new FileReader(file);
                 BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                String line;
                StringBuilder json = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    json.append(line);
                }
                boolean flag = false;
                JSONArray jsonArray = new JSONArray(json.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    long timestamp = jsonObject.getLong("timestamp");
                    if(!flag){
                        if (isToday(timestamp)) {
                            Notification notification = new AppExpiryNotification(jsonObject.getString("text"), jsonObject.getInt("importance"));
                            removedNotifications.add(notification);
                            flag = true;
                        }
                        else break;
                    }
                    else {
                        Notification notification = new AppExpiryNotification(jsonObject.getString("text"), jsonObject.getInt("importance"));
                        removedNotifications.add(notification);
                    }


                }
            } catch (Exception e) {
                Log.e("NotificationAdapter", "Error loading removed notifications", e);
            }
        }
    }

    @Override
    public int getCount() {
        return notifications.size();
    }

    @Override
    public Notification getItem(int position) {
        return notifications.get(position);
    }

    @NonNull
    @Override

    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Notification notification = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.notification_item, parent, false);
        }

        TextView notificationText = convertView.findViewById(R.id.notification_text);
        notificationText.setText(notification.getText());

        Button removeButton = convertView.findViewById(R.id.remove_button);
        removeButton.setOnClickListener(v -> {
            notifications.remove(notification);
            saveRemovedNotificationsToFile(notification);
            notificationFragment.deleteNotification(notification);
            notifyDataSetChanged();
        });

        ConstraintLayout background = convertView.findViewById(R.id.notification_background);
        View importanceIndicator = convertView.findViewById(R.id.importance_indicator);

        if (notification.getImportance() == 3) {
            background.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.background_notification_important));
            importanceIndicator.setBackgroundColor(getContext().getResources().getColor(R.color.important_notification_color, getContext().getTheme()));
            notificationText.setTextColor(getContext().getResources().getColor(R.color.important_notification_color, getContext().getTheme()));
        } else if (notification.getImportance() == 2) {
            background.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.background_notification_medium_important));
            importanceIndicator.setBackgroundColor(getContext().getResources().getColor(R.color.medium_important_notification_color, getContext().getTheme()));
            notificationText.setTextColor(getContext().getResources().getColor(R.color.medium_important_notification_color, getContext().getTheme()));
        }
        else {
            background.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.background_notification));
            importanceIndicator.setBackgroundColor(getContext().getResources().getColor(R.color.on_surface, getContext().getTheme()));
            notificationText.setTextColor(getContext().getResources().getColor(R.color.on_surface, getContext().getTheme()));
        }


        return convertView;
    }

    private void saveRemovedNotificationsToFile(Notification notification) {
        String fileName = currentUser.getUid() + "_" +
                currentKitchen.getKitchenID() + "_removed_notifications.json";
        File file = new File(context.getFilesDir(), fileName);
        removedNotifications.add(notification);

        JSONArray jsonArray = new JSONArray();
        for (Notification n : removedNotifications) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("text", n.getText());
                jsonObject.put("importance", n.getImportance());
                jsonObject.put("timestamp", System.currentTimeMillis());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                Log.e("RemoveNotification", "Error creating JSON object", e);
            }
        }

        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(jsonArray.toString());
        } catch (IOException e) {
            Log.e("RemoveNotification", "Error writing to file", e);
        }
    }

    private boolean isToday(long timestamp) {
        Calendar todayCalendar = Calendar.getInstance();
        int todayYear = todayCalendar.get(Calendar.YEAR);
        int todayMonth = todayCalendar.get(Calendar.MONTH);
        int todayDay = todayCalendar.get(Calendar.DAY_OF_MONTH);

        Calendar notificationCalendar = Calendar.getInstance();
        notificationCalendar.setTimeInMillis(timestamp);
        int notificationYear = notificationCalendar.get(Calendar.YEAR);
        int notificationMonth = notificationCalendar.get(Calendar.MONTH);
        int notificationDay = notificationCalendar.get(Calendar.DAY_OF_MONTH);

        return todayYear == notificationYear && todayMonth == notificationMonth && todayDay == notificationDay;
    }


}
