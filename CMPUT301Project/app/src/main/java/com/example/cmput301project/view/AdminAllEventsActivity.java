package com.example.cmput301project.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView; // Ensure this import is present
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cmput301project.R;
import com.example.cmput301project.controller.EventRecyclerViewAdapter;
import com.example.cmput301project.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminAllEventsActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private EventRecyclerViewAdapter eventAdapter;
    private List<Event> eventList;
    private SearchView searchView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_events);

        db = FirebaseFirestore.getInstance();

        setSupportActionBar(findViewById(R.id.toolbar_events));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Events");

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchView);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventList = new ArrayList<>();
        // Initialize adapter with the delete event callback
        eventAdapter = new EventRecyclerViewAdapter(this, eventList, event -> {
            Intent intent = new Intent(AdminAllEventsActivity.this, EventDetailsActivity.class);
            intent.putExtra("event", event);
            startActivity(intent);
        }, event -> {
            // Remove event from list
            deleteEvent(event);
        });
        recyclerView.setAdapter(eventAdapter);

        // Load all events from Firestore
        loadAllEventsFromFirebase();
        // Search functionality
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterEvents(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEvents(newText);
                return true;
            }
        });
    }

    private void loadAllEventsFromFirebase() {
        db.collectionGroup("events") // Retrieve all 'events'
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            eventList.add(event);
                        }
                        eventAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load events.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteEvent(Event event) {
        db.collection("events")
                .document(event.getId()) // Assuming getId() returns the event's document ID
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Remove from local list after successful Firebase delete
                    eventList.remove(event);
                    eventAdapter.updateList(eventList);
                    Toast.makeText(AdminAllEventsActivity.this, "Event removed successfully.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminAllEventsActivity.this, "Error deleting event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void filterEvents(String query) {
        List<Event> filteredList = new ArrayList<>();
        for (Event event : eventList) {
            if (event.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(event);
            }
        }
        eventAdapter.updateList(filteredList);
    }

    // navigate back to previous activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
