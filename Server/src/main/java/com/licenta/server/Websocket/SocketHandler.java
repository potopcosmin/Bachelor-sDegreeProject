package com.licenta.server.Websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.licenta.server.CarRepair.DistanceUtil;
import com.licenta.server.TripRequest;
import com.licenta.server.UserService.User;
import com.licenta.server.UserService.UserRepository;
import jakarta.xml.bind.SchemaOutputResolver;
import org.hibernate.Session;
import org.hibernate.usertype.UserType;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;

@Component
public class SocketHandler extends TextWebSocketHandler {
    private  Map<WebSocketSession , Integer  > sessionTripIdMap = new HashMap<>();

    private Map<WebSocketSession,HashMap<String,Double>> sessionLocationMap=new HashMap<>();
    private Map<WebSocketSession,ArrayList<WebSocketSession>> sessionNearbyDrivers=new HashMap<>();
    private final ObjectMapper objectMapper;

    public SocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }



    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String tripId = null;
        String userType = session.getHandshakeHeaders().getFirst("UserType");
        String receivedMessage = message.getPayload();
        System.out.println(receivedMessage);
        if (userType.equals("normal")) {
            if(receivedMessage.contains("newLocation")){
                String[] splitmessage = receivedMessage.split(" ");
                HashMap<String,Double> loc=new HashMap<>();
                loc.put("latitude",Double.parseDouble(splitmessage[1]));
                loc.put("longitude",Double.parseDouble(splitmessage[2]));
                sessionLocationMap.put(session,loc);
                return;
            }
            if(receivedMessage.contains("getNearbyDrivers")){
                //send command to client to read all nearby drivers
                sendMessageToSession(session ,"nearbyDrivers");
                String[] splitmessage = receivedMessage.split(" ");
                HashMap<String,Double> loc=new HashMap<>();
                loc.put("latitude",Double.parseDouble(splitmessage[1]));
                loc.put("longitude",Double.parseDouble(splitmessage[2]));
                sessionLocationMap.put(session,loc);
                for (WebSocketSession driverSession: sessionLocationMap.keySet()
                     ) {
                    if(driverSession.getHandshakeHeaders().getFirst("UserType").equals("normal")) continue;
                    Double driverLatitude = sessionLocationMap.get(driverSession).get("latitude");
                    Double driverLongitude = sessionLocationMap.get(driverSession).get("longitude");
                    if(DistanceUtil.calculateDistance(loc.get("latitude"),loc.get("longitude"),driverLatitude,driverLongitude)/1000<10){
                        sendMessageToSession(session,driverSession.getHandshakeHeaders().getFirst("email") +" "+
                                driverLatitude+" "+driverLongitude);
                        sessionNearbyDrivers.get(session).add(driverSession);
                    }
                 }
                System.out.println("gataNEarby");
                return;
            }
            if(receivedMessage.contains("request")) {
                String[] splitmessage = receivedMessage.split(" ");
                HashMap<String,Double> destloc=new HashMap<>();
                destloc.put("latitude",Double.parseDouble(splitmessage[1]));
                destloc.put("longitude",Double.parseDouble(splitmessage[2]));
                TripRequest tripRequest = new TripRequest();
                sessionTripIdMap.put(session, tripRequest.getTripId());
                System.out.println(receivedMessage +" "+tripRequest.getTripId());
                for (WebSocketSession ses: sessionNearbyDrivers.get(session)
                     ) {
                    sendMessageToSession(ses,receivedMessage +" "+tripRequest.getTripId());
                }
            return;
            }
        }
        if (userType.equals("driver")) {
            if(receivedMessage.contains("newLocation") && sessionTripIdMap.get(session)==null){
                String[] splitmessage = receivedMessage.split(" ");
                HashMap<String,Double> loc=new HashMap<>();
                System.out.println(splitmessage[0]);
                loc.put("latitude",Double.parseDouble(splitmessage[1]));
                loc.put("longitude",Double.parseDouble(splitmessage[2]));
                sessionLocationMap.put(session,loc);
                for (WebSocketSession ses: sessionNearbyDrivers.keySet()
                     ) {
                    if(sessionTripIdMap.containsKey(ses)) continue;
                    if(sessionNearbyDrivers.get(ses).contains(session)){
                        System.out.println("Location sent to normal nearby");
                        sendMessageToSession(ses,session.getHandshakeHeaders().getFirst("email") +" "+
                                loc.get("latitude")+" "+loc.get("longitude"));
                    }

                }
                System.out.println(sessionNearbyDrivers);
            }
            else if (receivedMessage.equals("decline")) return;
           else if (receivedMessage.contains("accept")) {
                String[] split=receivedMessage.split(" ");
                System.out.println(split[0]  + " "+split[1]);
                tripId = split[1];
                if (Collections.frequency(sessionTripIdMap.values(), Integer.parseInt(tripId)) == 2) {
                    sendMessageToSession(session, "Taken");
                    return;
                }
                sessionTripIdMap.put(session, Integer.parseInt(tripId));
                if (tripId != null) {
                    sendMessageToTripId(sessionTripIdMap.get(session), receivedMessage, session);
                }
            }
           else {
               sendMessageToTripId(sessionTripIdMap.get(session), receivedMessage, session);
           }


        }
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userType = session.getHandshakeHeaders().getFirst("UserType");
        if(userType.equals("driver")){
            sessionTripIdMap.put(session,null);
        }
        else{
            sessionNearbyDrivers.put(session,new ArrayList<>());
            System.out.println(sessionNearbyDrivers);
        }

        System.out.println("Client connected ");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println(session);
        sessionTripIdMap.remove(session);
        sessionLocationMap.remove(session);
        if(session.getHandshakeHeaders().getFirst("UserType").equals("normal")) {
            sessionNearbyDrivers.remove(session);
            System.out.println("Remove from Nearby normal");
        }
        else {
            for (WebSocketSession ses: sessionNearbyDrivers.keySet()
                 ) {
                if(sessionNearbyDrivers.get(ses).contains(session)){
                    System.out.println("removed from nearby");
                    sessionNearbyDrivers.get(ses).remove(session);
                }
            }
        }
        System.out.println("client disconnected");


    }

    public void sendMessageToTripId(Integer tripId, String message, WebSocketSession session) {
        sessionTripIdMap.keySet();
        for (WebSocketSession ses : sessionTripIdMap.keySet()
        ) {
            if (sessionTripIdMap.get(ses) == tripId && ses !=session) {
                sendMessageToSession(ses, message);
            }
        }
    }
    public void sendMessageToAll(WebSocketSession session,String message) {

        for (WebSocketSession ses : sessionTripIdMap.keySet()
        ) {
            if(ses!=session)
                sendMessageToSession(ses, message);
        }
    }

    private Integer extractTripIdFromSession(WebSocketSession session) {
        String tripId = session.getHandshakeHeaders().getFirst("X-Trip-ID");
        System.out.println(tripId);
        if(tripId!=null){
                        return Integer.parseInt(tripId);}
        else return 0;
    }
    private void sendMessageToSession(WebSocketSession ses, String message) {
        try {
            ses.sendMessage(new TextMessage(message) ) ;
        } catch (IOException e) {
        }
    }

}




