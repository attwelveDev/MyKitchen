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

public class BannedResidentsListAdapter extends ArrayAdapter<String> {
    private Context context;

    private List<String> users;

    public BannedResidentsListAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        this.context = context;
        this.users = objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.banned_resident_list_item, null);
        }

        TextView userName = convertView.findViewById(R.id.user_name);

        userName.setText(users.get(position));

        return convertView;
    }
}
