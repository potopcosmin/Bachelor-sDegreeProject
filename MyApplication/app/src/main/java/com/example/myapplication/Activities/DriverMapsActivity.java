package com.example.myapplication.Activities;

import static java.lang.Math.round;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.DataModel.Loc;
import com.example.myapplication.R;
import com.example.myapplication.TURN.DIRECTION;
import com.example.myapplication.Utils.DistanceUtil;
import com.example.myapplication.WebsocketClientEndpoint;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DriverMapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, LocationListener, Observer {
    public WebsocketClientEndpoint client;
    private  Location clientDest = new Location("");
    protected LocationManager locationManager;
    String serverUri = "ws://lucrarelicenta.pagekite.me:80/trip";
    ExecutorService executorService = Executors.newFixedThreadPool(1);
    private SupportMapFragment mapFragment;
    private Polyline polyline;
    private List<LatLng> ClientDestPoly;
    private Location lastLoc;
    private List<LatLng> decodedPath;
    private Marker curentPosMarker;
    private GoogleMap map;

    private Location previousPosition = new Location("");
    private Location tripDest=new Location(" ");
    private Map<LatLng, DIRECTION> turningPoints=new HashMap<>();
    private Loc c2=new Loc();
    Handler handler1;
    private final Object lock = new Object();
    public static void trimCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                boolean success = deleteDir(new File(dir, child));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    public static float calculateBearing(LatLng l1, LatLng l2) {
        double lat = Math.abs(l1.latitude - l2.latitude);
        double lng = Math.abs(l1.longitude - l2.longitude);
        double v=Math.toDegrees(Math.atan(lng / lat));
        if (l1.latitude < l2.latitude && l1.longitude < l2.longitude)
            return (float) (v);
        else if (l1.latitude >= l2.latitude && l1.longitude < l2.longitude)
            return (float) ((90 - v) + 90);
        else if (l1.latitude >= l2.latitude && l1.longitude >= l2.longitude)
            return (float) (v + 180);
        else if (l1.latitude < l2.latitude && l1.longitude >= l2.longitude)
            return (float) ((90 - v) + 270);
        return -1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        c2.addObserver(this);
        com.example.myapplication.Polyline.setTip(1);
        new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(700)
                .setMaxUpdateDelayMillis(200)
                .build();
        Context mContext = getApplicationContext();
        locationManager = (LocationManager) mContext
                .getSystemService(LOCATION_SERVICE);
        int permissionCheck = ContextCompat.checkSelfPermission(DriverMapsActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(DriverMapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    2);
        }
        long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
        long MIN_TIME_BW_UPDATES = 500;
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                MIN_TIME_BW_UPDATES,
                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

        lastLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
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
        setContentView(R.layout.activity_maps);

        // Get a handle to the fragment and register the callback.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    // Get a handle to the GoogleMap object and display marker.
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        handler1 = new Handler();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        map = googleMap;
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle));

        //map.setBuildingsEnabled(false);
        map.setMyLocationEnabled(false);
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);


        clientDest.setLatitude( 47.162121071821375);
        clientDest.setLongitude(27.614501345876945);

if(lastLoc==null){
    return;
}
        LatLng lastPos = new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude());
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(lastPos)
                .zoom(18.5F)
                .bearing(lastLoc.getBearing())
                .tilt(60)
                .build();


        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                map.animateCamera(CameraUpdateFactory.scrollBy(0, -500));
            }

            @Override
            public void onCancel() {

            }
        });

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(lastPos);
        markerOptions.anchor(0.5f, 0.5f);
        markerOptions.rotation(map.getCameraPosition().bearing);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.redcar));
        curentPosMarker = map.addMarker(markerOptions);
        Thread connectToSocket = new Thread(() -> {
            Map<String, String> header = new HashMap<>();
            header.put("UserType", "driver");
            header.put("email", FirebaseAuth.getInstance().getCurrentUser().getEmail());
            try {
                client = new WebsocketClientEndpoint(new URI(serverUri), header,c2,handler1,true);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            client.connect();
        });
        connectToSocket.start();
        try {
            connectToSocket.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }
    public static boolean tripOngoing=false;
    public void getRoute(Location start,Location end) throws IOException, JSONException, InterruptedException {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        lastLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        JSONObject body = createJson(start, end);
        MediaType JSON
                = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(body.toString(), JSON);

        Request request = new Request.Builder()
                .url("https://routes.googleapis.com/directions/v2:computeRoutes")
                .post(requestBody)
                .addHeader("X-Goog-FieldMask", "routes.duration,routes.distanceMeters,routes.polyline.encodedPolyline")
                .addHeader("X-Goog-Api-Key", "AIzaSyDurGfa4o2MAGmzxvJu-DtCcRTP-5w0D6c").build();

        CountDownLatch latch = new CountDownLatch(1);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERRRRORRRRR", e.toString());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                try {
                    JSONObject obj = new JSONObject(response.body().string());
                    JSONArray arr = obj.getJSONArray("routes");

                    String encodedPoly = arr.getJSONObject(0).getJSONObject("polyline").getString("encodedPolyline");

                    synchronized (lock){
                    decodedPath = PolyUtil.decode(encodedPoly);}
                    latch.countDown();


                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }


        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        AsyncTask.execute(this::computeTurninPoints);
    }

    public JSONObject createJson(Location originLoc, Location destinationLoc) throws JSONException {
        JSONObject request = new JSONObject();
        JSONObject locObj = new JSONObject();
        JSONObject latLngObj = new JSONObject();
        JSONObject coordObject = new JSONObject();


        coordObject.put("latitude", originLoc.getLatitude());
        coordObject.put("longitude", originLoc.getLongitude());

        latLngObj.put("latLng", coordObject);
        locObj.put("location", latLngObj);

        JSONObject locObj1 = new JSONObject();
        JSONObject latLngObj1 = new JSONObject();
        JSONObject coordObject1 = new JSONObject();

        coordObject1.put("latitude", destinationLoc.getLatitude());
        coordObject1.put("longitude", destinationLoc.getLongitude());

        latLngObj1.put("latLng", coordObject1);
        locObj1.put("location", latLngObj1);

        request.put("origin", locObj);
        request.put("destination", locObj1);


        request.put("travelMode", "DRIVE");
        request.put("polylineQuality", "HIGH_QUALITY");

        return request;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    //Fires after the OnStop() state
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            trimCache(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onLocationChanged(@NonNull Location location) {

        if(client!=null && !tripOngoing) {
            Thread thread = new Thread(() -> client.send( FirebaseAuth.getInstance().getCurrentUser().getEmail()+" " + location.getLatitude() + " " + location.getLongitude()+ " newLocation" ));
            thread.start();
        }

        lastLoc.set(location);
        if (map == null) return;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if (polyline == null) {

            GeodesicData geodesicData = Geodesic.WGS84.Direct(
                    latLng.latitude, latLng.longitude, map.getCameraPosition().bearing, 40);
            double targetLatitude = geodesicData.lat2;
            double targetLongitude = geodesicData.lon2;

            LatLng targetLatLng = new LatLng(targetLatitude, targetLongitude);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(targetLatLng)
                    .zoom(18.5F)
                    .bearing(map.getCameraPosition().bearing)
                    .tilt(60)
                    .build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            curentPosMarker.setPosition(latLng);
        } else {

            Gson gson=new Gson();


            if (previousPosition == null) {
                previousPosition = location;
                return;
            }

            TextView valueTV = findViewById(R.id.indications);

            double bearvalue;
            if(decodedPath.size()<11 && decodedPath.size()>8){
                View confirmview=findViewById(R.id.ConfirmPick);
                confirmview.setVisibility(View.VISIBLE);
            }
            if (decodedPath.size() > 1) {
                bearvalue = SphericalUtil.computeHeading(new LatLng(previousPosition.getLatitude(),previousPosition.getLongitude()), decodedPath.get(1));

                if (client != null) {
                    double finalBearvalue = bearvalue;
                    executorService.execute(() -> {

                        Loc loc=new Loc();
                        loc.latitude=location.getLatitude();
                        loc.longitude=location.getLongitude();
                        loc.bearing= finalBearvalue;
                        client.send( gson.toJson(loc));
                    });
                }

                bearvalue = SphericalUtil.computeHeading(curentPosMarker.getPosition(), decodedPath.get(1));
                if(turningPoints.size()>0) {
                    LatLng upcomingTurn = turningPoints.entrySet().iterator().next().getKey();
                    double distance = DistanceUtil.calculateDistance(latLng, upcomingTurn);
                    if (distance < 205) {
                        if (turningPoints.get(upcomingTurn) == DIRECTION.LEFT) {
                            ImageView turnImage=findViewById(R.id.imageTurn);
                            turnImage.setImageResource(R.drawable.left);
                                valueTV.setText("Turn LEFT\n IN " + round(distance)+" meters");

                        } else if (turningPoints.get(upcomingTurn) == DIRECTION.RIGTH){
                            valueTV.setText("Turn RIGTH\n IN" + round(distance));
                            ImageView turnImage=findViewById(R.id.imageTurn);
                        }


                    }
                }
                LatLng offset=SphericalUtil.computeOffset(latLng,80,bearvalue);
                animateMarker(curentPosMarker, latLng, false,bearvalue,valueTV);
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(offset)
                        .zoom(18.5F)
                        .bearing((float)bearvalue)
                        .tilt(60)
                        .build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
                    @Override
                    public void onFinish() {
                        map.animateCamera(CameraUpdateFactory.scrollBy(0, -500));
                    }

                    @Override
                    public void onCancel() {

                    }
                });

            } else {


                bearvalue=SphericalUtil.computeHeading(curentPosMarker.getPosition(),latLng);
                animateMarker(curentPosMarker,latLng,false,bearvalue,valueTV);
                polyline.remove();

            }
        }

    }

    public void animateMarker(final Marker marker, final LatLng toPosition,
                              final boolean hideMarker, double bearvalue, TextView indic) {

        final long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 1500;

        final Interpolator interpolator = new LinearInterpolator();
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (toPosition == null) {
                    handler.postDelayed(this, 16);
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
                decodedPath.set(0, pos);
                if (decodedPath.size() > 1){
                    double dis1 = DistanceUtil.calculateDistance(toPosition, decodedPath.get(1));
                if (dis1 < 8) {
                    decodedPath.remove(1);
                    if(turningPoints.size()>1){
                    LatLng turn = turningPoints.entrySet().iterator().next().getKey();
                    if(DistanceUtil.calculateDistance(toPosition,turn)<10){
                        turningPoints.remove(turn);
                        if(!indic.getText().equals("STRAIGTH")){
                            indic.setText("STRAIGTH");

                            ImageView view = (ImageView) findViewById(R.id.imageTurn);
                                    view.setImageResource(R.drawable.straigthpng);
                        }
                    }
                }
                }
                polyline.setPoints(decodedPath);
            }
                else polyline.remove();
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    marker.setVisible(!hideMarker);
                }
            }
        });
    }

    public void computeTurninPoints(){
        if(decodedPath.size()<3){
            return;
        }
        for (int i=1;i<decodedPath.size()-1;i++){
            LatLng prev=decodedPath.get(i-1);
            LatLng current=decodedPath.get(i);
            LatLng next=decodedPath.get(i+1);

            float prevBearing=calculateBearing(prev,current);
            float nextBearing=calculateBearing(current,next);

            float bearingDifference=nextBearing-prevBearing;

            if(bearingDifference>30 && bearingDifference<=180){
                turningPoints.put(current,DIRECTION.RIGTH);

            }
            else if(bearingDifference<-30 && bearingDifference>=-180){
                turningPoints.put(current,DIRECTION.LEFT);
            }

        }
    }
    private Marker destMarker;

    @Override
    public void update(Observable o, Object arg) {

        if(c2.latitude!=0){
            System.out.println("Primit locatie req");
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(c2.destLatitude,c2.destLongitude));
            markerOptions.anchor(0.5f, 0.5f);
            markerOptions.rotation(map.getCameraPosition().bearing);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.redcar));
            destMarker=map.addMarker(markerOptions);
            System.out.println("Marker pus");
            View view=findViewById(R.id.tripReqButtons);
            view.setVisibility(View.VISIBLE);
            clientDest.setLatitude(c2.getLatitude());
            clientDest.setLongitude(c2.getLongitude());
            tripDest.setLongitude(c2.destLongitude);
            tripDest.setLatitude(c2.destLatitude);
            try {

                getRoute(clientDest,tripDest);
                polyline = map.addPolyline(new PolylineOptions().addAll(decodedPath).width(50).color(0x7F00FF00));
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(clientDest.getLatitude(),clientDest.getLongitude()))
                        .zoom(15)
                        .build();
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                ClientDestPoly=decodedPath;
            } catch (IOException | JSONException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            return;
        }
    }

    public void acceptRoute(View view) throws JSONException, IOException, InterruptedException {
        tripOngoing=true;
        polyline.remove();
        clientDest.setLatitude(c2.latitude);
        clientDest.setLongitude(c2.longitude);
        getRoute(lastLoc,clientDest);
        polyline = map.addPolyline(new PolylineOptions().addAll(decodedPath).width(50).color(0x7F00FF00));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(lastLoc.getLatitude(),lastLoc.getLongitude()))
                .zoom(18.5F)
                .tilt(60)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                map.animateCamera(CameraUpdateFactory.scrollBy(0, -500));
            }

            @Override
            public void onCancel() {

            }
        });

        
        Thread send =new Thread(() -> {
            client.send("accept "+c2.id+" "+FirebaseAuth.getInstance().getCurrentUser().getEmail());
            Gson gson=new Gson();
            client.send("polyline "+gson.toJson(decodedPath));
        });
        send.start();
        View viewButtons=findViewById(R.id.tripReqButtons);
        viewButtons.setVisibility(View.GONE);
    }

    public void ConfirmPickUp(View view){

        decodedPath=ClientDestPoly;
        polyline = map.addPolyline(new PolylineOptions().addAll(decodedPath).width(50).color(0x7F00FF00));
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(clientDest.getLatitude(),clientDest.getLongitude()))
                .zoom(15)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        findViewById(R.id.ConfirmPick).setVisibility(View.GONE);
        Thread thread = new Thread(() -> {
            Gson gson = new Gson();
            synchronized (lock){
            client.send("polylineConfirmPickup "+gson.toJson(decodedPath));}
        });
        thread.start();
    }
}


