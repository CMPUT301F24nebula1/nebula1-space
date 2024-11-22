package com.example.cmput301project.controller;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cmput301project.FirebaseServer;
import com.example.cmput301project.R;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.view.QRCodeAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminQRController extends AppCompatActivity {

    private RecyclerView recyclerView;
    private QRCodeAdapter adapter;
    private List<Event> events;
    private Event selectedEvent; // To track the selected event
    private Button deleteButton;
    private FirebaseServer firebaseServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_qr_codes);

        firebaseServer = new FirebaseServer(getApplication());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        deleteButton = findViewById(R.id.deleteQRButton); // Single delete button
        deleteButton.setEnabled(false); // Disable until an item is selected

        events = new ArrayList<>();
        loadEvents();

        adapter = new QRCodeAdapter(events, this::onQRCodeSelected);
        recyclerView.setAdapter(adapter);

        deleteButton.setOnClickListener(v -> {
            if (selectedEvent != null) {
                deleteHashedQRCode(selectedEvent);
            }
        });
    }

    private void loadEvents() {
        firebaseServer.fetchAllEvents(
                fetchedEvents -> {
                    events.clear();
                    events.addAll(fetchedEvents);
                    adapter.notifyDataSetChanged();
                },
                e -> {
                    Toast.makeText(this, "Failed to load events.", Toast.LENGTH_SHORT).show();
                    Log.e("AdminQRController", "Error loading events", e);
                }
        );
    }


    private void deleteHashedQRCode(Event event) {
        if (event.getId() == null || event.getOrganizerId() == null || event.getId().isEmpty()) {
            Toast.makeText(this, "Invalid event or organizer ID.", Toast.LENGTH_SHORT).show();
            Log.e("AdminQRController", "Invalid eventId or organizerId: " + event.getId() + ", " + event.getOrganizerId());
            return;
        }

        Log.d("AdminQRController", "Deleting hashed QR code for eventId: " + event.getId() + ", organizerId: " + event.getOrganizerId());

        firebaseServer.deleteQRCode(event.getOrganizerId(), event.getId(),
                aVoid -> {
                    for (Event e : events) {
                        if (e.getId().equals(event.getId())) {
                            e.setHashedQRCode(null); // Update the list
                            break;
                        }
                    }
                    adapter.notifyDataSetChanged(); // Notify adapter of changes
                    selectedEvent = null;
                    deleteButton.setEnabled(false);
                    Toast.makeText(this, "Hashed QR Code deleted successfully.", Toast.LENGTH_SHORT).show();
                },
                e -> {
                    Toast.makeText(this, "Failed to delete hashed QR Code.", Toast.LENGTH_SHORT).show();
                    Log.e("AdminQRController", "Error deleting hashed QR Code", e);
                }
        );
    }


    private void onQRCodeSelected(Event event) {
        selectedEvent = event;
        deleteButton.setEnabled(true);
    }
}



