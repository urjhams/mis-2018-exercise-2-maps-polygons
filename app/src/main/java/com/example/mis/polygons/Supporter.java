package com.example.mis.polygons;

import android.app.Activity;
import android.app.LauncherActivity;
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

    public static ArrayList<LatLng> sortedPositionFrom(final LatLng center, ArrayList<LatLng> inputList) {
        ArrayList<LatLng> resultList = inputList;
        Collections.sort(resultList, (LatLng point1, LatLng point2) -> {
            double degree1 = Math.toDegrees(Math.atan2(point1.longitude - center.longitude,point1.latitude - center.latitude) + 360) % 360;
            double degree2 = Math.toDegrees(Math.atan2(point2.longitude - center.longitude,point2.latitude - center.latitude) + 360) % 360;
            return (int) (degree1 - degree2);
        });
        return resultList ;
    }

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
//        double sigma = 0;
//        for (int index = 0; index < points.size() - 1; index ++) {
//            LatLng first = points.get(index).getPosition();
//            LatLng second = points.get(index + 1).getPosition();
//            sigma += (first.longitude * second.latitude - second.longitude * first.latitude);
//        }
//        return (sigma / 2) * 1000000;  // to square meter
    }

    public static LatLng centerOfPolygon(List<Marker> points) {
        double centerLat = 0.0;
        double centerLong = 0.0;
        for (int index = 0; index < points.size(); index ++) {
            centerLat += points.get(index).getPosition().latitude;
            centerLong += points.get(index).getPosition().longitude;
        }

        for (int index = 0 ; index < points.size(); index ++) {

        }



        return new LatLng(centerLat / points.size(), centerLong / points.size());
    }

    public static boolean availablePolygon(ArrayList<LatLng> list) {    // alwasy have from 4 points
        LatLng first = list.get(0);
        int limitIndex = list.size() - 1;

        // get the array of lines
        ArrayList<Line> lines = new ArrayList<>();
        for (int index = 0; index <= limitIndex; index++) {
            LatLng current = list.get(index);
            if (index != limitIndex) {
                LatLng next = list.get(index + 1);
                lines.add(new Line(current, next));
            }
            lines.add(new Line(current, first));
        }

        //fetch the line to find intersect couple
        for (int index = 0; index < lines.size(); index ++) {
            Line current = lines.get(index);
            for (int anotherIndex = (index + 1); anotherIndex < lines.size(); anotherIndex ++) {
                if (!isInTouch(current,lines.get(anotherIndex)) && isIntersect(current,lines.get(anotherIndex))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isIntersect(Line first, Line second) {
        LatLng firstSubtract = substractPoints(first.start,first.end);
        LatLng secondSubtract = substractPoints(second.start,second.end);

        double uNumerator = crossProduct(substractPoints(second.end,first.end),firstSubtract);
        double denominator = crossProduct(firstSubtract,secondSubtract);

        if (denominator == 0) {
            return false;           //paralell
        }

        double u = uNumerator / denominator;
        double t = crossProduct(substractPoints(second.end,first.end),secondSubtract) / denominator;

        return (t >=0) && (t <= 1) && (u >= 0) && (u <= 1);
    }

    private static boolean isInTouch(Line first, Line second) {
        LatLng firstStart = first.start;
        LatLng firstEnd = first.end;
        LatLng secondStart = second.start;
        LatLng secondEnd = second.end;
        return (firstEnd == secondStart ||
                secondEnd == firstStart ||
                firstStart == secondStart ||
                firstEnd == secondEnd);
    }

    static class Line {
        LatLng start;
        LatLng end;
        double x;
        double y;
        public Line(LatLng start, LatLng end) {
            this.start = start;
            this.end = end;

        }
    }

    private static LatLng substractPoints(LatLng first, LatLng second) {
        double lat = first.latitude - second.longitude;
        double longt = first.longitude - second.longitude;
        return new LatLng(lat,longt);
    }

    private static double crossProduct(LatLng first, LatLng second) {
        return first.longitude * second.latitude - first.latitude * second.longitude;
    }

}


