package com.bushbungalo.weatherlion;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.text.HtmlCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.bushbungalo.weatherlion.custom.CustomPreferenceGrid;
import com.bushbungalo.weatherlion.database.DBHelper;
import com.bushbungalo.weatherlion.database.WeatherAccess;
import com.bushbungalo.weatherlion.model.CityData;
import com.bushbungalo.weatherlion.model.LastWeatherData;
import com.bushbungalo.weatherlion.model.TimeZoneInfo;
import com.bushbungalo.weatherlion.services.GeoLocationService;
import com.bushbungalo.weatherlion.services.WeatherDataXMLService;
import com.bushbungalo.weatherlion.services.WidgetUpdateService;
import com.bushbungalo.weatherlion.utils.JSONHelper;
import com.bushbungalo.weatherlion.utils.LastWeatherDataXmlParser;
import com.bushbungalo.weatherlion.utils.UtilityMethod;
import com.bushbungalo.weatherlion.utils.XMLHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Paul O. Patterson on 11/22/17.
 */

@SuppressWarnings({ "unused", "SameParameterValue"})
@SuppressLint("StaticFieldLeak")
public class WeatherLionApplication extends Application
{
    private static Context m_Context;
    private static List< String > m_KeysMissing;
    private static String m_TAG = "WeatherLionApplication";

    private int m_MethodResponse = 0;

    private BroadcastReceiver m_SystemBroadcastReceiver = new SystemBroadcastReceiver();
    private BroadcastReceiver m_AppBroadcastReceiver = new AppBroadcastReceiver();

    public static final int DAILY_CALL_LIMIT = 1000;

    public static final String LAUNCH_METHOD_EXTRA = "MethodToCall";
    public static final String EMPTY_JSON = "{}";

    public static final String LAUNCH_CONFIG = "Configure";
    public static final String LARGE_WIDGET = "LargeWeatherWidgetProvider";
    public static final String SMALL_WIDGET = "SmallWeatherWidgetProvider";

    public static final String PROGRAM_NAME = "Weather Lion";
    public static final String MAIN_DATABASE_NAME = "WeatherLion.db";
    public static final String CITIES_DATABASE_NAME = "WorldCities.db";
    public static final String WAK_DATABASE_NAME = "wak.db";

    public static final String WEATHER_IMAGES_ROOT = "weather_images/";
    public static final String WIDGET_BACKGROUNDS_PREVIEW_ROOT = "backgrounds/";

    // local weather data file
    public static final String WEATHER_DATA_XML = "WeatherData.xml";
    public static final String WEATHER_DATA_BACKUP = "WeatherData.bak";
    public static final String SERVICE_CALL_LOG = "ServiceCallsToday.json";

    public static final String OPEN_SOURCE_LICENCE = "licenses/open_source.json";

    // preferences constants
    public static final String WEATHER_SOURCE_PREFERENCE = "pref_wx_source";
    public static final String UPDATE_INTERVAL = "pref_update_interval";
    public static final String CURRENT_LOCATION_PREFERENCE = "pref_location";
    public static final String USE_GPS_LOCATION_PREFERENCE = "pref_use_gps";
    public static final String USE_METRIC_PREFERENCE = "pref_use_metric";
    public static final String WIDGET_BACKGROUND_PREFERENCE = "pref_widget_background";
    public static final String ICON_SET_PREFERENCE = "pref_icon_set";
    public static final String UI_FONT = "pref_ui_fonts";
    public static final String FIRST_RUN = "first_run";

    // font name constants
    public static final String SYSTEM_FONT = "System";
    public static final String HELVETICA_FONT = "Helvetica Neue";
    public static final String PRODUCT_SANS_FONT = "Product Sans";
    public static final String SAMSUNG_SANS_FONT = "Samsung Sans";

    // theme constants
    public static final String AQUA_THEME = "aqua";
    public static final String RABALAC_THEME = "rabalac";
    public static final String FROSTY_THEME = "frosty";
    public static final String LION_THEME = "lion";

    // weather provider and web API constants
    public static final String DARK_SKY = "Dark Sky Weather";
    public static final String GEO_NAMES = "GeoNames";
    public static final String HERE_MAPS = "Here Maps Weather";
    public static final String OPEN_WEATHER = "Open Weather Map";
    public static final String WEATHER_BIT = "Weather Bit";
    public static final String YAHOO_WEATHER = "Yahoo! Weather";
    public static final String YR_WEATHER = "Norwegian Meteorological Institute";
    public static String[] providerNames = new String[] {
            DARK_SKY, GEO_NAMES, HERE_MAPS, OPEN_WEATHER,
            WEATHER_BIT, YAHOO_WEATHER, YR_WEATHER };

    public static final String DEFAULT_ICON_SET = "MIUI";
    public static final String CELSIUS = "\u00B0C";
    public static final String DEGREES = "\u00B0";
    public static final String FAHRENHEIT = "\u00B0F";
    public static final String UNIT_NOT_CHANGED = "false";
    public static final String UNIT_CHANGED = "true";

    public static final String ERROR_SUNRISE_TIME = "5:00 AM";
    public static final String ERROR_SUNSET_TIME = "10:00 pM";

    public static String widBackgroundColor;
    public static String currentWxLocation; // this location must always reflect the user's last selection
    public static String iconSet = null; // To be updated
    public static String systemLocation;
    public static String selectedProvider;
    public static String selectedKeyName;
    public static String dataAccessKeyName;
    public static String timeOfDayToUse;

    public static StringBuilder currentSunriseTime = new StringBuilder();
    public static StringBuilder currentSunsetTime = new StringBuilder();
    public static StringBuilder previousWeatherProvider = new StringBuilder();

    public static String[] authorizedProviders;
    public static String[] darkSkyRequiredKeys = new String[] { "api_key" };
    public static String[] geoNamesRequiredKeys = new String[] { "username" };
    public static String[] hereMapsRequiredKeys = new String[] { "app_id", "app_code" };
    public static String[] openWeatherMapRequiredKeys = new String[] { "api_key" };
    public static String[] weatherBitRequiredKeys = new String[] { "api_key" };
    public static String[] yahooRequiredKeys = new String[] { "app_id", "consumer_key",
            "consumer_secret" };

    public static boolean geoNamesAccountLoaded;
    public static boolean weatherLoadedFromProvider;
    public static boolean noAccessToStoredProvider;
    public static boolean connectedToInternet;
    public static boolean locationSet = false;
    public static boolean useSystemLocation = false;

    public static Activity currentActivity;

    public static ArrayList< String > webAccessGranted;

    public static Color systemColor;

    public static Date previousLastUpdate;

    public static Drawable systemButtonDrawable;
    public static Drawable widgetBackgroundDrawable;

    public static File previousCitySearchFile = null;

    public static LinkedHashMap< String, Typeface > fonts;
    public static LinkedHashMap< String, String > updateIntervalValues;
    static
    {
        updateIntervalValues = new LinkedHashMap<>();
        updateIntervalValues.put( "900000", "15 Minutes" );
        updateIntervalValues.put( "1800000", "30 Minutes" );
        updateIntervalValues.put( "3600000", "1 Hour" );
    }

    public static LocalDateTime localDateTime;

    public static SharedPreferences spf = null;

    public static Typeface currentTypeface;
    public static Typeface helveticaNeue;
    public static Typeface productsSans;
    public static Typeface samsungSans;

    public static boolean firstRun;
    public static boolean localWeatherDataAvailable;
    public static boolean changeWidgetUnit;
    public static boolean useGps;
    public static boolean useMetric;
    public static boolean gpsRadioEnabled;

    public static int[] largeWidgetIds;
    public static int[] smallWidgetIds;

    public static boolean firstLaunchCompleted;
    public static boolean dataLoadedSuccessfully = false;
    public static boolean restoringWeatherData;
    public static boolean mainWindowShowing = false;

    public static CityData currentCityData;
    public static LastWeatherData lastDataReceived;
    public static LastWeatherData.WeatherData storedData;
    public static Preference systemPreferences;
    public static Preference storedPreferences;
    public static TimeZoneInfo currentLocationTimeZone;
    public static WeatherLionApplication thisClass;

    /**
     * Checks to see if the program is being run for the first time.
     */
    private void checkFirstRun()
    {
        if( spf != null )
        {
            if ( spf.getBoolean( FIRST_RUN, Preference.DEFAULT_FIRST_RUN ) )
            {
                firstRun = true;
                Preference.createDefaultPreferencesPropertiesFile();
                constructDataAccess();
                firstLaunchCompleted = true;

                systemColor = Color.valueOf( getColor( R.color.lion ) );
                systemButtonDrawable = getDrawable( R.drawable.wl_lion_rounded_btn_bg );
                widgetBackgroundDrawable = getDrawable( R.drawable.wl_lion_bg_large );
                setTheme( R.style.LionThemeLight );
            }//end of if block
            else
            {
                firstRun = false;
                firstLaunchCompleted = false;
            }// end of else block
        }// end of if block
        else
        {
            firstRun = false;
            firstLaunchCompleted = false;
        }// end of else block
    }// end of method checkFirstRun

    /**
     * Call the weather service that attempts to load the widget with
     * the applicable weather data returned from the weather service.
     *
     * @param uriData   The uri data that should be passed to the weather service class
     * @param methodName    A method that should be run instead of the normal routine
     */
    private static void actionWeatherService( String uriData, String methodName )
    {
        String invoker = m_TAG + "::" + "actionWeatherService";
        Bundle extras = new Bundle();

        extras.putString( WidgetUpdateService.WEATHER_SERVICE_INVOKER, invoker );
        extras.putString( WidgetUpdateService.WEATHER_DATA_UNIT_CHANGED, uriData );

        if( methodName == null )
        {
            extras.putString( LAUNCH_METHOD_EXTRA, null );
            UtilityMethod.refreshRequestedBySystem = true;
        }// end of if block
        else
        {
            extras.putString( LAUNCH_METHOD_EXTRA, methodName );
        }// end of else block

        Intent methodIntent = new Intent(m_Context, WidgetUpdateService.class );
        methodIntent.putExtras( extras );
        WidgetUpdateService.enqueueWork(m_Context, methodIntent );
    }// end of method actionWeatherService

    /**
     * Saves a city to a local SQLite 3 database.
     *
     * @param keyProvider  The name of the web service that supplies the key
     * @param keyName      The name of the key
     * @param keyValue     The value of the key
     * @param hex          A value used in the encryption process
     * @return             An {@code int} value indicating success or failure.<br /> 1 for success and 0 for failure.
     */
    public static int addSiteKeyToDatabase( String keyProvider, String keyName, String keyValue, String hex )
    {
        SQLiteOpenHelper dbHelper = new DBHelper( getAppContext(),
                WAK_DATABASE_NAME );
        SQLiteDatabase weatherAccessDB = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues( 4 );
        values.put( WeatherAccess.KEY_PROVIDER, keyProvider );
        values.put( WeatherAccess.KEY_NAME, keyName );
        values.put( WeatherAccess.KEY_VALUE, keyValue );
        values.put( WeatherAccess.HEX_VALUE, hex );

        return (int) weatherAccessDB.insertWithOnConflict( WeatherAccess.ACCESS_KEYS, null, values,
                SQLiteDatabase.CONFLICT_IGNORE );
    }// end of method addSiteKeyToDatabase

    /***
     * Build the required storage files.
     *
     * @return An {@code int} value 0 or 1 representing success or failure.
     */
    public static int buildRequiredDatabases()
    {
        int success;

        // The following lines will create the databases if they do not already exist
        SQLiteOpenHelper dbHelper = new DBHelper( getAppContext(), WAK_DATABASE_NAME );
        SQLiteDatabase weatherAccessDB = dbHelper.getReadableDatabase();
        weatherAccessDB.close();

        dbHelper = new DBHelper( getAppContext(), CITIES_DATABASE_NAME );
        SQLiteDatabase worldCitiesDB = dbHelper.getReadableDatabase();
        worldCitiesDB.close();

        dbHelper = new DBHelper( getAppContext(), MAIN_DATABASE_NAME );
        SQLiteDatabase mainDB = dbHelper.getReadableDatabase();
        mainDB.close();

        // if the code gets to here then it was a success
        success = 1;

        return success;
    }// end of method buildRequiredDatabases

    /**
     * This method uses refection to call a method using a {@code String} value representing the
     * method name.
     *
     * @param obj   The {@code Class} {@code Object} which contains the method.
     * @param methodName    A {@code String} representing the name of the method to be called.
     * @param parameterTypes    An array representing the param type example new Class[]{String.class} or null can be passed.
     * @param paramValues    An array representing the param value example new Object[]{"GeoNames"} or null can be passed.
     */
    public static void callMethodByName( Object obj, String methodName,
                                         Class[] parameterTypes, Object[] paramValues, String caller )
    {
        Method method;

        // If the object is null that means that the method is private to this class
        if( obj == null ) obj = thisClass;

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
                IllegalAccessException | InvocationTargetException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
        m_TAG + "::callMethodByName [line: " +
                    UtilityMethod.getExceptionLineNumber( e )  + "]::caller -> " + caller );
        }// end of catch block
    }// end of method callNMethodByName

    /**
     * Determines weather or not a specific database contains a specific table
     *
     * @param tableName The table that is in question
     *
     * @return        	 The {@code int} value of 0 if not found of 1 if the table is found
     */
    private boolean checkIfTableExists( String dbName, String tableName )
    {
        SQLiteOpenHelper dbHelper = new DBHelper( getAppContext(), dbName );
        SQLiteDatabase weatherAccessDB = dbHelper.getReadableDatabase();
        int found = 0;

        try
        {
            Cursor cursor = weatherAccessDB.query( true, dbName,
                    new String[]{"name"}, "type = ? AND name = ?", new String[]{ "table", tableName },
                    null, null, null, null, null );

            while ( cursor.moveToNext() )
            {
                found++;
            }// end of while loop

            cursor.close();
            weatherAccessDB.close();
        }// end of try block
        catch( Exception e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    m_TAG + "::getSiteKeyFromDatabase [line: " +
                            UtilityMethod.getExceptionLineNumber( e )  + "]" );
        }// end of catch block

        return found > 0;
    }// end of method checkIfTableExists

    /**
     *  Check to see if the user has updated the location for which the weather data should
     *  be displayed.
     */
    private void checkCurrentCityStatus()
    {
        String currLocation = spf.getString( CURRENT_LOCATION_PREFERENCE,
                Preference.DEFAULT_WEATHER_LOCATION );
        locationSet = !Objects.requireNonNull( currLocation ).equalsIgnoreCase(
                Preference.DEFAULT_WEATHER_LOCATION );

        // if the user did not specify a location then one will be required.
        if( !locationSet )
        {
            showPreferenceActivity();
        }// end of if block
    }// end of method checkCurrentCityStatus

    /**
     * Check to see if the user wants the program to get their current location by way of the
     * device's gps radio.
     */
    private void checkSystemLocationStatus()
    {
        if( useSystemLocation )
        {
            storedPreferences.setUseSystemLocation( true );
            storedPreferences.setLocation( systemLocation );

            systemPreferences.setPrefValues( USE_GPS_LOCATION_PREFERENCE,
                    Boolean.toString( true ) );

            systemPreferences.setPrefValues( CURRENT_LOCATION_PREFERENCE,
                    systemLocation );

            // save the city to the local WorldCites database
            if( UtilityMethod.addCityToDatabase(
                    currentCityData.getCityName(),
                    currentCityData.getCountryName(),
                    currentCityData.getCountryCode(),
                    currentCityData.getRegionName(),
                    currentCityData.getRegionCode(),
                    currentCityData.getTimeZone(),
                    currentCityData.getLatitude(),
                    currentCityData.getLongitude() ) == 1 )
            {
                UtilityMethod.logMessage(UtilityMethod.LogLevel.INFO,
                    String.format( "%s was added to the database",
                        systemLocation ), m_TAG + ":: checkSystemLocationStatus" );
            }// end of if block

            JSONHelper.exportCityToJSON( currentCityData );
            XMLHelper.exportCityDataToXML( currentCityData );

            locationSet = true;
        }// end of if block
        else
        {
            String prompt = "You must specify a current location in order to run the program.\n" +
                "Would you like to specify it now?";

            responseDialog(PROGRAM_NAME + " - Location Setup",
                prompt,"Yes", "No","setCurrentCityStatus",
                "checkCurrentCityStatus", new Object[]{true}, new Class[]{Boolean.class});
        }// end of else block
    }// end of method checkSystemLocationStatus

    /**
     * Checks to see if any provider stored in the database is missing a key
     * that is required.
     */
    private void checkForMissingKeys()
    {
        String mks = m_KeysMissing.toString().replaceAll( "[\\[\\](){}]", "" );
        String fMks;


        if( UtilityMethod.numberOfCharacterOccurrences( ',', mks ) > 1 )
        {
            fMks = UtilityMethod.replaceLast( ",", ", and", mks );
        }// end of if block
        else if( UtilityMethod.numberOfCharacterOccurrences( ',', mks ) == 1 )
        {
            fMks = mks.replace( ",", " and" );
        }// end of else block
        else
        {
            fMks = mks;
        }// end of else block

        String prompt = "Yahoo! Weather requires the following missing " +
                ( m_KeysMissing.size() > 1 ? "keys" : "key" ) + ":\n"
                + fMks + "\nDo you wish to add " +
                ( m_KeysMissing.size() > 1 ? "them" : "it" ) + " now?";

        responseDialog( PROGRAM_NAME + " - Missing Key",
                prompt,"Yes", "No","showDataKeysDialog",
                "lackPrivilegesMessage", null, null );
    }// end of message checkForMissingKeys

    /**
     * Construct the relevant databases that will be used throughout the program
     */
    public void constructDataAccess()
    {
        // build the required storage files
        if( buildRequiredDatabases() == 1 )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                    "All required databases constructed successfully.",
                    "WeatherLionMain::main" );
        }// end of if block
        else
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE,
                    "All required databases were not constructed successfully.",
                    "WeatherLionMain::main" );
        }// end of else block
    }// end of method constructDataAccess

    private void createServiceCallLog()
    {
        File callTracker = new File( this.getFileStreamPath( SERVICE_CALL_LOG ).toString() );
        Map< String, Integer > exportedServiceMap = new HashMap<>();
        exportedServiceMap.put( DARK_SKY, 0 );
        exportedServiceMap.put( HERE_MAPS, 0 );
        exportedServiceMap.put( OPEN_WEATHER, 0 );
        exportedServiceMap.put( WEATHER_BIT, 0 );
        exportedServiceMap.put( YAHOO_WEATHER, 0 );
        exportedServiceMap.put( YR_WEATHER, 0 );

        Map<String, Object> exportedServiceLog = new HashMap<>();
        exportedServiceLog.put( "Date", new Date() );
        exportedServiceLog.put( "Service", exportedServiceMap );

        Date lastModDate = new Date( callTracker.lastModified() );
        String lm = new SimpleDateFormat(
                "MMM dd, yyyy", Locale.ENGLISH ).format( lastModDate );
        String today = new SimpleDateFormat(
                "MMM dd, yyyy", Locale.ENGLISH ).format( new Date() );

        // create a new file if it does not exists or the file is from another date
        if( !callTracker.exists() || !today.equals( lm ) )
        {
            Gson gson = new GsonBuilder().create();

            // return the JSON map as a string
            String json = gson.toJson( exportedServiceLog );

            JSONHelper.saveToJSONFile( json, callTracker.toString(), true );

            // log the action taken
            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
        "Service log created!", m_TAG + "::createServiceCallLog" );
        }// end of if block
    }// end of method createServiceCallLog

    /***
     * Decrypt a {@code String} the was encrypted using DES Encryption
     *
     * @param encryptedKey An encrypted {@code String} value
     * @param hKey A hex value used during the encryption
     * @return A {@code String} value containing the decrypted {@code String}
     */
    @SuppressLint("GetInstance")
    public static String decrypt( String encryptedKey, String hKey )
    {
        Cipher dcipher;
        String strData = null;

        try
        {
            SecretKey secretKey = new SecretKeySpec( Base64.getDecoder().decode(
                    hKey.getBytes( StandardCharsets.UTF_8 ) ), "DES" );
            dcipher = Cipher.getInstance( "DES" );
            dcipher.init( Cipher.DECRYPT_MODE, secretKey );
            byte[] dec = Base64.getDecoder().decode( encryptedKey.getBytes() );
            byte[] utf8 = dcipher.doFinal( dec );
            strData = new String( utf8, StandardCharsets.UTF_8 );
        }// end of try block
        catch( Exception e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    m_TAG + "::decrypt [line: " +
                            UtilityMethod.getExceptionLineNumber( e )  + "]" );
        }// end of catch block

        return strData;
    }// end of method decrypt

    private void deletionAttempted()
    {
        if( m_MethodResponse == -1 || m_MethodResponse > 0 )
        {
            if( m_MethodResponse > 0 )
            {
                UtilityMethod.butteredToast( getAppContext(),"The " + selectedProvider +
                                " " + dataAccessKeyName + " has been removed from the database.", 1,
                        Toast.LENGTH_LONG );
            }// end of if block
            else
            {
                UtilityMethod.butteredToast( getAppContext(),"An error occurred while removing the " + selectedProvider +
                        " " + dataAccessKeyName + " from the database!\nPlease check the Key Provider" +
                        " and Key Name specified and try again.", 2, Toast.LENGTH_LONG );
            }// end of else block
        }// end of if block
        else
        {
            UtilityMethod.butteredToast( getAppContext(),"Key deletion aborted!",
                    1, Toast.LENGTH_SHORT );
        }// end of else block
    }// end of method deletionAttempted

    /**
     * Delete all notifications for this app
     */
    private void deleteAllNotification()
    {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        notificationManager.cancelAll();
    }// end of method deleteAllNotification

    /***
     * Removes a key that is stored in the local database
     *
     * @param keyProvider  The name of the web service that supplies the key
     * @param keyName The name of the key
     */
    public void deleteSiteKeyFromDatabase( String keyProvider, String keyName )
    {
        SQLiteOpenHelper dbHelper = new DBHelper( getAppContext(),
                WAK_DATABASE_NAME );
        SQLiteDatabase weatherAccessDB = dbHelper.getWritableDatabase();

        // return the number of rows affected if a whereClause is passed in, 0 otherwise.
        m_MethodResponse = weatherAccessDB.delete( WeatherAccess.ACCESS_KEYS,
                "KeyProvider = ? AND keyName = ?", new String[]{ keyProvider, keyName } );

        deletionAttempted();
    }// end of method deleteSiteKeyFromDatabase

    /***
     * Encrypt a {@code String} using DES Encryption
     *
     * @param userKey A {@code String} value that the user wishes to encrypt
     * @return A {@code String} array object containing the encrypted key and hex
     */
    @SuppressLint("GetInstance")
    public static String[] encrypt( String userKey )
    {
        String[] strData = new String[ 2 ];
        Cipher ecipher;

        try
        {
            String encKey;
            String res;
            SecretKey secretKey;

            secretKey = KeyGenerator.getInstance( "DES" ).generateKey();
            ecipher = Cipher.getInstance( "DES" );
            ecipher.init( Cipher.ENCRYPT_MODE, secretKey );
            byte[] utf8 = userKey.getBytes( StandardCharsets.UTF_8 );
            byte[] enc = ecipher.doFinal( utf8 );

            enc = Base64.getEncoder().encode( enc );
            res = new String( enc );

            // Returning values 0 = Encrypted String 1 = Key For Storage
            strData[ 0 ] = res;
            byte[] keyBytes = secretKey.getEncoded();
            encKey = new String( Base64.getEncoder().encode( keyBytes ), StandardCharsets.UTF_8 );
            strData[ 1 ] = encKey;
        }// end of try block
        catch( Exception e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    m_TAG + "::encrypt [line: " + UtilityMethod.getExceptionLineNumber( e )  + "]" );
        }// end of catch block

        return strData;
    }// end of method encrypt

    /**
     * Exits the application/program.
     */
    public static void exitApplication()
    {
        android.os.Process.killProcess( android.os.Process.myPid() );
    }// end of method exitApplication

    /**
     * Retrieves an encrypted access key from a local SQLite 3 database
     *
     * @param keyProvider  The name of the web service that supplies the key
     * @return				An {@code ArrayList} containing the keys assigned to the specified provider
     */
    public static ArrayList< String > getSiteKeyFromDatabase( String keyProvider )
    {
        ArrayList< String > ak = new ArrayList<>();
        SQLiteOpenHelper dbHelper = new DBHelper( getAppContext(),
                WAK_DATABASE_NAME);
        SQLiteDatabase weatherAccessDB = dbHelper.getReadableDatabase();

        try
        {
            Cursor cursor = weatherAccessDB.query( WeatherAccess.ACCESS_KEYS,
                WeatherAccess.ALL_COLUMNS, null, null, null,
                null, null );
            int found = 0;

            while ( cursor.moveToNext() )
            {
                if( cursor.getString( 0 ).equalsIgnoreCase( keyProvider ) )
                {
                    ak.add( cursor.getString( 1 )  + ":" + cursor.getString( 2 ) +
                            ":" + cursor.getString( 3 ) );
                    found++;
                }// end of if block

            }// end of while loop

            cursor.close();
            weatherAccessDB.close();

            if( found > 0 )
            {
                return ak;
            }// end of if block
            else
            {
                return null;
            }// end of else block
        }// end of try block
        catch( Exception e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    m_TAG + "::getSiteKeyFromDatabase [line: " +
                            UtilityMethod.getExceptionLineNumber( e )  + "]" );

            return null;
        }// end of catch block
    }// end of method getSiteKeyFromDatabase

    /***
     * Prepare the application/program for execution
     */
    private void init()
    {
        File backupFile = new File( WEATHER_DATA_BACKUP );

        if( backupFile.exists() && UtilityMethod.isFileEmpty( this, WEATHER_DATA_BACKUP ) )
        {
            UtilityMethod.removeFile( WEATHER_DATA_BACKUP );
        }// end of if block

        if( !locationCheck() )
        {
            String prompt = "The program will not run without a location set.\n"
                    + "Enjoy the weather!";

            showMessageDialog(null, prompt, PROGRAM_NAME + " - No Location",
                    "exitApplication", null, null);
        }// end of if block
        else
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO, "Necessary requirements met...",
                    m_TAG + "::init" );
            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,"Launching Weather Widget...",
                    m_TAG + "::init" );
        }// end of else block
    }// end of method init

    /**
     * The user does not provide sufficient data for the app to be run as intended
     */
    private void lackPrivilegesMessage()
    {
        UtilityMethod.missingRequirementsPrompt( "Insufficient Access Privileges" );
        exitApplication();
    }// end of method lackPrivilegesMessage

    /**
     * Load all access providers stored in the database
     */
    public void loadAccessProviders()
    {
        ArrayList< String > appKeys;
        webAccessGranted = new ArrayList<>();

        for ( String provider : providerNames )
        {
            appKeys = getSiteKeyFromDatabase( provider );

            if( appKeys != null )
            {
                switch ( provider )
                {
                    case DARK_SKY:

                        for ( String key : appKeys )
                        {
                            String[] kv = key.split( ":" );

                            if( Arrays.asList( darkSkyRequiredKeys ).contains( kv[ 0 ].toLowerCase() ) )
                            {
                                WidgetUpdateService.darkSkyApiKey =
                                    decrypt( kv[ 1 ], kv[ 2 ] );
                            }// end of if block
                        }// end of for each loop

                        if( WidgetUpdateService.darkSkyApiKey != null )
                        {
                            webAccessGranted.add( DARK_SKY );
                        }// end of if block

                        break;

                    case GEO_NAMES:

                        for ( String key : appKeys )
                        {
                            String[] kv = key.split( ":" );

                            if( Arrays.asList( geoNamesRequiredKeys ).contains( kv[ 0 ].toLowerCase() ) )
                            {
                                WidgetUpdateService.geoNameAccount = decrypt( kv[ 1 ], kv[ 2 ] );
                            }// end of if block
                        }// end of for each loop

                        if( WidgetUpdateService.geoNameAccount != null )
                        {
                            webAccessGranted.add( GEO_NAMES );
                            geoNamesAccountLoaded = true;
                        }// end of if block

                        break;
                    case OPEN_WEATHER:

                        for ( String key : appKeys )
                        {
                            String[] kv = key.split( ":" );

                            if( Arrays.asList( openWeatherMapRequiredKeys ).contains( kv[ 0 ].toLowerCase() ) )
                            {
                                WidgetUpdateService.openWeatherMapApiKey = decrypt( kv[ 1 ], kv[ 2 ] );
                            }// end of if block
                        }// end of for each loop

                        if( WidgetUpdateService.openWeatherMapApiKey != null )
                        {
                            webAccessGranted.add( OPEN_WEATHER );
                        }// end of if block

                        break;
                    case WEATHER_BIT:

                        for ( String key : appKeys )
                        {
                            String[] kv = key.split( ":" );

                            if( Arrays.asList( weatherBitRequiredKeys ).contains( kv[ 0 ].toLowerCase() ) )
                            {
                                WidgetUpdateService.weatherBitApiKey = decrypt( kv[ 1 ], kv[ 2 ] );
                            }// end of if block
                        }// end of for each loop

                        if( WidgetUpdateService.weatherBitApiKey != null )
                        {
                            webAccessGranted.add( WEATHER_BIT );
                        }// end of if block

                        break;
                    case HERE_MAPS:

                        for ( String key : appKeys )
                        {
                            String[] kv = key.split( ":" );

                            if( Arrays.asList( hereMapsRequiredKeys ).contains( kv[ 0 ].toLowerCase() ) )
                            {
                                switch(  kv[ 0 ].toLowerCase() )
                                {
                                    case "app_id":
                                        WidgetUpdateService.hereAppId = decrypt( kv[ 1 ], kv[ 2 ] );
                                        break;
                                    case "app_code":
                                        WidgetUpdateService.hereAppCode = decrypt( kv[ 1 ], kv[ 2 ] );
                                        break;
                                    default:
                                        break;
                                }// end of switch block
                            }// end of if block
                        }// end of for each loop

                        if( WidgetUpdateService.hereAppId != null && WidgetUpdateService.hereAppCode != null )
                        {
                            webAccessGranted.add( HERE_MAPS );
                        }// end of if block
                        else if( WidgetUpdateService.hereAppId != null )
                        {
                            UtilityMethod.showMessageDialog( UtilityMethod.MsgFormat.TEXT,
                                    "Here Maps Weather requires an app_code which is"
                                            + " not stored in the database.",
                                    PROGRAM_NAME + " - Missing Key", getAppContext() );
                        }// end of if block
                        else if( WidgetUpdateService.hereAppCode != null )
                        {

                            UtilityMethod.butteredToast( null, "Here Maps Weather requires an app_id which is"
                                    + " not stored in the database.", 2, Toast.LENGTH_SHORT );
                        }// end of if block
                        break;
                    case YAHOO_WEATHER:
                        ArrayList<String> keysFound = new ArrayList<>();

                        for ( String key : appKeys )
                        {
                            String[] kv = key.split( ":" );

                            if( Arrays.asList( yahooRequiredKeys ).contains( kv[ 0 ].toLowerCase() ) )
                            {
                                switch(  kv[ 0 ].toLowerCase() )
                                {
                                    case "app_id":
                                        WidgetUpdateService.yahooAppId = decrypt( kv[ 1 ], kv[ 2 ] );
                                        keysFound.add( getAppContext().getString( R.string.app_id ) );

                                        break;
                                    case "consumer_key":
                                        WidgetUpdateService.yahooConsumerKey = decrypt( kv[ 1 ], kv[ 2 ] );
                                        keysFound.add( getAppContext().getString( R.string.consumer_key ) );

                                        break;
                                    case "consumer_secret":
                                        WidgetUpdateService.yahooConsumerSecret = decrypt( kv[ 1 ], kv[ 2 ] );
                                        keysFound.add( getAppContext().getString( R.string.consumer_secret ) );

                                        break;
                                    default:
                                        break;
                                }// end of switch block
                            }// end of if block
                        }// end of for each loop

                        m_KeysMissing = new LinkedList<>( Arrays.asList( yahooRequiredKeys ) );
                        m_KeysMissing.removeAll( keysFound ); // remove all the keys found

                        if( m_KeysMissing.size() == 0 )
                        {
                            webAccessGranted.add( YAHOO_WEATHER );
                        }// end of if block
                        else
                        {
                            // do not check for missing keys if the form is already displayed
                            checkForMissingKeys();
                        }// end of else block

                        break;
                    default:
                        break;
                }// end of switch block
            }// end of if block
        }// end of outer for each loop

        // add the only weather provider that does not require a key
        webAccessGranted.add( YR_WEATHER );

        if( webAccessGranted.size() > 0 )
        {
            String s = webAccessGranted.toString().replaceAll( "[\\[\\](){}]", "" );
            String fs;

            if( UtilityMethod.numberOfCharacterOccurrences( ',', s ) > 1 )
            {
                fs = UtilityMethod.replaceLast( ",", ", and", s );
            }// end of if block
            else if( UtilityMethod.numberOfCharacterOccurrences( ',', s ) == 1 )
            {
                fs = s.replace( ",", " and" );
            }// end of else block
            else
            {
                fs = s;
            }// end of else block

            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                    "The following access providers were loaded:\n" + fs.trim() + ".", "WeatherLionApplication::loadAccessProviders" );

        }// end of if block
        else
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                    "No valid access privileges were stored in the database!", "WeatherLionApplication::loadAccessProviders" );
        }// end of else block

        if( webAccessGranted.size() == 0 )
        {
            noAccessPrivilegesStored();
        }// end of if block
    }// end of method loadAccessProviders

    /***
     * Attempt to store the user's current location
     *
     * @return A {@code boolean} value true/false if successful
     */
    private boolean locationCheck()
    {
        if( storedPreferences.getLocation().length() == 0 )
        {
            if( systemLocation != null )
            {
                String prompt = "You must specify a current location in order to run the program.\n" +
                        "Your current location is detected as " + systemLocation + ".\n" +
                        "Would you like to use it as your current location?";

                responseDialog(PROGRAM_NAME + " - Use GPS Location",
                        prompt,"Yes", "No","setSystemLocationUsage",
                        "setSystemLocationUsage", new Object[]{true}, new Class[]{Boolean.class});
            }// end of if block
            else
            {

                String prompt = "You must specify a current location in order to run the program.\n" +
                        "Would you like to specify it now?";

                responseDialog(PROGRAM_NAME + " - Location Setup",
                        prompt,"Yes", "No","setCurrentCityStatus",
                        "checkCurrentCityStatus", new Object[]{true}, new Class[]{Boolean.class});
            }// end of else block
        }// end of if block
        else
        {
            // the location was already set
            // ensure that this variable contains realtime data
            locationSet = !Objects.requireNonNull( spf.getString( CURRENT_LOCATION_PREFERENCE,
                    Preference.DEFAULT_WEATHER_LOCATION ) ).equals( Preference.DEFAULT_WEATHER_LOCATION );

            if( locationSet )
            {
                currentWxLocation = spf.getString( CURRENT_LOCATION_PREFERENCE,
                        Preference.DEFAULT_WEATHER_LOCATION );

                setCityName( currentWxLocation );
            }// end of if block
        }// end of else block

        return locationSet;
    }// end of method locationCheck

    /***
     * Displays a message box prompt to the user
     *
     * @param message Additional {@code String} representing the missing asset
     */
    public void missingRequirementsMessage( String message )
    {
        // display a toast message to the user
        UtilityMethod.butteredToast( getAppContext(), message, 2, Toast.LENGTH_LONG );

        // log message
        UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, "Missing: " + message,
                m_TAG + "::missingAssetPrompt" );
    }// end of method missingAssetPrompt

    /**
     * This confirmation dialog gives the user an opportunity to proved
     * access keys to the weather providers that they intend to use.
     */
    private void noAccessPrivilegesStored()
    {
        String prompt = "The program will not run without access privileges!" +
                "\nDo you wish to add some now?";

        // check if the user wishes to provide some accounts for access
        // to weather services.
        responseDialog( "Add Access Privileges",
                prompt,"Yes", "No","showDataKeysDialog",
                "exitApplication", null, null );
    }// end of message noAccessPrivilegesStored()

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate()
    {
        super.onCreate();
        m_Context = getApplicationContext();
        thisClass = this;
        connectedToInternet = UtilityMethod.hasInternetConnection( getAppContext() );

        //create the service log to track calls to each provider
        createServiceCallLog();

        String invoker;

        // setup a broadcast receiver that will listen local app broadcasts
        registerAppBroadcastReceivers();

        // setup a broadcast receiver that will listen for system broadcasts specifically
        // network connectivity and system clock changes
        registerSystemBroadcastReceivers();

        spf = PreferenceManager.getDefaultSharedPreferences( this );
        currentWxLocation = spf.getString( CURRENT_LOCATION_PREFERENCE, Preference.DEFAULT_WEATHER_LOCATION );
        useGps = spf.getBoolean( USE_GPS_LOCATION_PREFERENCE, Preference.DEFAULT_USE_GPS );
        useMetric = spf.getBoolean( USE_METRIC_PREFERENCE, Preference.DEFAULT_USE_METRIC );
        widBackgroundColor = spf.getString( WIDGET_BACKGROUND_PREFERENCE, Preference.DEFAULT_WIDGET_BACKGROUND );
        LocationManager manager = (LocationManager) getSystemService( LOCATION_SERVICE );
        gpsRadioEnabled = manager.isProviderEnabled( LocationManager.GPS_PROVIDER );
        systemPreferences = new Preference();

        // check if this if the first time the app is being ran or data was cleared
        checkFirstRun();

        // retrieve user preferences
        storedPreferences = systemPreferences.getSavedPreferences();

        // check for weather data stored locally
        if( UtilityMethod.checkForStoredWeatherData( this ) )
        {
            localDateTime = new Date().toInstant().atZone(
                ZoneId.of( storedData.getLocation().getTimezone() ) ).toLocalDateTime();
        }// end of if block

        previousWeatherProvider.setLength( 0 );
        previousWeatherProvider.append( storedPreferences.getProvider() );

        iconSet = spf.getString( ICON_SET_PREFERENCE, Preference.DEFAULT_ICON_SET );

        // initialize the time of day variable
        if(currentSunsetTime.length() > 0 && timeOfDayToUse == null)
        {
            timeOfDayToUse = UtilityMethod.getAstronomyTimeOfDay();
        }// end of if block

        if( iconSet == null )
        {
            iconSet = DEFAULT_ICON_SET;
        }// end of if block

        // load any available access providers
        loadAccessProviders();

        String uiFont = spf.getString( UI_FONT, Preference.DEFAULT_UI_FONT );

        // load system/user selected font
        loadAppFonts( uiFont );

        // the system should have a consistent flow based on the selected widget background
        themeApp();

        setupApp();
    }// end of method onCreate

    /**
     * Setup the application for running
     */
    private void setupApp()
    {
        String invoker;

        if( geoNamesAccountLoaded )
        {
            // see if the user specified a location
            checkCurrentCityStatus();

            // check to see if backup weather data exists on there is not original data
            if( new File( this.getFileStreamPath( WeatherLionApplication.WEATHER_DATA_BACKUP ).toString() ).exists()
                    && UtilityMethod.isFileEmpty( this, WeatherLionApplication.WEATHER_DATA_BACKUP ) )
            {
                UtilityMethod.removeFile( WeatherLionApplication.WEATHER_DATA_BACKUP );
            }// end of if block
            else if( new File( this.getFileStreamPath( WeatherLionApplication.WEATHER_DATA_BACKUP ).toString() ).exists()
                    && !( new File( this.getFileStreamPath( WeatherLionApplication.WEATHER_DATA_XML ).toString() ).exists() ) )
            {
                // if a backup file is present but, the original is not present, restore from the backup
                UtilityMethod.copyFile( new File( this.getFileStreamPath( WeatherLionApplication.WEATHER_DATA_BACKUP ).toString() ),
                        new File( this.getFileStreamPath( WeatherLionApplication.WEATHER_DATA_XML ).toString() ),
                        m_TAG + "::onCreate" );
            }// end of else if block

            // if the user has already selected a location
            if( locationSet )
            {
                // check if any weather data exists locally
                if( UtilityMethod.checkForStoredWeatherData( getAppContext() ) )
                {
                    // run the weather service and call the method that loads the previous weather data
                    actionWeatherService( UNIT_NOT_CHANGED,
                            WidgetUpdateService.LOAD_PREVIOUS_WEATHER );

                    UtilityMethod.refreshRequestedBySystem = true;
                    UtilityMethod.refreshRequestedByUser = false;

                    // perform a weather update
                    refreshWeather( m_TAG + "::onCreate" );

                }// end of if block
                else
                {
                    // generate some stored data by calling the provider for the
                    // data using the user's stored settings
                    if( storedPreferences.getLocation() != null )
                    {
                        restoringWeatherData = true;
                        invoker = m_TAG + "::onCreate()";

                        UtilityMethod.refreshRequestedBySystem = true;
                        UtilityMethod.refreshRequestedByUser = false;
                        refreshWeather( invoker );
                    }// end of if block
                }// end od else block
            }// end of if block
        }// end of if block

        // check if this the first time that the program is being run i.e new installation
        if( firstRun )
        {
            // set a flag in the stored preferences indicating that the first run has already
            // been completed.
            storedPreferences.setFirstRun( false );
        }// end of if block
    }// end of method setupApp

    /**
     * Theme the application based on the user/system selected background
     */
    private void themeApp()
    {
        if( widBackgroundColor != null )
        {
            switch( widBackgroundColor.toLowerCase() )
            {
                case AQUA_THEME:
                    systemColor = Color.valueOf( getColor( R.color.aqua ) );
                    systemButtonDrawable = getDrawable( R.drawable.wl_aqua_rounded_btn_bg );
                    widgetBackgroundDrawable = getDrawable( R.drawable.wl_aqua_bg_large );
                    setTheme( R.style.AquaThemeDark );

                    break;
                case FROSTY_THEME:
                    systemColor = Color.valueOf( getColor( R.color.frosty ) );
                    systemButtonDrawable = getDrawable( R.drawable.wl_frosty_rounded_btn_bg );
                    widgetBackgroundDrawable = getDrawable( R.drawable.wl_frosty_bg_large );
                    setTheme( R.style.FrostyThemeDark );

                    break;
                case RABALAC_THEME:
                    systemColor = Color.valueOf( getColor( R.color.rabalac ) );
                    systemButtonDrawable = getDrawable( R.drawable.wl_rabalac_rounded_btn_bg );
                    widgetBackgroundDrawable = getDrawable( R.drawable.wl_rabalac_bg_large );
                    setTheme( R.style.RabalacThemeDark );

                    break;
                default:
                    systemColor = Color.valueOf( getColor( R.color.lion ) );
                    systemButtonDrawable = getDrawable( R.drawable.wl_lion_rounded_btn_bg );
                    widgetBackgroundDrawable = getDrawable( R.drawable.wl_lion_bg_large );
                    setTheme( R.style.LionThemeDark );
                    break;
            }// end of switch block
        }// end of if block
    }// end of method themeApp

    /**
     * Load the custom fonts that will be used in the application
     *
     * @param uiFont    The font to be used throughout the UI
     */
    private void loadAppFonts( String uiFont )
    {
        helveticaNeue = Typeface.createFromAsset( getAssets(), "fonts/helvetica_neue_lt_pro.otf" );
        productsSans = Typeface.createFromAsset( getAssets(), "fonts/product_sans.ttf" );
        samsungSans = Typeface.createFromAsset( getAssets(), "fonts/samsung_sans_regular.ttf" );

        fonts = new LinkedHashMap<>();
        fonts.put( "Helvetica Neue", helveticaNeue );
        fonts.put( "Product Sans", productsSans );
        fonts.put( "Samsung Sans", samsungSans );

        if( uiFont != null )
        {
            switch( uiFont )
            {
                case SYSTEM_FONT:
                    currentTypeface = null;
                    break;
                case HELVETICA_FONT:
                    currentTypeface = helveticaNeue;
                    break;
                case PRODUCT_SANS_FONT:
                    currentTypeface = productsSans;
                    break;
                case SAMSUNG_SANS_FONT:
                    currentTypeface = samsungSans;
                    break;
            }// end of switch block
        }// end of if block
    }// end of method loadAppFonts

    /**
     * Register to receive broadcast sent out by the system
     */
    private void registerSystemBroadcastReceivers()
    {
        IntentFilter systemFilter = new IntentFilter();
        systemFilter.addAction( ConnectivityManager.CONNECTIVITY_ACTION );
        systemFilter.addAction( AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED );
        this.registerReceiver(m_SystemBroadcastReceiver, systemFilter );
    }// end of method registerSystemBroadcastReceivers

    /**
     * Register to receive broadcast sent out within the application
     */
    private void registerAppBroadcastReceivers()
    {
        IntentFilter appFilter = new IntentFilter();
        appFilter.addAction( GeoLocationService.GEO_LOCATION_SERVICE_MESSAGE );
        appFilter.addAction( WeatherLionMain.KEY_UPDATE_MESSAGE );
        appFilter.addAction( WidgetUpdateService.WEATHER_XML_SERVICE_MESSAGE );
        appFilter.addAction( WeatherDataXMLService.WEATHER_XML_STORAGE_MESSAGE );
        LocalBroadcastManager.getInstance( this ).registerReceiver(m_AppBroadcastReceiver,
                appFilter );
    }// end of method registerAppBroadcastReceivers

    /**
     * Invokes a weather data refresh
     */
    synchronized public static void refreshWeather( String invoker )
    {
        if( UtilityMethod.hasInternetConnection( getAppContext() ) )
        {
            // do not execute back-to-back requests
            if( UtilityMethod.updateRequired(m_Context) )
            {
                UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
            "Update requested by " + invoker, m_TAG + "::refreshWeather" );
                actionWeatherService( UNIT_NOT_CHANGED, null );
            }// end of if block
        }// end of if block
    }// end of method synchronized refreshWeather

    /**
     * Display a dialog eliciting a response from the user
     *
     * @param prompt    The prompt to be displayed to the user
     * @param title     The title of the dialog box
     * @param posResponse   The text to be displayed on the positive response button
     * @param negResponse   The text to be displayed on the negative response button
     * @param positiveAction A string representing a method that should be called after the user click's on the positive button
     * @param negativeAction A string representing a method that should be called after the user click's on the negative button
     */
    private void responseDialog( String title, String prompt, String posResponse,
                                 String negResponse, final String positiveAction,
                                 final String negativeAction, final Object[] params, final Class[] paramClassTypes )
    {
        final AlertDialog response = new AlertDialog.Builder( getAppContext() ).create();
        View dialogView = View.inflate( getAppContext(), R.layout.wl_response_dialog, null );
        response.setView( dialogView );
        response.setCancelable( false );

        UtilityMethod.loadCustomFont( (LinearLayout) dialogView.findViewById( R.id.llResponseDialog ) );

        // Initialize the view objects
        RelativeLayout rlTitleBar = dialogView.findViewById( R.id.rlDialogTitleBar );
        rlTitleBar.setBackgroundColor( systemColor.toArgb() );

        TextView txvDialogTitle = dialogView.findViewById( R.id.txvDialogTitle );
        TextView txvDialogMessage = dialogView.findViewById( R.id.txvAcknowledgements);

        Button btnPositive = dialogView.findViewById( R.id.btnPositive );
        btnPositive.setBackground( systemButtonDrawable );
        Button btnNegative = dialogView.findViewById( R.id.btnNegative );
        btnNegative.setBackground( systemButtonDrawable );

        txvDialogTitle.setText( title );
        txvDialogTitle.setTypeface( currentTypeface );

        txvDialogMessage.setText( prompt );
        txvDialogMessage.setTypeface( currentTypeface );

        btnPositive.setText( posResponse );
        btnPositive.setTypeface( currentTypeface );

        btnNegative.setText( negResponse );
        btnNegative.setTypeface( currentTypeface );

        btnPositive.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if( params == null || params.length == 0 )
                {
                    callMethodByName( this, positiveAction,
                        null, null,
                            "btnPositive.setOnClickListener" );
                }// end of if block
                else
                {
                    callMethodByName( this,
                        positiveAction, paramClassTypes, params,
                            "btnPositive.setOnClickListener" );
                }// end of else block

                response.dismiss();
            }
        });

        btnNegative.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if( negativeAction != null )
                {
                    callMethodByName( this, negativeAction,
                    null, null,
                        "btnNegative.setOnClickListener" );
                }// end of if block

                response.dismiss();
            }
        });

        response.show();
    }// end of method responseDialog

    /**
     * Sent a status notification to the notification bar
     */
    private void sendWeatherNotification()
    {
        RemoteViews notificationLayout = new RemoteViews( getPackageName(),
                R.layout.wl_basic_weather_notification  );
        RemoteViews notificationLayoutExpanded = new RemoteViews( getPackageName(),
                R.layout.wl_hourly_weather_notification_parent );

        NotificationManager notificationManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE );
        String channelId = "weatherLion";
        String description = "WeatherLion Notification";
        int importance = NotificationManager.IMPORTANCE_LOW;

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O )
        {
            //@SuppressLint("WrongConstant")
            NotificationChannel notificationChannel = new NotificationChannel( channelId,
                description, importance );
            // Configure the notification channel.
            notificationChannel.setShowBadge( false );
            notificationChannel.setSound( null, null );
            notificationChannel.setDescription( description );
            notificationChannel.enableLights( true );
            notificationChannel.setLightColor( systemColor.toArgb() );
            //notificationChannel.setVibrationPattern( new long[]{ 0, 1000, 500, 1000 } );
            notificationChannel.enableVibration( false );
            notificationManager.createNotificationChannel( notificationChannel );
        }// end of if block

        storedData = lastDataReceived.getWeatherData();

        notificationLayout.setTextViewText( R.id.txvCityName,
                storedData.getLocation().getCity() );
        notificationLayoutExpanded.setTextViewText( R.id.txvCityName,
                storedData.getLocation().getCity() );

        String currentConditionIcon = UtilityMethod.getConditionIcon(
            new StringBuilder(
                        storedData.getCurrent().getCondition() ),
                null );

        String fileName;

        if( storedPreferences.getIconSet().equalsIgnoreCase( "mono" ) )
        {
            fileName = String.format( "weather_images/%s/weather_notif_%s",
                    iconSet, currentConditionIcon );
        }// end of if block
        else
        {
            fileName = String.format( "weather_images/%s/weather_%s",
                    iconSet, currentConditionIcon );
        }// end of else block

        loadWeatherIcon( notificationLayout, R.id.weather_icon, fileName );
        loadWeatherIcon( notificationLayoutExpanded, R.id.weather_icon, fileName );

        String temps = String.format( "%s%s / %s%s",
                storedData.getCurrent().getHighTemperature(),
                DEGREES,
                storedData.getCurrent().getLowTemperature(),
                DEGREES );

        notificationLayout.setTextViewText( R.id.txvCurrentTemp,
                String.format(
                        "%s%s", storedData.getCurrent().getTemperature(), DEGREES ) );
        notificationLayoutExpanded.setTextViewText( R.id.txvCurrentTemp,
                String.format(
                        "%s%s", storedData.getCurrent().getTemperature(), DEGREES ) );

        notificationLayout.setTextViewText( R.id.txvCurrentReadings,temps );
        notificationLayoutExpanded.setTextViewText (R.id.txvCurrentReadings, temps );

        notificationLayout.setTextViewText( R.id.notification_humidity,
                String.format( "%s %%", storedData.getAtmosphere().getHumidity() ) );
        notificationLayoutExpanded.setTextViewText( R.id.notification_humidity,
                String.format( "%s %%", storedData.getAtmosphere().getHumidity() ) );

        String unit = useMetric ? "kph" : "mph";
        notificationLayout.setTextViewText(R.id.notification_wind,
                String.format( "%s %s",
                        Math.round( storedData.getWind().getWindSpeed() ),
                        unit ) );
        notificationLayoutExpanded.setTextViewText( R.id.notification_wind,
                String.format( "%s %s",
                        Math.round( storedData.getWind().getWindSpeed() ),
                        unit ) );

        Intent intent = new Intent( this, WeatherLionMain.class );

        PendingIntent pIntent = PendingIntent.getActivity( this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT );
        String tickerText = String.format( "Current temperature in %s is %s%s and %s",
                storedData.getLocation().getCity(), storedData.getCurrent().getTemperature(),
                DEGREES, storedData.getCurrent().getCondition() );

        // Apply the layouts to the notification
        NotificationCompat.Builder customNotificationBuilder = new NotificationCompat.Builder(m_Context,
                channelId )
                .setWhen( System.currentTimeMillis() )
                .setSmallIcon( R.drawable.wl_notification_icon )
                .setBadgeIconType( 1 )
                .setOngoing( false )
                .setTicker( tickerText )
                .setColor( systemColor.toArgb() )
                .setStyle( new NotificationCompat.DecoratedCustomViewStyle() )
                .setContentIntent( pIntent )
                .setCustomContentView( notificationLayout )
                .setAutoCancel( true )
                .setPriority( NotificationCompat.PRIORITY_DEFAULT )
                .setVisibility( NotificationCompat.VISIBILITY_PUBLIC );

        // if an hourly forecast is present then show it
        if( storedData.getHourlyForecast().size() > 0 )
        {
            for ( int i = 0; i < storedData.getHourlyForecast().size(); i++ )
            {
                LastWeatherData.WeatherData.HourlyForecast.HourForecast wxHourForecast =
                        storedData.getHourlyForecast().get( i );
                String forecastTime = null;

                RemoteViews notificationHourly = new RemoteViews( getPackageName(),
                        R.layout.wl_hourly_weather_notification_child );

                String today = new SimpleDateFormat( "MM/dd/yyyy",
                        Locale.ENGLISH ).format( new Date() );

                String hourForecast = String.format( "%s", wxHourForecast.getTime() );

                SimpleDateFormat sdf = new SimpleDateFormat( "EEE, MMM dd, yyyy HH:mm",
                        Locale.ENGLISH );

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                        "EEE, MMM dd, yyyy HH:mm" );

                String forecastHour = LocalDateTime.parse( wxHourForecast.getTime(),
                        formatter ).format( DateTimeFormatter.ofPattern(
                        "h a" ) );

                notificationHourly.setTextViewText( R.id.notification_hourly_weather_time,
                        forecastHour );

                Date onTime = null;

                try
                {
                    onTime = sdf.parse( hourForecast );
                } // end of try block
                catch ( ParseException e )
                {
                    UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE , e.getMessage(),
                            m_TAG + "::sendWeatherNotification [line: " +
                                    e.getStackTrace()[ 1 ].getLineNumber() + "]" );
                }// end of catch block

                // Load current forecast condition weather image
                StringBuilder fCondition = new StringBuilder(
                        UtilityMethod.validateCondition(
                                wxHourForecast.getCondition() ) );
                String fConditionIcon = UtilityMethod.getConditionIcon( fCondition, onTime );
                String fIconImage;

                if( storedPreferences.getIconSet().equalsIgnoreCase( "mono" ) )
                {
                    fIconImage = String.format( "weather_images/%s/weather_notif_%s",
                            iconSet, fConditionIcon );
                }// end of if block
                else
                {
                    fIconImage = String.format( "weather_images/%s/weather_%s",
                            iconSet, fConditionIcon );
                }// end of else block

                loadWeatherIcon( notificationHourly, R.id.notification_hourly_weather_icon,
                        fIconImage );

                notificationLayoutExpanded.addView( R.id.view_container, notificationHourly );

                if( i == 4 )
                {
                    break;
                }// end of if block
            }// end of for loop

            customNotificationBuilder.setCustomBigContentView(notificationLayoutExpanded);
        }// end of if block

        // Create Notification instance.
        Notification customNotification = customNotificationBuilder.build();
        notificationManager.notify( 0, customNotification );
    }// end of method sendWeatherNotification

    /**
     * Display a dialog with a specific message
     *
     * @param message   The message to be displayed in the alert dialog
     * @param title   The alert dialog title
     */
    @SuppressWarnings({"SameParameterValue"})
    private void showMessageDialog( String messageType, String message, String title,
                                    final String methodToCall, final Object[] params, final Class[] paramClassTypes )
    {
        final View messageDialogView = View.inflate( this, R.layout.wl_message_dialog, null );
        final AlertDialog messageDialog = new AlertDialog.Builder( this ).create();
        TextView txvTitle = messageDialogView.findViewById( R.id.txvDialogTitle );
        TextView txvMessage = messageDialogView.findViewById( R.id.txvAcknowledgements);

        txvTitle.setText( title );
        txvMessage.setText( message );

        RelativeLayout rlTitleBar = messageDialogView.findViewById( R.id.rlDialogTitleBar );
        GradientDrawable bgShape = (GradientDrawable) rlTitleBar.getBackground().getCurrent();
        bgShape.setColor( WeatherLionApplication.systemColor.toArgb() );

        Button btnOk = messageDialogView.findViewById( R.id.btnOk );
        btnOk.setBackground( WeatherLionApplication.systemButtonDrawable );

        btnOk.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                messageDialog.dismiss();
            }
        });

        messageDialog.setView( messageDialogView );
        Objects.requireNonNull( messageDialog.getWindow() ).setBackgroundDrawable(
                new ColorDrawable( Color.TRANSPARENT ) );
        UtilityMethod.loadCustomFont( (RelativeLayout) messageDialogView.findViewById(
                R.id.rlMessageDialog ) );

        if( messageType != null && messageType.equalsIgnoreCase( "html" ) )
        {
            txvMessage.setText( HtmlCompat.fromHtml( message, 0 ) );
        }// end of if block
        else
        {
            txvMessage.setText( message );
        }// end of else block

        btnOk.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                if( params == null || params.length == 0 )
                {
                    callMethodByName( this, methodToCall,null,
                null, "btnOk.setOnClickListener" );
                }// end of if block
                else
                {
                    callMethodByName( this, methodToCall, paramClassTypes,
                        params, "btnOk.setOnClickListener" );
                }// end of else block

                messageDialog.dismiss();
            }
        });

        messageDialog.show();

        // adjust the layout after the window is displayed
        Window dialogWindow = messageDialog.getWindow();
        dialogWindow.getAttributes().windowAnimations = R.style.ZoomAnimation;
        UtilityMethod.zoomInView( messageDialogView );
        dialogWindow.setLayout( CustomPreferenceGrid.DEFAULT_DIALOG_WIDTH,
                ViewGroup.LayoutParams.WRAP_CONTENT );
        dialogWindow.setGravity( Gravity.CENTER );
    }// end of method showMessageDialog

    /**
     * Show the preferences activity
     */
    private void showPreferenceActivity()
    {
        // Launch the settings activity
        Intent settingsIntent = new Intent( this, PrefsActivity.class );
        settingsIntent.putExtra( WeatherLionMain.LION_LOCATION_PAYLOAD, locationSet );
        startActivity( settingsIntent );
    }// end of method showPreferenceActivity

    /**
     * Return the program's current context.
     *
     * @return  The current program context
     */
    public static Context getAppContext()
    {
        return m_Context;
    }// end of method  getAppContext

    /**
     * Set the details for the user's location using the json data returned from the web service
     *
     * @param cityJSON  The data returned from the web service in JSON format.
     */
    private void setCityName( String cityJSON )
    {
        currentCityData = new CityData();

        String city;
        String countryCode;
        String countryName;
        String regionCode;
        String regionName;
        String currentLocation = null;

        try
        {
            Object json = new JSONTokener( cityJSON ).nextValue();

            // Check if a JSON was returned from the web service
            if ( json instanceof JSONObject)
            {
                // Get the full HTTP Data as JSONObject
                JSONObject geoNamesJSON = new JSONObject( cityJSON );
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
                currentLocation = cityJSON;
            }// end of else block
        }// end of try block
        catch ( JSONException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    m_TAG + "::setCityName [line: " + UtilityMethod.getExceptionLineNumber( e ) + "]" );
        }// end of catch block

        // combine the city and the region as the current location
        final String[] location;
        location = ( currentLocation != null ) ? currentLocation.split( "," ) : new String[ 0 ];

        if( location.length > 0 )
        {
            // countries who have a region such as a state or municipality
            if( location.length > 2 )
            {
                systemLocation = location[ 0 ].trim() + ", " + location[ 1 ].trim();

                if( location[ 2 ].trim().equalsIgnoreCase( "US" ) ||
                        location[ 2 ].trim().equalsIgnoreCase( "United States" ) )
                {
                    // if the state name has a length of 2 then nothing needs to be done
                    if( location[ 1 ].trim().length() > 2 )
                    {
                        systemLocation = location[ 0 ].trim() + ", " +
                                UtilityMethod.usStatesByName.get( location[ 0 ].trim() );
                    }// end of if block
                    else
                    {
                        systemLocation = location[ 0 ].trim() + ", " + location[ 1 ].trim();
                    }// end of else block
                }// end of if block
                else
                {
                    systemLocation = location[ 0 ].trim() + ", " + location[ 1 ].trim();
                }// end of else block
            }// end of if block
            else if( location.length > 1 )
            {
                if( location[ 1 ].trim().equalsIgnoreCase( "US" ) ||
                        location[ 1 ].trim().equalsIgnoreCase( "United States" ) )
                {
                    // if the state name has a length of 2 then nothing needs to be done
                    if( location[ 1 ].trim().length() > 2 )
                    {
                        systemLocation = location[ 0 ].trim() + ", " +
                                UtilityMethod.usStatesByName.get( location[ 0 ].trim() );
                    }// end of if block
                    else
                    {
                        systemLocation = location[ 0 ].trim() + ", " + location[ 1 ].trim();
                    }// end of else block
                }// end of if block
                else
                {
                    systemLocation = location[ 0 ].trim() + ", " + location[ 1 ].trim();
                }// end of else block
            }// end of else if block
        }// end of if block

        // if the user requires the current detected city location to be used as default
        if( storedPreferences.getUseSystemLocation() )
        {
            if( systemLocation != null )
            {
                // use the detected city location as the default
                storedPreferences.setLocation( systemLocation );

                if( !storedPreferences.getLocation().equals( systemLocation ) )
                {
                    // update the preferences file
                    systemPreferences.setPrefValues( CURRENT_LOCATION_PREFERENCE, systemLocation );

                    // save the city to the local WorldCites database
                    UtilityMethod.addCityToDatabase(
                            currentCityData.getCityName(),
                            currentCityData.getCountryName(),
                            currentCityData.getCountryCode(),
                            currentCityData.getRegionName(),
                            currentCityData.getRegionCode(),
                            currentCityData.getTimeZone(),
                            currentCityData.getLatitude(),
                            currentCityData.getLongitude() );

                    JSONHelper.exportCityToJSON( currentCityData );
                    XMLHelper.exportCityDataToXML( currentCityData );
                }// end of if block

                init();
            }// end of if block
            else
            {
                String prompt = "The program was unable to obtain your system's location."
                        + "\nYour location will have to be set manually using the preferences activity.";

                showMessageDialog( null, prompt, PROGRAM_NAME + " - Location Setup",
                        "showPreferenceActivity", null, null );
            }// end of else block
        }// end of if block
    }// end of method setCityName

    /**
     * Load the applicable weather icon image
     * @param resID The Id of the resource
     * @param imageFile  The file name for the icon
     */
    private void loadWeatherIcon( @NonNull RemoteViews widget, int resID, String imageFile )
    {
        try( InputStream is = this.getAssets().open( imageFile ) )
        {
            Bitmap bmp = BitmapFactory.decodeStream( is );
            widget.setImageViewBitmap( resID, bmp );
        }// end of try block
        catch ( IOException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE,"Weather icon " +
                    imageFile + " could not be loaded!", m_TAG + "::loadWeatherIcon [line: " +
                    e.getStackTrace()[1].getLineNumber() + "]" );
        }// end of catch block
    }// end of method loadWeatherIcon

    /**
     * This broadcast receiver listens for local broadcasts within the program
     */
    private class AppBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive( Context context, @NonNull Intent intent )
        {
            final String action = Objects.requireNonNull( intent.getAction() );

            switch( action )
            {
                case GeoLocationService.GEO_LOCATION_SERVICE_MESSAGE:
                    String cityMessage = intent.getStringExtra( GeoLocationService.GEO_LOCATION_SERVICE_PAYLOAD );
                    setCityName( cityMessage );
                    locationSet = storedPreferences.getLocation() != null;

                    break;
                case WeatherLionMain.KEY_UPDATE_MESSAGE:
                    String methodToCall = intent.getStringExtra( WeatherLionMain.KEY_UPDATE_PAYLOAD );

                    UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                            "Reloading access keys...", m_TAG + "::keyUpdateReceiver" );

                    callMethodByName( this, methodToCall,
            null, null, "AppBroadcastReceiver::onReceive" );

                    // Load only the providers who have access keys assigned to them
                    ArrayList<String> wxOnly = webAccessGranted;

                    Collections.sort( wxOnly );    // sort the list

                    // GeoNames is not a weather provider so it cannot be select here
                    wxOnly.remove( GEO_NAMES );

                    authorizedProviders = wxOnly.toArray( new String[ 0 ] );

                    break;
                case WidgetUpdateService.WEATHER_XML_SERVICE_MESSAGE:
                    // start xml storage service
                    String xmlJSON = intent.getStringExtra( WidgetUpdateService.WEATHER_XML_SERVICE_PAYLOAD );
                    Intent weatherXMLIntent = new Intent( WeatherLionApplication.this,
                            WeatherDataXMLService.class );
                    weatherXMLIntent.putExtra( WidgetUpdateService.WEATHER_XML_SERVICE_PAYLOAD, xmlJSON );
                    WeatherDataXMLService.enqueueWork( context, weatherXMLIntent );

                    break;
                case WeatherDataXMLService.WEATHER_XML_STORAGE_MESSAGE:
                    if( new File( WeatherLionApplication.this.getFileStreamPath( WEATHER_DATA_XML ).toString() ).exists() )
                    {
                        // refresh the xml data stored after the last update
                        lastDataReceived = LastWeatherDataXmlParser.parseXmlData(
                            UtilityMethod.readAll(
                                context.getFileStreamPath( WEATHER_DATA_XML ).toString() )
                                    .replaceAll( "\t", "" ).trim() );

                        storedData = lastDataReceived.getWeatherData();
                        DateFormat df = new SimpleDateFormat( "EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);

                        try
                        {
                            UtilityMethod.lastUpdated = df.parse(
                                    storedData.getProvider().getDate() );
                        }// end of try block
                        catch ( ParseException e )
                        {
                            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, "Unable to parse last weather data date.",
                                    m_TAG + "::onCreate [line: " +
                                            e.getStackTrace()[1].getLineNumber()+ "]" );
                        }// end of catch block
                        catch ( Exception e )
                        {
                            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, "Error occurred.",
                                    m_TAG + "::onCreate [line: " +
                                            e.getStackTrace()[1].getLineNumber()+ "]" );
                        }// end of catch block

                        currentSunriseTime = new StringBuilder(
                                storedData.getAstronomy().getSunrise() );
                        currentSunsetTime = new StringBuilder(
                                storedData.getAstronomy().getSunset() );

                        // send a heads up notification
                        sendWeatherNotification();
                    }// end of if block

                    break;
            }// end of switch block
        }// end of method onReceive
    }// end of class AppBroadcastReceiver

    /**
     * Receives broadcasts sent by the operating system.
     */
    private static class SystemBroadcastReceiver extends BroadcastReceiver
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onReceive( Context context, @NonNull Intent intent )
        {
            final String action = Objects.requireNonNull( intent.getAction() );
            String invoker = "WeatherLionApplication::SystemBroadcastReceiver::onReceive";
            Bundle extras;

            switch ( action )
            {
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    // if the last weather update is null then the program has just been launched
                    // an the system will always imply that the connection state has changed.
                    if( UtilityMethod.lastUpdated != null )
                    {
                        UtilityMethod.refreshRequestedBySystem = true;
                        UtilityMethod.refreshRequestedByUser = false;

                        WeatherLionApplication.refreshWeather( invoker );
                    }// end of if block

                    actionWeatherService( UNIT_NOT_CHANGED, WidgetUpdateService.UPDATE_CONNECTIVITY );

                    break;

                case AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED:
                    actionWeatherService( UNIT_NOT_CHANGED, WidgetUpdateService.LOAD_USER_ALARMS );

                    break;
            }// end of switch block
        }// end of method onReceive
    }// end of class SystemBroadcastReceiver
}// end of class WeatherLionApplication