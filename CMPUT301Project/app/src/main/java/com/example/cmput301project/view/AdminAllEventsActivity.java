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

import com.example.cmput301project.FirebaseServer;
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
        eventList = new ArrayList<>();  // event object list
        eventAdapter = new EventRecyclerViewAdapter(this, eventList, event -> {
            if (event.getId() == null || event.getOrganizerId() == null) {
                Toast.makeText(this, "Event data is incomplete. Cannot open details.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Open EventDetailsActivity with the selected event
            Intent intent = new Intent(AdminAllEventsActivity.this, EventDetailsActivity.class);
            intent.putExtra("event", event);
            startActivityForResult(intent, 100);

        });
        recyclerView.setAdapter(eventAdapter);

        // Load all events
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
        FirebaseServer server = new FirebaseServer();
        server.fetchAllEvents(
                events -> {
                    eventList.clear();
                    eventList.addAll(events);
                    eventAdapter.notifyDataSetChanged();
                },
                e -> Toast.makeText(this, "Failed to load events.", Toast.LENGTH_SHORT).show()
        );
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String deletedEventId = data.getStringExtra("deletedEventId");
            if (deletedEventId != null) {
                for (int i = 0; i < eventList.size(); i++) {
                    if (eventList.get(i).getId().equals(deletedEventId)) {
                        eventList.remove(i);
                        eventAdapter.notifyItemRemoved(i);
                        break;
                    }
                }
            }
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        loadAllEventsFromFirebase(); // Refresh the list when returning to this activity
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

