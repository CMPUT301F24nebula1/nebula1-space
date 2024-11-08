package com.example.cmput301project.view;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cmput301project.R;
import com.example.cmput301project.controller.ProfileRecyclerViewAdapter;
import com.example.cmput301project.model.Entrant;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminAllProfilesActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ProfileRecyclerViewAdapter profileAdapter;
    private List<Entrant> entrantList;
    private SearchView searchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_profiles);

        db = FirebaseFirestore.getInstance();

        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchName);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        entrantList = new ArrayList<>();
        profileAdapter = new ProfileRecyclerViewAdapter(this, entrantList);
        recyclerView.setAdapter(profileAdapter);

        loadAllEntrantsFromFirebase();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProfiles(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProfiles(newText);
                return true;
            }
        });
    }

    private void loadAllEntrantsFromFirebase() {
        db.collection("entrants") // Ensure this path is correct
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        entrantList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Entrant entrant = document.toObject(Entrant.class);
                            entrantList.add(entrant);
                        }
                        profileAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load entrants.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filterProfiles(String query) {
        List<Entrant> filteredList = new ArrayList<>();
        for (Entrant entrant : entrantList) {
            if (entrant.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(entrant);
            }
        }
        profileAdapter.updateList(filteredList);
    }
}
