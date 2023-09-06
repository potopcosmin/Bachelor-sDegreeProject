package com.example.myapplication.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.myapplication.DataModel.Loc;
import com.example.myapplication.Session.SessionManagement;
import com.example.myapplication.R;
import com.google.firebase.FirebaseApiNotAvailableException;
import com.google.firebase.auth.FirebaseAuth;


public class HomeActivity extends AppCompatActivity implements LocationListener {

    public static Location homeLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int MY_PERMISSIONS_REQUEST_LOCATION = 99;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ActivityCompat.requestPermissions(this,
                new String[]{"android.permission.ACCESS_FINE_LOCATION"},
                MY_PERMISSIONS_REQUEST_LOCATION);

        Context mContext = getApplicationContext();
        LocationManager locationManager = (LocationManager) mContext
                .getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                (long) 1.0,
                1.0F, this);
    }

    public void logout(View view){
        FirebaseAuth.getInstance().signOut();
        Intent login=new Intent(HomeActivity.this,LoginActivity.class);
        login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(login);
        HomeActivity.this.finish();
    }

    public void movetoBooking(View view){
        Intent booking=new Intent(HomeActivity.this,BookingActivity.class);
        startActivity(booking);
    }
    public void movetoDriverMap(View view){
        Intent map=new Intent(HomeActivity.this,DriverMapsActivity.class);
        startActivity(map);
    }

    public void moveToClientMap(View view){
        Intent map=new Intent(HomeActivity.this,UserMapsActivity.class);
        startActivity(map);
    }
    @Override
    public void onBackPressed(){
        Intent returnIntent=new Intent(getApplicationContext(),LoginActivity.class);
        SessionManagement sharePreferences=new SessionManagement(HomeActivity.this);
        sharePreferences.removeSession();

        HomeActivity.this.finish();
        startActivity(returnIntent);
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        System.out.println("Location changed on Home");
        homeLocation= location;
    }
}