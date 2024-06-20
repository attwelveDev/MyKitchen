package com.aaronnguyen.mykitchen;
import static org.junit.Assert.*;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Item;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemUseSchedule;
import com.aaronnguyen.mykitchen.model.user.User;

import org.junit.Before;
import org.junit.Test;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ItemCheckTest {
    User testUser = null;


    @Before
    public void setUp() throws Exception {
        testUser = new User("test","test","test@126.com",null);
    }
    @Test
    public void testIsCloseToExpiry() {
        Calendar todayCalendar = Calendar.getInstance();
        Date today = todayCalendar.getTime();
        todayCalendar.add(Calendar.DAY_OF_MONTH, 3); // Three days from now
        Date expiryDate = todayCalendar.getTime();
        Item testItem1 = new TestItem("Test Item 1", expiryDate, today, testUser, 1, "", null);

        todayCalendar = Calendar.getInstance();
        today = todayCalendar.getTime();
        todayCalendar.add(Calendar.DAY_OF_MONTH, 2); // Two days from now
        expiryDate = todayCalendar.getTime();
        Item testItem2 = new TestItem("Test Item 2", expiryDate, today, testUser, 1, "", null);

        assertTrue(testItem1.isCloseToExpiry());
        assertTrue(testItem2.isCloseToExpiry());
    }

    @Test
    public void testIsNotCloseToExpiry() {
        Calendar todayCalendar = Calendar.getInstance();
        Date today = todayCalendar.getTime();
        todayCalendar.add(Calendar.DAY_OF_MONTH, 5); // Five days from now
        Date expiryDate = todayCalendar.getTime();
        Item testItem1 = new TestItem("Test Item 1", expiryDate, today, testUser, 1, "", null);

        todayCalendar = Calendar.getInstance();
        today = todayCalendar.getTime();
        todayCalendar.add(Calendar.DAY_OF_MONTH, 10); // Ten days from now
        expiryDate = todayCalendar.getTime();
        Item testItem2 = new TestItem("Test Item 2", expiryDate, today, testUser, 1, "", null);

        assertFalse(testItem1.isCloseToExpiry());
        assertFalse(testItem2.isCloseToExpiry());
    }

    @Test
    public void testOutOfExpiry() {
        Calendar todayCalendar = Calendar.getInstance();
        Date today = todayCalendar.getTime();
        todayCalendar.add(Calendar.DAY_OF_MONTH, -5); // Five days ago
        Date expiryDate = todayCalendar.getTime();
        Item testItem1 = new TestItem("Test Item 1", expiryDate, today, testUser, 1, "", null);

        todayCalendar = Calendar.getInstance();
        today = todayCalendar.getTime();
        todayCalendar.add(Calendar.DAY_OF_MONTH, -2); // Two days ago
        expiryDate = todayCalendar.getTime();
        Item testItem2 = new TestItem("Test Item 2", expiryDate, today, testUser, 1, "", null);

        assertTrue(testItem1.OutOfExpiry());
        assertTrue(testItem2.OutOfExpiry());
    }

    @Test
    public void testNotOutOfExpiry() {
        Calendar todayCalendar = Calendar.getInstance();
        Date today = todayCalendar.getTime();
        todayCalendar.add(Calendar.DAY_OF_MONTH, 5); // Five days from now
        Date expiryDate = todayCalendar.getTime();
        Item testItem1 = new TestItem("Test Item 1", expiryDate, today, testUser, 1, "", null);

        todayCalendar = Calendar.getInstance();
        today = todayCalendar.getTime();
        todayCalendar.add(Calendar.DAY_OF_MONTH, 10); // Ten days from now
        expiryDate = todayCalendar.getTime();
        Item testItem2 = new TestItem("Test Item 2", expiryDate, today, testUser, 1, "", null);

        assertFalse(testItem1.OutOfExpiry());
        assertFalse(testItem2.OutOfExpiry());
    }

    @Test
    public void testDaysBeforeExpiry() {
        Calendar todayCalendar = Calendar.getInstance();
        Date today = todayCalendar.getTime();
        todayCalendar.add(Calendar.DAY_OF_MONTH, 5); // Five days from now
        Date expiryDate = todayCalendar.getTime();
        Item testItem1 = new TestItem("Test Item 1", expiryDate, today, testUser, 1, "", null);

        todayCalendar = Calendar.getInstance();
        today = todayCalendar.getTime();
        todayCalendar.add(Calendar.DAY_OF_MONTH, 10); // Ten days from now
        expiryDate = todayCalendar.getTime();
        Item testItem2 = new TestItem("Test Item 2", expiryDate, today, testUser, 1, "", null);

        assertEquals(5, testItem1.DaysBeforeExpiry());
        assertEquals(10, testItem2.DaysBeforeExpiry());
    }


    private class TestItem extends Item {
        public TestItem(String name, Date expiryDate, Date boughtDate, User associatedUser, int quantity, String storageLocation, List<ItemUseSchedule> schedule) {
            super(name, expiryDate, boughtDate, associatedUser, quantity, storageLocation, schedule);
        }
    }
}
