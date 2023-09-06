package com.example.myapplication;
import android.content.Context;
import android.content.SharedPreferences;

import android.os.Handler;

import android.util.Log;

import com.example.myapplication.Activities.HomeActivity;

import com.example.myapplication.DataModel.Loc;

import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import com.google.android.gms.maps.model.Polyline;

import com.google.firebase.auth.FirebaseAuth;


import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;


import java.net.URI;
import java.nio.ByteBuffer;

import java.util.List;
import java.util.Map;


import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.OnMessage;


@ClientEndpoint
    public class WebsocketClientEndpoint extends WebSocketClient {
    public boolean pathalreadyReceived=false;
    private  Loc loc;
    private boolean drive=false;
    private  Handler mainHandler;
    GoogleMap map;
    private Context context;
    Polyline polyline;
    SharedPreferences sharedPreferences;
    LatLng previousLocation;
    Marker driverMarker;
    List<LatLng> decodedPath;
    ServerHandshake handshakedata;

    public WebsocketClientEndpoint(URI serverUri, Draft draft) {
        super(serverUri, draft);
    }

    public WebsocketClientEndpoint(URI serverURI) {
        super(serverURI);
    }
    public WebsocketClientEndpoint(URI serverUri, Map<String, String> httpHeaders){
        super(serverUri,httpHeaders);
    }
    public WebsocketClientEndpoint(URI serverUri, Map<String, String> httpHeaders,Context context) {
        super(serverUri, httpHeaders);
        this.context=context;

    }
    public WebsocketClientEndpoint(URI serverUri, Map<String, String> httpHeaders,Loc loc) {
        super(serverUri, httpHeaders);
        this.map=map;
        this.loc=loc;
    }

    public WebsocketClientEndpoint(URI uri, Map<String, String> header, Loc c1, Handler handler1) {
        super(uri, header);

        this.loc=c1;
        this.mainHandler =handler1;
    }

    public WebsocketClientEndpoint(URI uri, Map<String, String> header, Loc c2, Handler handler1,boolean b) {
        super(uri,header);
        this.loc=c2;
        this.drive=b;
        this.mainHandler =handler1;
    }


    @Override
    public void onOpen(ServerHandshake handshakedata) {

        this.handshakedata=handshakedata;
        System.out.println("opening websocket");
        send( FirebaseAuth.getInstance().getCurrentUser().getEmail()+" " + String.valueOf(HomeActivity.homeLocation.getLatitude())+ " "+String.valueOf(HomeActivity.homeLocation.getLongitude())+" newLocation" );
    }

    @Override
    public void onMessage(String message) {
        System.out.println(message);
            if (message.contains("request")) {
                System.out.println("request received");
                String[] msgcomp = message.split(" ");
                System.out.println("Message splited");
                System.out.println(msgcomp[1]);
                System.out.println(msgcomp[2]);
                System.out.println(msgcomp[3]);
                System.out.println(msgcomp[4]);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        loc.setPosition(Double.parseDouble(msgcomp[1]), Double.parseDouble(msgcomp[2]), 0, Double.parseDouble(msgcomp[3]), Double.parseDouble(msgcomp[4]));
                        loc.id= Integer.parseInt(msgcomp[5]);
                    }
                });
            }

    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("closing websocket");
        try {
            this.connect();
        }
        catch (Exception e ) {
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


}


