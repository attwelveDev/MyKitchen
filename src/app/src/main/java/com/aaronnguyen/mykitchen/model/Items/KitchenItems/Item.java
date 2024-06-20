package com.aaronnguyen.mykitchen.model.Items.KitchenItems;

import android.util.Log;

import androidx.annotation.NonNull;

import com.aaronnguyen.mykitchen.model.Items.GeneralItem;
import com.aaronnguyen.mykitchen.ui.main.Overview.OverviewViewModel;
import com.aaronnguyen.mykitchen.model.user.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public abstract class Item extends GeneralItem {
    public static final String BOUGHT_DATE_FIELD_NAME = "bought date";
    public static final String EXPIRY_DATE_FIELD_NAME = "expiry date";
    public static final String SCHEDULE_FIELD_NAME = "schedule";

    public static DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

    Date expiryDate;
    Date boughtDate;
    final String userEmail;
    List<ItemUseSchedule> schedule;

    public Item(String name, Date expiryDate, Date boughtDate, User associatedUser, int quantity, String storageLocation, List<ItemUseSchedule> schedule) {
        super(name, associatedUser, quantity, storageLocation);
        this.expiryDate = expiryDate;
        this.boughtDate = boughtDate;
        this.userEmail = associatedUser.getEmail();
        this.schedule = schedule;
    }

    /**
     * This returns a string depending on the type of the object
     */
    public String getTypeString() {
        if(this instanceof Protein) return "Protein";
        else if(this instanceof Vegetable) return "Vegetable";
        else if(this instanceof Dairy) return "Dairy";
        else if(this instanceof Fruit) return "Fruit";
        else if(this instanceof Grain) return  "Grain";
        else if(this instanceof OtherItem) return "others";
        else throw new IllegalArgumentException("Undefined type of items");
    }

    @NonNull
    @Override
    public String toString() {
        String type = getTypeString();
        return name + " (" + type + ")" + " Bought on " + df.format(boughtDate) + " quantity: " + getQuantity();
    }


    public Date getExpiryDate() {
        return expiryDate;
    }

    public Date getBoughtDate() {
        return boughtDate;
    }
    public String getUserEmail() {
        return userEmail;
    }
    public List<ItemUseSchedule> getSchedule() {
        return schedule;
    }

    public boolean isCloseToExpiry() {
        if (expiryDate == null || boughtDate == null) {
            return false;
        }

        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);
        todayCalendar.set(Calendar.MILLISECOND, 0);
        Date today = todayCalendar.getTime();

        Calendar expiryCalendar = Calendar.getInstance();
        expiryCalendar.setTime(expiryDate);
        expiryCalendar.set(Calendar.HOUR_OF_DAY, 0);
        expiryCalendar.set(Calendar.MINUTE, 0);
        expiryCalendar.set(Calendar.SECOND, 0);
        expiryCalendar.set(Calendar.MILLISECOND, 0);
        Date expiry = expiryCalendar.getTime();

        long diff = expiry.getTime() - today.getTime();
        double days = Math.ceil((double) TimeUnit.MILLISECONDS.toDays(diff));
        return days <= 3 && days >= 0;

    }
    public  int DaysBeforeExpiry() {
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);
        todayCalendar.set(Calendar.MILLISECOND, 0);
        Date today = todayCalendar.getTime();

        Calendar expiryCalendar = Calendar.getInstance();
        expiryCalendar.setTime(expiryDate);
        expiryCalendar.set(Calendar.HOUR_OF_DAY, 0);
        expiryCalendar.set(Calendar.MINUTE, 0);
        expiryCalendar.set(Calendar.SECOND, 0);
        expiryCalendar.set(Calendar.MILLISECOND, 0);
        Date expiry = expiryCalendar.getTime();

        long diff = expiry.getTime() - today.getTime();
        double days = Math.ceil((double) TimeUnit.MILLISECONDS.toDays(diff));
        return (int) days;
    }

    public boolean OutOfExpiry() {
        if (expiryDate == null || boughtDate == null) {
            return false;
        }
        Calendar todayCalendar = Calendar.getInstance();
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0);
        todayCalendar.set(Calendar.MINUTE, 0);
        todayCalendar.set(Calendar.SECOND, 0);
        todayCalendar.set(Calendar.MILLISECOND, 0);
        Date today = todayCalendar.getTime();

        Calendar expiryCalendar = Calendar.getInstance();
        expiryCalendar.setTime(expiryDate);
        expiryCalendar.set(Calendar.HOUR_OF_DAY, 0);
        expiryCalendar.set(Calendar.MINUTE, 0);
        expiryCalendar.set(Calendar.SECOND, 0);
        expiryCalendar.set(Calendar.MILLISECOND, 0);
        Date expiry = expiryCalendar.getTime();

        long diff = expiry.getTime() - today.getTime();
        double days = Math.ceil((double) TimeUnit.MILLISECONDS.toDays(diff));
        return days <0;
    }

    /**
     * This creates an object that allows to be stored in Firestore
     * @return the JSON formatted object for firebase
     */
    public Map<String, Object> toJSONObject() {
        Map<String, Object> jsonObject = new HashMap<>();

        // Create a parser for the date
        DateFormat df = new SimpleDateFormat(OverviewViewModel.DATE_DISPLAY_FORMAT);

        // Populate the hashmap
        jsonObject.put(NAME_FIELD_NAME, name);
        jsonObject.put(TYPE_FIELD_NAME, getTypeString());
        jsonObject.put(BOUGHT_DATE_FIELD_NAME, df.format(getBoughtDate()));
        jsonObject.put(EXPIRY_DATE_FIELD_NAME, df.format(getExpiryDate()));

        ArrayList<Map<String,Object>> scheduleJson = new ArrayList<>();
        for (ItemUseSchedule schedule : getSchedule()) {
            scheduleJson.add(schedule.toJsonObject());
        }

        jsonObject.put(QUANTITY_FIELD_NAME, String.valueOf(quantity));
        jsonObject.put(STORAGE_LOCATION_FIELD_NAME, storageLocation);
        jsonObject.put(ASSOCIATED_USER_ID_FIELD_NAME, associatedUser.getUid());
        jsonObject.put(ASSOCIATED_USER_EMAIL_FIELD_NAME, userEmail);
        jsonObject.put(SCHEDULE_FIELD_NAME,scheduleJson);

        Log.i("DEBUG", "final product is: " + jsonObject);
        return jsonObject;
    }

    public void setSchedule(List<ItemUseSchedule> schedule) {
        this.schedule = schedule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        DateFormat df = new SimpleDateFormat(OverviewViewModel.DATE_DISPLAY_FORMAT);
        return quantity == item.quantity
                && Objects.equals(getTypeString(), item.getTypeString())
                && Objects.equals(name, item.name)
                && Objects.equals(df.format(expiryDate), df.format(expiryDate))
                && Objects.equals(df.format(boughtDate), df.format(boughtDate))
                && Objects.equals(storageLocation, item.storageLocation)
                && Objects.equals(schedule, item.schedule);
    }

    public boolean perfectEquals(Item item) {
        return quantity == item.quantity
                && Objects.equals(name, item.name)
                && Objects.equals(expiryDate, item.expiryDate)
                && Objects.equals(boughtDate, item.boughtDate)
                && Objects.equals(associatedUser.getUid(), item.getAssociatedUser().getUid())
                && Objects.equals(storageLocation, item.storageLocation)
                && Objects.equals(schedule, item.schedule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, expiryDate, boughtDate, quantity, storageLocation);
    }

}
