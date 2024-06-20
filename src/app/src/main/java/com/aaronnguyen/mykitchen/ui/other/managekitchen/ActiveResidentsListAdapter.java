package com.aaronnguyen.mykitchen.ui.other.managekitchen;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.aaronnguyen.mykitchen.R;

import java.util.List;

public class ActiveResidentsListAdapter extends ArrayAdapter<String> {
    private Context context;

    private List<String> userName;
    private List<String> userIds;
    private String currentUserId;

    public ActiveResidentsListAdapter(@NonNull Context context, int resource, @NonNull List<String> objects, @NonNull List<String> userIds, String currentUserId) {
        super(context, resource, objects);
        this.context = context;
        this.userName = objects;
        this.userIds = userIds;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.active_resident_list_item, null);
        }

        TextView userName = convertView.findViewById(R.id.user_name);

        userName.setText(this.userName.get(position));

        TextView meTextView = convertView.findViewById(R.id.me_lbl);
        if (userIds.get(position).equals(currentUserId)) {
            meTextView.setVisibility(View.VISIBLE);
        } else {
            meTextView.setVisibility(View.GONE);
        }

        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        if (userIds.get(position).equals(currentUserId)) {
            return false;
        }

        return super.isEnabled(position);
    }
}
