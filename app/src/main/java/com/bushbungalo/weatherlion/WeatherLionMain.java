package com.bushbungalo.weatherlion;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.text.HtmlCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bushbungalo.weatherlion.database.DBHelper;
import com.bushbungalo.weatherlion.database.WeatherAccess;
import com.bushbungalo.weatherlion.model.LastWeatherData;
import com.bushbungalo.weatherlion.services.WeatherDataXMLService;
import com.bushbungalo.weatherlion.utils.DividerItemDecoration;
import com.bushbungalo.weatherlion.utils.LastWeatherDataXmlParser;
import com.bushbungalo.weatherlion.utils.UtilityMethod;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * @author Paul O. Patterson
 * @version     1.1
 * @since       1.0
 *
 * <p>
 * This class is responsible for the main execution of the program and to ensure
 * that all required components and access is available before the program's launch.
 * </p>
 *
 * <br />
 * <b style="margin-left:-40px">Date Created:</b>
 * <br />
 * 11/21/17
 */
@SuppressWarnings({ "unused", "unchecked", "SameParameterValue"})
public class WeatherLionMain extends AppCompatActivity
{
    public static final String LION_MAIN_PAYLOAD = "WeatherLionMainPayload";

    public static final String KEY_UPDATE_PAYLOAD = "KeyUpdatePayload";
    public static final String KEY_UPDATE_MESSAGE = "loadAccessProviders";

    private final static String TAG = "WeatherLionMain";
    public Context mContext;    

    // Data Access
    private EditText edtKeyName;
    private EditText pwdKeyValue;
    private Spinner spnAccessProvider;
    private Spinner spnProviderKeys;
    private RelativeLayout rlKeyNameParent;

    private int answer;

    private AlertDialog keyEntryDialog;
    private View keysDialogView;

    private static StringBuilder currentCity = new StringBuilder();
    private static StringBuilder currentCountry = new StringBuilder();
    private static StringBuilder currentTemp = new StringBuilder();
    private static StringBuilder currentFeelsLikeTemp = new StringBuilder();
    private static StringBuilder currentWindSpeed = new StringBuilder();
    private static StringBuilder currentWindDirection = new StringBuilder();
    //private static StringBuilder currentHumidity = new StringBuilder();
    public  static StringBuilder currentCondition = new StringBuilder();
    private static StringBuilder currentHigh = new StringBuilder();
    private static StringBuilder currentLow = new StringBuilder();

    private static ArrayAdapter requiredKeysAdapter;

    /**
     * Refresh the main activity once new data has been stored
     */
    private BroadcastReceiver xmlStorageBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            // refresh the xml data stored after the last update
            WeatherLionApplication.lastDataReceived = LastWeatherDataXmlParser.parseXmlData(
                UtilityMethod.readAll(
                    context.getFileStreamPath( WeatherLionApplication.WEATHER_DATA_XML ).toString() )
                        .replaceAll( "\t", "" ).trim() );

            WeatherLionApplication.storedData = WeatherLionApplication.lastDataReceived.getWeatherData();
            DateFormat df = new SimpleDateFormat( "EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);

            try
            {
                UtilityMethod.lastUpdated = df.parse(
                        WeatherLionApplication.storedData.getProvider().getDate() );
            }// end of try block
            catch ( ParseException e )
            {
                UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, "Unable to parse last weather data date.",
                        TAG + "::onCreate [line: " +
                                e.getStackTrace()[1].getLineNumber()+ "]" );
            }// end of catch block

            WeatherLionApplication.currentSunriseTime = new StringBuilder(
                    WeatherLionApplication.storedData.getAstronomy().getSunrise() );
            WeatherLionApplication.currentSunsetTime = new StringBuilder(
                    WeatherLionApplication.storedData.getAstronomy().getSunset() );

            loadMainActivityWeather();
        }// end of method onReceive
    };

    /**
     * Method to be called after the required data accesses have be obtained.
     * This method may also be called using reflection and might appear to be unused.
     * Check that it is actually not being called using reflection before removal.
     */
    private static void accessLoaded()
    {
        // Load only the providers who have access keys assigned to them
        ArrayList< String > wxOnly = WeatherLionApplication.webAccessGranted;

        Collections.sort( wxOnly );	// sort the list

        // GeoNames is not a weather provider so it cannot be select here
        wxOnly.remove( "GeoNames" );

        WeatherLionApplication.authorizedProviders = wxOnly.toArray( new String[ 0 ] );

        // check for an Internet connection or previous weather data stored local
        if( !WeatherLionApplication.connectedToInternet &&
                !UtilityMethod.getInternalFile( WeatherLionApplication.WEATHER_DATA_XML ) )
        {
            UtilityMethod.butteredToast( WeatherLionApplication.getAppContext(),
                    "The program will not run without a working internet connection or data " +
                            "that was previously stored locally.",2, Toast.LENGTH_LONG );

            //finish();	// terminate the program
        }// end of if block
        else if( WeatherLionApplication.connectedToInternet )
        {
            if( WeatherLionApplication.useGps && WeatherLionApplication.gpsRadioEnabled )
            {
                // obtain the current city of the connected Internet service
                UtilityMethod.getGPSCityLocation( false );
            }// end of if block

        }// end of else if block

        // find the widget view and update it's fonts face
        View widget = View.inflate( WeatherLionApplication.getAppContext(), R.layout.wl_large_weather_widget_activity,null );
        UtilityMethod.loadCustomFont( (RelativeLayout) widget.findViewById( R.id.rlWidgetBody ) );
    }// end of method accessLoaded

    /**
     * Send out a broadcast that the keys data must be reloaded
     */
    private void broadcastKeyUpdate()
    {
        Intent messageIntent = new Intent( KEY_UPDATE_MESSAGE );
        messageIntent.putExtra( KEY_UPDATE_PAYLOAD, KEY_UPDATE_MESSAGE );
        LocalBroadcastManager manager =
                LocalBroadcastManager.getInstance( getApplicationContext() );
        manager.sendBroadcast( messageIntent );
    }// end of method broadcastKeyUpdate

     /** This method uses refection to call a method using a {@code String} value representing the
     * method name.
     *
     * @param obj   The {@code Class} {@code Object} which contains the method.
     * @param methodName    A {@code String} representing the name of the method to be called.
     * @param parameterTypes    An array representing the param type example new Class[]{String.class} or null can be passed.
     * @param paramValues    An array representing the param value example new Object[]{"GeoNames"} or null can be passed.
     */
    public static void callMethodByName( Object obj, String methodName, Class[] parameterTypes, Object[] paramValues )
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

    /** Checks to see if the user has loaded credentials to be used with the GeoNames web service.
     *
     * @return  A value of true if the credentials are in place or false if they are not.
     */
    private boolean checkGeoAccess()
    {
        boolean hasAccessCredentials = false;

        if( WeatherLionApplication.webAccessGranted.size() >= 1 &&
                !WeatherLionApplication.geoNamesAccountLoaded )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE,
                    "GeoNames user name not found!", TAG + "::loadAccessProviders" );

            // confirm that user has a GeoNames account and want's to store it
            String prompt = "This program requires a geonames username\n" +
                    "which was not stored in the database.\nIT IS FREE!" +
                    "\nDo you wish to add it now?";

            responseDialog( WeatherLionApplication.PROGRAM_NAME + " - Missing Key",
                    prompt,"Yes", "No","showDataKeysDialog",
                    "lackPrivilegesMessage", new Object[]{ "GeoNames" },
                    new Class[]{ String.class } );
        }// end of else if block
        else
        {
            hasAccessCredentials = true;
        }// end of else block

        return hasAccessCredentials;
    }// end of method checkGeoAccess

    public void doGlimpseRotation(View v )
    {
        Animation rotateAnim = AnimationUtils.loadAnimation( this, R.anim.single_blade_rotation );

        if( v.getAnimation() == null )
        {
            //v.setAnimation( rotateAnim );
            v.startAnimation( rotateAnim );
        }
    }// end of method doGlimpseRotation

    /**
     * Determines weather or not a specific database contains a specific table
     *
     * @param tableName The table that is in question
     *
     * @return        	 The {@code int} value of 0 if not found of 1 if the table is found
     */
    private int getRowCount( String dbName, String tableName )
    {
        SQLiteOpenHelper dbHelper = new DBHelper( mContext, dbName );
        SQLiteDatabase weatherAccessDB = dbHelper.getReadableDatabase();
        int found = 0;

        try
        {
            Cursor cursor = weatherAccessDB.query( true, tableName,
                    new String[]{"Count(*)"}, null, null,
                    null, null, null, null, null );

            while ( cursor.moveToNext() )
            {
                found = cursor.getInt(0);
            }// end of while loop

            cursor.close();
            weatherAccessDB.close();
        }// end of try block
        catch( Exception e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    TAG + "::getSiteKeyFromDatabase [line: " +
                            UtilityMethod.getExceptionLineNumber( e )  + "]" );
        }// end of catch block

        return found;
    }// end of method getRowCount

    private void lackPrivilegesMessage()
    {
        UtilityMethod.missingRequirementsPrompt( "Insufficient Access Privileges" );
        WeatherLionApplication.exitApplication();
    }// end of method lackPrivilegesMessage

    /**
     * Loads the applicable weather background image for the layout
     *
     * @param rl The {@code RelativeLayout} in which the image will be displayed
     * @param imageFile  The file name for the background image
     */
    private static void loadWeatherBackdrop( RelativeLayout rl, String imageFile )
    {
        InputStream is;
        Drawable d;

        try
        {
            is = WeatherLionApplication.getAppContext().getAssets().open( imageFile );
            d = Drawable.createFromStream( is, null );
            rl.setBackground( d );
        }// end of try block
        catch ( IOException e )
        {
            try
            {
                is = WeatherLionApplication.getAppContext().getAssets().open( "weather_backgrounds/background_na.jpg" );
                d = Drawable.createFromStream( is, null );
                rl.setBackground( d );
            }// end of try block
            catch ( IOException ex )
            {
                UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE,"Weather backdrop " +
                        imageFile + " could not be loaded!", TAG + "::loadWeatherBackdrop [line: "
                        + e.getStackTrace()[1].getLineNumber() + "]" );
            }// end of catch block

            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE,"Weather backdrop " +
                    imageFile + " could not be loaded!", TAG + "::loadWeatherBackdrop" );
        }// end of catch block
    }// end of method loadWeatherIcon

    /**
     * Loads the applicable weather icon image an sizes it accordingly
     *
     * @param imv The {@code ImageView} in which the image will be displayed
     * @param imageFile  The file name for the icon
     */
    private static void loadWeatherIcon( ImageView imv, String imageFile )
    {
        try
        {
            InputStream is = WeatherLionApplication.getAppContext().getAssets().open( imageFile );
            Drawable d = Drawable.createFromStream( is, null );
            imv.setImageDrawable( d );
        }// end of try block
        catch (IOException e)
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE,"Weather icon " +
                    imageFile + " could not be loaded!", TAG + "::loadWeatherIcon" );
        }// end of catch block
    }// end of method loadWeatherIcon

    private void loadMainActivityWeather()
    {
        StringBuilder currentLocation;
        // load the applicable typeface in use
        UtilityMethod.loadCustomFont( (RelativeLayout) findViewById( R.id.weather_main_container) );

        StringBuilder sunriseTime = new StringBuilder();
        StringBuilder sunsetTime = new StringBuilder();
        WeatherLionApplication.storedData = WeatherLionApplication.lastDataReceived.getWeatherData();

        RecyclerView forecastRecyclerView = findViewById( R.id.lstDayForecast );
        forecastRecyclerView.setHasFixedSize( true );

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        forecastRecyclerView.setLayoutManager( layoutManager );

        Drawable dividerDrawable = getDrawable( R.drawable.wl_forecast_list_divider );
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration( dividerDrawable );
        forecastRecyclerView.addItemDecoration( dividerItemDecoration );

        List< LastWeatherData.WeatherData.DailyForecast.DayForecast > forecastList =
                new ArrayList<>( WeatherLionApplication.storedData.getDailyForecast() );

        RecyclerView.Adapter weeklyForecastAdapter = new WeeklyForecastAdapter(forecastList);
        forecastRecyclerView.setAdapter(weeklyForecastAdapter);

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

        currentLocation = currentCity;

        sunriseTime.setLength( 0 );
        sunriseTime.append( WeatherLionApplication.storedData.getAstronomy().getSunrise().toUpperCase() );

        sunsetTime.setLength( 0 );
        sunsetTime.append( WeatherLionApplication.storedData.getAstronomy().getSunset().toUpperCase() );

        // Icon updater will need these values to be set
        WeatherLionApplication.currentSunriseTime = sunriseTime;
        WeatherLionApplication.currentSunsetTime = sunsetTime;

        updateTemps(); // call update temps here

        // Some providers like Yahoo! loves to omit a zero on the hour mark example: 7:0 am
        if( sunriseTime.length() == 6 )
        {
            String[] ft = sunriseTime.toString().split( ":" );
            sunriseTime.setLength( 0 );
            sunriseTime.append( String.format( "%s%s%s", ft[ 0 ], ":0", ft[ 1 ] ) );
        }// end of if block
        else if( sunsetTime.length() == 6 )
        {
            String[] ft= sunsetTime.toString().split( ":" );
            sunsetTime.setLength( 0 );
            sunsetTime.append( String.format( "%s%s%s", ft[ 0 ], ":0", ft[ 1 ] ) );
        }// end if else if block

        Date timeUpdated = null;

        try
        {
            timeUpdated = new SimpleDateFormat( "EEE MMM dd HH:mm:ss z yyyy",
                    Locale.ENGLISH ).parse( WeatherLionApplication.storedData.getProvider().getDate() );
        }// end of try block
        catch ( ParseException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE , e.getMessage(),
                    TAG + "::loadMainActivityWeather [line: " + e.getStackTrace()[1].getLineNumber()+ "]" );
        }// end of catch block

        UtilityMethod.lastUpdated = timeUpdated;

        String ts = null;
        String[] cityBreakdown = null;

        if( currentLocation.toString().contains( "," ) )
        {
            cityBreakdown = currentLocation.toString().split( "," );
        }// end of if block

        if( cityBreakdown != null )
        {
            if( UtilityMethod.usStatesByCode.get( cityBreakdown[ 1 ].trim() ) != null )
            {
                String cityName = cityBreakdown[ 0 ].trim();
                String stateName = UtilityMethod.usStatesByCode.get( cityBreakdown[ 1 ].trim() );

                ts = String.format( "%s, %s", cityName, stateName );
            }// end of if block
            else
            {
                ts = currentLocation.toString();
            }// end of else block
        }// end of if block

        TextView txvWeatherLocation = findViewById( R.id.txvCurrentWeatherLocation );
        txvWeatherLocation.setTypeface( WeatherLionApplication.currentTypeface );
        txvWeatherLocation.setText( ts );

        // Load current condition weather image
        Calendar rightNow = Calendar.getInstance();
        Calendar nightFall = Calendar.getInstance();
        Calendar sunUp = Calendar.getInstance();
        String sunsetTwenty4HourTime = new SimpleDateFormat( "yyyy-MM-dd",
                Locale.ENGLISH ).format( rightNow.getTime() )
                + " " + UtilityMethod.get24HourTime( WeatherLionApplication.currentSunsetTime.toString() );
        String sunriseTwenty4HourTime = new SimpleDateFormat( "yyyy-MM-dd",
                Locale.ENGLISH ).format( rightNow.getTime() )
                + " " + UtilityMethod.get24HourTime( WeatherLionApplication.currentSunriseTime.toString() );
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm", Locale.ENGLISH );
        Date rn = null; // date time right now (rn)
        Date nf = null; // date time night fall (nf)
        Date su = null; // date time sun up (su)

        try
        {
            rn = sdf.parse(sdf.format( rightNow.getTime() ) );
            nightFall.setTime(sdf.parse( sunsetTwenty4HourTime ) );
            nightFall.set(Calendar.MINUTE,
                    Integer.parseInt(sunsetTwenty4HourTime.split( ":" )[ 1 ].trim() ) );
            sunUp.setTime( sdf.parse( sunriseTwenty4HourTime ) );

            nf = sdf.parse( sdf.format( nightFall.getTime() ) );
            su = sdf.parse( sdf.format( sunUp.getTime() ) );
        } // end of try block
        catch ( ParseException e )
        {
            UtilityMethod.logMessage(UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    TAG + "::loadMainActivityWeather [line: " +
                            e.getStackTrace()[1].getLineNumber() + "]");
        }// end of catch block

        String currentConditionIcon;

        if( rn != null )
        {
            if ( rn.equals( nf ) || rn.after( nf ) || rn.before( su ) )
            {
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
                    }// end of if block

                    if ( UtilityMethod.weatherImages.containsKey(
                            currentCondition.toString().toLowerCase() + " (night)" ) )
                    {
                        currentConditionIcon =
                                UtilityMethod.weatherImages.get(
                                        currentCondition.toString().toLowerCase() + " (night)" );
                    }// end of if block
                    else {
                        currentConditionIcon = UtilityMethod.weatherImages.get(
                                currentCondition.toString().toLowerCase() );
                    }// end of else block
                }// end of else block
            }// end of if block
            else
            {
                currentConditionIcon =
                        UtilityMethod.weatherImages.get( currentCondition.toString().toLowerCase() );
            }// end of else block

            currentConditionIcon =  UtilityMethod.weatherImages.get(
                    currentCondition.toString().toLowerCase() ) == null ?
                    "na.png" :
                    currentConditionIcon;

            ImageView imvCurrentConditionImage = findViewById( R.id.imvCurrentCondition );
            String imageFile = "weather_images/" + WeatherLionApplication.iconSet + "/weather_" + currentConditionIcon;

            loadWeatherIcon( imvCurrentConditionImage, imageFile );

            RelativeLayout rlBackdrop = findViewById( R.id.weather_main_container);
            String backdropFile = "weather_backgrounds/background_" +
                    Objects.requireNonNull( currentConditionIcon ).replace(".png", ".jpg" );
            //String backdropFile = "weather_backgrounds/background_3.jpg";

            loadWeatherBackdrop( rlBackdrop, backdropFile );
        }// end of if block

        // Update the weather provider
        ImageView imvWeatherProviderLogo = findViewById( R.id.imvWeatherProviderLogo );

        TextView txvWeatherProvider = findViewById( R.id.txvWeatherProviderName );
        txvWeatherProvider.setText( WeatherLionApplication.storedData.getProvider().getName() );
        txvWeatherProvider.setTypeface( WeatherLionApplication.currentTypeface );

        String providerIcon = String.format( "%s%s", "wl_",
                WeatherLionApplication.storedData.getProvider().getName().toLowerCase().replaceAll(
                " ", "_" ) );

        if( providerIcon.equals( WeatherLionApplication.YAHOO_WEATHER ) )
        {
            providerIcon = providerIcon.replace( "!", "" ).replace( " ", "_" );
        }// end of if block

        imvWeatherProviderLogo.setImageResource( UtilityMethod.getImageResourceId( providerIcon ) );

        TextView txvLastUpdated = findViewById( R.id.txvLastUpdated );
        txvLastUpdated.setTypeface( WeatherLionApplication.currentTypeface );

        if( timeUpdated != null )
        {
            txvLastUpdated.setText( String.format( "%s%s", "Updated ",
                    UtilityMethod.getTimeSince( timeUpdated ) ) );
        }// end of if block

        if( UtilityMethod.refreshRequested )
        {
            UtilityMethod.refreshRequested = false;
        }// end of if block

    }// end of method loadMainActivityWeather

    /**
     * Displays a dialog alerting the user to activate the device's GPS radio.
     */
    private void noGpsAlert()
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( mContext );
        // return the use gps switch to the off setting
        settings.edit().putBoolean( "pref_use_gps", false ).apply();

        responseDialog( WeatherLionApplication.PROGRAM_NAME + " - No GPS",
                "Your GPS seems to be disabled, do you want to enable it?",
                "Yes", "No","openGPSSettings",
                null, null, null );
    }// end of method noGpsAlert

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        requestWindowFeature( Window.FEATURE_NO_TITLE ); //will hide the title
        Objects.requireNonNull( getSupportActionBar() ).hide(); // hide the title bar

        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        // WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen

        mContext = this;

        TextView txvMessage;
        Snackbar quickSnack;

        LocalBroadcastManager.getInstance( WeatherLionApplication.getAppContext() )
                .registerReceiver(xmlStorageBroadcastReceiver, new IntentFilter(
                        WeatherDataXMLService.WEATHER_XML_STORAGE_MESSAGE ) );

        // Check if any previous weather data is stored locally
        if( WeatherLionApplication.firstRun &&
                !WeatherLionApplication.localWeatherDataAvailable )
        {
            super.onCreate( savedInstanceState );
            setContentView( R.layout.wl_welcome_activity );

            // load the applicable typeface in use
            UtilityMethod.loadCustomFont( (RelativeLayout) findViewById( R.id.weather_main_container) );

            txvMessage = findViewById( R.id.txvMessage );
            txvMessage.setText( HtmlCompat.fromHtml( getString( R.string.announcement ), 0 ) );
            View welcomeActivity = findViewById( R.id.weather_main_container);

            quickSnack = Snackbar.make( welcomeActivity ,
            "", Snackbar.LENGTH_INDEFINITE ).setAction("OPEN SETTINGS",
                    new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            showPreferenceActivity( false );
                        }// end of method onClick
                    });
            View quickSnackView = quickSnack.getView();
            quickSnackView.setBackgroundColor( Color.parseColor("#CC3F85E1" ) );
            quickSnack.setActionTextColor( Color.WHITE );
            quickSnack.show();

            if( checkGeoAccess() )
            {
                accessLoaded();
            }// end of if block
        }// end of if block
        else
        {
            super.onCreate( savedInstanceState );

            WeatherLionApplication.callMethodByName(null, "checkForStoredWeatherData",
                    null, null );

            keysDialogView = View.inflate( this, R.layout.wl_data_keys_layout, null );
            WeatherLionApplication.iconSet = WeatherLionApplication.spf.getString(
                    WeatherLionApplication.ICON_SET_PREFERENCE, Preference.DEFAULT_ICON_SET );

            if( checkGeoAccess() )
            {
                accessLoaded();
            }// end of if block

            if( getRowCount( WeatherAccess.DATABASE_NAME, WeatherAccess.ACCESS_KEYS ) == 0 )
            {
                String message = "You will initially only receive weather data\nfrom "
                                 + WeatherLionApplication.YR_WEATHER + ". "
                                 + "You must add the other weather providers' access keys "
                                 + "to gain access to their data.\nThis can be done by using the "
                                 + "Add/Delete Keys dialog located in the settings menu at the "
                                 + "top right corner to provide access credentials.";

                showMessageDialog( "html",message, "IMPORTANT",
                        "constructDataAccess", null, null );
            }// end of if block

            if( WeatherLionApplication.lastDataReceived != null )
            {
                if( WeatherLionApplication.lastDataReceived.getWeatherData().getLocation() != null )
                {
                    // if data was stored then the original value the location is known
                    WeatherLionApplication.wxLocation =
                            WeatherLionApplication.lastDataReceived.getWeatherData().getLocation().getCity();

                    // if there is no location stored in the local preferences, go to the settings activity
                    if( WeatherLionApplication.wxLocation.equalsIgnoreCase( "Unknown" ) )
                    {
                        showPreferenceActivity( false );
                        return;
                    }// end of if block
                    else
                    {
                        setContentView( R.layout.wl_main_activity );
                    }// end of if block

                    if( WeatherLionApplication.useGps && !WeatherLionApplication.gpsRadioEnabled )
                    {
                        noGpsAlert();
                    }// end of if block

                    if( !UtilityMethod.hasInternetConnection( this ) )
                    {
                        View mainActivity = findViewById( R.id.main_window );

                        Snackbar.make( mainActivity ,
                                "Connect to the Internet", Snackbar.LENGTH_INDEFINITE )
                                .setAction("Wifi Settings", new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        startActivity( new Intent(Settings.ACTION_WIFI_SETTINGS ) );
                                    }// end of method onClick
                                }).show();
                    }// end of if block

                    UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO, "Initiating startup...",
                            TAG + "::main" );

                    doGlimpseRotation( findViewById( R.id.imvBlade ) );

                    loadMainActivityWeather();
                }// end of if block
            }// end of if block
            else
            {
                setContentView( R.layout.wl_welcome_activity );

                UtilityMethod.loadCustomFont( (RelativeLayout) findViewById( R.id.weather_main_container) );

                txvMessage = findViewById( R.id.txvMessage );
                txvMessage.setText( HtmlCompat.fromHtml( getString( R.string.announcement ), 0 ) );
                View welcomeActivity = findViewById( R.id.weather_main_container);

                quickSnack = Snackbar.make( welcomeActivity ,
                        "", Snackbar.LENGTH_INDEFINITE ).setAction("OPEN SETTINGS",
                        new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                showPreferenceActivity( false );
                            }// end of method onClick
                        });
                View quickSnackView = quickSnack.getView();
                quickSnackView.setBackgroundColor( Color.parseColor("#CC3F85E1" ) );
                quickSnack.setActionTextColor( Color.WHITE );
                quickSnack.show();
            }//end of else block
        }// end of else block
    }// end of method onCreate

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        getMenuInflater().inflate( R.menu.main_menu, menu );

        return true;
    }// end of method onCreateOptionsMenu

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        LocalBroadcastManager.getInstance( WeatherLionApplication.getAppContext() )
            .unregisterReceiver(xmlStorageBroadcastReceiver);
    }// end of method onDestroy

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume()
    {
        super.onResume();

        // recheck to see if some weather data has been obtained
        if( new File( this.getFileStreamPath(
                WeatherLionApplication.WEATHER_DATA_XML ).toString() ).exists() )
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
                UtilityMethod.lastUpdated = df.parse(
                        WeatherLionApplication.storedData.getProvider().getDate() );
            }// end of try block
            catch ( ParseException e )
            {
                UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, "Unable to parse last weather data date.",
                        TAG + "::onCreate [line: " +
                                e.getStackTrace()[1].getLineNumber()+ "]" );
            }// end of catch block

            WeatherLionApplication.currentSunriseTime = new StringBuilder(
                    WeatherLionApplication.storedData.getAstronomy().getSunrise() );
            WeatherLionApplication.currentSunsetTime = new StringBuilder(
                    WeatherLionApplication.storedData.getAstronomy().getSunset() );
        }// end of if block

        if( WeatherLionApplication.lastDataReceived != null )
        {
            if( WeatherLionApplication.lastDataReceived.getWeatherData().getLocation() != null )
            {
                if( !findViewById( R.id.weather_main_container ).getTag().equals( "main_screen" ) )
                {
                    setContentView( R.layout.wl_main_activity );
                }// end of if block

                doGlimpseRotation( findViewById( R.id.imvBlade ) );

                loadMainActivityWeather();
            }// end of if block
        }// end of if block
    }// end of method onResume

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        String currLocation = WeatherLionApplication.spf.getString( WeatherLionApplication.CURRENT_LOCATION_PREFERENCE,
                Preference.DEFAULT_WEATHER_LOCATION );
        boolean locationSet = currLocation != null && !currLocation.equalsIgnoreCase( Preference.DEFAULT_WEATHER_LOCATION );

        switch( item.getItemId() )
        {
            case R.id.action_settings:
                showPreferenceActivity( locationSet );

                return true;
            case R.id.action_add_keys:
                showDataKeysDialog( null );

                return true;
            default:
                break;
        }// end of switch block

        return super.onOptionsItemSelected( item );
    }// end of the method onOptionsItemSelected

    /**
     * Opens the GPS setting activity
     */
    private void openGPSSettings()
    {
        startActivity( new Intent( android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS ) );
    }// end of method openGPSSettings

    /**
     * Opens the WiFi setting activity
     */
    private void openWifiSettings()
    {
        startActivity( new Intent( Settings.ACTION_WIFI_IP_SETTINGS ) );
    }// end of method openGPSSettings

    /**
     * Display a dialog illiciting a response from the user
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
        final AlertDialog response = new AlertDialog.Builder( this ).create();
        View dialogView = View.inflate( WeatherLionMain.this, R.layout.wl_response_dialog, null );
        response.setView( dialogView );
        response.setCancelable( false );

        UtilityMethod.loadCustomFont( (LinearLayout) dialogView.findViewById( R.id.llResponseDialog ) );

        // Initialize the view objects
        RelativeLayout rlTitleBar = dialogView.findViewById( R.id.rlDialogTitleBar );
        rlTitleBar.setBackgroundColor( WeatherLionApplication.systemColor.toArgb() );

        TextView txvDialogTitle = dialogView.findViewById( R.id.txvDialogTitle );
        TextView txvDialogMessage = dialogView.findViewById( R.id.txvMessage );

        Button btnPositive = dialogView.findViewById( R.id.btnPositive );
        btnPositive.setBackground( WeatherLionApplication.systemButtonDrawable );
        Button btnNegative = dialogView.findViewById( R.id.btnNegative );
        btnNegative.setBackground( WeatherLionApplication.systemButtonDrawable );

        txvDialogTitle.setText( title );
        txvDialogMessage.setText( prompt );
        btnPositive.setText( posResponse );
        btnNegative.setText( negResponse );

        btnPositive.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if( params == null || params.length == 0 )
                {
                    callMethodByName( WeatherLionMain.this, positiveAction,
                            null, null );
                }// end of if block
                else
                {
                    callMethodByName( WeatherLionMain.this,
                            positiveAction, paramClassTypes, params );
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
                    callMethodByName( WeatherLionMain.this, negativeAction,
                            null, null );
                }// end of if block

                response.dismiss();
            }
        });

        response.show();
    }// end of method responseDialog

    /**
     * Determine which weather access provider the user selected form the
     * list of options.
     */
    private void getSelectedProvider()
    {
        spnAccessProvider = keysDialogView.findViewById( R.id.spnAccessProvider );
        edtKeyName = keysDialogView.findViewById( R.id.edtKeyName );
        pwdKeyValue = keysDialogView.findViewById( R.id.edtKeyValue );

        WeatherLionApplication.selectedProvider = (String) spnAccessProvider.getSelectedItem();

        if( WeatherLionApplication.selectedProvider.equals( WeatherLionApplication.GEO_NAMES ) )
        {
            edtKeyName.setEnabled( false );
            edtKeyName.setText( getString( R.string.user_name ) );
            pwdKeyValue.requestFocus();
        }// end of if block
        else if( WeatherLionApplication.selectedProvider.equals( WeatherLionApplication.DARK_SKY ) ||
                WeatherLionApplication.selectedProvider.equals( WeatherLionApplication.OPEN_WEATHER ) ||
                WeatherLionApplication.selectedProvider.equals( WeatherLionApplication.WEATHER_BIT ) )
        {
            edtKeyName.setEnabled( false );
            edtKeyName.setText( getString( R.string.api_key ) );
            pwdKeyValue.requestFocus();
        }// end of if block
        else
        {
            edtKeyName.setEnabled( true );
            edtKeyName.setText( "" );
            edtKeyName.requestFocus();
        }// end of else block
    }// end of method getSelectedProvider

    public void showMainMenuPopup( View v )
    {
        String wxLocation = WeatherLionApplication.spf.getString( WeatherLionApplication.CURRENT_LOCATION_PREFERENCE,
                Preference.DEFAULT_WEATHER_LOCATION );
        final boolean locationSet = wxLocation != null &&
                !wxLocation.equalsIgnoreCase( Preference.DEFAULT_WEATHER_LOCATION );

        PopupMenu popup = new PopupMenu( WeatherLionMain.this, v,
                Gravity.END, 0, R.style.MainActivityPopupMenu );
        popup.getMenuInflater().inflate( R.menu.main_menu, popup.getMenu() );

        popup.setOnMenuItemClickListener( new PopupMenu.OnMenuItemClickListener()
        {


            public boolean onMenuItemClick( MenuItem item )
            {
                switch( item.getItemId() )
                {
                    case R.id.action_settings:
                        if( WeatherLionApplication.geoNamesAccountLoaded )
                        {
                            showPreferenceActivity( locationSet );
                        }// end of if block
                        else
                        {
                            // confirm that user has a GeoNames account and want's to store it
                            String prompt = "This program requires a geonames username\n" +
                                    "which was not stored in the database.\nIT IS FREE!" +
                                    "\nDo you wish to add it now?";

                            responseDialog( WeatherLionApplication.PROGRAM_NAME + " - Missing Key",
                                    prompt,"Yes", "No","showDataKeysDialog",
                                    "lackPrivilegesMessage", new Object[]{ "GeoNames" },
                                    new Class[]{ String.class } );
                        }// end of else block

                        return true;
                    case R.id.action_add_keys:
                        showDataKeysDialog( null );

                        return true;
                    case R.id.action_about_app:
                        Intent settingsIntent = new Intent( mContext, AboutActivity.class );
                        startActivity( settingsIntent );

                        return true;
                    default:
                        break;
                }// end of switch block

                return true;
            }
        });

        popup.show();//showing popup menu
    }// end of method showMainMenuPopup

    private void showPreferenceActivity( boolean locationSet )
    {
        Intent settingsIntent = new Intent( this, PrefsActivity.class );
        settingsIntent.putExtra( LION_MAIN_PAYLOAD, locationSet );
        startActivity( settingsIntent );
    }// end of method showPreferenceActivity

    private void showDataKeysDialog( String defaultSelection )
    {
        final View keyDialogView = View.inflate( this, R.layout.wl_data_keys_layout, null );
        keyEntryDialog = new AlertDialog.Builder( mContext ).create();
        keyEntryDialog.setView( keyDialogView );

        RelativeLayout rlTitleBar = keyDialogView.findViewById( R.id.rlDialogTitleBar );
        rlTitleBar.setBackgroundColor( WeatherLionApplication.systemColor.toArgb() );

        ImageView imvClose = keyDialogView.findViewById( R.id.imvCloseDialog );
        spnAccessProvider = keyDialogView.findViewById( R.id.spnAccessProvider );
        rlKeyNameParent = keyDialogView.findViewById( R.id.spnKeyNameParent );
        spnProviderKeys = keyDialogView.findViewById( R.id.spnKeyName );
        edtKeyName = keyDialogView.findViewById( R.id.edtKeyName );
        pwdKeyValue = keyDialogView.findViewById( R.id.edtKeyValue );
        CheckBox chkShowPwd = keyDialogView.findViewById( R.id.cbShowPwd );

        Button btnAddKey = keyDialogView.findViewById( R.id.btnAddKey );
        btnAddKey.setBackground( WeatherLionApplication.systemButtonDrawable );

        Button btnDeleteKey = keyDialogView.findViewById( R.id.btnDeleteKey );
        btnDeleteKey.setBackground( WeatherLionApplication.systemButtonDrawable );

        Button btnFinish = keyDialogView.findViewById( R.id.btnFinish );
        btnFinish.setBackground( WeatherLionApplication.systemButtonDrawable );

        // Load only the providers who require access keys
        ArrayList< String > wxOnly =
                new ArrayList<>( Arrays.asList( WeatherLionApplication.providerNames ) );

        Collections.sort( wxOnly );	// sort the list

        // Yr.no (Norwegian Meteorological Institute) does not require an access key at the moment
        wxOnly.remove( WeatherLionApplication.YR_WEATHER );

        String[] accessNeededProviders = wxOnly.toArray( new String[ 0 ] );

        //ArrayAdapter adapter = new ArrayAdapter( mContext, R.layout.wl_access_provider_spinner_style, accessNeededProviders );

        // create array adapter with custom fonts
        ArrayAdapter accessProvidersAdapter = new ArrayAdapter( this,
                R.layout.wl_access_provider_spinner_style, accessNeededProviders )
        {
            @NonNull
            @Override
            public View getView( int position, View convertView, @NonNull ViewGroup parent )
            {
                TextView view = (TextView) super.getView( position, convertView, parent );
                view.setTypeface( WeatherLionApplication.currentTypeface );
                return view;
            }

            @NonNull
            @Override
            public View getDropDownView( int position, View convertView, @NonNull ViewGroup parent )
            {
                TextView view = (TextView) super.getDropDownView( position, convertView, parent );
                view.setTypeface( WeatherLionApplication.currentTypeface );
                return view;
            }
        };

        spnAccessProvider.setAdapter( accessProvidersAdapter );

        if( defaultSelection != null )
        {
            spnAccessProvider.setSelection( accessProvidersAdapter.getPosition( defaultSelection ) );
        }// end of if block

        spnAccessProvider.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected( AdapterView<?> parent, View view, int position, long id )
            {
                WeatherLionApplication.selectedProvider = parent.getItemAtPosition(position).toString();

                switch( WeatherLionApplication.selectedProvider )
                {
                    case WeatherLionApplication.GEO_NAMES:
                        rlKeyNameParent.setVisibility( View.GONE );
                        edtKeyName.setVisibility( View.VISIBLE );

                        edtKeyName.setEnabled( false );
                        edtKeyName.setText( getString( R.string.user_name ) );
                        pwdKeyValue.requestFocus();

                        break;
                    case WeatherLionApplication.DARK_SKY:
                    case WeatherLionApplication.OPEN_WEATHER:
                    case WeatherLionApplication.WEATHER_BIT:
                        rlKeyNameParent.setVisibility( View.GONE );
                        edtKeyName.setVisibility( View.VISIBLE );

                        edtKeyName.setEnabled( false );
                        edtKeyName.setText( getString( R.string.api_key ) );
                        pwdKeyValue.requestFocus();

                        break;
                    case WeatherLionApplication.HERE_MAPS:
                        edtKeyName.setVisibility( View.GONE );
                        rlKeyNameParent.setVisibility( View.VISIBLE );

                        requiredKeysAdapter = new ArrayAdapter( WeatherLionMain.this,
                                R.layout.wl_key_name_spinner_style,
                                WeatherLionApplication.hereMapsRequiredKeys )
                        {
                            @NonNull
                            @Override
                            public View getView( int position, View convertView, @NonNull ViewGroup parent )
                            {
                                TextView view = (TextView) super.getView( position, convertView, parent );
                                view.setTypeface( WeatherLionApplication.currentTypeface );
                                return view;
                            }

                            @NonNull
                            @Override
                            public View getDropDownView( int position, View convertView, @NonNull ViewGroup parent )
                            {
                                TextView view = (TextView) super.getDropDownView( position, convertView, parent );
                                view.setTypeface( WeatherLionApplication.currentTypeface );
                                return view;
                            }
                        };

                        spnProviderKeys.setAdapter( requiredKeysAdapter );
                        spnProviderKeys.setSelection( 0 );

                        pwdKeyValue.requestFocus();

                        break;
                    case WeatherLionApplication.YAHOO_WEATHER:
                        edtKeyName.setVisibility( View.GONE );
                        rlKeyNameParent.setVisibility( View.VISIBLE );

                        requiredKeysAdapter = new ArrayAdapter( WeatherLionMain.this,
                                R.layout.wl_key_name_spinner_style,
                                WeatherLionApplication.yahooRequiredKeys )
                        {
                            @NonNull
                            @Override
                            public View getView( int position, View convertView, @NonNull ViewGroup parent )
                            {
                                TextView view = (TextView) super.getView( position, convertView, parent );
                                view.setTypeface( WeatherLionApplication.currentTypeface );
                                return view;
                            }

                            @NonNull
                            @Override
                            public View getDropDownView( int position, View convertView, @NonNull ViewGroup parent )
                            {
                                TextView view = (TextView) super.getDropDownView( position, convertView, parent );
                                view.setTypeface( WeatherLionApplication.currentTypeface );
                                return view;
                            }
                        };

                        spnProviderKeys.setAdapter( requiredKeysAdapter );
                        spnProviderKeys.setSelection( 0 );

                        break;
                    default:
                        rlKeyNameParent.setVisibility( View.GONE );
                        edtKeyName.setVisibility( View.VISIBLE );
                        edtKeyName.setEnabled( true );
                        edtKeyName.setText( "" );
                        edtKeyName.requestFocus();

                        break;

                }// end of switch block
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spnProviderKeys.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected( AdapterView<?> parent, View view, int position, long id )
            {
                WeatherLionApplication.selectedKeyName = parent.getItemAtPosition(position).toString();
                pwdKeyValue.requestFocus();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // add onCheckedListener on checkbox
        // when user clicks on this checkbox, this is the handler.
        chkShowPwd.setOnCheckedChangeListener( new OnCheckedChangeListener()
        {
            public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
            {
                // checkbox status is changed from uncheck to checked.
                if ( !isChecked )
                {
                    // show password
                    pwdKeyValue.setTransformationMethod( PasswordTransformationMethod.getInstance() );
                }// end of if block
                else
                {
                    // hide password
                    pwdKeyValue.setTransformationMethod( HideReturnsTransformationMethod.getInstance() );
                }// end of else block
            }
        });

        imvClose.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                keyEntryDialog.dismiss();
            }
        });

        btnAddKey.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                String keyName = null;
                String keyValue = null;

                if( edtKeyName.getVisibility() == View.VISIBLE )
                {
                    if( edtKeyName == null || edtKeyName.getText().toString().length() == 0
                            || edtKeyName.getText().toString().equals( "" ) )
                    {
                        UtilityMethod.butteredToast( mContext,"Please enter a valid key name as given by the provider!",
                                2, Toast.LENGTH_LONG );
                        edtKeyName.requestFocus();
                    }// end of if block
                    else if( pwdKeyValue == null || pwdKeyValue.getText().toString().length() == 0
                            || pwdKeyValue.getText().toString().equals( "" ) )
                    {
                        UtilityMethod.butteredToast( mContext,"Please enter a valid key value as given by the provider!",
                                2, Toast.LENGTH_LONG );
                        pwdKeyValue.requestFocus();

                        return;
                    }// end of else if block
                    else
                    {
                        keyName = edtKeyName.getText().toString();
                        keyValue = pwdKeyValue.getText().toString();
                    }// end of else block
                }// end of if block
                else
                {
                    if( pwdKeyValue == null || pwdKeyValue.getText().toString().length() == 0
                        || pwdKeyValue.getText().toString().equals( "" ) )
                    {
                        UtilityMethod.butteredToast( mContext,"Please enter a valid key value as given by the provider!",
                                2, Toast.LENGTH_LONG );
                        pwdKeyValue.requestFocus();

                        return;
                    }// end of if block
                    else
                    {
                        keyName = WeatherLionApplication.selectedKeyName;
                        keyValue = pwdKeyValue.getText().toString();
                    }// end of else block
                }// end of else block

                String[] encryptedKey = WeatherLionApplication.encrypt( keyValue );

                answer = -1; // reset the field because it must be reused

                switch ( WeatherLionApplication.selectedProvider )
                {
                    case "Here Maps Weather":
                        if( !Arrays.asList( WeatherLionApplication.hereMapsRequiredKeys ).contains(
                                Objects.requireNonNull( keyName ).toLowerCase() ) )
                        {
                            UtilityMethod.butteredToast( mContext,"The " + WeatherLionApplication.selectedProvider +
                                            " does not require a key \"" + keyName + "\"!",
                                    2, Toast.LENGTH_LONG );

                            spnProviderKeys.requestFocus();

                            return;
                        }// end of if block

                        break;
                    case "Yahoo! Weather":
                        if( !Arrays.asList( WeatherLionApplication.yahooRequiredKeys ).contains(
                                Objects.requireNonNull( keyName ).toLowerCase() ) )
                        {
                            UtilityMethod.butteredToast( mContext,"The " + WeatherLionApplication.selectedProvider +
                                            " does not require a key \"" + keyName + "\"!",
                                    2, Toast.LENGTH_LONG);

                            spnProviderKeys.requestFocus();

                            return;
                        }// end of if block

                        break;
                    default:
                        break;
                }// end of switch block

                if( WeatherLionApplication.addSiteKeyToDatabase( WeatherLionApplication.selectedProvider,
                        keyName, encryptedKey[ 0 ], encryptedKey[ 1 ] ) != -1 )
                {
                    if( edtKeyName.isEnabled() ) edtKeyName.setText( "" );
                    pwdKeyValue.setText( "" );

                    UtilityMethod.butteredToast( mContext,"The key was successfully added to the database.",
                            1, Toast.LENGTH_LONG );
                    broadcastKeyUpdate();

                    if( edtKeyName.getVisibility() == View.VISIBLE )
                    {
                        edtKeyName.requestFocus();
                    }// end of if block
                    else
                    {
                        spnProviderKeys.requestFocus();
                    }// end of else block

                }// end of if block
                else
                {
                    UtilityMethod.butteredToast( mContext,"The key could not be added to the database!"
                            + "\nPlease recheck the key and try again.", 2, Toast.LENGTH_SHORT );

                    if( edtKeyName.getVisibility() == View.VISIBLE )
                    {
                        edtKeyName.requestFocus();
                    }// end of if block
                    else
                    {
                        spnProviderKeys.requestFocus();
                    }// end of else block
                }// end of else block
            }
        });

        btnDeleteKey.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                WeatherLionApplication.dataAccessKeyName = ( (EditText) keyEntryDialog.findViewById( R.id.edtKeyName ) ).getText().toString();

                if( edtKeyName == null || edtKeyName.getText().toString().length() == 0 )
                {
                    UtilityMethod.butteredToast( mContext,"Please enter a valid key name as given by the provider!",
                            2, Toast.LENGTH_LONG );

                    edtKeyName.requestFocus();
                }// end of if block
                else
                {
                    String keyToDelete = edtKeyName.getText().toString();

                    // confirm that user really wishes to delete the key
                    String prompt = "Are you sure that you wish to delete the " +
                            keyToDelete +
                            ( keyToDelete.contains( "key" ) ? " assigned by " : " key assigned by " ) +
                            WeatherLionApplication.selectedProvider + "?\nThis cannot be undone!";

                    responseDialog( WeatherLionApplication.PROGRAM_NAME + " Delete Key",
                            prompt,"Yes", "No","deleteSiteKeyFromDatabase",
                            null, new Object[]{ WeatherLionApplication.selectedProvider, keyToDelete },
                            new Class[]{ String.class, String.class } );

                    broadcastKeyUpdate();
                }// end of else block
            }
        });

        btnFinish.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                keyEntryDialog.dismiss();
            }
        } );

        keyEntryDialog.setCancelable( wxOnly.contains( WeatherLionApplication.GEO_NAMES ) ); // User is only allowed to cancel if GeoNames account exists

        Objects.requireNonNull( keyEntryDialog.getWindow() ).setBackgroundDrawable(
                new ColorDrawable( Color.TRANSPARENT ) );
        UtilityMethod.loadCustomFont( (RelativeLayout) keyDialogView.findViewById( R.id.rlKeysDialog ) );
        keyEntryDialog.show();
        keyEntryDialog.findViewById( R.id.edtKeyName ).requestFocus();
    }// end of method showDataKeysDialog

    /**
     * Display a dialog with a specific message
     *
     * @param message   The message to be displayed in the alert dialog
     * @param title   The alert dialog title
     */
    private void showMessageDialog( String messageType, String message, String title,
                                    final String methodToCall, final Object[] params, final Class[] paramClassTypes )
    {
        final View messageDialogView = View.inflate( this, R.layout.wl_message_dialog, null );
        final AlertDialog messageDialog = new AlertDialog.Builder( this ).create();
        messageDialog.setView( messageDialogView );

        RelativeLayout rlTitleBar = messageDialogView.findViewById( R.id.rlDialogTitleBar );
        rlTitleBar.setBackgroundColor( WeatherLionApplication.systemColor.toArgb() );

        TextView txvTitle = messageDialogView.findViewById( R.id.txvDialogTitle );
        txvTitle.setMovementMethod( new ScrollingMovementMethod() );

        TextView txvMessage = messageDialogView.findViewById( R.id.txvMessage );

        Button btnOk = messageDialogView.findViewById( R.id.btnOk );
        btnOk.setBackground( WeatherLionApplication.systemButtonDrawable );

        txvTitle.setText( title );

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
                    callMethodByName( WeatherLionMain.this, methodToCall,null, null );
                }// end of if block
                else
                {
                    callMethodByName( WeatherLionMain.this, methodToCall, paramClassTypes, params );
                }// end of else block

                messageDialog.dismiss();
            }
        });

        messageDialog.show();
    }// end of method showMessageDialog

    /***
     * Update the numerical values displayed on the widget
     *
     */
    private void updateTemps()
    {
        WeatherLionApplication.storedData = WeatherLionApplication.lastDataReceived.getWeatherData();

        TextView txvCurrentTemperature = findViewById( R.id.txvCurrentTemperature );
        TextView txvFeelsLikeTemperature = findViewById( R.id.txvClimateConditions);
        TextView txvHighTemp  = findViewById( R.id.txvHighTemp );
        TextView txvLowTemp  = findViewById( R.id.txvLowTemp );

        // populate the global variables
        if( WeatherLionApplication.storedPreferences.getUseMetric() )
        {
            currentTemp.setLength( 0 );
            currentTemp.append( Math.round( UtilityMethod.fahrenheitToCelsius(
                    (float) WeatherLionApplication.storedData.getCurrent().getTemperature() ) ) );

            currentFeelsLikeTemp.setLength( 0 );
            currentFeelsLikeTemp.append( Math.round( UtilityMethod.fahrenheitToCelsius(
                    (float) WeatherLionApplication.storedData.getCurrent().getFeelsLike() ) ) );

            currentHigh.setLength( 0 );
            currentHigh.append( Math.round( UtilityMethod.fahrenheitToCelsius(
                    (float) WeatherLionApplication.storedData.getCurrent().getHighTemperature() ) ) );

            currentLow.setLength( 0 );
            currentLow.append( Math.round( UtilityMethod.fahrenheitToCelsius(
                    (float) WeatherLionApplication.storedData.getCurrent().getLowTemperature() ) ) );

            currentWindSpeed.setLength( 0 );
            currentWindSpeed.append(
                    Math.round( UtilityMethod.mphToKmh( WeatherLionApplication.storedData.getWind().getWindSpeed() ) ) );
        }// end of if block
        else
        {
            currentTemp.setLength( 0 );
            currentTemp.append( Math.round(
                    (float) WeatherLionApplication.storedData.getCurrent().getTemperature() ) );

            currentFeelsLikeTemp.setLength( 0 );
            currentFeelsLikeTemp.append( Math.round(
                    (float) WeatherLionApplication.storedData.getCurrent().getFeelsLike() ) );

            currentHigh.setLength( 0 );
            currentHigh.append( Math.round(
                    (float) WeatherLionApplication.storedData.getCurrent().getHighTemperature() ) );

            currentLow.setLength( 0 );
            currentLow.append( Math.round(
                    (float) WeatherLionApplication.storedData.getCurrent().getLowTemperature() ) );

            currentWindSpeed.setLength( 0 );
            currentWindSpeed.append( Math.round(
                    WeatherLionApplication.storedData.getWind().getWindSpeed() ) );
        }// end of else block

        currentWindDirection.setLength( 0 );
        currentWindDirection.append( WeatherLionApplication.storedData.getWind().getWindDirection() );

        TextView txvWindReading = findViewById( R.id.txvWindReading );
        txvWindReading.setTypeface( WeatherLionApplication.currentTypeface );
        String windReading = String.format( "%s %s %s", currentWindDirection, currentWindSpeed,
                ( WeatherLionApplication.storedPreferences.getUseMetric() ? "km/h" : "mph" ) );
        txvWindReading.setText( windReading );
        txvWindReading.setTypeface( WeatherLionApplication.currentTypeface );

        // Windmill rotation
        ImageView blade = findViewById( R.id.imvBlade );
        Animation rotateAnim;

        if( currentWindDirection.toString().toLowerCase().contains( "w" ) )
        {
            // wind is blowing in a westward direction
            rotateAnim = AnimationUtils.loadAnimation( this, R.anim.westward_blade_rotation );
        }// end of if block
        else
        {
            // wind is blowing in a eastward direction
            rotateAnim = AnimationUtils.loadAnimation( this, R.anim.eastward_blade_rotation );
        }// end of else block

        // set the duration of the animation based on the wind speed
        int rotationSpeed = UtilityMethod.getWindRotationSpeed(
                Integer.parseInt( currentWindSpeed.toString() ), "mph" );

        // Tester
//        int rotationSpeed = UtilityMethod.getWindRotationSpeed(70, "mph" );

        // only set a rotation if the value is greater than 0
        if( rotationSpeed > 0 )
        {
            rotateAnim.setDuration( rotationSpeed );

            // start the animation
            blade.setAnimation( rotateAnim );
        }// end of if block
        else
        {
            blade.setAnimation( null );
        }// end of else block

        // Display weather data on widget
        txvCurrentTemperature.setText( String.format( "%s%s", currentTemp.toString(),
                WeatherLionApplication.DEGREES ) );
        txvCurrentTemperature.setTypeface( WeatherLionApplication.currentTypeface );
        txvFeelsLikeTemperature.setText( String.format( "%s%s%s %s","Feels Like ",
                currentFeelsLikeTemp, WeatherLionApplication.DEGREES, currentCondition ) );
        txvFeelsLikeTemperature.setTypeface( WeatherLionApplication.currentTypeface );

        txvHighTemp.setText( String.format( "%s%s",currentHigh.toString(), WeatherLionApplication.DEGREES ) );
        txvHighTemp.setTypeface( WeatherLionApplication.currentTypeface );
        txvLowTemp.setText( String.format( "%s%s", currentLow.toString(), WeatherLionApplication.DEGREES ) );
        txvLowTemp.setTypeface( WeatherLionApplication.currentTypeface );

        // Update the color of the temperature label
        txvCurrentTemperature.setTypeface( WeatherLionApplication.currentTypeface );
        txvCurrentTemperature.setTextColor(
                ( UtilityMethod.temperatureColor( Integer.parseInt(
                        currentTemp.toString().replaceAll( "\\D+","" ) ) ) ) );
    }// end of method updateTemps
}// end of class WeatherLionMain