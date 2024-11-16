package com.example.cmput301project.controller;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.cmput301project.R;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationArrayAdapter extends ArrayAdapter<Notification> {
    public NotificationArrayAdapter(@NonNull Context context, @NonNull ArrayList<Notification> notifications) {
        super(context, 0, notifications);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view;
        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.notification_list_item,
                    parent, false);
        } else {
            view = convertView;
        }
        Notification n = getItem(position);
        TextView title = view.findViewById(R.id.notification_title);
        TextView message = view.findViewById(R.id.notification_message);
        View badge = view.findViewById(R.id.notification_badge);

        if (n != null) {
            message.setText(n.getMessage());
            title.setText(n.getTitle());
        }

        // Show or hide the badge based on isRead value

        if (!n.isRead()) {
//            Log.d("notification", "isRead = false");
            badge.setVisibility(View.VISIBLE);
            notifyDataSetChanged();
        } else {
//            Log.d("notification", "isRead = true");
            badge.setVisibility(View.GONE);
            notifyDataSetChanged();
        }

        return view;
    }
}
