package com.bushbungalo.weatherlion.services;

import android.app.IntentService;
import android.content.Intent;

import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.model.CityData;
import com.bushbungalo.weatherlion.model.GeoNamesGeoLocation;
import com.bushbungalo.weatherlion.utils.JSONHelper;
import com.bushbungalo.weatherlion.utils.UtilityMethod;
import com.bushbungalo.weatherlion.utils.XMLHelper;

import java.util.Objects;

/**
 * @author Paul O. Patterson
 * <br />
 * <b style="margin-left:-40px">Date Created:</b>
 * <br />
 * 07/18/19
 * <br />
 */

public class CityStorageService extends IntentService
{
    private int m_index;
    private String m_city;

    public CityStorageService()
    {
        super("CityStorageService");
    }// end of default constructor

    public int getIndex()
    {
        return m_index;
    }

    public void setIndex(int m_index)
    {
        this.m_index = m_index;
    }

    public String getCity()
    {
        return m_city;
    }

    public void setCity( String cityName )
    {
        this.m_city = cityName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onHandleIntent( Intent intent )
    {
        if(Objects.requireNonNull( intent.getData() ).toString().contains( ":" ) )
        {
            String[] cityDetails = intent.getData().toString().split( ":" );
            setCity( cityDetails[ 1 ] );
            setIndex( Integer.parseInt( cityDetails[ 0 ] ) );

            if( getCity().trim().length() > 0 &&
                    !UtilityMethod.isKnownCity( getCity() ) )
            {
                if( GeoNamesGeoLocation.cityGeographicalData == null )
                {
                    CityDataService.serviceRequest = true;

                    // use the web service it was not used before
                    UtilityMethod.findGeoNamesCity( getCity(), getApplicationContext() );
                }// end of if block

                storeNewLocationLocally();
            }// end of if block
        }// end of if block
    }// end of method handleWeatherData

    /***
     * Add new location locally if it does not already exists
     */
    private void storeNewLocationLocally()
    {
        String TAG = "CityStorageService";
        CityData cd = CityData.searchCitiesData[ getIndex() ];

        String cityName = UtilityMethod.toProperCase( cd.getCityName() );
        String countryName = UtilityMethod
                .toProperCase( cd.getCountryName() );
        String countryCode = cd.getCountryCode().toUpperCase();
        String regionName = UtilityMethod
                .toProperCase( cd.getRegionName() );

        String regionCode = cd.getRegionCode();

        float Latitude = cd.getLatitude();
        float Longitude = cd.getLongitude();

        // fetch the time zone details for this city
        WeatherLionApplication.currentLocationTimeZone =
                UtilityMethod.retrieveGeoNamesTimeZoneInfo( Latitude, Longitude );

        String timeZone = WeatherLionApplication.currentLocationTimeZone.getTimezoneId();
        String currentCity = regionCode != null ? cityName + ", " + regionCode : cityName + ", " + countryName;
        cd.setTimeZone( timeZone );

        if( !UtilityMethod.cityFoundInDatabase( currentCity ) )
        {
            UtilityMethod.addCityToDatabase( cityName, countryName, countryCode,
                regionName, regionCode,	timeZone, Latitude, Longitude );
        }// end of if block

        if( UtilityMethod.cityFoundInJSONStorage( currentCity ) == null )
        {
            if( JSONHelper.exportCityToJSON( cd ) )
            {
                UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO, currentCity +
                    " has been added to the list of JSON known cities.", TAG + "::storeNewLocationLocally" );
            }// end of if block
        }// end of if block

        if( !UtilityMethod.cityFoundInXMLStorage( currentCity ) )
        {
            if(  XMLHelper.exportCityDataToXML( cd ) )
            {
                UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO, currentCity +
                    " has been added to the list of XML known cities.", TAG + "::storeNewLocationLocally" );
            }// end of if block
        }// end of if block
    }// end of method storeLocationLocally

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
}// end of class CityStorageService
