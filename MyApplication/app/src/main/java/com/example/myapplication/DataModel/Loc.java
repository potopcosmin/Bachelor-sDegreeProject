package com.example.myapplication.DataModel;

import java.util.List;
import java.util.Observable;

public class Loc extends Observable {
    public List<Loc> dec;
    public  double latitude=0;
    public  double longitude=0;
    public double destLatitude=0;
    public double destLongitude=0;

    public double bearing;

    public int id;

    public double getBearing() {
        return bearing;
    }

    public void setBearing(double bearing) {
        this.bearing = bearing;
    }

    public void setSomeVariable(List<Loc> decodedpath) {
        synchronized (this) {
            this.dec = decodedpath;
        }
        setChanged();
        notifyObservers();
    }
    public synchronized List<Loc> getSomeVariable() {
        return dec;
    }

    public void setPosition(double lat,double lon,double bearing,double destLatitude,double destLongitude) {
        synchronized (this) {
        this.latitude=lat;
        this.longitude=lon;
        this.bearing=bearing;
        this.destLatitude=destLatitude;
        this.destLongitude=destLongitude;
        }
        setChanged();
        notifyObservers();
    }


    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setPosition2(double latitude, double longitude, double bearing) {
        this.latitude=latitude;
        this.longitude=longitude;
        this.bearing=bearing;
        setChanged();
        notifyObservers();
    }
}
