package com.example.cmput301project.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.cmput301project.R;
import com.example.cmput301project.controller.EntrantArrayAdapter;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.service.PoolingService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.slider.Slider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ParticipantListActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private PoolingService poolingService;

    private MaterialButtonToggleGroup toggleGroup;

    private String organizerId;
    Event event;

    private ArrayList<Entrant> entrants_store;
    private ArrayList<Entrant> entrants_waitlist; // entrants with status "WAITING"
    private ArrayList<Entrant> entrants_selected; // entrants with status "SELECTED"
    private ArrayList<Entrant> entrants_canceled; // entrants with status "CANCELED"
    private ArrayList<Entrant> entrants_final; // entrants with status "FINAL"
    private String status;

    private Map<String, ArrayList<Entrant>> entrantCache = new HashMap<>();

    private ListView participantList;
    private EntrantArrayAdapter entrantAdapter;

    private Slider slider;
    private MaterialButton selectButton;
    private MaterialButton geoButton;
    private Button cancelButton;
    private Button finalizeButton;
    private MaterialButton notifyButton;
    private ProgressBar progressBar;
    private ConstraintLayout mainLayout;
    TextView finalizationText;

    private boolean isDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.participant_list);

        poolingService = new PoolingService();

        participantList = findViewById(R.id.participant_list);

        Intent intent = getIntent();

        event = (Event) intent.getSerializableExtra("event");
        organizerId = (String) intent.getStringExtra("organizerId");
        Log.d("organizerId", organizerId);

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
        cancelButton = findViewById(R.id.remove_button);
        notifyButton = findViewById(R.id.notify_button);
        finalizeButton = findViewById(R.id.finalize_button);
        progressBar = findViewById(R.id.progressBar);
        mainLayout = findViewById(R.id.main_layout);
        finalizationText = findViewById(R.id.finalization_text);

        finalizationText.setVisibility(View.GONE);

        setSupportActionBar(findViewById(R.id.toolbar_select));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Entrant Lists");

        lockUI();
        retrieveEntrantsWithRealtimeUpdates(event, "WAITING", new RetrieveEntrantsCallback() {
            @Override
            public void onRetrieveEntrantsCompleted(List<Entrant> entrants) {
                isDataLoaded = true;

                if (entrants == null || entrants.isEmpty()) {
                    setToggleButtonsAndSlider(0);
                    Toast.makeText(ParticipantListActivity.this, "Waitlist is empty.", Toast.LENGTH_SHORT).show();
                } else if (entrants.size() == 1) {
//                    setButtonState(true);
//                    slider.setVisibility(View.GONE);
                    selectButton.setText("Draw 1 Participant");
                } else {
//                    setButtonState(true);
                    slider.setValueTo(entrants.size());
                    slider.setVisibility(View.VISIBLE);
                }
                setButtonState();
            }
        });

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isDataLoaded) {
                    Toast.makeText(ParticipantListActivity.this, "Loading, please try later", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ParticipantListActivity.this, "Pooling!", Toast.LENGTH_SHORT).show();
                    lockUI();
                    poolingService.performPooling(event.getId(), entrants_waitlist, (int) slider.getValue(), new PoolingService.PoolingCallback() {

                        @Override
                        public void onSuccess() {
                            unlockUI();
                            Toast.makeText(ParticipantListActivity.this, "Pooling succeed.", Toast.LENGTH_SHORT).show();
                            entrantAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailure(Exception e) {
                            unlockUI();
                            Toast.makeText(ParticipantListActivity.this, "Pooling failed.", Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            }
        });

        // Set up the listener to update the button text based on the slider value
        slider.addOnChangeListener((slider1, value, fromUser) -> {
            // Update the button text with the current slider value, casting it to an integer
            selectButton.setText("Draw " + (int) value + " Participants");
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

                setButtonState();
            }
        });

        notifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = "";
                Log.d("notify", entrants_store.toString());

                if (!isDataLoaded) {
                    Toast.makeText(ParticipantListActivity.this, "Loading, please try later", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (toggleGroup.getCheckedButtonId() == R.id.btn_selected) {
                    message = "You won the lottery for event: " + event.getName();
                }

                showSimpleEditTextDialog(message, new InputDialogCallback() {
                    @Override
                    public void onInputConfirmed(String notification) {
                        ArrayList<Entrant> selectedEntrants = entrantAdapter.getSelectedEntrants();

                        for (Entrant entrant : selectedEntrants) {
                            Map<String, Object> notificationData = new HashMap<>();
                            notificationData.put("isRead", false); // or "true" if the notification is read
                            notificationData.put("message", notification);
                            notificationData.put("eventId", event.getId());
                            notificationData.put("timestamp", FieldValue.serverTimestamp());
                            notificationData.put("status", status);
                            notificationData.put("title", event.getName());

                            db.collection("entrants")
                                    .document(entrant.getId()) // Set the document ID to entrant.getId()
                                    .collection("notifications")
                                    .add(notificationData)
                                    .addOnSuccessListener(aVoid -> {
                                        // Successfully written to Firestore
                                        Log.d("Firestore", "Notification successfully written for entrant ID: " + entrant.getId());
                                        Toast.makeText(getApplicationContext(), "Notification sent!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle any errors
                                        Log.e("FirestoreError", "Error writing notification", e);
                                    });
                        }
                    }
                });



            }
        });


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Loop through each entrant in the list and set status to "cancelled"
                for (Entrant entrant : entrantAdapter.getSelectedEntrants()) {
                    String entrantId = entrant.getId();
                    db.collection("entrants")
                            .document(entrantId)
                            .collection("entrantWaitList")
                            .document(event.getId())
                            .update("status", "CANCELED")
                            .addOnSuccessListener(aVoid -> {
                                // Handle successful update
                                Log.d("Firestore", "Status updated to SELECTED for entrant ID: " + entrantId);
                                entrantAdapter.setAllCheckboxesSelected(false);
                            })
                            .addOnFailureListener(e -> {
                                // Handle failure in update
                                Log.e("Firestore", "Error updating status for entrant ID: " + entrantId, e);
                            });
                }
            }
        });

        finalizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (event.isFinalized()) {
                    Toast.makeText(ParticipantListActivity.this, "This event has been finalized.", Toast.LENGTH_SHORT).show();
                    return;
                }

                new AlertDialog.Builder(view.getContext())
                        .setTitle("Finalize Action")
                        .setMessage("Are you sure you want to finalize?")
                        .setPositiveButton("Confirm", (dialog, which) -> {
                            // Code to execute after "Confirm" is clicked
                            finalizeEvent(event.getId());
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            // Just dismiss the dialog
                            dialog.dismiss();
                        })
                        .create()
                        .show();

            }
        });

        geoButton.setOnClickListener(view -> {
            Log.d("GEOBUTTON", "GEO BUTTON HAS BEEN CLICKED");
            if (entrants_waitlist.isEmpty()) {
                Toast.makeText(this, "No entrants with location data to display.", Toast.LENGTH_SHORT).show();
            } else {
                // Show the dialog with the map
                Log.d("GeoButtonClick", "Opening WaitlistMapFragment with Event ID: " + event.getId());

                waitlistMapFragment dialog = waitlistMapFragment.newInstance(event.getId());
                dialog.show(getSupportFragmentManager(), "WaitlistMapFragment");
            }
        });


    }

    public void finalizeEvent(String eventId) {
        lockUI(); // Show progress or disable UI interactions

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the specific document in the 'organizers' collection
        DocumentReference organizerDocRef = db.collection("organizers")
                .document(organizerId)
                .collection("events")
                .document(eventId);
        Log.d("organizerId final", organizerId);

        // Update the 'Finalized' field to true
        organizerDocRef.update("isFinalized", true)
                .addOnSuccessListener(aVoid -> {
                    unlockUI(); // Re-enable UI or hide progress
                    Toast.makeText(this, "Event finalized successfully.", Toast.LENGTH_SHORT).show();
                    event.setFinalized(true);
                })
                .addOnFailureListener(e -> {
                    unlockUI(); // Re-enable UI even on failure
                    Toast.makeText(this, "Failed to finalize event: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FinalizeEvent", "Error updating Firestore", e);
                });
    }


    public interface InputDialogCallback {
        void onInputConfirmed(String notification);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showSimpleEditTextDialog(String defaultText, InputDialogCallback callback) {
        final EditText editTextInput = new EditText(this);
        editTextInput.setText(defaultText);

        if (!defaultText.isEmpty()) {
            editTextInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear, 0);
        }

        editTextInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    editTextInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_clear, 0);
                } else {
                    editTextInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editTextInput.setOnTouchListener((v, event) -> {
            Drawable rightDrawable = editTextInput.getCompoundDrawables()[2];
            if (rightDrawable != null && event.getRawX() >= (editTextInput.getRight() - rightDrawable.getBounds().width())) {
                editTextInput.setText(""); // Clear text
                v.performClick();
                return true;
            }
            return false;
        });

        editTextInput.setOnClickListener(v -> {
        });


        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Notification message")
                .setView(editTextInput)
                .setPositiveButton("Confirm", (dialogInterface, which) -> {
                    String inputText = editTextInput.getText().toString().trim();
                    if (!inputText.isEmpty()) {
                        // Pass the inputText to the callback
                        callback.onInputConfirmed(inputText);
                    } else {
                        Toast.makeText(getApplicationContext(), "Please enter some text", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        dialog.show();
    }


    public interface RetrieveEntrantsCallback {
        void onRetrieveEntrantsCompleted(List<Entrant> entrants);
    }

//    also retrieves lat and long info from firebase for users in the waitlist if geolocation is turned on
    public void retrieveEntrantsWithRealtimeUpdates(Event event, String status, RetrieveEntrantsCallback callback) {
        Log.d("no waitlist", event.getWaitlistEntrantIds().toString());

        lockUI();
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
                                            lockUI();

                                            if (error != null) {
                                                unlockUI();
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
                                                        Log.e("StatusError", "Unknown status: " + statusFirebase);
                                                        break;
                                                }


                                                entrantCache.put("WAITING", new ArrayList<>(entrants_waitlist));
                                                entrantCache.put("SELECTED", new ArrayList<>(entrants_selected));
                                                entrantCache.put("CANCELED", new ArrayList<>(entrants_canceled));
                                                entrantCache.put("FINAL", new ArrayList<>(entrants_final));

                                                setButtonState();
                                                Log.d("wishlist debug0", status + entrants_store.toString());

                                                if (entrantAdapter == null) {
                                                    entrantAdapter = new EntrantArrayAdapter(this, entrants_store);
                                                    participantList.setAdapter(entrantAdapter);
                                                } else {
                                                    entrantAdapter.notifyDataSetChanged();
                                                }

                                                completedCount[0]++;

                                                unlockUI();
                                                if (completedCount[0] == totalDocuments) {
                                                    if (entrantAdapter == null) {
                                                        entrantAdapter = new EntrantArrayAdapter(this, entrants_store);
                                                        participantList.setAdapter(entrantAdapter);
                                                    } else {
                                                        entrantAdapter.notifyDataSetChanged();
                                                    }
                                                    unlockUI();
                                                    callback.onRetrieveEntrantsCompleted(entrants_waitlist);
                                                }

                                            }
                                            else {
                                                unlockUI();
                                                callback.onRetrieveEntrantsCompleted(null);
                                            }
                                        });

                                // retrieving location info of users from database
                                if (event.isRequiresGeolocation()) {
                                    db.collection("locations").document(entrantId).get().addOnSuccessListener(locationDoc -> {
                                        if (locationDoc.exists()) {
                                            Double latitude = locationDoc.getDouble("latitude");
                                            Double longitude = locationDoc.getDouble("longitude");
                                            entrant.setLocation(latitude, longitude);
                                        }
                                        processEntrantStatus(event, entrant, entrantId, status, callback, totalDocuments, completedCount);
                                    })
                                            .addOnFailureListener(e -> {
                                                Log.e("FirestoreError", "Error fetching location for entrant: " + entrantId, e);
                                                processEntrantStatus(event, entrant, entrantId, status, callback, totalDocuments, completedCount);
                                            });

                                }else {
                                    processEntrantStatus(event, entrant, entrantId, status, callback, totalDocuments, completedCount);
                                }

                            }
                        } else {
                            unlockUI();
                            Toast.makeText(ParticipantListActivity.this, "Loading failed.", Toast.LENGTH_SHORT).show();
                            Log.e("FirebaseError", "Error fetching entrants: ", task.getException());
                        }
                    });
        } else {
            unlockUI();
            setToggleButtonsAndSlider(0);
            callback.onRetrieveEntrantsCompleted(null);
            Log.d("FirebaseQuery", "No wishlist IDs to query.");
        }
    }


//    helper method for retrieveEntrantWithRealtimeUpdates
private void processEntrantStatus(Event event, Entrant entrant, String entrantId, String status, RetrieveEntrantsCallback callback, int totalDocuments, int[] completedCount) {
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
                            Log.e("StatusError", "Unknown status: " + statusFirebase);
                            break;
                    }

                    entrantCache.put("WAITING", new ArrayList<>(entrants_waitlist));
                    entrantCache.put("SELECTED", new ArrayList<>(entrants_selected));
                    entrantCache.put("CANCELED", new ArrayList<>(entrants_canceled));
                    entrantCache.put("FINAL", new ArrayList<>(entrants_final));

                    setButtonState();
                    Log.d("wishlist debug0", status + entrants_store.toString());

                    if (entrantAdapter == null) {
                        entrantAdapter = new EntrantArrayAdapter(this, entrants_store);
                        participantList.setAdapter(entrantAdapter);
                    } else {
                        entrantAdapter.notifyDataSetChanged();
                    }

                    completedCount[0]++;
                    if (completedCount[0] == totalDocuments) {
                        callback.onRetrieveEntrantsCompleted(entrants_waitlist);
                    }
                }
            });
}

    private void setButtonState() {
        if (entrantAdapter != null)
            entrantAdapter.setAllCheckboxesSelected(false);

        finalizationText.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);

        if (toggleGroup.getCheckedButtonId() == R.id.btn_waitlist) {
            updateEntrantsList(new ArrayList<>(entrants_waitlist));
            setToggleButtonsAndSlider(entrants_waitlist.size());

            entrantAdapter.setCheckboxVisibility(true);
            status = "WAITING";
        } else if (toggleGroup.getCheckedButtonId() == R.id.btn_selected) {
            updateEntrantsList(new ArrayList<>(entrants_selected));
            setButtonInvisible();
            entrantAdapter.setCheckboxVisibility(true);
            cancelButton.setVisibility(View.VISIBLE);
            if (entrants_selected.isEmpty()) {
                Log.d("notifyButton", "debug1");
                cancelButton.setAlpha(0.2f);
                cancelButton.setClickable(false);
                notifyButton.setAlpha(0.2f);
                notifyButton.setClickable(false);
            } else {
                cancelButton.setAlpha(1f);
                cancelButton.setClickable(true);
                notifyButton.setAlpha(1f);
                notifyButton.setClickable(true);
            }
            status = "SELECTED";
        } else if (toggleGroup.getCheckedButtonId() == R.id.btn_canceled) {
            updateEntrantsList(new ArrayList<>(entrants_canceled));
            setButtonInvisible();
            if (entrants_canceled.isEmpty()) {
                notifyButton.setAlpha(0.2f);
                notifyButton.setClickable(false);
            } else {
                notifyButton.setAlpha(1f);
                notifyButton.setClickable(true);
            }
            entrantAdapter.setCheckboxVisibility(true);
            status = "CANCELED";
        } else if (toggleGroup.getCheckedButtonId() == R.id.btn_final) {
            updateEntrantsList(new ArrayList<>(entrants_final));
            setButtonInvisible();
            finalizeButton.setVisibility(View.VISIBLE);
            if (event.isFinalized()) {
                finalizationText.setVisibility(View.VISIBLE);
            }
            if (entrants_final.isEmpty()) {
                finalizeButton.setAlpha(0.2f);
                finalizeButton.setClickable(false);
                notifyButton.setAlpha(0.2f);
                Log.d("notifyButton", "debug2");
                notifyButton.setClickable(false);
            } else {
                finalizeButton.setAlpha(1f);
                finalizeButton.setClickable(true);
                notifyButton.setAlpha(1f);
                notifyButton.setClickable(true);
            }
            entrantAdapter.setCheckboxVisibility(true);
            status = "FINAL";
        }

        if (event.isFinalized()) {
            finalizeButton.setAlpha(0.2f);
            finalizeButton.setClickable(false);
            cancelButton.setAlpha(0.2f);
            cancelButton.setClickable(false);
            selectButton.setAlpha(0.2f);
            selectButton.setClickable(false);
        }
    }

    // update UI
    private void updateEntrantsList(ArrayList<Entrant> entrants) {
        entrants_store.clear();
        entrants_store.addAll(entrants);
        Log.d("waitlist debug entrants_store", entrants_store.toString());
        if (entrantAdapter == null) {
            entrantAdapter = new EntrantArrayAdapter(this, entrants_store);
            entrantAdapter.setCheckboxVisibility(true);
            participantList.setAdapter(entrantAdapter);
        } else {
            entrantAdapter.setCheckboxVisibility(true);
            entrantAdapter.notifyDataSetChanged();
        }
    }

    private void setToggleButtonsAndSlider(int waitingListLength) {
        boolean enabled = waitingListLength > 0;
        float alpha = 1;
        if (!enabled) {
            alpha = .2f;
        }
        slider.setAlpha(alpha);
        slider.setEnabled(enabled);
        if (waitingListLength > 1) {
            slider.setValue(1f);
            slider.setValueTo(waitingListLength);
            slider.setVisibility(View.VISIBLE);
        }
        else if (waitingListLength == 1) {
            slider.setValue(1f);
            slider.setVisibility(View.GONE);
        }
        else if (waitingListLength == 0) {
            slider.setValue(1f);
            slider.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
        }
        if (entrantAdapter != null)
            entrantAdapter.setCheckboxVisibility(false);

        selectButton.setVisibility(View.VISIBLE);
        geoButton.setVisibility(View.VISIBLE);

        selectButton.setAlpha(alpha);
        selectButton.setClickable(enabled);
        geoButton.setAlpha(alpha);
        geoButton.setClickable(enabled);
        notifyButton.setAlpha(alpha);
        notifyButton.setClickable(enabled);
    }

    private void setButtonInvisible() {
        selectButton.setVisibility(View.GONE);
        geoButton.setVisibility(View.GONE);
        slider.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        finalizeButton.setVisibility(View.GONE);
    }

    private void lockUI() {
        progressBar.setVisibility(View.VISIBLE);
        mainLayout.setAlpha(0.5f); // Dim background for effect
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void unlockUI() {
        progressBar.setVisibility(View.GONE);
        mainLayout.setAlpha(1.0f); // Restore background opacity
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    // navigate back to previous activity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.menu_select_all) {
            if (entrantAdapter != null) {
                if (entrantAdapter.areAllCheckboxesSelected()) {
                    entrantAdapter.setAllCheckboxesSelected(false);
                    item.setTitle("SELECT\nALL");
                }
                else {
                    entrantAdapter.setAllCheckboxesSelected(true);
                    item.setTitle("DESELECT\nALL");
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_all_menu, menu);
        return true;
    }


}