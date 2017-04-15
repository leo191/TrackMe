package com.example.leo.trackme;

import android.location.Location;

/**
 * Created by leo on 15/04/17.
 */

public class LocationOfUser {

    public String lati;

    public void setLati(String lati) {
        this.lati = lati;
    }

    public void setLongi(String longi) {
        this.longi = longi;
    }

    public String getLati() {
        return lati;
    }

    public String getLongi() {
        return longi;
    }

    public String longi;
    public LocationOfUser(){}

    public LocationOfUser(String latitude,String longitude)
    {
        lati=latitude;
        longi=longitude;
    }


}
