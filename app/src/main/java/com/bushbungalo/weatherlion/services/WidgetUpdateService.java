package com.bushbungalo.weatherlion.services;

/*
 * Created by Paul O. Patterson on 11/30/17.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.bushbungalo.weatherlion.AlarmBroadcastReceiver;
import com.bushbungalo.weatherlion.FiveDayForecast;
import com.bushbungalo.weatherlion.Preference;
import com.bushbungalo.weatherlion.R;
import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.WeatherWidgetProvider;
import com.bushbungalo.weatherlion.model.CityData;
import com.bushbungalo.weatherlion.model.DarkSkyWeatherDataItem;
import com.bushbungalo.weatherlion.model.HereMapsWeatherDataItem;
import com.bushbungalo.weatherlion.model.LastWeatherData;
import com.bushbungalo.weatherlion.model.OpenWeatherMapWeatherDataItem;
import com.bushbungalo.weatherlion.model.WeatherBitWeatherDataItem;
import com.bushbungalo.weatherlion.model.YahooWeatherYdnDataItem;
import com.bushbungalo.weatherlion.model.YrWeatherDataItem;
import com.bushbungalo.weatherlion.model.YrWeatherDataItem.Forecast;
import com.bushbungalo.weatherlion.utils.HttpRequest;
import com.bushbungalo.weatherlion.utils.LastWeatherDataXmlParser;
import com.bushbungalo.weatherlion.utils.UtilityMethod;
import com.bushbungalo.weatherlion.utils.WidgetHelper;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

@SuppressWarnings({"unused", "SameParameterValue"})
public class WidgetUpdateService extends JobIntentService
{
    public static final int JOB_ID = 79;

    public static final String TAG = "WidgetUpdateService";
    public static final String WEATHER_UPDATE_SERVICE_MESSAGE = "WidgetUpdateServiceMessage";
    public static final String  WEATHER_UPDATE_SERVICE_PAYLOAD = "WidgetUpdateServicePayload";
    public static final String  WEATHER_XML_DATA = "WeatherXmlData";

    private static DarkSkyWeatherDataItem darkSky;
    private static HereMapsWeatherDataItem.WeatherData hereWeatherWx;
    private static HereMapsWeatherDataItem.ForecastData hereWeatherFx;
    private static HereMapsWeatherDataItem.AstronomyData hereWeatherAx;
    private static OpenWeatherMapWeatherDataItem.WeatherData openWeatherWx;
    private static OpenWeatherMapWeatherDataItem.ForecastData openWeatherFx;
    private static WeatherBitWeatherDataItem.WeatherData weatherBitWx;
    private static WeatherBitWeatherDataItem.SixteenDayForecastData weatherBitFx;
    private static YahooWeatherYdnDataItem yahoo19;
    private static YrWeatherDataItem yr;

    private StringBuilder wxUrl = new StringBuilder();
    private StringBuilder fxUrl = new StringBuilder();
    private StringBuilder axUrl = new StringBuilder();
    private ArrayList<String> strJSON;

    private final String CELSIUS = "\u00B0C";
    private final String DEGREES = "\u00B0";
    private final String FAHRENHEIT = "\u00B0F";
    private static final String FEELS_LIKE = "Feels Like";

    private static StringBuilder currentCity = new StringBuilder();
    private static StringBuilder currentCountry = new StringBuilder();
    private static StringBuilder currentTemp = new StringBuilder();
    private static StringBuilder currentFeelsLikeTemp = new StringBuilder();
    private static StringBuilder currentWindSpeed = new StringBuilder();
    private static StringBuilder currentWindDirection = new StringBuilder();
    private static StringBuilder currentHumidity = new StringBuilder();
    private static StringBuilder currentLocation = new StringBuilder();
    public  static StringBuilder currentCondition = new StringBuilder();
    private static StringBuilder currentHigh = new StringBuilder();
    private static StringBuilder currentLow = new StringBuilder();
    private static List< FiveDayForecast > currentFiveDayForecast = new ArrayList<>();
    private static int[][] hl;

    private boolean unitChange;
    private WeatherDataXMLService wXML;
    private Dictionary< String, float[][] > dailyReading;

    private String tempUnits;

    private static LinkedHashMap<String, String> hereMapsWeatherProductKeys;
    static
    {
        hereMapsWeatherProductKeys = new LinkedHashMap<>();
        hereMapsWeatherProductKeys.put( "conditions", "observation" );
        hereMapsWeatherProductKeys.put( "forecast", "forecast_7days_simple" );
        hereMapsWeatherProductKeys.put( "astronomy", "forecast_astronomy" );
    }

    // public variables
    public static String darkSkyApiKey = null;
    public static String hereAppId = null;
    public static String hereAppCode = null;
    public static String yahooConsumerKey = null;
    public static String yahooConsumerSecret = null;
    public static String yahooAppId = null;
    public static String openWeatherMapApiKey = null;
    public static String weatherBitApiKey = null;
    public static String geoNameAccount = null;

    public static StringBuilder sunriseTime = new StringBuilder();
    public static StringBuilder sunsetTime = new StringBuilder();

    public static boolean widgetRefreshRequired;

    private static RemoteViews remoteViews;

    private Map< String, Object > xmlMapData;

    private SharedPreferences spf = null;

    private boolean loadingPreviousWeather;
    private boolean methodCalledByReflection;

    // method name constants
    public static final String LOAD_PREVIOUS_WEATHER = "loadPreviousWeatherData";
    public static final String LOAD_WIDGET_BACKGROUND = "loadWidgetBackground";

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate()
    {
        super.onCreate();
        spf = PreferenceManager.getDefaultSharedPreferences( this );
    }// end of method onCreate

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }// end of method onDestroy

    public static void enqueueWork( Context context, Intent work )
    {
        enqueueWork( context, WidgetUpdateService.class, JOB_ID, work );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onHandleWork( @NonNull Intent intent )
    {
        handleIntent( intent );
    }

    private void handleIntent( Intent intent )
    {
        remoteViews = new RemoteViews( this.getPackageName(), R.layout.wl_weather_widget_activity );
        unitChange = Boolean.parseBoolean( intent.getDataString() );

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance( this );
        int incomingAppWidgetId = 0;

        String currLocation = spf.getString( WeatherLionApplication.CURRENT_LOCATION_PREFERENCE,
                Preference.DEFAULT_WEATHER_LOCATION );
        boolean locationSet = !Objects.requireNonNull( currLocation ).equalsIgnoreCase( Preference.DEFAULT_WEATHER_LOCATION );

        // the extra must be a string representation of a method
        String callMethod = intent.getStringExtra( WeatherLionApplication.LAUNCH_METHOD_EXTRA );

        incomingAppWidgetId = WidgetHelper.getWidgetId();

        // if no widgets have been created then there is nothing to do
        if( incomingAppWidgetId == 0 ) return;

        // the caller requires only a method to be run
        if( callMethod != null )
        {
            incomingAppWidgetId = WidgetHelper.getWidgetId();

            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
            "Reflective call to method: " + callMethod + "...",
                TAG + "::handleIntent" );

            methodCalledByReflection = true;
            callMethodByName( WidgetUpdateService.this, callMethod,null,
            null );

            // If a location has not been set then the weather cannot be processed
            if( !locationSet && !callMethod.equals( LOAD_WIDGET_BACKGROUND ) ) return;
        }// end of if block
        else
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                    "Service called for weather update...",
                    TAG + "::handleIntent" );

            // If a location has not been set then the weather cannot be processed
            if( !locationSet ) return;

            if( WeatherLionApplication.storedPreferences != null )
            {
                tempUnits = WeatherLionApplication.storedPreferences.getUseMetric() ? CELSIUS : FAHRENHEIT;
                currentCity.setLength( 0 );

                if( WeatherLionApplication.storedPreferences.getLocation().length() > 0 &&
                        !WeatherLionApplication.storedPreferences.getLocation().equals(
                                Preference.DEFAULT_WEATHER_LOCATION ) )
                {
                    currentCity.append( WeatherLionApplication.storedPreferences.getLocation() );
                }// end of if block
                else
                {
                    currentCity.append( currLocation );
                }// ed of else block

                String json;
                float lat;
                float lng;
                strJSON = new ArrayList<>();
                String wxDataProvider;

                if( WeatherLionApplication.noAccessToStoredProvider )
                {
                    wxDataProvider = WeatherLionApplication.webAccessGranted.get( 0 );
                }// end of if block
                else
                {
                    wxDataProvider = WeatherLionApplication.storedPreferences.getProvider();
                }// end of else block

                if( !unitChange )
                {
                    // Check the Internet connection availability
                    if( UtilityMethod.hasInternetConnection( this ) &&
                            UtilityMethod.updateRequired( getApplicationContext() ) )
                    {
                        wxUrl.setLength( 0 );
                        fxUrl.setLength( 0 );
                        axUrl.setLength( 0 );

                        switch( wxDataProvider )
                        {
                            case WeatherLionApplication.DARK_SKY:
                                // if this location has already been used there is no need to query the
                                // web service as the location data has been stored locally
                                CityData.currentCityData = UtilityMethod.isFoundInJSONStorage( currentCity.toString() );

                                if( CityData.currentCityData == null )
                                {
                                    json =
                                            UtilityMethod.retrieveGeoNamesGeoLocationUsingAddress( currentCity.toString() );
                                    CityData.currentCityData = UtilityMethod.createGeoNamesCityData( json );
                                }// end of if block

                                lat = CityData.currentCityData.getLatitude();
                                lng = CityData.currentCityData.getLongitude();

                                wxUrl.setLength( 0 );
                                wxUrl.append( String.format( "https://api.darksky.net/forecast/%s/%s,%s",
                                        darkSkyApiKey, lat, lng ) );

                                break;
                            case WeatherLionApplication.OPEN_WEATHER:
                                // if this location has already been used there is no need to query the
                                // web service as the location data has been stored locally
                                CityData.currentCityData = UtilityMethod.isFoundInJSONStorage( currentCity.toString() );

                                if( CityData.currentCityData == null )
                                {
                                    json =
                                            UtilityMethod.retrieveGeoNamesGeoLocationUsingAddress( currentCity.toString() );
                                    CityData.currentCityData = UtilityMethod.createGeoNamesCityData( json );
                                }// end of if block

                                lat = CityData.currentCityData.getLatitude();
                                lng = CityData.currentCityData.getLongitude();

                                wxUrl.setLength( 0 );
                                wxUrl.append(
                                        String.format(
                                                "https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&appid=%s&units=imperial"
                                                , lat, lng, openWeatherMapApiKey ) );

                                fxUrl.setLength( 0 );
                                fxUrl.append(
                                        String.format(
                                                "https://api.openweathermap.org/data/2.5/forecast/daily?lat=%s&lon=%s&appid=%s&units=imperial"
                                                , lat, lng, openWeatherMapApiKey ) );

                                break;
                            case WeatherLionApplication.HERE_MAPS:
                                // if this location has already been used there is no need to query the
                                // web service as the location data has been stored locally
                                CityData.currentCityData = UtilityMethod.isFoundInJSONStorage( currentCity.toString() );

                                if( CityData.currentCityData == null )
                                {
                                    json =
                                            UtilityMethod.retrieveGeoNamesGeoLocationUsingAddress( currentCity.toString() );
                                    CityData.currentCityData = UtilityMethod.createGeoNamesCityData( json );
                                }// end of if block

                                wxUrl.setLength( 0 );
                                wxUrl.append(
                                        String.format(
                                                "https://weather.api.here.com/weather/1.0/report.json?app_id=%s&app_code=%s&product=%s&name=%s&metric=false"
                                                , hereAppId, hereAppCode, hereMapsWeatherProductKeys.get( "conditions" ),
                                                UtilityMethod.escapeUriString( currentCity.toString() ) ) );

                                fxUrl.setLength( 0 );
                                fxUrl.append(
                                        String.format(
                                                "https://weather.api.here.com/weather/1.0/report.json?app_id=%s&app_code=%s&product=%s&name=%s&metric=false"
                                                , hereAppId, hereAppCode, hereMapsWeatherProductKeys.get( "forecast" ),
                                                UtilityMethod.escapeUriString( currentCity.toString() ) ) );

                                axUrl.setLength( 0 );
                                axUrl.append(
                                        String.format(
                                                "https://weather.api.here.com/weather/1.0/report.json?app_id=%s&app_code=%s&product=%s&name=%s&metric=false"
                                                , hereAppId, hereAppCode, hereMapsWeatherProductKeys.get( "astronomy" ),
                                                UtilityMethod.escapeUriString( currentCity.toString() ) ) );
                                break;
                            case WeatherLionApplication.WEATHER_BIT:
                                // if this location has already been used there is no need to query the
                                // web service as the location data has been stored locally
                                CityData.currentCityData = UtilityMethod.isFoundInJSONStorage( currentCity.toString() );

                                if( CityData.currentCityData == null )
                                {
                                    json =
                                            UtilityMethod.retrieveGeoNamesGeoLocationUsingAddress( currentCity.toString() );
                                    CityData.currentCityData = UtilityMethod.createGeoNamesCityData( json );
                                }// end of if block

                                wxUrl.setLength( 0 );
                                wxUrl.append(
                                        String.format( "https://api.weatherbit.io/v2.0/current?city=%s&units=I&key=%s",
                                                UtilityMethod.escapeUriString( currentCity.toString() ), weatherBitApiKey ) );

                                // Sixteen day forecast will be used as it contains more relevant data
                                fxUrl.setLength( 0 );
                                fxUrl.append(
                                        String.format( "https://api.weatherbit.io/v2.0/forecast/daily?city=%s&units=I&key=%s",
                                                UtilityMethod.escapeUriString( currentCity.toString() ), weatherBitApiKey ) );
                                break;
                            case WeatherLionApplication.YR_WEATHER:

                                // if this location has already been used there is no need to query the
                                // web service as the location data has been stored locally
                                CityData.currentCityData = UtilityMethod.isFoundInJSONStorage( currentCity.toString() );

                                if( CityData.currentCityData == null )
                                {
                                    json =
                                            UtilityMethod.retrieveGeoNamesGeoLocationUsingAddress( currentCity.toString() );
                                    CityData.currentCityData = UtilityMethod.createGeoNamesCityData( json );
                                }// end of if block

                                String cityName =
                                        CityData.currentCityData.getCityName().contains( " " ) ?
                                                CityData.currentCityData.getCityName().replace( " ", "_" ) :
                                                CityData.currentCityData.getCityName();
                                String countryName =
                                        CityData.currentCityData.getCountryName().contains( " " ) ?
                                                CityData.currentCityData.getCountryName().replace( " ", "_" ) :
                                                CityData.currentCityData.getCountryName();
                                String regionName = cityName.equalsIgnoreCase("Kingston") ? "Kingston" :
                                        CityData.currentCityData.getRegionName().contains( " " ) ?
                                                CityData.currentCityData.getRegionName().replace( " ", "_" ) :
                                                CityData.currentCityData.getRegionName();	// Yr data mistakes Kingston as being in St. Andrew

                                wxUrl.setLength( 0 );
                                wxUrl.append( String.format( "https://www.yr.no/place/%s/%s/%s/forecast.xml",
                                        countryName, regionName, cityName ) );
                                break;
                            default:
                                strJSON.add( "Invalid Provider" );

                                break;
                        }// end of switch block

                        if( wxDataProvider.equals( WeatherLionApplication.YAHOO_WEATHER ) )
                        {
                            try
                            {
                                strJSON.add( getYahooWeatherData( WeatherLionApplication.storedPreferences.getLocation().toLowerCase() ) );
                            }// end of try block
                            catch ( Exception e )
                            {
                                strJSON = null;
                            }// end of catch block
                        }// end of if block
                        else
                        {
                            if( wxUrl.length() != 0 && fxUrl.length() != 0 && axUrl.length() != 0 )
                            {
                                strJSON.add( UtilityMethod.retrieveWeatherData( wxUrl.toString() ) );
                                strJSON.add( UtilityMethod.retrieveWeatherData( fxUrl.toString() ) );
                                strJSON.add( UtilityMethod.retrieveWeatherData( axUrl.toString() ) );
                            }// end of if block
                            else if( wxUrl.length() != 0 && fxUrl.length() != 0 && axUrl.length() == 0 )
                            {
                                strJSON.add( UtilityMethod.retrieveWeatherData( wxUrl.toString() ) );
                                strJSON.add( UtilityMethod.retrieveWeatherData( fxUrl.toString() ) );
                            }// end of if block
                            else if( wxUrl.length() != 0 && fxUrl.length() == 0  && axUrl.length() == 0 )
                            {
                                strJSON.add( UtilityMethod.retrieveWeatherData( wxUrl.toString() ) );
                            }// end of else if block
                            else if( wxUrl.length() == 0 && fxUrl.length() != 0  && axUrl.length() == 0 )
                            {
                                strJSON.add( UtilityMethod.retrieveWeatherData( fxUrl.toString() ) );
                            }// end of else if block
                            else if( wxUrl.length() == 0 && fxUrl.length() == 0  && axUrl.length() != 0 )
                            {
                                strJSON.add( UtilityMethod.retrieveWeatherData( axUrl.toString() ) );
                            }// end of else if block
                        }// end of else block

                        incomingAppWidgetId = intent.getIntExtra( EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID );

                        // schedule the next widget update
                        scheduleNextUpdate();
                    }// end of if block
                }// end of if block
            }// end of if block
        }// end of else block

        // determine how many widgets require updating
        if ( incomingAppWidgetId != INVALID_APPWIDGET_ID )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                    "Updating widget: " + incomingAppWidgetId + "...",
                    TAG + "::handleIntent" );
            updateOneAppWidget( appWidgetManager, incomingAppWidgetId );
        }// end of if block
        else
        {
            updateAllAppWidgets( appWidgetManager );
        }// end of else block
    }// end of method handleWeatherData

    /**
     * This method uses refection to call a method using a {@code String} value representing the
     * method name.
     *
     * @param obj   The {@code Class} {@code Object} which contains the method.
     * @param methodName    A {@code String} representing the name of the method to be called.
     * @param parameterTypes    An array representing the param type example new Class[]{String.class} or null can be passed.
     * @param paramValues    An array representing the param value example new Object[]{"GeoNames"} or null can be passed.
     */
    private void callMethodByName( Object obj, String methodName, Class[] parameterTypes, Object[] paramValues )
    {
        Method method;

        try
        {
            if( paramValues == null )
            {
                // Ignoring any possible result
                obj.getClass().getDeclaredMethod( methodName ).invoke( obj );
            }// end of if block
            else
            {
                method = obj.getClass().getDeclaredMethod( methodName, parameterTypes );
                method.invoke( obj, paramValues );
            }// end of else block

        } // end of try block
        catch ( SecurityException | NoSuchMethodException  | IllegalArgumentException |
                IllegalAccessException | InvocationTargetException e)
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    TAG + "::callMethodByName [line: " +
                            UtilityMethod.getExceptionLineNumber( e )  + "]" );
        }// end of catch block
    }// end of method callNMethodByName

    /**
     * Update a specific widget
     * @param appWidgetManager   The widget manager
     * @param appWidgetId   The ID associated with a particular widget
     */
    private void updateOneAppWidget( AppWidgetManager appWidgetManager, int appWidgetId )
    {
        if( !unitChange && UtilityMethod.updateRequired( this ) )
        {
            // check that the ArrayList is not empty and the the first element is not null
            if( strJSON != null && !strJSON.isEmpty() && strJSON.get( 0 ) != null )
            {
                // we are connected to the Internet if JSON data is returned
                remoteViews.setViewVisibility( R.id.imvOffline, View.INVISIBLE );

                try
                {
                    switch( WeatherLionApplication.storedPreferences.getProvider() )
                    {
                        case WeatherLionApplication.DARK_SKY:
                            darkSky = new Gson().fromJson( strJSON.get( 0 ), DarkSkyWeatherDataItem.class );
                            loadDarkSkyWeather();

                            break;
                        case WeatherLionApplication.HERE_MAPS:
                            hereWeatherWx = new Gson().fromJson( strJSON.get( 0 ), HereMapsWeatherDataItem.WeatherData.class );
                            hereWeatherFx = new Gson().fromJson( strJSON.get( 1 ), HereMapsWeatherDataItem.ForecastData.class );
                            hereWeatherAx = new Gson().fromJson( strJSON.get( 2 ), HereMapsWeatherDataItem.AstronomyData.class );
                            loadHereMapsWeather();

                            break;
                        case WeatherLionApplication.OPEN_WEATHER:
                            openWeatherWx = new Gson().fromJson( strJSON.get( 0 ), OpenWeatherMapWeatherDataItem.WeatherData.class );
                            openWeatherFx = new Gson().fromJson( strJSON.get( 1 ), OpenWeatherMapWeatherDataItem.ForecastData.class );
                            loadOpenWeather();

                            break;
                        case WeatherLionApplication.WEATHER_BIT:
                            weatherBitWx = new Gson().fromJson( strJSON.get( 0 ), WeatherBitWeatherDataItem.WeatherData.class );
                            weatherBitFx = new Gson().fromJson( strJSON.get( 1 ), WeatherBitWeatherDataItem.SixteenDayForecastData.class );
                            loadWeatherBitWeather();

                            break;
                        case WeatherLionApplication.YAHOO_WEATHER:

                            // Yahoo is constantly messing around with their API
                            String jsonWeatherObj = null;

                            // Check if a JSON was returned from the web service
                            for ( String wxD : strJSON)
                            {
                                Object json = null;

                                try
                                {
                                    json = new JSONTokener( wxD ).nextValue();
                                }// end of try block
                                catch ( JSONException e )
                                {
                                    UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, "Bad Yahoo data: " + e.getMessage(),
                                TAG + "::done [line: " +
                                            e.getStackTrace()[1].getLineNumber()+ "]" );
                                }// end of catch block

                                if ( json instanceof JSONObject)
                                {
                                    jsonWeatherObj = wxD;
                                }// end of if block
                            }// end of for each loop

                            yahoo19 = new Gson().fromJson( jsonWeatherObj, YahooWeatherYdnDataItem.class);
                            loadYahooYdnWeather();

                            break;
                        case WeatherLionApplication.YR_WEATHER:
                            YrWeatherDataItem.deserializeYrXML( strJSON.get( 0 ) );
                            yr = YrWeatherDataItem.yrWeatherDataItem;
                            loadYrWeather();

                            break;
                        default:
                            break;
                    }// end of switch block


                    if( UtilityMethod.refreshRequested )
                    {
                        UtilityMethod.refreshRequested = false;
                    }// end of if block

                    UtilityMethod.lastUpdated = new Date();
                    WeatherLionApplication.weatherLoadedFromProvider = true;
                    WeatherLionApplication.localWeatherDataAvailable = false;
                }// end of try block
                catch( Exception e )
                {
                    WeatherWidgetProvider.dataLoadedSuccessfully = false;

                    // Undo changes made
                    WeatherLionApplication.storedPreferences.setProvider(
                        WeatherLionApplication.previousWeatherProvider.toString() );

                    WeatherLionApplication.systemPreferences.setPrefValues(
                        WeatherLionApplication.WEATHER_SOURCE_PREFERENCE,
                            WeatherLionApplication.previousWeatherProvider.toString() );

                    // reverse the update time to the previous successful update time
                    if( WeatherLionApplication.previousLastUpdate != null )
                    {
                        UtilityMethod.lastUpdated = WeatherLionApplication.previousLastUpdate;
                    }// end of if block

                }// end of catch block

                //UtilityMethod.lastUpdated = new Date();
                SimpleDateFormat dt = new SimpleDateFormat( "E h:mm a", Locale.ENGLISH );
                String timeUpdated = dt.format( UtilityMethod.lastUpdated );
                currentLocation.setLength( 0 );
                currentLocation.append( currentCity.length() != 0 ?
                        currentCity : WeatherLionApplication.storedPreferences.getLocation() );

                remoteViews.setTextViewText( R.id.txvProvider,
                        WeatherLionApplication.storedPreferences.getProvider() );

                // Update the current location
                remoteViews.setTextViewText( R.id.txvCurrentLocation,
                        currentCity.substring( 0, currentCity.indexOf( "," ) ) );

                remoteViews.setTextViewText( R.id.txvWeatherCondition,
                        UtilityMethod.toProperCase( currentCondition.toString() ) );

                remoteViews.setTextViewText( R.id.txvWindReading, currentWindDirection +
                        " " + currentWindSpeed + ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                        " km/h" : " mph" ) );

                // Update the current location and update time stamp
                String ts = new SimpleDateFormat(
                        "E, MMM dd, h:mm a", Locale.ENGLISH ).format( UtilityMethod.lastUpdated );

                remoteViews.setTextViewText( R.id.txvLastUpdated, ts );

                // Update the weather provider image view and text view
                String providerIcon = String.format( "%s%s%s", "icons/",
                        WeatherLionApplication.storedPreferences.getProvider().toLowerCase(), ".png" );

                loadWeatherIcon( R.id.imvWeatherProviderLogo, providerIcon );
                remoteViews.setTextViewText( R.id.txvProvider,
                        WeatherLionApplication.storedPreferences.getProvider() );
            }// end of inner if block
            else // no json data was returned so check for Internet connectivity
            {
                // Check the Internet connection availability
                if( !UtilityMethod.hasInternetConnection(this) )
                {
                    File previousWeatherData = new File( WeatherLionApplication.WEATHER_DATA_XML );

                    // check for previous weather data stored locally
                    if( previousWeatherData.exists() )
                    {
                        loadPreviousWeatherData();
                        WeatherLionApplication.weatherLoadedFromProvider = false;

                        UtilityMethod.butteredToast(this, "No internet connection was detected so "
                            + "previous weather\ndata will be used until connection to the internet is restored.",
                        2, Toast.LENGTH_LONG );

                        // display the offline icon on the widget
                        remoteViews.setViewVisibility(R.id.imvOffline, View.VISIBLE);

                    }// end of if block
                }// end of if block
                else // we are connected to the Internet so perhaps the issue lies with the weather source
                {
                    // The previous weather data is stored locally so there will be no json data
                    // to be processed. If we are not loading the previous weather data then something
                    // went wrong with the weather data request.
                    if( !loadingPreviousWeather && !methodCalledByReflection )
                    {
                        // return to the previous data service
                        WeatherLionApplication.systemPreferences.getSavedPreferences().setProvider(
                                WeatherLionApplication.previousWeatherProvider.toString() );

                        // Calling from a Non-UI Thread
                        Handler handler = new Handler( Looper.getMainLooper() );

                        handler.post( new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                UtilityMethod.butteredToast( getApplicationContext(),
                                WeatherLionApplication.storedPreferences.getProvider() + " did not return data!",
                                2, Toast.LENGTH_LONG );
                            }
                        });

                        // display the preference activity screen
//                        Intent settingsIntent = new Intent( this, PrefsActivity.class );
//                        startActivity( settingsIntent );
                    }// end of if block
                }// end of else block
            }// end of inner else block
        }// end of if block
        else if( unitChange )
        {
            updateTemps( false );
        }// end of else if block
        else if( loadingPreviousWeather )
        {
            // if no update is required that means that previous weather data
            // should be loaded. Since this is so, ensure that the previous
            // weather data is loaded even if it already is.
            //loadPreviousWeatherData();
            WeatherLionApplication.lastDataReceived = LastWeatherDataXmlParser.parseXmlData(
                    UtilityMethod.readAll(
                            this.getFileStreamPath( WeatherLionApplication.WEATHER_DATA_XML ).toString() )
                            .replaceAll( "\t", "" ).trim() );
            WeatherLionApplication.localWeatherDataAvailable = true;

            // The icon updater service will need to look at these values
            WeatherLionApplication.currentSunriseTime = sunriseTime;
            WeatherLionApplication.currentSunsetTime = sunsetTime;
        }// end of else if block

        // update the widget
        appWidgetManager.updateAppWidget( appWidgetId, remoteViews );
    }// end of method UpdateOneWidget

    /**
     * Update all running widgets for this application
     * @param appWidgetManager  The widget manager
     */
    private void updateAllAppWidgets( AppWidgetManager appWidgetManager )
    {
        ComponentName appWidgetProvider = new ComponentName(this,
                WeatherWidgetProvider.class );
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(appWidgetProvider);

        for ( int appWidgetId : appWidgetIds )
        {
            updateOneAppWidget( appWidgetManager, appWidgetId );
        }// end of for each loop
    }// end of method updateAllAppWidgets

    /**
     * Load the applicable weather icon image
     * @param resID The Id of the resource
     * @param imageFile  The file name for the icon
     */
    private void loadWeatherIcon( int resID, String imageFile )
    {
        try( InputStream is = this.getAssets().open( imageFile ) )
        {
            Bitmap bmp = BitmapFactory.decodeStream( is );
            remoteViews.setImageViewBitmap( resID, bmp );
        }// end of try block
        catch ( IOException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE,"Weather icon " +
                imageFile + " could not be loaded!", TAG + "::loadWeatherIcon [line: " +
                    e.getStackTrace()[1].getLineNumber()+ "]" );
        }// end of catch block
    }// end of method loadWeatherIcon

    /**
     * Schedules the next App Widget update to occur based on
     * the interval specified by the user of the default interval
     * of 30 minutes of 1800000 milliseconds.
     *
     * The scheduled update does not wake the device up.  If
     * the update is scheduled to start while the device is
     * asleep, it will not run until the next time the device
     * is awake.
     */
    private void scheduleNextUpdate()
    {
        Intent intentToFire = new Intent( getApplicationContext(), AlarmBroadcastReceiver.class );
        intentToFire.setAction( AlarmBroadcastReceiver.ACTION_ALARM );

        PendingIntent alarmIntent = PendingIntent.getBroadcast( getApplicationContext(),
                0, intentToFire, 0 );
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().
                getSystemService( Context.ALARM_SERVICE );

        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences( this );

        // Obtain all default value from the stored preferences
        int timeAmount = UtilityMethod.millisecondsToMinutes(
                Integer.parseInt( WeatherLionApplication.storedPreferences.getInterval() ) );
        Calendar c = Calendar.getInstance();
        Date schedTime = UtilityMethod.lastUpdated != null ? UtilityMethod.lastUpdated : new Date();
        c.setTime( schedTime );
        c.add( Calendar.MINUTE, timeAmount );

        long updateTime = c.getTimeInMillis();

        alarmManager.setExact( AlarmManager.RTC, updateTime, alarmIntent );
        UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO, "Next update scheduled for " +
                new SimpleDateFormat( "h:mm:ss a", Locale.ENGLISH ).format( c.getTime() ) + ".",
                TAG + "::scheduleNextUpdate" );
    }// end of method scheduleNextUpdate

    /***
     * Yahoo! Developers Network 2019 documentation
     * url: https://developer.yahoo.com/weather/documentation.html#java
     *
     * @return A {@code String} representation of JSON data
     */
    public static String getYahooWeatherData( String wxCity ) throws Exception
    {
        wxCity = wxCity.replace( " ", "%2B" ).replace( ",", "%2C" ); // add URL Encoding for two characters
        final String url = "https://weather-ydn-yql.media.yahoo.com/forecastrss";

        long timestamp = new Date().getTime() / 1000;
        byte[] nonce = new byte[ 32 ];
        Random rand = new Random();
        rand.nextBytes( nonce );
        String oauthNonce = new String( nonce ).replaceAll( "\\W", "" );

        List<String> parameters = new ArrayList<>();
        parameters.add( "oauth_consumer_key=" + yahooConsumerKey );
        parameters.add( "oauth_nonce=" + oauthNonce );
        parameters.add( "oauth_signature_method=HMAC-SHA1" );
        parameters.add( "oauth_timestamp=" + timestamp );
        parameters.add( "oauth_version=1.0" );
        // Make sure value is encoded
        parameters.add( "location=" + wxCity );
        parameters.add( "format=json" );
        Collections.sort( parameters );

        StringBuffer parametersList = new StringBuffer();
//        StringBuilder parametersList = new StringBuilder();

        for ( int i = 0; i < parameters.size(); i++ )
        {
            parametersList.append( String.format( "%s%s", ( ( i > 0 ) ? "&" : "" ), parameters.get( i )  ) );
        }// end of for loop

        String signatureString = "GET&" +
                URLEncoder.encode( url, "UTF-8" ) + "&" +
                URLEncoder.encode( parametersList.toString(), "UTF-8" );

        String signature;

        try
        {
            SecretKeySpec signingKey =
                    new SecretKeySpec( ( yahooConsumerSecret + "&" ).getBytes(), "HmacSHA1" );
            Mac mac = Mac.getInstance( "HmacSHA1" );
            mac.init( signingKey );
            byte[] rawHMAC = mac.doFinal( signatureString.getBytes() );
            Base64.Encoder encoder = Base64.getEncoder();
            signature = encoder.encodeToString( rawHMAC );
        }// end of try block
        catch ( Exception e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE,
                    "Unable to append signature.", TAG + "::getYahooWeatherData" );
            return null;
        }// end of catch block

        String authorizationLine = "OAuth " +
                "oauth_consumer_key=\"" + yahooConsumerKey + "\", " +
                "oauth_nonce=\"" + oauthNonce + "\", " +
                "oauth_timestamp=\"" + timestamp + "\", " +
                "oauth_signature_method=\"HMAC-SHA1\", " +
                "oauth_signature=\"" + signature + "\", " +
                "oauth_version=\"1.0\"";

        // The app id header of "Yahoo-App-Id" has been deprecated to "X-Yahoo-App-Id"
        HttpRequest request = new HttpRequest( URI.create( url + "?location=" + wxCity + "&format=json" ).toString() );
        request.withHeaders(
                "Authorization: " + authorizationLine,
                "X-Yahoo-App-Id: " + yahooAppId,
                "Content-Type: application/json"
        );

        request.prepare( HttpRequest.Method.GET );

        return request.sendAndReadString();
    }// end of method getYahooWeatherData

    private void loadDarkSkyWeather()
    {
        currentCountry.setLength( 0 );
        currentCountry.append( CityData.currentCityData.getCountryName() );

        currentCondition.setLength( 0 ); // reset
        currentCondition.append( UtilityMethod.toProperCase( darkSky.getCurrently().getSummary() ) );

        currentWindDirection.setLength( 0 );
        currentWindDirection.append( UtilityMethod.compassDirection(darkSky.getCurrently().getWindBearing() ) );

        currentHumidity.setLength( 0 );
        currentHumidity.append( Math.round( darkSky.getCurrently().getHumidity() * 100 ) );

        sunriseTime.setLength( 0 );
        sunriseTime.append( new SimpleDateFormat( "h:mm a", Locale.ENGLISH ).format(
                UtilityMethod.getDateTime( darkSky.getDaily().getData().get( 0 ).getSunriseTime() ) )
                .toUpperCase() );

        sunsetTime.setLength( 0 );
        sunsetTime.append( new SimpleDateFormat( "h:mm a", Locale.ENGLISH ).format(
                UtilityMethod.getDateTime( darkSky.getDaily().getData().get( 0 ).getSunsetTime() ) ).
                toUpperCase() );

        updateTemps( true ); // call update temps here
        formatWeatherCondition();

        remoteViews.setTextViewText( R.id.txvWeatherCondition,
                UtilityMethod.toProperCase( currentCondition.toString() ) );

        remoteViews.setTextViewText( R.id.txvWindReading, currentWindDirection +
                " " + currentWindSpeed + ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                " km/h" : " mph" ) );

        remoteViews.setTextViewText( R.id.txvWindReading,
                currentWindDirection +
                        " " + currentWindSpeed + ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                        " km/h" : " mph" ) );
        remoteViews.setTextViewText( R.id.txvHumidity,
                currentHumidity.toString() + "%" );

        // Update the current location and update time stamp
        String ts = new SimpleDateFormat( "E, MMM dd, h:mm a", Locale.ENGLISH ).format( new Date() );

        remoteViews.setTextViewText( R.id.txvLastUpdated, ts );
        remoteViews.setTextViewText( R.id.txvSunrise, sunriseTime.toString() );
        remoteViews.setTextViewText( R.id.txvSunset, sunsetTime.toString() );

        // Load current condition weather image
        Calendar rightNow = Calendar.getInstance();
        Calendar nightFall = Calendar.getInstance();
        Calendar sunUp = Calendar.getInstance();
        String sunsetTwenty4HourTime = new SimpleDateFormat( "yyyy-MM-dd", Locale.ENGLISH ).format( rightNow.getTime() )
                + " " + UtilityMethod.get24HourTime( sunsetTime.toString() );
        String sunriseTwenty4HourTime = new SimpleDateFormat( "yyyy-MM-dd", Locale.ENGLISH ).format( rightNow.getTime() )
                + " " + UtilityMethod.get24HourTime( sunriseTime.toString() );
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm", Locale.ENGLISH );
        Date rn = null;
        Date nf = null;
        Date su = null;

        try
        {
            rn = sdf.parse( sdf.format( rightNow.getTime() ) );
            nightFall.setTime( sdf.parse( sunsetTwenty4HourTime ) );
            nightFall.set( Calendar.MINUTE, Integer.parseInt(
                    sunsetTwenty4HourTime.split( ":" )[ 1 ].trim() ) );
            sunUp.setTime( sdf.parse( sunriseTwenty4HourTime ) );

            nf = sdf.parse( sdf.format( nightFall.getTime() ) );
            su = sdf.parse( sdf.format( sunUp.getTime() ) );
        } // end of try block
        catch ( ParseException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE , e.getMessage(),
                    TAG + "::loadDarkSkyWeather [line: "
                            + e.getStackTrace()[ 1 ].getLineNumber() + "]" );
        }// end of catch block

        String currentConditionIcon = null;

        if ( Objects.requireNonNull( rn ).equals( nf ) || rn.after( nf ) || rn.before( su ) )
        {
            if ( currentCondition.toString().toLowerCase().contains( "(night)" ) )
            {
                currentConditionIcon = UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() );
            }// end of if block
            else
            {
                if( UtilityMethod.weatherImages.containsKey( currentCondition.toString().toLowerCase() + " (night)" ) )
                {
                    currentConditionIcon = UtilityMethod.weatherImages.get(currentCondition.toString().toLowerCase() + " (night)");
                }// end of if block
                else
                {
                    currentConditionIcon = UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() );
                }// end of else block
            }// end of else block

            if( UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() ) == null )
            {
                // sometimes the JSON data received is incomplete so this has to be taken into account
                for ( Map.Entry<String, String> e : UtilityMethod.weatherImages.entrySet() )
                {
                    if ( e.getKey() .startsWith( currentCondition.toString().toLowerCase() ) )
                    {
                        currentConditionIcon =  UtilityMethod.weatherImages.get( e.getKey() ); // use the closest match
                        break; // exit the loop
                    }// end of if block
                }// end of for block

                // if a match still could not be found, use the not available icon
                if( currentConditionIcon == null )
                {
                    currentConditionIcon = "na.png";
                }// end of if block
            }// end of if block

            WeatherWidgetProvider.sunsetIconsInUse = true;
            WeatherWidgetProvider.sunriseIconsInUse = false;
        }// end of if block
        else
        {
            if( UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() ) == null )
            {
                // sometimes the JSON data received is incomplete so this has to be taken into account
                for ( Map.Entry<String, String> e : UtilityMethod.weatherImages.entrySet() )
                {
                    if ( e.getKey().startsWith( currentCondition.toString().toLowerCase() ) )
                    {
                        currentConditionIcon =  UtilityMethod.weatherImages.get( e.getKey() ); // use the closest match
                        currentCondition.setLength( 0 ); // reset
                        currentCondition.append( e.getKey() );
                        break; // exit the loop
                    }// end of if block
                }// end of for block

                // if a match still could not be found, use the not available icon
                if( currentConditionIcon == null )
                {
                    currentConditionIcon = "na.png";
                }// end of if block
            }// end of if block
            else
            {
                currentConditionIcon = UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() );
            }// end of else block

            WeatherWidgetProvider.sunriseIconsInUse = true;
            WeatherWidgetProvider.sunsetIconsInUse = false;
        }// end of else block

        loadWeatherIcon( R.id.imvCurrentCondition,
                "weather_images/" + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        // Five Day Forecast
        int i = 1;
        currentFiveDayForecast.clear(); // ensure that this list is clean

        for ( DarkSkyWeatherDataItem.Daily.Data wxForecast : darkSky.getDaily().getData() )
        {
            Date fxDate = UtilityMethod.getDateTime( wxForecast.getTime() );
            String fCondition = wxForecast.getSummary().toLowerCase();

            if ( fCondition.contains( "until" ) )
            {
                fCondition = fCondition.substring( 0, fCondition.indexOf( "until" ) - 1 ).trim();
            }// end of if block

            if ( fCondition.contains( "starting" ) )
            {
                fCondition = fCondition.substring( 0, fCondition.indexOf( "starting" ) - 1 ).trim();
            }// end of if block

            if ( fCondition.contains( "overnight" ) )
            {
                fCondition = fCondition.substring( 0, fCondition.indexOf( "overnight" ) - 1 ).trim();
            }// end of if block

            if ( fCondition.contains( "throughout" ) )
            {
                fCondition = fCondition.substring( 0, fCondition.indexOf( "throughout" ) - 1 ).trim();
            }// end of if block

            if ( fCondition.contains( " in " ) )
            {
                fCondition = fCondition.substring( 0, fCondition.indexOf( " in " ) - 1 ).trim();
            }// end of if block

            if( fCondition.toLowerCase().contains( "and" ) )
            {
                String[] conditions = fCondition.toLowerCase().split( "and" );

                fCondition = conditions[ 0 ].trim();
            }// end of if block

            fCondition = UtilityMethod.toProperCase( fCondition );

            int  fDay= this.getResources().getIdentifier("txvDay" + (i),
                    "id", this.getPackageName());
            int  fIcon= this.getResources().getIdentifier("imvDay" + (i) + "Icon",
                    "id", this.getPackageName());
            int  fHigh= this.getResources().getIdentifier("txvDay" + (i) + "Temps",
                    "id", this.getPackageName());

            remoteViews.setTextViewText(fDay, new SimpleDateFormat(
            "E d", Locale.ENGLISH ).format( fxDate ));

            // Load current forecast condition weather image
            if( fCondition.toLowerCase().contains( "(day)" ) )
            {
                fCondition = fCondition.replace( "(day)", "").trim();
            }// end of if block
            else if( fCondition.toLowerCase().contains( "(night)" ) )
            {
                fCondition = fCondition.replace( "(night)", "" ).trim();
            }// end of if block

            String fConditionIcon = null;

            if( UtilityMethod.weatherImages.get( fCondition.toLowerCase() ) == null )
            {
                // sometimes the JSON data received is incomplete so this has to be taken into account
                for ( Map.Entry<String, String> e : UtilityMethod.weatherImages.entrySet() )
                {
                    if ( e.getKey() .startsWith( fCondition.toLowerCase() ) )
                    {
                        fConditionIcon =  UtilityMethod.weatherImages.get( e.getKey() ); // use the closest match
                        fCondition = e.getKey();
                        break; // exit the loop
                    }// end of if block
                }// end of for block

                // if a match still could not be found, use the not available icon
                if( fConditionIcon == null )
                {
                    fConditionIcon = "na.png";
                }// end of if block
            }// end of if block
            else
            {
                fConditionIcon = UtilityMethod.weatherImages.get( fCondition.toLowerCase() );
            }// end of if block

            loadWeatherIcon( fIcon,
            "weather_images/" + WeatherLionApplication.iconSet + "/weather_" + fConditionIcon );

            currentFiveDayForecast.add(
                    new FiveDayForecast( fxDate, String.valueOf( Math.round( hl[ i - 1 ][ 0 ] ) ),
                            String.valueOf( Math.round( hl[ i - 1 ][ 1 ] ) ), fCondition ) );

            if ( i == 5 )
            {
                break;
            }// end of if block

            i++; // increment sentinel

        }// end of for each loop

        // if the code gets to here then all was loaded successfully
        WeatherWidgetProvider.dataLoadedSuccessfully = true;

        Map< String, Object > xmlMapData = new LinkedHashMap<>();
        xmlMapData.put( "providerName", WeatherLionApplication.DARK_SKY );
        xmlMapData.put( "datePublished", new Date() );
        xmlMapData.put( "cityName", currentCity.toString() );
        xmlMapData.put( "countryName",  currentCountry.toString() );
        xmlMapData.put( "currentConditions", currentCondition.toString() );
        xmlMapData.put( "currentTemperature", currentTemp.toString() );
        xmlMapData.put( "currentFeelsLikeTemperature", currentFeelsLikeTemp.toString() );
        xmlMapData.put( "currentHigh", currentHigh.toString() );
        xmlMapData.put( "currentLow", currentLow.toString() );
        xmlMapData.put( "currentWindSpeed", currentWindSpeed.toString() );
        xmlMapData.put( "currentWindDirection", currentWindDirection.toString() );
        xmlMapData.put( "currentHumidity", currentHumidity.toString() );
        xmlMapData.put( "sunriseTime", sunriseTime.toString() );
        xmlMapData.put( "sunsetTime", sunsetTime.toString() );
        xmlMapData.put( "fiveDayForecast", currentFiveDayForecast );

        String xmlJSON = new Gson().toJson( xmlMapData );

        Intent weatherXMLIntent = new Intent( this, WeatherDataXMLService.class );
        weatherXMLIntent.putExtra( WEATHER_XML_DATA, xmlJSON.trim() );
        this.startService( weatherXMLIntent );
    }// end of method loadDarkSkyWeather

    private void loadHereMapsWeather()
    {
        HereMapsWeatherDataItem.WeatherData.Observations.Location.Observation obs = hereWeatherWx.getObservations().getLocation().get( 0 )
                .getObservation().get( 0 );
        HereMapsWeatherDataItem.AstronomyData.Astronomic.Astronomy ast = hereWeatherAx.getAstronomy().getAstronomy().get( 0 );

        currentCountry.setLength( 0 );
        currentCountry.append( CityData.currentCityData.getCountryName() );

        currentCondition.setLength( 0 );
        currentCondition.append( obs.getIconName().contains( "_" ) ?
                UtilityMethod.toProperCase( obs.getIconName().replaceAll( "_", " " ) ) :
                UtilityMethod.toProperCase( obs.getIconName().replaceAll( "_", " " ) ) );

        currentWindDirection.setLength( 0 );
        currentWindDirection.append( obs.getWindDescShort() );

        currentWindSpeed.setLength( 0 );
        currentWindSpeed.append( obs.getWindSpeed() );

        currentHumidity.setLength( 0 );
        currentHumidity.append( Math.round( obs.getHumidity() ) );

        sunriseTime.setLength( 0 );
        sunriseTime.append( ast.getSunrise().toUpperCase() );

        sunsetTime.setLength( 0 );
        sunsetTime.append( ast.getSunset().toUpperCase() );
        List< HereMapsWeatherDataItem.ForecastData.DailyForecasts.ForecastLocation.Forecast > fdf =
                hereWeatherFx.getDailyForecasts().getForecastLocation().getForecast();

        updateTemps( true ); // call update temps here
        formatWeatherCondition();

        remoteViews.setTextViewText( R.id.txvWeatherCondition,
                UtilityMethod.toProperCase( currentCondition.toString() ) );

        remoteViews.setTextViewText( R.id.txvWindReading, currentWindDirection +
                " " + currentWindSpeed + ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                " km/h" : " mph" ) );

        remoteViews.setTextViewText( R.id.txvWindReading,
                currentWindDirection +
                        " " + currentWindSpeed + ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                        " km/h" : " mph" ) );
        remoteViews.setTextViewText( R.id.txvHumidity,
                currentHumidity.toString() + "%" );

        // Update the current location and update time stamp
        String ts = new SimpleDateFormat( "E, MMM dd, h:mm a", Locale.ENGLISH ).format( new Date() );

        remoteViews.setTextViewText( R.id.txvLastUpdated, ts );
        remoteViews.setTextViewText( R.id.txvSunrise, sunriseTime.toString() );
        remoteViews.setTextViewText( R.id.txvSunset, sunsetTime.toString() );

        // Load current condition weather image
        Calendar rightNow = Calendar.getInstance();
        Calendar nightFall = Calendar.getInstance();
        Calendar sunUp = Calendar.getInstance();
        String sunsetTwenty4HourTime = new SimpleDateFormat( "yyyy-MM-dd", Locale.ENGLISH ).format(
                rightNow.getTime() ) + " " + UtilityMethod.get24HourTime( sunsetTime.toString() );
        String sunriseTwenty4HourTime = new SimpleDateFormat( "yyyy-MM-dd", Locale.ENGLISH ).format(
                rightNow.getTime() ) + " " + UtilityMethod.get24HourTime( sunriseTime.toString() );
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm", Locale.ENGLISH );
        Date rn = null;
        Date nf = null;
        Date su = null;

        try
        {
            rn = sdf.parse( sdf.format( rightNow.getTime() ) );
            nightFall.setTime( sdf.parse( sunsetTwenty4HourTime ) );
            nightFall.set( Calendar.MINUTE, Integer.parseInt( sunsetTwenty4HourTime.split( ":" )[ 1 ].trim() ) );
            sunUp.setTime( sdf.parse( sunriseTwenty4HourTime ) );

            nf = sdf.parse( sdf.format( nightFall.getTime() ) );
            su = sdf.parse( sdf.format( sunUp.getTime() ) );
        } // end of try block
        catch ( ParseException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE , e.getMessage(),
                    TAG + "::loadHereMapsWeather [line: "
                            + e.getStackTrace()[ 1 ].getLineNumber() + "]" );
        }// end of catch block

        String currentConditionIcon = null;

        if ( Objects.requireNonNull( rn ).equals( nf ) || rn.after( nf ) || rn.before( su ) )
        {
            if ( currentCondition.toString().toLowerCase().contains( "(night)" ) )
            {
                currentConditionIcon =
                        UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() );
            }// end of if block
            else
            {
                if( UtilityMethod.weatherImages.containsKey(
                        currentCondition.toString().toLowerCase() + " (night)" ) )
                {
                    currentConditionIcon =
                            UtilityMethod.weatherImages.get(
                                    currentCondition.toString().toLowerCase() + " (night)" );
                }// end of if block
                else
                {
                    currentConditionIcon = UtilityMethod.weatherImages.get(
                            currentCondition.toString().toLowerCase() );
                }// end of else block
            }// end of else block

            if( UtilityMethod.weatherImages.get(
                    currentCondition.toString().toLowerCase() ) == null )
            {
                // sometimes the JSON data received is incomplete so this has to be taken into account
                for( Map.Entry<String, String> e : UtilityMethod.weatherImages.entrySet() )
                {
                    if ( e.getKey() .startsWith( currentCondition.toString().toLowerCase() ) )
                    {
                        currentConditionIcon =  UtilityMethod.weatherImages.get( e.getKey() ); // use the closest match
                        break; // exit the loop
                    }// end of if block
                }// end of for block

                // if a match still could not be found, use the not available icon
                if( currentConditionIcon == null )
                {
                    currentConditionIcon = "na.png";
                }// end of if block
            }// end of if block

            WeatherWidgetProvider.sunsetIconsInUse = true;
            WeatherWidgetProvider.sunriseIconsInUse = false;
        }// end of if block
        else
        {
            if( UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() ) == null )
            {
                // sometimes the JSON data received is incomplete so this has to be taken into account
                for ( Map.Entry<String, String> e : UtilityMethod.weatherImages.entrySet() )
                {
                    if ( e.getKey().startsWith( currentCondition.toString().toLowerCase() ) )
                    {
                        currentConditionIcon =  UtilityMethod.weatherImages.get( e.getKey() ); // use the closest match
                        currentCondition.setLength( 0 ); // reset
                        currentCondition.append( e.getKey() );
                        break; // exit the loop
                    }// end of if block
                }// end of for block

                // if a match still could not be found, use the not available icon
                if( currentConditionIcon == null )
                {
                    currentConditionIcon = "na.png";
                }// end of if block
            }// end of if block
            else
            {
                currentConditionIcon = UtilityMethod.weatherImages.get(
                        currentCondition.toString().toLowerCase() );
            }// end of else block

            WeatherWidgetProvider.sunsetIconsInUse = true;
            WeatherWidgetProvider.sunriseIconsInUse = false;
        }// end of else block

        loadWeatherIcon( R.id.imvCurrentCondition,
    "weather_images/" + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        // Five Day Forecast
        int i = 1;
        Date lastDate = new Date();
        SimpleDateFormat df;
        currentFiveDayForecast.clear(); // ensure that this list is clean

        // loop through the forecast data. only 5 days are needed
        for ( HereMapsWeatherDataItem.ForecastData.DailyForecasts.ForecastLocation.Forecast wxForecast : fdf )
        {
            df = new SimpleDateFormat( "yyyy-MM-dd", Locale.ENGLISH );
            Date fxDate = null;

            try
            {
                fxDate = df.parse( wxForecast.getUtcTime().substring( 0, 10 ) );
            }// end of try block
            catch ( ParseException pe )
            {
                UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE , pe.getMessage(),
                        TAG + "::loadHereMapsWeather [line: " +
                                pe.getStackTrace()[ 1 ].getLineNumber() + "]" );
            }// end of catch block

            if ( !df.format( fxDate ).equals( df.format( lastDate ) ) )
            {
                lastDate = fxDate;

                String fCondition =wxForecast.getIconName().contains( "_" ) ?
                        UtilityMethod.toProperCase( wxForecast.getIconName().replaceAll( "_", " " ) ) :
                        UtilityMethod.toProperCase( wxForecast.getIconName().replaceAll( "_", " " ) );
                String fDay =  new SimpleDateFormat( "E d", Locale.ENGLISH ).format( fxDate );

                if ( fCondition.contains( "until" ) )
                {
                    fCondition = fCondition.substring( 0, fCondition.indexOf( "until" ) - 1 ).trim();
                }// end of if block

                if ( fCondition.contains( "starting" ) )
                {
                    fCondition = fCondition.substring( 0, fCondition.indexOf( "starting" ) - 1 ).trim();
                }// end of if block

                if ( fCondition.contains( "overnight" ) )
                {
                    fCondition = fCondition.substring( 0, fCondition.indexOf( "overnight" ) - 1 ).trim();
                }// end of if block

                if ( fCondition.contains( "throughout" ) )
                {
                    fCondition = fCondition.substring( 0, fCondition.indexOf( "throughout" ) - 1 ).trim();
                }// end of if block

                if ( fCondition.contains( "in " ) )
                {
                    fCondition = fCondition.substring( 0, fCondition.indexOf( "in " ) - 1 ).trim();
                }// end of if block

                if ( fCondition.contains( "is " ) )
                {
                    int len = fCondition.length();
                    fCondition = fCondition.substring( fCondition.indexOf( "is " ) + 3, len ).trim();
                }// end of if block

                if( fCondition.toLowerCase().contains( "and" ) )
                {
                    String[] conditions = fCondition.toLowerCase().split( "and" );

                    fCondition = conditions[ 0 ].trim();
                }// end of if block

                fCondition = UtilityMethod.toProperCase( fCondition );

                int  fDayView = this.getResources().getIdentifier("txvDay" + (i),
                        "id", this.getPackageName());
                int  fIcon = this.getResources().getIdentifier("imvDay" + (i) + "Icon",
                        "id", this.getPackageName());
                int  fHigh = this.getResources().getIdentifier("txvDay" + (i) + "Temps",
                        "id", this.getPackageName() );

                remoteViews.setTextViewText( fDayView, new SimpleDateFormat(
                "E d", Locale.ENGLISH ).format( fxDate ) );

                // Load current forecast condition weather image
                if( fCondition.toLowerCase().contains( "(day)") )
                {
                    fCondition = fCondition.replace( "(day)", "").trim();
                }// end of if block
                else if( fCondition.toLowerCase().contains( "(night)" ) )
                {
                    fCondition = fCondition.replace( "(night)", "" ).trim();
                }// end of if block

                String fConditionIcon = null;

                if( UtilityMethod.weatherImages.get( fCondition.toLowerCase() ) == null )
                {
                    // sometimes the JSON data received is incomplete so this has to be taken into account
                    for ( Map.Entry<String, String> e : UtilityMethod.weatherImages.entrySet() )
                    {
                        if ( e.getKey() .startsWith( fCondition.toLowerCase() ) )
                        {
                            fConditionIcon =  UtilityMethod.weatherImages.get( e.getKey() ); // use the closest match
                            fCondition = e.getKey();
                            break; // exit the loop
                        }// end of if block
                    }// end of for block

                    // if a match still could not be found, use the not available icon
                    if( fConditionIcon == null )
                    {
                        fConditionIcon = "na.png";
                    }// end of if block
                }// end of if block
                else
                {
                    fConditionIcon = UtilityMethod.weatherImages.get( fCondition.toLowerCase() );
                }// end of if block

                loadWeatherIcon( fIcon,
            "weather_images/" + WeatherLionApplication.iconSet + "/weather_" + fConditionIcon );

                currentFiveDayForecast.add(
                        new FiveDayForecast( fxDate, String.valueOf( Math.round( hl[ i - 1 ][ 0 ] ) ),
                                String.valueOf( Math.round( hl[ i - 1 ][ 1 ] ) ), fCondition ) );

                if ( i == 5 )
                {
                    break;
                }// end of if block

                i++;
            }// end of if block
        }// end of for each loop

        // if the code gets to here then all was loaded successfully
        WeatherWidgetProvider.dataLoadedSuccessfully = true;

        Map< String, Object > xmlMapData = new LinkedHashMap<>();
        xmlMapData.put( "providerName", WeatherLionApplication.HERE_MAPS );
        xmlMapData.put( "datePublished", new Date() );
        xmlMapData.put( "cityName", currentCity.toString() );
        xmlMapData.put( "countryName",  currentCountry.toString() );
        xmlMapData.put( "currentConditions", currentCondition.toString() );
        xmlMapData.put( "currentTemperature", currentTemp.toString() );
        xmlMapData.put( "currentFeelsLikeTemperature", currentFeelsLikeTemp.toString() );
        xmlMapData.put( "currentHigh", currentHigh.toString() );
        xmlMapData.put( "currentLow", currentLow.toString() );
        xmlMapData.put( "currentWindSpeed", currentWindSpeed.toString() );
        xmlMapData.put( "currentWindDirection", currentWindDirection.toString() );
        xmlMapData.put( "currentHumidity", currentHumidity.toString() );
        xmlMapData.put( "sunriseTime", sunriseTime.toString() );
        xmlMapData.put( "sunsetTime", sunsetTime.toString() );
        xmlMapData.put( "fiveDayForecast", currentFiveDayForecast );

        String xmlJSON = new Gson().toJson( xmlMapData );

        Intent weatherXMLIntent = new Intent( this, WeatherDataXMLService.class );
        weatherXMLIntent.putExtra( WEATHER_XML_DATA, xmlJSON.trim() );
        this.startService( weatherXMLIntent );
    }// end of method loadHereMapsWeather

    private void loadOpenWeather()
    {
        currentCondition.setLength( 0 ); // reset
        currentCondition.append( openWeatherWx.getWeather().get( 0 ).getDescription() );

        currentWindDirection.setLength( 0 ); // reset
        currentWindDirection.append( UtilityMethod.compassDirection( openWeatherWx.getWind().getDeg() ) );

        currentHumidity.setLength( 0 );
        currentHumidity.append( Math.round( openWeatherWx.getMain().getHumidity() ) );

        sunriseTime.setLength( 0 );
        sunriseTime.append( new SimpleDateFormat( "h:mm a", Locale.ENGLISH ).format(
                UtilityMethod.getDateTime( openWeatherWx.getSys().getSunrise() ) ).toUpperCase() );

        sunsetTime.setLength( 0 );
        sunsetTime.append( new SimpleDateFormat( "h:mm a", Locale.ENGLISH ).format(
                UtilityMethod.getDateTime( openWeatherWx.getSys().getSunset() ) ).toUpperCase() );
        List< OpenWeatherMapWeatherDataItem.ForecastData.Data > fdf = openWeatherFx.getList();

        updateTemps( true ); // call update temps here
        formatWeatherCondition();

        remoteViews.setTextViewText( R.id.txvWeatherCondition,
                UtilityMethod.toProperCase( currentCondition.toString() ) );

        remoteViews.setTextViewText( R.id.txvWindReading, currentWindDirection +
                " " + currentWindSpeed + ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                " km/h" : " mph" ) );

        remoteViews.setTextViewText( R.id.txvHumidity,
                currentHumidity.toString() + "%" );

        // Update the current location and update time stamp
        String ts = new SimpleDateFormat( "E, MMM dd, h:mm a", Locale.ENGLISH ).format( new Date() );

        remoteViews.setTextViewText( R.id.txvLastUpdated, ts );
        remoteViews.setTextViewText( R.id.txvSunrise, sunriseTime.toString() );
        remoteViews.setTextViewText( R.id.txvSunset, sunsetTime.toString() );

        // Load current condition weather image
        Calendar rightNow = Calendar.getInstance();
        Calendar nightFall = Calendar.getInstance();
        Calendar sunUp = Calendar.getInstance();
        String sunsetTwenty4HourTime = new SimpleDateFormat( "yyyy-MM-dd", Locale.ENGLISH ).format( rightNow.getTime() )
                + " " + UtilityMethod.get24HourTime( sunsetTime.toString() );
        String sunriseTwenty4HourTime = new SimpleDateFormat( "yyyy-MM-dd", Locale.ENGLISH ).format( rightNow.getTime() )
                + " " + UtilityMethod.get24HourTime( sunriseTime.toString() );
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm", Locale.ENGLISH );
        Date rn = null;
        Date nf = null;
        Date su = null;

        try
        {
            rn = sdf.parse( sdf.format( rightNow.getTime() ) );
            nightFall.setTime( sdf.parse( sunsetTwenty4HourTime ) );
            nightFall.set( Calendar.MINUTE, Integer.parseInt( sunsetTwenty4HourTime.split( ":" )[ 1 ].trim() ) );
            sunUp.setTime( sdf.parse( sunriseTwenty4HourTime ) );

            nf = sdf.parse( sdf.format( nightFall.getTime() ) );
            su = sdf.parse( sdf.format( sunUp.getTime() ) );
        } // end of try block
        catch ( ParseException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE , e.getMessage(),
                    TAG + "::loadOpenWeather [line: " +
                            e.getStackTrace()[ 1 ].getLineNumber() + "]" );
        }// end of catch block

        String currentConditionIcon = null;

        if ( Objects.requireNonNull( rn ).equals( nf ) || rn.after( nf ) || rn.before( su ) )
        {
            if ( currentCondition.toString().toLowerCase().contains( "(night)" ) )
            {
                currentConditionIcon = UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() );
            }// end of if block
            else
            {
                if( UtilityMethod.weatherImages.containsKey(
                        currentCondition.toString().toLowerCase() + " (night)" ) )
                {
                    currentConditionIcon = UtilityMethod.weatherImages.get(
                            currentCondition.toString().toLowerCase() + " (night)");
                }// end of if block
                else
                {
                    currentConditionIcon = UtilityMethod.weatherImages.get(
                            currentCondition.toString().toLowerCase() );
                }// end of else block
            }// end of else block

            if( UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() ) == null )
            {
                // sometimes the JSON data received is incomplete so this has to be taken into account
                for ( Map.Entry<String, String> e : UtilityMethod.weatherImages.entrySet() )
                {
                    if ( e.getKey() .startsWith( currentCondition.toString().toLowerCase() ) )
                    {
                        currentConditionIcon =  UtilityMethod.weatherImages.get( e.getKey() ); // use the closest match
                        break; // exit the loop
                    }// end of if block
                }// end of for block

                // if a match still could not be found, use the not available icon
                if( currentConditionIcon == null )
                {
                    currentConditionIcon = "na.png";
                }// end of if block
            }// end of if block

            WeatherWidgetProvider.sunsetIconsInUse = true;
            WeatherWidgetProvider.sunriseIconsInUse = false;
        }// end of if block
        else
        {
            if( UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() ) == null )
            {
                // sometimes the JSON data received is incomplete so this has to be taken into account
                for ( Map.Entry<String, String> e : UtilityMethod.weatherImages.entrySet() )
                {
                    if ( e.getKey() .startsWith( currentCondition.toString().toLowerCase() ) )
                    {
                        currentConditionIcon =  UtilityMethod.weatherImages.get( e.getKey() ); // use the closest match
                        currentCondition.setLength( 0 ); // reset
                        currentCondition.append( e.getKey() );
                        break; // exit the loop
                    }// end of if block
                }// end of for block

                // if a match still could not be found, use the not available icon
                if( currentConditionIcon == null )
                {
                    currentConditionIcon = "na.png";
                }// end of if block
            }// end of if block
            else
            {
                currentConditionIcon = UtilityMethod.weatherImages.get(
                        currentCondition.toString().toLowerCase() );
            }// end of else block

            WeatherWidgetProvider.sunsetIconsInUse = true;
            WeatherWidgetProvider.sunriseIconsInUse = false;
        }// end of else block

        loadWeatherIcon( R.id.imvCurrentCondition,
    "weather_images/" + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        // Five Day Forecast
        int i = 1;
        Date lastDate = new Date();
        SimpleDateFormat df = new SimpleDateFormat( "MMM dd, yyyy", Locale.ENGLISH );
        currentFiveDayForecast.clear(); // ensure that this list is clean

        // loop through the forecast data. only 5 days are needed
        for ( OpenWeatherMapWeatherDataItem.ForecastData.Data wxForecast : fdf )
        {
            Date fxDate = UtilityMethod.getDateTime( wxForecast.getDt() );

            if ( !df.format( fxDate ).equals( df.format( lastDate ) ) )
            {
                lastDate = UtilityMethod.getDateTime(wxForecast.getDt() );
                String fCondition = wxForecast.getWeather().get( 0 ).getDescription();
                String fDay =  new SimpleDateFormat( "E d", Locale.ENGLISH ).format( fxDate );

                if ( fCondition.contains( "until" ) )
                {
                    fCondition = fCondition.substring( 0, fCondition.indexOf( "until" ) - 1 ).trim();
                }// end of if block

                if ( fCondition.contains( "starting" ) )
                {
                    fCondition = fCondition.substring( 0, fCondition.indexOf( "starting" ) - 1 ).trim();
                }// end of if block

                if ( fCondition.contains( "overnight" ) )
                {
                    fCondition = fCondition.substring( 0, fCondition.indexOf( "overnight" ) - 1 ).trim();
                }// end of if block

                if ( fCondition.contains( "throughout" ) )
                {
                    fCondition = fCondition.substring( 0, fCondition.indexOf( "throughout" ) - 1 ).trim();
                }// end of if block

                if ( fCondition.contains( "in " ) )
                {
                    fCondition = fCondition.substring( 0, fCondition.indexOf( "in " ) - 1 ).trim();
                }// end of if block

                if ( fCondition.contains( "is " ) )
                {
                    int len = fCondition.length();
                    fCondition = fCondition.substring( fCondition.indexOf( "is " ) + 3, len ).trim();
                }// end of if block

                if( fCondition.toLowerCase().contains( "and" ) )
                {
                    String[] conditions = fCondition.toLowerCase().split( "and" );

                    fCondition = conditions[ 0 ].trim();
                }// end of if block

                fCondition = UtilityMethod.toProperCase( fCondition );

                int  fDayView= this.getResources().getIdentifier( "txvDay" + (i),
                        "id", this.getPackageName() );
                int  fIcon= this.getResources().getIdentifier( "imvDay" + (i) + "Icon",
                        "id", this.getPackageName() );
                int  fHigh= this.getResources().getIdentifier( "txvDay" + (i) + "Temps",
                        "id", this.getPackageName() );

                remoteViews.setTextViewText( fDayView,  fDay );

                // Load current forecast condition weather image
                if( fCondition.toLowerCase().contains( "(day)") )
                {
                    fCondition = fCondition.replace( "(day)", "").trim();
                }// end of if block
                else if( fCondition.toLowerCase().contains( "(night)" ) )
                {
                    fCondition = fCondition.replace( "(night)", "" ).trim();
                }// end of if block

                String fConditionIcon = null;

                if( UtilityMethod.weatherImages.get( fCondition.toLowerCase() ) == null )
                {
                    // sometimes the JSON data received is incomplete so this has to be taken into account
                    for ( Map.Entry<String, String> e : UtilityMethod.weatherImages.entrySet() )
                    {
                        if ( e.getKey() .startsWith( fCondition.toLowerCase() ) )
                        {
                            fConditionIcon =  UtilityMethod.weatherImages.get( e.getKey() ); // use the closest match
                            fCondition = e.getKey();
                            break; // exit the loop
                        }// end of if block
                    }// end of for block

                    // if a match still could not be found, use the not available icon
                    if( fConditionIcon == null )
                    {
                        fConditionIcon = "na.png";
                    }// end of if block
                }// end of if block
                else
                {
                    fConditionIcon = UtilityMethod.weatherImages.get( fCondition.toLowerCase() );
                }// end of if block

                loadWeatherIcon( fIcon,
                "weather_images/" + WeatherLionApplication.iconSet + "/weather_" + fConditionIcon );

                currentFiveDayForecast.add(
                        new FiveDayForecast( fxDate, String.valueOf( Math.round( hl[ i - 1 ][ 0 ] ) ),
                                String.valueOf( Math.round( hl[ i - 1 ][ 1 ] ) ), fCondition ) );

                if ( i == 5 )
                {
                    break;
                }// end of if block

                i++;
            }// end of if block
        }// end of for each loop

        // if the code gets to here then all was loaded successfully
        WeatherWidgetProvider.dataLoadedSuccessfully = true;

        Map< String, Object > xmlMapData = new LinkedHashMap<>();
        xmlMapData.put( "providerName", WeatherLionApplication.OPEN_WEATHER );
        xmlMapData.put( "datePublished", new Date() );
        xmlMapData.put( "cityName", currentCity.toString() );
        xmlMapData.put( "countryName",  currentCountry.toString() );
        xmlMapData.put( "currentConditions", currentCondition.toString() );
        xmlMapData.put( "currentTemperature", currentTemp.toString() );
        xmlMapData.put( "currentFeelsLikeTemperature", currentFeelsLikeTemp.toString() );
        xmlMapData.put( "currentHigh", currentHigh.toString() );
        xmlMapData.put( "currentLow", currentLow.toString() );
        xmlMapData.put( "currentWindSpeed", currentWindSpeed.toString() );
        xmlMapData.put( "currentWindDirection", currentWindDirection.toString() );
        xmlMapData.put( "currentHumidity", currentHumidity.toString() );
        xmlMapData.put( "sunriseTime", sunriseTime.toString() );
        xmlMapData.put( "sunsetTime", sunsetTime.toString() );
        xmlMapData.put( "fiveDayForecast", currentFiveDayForecast );

        String xmlJSON = new Gson().toJson( xmlMapData );

        Intent weatherXMLIntent = new Intent( this, WeatherDataXMLService.class );
        weatherXMLIntent.putExtra( WEATHER_XML_DATA, xmlJSON.trim() );
        this.startService( weatherXMLIntent );
    }// end of method loadOpenWeather

    /**
     * Load Weather previously received from provider
     */
    private void loadPreviousWeatherData()
    {
        File previousWeatherData = new File( this.getFileStreamPath(
                WeatherLionApplication.WEATHER_DATA_XML ).toString() );
        loadingPreviousWeather =  true;

        // load the current background in use
        loadWidgetBackground();

        // check for previous weather data stored locally
        if( previousWeatherData.exists() )
        {
            currentCity.setLength( 0 );
            currentCity.append( WeatherLionApplication.storedData.getLocation().getCity() );

            currentCountry.setLength( 0 );
            currentCountry.append( WeatherLionApplication.storedData.getLocation().getCountry() );

            currentCondition.setLength( 0 ); // reset
            currentCondition.append( WeatherLionApplication.storedData.getCurrent().getCondition() );

            currentWindDirection.setLength( 0 ); // reset
            currentWindDirection.append( WeatherLionApplication.storedData.getWind().getWindDirection() );

            currentWindSpeed.setLength( 0 );
            currentWindSpeed.append( WeatherLionApplication.storedData.getWind().getWindSpeed() );

            currentHumidity.setLength( 0 );
            currentHumidity.append( WeatherLionApplication.storedData.getAtmosphere().getHumidity() );

            currentLocation = currentCity;

            sunriseTime.setLength( 0 );
            sunriseTime.append( WeatherLionApplication.storedData.getAstronomy().getSunrise().toUpperCase() );

            sunsetTime.setLength( 0 );
            sunsetTime.append(WeatherLionApplication.storedData.getAstronomy().getSunset().toUpperCase() );

        }// end of if block

        updateTemps( false ); // call update temps here
        formatWeatherCondition();

        // Some providers like Yahoo! loves to omit a zero on the hour mark example: 7:0 am
        if( sunriseTime.length() == 6 )
        {
            String[] ft = sunriseTime.toString().split( ":" );
            sunriseTime.setLength( 0 );
            sunriseTime.append( String.format( "%s:0%s", ft[ 0 ], ft[ 1 ] ) );
        }// end of if block
        else if( sunsetTime.length() == 6 )
        {
            String[] ft= sunsetTime.toString().split( ":" );
            sunsetTime.setLength( 0 );
            sunsetTime.append( String.format( "%s:0%s", ft[ 0 ], ft[ 1 ] ) );
        }// end if else if block

        // Update the current location
        remoteViews.setTextViewText( R.id.txvCurrentLocation,
                currentCity.substring( 0, currentCity.indexOf( "," ) ) );

        remoteViews.setTextViewText( R.id.txvWeatherCondition,
                UtilityMethod.toProperCase( currentCondition.toString() ) );

        remoteViews.setTextViewText( R.id.txvWindReading, currentWindDirection +
                " " + currentWindSpeed + ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                " km/h" : " mph" ) );

        // Yr's Weather Service does not track humidity
        if( currentHumidity.toString().length() == 0 ) currentHumidity.append( "0" );

        currentHumidity = currentHumidity.toString().contains( "%" )
                ? new StringBuilder( currentHumidity.toString().replaceAll( "%", "" ) )
                : currentHumidity; // remove before parsing

        remoteViews.setTextViewText( R.id.txvHumidity,
                Math.round( Float.parseFloat( currentHumidity.toString() ) )
                        + ( !currentHumidity.toString().contains( "%" ) ? "%" : "" ) );

        Date timeUpdated = null;

        try
        {
            timeUpdated = new SimpleDateFormat( "EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH ).parse(
                    Objects.requireNonNull( WeatherLionApplication.storedData.getProvider().getDate() ) );
        }// end of try block
        catch ( ParseException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE , e.getMessage(),
                    TAG + "::loadPreviousWeather [line: " + e.getStackTrace()[1].getLineNumber()+ "]" );
        }// end of catch block

        // Update the current location and update time stamp
        String ts = new SimpleDateFormat( "E, MMM dd, h:mm a", Locale.ENGLISH ).format( timeUpdated );                

        remoteViews.setTextViewText( R.id.txvLastUpdated, ts );
        remoteViews.setTextViewText( R.id.txvSunrise, sunriseTime.toString() );
        remoteViews.setTextViewText( R.id.txvSunset, sunsetTime.toString() );

        // Load current condition weather image
        Calendar rightNow = Calendar.getInstance();
        Calendar nightFall = Calendar.getInstance();
        String twenty4HourTime = new SimpleDateFormat( "yyyy-MM-dd",
                Locale.ENGLISH ).format( rightNow.getTime() )
                + " " + UtilityMethod.get24HourTime( sunsetTime.toString() );

        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm", Locale.ENGLISH );
        Date rn = null;
        Date nf = null;

        try
        {
            rn = sdf.parse( sdf.format( rightNow.getTime() ) );
            nightFall.setTime( sdf.parse( twenty4HourTime ) );
            nightFall.set( Calendar.MINUTE, Integer.parseInt( twenty4HourTime.split( ":" )[ 1 ].trim() ) );
            nf = sdf.parse( sdf.format( nightFall.getTime() ) );
        } // end of try block
        catch ( ParseException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE , e.getMessage(),
                    TAG + "::loadPreviousWeather [line: " + e.getStackTrace()[1].getLineNumber()+ "]" );
        }// end of catch block

        String currentConditionIcon;

        if ( Objects.requireNonNull( rn ).equals( nf ) || rn.after( nf ) )
        {
            if ( currentCondition.toString().toLowerCase().contains( "(night)" ) )
            {
                currentConditionIcon = UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() );
            }// end of if block
            else
            {
                if( UtilityMethod.weatherImages.containsKey( currentCondition.toString().toLowerCase() + " (night)" ) )
                {
                    currentConditionIcon = UtilityMethod.weatherImages.get(
                            currentCondition.toString().toLowerCase() + " (night)" );
                }// end of if block
                else
                {
                    currentConditionIcon = UtilityMethod.weatherImages.get(
                            currentCondition.toString().toLowerCase() );
                }// end of else block
            }// end of else block
        }// end of if block
        else
        {
            currentConditionIcon = UtilityMethod.weatherImages.get(
                    currentCondition.toString().toLowerCase() );
        }// end of else block

        currentConditionIcon =  UtilityMethod.weatherImages.get(
                currentCondition.toString().toLowerCase() ) == null ?
                "na.png" :
                currentConditionIcon;

        loadWeatherIcon( R.id.imvCurrentCondition,
    "weather_images/" + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        SimpleDateFormat df = new SimpleDateFormat( "MMMM dd, yyyy", Locale.ENGLISH );

        int x = 0;

        for ( int i = 0; i < WeatherLionApplication.storedData.getDailyForecast().size(); i++ )
        {
            x++;
            LastWeatherData.WeatherData.DailyForecast.DayForecast wxDayForecast =
                    WeatherLionApplication.storedData.getDailyForecast().get( i );
            Date forecastDate = null;

            try
            {
                forecastDate = new SimpleDateFormat( "EEE MMM dd HH:mm:ss z yyyy",
                        Locale.ENGLISH ).parse( wxDayForecast.getDate() );
            }// end of try block
            catch ( ParseException e )
            {
                UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, "Couldn't parse the forecast date!",
                        TAG + "::loadPreviousWeather"  );
            }// end of catch block

            int  fDay = this.getResources().getIdentifier( "txvDay" + (i + 1),
                    "id", this.getPackageName() );
            int  fIcon = this.getResources().getIdentifier( "imvDay" + (i + 1) + "Icon",
                    "id", this.getPackageName() );
            int  fHigh = this.getResources().getIdentifier( "txvDay" + (i + 1) + "Temps",
                    "id", this.getPackageName() );

            remoteViews.setTextViewText( fDay, new SimpleDateFormat( "E d", Locale.ENGLISH ).format( forecastDate ) );

            // Load current forecast condition weather image
            String fCondition = wxDayForecast.getCondition();

            if( fCondition.toLowerCase().contains( "(day)" ) )
            {
                fCondition = fCondition.replace( "(day)", "" ).trim();
            }// end of if block
            else if( fCondition.toLowerCase().contains( "(night)" ) )
            {
                fCondition = fCondition.replace( "(night)", "" ).trim();
            }// end of if block

            String fConditionIcon
                    = UtilityMethod.weatherImages.get( fCondition.toLowerCase() ) == null
                    ? "na.png" : UtilityMethod.weatherImages.get( fCondition.toLowerCase() );

            loadWeatherIcon( fIcon,
        "weather_images/" + WeatherLionApplication.iconSet + "/weather_" + fConditionIcon );

            currentFiveDayForecast.add(
                    new FiveDayForecast( forecastDate,
                            String.valueOf( wxDayForecast.getHighTemperature() ),
                            String.valueOf( wxDayForecast.getLowTemperature() ),
                            fCondition ) );

            if( i == 4 )
            {
                break;
            }// end of if block
        }// end of for loop

        // Update the weather provider image view and text view
        String providerIcon = String.format( "%s%s%s", "icons/",
                WeatherLionApplication.storedData.getProvider().getName().toLowerCase(), ".png" );

        loadWeatherIcon( R.id.imvWeatherProviderLogo, providerIcon );
        remoteViews.setTextViewText( R.id.txvProvider,
                WeatherLionApplication.storedPreferences.getProvider() );

        if( UtilityMethod.refreshRequested )
        {
            UtilityMethod.refreshRequested = false;
        }// end of if block

        WeatherLionApplication.localWeatherDataAvailable = true; // indicate that old weather data is being used
    }// end of method loadPreviousWeatherData

    private void loadWeatherBitWeather()
    {
        currentCountry.setLength( 0 );
        currentCountry.append( CityData.currentCityData.getCountryName() );

        currentCondition.setLength( 0 ); // reset
        currentCondition.append( UtilityMethod.toProperCase(
                weatherBitWx.getData().get( 0 ).getWeather().getDescription() ) );

        currentWindDirection.setLength( 0 );
        currentWindDirection.append( weatherBitWx.getData().get( 0 ).getWind_cdir() );

        currentHumidity.setLength( 0 );
        currentHumidity.append( Math.round( weatherBitWx.getData().get( 0 ).getRh() ) );

        // Weather seems to be in a time-zone that is four hours ahead of Eastern Standard Time
        // They do not supply that information though.
        int tzOffset = 5;

        sunriseTime.setLength( 0 );
        sunriseTime.append( UtilityMethod.get12HourTime(
                Integer.parseInt( weatherBitWx.getData().get( 0 ).getSunrise().split( ":" )[ 0 ] )
                        - tzOffset, Integer.parseInt(
                        weatherBitWx.getData().get( 0 ).getSunrise().split( ":" )[ 1 ] ) ) );

        sunsetTime.setLength( 0 );
        sunsetTime.append( UtilityMethod.get12HourTime(
                Integer.parseInt( weatherBitWx.getData().get( 0 ).getSunset().split( ":" )[ 0 ] )
                        - tzOffset, Integer.parseInt(
                        weatherBitWx.getData().get( 0 ).getSunset().split( ":" )[ 1 ] ) ) );

        List< WeatherBitWeatherDataItem.SixteenDayForecastData.Data > fdf = weatherBitFx.getData();

        // call update temps here
        updateTemps( true );
        formatWeatherCondition();

        remoteViews.setTextViewText( R.id.txvWeatherCondition,
                UtilityMethod.toProperCase( currentCondition.toString() ) );

        remoteViews.setTextViewText( R.id.txvWindReading, currentWindDirection +
                " " + currentWindSpeed + ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                " km/h" : " mph" ) );
        // Update the current location and update time stamp
        String ts = new SimpleDateFormat( "E, MMM dd, h:mm a", Locale.ENGLISH ).format( new Date() );

        remoteViews.setTextViewText( R.id.txvLastUpdated, ts );
        remoteViews.setTextViewText( R.id.txvHumidity,currentHumidity.toString() + "%" );
        remoteViews.setTextViewText( R.id.txvSunrise, sunriseTime.toString() );
        remoteViews.setTextViewText( R.id.txvSunset, sunsetTime.toString() );

        // Load current condition weather image
        Calendar rightNow = Calendar.getInstance();
        Calendar nightFall = Calendar.getInstance();
        Calendar sunUp = Calendar.getInstance();
        String sunsetTwenty4HourTime = new SimpleDateFormat( "yyyy-MM-dd", Locale.ENGLISH ).format(
                rightNow.getTime() ) + " " + UtilityMethod.get24HourTime( sunsetTime.toString() );
        String sunriseTwenty4HourTime = new SimpleDateFormat( "yyyy-MM-dd", Locale.ENGLISH ).format(
                rightNow.getTime() ) + " " + UtilityMethod.get24HourTime( sunriseTime.toString() );
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm", Locale.ENGLISH );
        Date rn = null;
        Date nf = null;
        Date su = null;

        try
        {
            rn = sdf.parse( sdf.format( rightNow.getTime() ) );
            nightFall.setTime( sdf.parse( sunsetTwenty4HourTime ) );
            nightFall.set( Calendar.MINUTE, Integer.parseInt( sunsetTwenty4HourTime.split( ":" )[ 1 ].trim() ) );
            sunUp.setTime( sdf.parse( sunriseTwenty4HourTime ) );

            nf = sdf.parse( sdf.format( nightFall.getTime() ) );
            su = sdf.parse( sdf.format( sunUp.getTime() ) );
        } // end of try block
        catch ( ParseException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE , e.getMessage(),
                    TAG + "::loadWeatherBitWeather [line: " +
                            e.getStackTrace()[ 1 ].getLineNumber() + "]" );
        }// end of catch block

        String currentConditionIcon = null;

        if ( Objects.requireNonNull( rn ).equals( nf ) || rn.after( nf ) || rn.before( su ) )
        {
            if ( currentCondition.toString().toLowerCase().contains( "(night)" ) )
            {
                currentConditionIcon = UtilityMethod.weatherImages.get(
                        currentCondition.toString().toLowerCase() );
            }// end of if block
            else
            {
                if( UtilityMethod.weatherImages.containsKey( currentCondition.toString().toLowerCase() + " (night)" ) )
                {
                    currentConditionIcon = UtilityMethod.weatherImages.get(
                            currentCondition.toString().toLowerCase() + " (night)" );
                }// end of if block
                else
                {
                    currentConditionIcon = UtilityMethod.weatherImages.get(
                            currentCondition.toString().toLowerCase() );
                }// end of else block
            }// end of else block

            if( UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() ) == null )
            {
                // sometimes the JSON data received is incomplete so this has to be taken into account
                for ( Map.Entry<String, String> e : UtilityMethod.weatherImages.entrySet() )
                {
                    if ( e.getKey() .startsWith( currentCondition.toString().toLowerCase() ) )
                    {
                        currentConditionIcon =  UtilityMethod.weatherImages.get( e.getKey() ); // use the closest match
                        break; // exit the loop
                    }// end of if block
                }// end of for block

                // if a match still could not be found, use the not available icon
                if( currentConditionIcon == null )
                {
                    currentConditionIcon = "na.png";
                }// end of if block
            }// end of if block

            WeatherWidgetProvider.sunsetIconsInUse = true;
            WeatherWidgetProvider.sunriseIconsInUse = false;
        }// end of if block
        else
        {
            if( UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() ) == null )
            {
                // sometimes the JSON data received is incomplete so this has to be taken into account
                for ( Map.Entry<String, String> e : UtilityMethod.weatherImages.entrySet() )
                {
                    if ( e.getKey() .startsWith( currentCondition.toString().toLowerCase() ) )
                    {
                        currentConditionIcon =  UtilityMethod.weatherImages.get( e.getKey() ); // use the closest match
                        currentCondition.setLength( 0 ); // reset
                        currentCondition.append( e.getKey() );
                        break; // exit the loop
                    }// end of if block
                }// end of for block

                // if a match still could not be found, use the not available icon
                if( currentConditionIcon == null )
                {
                    currentConditionIcon = "na.png";
                }// end of if block
            }// end of if block
            else
            {
                currentConditionIcon = UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() );
            }// end of else block

            WeatherWidgetProvider.sunsetIconsInUse = true;
            WeatherWidgetProvider.sunriseIconsInUse = false;
        }// end of else block

        loadWeatherIcon( R.id.imvCurrentCondition,
    "weather_images/" + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        // Five Day Forecast
        int currentHour = Integer.parseInt( new SimpleDateFormat( "h", Locale.ENGLISH ).format( new Date() ) );
        int i = 1;
        currentFiveDayForecast.clear(); // ensure that this list is clean

        // loop through the 16 day forecast data. only 5 days are needed
        for ( WeatherBitWeatherDataItem.SixteenDayForecastData.Data wxForecast : fdf )
        {
            Date fxDate = null;
            SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd", Locale.ENGLISH );
            String dt = wxForecast.getDatetime();

            try
            {
                fxDate = df.parse( dt );
            }// end of try block
            catch ( ParseException e )
            {
                UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, "Unable to parse date string!",
                        TAG + "::loadWeatherBitWeather" );
                UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                        TAG + "::loadWeatherBitWeather");
            }// end of catch block

            if ( Objects.requireNonNull( fxDate ).after( new Date() ) )
            {
                String fCondition = wxForecast.getWeather().getDescription();

                if ( fxDate.after( new Date() ) )
                {
                    if ( fCondition.contains( "until" ) )
                    {
                        fCondition = fCondition.substring( 0, fCondition.indexOf( "until" ) - 1 ).trim();
                    }// end of if block

                    if ( fCondition.contains( "starting" ) )
                    {
                        fCondition = fCondition.substring( 0, fCondition.indexOf( "starting" ) - 1 ).trim();
                    }// end of if block

                    if ( fCondition.contains( "overnight" ) )
                    {
                        fCondition = fCondition.substring( 0, fCondition.indexOf( "overnight" ) - 1 ).trim();
                    }// end of if block

                    if ( fCondition.contains( "throughout" ) )
                    {
                        fCondition = fCondition.substring( 0, fCondition.indexOf( "throughout" ) - 1 ).trim();
                    }// end of if block

                    if ( fCondition.contains( "in " ) )
                    {
                        fCondition = fCondition.substring( 0, fCondition.indexOf( "in " ) - 1 ).trim();
                    }// end of if block

                    if ( fCondition.contains( "is " ) )
                    {
                        int len = fCondition.length();
                        fCondition = fCondition.substring( fCondition.indexOf( "is " ) + 3, len ).trim();
                    }// end of if block

                    if( fCondition.toLowerCase().contains( "and" ) )
                    {
                        String[] conditions = fCondition.toLowerCase().split( "and" );

                        fCondition = conditions[ 0 ].trim();
                    }// end of if block

                    fCondition = UtilityMethod.toProperCase( fCondition );

                    int  fDay = this.getResources().getIdentifier( "txvDay" + (i),
                            "id", this.getPackageName() );
                    int  fIcon = this.getResources().getIdentifier("imvDay" + (i) + "Icon",
                            "id", this.getPackageName() );
                    int  fHigh = this.getResources().getIdentifier("txvDay" + (i) + "Temps",
                            "id", this.getPackageName() );

                    remoteViews.setTextViewText( fDay, new SimpleDateFormat(
                        "E d", Locale.ENGLISH ).format( fxDate ) );

                    // Load current forecast condition weather image
                    if(fCondition.toLowerCase().contains( "(day)" ) )
                    {
                        fCondition = fCondition.replace( "(day)", "").trim();
                    }// end of if block
                    else if(fCondition.toLowerCase().contains( "(night)" ) )
                    {
                        fCondition = fCondition.replace( "(night)", "" ).trim();
                    }// end of if block

                    String fConditionIcon = null;

                    if( UtilityMethod.weatherImages.get( fCondition.toLowerCase() ) == null )
                    {
                        // sometimes the JSON data received is incomplete so this has to be taken into account
                        for ( Map.Entry<String, String> e : UtilityMethod.weatherImages.entrySet() )
                        {
                            if ( e.getKey() .startsWith( fCondition.toLowerCase() ) )
                            {
                                fConditionIcon =  UtilityMethod.weatherImages.get( e.getKey() ); // use the closest match
                                fCondition = e.getKey();
                                break; // exit the loop
                            }// end of if block
                        }// end of for block

                        // if a match still could not be found, use the not available icon
                        if( fConditionIcon == null )
                        {
                            fConditionIcon = "na.png";
                        }// end of if block
                    }// end of if block
                    else
                    {
                        fConditionIcon = UtilityMethod.weatherImages.get( fCondition.toLowerCase() );
                    }// end of if block

                    loadWeatherIcon( fIcon,
                "weather_images/" + WeatherLionApplication.iconSet + "/weather_" + fConditionIcon );

                    currentFiveDayForecast.add(
                            new FiveDayForecast( fxDate, String.valueOf( Math.round( hl[ i - 1 ][ 0 ] ) ),
                                    String.valueOf( Math.round( hl[ i - 1 ][ 1 ] ) ), fCondition ) );

                    i++; // increment sentinel

                    if( i == 6 )
                    {
                        break;
                    }// end of if block
                }// end of if block
            }// end of if block
        }// end of for each loop

        String ct;

        // sometimes weather bit includes symbols in their data
        if( currentTemp.toString().contains( DEGREES )  )
        {
            ct = currentTemp.substring( 0, currentTemp.indexOf( DEGREES ) ).trim();
        }// end of if block
        else
        {
            ct = currentTemp.toString();
        }// end of else block

        // if the code gets to here then all was loaded successfully
        WeatherWidgetProvider.dataLoadedSuccessfully = true;

        Map< String, Object > xmlMapData = new LinkedHashMap<>();
        xmlMapData.put( "providerName", WeatherLionApplication.WEATHER_BIT );
        xmlMapData.put( "datePublished", new Date() );
        xmlMapData.put( "cityName", currentCity.toString() );
        xmlMapData.put( "countryName",  currentCountry.toString() );
        xmlMapData.put( "currentConditions", currentCondition.toString() );
        xmlMapData.put( "currentTemperature", ct );
        xmlMapData.put( "currentFeelsLikeTemperature", currentFeelsLikeTemp.toString() );
        xmlMapData.put( "currentHigh", currentHigh.toString() );
        xmlMapData.put( "currentLow", currentLow.toString() );
        xmlMapData.put( "currentWindSpeed", currentWindSpeed.toString() );
        xmlMapData.put( "currentWindDirection", currentWindDirection.toString() );
        xmlMapData.put( "currentHumidity", currentHumidity.toString() );
        xmlMapData.put( "sunriseTime", sunriseTime.toString() );
        xmlMapData.put( "sunsetTime", sunsetTime.toString() );
        xmlMapData.put( "fiveDayForecast", currentFiveDayForecast );

        String xmlJSON = new Gson().toJson( xmlMapData );

        Intent weatherXMLIntent = new Intent( this, WeatherDataXMLService.class );
        weatherXMLIntent.putExtra( WEATHER_XML_DATA, xmlJSON.trim() );
        this.startService( weatherXMLIntent );
    }// end of method loadWeatherBitWeather

    /**
     * Load Yahoo! Weather data
     */
    private void loadYahooYdnWeather()
    {
        currentCountry.setLength( 0 ); // reset
        currentCountry.append( yahoo19.getLocation().getCountry() );

        currentCondition.setLength( 0 ); // reset
        currentCondition.append(
                yahoo19.getCurrentObservation().getCondition().getText() );

        currentHumidity.setLength( 0 );
        currentHumidity.append( Math.round( yahoo19.getCurrentObservation().getAtmosphere().getHumidity() ) );

        currentLocation = currentCity;

        sunriseTime.setLength( 0 ); // reset
        sunriseTime.append( yahoo19.getCurrentObservation().getAstronomy().getSunrise().toUpperCase() );

        sunsetTime.setLength( 0 ); // reset
        sunsetTime.append(  yahoo19.getCurrentObservation().getAstronomy().getSunset().toUpperCase() );

        updateTemps( true ); // call update temps here
        formatWeatherCondition();

        remoteViews.setTextViewText( R.id.txvWeatherCondition, UtilityMethod.toProperCase( currentCondition.toString() ) );

        remoteViews.setTextViewText( R.id.txvWindReading, currentWindDirection +
                " " + currentWindSpeed + ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                " km/h" : " mph" ) );
        remoteViews.setTextViewText( R.id.txvHumidity,currentHumidity.toString() + "%" );

        // Yahoo loves to omit a zero on the hour mark ex: 7:0 am
        if( sunriseTime.length() == 6 )
        {
            String[] ft= sunriseTime.toString().split( ":" );
            sunriseTime.append( String.format( "%s:0%s", ft[ 0 ], ft[ 1 ] ) );
        }// end of if block
        else if( sunsetTime.length() == 6 )
        {
            String[] ft= sunsetTime.toString().split( ":" );
            sunsetTime.append( String.format( "%s:0%s", ft[ 0 ], ft[ 1 ] ) );
        }// end if else if block

        // Update the current location and update time stamp
        String ts = new SimpleDateFormat( "E, MMM dd, h:mm a", Locale.ENGLISH ).format( new Date() );

        remoteViews.setTextViewText( R.id.txvLastUpdated, ts );
        remoteViews.setTextViewText( R.id.txvSunrise, sunriseTime.toString() );
        remoteViews.setTextViewText( R.id.txvSunset, sunsetTime.toString() );

        // Load current condition weather image
        Calendar rightNow = Calendar.getInstance();
        Calendar nightFall = Calendar.getInstance();
        Calendar sunUp = Calendar.getInstance();
        String sunsetTwenty4HourTime = new SimpleDateFormat( "yyyy-MM-dd", Locale.ENGLISH ).format(
                rightNow.getTime() ) + " " + UtilityMethod.get24HourTime( sunsetTime.toString() );
        String sunriseTwenty4HourTime = new SimpleDateFormat( "yyyy-MM-dd", Locale.ENGLISH ).format(
                rightNow.getTime() ) + " " + UtilityMethod.get24HourTime( sunriseTime.toString() );
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm", Locale.ENGLISH );
        Date rn = null;
        Date nf = null;
        Date su = null;

        try
        {
            rn = sdf.parse( sdf.format( rightNow.getTime() ) );
            nightFall.setTime( sdf.parse( sunsetTwenty4HourTime ) );
            nightFall.set( Calendar.MINUTE, Integer.parseInt( sunsetTwenty4HourTime.split( ":" )[ 1 ].trim() ) );
            sunUp.setTime( sdf.parse( sunriseTwenty4HourTime ) );

            nf = sdf.parse( sdf.format( nightFall.getTime() ) );
            su = sdf.parse( sdf.format( sunUp.getTime() ) );
        } // end of try block
        catch ( ParseException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    TAG + "::loadYahooYdnWeather" );
        }// end of catch block

        String currentConditionIcon = null;

        if ( Objects.requireNonNull( rn ).equals( nf ) || rn.after( nf ) || rn.before( su ) )
        {
            if ( currentCondition.toString().toLowerCase().contains( "(night)" ) )
            {
                currentConditionIcon = UtilityMethod.weatherImages.get(
                        currentCondition.toString().toLowerCase() );
            }// end of if block
            else
            {
                if( UtilityMethod.weatherImages.containsKey(
                        currentCondition.toString().toLowerCase() + " (night)" ) )
                {
                    currentConditionIcon = UtilityMethod.weatherImages.get(
                            currentCondition.toString().toLowerCase() + " (night)");
                }// end of if block
                else
                {
                    currentConditionIcon = UtilityMethod.weatherImages.get(
                            currentCondition.toString().toLowerCase() );
                }// end of else block
            }// end of else block

            if( UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() ) == null )
            {
                // sometimes the JSON data received is incomplete so this has to be taken into account
                for ( Map.Entry<String, String> e : UtilityMethod.weatherImages.entrySet() )
                {
                    if ( e.getKey() .startsWith( currentCondition.toString().toLowerCase() ) )
                    {
                        currentConditionIcon =  UtilityMethod.weatherImages.get( e.getKey() ); // use the closest match
                        break; // exit the loop
                    }// end of if block
                }// end of for block

                // if a match still could not be found, use the not available icon
                if( currentConditionIcon == null )
                {
                    currentConditionIcon = "na.png";
                }// end of if block
            }// end of if block

            WeatherWidgetProvider.sunsetIconsInUse = true;
            WeatherWidgetProvider.sunriseIconsInUse = false;
        }// end of if block
        else
        {
            if( UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() ) == null )
            {
                // sometimes the JSON data received is incomplete so this has to be taken into account
                for ( Map.Entry<String, String> e : UtilityMethod.weatherImages.entrySet() )
                {
                    if ( e.getKey() .startsWith( currentCondition.toString().toLowerCase() ) )
                    {
                        currentConditionIcon =  UtilityMethod.weatherImages.get( e.getKey() ); // use the closest match
                        currentCondition.setLength( 0 ); // reset
                        currentCondition.append( e.getKey() );
                        break; // exit the loop
                    }// end of if block
                }// end of for block

                // if a match still could not be found, use the not available icon
                if( currentConditionIcon == null )
                {
                    currentConditionIcon = "na.png";
                }// end of if block
            }// end of if block
            else
            {
                currentConditionIcon = UtilityMethod.weatherImages.get(
                        currentCondition.toString().toLowerCase() );
            }// end of else block

            WeatherWidgetProvider.sunsetIconsInUse = true;
            WeatherWidgetProvider.sunriseIconsInUse = false;
        }// end of else block

        loadWeatherIcon( R.id.imvCurrentCondition,
    "weather_images/" + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        List< YahooWeatherYdnDataItem.Forecast > fdf = yahoo19.getForecast();
        currentFiveDayForecast.clear(); // ensure that this list is clean

        for ( int i = 0; i <= fdf.size(); i++ )
        {
            Date fDate = UtilityMethod.getDateTime( fdf.get( i ).getDate() );

            // Load current forecast condition weather image
            String fCondition =   UtilityMethod.yahooWeatherCodes[
                    fdf.get( i ).getCode() ];
            int  fDay = this.getResources().getIdentifier("txvDay" +  (i + 1),
                    "id", this.getPackageName());
            int  fIcon = this.getResources().getIdentifier("imvDay" +  (i + 1) + "Icon",
                    "id", this.getPackageName());
            int  fHigh = this.getResources().getIdentifier("txvDay" +  (i + 1) + "Temps",
                    "id", this.getPackageName());

            remoteViews.setTextViewText(fDay, new SimpleDateFormat( "E d", Locale.ENGLISH ).format( fDate ));

            if( fCondition.toLowerCase().contains( "(day)" ) )
            {
                fCondition = fCondition.replace( "(day)", "" ).trim();
            }// end of if block
            else if( fCondition.toLowerCase().contains( "(night)" ) )
            {
                fCondition = fCondition.replace( "(night)", "" ).trim();
            }// end of if block

            if( fCondition.toLowerCase().contains( "and" ) )
            {
                String[] conditions = fCondition.toLowerCase().split( "and" );

                fCondition = conditions[ 0 ].trim();
            }// end of if block

            String fConditionIcon = null;

            if( UtilityMethod.weatherImages.get( fCondition.toLowerCase() ) == null )
            {
                // sometimes the JSON data received is incomplete so this has to be taken into account
                for ( Map.Entry<String, String> e : UtilityMethod.weatherImages.entrySet() )
                {
                    if ( e.getKey().startsWith( fCondition.toLowerCase() ) )
                    {
                        fConditionIcon =  UtilityMethod.weatherImages.get( e.getKey() ); // use the closest match
                        fCondition = e.getKey();
                        break; // exit the loop
                    }// end of if block
                }// end of for block

                // if a match still could not be found, use the not available icon
                if( fConditionIcon == null )
                {
                    fConditionIcon = "na.png";
                }// end of if block
            }// end of if block
            else
            {
                fConditionIcon = UtilityMethod.weatherImages.get( fCondition.toLowerCase() );
            }// end of if block

            loadWeatherIcon( fIcon,
        "weather_images/" + WeatherLionApplication.iconSet + "/weather_" + fConditionIcon );

            Date forecastDate = UtilityMethod.getDateTime( fdf.get( i ).getDate() );

            currentFiveDayForecast.add(
                    new FiveDayForecast(forecastDate, String.valueOf( hl[i][0] ),
                            String.valueOf( hl[i][1] ), fCondition ) );
            if( i == 4 )
            {
                break;
            }// end of if block
        }// end of for loop

        // if the code gets to here then all was loaded successfully
        WeatherWidgetProvider.dataLoadedSuccessfully = true;

        Map< String, Object > xmlMapData = new LinkedHashMap<>();
        xmlMapData.put( "providerName", WeatherLionApplication.YAHOO_WEATHER );
        xmlMapData.put( "datePublished", new Date() );
        xmlMapData.put( "cityName", currentCity.toString() );
        xmlMapData.put( "countryName",  currentCountry.toString() );
        xmlMapData.put( "currentConditions", currentCondition.toString() );
        xmlMapData.put( "currentTemperature", currentTemp.toString() );
        xmlMapData.put( "currentFeelsLikeTemperature", currentFeelsLikeTemp.toString() );
        xmlMapData.put( "currentHigh", currentHigh.toString() );
        xmlMapData.put( "currentLow", currentLow.toString() );
        xmlMapData.put( "currentWindSpeed", currentWindSpeed.toString() );
        xmlMapData.put( "currentWindDirection", currentWindDirection.toString() );
        xmlMapData.put( "currentHumidity", currentHumidity.toString() );
        xmlMapData.put( "sunriseTime", sunriseTime.toString() );
        xmlMapData.put( "sunsetTime", sunsetTime.toString() );
        xmlMapData.put( "fiveDayForecast", currentFiveDayForecast );

        String xmlJSON = new Gson().toJson( xmlMapData );

        Intent weatherXMLIntent = new Intent( this, WeatherDataXMLService.class );
        weatherXMLIntent.putExtra( WEATHER_XML_DATA, xmlJSON.trim() );
        this.startService( weatherXMLIntent );

    }// end of method loadYahooYdnWeather

    /**
     * Load Yr Weather data
     */
    private void loadYrWeather()
    {
        currentCountry.setLength( 0 );
        currentCountry.append( yr.getCountry() );

        currentCondition.setLength( 0 ); // reset
        currentCondition.append( UtilityMethod.toProperCase( yr.getForecast().get( 0 ).getSymbolName() ) );

        currentHumidity.setLength( 0 );
        currentHumidity.append( currentHumidity.toString().length() == 0 ? currentHumidity : String.valueOf( 0 ) ); // use the humidity reading from previous providers

        // append a zero if there is no humidity
        if( currentHumidity.length() == 0 ) currentHumidity.append( "0" );

        currentLocation = currentCity;

        sunriseTime.setLength( 0 );
        sunriseTime.append( new SimpleDateFormat( "h:mm a", Locale.ENGLISH ).format( yr.getSunrise() ) );

        sunsetTime.setLength( 0 );
        sunsetTime.append( new SimpleDateFormat( "h:mm a", Locale.ENGLISH ).format( yr.getSunset() ) );

        // call update temps here
        updateTemps( true );
        formatWeatherCondition();

        remoteViews.setTextViewText( R.id.txvWeatherCondition, UtilityMethod.toProperCase( currentCondition.toString() ) );
        remoteViews.setTextViewText( R.id.txvHumidity,
                !currentHumidity.toString().contains( "%" ) ?  currentHumidity.toString() + "%" : currentHumidity.toString() );

        // Some providers like Yahoo love to omit a zero on the hour mark example: 7:0 am
        if( sunriseTime.length() == 6 )
        {
            String[] ft = sunriseTime.toString().split( ":" );
            sunriseTime.append( String.format( "%s:0%s", ft[ 0 ], ft[ 1 ] ) );
        }// end of if block
        else if( sunsetTime.length() == 6 )
        {
            String[] ft= sunsetTime.toString().split( ":" );
            sunsetTime.append( String.format( "%s:0%s", ft[ 0 ], ft[ 1 ] ) );
        }// end if else if block

        // Update the current location and update time stamp
        String ts = new SimpleDateFormat( "E, MMM dd, h:mm a", Locale.ENGLISH ).format( new Date() );

        remoteViews.setTextViewText( R.id.txvLastUpdated, ts );

        remoteViews.setTextViewText( R.id.txvSunrise, sunriseTime.toString() );
        remoteViews.setTextViewText( R.id.txvSunset, sunsetTime.toString() );

        // Load current condition weather image
        Calendar rightNow = Calendar.getInstance();
        Calendar nightFall = Calendar.getInstance();
        Calendar sunUp = Calendar.getInstance();
        String sunsetTwenty4HourTime = new SimpleDateFormat( "yyyy-MM-dd", Locale.ENGLISH ).format( rightNow.getTime() )
                + " " + UtilityMethod.get24HourTime( sunsetTime.toString() );
        String sunriseTwenty4HourTime = new SimpleDateFormat( "yyyy-MM-dd", Locale.ENGLISH ).format( rightNow.getTime() )
                + " " + UtilityMethod.get24HourTime( sunriseTime.toString() );
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm", Locale.ENGLISH );
        Date rn = null;
        Date nf = null;
        Date su = null;

        try
        {
            rn = sdf.parse( sdf.format( rightNow.getTime() ) );
            nightFall.setTime( sdf.parse( sunsetTwenty4HourTime ) );
            nightFall.set( Calendar.MINUTE, Integer.parseInt( sunsetTwenty4HourTime.split( ":" )[ 1 ].trim() ) );
            sunUp.setTime( sdf.parse( sunriseTwenty4HourTime ) );

            nf = sdf.parse( sdf.format( nightFall.getTime() ) );
            su = sdf.parse( sdf.format( sunUp.getTime() ) );
        } // end of try block
        catch ( ParseException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE , e.getMessage(),
                    TAG + "::loadHereMapsWeather [line: " +
                            e.getStackTrace()[ 1 ].getLineNumber() + "]" );
        }// end of catch block

        String currentConditionIcon = null;

        if ( Objects.requireNonNull( rn ).equals( nf ) || rn.after( nf ) || rn.before( su ) )
        {
            if ( currentCondition.toString().toLowerCase().contains( "(night)" ) )
            {
                currentConditionIcon = UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() );
            }// end of if block
            else
            {
                if( UtilityMethod.weatherImages.containsKey( currentCondition.toString().toLowerCase() + " (night)" ) )
                {
                    currentConditionIcon = UtilityMethod.weatherImages.get(currentCondition.toString().toLowerCase() + " (night)");
                }// end of if block
                else
                {
                    currentConditionIcon = UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() );
                }// end of else block
            }// end of else block

            if( UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() ) == null )
            {
                // sometimes the JSON data received is incomplete so this has to be taken into account
                for ( Map.Entry<String, String> e : UtilityMethod.weatherImages.entrySet() )
                {
                    if ( e.getKey() .startsWith( currentCondition.toString().toLowerCase() ) )
                    {
                        currentConditionIcon =  UtilityMethod.weatherImages.get( e.getKey() ); // use the closest match
                        break; // exit the loop
                    }// end of if block
                }// end of for block

                // if a match still could not be found, use the not available icon
                if( currentConditionIcon == null )
                {
                    currentConditionIcon = "na.png";
                }// end of if block
            }// end of if block

            WeatherWidgetProvider.sunsetIconsInUse = true;
            WeatherWidgetProvider.sunriseIconsInUse = false;
        }// end of if block
        else
        {
            if( UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() ) == null )
            {
                // sometimes the JSON data received is incomplete so this has to be taken into account
                for ( Map.Entry<String, String> e : UtilityMethod.weatherImages.entrySet() )
                {
                    if ( e.getKey() .startsWith( currentCondition.toString().toLowerCase() ) )
                    {
                        currentConditionIcon =  UtilityMethod.weatherImages.get( e.getKey() ); // use the closest match
                        currentCondition.setLength( 0 ); // reset
                        currentCondition.append( e.getKey() );
                        break; // exit the loop
                    }// end of if block
                }// end of for block

                // if a match still could not be found, use the not available icon
                if( currentConditionIcon == null )
                {
                    currentConditionIcon = "na.png";
                }// end of if block
            }// end of if block
            else
            {
                currentConditionIcon = UtilityMethod.weatherImages.get(
                        currentCondition.toString().toLowerCase() );
            }// end of else block

            WeatherWidgetProvider.sunsetIconsInUse = true;
            WeatherWidgetProvider.sunriseIconsInUse = false;
        }// end of else block

        loadWeatherIcon( R.id.imvCurrentCondition,
    "weather_images/" + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        List< YrWeatherDataItem.Forecast > fdf = yr.getForecast();
        SimpleDateFormat df = new SimpleDateFormat( "MMMM dd, yyyy", Locale.ENGLISH );

        int i = 1;
        int x = 0;
        currentFiveDayForecast.clear(); // ensure that this list is clean

        for ( Forecast wxDailyForecast : fdf )
        {
            x++;

            // the first time period is one that will be stored
            if ( x == 1 )
            {
                Date forecastDate = wxDailyForecast.getTimeFrom();

                // Load current forecast condition weather image
                String fCondition =   wxDailyForecast.getSymbolName();

                int  fDay = this.getResources().getIdentifier("txvDay" + (i),
                        "id", this.getPackageName());
                int  fIcon = this.getResources().getIdentifier("imvDay" + (i) + "Icon",
                        "id", this.getPackageName());
                int  fHigh = this.getResources().getIdentifier("txvDay" + (i) + "Temps",
                        "id", this.getPackageName());

                remoteViews.setTextViewText(fDay, new SimpleDateFormat( "E d", Locale.ENGLISH ).format( forecastDate ));

                if( fCondition.toLowerCase().contains( "(day)" ) )
                {
                    fCondition = fCondition.replace( "(day)", "" ).trim();
                }// end of if block
                else if( fCondition.toLowerCase().contains( "(night)" ) )
                {
                    fCondition = fCondition.replace( "(night)", "" ).trim();
                }// end of if block

                if( fCondition.toLowerCase().contains( "and" ) )
                {
                    String[] conditions = fCondition.toLowerCase().split( "and" );

                    fCondition = conditions[ 0 ].trim();
                }// end of if block

                String fConditionIcon = null;

                if( UtilityMethod.weatherImages.get( fCondition.toLowerCase() ) == null )
                {
                    // sometimes the JSON data received is incomplete so this has to be taken into account
                    for ( Map.Entry<String, String> e : UtilityMethod.weatherImages.entrySet() )
                    {
                        if ( e.getKey() .startsWith( fCondition.toLowerCase() ) )
                        {
                            fConditionIcon =  UtilityMethod.weatherImages.get( e.getKey() ); // use the closest match
                            fCondition = e.getKey();
                            break; // exit the loop
                        }// end of if block
                    }// end of for block

                    // if a match still could not be found, use the not available icon
                    if( fConditionIcon == null )
                    {
                        fConditionIcon = "na.png";
                    }// end of if block
                }// end of if block
                else
                {
                    fConditionIcon = UtilityMethod.weatherImages.get( fCondition.toLowerCase() );
                }// end of if block

                loadWeatherIcon( fIcon,
            "weather_images/" + WeatherLionApplication.iconSet + "/weather_" + fConditionIcon );

                currentFiveDayForecast.add(
                    new FiveDayForecast( forecastDate, String.valueOf(
                        Math.round( dailyReading.get( df.format( wxDailyForecast.getTimeFrom() ) ) [ 0 ][ 0 ] ) ),
                            String.valueOf( Math.round( dailyReading.get(
                                df.format( wxDailyForecast.getTimeFrom() ) ) [ 0 ][ 1 ] ) ), fCondition ) );
                if( i == 5 )
                {
                    break;
                }// end of if block

                i++; // increment sentinel
            }// end of if block

            if ( wxDailyForecast.getTimePeriod() == 3 )
            {
                x = 0;
            }// end of if block
        }// end of for loop

        // if the code gets to here then all was loaded successfully
        WeatherWidgetProvider.dataLoadedSuccessfully = true;

        Map< String, Object > xmlMapData = new LinkedHashMap<>();
        xmlMapData.put( "providerName", WeatherLionApplication.YR_WEATHER );
        xmlMapData.put( "datePublished", new Date() );
        xmlMapData.put( "cityName", currentCity.toString() );
        xmlMapData.put( "countryName",  currentCountry.toString() );
        xmlMapData.put( "currentConditions", currentCondition.toString() );
        xmlMapData.put( "currentTemperature", currentTemp.toString() );
        xmlMapData.put( "currentFeelsLikeTemperature", currentFeelsLikeTemp.toString() );
        xmlMapData.put( "currentHigh", currentHigh.toString() );
        xmlMapData.put( "currentLow", currentLow.toString() );
        xmlMapData.put( "currentWindSpeed", currentWindSpeed.toString() );
        xmlMapData.put( "currentWindDirection", currentWindDirection.toString() );
        xmlMapData.put( "currentHumidity", currentHumidity.toString() );
        xmlMapData.put( "sunriseTime", sunriseTime.toString() );
        xmlMapData.put( "sunsetTime", sunsetTime.toString() );
        xmlMapData.put( "fiveDayForecast", currentFiveDayForecast );

        String xmlJSON = new Gson().toJson( xmlMapData );

        Intent weatherXMLIntent = new Intent( this, WeatherDataXMLService.class );
        weatherXMLIntent.putExtra( WEATHER_XML_DATA, xmlJSON.trim() );
        this.startService( weatherXMLIntent );
    }// end of method loadYrWeather

    /***
     * Check the current time of day
     */
    private void formatWeatherCondition()
    {
        String tc = currentCondition.toString();

        if( currentCondition.toString().toLowerCase().startsWith( "txvDay" ) )
        {
            currentCondition.setLength( 0 ); // reset
            currentCondition.append( tc.toLowerCase().replace( "txvDay", "" ).trim() );
        }// end of if block
        else if( currentCondition.toString().toLowerCase().startsWith( "night" ) )
        {
            currentCondition.setLength( 0 ); // reset
            currentCondition.append( tc.toLowerCase().replace( "night", "" ).trim() );
        }// end of else if block

        if( currentCondition.toString().toLowerCase().contains( "(day)" ) )
        {
            currentCondition.setLength( 0 ); // reset
            currentCondition.append(
                    UtilityMethod.toProperCase( tc.replace( "(day)", "" ).trim() ) );
        }// end of if block
        else if( currentCondition.toString().toLowerCase().contains("(night)" ) )
        {
            currentCondition.setLength( 0 ); // reset
            currentCondition.append(
                    UtilityMethod.toProperCase( tc.replace( "(night)", "" ).trim() ) );
        }// end of else if block

        if( currentCondition.toString().toLowerCase().contains( "thunderstorms" ) &&
                currentCondition.toString().toLowerCase().indexOf( "thunderstorms" ) > 0 )
        {
            currentCondition.setLength( 0 ); // reset
            currentCondition.append( tc.toLowerCase().replace( "thunderstorms", "t-storms" ) );
        }// end of if block
        else if( currentCondition.toString().toLowerCase().contains( "thundershowers" ) &&
                currentCondition.toString().toLowerCase().indexOf( "thundershowers" ) > 0 )
        {
            currentCondition.setLength( 0 ); // reset
            currentCondition.append( tc.toLowerCase().replace( "thundershowers", "t-showers" ) );
        }// end of else if block
        else if( currentCondition.toString().toLowerCase().contains( "and" ) )
        {
            String[] conditions = currentCondition.toString().toLowerCase().split( "and" );

            currentCondition.setLength( 0 ); // reset
            currentCondition.append(
                    UtilityMethod.toProperCase( conditions[ 0 ].trim() ) );
        }// end of if block
    }// end of method formatWeatherCondition()

    private void loadLocalWeatherData()
    {
        if( new File( this.getFileStreamPath( WeatherLionApplication.WEATHER_DATA_XML ).toString() ).exists() )
        {
            // If the weather data xml file exists, that means the program has previously received
            // data from a web service. The data must then be loaded into memory.
            WeatherLionApplication.lastDataReceived = LastWeatherDataXmlParser.parseXmlData(
                    UtilityMethod.readAll(
                            this.getFileStreamPath( WeatherLionApplication.WEATHER_DATA_XML ).toString() )
                            .replaceAll( "\t", "" ).trim() );

            WeatherLionApplication.storedData = WeatherLionApplication.lastDataReceived.getWeatherData();
            DateFormat df = new SimpleDateFormat( "EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);

            try
            {
                UtilityMethod.lastUpdated = df.parse( WeatherLionApplication.storedData.getProvider().getDate() );
            }// end of try block
            catch ( ParseException e )
            {
                UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, "Unable to parse last weather data date.",
                        TAG + "::onCreate");
            }// end of catch block

            WeatherLionApplication.currentSunriseTime =
                    new StringBuilder( WeatherLionApplication.storedData.getAstronomy().getSunrise() );
            WeatherLionApplication.currentSunsetTime =
                    new StringBuilder( WeatherLionApplication.storedData.getAstronomy().getSunset() );

        }// end of if block
    }// end of method loadLocalWeatherData

    private void loadWeatherIconSet()
    {
        // load the weather data stored locally
        loadLocalWeatherData();

        // Load current condition weather image
        Calendar rightNow = Calendar.getInstance();
        Calendar nightFall = Calendar.getInstance();
        String twenty4HourTime = new SimpleDateFormat( "yyyy-MM-dd",
                Locale.ENGLISH ).format( rightNow.getTime() )
                + " " + UtilityMethod.get24HourTime( sunsetTime.toString() );

        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm", Locale.ENGLISH );
        Date rn = null;
        Date nf = null;

        try
        {
            rn = sdf.parse( sdf.format( rightNow.getTime() ) );
            nightFall.setTime( sdf.parse( twenty4HourTime ) );
            nightFall.set( Calendar.MINUTE, Integer.parseInt( twenty4HourTime.split( ":" )[ 1 ].trim() ) );
            nf = sdf.parse( sdf.format( nightFall.getTime() ) );
        } // end of try block
        catch ( ParseException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE , e.getMessage(),
                    TAG + "::loadWeatherIconSet [line: " + e.getStackTrace()[1].getLineNumber()+ "]" );
        }// end of catch block

        String currentConditionIcon;
        String weatherCondition = WeatherLionApplication.storedData.getCurrent().getCondition();

        if ( Objects.requireNonNull( rn ).equals( nf ) || rn.after( nf ) )
        {
            if ( weatherCondition.toLowerCase().contains( "(night)" ) )
            {
                currentConditionIcon = UtilityMethod.weatherImages.get( weatherCondition.toLowerCase() );
            }// end of if block
            else
            {
                if( UtilityMethod.weatherImages.containsKey( currentCondition.toString().toLowerCase() + " (night)" ) )
                {
                    currentConditionIcon = UtilityMethod.weatherImages.get(
                            weatherCondition.toLowerCase() + " (night)" );
                }// end of if block
                else
                {
                    currentConditionIcon = UtilityMethod.weatherImages.get(
                            weatherCondition.toLowerCase() );
                }// end of else block
            }// end of else block
        }// end of if block
        else
        {
            currentConditionIcon = UtilityMethod.weatherImages.get(
                    weatherCondition.toLowerCase() );
        }// end of else block

        currentConditionIcon =  UtilityMethod.weatherImages.get(
                weatherCondition.toLowerCase() ) == null ?
                "na.png" :
                currentConditionIcon;

        loadWeatherIcon( R.id.imvCurrentCondition,
                "weather_images/" + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        SimpleDateFormat df = new SimpleDateFormat( "MMMM dd, yyyy", Locale.ENGLISH );

        int x = 0;

        for ( int i = 0; i < WeatherLionApplication.storedData.getDailyForecast().size(); i++ )
        {
            x++;
            LastWeatherData.WeatherData.DailyForecast.DayForecast wxDayForecast =
                    WeatherLionApplication.storedData.getDailyForecast().get( i );
            Date forecastDate = null;

            int  fIcon = this.getResources().getIdentifier( "imvDay" + (i + 1) + "Icon",
                    "id", this.getPackageName() );

            // Load current forecast condition weather image
            String fCondition = wxDayForecast.getCondition();

            if( fCondition.toLowerCase().contains( "(day)" ) )
            {
                fCondition = fCondition.replace( "(day)", "" ).trim();
            }// end of if block
            else if( fCondition.toLowerCase().contains( "(night)" ) )
            {
                fCondition = fCondition.replace( "(night)", "" ).trim();
            }// end of if block

            String fConditionIcon
                    = UtilityMethod.weatherImages.get( fCondition.toLowerCase() ) == null
                    ? "na.png" : UtilityMethod.weatherImages.get( fCondition.toLowerCase() );

            loadWeatherIcon( fIcon,
                    "weather_images/" + WeatherLionApplication.iconSet + "/weather_" + fConditionIcon );

            if( i == 4 )
            {
                break;
            }// end of if block
        }// end of for loop
    }// end of method loadWeatherIconSet

    private void loadWidgetBackground()
    {
        UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
        "Attempting background change",TAG + LOAD_WIDGET_BACKGROUND );
        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences( this );
        String widBackgroundColor = spf.getString(
            WeatherLionApplication.WIDGET_BACKGROUND_PREFERENCE,
                Preference.DEFAULT_WIDGET_BACKGROUND );

        int drawableId = 0;

        if( widBackgroundColor != null )
        {
            switch ( widBackgroundColor.toLowerCase() )
            {
                case WeatherLionApplication.AQUA_THEME:
                    drawableId = R.drawable.wl_aqua_bg;

                    break;
                case WeatherLionApplication.RABALAC_THEME:
                    drawableId = R.drawable.wl_rabalac_bg;

                    break;
                case WeatherLionApplication.LION_THEME:
                default:
                    drawableId = R.drawable.wl_lion_bg;

                    break;
            }// end of switch block
        }// end of if block

        remoteViews.setImageViewBitmap( R.id.imvWidgetBackground, getBitmap( drawableId ) );
    }// end of method loadWidgetBackground

    private Bitmap getBitmap( int drawableRes )
    {
        Drawable drawable = ContextCompat.getDrawable( this, drawableRes );
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap( Objects.requireNonNull( drawable ).getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888 );
        canvas.setBitmap( bitmap );
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight() );
        drawable.draw( canvas );

        return bitmap;
    }// end of method getBitmap

    private void setBitmap( RemoteViews views, int resId, Bitmap bitmap )
    {
        Bitmap proxy = Bitmap.createBitmap( bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888 );
        Canvas c = new Canvas( proxy );
        c.drawBitmap(bitmap, new Matrix(), null);
        views.setImageViewBitmap( resId, proxy );
    // end fo method setBitmap
    }// end of method setBitmap

    /***
     * Update the numerical values displayed on the widget
     *
     * @param hasConnection A {@code boolean} value representing Internet Connectivity.
     */
    private void updateTemps( boolean hasConnection )
    {
        String today;
        int i;

        if( hasConnection )
        {
            switch( WeatherLionApplication.storedPreferences.getProvider() )
            {
                case WeatherLionApplication.DARK_SKY:
                    if( WeatherLionApplication.storedPreferences.getUseMetric() )
                    {
                        currentTemp.setLength( 0 );
                        currentTemp.append( Math.round(
                                UtilityMethod.fahrenheitToCelsius( darkSky.getCurrently().getTemperature() ) ) );

                        currentFeelsLikeTemp.setLength( 0 );
                        currentFeelsLikeTemp.append( Math.round(
                                UtilityMethod.fahrenheitToCelsius( darkSky.getCurrently().getApparentTemperature() ) ) );

                        currentHigh.setLength( 0 );
                        currentHigh.append(
                                Math.round( UtilityMethod.fahrenheitToCelsius(
                                        darkSky.getDaily().getData().get( 0 ).getTemperatureMax() ) ) );

                        currentHigh.setLength( 0 );
                        currentHigh.append(
                                Math.round(
                                        UtilityMethod.celsiusToFahrenheit(
                                                darkSky.getDaily().getData().get( 0 ).getTemperatureMin() ) ) );

                        currentWindSpeed.setLength( 0 );
                        currentWindSpeed.append( Math.round(
                                UtilityMethod.mphToKmh( darkSky.getCurrently().getWindSpeed() ) ) );
                    }// end of if block
                    else
                    {
                        currentTemp.setLength( 0 );
                        currentTemp.append( Math.round( darkSky.getCurrently().getTemperature() ) );

                        currentFeelsLikeTemp.setLength( 0 );
                        currentFeelsLikeTemp.append( Math.round( darkSky.getCurrently().getApparentTemperature() ) );

                        currentHigh.setLength( 0 );
                        currentHigh.append(
                                Math.round( darkSky.getDaily().getData().get( 0 ).getTemperatureMax() ) );

                        currentLow.setLength( 0 );
                        currentLow.append(
                                Math.round( darkSky.getDaily().getData().get( 0 ).getTemperatureMin() ) );

                        currentWindSpeed.setLength( 0 );
                        currentWindSpeed.append(
                                Math.round( darkSky.getCurrently().getWindSpeed() ) );
                    }// end of else block

                    // Display weather data on widget
                    remoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + tempUnits);
                    remoteViews.setTextViewText( R.id.txvFeelsLike, String.format(
                            "%s %s%s", FEELS_LIKE, currentFeelsLikeTemp, DEGREES ) );
                    remoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                    remoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );

                    remoteViews.setTextViewText( R.id.txvWindReading, currentWindDirection +
                            " " + currentWindSpeed + ( WeatherLionApplication.storedPreferences.getUseMetric()
                            ? " km/h" : " mph" ) );
                    remoteViews.setTextViewText( R.id.txvHumidity, currentHumidity.toString() );

                    // Five Day Forecast
                    i = 1;
                    hl = new int[ 5 ][ 2 ];

                    for ( DarkSkyWeatherDataItem.Daily.Data wxForecast : darkSky.getDaily().getData()  )
                    {
                        String fHigh;
                        String fLow;

                        if( WeatherLionApplication.storedPreferences.getUseMetric() )
                        {
                            fHigh = String.valueOf( Math.round(
                                    UtilityMethod.fahrenheitToCelsius( wxForecast.getTemperatureMax() ) ) );
                            fLow = String.valueOf( Math.round(
                                    UtilityMethod.fahrenheitToCelsius( wxForecast.getTemperatureMin() ) ) );
                        }// end of if block
                        else
                        {
                            fHigh = String.valueOf( Math.round( wxForecast.getTemperatureMax() ) );
                            fLow = String.valueOf( Math.round( wxForecast.getTemperatureMin() ) );
                        }// end of else block

                        String temps = String.format( "%s° %s°", fLow, fHigh );

                        hl[ i - 1 ][ 0 ] = Integer.parseInt( fHigh );
                        hl[ i - 1 ][ 1 ] = Integer.parseInt( fLow );

                        int  dayTemps = this.getResources().getIdentifier( "txvDay" + (i) + "Temps",
                                "id", this.getPackageName() );

                        remoteViews.setTextViewText( dayTemps, temps );

                        if ( i == 5 )
                        {
                            break;
                        }// end of if block

                        i++; // increment sentinel
                    }// end of for each loop

                    break;
                case WeatherLionApplication.HERE_MAPS:
                    double fl =
                            hereWeatherWx
                                    .getObservations()
                                    .getLocation()
                                    .get( 0 )
                                    .getObservation()
                                    .get( 0 )
                                    .getComfort();

                    if( WeatherLionApplication.storedPreferences.getUseMetric() )
                    {
                        currentTemp.setLength( 0 );
                        currentTemp.append(
                                Math.round(
                                        UtilityMethod.fahrenheitToCelsius(
                                                hereWeatherWx
                                                        .getObservations()
                                                        .getLocation()
                                                        .get( 0 )
                                                        .getObservation()
                                                        .get( 0 )
                                                        .getTemperature()
                                        ) ) );

                        currentFeelsLikeTemp.setLength( 0 );
                        currentFeelsLikeTemp.append(
                                Math.round( UtilityMethod.fahrenheitToCelsius( (float) fl ) ) );

                        currentHigh.setLength( 0 );
                        currentHigh.append(
                                Math.round(
                                        UtilityMethod.fahrenheitToCelsius(
                                                hereWeatherWx
                                                        .getObservations()
                                                        .getLocation()
                                                        .get( 0 )
                                                        .getObservation()
                                                        .get( 0 )
                                                        .getHighTemperature()
                                        ) ) );

                        currentLow.setLength( 0 );
                        currentLow.append(
                                Math.round(
                                        UtilityMethod.fahrenheitToCelsius(
                                                hereWeatherWx
                                                        .getObservations()
                                                        .getLocation()
                                                        .get( 0 )
                                                        .getObservation()
                                                        .get( 0 )
                                                        .getLowTemperature()
                                        ) ) );

                        currentWindSpeed.setLength( 0 );
                        currentWindSpeed.append(
                                Math.round(
                                        UtilityMethod.fahrenheitToCelsius(
                                                hereWeatherWx
                                                        .getObservations()
                                                        .getLocation()
                                                        .get( 0 )
                                                        .getObservation()
                                                        .get( 0 )
                                                        .getWindSpeed()
                                        ) ) );
                    }// end of if block
                    else
                    {
                        currentTemp.setLength( 0 );
                        currentTemp.append(
                                Math.round(
                                        hereWeatherWx
                                                .getObservations()
                                                .getLocation()
                                                .get( 0 )
                                                .getObservation()
                                                .get( 0 )
                                                .getTemperature()
                                ) );

                        currentFeelsLikeTemp.setLength( 0 );
                        currentFeelsLikeTemp.append( Math.round( (float) fl ) );

                        currentHigh.setLength( 0 );
                        currentHigh.append(
                                Math.round(
                                        hereWeatherWx
                                                .getObservations()
                                                .getLocation()
                                                .get( 0 )
                                                .getObservation()
                                                .get( 0 )
                                                .getHighTemperature()
                                ) );

                        currentLow.setLength( 0 );
                        currentLow.append(
                                Math.round(
                                        hereWeatherWx
                                                .getObservations()
                                                .getLocation()
                                                .get( 0 )
                                                .getObservation()
                                                .get( 0 )
                                                .getLowTemperature()
                                ) );

                        currentWindSpeed.setLength( 0 );
                        currentWindSpeed.append(
                                Math.round(
                                        hereWeatherWx
                                                .getObservations()
                                                .getLocation()
                                                .get( 0 )
                                                .getObservation()
                                                .get( 0 )
                                                .getWindSpeed()
                                ) );
                    }// end of else block

                    // Display weather data on widget
                    remoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + tempUnits);
                    remoteViews.setTextViewText( R.id.txvFeelsLike, String.format(
                            "%s %s%s", FEELS_LIKE, currentFeelsLikeTemp, DEGREES ) );
                    remoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                    remoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );

                    remoteViews.setTextViewText( R.id.txvWindReading, currentWindDirection +
                            " " + currentWindSpeed + ( WeatherLionApplication.storedPreferences.getUseMetric()
                            ? " km/h" : " mph" ) );
                    remoteViews.setTextViewText( R.id.txvHumidity, currentHumidity.toString() );

                    // Five Day Forecast
                    List< HereMapsWeatherDataItem.ForecastData.DailyForecasts.ForecastLocation.Forecast > hFdf =
                            hereWeatherFx.getDailyForecasts().getForecastLocation().getForecast();
                    i = 1;
                    hl = new int[ 5 ][ 2 ];

                    for ( HereMapsWeatherDataItem.ForecastData.DailyForecasts.ForecastLocation.Forecast wxForecast : hFdf )
                    {
                        String fHigh;
                        String fLow;

                        if( WeatherLionApplication.storedPreferences.getUseMetric() )
                        {
                            fHigh = String.valueOf( Math.round(
                                    UtilityMethod.fahrenheitToCelsius( wxForecast.getHighTemperature() ) ) );
                            fLow = String.valueOf( Math.round(
                                    UtilityMethod.fahrenheitToCelsius( wxForecast.getLowTemperature() ) ) );
                        }// end of if block
                        else
                        {
                            fHigh = String.valueOf( Math.round( wxForecast.getHighTemperature() ) );
                            fLow = String.valueOf( Math.round( wxForecast.getLowTemperature() ) );
                        }// end of else block

                        String temps = String.format( "%s° %s°", fLow, fHigh );
                        int dayTemps = this.getResources().getIdentifier( "txvDay" + (i) + "Temps",
                                "id", this.getPackageName() );

                        hl[ i - 1 ][ 0 ] = Integer.parseInt( fHigh );
                        hl[ i - 1 ][ 1 ] = Integer.parseInt( fLow );

                        remoteViews.setTextViewText( dayTemps, temps );

                        if ( i == 5 )
                        {
                            break;
                        }// end of if block

                        i++; // increment sentinel
                    }// end of for each loop

                    break;
                case WeatherLionApplication.OPEN_WEATHER:
                    fl = UtilityMethod.heatIndex( openWeatherWx.getMain().getTemp(),
                            openWeatherWx.getMain().getHumidity() );

                    if( WeatherLionApplication.storedPreferences.getUseMetric() )
                    {
                        currentTemp.setLength( 0 );
                        currentTemp.append( Math.round(
                                UtilityMethod.fahrenheitToCelsius( openWeatherWx.getMain().getTemp() ) ) );

                        currentFeelsLikeTemp.setLength( 0 );
                        currentFeelsLikeTemp.append( Math.round(
                                UtilityMethod.fahrenheitToCelsius( (float) fl ) ) );

                        currentHigh.setLength( 0 );
                        currentHigh.append(
                                Math.round(
                                        UtilityMethod.fahrenheitToCelsius(
                                                openWeatherFx.getList().get( 0 ).getTemp().getMax() ) ) );

                        currentHigh.setLength( 0 );
                        currentLow.append(
                                Math.round(
                                        UtilityMethod.celsiusToFahrenheit(
                                                openWeatherFx.getList().get( 0 ).getTemp().getMin() ) ) );

                        currentWindSpeed.setLength( 0 );
                        currentWindSpeed.append(
                                Math.round( UtilityMethod.mphToKmh( openWeatherWx.getWind().getSpeed() ) ) );
                    }// end of if block
                    else
                    {
                        currentTemp.setLength( 0 );
                        currentTemp.append( Math.round( openWeatherWx.getMain().getTemp() ) );

                        currentFeelsLikeTemp.setLength( 0 );
                        currentFeelsLikeTemp.append( Math.round( (float) fl ) );

                        currentHigh.setLength( 0 );
                        currentHigh.append( Math.round( openWeatherFx.getList().get( 0 ).getTemp().getMax() ) );

                        currentLow.setLength( 0 );
                        currentLow.append( Math.round( openWeatherFx.getList().get( 0 ).getTemp().getMin() ) );

                        currentWindSpeed.setLength( 0 );
                        currentWindSpeed.append( Math.round( openWeatherWx.getWind().getSpeed() ) );
                    }// end of else block

                    // Display weather data on widget
                    remoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + tempUnits );
                    remoteViews.setTextViewText( R.id.txvFeelsLike, String.format(
                            "%s %s%s", FEELS_LIKE, currentFeelsLikeTemp, DEGREES ) );
                    remoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                    remoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );

                    remoteViews.setTextViewText( R.id.txvWindReading, currentWindDirection +
                            " " + currentWindSpeed + ( WeatherLionApplication.storedPreferences.getUseMetric()
                            ? " km/h" : " mph" ) );
                    remoteViews.setTextViewText( R.id.txvHumidity, currentHumidity.toString() );

                    // Five Day Forecast
                    List< OpenWeatherMapWeatherDataItem.ForecastData.Data > oFdf = openWeatherFx.getList();
                    i = 1;
                    hl = new int[ 5 ][ 2 ];

                    for ( OpenWeatherMapWeatherDataItem.ForecastData.Data wxForecast : oFdf )
                    {
                        String fHigh;
                        String fLow;

                        if( WeatherLionApplication.storedPreferences.getUseMetric() )
                        {
                            fHigh = String.valueOf( Math.round(
                                    UtilityMethod.fahrenheitToCelsius( wxForecast.getTemp().getMax() ) ) );
                            fLow = String.valueOf( Math.round(
                                    UtilityMethod.fahrenheitToCelsius( wxForecast.getTemp().getMin() ) ) );
                        }// end of if block
                        else
                        {
                            fHigh = String.valueOf( Math.round( wxForecast.getTemp().getMax() ) );
                            fLow = String.valueOf( Math.round( wxForecast.getTemp().getMin() ) );
                        }// end of else block

                        String temps = String.format( "%s° %s°", fLow, fHigh );
                        int dayTemps = this.getResources().getIdentifier( "txvDay" + (i) + "Temps",
                                "id", this.getPackageName() );

                        hl[ i - 1 ][ 0 ] = Integer.parseInt( fHigh );
                        hl[ i - 1 ][ 1 ] = Integer.parseInt( fLow );

                        remoteViews.setTextViewText( dayTemps, temps );

                        if ( i == 5 )
                        {
                            break;
                        }// end of if block

                        i++; // increment sentinel
                    }// end of for each loop

                    break;
                case WeatherLionApplication.WEATHER_BIT:
                    fl = weatherBitWx.getData().get( 0 ).getAppTemp() == 0
                            ?  weatherBitWx.getData().get( 0 ).getTemp()
                            : weatherBitWx.getData().get( 0 ).getAppTemp();

                    if( WeatherLionApplication.storedPreferences.getUseMetric() )
                    {
                        currentTemp.setLength( 0 );
                        currentTemp.append( Math.round( UtilityMethod.fahrenheitToCelsius( (float) fl ) ) );

                        currentFeelsLikeTemp.setLength( 0 );
                        currentFeelsLikeTemp.append( Math.round(
                                UtilityMethod.fahrenheitToCelsius( (float) weatherBitWx.getData().get( 0 ).getAppTemp() ) ) );

                        // not supplied by provider
                        currentHigh.setLength( 0 );
                        currentHigh.append( 0  + DEGREES );

                        currentWindSpeed.setLength( 0 );
                        currentWindSpeed.append(
                                Math.round( UtilityMethod.mphToKmh( weatherBitWx.getData().get( 0 ).getWindSpeed() ) ) );
                    }// end of if block
                    else
                    {
                        currentTemp.setLength( 0 );
                        currentTemp.append( Math.round( (float) weatherBitWx.getData().get( 0 ).getTemp() ) );

                        currentHigh.setLength( 0 );
                        currentHigh.append( Math.round( 0 ) ); // not supplied by provider

                        currentLow.setLength( 0 );
                        currentLow.append( 0 ); // not supplied by provider

                        currentWindSpeed.setLength( 0 );
                        currentWindSpeed.append( Math.round( weatherBitWx.getData().get( 0 ).getWindSpeed() ) );
                    }// end of else block

                    // Display weather data on widget
                    remoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + tempUnits);
                    remoteViews.setTextViewText( R.id.txvFeelsLike, String.format(
                            "%s %s%s", FEELS_LIKE, currentFeelsLikeTemp, DEGREES ) );
                    remoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                    remoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );

                    remoteViews.setTextViewText( R.id.txvWindReading, currentWindDirection +
                            " " + currentWindSpeed + ( WeatherLionApplication.storedPreferences.getUseMetric()
                            ? " km/h" : " mph" ) );
                    remoteViews.setTextViewText( R.id.txvHumidity, currentHumidity.toString() );

                    // Five Day Forecast
                    List< WeatherBitWeatherDataItem.SixteenDayForecastData.Data > wFdf = weatherBitFx.getData();
                    int count = wFdf.size(); // number of items in the array
                    double lowTemp;
                    double Temps;
                    i = 1;
                    hl = new int[ 5 ][ 2 ];

                    SimpleDateFormat df;

                    for ( WeatherBitWeatherDataItem.SixteenDayForecastData.Data wxForecast : wFdf )
                    {
                        String fxDate = null;
                        df = new SimpleDateFormat( "yyyy-MM-dd", Locale.ENGLISH );
                        String dt = wxForecast.getDatetime();

                        try
                        {
                            fxDate = df.format( df.parse( dt ) );
                        }// end of try block
                        catch ( ParseException e )
                        {
                            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE , e.getMessage(),
                                    TAG + "::updateTemps [line: " +
                                            e.getStackTrace()[ 1 ].getLineNumber() + "]" );
                        }// end of catch block

                        if( Objects.requireNonNull( fxDate ).equals( df.format( new Date() ) ) )
                        {
                            currentHigh.setLength( 0 );
                            currentHigh.append( Math.round( wFdf.get( i ).getMaxTemp() ) );

                            currentLow.setLength( 0 );
                            currentLow.append( Math.round( wFdf.get( i ).getMinTemp() ) );

                            remoteViews.setTextViewText( R.id.txvDayHigh, ( Integer.parseInt( currentHigh.toString() ) > Integer.parseInt( currentTemp.toString().replace( "°F" , "" ) )
                                    ? currentHigh.toString() : Integer.parseInt( currentTemp.toString().replace( "°F" , "" ) ) + DEGREES ) );

                            remoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );

                            Temps = wFdf.get( i ).getMaxTemp() > Double.parseDouble( currentTemp.toString().replace( "°F" , "" ) )
                                    ? wFdf.get( i ).getMaxTemp() : Double.parseDouble( currentTemp.toString().replace( "°F" , "" ) );
                        }// end of if block
                        else
                        {
                            Temps = wFdf.get( i ).getMaxTemp();
                        }// end of else block

                        // this data that they provide is inaccurate but it will be used
                        lowTemp = wFdf.get( i ).getMinTemp();

                        if( WeatherLionApplication.storedPreferences.getUseMetric() )
                        {
                            Temps = Math.round(
                                    UtilityMethod.fahrenheitToCelsius( (float) Temps ) );
                            lowTemp = Math.round(
                                    UtilityMethod.fahrenheitToCelsius( (float) lowTemp ) );
                        }// end of if block
                        else
                        {
                            Temps = Math.round( Temps );
                            lowTemp = Math.round( lowTemp );
                        }// end of else block

                        hl[ i - 1 ][ 0 ] = (int) Temps;
                        hl[ i - 1 ][ 1 ] = (int) lowTemp;

                        String fHigh = String.valueOf(  (int) Temps );
                        String fLow = String.valueOf(  (int) lowTemp );
                        String temps = String.format( "%s° %s°", fLow, fHigh );
                        int dayTemps = this.getResources().getIdentifier( "txvDay" + (i) + "Temps",
                                "id", this.getPackageName() );

                        remoteViews.setTextViewText( dayTemps, temps );

                        if( i == 5 )
                        {
                            break;
                        }// end of if block

                        i++; // increment sentinel
                    }// end of for each loop

                    break;
                case WeatherLionApplication.YAHOO_WEATHER:
                    currentWindSpeed.setLength( 0 );
                    currentWindSpeed.append( yahoo19.getCurrentObservation().getWind().getSpeed() );

                    currentWindDirection.setLength( 0 );
                    currentWindDirection.append( UtilityMethod.compassDirection(
                            yahoo19.getCurrentObservation().getWind().getDirection() ) );

                    fl = UtilityMethod.heatIndex(
                            yahoo19.getCurrentObservation().getCondition().getTemperature(),
                            yahoo19.getCurrentObservation().getAtmosphere().getHumidity() );

                    if( WeatherLionApplication.storedPreferences.getUseMetric() )
                    {
                        currentTemp.setLength( 0 );
                        currentTemp.append( Math.round( UtilityMethod.fahrenheitToCelsius(
                                (float) yahoo19.getCurrentObservation().getCondition().getTemperature() ) ) );

                        currentFeelsLikeTemp.setLength( 0 );
                        currentFeelsLikeTemp.append( Math.round(
                                UtilityMethod.fahrenheitToCelsius( (float) fl ) ) );

                        currentWindSpeed.setLength( 0 );
                        currentWindSpeed.append(
                                Math.round(UtilityMethod.mphToKmh( yahoo19.getCurrentObservation().getWind().getSpeed() ) ) );
                    }// end of if block
                    else
                    {
                        currentTemp.setLength( 0 );
                        currentTemp.append( Math.round(
                                yahoo19.getCurrentObservation().getCondition().getTemperature() ) );

                        currentFeelsLikeTemp.setLength( 0 );
                        currentFeelsLikeTemp.append( Math.round( fl ) );

                        currentWindSpeed.setLength( 0 ); // reset
                        currentWindSpeed.append( yahoo19.getCurrentObservation().getWind().getSpeed() );
                    }// end of else block

                    // Display weather data on widget
                    remoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + tempUnits );
                    remoteViews.setTextViewText( R.id.txvFeelsLike, String.format(
                            "%s %s%s", FEELS_LIKE, currentFeelsLikeTemp, DEGREES ) );
                    remoteViews.setTextViewText( R.id.txvWindReading, currentWindDirection +
                            " " + currentWindSpeed + ( WeatherLionApplication.storedPreferences.getUseMetric()
                            ? " km/h" : " mph" ) );

                    List< YahooWeatherYdnDataItem.Forecast > yFdf = yahoo19.getForecast();

                    hl = new int[ 5 ][ 2 ];

                    for ( i = 0; i <= yFdf.size() - 1; i++ )
                    {
                        df = new SimpleDateFormat( "dd MMM yyyy", Locale.ENGLISH );
                        String fDate = df.format( UtilityMethod.getDateTime( yFdf.get( i ).getDate() ) );
                        today = df.format( new Date() );

                        String temps;
                        String fh;
                        String fLow;

                        if( WeatherLionApplication.storedPreferences.getUseMetric() )
                        {
                            remoteViews.setTextViewText( R.id.txvDayHigh, Math.round( UtilityMethod.fahrenheitToCelsius(
                                    (float) yFdf.get( i ).getHigh() ) ) + DEGREES );
                            remoteViews.setTextViewText( R.id.txvDayLow, Math.round( UtilityMethod.fahrenheitToCelsius(
                                    (float) yFdf.get( i ).getLow() ) ) + DEGREES );

                            fh = String.valueOf( Math.round( UtilityMethod.fahrenheitToCelsius( (float) yFdf.get( i ).getHigh() ) ) );
                            fLow = String.valueOf( Math.round( UtilityMethod.fahrenheitToCelsius( (float) yFdf.get( i ).getLow() ) ) );
                            temps = String.format( "%s° %s°", fLow, fh );

                        }// end of if block
                        else
                        {
                            if( fDate.equals( today ) )
                            {
                                currentHigh.setLength( 0 );
                                currentHigh.append( (int) yFdf.get( i ).getHigh() );

                                currentLow.setLength( 0 );
                                currentLow.append( (int) yFdf.get( i ).getLow() );

                                remoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                                remoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );
                            }// end of if block

                            fh = String.valueOf( Math.round( yFdf.get( i ).getHigh() ) );
                            fLow = String.valueOf( Math.round( yFdf.get( i ).getLow() ) );

                            temps = String.format( "%s° %s°", fLow, fh );
                        }// end of else block

                        hl[i][0] = Integer.parseInt( fh );
                        hl[i][1] = Integer.parseInt( fLow );
                        int dayTemps = this.getResources().getIdentifier( "txvDay" + (i) + "Temps",
                                "id", this.getPackageName() );

                        remoteViews.setTextViewText( dayTemps, temps );

                        if( i == 4 )
                        {
                            break;
                        }// end of if block
                    }// end of for loop

                    break;
                case WeatherLionApplication.YR_WEATHER:
                    currentWindDirection.setLength( 0 );
                    currentWindDirection.append(
                            yr.getForecast().get( 0 ).getWindDirCode() );

                    if( WeatherLionApplication.storedPreferences.getUseMetric() )
                    {
                        currentTemp.setLength( 0 );
                        currentTemp.append( yr.getForecast().get( 0 ).getTemperatureValue() );

                        currentWindSpeed.setLength( 0 );
                        currentWindSpeed.append(
                                Math.round(
                                        UtilityMethod.mpsToKmh( yr.getForecast().get( 0 ).getWindSpeedMps() ) ) );

                        int feelsLike = UtilityMethod.calculateWindChill(
                                Math.round( yr.getForecast().get( 0 ).getTemperatureValue() ),
                                Integer.parseInt( currentWindSpeed.toString() ) );

                        currentFeelsLikeTemp.setLength( 0 );
                        currentFeelsLikeTemp.append( feelsLike );
                    }// end of if block
                    else
                    {
                        currentTemp.setLength( 0 );
                        currentTemp.append( Math.round(
                                UtilityMethod.celsiusToFahrenheit(
                                        yr.getForecast().get( 0 ).getTemperatureValue() ) ) );

                        currentWindSpeed.setLength( 0 );
                        currentWindSpeed.append(
                                Math.round( UtilityMethod.mpsToMph( yr.getForecast().get( 0 ).getWindSpeedMps() ) ) );

                        int feelsLike = UtilityMethod.calculateWindChill(
                                Integer.parseInt( currentTemp.toString() ),
                                Integer.parseInt( currentWindSpeed.toString() ) );

                        currentFeelsLikeTemp.setLength( 0 );
                        currentFeelsLikeTemp.append( feelsLike );
                    }// end of else block

                    // Display weather data on widget
                    remoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + tempUnits );
                    remoteViews.setTextViewText( R.id.txvFeelsLike, String.format(
                            "%s %s%s", FEELS_LIKE, currentFeelsLikeTemp, DEGREES ) );
                    remoteViews.setTextViewText( R.id.txvWindReading, currentWindDirection +
                            " " + currentWindSpeed + ( WeatherLionApplication.storedPreferences.getUseMetric()
                            ? " km/h" : " mph" ) );

                    List< YrWeatherDataItem.Forecast > fdf = yr.getForecast();

                    // Five Day Forecast
                    i = 1;
                    float fHigh = 0;    // forecasted high
                    float fLow = 0;     // forecasted low
                    Date currentDate = new Date();
                    dailyReading = new Hashtable<>();
                    int x = 0;
                    df = new SimpleDateFormat( "MMMM dd, yyyy", Locale.ENGLISH );
                    String temps;

                    // get the highs and lows from the forecast first
                    for ( Forecast wxTempReading : fdf )
                    {
                        x++;

                        if ( x == 1 )
                        {
                            currentDate = wxTempReading.getTimeFrom();
                            fHigh = (float) Math.round( UtilityMethod.celsiusToFahrenheit( wxTempReading.getTemperatureValue() ) );
                            fLow = (float) Math.round( UtilityMethod.celsiusToFahrenheit( wxTempReading.getTemperatureValue() ) );
                        }// end of if block

                        // monitor date change
                        if ( df.format( wxTempReading.getTimeFrom() ).equals( df.format( currentDate ) ) )
                        {
                            float cr = (float) Math.round( UtilityMethod.celsiusToFahrenheit( wxTempReading.getTemperatureValue() ) );

                            if ( cr > fHigh )
                            {
                                fHigh = cr;
                            }// end of if block

                            if (cr < fLow)
                            {
                                fLow = cr;
                            }// end of if block
                        }// end of if block

                        if ( wxTempReading.getTimePeriod() == 3 )
                        {
                            x = 0;
                            float[][] hl = { { fHigh, fLow } };
                            dailyReading.put( df.format( wxTempReading.getTimeFrom() ), hl );
                        }// end of if block
                    }// end of first for each loop

                    x = 0;

                    // repeat the loop and store the five day forecast
                    for ( Forecast wxForecast : fdf )
                    {
                        x++;

                        String fDate = df.format( wxForecast.getTimeFrom() );

                        // the first time period is always the current reading for this moment
                        if ( x == 1 )
                        {
                            fHigh = dailyReading.get( df.format( wxForecast.getTimeFrom() ) ) [ 0 ][ 0 ];
                            fLow = dailyReading.get( df.format( wxForecast.getTimeFrom() ) ) [ 0 ][ 1 ];

                            if( WeatherLionApplication.storedPreferences.getUseMetric() )
                            {
                                remoteViews.setTextViewText( R.id.txvDayHigh, fHigh + DEGREES );
                                remoteViews.setTextViewText( R.id.txvDayLow, fLow + DEGREES );

                                fHigh = Math.round(
                                        UtilityMethod.celsiusToFahrenheit(
                                                dailyReading.get( df.format( wxForecast.getTimeFrom() ) )[ 0 ][ 0 ] ) );
                                fLow = Math.round(
                                        UtilityMethod.celsiusToFahrenheit(
                                                dailyReading.get( df.format( wxForecast.getTimeFrom() ) )[ 0 ][ 1 ] ) );

                                temps = String.format( "%s° %s°", (int) fLow, (int) fHigh );

                            }// end of if block
                            else
                            {
                                if( fDate.equals( df.format( new Date() ) ) )
                                {
                                    currentHigh.setLength( 0 );
                                    currentHigh.append( (int) fHigh );

                                    currentLow.setLength( 0 );
                                    currentLow.append( (int) fLow );

                                    remoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                                    remoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );
                                }// end of if block

                                temps = String.format( "%s° %s°", (int) fLow, (int) fHigh );
                            }// end of else block

                            int dayTemps = this.getResources().getIdentifier( "txvDay" + (i) + "Temps",
                                    "id", this.getPackageName() );

                            remoteViews.setTextViewText( dayTemps, temps );

                            if ( i == 5 )
                            {
                                break;
                            }// end of if block

                            i++; // increment sentinel
                        }// end of if block

                        if ( wxForecast.getTimePeriod() == 3 )
                        {
                            x = 0;
                        }// end of if block

                    }// end of second for each loop

                    break;
                default:
                    break;
            }// end of switch block
        }// end of if block
        else // if there is no Internet connection
        {
            tempUnits = WeatherLionApplication.storedPreferences.getUseMetric() ? CELSIUS : FAHRENHEIT;

            // populate the global variables
            currentWindDirection.setLength( 0 );
            currentWindDirection.append( WeatherLionApplication.storedData.getWind().getWindDirection() );

            currentWindSpeed.setLength( 0 );
            currentWindSpeed.append( WeatherLionApplication.storedData.getWind().getWindSpeed() );

            currentHumidity.setLength( 0 );
            currentHumidity.append( WeatherLionApplication.storedData.getAtmosphere().getHumidity() );

            if( WeatherLionApplication.storedPreferences.getUseMetric() )
            {
                currentTemp.setLength( 0 );
                currentTemp.append( Math.round( UtilityMethod.fahrenheitToCelsius(
                        WeatherLionApplication.storedData.getCurrent().getTemperature() ) ) );

                currentFeelsLikeTemp.setLength( 0 );
                currentFeelsLikeTemp.append( Math.round( UtilityMethod.fahrenheitToCelsius(
                        WeatherLionApplication.storedData.getCurrent().getFeelsLike() ) ) );

                currentHigh.setLength( 0 );
                currentHigh.append( Math.round( UtilityMethod.fahrenheitToCelsius(
                        WeatherLionApplication.storedData.getCurrent().getHighTemperature() ) ) );

                currentLow.setLength( 0 );
                currentLow.append( Math.round( UtilityMethod.fahrenheitToCelsius(
                        WeatherLionApplication.storedData.getCurrent().getLowTemperature() ) ) );

                currentWindSpeed.setLength( 0 );
                currentWindSpeed.append(
                        Math.round( UtilityMethod.mphToKmh( WeatherLionApplication.storedData.getWind().getWindSpeed() ) ) );
            }// end of if block
            else
            {
                currentTemp.setLength( 0 );
                currentTemp.append( WeatherLionApplication.storedData.getCurrent().getTemperature() );

                currentFeelsLikeTemp.setLength( 0 );
                currentFeelsLikeTemp.append( Math.round(
                        WeatherLionApplication.storedData.getCurrent().getFeelsLike() ) );

                currentHigh.setLength( 0 );
                currentHigh.append( Math.round(
                        WeatherLionApplication.storedData.getCurrent().getHighTemperature() ) );

                currentLow.setLength( 0 );
                currentLow.append( Math.round(
                        WeatherLionApplication.storedData.getCurrent().getLowTemperature() ) );

                currentWindSpeed.setLength( 0 );
                currentWindSpeed.append( Math.round(
                        WeatherLionApplication.storedData.getWind().getWindSpeed() ) );
            }// end of else block

            // Display weather data on widget
            remoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + tempUnits );
            remoteViews.setTextViewText( R.id.txvFeelsLike, "Feels Like " +
                    currentFeelsLikeTemp.toString() + DEGREES );
            remoteViews.setTextViewText( R.id.txvDayHigh, currentHigh.toString() + DEGREES );
            remoteViews.setTextViewText( R.id.txvDayLow, currentLow.toString() + DEGREES );

            remoteViews.setTextViewText( R.id.txvWindReading, currentWindDirection +
                    " " + currentWindSpeed + ( WeatherLionApplication.storedPreferences.getUseMetric()
                    ? " km/h" : " mph" ) );

            hl = new int[ 5 ][ 2 ];

            for ( i = 0; i <=  WeatherLionApplication.storedData.getDailyForecast().size(); i++ )
            {
                LastWeatherData.WeatherData.DailyForecast.DayForecast wxDayForecast =
                        WeatherLionApplication.storedData.getDailyForecast().get( i );

                Date forecastDate = null;

                try
                {
                    forecastDate = new SimpleDateFormat( "EEE MMM dd HH:mm:ss z yyyy",
                        Locale.ENGLISH ).parse( wxDayForecast.getDate() );
                }// end of try block
                catch ( ParseException e )
                {
                    UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, "Couldn't parse the forecast date!",
                TAG + "::updateTemps");
                }// end of catch block

                DateFormat df = new SimpleDateFormat( "dd MMM yyyy", Locale.ENGLISH );
                String fDate = df.format( forecastDate );

                String temps;
                String fh;
                String fLow;

                if( WeatherLionApplication.storedPreferences.getUseMetric() )
                {
                    remoteViews.setTextViewText( R.id.txvDayHigh, Math.round( UtilityMethod.fahrenheitToCelsius(
                            wxDayForecast.getHighTemperature() ) ) + DEGREES );
                    remoteViews.setTextViewText( R.id.txvDayLow, Math.round( UtilityMethod.fahrenheitToCelsius(
                            wxDayForecast.getLowTemperature() ) ) + DEGREES );

                    fh = String.valueOf( Math.round( UtilityMethod.fahrenheitToCelsius( wxDayForecast.getHighTemperature() ) ) );
                    fLow = String.valueOf( Math.round( UtilityMethod.fahrenheitToCelsius( wxDayForecast.getLowTemperature() ) ) );
                    temps = String.format( "%s° %s°", ( int ) Float.parseFloat( fLow ), ( int ) Float.parseFloat( fh ) );

                }// end of if block
                else
                {
                    fh = String.valueOf( wxDayForecast.getHighTemperature() );
                    fLow = String.valueOf( wxDayForecast.getLowTemperature() );

                    if( fh.equals( "" ) )
                    {
                        fh = "0";
                    }// end of if block
                    else if( fLow.equals( "" )  )
                    {
                        fLow = "0";
                    }// end of if block

                    temps = String.format( "%s° %s°", ( int ) Float.parseFloat( fLow ), ( int ) Float.parseFloat( fh ) );
                }// end of else block

                hl[i][0] = ( int ) Float.parseFloat( fh );
                hl[i][1] = ( int ) Float.parseFloat( fLow );

                int dayTemps = this.getResources().getIdentifier( "txvDay" + (i + 1) + "Temps",
                        "id", this.getPackageName() );

                remoteViews.setTextViewText( dayTemps, temps );

                if( i == 4 )
                {
                    break;
                }// end of if block
            }// end of for loop
        }// end of else block

        // Update the color of the temperature label
        remoteViews.setTextColor( R.id.txvCurrentTemperature,
            ( UtilityMethod.temperatureColor( Integer.parseInt(
                currentTemp.toString().replaceAll( "\\D+","" ) ) ) ) );
    }// end of method updateTemps
}// end of class WidgetUpdateService