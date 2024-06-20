package com.aaronnguyen.mykitchen.model.chat;

import android.net.Uri;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * This class represents a chat message.
 * @author u7643339 Isaac Leong
 */
public class ChatMessage {
    public static final String USER_ID_FIELD_NAME = "user id";
    public static final String USER_NAME_FIELD_NAME = "user name";
    public static final String MESSAGE_FIELD_NAME = "message";
    public static final String TIME_FIELD_NAME = "time";
    public static final String MESSAGE_TIME_FORMAT = "MMMM dd, yyyy - hh:mm a";
    private final String userID;
    private final Date date;
    private final String message;
    private String userName;
    private Uri profilePicture;

    public ChatMessage(String userID, Date date, String message, String name) {
        this.userID = userID;
        this.date = date;
        this.message = message;
        this.userName = name;
    }

    public String getUserID() {
        return userID;
    }

    public String getMessageTime() {
        return new SimpleDateFormat(ChatMessage.MESSAGE_TIME_FORMAT, Locale.getDefault()).format(date);
    }

    public String getMessage() {
        return message;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Uri getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(Uri profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Map<String, Object> toJSONObject() {
        Map<String, Object> jsonObject = new HashMap<>();

        jsonObject.put(ChatMessage.USER_ID_FIELD_NAME, this.getUserID());
        jsonObject.put(ChatMessage.USER_NAME_FIELD_NAME, this.getUserName());
        jsonObject.put(ChatMessage.MESSAGE_FIELD_NAME, this.getMessage());
        jsonObject.put(ChatMessage.TIME_FIELD_NAME, this.getMessageTime());

        return jsonObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChatMessage that = (ChatMessage) o;
        return Objects.equals(userID, that.userID) && Objects.equals(date, that.date) && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID, date, message);
    }
}
