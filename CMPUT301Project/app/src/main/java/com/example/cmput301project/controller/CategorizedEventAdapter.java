package com.example.cmput301project.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.cmput301project.R;
import com.example.cmput301project.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategorizedEventAdapter extends BaseAdapter {

    private Context context;
    private Map<String, ArrayList<Event>> categorizedEvents;
    private List<Object> displayItems; // Headers and events

    public CategorizedEventAdapter(Context context, Map<String, ArrayList<Event>> categorizedEvents) {
        this.context = context;
        this.categorizedEvents = categorizedEvents;
        this.displayItems = new ArrayList<>();

        for (Map.Entry<String, ArrayList<Event>> entry : categorizedEvents.entrySet()) {
            // Add headers first
            displayItems.add(getStatusHeader(entry.getKey()));

            // Add events for each category
            displayItems.addAll(entry.getValue());
        }
    }

    private String getStatusHeader(String status) {
        switch (status) {
            case "WAITING":
                return "Upcoming Lotteries";
            case "WAITING1":
                return "Waiting for Second Chance";
            case "SELECTED":
                return "Pending invitation";
            case "CANCELED":
                return "Canceled or Declined";
            case "FINAL":
                return "Upcoming Classes";
            default:
                return "Unknown Status";
        }
    }

    @Override
    public int getCount() {
        return displayItems.size();
    }

    @Override
    public Object getItem(int position) {
        return displayItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return (getItem(position) instanceof Event) ? 1 : 0; // 1 for event, 0 for header
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);

        if (convertView != null) {
            // Check if convertView matches the expected view type
            int existingViewType = (convertView.getTag() instanceof Integer) ? (int) convertView.getTag() : -1;
            if (existingViewType != viewType) {
                convertView = null; // Reset convertView if it doesn't match the current view type
            }
        }

        if (viewType == 0) { // Header
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_header, parent, false);
                convertView.setTag(0); // Tag this view as a header
            }
            TextView headerTextView = convertView.findViewById(R.id.header_text);
            headerTextView.setText((String) getItem(position));
        } else { // Event
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.event_list_item, parent, false);
                convertView.setTag(1);
            }

            Event event = (Event) getItem(position);

            // Populate event views
            TextView name = convertView.findViewById(R.id.event_name);
            TextView endDateTextView = convertView.findViewById(R.id.lottery_ends_date);
            ImageView poster = convertView.findViewById(R.id.event_poster);

            if (event != null) {
                name.setText(event.getName());
                endDateTextView.setText("Ends date: " + event.getEndDate());

                if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                    poster.setVisibility(View.VISIBLE);
                    Glide.with(context)
                            .load(event.getPosterUrl())
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .into(poster);
                } else {
                    poster.setVisibility(View.GONE);
                }
            }
        }

        return convertView;
    }


}

