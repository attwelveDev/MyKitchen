package com.aaronnguyen.mykitchen.ScheduleAlarms.CartScheduleAlarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aaronnguyen.mykitchen.DAO.KitchenFirebaseDAO;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Class specifies how the device should behave when receiving intents specific to this BroadcastReceiver class
 * BroadCast Receiver for CartScheduleAlarm
 * @author u7648367 Ruixian Wu
 */
public class CartScheduleReceiver extends BroadcastReceiver {
    public CartScheduleReceiver() { }

    /**
     * On receiving an intent, write to FireStore as outlined by the intent
     * @param context in which the receiver is running in
     * @param intent The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("DEBUG", "received broadcast");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String kitchenID = intent.getStringExtra(CartScheduleAlarm.KITCHEN_ID);

        String itemName = intent.getStringExtra(CartScheduleAlarm.ITEM_NAME);
        int scheduleDaysReoccurring = intent.getIntExtra(CartScheduleAlarm.SCHEDULE_DAYS_REOCCURRING, 0);
        String uid = intent.getStringExtra(CartScheduleAlarm.UID);
        String scheduleID = intent.getStringExtra(CartScheduleAlarm.SCHEDULE_ID);
        int requestCode = intent.getIntExtra(CartScheduleAlarm.REQUEST_CODE, 0);

        KitchenFirebaseDAO.getInstance().executeCartSchedule(kitchenID, uid, itemName, scheduleID, scheduleDaysReoccurring, requestCode, context);

    }
}