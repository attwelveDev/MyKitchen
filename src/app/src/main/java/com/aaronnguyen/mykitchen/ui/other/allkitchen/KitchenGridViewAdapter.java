package com.aaronnguyen.mykitchen.ui.other.allkitchen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aaronnguyen.mykitchen.DAO.KitchenData;
import com.aaronnguyen.mykitchen.R;

import java.util.List;
import java.util.Objects;

public class KitchenGridViewAdapter extends ArrayAdapter<KitchenData> {
    public KitchenGridViewAdapter(@NonNull Context context, @NonNull List<KitchenData> kitchens) {
        super(context, 0, kitchens);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View gridItemView = convertView;
        if (gridItemView == null) {
            gridItemView = LayoutInflater.from(getContext()).inflate(R.layout.kitchen_grid_item, parent, false);
        }

        KitchenData kitchen = Objects.requireNonNull(getItem(position));

        String kitchenName = kitchen.getKitchenName();
        if (kitchenName != null) {
            TextView kitchenNameTextView = gridItemView.findViewById(R.id.kitchen_name_grid_lbl);
            kitchenNameTextView.setText(kitchenName);
        }

        if (kitchen.getMembers() != null) {
            int numMembers = kitchen.getMembers().size();
            String memberCount = numMembers + " " + getContext().getResources().getQuantityString(R.plurals.residents, numMembers);
            TextView membersCountTextView = gridItemView.findViewById(R.id.members_count_grid_lbl);
            membersCountTextView.setText(memberCount);
        }

        if (kitchen.getItemList() != null) {
            int numItems = kitchen.getItemList().size();
            String itemCount = numItems + " " + getContext().getResources().getQuantityString(R.plurals.food_items, numItems);
            TextView itemsCountTextView = gridItemView.findViewById(R.id.items_count_grid_lbl);
            itemsCountTextView.setText(itemCount);
        }

        if (kitchen.getNotifications() != null) {
            int numNotifications = kitchen.getNotifications().size();
            TextView notifsBadgeTextView = gridItemView.findViewById(R.id.notif_badge_text_view);
            if (numNotifications > 0 && numNotifications <= 99) {
                notifsBadgeTextView.setVisibility(View.VISIBLE);
                notifsBadgeTextView.setText(String.valueOf(numNotifications));
            } else if (numNotifications > 99) {
                notifsBadgeTextView.setVisibility(View.VISIBLE);
                notifsBadgeTextView.setText(R.string.greater_than_two_digits_notifs);
            } else {
                notifsBadgeTextView.setVisibility(View.GONE);
            }
        }

        return gridItemView;
    }
}
