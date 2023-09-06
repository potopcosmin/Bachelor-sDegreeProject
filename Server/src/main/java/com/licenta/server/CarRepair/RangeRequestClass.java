package com.licenta.server.CarRepair;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class RangeRequestClass {

    double latitude;
    double longitude;
    double range;

    public RangeRequestClass() {
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }
}
