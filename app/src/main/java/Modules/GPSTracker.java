package Modules;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.example.leo.trackme.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by leo on 14/04/17.
 */

public class GPSTracker extends Service implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{


    private final Context context;
    boolean isGPSEnabled =false;
    boolean isNetworkEnabled =false;
    boolean canGetLocation = false;

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private FusedLocationProviderApi locationProviderApi = LocationServices.FusedLocationApi;

    Location myLocation;
    protected LocationManager locationManager;

    public GPSTracker(Context context){
        this.context=context;
        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(GPSTracker.this).
                addOnConnectionFailedListener(this).
                build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(10 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    public  Location getLocation(){
        try{

            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);
            isNetworkEnabled=locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);

            if(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ){

                if(isGPSEnabled){
                    if(myLocation==null){
                        locationProviderApi.requestLocationUpdates(googleApiClient,locationRequest, (com.google.android.gms.location.LocationListener) context);
//                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000,10,this);
                        if(locationManager!=null){
                            myLocation = locationProviderApi.getLastLocation(googleApiClient);
                        }
                    }
                }
                // gps na parle network
                if(myLocation==null){
                    if(isNetworkEnabled){

                        locationProviderApi.requestLocationUpdates(googleApiClient,locationRequest, (com.google.android.gms.location.LocationListener) context);
                        if(locationManager!=null){
                            myLocation = locationProviderApi.getLastLocation(googleApiClient);
                        }

                    }
                }

            }

        }catch(Exception ex){

        }
        return  myLocation;
    }

    // some def methods
    @Override
    public void onConnected( Bundle bundle) {
        requestLocationUp();
    }

    private void requestLocationUp() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, (com.google.android.gms.location.LocationListener) this);
    }

    @Override
    public void onConnectionSuspended(int i) {


    }

    @Override
    public void onConnectionFailed( ConnectionResult connectionResult) {

    }








    private void checkforNetworkAndGps() {

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
            builder.setTitle("Need Location");
            builder.setMessage("We Require Location access to Continue Tracking.... ");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);


                }
            });

            builder.setCancelable(false);
            builder.show();


        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
