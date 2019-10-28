package com.bushbungalo.weatherlion.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.services.WidgetUpdateService;
import com.bushbungalo.weatherlion.utils.UtilityMethod;

public class SunsetAlarmBroadcastReceiver extends BroadcastReceiver
{
    public static final String ACTION_ALARM = "SunsetAlarm";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if ( ACTION_ALARM.equals( intent.getAction() ) )
        {
            WeatherLionApplication.timeOfDayToUse = WidgetUpdateService.SUNSET;

            String invoker = this.getClass().getSimpleName() + "::onReceive";
            Bundle extras = new Bundle();
            extras.putString( WidgetUpdateService.WEATHER_SERVICE_INVOKER, invoker );
            extras.putString( WeatherLionApplication.LAUNCH_METHOD_EXTRA,
                    WidgetUpdateService.ASTRONOMY_CHANGE );
            extras.putString( WidgetUpdateService.WEATHER_DATA_UNIT_CHANGED,
                    WeatherLionApplication.UNIT_NOT_CHANGED );

            Intent methodIntent = new Intent( context, WidgetUpdateService.class );
            methodIntent.putExtras( extras );
            WidgetUpdateService.enqueueWork( context, methodIntent );

            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                    "Sunset time...",
                    "SunsetAlarmBroadcastReceiver::onReceive" );
        }// end of if block
    }// end of method onReceive
}// end of class SunsetAlarmBroadcastReceiver