package com.example.cmput301project.view;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.cmput301project.R;
import com.example.cmput301project.model.Entrant;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText nameInput;
    private EditText emailInput;
    private EditText phoneInput;
    private ImageView profileImageView;
    private Button deleteUserButton;

    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_single_profile); // Use the XML layout you provided

        db = FirebaseFirestore.getInstance();

        // Initialize views
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        profileImageView = findViewById(R.id.edit_profile_imageview);
        deleteUserButton = findViewById(R.id.deleteUserButton);

        // Get userId from Intent
        userId = getIntent().getStringExtra("userId");

        // Load user data into fields
        loadUserData();

        // Set up delete user functionality
        deleteUserButton.setOnClickListener(v -> deleteUser());
    }

    private void loadUserData() {
        db.collection("entrants").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Entrant entrant = documentSnapshot.toObject(Entrant.class);
                nameInput.setText(entrant.getName());
                emailInput.setText(entrant.getEmail());
                phoneInput.setText(entrant.getPhone());
                if (entrant.getProfilePictureUrl() != null && !entrant.getProfilePictureUrl().isEmpty()) {
                    Glide.with(this)
                            .load(entrant.getProfilePictureUrl())
                            .error(R.drawable.error_image) // Default image in case of error
                            .into(profileImageView);
                }
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
        });
    }

    private void deleteUser() {
        db.collection("entrants").document(userId).delete().addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "User deleted", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error deleting user", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveUserData() {
        String name = nameInput.getText().toString();
        String email = emailInput.getText().toString();
        String phone = phoneInput.getText().toString();

        Entrant updatedEntrant = new Entrant(userId);
        updatedEntrant.setName(name);
        updatedEntrant.setEmail(email);
        updatedEntrant.setPhone(phone);

        db.collection("entrants").document(userId).set(updatedEntrant).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "User data updated", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error updating user data", Toast.LENGTH_SHORT).show();
        });
    }


    // Implement save functionality here if needed
}
