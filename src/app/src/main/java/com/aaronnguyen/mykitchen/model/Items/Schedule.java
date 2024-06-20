package com.aaronnguyen.mykitchen.model.Items;

import com.aaronnguyen.mykitchen.model.user.User;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * Super class of ItemUseSchedule and CartSchedule
 * A schedule specifies a change in quantity to an item or cart item of a certain kitchen
 * And this change is to be made at a future time
 * @author u7648367 Ruixian Wu
 */
public abstract class Schedule {
    public static final String SCHEDULE_DATE_FIELD_NAME = "Schedule date";
    public static final String SCHEDULE_QUANTITY_FIELD_NAME = "Quantity";
    public static final String SCHEDULE_ID_FIELD_NAME = "Schedule id";
    public static final String SCHEDULE_UEMAIL_FIELD_NAME = "Schedule uemail";
    public static final String SCHEDULE_UID_FIELD_NAME = "Schedule uid";


    /**
     * Date the schedule changes will take place
     */
    protected Date scheduledDate;
    /**
     * For ItemUseSchedule, the quantity we want to reduce a certain Item by
     * For CartSchedule, the quantity we want to add to a certain Cart Item
     */
    protected int scheduledQuantity;
    /**
     * Email of the user that set the schedule
     */
    protected String uemail;
    /**
     * ID of the user that set the schedule
     */
    protected String uid;
    /**
     * Unique ID of the schedule
     * Contains the request code of the AlarmManager alarm that belongs to this schedule
     */
    protected String scheduleID;

    public Schedule(Date scheduledDate, int scheduledQuantity, User user) {
        this.scheduledDate = scheduledDate;
        this.scheduledQuantity = scheduledQuantity;
        this.uemail = user.getEmail();
        this.uid = user.getUid();
        this.scheduleID = generateID(user);

    }

    public Schedule(Date scheduledDate, int scheduledQuantity, String uemail, String uid, String scheduleID) {
        this.scheduledDate = scheduledDate;
        this.scheduledQuantity = scheduledQuantity;
        this.uemail = uemail;
        this.uid = uid;
        this.scheduleID = scheduleID;
    }

    public Schedule() {}

    /**
     * Creates a unique ID for the schedule
     * @param user
     * @return scheduleID
     */
    public static String generateID(User user) {
        return user.getUid() + "/" + generateRequestCode();
    }

    /**
     * Used in calculating the pending intent request code of the AlarmManager alarm belonging to this schedule
     * @return the number of milliseconds between January 1 2024 from January 1 1970
     */
    private static double returnBaseDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR,2024);
        calendar.set(Calendar.DATE,1);
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);
        return (double) calendar.getTimeInMillis();
    }

    /**
     * @return new request code to be given to a pending intent
     */
    private static int generateRequestCode() {
        return ((int) (((double) (Calendar.getInstance().getTimeInMillis()))- returnBaseDate())/1000) - Integer.MIN_VALUE;
    }

    public int getRequestCode() {
        return Integer.parseInt(scheduleID.split("/")[1]);

    }

    /**
     *
     * @return a map representign a schedule that can be uploaded to FireStore
     */
    public abstract Map<String, Object> toJsonObject();

    public Date getScheduledDate() {
        return scheduledDate;
    };
    public int getScheduledQuantity() {
        return scheduledQuantity;
    };

    public String getScheduleID() {
        return scheduleID;
    }

    public String getUemail() {
        return uemail;
    }
    public String getUid() {
        return uid;
    }

    /**
     * Ensures that null values fetched from FireStore are parsed as Null and not a string "null"
     * @param string
     * @return null if the string spells out null, the string itself otherwise
     */
    public String toNull(String string) {
        if (string.equals("null")) {
            return null;
        } else {
            return string;
        }
    }
}
