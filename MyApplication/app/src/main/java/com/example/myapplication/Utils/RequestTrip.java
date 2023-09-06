package com.example.myapplication.Utils;

import java.util.Observable;

public class RequestTrip extends Observable {
    private int  tripID=-1;
    private float latitude;
    private float longitude;

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public int getTripID() {
        return tripID;
    }

    public void setTripID(int tripID) {
        this.tripID = tripID;
        setChanged();
        notifyObservers();
    }
}
