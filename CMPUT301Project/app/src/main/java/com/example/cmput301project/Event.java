package com.example.cmput301project;

import java.util.ArrayList;

public class Event {
    private ArrayList<Entrant> waitlist;
    private int limit;
    private String name;
    private String description;
    private String posterUrl;

    public Event(String name) {
        this.waitlist = new ArrayList<Entrant>();
        this.name = name;
    }

    public void add_entrant(Entrant e) {
        waitlist.add(e);
    }

    public void remove_entrant(Entrant e) {
        waitlist.remove(e);
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }
}
