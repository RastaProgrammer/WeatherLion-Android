package com.bushbungalo.weatherlion.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.services.WidgetUpdateService;
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
                UtilityMethod.refreshRequested = true;

                String invoker = this.getClass().getSimpleName() + "::onReceive";
                Bundle extras = new Bundle();
                extras.putString( WidgetUpdateService.WEATHER_SERVICE_INVOKER, invoker );
                extras.putString( WeatherLionApplication.LAUNCH_METHOD_EXTRA, null );
                extras.putString( WidgetUpdateService.WEATHER_DATA_UNIT_CHANGED,
                        WeatherLionApplication.UNIT_NOT_CHANGED );

                Intent updateIntent = new Intent( context, WidgetUpdateService.class );
                updateIntent.putExtras( extras );
                WidgetUpdateService.enqueueWork( context, updateIntent );

                UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                        "Update requested by the alarm broadcast receiver",
                        this.getClass().getSimpleName() + "::onReceive" );
            }// end of if block
        }// end of if block
    }// end of method onReceive
}// end of class AlarmBroadcastReceiver