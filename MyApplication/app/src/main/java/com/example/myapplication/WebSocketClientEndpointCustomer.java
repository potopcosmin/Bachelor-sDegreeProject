package com.example.myapplication;

import android.os.Handler;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import com.example.myapplication.Activities.DriverMapsActivity;
import com.example.myapplication.Activities.HomeActivity;
import com.example.myapplication.Activities.UserMapsActivity;
import com.example.myapplication.DataModel.Loc;
import com.example.myapplication.DataModel.User;
import com.example.myapplication.Utils.ObservableMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.UnknownServiceException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.OnMessage;

@ClientEndpoint
public class WebSocketClientEndpointCustomer extends WebSocketClient {


    private Loc loc;
    private boolean drive;
    private Handler mainHandler;
    private ServerHandshake handshakedata;

    private List<User> driverList;
    private  boolean gettinNearbyDrivers=false;

    private ObservableMap<String,HashMap<String,Double>> nearbyDrivers=new ObservableMap<>();


    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("closing websocket");
        try {
            this.connect();
        } catch (Exception e) {
            Log.e("FAILED", e.getMessage());
        }
    }

    @Override
    public void onError(Exception ex) {
        System.out.println(ex);
    }

    @OnMessage
    public void onMessage(ByteBuffer bytes) {
        System.out.println("Handle byte buffer");
    }

    public WebSocketClientEndpointCustomer(URI uri, Map<String, String> header, Loc c2, Handler handler1, boolean b) {
        super(uri, header);
        this.loc = c2;
        this.drive = b;
        this.mainHandler = handler1;
    }


    @Override
    public void onOpen(ServerHandshake handshakedata) {

        this.handshakedata = handshakedata;
        System.out.println("##########################");
        System.out.println("opening websocket");
        System.out.println("##########################");
        send("getNearbyDrivers " + String.valueOf(HomeActivity.homeLocation.getLatitude())+" "+String.valueOf(HomeActivity.homeLocation.getLongitude()));
    }

    boolean pathalreadyReceived;

    @Override
    public void onMessage(String message) {
        System.out.println("MESAJUL PRIMIT");
        System.out.println(message);

        if(message.contains("accept")){
            System.out.println("Trip Starting");
            String[] splitmsg=message.split(" ");
            gettinNearbyDrivers=false;
            UserMapsActivity.searchDrivers=false;
            for (String email:UserMapsActivity.nearbyDrivers.keySet()
                 ) {
                if(!email.equals(splitmsg[2])){
                    UserMapsActivity.nearbyDrivers.remove(email);
                }
            }
            return;
        }
        if (message.equals("nearbyDrivers")) {
            System.out.println("Getting nearby");
            gettinNearbyDrivers = true;
            UserMapsActivity.searchDrivers = true;
            return;
        }

        if (gettinNearbyDrivers == true) {
            String[] splitMessage = message.split(" ");

            HashMap<String, Double> loc = new HashMap<>();
            loc.put("latitude", Double.parseDouble(splitMessage[1]));
            loc.put("longitude", Double.parseDouble(splitMessage[2]));
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    UserMapsActivity.nearbyDrivers.put(splitMessage[0], loc);
                    System.out.println("nearbyDrivers updated");
                }
                });
            return;
            }
        Gson gson = new Gson();
        if(message.contains("ConfirmPickup")) pathalreadyReceived=false;
        if(gettinNearbyDrivers==false){
            if(message.contains("polyline")) {
                System.out.println("Show Polyline Driver to User");
                String[] split = message.split(" ");

                JsonReader reader = new JsonReader(new StringReader(split[1]));
                JsonToken firstToken;
                try {
                    firstToken = reader.peek();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (firstToken == JsonToken.BEGIN_ARRAY && pathalreadyReceived == false) {
                    pathalreadyReceived = true;
                    Type founderListType = new TypeToken<ArrayList<Loc>>() {
                    }.getType();

                    List<Loc> path = new ArrayList<>();
                    for (Loc a : (ArrayList<Loc>) gson.fromJson(split[1], founderListType)
                    ) {
                        path.add(a);
                    }
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            loc.setSomeVariable(path);
                        }

                    });
                }
                return;
            }
             else if(UserMapsActivity.searchDrivers==false)
             {


                 System.out.println("Locatie primita");
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Loc b=gson.fromJson(message,Loc.class);
                        loc.setPosition2(b.latitude,b.longitude,b.bearing);
                    }
                });
             }

        }



    }

        /*if (message.contains("accept")) return;
        Gson gson=new Gson();
        JsonReader reader=new JsonReader(new StringReader(message));
        JsonToken firstToken ;
        try {
            firstToken =reader.peek();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if(firstToken==JsonToken.BEGIN_ARRAY && pathalreadyReceived==false)
        {
            pathalreadyReceived=true;
            Type founderListType = new TypeToken<ArrayList<Loc>>(){}.getType();

            List<Loc> path=new ArrayList<>();
            for (Loc a:(ArrayList<Loc>)gson.fromJson(message, founderListType)
            ) {
                path.add(a);
            }
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    loc.setSomeVariable(path);
                }
                
            });
            return;
        }

        System.out.println("Locatie primita");
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Loc b=gson.fromJson(message,Loc.class);
                loc.setPosition(b.latitude,b.longitude,b.bearing);
            }
        });
    }*/


}
