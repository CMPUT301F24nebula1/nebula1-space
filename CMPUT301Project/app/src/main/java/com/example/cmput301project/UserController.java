package com.example.cmput301project;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class UserController {

    public static void updateUserRole(String userId, String roleToAdd) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                ArrayList<String> roles = (ArrayList<String>) documentSnapshot.get("role");
                if (roles != null && !roles.contains(roleToAdd)) {
                    roles.add(roleToAdd);
                    userRef.update("role", roles)
                            .addOnSuccessListener(aVoid -> Log.d("Firestore", "User role successfully updated!"))
                            .addOnFailureListener(e -> Log.w("Firestore", "Error updating user role", e));
                    addOrganizer(new Organizer(userId));

                    DocumentReference entrantRef = db.collection("entrants").document(userId);
                    entrantRef.get().addOnSuccessListener(documentSnapshot1 -> {
//                        ArrayList<String> roles1 = (ArrayList<String>) documentSnapshot.get("role");
//                        roles1.add("organizer");
                        entrantRef.update("role", roles)
                                .addOnSuccessListener(aVoid -> Log.e("Firestore", "Entrant role successfully updated!"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Error updating entrant role", e));
                    }).addOnFailureListener(e -> Log.e("Firestore", "Error getting entrant document", e));

                } else {
                    Log.d("Firestore", "Role already exists or roles is null");
                }
            } else {
                Log.d("Firestore", "No such document");
            }
        }).addOnFailureListener(e -> Log.w("Firestore", "Error getting document", e));
    }

    public static void addOrganizer(Organizer o) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("organizers").document(o.getId())
                .set(o)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User successfully added!");
                })
                .addOnFailureListener(f -> {
                    Log.w("Firestore", "Error adding user", f);
                });
    }
}
