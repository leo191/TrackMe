package com.example.leo.trackme;

import android.*;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import static com.example.leo.trackme.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {



    //Basic variables
    String searchfor,whoru;
    Double vlat,vlong;
    double latitude, longitude;
    //Views Variables
    Button btTrack;
    EditText edsearch, edusername;
    DatabaseReference dRef;
    Button pl,min;
    LatLng lo;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private FusedLocationProviderApi locationProviderApi = LocationServices.FusedLocationApi;
    TextView ltlng;
    LocationOfUser usr;
    private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_maps);

        //Supppor for M and later


        //Google api client for FusedLocationProvider
        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).
                build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(10 * 1000);
        locationRequest.setFastestInterval(15 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        vlat=vlong=0.0000;

        //firebase Reference Initialization
        dRef = FirebaseDatabase.getInstance().getReference();


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);



//Basic UI and Initialization

        btTrack = (Button) findViewById(R.id.trackbt);
        edusername = (EditText) findViewById(R.id.user_ed);
        ltlng = (TextView)findViewById(R.id.latlongtv);

        edsearch = (EditText) findViewById(R.id.search_ed);

        pl = (Button)findViewById(R.id.add);
        min = (Button)findViewById(R.id.sub);


       /* pl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vlat+=0.0010000;
                vlong+=0.0010000;

            }
        });
        pl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vlat-=0.0010000;
                vlong-=0.0010000;

            }
        });
*/


       btTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                searchfor = edsearch.getText().toString();
                whoru = edusername.getText().toString();

                if(mMap==null)
               {
                    return;
                }
               else
                {
                    if(searchfor.isEmpty() || whoru.isEmpty())
                    {
                        Toast.makeText(MapsActivity.this,"Enter all mentions",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        locationUpUI(searchfor);



                    }
                }
            }
        });



    }


//Main Tracking Code and Updation of UI

    void locationUpUI(String s)
    {

        //Read Specific User Data From FireBase
        DatabaseReference tr = dRef.child(s);
        tr.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                usr = dataSnapshot.getValue(LocationOfUser.class);

                final LatLng opp = new LatLng(usr.getLati(), usr.getLongi());
                mMap.clear();
                final Marker mrk = mMap.addMarker(new MarkerOptions().position(opp).title("Marker in " + edsearch.getText().toString() + " ass"));

                //Handler for Animating Marker
                if(lo!=null) {
                    final LatLng startPosition = lo;
                    final LatLng finalPosition = opp;
                    final Handler handler = new Handler();
                    final long start = SystemClock.uptimeMillis();
                    final Interpolator interpolator = new AccelerateDecelerateInterpolator();
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
                            t = elapsed / durationInMs;
                            v = interpolator.getInterpolation(t);

                            LatLng currentPosition = new LatLng(
                                    startPosition.latitude * (1 - t) + finalPosition.latitude * t,
                                    startPosition.longitude * (1 - t) + finalPosition.longitude * t);


                            mrk.setPosition(currentPosition);
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(opp));
                            // Repeat till progress is complete.
                            if (t < 1) {
                                // Post again 16ms later.
                                handler.postDelayed(this, 16);
                            } else {
                                if (hideMarker) {
                                    mrk.setVisible(false);
                                } else {
                                    mrk.setVisible(true);
                                }
                            }
                        }
                    });
                }
                lo=opp;


                mMap.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);





            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }



















    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setBuildingsEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
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
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }
}
