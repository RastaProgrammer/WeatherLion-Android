package com.bushbungalo.weatherlion.services;

/*
  Created by Paul O. Patterson on 11/21/17.
 */

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.model.CityData;
import com.bushbungalo.weatherlion.model.GeoNamesGeoLocation;
import com.bushbungalo.weatherlion.model.HereGeoLocation;
import com.bushbungalo.weatherlion.utils.HttpHelper;
import com.bushbungalo.weatherlion.utils.JSONHelper;
import com.bushbungalo.weatherlion.utils.UtilityMethod;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;

/**
 * @author Paul O. Patterson
 * <br />
 * <b style="margin-left:-40px">Date Created:</b>
 * <br />
 * 11/21/17
 * <br />
 * <b style="margin-left:-40px">Updates:</b><br />
 * <ul>
 * 		<li>01/21/19 - Added param {@code service} to default constructor</li>
 * 		<li>03/23/19 - Added method {@code getHereSuggestions} for Here Maps data</li>
 * </ul>
 */

public class CityDataService extends IntentService
{
    private static String TAG = "CityDataService";
    public static final String CITY_DATA_SERVICE_MESSAGE = "CityDataServiceMessage";
    public static final String CITY_DATA_SERVICE_PAYLOAD = "CityDataServicePayload";

    private String m_service;
    private static String cityName = null;
    private static String countryName = null;
    private static String countryCode = null;
    private static String regionName = null;
    private static String regionCode = null;
    private static String Latitude = null;
    private static String Longitude = null;

    public static boolean serviceRequest = false;

    private String response;
    private int matchCount;
    private String dataURL = null;

    private static final String QUERY_COMMAND = "name_equals";

    public CityDataService()
    {
        super("CityDataService");
    }// end of default constructor

    public String getService()
    {
        return m_service;
    }

    public void setService( String m_service )
    {
        this.m_service = m_service;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onHandleIntent( Intent intent )
    {
        Uri uri = intent.getData();
        dataURL = uri != null ? uri.toString() : null;

        if ( dataURL != null )
        {
            if( dataURL.contains( "geonames" ) )
            {
                setService( "geo" );
            }// end of if block'
            else if( dataURL.contains( "api.here" ) )
            {
                setService( "here" );
            }// end of else block
        }// end of if block

        switch( getService() )
        {
            case "geo":
                String cityName = null;

                if( UtilityMethod.hasInternetConnection( this ) )
                {
                    // the means that we only have GPS coordinates from the on board radio
                    if( dataURL.contains( "findNearbyPlaceNameJSON" ) )
                    {
                        try
                        {
                            response = HttpHelper.downloadUrl( dataURL, false );

                            String city;
                            String countryCode;
                            String countryName;
                            String regionCode;
                            String regionName;
                            String currentLocation = null;

                            try
                            {
                                Object json = new JSONTokener( response ).nextValue();

                                // Check if a JSON was returned from the web service
                                if ( json instanceof JSONObject)
                                {
                                    // Get the full HTTP Data as JSONObject
                                    JSONObject geoNamesJSON = new JSONObject( response );
                                    // Get the JSONObject "geonames"
                                    JSONArray geoNames = geoNamesJSON.optJSONArray( "geonames" );

                                    JSONObject place = geoNames.getJSONObject(0);

                                    city = place.getString( "name" );
                                    countryCode = place.getString( "countryCode" );
                                    countryName = place.getString( "countryName" );
                                    regionCode = place.getString( "adminCode1" );
                                    regionName = countryCode.equalsIgnoreCase( "US" ) ?
                                            UtilityMethod.usStatesByCode.get(regionCode) :
                                            null;

                                    if ( regionName != null )
                                    {
                                        currentLocation = city + ", " + regionName + ", "
                                                + countryName;
                                    }// end of if block
                                    else
                                    {
                                        currentLocation = city + ", " + countryName;
                                    }// end of else block
                                }// end of if block
                                else
                                {
                                    // this means tht the user entered a city manually
                                    currentLocation = response;
                                }// end of else block
                            }// end of try block
                            catch ( JSONException e )
                            {
                                UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                                        TAG + "::handleWeatherData [line: " + UtilityMethod.getExceptionLineNumber( e ) + "]" );
                            }// end of catch block

                            cityName = currentLocation;
                        }// end of try block
                        catch ( IOException e )
                        {
                            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                                    TAG + "::handleWeatherData [line: " +
                                            UtilityMethod.getExceptionLineNumber( e )  + "]" );

                            response = null;
                        }// end of catch block
                    }// end of if block
                    else // the URL contains the city name which can be extracted
                    {
                        int start = dataURL.indexOf( QUERY_COMMAND ) + QUERY_COMMAND.length() + 1;
                        int end = dataURL.indexOf( "&" );

                        try
                        {
                            cityName = URLDecoder.decode( dataURL.substring( start, end ).toLowerCase(), "UTF-8" );
                            cityName = cityName.replaceAll("\\W", " ");
                        }// end of try block
                        catch ( UnsupportedEncodingException e )
                        {
                            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                                    TAG + "::handleWeatherData" );
                        }// end of else block

                    }// end of else block

                    // just the city name is required and nothing else
                    if ( cityName != null && cityName.contains( "," ) )
                    {
                        cityName = cityName.substring( 0, cityName.indexOf( ",") ).toLowerCase();
                    }// end of if block // end of if block

                    String ps;
                    StringBuilder fileData = new StringBuilder();

                    if ( cityName != null )
                    {
                        ps = String.format( "%s%s%s", "gn_sd_", cityName.replaceAll( " ", "_" ), ".json" );
                        WeatherLionApplication.previousCitySearchFile = this.getFileStreamPath( ps );
                    }// end of if block

                    if( WeatherLionApplication.previousCitySearchFile.exists() )
                    {
                        try(
                                FileReader fr = new FileReader( WeatherLionApplication.previousCitySearchFile );	// declare and initialize the file reader object
                                BufferedReader br = new BufferedReader( fr ) 	// declare and initialize the buffered reader object
                        )
                        {
                            String line;

                            while( ( line = br.readLine() ) != null )
                            {
                                fileData.append( line );
                            }// end of while loop

                            response = fileData.toString();
                        }// end of try block
                        catch ( IOException e )
                        {
                            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                                    TAG + "::handleWeatherData [line: " +
                                            UtilityMethod.getExceptionLineNumber( e )  + "]" );
                        }// end of catch block
                    }// end of if block
                    else
                    {
                        saveGeoNamesSearchResults( WeatherLionApplication.previousCitySearchFile, cityName );
                    }// end of else block
                }// end of if block

                break;
            case "here":
                // I prefer to use the GeoNames search results as the Here results only returns a single city.
                // I might just add if in the future though.
                break;
            default:
                break;
        }// end of switch block

        // the application/program will only need the broadcast message
        if( UtilityMethod.listRequested )
        {
            processData();
        }// end of if block
        else
        {
            broadcastResult();
        }// end of else block
    }// end of method handleWeatherData

    private void broadcastResult()
    {
        Intent messageIntent = new Intent( CITY_DATA_SERVICE_MESSAGE );
        messageIntent.putExtra( CITY_DATA_SERVICE_PAYLOAD, ( response ).trim() );
        LocalBroadcastManager manager =
                LocalBroadcastManager.getInstance( getApplicationContext() );
        manager.sendBroadcast( messageIntent );
    }// end of method broadcastResult

    private void processData()
    {
        switch ( getService() )
        {
            case "geo":
                getGeoNamesSuggestions();
                break;
            case "here":
                getHereSuggestions();
                break;
            default:
                break;
        }// end of switch block
    }// end of method processData

    private void getGeoNamesSuggestions()
    {
        ArrayList< String > matches = new ArrayList<>();

        try
        {
            Object json = new JSONTokener( response ).nextValue();

            // Check if a JSON was returned from the web service
            if ( json instanceof JSONObject )
            {
                // Get the full HTTP Data as JSONObject
                JSONObject geoNamesJSON = new JSONObject( response );
                // Get the JSONObject "geonames"
                JSONArray geoNames = geoNamesJSON.optJSONArray( "geonames" );
                matchCount = geoNamesJSON.getInt( "totalResultsCount" );

                // if the place array only contains one object, then only one
                // match was found
                if ( matchCount == 1 )
                {
                    JSONObject place = geoNames.getJSONObject( 0 );

                    cityName = place.getString( "name" );
                    countryCode = place.getString( "countryCode" );
                    countryName = place.getString( "countryName" );
                    regionCode = place.getString( "adminCode1" );
                    regionName = countryCode.equalsIgnoreCase( "US" ) ?
                            UtilityMethod.usStatesByCode.get( regionCode ) :
                            null;
                    Latitude = place.getString( "lat" );
                    Longitude = place.getString( "lng" );

                    if ( regionName != null )
                    {
                        response = cityName + ", " + regionName + ", "
                                + countryName;
                    }// end of if block
                    else
                    {
                        response = cityName + ", " + countryName;
                    }// end of else block
                }// end of if block
                else
                {
                    // Multiple matches were found
                    // Store the data in local storage
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(
                                    GeoNamesGeoLocation.class,
                                    new GeoNamesGeoLocation.GeoNamesGeoLocationDeserializer() )
                            .create();
                    GeoNamesGeoLocation.cityGeographicalData = gson.fromJson(
                            response, GeoNamesGeoLocation.class );

                    // This indicates that an array of place were found
                    response = "GeoNamesGeoLocation object created";
                }// end of else block
            }// end of if block
        }// end of try block
        catch ( Exception e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    TAG + "::getGeoNamesSuggestions [line: " +
                            UtilityMethod.getExceptionLineNumber( e )  + "]" );
        }// end of catch block

        ArrayList<CityData> cityMatches = new ArrayList<>();

        // We only need the cities with the same name
        String decodedURL = null;

        try
        {
            decodedURL = URLDecoder.decode( dataURL, "UTF-8" );
        }// end of try block
        catch ( Exception e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    TAG + "::getGeoNamesSuggestions [line: " +
                            UtilityMethod.getExceptionLineNumber(e) + "]" );
        }// end of catch block

        String searchCity = null;

        if ( decodedURL != null )
        {
            searchCity =
                UtilityMethod.getUrlParameter(
                    QUERY_COMMAND, decodedURL ).replaceAll("\\W", " " );
        }// end of if block

        for ( GeoNamesGeoLocation.GeoNames place : GeoNamesGeoLocation.cityGeographicalData.getGeoNames() )
        {
            boolean addToList = false;
            StringBuilder match = new StringBuilder();

            if ( !place.getName().equalsIgnoreCase( searchCity ) ) continue;

            if ( place.getName() != null )
            {
                match.append( place.getName() );
            }// end of if block

            // Geo Names may not return adminCodes1 for some results
            if( place.getAdminCodes1() != null )
            {
                if ( place.getAdminCodes1().getISO() != null &&
                        !UtilityMethod.isNumeric( place.getAdminCodes1().getISO() ) )
                {
                    String region = place.getAdminCodes1().getISO();

                    match.append( ", " ).append( region );
                }// end of outer if block
            }// end of if block

            if( place.getCountryName() != null )
            {
                match.append( ", " ).append( place.getCountryName() );
            }// end of if block

            // Always verify that the adminName1 and countryName does not indicate a city already added
            if( matches.size() == 0 )
            {
                addToList = true;
            }// end of if block
            else if( !matches.contains( place.getAdminName1() + ", " + place.getCountryName() ) )
            {
                // Redundancy check
                if( !matches.contains( match.toString() ) )
                {
                    addToList = true;
                }// end of else if block
            }// end of if block

            if( addToList )
            {
                matches.add( match.toString() );

                cityName = UtilityMethod.toProperCase( place.getName() );
                countryName = UtilityMethod
                        .toProperCase( place.getCountryName() );
                countryCode = place.getCountryCode().toUpperCase();

                regionName = place.getAdminName1() != null && place.getAdminName1().length() != 0 ?
                        UtilityMethod.toProperCase( place.getAdminName1() ) :
                        null;

                regionCode = place.getAdminCodes1() != null ?
                        place.getAdminCodes1().getISO().toUpperCase() :
                        null;

                Latitude = place.getLatitude().toString();
                Longitude = place.getLongitude().toString();

                cityMatches.add( new CityData( cityName, countryName, countryCode,
                        regionName, regionCode, Float.parseFloat( Latitude ),
                        Float.parseFloat( Longitude ) ) );
            }// end of if block
        }// end of for each loop

        if( cityMatches.size() != 0 )
        {
            CityData.searchCitiesData = cityMatches.toArray( new CityData[ 0 ] );
        }// end of if block

        if( !serviceRequest )
        {
            String[] s; // convert the list of matches found to a string array

            if( matches.size() > 0 )
            {
                s = matches.toArray ( new String[ 0 ] );
            }// end of if block
            else
            {
               s = new String[] { "No match found..." };
            }// end of else block

            Gson gson = new GsonBuilder().create();

            // return the JSON string array as a string
            response = gson.toJson( s );

            broadcastResult(); // send out the broadcast to all receivers
        }// end of if block

    }// end of method getGeoNamesSuggestions

    private void getHereSuggestions()
    {
        ArrayList< String > matches = new ArrayList<>();

        try
        {
            Object json = new JSONTokener( response ).nextValue();

            // Check if a JSON was returned from the web service
            if ( json instanceof JSONObject )
            {
                // Get the full HTTP Data as JSONObject
                JSONObject reader = new JSONObject( response );
                // Get the JSONObject "Response"
                JSONObject hResponse = reader.getJSONObject( "Response" );
                JSONObject view = hResponse.getJSONArray( "View" ).getJSONObject( 0 );
                JSONArray places = view.optJSONArray( "Result" );
                matchCount = places.length();

                // if the place array only contains one object, then only one
                // match was found
                if ( matchCount == 1 )
                {
                    JSONObject place = places.getJSONObject( 0 );
                    JSONObject location = place.getJSONObject( "Location" );
                    JSONObject address = location.getJSONObject( "Address" );
                    JSONObject displayPosition = location.getJSONObject( "DisplayPosition" );
                    JSONObject country = address.getJSONArray( "AdditionalData" ).getJSONObject( 0 );
                    String label = address.getString( "Label" );

                    cityName = label.substring( 0, label.indexOf( "," ) );
                    countryName = UtilityMethod.toProperCase( country
                            .getString( "value" ) );
                    countryCode = UtilityMethod.worldCountryCodes.get( countryName );
                    regionName = null;
                    regionCode = null;
                    Latitude = displayPosition.getString( "Latitude" );
                    Longitude = displayPosition.getString( "Longitude" );

                    response = label;
                    matches.add( response );
                }// end of if block
                else
                {
                    // Multiple matches were found
                    // Store the data in local storage
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(
                                    HereGeoLocation.class,
                                    new HereGeoLocation.HereGeoLocationDeserializer() )
                            .create();
                    HereGeoLocation.cityGeographicalData = gson.fromJson(
                            response, HereGeoLocation.class );

                    for ( HereGeoLocation.Response.View.Result place :
                            HereGeoLocation.cityGeographicalData.getResponse().getView().getResult() )
                    {
                        String match = place.getLocation().getAddress().getLabel();

                        if( !matches.contains( match ) )
                        {
                            matches.add( match );
                        }// end of if block
                    }// end of for each loop
                    // This indicates that an array of place were found
                    response = "HereGeoLocation object created";

                }// end of else block
            }// end of if block
        }// end of try block
        catch ( Exception e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    TAG + "::getHereSuggestions [line: " +
                            UtilityMethod.getExceptionLineNumber( e )  + "]" );
        }// end of catch block

        if( !serviceRequest )
        {
            String[] s; // convert the list of matches found to a string array

            if( matches.size() > 0 )
            {
                s = matches.toArray ( new String[ 0 ] );
            }// end of if block
            else
            {
                s = new String[] { "No match found..." };
            }// end of else block

            Gson gson = new GsonBuilder().create();

            // return the JSON string array as a string
            response = gson.toJson( s );

            broadcastResult(); // send out the broadcast to all receivers
        }// end of if block
    }// end of method getHereSuggestions

    private void saveGeoNamesSearchResults( File previousCitySearchFile, String cityName )
    {
        String searchURL;

        if( UtilityMethod.hasInternetConnection( this ) )
        {
            // now that we have the name of the city, we need some search results from GeoNames
            try
            {
                int maxRows = 100;

                // All spaces must be replaced with the + symbols for the HERE Maps web service
                if( cityName.contains( " " ) )
                {
                    cityName = cityName.replace( " ", "+" );
                }// end of if block

                searchURL = String.format(
                        "http://api.geonames.org/searchJSON?%s=%s&maxRows=%s&username=%s",
                        QUERY_COMMAND,
                        cityName.toLowerCase(),
                        maxRows,
                        WidgetUpdateService.geoNameAccount );

                response = HttpHelper.downloadUrl( searchURL, false ); // get the search results from the GeoNames web service
            }// end of try block
            catch ( Exception e )
            {
                UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                        TAG + "::handleWeatherData [line: " +
                                UtilityMethod.getExceptionLineNumber( e )  + "]" );

                response = null;
            }// end of catch block

            // attempt to store the search results locally if this search was not performed before
            if( !previousCitySearchFile.exists() )
            {
                if( response != null )
                {
                    try
                    {
                        if(  JSONHelper.saveToJSONFile( response, previousCitySearchFile.toString(), true ) )
                        {
                            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO, "JSON search data stored locally for " + cityName + ".",
                                    TAG + "::saveGeoNamesSearchResults" );
                        }// end of if block
                    }// end of try block
                    catch ( Exception e )
                    {
                        UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                                TAG + "::handleWeatherData [line: " + UtilityMethod.getExceptionLineNumber( e )  + "]" );
                    }// end of catch block
                }// end of if block
            }// end of if block
        }// end of if block
    }// end of method saveGeoNamesSearchResults

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
}// end of class CityDataService