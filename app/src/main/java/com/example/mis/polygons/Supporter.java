package com.example.mis.polygons;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.SphericalUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Supporter {
    private static DecimalFormat twoDecimalDouble = new DecimalFormat(".##");
    public static void makeToast(String content, Context context) {
        Toast toast = Toast.makeText(context,content,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
    }

    public static void hideKeyboardOf(EditText textField, Activity activity) {
        InputMethodManager inputMng =
                (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            inputMng.hideSoftInputFromWindow(textField.getWindowToken(), 0);
        } catch (NullPointerException ex) {

        }
    }

    public static String firstStringFrom(String markerContent) {
        return markerContent.split("\\r?\\n")[0];
    }

    public static double changeToKmFrom(double meter) {
        return meter / 1000;
    }

    public static DecimalFormat getTwoDecimalDouble() {
        return twoDecimalDouble;
    }

//    public static ArrayList<LatLng> sortedPositionFrom(final LatLng center, ArrayList<LatLng> inputList) {
//        ArrayList<LatLng> resultList = inputList;
//        Collections.sort(resultList, (LatLng point1, LatLng point2) -> {
//            double degree1 = Math.toDegrees(Math.atan2(point1.longitude - center.longitude,point1.latitude - center.latitude) + 360) % 360;
//            double degree2 = Math.toDegrees(Math.atan2(point2.longitude - center.longitude,point2.latitude - center.latitude) + 360) % 360;
//            return (int) (degree1 - degree2);
//        });
//        return resultList ;
//    }

    public static Location defaultLocation() {
        Location defaultLocation = new Location("Weimar, Thuringia, Germany");
        defaultLocation.setLatitude(50.979492);
        defaultLocation.setLongitude(11.323544);
        return defaultLocation;
    }

    //----------- http://googlemaps.github.io/android-maps-utils/javadoc/com/google/maps/android/SphericalUtil.html
    // in gradle: 'com.google.maps.android:android-maps-utils:0.5+'
    public static double areaOfPolygon(List<Marker> points) {
        List<LatLng> positionList = new ArrayList<>();
        for (int index = 0; index < points.size(); index++) {
            Marker mark = points.get(index);
            positionList.add(mark.getPosition());
        }
        return SphericalUtil.computeArea(positionList); //square meter
    }

    public static LatLng centerOfPolygon(List<Marker> points) {
        double centerLat = 0.0;
        double centerLong = 0.0;
        for (int index = 0; index < points.size(); index ++) {
            centerLat += points.get(index).getPosition().latitude;
            centerLong += points.get(index).getPosition().longitude;
        }
        return new LatLng(centerLat / points.size(), centerLong / points.size());
    }
}
