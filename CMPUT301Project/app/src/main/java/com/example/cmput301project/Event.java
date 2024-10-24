package com.example.cmput301project;

import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

public class Event {
    private ArrayList<Entrant> waitlist;
    private int limit;
    private String name;
    private String description;
    private String posterUrl;
    private String qrCode;
    private String hashedQRCode;
    private final String id;

    public Event() {
        this.waitlist = new ArrayList<Entrant>();
        this.id = UUID.randomUUID().toString();
    }

    public Event(String name) {
        this.waitlist = new ArrayList<Entrant>();
        this.name = name;
        this.id = UUID.randomUUID().toString();
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

    public String getId() {
        return id;
    }

    public String getHashedQRCode() {
        return hashedQRCode;
    }

    public void setHashedQRCode(String hashedQRCode) {
        this.hashedQRCode = hashedQRCode;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }
}
