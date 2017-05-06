package com.example.leo.trackme;




 class LocationOfUser {

    public Double lat;

    public void setLati(Double lati) {
        this.lat = lati;
    }

    public void setLongi(Double longi) {
        this.longi = longi;
    }

    public Double getLati() {
        return lat;
    }

    public Double getLongi() {
        return longi;
    }

    public Double longi;
    public LocationOfUser(){}

    public LocationOfUser(Double latitude,Double longitude)
    {
        lat=latitude;
        longi=longitude;
    }


}
