package com.aaronnguyen.mykitchen.model.Items;

import com.aaronnguyen.mykitchen.model.user.User;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ItemUsage {

    public static final String NAME_FIELD_NAME = "name";
    public static final String USING_DATE = "dateUsed";
    public static final String QUANTITY_FIELD_NAME = "quantity";
    public static final String ASSOCIATED_USER_ID_FIELD_NAME = "uid";
    public static final String ASSOCIATED_USER_EMAIL_FIELD_NAME = "uemail";



    private String itemName;
    private Date dateUsed;
    private User user;
    private int quantityUsed;

    public ItemUsage(String itemName, User user, Date dateUsed, int quantityUsed) {
        this.itemName = itemName;
        this.dateUsed = dateUsed;
        this.quantityUsed = quantityUsed;
        this.user = user;
    }

    public static ItemUsage createItemUsage(String string, Date dateUsed, String string2, User user) {
        return new ItemUsage(string, user,dateUsed,Integer.parseInt(string2));

    }

    public Date getDateUsed() {
        return dateUsed;
    }
    public String getUsername(){return  user.getUserName();}
    public  User getUser(){ return user;}
    public int getQuantityUsed() {
        return quantityUsed;
    }
    public String getName() {
        return itemName;
    }


    public Map<String, Object> toJSONObject() {
        Map<String, Object> jsonObject = new HashMap<>();

        // Create a parser for the date
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        jsonObject.put(NAME_FIELD_NAME, itemName);
        jsonObject.put(USING_DATE, df.format(dateUsed));
        jsonObject.put(QUANTITY_FIELD_NAME,quantityUsed);
        jsonObject.put(ASSOCIATED_USER_ID_FIELD_NAME, user.getUid());
        jsonObject.put(ASSOCIATED_USER_EMAIL_FIELD_NAME, user.getUserName());

        return jsonObject;
    }

    public Date getUsingDate() {
        return dateUsed;
    }

    public double getQuantity() {
        return  quantityUsed;
    }
}
