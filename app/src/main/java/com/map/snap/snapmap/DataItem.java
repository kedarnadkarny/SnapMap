package com.map.snap.snapmap;

public class DataItem {

    String placeID;
    String placeName;
    String lat;
    String lon;
    String dnd;

    public DataItem(String placeID, String placeName, String lat, String lon, String dnd) {
        this.placeID = placeID;
        this.placeName = placeName;
        this.lat = lat;
        this.lon = lon;
        this.dnd = dnd;
    }
}
