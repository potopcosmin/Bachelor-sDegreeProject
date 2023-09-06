package com.example.myapplication;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.myapplication.DataModel.CarService;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class APICalls {
    private static ArrayList<CarService> carServicestoShow =new ArrayList<>();
    private static final String baseUrl = "http://192.168.1.2:8000" ;


    public static ArrayList<CarService>getCarRepairInRange(Context context, LocationManager locationManager, CountDownLatch latch,int range) {
        OkHttpClient okHttpClient = new OkHttpClient();
        JSONObject body = new JSONObject();
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        Location last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        try {
            body.put("latitude"  , last.getLatitude());
            body.put("longitude"  , last.getLongitude());
            body.put("range"      , range);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        RequestBody requestBody = RequestBody.create(body.toString(), MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(baseUrl+"/carservices/range")
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
                System.out.println(response);
                System.out.println(respons);
                carServicestoShow = gson.fromJson(respons, listType);
                System.out.println(carServicestoShow.get(0).toString());
                System.out.println(carServicestoShow.size());
                latch.countDown();
            }
        });
        return carServicestoShow;
    }

}
