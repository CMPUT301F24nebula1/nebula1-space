package com.example.cmput301project;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.cmput301project.databinding.ActivityMainBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;


    private FirebaseFirestore db;
    private CollectionReference userRef;
    private String id;
    private Organizer o;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        id = getDeviceId(this);

        ((MyApplication) this.getApplication()).setUserId(id);
        ((MyApplication) this.getApplication()).setDb(FirebaseFirestore.getInstance());
        db = ((MyApplication) this.getApplication()).getDb();
        retrieveUser(id);
        ((MyApplication) this.getApplication()).listenToFirebaseUpdates(id);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Go back to the previous activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private String getDeviceId(Context context) {
        // Retrieve ANDROID_ID as the device ID
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void retrieveUser(String userId) {
        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Organizer organizer = documentSnapshot.toObject(Organizer.class);
                if (organizer != null) {
                    // Set the retrieved organizer to the Application class
                    ((MyApplication) getApplication()).setOrganizer(organizer);
                    ((MyApplication) getApplication()).setOrganizerLiveData(organizer);
                }
            } else {
                Organizer newOrganizer = new Organizer(userId);
                ((MyApplication) getApplication()).setOrganizer(newOrganizer);
                addUser(newOrganizer);
            }
        }).addOnFailureListener(e -> {
            Log.e("Firebase", "Error retrieving organizer", e);
        });
    }

    public void addUser(Organizer organizer) {
        db.collection("users").document(organizer.getId())
                .set(organizer)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User successfully added!");
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error adding user", e);
                });
    }


}