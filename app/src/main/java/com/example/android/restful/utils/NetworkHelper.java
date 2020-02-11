package com.example.android.restful.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;


public class NetworkHelper extends ConnectivityManager.NetworkCallback {

    private static final String TAG = "NetworkHelper";

    //Check that app is connected to network
    public static boolean hasNetworkAccess(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = null;
        try {
            if (cm != null) {
                activeNetwork = cm.getActiveNetworkInfo();
            }
            return activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
