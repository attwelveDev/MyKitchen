package com.aaronnguyen.mykitchen;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static com.google.common.base.Verify.verify;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;


import com.aaronnguyen.mykitchen.CustomExceptions.FetchKitchenException;
import com.aaronnguyen.mykitchen.CustomExceptions.InvalidQuantityException;
import com.aaronnguyen.mykitchen.DAO.ChatRoomFirebaseDAO;
import com.aaronnguyen.mykitchen.DAO.FetchListener;
import com.aaronnguyen.mykitchen.DAO.KitchenDAO;
import com.aaronnguyen.mykitchen.DAO.KitchenData;
import com.aaronnguyen.mykitchen.DAO.KitchenFirebaseDAO;
import com.aaronnguyen.mykitchen.DAO.UserDaoFirebase;
import com.aaronnguyen.mykitchen.DAO.WriteListener;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartItem;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartItemFactory;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartSchedule;
import com.aaronnguyen.mykitchen.model.Items.ItemUsage;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Item;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemFactory;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemUseSchedule;
import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.model.kitchen.KitchenObserver;
import com.aaronnguyen.mykitchen.model.notification.AppExpiryNotification;
import com.aaronnguyen.mykitchen.model.notification.Notification;
import com.aaronnguyen.mykitchen.model.user.User;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.A;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class KitchenUnitTest {
    private Kitchen kitchen;

    public static String TEST_KITCHEN_ID = "111";
    public static String TEST_USER_ID = "22";
    public static String TEST_USER_EMAIL = "n@testkitchen.com";
    public static String TEST_USER_NAME = "";
    public static String TEST_CHAT_ROOM_ID = "";

    static Item[] items = new Item[6];
    static CartItem[] cartItems = new CartItem[6];
    static ItemUsage[] itemUsages = new ItemUsage[6];

    static Notification[] notifications = new Notification[6];


    static User testUser;


    @Before
    public void setUp() {
        // Initialize Kitchen object before each test
        kitchen =  new Kitchen("testKitchenID","testKitchen");
        ArrayList<String> allKitchens = new ArrayList<>();
        kitchen.observers = new ArrayList<>();
        allKitchens.add(kitchen.getKitchenID());
        kitchen.observers.add(new User("testUid","testUser","testEmail",allKitchens));
    }


    @BeforeClass
    public static void setUpItems() {
        testUser = new User(TEST_USER_ID, "I am Groot", "000testkitchen@testkitchen.com", Collections.singletonList("000 Test Kitchen"));
        items[0] = ItemFactory.createItem("Apple", "Fruit", new Date(), new Date(), testUser, 5, "Pantry", new ArrayList<>());
        items[1] = ItemFactory.createItem("Beef Ribs", "Protein", new Date(), new Date(), testUser, 2, "Freezer", new ArrayList<>());
        items[2] = ItemFactory.createItem("Chinese Bok Choy", "Vegetable", new Date(), new Date(), testUser, 5, "Fridge", new ArrayList<>());
        items[3] = ItemFactory.createItem("Rice", "Grain", new Date(), new Date(), testUser, 1, "Pantry", new ArrayList<>());
        items[4] = ItemFactory.createItem("Indomie", "Others", new Date(), new Date(), testUser, 5, "Pantry", new ArrayList<>());
        items[5] = ItemFactory.createItem("Full Cream Milk", "Dairy", new Date(), new Date(), testUser, 2, "Fridge", new ArrayList<>());


        Date cartDate1 = new Date();
        Date cartDate2 = new Date();
        Date cartDate3 = new Date();

        try {
            cartDate1 = (CartSchedule.df).parse("24/05/2025");
            cartDate2 = (CartSchedule.df).parse("2/06/2024");
            cartDate3 = (CartSchedule.df).parse("30/11/2025");

        } catch (ParseException e) {
            e.printStackTrace();
        }

        CartSchedule cartSchedule1 = new CartSchedule(cartDate1, 5, 4, testUser);
        CartSchedule cartSchedule2 = new CartSchedule(cartDate2, 7, 2, testUser);
        CartSchedule cartSchedule3 = new CartSchedule(cartDate3, 53, 41, testUser);


        cartItems[0] = CartItemFactory.createCartItem("Mango", "Fruit", 4, testUser, 4, "Fridge", new ArrayList<>());
        cartItems[1] = CartItemFactory.createCartItem("Orange", "Fruit", 1, testUser, 1, "Pantry", new ArrayList<>());

        ArrayList<CartSchedule> cartSchedule = new ArrayList<>();
        cartSchedule.add(cartSchedule1);
        cartSchedule.add(cartSchedule2);
        cartSchedule.add(cartSchedule3);

        cartItems[2] = CartItemFactory.createCartItem("Shrimp", "Protein", 352, testUser, 436, "Fridge", new ArrayList<>());
        cartItems[3] = CartItemFactory.createCartItem("Peanuts", "Others", 42, testUser, 7, "Pantry", new ArrayList<>());
        cartItems[4] = CartItemFactory.createCartItem("Corn", "Vegetable", 12, testUser, 9, "Pantry", new ArrayList<>());
        cartItems[5] = CartItemFactory.createCartItem("Milk", "Dairy", 352, testUser, 436, "Freezer", new ArrayList<>());
    }

    @BeforeClass
    public static void setUpItemUsages() {
        testUser = new User(TEST_USER_ID, "I am Groot", "000testkitchen@testkitchen.com", Collections.singletonList("000 Test Kitchen"));
        itemUsages[0] = new ItemUsage("Apple", testUser, new Date(), 5);
        itemUsages[1] = new ItemUsage("Beef Ribs", testUser, new Date(), 5);
        itemUsages[2] = new ItemUsage("Chinese Bok Choy", testUser, new Date(), 5);
        itemUsages[3] = new ItemUsage("Rice", testUser, new Date(), 5);
        itemUsages[4] = new ItemUsage("Indomie", testUser, new Date(), 5);
        itemUsages[5] = new ItemUsage("Full Cream Milk", testUser, new Date(), 5);
    }

    /**
     * This is to ensure that after every test, the following set of six items will still be the same before executing the next tests
     */
    @After
    public void resetItems() {
        items[0] = ItemFactory.createItem("Apple", "Fruit", new Date(), new Date(), testUser, 5, "Pantry", new ArrayList<>());
        items[1] = ItemFactory.createItem("Beef Ribs", "Protein", new Date(), new Date(), testUser, 2, "Freezer", new ArrayList<>());
        items[2] = ItemFactory.createItem("Chinese Bok Choy", "Vegetable", new Date(), new Date(), testUser, 5, "Fridge", new ArrayList<>());
        items[3] = ItemFactory.createItem("Rice", "Grain", new Date(), new Date(), testUser, 1, "Pantry", new ArrayList<>());
        items[4] = ItemFactory.createItem("Indomie", "Others", new Date(), new Date(), testUser, 5, "Pantry", new ArrayList<>());
        items[5] = ItemFactory.createItem("Full Cream Milk", "Dairy", new Date(), new Date(), testUser, 2, "Fridge", new ArrayList<>());
    }

    @BeforeClass
    public static void setUpNotifications() {
        testUser = new User(TEST_USER_ID, "I am Groot", "000testkitchen@testkitchen.com", Collections.singletonList("000 Test Kitchen"));
        notifications[0] = new AppExpiryNotification("apple has expired", 3);
        notifications[1] = new AppExpiryNotification("pork belly has expired", 3);
        notifications[2] = new AppExpiryNotification("tofu has expired", 3);
        notifications[3] = new AppExpiryNotification("apple will expire in 3 days", 2);
        notifications[4] = new AppExpiryNotification("peach will expire in 2 days", 2);
        notifications[5] = new AppExpiryNotification("watermelon will expired today", 2);
    }


    /**
     * <p>
     * We first check that the hard coding information is correct. Clearly it is not ideal to have fixed arguments.
     * </p>
     * <p>
     * Therefore, we will need to use the later test. Also, this function also tests the correctness of the fetch kitchen as it is the basic function
     * for the whole application. I should consider adding more things inside the kitchen so that the testing can have higher coverage.
     * </p>
     */
    @Test
    public void testAddingItem() throws InvalidQuantityException {
        // Expected arrays after each addition
        Item[][] expectedArrays = {
                {items[0]},
                {items[0], items[1]},
                {items[0], items[1], items[2]},
                {items[0], items[1], items[2], items[3]},
                {items[0], items[1], items[2], items[3], items[4]},
                {items[0], items[1], items[2], items[3], items[4], items[5]}
        };

        for (int i = 0; i < items.length; i++) {
            kitchen.addItem(items[i]);
            List<Item> actualItems = kitchen.getItemList();
            Item[] actualArray = actualItems.toArray(new Item[0]);
            assertItemListEqual(expectedArrays[i], actualArray);
        }
    }

    private void assertItemListEqual(Item[] expectedArray, Item[] actualArray) {
        // Assert that the arrays match
        assertArrayEquals("The array of items should match the expected array", expectedArray, actualArray);
    }

    @Test
    public void testChangeKitchenName() {
        // Setup
        String newName = "testKitchen";
        kitchen.changeKitchenName(newName);
        assertEquals("Name should be updated", newName, kitchen.getKitchenName());
    }
    @Test
    public void testItemCloseToExpiry() throws InvalidQuantityException {
        // Remove all items from the kitchen
        for (int i = 0; i < items.length; i++) {
            kitchen.removeItem(items[i]);
        }

        for (int i = 0; i < 3; i++) {
            kitchen.addItem(items[i]);
            int actual =0;
            for (Item item : kitchen.getItemList()) {
                actual += kitchen.itemCloseTOExpiry(item);
            }
            assertEquals(i+1, actual);
        }
    }
    @Test
    public void testItemHasExpired() throws InvalidQuantityException {
        for (int i = 0; i < items.length; i++) {
            kitchen.removeItem(items[i]);
        }
        for (int i = 0; i < 3; i++) {
            kitchen.addItem(items[i]);
            int actual =0;
            for (Item item : items) {
                actual += kitchen.itemOutOfExpiry(item);
            }
            assertEquals(0, actual);
        }
    }

    @Test
    public void testAddItemUsage() {
        ItemUsage itemUsage = itemUsages[0];
        kitchen.addItemUsage(itemUsage);
        assertTrue(kitchen.getItemUsageList().contains(itemUsage));
        itemUsage = itemUsages[1];
        kitchen.addItemUsage(itemUsage);
        assertTrue(kitchen.getItemUsageList().contains(itemUsage));
        itemUsage = itemUsages[3];
        kitchen.addItemUsage(itemUsage);
        assertTrue(kitchen.getItemUsageList().contains(itemUsage));

    }

    @Test
    public void testUseItem() throws InvalidQuantityException {

        for (int i = 0; i < items.length; i++) {
            kitchen.removeItem(items[i]);
        }
        Item item = items[0];
        kitchen.addItem(item);
        assertTrue(kitchen.useItem(item, 5, testUser));

        assertFalse(kitchen.useItem(item, 20, testUser));

        assertFalse(kitchen.useItem(items[4],10,testUser));
    }


    @Test
    public void testReduceItem() throws InvalidQuantityException {
        for (int i = 0; i < items.length; i++) {
            kitchen.removeItem(items[i]);
        }
        kitchen.addItem(items[4]);
        kitchen.reduceItem(items[4], 5);
        assertEquals(5, kitchen.getItemList().get(0).getQuantity());
    }


}
