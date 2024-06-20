package com.aaronnguyen.mykitchen.model.kitchen;

import com.aaronnguyen.mykitchen.DAO.FetchListener;
import com.aaronnguyen.mykitchen.model.user.User;

import java.util.HashMap;
import java.util.function.Function;

public interface KitchenUserSubject {
    void attach(KitchenUsersObserver observer);
    void detach(KitchenUsersObserver observer);
    void notifyUserChanges();
    void fetchUserInfo(Function<HashMap<String, User>, Void> callBack);
}
