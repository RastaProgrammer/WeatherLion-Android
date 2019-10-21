package com.bushbungalo.weatherlion.alarms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

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
                UtilityMethod.refreshRequested = true;

                Intent updateIntent = new Intent( context, WidgetUpdateService.class );
                updateIntent.setData( Uri.parse( WeatherLionApplication.UNIT_NOT_CHANGED ) );
                WidgetUpdateService.enqueueWork( context, updateIntent );

                UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                        "Update requested by the alarm broadcast receiver",
                        "UpdateAlarmBroadcastReceiver::onReceive" );
            }// end of if block
        }// end of if block
    }// end of method onReceive
}// end of class UpdateAlarmBroadcastReceiver