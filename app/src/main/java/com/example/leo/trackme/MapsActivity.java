package com.example.leo.trackme;


import android.*;
import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Color;
import android.location.GnssStatus;
import android.location.Location;

import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;

import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;


import static com.example.leo.trackme.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    //Basic variables
    String searchfor, whoru;
    Double vlat, vlong;
    Double latitude, longitude;
    //Views Variables
    Button btTrack;
    EditText edsearch, edusername;
    DatabaseReference dRef;
    Button pl, min;
    LatLng last_curr_pos,notlatlong;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private FusedLocationProviderApi locationProviderApi = LocationServices.FusedLocationApi;
    TextView ltlng;
    LocationOfUser usr;
    private GoogleMap mMap;
    RadioGroup radioGroup;
    ImageView normal, satalit;
    View mapView;
    LatLng first_curr_pos;
    Marker mrk;
    Polyline polyLine; Circle circle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_maps);
        //If Location Is turned off
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        //Google api client for FusedLocationProvider
        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).
                build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(10 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        vlat = vlong = 0.0000;

        //firebase Reference Initialization
        dRef = FirebaseDatabase.getInstance().getReference();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();


//Basic UI and Initialization

        btTrack = (Button) findViewById(R.id.trackbt);
        edusername = (EditText) findViewById(R.id.user_ed);
        ltlng = (TextView) findViewById(R.id.latlongtv);

        edsearch = (EditText) findViewById(R.id.search_ed);

        pl = (Button) findViewById(R.id.add);
        min = (Button) findViewById(R.id.sub);


        normal = (ImageView) findViewById(R.id.normal_map_button);
        satalit = (ImageView) findViewById(R.id.satalite_map_button);

        normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap != null)
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }
        });

        satalit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMap != null)
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            }
        });


        btTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                searchfor = edsearch.getText().toString();
                whoru = edusername.getText().toString();

                if (mMap == null) {
                    return;
                } else {
                    if (searchfor.isEmpty() || whoru.isEmpty()) {
                        Toast.makeText(MapsActivity.this, "Enter all mentions", Toast.LENGTH_SHORT).show();
                    } else {
                        locationUpUI(searchfor);

                    }
                }
            }
        });


//        if(mMap!=null)
//        {
//            mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
//                @Override
//                public boolean onMyLocationButtonClick() {
//                    if(latitude == null&& longitude == null)
//                    {
//
//                    }
//                    LatLng target = new LatLng(latitude, longitude);
//                    CameraPosition position = mMap.getCameraPosition();
//
//                    CameraPosition.Builder builder = new CameraPosition.Builder();
//                    builder.zoom(15);
//                    builder.target(target);
//
//                    mMap.animateCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
//                    return  true;
//
//                }
//
//            });
//        }
//
        


         LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        checkforNetworkAndGps(lm);

    }


    private void checkforNetworkAndGps(LocationManager lm) {


        if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {

        }
        else {

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
            builder.setTitle("Need Location");
            builder.setMessage("We Require Location access to Continue Tracking.... ");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);


                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            builder.setCancelable(false);
            builder.show();


        }




        /*boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage(context.getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(context.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton(context.getString(R.string.Cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    finish();

                }
            });
            dialog.show();
        }*/
    }
    float[] distance = new float[2];
    int c=0;

//Main Tracking Code and Updation of UI

    void locationUpUI(String s)
    {


        //Read Specific User Data From FireBase
        DatabaseReference tr = dRef.child(s);
        tr.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usr = dataSnapshot.getValue(LocationOfUser.class);

                 first_curr_pos= new LatLng(usr.getLati(), usr.getLongi());
                if(mrk!=null){mrk.remove();}
                 mrk = mMap.addMarker(new MarkerOptions().position(first_curr_pos).title("Marker in " + edsearch.getText().toString() + " ass"));

                mMap.moveCamera(CameraUpdateFactory.newLatLng(first_curr_pos));

                CameraPosition newCamPos = new CameraPosition(new LatLng(usr.getLati(),usr.getLongi()),
                        16f,
                        mMap.getCameraPosition().tilt, //use old tilt
                        mMap.getCameraPosition().bearing); //use old bearing
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCamPos), 4000, null);

                //Handler for Animating Marker
                if(last_curr_pos!=null) {
                    animateMarker(last_curr_pos,first_curr_pos,mrk);
                }
                last_curr_pos=first_curr_pos;
                if(circle!=null)
                {
                    Location.distanceBetween( mrk.getPosition().latitude, mrk.getPosition().longitude,
                            circle.getCenter().latitude, circle.getCenter().longitude, distance);
                    if( distance[0] < circle.getRadius()  ){

                    if(c==0)
                    {
                       NotifyParents();

                    }
                    }
                    else {
                        c=0;
                    }
                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }




    //




        void NotifyParents()
        {
            c=1;
            Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            new Intent(this,MapsActivity.class),
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(MapsActivity.this)
                            .setSmallIcon(R.mipmap.ic_launcher).setSound(uri)
                            .setContentTitle("Puchka Arriving")
                            .setContentText("He is just few minutes away from you.. :)")
                    .setContentIntent(resultPendingIntent);
            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

            notificationManager.notify(001,mBuilder.build());



        }




    //
//animating marker smoothly
    void animateMarker(final LatLng startltln, final LatLng endltln, final Marker marker)
{
    final LatLng startPosition = startltln;
    final LatLng finalPosition = endltln;
    final Handler handler = new Handler();
    final long start = SystemClock.uptimeMillis();
    final Interpolator interpolator = new LinearInterpolator();
    final float durationInMs = 3000;
    final boolean hideMarker = false;

    handler.post(new Runnable() {
        long elapsed;
        float t;
        float v;

        @Override
        public void run() {
            // Calculate progress using interpolator
            elapsed = SystemClock.uptimeMillis() - start;
            t = (float)elapsed / durationInMs;
            v = interpolator.getInterpolation(t);

            LatLng currentPosition = new LatLng(
                    startPosition.latitude * (1 - t) + finalPosition.latitude * t,
                    startPosition.longitude * (1 - t) + finalPosition.longitude * t);



            marker.setPosition(currentPosition);



            // Repeat till progress is complete.
            if (t < 1.0 ) {
                // Post again 16ms later.
                handler.postDelayed(this, 16);
            } else {
                if (hideMarker) {
                    marker.setVisible(false);
                } else {
                    marker.setVisible(true);
                }
            }
        }
    });

}













    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setBuildingsEnabled(true);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }


        if (mapView != null &&
                mapView.findViewById(Integer.parseInt("1")) != null) {
            // Get the button view
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            // and next place it, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)
                    locationButton.getLayoutParams();
            // position on right bottom
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 30, 100);
        }
        mMap.setPadding(0,displayMetrics.heightPixels-500,0,0);

        mMap.setMyLocationEnabled(true);

    }



    //FusedLocationProvider to get Users current location details;
    @Override
    public void onConnected( Bundle bundle) {
        requestLocationUp();
    }

    private void requestLocationUp() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {


    }

    @Override
    public void onConnectionFailed( ConnectionResult connectionResult) {

    }




    @Override
    public void onLocationChanged(Location location) {

        latitude = location.getLatitude() ;
        longitude = location.getLongitude();
        ltlng.setText(String.valueOf(latitude)+ " " +String.valueOf(longitude));
        if(circle!=null){circle.remove();}
         circle = mMap.addCircle(new CircleOptions()
                .center(new LatLng(latitude,longitude))
                .radius(100)
                .strokeColor(Color.BLUE).strokeWidth(1)
                );


        if(whoru!=null)
        {
            DatabaseReference ref = dRef.child(whoru);
            ref.child("lat").setValue(latitude);
            ref.child("longi").setValue(longitude);


        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (googleApiClient.isConnected()) {
            requestLocationUp();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        requestLocationUp();
    }
}
