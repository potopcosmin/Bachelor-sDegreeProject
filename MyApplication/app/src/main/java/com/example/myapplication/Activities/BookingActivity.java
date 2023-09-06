package com.example.myapplication.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.APICalls;
import com.example.myapplication.DataModel.CarService;
import com.example.myapplication.DataModel.Service;

import com.example.myapplication.RecycleViewAdapter;
import com.example.myapplication.Utils.ServiceSearchFilter;
import com.example.myapplication.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class BookingActivity extends AppCompatActivity implements LocationListener {

    private RecycleViewAdapter adapter;
    private ArrayList<CarService> carServicestoShow;
    LocationManager locationManager;
    RecyclerView recyclerView;

    Location lastUsedLocation = new Location("");

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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


        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);
        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10, this);

        CountDownLatch latch = new CountDownLatch(1);
        carServicestoShow= APICalls.getCarRepairInRange(this,locationManager,latch,10);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        //getNewServicesByRange(10);
       /* OkHttpClient okHttpClient = new OkHttpClient();

        JSONObject body = new JSONObject();
        Location last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        try {
            body.put("latitude", last.getLatitude());
            body.put("longitude", last.getLongitude());
            body.put("range", 10);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        RequestBody requestBody = RequestBody.create(body.toString(), MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("https://1899-82-77-19-156.ngrok-free.app/carservices/range")
                .post(requestBody).build();



        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERRRRORRRRR", e.toString());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new Gson();
                Type listType = new TypeToken<ArrayList<CarService>>() {
                }.getType();
                String respons=response.body().string();
                System.out.println(respons);
                carServicestoShow = gson.fromJson(respons, listType);
                System.out.println(carServicestoShow.get(0).toString());
                System.out.println(carServicestoShow.size());
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
        adapter = new RecycleViewAdapter(getApplicationContext(), carServicestoShow);
         recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // below line is to get our inflater
        MenuInflater inflater = getMenuInflater();

        // inside inflater we are inflating our menu file.
        inflater.inflate(R.menu.search_menu, menu);

        // below line is to get our menu item.
        MenuItem searchItem = menu.findItem(R.id.actionSearch);

        // getting search view of our item.
        SearchView searchView = (SearchView) searchItem.getActionView();

        // below line is to call set on query text listener method.
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText == null) return false;
                filter(newText);
                return false;
            }
        });
        return true;
    }

    private void filter(String text) {

        ArrayList<CarService> filterdList = ServiceSearchFilter.filterListofServices(carServicestoShow, text);
        if (filterdList.isEmpty()) {

            Toast.makeText(this, "No Data Found..", Toast.LENGTH_SHORT).show();
        } else {
            adapter.filterList(filterdList);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }


    public void getNewServicesByRange(int range) {


        JSONObject body = new JSONObject();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        try {
            body.put("latitude",last.getLatitude());
            body.put("longitude",last.getLongitude());
            body.put("range",range);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(body.toString(), MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("http://10.0.2.2:8000/carservices/range")
                .post(requestBody).build();


        CountDownLatch latch = new CountDownLatch(1);
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("ERRRRORRRRR", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response ) throws IOException {
                Gson gson=new Gson();
                Type listType = new TypeToken<ArrayList<CarService>>(){}.getType();
                String respons=response.body().string();
                System.out.println(response);
                System.out.println(respons);
                carServicestoShow=gson.fromJson(respons.trim(),listType);
                System.out.println(carServicestoShow.get(0).toString());
                System.out.println(carServicestoShow.size());
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        adapter = new RecycleViewAdapter(getApplicationContext(), carServicestoShow);
        recyclerView.setAdapter(adapter);

    }

}
