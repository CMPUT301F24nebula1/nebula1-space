package com.example.cmput301project.view;

import static androidx.test.InstrumentationRegistry.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cmput301project.R;
import com.example.cmput301project.controller.EntrantArrayAdapter;
import com.example.cmput301project.controller.UserController;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.service.PoolingService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.slider.Slider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Source;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ParticipantListActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private PoolingService poolingService;

    private MaterialButtonToggleGroup toggleGroup;

    private ArrayList<Entrant> entrants_store;
    private ArrayList<Entrant> entrants_waitlist; // entrants with status "WAITING"
    private ArrayList<Entrant> entrants_selected; // entrants with status "SELECTED"
    private ArrayList<Entrant> entrants_canceled; // entrants with status "CANCELED"
    private ArrayList<Entrant> entrants_final; // entrants with status "FINAL"

    private Map<String, ArrayList<Entrant>> entrantCache = new HashMap<>();

    private ListView participantList;
    private EntrantArrayAdapter entrantAdapter;

    private Slider slider;
    private MaterialButton selectButton;
    private MaterialButton geoButton;

    private boolean isDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.participant_list);

        poolingService = new PoolingService();

        participantList = findViewById(R.id.participant_list);

        Intent intent = getIntent();

        Event event = (Event) intent.getSerializableExtra("event");

        entrants_store = new ArrayList<Entrant>();
        entrants_waitlist = new ArrayList<Entrant>();
        entrants_selected = new ArrayList<Entrant>();
        entrants_canceled = new ArrayList<Entrant>();
        entrants_final = new ArrayList<Entrant>();

        Log.d("event wishlist participantActivity", event.getWaitlistEntrantIds().toString());

        toggleGroup = findViewById(R.id.listToggleGroup);

        slider = findViewById(R.id.slider);
        selectButton = findViewById(R.id.select_button);
        geoButton = findViewById(R.id.select_button3);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Entrant Lists");

        retrieveEntrantsWithRealtimeUpdates(event, "WAITING", new RetrieveEntrantsCallback() {
            @Override
            public void onRetrieveEntrantsCompleted(List<Entrant> entrants) {
                isDataLoaded = true;

                if (entrants.isEmpty()) {
//                    setButtonState(false);
                } else if (entrants.size() == 1) {
//                    setButtonState(true);
//                    slider.setVisibility(View.GONE);
                    selectButton.setText("Select 1 Participant");
                } else {
//                    setButtonState(true);
                    slider.setValueTo(entrants.size());
                    slider.setVisibility(View.VISIBLE);
                }
            }
        });

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isDataLoaded) {
                    Toast.makeText(ParticipantListActivity.this, "Loading, please try later", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ParticipantListActivity.this, "Pooling!", Toast.LENGTH_SHORT).show();
                    poolingService.performPooling(event.getId(), entrants_waitlist, (int) slider.getValue(), new PoolingService.PoolingCallback() {

                        @Override
                        public void onSuccess() {
                            Toast.makeText(ParticipantListActivity.this, "Pooling succeed.", Toast.LENGTH_SHORT).show();
                            entrantAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(ParticipantListActivity.this, "Pooling failed.", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });

        // Set up the listener to update the button text based on the slider value
        slider.addOnChangeListener((slider1, value, fromUser) -> {
            // Update the button text with the current slider value, casting it to an integer
            selectButton.setText("Select " + (int) value + " Participants");
        });

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                String status;
                if (checkedId == R.id.btn_waitlist) {
                    status = "WAITING";
                } else if (checkedId == R.id.btn_selected) {
                    status = "SELECTED";
                } else if (checkedId == R.id.btn_canceled) {
                    status = "CANCELED";
                } else if (checkedId == R.id.btn_final) {
                    status = "FINAL";
                } else {
                    status = "WAITING";
                }
                if (entrantCache.containsKey(status)) {
                    Log.d("waitlist debug cache", status + entrantCache.get(status).toString());
                    updateEntrantsList(entrantCache.get(status));
                } else {
                    retrieveEntrantsWithRealtimeUpdates(event, status, new RetrieveEntrantsCallback() {
                        @Override
                        public void onRetrieveEntrantsCompleted(List<Entrant> entrants) {
                            Log.d("waitlist", "data retrieved and listened.");
                            Log.d("waitlist", entrants_waitlist.toString());

                        }
                    });
                }
            }
        });

        retrieveEntrantsWithRealtimeUpdates(event, "WAITING", new RetrieveEntrantsCallback() {
            @Override
            public void onRetrieveEntrantsCompleted(List<Entrant> entrants) {
                Log.d("waitlist", "data retrieved and listened.");
            }
        });

    }

    public interface RetrieveEntrantsCallback {
        void onRetrieveEntrantsCompleted(List<Entrant> entrants);
    }

    public void retrieveEntrantsWithRealtimeUpdates(Event event, String status, RetrieveEntrantsCallback callback) {
        Log.d("no wishlist", event.getWaitlistEntrantIds().toString());

        if (event.getWaitlistEntrantIds() != null && !event.getWaitlistEntrantIds().isEmpty()) {
            db.collection("entrants")
                    .whereIn(FieldPath.documentId(), event.getWaitlistEntrantIds())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            entrants_waitlist.clear(); // Clear the list to avoid duplicates


                            entrants_selected.clear();
                            entrants_canceled.clear();
                            entrants_final.clear();


                            int totalDocuments = task.getResult().size();
                            int[] completedCount = {0};

                            for (DocumentSnapshot document : task.getResult()) {
                                Entrant entrant = document.toObject(Entrant.class);
                                String entrantId = document.getId();

                                db.collection("entrants")
                                        .document(entrantId)
                                        .collection("entrantWaitList")
                                        .document(event.getId())
                                        .addSnapshotListener((subDocument, error) -> {
                                            if (error != null) {
                                                Log.e("FirebaseError", "Error listening for status changes", error);
                                                return;
                                            }

                                            if (subDocument != null && subDocument.exists()) {
                                                String statusFirebase = subDocument.getString("status");

                                                switch (statusFirebase) {
                                                    case "WAITING":
                                                        if (!entrants_waitlist.contains(entrant)) {
                                                            entrants_waitlist.add(entrant);
                                                        }
                                                        entrants_selected.remove(entrant);
                                                        entrants_canceled.remove(entrant);
                                                        entrants_final.remove(entrant);
                                                        break;

                                                    case "SELECTED":
                                                        if (!entrants_selected.contains(entrant)) {
                                                            entrants_selected.add(entrant);
                                                        }
                                                        entrants_waitlist.remove(entrant);
                                                        entrants_canceled.remove(entrant);
                                                        entrants_final.remove(entrant);
                                                        break;

                                                    case "CANCELED":
                                                        if (!entrants_canceled.contains(entrant)) {
                                                            entrants_canceled.add(entrant);
                                                        }
                                                        entrants_waitlist.remove(entrant);
                                                        entrants_selected.remove(entrant);
                                                        entrants_final.remove(entrant);
                                                        break;

                                                    case "FINAL":
                                                        if (!entrants_final.contains(entrant)) {
                                                            entrants_final.add(entrant);
                                                        }
                                                        entrants_waitlist.remove(entrant);
                                                        entrants_selected.remove(entrant);
                                                        entrants_canceled.remove(entrant);
                                                        break;

                                                    default:
                                                        // Optionally, handle unexpected status values here.
                                                        Log.e("StatusError", "Unknown status: " + statusFirebase);
                                                        break;
                                                }


                                                entrantCache.put("WAITING", new ArrayList<>(entrants_waitlist));
                                                entrantCache.put("SELECTED", new ArrayList<>(entrants_selected));
                                                entrantCache.put("CANCELED", new ArrayList<>(entrants_canceled));
                                                entrantCache.put("FINAL", new ArrayList<>(entrants_final));

                                                if (toggleGroup.getCheckedButtonId() == R.id.btn_waitlist) {
                                                    updateEntrantsList(new ArrayList<>(entrants_waitlist));
                                                } else if (toggleGroup.getCheckedButtonId() == R.id.btn_selected) {
                                                    updateEntrantsList(new ArrayList<>(entrants_selected));
                                                } else if (toggleGroup.getCheckedButtonId() == R.id.btn_canceled) {
                                                    updateEntrantsList(new ArrayList<>(entrants_canceled));
                                                } else if (toggleGroup.getCheckedButtonId() == R.id.btn_final) {
                                                    updateEntrantsList(new ArrayList<>(entrants_final));
                                                }
                                                Log.d("wishlist debug0", status + entrants_store.toString());

                                                if (entrantAdapter == null) {
                                                    entrantAdapter = new EntrantArrayAdapter(this, entrants_store);
                                                    participantList.setAdapter(entrantAdapter);
                                                } else {
                                                    entrantAdapter.notifyDataSetChanged();
                                                }

                                                completedCount[0]++;


                                                if (completedCount[0] == totalDocuments) {
                                                    if (entrantAdapter == null) {
                                                        entrantAdapter = new EntrantArrayAdapter(this, entrants_store);
                                                        participantList.setAdapter(entrantAdapter);
                                                    } else {
                                                        entrantAdapter.notifyDataSetChanged();
                                                    }
                                                    callback.onRetrieveEntrantsCompleted(entrants_waitlist);
                                                }

                                            }
                                        });
                            }
                        } else {
                            Log.e("FirebaseError", "Error fetching entrants: ", task.getException());
                        }
                    });
        } else {
            Log.d("FirebaseQuery", "No wishlist IDs to query.");
        }
    }

    // update UI
    private void updateEntrantsList(ArrayList<Entrant> entrants) {
        entrants_store.clear();
        entrants_store.addAll(entrants);
        Log.d("waitlist debug entrants_store", entrants_store.toString());
        if (entrantAdapter == null) {
            entrantAdapter = new EntrantArrayAdapter(this, entrants_store);
            participantList.setAdapter(entrantAdapter);
        } else {
            entrantAdapter.notifyDataSetChanged();
        }
    }

    public void setButtonState(Boolean enabled) {

        slider.setEnabled(enabled);
        selectButton.setEnabled(enabled);
        geoButton.setEnabled(enabled);
        if (enabled) {
            slider.setAlpha(1f);
            selectButton.setAlpha(1f);
            geoButton.setAlpha(1f);
        } else {
            slider.setAlpha(0.1f);
            selectButton.setAlpha(0.1f);
            geoButton.setAlpha(0.1f);
        }

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