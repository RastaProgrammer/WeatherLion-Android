package com.bushbungalo.weatherlion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bushbungalo.weatherlion.services.WidgetUpdateService;
import com.bushbungalo.weatherlion.utils.UtilityMethod;

import java.util.Objects;

public class BootCompleteReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive( Context context, Intent intent )
    {
        if ( Objects.requireNonNull( intent.getAction() ).equals( Intent.ACTION_BOOT_COMPLETED ) )
        {
            if( UtilityMethod.hasInternetConnection( WeatherLionApplication.getAppContext() ) )
            {
                String invoker = this.getClass().getSimpleName() + "::onReceive";
                Bundle extras = new Bundle();
                extras.putString( WidgetUpdateService.WEATHER_SERVICE_INVOKER, invoker );
                extras.putString( WeatherLionApplication.LAUNCH_METHOD_EXTRA, null );
                extras.putString( WidgetUpdateService.WEATHER_DATA_UNIT_CHANGED,
                        WeatherLionApplication.UNIT_NOT_CHANGED );

                WeatherLionApplication.callMethodByName( WeatherLionApplication.class,
                        "createServiceCallLog",
                        null, null );

                UtilityMethod.refreshRequestedBySystem = true;
                Intent updateIntent = new Intent( context, WidgetUpdateService.class );
                updateIntent.putExtras( extras );
                WidgetUpdateService.enqueueWork( context, updateIntent );

                UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                        "Update requested by the boot completed receiver",
                        invoker );
            }// end of if block
        }// end of if block
    }// end of method onReceive
}// end of class BootCompleteReceiver
