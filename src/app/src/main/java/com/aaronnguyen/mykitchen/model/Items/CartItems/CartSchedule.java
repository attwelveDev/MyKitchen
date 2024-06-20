package com.aaronnguyen.mykitchen.model.Items.CartItems;
import com.aaronnguyen.mykitchen.model.Items.Schedule;
import com.aaronnguyen.mykitchen.model.user.User;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A schedule belonging to a Cart Item
 * @author u7648367 Ruixian Wu (adapted from u7643339 Isaac Leong)
 */
public class CartSchedule extends Schedule {
    public static final String SCHEDULE_DAYS_REOCCURRING_FIELD_NAME = "Days reoccurring";

    /**
     * Date of the first time the schedule was executed
     */

    public static final DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
    /**
     * The schedule will repeat after this many days. If 0, then the schedule does not repeat
     */
    final int daysReoccurring;


    public CartSchedule(Date scheduledDate, int scheduledQuantity, int daysReoccurring, User user) {
        super(scheduledDate, scheduledQuantity, user);
        this.daysReoccurring = daysReoccurring;
    }

    public CartSchedule(Date scheduleDate, int scheduledQuantity, int daysReoccurring, String uemail, String uid, String scheduleID) {
        super (scheduleDate,scheduledQuantity,uemail,uid,scheduleID);
        this.daysReoccurring = daysReoccurring;
    }


    /**
     * Extracts the string representation of a certain field from the string representation of a schedule
     * @param scheduleString is the string representation of a string as specified by toString()
     * @param desiredField The name of the Schedule field that we want to extract from the string
     * @return
     */
    public static String getFieldStringFromScheduleString(String scheduleString, String desiredField) {
        String[] scheduleStringFields = scheduleString.split("_");


        if (scheduleStringFields.length == 6) {
            switch(desiredField) {
                case SCHEDULE_DATE_FIELD_NAME:
                    return scheduleStringFields[0];

                case SCHEDULE_QUANTITY_FIELD_NAME:
                    return scheduleStringFields[1];

                case SCHEDULE_DAYS_REOCCURRING_FIELD_NAME:
                    return scheduleStringFields[2];

                case SCHEDULE_ID_FIELD_NAME:
                    return scheduleStringFields[3];

                case SCHEDULE_UID_FIELD_NAME:
                    return scheduleStringFields[4];

                case SCHEDULE_UEMAIL_FIELD_NAME:
                    return scheduleStringFields[5];

                default:
                    throw new IllegalArgumentException("Invalid desired field");

            }
        }

        throw new IllegalArgumentException("Fields in the string should be separated by \"_\"");
    }


    /**
     * Creates a CartSchedule from the string representation of a CartSchedule, based on the assumption that the string is formatted as specified in toString()
     * @param string
     */
    public CartSchedule(String string) {
        super();
        String[] stringFields = string.split("_");
        if (stringFields.length == 6) {
            try {
                this.scheduledDate = df.parse(stringFields[0]);
                this.scheduledQuantity = Integer.parseInt(stringFields[1]);
                this.daysReoccurring = Integer.parseInt(stringFields[2]);
                this.scheduleID = toNull(stringFields[3]);
                this.uid = toNull(stringFields[4]);
                this.uemail = toNull(stringFields[5]);

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException("Fields in the string should be separated by \"_\"");
        }
    }
    public int getDaysReoccurring() {
        return daysReoccurring;
    }

    public boolean isReoccurring() {
        return daysReoccurring != 0;
    }

    /**
     *
     * @return a map representing the current CartSchedule that can be uploaded to FireStore
     */
    @Override
    public Map<String, Object> toJsonObject() {
        Map<String, Object> jsonObject = new HashMap<>();
        jsonObject.put(SCHEDULE_DATE_FIELD_NAME, df.format(scheduledDate));
        jsonObject.put(SCHEDULE_QUANTITY_FIELD_NAME, String.valueOf(scheduledQuantity));
        jsonObject.put(SCHEDULE_DAYS_REOCCURRING_FIELD_NAME, String.valueOf(daysReoccurring));
        jsonObject.put(SCHEDULE_ID_FIELD_NAME, scheduleID);
        jsonObject.put(SCHEDULE_UID_FIELD_NAME, uid);
        jsonObject.put(SCHEDULE_UEMAIL_FIELD_NAME, uemail);

        return jsonObject;
    }


    @Override
    public String toString() {
        return df.format(scheduledDate)
                + "_" + scheduledQuantity
                + "_" + daysReoccurring
                + "_" + scheduleID
                + "_" + uid
                + "_" + uemail;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        try {
            CartSchedule schedule = (CartSchedule) o;
            return Objects.equals(scheduledDate, schedule.getScheduledDate())
                    && scheduledQuantity == schedule.getScheduledQuantity()
                    && daysReoccurring == schedule.getDaysReoccurring()
                    && Objects.equals(scheduleID, schedule.getScheduleID())
                    && Objects.equals(uid, schedule.getUid())
                    && Objects.equals(uemail, schedule.getUemail());
        } catch(ClassCastException e) {
            return false;
        }
    }

}
