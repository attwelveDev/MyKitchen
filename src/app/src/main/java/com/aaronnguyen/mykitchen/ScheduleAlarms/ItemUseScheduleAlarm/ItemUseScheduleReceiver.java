package com.aaronnguyen.mykitchen.ScheduleAlarms.ItemUseScheduleAlarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aaronnguyen.mykitchen.DAO.KitchenFirebaseDAO;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemUseSchedule;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
/**
 * Class specifies how the device should behave when receiving intents specific to this BroadcastReceiver class
 * BroadCast Receiver for ItemUseScheduleAlarm
 * @author u7648367 Ruixian Wu
 */
public class ItemUseScheduleReceiver extends BroadcastReceiver {

    public ItemUseScheduleReceiver(){ }

    /**
     * On receiving an intent, write to FireStore as outlined by the intent
     * @param context in which the receiver is running in
     * @param intent The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("DEBUG","alarm went off at" + ItemUseSchedule.calendarToString(Calendar.getInstance()));
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String kitchenID = intent.getStringExtra(ItemUseScheduleAlarm.KITCHEN_ID);
        String itemName = intent.getStringExtra(ItemUseScheduleAlarm.ITEM_NAME);
        int scheduleQuantity = intent.getIntExtra(ItemUseScheduleAlarm.SCHEDULE_QUANTITY,0);
        String scheduleID = intent.getStringExtra(ItemUseScheduleAlarm.SCHEDULE_ID);
        String uemail = intent.getStringExtra(ItemUseScheduleAlarm.UEMAIL);
        String uid = intent.getStringExtra(ItemUseScheduleAlarm.UID);

        KitchenFirebaseDAO.getInstance().executeItemUseSchedule(kitchenID, uid, itemName, scheduleQuantity, scheduleID, uemail);
    }


}
