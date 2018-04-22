package com.example.mis.polygons;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapsActivity extends FragmentActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowLongClickListener{

    private GoogleMap mMap;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private static String pref_name;
    private static String pref_key;
    private EditText textField;
    private Button polygonButton;
    private SharedPreferences sharedPref_OldContents;
    private Set<String> contentsArraySet;
    private ArrayList<Marker> storedMarker;
    private Polygon userPolygon;
    private Marker polygonMarker;

    //-------------------------------------- overriding functions ----------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        textField = findViewById(R.id.nameTextEdit);
        polygonButton = findViewById(R.id.polygonButton);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (!isGrantedPermission()) {
            askForLocationPermission();
        }

        pref_key = getString(R.string.marker_pref_key);
        pref_name = getString(R.string.marker_pref_name);

        sharedPref_OldContents = MapsActivity.this.
                getSharedPreferences(pref_name, Context.MODE_PRIVATE);

        contentsArraySet = contentsOfOldMarks(pref_key);
        storedMarker = new ArrayList<Marker>();
        polygonButton.setTag(0);

        Supporter.makeToast(
                "- Put String and hold on Map to mark\n- Hold on info windows of each marker for delete it",
                this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {   //in here, the map has been already initialized
        mMap = googleMap;
        this.initLocateOf(deviceCurrentLocation(),"Current location", mMap);

        for (String text : contentsArraySet) {
            String[] content = getMarkValueFrom(text);
            if (content.length >= 3) {
                LatLng locate = new LatLng(Double.parseDouble(content[1]),Double.parseDouble(content[2]));
                makeMarkerOf(locate,content[0],mMap);
            }
        }

        //add listener:
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //dismiss keyboard
                Supporter.hideKeyboardOf(textField, MapsActivity.this);
            }
        });
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                //dismiss keyboard
                Supporter.hideKeyboardOf(textField, MapsActivity.this);
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if ((Integer) polygonButton.getTag() == 1) {
                    Supporter.makeToast("Please stop the polygon first",MapsActivity.this);
                    return;
                }

                String[] refer = contentsArraySet.toArray(new String[contentsArraySet.size()]);
                for (int index = 0; index < refer.length; index ++) {

                    String first = Supporter.firstStringFrom(refer[index]);
                    String second = textField.getText().toString();

                    if (first.equals(second)) {
                        Supporter.makeToast("Please choose different name",MapsActivity.this);
                        return;
                    }
                }

                String text = textField.getText().toString();
                if (text.isEmpty()) {
                    Supporter.makeToast("You must set a message",MapsActivity.this);
                } else {
                    makeMarkerOf(latLng,text,mMap);
                    textField.setText("");
                    //add element to content Array, and then save to shared preferences
                    saveContentValue(text,latLng,pref_key);
                }
                //dismiss keyboard
                Supporter.hideKeyboardOf(textField, MapsActivity.this);
            }
        });
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowLongClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Supporter.hideKeyboardOf(textField, MapsActivity.this);
        return false;
    }

    @Override
    public void onInfoWindowLongClick(final Marker marker) {
        if ((Integer) polygonButton.getTag() == 1) {
            Supporter.makeToast("Please end the polygon first",this);
            return;
        }
        AlertDialog.Builder builder = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) ?
                new AlertDialog.Builder(this,R.style.Theme_AppCompat_DayNight_Dialog_Alert) :
                new AlertDialog.Builder(this);
        builder.setTitle("Confirm").setMessage("Are you sure to delete this marker?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                marker.remove();

                String[] refer = contentsArraySet.toArray(new String[contentsArraySet.size()]);
                for (int index = 0; index < refer.length; index ++) {

                    String first = Supporter.firstStringFrom(refer[index]);
                    String second = marker.getTitle();

                    if (first.equals(second)) {
                        Object obj = refer[index];
                        contentsArraySet.remove(obj);
                    }
                }

                Marker[] referMarker = storedMarker.toArray(new Marker[storedMarker.size()]);
                //remove latlng array
                for (int index = 0; index < referMarker.length; index ++) {
                    if (referMarker[index].equals(marker)) {
                        storedMarker.remove(referMarker[index]);
                    }
                }

                saveCurrentMarks(pref_key);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // no nothing
            }
        });
        builder.setIcon(R.drawable.common_google_signin_btn_icon_disabled).show();
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
                        Supporter.makeToast("Cannot located device, set default place to Weimar",this);
                    }
                    return;
                }
            }
        }
    }

    //-------------------------------------- custom functions --------------------------------------

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
        Marker marker = map.addMarker(new MarkerOptions().
                position(location).
                title(title).
                snippet("Latitude: " +
                        Supporter.getTwoDecimalDouble().format(location.latitude) +
                        ", Longitude: " +
                        Supporter.getTwoDecimalDouble().format(location.longitude)
                ));
        System.out.println(title);
        storedMarker.add(marker);
    }

    @NonNull
    private LatLng deviceCurrentLocation() {
        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            //set the default location is Weimar
            Location defaultLocation = new Location("Weimar, Thuringia, Germany");
            defaultLocation.setLatitude(50.979492);
            defaultLocation.setLongitude(11.323544);

            return new LatLng(defaultLocation.getLatitude(),defaultLocation.getLongitude());
        } else {
            //get current latitude and longitude
            Location currentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (currentLocation == null) {
                currentLocation = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            return new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
        }
    }

    private Set<String> contentsOfOldMarks(String key) {
        return this.sharedPref_OldContents.getStringSet(key,new HashSet<String>());
    }

    private void saveContentValue(String mess, LatLng latLng, String key) {
        SharedPreferences.Editor editor = this.sharedPref_OldContents.edit();
        String content = mess + "\n" +
                String.valueOf(latLng.latitude) + "\n" +
                String.valueOf(latLng.longitude);
        editor.remove(key);
        editor.apply();
        contentsArraySet.add(content);
        editor.putStringSet(key,contentsArraySet);
        editor.apply();
    }

    private void saveCurrentMarks(String key) {
        SharedPreferences.Editor editor = this.sharedPref_OldContents.edit();
        editor.remove(key);
        editor.apply();
        editor.putStringSet(key,contentsArraySet);
        editor.apply();
    }

    private String[] getMarkValueFrom(String value) {
        return value.split("\\r?\\n");
    }

    private boolean initPolygon(GoogleMap map) {
        ArrayList<LatLng> positions = new ArrayList<>();
        for (Marker marker : storedMarker) {
            positions.add(marker.getPosition());
        }

        //sort the list (may be by clockwise)
        LatLng[] list = new LatLng[positions.size()];
        LatLng centerPoint = centerOfPolygon(storedMarker);
        System.out.println(positions);
        list = Supporter.sortedPositionFrom(centerPoint,positions).toArray(list);
        System.out.println(list);
        if (list.length > 2) {
            PolygonOptions polygonOpt =
                    new PolygonOptions().
                            add(list).
                            strokeColor(Color.argb(10,192,192,192)).
                            fillColor(Color.argb(160,192,192,192));
            userPolygon =  map.addPolygon(polygonOpt);
            double areaPolygon = areaOfPolygon(storedMarker);
            polygonMarker = map.addMarker(new MarkerOptions().
                    title("Area").
                    position(centerPoint).
                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)).
                    snippet("" +
                            ((areaPolygon >= 1000) ?
                                    Supporter.getTwoDecimalDouble().format(Supporter.changeToKmFrom(areaPolygon)) +
                                            " km\u00b2" :
                                    Supporter.getTwoDecimalDouble().format(areaPolygon) +
                                            " m\u00b2"))
            );
            return true;
        }
        return false;
    }

    public void clickPolygon(View sender) {
        final int status = (Integer) sender.getTag();
        if (status == 0) {
            if (initPolygon(mMap)) {
                sender.setTag(1);
                Button self = (Button) sender;
                self.setText("End Polygon");
            }
        } else {
            sender.setTag(0);
            userPolygon.remove();
            if (polygonMarker != null) {
                polygonMarker.remove();
            }
            Button self = (Button) sender;
            self.setText("Start Polygon");
        }
    }

    //----------- http://googlemaps.github.io/android-maps-utils/javadoc/com/google/maps/android/SphericalUtil.html
    // in gradle: 'com.google.maps.android:android-maps-utils:0.5+'
    private double areaOfPolygon(List<Marker> points) {
        List<LatLng> positionList = new ArrayList<>();
        for (int index = 0; index < points.size(); index++) {
            Marker mark = points.get(index);
            positionList.add(mark.getPosition());
        }
        return SphericalUtil.computeArea(positionList); //square meter
    }

    private LatLng centerOfPolygon(List<Marker> points) {
        double centerLat = 0.0;
        double centerLong = 0.0;
        for (int index = 0; index < points.size(); index ++) {
            centerLat += points.get(index).getPosition().latitude;
            centerLong += points.get(index).getPosition().longitude;
        }
        return new LatLng(centerLat / points.size(), centerLong / points.size());
    }
}
