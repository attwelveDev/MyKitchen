package com.aaronnguyen.mykitchen.model.kitchen;

import com.aaronnguyen.mykitchen.model.chat.ChatMessage;
import com.aaronnguyen.mykitchen.model.user.User;

public interface KitchenSubject {
    void notifyCloseToExpire();
    void notifyExpired();
    void notifyCloseToExpireAndExpired();
    void notifyAddingItem(String message);
    void notifyChatToOthers(ChatMessage chatMessage, User currentUser);
    }