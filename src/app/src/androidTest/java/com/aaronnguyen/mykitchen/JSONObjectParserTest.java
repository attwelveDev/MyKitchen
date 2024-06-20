package com.aaronnguyen.mykitchen;

import static com.aaronnguyen.mykitchen.DAO.JSONObjectParser.parseItemList;
import static com.aaronnguyen.mykitchen.DAO.JSONObjectParser.parseJSONCartScheduleList;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.util.Log;

import com.aaronnguyen.mykitchen.DAO.JSONObjectParser;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartItem;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartItemFactory;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartSchedule;
import com.aaronnguyen.mykitchen.model.Items.ItemUsage;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemUseSchedule;
import com.aaronnguyen.mykitchen.model.Items.Schedule;
import com.aaronnguyen.mykitchen.model.notification.AppExpiryNotification;
import com.aaronnguyen.mykitchen.model.notification.Notification;
import com.aaronnguyen.mykitchen.model.notification.NotificationFactory;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Item;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemFactory;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemUseSchedule;
import com.aaronnguyen.mykitchen.model.Items.Schedule;
import com.aaronnguyen.mykitchen.model.user.User;

import org.checkerframework.checker.units.qual.C;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class JSONObjectParserTest {
    User testUser = new User("SGJNESJsdnfjk485usdjk", "username", "user.gmail.com", Collections.emptyList());
    User testUser2 = new User("dFJ34dfm3q45usfwEG", "username2", "username2@gmail.com", Collections.emptyList());

    List<Map<String, Object>> scheduleList = new ArrayList<>();

    @Test
    public void testItemUseScheduleStandard() {
        List<Map<String, Object>> scheduleJsonList = createJsonScheduleList1(false);
        List<ItemUseSchedule> parsedList = JSONObjectParser.parseJSONScheduleList(scheduleJsonList);

        List<ItemUseSchedule> expectedList = createItemUseScheduleList1();
        assertEquals(expectedList,parsedList);

    }

    @Test
    public void testItemUseSchedulePoorlyFormatted() {
        List<Map<String, Object>> scheduleJsonList = createPoorlyFormattedJsonScheduleList(false);
        List<ItemUseSchedule> parsedList = JSONObjectParser.parseJSONScheduleList(scheduleJsonList);

        List<ItemUseSchedule> expectedList = createItemUseSchedulePoorlyFormatted();

        assertEquals(expectedList,parsedList);

    }

    public List<ItemUseSchedule> createItemUseSchedulePoorlyFormatted() {
        List<ItemUseSchedule> expectedList = new ArrayList<>();

        Date date1 = Calendar.getInstance().getTime();
        try {
            date1 = (ItemUseSchedule.df).parse("05/02/2024 12:43");
        } catch (ParseException e) {
            Log.i("TEST", "Poorly formatted date");
        }

        expectedList.add(new ItemUseSchedule(date1, 4, "abc@abc.com", "abc", "a"));


        Date date2 = Calendar.getInstance().getTime();
        try {
            date2 = (ItemUseSchedule.df).parse("10/02/2026 05:20");
        } catch (ParseException e) {
            Log.i("TEST", "Poorly formatted date");
        }

        expectedList.add(new ItemUseSchedule(date2, 435324548,  "jfreafHUFEnfdfkjw@yahoo.com", "WaSsfRGJdSE", "fsdjYYYYfhuiFWFrwGGhfui"));

        return expectedList;
    }


    @Test
    public void testCartScheduleStandard() {
        List<Map<String, Object>> cartScheduleJsonList = createJsonScheduleList1(true);
        List<CartSchedule> parsedList = JSONObjectParser.parseJSONCartScheduleList(cartScheduleJsonList);

        List<CartSchedule> expectedList = createCartScheduleListStandard();

        assertEquals(expectedList,parsedList);


    }

    public List<CartSchedule> createCartScheduleListStandard() {
        List<CartSchedule> expectedList = new ArrayList<>();

        Date date1 = Calendar.getInstance().getTime();
        try {
            date1 = (CartSchedule.df).parse("05/02/2024");
        } catch (ParseException e) {
            Log.i("TEST", "Poorly formatted date");
        }

        expectedList.add(new CartSchedule(date1, 4, 5, "abc@abc.com", "abc", "a"));

        Date date2 = Calendar.getInstance().getTime();
        try {
            date2 = (CartSchedule.df).parse("24/02/2004");
        } catch (ParseException e) {
            Log.i("TEST", "Poorly formatted date");
        }

        expectedList.add(new CartSchedule(date2, 6, 30, "jfrbjhwrbfwkebfkjw@gmail.com", "fewad", "bbbbbbb"));


        Date date3 = Calendar.getInstance().getTime();
        try {
            date3 = (CartSchedule.df).parse("10/02/2026");
        } catch (ParseException e) {
            Log.i("TEST", "Poorly formatted date");
        }

        expectedList.add(new CartSchedule(date3, 435324548, 10, "jfreafHUFEnfdfkjw@yahoo.com", "WaSsfRGJdSE", "fsdjYYYYfhuiFWFrwGGhfui"));

        return expectedList;

    }
    @Test
    public void testCartSchedulePoorlyFormatted() {
        List<Map<String, Object>> cartScheduleJsonList = createPoorlyFormattedJsonScheduleList(true);
        List<CartSchedule> parsedList = JSONObjectParser.parseJSONCartScheduleList(cartScheduleJsonList);

        List<CartSchedule> expectedList = createPoorlyFormattedCartScheduleList();


        assertEquals(expectedList,parsedList);


    }

    public List<CartSchedule> createPoorlyFormattedCartScheduleList() {
        List<CartSchedule> expectedList = new ArrayList<>();

        Date date1 = Calendar.getInstance().getTime();
        try {
            date1 = (CartSchedule.df).parse("05/02/2024");
        } catch (ParseException e) {
            Log.i("TEST", "Poorly formatted date");
        }

        expectedList.add(new CartSchedule(date1, 4, 5, "abc@abc.com", "abc", "a"));

        Date date2 = Calendar.getInstance().getTime();
        try {
            date2 = (CartSchedule.df).parse("10/02/2026");
        } catch (ParseException e) {
            Log.i("TEST", "Poorly formatted date");
        }

        expectedList.add(new CartSchedule(date2, 435324548, 10, "jfreafHUFEnfdfkjw@yahoo.com", "WaSsfRGJdSE", "fsdjYYYYfhuiFWFrwGGhfui"));

        return expectedList;
    }


    @Test
    public void testItemListStandard() {
        List<Item> parsedList = JSONObjectParser.parseItemList(createJsonItemListStandard());

        Date date1 = new Date();
        Date date2 = new Date();
        Date date3 = new Date();
        Date date4 = new Date();
        try {
            date1 = (Item.df).parse("04/12/2023");
            date2 = (Item.df).parse("12/12/2010");
            date3 = (Item.df).parse("24/05/2013");
            date4 = (Item.df).parse("16/05/2024");

        } catch (ParseException e) {
            e.printStackTrace();
        }

        List<Item> expectedList = new ArrayList<>();
        expectedList.add(ItemFactory.createItem("Orange", "Fruit", date2, date1, testUser, 4, "Fridge", createItemUseScheduleList1()));
        expectedList.add(ItemFactory.createItem("Shrimp", "Protein", date4, date3, testUser2, 8, "Pantry", Collections.emptyList()));


        assertEquals(expectedList,parsedList);
    }

    @Test
    public void testItemListPoorlyFormatted() {
        //Poorly formatted date
        List<Map<String, Object>> totalJsonItemList = createPoorlyFormattedJsonItemList();
        List<Map<String, Object>> toBeTested = new ArrayList<>();
        toBeTested.add(totalJsonItemList.get(0));
        toBeTested.add(totalJsonItemList.get(2));
        toBeTested.add(totalJsonItemList.get(1));


        //Poorly formatted schedule
        List<Item> expectedList = new ArrayList<>();


        Date date1 = new Date();
        Date date2 = new Date();
        Date date3 = new Date();
        Date date4 = new Date();
        Date date5 = new Date();
        try {
            date1 = (Item.df).parse("04/12/2023");
            date2 = (Item.df).parse("12/12/2010");
            date3 = (Item.df).parse("24/05/2013");
            date4 = (Item.df).parse("16/05/2024");
            date5 = (Item.df).parse("12/12/2020");

        } catch (ParseException e) {
            e.printStackTrace();
        }


        expectedList.add(ItemFactory.createItem("Orange", "Fruit", date2, date1, testUser, 4, "Fridge", createItemUseScheduleList1()));
        expectedList.add(ItemFactory.createItem("Bread", "Grain", date5, date3, testUser2, 8, "Pantry", createItemUseSchedulePoorlyFormatted()));
        expectedList.add(ItemFactory.createItem("Shrimp", "Protein", date4, date3, testUser2, 8, "Pantry", Collections.emptyList()));


        assertEquals(expectedList, JSONObjectParser.parseItemList(toBeTested));



        //Poorly formatted expiry date
        toBeTested.add(totalJsonItemList.get(3));
        assertThrows(RuntimeException.class, () -> parseItemList(toBeTested));



        //Missing quantity field
        toBeTested.remove(totalJsonItemList.get(3));
        toBeTested.add(totalJsonItemList.get(4));
        assertItemListEquals(expectedList, JSONObjectParser.parseItemList(toBeTested));
        toBeTested.remove(totalJsonItemList.get(4));

        //Missing name field
        toBeTested.add(totalJsonItemList.get(5));
        assertItemListEquals(expectedList, JSONObjectParser.parseItemList(toBeTested));
        toBeTested.remove(totalJsonItemList.get(5));

        //Missing type field
        toBeTested.add(totalJsonItemList.get(6));
        assertItemListEquals(expectedList, JSONObjectParser.parseItemList(toBeTested));
        toBeTested.remove(totalJsonItemList.get(6));

        //Missing bought date field
        toBeTested.add(totalJsonItemList.get(7));
        assertItemListEquals(expectedList, JSONObjectParser.parseItemList(toBeTested));
        toBeTested.remove(totalJsonItemList.get(7));

        //Poorly formatted bought date
        toBeTested.add(totalJsonItemList.get(8));
        assertThrows(RuntimeException.class, () -> parseItemList(toBeTested));

        //Missing expiry date field
        toBeTested.remove(totalJsonItemList.get(8));
        toBeTested.add(totalJsonItemList.get(9));
        assertItemListEquals(expectedList, JSONObjectParser.parseItemList(toBeTested));
        toBeTested.remove(totalJsonItemList.get(9));

        //Missing storage location field
        toBeTested.add(totalJsonItemList.get(10));
        assertItemListEquals(expectedList, JSONObjectParser.parseItemList(toBeTested));
        toBeTested.remove(totalJsonItemList.get(10));

        //Missing associated user id
        toBeTested.add(totalJsonItemList.get(11));
        assertItemListEquals(expectedList, JSONObjectParser.parseItemList(toBeTested));
        toBeTested.remove(totalJsonItemList.get(11));

        //Empty list
        assertItemListEquals(Collections.emptyList(), JSONObjectParser.parseItemList(Collections.emptyList()));

    }

    public void assertItemListEquals(List<Item> expected, List<Item> actual) {

        if (expected.size() == actual.size()) {
            for (int i = 0; i < expected.size(); i++) {
                if (!expected.get(i).perfectEquals(actual.get(i))) {
                    fail("Different items at the same index. Exptected: " + expected.get(i).toString() + "But got: " + actual.get(i).toString());
                }
            }
        } else {
            fail("Item lists are not the same size. expected.size() - actual.size() = " + (expected.size() - actual.size()));
        }

        assertTrue(true);

    }


    @Test
    public void testCartItemListStandard() {
        assertCartListEquals(createCartItemListStandard(), JSONObjectParser.parseCartItemList(createJsonCartItemListStandard()));
    }


    @Test
    public void testCartItemListPoorlyFormatted() {
        assertCartListEquals(createCartItemListStandard(), JSONObjectParser.parseCartItemList(createJsonCartItemListPoorlyFormatted1()));

    }
    /**
     *
     * @return well-formatted json cart item list
     */
    public List<Map<String, Object>> createJsonCartItemListStandard() {
        List<Map<String, Object>> itemList = new ArrayList<>();

        Map<String, Object> item1 = new HashMap<>();
        item1.put(CartItem.NAME_FIELD_NAME, "Orange");
        item1.put(CartItem.TYPE_FIELD_NAME, "Fruit");
        item1.put(CartItem.EXPIRY_DAYS_FIELD_NAME, "5");
        item1.put(CartItem.SCHEDULE_FIELD_NAME, createJsonScheduleList1(true));
        item1.put(CartItem.QUANTITY_FIELD_NAME, "4");
        item1.put(CartItem.STORAGE_LOCATION_FIELD_NAME, "Fridge");
        item1.put(CartItem.ASSOCIATED_USER_ID_FIELD_NAME, "SGJNESJsdnfjk485usdjk");
        item1.put(CartItem.ASSOCIATED_USER_EMAIL_FIELD_NAME, "user@gmail.com");

        itemList.add(item1);

        Map<String, Object> item2 = new HashMap<>();
        item2.put(CartItem.NAME_FIELD_NAME, "Shrimp");
        item2.put(CartItem.TYPE_FIELD_NAME, "Protein");
        item2.put(CartItem.EXPIRY_DAYS_FIELD_NAME, "6");
        item2.put(CartItem.SCHEDULE_FIELD_NAME, Collections.emptyList());
        item2.put(CartItem.QUANTITY_FIELD_NAME, "8");
        item2.put(CartItem.STORAGE_LOCATION_FIELD_NAME, "Pantry");
        item2.put(CartItem.ASSOCIATED_USER_ID_FIELD_NAME, "dFJ34dfm3q45usfwEG");
        item2.put(CartItem.ASSOCIATED_USER_EMAIL_FIELD_NAME, "username2@gmail.com");

        itemList.add(item2);


        return itemList;


    }

    /**
     *
     * @return list of poorly-formatted maps representing cart-items
     */
    public List<Map<String, Object>> createJsonCartItemListPoorlyFormatted1() {
        List<Map<String, Object>> itemList = new ArrayList<>();

        Map<String, Object> item1 = new HashMap<>();
        item1.put(CartItem.NAME_FIELD_NAME, "Orange");
        item1.put(CartItem.TYPE_FIELD_NAME, "Fruit");
        item1.put(CartItem.EXPIRY_DAYS_FIELD_NAME, "5");
        item1.put(CartItem.SCHEDULE_FIELD_NAME, createJsonScheduleList1(true));
        item1.put(CartItem.QUANTITY_FIELD_NAME, "4");
        item1.put(CartItem.STORAGE_LOCATION_FIELD_NAME, "Fridge");
        item1.put(CartItem.ASSOCIATED_USER_ID_FIELD_NAME, "SGJNESJsdnfjk485usdjk");
        item1.put(CartItem.ASSOCIATED_USER_EMAIL_FIELD_NAME, "user@gmail.com");

        itemList.add(item1);



        //Missing type
        Map<String, Object> item2 = new HashMap<>();
        item2.put(CartItem.NAME_FIELD_NAME, "Corn");
        item2.put(CartItem.EXPIRY_DAYS_FIELD_NAME, "6");
        item2.put(CartItem.SCHEDULE_FIELD_NAME, Collections.emptyList());
        item2.put(CartItem.QUANTITY_FIELD_NAME, "8");
        item2.put(CartItem.STORAGE_LOCATION_FIELD_NAME, "Pantry");
        item2.put(CartItem.ASSOCIATED_USER_ID_FIELD_NAME, "dFJ34dfm3q45usfwEG");
        item2.put(CartItem.ASSOCIATED_USER_EMAIL_FIELD_NAME, "username2@gmail.com");

        itemList.add(item2);



        Map<String, Object> item3 = new HashMap<>();
        item3.put(CartItem.NAME_FIELD_NAME, "Shrimp");
        item3.put(CartItem.TYPE_FIELD_NAME, "Protein");
        item3.put(CartItem.EXPIRY_DAYS_FIELD_NAME, "6");
        item3.put(CartItem.SCHEDULE_FIELD_NAME, Collections.emptyList());
        item3.put(CartItem.QUANTITY_FIELD_NAME, "8");
        item3.put(CartItem.STORAGE_LOCATION_FIELD_NAME, "Pantry");
        item3.put(CartItem.ASSOCIATED_USER_ID_FIELD_NAME, "dFJ34dfm3q45usfwEG");
        item3.put(CartItem.ASSOCIATED_USER_EMAIL_FIELD_NAME, "username2@gmail.com");

        itemList.add(item3);


        return itemList;


    }




    /**
     * Returns the correct cart item list corresponding to createJsonCartItemListStandard()
     * @return
     */
    public List<CartItem> createCartItemListStandard() {
        List<CartItem> itemList = new ArrayList<>();

        itemList.add(CartItemFactory.createCartItem("Orange", "Fruit", 5, testUser, 4, "Fridge", createCartScheduleListStandard()));
        itemList.add(CartItemFactory.createCartItem("Shrimp", "Protein", 6, testUser2, 8, "Pantry", Collections.emptyList()));

        return itemList;

    }

    public void assertCartListEquals(List<CartItem> expected, List<CartItem> actual) {

        if (expected.size() == actual.size()) {
            for (int i = 0; i < expected.size(); i++) {
                if (!expected.get(i).perfectEquals(actual.get(i))) {
                    fail("Different items at the same index. Exptected: " + expected.get(i).toString() + "But got: " + actual.get(i).toString());
                }
            }
        } else {
            fail("Cart item lists are not the same size. expected.size() - actual.size() = " + (expected.size() - actual.size()));
        }

        assertTrue(true);

    }







    /**
     *
     * @return well-formatted json item list
     */
    public List<Map<String, Object>> createJsonItemListStandard() {
        List<Map<String, Object>> itemList = new ArrayList<>();

        Map<String, Object> item1 = new HashMap<>();
        item1.put(Item.NAME_FIELD_NAME, "Orange");
        item1.put(Item.TYPE_FIELD_NAME, "Fruit");
        item1.put(Item.BOUGHT_DATE_FIELD_NAME, "04/12/2023");
        item1.put(Item.EXPIRY_DATE_FIELD_NAME, "12/12/2010");
        item1.put(Item.SCHEDULE_FIELD_NAME, createJsonScheduleList1(false));
        item1.put(Item.QUANTITY_FIELD_NAME, "4");
        item1.put(Item.STORAGE_LOCATION_FIELD_NAME, "Fridge");
        item1.put(Item.ASSOCIATED_USER_ID_FIELD_NAME, "SGJNESJsdnfjk485usdjk");
        item1.put(Item.ASSOCIATED_USER_EMAIL_FIELD_NAME, "user@gmail.com");

        itemList.add(item1);

        Map<String, Object> item2 = new HashMap<>();
        item2.put(Item.NAME_FIELD_NAME, "Shrimp");
        item2.put(Item.TYPE_FIELD_NAME, "Protein");
        item2.put(Item.BOUGHT_DATE_FIELD_NAME, "24/05/2013");
        item2.put(Item.EXPIRY_DATE_FIELD_NAME, "16/05/2024");
        item2.put(Item.SCHEDULE_FIELD_NAME, Collections.emptyList());
        item2.put(Item.QUANTITY_FIELD_NAME, "8");
        item2.put(Item.STORAGE_LOCATION_FIELD_NAME, "Pantry");
        item2.put(Item.ASSOCIATED_USER_ID_FIELD_NAME, "dFJ34dfm3q45usfwEG");
        item2.put(Item.ASSOCIATED_USER_EMAIL_FIELD_NAME, "username2@gmail.com");

        itemList.add(item2);


        return itemList;


    }

    /**
     *
     * @return poorly formatted json item list
     */
    public List<Map<String, Object>> createPoorlyFormattedJsonItemList() {
        List<Map<String, Object>> itemList = new ArrayList<>();

        //Well-formatted item
        Map<String, Object> item0 = new HashMap<>();
        item0.put(Item.NAME_FIELD_NAME, "Orange");
        item0.put(Item.TYPE_FIELD_NAME, "Fruit");
        item0.put(Item.BOUGHT_DATE_FIELD_NAME, "04/12/2023");
        item0.put(Item.EXPIRY_DATE_FIELD_NAME, "12/12/2010");
        item0.put(Item.SCHEDULE_FIELD_NAME, createJsonScheduleList1(false));
        item0.put(Item.QUANTITY_FIELD_NAME, "4");
        item0.put(Item.STORAGE_LOCATION_FIELD_NAME, "Fridge");
        item0.put(Item.ASSOCIATED_USER_ID_FIELD_NAME, "SGJNESJsdnfjk485usdjk");
        item0.put(Item.ASSOCIATED_USER_EMAIL_FIELD_NAME, "user@gmail.com");

        itemList.add(item0);

        //Well-formatted item;
        Map<String, Object> item1 = new HashMap<>();
        item1.put(Item.NAME_FIELD_NAME, "Shrimp");
        item1.put(Item.TYPE_FIELD_NAME, "Protein");
        item1.put(Item.BOUGHT_DATE_FIELD_NAME, "24/05/2013");
        item1.put(Item.EXPIRY_DATE_FIELD_NAME, "16/05/2024");
        item1.put(Item.SCHEDULE_FIELD_NAME, Collections.emptyList());
        item1.put(Item.QUANTITY_FIELD_NAME, "8");
        item1.put(Item.STORAGE_LOCATION_FIELD_NAME, "Pantry");
        item1.put(Item.ASSOCIATED_USER_ID_FIELD_NAME, "dFJ34dfm3q45usfwEG");
        item1.put(Item.ASSOCIATED_USER_EMAIL_FIELD_NAME, "username2@gmail.com");

        itemList.add(item1);

        //Poorly formatted item, poorly formatted schedule
        Map<String, Object> item2 = new HashMap<>();
        item2.put(Item.NAME_FIELD_NAME, "Bread");
        item2.put(Item.TYPE_FIELD_NAME, "Grain");
        item2.put(Item.BOUGHT_DATE_FIELD_NAME, "24/05/2013");
        item2.put(Item.EXPIRY_DATE_FIELD_NAME, "12/12/2020");
        item2.put(Item.SCHEDULE_FIELD_NAME, createPoorlyFormattedJsonScheduleList(false));
        item2.put(Item.QUANTITY_FIELD_NAME, "8");
        item2.put(Item.STORAGE_LOCATION_FIELD_NAME, "Pantry");
        item2.put(Item.ASSOCIATED_USER_ID_FIELD_NAME, "dFJ34dfm3q45usfwEG");
        item2.put(Item.ASSOCIATED_USER_EMAIL_FIELD_NAME, "username2@gmail.com");

        itemList.add(item2);


        //Poorly formatted item, poorly formatted expiry date
        Map<String, Object> item3 = new HashMap<>();
        item3.put(Item.NAME_FIELD_NAME, "Milk");
        item3.put(Item.TYPE_FIELD_NAME, "Dairy");
        item3.put(Item.BOUGHT_DATE_FIELD_NAME, "24/05/2013");
        item3.put(Item.EXPIRY_DATE_FIELD_NAME, "16");
        item3.put(Item.SCHEDULE_FIELD_NAME, Collections.emptyList());
        item3.put(Item.QUANTITY_FIELD_NAME, "8");
        item3.put(Item.STORAGE_LOCATION_FIELD_NAME, "Pantry");
        item3.put(Item.ASSOCIATED_USER_ID_FIELD_NAME, "dFJ34dfm3q45usfwEG");
        item3.put(Item.ASSOCIATED_USER_EMAIL_FIELD_NAME, "username2@gmail.com");

        itemList.add(item3);


        //Poorly formatted item, missing quantity field
        Map<String, Object> item4 = new HashMap<>();
        item4.put(Item.NAME_FIELD_NAME, "Fish");
        item4.put(Item.TYPE_FIELD_NAME, "Protein");
        item4.put(Item.BOUGHT_DATE_FIELD_NAME, "24/05/2013");
        item4.put(Item.EXPIRY_DATE_FIELD_NAME, "12/12/2020");
        item4.put(Item.SCHEDULE_FIELD_NAME, Collections.emptyList());
        item4.put(Item.STORAGE_LOCATION_FIELD_NAME, "Pantry");
        item4.put(Item.ASSOCIATED_USER_ID_FIELD_NAME, "dFJ34dfm3q45usfwEG");
        item4.put(Item.ASSOCIATED_USER_EMAIL_FIELD_NAME, "username2@gmail.com");

        itemList.add(item4);

        //Poorly formatted item, missing name field
        Map<String, Object> item5 = new HashMap<>();
        item5.put(Item.TYPE_FIELD_NAME, "Protein");
        item5.put(Item.BOUGHT_DATE_FIELD_NAME, "24/05/2013");
        item5.put(Item.EXPIRY_DATE_FIELD_NAME, "12/12/2020");
        item5.put(Item.SCHEDULE_FIELD_NAME, Collections.emptyList());
        item5.put(Item.QUANTITY_FIELD_NAME, "8");
        item5.put(Item.STORAGE_LOCATION_FIELD_NAME, "Pantry");
        item5.put(Item.ASSOCIATED_USER_ID_FIELD_NAME, "dFJ34dfm3q45usfwEG");
        item5.put(Item.ASSOCIATED_USER_EMAIL_FIELD_NAME, "username2@gmail.com");

        itemList.add(item5);



        //Poorly formatted item, missing type field
        Map<String, Object> item6 = new HashMap<>();
        item6.put(Item.NAME_FIELD_NAME, "Lamb");
        item6.put(Item.BOUGHT_DATE_FIELD_NAME, "24/05/2013");
        item6.put(Item.EXPIRY_DATE_FIELD_NAME, "12/12/2020");
        item6.put(Item.SCHEDULE_FIELD_NAME, Collections.emptyList());
        item6.put(Item.QUANTITY_FIELD_NAME, "4");
        item6.put(Item.STORAGE_LOCATION_FIELD_NAME, "Fridge");
        item6.put(Item.ASSOCIATED_USER_ID_FIELD_NAME, "dFJ34dfm3q45usfwEG");
        item6.put(Item.ASSOCIATED_USER_EMAIL_FIELD_NAME, "username2@gmail.com");

        itemList.add(item6);

        //Poorly formatted item, missing bought date field
        Map<String, Object> item7 = new HashMap<>();
        item7.put(Item.NAME_FIELD_NAME, "Yoghurt");
        item7.put(Item.TYPE_FIELD_NAME, "Dairy");
        item7.put(Item.EXPIRY_DATE_FIELD_NAME, "12/12/2010");
        item7.put(Item.SCHEDULE_FIELD_NAME, Collections.emptyList());
        item7.put(Item.QUANTITY_FIELD_NAME, "4");
        item7.put(Item.STORAGE_LOCATION_FIELD_NAME, "Fridge");
        item7.put(Item.ASSOCIATED_USER_ID_FIELD_NAME, "dFJ34dfm3q45usfwEG");
        item7.put(Item.ASSOCIATED_USER_EMAIL_FIELD_NAME, "username2@gmail.com");

        itemList.add(item7);

        //Poorly formatted item, poorly formatted bought date
        Map<String, Object> item8 = new HashMap<>();
        item8.put(Item.NAME_FIELD_NAME, "Jam");
        item8.put(Item.TYPE_FIELD_NAME, "Other");
        item8.put(Item.BOUGHT_DATE_FIELD_NAME, "hi");
        item8.put(Item.EXPIRY_DATE_FIELD_NAME, "12/12/2010");
        item8.put(Item.SCHEDULE_FIELD_NAME, Collections.emptyList());
        item8.put(Item.QUANTITY_FIELD_NAME, "4");
        item8.put(Item.STORAGE_LOCATION_FIELD_NAME, "Fridge");
        item8.put(Item.ASSOCIATED_USER_ID_FIELD_NAME, "dFJ34dfm3q45usfwEG");
        item8.put(Item.ASSOCIATED_USER_EMAIL_FIELD_NAME, "username2@gmail.com");

        itemList.add(item8);

        //Poorly formatted item, missing expiry date
        Map<String, Object> item9 = new HashMap<>();
        item9.put(Item.NAME_FIELD_NAME, "Banana");
        item9.put(Item.TYPE_FIELD_NAME, "Fruit");
        item9.put(Item.BOUGHT_DATE_FIELD_NAME, "24/05/2013");
        item9.put(Item.SCHEDULE_FIELD_NAME, Collections.emptyList());
        item9.put(Item.QUANTITY_FIELD_NAME, "4");
        item9.put(Item.STORAGE_LOCATION_FIELD_NAME, "Fridge");
        item9.put(Item.ASSOCIATED_USER_ID_FIELD_NAME, "dFJ34dfm3q45usfwEG");
        item9.put(Item.ASSOCIATED_USER_EMAIL_FIELD_NAME, "username2@gmail.com");

        itemList.add(item9);


        //Poorly formatted item, missing storage location
        Map<String, Object> item10 = new HashMap<>();
        item10.put(Item.NAME_FIELD_NAME, "Cucumber");
        item10.put(Item.TYPE_FIELD_NAME, "Vegetable");
        item10.put(Item.BOUGHT_DATE_FIELD_NAME, "24/05/2013");
        item10.put(Item.EXPIRY_DATE_FIELD_NAME, "12/12/2010");
        item10.put(Item.SCHEDULE_FIELD_NAME, Collections.emptyList());
        item10.put(Item.QUANTITY_FIELD_NAME, "20");
        item10.put(Item.ASSOCIATED_USER_ID_FIELD_NAME, "dFJ34dfm3q45usfwEG");
        item10.put(Item.ASSOCIATED_USER_EMAIL_FIELD_NAME, "username2@gmail.com");

        itemList.add(item10);



        //Poorly formatted item, missing associated user id
        Map<String, Object> item11 = new HashMap<>();
        item11.put(Item.NAME_FIELD_NAME, "Capsicum");
        item11.put(Item.TYPE_FIELD_NAME, "Vegetable");
        item11.put(Item.BOUGHT_DATE_FIELD_NAME, "24/05/2013");
        item11.put(Item.EXPIRY_DATE_FIELD_NAME, "12/12/2010");
        item11.put(Item.SCHEDULE_FIELD_NAME, Collections.emptyList());
        item11.put(Item.QUANTITY_FIELD_NAME, "20");
        item11.put(Item.STORAGE_LOCATION_FIELD_NAME, "Fridge");
        item11.put(Item.ASSOCIATED_USER_EMAIL_FIELD_NAME, "username2@gmail.com");

        itemList.add(item11);


        return itemList;

    }


    /**
     *
     * @param scheduleType false if ItemUseSchedule,true if CartSchedule
     * @return
     */
    public List<Map<String, Object>> createJsonScheduleList1(boolean scheduleType) {
        List<Map<String, Object>> scheduleJsonList = new ArrayList<>();
        Map<String, Object> scheduleJson1 = new HashMap<>();

        if (scheduleType) {
            scheduleJson1.put(Schedule.SCHEDULE_DATE_FIELD_NAME, "05/02/2024");
        } else {
            scheduleJson1.put(Schedule.SCHEDULE_DATE_FIELD_NAME, "05/02/2024 12:43");
        }
        scheduleJson1.put(Schedule.SCHEDULE_QUANTITY_FIELD_NAME, "4");
        scheduleJson1.put(Schedule.SCHEDULE_ID_FIELD_NAME, "a");
        scheduleJson1.put(Schedule.SCHEDULE_UEMAIL_FIELD_NAME, "abc@abc.com");
        scheduleJson1.put(Schedule.SCHEDULE_UID_FIELD_NAME, "abc");
        if (scheduleType) {
            scheduleJson1.put(CartSchedule.SCHEDULE_DAYS_REOCCURRING_FIELD_NAME, "5");
        }

        scheduleJsonList.add(scheduleJson1);


        Map<String, Object> scheduleJson2 = new HashMap<>();

        if (scheduleType) {
            scheduleJson2.put(Schedule.SCHEDULE_DATE_FIELD_NAME, "24/02/2004");
        } else {
            scheduleJson2.put(Schedule.SCHEDULE_DATE_FIELD_NAME, "24/02/2004 22:13");
        }

        scheduleJson2.put(Schedule.SCHEDULE_QUANTITY_FIELD_NAME, "6");
        scheduleJson2.put(Schedule.SCHEDULE_ID_FIELD_NAME, "bbbbbbb");
        scheduleJson2.put(Schedule.SCHEDULE_UEMAIL_FIELD_NAME, "jfrbjhwrbfwkebfkjw@gmail.com");
        scheduleJson2.put(Schedule.SCHEDULE_UID_FIELD_NAME, "fewad");
        if (scheduleType) {
            scheduleJson2.put(CartSchedule.SCHEDULE_DAYS_REOCCURRING_FIELD_NAME, "30");
        }

        scheduleJsonList.add(scheduleJson2);



        Map<String, Object> scheduleJson3 = new HashMap<>();

        if (scheduleType) {
            scheduleJson3.put(Schedule.SCHEDULE_DATE_FIELD_NAME, "10/02/2026");
        } else {
            scheduleJson3.put(Schedule.SCHEDULE_DATE_FIELD_NAME, "10/02/2026 05:20");
        }

        scheduleJson3.put(Schedule.SCHEDULE_QUANTITY_FIELD_NAME, "435324548");
        scheduleJson3.put(Schedule.SCHEDULE_ID_FIELD_NAME, "fsdjYYYYfhuiFWFrwGGhfui");
        scheduleJson3.put(Schedule.SCHEDULE_UEMAIL_FIELD_NAME, "jfreafHUFEnfdfkjw@yahoo.com");
        scheduleJson3.put(Schedule.SCHEDULE_UID_FIELD_NAME, "WaSsfRGJdSE");
        if (scheduleType) {
            scheduleJson3.put(CartSchedule.SCHEDULE_DAYS_REOCCURRING_FIELD_NAME, "10");
        }

        scheduleJsonList.add(scheduleJson3);



        return scheduleJsonList;
    }

    /**
     * Create item list corresponding to createJsonScheduleList1
     * @return
     */
    public List<ItemUseSchedule> createItemUseScheduleList1() {
        List<ItemUseSchedule> expectedList = new ArrayList<>();

        Date date1 = Calendar.getInstance().getTime();
        try {
            date1 = (ItemUseSchedule.df).parse("05/02/2024 12:43");
        } catch (ParseException e) {
            Log.i("TEST", "Poorly formatted date");
        }

        expectedList.add(new ItemUseSchedule(date1, 4, "abc@abc.com", "abc", "a"));

        Date date2 = Calendar.getInstance().getTime();
        try {
            date2 = (ItemUseSchedule.df).parse("24/02/2004 22:13");
        } catch (ParseException e) {
            Log.i("TEST", "Poorly formatted date");
        }

        expectedList.add(new ItemUseSchedule(date2, 6,  "jfrbjhwrbfwkebfkjw@gmail.com", "fewad", "bbbbbbb"));


        Date date3 = Calendar.getInstance().getTime();
        try {
            date3 = (ItemUseSchedule.df).parse("10/02/2026 05:20");
        } catch (ParseException e) {
            Log.i("TEST", "Poorly formatted date");
        }

        expectedList.add(new ItemUseSchedule(date3, 435324548,  "jfreafHUFEnfdfkjw@yahoo.com", "WaSsfRGJdSE", "fsdjYYYYfhuiFWFrwGGhfui"));

        return expectedList;
    }

    /**
     *
     * @param scheduleType false if ItemUseSchedule,true if CartSchedule
     * @return
     */
    public List<Map<String, Object>> createPoorlyFormattedJsonScheduleList(boolean scheduleType) {
        List<Map<String, Object>> scheduleJsonList = new ArrayList<>();
        Map<String, Object> scheduleJson1 = new HashMap<>();

        if (scheduleType) {
            scheduleJson1.put(Schedule.SCHEDULE_DATE_FIELD_NAME, "05/02/2024");
        } else {
            scheduleJson1.put(Schedule.SCHEDULE_DATE_FIELD_NAME, "05/02/2024 12:43");
        }
        scheduleJson1.put(CartSchedule.SCHEDULE_QUANTITY_FIELD_NAME, "4");
        scheduleJson1.put(CartSchedule.SCHEDULE_ID_FIELD_NAME, "a");
        scheduleJson1.put(CartSchedule.SCHEDULE_UEMAIL_FIELD_NAME, "abc@abc.com");
        scheduleJson1.put(CartSchedule.SCHEDULE_UID_FIELD_NAME, "abc");
        if (scheduleType) {
            scheduleJson1.put(CartSchedule.SCHEDULE_DAYS_REOCCURRING_FIELD_NAME, "5");
        }

        scheduleJsonList.add(scheduleJson1);


        Map<String, Object> scheduleJson2 = new HashMap<>();

        if (scheduleType) {
            scheduleJson2.put(Schedule.SCHEDULE_DATE_FIELD_NAME, "24/02/2004");
        } else {
            scheduleJson2.put(Schedule.SCHEDULE_DATE_FIELD_NAME, "24/02/2004 22:13");
        }
        scheduleJson2.put(CartSchedule.SCHEDULE_UEMAIL_FIELD_NAME, "jfrbjhwrbfwkebfkjw@gmail.com");
        scheduleJson2.put(CartSchedule.SCHEDULE_UID_FIELD_NAME, "fewad");
        scheduleJson2.put(CartSchedule.SCHEDULE_DAYS_REOCCURRING_FIELD_NAME, "30");

        scheduleJsonList.add(scheduleJson2);



        Map<String, Object> scheduleJson3 = new HashMap<>();

        if (scheduleType) {
            scheduleJson3.put(Schedule.SCHEDULE_DATE_FIELD_NAME, "10/02/2026");
        } else {
            scheduleJson3.put(Schedule.SCHEDULE_DATE_FIELD_NAME, "10/02/2026 05:20");
        }

        scheduleJson3.put(CartSchedule.SCHEDULE_QUANTITY_FIELD_NAME, "435324548");
        scheduleJson3.put(CartSchedule.SCHEDULE_ID_FIELD_NAME, "fsdjYYYYfhuiFWFrwGGhfui");
        scheduleJson3.put(CartSchedule.SCHEDULE_UEMAIL_FIELD_NAME, "jfreafHUFEnfdfkjw@yahoo.com");
        scheduleJson3.put(CartSchedule.SCHEDULE_UID_FIELD_NAME, "WaSsfRGJdSE");
        if (scheduleType) {
            scheduleJson3.put(CartSchedule.SCHEDULE_DAYS_REOCCURRING_FIELD_NAME, "10");
        }

        scheduleJsonList.add(scheduleJson3);



        return scheduleJsonList;
    }

    @Test
    public void testParseNotification() {
        List<Map<String, Object>> notificationsFromFirebase = new ArrayList<>();

        // Create a sample notification
        Map<String, Object> notification1 = new HashMap<>();
        notification1.put(AppExpiryNotification.MESSAGE, "Expiry alert!");
        notification1.put(AppExpiryNotification.IMPORTANCE, "1");
        notificationsFromFirebase.add(notification1);

        // Create another sample notification
        Map<String, Object> notification2 = new HashMap<>();
        notification2.put(AppExpiryNotification.MESSAGE, "Close to expiry alert!");
        notification2.put(AppExpiryNotification.IMPORTANCE, "2");
        notificationsFromFirebase.add(notification2);

        // Call the method under test
        ArrayList<Notification> notifications = JSONObjectParser.parseNotification(notificationsFromFirebase);

        // Expected notifications
        ArrayList<Notification> expectedNotifications = new ArrayList<>();
        expectedNotifications.add(NotificationFactory.createNotification("Expiry alert!", 1));
        expectedNotifications.add(NotificationFactory.createNotification("Close to expiry alert!", 2));

        assertEquals(expectedNotifications, notifications);
    }


    @Test
    public void testParseHistoryList() throws Exception {
        List<Map<String, Object>> historyFromFireBase = new ArrayList<>();

        Map<String, Object> history1 = new HashMap<>();
        history1.put(ItemUsage.NAME_FIELD_NAME, "Apple");
        history1.put(ItemUsage.USING_DATE, "15/05/2023");
        history1.put(ItemUsage.QUANTITY_FIELD_NAME, "2");
        history1.put(ItemUsage.ASSOCIATED_USER_ID_FIELD_NAME, "user123");
        history1.put(ItemUsage.ASSOCIATED_USER_EMAIL_FIELD_NAME, "user123@example.com");
        historyFromFireBase.add(history1);

        Map<String, Object> history2 = new HashMap<>();
        history2.put(ItemUsage.NAME_FIELD_NAME, "Banana");
        history2.put(ItemUsage.USING_DATE, "16/05/2023");
        history2.put(ItemUsage.QUANTITY_FIELD_NAME, "5");
        history2.put(ItemUsage.ASSOCIATED_USER_ID_FIELD_NAME, "user456");
        history2.put(ItemUsage.ASSOCIATED_USER_EMAIL_FIELD_NAME, "user456@example.com");
        historyFromFireBase.add(history2);

        ArrayList<ItemUsage> itemUsageList = JSONObjectParser.parseHistoryList(historyFromFireBase);

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        ArrayList<ItemUsage> expectedItemUsageList = new ArrayList<>();
        expectedItemUsageList.add(ItemUsage.createItemUsage("Apple", df.parse("15/05/2023"), "2", null));
        expectedItemUsageList.add(ItemUsage.createItemUsage("Banana", df.parse("16/05/2023"), "5", null));

        for (int i = 0; i < expectedItemUsageList.size(); i++) {
            ItemUsage expected = expectedItemUsageList.get(i);
            ItemUsage actual = itemUsageList.get(i);

            assertEquals(expected.getName(), actual.getName());
            assertEquals(expected.getUsingDate(), actual.getUsingDate());
            assertNull(null);
        }
    }

}
