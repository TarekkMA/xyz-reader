package com.example.xyzreader;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Created by TarekLMA on 7/9/17.
 * tarekkma@gmail.com
 */

public class Utils {
  public static boolean isOnline(Context context){
    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(CONNECTIVITY_SERVICE);
    NetworkInfo ni = cm.getActiveNetworkInfo();
    return !(ni == null || !ni.isConnected());
  }
}
