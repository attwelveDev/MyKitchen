package com.aaronnguyen.mykitchen.model.notification;

import java.util.Map;

/**
 * @author u7517596 Chengbo Yan
 */
public interface Notification extends Comparable<Notification> {
    String MESSAGE = "message";
    String  IMPORTANCE= "importance";
    int getImportance();
    String getText();
    Map<String, Object> toJSONObject();
}
