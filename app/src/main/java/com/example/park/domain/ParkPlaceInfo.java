package com.example.park.domain;

import android.graphics.Bitmap;

import com.example.park.util.Constant;
import com.example.park.util.HttpUtils;

/**
 * Created by 张海逢 on 2017/11/9.
 */

public class ParkPlaceInfo {
    private String userName;
    private String parkName;
    private String price;
    private String startTime;
    private String endTime;
    private String address;
    private String detailAddress;
    private String hostName;
    private String hostContact;
    private String comment;
    private double latitude;
    private double longitude;
    private String imagePath;
    private Bitmap bitmap;
    private Bitmap clearBitmap;
    private int distance;
    private boolean isPublished = false;
    private int index;
    private int pri_index;



    public ParkPlaceInfo(final int index, String address, String startTime, String price, String parkName, double latitude, double longitude, String hostName, String hostContact, String endTime, String detailAddress, Bitmap tempbitmap, String comment) {
        this.index = index;
        this.address = address;
        this.startTime = startTime;
        this.price = price;
        this.parkName = parkName;
        this.longitude = longitude;
        this.latitude = latitude;
        this.hostName = hostName;
        this.hostContact = hostContact;
        this.endTime = endTime;
        this.detailAddress = detailAddress;
        this.bitmap = tempbitmap;
        this.comment = comment;
    }

    public ParkPlaceInfo(String userName, String parkName, String price, String startTime, String endTime, String address, String detailAddress, String hostName, String hostContact, String comment, double latitude, double longitude, final String imagePath, int distance, boolean isPublished, int index, int pri_index){
        this.userName = userName;
        this.parkName = parkName;
        this.price = price;
        this.startTime = startTime;
        this.endTime = endTime;
        this.address = address;
        this.detailAddress = detailAddress;
        this.hostContact = hostContact;
        this.hostName = hostName;
        this.comment = comment;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
        if(isPublished){
            setPublished(true);
        }else{
            setPublished(false);
        }
        this.index = index;
        this.pri_index = pri_index;
        this.imagePath = imagePath;
        new Thread(new Runnable() {
            @Override
            public void run() {
                bitmap = HttpUtils.getBitmapFromServer(Constant.imageUrl+imagePath+"_vague.png");
            }
        }).start();
    }

    public Bitmap getClearBitmap() {
        return clearBitmap;
    }

    public void setClearBitmap(Bitmap clearBitmap) {
        this.clearBitmap = clearBitmap;
    }

    public int getPri_index() {
        return pri_index;
    }

    public void setPri_index(int pri_index) {
        this.pri_index = pri_index;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public boolean isPublished() {
        return isPublished;
    }

    public void setPublished(boolean published) {
        isPublished = published;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getParkName() {
        return parkName;
    }

    public void setParkName(String parkName) {
        this.parkName = parkName;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostContact() {
        return hostContact;
    }

    public void setHostContact(String hostContact) {
        this.hostContact = hostContact;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }


}
