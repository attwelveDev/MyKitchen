package com.aaronnguyen.mykitchen.model.notification;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The AppExpiryNotification class represents a notification in the app
 * that alerts users about item expiry.
 * @author u7517596 Chengbo Yan
 */
public class AppExpiryNotification implements Notification {
    public static final String MESSAGE = "message";
    public static final String  IMPORTANCE= "importance";
    private String message;
    private int importance;

    public AppExpiryNotification(String message, int importance) {
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

    @Override
    public int compareTo(Notification other) {
        // Sort in descending order of importance
        return Integer.compare(other.getImportance(), this.getImportance());
    }


    public Map<String, Object> toJSONObject() {
        Map<String, Object> jsonObject = new HashMap<>();

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
}
