package com.example.cmput301project.view;
import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.cmput301project.FirebaseServer;
import com.example.cmput301project.R;
import com.example.cmput301project.controller.OrganizerEventController;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Organizer;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventDetailsActivity extends AppCompatActivity {
    private Event event;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_single_event);
        db = FirebaseFirestore.getInstance();

        setSupportActionBar(findViewById(R.id.toolbar_single_event));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // Retrieve the Event object passed from Admin Events (list of events)
        event = (Event) getIntent().getSerializableExtra("event");

        if (event == null) {
            Toast.makeText(this, "Event data is missing.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        if (event.getId() == null || event.getOrganizerId() == null) {
            Toast.makeText(this, "Incomplete event data. Cannot proceed.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        // If event exists
        if (event != null) {
            // Initialize views
            TextView eventNameTextView = findViewById(R.id.eventNameTextView);
            TextView eventStartDateTextView = findViewById(R.id.eventStartDateTextView);
            TextView eventEndDateTextView = findViewById(R.id.eventEndDateTextView);
            TextView eventDescriptionTextView = findViewById(R.id.eventDescriptionTextView);
            ImageView eventImageView = findViewById(R.id.eventImageView);
            Button deleteButton = findViewById(R.id.deleteEventButton);

            // Set views
            eventNameTextView.setText(event.getName());
            eventStartDateTextView.setText("Start date: " + event.getStartDate());
            eventEndDateTextView.setText("End date: " + event.getEndDate());
            eventDescriptionTextView.setText("Description: "+ event.getDescription());

            // Load the image, if the URL exists
            if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
                Glide.with(this)
                        .load(event.getPosterUrl())
                        .placeholder(R.drawable.placeholder_image)
//                        .error(R.drawable.error_image)
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                // If the image fails to load, set the ImageView's visibility to GONE
                                eventImageView.setVisibility(View.GONE);
                                Log.e("Glide", "Image load failed", e); // Log the error
                                return true; // Return true to prevent Glide from applying the error placeholder
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                // If the image loads successfully, ensure the ImageView is visible
                                eventImageView.setVisibility(View.VISIBLE);
                                return false; // Return false to let Glide handle displaying the image
                            }
                        })
                        .into(eventImageView);
            }
            // delete button click listener
            deleteButton.setOnClickListener(v -> confirmDeleteEvent());

        } else {
            // No event
            TextView eventNameTextView = findViewById(R.id.eventNameTextView);
            eventNameTextView.setText("Event not found");
        }
    }

    private void confirmDeleteEvent() {
        // Ask for confirmation before deleting the event
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Yes", (dialog, which) -> deleteEvent())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteEvent() {
        FirebaseServer firebaseServer = new FirebaseServer();
        firebaseServer.deleteEvent(
                event.getOrganizerId(),
                event.getId(),
                aVoid -> {
                    Toast.makeText(this, "Event deleted successfully :)", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.putExtra("deletedEventId", event.getId());
                    setResult(RESULT_OK, intent);
                    finish();
                },
                e -> {
                    Toast.makeText(this, "Failed to delete event.", Toast.LENGTH_SHORT).show();
                    Log.e("EventDetailsActivity", "Error deleting event", e);
                }
        );
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
