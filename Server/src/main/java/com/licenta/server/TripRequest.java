package com.licenta.server;

import java.util.concurrent.atomic.AtomicInteger;

public class TripRequest {
    private static final AtomicInteger count = new AtomicInteger(0);
    int tripId;
    int clientLatitude;
    int clientLongitude;

    public TripRequest() {
        tripId=count.incrementAndGet();

    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public int getClientLatitude() {
        return clientLatitude;
    }

    public void setClientLatitude(int clientLatitude) {
        this.clientLatitude = clientLatitude;
    }

    public int getClientLongitude() {
        return clientLongitude;
    }

    public void setClientLongitude(int clientLongitude) {
        this.clientLongitude = clientLongitude;
    }
}
