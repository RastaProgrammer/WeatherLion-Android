package com.bushbungalo.weatherlion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

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
                WeatherLionApplication.callMethodByName( WeatherLionApplication.class,
                        "createServiceCallLog",
                        null, null );

                UtilityMethod.refreshRequested = true;
                Intent updateIntent = new Intent( context, WidgetUpdateService.class );
                updateIntent.setData( Uri.parse( WeatherLionApplication.UNIT_NOT_CHANGED ) );
                WidgetUpdateService.enqueueWork( context, updateIntent );

                UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                        "Update requested by the boot completed receiver",
                        this.getClass().getSimpleName() + "::onReceive" );
            }// end of if block
        }// end of if block
    }// end of method onReceive
}// end of class BootCompleteReceiver
