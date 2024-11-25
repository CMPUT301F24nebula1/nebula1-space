package com.example.cmput301project.view;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.cmput301project.R;
import com.example.cmput301project.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventDetailsActivity extends AppCompatActivity {
    private Event event;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_single_event);
        db = FirebaseFirestore.getInstance();

        // Retrieve the Event object passed from Admin Events (list of events)
        Event event = (Event) getIntent().getSerializableExtra("event");

        // If event exists
        if (event != null) {
            // Initialize views
            TextView eventNameTextView = findViewById(R.id.eventNameTextView);
            TextView eventStartDateTextView = findViewById(R.id.eventStartDateTextView);
            TextView eventEndDateTextView = findViewById(R.id.eventEndDateTextView);
            TextView eventDescriptionTextView = findViewById(R.id.eventDescriptionTextView);
            ImageView eventImageView = findViewById(R.id.eventImageView);
            Button deleteEventButton = findViewById(R.id.deleteEventButton);

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
                        .error(R.drawable.error_image)
                        .into(eventImageView);
            }
            // Set up delete button
            deleteEventButton.setOnClickListener(v -> {
                if (event != null) {
                    deleteEventFromFirebase(event);
                }
            });
        } else {
            // No event
            TextView eventNameTextView = findViewById(R.id.eventNameTextView);
            eventNameTextView.setText("Event not found");
        }
    }
    // Method to delete the event from Firestore
    private void deleteEventFromFirebase(Event event) {
        db.collection("organizers")
                .document(event.getOrganizerId()) // Organizer ID
                .collection("events")
                .document(event.getId()) // Event ID
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EventDetailsActivity.this, "Event deleted successfully.", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity after deleting the event
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(EventDetailsActivity.this, "Error deleting event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
