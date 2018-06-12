package com.example.park.domain;

/**
 * Created by xmuSistone on 2017/9/20.
 */

public class StockEntity {

    private String name;
    private String address;
    private int distance;



    private double latitude;
    private double longitude;

    public StockEntity(String name, String address, double latitude, double longitude, int distance) {
        this.name = name;
        this.address = address;
        this.distance = distance;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public int getDistance() {
        return distance;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }


}
