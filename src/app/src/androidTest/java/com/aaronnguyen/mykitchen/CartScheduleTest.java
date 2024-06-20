package com.aaronnguyen.mykitchen;

import com.aaronnguyen.mykitchen.model.Items.CartItems.CartSchedule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CartScheduleTest {
    ArrayList<CartSchedule> cartSchedules = new ArrayList<>();
    ArrayList<String> cartScheduleStrings = new ArrayList<>();

    @Before
    public void setUpCartSchedules() {
        Date date1 = new Date();
        Date date2 = new Date();
        Date date3 = new Date();

        try {
            date1 = (CartSchedule.df).parse("24/05/2020");
            date2 = (CartSchedule.df).parse("12/12/2025");
            date3 = (CartSchedule.df).parse("04/01/2022");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        cartSchedules.add(new CartSchedule(date1, 5, 4, "a@a.com", "aaa", "aefr/345"));
        cartSchedules.add(new CartSchedule(date2, 1, 0, "abc@abc.com", "aaaaef", "aaefAFSefFr/345eaf"));
        cartSchedules.add(new CartSchedule(date1, 5, 4, "a@a.com", "aaa", "aefr/345"));
        cartSchedules.add(new CartSchedule(date1, 5, 4, "aa@a.com", "aaa", "aefr/345"));
        cartSchedules.add(new CartSchedule(date1, 4, 4, "a@a.com", "aaa", "aefr/345"));
        cartSchedules.add(new CartSchedule(date1, 5, 4, "a@a.com", "aa", "aefr/345"));
        cartSchedules.add(new CartSchedule(date1, 5, 4, "a@a.com", "aaa", "aefr/-345"));
        cartSchedules.add(new CartSchedule(date2, 5, 4, "a@a.com", "aaa", "aefr/-345"));

        cartScheduleStrings.add("24/05/2020_5_4_aefr/345_aaa_a@a.com");
        cartScheduleStrings.add("12/12/2025_1_0_aaefAFSefFr/345eaf_aaaaef_abc@abc.com");

    }
    @Test
    public void toStringStandard() {

        String expected1 = cartScheduleStrings.get(0);
        Assert.assertEquals(expected1, cartSchedules.get(0).toString());

        String expected2 = cartScheduleStrings.get(1);
        Assert.assertEquals(expected2, cartSchedules.get(1).toString());

    }

    @Test
    public void getFieldStringFromScheduleStringTest() {
        Assert.assertEquals("24/05/2020", CartSchedule.getFieldStringFromScheduleString(cartSchedules.get(0).toString(), CartSchedule.SCHEDULE_DATE_FIELD_NAME));
        Assert.assertEquals("5", CartSchedule.getFieldStringFromScheduleString(cartSchedules.get(0).toString(), CartSchedule.SCHEDULE_QUANTITY_FIELD_NAME));
        Assert.assertEquals("4", CartSchedule.getFieldStringFromScheduleString(cartSchedules.get(0).toString(), CartSchedule.SCHEDULE_DAYS_REOCCURRING_FIELD_NAME));
        Assert.assertEquals("a@a.com", CartSchedule.getFieldStringFromScheduleString(cartSchedules.get(0).toString(), CartSchedule.SCHEDULE_UEMAIL_FIELD_NAME));
        Assert.assertEquals("aaa", CartSchedule.getFieldStringFromScheduleString(cartSchedules.get(0).toString(), CartSchedule.SCHEDULE_UID_FIELD_NAME));
        Assert.assertEquals("aefr/345", CartSchedule.getFieldStringFromScheduleString(cartSchedules.get(0).toString(), CartSchedule.SCHEDULE_ID_FIELD_NAME));
    }

    @Test
    public void CartScheduleStringConstructorTest() {
        Assert.assertEquals(cartSchedules.get(0), new CartSchedule(cartScheduleStrings.get(0)));
        Assert.assertEquals(cartSchedules.get(1), new CartSchedule(cartScheduleStrings.get(1)));
    }


    @Test
    public void equals() {
        Assert.assertEquals(cartSchedules.get(0), cartSchedules.get(0));
        Assert.assertEquals(cartSchedules.get(0), cartSchedules.get(2));
        Assert.assertNotEquals(cartSchedules.get(0), cartSchedules.get(3));
        Assert.assertNotEquals(cartSchedules.get(0), cartSchedules.get(4));
        Assert.assertNotEquals(cartSchedules.get(0), cartSchedules.get(5));
        Assert.assertNotEquals(cartSchedules.get(0), cartSchedules.get(6));
        Assert.assertNotEquals(cartSchedules.get(0), cartSchedules.get(6));

    }
}
