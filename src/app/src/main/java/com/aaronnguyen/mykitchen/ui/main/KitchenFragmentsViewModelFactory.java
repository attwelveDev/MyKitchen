package com.aaronnguyen.mykitchen.ui.main;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.model.user.User;
import com.aaronnguyen.mykitchen.ui.main.Chat.ChatViewModel;
import com.aaronnguyen.mykitchen.ui.main.History.HistoryViewModel;
import com.aaronnguyen.mykitchen.ui.main.Notification.NotificationViewModel;
import com.aaronnguyen.mykitchen.ui.main.Overview.OverviewViewModel;
import com.aaronnguyen.mykitchen.ui.main.ShoppingCart.CartViewModel;

public class KitchenFragmentsViewModelFactory implements ViewModelProvider.Factory{
    public enum TargetViewModelCode {
        Chat,
        History,
        Notification,
        Overview,
        ShoppingCart,
        Home
    }
    private TargetViewModelCode code;
    private Kitchen kitchen;
    private User currentUser;

    public KitchenFragmentsViewModelFactory(Kitchen kitchen, User currentUser, TargetViewModelCode targetCode) {
        this.kitchen = kitchen;
        this.currentUser = currentUser;
        this.code = targetCode;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return switch (code) {
            case Chat -> (T) new ChatViewModel(kitchen, currentUser);
            case History -> (T) new HistoryViewModel(kitchen);
            case Notification -> (T) new NotificationViewModel(kitchen, currentUser);
            case Overview -> (T) new OverviewViewModel(kitchen, currentUser);
            case ShoppingCart -> (T) new CartViewModel(kitchen, currentUser);
            case Home -> (T) new HomeViewModel(kitchen, currentUser);
        };
    }
}
