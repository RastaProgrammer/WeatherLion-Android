package com.bushbungalo.weatherlion.services;

import android.app.IntentService;
import android.content.Intent;

import com.bushbungalo.weatherlion.model.CityData;
import com.bushbungalo.weatherlion.model.GeoNamesGeoLocation;
import com.bushbungalo.weatherlion.utils.JSONHelper;
import com.bushbungalo.weatherlion.utils.UtilityMethod;
import com.bushbungalo.weatherlion.utils.XMLHelper;

import java.util.Objects;

/**
 * @author Paul O. Patterson
 * <br />
 * <b style="margin-left:-40px">Class Description:</b>
 * <br />
 * 		<span>Service that calls method <b><i>checkAstronomy</i></b> in the <b>WeatherLionWidget</b> class which checks the current time of day</span>
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

        String currentCity = regionCode != null ? cityName + ", " + regionCode : cityName + ", " + countryName;

        if( !UtilityMethod.isFoundInDatabase( currentCity ) )
        {
            UtilityMethod.addCityToDatabase( cityName, countryName, countryCode,
                regionName, regionCode,	Latitude, Longitude );
        }// end of if block

        if( UtilityMethod.isFoundInJSONStorage( currentCity ) == null )
        {
            if( JSONHelper.exportToJSON( cd ) )
            {
                UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO, currentCity +
                    " has been added to the list of known cities.", TAG + "::storeNewLocationLocally" );
            }// end of if block
        }// end of if block

        if( !UtilityMethod.isFoundInXMLStorage( currentCity ) )
        {
            if(  XMLHelper.exportToXML( cd ) )
            {
                UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO, currentCity +
                    " has been added to the list of known cities.", TAG + "::storeNewLocationLocally" );
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
