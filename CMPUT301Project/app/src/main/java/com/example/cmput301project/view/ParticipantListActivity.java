package com.example.cmput301project.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cmput301project.R;
import com.example.cmput301project.controller.EntrantArrayAdapter;
import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Event;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;

import org.checkerframework.checker.units.qual.A;

import java.io.Serializable;
import java.util.ArrayList;


public class ParticipantListActivity extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private MaterialButtonToggleGroup toggleGroup;

    private ArrayList<Entrant> entrants;
    private ListView participantList;
    private EntrantArrayAdapter entrantAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.participant_list);

        participantList = findViewById(R.id.participant_list);

        Intent intent = getIntent();

        Event event = (Event) intent.getSerializableExtra("event");

        entrants = new ArrayList<Entrant>();

        Log.d("event wishlist participantactivity", event.getWaitlistEntrantIds().toString());

        toggleGroup = findViewById(R.id.listToggleGroup);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Entrant Lists");

        retrieveEntrants(event);

//        if (entrants != null && !entrants.isEmpty()) {
//            entrantAdapter = new EntrantArrayAdapter(this, entrants);
//            participantList.setAdapter(entrantAdapter);
//        }

    }

    public void retrieveEntrants(Event event) {
        if (event.getWaitlistEntrantIds() != null && !event.getWaitlistEntrantIds().isEmpty()) {
            db.collection("entrants")
                    .whereIn(FieldPath.documentId(), event.getWaitlistEntrantIds())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Entrant entrant = document.toObject(Entrant.class);
                                entrants.add(entrant);
                            }
                            if (entrants != null && !entrants.isEmpty()) {
                                entrantAdapter = new EntrantArrayAdapter(this, entrants);
                                participantList.setAdapter(entrantAdapter);
                            }
//                            entrantAdapter.notifyDataSetChanged();
                        } else {
                            // Handle the error
                            Log.e("FirebaseError", "Error fetching entrants: ", task.getException());
                        }
                    });
        } else {
            Log.d("FirebaseQuery", "No wishlist IDs to query.");
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