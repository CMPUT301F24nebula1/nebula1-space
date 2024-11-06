//code review from ChatGPT
package com.example.cmput301project.pooling;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cmput301project.R;
import com.example.cmput301project.model.Entrant;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Optional;

public class EntrantStatusActivity extends AppCompatActivity {

    private TextView tvStatus;
    private Button btnAccept, btnDecline;
    private FirebaseFirestore db;
    private String entrantId;
//   this part working on for finding the entrant status
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_status);

        tvStatus = findViewById(R.id.tvStatus);
        btnAccept = findViewById(R.id.btnAccept);
        btnDecline = findViewById(R.id.btnDecline);

        db = FirebaseFirestore.getInstance();

        // get entrantId from intent
        entrantId = Optional.ofNullable(getIntent().getStringExtra("entrantId"))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .orElseGet(() -> {
                    showErrorAndFinish("Entrant ID not provided");
                    return null;
                });

        if (entrantId == null) {
            return;
        }
        fetchEntrantStatus();
        btnAccept.setOnClickListener(v -> updateEntrantStatus("ENROLLED"));
        btnDecline.setOnClickListener(v -> updateEntrantStatus("DECLINED"));
    }
    private void showErrorAndFinish(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        finish();
    }

    //   this part working on the database
    private void fetchEntrantStatus() {
        DocumentReference entrantRef = db.collection("waitingList").document(entrantId);
        entrantRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot != null && snapshot.exists()) {
                Entrant entrant = snapshot.toObject(Entrant.class);
                if (entrant != null) {
                    updateUI(entrant.getStatus()); // 更新UI
                } else {
                    showToast("Entrant data is corrupted.");
                }
            } else {
                showToast("Entrant not found.");
            }
        }).addOnFailureListener(e -> {
            showToast("Failed to fetch entrant: " + e.getMessage());
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

//    this part working on the UI
    private void updateUI(String status) {
        if ("WAITING".equals(status)) {
            tvStatus.setText("waiting...");
            btnAccept.setVisibility(View.GONE);
            btnDecline.setVisibility(View.GONE);
        } else if ("SELECTED".equals(status)) {
            tvStatus.setText("selected！");
            btnAccept.setVisibility(View.VISIBLE);
            btnDecline.setVisibility(View.VISIBLE);
        } else if ("ENROLLED".equals(status)) {
            tvStatus.setText("enrolled！");
            btnAccept.setVisibility(View.GONE);
            btnDecline.setVisibility(View.GONE);
        } else if ("DECLINED".equals(status)) {
            tvStatus.setText("declined。");
            btnAccept.setVisibility(View.GONE);
            btnDecline.setVisibility(View.GONE);
        } else {
            tvStatus.setText("unknow");
            btnAccept.setVisibility(View.GONE);
            btnDecline.setVisibility(View.GONE);
        }
    }
//    this part working on the button click
    private void updateEntrantStatus(String newStatus) {
        btnAccept.setEnabled(false);
        btnDecline.setEnabled(false);

        db.collection("waitingList").document(entrantId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "update " + newStatus, Toast.LENGTH_SHORT).show();
                    fetchEntrantStatus();  // updae UI
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "updae fail", Toast.LENGTH_SHORT).show();
                    btnAccept.setEnabled(true);
                    btnDecline.setEnabled(true);
                });
    }
}
