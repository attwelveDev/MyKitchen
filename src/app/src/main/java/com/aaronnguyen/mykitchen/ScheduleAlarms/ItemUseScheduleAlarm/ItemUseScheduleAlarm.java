package com.aaronnguyen.mykitchen.ScheduleAlarms.ItemUseScheduleAlarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aaronnguyen.mykitchen.model.Items.KitchenItems.Item;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemUseSchedule;
import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.ui.main.Overview.KitchenOverviewFragment;

import java.util.Calendar;

/**
 * Manages the existence of Alarm Managers on a device for ItemUseSchedules
 * @author u7648367 Ruixian Wu
 */
public class ItemUseScheduleAlarm {

    /**
     * Field name for: FireStore id of the kitchen that contains the item we are adding a schedule to
     */
    public static final String KITCHEN_ID = "kitchenID";
    /**
     * Field name for: Name of the item that we are adding a schedule to
     */
    public static final String ITEM_NAME = "itemName";
    /**
     * Field name for: The amount by which ItemUseSchedules decreases the quantity of an item
     */
    public static final String SCHEDULE_QUANTITY = "scheduleQuantity";
    /**
     * Field name for: ID of the schedule we are setting an alarm for
     */
    public static final String SCHEDULE_ID = "scheduleID";
    /**
     * Field name for: email of the user setting the schedule
     */
    public static final String UEMAIL = "uemail";
    /**
     * Field name for: UID of the user setting the schedule
     */
    public static final String UID = "uid";

    /**
     * Set an alarm manager on this device to execute an ItemUseSchedule
     * @param overviewFragment
     * @param kitchen that the schedule is set for
     * @param item that the ItemUseSchedule is set for
     * @param schedule The schedule being set on the device
     */
    public ItemUseScheduleAlarm(KitchenOverviewFragment overviewFragment, Kitchen kitchen, Item item, ItemUseSchedule schedule) {
        //Reference: https://stackoverflow.com/a/1082836
        AlarmManager alarmManager = (AlarmManager) overviewFragment.getContext().getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(overviewFragment.getContext().getApplicationContext(), ItemUseScheduleReceiver.class);

        int requestCode = schedule.getRequestCode();

        intent.putExtra(KITCHEN_ID, kitchen.getKitchenID());
        intent.putExtra(ITEM_NAME, item.getName());
        intent.putExtra(SCHEDULE_QUANTITY, schedule.getScheduledQuantity());
        intent.putExtra(SCHEDULE_ID, schedule.getScheduleID());
        intent.putExtra(UEMAIL, schedule.getUemail());
        intent.putExtra(UID, schedule.getUid());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(overviewFragment.getContext().getApplicationContext(), requestCode, intent, PendingIntent.FLAG_IMMUTABLE);
        Log.i("DEBUG", "pending intent " + requestCode);
        alarmManager.set(AlarmManager.RTC_WAKEUP, schedule.getScheduledDate().getTime(), pendingIntent);

    }

    /**
     * Cancel the alarm of an ItemUseSchedule
     * @param requestCode of the schedule we want to cancel
     * @param context
     */
    public static void cancelItemUseSchedule(int requestCode, Context context) {
        Log.i("DEBUG", "item use schedule cancelled");
        Intent intent = new Intent(context, ItemUseScheduleReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);
        pendingIntent.cancel();
    }
}