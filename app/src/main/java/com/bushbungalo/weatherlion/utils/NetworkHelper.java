package com.bushbungalo.weatherlion.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Paul O. Patterson on 11/16/17.
 */

@SuppressWarnings("WeakerAccess")
public class NetworkHelper
{
    public static boolean hasNetworkAccess( Context context )
    {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );

        try
        {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            return activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        }// end of try block
        catch ( Exception e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    "NetworkHelper::hasNetworkAccess" );

            return false;
        }// end of catch block

    }// end of method hasNetworkAccess
}// end of class NetworkHelper
