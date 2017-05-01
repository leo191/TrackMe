package com.example.leo.trackme;

import android.*;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
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
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GPSTracker gpsTracker;
    private Location mLocation;
    double latitude, longitude;
    Button btTrack, btdirec;
    EditText edsearch, edusername;
    FirebaseDatabase fDB;
    DatabaseReference dRef;

    LatLng opp ;
    private static final int REQUEST_CODE_PERMISSION = 2;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private FusedLocationProviderApi locationProviderApi = LocationServices.FusedLocationApi;
    TextView ltlng;
    String searchfor,whoru;
    Object ll,lo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_maps);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_PERMISSION);

        }

        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).
                build();

        locationRequest = new LocationRequest();
        locationRequest.setInterval(10 * 1000);
        locationRequest.setFastestInterval(15 * 1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);


        gpsTracker = new GPSTracker(getApplicationContext());
        mLocation = gpsTracker.getLocation();

        latitude = mLocation.getLatitude();
        longitude = mLocation.getLongitude();
        dRef = FirebaseDatabase.getInstance().getReference();

        // map e hat diben na
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btTrack = (Button) findViewById(R.id.trackbt);
        btdirec = (Button) findViewById(R.id.directbt);
        edusername = (EditText) findViewById(R.id.user_ed);
        ltlng = (TextView)findViewById(R.id.latlongtv);
        btdirec.setVisibility(View.INVISIBLE);
        edsearch = (EditText) findViewById(R.id.search_ed);






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
                    if(searchfor.equals("") && whoru.equals(""))
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

    public class fetchLatLong extends AsyncTask<String, Void, String>{


        public fetchLatLong() {
            super();
        }


        @Override
        protected String doInBackground(String... params) {

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            //Toast.makeText(getApplicationContext(),usr.getLati().toString(),Toast.LENGTH_SHORT).show();
        }

       /* dRef.child(st).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Object lat = dataSnapshot.child("lat").getValue();
                Object longi = dataSnapshot.child("long").getValue();


//                    usr = dataSnapshot.getValue(LocationOfUser.class);
                newLatlong = new LatLng((double) lat, (double) longi);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/

    }


    void locationUpUI(String s)
    {
        DatabaseReference tr = dRef.child(s);
        tr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LocationOfUser usr = dataSnapshot.getValue(LocationOfUser.class);

                opp = new LatLng(usr.getLati(), usr.getLongi());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(opp).title("Marker in " + edsearch.getText().toString() + " ass"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(opp));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }






    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;



        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);

    }


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

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        ltlng.setText(String.valueOf(latitude)+ " " +String.valueOf(longitude));

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
