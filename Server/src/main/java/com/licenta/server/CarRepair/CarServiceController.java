package com.licenta.server.CarRepair;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.licenta.server.UserService.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("carservices")
public class CarServiceController {

    @Autowired
    private CarServiceRepository carServiceRepository;
    @PostMapping("/range")
    public ResponseEntity<?> getByRange(@RequestBody RangeRequestClass body){

        ArrayList<CarService> carrepairs=carServiceRepository.findByDistance((Double) body.getLatitude(),(Double)body.getLongitude(),(Double)body.getRange());
        ArrayList<Map<String,Object>> respons=new ArrayList<>();
        System.out.println(carrepairs.size());
        if(carrepairs.size()==0){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        for (CarService s:carrepairs
             ) {
            respons.add(retreiveCarServiceInfo(s));
        }
        return ResponseEntity.status(HttpStatus.OK).body(respons);
    }
    private Map<String, Object> retreiveCarServiceInfo(CarService carService) {
        Map<String,Object> carServiceInfo =new HashMap<>();
        carServiceInfo.put("id",carService.getId());
        carServiceInfo.put("address",carService.getAddress())    ;
        carServiceInfo.put("serviceName",carService.getServiceName());
        carServiceInfo.put("longitude",carService.getLongitude());
        carServiceInfo.put("latitude",carService.getLatitude());
        carServiceInfo.put("city",carService.getCity());
        return carServiceInfo;
    }

}
