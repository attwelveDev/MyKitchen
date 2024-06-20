package com.aaronnguyen.mykitchen.ui.main.Notification;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.model.notification.Notification;
import com.aaronnguyen.mykitchen.model.user.User;

import java.util.ArrayList;
import java.util.Collections;

/**
 * ViewModel for managing the notifications in a kitchen.
 * @author u7515796 ChengboYan
 */
public class NotificationViewModel extends ViewModel {
    User currentUser;
    Kitchen currentKitchen;
    private final MutableLiveData<ArrayList<Notification>> notificationsLiveData;

    public NotificationViewModel(Kitchen kitchen, User user) {
        this.currentKitchen = kitchen;
        this.currentUser = user;
        notificationsLiveData = new MutableLiveData<>(new ArrayList<>());
    }

    public void startUpFetch() {
        refreshList(currentKitchen.getNotifications());
    }

    public void refreshList(ArrayList<Notification> notifications) {
        Collections.sort(notifications);
        notificationsLiveData.setValue(notifications);
    }

    public void deleteNotification(Notification notification) {
        currentKitchen.deleteNotification(notification);
    }

    public MutableLiveData<ArrayList<Notification>> getNotifications() {
        return notificationsLiveData;
    }
}