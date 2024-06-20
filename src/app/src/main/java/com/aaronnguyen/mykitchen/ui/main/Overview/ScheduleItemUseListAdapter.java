package com.aaronnguyen.mykitchen.ui.main.Overview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.model.Items.KitchenItems.ItemUseSchedule;

import java.util.List;

/**
 * List adapter for the schedule list contained by each item in the kitchen
 */
public class ScheduleItemUseListAdapter extends BaseAdapter {
    private final Context context;
    private final List<String> itemSchedule;
    //groupPosition of the item in the expandable itemList
    private final int groupPosition;
    //child Position of the item in the expandable item list
    private final int childPosition;


    public ScheduleItemUseListAdapter(Context context, OverviewViewModel viewModel, int groupPosition, int childPosition) {
        this.context = context;
        this.itemSchedule = viewModel.getUiState().getValue().getItemSchedule().get(
                (viewModel.getUiState().getValue().getGroupList()).get(groupPosition)
        ).get(childPosition);
        this.groupPosition = groupPosition;
        this.childPosition = childPosition;
    }

    @Override
    public int getCount() {
        return itemSchedule.size();
    }

    @Override
    public Object getItem(int position) {
        return itemSchedule.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.child_schedule_use, null);
        }

        TextView scheduleUseDate = convertView.findViewById(R.id.schedule_use_date);
        TextView scheduleQuantity = convertView.findViewById(R.id.schedule_use_quantity);

        scheduleUseDate.setText(ItemUseSchedule.dateBtnStringFromScheduleString(itemSchedule.get(position)));
        scheduleQuantity.setText("x " + ItemUseSchedule.getScheduleQuantityFromString(itemSchedule.get(position)));

        return convertView;
    }
}
