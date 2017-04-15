package com.example.leo.trackme;

import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private GPSTracker gpsTracker;
    private Location mLocation;
    double latitude, longitude;
    Button btTrack,btdirec;
    EditText edsearch,edusername;
    FirebaseDatabase fDB;
    DatabaseReference dRef;
    LocationOfUser usr;
    LatLng newLatlong;

    String searchfor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);





        setContentView(R.layout.activity_maps);

        gpsTracker = new GPSTracker(getApplicationContext());
        mLocation = gpsTracker.getLocation();

        latitude = mLocation.getLatitude();
        longitude = mLocation.getLongitude();


        // map e hat diben na
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btTrack = (Button)findViewById(R.id.trackbt);
        btdirec = (Button)findViewById(R.id.directbt);
        edusername =(EditText)findViewById(R.id.user_ed);

        btdirec.setVisibility(View.INVISIBLE);
        edsearch =(EditText)findViewById(R.id.search_ed);


        btTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                searchfor = edsearch.getText().toString();

                if(mMap==null)
               {
                    return;
                }
               else
                {
                    if(searchfor.equals("") || searchfor.equals(""))
                    {
                        Toast.makeText(MapsActivity.this,"Enter A Nanme",Toast.LENGTH_SHORT).show();
                    }
                    else {


                        new ContinuosFetch().execute();


                    }
                }
            }
        });



    }

    private void fetchLatLong(String st) {



        dRef = FirebaseDatabase.getInstance().getReference();
        dRef.child(st).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Object lat=dataSnapshot.child("lat").getValue();
                Object longi = dataSnapshot.child("long").getValue();


//                    usr = dataSnapshot.getValue(LocationOfUser.class);
                   newLatlong = new LatLng((double)lat,(double)longi);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mMap.addMarker(new MarkerOptions().position(newLatlong).title("Marker in "+edsearch.getText().toString()+" ass"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(newLatlong));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
    }



    public class ContinuosFetch extends AsyncTask<String,String,String>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected String doInBackground(String... params) {
            try{

                fetchLatLong(searchfor);



            }catch (Exception e)
            {

            }
            return  null;
        }

    }









    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        dRef = FirebaseDatabase.getInstance().getReference();
        dRef.child("leo").child("lat").setValue(latitude);
        dRef.child("leo").child("long").setValue(longitude);
        LatLng me = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(me).title("Marker in My ass"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(me));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
    }







}
