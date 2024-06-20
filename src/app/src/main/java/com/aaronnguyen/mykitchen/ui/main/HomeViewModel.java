package com.aaronnguyen.mykitchen.ui.main;

import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;

import com.aaronnguyen.mykitchen.DAO.FetchListener;
import com.aaronnguyen.mykitchen.DAO.UserDaoFirebase;
import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.model.user.User;
import com.aaronnguyen.mykitchen.ui.main.Chat.ChatFragment;
import com.aaronnguyen.mykitchen.ui.main.History.HistoryFragment;
import com.aaronnguyen.mykitchen.ui.main.Notification.KitchenNotificationFragment;
import com.aaronnguyen.mykitchen.ui.main.Overview.KitchenOverviewFragment;
import com.aaronnguyen.mykitchen.ui.main.ShoppingCart.ShoppingCartFragment;

public class HomeViewModel extends ViewModel {

    protected Fragment fragment;
    public static int defaultSelected;
    private final Kitchen kitchen;
    private User user;
    private int selected;

    public HomeViewModel(Kitchen kitchen, User currentUser) {
        this.kitchen = kitchen;
        defaultSelected = R.id.navigation_notifications;
        selected = defaultSelected;
        fragment = new KitchenNotificationFragment();

        this.user = currentUser;

        String uid = currentUser.getUid();
        UserDaoFirebase.getInstance().syncUser(uid, new FetchListener() {
            @Override
            public <T> void onFetchSuccess(T data) {
                if(data instanceof User userData) {
                    user.setEmail(userData.getEmail());
                    user.setUserName(userData.getUserName());
                    user.setKitchenIDs(userData.getKitchenIDs());
                    user.setFCM(userData.getFCM());
                }
            }

            @Override
            public void onFetchFailure(Exception exception) {
//                Toast.makeText(getApplication(), R.string.generic_error, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void pickFragment(int itemID) {
        selected = itemID;
        if(itemID == R.id.navigation_overview) {
            fragment = new KitchenOverviewFragment();
        } else if(itemID == R.id.navigation_cart) {
            fragment = new ShoppingCartFragment();
        } else if(itemID == R.id.navigation_notifications) {
            fragment = new KitchenNotificationFragment();
        } else if(itemID == R.id.navigation_chat) {
            fragment = new ChatFragment();
        } else if(itemID == R.id.navigation_history) {
            fragment = new HistoryFragment();
        }
    }

    public int getSelected() {
        return selected;
    }
    public Kitchen getKitchen() {
        return kitchen;
    }
    public User getCurrentUser() {
        return user;
    }
}
