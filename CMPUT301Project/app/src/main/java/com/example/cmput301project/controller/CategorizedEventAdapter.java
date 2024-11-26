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

    public Map<String, ArrayList<Event>> getCategorizedEvents() {
        return categorizedEvents;
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
    public int getViewTypeCount() {
        return 2; // Two types: Header (0) and Event (1)
    }

    public void updateData(Map<String, ArrayList<Event>> newCategorizedEvents) {
        this.categorizedEvents = newCategorizedEvents;

        // Clear and rebuild displayItems
        displayItems.clear();
        for (Map.Entry<String, ArrayList<Event>> entry : categorizedEvents.entrySet()) {
            // Add the header for each category
            displayItems.add(getStatusHeader(entry.getKey()));

            // Add all events in the category
            displayItems.addAll(entry.getValue());
        }

        // Notify the adapter that the data has changed
        notifyDataSetChanged();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);

        // Ensure the convertView matches the expected view type
        if (convertView != null) {
            int existingViewType = (convertView.getTag() instanceof Integer) ? (int) convertView.getTag() : -1;
            if (existingViewType != viewType) {
                convertView = null; // Reset convertView if it doesn't match the current view type
            }
        }

        // Inflate the correct layout for the view type
        if (convertView == null) {
            if (viewType == 0) { // Header
                convertView = LayoutInflater.from(context).inflate(R.layout.item_header, parent, false);
                convertView.setTag(0); // Tag this view as a header
            } else { // Event
                convertView = LayoutInflater.from(context).inflate(R.layout.event_list_item, parent, false);
                convertView.setTag(1); // Tag this view as an event
            }
        }

        // Populate the view
        if (viewType == 0) { // Header
            TextView headerTextView = convertView.findViewById(R.id.header_text);
            if (headerTextView == null) {
                throw new IllegalStateException("Header TextView is null. Check your item_header.xml layout.");
            }

            String header = (String) getItem(position);
            headerTextView.setText(header != null ? header : "Unknown Header");
        } else { // Event
            Event event = (Event) getItem(position);

            TextView name = convertView.findViewById(R.id.event_name);
            TextView endDateTextView = convertView.findViewById(R.id.lottery_ends_date);
            ImageView poster = convertView.findViewById(R.id.event_poster);

            if (name == null || endDateTextView == null || poster == null) {
                throw new IllegalStateException("One or more views are null. Check event_list_item.xml layout.");
            }

            name.setText(event != null ? event.getName() : "");
            endDateTextView.setText(event != null ? "Ends date: " + event.getEndDate() : "");
            if (event != null && event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
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

        return convertView;
    }


}

