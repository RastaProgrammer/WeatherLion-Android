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

                WeatherLionApplication.callMethodByName( null,
                        "refreshWeather",
                        new Class[]{ String.class }, new Object[]{ invoker } );

            }// end of if block
        }// end of if block
    }// end of method onReceive
}// end of class BootCompleteReceiver
