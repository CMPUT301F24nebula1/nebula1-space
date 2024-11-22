package com.example.cmput301project.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cmput301project.R;
import com.example.cmput301project.controller.EntrantArrayAdapter;
import com.example.cmput301project.controller.EntrantController;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.service.PoolingService;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.slider.Slider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

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
    private String status;

    private Map<String, ArrayList<Entrant>> entrantCache = new HashMap<>();

    private ListView participantList;
    private EntrantArrayAdapter entrantAdapter;

    private Slider slider;
    private MaterialButton selectButton;
    private MaterialButton geoButton;
    private Button cancelButton;
    private MaterialButton notifyButton;

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
        cancelButton = findViewById(R.id.remove_button);
        notifyButton = findViewById(R.id.notify_button);

        setSupportActionBar(findViewById(R.id.toolbar_select));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Entrant Lists");

        retrieveEntrantsWithRealtimeUpdates(event, "WAITING", new RetrieveEntrantsCallback() {
            @Override
            public void onRetrieveEntrantsCompleted(List<Entrant> entrants) {
                isDataLoaded = true;

                if (entrants.isEmpty()) {
                    setToggleButtonsAndSlider(0);
                } else if (entrants.size() == 1) {
//                    setButtonState(true);
//                    slider.setVisibility(View.GONE);
                    selectButton.setText("Select 1 Participant");
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

                        for (Entrant entrant : entrants_store) {
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
            setToggleButtonsAndSlider(0);
            Log.d("FirebaseQuery", "No wishlist IDs to query.");
        }
    }

    private void setButtonState() {
        if (entrantAdapter != null)
            entrantAdapter.setAllCheckboxesSelected(false);
        if (toggleGroup.getCheckedButtonId() == R.id.btn_waitlist) {
            updateEntrantsList(new ArrayList<>(entrants_waitlist));
            setToggleButtonsAndSlider(entrants_waitlist.size());
            cancelButton.setVisibility(View.GONE);
            status = "WAITING";
        } else if (toggleGroup.getCheckedButtonId() == R.id.btn_selected) {
            updateEntrantsList(new ArrayList<>(entrants_selected));
            setButtonInvisible();
            entrantAdapter.setCheckboxVisibility(true);
            cancelButton.setVisibility(View.VISIBLE);
            status = "SELECTED";
        } else if (toggleGroup.getCheckedButtonId() == R.id.btn_canceled) {
            updateEntrantsList(new ArrayList<>(entrants_canceled));
            setButtonInvisible();
            entrantAdapter.setCheckboxVisibility(false);
            status = "CANCELED";
        } else if (toggleGroup.getCheckedButtonId() == R.id.btn_final) {
            updateEntrantsList(new ArrayList<>(entrants_final));
            setButtonInvisible();
            entrantAdapter.setCheckboxVisibility(false);
            status = "FINAL";
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
        }
        if (entrantAdapter != null)
            entrantAdapter.setCheckboxVisibility(false);

        selectButton.setVisibility(View.VISIBLE);
        geoButton.setVisibility(View.VISIBLE);

        selectButton.setAlpha(alpha);
        selectButton.setClickable(enabled);
        geoButton.setAlpha(alpha);
        geoButton.setClickable(enabled);
    }

    private void setButtonInvisible() {
        selectButton.setVisibility(View.GONE);
        geoButton.setVisibility(View.GONE);
        slider.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_all_menu, menu);
        return true;
    }


}