package com.aaronnguyen.mykitchen.model.kitchen;

public interface KitchenObserver {
    void notifyCloseToExpire(String kitchenName);
    void onExpired(String kitchenName);
    String getUid();
    void notifyCloseToExpireAndExpired(String kitchenName);
    void chatNotify(String message,String username);

    void notifyAddingItem(String message, String kitchenName);
}