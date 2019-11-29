package com.hasanfakhra.noise_detector;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
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
import static com.hasanfakhra.noise_detector.Surrounding.dbCount;

import java.util.ArrayList;
import java.util.HashMap;

public class SoundHeatMap extends AppCompatActivity implements LocationListener, GoogleMap.OnMarkerClickListener, OnMapReadyCallback {



    private static final LatLng Drumcondra_library = new LatLng(53.369894,-6.258985);
    private static final LatLng Bull_Island = new LatLng(53.3704969,-6.1440493);
    private static final LatLng Ballyfermot_Civic_Centre = new LatLng(53.343281,-6.3618722);

    private static final LatLng Dublin_City_Council_Rowing_Club = new LatLng(53.3462979,-6.3199523);

    private static final LatLng Walkinstown_Library = new LatLng(53.318691,-6.321806);

    private static final LatLng Woodstock_Gardens = new LatLng(53.3232701,-6.2491243);

    private static final LatLng Navan_Road = new LatLng(53.3825149,-6.361692);

    private static final LatLng Raheny_Library = new LatLng(53.380015,-6.173108);
    private static final LatLng Irishtown_Stadium = new LatLng(53.3401623,-6.2199055);

    private static final LatLng Chancery_Park = new LatLng(53.3465867,-6.2721919);

    private static final LatLng Blessington_St_Basin = new LatLng(53.3572136,-6.2706976);
    private static final LatLng Dolphins_Barn= new LatLng(53.3335324,-6.2914604);

    private static final LatLng Sean_Moore_Road = new LatLng(53.339742,-6.21576);
    private static final LatLng Mellows_Park = new LatLng(53.3930654,-6.3149244);


    private GoogleMap viewcMap;
     private android.location.LocationManager lm;
     private Marker markerLocation;
    private ListView mfirebaseListView;
    private ArrayList<String> mSensor = new ArrayList<>();
    DatabaseReference hdbRef;
    String [] locarray;
//    ArrayList<String> locationarr = new ArrayList<String>();
//    ArrayList<String> noisearr = new ArrayList<String>();


    HashMap<Long, String> map = new HashMap<Long, String>();


//Markers

    private Marker mDrumcondra_library;
    private Marker mBull_Island;
    private Marker mBallyfermot_Civic_Centre;
    private Marker mDublin_City_Council_Rowing_Club;
    private Marker mWalkinstown_Library;
    private Marker mWoodstock_Gardens;
    private Marker mNavan_Road;
    private Marker mRaheny_Library;
    private Marker mIrishtown_Stadium;
    private Marker mChancery_Park;
    private Marker mBlessington_St_Basin;
    private Marker mDolphins_Barn;
    private Marker mSean_Moore_Road;
    private Marker mMellows_Park;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_heat_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.viewcMapFragment);
        mapFragment.getMapAsync(this);

        retrieveFirebase();


    }

    public void onMapReady(GoogleMap googleMap) {
        viewcMap = googleMap;


        mDrumcondra_library = viewcMap.addMarker(new MarkerOptions()
                .position(Drumcondra_library)
                .title("Drumcondra Library")
                .snippet("Average Noise: 46.31 "));
        mDrumcondra_library.setTag(0);

        mBull_Island = viewcMap.addMarker(new MarkerOptions()
                .position(Bull_Island)
                .title("Bull Island")
                .snippet("Average Noise: 45.4"));
        mBull_Island.setTag(0);

        mBallyfermot_Civic_Centre = viewcMap.addMarker(new MarkerOptions()
                .position(Ballyfermot_Civic_Centre)
                .title("Ballyfermot Civic Centre")
                .snippet("Average Noise: 46.78"));
        mBallyfermot_Civic_Centre.setTag(0);

        mDublin_City_Council_Rowing_Club = viewcMap.addMarker(new MarkerOptions()
                .position(Dublin_City_Council_Rowing_Club)
                .title("Dublin City Council Rowing Club")
                .snippet("Average Noise: 66.13"));
        mDublin_City_Council_Rowing_Club.setTag(0);

        mWalkinstown_Library = viewcMap.addMarker(new MarkerOptions()
                .position(Walkinstown_Library)
                .title("Walkinstown Library")
                .snippet("Average Noise: 55.6"));
        mWalkinstown_Library.setTag(0);

        mWoodstock_Gardens = viewcMap.addMarker(new MarkerOptions()
                .position(Woodstock_Gardens)
                .title("Woodstock Garden")
                .snippet("Average Noise: 54.3"));
        mWoodstock_Gardens.setTag(0);

        mNavan_Road = viewcMap.addMarker(new MarkerOptions()
                .position(Navan_Road)
                .title("Navan Road")
                .snippet("Average Noise: 58.9"));
        mNavan_Road.setTag(0);

        mRaheny_Library = viewcMap.addMarker(new MarkerOptions()
                .position(Raheny_Library)
                .title("Raheny Library")
                .snippet("Average Noise: 48.9"));
        mRaheny_Library.setTag(0);

        mIrishtown_Stadium = viewcMap.addMarker(new MarkerOptions()
                .position(Irishtown_Stadium)
                .title("Irishtown Stadium")
                .snippet("Average Noise: 62.1"));


        mIrishtown_Stadium.setTag(0);

        mBlessington_St_Basin = viewcMap.addMarker(new MarkerOptions()
                .position(Blessington_St_Basin)
                .title("Blessington St Basin")
                .snippet("Average Noise: 64.3"));

        mBlessington_St_Basin.setTag(0);

        mChancery_Park = viewcMap.addMarker(new MarkerOptions()
                .position(Chancery_Park)
                .title("Chancery Park")
                .snippet("Average Noise: 59.1"));
        mChancery_Park.setTag(0);

        mDolphins_Barn = viewcMap.addMarker(new MarkerOptions()
                .position(Dolphins_Barn)
                .title("Dolphins Barn")
                .snippet("Average Noise: 64.3"));
        mDolphins_Barn.setTag(0);

        mSean_Moore_Road = viewcMap.addMarker(new MarkerOptions()
                .position(Sean_Moore_Road)
                .title("Sean Moore Road")
                .snippet("Average Noise: 64.1"));
        mSean_Moore_Road.setTag(0);

        mMellows_Park = viewcMap.addMarker(new MarkerOptions()
                .position(Mellows_Park)
                .title("Mellows Park")
                .snippet("Average Noise: 55.11"));
        mMellows_Park.setTag(0);

        startGettingLocations();


    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        // Retrieve the data from the marker.
        Integer clickCount = (Integer) marker.getTag();

        // Check if a click count was set, then display the click count.
        if (clickCount != null) {
            clickCount = clickCount + 1;
            marker.setTag(clickCount);
            Toast.makeText(this,
                    marker.getTitle() +
                            " has been clicked " + clickCount + " times.",
                    Toast.LENGTH_SHORT).show();
        }

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    private void retrieveFirebase(){

            hdbRef = FirebaseDatabase.getInstance().getReference();

            mfirebaseListView = (ListView) findViewById(R.id.firebase_list);

            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mSensor);
            final ArrayList<String> locationarr = new ArrayList<String>();
            final ArrayList<String> noisearr = new ArrayList<String>();



            mfirebaseListView.setAdapter(arrayAdapter);

            hdbRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //String childCount = dataSnapshot.getChildrenCount();
                    for(int i=0;i<10;i++){
                        String location = dataSnapshot.child(i+"").child("contLocationinent").getValue(String.class);
                       // ArrayList<String> locationarr = new ArrayList<String>();
                        //locationarr.add(location);

                        String sound = dataSnapshot.child(i+"").child("Noise").getValue().toString();
                        //int soundInt = Integer.parseInt(sound);
                       // ArrayList<String> noisearr = new ArrayList<String>();
                       // noisearr.add(sound);

                        mSensor.add(location+" " + sound);
//                    // mSensor.add(sound);
                        arrayAdapter.notifyDataSetChanged();

                    }
//                    mSensor.add(locationarr+" " + noisearr);
//                    // mSensor.add(sound);
//                    arrayAdapter.notifyDataSetChanged();

//                    for(int i=0;i<noisearr.size()-1;i++) {
//                        int count = 1;
//
//                        int noise2 = 1;
//                        if (locationarr.get(i).equals(locationarr.get(i + 1))) {
//                            count++;
//                            String noisearr2;
//                            noisearr2 = noisearr.get(i);
//                            noise2 = Integer.parseInt(noisearr2);
//
//
//                        } else {
//                            mSensor.add(locationarr.size() + "");
//                            // mSensor.add(sound);
//                            arrayAdapter.notifyDataSetChanged();
//                        }
//                        String average = noise2 / count+"";
//                        ;
//                        //Toast.makeText(getApplicationContext(), location, Toast.LENGTH_LONG).show();
//                        mSensor.add(locationarr.get(i) + " " + average);
//                        // mSensor.add(sound);
//                        arrayAdapter.notifyDataSetChanged();
//                    }



//                    Toast.makeText(getApplicationContext(), location, Toast.LENGTH_LONG).show();
//                    mSensor.add(location+" " + sound);
//                   // mSensor.add(sound);
//                    arrayAdapter.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
//




    }




    private void startGettingLocations() {
        lm = (android.location.LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPS = lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
        boolean isNetwork = lm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
        long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;// Distance in meters
        long MIN_TIME_BW_UPDATES = 100;// Time in milliseconds

        //Starts requesting location updates
        //This line stops gps being used as its not useful indoors comment out for gps location
        //over network location
        try {
            isGPS=false;

            if (isGPS) {
                lm.requestLocationUpdates(
                        android.location.LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            } else if (isNetwork) {
                // from Network Provider

                lm.requestLocationUpdates(
                        android.location.LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            }
        }catch(SecurityException e){

        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng latlng = new LatLng(location.getLatitude(),location.getLongitude());
        addMarker(latlng);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    private void addMarker(LatLng latLng) {
        if (latLng == null) {
            return;
        }
        if (markerLocation != null) {
            markerLocation.remove();
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Location");
        markerOptions.snippet("Average Noise:" + dbCount+"");

        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        if (viewcMap != null)
            markerLocation = viewcMap.addMarker(markerOptions);


        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latLng.latitude, latLng.longitude))
                .zoom(16)
                .build();

        if (viewcMap != null)
            viewcMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    }
