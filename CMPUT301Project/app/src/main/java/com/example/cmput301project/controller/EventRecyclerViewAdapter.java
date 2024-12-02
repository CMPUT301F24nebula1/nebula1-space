package com.example.cmput301project.controller;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.cmput301project.R;
import com.example.cmput301project.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRecyclerViewAdapter.EventViewHolder> {
    private Context context;
    private List<Event> eventList;
    private OnEventClickListener onEventClickListener;


    // Interface for click listener
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }


    public EventRecyclerViewAdapter(Context context, List<Event> eventList, OnEventClickListener listener) {
        this.context = context;
        this.eventList = eventList;
        this.onEventClickListener = listener;

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

        // Clear the ImageView before loading a new image
        Glide.with(context).clear(holder.eventPoster);

        // Load image
        if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
            Glide.with(context)
                    .load(event.getPosterUrl())
                    .placeholder(R.drawable.placeholder_image)
//                    .error(R.drawable.error_image) // Replace with your error image
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            // If the image fails to load, set the ImageView's visibility to GONE
                            holder.eventPoster.setVisibility(View.GONE);
                            Log.e("Glide", "Image load failed", e); // Log the error
                            return true; // Return true to prevent Glide from applying the error placeholder
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            // If the image loads successfully, ensure the ImageView is visible
                            holder.eventPoster.setVisibility(View.VISIBLE);
                            return false; // Return false to let Glide handle displaying the image
                        }
                    })
                    .into(holder.eventPoster);
//            holder.eventPoster.setVisibility(View.VISIBLE);
            holder.container.setVisibility(View.VISIBLE);
        }else{
//            holder.eventPoster.setVisibility(View.GONE);
            holder.container.setVisibility(View.INVISIBLE);
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
        CardView container;


        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.event_name);
            eventStartDate = itemView.findViewById(R.id.lottery_starts_date);
            eventEndDate = itemView.findViewById(R.id.lottery_ends_date);
            eventPoster = itemView.findViewById(R.id.event_poster);
            container = itemView.findViewById(R.id.event_poster_container);

        }
    }
}
