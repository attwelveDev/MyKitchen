package com.aaronnguyen.mykitchen.model.Items.CartItems;

import com.aaronnguyen.mykitchen.model.Items.GeneralItem;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Item;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemFactory;
import com.aaronnguyen.mykitchen.model.user.User;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Class for items in the shopping cart
 * @author u7648367 Ruixian Wu (adapted from u7643339 Isaac Leong)
 */
public abstract class CartItem extends GeneralItem {
    public static final String EXPIRY_DAYS_FIELD_NAME = "expiry days";
    public static final String SCHEDULE_FIELD_NAME = "schedule";

    int expiryDays;
    List<CartSchedule> schedule;


    public CartItem(String name, int expiryDays, User associatedUser, int quantity, String storageLocation, List<CartSchedule> schedule) {
        super(name, associatedUser, quantity, storageLocation);
        this.expiryDays = expiryDays;
        this.schedule = schedule;
    }

    public int getExpiryDays() {
        return expiryDays;
    }

    public List<CartSchedule> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<CartSchedule> newSchedule) {
        this.schedule = newSchedule;
    }
    public void setExpiryDays(int newExpiryDays) {
        this.expiryDays = newExpiryDays;

    }

    /**
     * This returns a string depending on the type of the object
     */
    public String getTypeString() {
        // TODO: Refactor this to align with the open and close principle
        if(this instanceof CartProtein) return "Protein";
        else if(this instanceof CartVegetable) return "Vegetable";
        else if(this instanceof CartDairy) return "Dairy";
        else if(this instanceof CartFruit) return "Fruit";
        else if(this instanceof CartGrain) return  "Grain";
        else if(this instanceof CartOtherItem) return "others";
        else throw new IllegalArgumentException("Undefined type of items");
    }

    /**
     * Creates an object that allows cartItem to be stored in Firestore
     */
    public Map<String, Object> toJSONObject() {
        Map<String, Object> jsonObject = new HashMap<>();

        // Populate the hashmap
        jsonObject.put(NAME_FIELD_NAME, name);
        jsonObject.put(TYPE_FIELD_NAME, getTypeString());
        jsonObject.put(EXPIRY_DAYS_FIELD_NAME, String.valueOf(getExpiryDays()));
        jsonObject.put(QUANTITY_FIELD_NAME, String.valueOf(quantity));
        jsonObject.put(STORAGE_LOCATION_FIELD_NAME, storageLocation);
        jsonObject.put(ASSOCIATED_USER_ID_FIELD_NAME, associatedUser.getUid());
        jsonObject.put(ASSOCIATED_USER_EMAIL_FIELD_NAME, associatedUser.getEmail());
        ArrayList<Map<String,Object>> scheduleJson = new ArrayList<>();
        for (CartSchedule schedule : getSchedule()) {
            scheduleJson.add(schedule.toJsonObject());
        }
        jsonObject.put(SCHEDULE_FIELD_NAME,scheduleJson);

        return jsonObject;
    }

    public Item buyCartItem(User user) {
        Date expiryDate = Calendar.getInstance().getTime();
        expiryDate.setTime(Calendar.getInstance().getTimeInMillis() + expiryDays*24*60*60*1000);
        return ItemFactory.createItem(
                name,
                getTypeString(),
                expiryDate,
                Calendar.getInstance().getTime(),
                user,
                quantity,
                storageLocation,
                new ArrayList<>()
        );
    }

    public boolean noSchedules() {
        return schedule.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        try {
            CartItem item = (CartItem) o;
            return quantity == item.quantity
                    && getTypeString().equals(item.getTypeString())
                    && Objects.equals(name, item.name)
                    && expiryDays == item.expiryDays
                    && Objects.equals(storageLocation, item.storageLocation)
                    && Objects.equals(schedule, item.schedule);
        } catch(ClassCastException e) {
            return false;
        }
    }

    public boolean perfectEquals(CartItem item) {
        return quantity == item.quantity
                && Objects.equals(name, item.name)
                && Objects.equals(getTypeString(), item.getTypeString())
                && expiryDays == item.expiryDays
                && Objects.equals(storageLocation, item.storageLocation)
                && Objects.equals(schedule, item.schedule)
                && Objects.equals(associatedUser.getUid(), item.getAssociatedUser().getUid());


    }
}
