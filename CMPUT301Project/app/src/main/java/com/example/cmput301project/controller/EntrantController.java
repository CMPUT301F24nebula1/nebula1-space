package com.example.cmput301project.controller;

import android.net.Uri;
import android.util.Log;

import com.example.cmput301project.FirebaseServer;
import com.example.cmput301project.model.Event;
import com.example.cmput301project.model.Entrant;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Acts as the controller for an entrant.
 * @author Xinjia Fan
 */
public class EntrantController {
    private Entrant entrant;
    FirebaseServer fb;

    public EntrantController(Entrant e) {
        this.entrant = e;
        fb = new FirebaseServer();
    }

    public void joinEventWaitingList(Event event) {
        entrant.join_event(event);
        //fb.joinEventWaitingList(event, entrant.getId());
    }

    public void addToEventWaitingList(Event event) {
        event.add_entrant(entrant);
        fb.addToEventWaitingList(event, entrant.getId());
    }

    public void leaveEventWaitingList(Event event) {
        entrant.leave_event(event);
        //fb.leaveEventWaitingList(event, entrant.getId());
    }

    public void removeFromEventWaitingList(Event event) {
        event.remove_entrant(entrant);
        fb.removeFromEventWaitingList(event, entrant.getId());
    }

}
