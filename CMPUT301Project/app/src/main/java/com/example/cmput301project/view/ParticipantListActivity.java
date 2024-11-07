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

import java.io.Serializable;
import java.util.ArrayList;


public class ParticipantListActivity extends AppCompatActivity {
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

        Event event = (Event)intent.getSerializableExtra("event");

        Log.d("event wishlist", event.getWaitlistEntrantIds().toString());

        toggleGroup = findViewById(R.id.listToggleGroup);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Entrant Lists");



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
