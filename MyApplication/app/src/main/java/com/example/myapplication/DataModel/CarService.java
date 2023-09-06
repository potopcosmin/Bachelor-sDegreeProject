package com.example.myapplication.DataModel;

public class CarService  {
    @Override
    public String toString() {
        return "CarService{" +
                "id=" + id +
                ", serviceName='" + serviceName + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    private Integer id;


    private String serviceName;


    private User serviceManager;

    private String address;


    private String city;

    private Double latitude;

    private Double longitude;

    public CarService() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public User getServiceManager() {
        return serviceManager;
    }

    public void setServiceManager(User serviceManager) {
        this.serviceManager = serviceManager;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}