package com.aaronnguyen.mykitchen.model.Items.KitchenItems;

import android.util.Log;

import com.aaronnguyen.mykitchen.model.Items.Schedule;
import com.aaronnguyen.mykitchen.model.user.User;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ItemUseSchedule extends Schedule {

    public static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final String regex = "///";

    public ItemUseSchedule(Date scheduledDate, int scheduledQuantity, User user) {
        super(scheduledDate, scheduledQuantity, user);
    }

    public ItemUseSchedule(Date scheduledDate, int scheduledQuantity, String uemail, String uid, String scheduleID) {
        super(scheduledDate, scheduledQuantity, uemail, uid, scheduleID);
    }

    @Override
    public Map<String,Object> toJsonObject() {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(SCHEDULE_DATE_FIELD_NAME, df.format(scheduledDate));
        jsonObject.put(SCHEDULE_QUANTITY_FIELD_NAME, String.valueOf(scheduledQuantity));
        jsonObject.put(SCHEDULE_ID_FIELD_NAME, scheduleID);
        jsonObject.put(SCHEDULE_UEMAIL_FIELD_NAME, uemail);
        jsonObject.put(SCHEDULE_UID_FIELD_NAME, uid);

        return jsonObject;
    }

    @Override
    public String toString() {
        return df.format(scheduledDate) + regex + scheduledQuantity + regex + scheduleID + regex + uemail + regex + uid;
    }

    public static String calendarToString(Calendar calendar) {
        return calendar.get(Calendar.DATE) + "/" + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR)
                + "/" + calendar.get(Calendar.HOUR_OF_DAY) + "/" + calendar.get(Calendar.MINUTE);
    }

    public ItemUseSchedule(String s) {
        String[] fields = s.split(regex);
        if (fields.length == 5) {
            try {
                scheduledDate = df.parse(fields[0]);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            scheduledQuantity = Integer.parseInt(fields[1]);
            scheduleID = toNull(fields[2]);
            uemail = toNull(fields[3]);
            uid = toNull(fields[4]);
        }
    }

    public static int getScheduleQuantityFromString(String s) {
        String[] fields = s.split(regex);
        if (fields.length == 5) {
            return Integer.parseInt(fields[1]);
        }
        return -1;
    }

    public static String dateBtnStringFromScheduleString(String s) {
        String[] fields = s.split("///");
        if (fields.length == 5) {
            String[] dateStrings = fields[0].split(" ");
            String dateString = dateStrings[0];
            String timeString = dateStrings[1];
            String[] timeStrings = timeString.split(":");
            String hourString = timeStrings[0];
            String minuteString = timeStrings[1];

            String btnString = dateString + " ";
            int hour =  Integer.parseInt(hourString);
            String AMPM = (hour > 11 ? "pm" : "am");
            if (hour == 0) {
                hour = 12;
            } else {
                hour = hour > 12 ? hour - 12 : hour;
            }
            btnString = btnString + hour + ":";
            return btnString + minuteString + AMPM;
        }
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemUseSchedule schedule = (ItemUseSchedule) o;
        if (uid == null) {
            Log.i("DEBUG","uid == null");
        } else {
            Log.i("DEBUG","uid: " + uid);
        }

        if (schedule == null) {
            Log.i("DEBUG","schedule == null");
        } else {
            Log.i("DEBUG","schedule not null ");
        }


        if (schedule.uid == null) {
            Log.i("DEBUG","schedule uid == null");
        } else {
            Log.i("DEBUG","schedule.uid: " + schedule.uid);
        }


        return dateEquals(scheduledDate,schedule.scheduledDate)
                && scheduledQuantity == schedule.scheduledQuantity
                && scheduleID.equals(schedule.scheduleID)
                && Objects.equals(uid,schedule.uid)
                && Objects.equals(uemail,schedule.uemail);

    }

    public boolean dateEquals(Date a, Date b) {
        return (df.format(a)).equals(df.format(b));
    }

}
