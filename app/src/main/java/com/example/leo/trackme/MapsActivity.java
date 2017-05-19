package com.example.leo.trackme;


import android.*;
import android.Manifest;
import android.animation.ValueAnimator;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
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
import com.google.maps.android.SphericalUtil;

import java.util.List;
import java.util.Objects;


import Modules.LatLngInterpolator;

import static com.example.leo.trackme.R.id.add;
import static com.example.leo.trackme.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    //Basic variables
    String searchfor, whoru;
    Double vlat, vlong;
    Double latitude, longitude;
    //Views Variables
    Button btTrack;//Me testing
    EditText edsearch, edusername;
    DatabaseReference dRef;
    Button pl, min;
    LatLng last_curr_pos, notlatlong;
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
    Polyline polyLine;
    Circle circle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_maps);
        //If Location Is turned off


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

        StatusBarMod();


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





        //animateToMyLoc();

    }







    void StatusBarMod()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }





    float[] distance = new float[2];
    int c=0;

//Main Tracking Code and Updation of UI

    void locationUpUI(String s)
    {
        Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.bus);
        icon = Bitmap.createScaledBitmap(icon,100,50,true);

        mrk = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude,longitude))
                .title("Marker in " + edsearch.getText().toString() + " ass")
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                .anchor(0.5f, 1));
        mrk.setVisible(false);

        //Read Specific User Data From FireBase
        DatabaseReference tr = dRef.child(s);
        tr.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usr = dataSnapshot.getValue(LocationOfUser.class);
                mrk.setVisible(true);
                 first_curr_pos= new LatLng(usr.getLati(), usr.getLongi());



                mrk.setPosition(first_curr_pos);


                mMap.moveCamera(CameraUpdateFactory.newLatLng(first_curr_pos));

                CameraPosition newCamPos = new CameraPosition(new LatLng(usr.getLati(),usr.getLongi()),
                        16f,
                        mMap.getCameraPosition().tilt, //use old tilt
                        mMap.getCameraPosition().bearing); //use old bearing
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(newCamPos), 3000, null);

                //Handler for Animating Marker
                if(last_curr_pos!=null) {
                    float rotation = (float) SphericalUtil.computeHeading(last_curr_pos, first_curr_pos);
                    animateMarker(last_curr_pos,first_curr_pos,mrk,rotation);
                }
                last_curr_pos=first_curr_pos;


                CheckBusDistance();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    void CheckBusDistance()
    {
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







    //




        void NotifyParents()
        {
            c=1;
            Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            new Intent(getApplicationContext(),MapsActivity.class),
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(MapsActivity.this)
                            .setSmallIcon(R.mipmap.ic_launcher).setSound(uri)
                            .setContentTitle("Your child is Arriving")
                            .setContentText("Bus is just few minutes away from you.. :)")
                    .setContentIntent(resultPendingIntent);
            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

            notificationManager.notify(001,mBuilder.build());



        }









    //
//animating marker smoothly
    void animateMarker(final LatLng startltln, final LatLng endltln, final Marker marker,final float toRotation)
{
    final LatLng startPosition = startltln;
    final LatLng finalPosition = endltln;
    final Handler handler = new Handler();
    final float startRotation = marker.getRotation();
    final long start = SystemClock.uptimeMillis();
    final Interpolator moveinterpolator = new LinearInterpolator();
    final Interpolator rotateinterpolator = new LinearInterpolator();

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
            v = moveinterpolator.getInterpolation(t);

            LatLng currentPosition = new LatLng(
                    startPosition.latitude * (1 - t) + finalPosition.latitude * t,
                    startPosition.longitude * (1 - t) + finalPosition.longitude * t);

            float tR = rotateinterpolator.getInterpolation((float) elapsed / durationInMs);

            float rot = tR * toRotation + (1 -tR) * startRotation;

            marker.setRotation(-rot > 180 ? rot/2 : rot);

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



//



//    private void rotateMarker(final Marker marker, final LatLng destination, final float rotation) {
//
//        if (marker != null) {
//
//            final LatLng startPosition = marker.getPosition();
//            final float startRotation = marker.getRotation();
//
//            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.Spherical();
//            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
//            valueAnimator.setDuration(3000); // duration 3 second
//            valueAnimator.setInterpolator(new LinearInterpolator());
//            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//
//                    try {
//                        float v = animation.getAnimatedFraction();
//                        LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, destination);
//                        float bearing = computeRotation(v, startRotation, rotation);
//
//                        marker.setRotation(bearing);
//                        marker.setPosition(newPosition);
//
//                    }
//                    catch (Exception e){
//                        e.printStackTrace();
//                    }
//                }
//            });
//            valueAnimator.start();
//        }
//    }
//    private static float computeRotation(float fraction, float start, float end) {
//        float normalizeEnd = end - start; // rotate start to 0
//        float normalizedEndAbs = (normalizeEnd + 360) % 360;
//
//        float direction = (normalizedEndAbs > 180) ? -1 : 1; // -1 = anticlockwise, 1 = clockwise
//        float rotation;
//        if (direction > 0) {
//            rotation = normalizedEndAbs;
//        } else {
//            rotation = normalizedEndAbs - 360;
//        }
//
//        float result = fraction * rotation + start;
//        return (result + 360) % 360;
//    }

//









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
       // mMap.setPadding(0,displayMetrics.heightPixels-500,0,0);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        mMap.setMyLocationEnabled(true);


    }




    void animateToMyLoc() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location loc = locationProviderApi.getLastLocation(googleApiClient);
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(),loc.getLongitude()), 15));


    }
    Marker jh;

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
        animateToMyLoc();


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
