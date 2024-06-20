package com.aaronnguyen.mykitchen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import androidx.core.app.RemoteInput;

import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Item;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemFactory;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemUseSchedule;
import com.aaronnguyen.mykitchen.model.user.User;
import com.aaronnguyen.mykitchen.ui.main.Overview.OverviewViewModel;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class ItemTest {
    static User testUser = new User("000 Test User", "I am Groot", "000testkitchen@testkitchen.com", Collections.singletonList("000 Test Kitchen"));
    static List<Date> itemDates = new ArrayList<>();
    static List<List<ItemUseSchedule>> schedules = new ArrayList<List<ItemUseSchedule>>();

    @Before
    public void setUpItemsAndSchedules() {
        DateFormat itemDF = new SimpleDateFormat(OverviewViewModel.DATE_DISPLAY_FORMAT);
        Date itemDate1 = new Date();
        Date itemDate2 = new Date();

        Date itemScheduleDate1 = new Date();
        Date itemScheduleDate2 = new Date();

        try {
            itemDate1 = itemDF.parse("03/12/2005");
            itemDate2 = itemDF.parse("14/02/2015");

            itemScheduleDate1 = (ItemUseSchedule.df).parse("30/11/2017 12:54");
            itemScheduleDate2 = (ItemUseSchedule.df).parse("15/08/2024 5:32");
        } catch (ParseException e) {
            fail("setUpItemsAndSchedules has poorly formatted dates");
        }

        itemDates.add(itemDate1);
        itemDates.add(itemDate2);

        ArrayList<ItemUseSchedule> schedule1 = new ArrayList<>();
        schedule1.add(new ItemUseSchedule(itemScheduleDate1, 4, "uemail", "uid", "AEFRDSsdfeWQ3224sdFG/-40023043"));

        ArrayList<ItemUseSchedule> schedule2 = new ArrayList<>();
        schedule2.add(new ItemUseSchedule(itemScheduleDate2, 7, "uemail2", "uid2", "uid/3"));
        schedule2.add(new ItemUseSchedule(itemScheduleDate1, 20, "uemail3", "uid3", "SKMdsm34/-43"));

        schedules.add(schedule1);
        schedules.add(schedule2);

    }
    @Test
    public void testEquals() {

        Item item1 = ItemFactory.createItem("Tuna", "Protein", itemDates.get(0), itemDates.get(1), testUser, 4, "hole", new ArrayList<>());
        Assert.assertEquals(item1, item1);
        Assert.assertNotEquals(null, item1);

        //Items with different schedules are not equal
        Item item2 = ItemFactory.createItem("Tuna", "Protein", itemDates.get(0), itemDates.get(1), testUser, 4, "hole", schedules.get(1));
        Assert.assertNotEquals(item1, item2);

    }
}
