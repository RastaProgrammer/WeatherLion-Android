package com.bushbungalo.weatherlion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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

                UtilityMethod.refreshRequestedBySystem = true;
                UtilityMethod.refreshRequestedByUser = false;
                WeatherLionApplication.refreshWeather( invoker );
            }// end of if block
        }// end of if block
    }// end of method onReceive
}// end of class BootCompleteReceiver
