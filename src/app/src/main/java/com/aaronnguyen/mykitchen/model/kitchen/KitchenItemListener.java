package com.aaronnguyen.mykitchen.model.kitchen;

import com.aaronnguyen.mykitchen.model.Items.CartItems.CartItem;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Item;
import com.aaronnguyen.mykitchen.model.Items.ItemUsage;
import com.aaronnguyen.mykitchen.model.notification.Notification;

import java.util.ArrayList;

public interface KitchenItemListener {
    /**
     * Called when the current list of items in the kitchen is changed
     * @param items the current list of items
     */
    void onItemListUpdateListener(ArrayList<Item> items);

    void onCartItemListUpdateListener(ArrayList<CartItem> items);

    /**
     * Called when some item is used and has been recorded in the database
     * @param itemUsageList the current history of usage
     */
    void onItemUsageListUpdateListener(ArrayList<ItemUsage> itemUsageList);

    /**
     * Called when the notifications are updated
     * @param notifications the current list of notifications
     */
    void onNotificationListener(ArrayList<Notification> notifications);
}
