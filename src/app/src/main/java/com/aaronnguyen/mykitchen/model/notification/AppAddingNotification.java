package com.aaronnguyen.mykitchen.model.notification;

import com.aaronnguyen.mykitchen.ui.main.Overview.OverviewViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The AppAddingNotification class represents a notification in the app, it is
 *  created when user have add an item in the kitchen
 * with a message and an importance level. It implements the Notification interface.
 * @author u7517596 Chengbo Yan
 */
public class AppAddingNotification implements Notification{
    private String message;
    private int importance;

    public AppAddingNotification(String message, int importance) {
        this.message = message;
        this.importance = importance;
    }

    @Override
    public int getImportance() {
        return importance;
    }

    @Override
    public String getText() {
        return message;
    }

    public Map<String, Object> toJSONObject() {
        Map<String, Object> jsonObject = new HashMap<>();

        DateFormat df = new SimpleDateFormat(OverviewViewModel.DATE_DISPLAY_FORMAT);
        // Populate the hashmap
        jsonObject.put(MESSAGE, message);
        jsonObject.put(IMPORTANCE, String.valueOf(importance));
        return jsonObject;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null ) {
            return false;
        }
        Notification other = (Notification) obj;
        return message.equals(other.getText());
    }
    @Override
    public int hashCode() {
        return Objects.hash(message, importance);
    }

    /**
     * Compares this notification with another based on importance.
     *
     * @param other the notification to compare with
     * @return a negative integer, zero, or a positive integer as this notification
     *         is less than, equal to, or greater than the specified notification
     */
    public int compareTo(Notification other) {
        return Integer.compare(other.getImportance(), this.getImportance());
    }
}
