package com.bushbungalo.weatherlion.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.services.WidgetUpdateService;
import com.bushbungalo.weatherlion.utils.UtilityMethod;

/**
 * @author Paul O. Patterson
 * Created on 12/26/17.
 */

public class UpdateAlarmBroadcastReceiver extends BroadcastReceiver
{
    public static final String ACTION_ALARM = "UpdateAlarm";

    @Override
    public void onReceive( Context context, Intent intent )
    {
        if ( ACTION_ALARM.equals( intent.getAction() ) )
        {
            if( UtilityMethod.hasInternetConnection( WeatherLionApplication.getAppContext() ) )
            {
                UtilityMethod.refreshRequestedBySystem = true;

                String invoker = this.getClass().getSimpleName() + "::onReceive";
                WeatherLionApplication.callMethodByName( null,
                        "refreshWeather",
                        new Class[]{ String.class }, new Object[]{ invoker } );

                UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                        "Update requested by the alarm broadcast receiver",
                        invoker );
            }// end of if block
        }// end of if block
    }// end of method onReceive
}// end of class UpdateAlarmBroadcastReceiver