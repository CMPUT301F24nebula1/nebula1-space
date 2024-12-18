package com.example.cmput301project.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cmput301project.R;
import com.example.cmput301project.controller.AdminQRController;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_homepage);

        // Initialize buttons
        Button manageProfilesButton = findViewById(R.id.manageProfilesButton);
        Button manageEventsButton = findViewById(R.id.manageEventsButton);
        Button manageImagesButton = findViewById(R.id.manageImagesButton);
        Button manageQrButton = findViewById(R.id.manageQrButton);
        Button manageFacilitiesButton = findViewById(R.id.manageFacilitiesButton);

        // Set up toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Admin Dashboard");
        }

        // Manage Profiles button listener
        manageProfilesButton.setOnClickListener(view -> {
            Log.d("AdminDashboardActivity", "Manage Profiles button clicked");
            Intent intent = new Intent(AdminDashboardActivity.this, AdminAllProfilesActivity.class);
            startActivity(intent);
        });

        // Manage Events button listener
        manageEventsButton.setOnClickListener(view -> {
            Log.d("AdminDashboardActivity", "Manage Events button clicked");
            Intent intent = new Intent(AdminDashboardActivity.this, AdminAllEventsActivity.class);
            startActivity(intent);
        });

        // Manage Images button listener
        manageImagesButton.setOnClickListener(view -> {
            Log.d("AdminDashboardActivity", "Manage Images button clicked");
            Intent intent = new Intent(AdminDashboardActivity.this, AdminManageImagesActivity.class);
            startActivity(intent);
        });

        // Manage QR Codes button listener
        manageQrButton.setOnClickListener(view -> {
            Log.d("AdminDashboardActivity", "Manage QR Codes button clicked");
            Intent intent = new Intent(AdminDashboardActivity.this, AdminQRController.class); // Use correct Activity class
            startActivity(intent);
        });

        // Manage Facilities button listener
        manageFacilitiesButton.setOnClickListener(view -> {
            Log.d("AdminDashboardActivity", "Manage Facilities button clicked");
            Intent intent = new Intent(AdminDashboardActivity.this, AdminManageFacilitiesActivity.class);
            startActivity(intent);
        });
    }

    // Handle back navigation
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
