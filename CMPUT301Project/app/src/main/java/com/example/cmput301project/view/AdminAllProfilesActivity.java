package com.example.cmput301project.view;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cmput301project.R;
import com.example.cmput301project.controller.ProfileRecyclerViewAdapter;
import com.example.cmput301project.model.Entrant;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AdminAllProfilesActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ProfileRecyclerViewAdapter profileAdapter;
    private List<Entrant> entrantList;
    private SearchView searchView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_profiles);

        db = FirebaseFirestore.getInstance();

        setSupportActionBar(findViewById(R.id.toolbar_profiles));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Profiles");
        }

        recyclerView = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.searchName);
        progressBar = findViewById(R.id.progressBar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        entrantList = new ArrayList<>();

        profileAdapter = new ProfileRecyclerViewAdapter(this, entrantList, new ProfileRecyclerViewAdapter.OnProfileClickListener() {
            @Override
            public void onDeleteClick(Entrant entrant, int position) {
                deleteEntrant(entrant, position);
            }
        });

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
        progressBar.setVisibility(ProgressBar.VISIBLE); // Show the progress bar while loading
        db.collection("entrants")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(ProgressBar.GONE); // Hide the progress bar once done
                    if (task.isSuccessful()) {
                        entrantList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Entrant entrant = document.toObject(Entrant.class);
                            if (entrant != null) {
                                entrant.setId(document.getId()); // Set the ID manually
                                entrantList.add(entrant);
                            }
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
            if (entrant.getName() != null && entrant.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(entrant);
            }
        }
        profileAdapter.updateList(filteredList);
    }

//    private void deleteEntrant(Entrant entrant, int position) {
//        if (entrant == null || entrant.getId() == null) {
//            Toast.makeText(this, "Error: Invalid entrant", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        db.collection("entrants").document(entrant.getId())
//                .delete()
//                .addOnSuccessListener(aVoid -> {
//                    deleteUserRole("entrant", entrant.getId(), new DeleteRoleCallback() {
//                        @Override
//                        public void onSuccess(String message) {
//                            Toast.makeText(AdminAllProfilesActivity.this, "Profile deleted successfully", Toast.LENGTH_SHORT).show();
//                            entrantList.remove(position); // Remove from the list
//                            profileAdapter.notifyItemRemoved(position); // Notify adapter
//                        }
//                        @Override
//                        public void onFailure(String error) {
//                            Toast.makeText(AdminAllProfilesActivity.this, "Failed to delete profile", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(this, "Failed to delete profile", Toast.LENGTH_SHORT).show();
//                });
//    }

    private void deleteEntrant(Entrant entrant, int position) {
        if (entrant == null || entrant.getId() == null) {
            Toast.makeText(this, "Error: Invalid entrant", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference entrantRef = db.collection("entrants").document(entrant.getId());

        // Step 1: Delete subcollections explicitly
        deleteSubcollection(entrantRef.collection("entrantWaitList"), db, new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                deleteSubcollection(entrantRef.collection("notifications"), db, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Step 2: Delete the main document
                        entrantRef.delete().addOnSuccessListener(aVoid1 -> {
                            deleteUserRole("entrant", entrant.getId(), new DeleteRoleCallback() {
                                @Override
                                public void onSuccess(String message) {
                                    Toast.makeText(AdminAllProfilesActivity.this, "Profile deleted successfully", Toast.LENGTH_SHORT).show();
                                    entrantList.remove(position); // Remove from the list
                                    profileAdapter.notifyItemRemoved(position); // Notify adapter
                                }

                                @Override
                                public void onFailure(String error) {
                                    Toast.makeText(AdminAllProfilesActivity.this, "Failed to delete profile role", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }).addOnFailureListener(e -> {
                            Toast.makeText(AdminAllProfilesActivity.this, "Failed to delete profile", Toast.LENGTH_SHORT).show();
                        });
                    }
                }, e -> {
                    Toast.makeText(AdminAllProfilesActivity.this, "Failed to delete notifications subcollection", Toast.LENGTH_SHORT).show();
                });
            }
        }, e -> {
            Toast.makeText(AdminAllProfilesActivity.this, "Failed to delete entrantWaitList subcollection", Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteSubcollection(CollectionReference subcollection, FirebaseFirestore db, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        subcollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                WriteBatch batch = db.batch();
                for (DocumentSnapshot doc : task.getResult()) {
                    batch.delete(doc.getReference());
                }
                batch.commit().addOnCompleteListener(batchTask -> {
                    if (batchTask.isSuccessful()) {
                        onSuccess.onSuccess(null);
                    } else {
                        onFailure.onFailure(batchTask.getException());
                    }
                });
            } else {
                onFailure.onFailure(task.getException());
            }
        });
    }


    public interface DeleteRoleCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }

    private void deleteUserRole(String role, String id, DeleteRoleCallback callback) {
        // Reference the specific document in the "users" collection
        db.collection("users").document(id)
                .update("role", FieldValue.arrayRemove(role))
                .addOnSuccessListener(aVoid -> {
                    String message = "Successfully removed '" + role + "' from the role array of document: " + id;
                    callback.onSuccess(message);
                })
                .addOnFailureListener(e -> {
                    String error = "Error removing '" + role + "' from document " + id + ": " + e.getMessage();
                    callback.onFailure(error);
                });
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