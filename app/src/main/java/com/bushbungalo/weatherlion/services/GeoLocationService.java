package com.bushbungalo.weatherlion.services;

/*
 * Created by Paul O. Patterson on 11/16/17.
 */

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import com.bushbungalo.weatherlion.utils.HttpHelper;
import com.bushbungalo.weatherlion.utils.UtilityMethod;

import java.io.IOException;
import java.util.Objects;


public class GeoLocationService extends IntentService
{
    public static final String GEO_LOCATION_SERVICE_MESSAGE = "geoLocationServiceMessage";
    public static final String GEO_LOCATION_SERVICE_PAYLOAD = "geoLocationServicePayload";

    public GeoLocationService()
    {
        super("GeoLocationService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Uri uri = intent.getData();

        String response;

        // if it is a website then we know it is a webservice that were are calling
        if( Objects.requireNonNull( uri ).toString().contains( "http" ) )
        {
            if( UtilityMethod.hasInternetConnection( this ) )
            {
                try
                {
                    response = HttpHelper.downloadUrl( Objects.requireNonNull( uri ).toString(),
                        false );
                }// end of try block
                catch ( IOException e )
                {
                    UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                            "::handleWeatherData [line: " +
                                    e.getStackTrace()[1].getLineNumber()+ "]" );
                    return;
                }// end of catch block
            }// end of if block
            else
            {
                response = uri.toString();
            }// end of else block
        }// end of if block
        else
        {
            response = uri.toString();
        }// end of else block

        Intent messageIntent = new Intent( GEO_LOCATION_SERVICE_MESSAGE );
        messageIntent.putExtra( GEO_LOCATION_SERVICE_PAYLOAD, response );
        LocalBroadcastManager manager =
                LocalBroadcastManager.getInstance( getApplicationContext() );
        manager.sendBroadcast( messageIntent );
    }// end of method onHandleIntent

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
}// end of class GeoLocationService

