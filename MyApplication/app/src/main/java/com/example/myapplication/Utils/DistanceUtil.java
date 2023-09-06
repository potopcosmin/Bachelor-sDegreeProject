package com.example.myapplication.Utils;

import com.google.android.gms.maps.model.LatLng;

public class DistanceUtil {

    public static double calculateDistance(LatLng l1, LatLng l2) {
        double lat1 = l1.latitude;
        double lon1 = l1.longitude;
        double lat2 = l2.latitude;
        double lon2 = l2.longitude;
        double R = 6371000;

        // Convert latitude and longitude from degrees to radians
        double lat1_rad = Math.toRadians(lat1);
        double lon1_rad = Math.toRadians(lon1);
        double lat2_rad = Math.toRadians(lat2);
        double lon2_rad = Math.toRadians(lon2);

        // Calculate the differences between the coordinates
        double dlat = lat2_rad - lat1_rad;
        double dlon = lon2_rad - lon1_rad;

        // Haversine formula
        double a = Math.sin(dlat / 2) * Math.sin(dlat / 2)
                + Math.cos(lat1_rad) * Math.cos(lat2_rad) * Math.sin(dlon / 2) * Math.sin(dlon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Calculate the distance
        double distance = R * c;

        return distance;
    }

}
