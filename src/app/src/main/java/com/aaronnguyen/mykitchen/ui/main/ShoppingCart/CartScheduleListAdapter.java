package com.aaronnguyen.mykitchen.ui.main.ShoppingCart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.aaronnguyen.mykitchen.R;
import com.aaronnguyen.mykitchen.model.Items.CartItems.CartSchedule;

import java.util.List;

/**
 * ListAdapter to let us show the list of schedules held by each cart item
 * @author u7648367 Ruixian Wu
 */
public class CartScheduleListAdapter extends BaseAdapter {
    private final Context context;
    private final List<String> itemSchedule;


    /**
     *
     * @param context
     * @param viewModel
     * @param groupPosition within the ShoppingCartFragment expandable list adapter of the item whose schedule is being viewed
     * @param childPosition within the ShoppingCartFragment expandable list adapter of the item whose schedule is being viewed (within the given group position)
     */
    public CartScheduleListAdapter(Context context, CartViewModel viewModel, int groupPosition, int childPosition) {
        this.context = context;
        this.itemSchedule = (viewModel.itemSchedule).get((viewModel.getGroupList()).get(groupPosition)).get(childPosition);
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
            convertView = inflater.inflate(R.layout.child_cart_schedule, null);
        }

        TextView scheduleDate = convertView.findViewById(R.id.schedule_date);
        TextView scheduleRepeat = convertView.findViewById(R.id.repeat_after);
        TextView scheduleQuantity = convertView.findViewById(R.id.schedule_quantity);

        scheduleDate.setText(CartSchedule.getFieldStringFromScheduleString(itemSchedule.get(position), CartSchedule.SCHEDULE_DATE_FIELD_NAME));
        scheduleRepeat.setText("Repeat after: "
                + CartSchedule.getFieldStringFromScheduleString(itemSchedule.get(position)
                                                                , CartSchedule.SCHEDULE_DAYS_REOCCURRING_FIELD_NAME) + " days");
        scheduleQuantity.setText("x " + CartSchedule.getFieldStringFromScheduleString(itemSchedule.get(position), CartSchedule.SCHEDULE_QUANTITY_FIELD_NAME));

        return convertView;
    }
}
