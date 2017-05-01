package com.example.leo.trackme;




 class LocationOfUser {

    private Double lat;

    public void setLati(Double lati) {
        this.lat = lati;
    }

    public void setLongi(Double longi) {
        this.longi = longi;
    }

    private Double getLati() {
        return lat;
    }

    private Double getLongi() {
        return longi;
    }

    private Double longi;
    public LocationOfUser(){}

    public LocationOfUser(Double latitude,Double longitude)
    {
        lat=latitude;
        longi=longitude;
    }


}
