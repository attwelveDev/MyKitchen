package com.aaronnguyen.mykitchen.ui.main.Overview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemUseSchedule;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {
    private final Context context;
    private final Map<String, List<String>> itemCollection;
    private final Map<String, List<String>> itemExpiryDate;
    private final Map<String, List<String>> itemBoughtDate;
    private final Map<String, List<String>> itemQuantity;
    private final Map<String, List<List<String>>> itemSchedule;
    private final Map<String, List<String>> itemStorageLocation;
    private final List<String> groupList;
    public MyExpandableListAdapter(Context context,
                                   Map<String, List<String>> itemCollection,
                                   Map<String, List<String>> itemExpiryDate,
                                   Map<String, List<String>> itemBoughtDate,
                                   Map<String, List<String>> itemQuantity,
                                   Map<String, List<String>> itemStorageLocation,
                                   Map<String, List<List<String>>> itemSchedule,
                                   List<String> groupList) {
        this.context = context;
        this.itemCollection = itemCollection;
        this.itemExpiryDate = itemExpiryDate;
        this.itemBoughtDate = itemBoughtDate;
        this.itemQuantity = itemQuantity;
        this.itemSchedule = itemSchedule;
        this.itemStorageLocation = itemStorageLocation;
        this.groupList = groupList;
    }

    @Override
    public int getGroupCount() {
        return itemCollection.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return Objects.requireNonNull(itemCollection.get(groupList.get(groupPosition))).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groupList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return itemCollection.get(groupList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String categoryName = getGroup(groupPosition).toString();
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.group_item, null);
        }
        TextView item = convertView.findViewById(R.id.food_categories);
        item.setText(categoryName);

        TextView childCountTextView = convertView.findViewById(R.id.child_count_lbl);
        childCountTextView.setText(String.valueOf(getChildrenCount(groupPosition)));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        String foodName = getChild(groupPosition, childPosition).toString();
        String expiryDate = itemExpiryDate.get(groupList.get(groupPosition)).get(childPosition);
        String boughtDate = itemBoughtDate.get(groupList.get(groupPosition)).get(childPosition);
        List<String> scheduleUse = itemSchedule.get(groupList.get(groupPosition)).get(childPosition);
        String quantity = itemQuantity.get(groupList.get(groupPosition)).get(childPosition);
        String storageLocation = itemStorageLocation.get(groupList.get(groupPosition)).get(childPosition);
        storageLocation = storageLocation.substring(0, 1).toUpperCase() + storageLocation.substring(1);  // capitalise first letter

        // TODO: add storage location

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.child_item, null);
        }

        TextView item = convertView.findViewById(R.id.food_name);
        TextView expiryView = convertView.findViewById(R.id.expiry_date_text);
        TextView boughtView = convertView.findViewById(R.id.bought_date_text);
        TextView quantityView = convertView.findViewById(R.id.quantity_text);
        TextView scheduleQuantityView = convertView.findViewById(R.id.total_schedule_use_quantity);
        TextView storageView = convertView.findViewById(R.id.storage_location_text);

        int totalScheduledQuantity = 0;
        for (String sched : scheduleUse) {
            totalScheduledQuantity+= ItemUseSchedule.getScheduleQuantityFromString(sched);
        }

        item.setText(foodName);
        expiryView.setText("Expiry: " + expiryDate);
        boughtView.setText("Bought: " + boughtDate);
        quantityView.setText("x " + quantity);
        scheduleQuantityView.setText("Scheduled: x " + totalScheduledQuantity);
        storageView.setText(storageLocation);

        if (totalScheduledQuantity > 0) {
            scheduleQuantityView.setVisibility(View.VISIBLE);
        } else {
            scheduleQuantityView.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

}
