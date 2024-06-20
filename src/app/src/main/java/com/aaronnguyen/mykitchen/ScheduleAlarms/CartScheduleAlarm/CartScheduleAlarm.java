package com.aaronnguyen.mykitchen.ScheduleAlarms.CartScheduleAlarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aaronnguyen.mykitchen.model.Items.CartItems.CartItem;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartSchedule;
import com.aaronnguyen.mykitchen.model.kitchen.Kitchen;
import com.aaronnguyen.mykitchen.ui.main.ShoppingCart.ShoppingCartFragment;

import java.util.Calendar;

/**
 * Manages the existence of Alarm Managers on a device for CartSchedules
 * @author u7648367 Ruixian Wu
 */
public class CartScheduleAlarm {
    /**
     * Field name for: FireStore id of the kitchen that contains the item we are adding a schedule to
     */
    public static final String KITCHEN_ID = "kitchenID";
    /**
     * Field name for:Name of the item that we are adding a schedule to
     */
    public static final String ITEM_NAME = "itemName";
    /**
     * Field name for:ID of the schedule we are setting an alarm for
     */
    public static final String SCHEDULE_ID = "scheduleID";
    /**
     * Field name for: Numbers of days between the schedule alarm repeating.
     * Value is zero is the schedule alarm does not repeat
     */
    public static final String SCHEDULE_DAYS_REOCCURRING = "daysReoccurring";
    /**
     * Field name for: UID of the user that is setting this schedule and alarm
     */
    public static final String UID = "uid";
    /**
     * Field name for: Request code of the pending intent we are using to set the schedule alarm
     */
    public static final String REQUEST_CODE = "requestCode";

    /**
     * Set an alarm manager on this device to execute a Cart Schedule
     * @param fragment
     * @param kitchen that the schedule is set for
     * @param item that the Cart Schedule is set for
     * @param schedule The schedule being set on the device
     */
    public CartScheduleAlarm(ShoppingCartFragment fragment, Kitchen kitchen, CartItem item, CartSchedule schedule) {
        //Reference: https://stackoverflow.com/a/1082836
        AlarmManager alarmManager = (AlarmManager) fragment.requireContext().getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(fragment.requireContext().getApplicationContext(), CartScheduleReceiver.class);

        int requestCode = schedule.getRequestCode();

        intent.putExtra(KITCHEN_ID,kitchen.getKitchenID());
        intent.putExtra(ITEM_NAME,item.getName());
        intent.putExtra(SCHEDULE_DAYS_REOCCURRING, schedule.getDaysReoccurring());
        intent.putExtra(UID, schedule.getUid());
        intent.putExtra(SCHEDULE_ID,schedule.getScheduleID());
        intent.putExtra(REQUEST_CODE, requestCode);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(fragment.requireContext().getApplicationContext(), requestCode, intent, PendingIntent.FLAG_IMMUTABLE);

        int reOccurringMilliseconds = schedule.getDaysReoccurring() * 24 * 60 * 60 * 1000;


        if (schedule.isReoccurring()) {
            Log.i("DEBUG", "repeating cart schedule");
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, schedule.getScheduledDate().getTime(), reOccurringMilliseconds, pendingIntent);
        } else {
            Log.i("DEBUG", "non-repeating cart schedule");
            alarmManager.set(AlarmManager.RTC_WAKEUP, schedule.getScheduledDate().getTime(), pendingIntent);
        }

    }

    /**
     * Cancel the alarm of a cart schedule
     * @param requestCode of the schedule we want to cancel
     * @param context
     */
    public static void cancelCartSchedule(int requestCode, Context context) {
        Log.i("DEBUG", "cart schedule cancelled");
        Intent intent = new Intent(context, CartScheduleReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);
        pendingIntent.cancel();
    }


}
