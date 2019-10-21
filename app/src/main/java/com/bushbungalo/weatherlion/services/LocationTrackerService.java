package com.bushbungalo.weatherlion.services;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.bushbungalo.weatherlion.utils.UtilityMethod;

/**
 * Created by Paul O. Patterson on 11/22/17.
 */
@SuppressWarnings({"unused"})
public class LocationTrackerService extends Service implements LocationListener
{
    public boolean checkGPS = false;
    public boolean checkNetwork = false;
    public  boolean canGetLocation = false;

    public Location loc;
    public double latitude;
    public double longitude;

    private Context mContext;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60;
    protected LocationManager locationManager;

    public LocationTrackerService(){}// default constructor

    public LocationTrackerService( Context mContext )
    {
        this.mContext = mContext;
        loc = getLocation();
    }// end of one-argument constructor

    private Location getLocation()
    {
        try
        {
            locationManager = (LocationManager) mContext.getSystemService( LOCATION_SERVICE );

            // get GPS status
            checkGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            // get network provider status
            checkNetwork = locationManager.isProviderEnabled( LocationManager.NETWORK_PROVIDER );

            if ( !checkGPS && !checkNetwork )
            {
                UtilityMethod.butteredToast( mContext,
                        "GPS Radio is not switched on!", 2, Toast.LENGTH_LONG );
            }// end of if block
            else
            {
                this.canGetLocation = true;

                // if GPS Enabled get lat/long using GPS Services
                if ( checkGPS )
                {
                    if ( ( ActivityCompat.checkSelfPermission( mContext, Manifest.permission.ACCESS_FINE_LOCATION ) !=
                            PackageManager.PERMISSION_GRANTED ) && ( ActivityCompat.checkSelfPermission(mContext,
                            Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) )
                    {
                        // All good
                        UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO, "Location permissions granted!",
                                "LocationTrackerService::getLocation" );
                    }// end of if block
                    else
                    {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this );

                        if ( locationManager != null )
                        {
                            loc = locationManager.getLastKnownLocation( LocationManager.GPS_PROVIDER );

                            if ( loc != null )
                            {
                                latitude = loc.getLatitude();
                                longitude = loc.getLongitude();
                            }// end of if block
                        }// end of if block
                    }// end of else block
                }// end of if block

                if ( checkNetwork )
                {

                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        // All good
                        UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO, "Location permissions granted!",
                                "LocationTrackerService::getLocation" );
                    }// end of if block
                    else
                    {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this );

                        if ( locationManager != null )
                        {
                            loc = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        }// end of if block

                        if ( loc != null )
                        {
                            latitude = loc.getLatitude();
                            longitude = loc.getLongitude();
                        }// end of if block
                    }// end of else block
                }// end of if block
            }// end of else block

        }// end of try block
        catch ( Exception e )
        {
            UtilityMethod.butteredToast( mContext, e.toString(), 2, Toast.LENGTH_SHORT );
        }// end of catch block

        return loc;
    }// end of method getLocation

    public double getLongitude()
    {
        if ( loc != null )
        {
            longitude = loc.getLongitude();
        }// end of if block

        return longitude;
    }// end of method getLatitude

    public double getLatitude()
    {
        if ( loc != null )
        {
            latitude = loc.getLatitude();
        }// end of if block

        return latitude;
    }// end of method getLatitude

    public boolean canGetLocation() {
        return this.canGetLocation;
    }// end of method canGetLocation

    public void stopListener()
    {
        if ( locationManager != null )
        {

            if ( ActivityCompat.checkSelfPermission( mContext, Manifest.permission.ACCESS_FINE_LOCATION ) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission( mContext, Manifest.permission.ACCESS_COARSE_LOCATION ) !=
                            PackageManager.PERMISSION_GRANTED )
            {
                return;
            }// end of if block

            locationManager.removeUpdates( LocationTrackerService.this );
        }// end of if block
    }// end of method stopListener

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind( Intent intent ) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLocationChanged( Location location )
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusChanged( String s, int i, Bundle bundle )
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onProviderEnabled( String s )
    {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onProviderDisabled( String s )
    {
    }
}// end of class LocationTrackerService
