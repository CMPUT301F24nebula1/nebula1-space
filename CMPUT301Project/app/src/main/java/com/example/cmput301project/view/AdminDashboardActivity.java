package com.example.cmput301project.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cmput301project.R;

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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Home");
        }

        // Navigate to Profiles Management
        manageProfilesButton.setOnClickListener(view -> {
            Log.d("AdminDashboardActivity", "Manage Profiles button clicked");
            Intent intent = new Intent(AdminDashboardActivity.this, AdminAllProfilesActivity.class);
            startActivity(intent);
        });

        // Navigate to Events Management
        manageEventsButton.setOnClickListener(view -> {
            Log.d("AdminDashboardActivity", "Manage Events button clicked");
            Intent intent = new Intent(AdminDashboardActivity.this, AdminAllEventsActivity.class);
            startActivity(intent);
        });

        // Navigate to Images Management
        manageImagesButton.setOnClickListener(view -> {
            Log.d("AdminDashboardActivity", "Manage Images button clicked");
            Intent intent = new Intent(AdminDashboardActivity.this, AdminManageImagesActivity.class);
            startActivity(intent);
        });

        // Navigate to QR Code Management (Placeholder for future functionality)
        manageQrButton.setOnClickListener(view -> {
            Log.d("AdminDashboardActivity", "Manage QR Code button clicked");
            // TODO: Implement QR Code Management
        });
    }

    // Handle the back button in the toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
