package com.example.myapplication.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.example.myapplication.DataModel.Loc;
import com.example.myapplication.R;
import com.example.myapplication.Utils.ObservableMap;
import com.example.myapplication.WebSocketClientEndpointCustomer;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;


public class UserMapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        Observer, GoogleMap.OnMyLocationClickListener, LocationListener {

    WebSocketClientEndpointCustomer client;
    Handler handler1;
    String serverUri = "ws://lucrarelicenta.pagekite.me:80/trip";
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    private static final long MIN_TIME_BW_UPDATES = 1;
    protected LocationManager locationManager;
    private SupportMapFragment mapFragment;
    private Polyline polyline;
    private Context mContext;
    private static GoogleMap mapUser;
    Marker driverMarker;
    public static boolean searchDrivers = false;
    HashMap<String, Marker> markerList = new HashMap<>();

    private List<LatLng> decodedPath;
    Loc c1 = new Loc();
    private Location currentLocation;


    public static ObservableMap<String, HashMap<String, Double>> nearbyDrivers = new ObservableMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        handler1 = new Handler();
        nearbyDrivers.addListener(event -> {
            System.out.println("driverMap changed");
            switch (event.getChangeType()) {
                case PUT:
                    System.out.println(event.getKey());
                    System.out.println(event.getNewValue());
                    if(markerList.get(event.getKey())==null)
                    {
                        System.out.println("Nu exista marker-rul");
                        MarkerOptions markerOptions = new MarkerOptions();
                        LatLng markerPosition=new LatLng(event.getNewValue().get("latitude"),event.getNewValue().get("longitude"));
                        markerOptions.position(markerPosition);
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.redcar));
                        Marker newMarker= mapUser.addMarker(markerOptions);
                        markerList.put(event.getKey(),newMarker);
                    return;
                    }
                    else{
                        System.out.println("Exista markerul");
                        LatLng markerPosition=new LatLng(event.getNewValue().get("latitude"),event.getNewValue().get("longitude"));
                        markerList.get(event.getKey()).setPosition(markerPosition);
                        return;
                    }

                case REMOVE:
                    System.out.println("removed");
                    markerList.get(event.getKey()).remove();
                    markerList.remove(event.getKey());
                    break;
            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        c1.addObserver(this);
        com.example.myapplication.Polyline.setTip(1);
        new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(500)
                .setMaxUpdateDelayMillis(1000)
                .build();
        mContext = getApplicationContext();
        locationManager = (LocationManager) mContext
                .getSystemService(LOCATION_SERVICE);
        int permissionCheck = ContextCompat.checkSelfPermission(UserMapsActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(UserMapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    2);
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
        currentLocation = HomeActivity.homeLocation;
        System.out.println(currentLocation.getLatitude());
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION, false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                            } else {
                                // No location access granted.
                            }
                        }
                );


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_maps);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapUser);
        mapFragment.getMapAsync(this);
        Thread connectToSocket = new Thread(() -> {
            try {
                Map<String, String> header = new HashMap<>();
                header.put("UserType", "normal");
                client = new WebSocketClientEndpointCustomer(new URI(serverUri), header, c1, handler1, false);
                client.connect();

            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
        connectToSocket.start();
    }





    @Override
    public void onLocationChanged(@NonNull Location location) {
    currentLocation=location;
    }

    public void sendRequest(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        client.send("request " + currentLocation.getLatitude() +" "+currentLocation.getLongitude()+" " +"47.153270653882515"+" 27.611321084708752");

    }
    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        searchDrivers=true;

        mapUser = googleMap;
        mapUser.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle));

        mapUser.setMyLocationEnabled(true);
        mapUser.setOnMyLocationButtonClickListener(this);
        mapUser.setOnMyLocationClickListener(this);

        Location loc=locationManager.getLastKnownLocation("");
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(HomeActivity.homeLocation.getLatitude(),HomeActivity.homeLocation.getLongitude()))
                .zoom(15)
                .build();
        mapUser.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));



    }

    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker,double bear) {
        final long start = SystemClock.uptimeMillis();
        Projection proj = mapUser.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 1500;

        final Interpolator interpolator = new LinearInterpolator();

        handler1.post(new Runnable() {
            @Override
            public void run() {
                if(decodedPath==null) return;
                if (toPosition == null) {
                    handler1.postDelayed(this, 16);
                }
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                LatLng pos = new LatLng(lat, lng);
                marker.setPosition(pos);
                marker.setRotation((float) bear);
                if(decodedPath.size()<2)
                {
                    decodedPath=null;
                    return;
                }
                decodedPath.set(0, pos);


                double dis1 = calculateDistance(toPosition, decodedPath.get(1));
                System.out.println(dis1);
                if (dis1 < 5) {
                    decodedPath.remove(1);
                }
                polyline.setPoints(decodedPath);

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler1.postDelayed(this, 16);
                } else {
                    marker.setVisible(!hideMarker);
                }
            }
        });
    }


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



    @Override
    public void update(Observable o, Object arg) {
        System.out.println("Listener work");
        if (decodedPath==null) {
            System.out.println("DIMENSIUNEA MARKER LIST (1 e ok)");
            System.out.println(markerList.size());
            System.out.println("Observer working");
            List<Loc> loc=c1.getSomeVariable();
            decodedPath=new ArrayList<>();
            for (Loc a: loc
                 ) {
                decodedPath.add(new LatLng(a.latitude,a.longitude));
            }
            System.out.println(decodedPath);
            polyline = mapUser.addPolyline(new PolylineOptions().addAll(decodedPath).width(30).color(0x7F00FF00));
            driverMarker = markerList.get(markerList.keySet().toArray()[0]);
            return;
        }

        System.out.println("position changed");
        if(decodedPath.size()==0) return;
        animateMarker(driverMarker, new LatLng(c1.getLatitude(), c1.getLongitude()), false,c1.getBearing());

        }


}


