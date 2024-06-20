package com.aaronnguyen.mykitchen.ui.main.History;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;


import com.aaronnguyen.mykitchen.model.Items.ItemUsage;
import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.model.kitchen.KitchenUserSubject;
import com.aaronnguyen.mykitchen.model.kitchen.KitchenUsersObserver;
import com.aaronnguyen.mykitchen.model.user.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Adapter for managing the history of item usage in a kitchen.
 * It also implements the KitchenUsersObserver interface to handle user information updates.
 *
 * @author u7515796 ChengboYan
 */
public class ItemUsageAdapter extends BaseExpandableListAdapter implements KitchenUsersObserver {
    private final KitchenUserSubject nameHost;
    private final Context context;
    private List<Date> dateGroups;
    private Map<Date, List<ItemUsage>> usageItemsWithDate;

    /**
     * Constructs an ItemUsageAdapter.
     * @param context The context in which the adapter is used.
     * @param itemUsageList The list of item usage records.
     * @param nameHost The subject responsible for fetching user information.
     */
    public ItemUsageAdapter(Context context, List<ItemUsage> itemUsageList, KitchenUserSubject nameHost) {
        this.nameHost = nameHost;

        List<Date> groupList = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Map<Date, List<ItemUsage>> childMapping = new HashMap<>();

        for (ItemUsage usage : itemUsageList) {
            Date dateStr = usage.getDateUsed();
            List<ItemUsage> childList = childMapping.getOrDefault(dateStr, new ArrayList<>());
            childList.add(usage);
            childMapping.put(dateStr, childList);

            if (!groupList.contains(dateStr)) {
                groupList.add(dateStr);
            }
        }

        Collections.sort(groupList, Collections.reverseOrder());
        this.context = context;
        this.dateGroups = groupList;
        this.usageItemsWithDate = childMapping;
    }

    public void update(List<ItemUsage> itemUsageList) {
        List<Date> groupList = new ArrayList<>();
        Map<Date, List<ItemUsage>> childMapping = new HashMap<>();
        Collections.reverse(itemUsageList);

        for (ItemUsage usage : itemUsageList) {
            Date dateStr = (usage.getDateUsed());
            List<ItemUsage> childList = childMapping.getOrDefault(dateStr, new ArrayList<>());
            childList.add(usage);
            childMapping.put(dateStr, childList);

            if (!groupList.contains(dateStr)) {
                groupList.add(dateStr);
            }
        }

        Collections.sort(groupList, Collections.reverseOrder());
        this.dateGroups = groupList;
        this.usageItemsWithDate = childMapping;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return dateGroups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        Date group = dateGroups.get(groupPosition);
        List<ItemUsage> children = usageItemsWithDate.get(group);
        return children != null ? children.size() : 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return dateGroups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        Date group = dateGroups.get(groupPosition);
        List<ItemUsage> children = usageItemsWithDate.get(group);
        return children != null ? children.get(childPosition) : null;
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
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.group_history, parent, false);
        }

        TextView titleDate = convertView.findViewById(R.id.date);
        SimpleDateFormat dateFormat = new SimpleDateFormat(" dd/MM/yyyy", Locale.getDefault());
        String dateStr = dateFormat.format(dateGroups.get(groupPosition));
        titleDate.setText(dateStr);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.child_history, parent, false);
        }

        TextView itemName = convertView.findViewById(R.id.item_name);
        TextView itemQuantity = convertView.findViewById(R.id.quantity_used);
        TextView userName = convertView.findViewById(R.id.user_text);

        // Get the ItemUsage corresponding to this child
        ItemUsage usage = (ItemUsage) getChild(groupPosition, childPosition);
        nameHost.fetchUserInfo(usersDictionary -> {
            if(usage.getUser().getUid() != null && usersDictionary.get(usage.getUser().getUid()) != null) {
                usage.getUser().setUserName(usersDictionary.get(usage.getUser().getUid()).getUserName());
            }
            return null;
        });

        // Set the data from the ItemUsage to the views
        itemName.setText(usage.getName());
        Log.i("true",String.valueOf(usage.getQuantityUsed()));
        itemQuantity.setText("quantity used: "+ usage.getQuantityUsed());
        userName.setText(String.format("User: %s", usage.getUser().getUserName()));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public void notify(HashMap<String, User> usersDictionary) {
        for(int groupIndex = 0; groupIndex < getGroupCount(); groupIndex++) {
            for (int childIndex = 0;  childIndex < getChildrenCount(groupIndex); childIndex++) {
                String currentID = Objects.requireNonNull(usageItemsWithDate.get(dateGroups.get(groupIndex))).get(childIndex).getUser().getUid();
                if(currentID != null && usersDictionary.get(currentID) != null) {
                    Objects.requireNonNull(usageItemsWithDate.get(dateGroups.get(groupIndex))).get(childIndex).getUser().setUserName(usersDictionary.get(currentID).getUserName());
                }
            }
        }
        notifyDataSetChanged();
    }
}
