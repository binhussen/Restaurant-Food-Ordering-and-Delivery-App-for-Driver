package com.group_7.mhd.driver.Common;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.database.FirebaseDatabase;
import com.group_7.mhd.driver.Model.Request;
import com.group_7.mhd.driver.Model.Shipper;
import com.group_7.mhd.driver.Model.ShippingInformation;
import com.group_7.mhd.driver.Remote.IGeoCoordinates;
import com.group_7.mhd.driver.Remote.RetrofitClient;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Common {
    public static final String SHIPPERS_TABLE = "Drivers";
    public static final String ORDER_NEED_SHIPPERS_TABLE = "Delivering";
    public static final String SHIPPERS_INFO_TABLE = "Delivered";
    public static final String ORDER_TABLE = "Order";

    public static final int REQUEST_CODE =1000;

    public static Request currentRequest;
    public static Shipper currentShipper;
    public static String currentKey;

    public static String customer_addresslat = "";
    public static String customer_addresslon = "";

    public final static String USER_KEY = "USER_KEY";
    public final static String PWD_KEY = "PWD_KEY";

    private static final String BASE_URL = "https://fcm.googleapis.com/";

    public static String convertCodeToStatus(String status) {
        if (status.equals("0")) {
            return "Placed";
        } else if (status.equals("1")) {
            return "Payed";
        } else if (status.equals("2")) {
            return "Cooked";
        } else if (status.equals("3")) {
            return "Delivering";
        } else if (status.equals("4")){
            return "Delivered";
        } else{
            return "Cash On Delivery";
        }
    }

    public static String getDate(Long time)
    {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        StringBuilder date = new StringBuilder(
                android.text.format.DateFormat.format("dd-MM-yyyy-HH:mm"
                        ,calendar)
                        .toString());
        return date.toString();
    }

    public static IGeoCoordinates getGeoCodeService()
    {
        return RetrofitClient.getClient(BASE_URL).create(IGeoCoordinates.class);

    }
    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight) {

        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth,newHeight,Bitmap.Config.ARGB_8888);

        float scaleX = newWidth/(float)bitmap.getWidth();
        float scaleY = newHeight/(float)bitmap.getHeight();
        float pivotX=0,pivotY=0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX,scaleY,pivotX,pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap,0,0,new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }

    public static void createShipperOrder(String key, String phone, Location mLastLocation) {

        ShippingInformation shippingInformation = new ShippingInformation();
        shippingInformation.setOrderId(key);
        shippingInformation.setShipperPhone(phone);
        shippingInformation.setLat(mLastLocation.getLatitude());
        shippingInformation.setLng(mLastLocation.getLongitude());

        //crete new item on shipper table
        FirebaseDatabase.getInstance()
                .getReference(SHIPPERS_INFO_TABLE)
                .child(key)
                .setValue(shippingInformation)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ERROR",e.getMessage());
                    }
                });
    }

    public static void updateShippingInformation(String currentKey, Location mLastLocation) {

        Map<String, Object> update_loaction = new HashMap<>();
        update_loaction.put("lat",mLastLocation.getLatitude());
        update_loaction.put("lng",mLastLocation.getLongitude());

        FirebaseDatabase.getInstance()
                .getReference(SHIPPERS_INFO_TABLE)
                .child(currentKey)
                .updateChildren(update_loaction)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("ERROR",e.getMessage());
                    }
                });
    }
    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
            /*assert connectivityManager != null;
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();*/
        }
        return false;
    }
}

