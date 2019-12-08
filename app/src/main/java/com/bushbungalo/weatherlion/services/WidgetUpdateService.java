package com.bushbungalo.weatherlion.services;

/*
 * Created by Paul O. Patterson on 11/30/17.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.bushbungalo.weatherlion.Preference;
import com.bushbungalo.weatherlion.PrefsActivity;
import com.bushbungalo.weatherlion.R;
import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.WeatherLionMain;
import com.bushbungalo.weatherlion.alarms.SunriseAlarmBroadcastReceiver;
import com.bushbungalo.weatherlion.alarms.SunsetAlarmBroadcastReceiver;
import com.bushbungalo.weatherlion.alarms.UpdateAlarmBroadcastReceiver;
import com.bushbungalo.weatherlion.model.CityData;
import com.bushbungalo.weatherlion.model.DarkSkyWeatherDataItem;
import com.bushbungalo.weatherlion.model.FiveDayForecast;
import com.bushbungalo.weatherlion.model.FiveHourForecast;
import com.bushbungalo.weatherlion.model.HereMapsWeatherDataItem;
import com.bushbungalo.weatherlion.model.LastWeatherData;
import com.bushbungalo.weatherlion.model.OpenWeatherMapWeatherDataItem;
import com.bushbungalo.weatherlion.model.TimeZoneInfo;
import com.bushbungalo.weatherlion.model.WeatherBitWeatherDataItem;
import com.bushbungalo.weatherlion.model.YahooWeatherYdnDataItem;
import com.bushbungalo.weatherlion.model.YrWeatherDataItem;
import com.bushbungalo.weatherlion.model.YrWeatherDataItem.Forecast;
import com.bushbungalo.weatherlion.utils.HttpHelper;
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings({"unused", "SameParameterValue"})
public class WidgetUpdateService extends JobIntentService
{
    private static final int JOB_ID = 79;

    public static final String TAG = "WidgetUpdateService";
    public static final String WEATHER_SERVICE_INVOKER = "WeatherServiceInvoker";
    public static final String WEATHER_DATA_UNIT_CHANGED = "WeatherDataUnitChanged";
    public static final String WEATHER_UPDATE_SERVICE_MESSAGE = "WidgetUpdateServiceMessage";
    public static final String WEATHER_XML_SERVICE_MESSAGE = "WeatherXmlServiceMessage";
    public static final String WEATHER_XML_SERVICE_PAYLOAD = "WeatherXmlServicePayload";
    public static final String WEATHER_LOADING_ERROR_MESSAGE = "WidgetLoadingErrorMessage";
    public static final String ASTRONOMY_MESSAGE = "UpdateAstronomy";
    public static final String ASTRONOMY_PAYLOAD = "TimeOfDay";

    private static DarkSkyWeatherDataItem darkSky;
    private static HereMapsWeatherDataItem.WeatherData hereWeatherWx;
    private static HereMapsWeatherDataItem.ForecastData hereWeatherFx;
    private static HereMapsWeatherDataItem.AstronomyData hereWeatherAx;
    private static OpenWeatherMapWeatherDataItem.WeatherData openWeatherWx;
    private static OpenWeatherMapWeatherDataItem.ForecastData openWeatherFx;
    private static WeatherBitWeatherDataItem.WeatherData weatherBitWx;
    private static WeatherBitWeatherDataItem.SixteenDayForecastData weatherBitFx;
    private static WeatherBitWeatherDataItem.FortyEightHourForecastData weatherBitHx;
    private static YahooWeatherYdnDataItem yahoo19;
    private static YrWeatherDataItem yr;

    private StringBuilder wxUrl = new StringBuilder();  // weather data url
    private StringBuilder fxUrl = new StringBuilder();  // forecast data url
    private StringBuilder hxUrl = new StringBuilder();  // hourly forecast data url
    private StringBuilder axUrl = new StringBuilder();  // astronomy data url
    private ArrayList<String> strJSON;

    private final String CELSIUS = "\u00B0C";
    private final String DEGREES = "\u00B0";
    private final String FAHRENHEIT = "\u00B0F";
    public static final String FEELS_LIKE = "Feels Like";

    private static StringBuilder currentCity = new StringBuilder();
    private static StringBuilder currentCountry = new StringBuilder();
    private static StringBuilder currentTemp = new StringBuilder();
    private static StringBuilder currentFeelsLikeTemp = new StringBuilder();
    private static StringBuilder currentWindSpeed = new StringBuilder();
    private static StringBuilder currentWindDirection = new StringBuilder();
    private static StringBuilder currentHumidity = new StringBuilder();
    public  static StringBuilder currentCondition = new StringBuilder();
    private static StringBuilder currentHigh = new StringBuilder();
    private static StringBuilder currentLow = new StringBuilder();

    private static String currentLocation;
    private static List< FiveDayForecast > currentFiveDayForecast = new ArrayList<>();
    private static List< FiveHourForecast > currentFiveHourForecast = new ArrayList<>();
    private static int[][] hl;

    private boolean unitChange;
    private boolean weatherUpdate;
    private Dictionary< String, float[][] > dailyReading;
    private Dictionary< String, Float > hourlyReading;
    private String tempUnits;
    private LocalDateTime checkTime;

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

    public static final String SUNRISE = "Sunrise";
    public static final String SUNSET = "Sunset";

    public static boolean widgetRefreshRequired;

    private static RemoteViews largeWidgetRemoteViews;
    private static RemoteViews smallWidgetRemoteViews;

    private SharedPreferences spf = null;

    private boolean loadingPreviousWeather;
    private boolean methodCalledByReflection;

    private AppWidgetManager appWidgetManager;

    // method name constants
    public static final String LOAD_PREVIOUS_WEATHER = "loadPreviousWeatherData";
    public static final String LOAD_WIDGET_BACKGROUND = "loadWidgetBackground";
    public static final String LOAD_WIDGET_ICON_SET = "loadWeatherIconSet";
    public static final String ASTRONOMY_CHANGE = "astronomyChange";

    private String wxDataProvider;
    private int expectedJSONSize;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate()
    {
        super.onCreate();
        spf = PreferenceManager.getDefaultSharedPreferences( this );

        LocalBroadcastManager.getInstance( this )
            .registerReceiver( webServiceData,
                new IntentFilter( HttpHelper.WEB_SERVICE_DATA_MESSAGE ) );

    }// end of method onCreate

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        LocalBroadcastManager.getInstance( this ).unregisterReceiver( webServiceData );
    }// end of method onDestroy

    public static void enqueueWork( Context context, Intent work )
    {
        enqueueWork( context, WidgetUpdateService.class, JOB_ID, work );
    }// end of method enqueueWork

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
        // load all widget ids associated with the application
        WidgetHelper.getWidgetIds();

        largeWidgetRemoteViews = new RemoteViews( this.getPackageName(),
            R.layout.wl_large_weather_widget_activity_alternate);

        smallWidgetRemoteViews = new RemoteViews( this.getPackageName(),
                R.layout.wl_small_weather_widget_activity );

        appWidgetManager = AppWidgetManager.getInstance( this );

        currentLocation = spf.getString( WeatherLionApplication.CURRENT_LOCATION_PREFERENCE,
                Preference.DEFAULT_WEATHER_LOCATION );
        boolean locationSet = !Objects.requireNonNull( currentLocation ).equalsIgnoreCase(
                Preference.DEFAULT_WEATHER_LOCATION );

        Bundle extras = intent.getExtras();
        String callMethod = null;
        String invoker = null;

        if( extras != null )
        {
            // if the unit in use is to be changed
            if( extras.getString( WEATHER_DATA_UNIT_CHANGED ) != null )
            {
                unitChange = Boolean.parseBoolean( extras.getString( WEATHER_DATA_UNIT_CHANGED ) );
            }// end of if block

            // the extra must be a string representation of a method
            callMethod = extras.getString( WeatherLionApplication.LAUNCH_METHOD_EXTRA );

            // the algorithm that invoked this class
            invoker = extras.getString( WEATHER_SERVICE_INVOKER );
        }// end of if block

        // the caller requires only a method to be run
        if( callMethod != null )
        {
            methodCalledByReflection = true;
            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                    "Method " + callMethod + " called...", TAG + "::handleIntent" );

            if( callMethod.equals( ASTRONOMY_CHANGE ) )
            {
                callMethodByName( WidgetUpdateService.this, callMethod,
                        new Class[]{String.class, AppWidgetManager.class},
                        new Object[]{WeatherLionApplication.timeOfDayToUse,
                                appWidgetManager} );
            }// end of if block
            else
            {
                callMethodByName( WidgetUpdateService.this, callMethod,null,
                        null );
            }// end of else block

            // If a location has not been set then the weather cannot be processed
            if( !locationSet && !callMethod.equals( LOAD_WIDGET_BACKGROUND ) ) return;

            // update applicable widgets after the method call has completed
            updateAllAppWidgets( appWidgetManager );
        }// end of if block
        else
        {
            // ensure that a widget refresh has been requested or it is time for an update
            if( UtilityMethod.refreshRequestedBySystem
                    && !UtilityMethod.updateRequired( this ) )
            {
                return;
            }// end of if block
            else if( !UtilityMethod.refreshRequestedByUser
                    && !UtilityMethod.updateRequired( this ) )
            {
                return;
            }// end of if block

            // If a location has not been set then the weather cannot be processed
            if( !locationSet ) return;

            if( WeatherLionApplication.storedPreferences != null )
            {
                UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
            "Loading weather data requested by " + invoker + "...",
                TAG + "::handleIntent" );

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
                    currentCity.append( currentLocation );
                }// ed of else block

                // user only requires unit changes
                if( unitChange )
                {
                    weatherUpdate = false;
                    // update applicable widgets after the method call has completed
                    updateAllAppWidgets( appWidgetManager );
                }// end of if block
                else
                {
                    String json;
                    float lat;
                    float lng;
                    strJSON = new ArrayList<>();
                    weatherUpdate = true;

                    if( WeatherLionApplication.noAccessToStoredProvider )
                    {
                        wxDataProvider = WeatherLionApplication.webAccessGranted.get( 0 );
                    }// end of if block
                    else
                    {
                        wxDataProvider = WeatherLionApplication.storedPreferences.getProvider();
                    }// end of else block

                    boolean okToUse = UtilityMethod.okToUseService( wxDataProvider );

                    if( okToUse )
                    {
                        // Check the Internet connection availability
                        if( UtilityMethod.hasInternetConnection( this ) &&
                                UtilityMethod.updateRequired( getApplicationContext() ) ||
                                UtilityMethod.hasInternetConnection( this ) &&
                                        UtilityMethod.refreshRequestedBySystem ||
                                UtilityMethod.refreshRequestedByUser )
                        {
                            wxUrl.setLength( 0 );
                            fxUrl.setLength( 0 );
                            hxUrl.setLength( 0 );
                            axUrl.setLength( 0 );

                            // if this location has already been used there is no need to query the
                            // web service as the location data has been stored locally
                            CityData.currentCityData = UtilityMethod.cityFoundInJSONStorage(
                                    WeatherLionApplication.currentWxLocation );

                            if( CityData.currentCityData == null )
                            {
                                json =
                                        UtilityMethod.retrieveGeoNamesGeoLocationUsingAddress(
                                                WeatherLionApplication.currentWxLocation );
                                CityData.currentCityData = UtilityMethod.createGeoNamesCityData( json );

                                lat = CityData.currentCityData.getLatitude();
                                lng = CityData.currentCityData.getLongitude();

                                if( WeatherLionApplication.currentLocationTimeZone == null )
                                {
                                    WeatherLionApplication.currentLocationTimeZone =
                                            UtilityMethod.retrieveGeoNamesTimeZoneInfo( lat, lng );
                                }// end of if block

                                // This data may have been corrupted due to a previous crash
                                if( !WeatherLionApplication.storedData.getLocation().getTimezone()
                                        .equalsIgnoreCase( CityData.currentCityData.getTimeZone() ) )
                                {
                                    WeatherLionApplication.storedData.getLocation().setTimezone(
                                            CityData.currentCityData.getTimeZone() );

                                    WeatherLionApplication.storedData.getLocation().setCountry(
                                            CityData.currentCityData.getCountryName() );
                                }// end of if block

                                CityData.currentCityData.setTimeZone(
                                        WeatherLionApplication.currentLocationTimeZone.getTimezoneId() );
                            }// end of if block
                            else
                            {
                                lat = CityData.currentCityData.getLatitude();
                                lng = CityData.currentCityData.getLongitude();

                                // If timezones are inconsistent
                                if( !WeatherLionApplication.storedData.getLocation().getTimezone()
                                    .equalsIgnoreCase( CityData.currentCityData.getTimeZone() ) )
                                {
                                    WeatherLionApplication.storedData.getLocation().setTimezone(
                                            CityData.currentCityData.getTimeZone() );

                                    WeatherLionApplication.currentSunriseTime.setLength( 0 );
                                    WeatherLionApplication.currentSunsetTime.setLength( 0 );
                                }// end of if block

                                if( WeatherLionApplication.currentSunriseTime.length() == 0 )
                                {
                                    if( WeatherLionApplication.currentLocationTimeZone == null )
                                    {
                                        WeatherLionApplication.currentLocationTimeZone =
                                                UtilityMethod.retrieveGeoNamesTimeZoneInfo( lat, lng );

                                        WeatherLionApplication.currentSunriseTime = new StringBuilder();
                                        WeatherLionApplication.currentSunsetTime = new StringBuilder();

                                        sunriseTime.setLength( 0 );
                                        sunsetTime.setLength( 0 );

                                        WeatherLionApplication.currentSunriseTime.append( new SimpleDateFormat( "h:mm a",
                                            Locale.ENGLISH ).format(
                                                WeatherLionApplication.currentLocationTimeZone.getSunrise() ) );

                                        WeatherLionApplication.currentSunsetTime.append( new SimpleDateFormat( "h:mm a",
                                            Locale.ENGLISH ).format(
                                                WeatherLionApplication.currentLocationTimeZone.getSunset() ) );

                                        sunriseTime.append( new SimpleDateFormat( "h:mm a",
                                                Locale.ENGLISH ).format(
                                                WeatherLionApplication.currentLocationTimeZone.getSunrise() ) );

                                        sunsetTime.append( new SimpleDateFormat( "h:mm a",
                                                Locale.ENGLISH ).format(
                                                WeatherLionApplication.currentLocationTimeZone.getSunset() ) );

                                    }// end of if block
                                    else
                                    {
                                        WeatherLionApplication.currentSunriseTime = new StringBuilder();
                                        WeatherLionApplication.currentSunsetTime = new StringBuilder();

                                        sunriseTime.setLength( 0 );
                                        sunsetTime.setLength( 0 );

                                        WeatherLionApplication.currentSunriseTime.append( new SimpleDateFormat( "h:mm a",
                                                Locale.ENGLISH ).format(
                                                WeatherLionApplication.currentLocationTimeZone.getSunrise() ) );

                                        WeatherLionApplication.currentSunsetTime.append( new SimpleDateFormat( "h:mm a",
                                                Locale.ENGLISH ).format(
                                                WeatherLionApplication.currentLocationTimeZone.getSunset() ) );

                                        sunriseTime.append( new SimpleDateFormat( "h:mm a",
                                                Locale.ENGLISH ).format(
                                                WeatherLionApplication.currentLocationTimeZone.getSunrise() ) );

                                        sunsetTime.append( new SimpleDateFormat( "h:mm a",
                                                Locale.ENGLISH ).format(
                                                WeatherLionApplication.currentLocationTimeZone.getSunset() ) );
                                    }// end of else block
                                }// end of if block

                                String today = new SimpleDateFormat( "MM/dd/yyyy",
                                        Locale.ENGLISH ).format( new Date() );

                                String sst = String.format( "%s %s", today,  WeatherLionApplication.currentSunsetTime.toString() );
                                String srt = String.format( "%s %s", today,  WeatherLionApplication.currentSunriseTime.toString() );

                                Date schedSunriseTime = null;
                                Date schedSunsetTime = null;

                                SimpleDateFormat sdf = new SimpleDateFormat( "MM/dd/yyyy h:mm a",
                                        Locale.ENGLISH );

                                try
                                {
                                    schedSunsetTime = sdf.parse( sst );
                                    schedSunriseTime = sdf.parse( srt );
                                } // end of try block
                                catch ( ParseException e )
                                {
                                    UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE , e.getMessage(),
                                    TAG + "::scheduleAstronomyUpdate [line: " + e.getStackTrace()[ 1 ].getLineNumber() + "]" );
                                }// end of catch block

                                WeatherLionApplication.localDateTime = new Date().toInstant().atZone(
                                    ZoneId.of( CityData.currentCityData.getTimeZone()
                                        ) ).toLocalDateTime();

                                // Load the time zone info for the current city
                                WeatherLionApplication.currentLocationTimeZone = new TimeZoneInfo(
                                        CityData.currentCityData.getCountryCode(),
                                        CityData.currentCityData.getCountryName(),
                                        CityData.currentCityData.getLatitude(),
                                        CityData.currentCityData.getLongitude(),
                                        CityData.currentCityData.getTimeZone(),
                                        UtilityMethod.getDateTime( WeatherLionApplication.localDateTime ),
                                        schedSunriseTime,
                                        schedSunsetTime );
                            }// end of else block

                            switch( wxDataProvider )
                            {
                                case WeatherLionApplication.DARK_SKY:
                                    wxUrl.setLength( 0 );
                                    wxUrl.append( String.format( "https://api.darksky.net/forecast/%s/%s,%s",
                                            darkSkyApiKey, lat, lng ) );

                                    break;
                                case WeatherLionApplication.OPEN_WEATHER:
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
                                    wxUrl.setLength( 0 );
                                    wxUrl.append(
                                            String.format( "https://api.weatherbit.io/v2.0/current?city=%s&units=I&key=%s",
                                                    UtilityMethod.escapeUriString( currentCity.toString() ), weatherBitApiKey ) );

                                    // 48 hour forecast data
                                    hxUrl.setLength( 0 );
                                    hxUrl.append(
                                            String.format( "https://api.weatherbit.io/v2.0/forecast/hourly?city=%s&units=I&key=%s&hours=48",
                                                    UtilityMethod.escapeUriString( currentCity.toString() ), weatherBitApiKey ) );

                                    // Sixteen day forecast will be used as it contains more relevant data
                                    fxUrl.setLength( 0 );
                                    fxUrl.append(
                                            String.format( "https://api.weatherbit.io/v2.0/forecast/daily?city=%s&units=I&key=%s",
                                                    UtilityMethod.escapeUriString( currentCity.toString() ), weatherBitApiKey ) );
                                    break;
                                case WeatherLionApplication.YAHOO_WEATHER:
                                    expectedJSONSize = 1;

                                    try
                                    {
                                        HttpHelper.getYahooWeatherData(
                                                WeatherLionApplication.storedPreferences.getLocation().toLowerCase(),
                                                yahooAppId, yahooConsumerKey, yahooConsumerSecret
                                        );
                                    }// end of try block
                                    catch ( Exception e )
                                    {
                                        dataRetrievalError( e );

                                        return;
                                    }// end of catch block

                                    break;
                                case WeatherLionApplication.YR_WEATHER:
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

                                    // Hourly forecast data
                                    hxUrl.setLength( 0 );
                                    hxUrl.append( String.format( "https://www.yr.no/place/%s/%s/%s/forecast_hour_by_hour.xml",
                                            countryName, regionName, cityName ) );
                                    break;
                            }// end of switch block

                            // Yahoo! Weather uses an OAuth method to access data from the web service
                            if( !wxDataProvider.equals( WeatherLionApplication.YAHOO_WEATHER ) )
                            {
                                try
                                {
                                    if( wxUrl.length() != 0 && fxUrl.length() != 0 && hxUrl.length() != 0 && axUrl.length() != 0 )
                                    {
                                        expectedJSONSize = 4;
                                        retrieveWeatherData( wxUrl.toString() );
                                        retrieveWeatherData( hxUrl.toString() );
                                        retrieveWeatherData( fxUrl.toString() );
                                        retrieveWeatherData( axUrl.toString() );
                                    }// end of if block
                                    else if( wxUrl.length() != 0 && fxUrl.length() != 0 && hxUrl.length() != 0 && axUrl.length() == 0 )
                                    {
                                        expectedJSONSize = 3;
                                        retrieveWeatherData( wxUrl.toString() );
                                        retrieveWeatherData( hxUrl.toString() );
                                        retrieveWeatherData( fxUrl.toString() );
                                    }// end of else if block
                                    else if( wxUrl.length() != 0 && fxUrl.length() != 0 && hxUrl.length() == 0 && axUrl.length() != 0 )
                                    {
                                        expectedJSONSize = 3;
                                        retrieveWeatherData( wxUrl.toString() );
                                        retrieveWeatherData( fxUrl.toString() );
                                        retrieveWeatherData( axUrl.toString() );
                                    }// end of else if block
                                    else if( wxUrl.length() != 0 && fxUrl.length() == 0 && hxUrl.length() == 0 && axUrl.length() != 0 )
                                    {
                                        expectedJSONSize = 3;
                                        retrieveWeatherData( wxUrl.toString() );
                                        retrieveWeatherData( hxUrl.toString() );
                                        retrieveWeatherData( axUrl.toString() );
                                    }// end of else if block
                                    else if( wxUrl.length() != 0 && fxUrl.length() != 0 && hxUrl.length() == 0 && axUrl.length() == 0 )
                                    {
                                        expectedJSONSize = 2;
                                        retrieveWeatherData( wxUrl.toString() );
                                        retrieveWeatherData( fxUrl.toString() );
                                    }// end of else if block
                                    else if( wxUrl.length() != 0 && fxUrl.length() == 0 && hxUrl.length() != 0 && axUrl.length() == 0 )
                                    {
                                        expectedJSONSize = 2;
                                        retrieveWeatherData( wxUrl.toString() );
                                        retrieveWeatherData( hxUrl.toString() );
                                    }// end of else if block
                                    else if( wxUrl.length() != 0 && fxUrl.length() == 0 && hxUrl.length() == 0 && axUrl.length() != 0 )
                                    {
                                        expectedJSONSize = 2;
                                        retrieveWeatherData( wxUrl.toString() );
                                        retrieveWeatherData( axUrl.toString() );
                                    }// end of else if block
                                    else if( ( wxUrl.length() != 0 && fxUrl.length() == 0 && hxUrl.length() == 0 && axUrl.length() == 0 ) ||
                                            ( wxUrl.length() == 0 && fxUrl.length() != 0  && hxUrl.length() == 0 && axUrl.length() == 0 ) ||
                                            ( wxUrl.length() == 0 && fxUrl.length() == 0  && hxUrl.length() != 0 && axUrl.length() == 0 ) ||
                                            ( wxUrl.length() == 0 && fxUrl.length() == 0  && hxUrl.length() == 0 && axUrl.length() != 0 ) )
                                    {
                                        expectedJSONSize = 1;

                                        if( wxUrl.length() != 0 )
                                        {
                                            retrieveWeatherData( wxUrl.toString() );
                                        }// end of if block
                                        else if( fxUrl.length() != 0 )
                                        {
                                            retrieveWeatherData( fxUrl.toString() );
                                        }// end of else if block
                                        else if( hxUrl.length() != 0 )
                                        {
                                            retrieveWeatherData( hxUrl.toString() );
                                        }// end of else if block
                                        else if( axUrl.length() != 0 )
                                        {
                                            retrieveWeatherData( axUrl.toString() );
                                        }// end of else if block
                                    }// end of else if block
                                }// end of try block
                                catch ( Exception e )
                                {
                                    dataRetrievalError( e );
                                }// end of catch block
                            }// end of if block
                        }// end of if block

                    }// end of if block
                    else
                    {
                        Intent settingsIntent = new Intent( this, PrefsActivity.class );
                        settingsIntent.putExtra( WeatherLionMain.LION_LIMIT_EXCEEDED_PAYLOAD, true );
                        startActivity( settingsIntent );
                    }// end of else
                }// end of else block
            }// end of if block
        }// end of else block
    }// end of method handleWeatherData

    /**
     * Changes the weather icon for the current reading based on the time of day.
     *
     * This method might be called reflectively and may appear to be unused
     */
    private void astronomyChange( String timeOfDay, AppWidgetManager appWidgetManager )
    {
        String currentConditionIcon = null;
        broadcastAstronomyChange( timeOfDay ); // inform the rest of the application

        switch( timeOfDay )
        {
            case SUNRISE:
                currentConditionIcon = UtilityMethod.weatherImages.get(
                        currentCondition.toString().toLowerCase() );

                UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                        String.format( "Switching to sunrise icon %s!", currentConditionIcon ),
                        TAG + "::astronomyChange" );
                break;
            case SUNSET:
                if ( currentCondition.toString().toLowerCase().contains( "(night)" ) )
                {
                    currentConditionIcon = UtilityMethod.weatherImages.get(
                            currentCondition.toString().toLowerCase() );
                }// end of if block
                else
                {
                    // Yahoo has a habit of having sunny nights
                    if ( currentCondition.toString().equalsIgnoreCase( "sunny" ) )
                    {
                        currentCondition.setLength( 0 );
                        currentCondition.append( "Clear" );
                        largeWidgetRemoteViews.setTextViewText( R.id.txvWeatherCondition,
                                currentCondition.toString() );
                    }// end of if block

                    if ( UtilityMethod.weatherImages.containsKey(
                            WidgetUpdateService.currentCondition.toString().toLowerCase() + " (night)" ) )
                    {
                        currentConditionIcon =
                            UtilityMethod.weatherImages.get(
                                currentCondition.toString().toLowerCase() + " (night)" );

                        UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                    "Switching to sunset icon to " + currentConditionIcon,
                        TAG + "::astronomyChange" );
                    }// end of if block
                    else
                    {
                        UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                            String.format( "No night icon exists for %s!",
                                currentCondition.toString() ),
                                TAG + "::astronomyChange" );

                        // there will most likely be a day icon but not a night one so exit here
                        return;
                    }// end of else block
                }// end of else block

                break;
        }// end of switch block

        // Load applicable icon based on the time of day
        String imageFile = String.format( "%s%s/weather_%s",
             WeatherLionApplication.WEATHER_IMAGES_ROOT, WeatherLionApplication.iconSet
                , currentConditionIcon );

        largeWidgetRemoteViews = new RemoteViews(
            WeatherLionApplication.getAppContext().getPackageName(),
                R.layout.wl_large_weather_widget_activity_alternate);

        smallWidgetRemoteViews = new RemoteViews(
            WeatherLionApplication.getAppContext().getPackageName(),
                R.layout.wl_small_weather_widget_activity );

        try( InputStream is = WeatherLionApplication.getAppContext().getAssets().open( imageFile ) )
        {
            Bitmap bmp = BitmapFactory.decodeStream( is );
            largeWidgetRemoteViews.setImageViewBitmap( R.id.imvCurrentCondition, bmp );
            smallWidgetRemoteViews.setImageViewBitmap( R.id.imvCurrentCondition, bmp );

            // update all widgets
            updateAllAppWidgets( appWidgetManager );
        }// end of try block
        catch ( IOException e )
        {
            UtilityMethod.butteredToast( WeatherLionApplication.getAppContext(), e.toString(), 2, Toast.LENGTH_SHORT );
        }// end of catch block
    }// end of method astronomyChange

    /**
     * Performs a local broadcast that the astronomy has changed.
     */
    private void broadcastAstronomyChange( String timeOfDay )
    {
        Intent astronomyChangeIntent = new Intent( ASTRONOMY_MESSAGE );
        astronomyChangeIntent.putExtra( ASTRONOMY_PAYLOAD, timeOfDay );
        LocalBroadcastManager manager =
                LocalBroadcastManager.getInstance( getApplicationContext() );
        manager.sendBroadcast( astronomyChangeIntent );
    }// end of method broadcastAstronomyChange

    /**
     * Performs a local broadcast that an error was thrown when attempting to load the weather data.
     *
     * This method might be called reflectively and may appear to be unused
     */
    private void broadcastLoadingError()
    {
        Intent updateIntent = new Intent( WEATHER_LOADING_ERROR_MESSAGE );
        LocalBroadcastManager manager =
                LocalBroadcastManager.getInstance( getApplicationContext() );
        manager.sendBroadcast( updateIntent );
    }// end of method broadcastLoadingError

    private void broadcastWeatherUpdate()
    {
        // reset the request flags to defaults
        UtilityMethod.refreshRequestedByUser = false;
        UtilityMethod.refreshRequestedBySystem  = false;

        Intent updateIntent = new Intent( WEATHER_UPDATE_SERVICE_MESSAGE );
        LocalBroadcastManager manager =
                LocalBroadcastManager.getInstance( getApplicationContext() );
        manager.sendBroadcast( updateIntent );
    }// end of method broadcastWeatherUpdate

    private void broadcastXmlData( String xmlJson )
    {
        Intent messageIntent = new Intent( WEATHER_XML_SERVICE_MESSAGE );
        messageIntent.putExtra( WEATHER_XML_SERVICE_PAYLOAD, xmlJson );
        LocalBroadcastManager manager =
                LocalBroadcastManager.getInstance( getApplicationContext() );
        manager.sendBroadcast( messageIntent );
    }// end of method broadcastXmlData

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

    private void dataRetrievalError( Exception e )
    {
        // inform all receivers that there was a error obtaining weather details
        broadcastLoadingError();

        // reverse the attempt to use the provider
        loadPreviousWeatherData();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(
                WeatherLionApplication.getAppContext() );
        settings.edit().putString( WeatherLionApplication.WEATHER_SOURCE_PREFERENCE,
                WeatherLionApplication.previousWeatherProvider.toString() ).apply();

        UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE,
        wxDataProvider + " returned: " + e.getMessage(), TAG + "::dataRetrievalError" );
        // Calling from a Non-UI Thread
        Handler handler = new Handler( Looper.getMainLooper() );
        final String message;

        if( strJSON.size() == 0 )
        {
            message = wxDataProvider + " did not return data!";
        }// end of if block
        else
        {
            message = wxDataProvider + " returned " + e.getMessage();
        }// end of else block

        handler.post( new Runnable()
        {
            @Override
            public void run()
            {
                UtilityMethod.butteredToast( getApplicationContext(),
                        message, 2, Toast.LENGTH_LONG );
            }
        });
    }// end of method dataRetrievalError

    /**
     * Indicate that the location was supplied by the GPS radio
     */
    private void geolocationIndication()
    {
        boolean useSystemLocation = spf.getBoolean( WeatherLionApplication.USE_GPS_LOCATION_PREFERENCE,
                Preference.DEFAULT_USE_GPS );

        if( useSystemLocation )
        {
            largeWidgetRemoteViews.setImageViewBitmap( R.id.imvUsingGps, getBitmap( R.drawable.wl_geolocation_on ) );
        }// end of if block
        else
        {
            largeWidgetRemoteViews.setImageViewBitmap( R.id.imvUsingGps, getBitmap( R.drawable.wl_geolocation_off ) );
        }// end of else block
    }// end of method  geolocationIndication()

    /**
     * Update all running widgets for this application
     * @param appWidgetManager  The widget manager
     */
    private void updateAllAppWidgets( AppWidgetManager appWidgetManager )
    {
        if( unitChange )
        {
            // there was a unit change
            updateTemps( false );
        }// end of if block
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

            // schedule the update for astronomy switch
            scheduleAstronomyUpdate();
        }// end of else if block
        else
        {
            // check that the ArrayList is not empty and the the first element is not null
            if( strJSON != null && !strJSON.isEmpty() )
            {
                // we are connected to the Internet if JSON data is returned
                if( WeatherLionApplication.largeWidgetIds.length > 0 )
                {
                    largeWidgetRemoteViews.setViewVisibility( R.id.imvOffline, View.INVISIBLE );
                }// end of if block

                if( WeatherLionApplication.smallWidgetIds.length > 0 )
                {
                    smallWidgetRemoteViews.setViewVisibility( R.id.imvOffline, View.INVISIBLE );
                }// end of if block

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
                            weatherBitHx = new Gson().fromJson( strJSON.get( 1 ), WeatherBitWeatherDataItem.FortyEightHourForecastData.class );
                            weatherBitFx = new Gson().fromJson( strJSON.get( 2 ), WeatherBitWeatherDataItem.SixteenDayForecastData.class );
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
                                            TAG + "::updateAllAppWidgets [line: " +
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
                            YrWeatherDataItem.deserializeYrWeatherXML( strJSON.get( 0 ) );
                            YrWeatherDataItem.deserializeYrHourlyXML( strJSON.get( 1 ) );
                            yr = YrWeatherDataItem.yrWeatherDataItem;
                            loadYrWeather();

                            break;
                        default:
                            break;
                    }// end of switch block

                    if( UtilityMethod.refreshRequestedBySystem )
                    {
                        UtilityMethod.refreshRequestedBySystem = false;
                    }// end of if block
                    else if( UtilityMethod.refreshRequestedByUser )
                    {
                        UtilityMethod.refreshRequestedByUser = false;
                    }// end of if block

                    Calendar updateCalendar = Calendar.getInstance();
                    updateCalendar.set( Calendar.SECOND, 0 ); // perform updates on the minute mark
                    UtilityMethod.lastUpdated = updateCalendar.getTime();

                    WeatherLionApplication.weatherLoadedFromProvider = true;
                    WeatherLionApplication.localWeatherDataAvailable = false;
                }// end of try block
                catch( Exception e )
                {
                    UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                TAG + "::updateAllAppWidgets [line: " +
                            e.getStackTrace()[1].getLineNumber()+ "]" );
                    WeatherLionApplication.dataLoadedSuccessfully = false;

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

                    broadcastLoadingError();

                }// end of catch block

                // if the last update is not present then it can be assumed that data is being restored
                if( WeatherLionApplication.restoringWeatherData ) UtilityMethod.lastUpdated = new Date();

                // Update the current location and update time stamp
                largeWidgetRemoteViews.setTextViewText( R.id.txvCurrentLocation,
                        currentLocation.substring( 0, currentLocation.indexOf( "," ) ) );

                smallWidgetRemoteViews.setTextViewText( R.id.txvCurrentLocation,
                        currentLocation.substring( 0, currentLocation.indexOf( "," ) ) );

                String storedProviderName = WeatherLionApplication.storedPreferences.getProvider()
                        .equalsIgnoreCase( WeatherLionApplication.YAHOO_WEATHER ) ?
                        WeatherLionApplication.storedPreferences.getProvider()
                                .replaceAll( "!", "" ) :
                        WeatherLionApplication.storedPreferences.getProvider();

                String providerIcon = String.format( "%s%s", "wl_",
                        storedProviderName.toLowerCase().replaceAll( " ", "_" ) );

                largeWidgetRemoteViews.setImageViewResource( R.id.imvWeatherProviderLogo,
                        UtilityMethod.getImageResourceId( providerIcon ) );

                smallWidgetRemoteViews.setImageViewResource( R.id.imvWeatherProviderLogo,
                        UtilityMethod.getImageResourceId( providerIcon ) );

                largeWidgetRemoteViews.setTextViewText( R.id.txvProvider,
                        WeatherLionApplication.storedPreferences.getProvider() );

                smallWidgetRemoteViews.setTextViewText( R.id.txvProvider,
                        WeatherLionApplication.storedPreferences.getProvider() );
            }// end of inner if block
            else // no json data was returned so check for Internet connectivity
            {
                // Check the Internet connection availability
                if( !UtilityMethod.hasInternetConnection(this ) )
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
                        largeWidgetRemoteViews.setViewVisibility( R.id.imvOffline, View.VISIBLE );
                        smallWidgetRemoteViews.setViewVisibility( R.id.imvOffline, View.VISIBLE );
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
                                        WeatherLionApplication.storedPreferences.getProvider() +
                                                " did not return data!",2, Toast.LENGTH_LONG );
                            }
                        });
                    }// end of if block
                }// end of else block
            }// end of inner else block
        }// end of else if block

        WidgetHelper.getWidgetIds();

        // Set the current location
        largeWidgetRemoteViews.setTextViewText( R.id.txvCurrentLocation,
                currentLocation.substring( 0, currentLocation.indexOf( "," ) ) );

        smallWidgetRemoteViews.setTextViewText( R.id.txvCurrentLocation,
                currentLocation.substring( 0, currentLocation.indexOf( "," ) ) );

        String widBackgroundColor = spf.getString( WeatherLionApplication.WIDGET_BACKGROUND_PREFERENCE,
                com.bushbungalo.weatherlion.Preference.DEFAULT_WIDGET_BACKGROUND );

        // schedule the weather update only if weather was just updated
        if( weatherUpdate && strJSON != null && !strJSON.isEmpty() )
        {
            if( WeatherLionApplication.largeWidgetIds.length > 0 )
            {
                for ( int largeWidgetId : WeatherLionApplication.largeWidgetIds )
                {
                    UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                        String.format( Locale.ENGLISH,
                            "Updating weather data on widget %d...", largeWidgetId ),
                            TAG + "::updateAllAppWidgets" );

                    if( widBackgroundColor != null )
                    {
                        View widgetView = View.inflate( this,
                                R.layout.wl_large_weather_widget_activity_alternate, null );
                        ViewGroup root = widgetView.findViewById( R.id.flWidgetParent );

                        UtilityMethod.widgetTextViews.clear();
                        UtilityMethod.widgetImageViews.clear();
                        UtilityMethod.getViewIds( root );

                        if( widBackgroundColor.toLowerCase().equals(
                                WeatherLionApplication.FROSTY_THEME ) )
                        {
                            for( int viewId : UtilityMethod.widgetTextViews )
                            {
                                largeWidgetRemoteViews.setTextColor( viewId,
                                        Color.parseColor("#000000" ) );
                            }// end of for each loop

                            for( int viewId : UtilityMethod.widgetImageViews )
                            {
                                largeWidgetRemoteViews.setInt( viewId, "setColorFilter",
                                        Color.parseColor("#000000") );
                            }// end of for each loop

                            largeWidgetRemoteViews.setTextColor( R.id.txvDayLow,
                                    Color.parseColor("#3C3F41" ) );

                            largeWidgetRemoteViews.setTextColor( R.id.tcCurrentTime,
                                    Color.parseColor("#F26E1B" ) );

                            largeWidgetRemoteViews.setTextColor( R.id.tcAMPM,
                                    Color.parseColor("#F26E1B" ) );
                        }// end of if block
                        else
                        {
                            for( int viewId : UtilityMethod.widgetTextViews )
                            {
                                largeWidgetRemoteViews.setTextColor( viewId,
                                        Color.parseColor("#FFFFFF" ) );
                            }// end of for each loop

                            for( int viewId : UtilityMethod.widgetImageViews )
                            {
                                largeWidgetRemoteViews.setInt( viewId, "setColorFilter",
                                        Color.parseColor("#FFFFFF") );
                            }// end of for each loop

                            largeWidgetRemoteViews.setTextColor( R.id.txvDayLow,
                                    Color.parseColor("#C0C0C0" ) );

                            largeWidgetRemoteViews.setTextColor( R.id.tcCurrentTime,
                                    Color.parseColor("#F39530" ) );

                            largeWidgetRemoteViews.setTextColor( R.id.tcAMPM,
                                    Color.parseColor("#F39530" ) );
                        }// end of else block
                    }// end of if block

                    largeWidgetRemoteViews.setString( R.id.tcCurrentTime, "setTimeZone",
                        CityData.currentCityData.getTimeZone() );
                    largeWidgetRemoteViews.setString( R.id.tcAMPM, "setTimeZone",
                        CityData.currentCityData.getTimeZone() );

                    appWidgetManager.updateAppWidget( largeWidgetId,
                            largeWidgetRemoteViews );
                }// end of for each loop
            }// end of if block

            if( WeatherLionApplication.smallWidgetIds.length > 0 )
            {
                // update all the small widgets
                for ( int smallWidgetId : WeatherLionApplication.smallWidgetIds )
                {
                    UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                            String.format( Locale.ENGLISH,
                                    "Updating weather data on widget %d...", smallWidgetId ),
                            TAG + "::updateAllAppWidgets" );

                    if( widBackgroundColor != null )
                    {
                        View widgetView = View.inflate( this,
                                R.layout.wl_small_weather_widget_activity, null );
                        ViewGroup root = widgetView.findViewById( R.id.flWidgetParent );

                        UtilityMethod.widgetTextViews.clear();
                        UtilityMethod.widgetImageViews.clear();
                        UtilityMethod.getViewIds( root );

                        if( widBackgroundColor.toLowerCase().equals(
                                WeatherLionApplication.FROSTY_THEME ) )
                        {
                            for( int viewId : UtilityMethod.widgetTextViews )
                            {
                                smallWidgetRemoteViews.setTextColor( viewId,
                                        Color.parseColor("#000000" ) );
                            }// end of for each loop

                            for( int viewId : UtilityMethod.widgetImageViews )
                            {
                                smallWidgetRemoteViews.setInt( viewId, "setColorFilter",
                                        Color.parseColor("#000000") );
                            }// end of for each loop

                            smallWidgetRemoteViews.setTextColor( R.id.txvDayLow,
                                    Color.parseColor("#3C3F41" ) );
                        }// end of if block
                        else
                        {
                            for( int viewId : UtilityMethod.widgetTextViews )
                            {
                                smallWidgetRemoteViews.setTextColor( viewId,
                                        Color.parseColor("#FFFFFF" ) );
                            }// end of for each loop

                            for( int viewId : UtilityMethod.widgetImageViews )
                            {
                                smallWidgetRemoteViews.setInt( viewId, "setColorFilter",
                                        Color.parseColor("#FFFFFF") );
                            }// end of for each loop

                            smallWidgetRemoteViews.setTextColor( R.id.txvDayLow,
                                    Color.parseColor("#C0C0C0" ) );
                        }// end of else block
                    }// end of if block

                    appWidgetManager.updateAppWidget( smallWidgetId,
                            smallWidgetRemoteViews );
                }// end of for each loop
            }// end of if block

            // schedule the next widget update
            scheduleNextUpdate();

            // schedule the update for astronomy switch
            scheduleAstronomyUpdate();

            // send out a broadcast that the weather has been updated
            broadcastWeatherUpdate();
        }// end of if block
        else if( weatherUpdate && strJSON == null )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO, "Undoing preference change...",
                TAG + "::updateAllAppWidgets" );
            // reverse the preference change
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( WeatherLionApplication.getAppContext() );
            settings.edit().putString( WeatherLionApplication.WEATHER_SOURCE_PREFERENCE,
                    WeatherLionApplication.previousWeatherProvider.toString() ).apply();
        }// end of else if block
        else
        {
            // Set the current location
            largeWidgetRemoteViews.setTextViewText( R.id.txvCurrentLocation,
                    currentLocation.substring( 0, currentLocation.indexOf( "," ) ) );

            smallWidgetRemoteViews.setTextViewText( R.id.txvCurrentLocation,
                    currentLocation.substring( 0, currentLocation.indexOf( "," ) ) );

            if( WeatherLionApplication.largeWidgetIds.length > 0 )
            {
                for ( int largeWidgetId : WeatherLionApplication.largeWidgetIds )
                {
                    if( widBackgroundColor != null )
                    {
                        View widgetView = View.inflate( this,
                                R.layout.wl_large_weather_widget_activity_alternate, null );
                        ViewGroup root = widgetView.findViewById( R.id.flWidgetParent );

                        UtilityMethod.widgetTextViews.clear();
                        UtilityMethod.widgetImageViews.clear();
                        UtilityMethod.getViewIds( root );

                        if( widBackgroundColor.toLowerCase().equals(
                                WeatherLionApplication.FROSTY_THEME ) )
                        {
                            for( int viewId : UtilityMethod.widgetTextViews )
                            {
                                largeWidgetRemoteViews.setTextColor( viewId,
                                        Color.parseColor("#000000" ) );
                            }// end of for each loop

                            for( int viewId : UtilityMethod.widgetImageViews )
                            {
                                largeWidgetRemoteViews.setInt( viewId, "setColorFilter",
                                        Color.parseColor("#000000") );
                            }// end of for each loop

                            largeWidgetRemoteViews.setTextColor( R.id.txvDayLow,
                                    Color.parseColor("#3C3F41" ) );

                            largeWidgetRemoteViews.setTextColor( R.id.tcCurrentTime,
                                    Color.parseColor("#F26E1B" ) );

                            largeWidgetRemoteViews.setTextColor( R.id.tcAMPM,
                                    Color.parseColor("#F26E1B" ) );
                        }// end of if block
                        else
                        {
                            for( int viewId : UtilityMethod.widgetTextViews )
                            {
                                largeWidgetRemoteViews.setTextColor( viewId,
                                        Color.parseColor("#FFFFFF" ) );
                            }// end of for each loop

                            for( int viewId : UtilityMethod.widgetImageViews )
                            {
                                largeWidgetRemoteViews.setInt( viewId, "setColorFilter",
                                        Color.parseColor("#FFFFFF") );
                            }// end of for each loop

                            largeWidgetRemoteViews.setTextColor( R.id.txvDayLow,
                                    Color.parseColor("#C0C0C0" ) );

                            largeWidgetRemoteViews.setTextColor( R.id.tcCurrentTime,
                                    Color.parseColor("#F39530" ) );

                            largeWidgetRemoteViews.setTextColor( R.id.tcAMPM,
                                    Color.parseColor("#F39530" ) );
                        }// end of else block
                    }// end of if block

                    largeWidgetRemoteViews.setString( R.id.tcCurrentTime, "setTimeZone",
                            CityData.currentCityData.getTimeZone() );
                    largeWidgetRemoteViews.setString( R.id.tcAMPM, "setTimeZone",
                            CityData.currentCityData.getTimeZone() );

                    appWidgetManager.updateAppWidget( largeWidgetId,
                            largeWidgetRemoteViews );
                }// end of for each loop
            }// end of if block

            if( WeatherLionApplication.smallWidgetIds.length > 0 )
            {
                // update all the small widgets
                for ( int smallWidgetId : WeatherLionApplication.smallWidgetIds )
                {
                    if( widBackgroundColor != null )
                    {
                        View widgetView = View.inflate( this,
                                R.layout.wl_small_weather_widget_activity, null );
                        ViewGroup root = widgetView.findViewById( R.id.flWidgetParent );

                        UtilityMethod.widgetTextViews.clear();
                        UtilityMethod.widgetImageViews.clear();
                        UtilityMethod.getViewIds( root );

                        if( widBackgroundColor.toLowerCase().equals(
                                WeatherLionApplication.FROSTY_THEME ) )
                        {
                            for( int viewId : UtilityMethod.widgetTextViews )
                            {
                                smallWidgetRemoteViews.setTextColor( viewId,
                                        Color.parseColor("#000000" ) );
                            }// end of for each loop

                            for( int viewId : UtilityMethod.widgetImageViews )
                            {
                                smallWidgetRemoteViews.setInt( viewId, "setColorFilter",
                                        Color.parseColor("#000000") );
                            }// end of for each loop

                            smallWidgetRemoteViews.setTextColor( R.id.txvDayLow,
                                    Color.parseColor("#3C3F41" ) );
                        }// end of if block
                        else
                        {
                            for( int viewId : UtilityMethod.widgetTextViews )
                            {
                                smallWidgetRemoteViews.setTextColor( viewId,
                                        Color.parseColor("#FFFFFF" ) );
                            }// end of for each loop

                            for( int viewId : UtilityMethod.widgetImageViews )
                            {
                                smallWidgetRemoteViews.setInt( viewId, "setColorFilter",
                                        Color.parseColor("#FFFFFF") );
                            }// end of for each loop

                            smallWidgetRemoteViews.setTextColor( R.id.txvDayLow,
                                    Color.parseColor("#C0C0C0" ) );
                        }// end of else block
                    }// end of if block

                    appWidgetManager.updateAppWidget( smallWidgetId,
                            smallWidgetRemoteViews );
                }// end of for each loop
            }// end of if block
        }// end of else block
    }// end of method updateAllAppWidgets

    /**
     * Load the applicable weather icon image
     * @param resID The Id of the resource
     * @param imageFile  The file name for the icon
     */
    private void loadWeatherIcon( RemoteViews widget, int resID, String imageFile )
    {
        try( InputStream is = this.getAssets().open( imageFile ) )
        {
            Bitmap bmp = BitmapFactory.decodeStream( is );
            widget.setImageViewBitmap( resID, bmp );
        }// end of try block
        catch ( IOException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE,"Weather icon " +
                imageFile + " could not be loaded!", TAG + "::loadWeatherIcon [line: " +
                    e.getStackTrace()[1].getLineNumber()+ "]" );

            String defaultIcon =  WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet
                    + "/weather_na.png";

            loadWeatherIcon( widget, resID, defaultIcon );


        }// end of catch block
    }// end of method loadWeatherIcon

    /**
     * Retrieves weather information from a specific weather provider's web service Url.
     *
     * @param wxUrl The providers webservice Url
     */
    private static void retrieveWeatherData( String wxUrl )
    {
        try
        {
            HttpHelper.downloadUrl( wxUrl, true );
        }// end of try block
        catch ( IOException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE,
                e.getMessage(), TAG +
                    "::retrieveWeatherData" );
        }// end of catch block
    }// end of method retrieveWeatherData

    private void scheduleAstronomyUpdate()
    {
        Intent sunsetIntent = new Intent( getApplicationContext(), SunsetAlarmBroadcastReceiver.class );
        sunsetIntent.setAction( SunsetAlarmBroadcastReceiver.ACTION_ALARM );

        PendingIntent sunsetAlarmIntent = PendingIntent.getBroadcast( getApplicationContext(),
                0, sunsetIntent, 0 );

        Intent sunriseIntent = new Intent( getApplicationContext(), SunriseAlarmBroadcastReceiver.class );
        sunriseIntent.setAction( SunriseAlarmBroadcastReceiver.ACTION_ALARM );

        PendingIntent sunriseAlarmIntent = PendingIntent.getBroadcast( getApplicationContext(),
                0, sunsetIntent, 0 );

        AlarmManager sunriseAlarmManager = (AlarmManager) getApplicationContext().
                getSystemService( Context.ALARM_SERVICE );

        AlarmManager sunsetAlarmManager = (AlarmManager) getApplicationContext().
                getSystemService( Context.ALARM_SERVICE );

        String today = new SimpleDateFormat( "MM/dd/yyyy",
            Locale.ENGLISH ).format( new Date() );

        String sst = String.format( "%s %s", today,
                WeatherLionApplication.currentSunsetTime.toString() );

        String srt = String.format( "%s %s", today,
                WeatherLionApplication.currentSunriseTime.toString() );

        // some providers do not include a space between the time and the meridiem
        String pattern = WeatherLionApplication.currentSunsetTime.toString().contains( " " )
                ? "MM/dd/yyyy h:mm a" : "MM/dd/yyyy h:mma";

        SimpleDateFormat sdf = new SimpleDateFormat( pattern, Locale.ENGLISH );

        // Obtain all default value from the stored preferences
        Date rn = new Date();
        Date schedSunriseTime = null;
        Date schedSunsetTime = null;

        try
        {
            schedSunsetTime = sdf.parse( sst );
            schedSunriseTime = sdf.parse( srt );
        } // end of try block
        catch ( ParseException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE , e.getMessage(),
                    TAG + "::scheduleAstronomyUpdate [line: " + e.getStackTrace()[ 1 ].getLineNumber() + "]" );
        }// end of catch block

        if ( ( rn.before( schedSunriseTime ) || rn.equals( schedSunriseTime ) ) )
        {
            Calendar sunriseCalendar = Calendar.getInstance();
            sunriseCalendar.setTime( schedSunriseTime );

            long sunriseUpdate = sunriseCalendar.getTimeInMillis();

            sunriseAlarmManager.setExact( AlarmManager.RTC, sunriseUpdate, sunriseAlarmIntent );

            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                    "Sunrise alarm scheduled for " +
                            new SimpleDateFormat( "h:mm:ss a",
                                    Locale.ENGLISH ).format( sunriseCalendar.getTime() ) + ".",
                    TAG + "::scheduleAstronomyUpdate" );
        }// end iof if block

        if ( ( rn.before( schedSunsetTime ) || rn.equals( schedSunsetTime ) ) )
        {
            Calendar sunsetCalendar = Calendar.getInstance();
            sunsetCalendar.setTime( schedSunsetTime );

            long sunsetUpdate = sunsetCalendar.getTimeInMillis();

            sunsetAlarmManager.setExact( AlarmManager.RTC, sunsetUpdate, sunsetAlarmIntent );

            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
        "Sunset alarm scheduled for " +
                    new SimpleDateFormat( "h:mm:ss a",
                        Locale.ENGLISH ).format( sunsetCalendar.getTime() ) + ".",
                    TAG + "::scheduleAstronomyUpdate" );
        }// end of if block
    }// end of method scheduleAstronomyUpdate

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
        Intent intentToFire = new Intent( getApplicationContext(), UpdateAlarmBroadcastReceiver.class );
        intentToFire.setAction( UpdateAlarmBroadcastReceiver.ACTION_ALARM );

        PendingIntent alarmIntent = PendingIntent.getBroadcast( getApplicationContext(),
                0, intentToFire, 0 );
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().
            getSystemService( Context.ALARM_SERVICE );

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

        // Test Area
//        int seconds = 120;
//        alarmManager.setExact( AlarmManager.RTC,
//                System.currentTimeMillis() + (seconds * 1000), alarmIntent );
//
//        UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO, "Next update scheduled for " +
//                new SimpleDateFormat( "h:mm:ss a", Locale.ENGLISH ).format(
//                        System.currentTimeMillis() + (seconds * 1000) ) + ".",
//                TAG + "::scheduleNextUpdate" );
    }// end of method scheduleNextUpdate

    private void loadDarkSkyWeather()
    {
        currentCountry.setLength( 0 );
        currentCountry.append( CityData.currentCityData.getCountryName() );

        currentCondition.setLength( 0 ); // reset
        currentCondition.append( UtilityMethod.validateCondition(
                UtilityMethod.toProperCase( darkSky.getCurrently().getSummary() ) ) );

        currentWindDirection.setLength( 0 );
        currentWindDirection.append( UtilityMethod.compassDirection( darkSky.getCurrently().getWindBearing() ) );

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

        WeatherLionApplication.currentSunriseTime = sunriseTime;
        WeatherLionApplication.currentSunsetTime = sunsetTime;

        updateTemps( true ); // call update temps here
        formatWeatherCondition();

        largeWidgetRemoteViews.setTextViewText( R.id.txvWeatherCondition,
                UtilityMethod.toProperCase( currentCondition.toString() ) );

        largeWidgetRemoteViews.setTextViewText( R.id.txvWindReading,
            String.format( Locale.ENGLISH, "%s %s %d %s", getString( R.string.wind ),
                currentWindDirection.toString(),
                    Math.round( Float.parseFloat( currentWindSpeed.toString() ) ),
                        ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                            "km/h" : "mph" ) ) );

        largeWidgetRemoteViews.setTextViewText( R.id.txvHumidity,
            String.format( Locale.ENGLISH, "%s %s%%", getString( R.string.humidity ),
                currentHumidity.toString() ) );

        smallWidgetRemoteViews.setTextViewText( R.id.txvWeatherCondition,
                UtilityMethod.toProperCase( currentCondition.toString() ) );

        // Update the current location and update time stamp
        String ts = new SimpleDateFormat( "E, MMM dd, h:mm a", Locale.ENGLISH ).format( new Date() );

        largeWidgetRemoteViews.setTextViewText( R.id.txvLastUpdated, ts );

        largeWidgetRemoteViews.setViewVisibility( R.id.imvRefresh, View.VISIBLE );
        largeWidgetRemoteViews.setViewVisibility( R.id.view_flipper, View.INVISIBLE );

        largeWidgetRemoteViews.setTextViewText( R.id.txvSunrise, sunriseTime.toString() );
        largeWidgetRemoteViews.setTextViewText( R.id.txvSunset, sunsetTime.toString() );

        smallWidgetRemoteViews.setTextViewText( R.id.txvLastUpdated, ts );

        smallWidgetRemoteViews.setViewVisibility( R.id.imvRefresh, View.VISIBLE );
        smallWidgetRemoteViews.setViewVisibility( R.id.view_flipper, View.INVISIBLE );

        WeatherLionApplication.currentSunriseTime = sunriseTime;
        WeatherLionApplication.currentSunsetTime = sunsetTime;

        // Load current condition weather image
        String currentConditionIcon = UtilityMethod.getConditionIcon( currentCondition, null );

        loadWeatherIcon( largeWidgetRemoteViews, R.id.imvCurrentCondition,
                 WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        loadWeatherIcon( smallWidgetRemoteViews, R.id.imvCurrentCondition,
                 WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        // Five Hour Forecast
        int x = 1;

        if( ZoneId.systemDefault().getId().equalsIgnoreCase(
                WeatherLionApplication.currentLocationTimeZone.getTimezoneId() ) )
        {
            checkTime = LocalDateTime.now();
        }// end of if block
        else
        {
            checkTime = WeatherLionApplication.localDateTime;
        }// end of else block

        LocalDateTime currentForecastHour;
        DateTimeFormatter hourlyFormat = DateTimeFormatter.ofPattern( "h:mm a" );

        // clear any previous data stored
        currentFiveHourForecast.clear();

        // get and store hourly forecast
        for ( DarkSkyWeatherDataItem.Hourly.Data wxHourlyForecast : darkSky.getHourly().getData() )
        {
            currentForecastHour = UtilityMethod.getDateTime(
                Integer.parseInt( wxHourlyForecast.getTime() ) ).toInstant().atZone(
                    ZoneId.systemDefault() ).toLocalDateTime();

            String forecastTime = currentForecastHour.format( hourlyFormat );

            if ( currentForecastHour.getMonth() == checkTime.getMonth() &&
                    currentForecastHour.getDayOfMonth() == checkTime.getDayOfMonth() &&
                    currentForecastHour.getYear() == checkTime.getYear() )
            {
                if( currentForecastHour.getHour() <= checkTime.getHour() )
                {
                    continue;
                }// end of if block
                else if( currentForecastHour.getHour() > checkTime.getHour() )
                {
                    currentFiveHourForecast.add(
                        new FiveHourForecast( currentForecastHour, String.valueOf(
                            Math.round( hourlyReading.get( forecastTime ) ) ),
                                UtilityMethod.toProperCase(
                                    UtilityMethod.validateCondition(
                                        wxHourlyForecast.getSummary().toLowerCase() ) ) ) );
                    x++;
                }// end of else if block
            }// end of if block
            else if ( currentForecastHour.isAfter( checkTime ) )
            {
                currentFiveHourForecast.add(
                    new FiveHourForecast( currentForecastHour, String.valueOf(
                        Math.round( hourlyReading.get( forecastTime ) ) ),
                            UtilityMethod.toProperCase(
                                UtilityMethod.validateCondition(
                                    wxHourlyForecast.getSummary().toLowerCase() ) ) ) );
                x++;
            }// end of if block

            if( x == 6 )
            {
                break;
            }// end of if block
        }// end of first for each loop

        // Five Day Forecast
        int i = 1;
        currentFiveDayForecast.clear(); // ensure that this list is clean

        for( DarkSkyWeatherDataItem.Daily.Data wxForecast : darkSky.getDaily().getData() )
        {
            Date fxDate = UtilityMethod.getDateTime( wxForecast.getTime() );
            String fCondition = UtilityMethod.validateCondition( wxForecast.getSummary() );

            int fDay = this.getResources().getIdentifier( "txvDay" + (i),
                    "id", this.getPackageName() );
            int fIcon = this.getResources().getIdentifier( "imvDay" + (i) + "Icon",
                    "id", this.getPackageName() );

            largeWidgetRemoteViews.setTextViewText( fDay, new SimpleDateFormat(
            "E d", Locale.ENGLISH ).format( fxDate ) );

            String fConditionIcon = UtilityMethod.getForecastConditionIcon(
                    fCondition );

            loadWeatherIcon( largeWidgetRemoteViews, fIcon,
             WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + fConditionIcon );

            currentFiveDayForecast.add(
                new FiveDayForecast( fxDate, String.valueOf( Math.round( hl[ i - 1 ][ 0 ] ) ),
                    String.valueOf( Math.round( hl[ i - 1 ][ 1 ] ) ), fCondition,
                    wxForecast.getDewPoint(), wxForecast.getHumidity(),
                    UtilityMethod.hpaToInHg( wxForecast.getPressure() ),
                    wxForecast.getWindBearing(), wxForecast.getWindSpeed(),
                    UtilityMethod.compassDirection( wxForecast.getWindBearing() ),
                    wxForecast.getUvIndex(),  wxForecast.getVisibility(),
                    wxForecast.getOzone(),
                    UtilityMethod.getDateTime( wxForecast.getSunriseTime() ),
                    UtilityMethod.getDateTime( wxForecast.getSunsetTime() ) ) );

            if ( i == 5 )
            {
                break;
            }// end of if block

            i++; // increment sentinel

        }// end of for each loop

        // if the code gets to here then all was loaded successfully
        WeatherLionApplication.dataLoadedSuccessfully = true;

        Map< String, Object > xmlMapData = new LinkedHashMap<>();
        xmlMapData.put( "providerName", WeatherLionApplication.DARK_SKY );
        xmlMapData.put( "datePublished", new Date() );
        xmlMapData.put( "cityName",currentLocation );
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
        xmlMapData.put( "fiveHourForecast", currentFiveHourForecast );
        xmlMapData.put( "fiveDayForecast", currentFiveDayForecast );

        String xmlJSON = new Gson().toJson( xmlMapData );

        broadcastXmlData( xmlJSON );
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
                UtilityMethod.toProperCase( UtilityMethod.validateCondition( obs.getIconName().replaceAll( "_", " " ) ) ) :
                UtilityMethod.toProperCase( UtilityMethod.validateCondition( obs.getIconName().replaceAll( "_", " " ) ) ) );

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

        largeWidgetRemoteViews.setTextViewText( R.id.txvWeatherCondition,
                UtilityMethod.toProperCase( currentCondition.toString() ) );

        largeWidgetRemoteViews.setTextViewText( R.id.txvWindReading,
            String.format( Locale.ENGLISH, "%s %s %d %s", getString( R.string.wind ),
                currentWindDirection.toString(),
                    Math.round( Float.parseFloat( currentWindSpeed.toString() ) ),
                        ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                            "km/h" : "mph" ) ) );

        largeWidgetRemoteViews.setTextViewText( R.id.txvHumidity,
            String.format( Locale.ENGLISH, "%s %s%%", getString( R.string.humidity ),
                currentHumidity.toString() ) );

        smallWidgetRemoteViews.setTextViewText( R.id.txvWeatherCondition,
                UtilityMethod.toProperCase( currentCondition.toString() ) );

        // Update the current location and update time stamp
        String ts = new SimpleDateFormat( "E, MMM dd, h:mm a", Locale.ENGLISH ).format( new Date() );

        largeWidgetRemoteViews.setTextViewText( R.id.txvLastUpdated, ts );

        largeWidgetRemoteViews.setViewVisibility( R.id.imvRefresh, View.VISIBLE );
        largeWidgetRemoteViews.setViewVisibility( R.id.view_flipper, View.INVISIBLE );

        largeWidgetRemoteViews.setTextViewText( R.id.txvSunrise, sunriseTime.toString() );
        largeWidgetRemoteViews.setTextViewText( R.id.txvSunset, sunsetTime.toString() );

        smallWidgetRemoteViews.setTextViewText( R.id.txvLastUpdated, ts );

        smallWidgetRemoteViews.setViewVisibility( R.id.imvRefresh, View.VISIBLE );
        smallWidgetRemoteViews.setViewVisibility( R.id.view_flipper, View.INVISIBLE );

        WeatherLionApplication.currentSunriseTime = sunriseTime;
        WeatherLionApplication.currentSunsetTime = sunsetTime;

        // Load current condition weather image
        String currentConditionIcon = UtilityMethod.getConditionIcon( currentCondition, null );

        loadWeatherIcon( largeWidgetRemoteViews, R.id.imvCurrentCondition,
                 WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        loadWeatherIcon( smallWidgetRemoteViews, R.id.imvCurrentCondition,
                 WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

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

                String fCondition = wxForecast.getIconName().contains( "_" ) ?
                        UtilityMethod.toProperCase( wxForecast.getIconName().replaceAll( "_", " " ) ) :
                        UtilityMethod.toProperCase( wxForecast.getIconName().replaceAll( "_", " " ) );
                fCondition = UtilityMethod.toProperCase(
                        UtilityMethod.validateCondition( fCondition ) );

                int  fDayView = this.getResources().getIdentifier( "txvDay" + (i),
                        "id", this.getPackageName() );
                int  fIcon = this.getResources().getIdentifier( "imvDay" + (i) + "Icon",
                        "id", this.getPackageName() );

                largeWidgetRemoteViews.setTextViewText( fDayView, new SimpleDateFormat(
                "E d", Locale.ENGLISH ).format( fxDate ) );

                String fConditionIcon = UtilityMethod.getForecastConditionIcon(
                        fCondition );

                loadWeatherIcon( largeWidgetRemoteViews, fIcon,
                         WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + fConditionIcon );

                currentFiveDayForecast.add(
                        new FiveDayForecast( fxDate, String.valueOf( Math.round( hl[ i - 1 ][ 0 ] ) ),
                                String.valueOf( Math.round( hl[ i - 1 ][ 1 ] ) ), fCondition,
                                Float.parseFloat( wxForecast.getDewPoint() ),
                                Float.parseFloat( wxForecast.getHumidity() ),
                                0, 0,
                                Float.parseFloat( wxForecast.getWindSpeed() ),
                                wxForecast.getWindDirection(),
                                Float.parseFloat( wxForecast.getUvIndex() ),
                                0f, 0f, null, null ) );

                if ( i == 5 )
                {
                    break;
                }// end of if block

                i++;
            }// end of if block
        }// end of for each loop

        // if the code gets to here then all was loaded successfully
        WeatherLionApplication.dataLoadedSuccessfully = true;

        Map< String, Object > xmlMapData = new LinkedHashMap<>();
        xmlMapData.put( "providerName", WeatherLionApplication.HERE_MAPS );
        xmlMapData.put( "datePublished", new Date() );
        xmlMapData.put( "cityName", currentLocation );
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

        broadcastXmlData( xmlJSON );
    }// end of method loadHereMapsWeather

    private void loadOpenWeather()
    {
        currentCountry.setLength( 0 );
        currentCountry.append( CityData.currentCityData.getCountryName() );

        currentCondition.setLength( 0 ); // reset
        currentCondition.append( UtilityMethod.validateCondition(
                openWeatherWx.getWeather().get( 0 ).getDescription() ) );

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

        largeWidgetRemoteViews.setTextViewText( R.id.txvWeatherCondition,
                UtilityMethod.toProperCase( currentCondition.toString() ) );

        largeWidgetRemoteViews.setTextViewText( R.id.txvWindReading,
            String.format( Locale.ENGLISH, "%s %s %d %s", getString( R.string.wind ),
                currentWindDirection.toString(),
                    Math.round( Float.parseFloat( currentWindSpeed.toString() ) ),
                        ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                            "km/h" : "mph" ) ) );

        largeWidgetRemoteViews.setTextViewText( R.id.txvHumidity,
            String.format( Locale.ENGLISH, "%s %s%%", getString( R.string.humidity ),
                currentHumidity.toString() ) );

        smallWidgetRemoteViews.setTextViewText( R.id.txvWeatherCondition,
                UtilityMethod.toProperCase( currentCondition.toString() ) );

        // Update the current location and update time stamp
        String ts = new SimpleDateFormat( "E, MMM dd, h:mm a", Locale.ENGLISH ).format( new Date() );

        largeWidgetRemoteViews.setTextViewText( R.id.txvLastUpdated, ts );

        largeWidgetRemoteViews.setViewVisibility( R.id.imvRefresh, View.VISIBLE );
        largeWidgetRemoteViews.setViewVisibility( R.id.view_flipper, View.INVISIBLE );

        largeWidgetRemoteViews.setTextViewText( R.id.txvSunrise, sunriseTime.toString() );
        largeWidgetRemoteViews.setTextViewText( R.id.txvSunset, sunsetTime.toString() );

        smallWidgetRemoteViews.setTextViewText( R.id.txvLastUpdated, ts );

        smallWidgetRemoteViews.setViewVisibility( R.id.imvRefresh, View.VISIBLE );
        smallWidgetRemoteViews.setViewVisibility( R.id.view_flipper, View.INVISIBLE );

        WeatherLionApplication.currentSunriseTime = sunriseTime;
        WeatherLionApplication.currentSunsetTime = sunsetTime;

        // Load current condition weather image
        String currentConditionIcon = UtilityMethod.getConditionIcon( currentCondition, null );

        loadWeatherIcon( largeWidgetRemoteViews, R.id.imvCurrentCondition,
                 WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        loadWeatherIcon( smallWidgetRemoteViews, R.id.imvCurrentCondition,
                 WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

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
                String fCondition =  UtilityMethod.toProperCase(
                        UtilityMethod.validateCondition( wxForecast.getWeather().get( 0 ).getDescription() ) );
                String fDay =  new SimpleDateFormat( "E d", Locale.ENGLISH ).format( fxDate );

                int  fDayView= this.getResources().getIdentifier( "txvDay" + (i),
                        "id", this.getPackageName() );
                int  fIcon= this.getResources().getIdentifier( "imvDay" + (i) + "Icon",
                        "id", this.getPackageName() );

                largeWidgetRemoteViews.setTextViewText( fDayView,  fDay );

                // Load current forecast condition weather image
                String fConditionIcon = UtilityMethod.getForecastConditionIcon(
                        fCondition );

                loadWeatherIcon( largeWidgetRemoteViews, fIcon,
             WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + fConditionIcon );

                currentFiveDayForecast.add(
                        new FiveDayForecast( fxDate, String.valueOf( Math.round( hl[ i - 1 ][ 0 ] ) ),
                                String.valueOf( Math.round( hl[ i - 1 ][ 1 ] ) ), fCondition,
                                0f, wxForecast.getHumidity(),
                                wxForecast.getPressure(), wxForecast.getDeg(),
                                wxForecast.getSpeed(),
                                UtilityMethod.compassDirection( wxForecast.getDeg() ),
                                0f,0f, 0f,
                                UtilityMethod.getDateTime( wxForecast.getSunrise() ),
                                UtilityMethod.getDateTime( wxForecast.getSunset() ) ) );
                if ( i == 5 )
                {
                    break;
                }// end of if block

                i++;
            }// end of if block
        }// end of for each loop

        // if the code gets to here then all was loaded successfully
        WeatherLionApplication.dataLoadedSuccessfully = true;

        Map< String, Object > xmlMapData = new LinkedHashMap<>();
        xmlMapData.put( "providerName", WeatherLionApplication.OPEN_WEATHER );
        xmlMapData.put( "datePublished", new Date() );
        xmlMapData.put( "cityName", currentLocation );
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

        broadcastXmlData( xmlJSON );
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

            sunriseTime.setLength( 0 );
            sunriseTime.append( WeatherLionApplication.storedData.getAstronomy().getSunrise().toUpperCase() );

            sunsetTime.setLength( 0 );
            sunsetTime.append(WeatherLionApplication.storedData.getAstronomy().getSunset().toUpperCase() );

        }// end of if block

        updateTemps( false ); // call update temps here
        formatWeatherCondition();

        // Some providers like Yahoo! loves to omit a zero on the hour mark example: 7:0 am
        if( sunriseTime.length() == 6 && sunriseTime.toString().contains( " " ) )
        {
            String[] ft = sunriseTime.toString().split( ":" );
            sunriseTime.setLength( 0 );
            sunriseTime.append( String.format( "%s%s%s", ft[ 0 ], ":0", ft[ 1 ] ) );
        }// end of if block
        else if( sunsetTime.length() == 6 && sunsetTime.toString().contains( " " ) )
        {
            String[] ft= sunsetTime.toString().split( ":" );
            sunsetTime.setLength( 0 );
            sunsetTime.append( String.format( "%s%s%s", ft[ 0 ], ":0", ft[ 1 ] ) );
        }// end if else if block

        largeWidgetRemoteViews.setTextViewText( R.id.txvWeatherCondition,
                UtilityMethod.toProperCase( currentCondition.toString() ) );

        largeWidgetRemoteViews.setTextViewText( R.id.txvWindReading,
            String.format( Locale.ENGLISH, "%s %s %d %s", getString( R.string.wind ),
                currentWindDirection.toString(),
                    Math.round( Float.parseFloat( currentWindSpeed.toString() ) ),
                        ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                            "km/h" : "mph" ) ) );

        smallWidgetRemoteViews.setTextViewText( R.id.txvWeatherCondition,
                UtilityMethod.toProperCase( currentCondition.toString() ) );

        // Yr's Weather Service does not track humidity
        if( currentHumidity.toString().length() == 0 ) currentHumidity.append( "0" );

        currentHumidity = currentHumidity.toString().contains( "%" )
                ? new StringBuilder( currentHumidity.toString().replaceAll( "%", "" ) )
                : currentHumidity; // remove before parsing

        largeWidgetRemoteViews.setTextViewText( R.id.txvHumidity,
            String.format( Locale.ENGLISH, "%s %s%%", getString( R.string.humidity ),
                Math.round( Float.parseFloat( currentHumidity.toString() ) ) ) );

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

        largeWidgetRemoteViews.setTextViewText( R.id.txvLastUpdated, ts );

        largeWidgetRemoteViews.setViewVisibility( R.id.imvRefresh, View.VISIBLE );
        largeWidgetRemoteViews.setViewVisibility( R.id.view_flipper, View.INVISIBLE );

        largeWidgetRemoteViews.setTextViewText( R.id.txvSunrise, sunriseTime.toString() );
        largeWidgetRemoteViews.setTextViewText( R.id.txvSunset, sunsetTime.toString() );

        smallWidgetRemoteViews.setTextViewText( R.id.txvLastUpdated, ts );

        smallWidgetRemoteViews.setViewVisibility( R.id.imvRefresh, View.VISIBLE );
        smallWidgetRemoteViews.setViewVisibility( R.id.view_flipper, View.INVISIBLE );

        WeatherLionApplication.currentSunriseTime = sunriseTime;
        WeatherLionApplication.currentSunsetTime = sunsetTime;

        // Load current condition weather image
        String currentConditionIcon = UtilityMethod.getConditionIcon( currentCondition, null );

        loadWeatherIcon( largeWidgetRemoteViews, R.id.imvCurrentCondition,
         WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        loadWeatherIcon( smallWidgetRemoteViews, R.id.imvCurrentCondition,
                 WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        for ( int i = 0; i < WeatherLionApplication.storedData.getDailyForecast().size(); i++ )
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
                        TAG + "::loadPreviousWeather"  );
            }// end of catch block

            int  fDay = this.getResources().getIdentifier( "txvDay" + (i + 1),
                    "id", this.getPackageName() );
            int  fIcon = this.getResources().getIdentifier( "imvDay" + (i + 1) + "Icon",
                    "id", this.getPackageName() );

            largeWidgetRemoteViews.setTextViewText( fDay, new SimpleDateFormat( "E d", Locale.ENGLISH ).format( forecastDate ) );

            // Load current forecast condition weather image
            String fCondition = UtilityMethod.validateCondition( wxDayForecast.getCondition() );

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
            String wxIcon =  WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + fConditionIcon;

            loadWeatherIcon( largeWidgetRemoteViews, fIcon,
         WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + fConditionIcon );

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

        String storedProviderName = WeatherLionApplication.storedPreferences.getProvider()
                .equalsIgnoreCase( WeatherLionApplication.YAHOO_WEATHER ) ?
                WeatherLionApplication.storedPreferences.getProvider()
                        .replaceAll( "!", "" ) :
                WeatherLionApplication.storedPreferences.getProvider();

        String providerIcon = String.format( "%s%s", "wl_",
                storedProviderName.toLowerCase().replaceAll( " ", "_" ) );

        largeWidgetRemoteViews.setImageViewResource( R.id.imvWeatherProviderLogo,
                UtilityMethod.getImageResourceId( providerIcon ) );

        smallWidgetRemoteViews.setImageViewResource( R.id.imvWeatherProviderLogo,
                UtilityMethod.getImageResourceId( providerIcon ) );

        largeWidgetRemoteViews.setTextViewText( R.id.txvProvider,
                WeatherLionApplication.storedPreferences.getProvider() );

        smallWidgetRemoteViews.setTextViewText( R.id.txvProvider,
                WeatherLionApplication.storedPreferences.getProvider() );

        if( UtilityMethod.refreshRequestedBySystem )
        {
            UtilityMethod.refreshRequestedBySystem = false;
        }// end of if block
        else if( UtilityMethod.refreshRequestedByUser )
        {
            UtilityMethod.refreshRequestedByUser = false;
        }// end of if block

        WeatherLionApplication.localWeatherDataAvailable = true; // indicate that old weather data is being used
    }// end of method loadPreviousWeatherData

    private void loadWeatherBitWeather()
    {
        currentCountry.setLength( 0 );
        currentCountry.append( CityData.currentCityData.getCountryName() );

        currentCondition.setLength( 0 ); // reset
        currentCondition.append( UtilityMethod.toProperCase(
            UtilityMethod.validateCondition(
                weatherBitWx.getData().get( 0 ).getWeather().getDescription() ) ) );

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

        // call update temps here
        updateTemps( true );
        formatWeatherCondition();

        largeWidgetRemoteViews.setTextViewText( R.id.txvWeatherCondition,
                UtilityMethod.toProperCase( currentCondition.toString() ) );

        largeWidgetRemoteViews.setTextViewText( R.id.txvWindReading,
            String.format( Locale.ENGLISH, "%s %s %d %s", getString( R.string.wind ),
                currentWindDirection.toString(),
                    Math.round( Float.parseFloat( currentWindSpeed.toString() ) ),
                        ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                            "km/h" : "mph" ) ) );

        smallWidgetRemoteViews.setTextViewText( R.id.txvWeatherCondition,
                UtilityMethod.toProperCase( currentCondition.toString() ) );

        // Update the current location and update time stamp
        String ts = new SimpleDateFormat( "E, MMM dd, h:mm a", Locale.ENGLISH ).format( new Date() );

        largeWidgetRemoteViews.setTextViewText( R.id.txvLastUpdated, ts );

        largeWidgetRemoteViews.setViewVisibility( R.id.imvRefresh, View.VISIBLE );
        largeWidgetRemoteViews.setViewVisibility( R.id.view_flipper, View.INVISIBLE );

        largeWidgetRemoteViews.setTextViewText( R.id.txvHumidity,
            String.format( Locale.ENGLISH, "%s %s%%", getString( R.string.humidity ),
                currentHumidity.toString() ) );
        largeWidgetRemoteViews.setTextViewText( R.id.txvSunrise, sunriseTime.toString() );
        largeWidgetRemoteViews.setTextViewText( R.id.txvSunset, sunsetTime.toString() );

        smallWidgetRemoteViews.setTextViewText( R.id.txvLastUpdated, ts );

        smallWidgetRemoteViews.setViewVisibility( R.id.imvRefresh, View.VISIBLE );
        smallWidgetRemoteViews.setViewVisibility( R.id.view_flipper, View.INVISIBLE );

        WeatherLionApplication.currentSunriseTime = sunriseTime;
        WeatherLionApplication.currentSunsetTime = sunsetTime;

        // Load current condition weather image
        String currentConditionIcon = UtilityMethod.getConditionIcon( currentCondition, null );

        loadWeatherIcon( largeWidgetRemoteViews, R.id.imvCurrentCondition,
         WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        loadWeatherIcon( smallWidgetRemoteViews, R.id.imvCurrentCondition,
                 WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        // Five Hour Forecast
        float fTemp;    // forecasted hour temperature

        if( ZoneId.systemDefault().getId().equalsIgnoreCase(
                WeatherLionApplication.currentLocationTimeZone.getTimezoneId() ) )
        {
            checkTime = LocalDateTime.now();
        }// end of if block
        else
        {
            checkTime = WeatherLionApplication.localDateTime;
        }// end of else block

        LocalDateTime currentForecastHour;
        DateTimeFormatter hourFormat = DateTimeFormatter.ofPattern( "h:mm a" );
        DateTimeFormatter localDateFormat = DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm:ss" );
        List< WeatherBitWeatherDataItem.FortyEightHourForecastData.Data > wFhf = weatherBitHx.getData();
        int x = 1;

        // clear any previous data stores
        currentFiveHourForecast.clear();

        // Five Hour Forecast
        for ( WeatherBitWeatherDataItem.FortyEightHourForecastData.Data wxHourlyForecast : wFhf )
        {
            currentForecastHour = LocalDateTime.parse(
                    wxHourlyForecast.getTimestampLocal(), localDateFormat );
            String forecastTime = currentForecastHour.format( hourFormat );


            if ( currentForecastHour.getMonth() == checkTime.getMonth() &&
                    currentForecastHour.getDayOfMonth() == checkTime.getDayOfMonth() &&
                    currentForecastHour.getYear() == checkTime.getYear() )
            {
                if( currentForecastHour.getHour() <= checkTime.getHour() )
                {
                    continue;
                }// end of if block
                else if( currentForecastHour.getHour() > checkTime.getHour() )
                {
                    currentFiveHourForecast.add(
                        new FiveHourForecast( currentForecastHour, String.valueOf(
                            Math.round( hourlyReading.get( forecastTime ) ) ),
                                wxHourlyForecast.getWeather().getDescription() ) );
                    x++;
                }// end of else if block
            }// end of if block
            else if ( currentForecastHour.isAfter( checkTime ) )
            {
                currentFiveHourForecast.add(
                        new FiveHourForecast( currentForecastHour, String.valueOf(
                                Math.round( hourlyReading.get( forecastTime ) ) ),
                                wxHourlyForecast.getWeather().getDescription() ) );
                x++;
            }// end of if block

            if( x == 6 )
            {
                break;
            }// end of if block

        }// end of for each loop

        // Five Day Forecast
        int i = 1;
        currentFiveDayForecast.clear(); // ensure that this list is clean
        List< WeatherBitWeatherDataItem.SixteenDayForecastData.Data > fdf = weatherBitFx.getData();

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
                String fCondition = UtilityMethod.validateCondition(
                        wxForecast.getWeather().getDescription() );

                if ( fxDate.after( new Date() ) )
                {
                    int  fDay = this.getResources().getIdentifier( "txvDay" + (i),
                            "id", this.getPackageName() );
                    int  fIcon = this.getResources().getIdentifier("imvDay" + (i) + "Icon",
                            "id", this.getPackageName() );

                    largeWidgetRemoteViews.setTextViewText( fDay, new SimpleDateFormat(
                        "E d", Locale.ENGLISH ).format( fxDate ) );

                    // Load current forecast condition weather image
                    String fConditionIcon = UtilityMethod.getForecastConditionIcon(
                            fCondition );

                    loadWeatherIcon( largeWidgetRemoteViews, fIcon,
                 WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + fConditionIcon );

                    currentFiveDayForecast.add(
                        new FiveDayForecast( fxDate, String.valueOf( Math.round( hl[ i - 1 ][ 0 ] ) ),
                            String.valueOf( Math.round( hl[ i - 1 ][ 1 ] ) ), fCondition,
                            (float) wxForecast.getDewpt(), 0f,
                            (float) wxForecast.getPres(), 0f,
                            (float) wxForecast.getWindSpd(),
                            UtilityMethod.compassDirection( (float) wxForecast.getWindDir() ),
                            0f,0f, (float) wxForecast.getOzone(),
                            UtilityMethod.getDateTime( wxForecast.getSunriseTs() ),
                            UtilityMethod.getDateTime( wxForecast.getSunsetTs() ) ) );

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
        WeatherLionApplication.dataLoadedSuccessfully = true;

        Map< String, Object > xmlMapData = new LinkedHashMap<>();
        xmlMapData.put( "providerName", WeatherLionApplication.WEATHER_BIT );
        xmlMapData.put( "datePublished", new Date() );
        xmlMapData.put( "cityName", currentLocation );
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
        xmlMapData.put( "fiveHourForecast", currentFiveHourForecast );
        xmlMapData.put( "fiveDayForecast", currentFiveDayForecast );

        String xmlJSON = new Gson().toJson( xmlMapData );

        broadcastXmlData( xmlJSON );
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
            UtilityMethod.validateCondition( yahoo19.getCurrentObservation().getCondition().getText() ) );

        currentHumidity.setLength( 0 );
        currentHumidity.append( Math.round( yahoo19.getCurrentObservation().getAtmosphere().getHumidity() ) );

        sunriseTime.setLength( 0 ); // reset
        sunriseTime.append( yahoo19.getCurrentObservation().getAstronomy().getSunrise().toUpperCase() );

        sunsetTime.setLength( 0 ); // reset
        sunsetTime.append(  yahoo19.getCurrentObservation().getAstronomy().getSunset().toUpperCase() );

        updateTemps( true ); // call update temps here
        formatWeatherCondition();

        largeWidgetRemoteViews.setTextViewText( R.id.txvWeatherCondition, UtilityMethod.toProperCase( currentCondition.toString() ) );

        largeWidgetRemoteViews.setTextViewText( R.id.txvWindReading,
            String.format( Locale.ENGLISH, "%s %s %d %s", getString( R.string.wind ),
                currentWindDirection.toString(),
                    Math.round( Float.parseFloat( currentWindSpeed.toString() ) ),
                        ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                            "km/h" : "mph" ) ) );

        largeWidgetRemoteViews.setTextViewText( R.id.txvHumidity,
            String.format( Locale.ENGLISH, "%s %s%%", getString( R.string.humidity ),
                currentHumidity.toString() ) );

        smallWidgetRemoteViews.setTextViewText( R.id.txvWeatherCondition,
                UtilityMethod.toProperCase( currentCondition.toString() ) );

        // Some providers like Yahoo! loves to omit a zero on the hour mark example: 7:0 am
        if( sunriseTime.length() == 6 && sunriseTime.toString().contains( " " ) )
        {
            String[] ft = sunriseTime.toString().split( ":" );
            sunriseTime.setLength( 0 );
            sunriseTime.append( String.format( "%s%s%s", ft[ 0 ], ":0", ft[ 1 ] ) );
        }// end of if block
        else if( sunsetTime.length() == 6 && sunsetTime.toString().contains( " " ) )
        {
            String[] ft= sunsetTime.toString().split( ":" );
            sunsetTime.setLength( 0 );
            sunsetTime.append( String.format( "%s%s%s", ft[ 0 ], ":0", ft[ 1 ] ) );
        }// end if else if block

        // Update the current location and update time stamp
        String ts = new SimpleDateFormat( "E, MMM dd, h:mm a", Locale.ENGLISH ).format( new Date() );

        largeWidgetRemoteViews.setTextViewText( R.id.txvLastUpdated, ts );

        largeWidgetRemoteViews.setViewVisibility( R.id.imvRefresh, View.VISIBLE );
        largeWidgetRemoteViews.setViewVisibility( R.id.view_flipper, View.INVISIBLE );

        largeWidgetRemoteViews.setTextViewText( R.id.txvSunrise, sunriseTime.toString() );
        largeWidgetRemoteViews.setTextViewText( R.id.txvSunset, sunsetTime.toString() );

        smallWidgetRemoteViews.setTextViewText( R.id.txvLastUpdated, ts );

        smallWidgetRemoteViews.setViewVisibility( R.id.imvRefresh, View.VISIBLE );
        smallWidgetRemoteViews.setViewVisibility( R.id.view_flipper, View.INVISIBLE );

        WeatherLionApplication.currentSunriseTime = sunriseTime;
        WeatherLionApplication.currentSunsetTime = sunsetTime;

        // Load current condition weather image
        String currentConditionIcon = UtilityMethod.getConditionIcon( currentCondition, null );

        loadWeatherIcon( largeWidgetRemoteViews, R.id.imvCurrentCondition,
     WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        loadWeatherIcon( smallWidgetRemoteViews, R.id.imvCurrentCondition,
     WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        List< YahooWeatherYdnDataItem.Forecast > fdf = yahoo19.getForecast();
        currentFiveDayForecast.clear(); // ensure that this list is clean

        for ( int i = 0; i <= fdf.size(); i++ )
        {
            Date fDate = UtilityMethod.getDateTime( fdf.get( i ).getDate() );

            // Load current forecast condition weather image
            String fCondition = UtilityMethod.toProperCase(
                    UtilityMethod.validateCondition( UtilityMethod.yahooWeatherCodes[
                    fdf.get( i ).getCode() ] ) );
            int  fDay = this.getResources().getIdentifier( "txvDay" +  (i + 1),
                    "id", this.getPackageName() );
            int  fIcon = this.getResources().getIdentifier( "imvDay" +  (i + 1) + "Icon",
                    "id", this.getPackageName() );

            largeWidgetRemoteViews.setTextViewText(fDay, new SimpleDateFormat( "E d", Locale.ENGLISH ).format( fDate ));

            String fConditionIcon = UtilityMethod.getForecastConditionIcon(
                    fCondition );

            loadWeatherIcon( largeWidgetRemoteViews, fIcon,
         WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + fConditionIcon );

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
        WeatherLionApplication.dataLoadedSuccessfully = true;

        Map< String, Object > xmlMapData = new LinkedHashMap<>();
        xmlMapData.put( "providerName", WeatherLionApplication.YAHOO_WEATHER );
        xmlMapData.put( "datePublished", new Date() );
        xmlMapData.put( "cityName", currentLocation );
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

        broadcastXmlData( xmlJSON );
    }// end of method loadYahooYdnWeather

    /**
     * Load Yr Weather data
     */
    private void loadYrWeather()
    {
        currentCountry.setLength( 0 );
        currentCountry.append( yr.getCountry() );

        currentCondition.setLength( 0 ); // reset
        currentCondition.append( UtilityMethod.toProperCase(
            UtilityMethod.validateCondition( yr.getForecast().get( 0 ).getSymbolName() ) ) );

        currentHumidity.setLength( 0 );
        currentHumidity.append( currentHumidity.toString().length() == 0 ? currentHumidity : String.valueOf( 0 ) ); // use the humidity reading from previous providers

        // append a zero if there is no humidity
        if( currentHumidity.length() == 0 ) currentHumidity.append( "0" );

        sunriseTime.setLength( 0 );
        sunriseTime.append( new SimpleDateFormat( "h:mm a", Locale.ENGLISH ).format( yr.getSunrise() ) );

        sunsetTime.setLength( 0 );
        sunsetTime.append( new SimpleDateFormat( "h:mm a", Locale.ENGLISH ).format( yr.getSunset() ) );

        // call update temps here
        updateTemps( true );
        formatWeatherCondition();

        largeWidgetRemoteViews.setTextViewText( R.id.txvWeatherCondition,
                UtilityMethod.toProperCase( currentCondition.toString() ) );

        largeWidgetRemoteViews.setTextViewText( R.id.txvWindReading,
            String.format( Locale.ENGLISH, "%s %s %d %s", getString( R.string.wind ),
                currentWindDirection.toString(),
                    Math.round( Float.parseFloat( currentWindSpeed.toString() ) ),
                        ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                            "km/h" : "mph" ) ) );

        largeWidgetRemoteViews.setTextViewText( R.id.txvHumidity,
            String.format( Locale.ENGLISH, "%s %s%%", getString( R.string.humidity ),
                currentHumidity.toString() ) );

        smallWidgetRemoteViews.setTextViewText( R.id.txvWeatherCondition,
                UtilityMethod.toProperCase( currentCondition.toString() ) );

        // Some providers like Yahoo! loves to omit a zero on the hour mark example: 7:0 am
        if( sunriseTime.length() == 6 && sunriseTime.toString().contains( " " ) )
        {
            String[] ft = sunriseTime.toString().split( ":" );
            sunriseTime.setLength( 0 );
            sunriseTime.append( String.format( "%s%s%s", ft[ 0 ], ":0", ft[ 1 ] ) );
        }// end of if block
        else if( sunsetTime.length() == 6 && sunsetTime.toString().contains( " " ) )
        {
            String[] ft= sunsetTime.toString().split( ":" );
            sunsetTime.setLength( 0 );
            sunsetTime.append( String.format( "%s%s%s", ft[ 0 ], ":0", ft[ 1 ] ) );
        }// end if else if block

        // Update the current location and update time stamp
        String ts = new SimpleDateFormat( "E, MMM dd, h:mm a", Locale.ENGLISH ).format( new Date() );

        largeWidgetRemoteViews.setTextViewText( R.id.txvLastUpdated, ts );

        largeWidgetRemoteViews.setViewVisibility( R.id.imvRefresh, View.VISIBLE );
        largeWidgetRemoteViews.setViewVisibility( R.id.view_flipper, View.INVISIBLE );

        largeWidgetRemoteViews.setTextViewText( R.id.txvSunrise, sunriseTime.toString() );
        largeWidgetRemoteViews.setTextViewText( R.id.txvSunset, sunsetTime.toString() );

        smallWidgetRemoteViews.setTextViewText( R.id.txvLastUpdated, ts );

        smallWidgetRemoteViews.setViewVisibility( R.id.imvRefresh, View.VISIBLE );
        smallWidgetRemoteViews.setViewVisibility( R.id.view_flipper, View.INVISIBLE );

        WeatherLionApplication.currentSunriseTime = sunriseTime;
        WeatherLionApplication.currentSunsetTime = sunsetTime;

        // Load current condition weather image
        String currentConditionIcon = UtilityMethod.getConditionIcon( currentCondition, null );

        loadWeatherIcon( largeWidgetRemoteViews, R.id.imvCurrentCondition,
     WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        loadWeatherIcon( smallWidgetRemoteViews, R.id.imvCurrentCondition,
     WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        List< YrWeatherDataItem.HourByHourForecast > fhf = yr.getHourlyForecast();
        SimpleDateFormat df = new SimpleDateFormat( "MMMM dd, yyyy", Locale.ENGLISH );

        int x;

        currentFiveHourForecast.clear(); // ensure that this list is clean

        float fTemp;    // forecasted hour temperature

        if( ZoneId.systemDefault().getId().equalsIgnoreCase(
                WeatherLionApplication.currentLocationTimeZone.getTimezoneId() ) )
        {
            checkTime = LocalDateTime.now();
        }// end of if block
        else
        {
            checkTime = WeatherLionApplication.localDateTime;
        }// end of else block

        LocalDateTime currentForecastHour;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "h:mm a" );

        x = 1;

        // get five hour forecast
        for ( YrWeatherDataItem.HourByHourForecast wxTempReading : fhf )
        {
            currentForecastHour = wxTempReading.getTimeFrom().toInstant().atZone(
                    ZoneId.systemDefault() ).toLocalDateTime();
            String forecastTime = currentForecastHour.format( formatter );

            if ( currentForecastHour.getMonth() == checkTime.getMonth() &&
                    currentForecastHour.getDayOfMonth() == checkTime.getDayOfMonth() &&
                    currentForecastHour.getYear() == checkTime.getYear() )
            {
                if( currentForecastHour.getHour() <= checkTime.getHour() )
                {
                    continue;
                }// end of if block
                else if( currentForecastHour.getHour() > checkTime.getHour() )
                {
                    currentFiveHourForecast.add(
                        new FiveHourForecast( currentForecastHour, String.valueOf(
                            Math.round( hourlyReading.get( forecastTime ) ) ),
                                wxTempReading.getSymbolName() ) );
                    x++;
                }// end of else if block
            }// end of if block
            else if ( currentForecastHour.isAfter( checkTime ) )
            {
                currentFiveHourForecast.add(
                    new FiveHourForecast( currentForecastHour, String.valueOf(
                        Math.round( hourlyReading.get( forecastTime ) ) ),
                            wxTempReading.getSymbolName() ) );

                x++;
            }// end of if block

            if( x == 6 )
            {
                break;
            }// end of if block
        }// end of first for each loop

        List< YrWeatherDataItem.Forecast > fdf = yr.getForecast();
        currentFiveDayForecast.clear(); // ensure that this list is clean

        List< YrWeatherDataItem.Forecast > fiveDays = new ArrayList<>();

        int i = 1;
        x = 0;

        // Five Day Forecast
        for ( Forecast wxDailyForecast : fdf )
        {
            x++;

            // the first time period is one that will be stored
            if ( x == 1 )
            {
                Date forecastDate = wxDailyForecast.getTimeFrom();

                // Load current forecast condition weather image
                String fCondition = UtilityMethod.toProperCase(
                        UtilityMethod.validateCondition( wxDailyForecast.getSymbolName() ) );

                int  fDay = this.getResources().getIdentifier( "txvDay" + (i),
                        "id", this.getPackageName() );
                int  fIcon = this.getResources().getIdentifier( "imvDay" + (i) + "Icon",
                        "id", this.getPackageName() );

                largeWidgetRemoteViews.setTextViewText( fDay, new SimpleDateFormat(
                        "E d", Locale.ENGLISH ).format( forecastDate ) );

                String fConditionIcon = UtilityMethod.getForecastConditionIcon(
                        fCondition );

                loadWeatherIcon( largeWidgetRemoteViews, fIcon,
                         WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + fConditionIcon );

                fiveDays.add( wxDailyForecast );

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

        for( Forecast wxDailyForecast : fiveDays )
        {
            Date forecastDate = wxDailyForecast.getTimeFrom();
            // Load current forecast condition weather image
            String fCondition = UtilityMethod.toProperCase(
                    UtilityMethod.validateCondition( wxDailyForecast.getSymbolName() ) );

            currentFiveDayForecast.add(
                new FiveDayForecast( forecastDate, String.valueOf(
                    Math.round( dailyReading.get( df.format( wxDailyForecast.getTimeFrom() ) ) [ 0 ][ 0 ] ) ),
                        String.valueOf( Math.round( dailyReading.get(
                                df.format( wxDailyForecast.getTimeFrom() ) ) [ 0 ][ 1 ] ) ), fCondition,
                        0f, 0f,
                        UtilityMethod.hpaToInHg( wxDailyForecast.getPressureValue() ), 0f,
                        UtilityMethod.mpsToMph( wxDailyForecast.getWindSpeedMps() ),
                        wxDailyForecast.getWindDirCode(),
                        0f,0f, wxDailyForecast.getPrecipValue(),
                        null,null ) );
        }// end of for loop

        // if the code gets to here then all was loaded successfully
        WeatherLionApplication.dataLoadedSuccessfully = true;

        Map< String, Object > xmlMapData = new LinkedHashMap<>();
        xmlMapData.put( "providerName", WeatherLionApplication.YR_WEATHER );
        xmlMapData.put( "datePublished", new Date() );
        xmlMapData.put( "cityName", currentLocation );
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
        xmlMapData.put( "fiveHourForecast", currentFiveHourForecast );
        xmlMapData.put( "fiveDayForecast", currentFiveDayForecast );

        String xmlJSON = new Gson().toJson( xmlMapData );

        broadcastXmlData( xmlJSON );
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

        tc = UtilityMethod.validateCondition( currentCondition.toString() );

        currentCondition.setLength( 0 ); // reset
        currentCondition.append( tc );
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

    /**
     * Changes the current weather icons in use.
     *
     * This method might be called reflectively and may appear to be unused
     */
    private void loadWeatherIconSet()
    {
        // load the weather data stored locally
        loadLocalWeatherData();

        // Load current condition weather image
        String currentConditionIcon = UtilityMethod.getConditionIcon( currentCondition, null );
        String weatherCondition = WeatherLionApplication.storedData.getCurrent().getCondition();

        loadWeatherIcon( largeWidgetRemoteViews, R.id.imvCurrentCondition,
                 WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        loadWeatherIcon( smallWidgetRemoteViews, R.id.imvCurrentCondition,
                 WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon );

        for ( int i = 0; i < WeatherLionApplication.storedData.getDailyForecast().size(); i++ )
        {
            LastWeatherData.WeatherData.DailyForecast.DayForecast wxDayForecast =
                    WeatherLionApplication.storedData.getDailyForecast().get( i );

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

            loadWeatherIcon( largeWidgetRemoteViews, fIcon,
                     WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet + "/weather_" + fConditionIcon );

            if( i == 4 )
            {
                break;
            }// end of if block
        }// end of for loop
    }// end of method loadWeatherIconSet

    private void loadWidgetBackground()
    {
        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences( this );
        String widBackgroundColor = spf.getString(
            WeatherLionApplication.WIDGET_BACKGROUND_PREFERENCE,
                Preference.DEFAULT_WIDGET_BACKGROUND );

        int largeDrawableId = 0;
        int smallDrawableId = 0;

        if( widBackgroundColor != null )
        {
            switch ( widBackgroundColor.toLowerCase() )
            {
                case WeatherLionApplication.AQUA_THEME:
                    largeDrawableId = R.drawable.wl_aqua_bg_large;
                    smallDrawableId = R.drawable.wl_aqua_bg_small;

                    break;
                case WeatherLionApplication.RABALAC_THEME:
                    largeDrawableId = R.drawable.wl_rabalac_bg_large;
                    smallDrawableId = R.drawable.wl_rabalac_bg_small;

                    break;
                case WeatherLionApplication.FROSTY_THEME:
                    largeDrawableId = R.drawable.wl_frosty_bg_large;
                    smallDrawableId = R.drawable.wl_frosty_bg_small;

                    break;
                case WeatherLionApplication.LION_THEME:
                default:
                    largeDrawableId = R.drawable.wl_lion_bg_large;
                    smallDrawableId = R.drawable.wl_lion_bg_small;

                    break;
            }// end of switch block
        }// end of if block

        largeWidgetRemoteViews.setImageViewBitmap( R.id.imvWidgetBackground, getBitmap( largeDrawableId ) );
        smallWidgetRemoteViews.setImageViewBitmap( R.id.imvWidgetBackground, getBitmap( smallDrawableId ) );
    }// end of method loadWidgetBackground

    /**
     * Show/Hide the internet connectivity icon on the widget
     */
    private void updateUserSetAlarm()
    {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService( Context.ALARM_SERVICE );

        if( alarmManager.getNextAlarmClock() != null )
        {
            long nextAlarmTime = alarmManager.getNextAlarmClock().getTriggerTime();
            Date nextAlarmDate = new Date( nextAlarmTime );
            String alarmTime = new SimpleDateFormat(
                    "E h:mm a", Locale.ENGLISH ).format( nextAlarmDate ).toUpperCase();
            int hoursTilNextAlarm = UtilityMethod.getHoursDifference( new Date(), nextAlarmDate );

            if( hoursTilNextAlarm <= 12 )
            {
                //largeWidgetRemoteViews.setViewVisibility( R.id.relNextAlarm, View.VISIBLE );
                largeWidgetRemoteViews.setTextViewText( R.id.txvAlarmTime, alarmTime );
                String message = String.format( Locale.ENGLISH, "System clock alarm set for %s which is %s from now.",
                    alarmTime.toLowerCase(),
                        UtilityMethod.getDetailedTimeDifference( nextAlarmDate, new Date() ) );
                UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO, message,
                        TAG + "::updateUserSetAlarm" );
            }// end of if block
            else
            {
                //largeWidgetRemoteViews.setViewVisibility( R.id.relNextAlarm, View.INVISIBLE );
                largeWidgetRemoteViews.setTextViewText( R.id.txvAlarmTime,
                        getString( R.string.default_alarm ) );
                UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO, "No Alarm Due",
                        TAG + "::updateUserSetAlarm" );
            }// end of else block
        }// end of if block
        else
        {
            //largeWidgetRemoteViews.setViewVisibility( R.id.relNextAlarm, View.INVISIBLE );
            largeWidgetRemoteViews.setTextViewText( R.id.txvAlarmTime,
                    getString( R.string.default_alarm ) );
            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO, "No Alarm Due",
                    TAG + "::updateUserSetAlarm");
        }// end of else block
    }// end of method updateConnectivity

    /**
     * Show/Hide the internet connectivity icon on the widget
     */
    private void updateConnectivity()
    {
        if( !UtilityMethod.hasInternetConnection( this ) )
        {
            largeWidgetRemoteViews.setViewVisibility( R.id.imvOffline, View.VISIBLE );
            smallWidgetRemoteViews.setViewVisibility( R.id.imvOffline, View.VISIBLE );
        }// end of if block
        else
        {
            largeWidgetRemoteViews.setViewVisibility( R.id.imvOffline, View.INVISIBLE );
            smallWidgetRemoteViews.setViewVisibility( R.id.imvOffline, View.INVISIBLE );
        }// end of else block
    }// end of method updateConnectivity

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

                    // Display weather data on the large widget
                    largeWidgetRemoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + tempUnits );
                    largeWidgetRemoteViews.setTextViewText( R.id.txvFeelsLike, String.format(
                            "%s %s%s", FEELS_LIKE, currentFeelsLikeTemp, DEGREES ) );
                    largeWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                    largeWidgetRemoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );

                    largeWidgetRemoteViews.setTextViewText( R.id.txvWindReading,
                        String.format( Locale.ENGLISH, "%s %s %d %s", getString( R.string.wind ),
                            currentWindDirection.toString(),
                                Math.round( Float.parseFloat( currentWindSpeed.toString() ) ),
                                    ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                                        "km/h" : "mph" ) ) );

                    largeWidgetRemoteViews.setTextViewText( R.id.txvHumidity,
                        String.format( Locale.ENGLISH, "%s %s%%", getString( R.string.humidity ),
                            currentHumidity.toString() ) );

                    // Display weather data on the small widget
                    smallWidgetRemoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + DEGREES );
                    smallWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                    smallWidgetRemoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );

                    // Five Hour Forecast
                    int x = 1;

                    if( ZoneId.systemDefault().getId().equalsIgnoreCase(
                            WeatherLionApplication.currentLocationTimeZone.getTimezoneId() ) )
                    {
                        checkTime = LocalDateTime.now();
                    }// end of if block
                    else
                    {
                        checkTime = WeatherLionApplication.localDateTime;
                    }// end of else block

                    LocalDateTime currentForecastHour;
                    hourlyReading = new Hashtable<>();
                    DateTimeFormatter hourlyFormat = DateTimeFormatter.ofPattern( "h:mm a" );

                    // get the highs and lows from the forecast first
                    for ( DarkSkyWeatherDataItem.Hourly.Data wxHourlyForecast : darkSky.getHourly().getData() )
                    {
                        currentForecastHour = UtilityMethod.getDateTime(
                            Integer.parseInt( wxHourlyForecast.getTime() ) ).toInstant().atZone(
                                ZoneId.systemDefault() ).toLocalDateTime();

                        float fTemp = wxHourlyForecast.getTemperature();
                        String forecastTime = currentForecastHour.format( hourlyFormat );

                        if ( currentForecastHour.getMonth() == checkTime.getMonth() &&
                                currentForecastHour.getDayOfMonth() == checkTime.getDayOfMonth() &&
                                currentForecastHour.getYear() == checkTime.getYear() )
                        {
                            if( currentForecastHour.getHour() == checkTime.getHour() ||
                                    currentForecastHour.getHour() < checkTime.getHour())
                            {
                                continue;
                            }// end of if block
                            else if( currentForecastHour.getHour() == ( checkTime.getHour() + x ) )
                            {
                                hourlyReading.put( forecastTime, fTemp );
                                x++;
                            }// end of else if block
                        }// end of if block
                        else if ( currentForecastHour.isAfter( checkTime ) )
                        {
                            hourlyReading.put( forecastTime, fTemp );
                            x++;
                        }// end of if block

                        if( x == 6 )
                        {
                            break;
                        }// end of if block
                    }// end of first for each loop

                    // Five Day Forecast
                    i = 1;
                    hl = new int[ 5 ][ 2 ];

                    for ( DarkSkyWeatherDataItem.Daily.Data wxForecast : darkSky.getDaily().getData() )
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

                        largeWidgetRemoteViews.setTextViewText( dayTemps, temps );

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

                    // Display weather data on the large widget
                    largeWidgetRemoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + tempUnits);
                    largeWidgetRemoteViews.setTextViewText( R.id.txvFeelsLike, String.format(
                            "%s %s%s", FEELS_LIKE, currentFeelsLikeTemp, DEGREES ) );
                    largeWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                    largeWidgetRemoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );

                    largeWidgetRemoteViews.setTextViewText( R.id.txvWindReading,
                        String.format( Locale.ENGLISH, "%s %s %d %s", getString( R.string.wind ),
                            currentWindDirection.toString(),
                                Math.round( Float.parseFloat( currentWindSpeed.toString() ) ),
                                    ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                                        "km/h" : "mph" ) ) );

                    largeWidgetRemoteViews.setTextViewText( R.id.txvHumidity,
                        String.format( Locale.ENGLISH, "%s %s%%", getString( R.string.humidity ),
                            currentHumidity.toString() ) );

                    // Display weather data on the small widget
                    smallWidgetRemoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + DEGREES );
                    smallWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                    smallWidgetRemoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );

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

                        largeWidgetRemoteViews.setTextViewText( dayTemps, temps );

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

                    // Display weather data on the large widget
                    largeWidgetRemoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + tempUnits );
                    largeWidgetRemoteViews.setTextViewText( R.id.txvFeelsLike, String.format(
                            "%s %s%s", FEELS_LIKE, currentFeelsLikeTemp, DEGREES ) );
                    largeWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                    largeWidgetRemoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );

                    largeWidgetRemoteViews.setTextViewText( R.id.txvWindReading,
                        String.format( Locale.ENGLISH, "%s %s %d %s", getString( R.string.wind ),
                            currentWindDirection.toString(),
                                Math.round( Float.parseFloat( currentWindSpeed.toString() ) ),
                                    ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                                        "km/h" : "mph" ) ) );

                    largeWidgetRemoteViews.setTextViewText( R.id.txvHumidity,
                        String.format( Locale.ENGLISH, "%s %s%%", getString( R.string.humidity ),
                            currentHumidity.toString() ) );

                    // Display weather data on the small widget
                    smallWidgetRemoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + DEGREES );
                    smallWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                    smallWidgetRemoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );

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

                        largeWidgetRemoteViews.setTextViewText( dayTemps, temps );

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
                        currentFeelsLikeTemp.append(
                                UtilityMethod.calculateWindChill( Math.round( UtilityMethod.fahrenheitToCelsius( (float) fl ) ),
                                (int) Math.round( UtilityMethod.mphToKmh( weatherBitWx.getData().get( 0 ).getWindSpeed() ) ) ) );

                        // not supplied by provider
                        currentHigh.setLength( 0 );
                        currentHigh.append( 0 );

                        // not supplied by provider
                        currentLow.setLength( 0 );
                        currentHigh.append( 0 );

                        currentWindSpeed.setLength( 0 );
                        currentWindSpeed.append(
                                Math.round( UtilityMethod.mphToKmh( weatherBitWx.getData().get( 0 ).getWindSpeed() ) ) );
                    }// end of if block
                    else
                    {
                        currentTemp.setLength( 0 );
                        currentTemp.append( Math.round( (float) weatherBitWx.getData().get( 0 ).getTemp() ) );

                        currentFeelsLikeTemp.setLength( 0 );
                        currentFeelsLikeTemp.append(
                                UtilityMethod.calculateWindChill( Math.round( (float) fl ),
                                        (int) Math.round( weatherBitWx.getData().get( 0 ).getWindSpeed() ) ) );

                        // not supplied by provider
                        currentHigh.setLength( 0 );
                        currentHigh.append( 0 );

                        // not supplied by provider
                        currentLow.setLength( 0 );
                        currentHigh.append( 0 );

                        currentWindSpeed.setLength( 0 );
                        currentWindSpeed.append( Math.round( weatherBitWx.getData().get( 0 ).getWindSpeed() ) );
                    }// end of else block

                    // Display weather data on widget
                    largeWidgetRemoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + tempUnits);
                    largeWidgetRemoteViews.setTextViewText( R.id.txvFeelsLike, String.format(
                            "%s %s%s", FEELS_LIKE, currentFeelsLikeTemp, DEGREES ) );
                    largeWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                    largeWidgetRemoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );

                    largeWidgetRemoteViews.setTextViewText( R.id.txvWindReading,
                        String.format( Locale.ENGLISH, "%s %s %d %s", getString( R.string.wind ),
                            currentWindDirection.toString(),
                                Math.round( Float.parseFloat( currentWindSpeed.toString() ) ),
                                    ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                                        "km/h" : "mph" ) ) );

                    largeWidgetRemoteViews.setTextViewText( R.id.txvHumidity,
                        String.format( Locale.ENGLISH, "%s %s%%", getString( R.string.humidity ),
                            currentHumidity.toString() ) );

                    // Display weather data on the small widget
                    smallWidgetRemoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + DEGREES );
                    smallWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                    smallWidgetRemoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );

                    hourlyReading = new Hashtable<>();
                    float fTemp;    // forecasted hour temperature

                    if( ZoneId.systemDefault().getId().equalsIgnoreCase(
                            WeatherLionApplication.currentLocationTimeZone.getTimezoneId() ) )
                    {
                        checkTime = LocalDateTime.now();
                    }// end of if block
                    else
                    {
                        checkTime = WeatherLionApplication.localDateTime;
                    }// end of else block

                    DateTimeFormatter hourFormat = DateTimeFormatter.ofPattern( "h:mm a" );
                    DateTimeFormatter localDateFormat = DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm:ss" );
                    List< WeatherBitWeatherDataItem.FortyEightHourForecastData.Data > wFhf = weatherBitHx.getData();
                    x = 1;

                    for ( WeatherBitWeatherDataItem.FortyEightHourForecastData.Data wxHourlyForecast : wFhf )
                    {
                        currentForecastHour = LocalDateTime.parse(
                            wxHourlyForecast.getTimestampLocal(), localDateFormat );
                        String forecastTime = currentForecastHour.format( hourFormat );

                        fTemp = Math.round( wxHourlyForecast.getTemp() );

                        if ( currentForecastHour.getMonth() == checkTime.getMonth() &&
                                currentForecastHour.getDayOfMonth() == checkTime.getDayOfMonth() &&
                                currentForecastHour.getYear() == checkTime.getYear() )
                        {
                            // we don't need the current hour's forecast data or any before
                            if( currentForecastHour.getHour() == checkTime.getHour() ||
                                currentForecastHour.getHour() < checkTime.getHour())
                            {
                                continue;
                            }// end of if block
                            else if( currentForecastHour.getHour() == ( checkTime.getHour() + x ) )
                            {
                                hourlyReading.put( forecastTime, fTemp );
                                x++;
                            }// end of else if block
                        }// end of if block
                        else if ( currentForecastHour.isAfter( checkTime ) )
                        {
                            hourlyReading.put( forecastTime, fTemp );
                            x++;
                        }// end of if block

                        if( x == 6 )
                        {
                            break;
                        }// end of if block
                    }// end of for each loop

                    // Five Day Forecast
                    List< WeatherBitWeatherDataItem.SixteenDayForecastData.Data > wFdf = weatherBitFx.getData();
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

                            largeWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, ( Integer.parseInt( currentHigh.toString() ) > Integer.parseInt( currentTemp.toString().replace( "°F" , "" ) )
                                    ? currentHigh.toString() + DEGREES : Integer.parseInt( currentTemp.toString().replace( "°F" , "" ) ) + DEGREES ) );

                            largeWidgetRemoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );

                            smallWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, ( Integer.parseInt( currentHigh.toString() ) > Integer.parseInt( currentTemp.toString().replace( "°F" , "" ) )
                                    ? currentHigh.toString() + DEGREES : Integer.parseInt( currentTemp.toString().replace( "°F" , "" ) ) + DEGREES ) );

                            smallWidgetRemoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );

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

                        largeWidgetRemoteViews.setTextViewText( dayTemps, temps );

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
                        currentWindSpeed.append( Math.round( yahoo19.getCurrentObservation().getWind().getSpeed() ) );
                    }// end of else block

                    // Display weather data on the large widget
                    largeWidgetRemoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + tempUnits );
                    largeWidgetRemoteViews.setTextViewText( R.id.txvFeelsLike, String.format(
                            "%s %s%s", FEELS_LIKE, currentFeelsLikeTemp, DEGREES ) );

                    largeWidgetRemoteViews.setTextViewText( R.id.txvWindReading,
                        String.format( Locale.ENGLISH, "%s %s %d %s", getString( R.string.wind ),
                            currentWindDirection.toString(),
                                Math.round( Float.parseFloat( currentWindSpeed.toString() ) ),
                                    ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                                        "km/h" : "mph" ) ) );

                    // Display weather data on the small widget
                    smallWidgetRemoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + DEGREES );

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
                            largeWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, Math.round( UtilityMethod.fahrenheitToCelsius(
                                    (float) yFdf.get( i ).getHigh() ) ) + DEGREES );
                            largeWidgetRemoteViews.setTextViewText( R.id.txvDayLow, Math.round( UtilityMethod.fahrenheitToCelsius(
                                    (float) yFdf.get( i ).getLow() ) ) + DEGREES );

                            smallWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, Math.round( UtilityMethod.fahrenheitToCelsius(
                                    (float) yFdf.get( i ).getHigh() ) ) + DEGREES );
                            smallWidgetRemoteViews.setTextViewText( R.id.txvDayLow, Math.round( UtilityMethod.fahrenheitToCelsius(
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

                                largeWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                                largeWidgetRemoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );

                                smallWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                                smallWidgetRemoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );
                            }// end of if block

                            fh = String.valueOf( Math.round( yFdf.get( i ).getHigh() ) );
                            fLow = String.valueOf( Math.round( yFdf.get( i ).getLow() ) );

                            temps = String.format( "%s° %s°", fLow, fh );
                        }// end of else block

                        hl[i][0] = Integer.parseInt( fh );
                        hl[i][1] = Integer.parseInt( fLow );
                        int dayTemps = this.getResources().getIdentifier( "txvDay" + (i) + "Temps",
                                "id", this.getPackageName() );

                        largeWidgetRemoteViews.setTextViewText( dayTemps, temps );

                        if( i == 4 )
                        {
                            break;
                        }// end of if block
                    }// end of for loop

                    break;
                case WeatherLionApplication.YR_WEATHER:
                    // Yr always outputs data using metric values which must always
                    // be converted to imperial
                    currentWindDirection.setLength( 0 );
                    currentWindDirection.append(
                            yr.getForecast().get( 0 ).getWindDirCode() );

                    // convert units to imperial value
                    currentTemp.setLength( 0 );
                    currentTemp.append( Math.round(
                            UtilityMethod.celsiusToFahrenheit(
                                    yr.getForecast().get( 0 ).getTemperatureValue() ) ) );

                    currentWindSpeed.setLength( 0 );
                    currentWindSpeed.append(
                            Math.round(
                                UtilityMethod.mpsToMph( yr.getForecast().get( 0 ).getWindSpeedMps() ) ) );

                    int feelsLike = UtilityMethod.calculateWindChill(
                        Integer.parseInt( currentTemp.toString() ),
                            Integer.parseInt( currentWindSpeed.toString() ) );

                    currentFeelsLikeTemp.setLength( 0 );
                    currentFeelsLikeTemp.append( feelsLike );

                    List< YrWeatherDataItem.Forecast > fdf = yr.getForecast();
                    List< YrWeatherDataItem.HourByHourForecast > fhf = yr.getHourlyForecast();

                    // current temperature
                    int ct = Math.round( Float.parseFloat( currentTemp.toString() ) );
                    float lowestTempToday = ct;
                    float highestTempToday = ct;

                    if( ZoneId.systemDefault().getId().equalsIgnoreCase(
                            WeatherLionApplication.currentLocationTimeZone.getTimezoneId() ) )
                    {
                        checkTime = LocalDateTime.now();
                    }// end of if block
                    else
                    {
                        checkTime = WeatherLionApplication.localDateTime;
                    }// end of else block

                    hourlyReading = new Hashtable<>();
                    hourlyFormat = DateTimeFormatter.ofPattern( "h:mm a" );

                    x = 1;

                    // get the highs and lows from the forecast first
                    for ( YrWeatherDataItem.HourByHourForecast wxTempReading : fhf )
                    {
                        currentForecastHour = wxTempReading.getTimeFrom().toInstant().atZone(
                                ZoneId.systemDefault() ).toLocalDateTime();

                        fTemp = (float) Math.round(
                                UtilityMethod.celsiusToFahrenheit(
                                        wxTempReading.getTemperatureValue() ) );

                        if( fTemp > highestTempToday )
                        {
                            highestTempToday = Math.round( fTemp );
                        }// end of if block

                        if( fTemp < lowestTempToday )
                        {
                            lowestTempToday = Math.round( fTemp );
                        }// end of if block

                        String forecastTime = currentForecastHour.format( hourlyFormat );

                        if ( currentForecastHour.getMonth() == checkTime.getMonth() &&
                                currentForecastHour.getDayOfMonth() == checkTime.getDayOfMonth() &&
                                currentForecastHour.getYear() == checkTime.getYear() )
                        {
                            if( currentForecastHour.getHour() == checkTime.getHour() ||
                                    currentForecastHour.getHour() < checkTime.getHour())
                            {
                                continue;
                            }// end of if block
                            else if( currentForecastHour.getHour() == ( checkTime.getHour() + x ) )
                            {
                                hourlyReading.put( forecastTime, fTemp );
                                x++;
                            }// end of else if block
                        }// end of if block
                        else if ( currentForecastHour.isAfter( checkTime ) )
                        {
                           hourlyReading.put( forecastTime, fTemp );
                            x++;
                        }// end of if block

                        if( x == 6 )
                        {
                            break;
                        }// end of if block
                    }// end of first for each loop

                    currentHigh.setLength( 0 );
                    currentHigh.append( Math.round( highestTempToday ) );

                    currentLow.setLength( 0 );
                    currentLow.append( Math.round( lowestTempToday ) );

                    if( WeatherLionApplication.storedPreferences.getUseMetric() )
                    {
                        String metricCurrentTemp = String.format( "%s%s",
                                Math.round(
                                        UtilityMethod.fahrenheitToCelsius(
                                                Float.parseFloat( currentTemp.toString() ) ) ), tempUnits );
                        String metricCurrentHigh = String.format( "%s%s",
                                Math.round(
                                        UtilityMethod.fahrenheitToCelsius(
                                                Float.parseFloat( currentHigh.toString() ) ) ), DEGREES );
                        String metricCurrentLow = String.format( "%s%s",
                                Math.round(
                                        UtilityMethod.fahrenheitToCelsius(
                                                Float.parseFloat( currentLow.toString() ) ) ), DEGREES );
                        String metricCurrentFeelsLike = String.format( "%s %s%s",
                                FEELS_LIKE,
                                Math.round(
                                        UtilityMethod.fahrenheitToCelsius(
                                                Float.parseFloat( currentFeelsLikeTemp.toString() ) ) ), DEGREES );
                        String metricCurrentWindReading = String.format( "%s %s %s",
                                currentWindDirection.toString(),
                                currentWindSpeed, "km/h" );

                         // Display weather data on the large widget using metric values
                        largeWidgetRemoteViews.setTextViewText( R.id.txvCurrentTemperature, metricCurrentTemp );
                        largeWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, metricCurrentHigh );
                        largeWidgetRemoteViews.setTextViewText( R.id.txvDayLow, metricCurrentLow );
                        largeWidgetRemoteViews.setTextViewText( R.id.txvFeelsLike, metricCurrentFeelsLike );

                        largeWidgetRemoteViews.setTextViewText( R.id.txvWindReading,
                            String.format( Locale.ENGLISH, "%s %s %s", getString( R.string.wind ),
                                currentWindDirection.toString(),
                                    metricCurrentWindReading ) );

                        // Display weather data on the small widget using metric values
                        smallWidgetRemoteViews.setTextViewText( R.id.txvCurrentTemperature,  metricCurrentTemp );
                        smallWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, metricCurrentHigh );
                        smallWidgetRemoteViews.setTextViewText( R.id.txvDayLow, metricCurrentLow );
                    }// end of if block
                    else
                    {
                        // Display weather data on the large widget using imperial values
                        largeWidgetRemoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + tempUnits );
                        largeWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                        largeWidgetRemoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );
                        largeWidgetRemoteViews.setTextViewText( R.id.txvFeelsLike, String.format(
                                "%s %s%s", FEELS_LIKE, currentFeelsLikeTemp, DEGREES ) );

                        largeWidgetRemoteViews.setTextViewText( R.id.txvWindReading,
                            String.format( Locale.ENGLISH, "%s %s %d %s", getString( R.string.wind ),
                                currentWindDirection.toString(),
                                    Math.round( Float.parseFloat( currentWindSpeed.toString() ) ),
                                        "mph" ) );

                        // Display weather data on the small widget using imperial values
                        smallWidgetRemoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + DEGREES );
                        smallWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                        smallWidgetRemoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );
                    }// end of else block

                    // Five Day Forecast
                    i = 1;
                    float fHigh = 0;    // forecasted high
                    float fLow = 0;     // forecasted low
                    Date currentDate = new Date();
                    Date readingDate;
                    dailyReading = new Hashtable<>();

                    df = new SimpleDateFormat( "MMMM dd, yyyy", Locale.ENGLISH );
                    String temps;
                    x = 0;

                    // get the highs and lows from the forecast first
                    for ( Forecast wxTempReading : fdf )
                    {
                        readingDate = wxTempReading.getTimeFrom();
                        String cd = df.format( currentDate );
                        String rd = df.format( readingDate );
                        x++;

                        if( x == 1 && dailyReading.size() == 0 )
                        {
                            if( rd.equals(cd ) || readingDate.after( currentDate ) )
                            {
                                fHigh = highestTempToday;
                                fLow = lowestTempToday;

                                float[][] hl = { { highestTempToday, lowestTempToday } };
                                dailyReading.put( df.format( wxTempReading.getTimeFrom() ), hl );
                                x = 0;
                            }// end of if block
                        }// end of block
                        else
                        {
                            if( !df.format( readingDate ).equals( df.format( currentDate ) ) )
                            {
                                if ( x == 1 )
                                {
                                    fHigh = (float) Math.round( UtilityMethod.celsiusToFahrenheit( wxTempReading.getTemperatureValue() ) );
                                    fLow = (float) Math.round( UtilityMethod.celsiusToFahrenheit( wxTempReading.getTemperatureValue() ) );
                                }// end of if block

                                float cr = (float) Math.round( UtilityMethod.celsiusToFahrenheit( wxTempReading.getTemperatureValue() ) );

                                if ( cr > fHigh )
                                {
                                    fHigh = cr;
                                }// end of if block

                                if ( cr < fLow )
                                {
                                    fLow = cr;
                                }// end of if block

                                if ( wxTempReading.getTimePeriod() == 3 )
                                {
                                    x = 0;
                                    float[][] hl = { { fHigh, fLow } };
                                    dailyReading.put( df.format( wxTempReading.getTimeFrom() ), hl );
                                }// end of if block
                            }// end of if block
                        }// end of else block
                    }// end of first for each loop

                    x = 0;

                    // repeat the loop and store the five day forecast
                    for ( Forecast wxForecast : fdf )
                    {
                        x++;
                        String fDate = df.format( wxForecast.getTimeFrom() );

                        // data should already be in imperial values
                        fHigh = dailyReading.get( fDate ) [ 0 ][ 0 ];
                        fLow =  dailyReading.get( fDate ) [ 0 ][ 1 ];

                        // the first time period is always the current reading for this moment
                        if ( x == 1 )
                        {
                            if( WeatherLionApplication.storedPreferences.getUseMetric() )
                            {
                                String metricForecastHigh = String.format( "%s%s",
                                    Math.round(
                                        UtilityMethod.fahrenheitToCelsius( fHigh ) ),
                                            DEGREES );
                                String metricForecastLow = String.format( "%s%s",
                                    Math.round(
                                        UtilityMethod.fahrenheitToCelsius( fLow ) ),
                                            DEGREES );

                                largeWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, metricForecastHigh );
                                largeWidgetRemoteViews.setTextViewText( R.id.txvDayLow,  metricForecastLow );

                                smallWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, metricForecastHigh );
                                smallWidgetRemoteViews.setTextViewText( R.id.txvDayLow, metricForecastLow );

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

                                    largeWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                                    largeWidgetRemoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );

                                    smallWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                                    smallWidgetRemoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );
                                }// end of if block

                                temps = String.format( "%s° %s°", (int) fLow, (int) fHigh );
                            }// end of else block

                            int dayTemps = this.getResources().getIdentifier( "txvDay" + (i) + "Temps",
                                    "id", this.getPackageName() );

                            largeWidgetRemoteViews.setTextViewText( dayTemps, temps );

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
        else // if there is no Internet connection or just a unit change
        {
            WeatherLionApplication.storedData =
                    WeatherLionApplication.lastDataReceived.getWeatherData();

            tempUnits = WeatherLionApplication.storedPreferences.getUseMetric() ? CELSIUS : FAHRENHEIT;

            // populate the global variables
            currentWindDirection.setLength( 0 );
            currentWindDirection.append( WeatherLionApplication.storedData.getWind().getWindDirection() );

            currentWindSpeed.setLength( 0 );
            currentWindSpeed.append( WeatherLionApplication.storedData.getWind().getWindSpeed() );

            currentHumidity.setLength( 0 );
            currentHumidity.append( WeatherLionApplication.storedData.getAtmosphere().getHumidity() );

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

            // Display weather data on the widgets according to the selected unit of measures
            if( WeatherLionApplication.storedPreferences.getUseMetric() )
            {
                String metricCurrentTemp = String.format( "%s%s",
                        Math.round(
                                UtilityMethod.fahrenheitToCelsius(
                                        Float.parseFloat( currentTemp.toString() ) ) ), tempUnits );
                String metricCurrentHigh = String.format( "%s%s",
                        Math.round(
                                UtilityMethod.fahrenheitToCelsius(
                                        Float.parseFloat( currentHigh.toString() ) ) ), DEGREES );
                String metricCurrentLow = String.format( "%s%s",
                        Math.round(
                                UtilityMethod.fahrenheitToCelsius(
                                        Float.parseFloat( currentLow.toString() ) ) ), DEGREES );
                String metricCurrentFeelsLike = String.format( "%s %s%s",
                        FEELS_LIKE,
                        Math.round(
                                UtilityMethod.fahrenheitToCelsius(
                                        Float.parseFloat( currentFeelsLikeTemp.toString() ) ) ), DEGREES );
                String metricCurrentWindReading = String.format( "%s %s %s",
                        currentWindDirection.toString(),
                        currentWindSpeed,
                        ( WeatherLionApplication.storedPreferences.getUseMetric()
                                ? "km/h" : "mph" ) );

                // Display weather data on the large widget using metric values
                largeWidgetRemoteViews.setTextViewText( R.id.txvCurrentTemperature, metricCurrentTemp );
                largeWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, metricCurrentHigh );
                largeWidgetRemoteViews.setTextViewText( R.id.txvDayLow, metricCurrentLow );
                largeWidgetRemoteViews.setTextViewText( R.id.txvFeelsLike, metricCurrentFeelsLike );

                largeWidgetRemoteViews.setTextViewText( R.id.txvWindReading,
                    String.format( Locale.ENGLISH, "%s %s %s", getString( R.string.wind ),
                        currentWindDirection.toString(),
                            metricCurrentWindReading ) );

                // Display weather data on the small widget using metric values
                smallWidgetRemoteViews.setTextViewText( R.id.txvCurrentTemperature,  metricCurrentTemp );
                smallWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, metricCurrentHigh );
                smallWidgetRemoteViews.setTextViewText( R.id.txvDayLow, metricCurrentLow );
            }// end of if block
            else
            {
                // Display weather data on the large widget using imperial values
                largeWidgetRemoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + tempUnits );
                largeWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                largeWidgetRemoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );
                largeWidgetRemoteViews.setTextViewText( R.id.txvFeelsLike, String.format(
                        "%s %s%s", FEELS_LIKE, currentFeelsLikeTemp, DEGREES ) );


                largeWidgetRemoteViews.setTextViewText( R.id.txvWindReading,
                    String.format( Locale.ENGLISH, "%s %s %d %s", getString( R.string.wind ),
                        currentWindDirection.toString(),
                            Math.round( Float.parseFloat( currentWindSpeed.toString() ) ),
                                "mph" ) );

                // Display weather data on the small widget using imperial values
                smallWidgetRemoteViews.setTextViewText( R.id.txvCurrentTemperature, currentTemp.toString() + DEGREES );
                smallWidgetRemoteViews.setTextViewText( R.id.txvDayHigh, currentHigh + DEGREES );
                smallWidgetRemoteViews.setTextViewText( R.id.txvDayLow, currentLow + DEGREES );
            }// end of else block

            hl = new int[ 5 ][ 2 ];

            for ( i = 0; i <=  WeatherLionApplication.storedData.getDailyForecast().size(); i++ )
            {
                LastWeatherData.WeatherData.DailyForecast.DayForecast wxDayForecast =
                        WeatherLionApplication.storedData.getDailyForecast().get( i );
                String temps;
                int fHigh = wxDayForecast.getHighTemperature();
                int fLow = wxDayForecast.getLowTemperature();

                if( WeatherLionApplication.storedPreferences.getUseMetric() )
                {
                    String metricForecastHigh = String.format( "%s%s",
                        Math.round(
                            UtilityMethod.fahrenheitToCelsius( fHigh ) ),
                                DEGREES );
                    String metricForecastLow = String.format( "%s%s",
                        Math.round(
                            UtilityMethod.fahrenheitToCelsius( fLow ) ),
                                DEGREES );

                    temps = String.format( "%s %s", metricForecastHigh, metricForecastLow );
                }// end of if block
                else
                {
                    fHigh = wxDayForecast.getHighTemperature();
                    fLow = wxDayForecast.getLowTemperature();

                    temps = String.format( "%s° %s°", fLow, fHigh );
                }// end of else block

                hl[ i ][ 0 ] = fHigh;
                hl[ i ][ 1 ] = fLow;
                String viewIdName = "txvDay" + (i + 1) + "Temps";

                int dayTemps = this.getResources().getIdentifier( viewIdName,
                        "id", this.getPackageName() );

                largeWidgetRemoteViews.setTextViewText( dayTemps, temps );

                if( i == 4 )
                {
                    break;
                }// end of if block
            }// end of for loop
        }// end of else block

        int inputValue = WeatherLionApplication.storedPreferences.getUseMetric()
                ? (int) UtilityMethod.fahrenheitToCelsius( Integer.parseInt(
                    currentTemp.toString().replaceAll( "\\D+","" ) ) )
                : Integer.parseInt( currentTemp.toString().replaceAll( "\\D+","" ) );
        int colour = UtilityMethod.temperatureColor( inputValue );

        // Update the color of the temperature label
        largeWidgetRemoteViews.setTextColor( R.id.txvCurrentTemperature, colour );
        smallWidgetRemoteViews.setTextColor( R.id.txvCurrentTemperature, colour );
    }// end of method updateTemps  

    /**
     * This broadcast receiver waits for a broadcast from HttpHelper before
     * attempting to load the widget data
     */
    private BroadcastReceiver webServiceData = new BroadcastReceiver()
    {
        @Override
        public void onReceive( Context context, Intent intent )
        {
            WidgetHelper.getWidgetIds();
            String webData = intent.getStringExtra( HttpHelper.WEB_SERVICE_DATA_PAYLOAD );

            if( strJSON != null && !webData.equals( WeatherLionApplication.EMPTY_JSON ) )
            {
                strJSON.add( webData );

                if( expectedJSONSize == strJSON.size() )
                {
                    UtilityMethod.serviceCall(
                            WeatherLionApplication.storedPreferences.getProvider() );
                    updateAllAppWidgets( appWidgetManager );
                }// end of if block
            }// end of if block
            else if( !webData.equals( WeatherLionApplication.EMPTY_JSON ) )
            {
                dataRetrievalError( new NullPointerException( "No data from web service." ) );
            }// end of else if block
        }// end of anonymous method onReceive
    };// end of webServiceData
}// end of class WidgetUpdateService