package com.msc.EasyRoutePlanner;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.msc.route.AbstractRouting;
import com.msc.route.Route;
import com.msc.route.Routing;
import com.msc.route.RoutingListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static android.R.style.Theme_Dialog;

public class RouteSelection extends AppCompatActivity implements RoutingListener,
        GoogleApiClient.OnConnectionFailedListener, SensorEventListener,
        GoogleApiClient.ConnectionCallbacks, OnStreetViewPanoramaReadyCallback, TextToSpeech.OnInitListener {
    private static final LatLngBounds BOUNDS_CROYDON = new LatLngBounds(new LatLng(51.388500900000004, -0.1205977),
            new LatLng(72.77492067739843, -9.998857788741589));
    public LatLng start;
    public RelativeLayout routehide;
    public LinearLayout stepshide;
    public RadioGroup rgViews;
    public RadioGroup rgViews_Route;
    public AlertDialog levelDialog;
    protected GoogleMap googleMap;
    protected LatLng end;
    protected GoogleApiClient mGoogleApiClient;
    @InjectView(R.id.start)
    AutoCompleteTextView starting;
    @InjectView(R.id.destination)
    AutoCompleteTextView destination;
    @InjectView(R.id.send)
    ImageView send;
    StreetViewPanoramaFragment streetViewPanoramaFragment;
    private String LOG_TAG = "MyActivity";
    private PlaceAutoCompleteAdapter mAdapter;
    private PlaceAutoCompleteAdapter dAdapter;
    private ProgressDialog progressDialog;
    private TextView speedMeter;
    private ArrayList<Polyline> polylines;
    private int[] colors = new int[]{R.color.primary_dark, R.color.primary, R.color.primary_light, R.color.accent, R.color.primary_dark_material_light};
    private TextView StepCount;
    private TextView Miles;
    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;
    private Sensor mStepDetectorSensor;
    private TextView CaloriesBurn;
    private TextToSpeech speech;
    private RelativeLayout speedDisplay;
    private TextView RouteOneDistance;
    private TextView RouteTwoDistance;
    private TextView RouteThreeDistance;
    private TextView RouteOneTime;
    private TextView RouteTwoTime;
    private TextView RouteThreeTime;
    private TextView mRouteMode;
    private RelativeLayout mRouteInfo;

    /**
     * This activity loads a googleMap and then displays the route and pushpins on it.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_selection);
        ButterKnife.inject(this);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Miles = (TextView) findViewById(R.id.distance);
        StepCount = (TextView) findViewById(R.id.StepsCount);
        CaloriesBurn = (TextView) findViewById(R.id.calories_value);
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mStepDetectorSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        rgViews = (RadioGroup) findViewById(R.id.rg_views_routeselection);
        speedMeter = (TextView) findViewById(R.id.spedo_meter);

        // calling view and relativelavyout for hide and visible
        speech = new TextToSpeech(this, this);
        routehide = (RelativeLayout) findViewById(R.id.routeselection1);
        stepshide = (LinearLayout) findViewById(R.id.routeselectionhide);
        speedDisplay = (RelativeLayout) findViewById(R.id.speedMeter);
        mRouteInfo = (RelativeLayout) findViewById(R.id.RouteInformation);
        mRouteMode = (TextView) findViewById(R.id.routeMode);

        //Route Details TextView Setup
        RouteOneTime = (TextView) findViewById(R.id.timeRoueOne);
        RouteTwoTime = (TextView) findViewById(R.id.timeRouteTwo);
        RouteThreeTime = (TextView) findViewById(R.id.timeRoueThree);

        RouteOneDistance = (TextView) findViewById(R.id.RoutOneDis);
        RouteTwoDistance = (TextView) findViewById(R.id.RoutTwoDis);
        RouteThreeDistance = (TextView) findViewById(R.id.distanceRouteThree);

        // getting map fragment to load map in this activity
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        googleMap = mapFragment.getMap();
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Getting LocationManager object from System Service LOCATION_SERVICE
        final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        final String provider = locationManager.getBestProvider(criteria, true);

        // Getting Current Location
        final Location location = locationManager.getLastKnownLocation(provider);

        if (location == null) {

            speedDisplay.setVisibility(View.GONE);


        } else {
            speedDisplay.setVisibility(View.VISIBLE);
        }

        mRouteInfo.setVisibility(View.GONE);

        stepshide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                routehide.setVisibility(View.VISIBLE);
                rgViews.setVisibility(View.VISIBLE);
                //googleMap.setMyLocationEnabled(false);
                mRouteInfo.setVisibility(View.VISIBLE);
                googleMap.getUiSettings().setZoomControlsEnabled(false);
                starting.setHint("Route Starting Point");
            }
        });

        routehide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                routehide.setVisibility(View.GONE);
                rgViews.setVisibility(View.GONE);
                mRouteInfo.setVisibility(View.GONE);
                //googleMap.setMyLocationEnabled(true);
                mRouteInfo.setVisibility(View.GONE);
                googleMap.getUiSettings().setZoomControlsEnabled(true);

            }
        });

        mRouteInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRouteInfo.setVisibility(View.GONE);
            }
        });
        rgViews.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stepshide.setVisibility(View.GONE);
            }
        });


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
        streetViewPanoramaFragment = (StreetViewPanoramaFragment) getFragmentManager()
                .findFragmentById(R.id.map_Route);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);
        streetViewPanoramaFragment.getStreetViewPanorama().setPosition(new LatLng(51.388500900000004, -0.1205977));


        polylines = new ArrayList<>();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        MapsInitializer.initialize(this);
        mGoogleApiClient.connect();

        // SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.map, mapFragment).commit();
        }
        googleMap = mapFragment.getMap();

        mAdapter = new PlaceAutoCompleteAdapter(this, android.R.layout.simple_list_item_1,
                mGoogleApiClient, BOUNDS_CROYDON, null);



        /*
        * Updates the bounds being used by the auto complete adapter based on the position of the
        * map.
        * */
        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
                mAdapter.setBounds(bounds);
            }
        });


        CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(51.388500900000004, -0.1205977));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        googleMap.moveCamera(center);
        googleMap.animateCamera(zoom);
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        final MarkerOptions markerOptions = new MarkerOptions();

        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 3000, 0,
                new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                // Sets the center of the mapGoogle to location user
                                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                .zoom(17)                   // Sets the zoom
                                .bearing(90)                // Sets the orientation of the camera to east
                                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                                .build();                   // Creates a CameraPosition from the builder

                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        markerOptions.title("Current Location");
                        markerOptions.snippet("You are here");
                        markerOptions.flat(true);
                        markerOptions.rotation(245);

                        markerOptions.position(latLng);
                        //googleMap.addMarker(markerOptions);
                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        streetViewPanoramaFragment.getStreetViewPanorama().setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                        //setting up text view format
                        DecimalFormat df = new DecimalFormat("#.##");
                        // calling street view on background

                        googleMap.isIndoorEnabled();
                        speedDisplay.setVisibility(View.VISIBLE);
                        final double speed = Double.parseDouble(String.valueOf(location.getSpeed()));
                        speedMeter.setText(df.format(speed));

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
                });


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                3000, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {


                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                // Sets the center of the mapGoogle to location user
                                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                .zoom(17)                   // Sets the zoom
                                .bearing(90)                // Sets the orientation of the camera to east
                                .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                                .build();                   // Creates a CameraPosition from the builder

                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        markerOptions.title("Current Location");
                        markerOptions.snippet("You are here");
                        markerOptions.flat(true);
                        markerOptions.rotation(245);

                        markerOptions.position(latLng);
                        //googleMap.addMarker(markerOptions);
                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        streetViewPanoramaFragment.getStreetViewPanorama().setPosition(new LatLng(location.getLatitude(), location.getLongitude()));


                        //setting up text view format
                        DecimalFormat df = new DecimalFormat("#.##");
                        // calling street view on background

                        googleMap.isIndoorEnabled();
                        speedDisplay.setVisibility(View.VISIBLE);
                        final double speed = Double.parseDouble(String.valueOf(location.getSpeed()));
                        speedMeter.setText(df.format(speed));

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
                });


        if (!(location == null)) {
            start = new LatLng(location.getLatitude(), location.getLongitude());
            starting.clearListSelection();
            starting.setHint("Current Location");

        }
        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {

            @Override
            public boolean onMyLocationButtonClick() {

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                    if (!(location == null)) {

                        start = new LatLng(location.getLatitude(), location.getLongitude());
                        starting.clearListSelection();
                        starting.setHint("Current Location");
                        Toast.makeText(RouteSelection.this, "Starting Point Set To Current Location", Toast.LENGTH_SHORT).show();

                    }

                } else {

                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                    //  Toast.makeText(RouteSelection.this, "GPS Enabled", Toast.LENGTH_SHORT).show();
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                        if (!(location == null)) {

                            start = new LatLng(location.getLatitude(), location.getLongitude());
                            starting.clearListSelection();
                            starting.setHint("Current Location");
                            Toast.makeText(RouteSelection.this, "Starting Point Set To Current Location", Toast.LENGTH_SHORT).show();

                        }
                    }
                }
                return false;
            }
        });




        /*
        * Adds auto complete adapter to both auto complete
        * text views.
        * */
        starting.setAdapter(mAdapter);
        destination.setAdapter(mAdapter);




        starting.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final PlaceAutoCompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
                final String placeId = String.valueOf(item.placeId);
                Log.i(LOG_TAG, "Autocomplete item selected: " + item.description);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (!places.getStatus().isSuccess()) {
                            // Request did not complete successfully
                            Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                            places.release();
                            return;
                        }
                        // Get the Place object from the buffer.
                        final Place place = places.get(0);

                        start = place.getLatLng();

                        start = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                        streetViewPanoramaFragment.getStreetViewPanorama().setPosition(start);

                    }
                });

            }
        });


        destination.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final PlaceAutoCompleteAdapter.PlaceAutocomplete item = mAdapter.getItem(position);
                final String placeId = String.valueOf(item.placeId);
                Log.i(LOG_TAG, "Autocomplete item selected: " + item.description);

            /*
             Issue a request to the Places Geo Data API to retrieve a Place object with additional
              details about the place.
              */
                PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                        .getPlaceById(mGoogleApiClient, placeId);
                placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (!places.getStatus().isSuccess()) {
                            // Request did not complete successfully
                            Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                            places.release();
                            return;
                        }
                        // Get the Place object from the buffer.
                        final Place place = places.get(0);

                        end = place.getLatLng();
                    }
                });

            }
        });

        /*
        These text watchers set the start and end points to null because once there's
        * a change after a value has been selected from the dropdown
        * then the value has to reselected from dropdown to get
        * the correct location.
        * */
        starting.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int startNum, int before, int count) {
                if (start != null) {
                    start = null;

                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        destination.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                if (end != null) {
                    end = null;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @OnClick(R.id.send)
    public void sendRequest() {
        if (Util.Operations.isOnline(this)) {

            routehide.setVisibility(View.GONE);
            googleMap.setMyLocationEnabled(true);
            googleMap.clear();
            mRouteInfo.setVisibility(View.VISIBLE);
            route();

        } else {
            Toast.makeText(this, "No internet connectivity", Toast.LENGTH_SHORT).show();
        }
    }

    public void route() {
        if (start == null || end == null) {


            if (start == null) {
                if (starting.getText().length() > 0) {
                    starting.setError("Choose location from dropdown.");
                } else {


                    // Toast.makeText(this,"Please choose a starting point.",Toast.LENGTH_SHORT).show();
                    speech = new TextToSpeech(RouteSelection.this, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status != TextToSpeech.ERROR) {
                                speech.setLanguage(Locale.UK);
                                String toSpeak = ("Please choose a starting and ending point");
                                speech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                Toast.makeText(RouteSelection.this, toSpeak,
                                        Toast.LENGTH_SHORT).show();
                                routehide.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }

            }
            if (end == null) {
                if (destination.getText().length() > 0) {
                    destination.setError("Choose location from dropdown.");
                } else {
                    //Toast.makeText(this,"Please choose a destination.",Toast.LENGTH_SHORT).show();
                    // Toast.makeText(this,"Please choose a starting point.",Toast.LENGTH_SHORT).show();
                    speech = new TextToSpeech(RouteSelection.this, new TextToSpeech.OnInitListener() {
                        @Override
                        public void onInit(int status) {
                            if (status != TextToSpeech.ERROR) {
                                speech.setLanguage(Locale.UK);
                                String toSpeak = ("Please choose a starting and ending point");
                                speech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
                                Toast.makeText(RouteSelection.this, toSpeak,
                                        Toast.LENGTH_SHORT).show();
                                routehide.setVisibility(View.VISIBLE);

                                routehide.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }

            }
        } else {


            final CharSequence[] items = {" Walking ", " Biking ", " Driving ", " Transit "};

            // Creating and Building the Dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    new ContextThemeWrapper(this, Theme_Dialog));
            builder.setTitle("Select Route Type");


            builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {


                    switch (item) {
                        case 0:
                            // Your code when first option seletced
                            progressDialog = ProgressDialog.show(RouteSelection.this, "Please wait.",
                                    "Fetching route information.", true);
                            Routing routing = new Routing.Builder()
                                    .travelMode(AbstractRouting.TravelMode.WALKING)
                                    .withListener(RouteSelection.this)
                                    .alternativeRoutes(true)
                                    .waypoints(start, end)
                                    .build();
                            googleMap.setTrafficEnabled(false);
                            mRouteMode.setText("Walking");
                            routing.execute();
                            break;
                        case 1:
                            // Your code when 2nd  option seletced

                            // Your code when first option seletced
                            progressDialog = ProgressDialog.show(RouteSelection.this, "Please wait.",
                                    "Fetching route information.", true);
                            routing = new Routing.Builder()
                                    .travelMode(AbstractRouting.TravelMode.BIKING)
                                    .withListener(RouteSelection.this)
                                    .alternativeRoutes(true)
                                    .waypoints(start, end)
                                    .build();
                            googleMap.setTrafficEnabled(false);
                            mRouteMode.setText("Biking");
                            routing.execute();

                            break;
                        case 2:
                            // Your code when 3rd option seletced

                            // Your code when first option seletced
                            progressDialog = ProgressDialog.show(RouteSelection.this, "Please wait.",
                                    "Fetching route information.", true);
                            routing = new Routing.Builder()
                                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                                    .withListener(RouteSelection.this)
                                    .alternativeRoutes(true)
                                    .waypoints(start, end)
                                    .build();
                            googleMap.setTrafficEnabled(true);
                            speedMeter.setVisibility(View.VISIBLE);
                            mRouteMode.setText("Driving");
                            routing.execute();

                            //stepshide.setVisibility(View.GONE);

                            break;
                        case 3:
                            // Your code when 4th  option seletced
                            // Your code when first option seletced
                            progressDialog = ProgressDialog.show(RouteSelection.this, "Please wait.",
                                    "Fetching route information.", true);
                            routing = new Routing.Builder()
                                    .travelMode(AbstractRouting.TravelMode.TRANSIT)
                                    .withListener(RouteSelection.this)
                                    .alternativeRoutes(true)
                                    .waypoints(start, end)
                                    .build();
                            googleMap.setTrafficEnabled(true);
                            mRouteMode.setText("Transit");
                            routing.execute();

                            break;

                    }
                    levelDialog.dismiss();
                }
            });
            levelDialog = builder.create();
            levelDialog.show();


        }
    }


    @Override
    public void onRoutingFailure() {
        // The Routing request failed
        progressDialog.dismiss();
        Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRoutingStart() {
        // The Routing Request starts
    }


    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        progressDialog.dismiss();


        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(16), 5000, null);
        googleMap.moveCamera(center);
        googleMap.moveCamera(zoom);

        streetViewPanoramaFragment.getStreetViewPanoramaAsync(RouteSelection.this);
        streetViewPanoramaFragment.getStreetViewPanorama().setPosition(start);


        DecimalFormat df = new DecimalFormat("#.##");

        double OneMileMatrices = 0.00062137;// One mile in Matrices
        double OneHourMatrices = 3600; // 1 hours seconds

        double RouteDistanceOne;
        double RouteDistanceTwo;
        double RouteDistanceThree;


        double RouteTimeOne;
        double RouteTimeTwo;
        double RouteTimeThree;


        // streetViewPanorama.setPosition(new LatLng(51.388500900000004, -0.1205977));
        if (polylines.size() >= 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the googleMap.
        for (int i = 0; i < route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % colors.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(colors[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = googleMap.addPolyline(polyOptions);
            polylines.add(polyline);

          /*  Toast.makeText(getApplicationContext(), "Route " + (i + 1) + ": distance - " +
                    route.get(i).getDistanceValue()
                    + ": duration - " + route.get(i).getDurationValue(), Toast.LENGTH_SHORT).show();

          */
        }

        if (route.size() < 3) {

            Toast toast = Toast.makeText(RouteSelection.this, " Please Try Another Travelling Mode ", Toast.LENGTH_LONG);
            toast.show();

        } else {

            RouteDistanceOne = Double.parseDouble(String.valueOf(route.get(0).getDistanceValue() * OneMileMatrices));
            RouteDistanceTwo = Double.parseDouble(String.valueOf(route.get(1).getDistanceValue() * OneMileMatrices));
            RouteDistanceThree = Double.parseDouble(String.valueOf(route.get(2).getDistanceValue() * OneMileMatrices));

            RouteTimeOne = Double.parseDouble(String.valueOf(route.get(0).getDurationValue() / OneHourMatrices));
            RouteTimeTwo = Double.parseDouble(String.valueOf(route.get(1).getDurationValue() / OneHourMatrices));
            RouteTimeThree = Double.parseDouble(String.valueOf(route.get(2).getDurationValue() / OneHourMatrices));

            RouteOneDistance.setText(df.format(RouteDistanceOne) + " Mile");
            RouteTwoDistance.setText(df.format(RouteDistanceTwo) + " Mile");
            RouteThreeDistance.setText(df.format(RouteDistanceThree) + " Mile");

            RouteOneTime.setText(df.format(RouteTimeOne) + " Hours");
            RouteTwoTime.setText(df.format(RouteTimeTwo) + " Hours");
            RouteThreeTime.setText(df.format(RouteTimeThree) + " Hours");


        }


        // Start marker
        MarkerOptions options = new MarkerOptions();

        options.position(start);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.start_blue));
        googleMap.addMarker(options);

        // End marker
        options = new MarkerOptions();
        options.position(end);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.end_green));
        googleMap.addMarker(options);

    }

    @Override
    public void onRoutingCancelled() {
        Log.i(LOG_TAG, "Routing was cancelled.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.v(LOG_TAG, connectionResult.toString());
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
        // Here i am not setting up the Default location because i need to change the
        // location when i want to change it so here i am leaving default location
        // so have setup the default location in side onCreate method so when application
        // runs it will display on streetView following address
        // 281 Thornton Road, Croydon,CR0 3EW
        // LatLng(51.388500900000004, -0.1205977)

    }


    // Setting up step counter and setup detector sensor for distance and calories burn calculation
    public void onSensorChanged(SensorEvent event) {


        Sensor sensor = event.sensor;
        float[] values = event.values;


        int value = -1;


        DecimalFormat df = new DecimalFormat("#.##");

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


    public void cameraPosition() {
        // Getting LocationManager object from System Service LOCATION_SERVICE
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();

        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);

        // Getting Current Location
        Location location = locationManager.getLastKnownLocation(provider);


        MarkerOptions markerOptions = new MarkerOptions();


        if (location.equals(null)) {


            location.setLatitude(51.388500900000004);
            location.setLatitude(-0.1205977);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    // Sets the center of the mapGoogle to location user
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            markerOptions.title("Current Location");
            markerOptions.snippet("You are here");
            markerOptions.flat(true);
            markerOptions.rotation(245);

            markerOptions.position(latLng);
            //googleMap.addMarker(markerOptions);
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        } else {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    // Sets the center of the mapGoogle to location user
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            markerOptions.title("Current Location");
            markerOptions.snippet("You are here");
            markerOptions.flat(true);
            markerOptions.rotation(245);
            markerOptions.getRotation();

            markerOptions.position(latLng);
            //googleMap.addMarker(markerOptions);
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


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
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Intent.ACTION_MAIN);
                                intent.addCategory(Intent.CATEGORY_HOME);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                RouteSelection.this.finish();

                            }
                        }
                )
                .setNegativeButton("No", null)
                .show();

    }

    @Override
    public void onInit(int status) {

    }


    public void setTimer() {


        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            public void run() {
                mRouteInfo.setVisibility(View.VISIBLE);
            }

        }, 1000);
    }
}
