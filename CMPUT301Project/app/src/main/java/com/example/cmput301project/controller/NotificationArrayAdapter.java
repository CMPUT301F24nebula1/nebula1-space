package com.example.cmput301project.controller;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import com.example.cmput301project.model.Entrant;
import com.example.cmput301project.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationArrayAdapter extends ArrayAdapter<Notification> {
    public NotificationArrayAdapter(@NonNull Context context, int resource, @NonNull ArrayList<Notification> objects) {
        super(context, resource, objects);
    }
}
