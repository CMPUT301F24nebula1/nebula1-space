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

        Button manageProfilesButton = findViewById(R.id.manageProfilesButton);
        Button manageEventsButton = findViewById(R.id.manageEventsButton);
        Button manageImagesButton = findViewById(R.id.manageImagesButton);
        Button manageQrButton = findViewById(R.id.manageQrButton);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Home");

        manageProfilesButton.setOnClickListener(view -> {
            Log.d("AdminDashboardActivity", "Manage Profiles button clicked");
            Intent intent = new Intent(AdminDashboardActivity.this, AdminAllProfilesActivity.class);
            startActivity(intent);
        });


        manageEventsButton.setOnClickListener(view -> {
            Log.d("AdminDashboardActivity", "Manage Events button clicked");
            Intent intent = new Intent(AdminDashboardActivity.this, AdminAllEventsActivity.class);
            startActivity(intent);
            Log.d("AdminDashboardActivity", "Intent to AdminAllEventsActivity started");
        });

        manageImagesButton.setOnClickListener(v -> {
            // TODO: Navigate to images management
        });

        manageQrButton.setOnClickListener(v -> {
            Log.d("AdminDashboardActivity", "Manage QR Codes button clicked");
            Intent intent = new Intent(AdminDashboardActivity.this, AdminQRController.class); // Navigates to QR code management
            startActivity(intent);
        });

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