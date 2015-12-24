package com.msc.EasyRoutePlanner;

import android.app.Dialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;

public class MainActivity extends FragmentActivity implements LocationListener,
        OnStreetViewPanoramaReadyCallback, SensorEventListener {

    GoogleMap googleMap;
    private TextView StepCount;
    private TextView Miles;
    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;
    private Sensor mStepDetectorSensor;
    private TextView CaloriesBurn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Miles = (TextView) findViewById(R.id.distance);
        StepCount = (TextView) findViewById(R.id.StepsCount);
        CaloriesBurn = (TextView) findViewById(R.id.calories_value);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());


        // Showing status
        if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();


        } else { // Google Play Services are available

            // Getting reference to the SupportMapFragment of activity_main.xml
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);


            // Getting GoogleMap object from the fragment
            googleMap = fm.getMap();

            // Enabling MyLocation Layer of Google Map
            googleMap.setMyLocationEnabled(true);


            // Getting LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);

            // Getting Current Location
            Location location = locationManager.getLastKnownLocation(provider);

            if (location != null) {
                onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(provider, 20000, 0, this);


        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        googleMap = mapFragment.getMap();

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);


        RadioGroup rgViews = (RadioGroup) findViewById(R.id.rg_views);

        rgViews.setOnCheckedChangeListener
                (new RadioGroup.OnCheckedChangeListener() {

                     @Override
                     public void onCheckedChanged(RadioGroup group, int checkedId) {


                         if (checkedId == R.id.rb_normal) {
                             googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                             googleMap.setBuildingsEnabled(true);
                             googleMap.setIndoorEnabled(true);


                         } else if (checkedId == R.id.rb_satellite) {
                             googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

                         } else if (checkedId == R.id.rb_terrain) {

                             googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                         }
                     }


                 }

                );
        // calling street view on background
        StreetViewPanoramaFragment streetViewPanoramaFragment = (StreetViewPanoramaFragment) getFragmentManager()
                .findFragmentById(R.id.streetviewpanorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(MainActivity.this);

    }

    // Setting up panorama view method for streetView
    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama panorama) {

        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Getting Current Location
        Location location = locationManager.getLastKnownLocation(provider);

        // setting up live location to StreetView
        if (location != null) {
            locationManager.requestLocationUpdates(provider, 20000, 0, this);
            panorama.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));

        }// setting up default location to StreetView 281 Thornton Road, Croydon,CR0 3EW

        else {
            panorama.setPosition(new LatLng(51.388500900000004, -0.1205977));
        }
    }

    // Setting up step counter and setup detector sensor for distance and calories burn calculation
    public void onSensorChanged(SensorEvent event) {


        Sensor sensor = event.sensor;
        float[] values = event.values;


        int value = -1;


        DecimalFormat df = new DecimalFormat("#.###");

        double myweight = 70;

        double onestep = (0.0004734848484848485);//one step in mile

        // Calorie calculations from equation: (METs x 3.5 x body weight in kg)/200 = calories/minute
        double oneSetpCalPerOneKilo = 1.32352941;
        // if user walk 1 mile and weight is 1 kg he/she will burn 1.32352941cal
        double calTest = (myweight);

        double oneStepCalories = (oneSetpCalPerOneKilo * calTest) / (2112);
        // 2112 steps in one mile if steps length is 30 inches
        // if user weight is 70 kilogram and walk 1 mile 70*1.3252941 = 92.40 calories burn
        // in one mile Total steps are 2112
        if (values.length > 0) {
            value = (int) values[0];
        }
        if (sensor.getType() == Sensor.TYPE_STEP_COUNTER) {

            StepCount.setText("" + value);
            Miles.setText("" + df.format(onestep * value));
            CaloriesBurn.setText("" + df.format(oneStepCalories * value));

        } else if (sensor.getType() == Sensor.TYPE_STEP_DETECTOR) {

            StepCount.setText("" + value);
            Miles.setText("" + df.format(onestep * value));
            CaloriesBurn.setText("" + df.format(oneStepCalories * value));
        }


    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    // Step 4: Register and Unregister Sensors

    protected void onResume() {

        super.onResume();
        mSensorManager.registerListener(this, mStepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mStepDetectorSensor, SensorManager.SENSOR_DELAY_FASTEST);

    }

    @Override
    public void onLocationChanged(final Location location) {

        // calling street view on background
        StreetViewPanoramaFragment streetViewPanoramaFragment = (StreetViewPanoramaFragment) getFragmentManager()
                .findFragmentById(R.id.streetviewpanorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(MainActivity.this);
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title("Current Location");
        markerOptions.snippet("You are here");


        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(16), 20000, null);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                // Sets the center of the mapGoogle to location user
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        markerOptions.position(latLng);

        location.getLatitude();
        location.getAccuracy();

    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


}