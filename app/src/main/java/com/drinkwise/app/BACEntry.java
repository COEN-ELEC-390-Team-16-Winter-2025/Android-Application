package com.drinkwise.app;

import com.google.firebase.Timestamp;

public class BACEntry {
    private final double bac;
    private final Timestamp timestamp;

    public BACEntry(double bac, Timestamp timestamp) {
        this.bac = bac;
        this.timestamp = timestamp;
    }

    public double getBac() {
        return bac;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }}