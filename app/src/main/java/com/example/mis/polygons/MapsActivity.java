package com.example.mis.polygons;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback{

    private GoogleMap mMap;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static DecimalFormat twoDecimalDouble = new DecimalFormat(".##");
    private String saveMarkNumberFile;
    private String saveMArkContentFile;
    private String saveMarkNumberKey;
    private EditText textField;
    private int numberOfOldMarked;
    private  SharedPreferences sharedPref_numberOfMarks;
    private SharedPreferences sharedPref_contentOfMarks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        textField = findViewById(R.id.nameTextEdit);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (!isGrantedPermission()) {
            askForLocationPermission();
        }

        saveMarkNumberFile = this.getString(R.string.marker_number_file);
        saveMArkContentFile = this.getString(R.string.marker_file);
        saveMarkNumberKey = this.getString(R.string.marker_number_key);

        // get the shared preferences which store old marks value and number of old marks
        sharedPref_numberOfMarks = MapsActivity.this.
                getSharedPreferences(saveMarkNumberFile, Context.MODE_PRIVATE);
        sharedPref_contentOfMarks = MapsActivity.this.
                getSharedPreferences(saveMArkContentFile, Context.MODE_PRIVATE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {   //in here, the map has been already initialized
        mMap = googleMap;
        mMap.setBuildingsEnabled(true);
        this.initLocateOf(deviceCurrentLocation(),"Current location", mMap);

        //get old values, if doesn't have yet, its equal 0
        numberOfOldMarked = numberOfOldMarked(saveMarkNumberKey);
        //get all marked contents
        if (numberOfOldMarked > 0) {    //means it has old marked
            for (int key = 1; key <= numberOfOldMarked; key++) {
                //markedContentsArray.add(contentOfOldMarked(key));
                String[] content = getMarkValueFrom(contentOfOldMarked(key));
                if (content.length >= 3) {
                    LatLng locate = new LatLng(Double.parseDouble(content[1]),Double.parseDouble(content[2]));
                    makeMarkerOf(locate,content[0],mMap);
                }
            }
        }

        //add listener:
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //dismiss keyboard
                InputMethodManager inputMng =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMng.hideSoftInputFromWindow(textField.getWindowToken(), 0);
            }
        });

        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                //dismiss keyboard
                InputMethodManager inputMng =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMng.hideSoftInputFromWindow(textField.getWindowToken(), 0);
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                String text = textField.getText().toString();
                if (text.isEmpty()) {
                    makeToast("You must set a message",MapsActivity.this);
                } else {
                    makeMarkerOf(latLng,text,mMap);
                    textField.setText("");
                    numberOfOldMarked++;

                    //save key value
                    saveKeyValue(saveMarkNumberKey,numberOfOldMarked);

                    //save content value
                    saveContentValue(text,
                            latLng,
                            String.valueOf(numberOfOldMarked));
                }

                //dismiss keyboard
                InputMethodManager inputMng = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMng.hideSoftInputFromWindow(textField.getWindowToken(), 0);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                //dismiss keyboard
                InputMethodManager inputMng = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMng.hideSoftInputFromWindow(textField.getWindowToken(), 0);

                return false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            switch (requestCode) {
                case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                        // permission granted
                        LatLng current = deviceCurrentLocation();
                        this.initLocateOf(current,"Current location", mMap);

                    } else {
                        // not granted - permission denied
                        this.makeToast("Cannot located device, set default place to Weimar",this);
                    }
                    return;
                }
            }
        }
    }

    private boolean isGrantedPermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED);
    }

    private void askForLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_READ_CONTACTS);
    }

    private void initLocateOf(LatLng location, String title, GoogleMap map) {
        //move camera to new marked with zoom level 12
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12.0f));
        //zoom in to map til level 16 in 2 seconds
        map.animateCamera(CameraUpdateFactory.zoomIn());
        map.animateCamera(CameraUpdateFactory.zoomTo(16.0f),2000,null);
    }

    private void makeMarkerOf(LatLng location, String title, GoogleMap map) {
        map.addMarker(new MarkerOptions().
                position(location).
                title(title).
                snippet("Latitude: " +
                        twoDecimalDouble.format(location.latitude) +
                        ", Longitude: " +
                        twoDecimalDouble.format(location.longitude))
        );
    }

    private LatLng deviceCurrentLocation() {
        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // NO_NEED_TO_DO: Consider calling (cause already handled bellow)
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

            //set the default location is Weimar
            Location defaultLocation = new Location("Weimar, Thuringia, Germany");
            defaultLocation.setLatitude(50.979492);
            defaultLocation.setLongitude(11.323544);

            return new LatLng(defaultLocation.getLatitude(),defaultLocation.getLongitude());
        } else {
            //get current latitude and longitude
            Location currentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            return new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        }
    }

    private void makeToast(String content, Context context) {
        Toast toast = Toast.makeText(context,content,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }

    private int numberOfOldMarked(String key) {
        return this.sharedPref_numberOfMarks.getInt(key,0);
    }

    private String contentOfOldMarked(int key) {
        return this.sharedPref_contentOfMarks.getString(String.valueOf(key),"DEFAULT");
    }

    private void saveKeyValue(String key, int value) {
        SharedPreferences.Editor editor = this.sharedPref_numberOfMarks.edit();
        editor.putInt(key,value);
        editor.apply();
    }

    private void saveContentValue(String mess, LatLng latLng, String key) {
        SharedPreferences.Editor editor = this.sharedPref_contentOfMarks.edit();
        editor.putString(key, mess + "\n" +
                String.valueOf(latLng.latitude) + "\n" +
                String.valueOf(latLng.longitude));
        editor.apply();
    }

    private String[] getMarkValueFrom(String value) {
        return value.split("\\r?\\n");
    }


}
