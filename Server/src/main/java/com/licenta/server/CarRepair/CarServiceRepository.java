package com.licenta.server.CarRepair;


import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public interface CarServiceRepository extends JpaRepository<CarService, Long> {

    CarService findByAddress(String address) ;
    //@Query("SELECT s FROM CarService s WHERE DistanceUtil.calculateDistance(s.latitude, s.longitude, :latitude, :longitude) <=:range")
    default ArrayList<CarService> findByDistance(double latitude, double longitude,  double range)
    {
    List<CarService> allCarServices=findAll();
    ArrayList<CarService> filteredCarServices=new ArrayList<>();

        for (CarService s : allCarServices
             ) {
            double result=DistanceUtil.calculateDistance(latitude,longitude,s.getLatitude(),s.getLongitude());
            System.out.println(s.getServiceName() + result);
            if(result/1000<range) {
                filteredCarServices.add(s);
            }
            }
    return filteredCarServices;
    }

}
