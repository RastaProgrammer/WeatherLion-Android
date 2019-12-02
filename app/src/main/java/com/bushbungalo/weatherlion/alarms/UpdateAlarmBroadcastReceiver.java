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
            String invoker = this.getClass().getSimpleName() + "::onReceive";

            if( UtilityMethod.hasInternetConnection( WeatherLionApplication.getAppContext() ) )
            {
                UtilityMethod.refreshRequestedBySystem = true;
                UtilityMethod.refreshRequestedByUser = false;

                // avoid bottle neck requests
                if( UtilityMethod.updateRequired( context ) )
                {
                    WeatherLionApplication.callMethodByName( null,
            "refreshWeather", new Class[]{ String.class }, new Object[]{ invoker } );
                }// end of if block
            }// end of if block
            else
            {
                Bundle extras = new Bundle();
                extras.putString( WidgetUpdateService.WEATHER_SERVICE_INVOKER, invoker );
                extras.putString( WeatherLionApplication.LAUNCH_METHOD_EXTRA,
                        WidgetUpdateService.LOAD_PREVIOUS_WEATHER );
                extras.putString( WidgetUpdateService.WEATHER_DATA_UNIT_CHANGED,
                        WeatherLionApplication.UNIT_NOT_CHANGED );

               // reload the previous weather data since there is no internet connection
                Intent methodIntent = new Intent( context, WidgetUpdateService.class );
                methodIntent.putExtras( extras );
                WidgetUpdateService.enqueueWork( context, methodIntent );
            }// end of else block
        }// end of if block
    }// end of method onReceive
}// end of class UpdateAlarmBroadcastReceiver