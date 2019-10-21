package com.bushbungalo.weatherlion.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.services.WidgetUpdateService;
import com.bushbungalo.weatherlion.utils.UtilityMethod;

public class SunriseAlarmBroadcastReceiver extends BroadcastReceiver
{
    public static final String ACTION_ALARM = "SunriseAlarm";

    @Override
    public void onReceive( Context context, Intent intent )
    {
        if ( ACTION_ALARM.equals( intent.getAction() ) )
        {
            WeatherLionApplication.timeOfDayToUse = WidgetUpdateService.SUNRISE;

            Intent methodIntent = new Intent( context, WidgetUpdateService.class );
            methodIntent.setData( Uri.parse( WeatherLionApplication.UNIT_NOT_CHANGED ) );
            methodIntent.putExtra( WeatherLionApplication.LAUNCH_METHOD_EXTRA,
                    WidgetUpdateService.ASTRONOMY_CHANGE );
            WidgetUpdateService.enqueueWork( context, methodIntent );

            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                    "Sunrise time...",
                    "SunriseAlarmBroadcastReceiver::onReceive" );
        }// end of if block
    }// end of method
}// end of class SunriseAlarmBroadcastReceiver
