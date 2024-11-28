package com.example.cmput301project.controller;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
    private Event selectedEvent;
    private Button deleteButton;
    private FirebaseServer firebaseServer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_qr_codes);

        setSupportActionBar(findViewById(R.id.toolbar_qr));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        firebaseServer = new FirebaseServer(getApplication());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        deleteButton = findViewById(R.id.deleteQRButton); // Single delete button
        deleteButton.setEnabled(false); // Disable until an item is selected

        events = new ArrayList<>();
        loadEvents();

        adapter = new QRCodeAdapter(events, this::onQRCodeSelected, this::showEnlargedQRCode);
        recyclerView.setAdapter(adapter);

        deleteButton.setOnClickListener(v -> {
            if (selectedEvent != null) {
                deleteQRCode(selectedEvent);
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


    private void deleteQRCode(Event event) {
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
                            e.setQrCode(null); // Update the list
                            break;
                        }
                    }
                    adapter.notifyDataSetChanged(); // Notify adapter of changes
                    selectedEvent = null;
                    deleteButton.setEnabled(false);
                    Toast.makeText(this, "QR Code deleted successfully.", Toast.LENGTH_SHORT).show();
                },
                e -> {
                    Toast.makeText(this, "Failed to delete QR Code.", Toast.LENGTH_SHORT).show();
                    Log.e("AdminQRController", "Error deleting QR Code", e);
                }
        );
    }


    private void onQRCodeSelected(Event event) {
        selectedEvent = event;
        deleteButton.setEnabled(true);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Navigate back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showEnlargedQRCode(Bitmap qrCodeBitmap) {
        // displays a dialog with the enlarged QR code
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.enlarged_qr);
        ImageView qrImageView = dialog.findViewById(R.id.enlargedQRCodeImageView);
        qrImageView.setImageBitmap(qrCodeBitmap);
        dialog.show();
    }

}