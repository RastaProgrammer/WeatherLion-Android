package com.bushbungalo.weatherlion.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.utils.UtilityMethod;

/**
 * @author Paul O. Patterson
 * Created on 12/26/17.
 */

public class AlarmBroadcastReceiver extends BroadcastReceiver
{
    public static final String ACTION_ALARM = "com.bushbungalo.weatherlion.alarms.ACTION_ALARM";

    @Override
    public void onReceive( Context context, Intent intent )
    {
        if ( ACTION_ALARM.equals( intent.getAction() ) )
        {
            if( UtilityMethod.hasInternetConnection( WeatherLionApplication.getAppContext() ) )
            {
                UtilityMethod.refreshRequestedBySystem = true;
                UtilityMethod.refreshRequestedByUser = false;

                String invoker = this.getClass().getSimpleName() + "::onReceive";
                WeatherLionApplication.callMethodByName( null,
                        "refreshWeather",
                        new Class[]{ String.class }, new Object[]{ invoker }, invoker );
            }// end of if block
        }// end of if block
    }// end of method onReceive
}// end of class AlarmBroadcastReceiver