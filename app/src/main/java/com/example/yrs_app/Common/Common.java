package com.example.yrs_app.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;

import com.example.yrs_app.Model.Request;
import com.example.yrs_app.Model.User;

public class Common {
    public static User currentUser;
    public static Request currentRequest;

    public static String convertCodeToStatus(String status) {
        if(status.equals("0"))
            return "Placed";
        else if(status.equals("1"))
            return "On the way";
        else  if (status.equals("2"))
            return "Shipping";
        else
            return "Shipped";
    }

    public static final String DELETE = "Delete";
    public static final String USER_KEY = "User";         // used to remember user (phone)
    public static final String PWD_KEY = "Password";     // used to remember user (password)
    public static final String INTENT_FOOD_ID = "FoodId";

    // פונקציה לבדיקת הרשת עבור המכשיר
    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null)
        {
            NetworkInfo [] info = connectivityManager.getAllNetworkInfo();
            if(info != null)
            {
                for(int i = 0; i < info.length; i++)
                {
                    if(info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }
}
