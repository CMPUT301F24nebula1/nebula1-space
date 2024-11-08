package com.example.cmput301project.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
            // TODO: Navigate to QR code management
        });
    }
}
