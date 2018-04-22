package com.example.mis.polygons;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

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
        //TODO: make sorting through clockwise
        ArrayList<LatLng> resultList = inputList;
//        Comparator<LatLng> comparator = new Comparator<LatLng>() {
//            @Override
//            public int compare(LatLng o1, LatLng o2) {
//                double degree1 = (Math.toDegrees(Math.atan2(o1.latitude * 1000 - center.latitude * 1000, o1.longitude * 1000 - center.longitude * 1000)) + 360) % 360;
//                double degree2 = (Math.toDegrees(Math.atan2(o1.latitude * 1000 - center.latitude * 1000, o1.longitude * 1000 - center.longitude * 1000)) + 360) % 360;
//                return Integer.compare((int) (degree1 * 1000000),(int) (degree2 * 1000000));
//            }
//        };
        Collections.sort(resultList, (LatLng point1, LatLng point2) -> {
            double degree1 = Math.toDegrees(Math.atan2(point1.longitude - center.longitude,point1.latitude - center.latitude) + 360) % 360;
            double degree2 = Math.toDegrees(Math.atan2(point2.longitude - center.longitude,point2.latitude - center.latitude) + 360) % 360;
            return (int) (degree1 - degree2);
        });
        return resultList ;
    }
}
