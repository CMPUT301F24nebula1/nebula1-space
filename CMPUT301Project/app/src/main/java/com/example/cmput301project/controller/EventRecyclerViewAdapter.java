package com.example.cmput301project.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.cmput301project.R;
import com.example.cmput301project.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRecyclerViewAdapter.EventViewHolder> {
    private Context context;
    private List<Event> eventList;
    private OnEventClickListener onEventClickListener;
    private OnEventDeleteListener onEventDeleteListener;


    // Interface for click listener
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }

    public interface OnEventDeleteListener {
        void onEventDelete(Event event);
    }

    public EventRecyclerViewAdapter(Context context, List<Event> eventList, OnEventClickListener listener, OnEventDeleteListener deleteListener) {
        this.context = context;
        this.eventList = eventList;
        this.onEventClickListener = listener;
        this.onEventDeleteListener = deleteListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_list_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventList.get(position);
        holder.eventName.setText(event.getName());
        if(event.getStartDate() != null) { holder.eventStartDate.setText("Start date: " + event.getStartDate());}
        if(event.getEndDate() != null){holder.eventEndDate.setText("End date: " + event.getEndDate());}


        // Load image
        if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
            Glide.with(context)
                    .load(event.getPosterUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image) // Replace with your error image
                    .into(holder.eventPoster);
        }else{
            holder.eventPoster.setVisibility(View.GONE);
        }
        // click listener
        holder.itemView.setOnClickListener(v -> {
            if (onEventClickListener != null) {
                onEventClickListener.onEventClick(event);
            }
        });


    }

    public void updateList(List<Event> newList) {
        eventList = newList;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventName;
        TextView eventEndDate;
        TextView eventStartDate;
        ImageView eventPoster;


        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.event_name);
            eventStartDate = itemView.findViewById(R.id.lottery_starts_date);
            eventEndDate = itemView.findViewById(R.id.lottery_ends_date);
            eventPoster = itemView.findViewById(R.id.event_poster);

        }
    }
}
