package com.example.cmput301project.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cmput301project.FirebaseServer;
import com.example.cmput301project.R;
import com.example.cmput301project.controller.FacilityAdapter;
import com.example.cmput301project.model.Organizer;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class AdminManageFacilitiesActivity extends AppCompatActivity {
    private RecyclerView facilitiesRecyclerView;
    private FacilityAdapter facilityAdapter;
    private List<Organizer> facilitiesList = new ArrayList<>();
    private FirebaseServer firebaseServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_manage_facilities_activity);

        firebaseServer = new FirebaseServer();

        // Setup Toolbar Back Button
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup RecyclerView
        facilitiesRecyclerView = findViewById(R.id.facilitiesRecyclerView);
        facilitiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        facilityAdapter = new FacilityAdapter(facilitiesList, this::onDeleteFacility);
        facilitiesRecyclerView.setAdapter(facilityAdapter);

        // Setup SearchView
        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.searchName);
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterFacilities(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterFacilities(newText);
                return true;
            }
        });

        // Fetch facilities
        fetchFacilities();
    }

    private void fetchFacilities() {
        firebaseServer.fetchAllFacilities(organizers -> {
            facilitiesList.clear();
            for (Organizer organizer : organizers) {
                if (organizer.getName() != null && !organizer.getName().trim().isEmpty()) {
                    facilitiesList.add(organizer);
                }
            }
            facilityAdapter.notifyDataSetChanged();
        }, e -> {
            Toast.makeText(this, "Failed to fetch facilities", Toast.LENGTH_SHORT).show();
        });
    }

    private void onDeleteFacility(Organizer organizer) {
        firebaseServer.deleteOrganizerWithDependencies(organizer.getId(), aVoid -> {
            facilitiesList.remove(organizer);
            facilityAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Facility deleted successfully", Toast.LENGTH_SHORT).show();
        }, e -> {
            Toast.makeText(this, "Failed to delete facility", Toast.LENGTH_SHORT).show();
            Log.e("AdminManageFacilities", "Error deleting facility", e);
        });
    }

    private void filterFacilities(String query) {
        List<Organizer> filteredList = new ArrayList<>();
        for (Organizer organizer : facilitiesList) {
            if (organizer.getName() != null && organizer.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(organizer);
            }
        }
        facilityAdapter.updateFacilities(filteredList); // Update the adapter with filtered results
    }
}
