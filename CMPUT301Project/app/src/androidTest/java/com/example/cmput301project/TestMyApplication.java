package com.example.cmput301project;

import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Organizer;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class TestMyApplication extends MyApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Firebase emulator or other test-specific dependencies here
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.useEmulator("10.0.2.2", 8080);
        setDb(db);
    }

    public void setMockData() {
        // Set mock data for testing
        String mockId = UUID.randomUUID().toString();

        Entrant mockEntrant = new Entrant(mockId);
        mockEntrant.setName("Mock Entrant");
        setEntrant(mockEntrant);
        getEntrantLiveData().postValue(mockEntrant);

        Organizer mockOrganizer = new Organizer(mockId);
        mockOrganizer.setName("Mock Organizer");
        setOrganizer(mockOrganizer);
        getOrganizerLiveData().postValue(mockOrganizer);


    }
}

