package com.aaronnguyen.mykitchen.model.kitchen;

import com.aaronnguyen.mykitchen.model.user.User;

import java.util.HashMap;

public interface KitchenUsersObserver {
    void notify(HashMap<String, User> usersDictionary);
}
