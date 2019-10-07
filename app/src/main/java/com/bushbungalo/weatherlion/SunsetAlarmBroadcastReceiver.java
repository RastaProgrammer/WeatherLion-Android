package com.bushbungalo.weatherlion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

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

            Intent methodIntent = new Intent( context, WidgetUpdateService.class );
            methodIntent.setData( Uri.parse( WeatherLionApplication.UNIT_NOT_CHANGED ) );
            methodIntent.putExtra( WeatherLionApplication.LAUNCH_METHOD_EXTRA,
                    WidgetUpdateService.ASTRONOMY_CHANGE );
            WidgetUpdateService.enqueueWork( context, methodIntent );

            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                    "Sunset time...",
                    "SunsetAlarmBroadcastReceiver::onReceive" );
        }// end of if block
    }// end of method onReceive
}// end of class SunsetAlarmBroadcastReceiver