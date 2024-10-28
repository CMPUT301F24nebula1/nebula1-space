package com.example.cmput301project;

import java.util.ArrayList;

public class Entrant extends User{
    protected transient ArrayList<String> waitlistEventIds;
    protected String name;
    protected String email;
    protected String phone;
    private String profilePictureUrl;

    public Entrant() {
        this.waitlistEventIds = new ArrayList<String>();;
    }
    public Entrant(String id) {
        super(id);
        this.waitlistEventIds = new ArrayList<String>();
    }

    public void join_event(Event event) {
        event.add_entrant(this);
        waitlistEventIds.add(event.getId());
    }

    public void leave_event(Event event) {
        event.remove_entrant(this);
        waitlistEventIds.remove(event.getId());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public ArrayList<String> getWaitlistEventIds() {
        return waitlistEventIds;
    }

    public void setWaitlistEventIds(ArrayList<String> waitlistEventIds) {
        this.waitlistEventIds = waitlistEventIds;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }
}
