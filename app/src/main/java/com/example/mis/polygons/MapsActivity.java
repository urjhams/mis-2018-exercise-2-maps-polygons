package com.example.mis.polygons;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        if (isGrantedPermission()) {

        } else {
            askForLocationPermission();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setBuildingsEnabled(true);
        this.setMarkerOf(deviceCurrentLocation(),"You are here my friend", mMap);
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

    private void setMarkerOf(Location location, String title, GoogleMap map) {
        LatLng currentLatlng = new LatLng(location.getLatitude(), location.getLongitude());
        map.addMarker(new MarkerOptions().position(currentLatlng).title(title));
        //move camera to new marked with zoom level 15
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatlng, 15.0f));
        //zoom in to map til level 12
        map.animateCamera(CameraUpdateFactory.zoomIn());
        //map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatlng,12.0f));
        map.animateCamera(CameraUpdateFactory.zoomTo(12.0f),2000,null);
    }

    private Location deviceCurrentLocation() {
        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Location defaultLocation = new Location("Weimar, Thuringia, Germany");
            defaultLocation.setLatitude(50.979492);
            defaultLocation.setLongitude(11.323544);
            return defaultLocation;
        } else {
            //get current latitude and longitude
            return mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
    }

    private void makeToast(String content, Context context) {
        Toast toast = Toast.makeText(context,content,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_CONTACTS) {
            switch (requestCode) {
                case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                        // permission granted
                        Location current = deviceCurrentLocation();
                        this.setMarkerOf(current,"You are here my friend", mMap);
                    } else {
                        // not granted - permission denied
                        this.makeToast("Cannot located device, set default place to Weimar",this);
                    }
                    return;
                }
            }
        }
    }
}
