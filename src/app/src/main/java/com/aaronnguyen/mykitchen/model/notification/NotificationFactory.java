package com.aaronnguyen.mykitchen.model.notification;

/**
 * Factory class for creating various types of notifications.
 * This class contains methods to instantiate different notification
 * objects based on the provided notification type.
 *
 * @authors u7517596 Chengbo Yan
 */
public class NotificationFactory {
    public static AppExpiryNotification createNotification(String msg, int importance) {

        return new AppExpiryNotification(msg, importance);

    }

}
