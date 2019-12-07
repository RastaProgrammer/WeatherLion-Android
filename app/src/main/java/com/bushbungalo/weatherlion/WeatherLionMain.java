package com.bushbungalo.weatherlion;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.text.HtmlCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import com.bushbungalo.weatherlion.custom.CustomPreferenceGrid;
import com.bushbungalo.weatherlion.custom.WeeklyForecastAdapter;
import com.bushbungalo.weatherlion.database.DBHelper;
import com.bushbungalo.weatherlion.database.WeatherAccess;
import com.bushbungalo.weatherlion.model.CityData;
import com.bushbungalo.weatherlion.model.LastWeatherData;
import com.bushbungalo.weatherlion.model.TimeZoneInfo;
import com.bushbungalo.weatherlion.services.WeatherDataXMLService;
import com.bushbungalo.weatherlion.services.WidgetUpdateService;
import com.bushbungalo.weatherlion.utils.DividerItemDecoration;
import com.bushbungalo.weatherlion.utils.JSONHelper;
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
    public static final String LION_LOCATION_PAYLOAD = "LocationPayload";
    public static final String LION_LIMIT_EXCEEDED_PAYLOAD = "LimitExceededPayload";
    public static final String KEY_UPDATE_PAYLOAD = "KeyUpdatePayload";
    public static final String KEY_UPDATE_MESSAGE = "loadAccessProviders";
    private final static String TAG = "WeatherLionMain";

    public final static String RECYCLER_ITEM_CLICK = "RecyclerViewClicked";
    public final static String RECYCLER_ITEM_LOCATION = "RecyclerItemLocation";
    public final static String RECYCLER_ITEM_POSITION = "RecyclerItemPosition";

    public Context mContext;

    // Data Access
    private EditText edtKeyName;
    private EditText pwdKeyValue;
    private Spinner spnProviderKeys;
    private RelativeLayout rlKeyNameParent;

    private AlertDialog keyEntryDialog;

    private static StringBuilder currentCity = new StringBuilder();
    private static StringBuilder currentCountry = new StringBuilder();
    private static StringBuilder currentTemp = new StringBuilder();
    private static StringBuilder currentFeelsLikeTemp = new StringBuilder();
    private static StringBuilder currentWindSpeed = new StringBuilder();
    private static StringBuilder currentWindDirection = new StringBuilder();
    public  static StringBuilder currentCondition = new StringBuilder();
    private static StringBuilder currentHigh = new StringBuilder();
    private static StringBuilder currentLow = new StringBuilder();

    private static ArrayAdapter requiredKeysAdapter;

    private RecyclerView forecastRecyclerView;
    List< LastWeatherData.WeatherData.HourlyForecast.HourForecast > hourlyForecastList;
    List< LastWeatherData.WeatherData.DailyForecast.DayForecast > fiveDayForecastList;

    private TextView txvWeatherLocation;
    private TextClock txcLocalTime;
    private ImageView imvShowPreviousSearches;
    private TextView txvLastUpdated;
    private ScrollView detailsScroll;

    private static ListPopupWindow popupWindow;
    private static PopupMenu popupMenu;
    private static String[] listItems;
    private static String selectedCity;
    private StringBuilder currentLocation;

    private SwipeRefreshLayout appRefresh;

    private View internetCafeView;

    private AlertDialog loadingDialog;
    private AnimationDrawable loadingAnimation;

    private BroadcastReceiver  systemEventsBroadcastReceiver = new SystemBroadcastReceiver();
    private BroadcastReceiver appBroadcastReceiver = new AppBroadcastReceiver();

    public static final int LIST_ANIMATION_DURATION = 300;

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
        wxOnly.remove( WeatherLionApplication.GEO_NAMES );

        WeatherLionApplication.authorizedProviders = wxOnly.toArray( new String[ 0 ] );

        // check for an Internet connection or previous weather data stored local
        if( !WeatherLionApplication.connectedToInternet &&
                !UtilityMethod.getInternalFile( WeatherLionApplication.WEATHER_DATA_XML ) )
        {
            UtilityMethod.butteredToast( WeatherLionApplication.getAppContext(),
                    "The program will not run without a working internet connection or data " +
                            "that was previously stored locally.",2, Toast.LENGTH_LONG );
        }// end of if block
        else if( WeatherLionApplication.connectedToInternet )
        {
            if( WeatherLionApplication.useGps && WeatherLionApplication.gpsRadioEnabled )
            {
                // obtain the current city of the connected Internet service
                UtilityMethod.getGPSCityLocation( false );
            }// end of if block
        }// end of else if block
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

    /**
     * Perform an initial animation of the view to show that it actually rotates
     *
     * @param v The view to be animated
     */
    public void doGlimpseRotation( View v )
    {
        Animation rotateAnim = AnimationUtils.loadAnimation( this,
                R.anim.single_blade_rotation );

        if( v.getAnimation() == null )
        {
            v.startAnimation( rotateAnim );
        }// end of if block
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

    /**
     * The user does not provide sufficient data for the app to be run as intended
     */
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
    private static void loadWeatherBackdrop( ViewGroup rl, String imageFile )
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
        catch ( IOException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE,"Weather icon " +
                    imageFile + " could not be loaded!", TAG + "::loadWeatherIcon" );

            String defaultIcon = WeatherLionApplication.WEATHER_IMAGES_ROOT + WeatherLionApplication.iconSet
                    + "/weather_na.png";

            loadWeatherIcon( imv, defaultIcon );
        }// end of catch block
    }// end of method loadWeatherIcon

    /**
     * Load the weather details for the main activity
     */
    private void loadMainActivityWeather()
    {
        ViewGroup rootView = findViewById( R.id.weather_main_container );
        TextView txvTimezone = findViewById( R.id.txvTimezone );
        String shortTZ;

        if( TimeZoneInfo.timeZoneCodes.get(
                WeatherLionApplication.currentLocationTimeZone.getTimezoneId() ) != null )
        {
            shortTZ = TimeZoneInfo.timeZoneCodes.get(
                    WeatherLionApplication.currentLocationTimeZone.getTimezoneId() );
        }// end of if block
        else if( TimeZoneInfo.timeZoneCodes.get(
                WeatherLionApplication.currentLocationTimeZone.getGmtOffset() ) != null)
        {
            shortTZ = TimeZoneInfo.timeZoneCodes.get(
                    WeatherLionApplication.currentLocationTimeZone.getGmtOffset() );
        }// end of else if block
        else
        {
            shortTZ = null;
        }// end of else block

        TextView txvCurrentCondition = findViewById( R.id.txvClimateConditions );

        if( shortTZ != null )
        {
            txvTimezone.setText( shortTZ );
        }// end of if block
        else
        {
            txvTimezone.setText(
                WeatherLionApplication.currentLocationTimeZone.getTimezoneId() );
        }// end of else block

        WeatherLionApplication.storedData =
            WeatherLionApplication.lastDataReceived.getWeatherData();

        StringBuilder sunriseTime = new StringBuilder(
                WeatherLionApplication.storedData.getAstronomy().getSunrise() );
        StringBuilder sunsetTime = new StringBuilder(
                WeatherLionApplication.storedData.getAstronomy().getSunset() );

        TextView txvSunriseTime = findViewById( R.id.txvSunriseTime );
        TextView txvSunsetTime = findViewById( R.id.txvSunsetTime );

        txvSunriseTime.setText( sunriseTime.toString() );
        txvSunsetTime.setText( sunsetTime.toString() );

        LinearLayout hourlyForecast = findViewById( R.id.hourlyForecastParent );
        TextView txvHumidity = findViewById( R.id.txvHumidity );

        txvHumidity.setText( String.format( Locale.ENGLISH,
        "%s%%", WeatherLionApplication.storedData
                    .getAtmosphere().getHumidity() ) );

        // if an hourly forecast is present then show it
        if( WeatherLionApplication.storedData.getHourlyForecast().size() > 0 )
        {
            hourlyForecast.setVisibility( View.VISIBLE );
            hourlyForecastList = new ArrayList<>(
                WeatherLionApplication.storedData.getHourlyForecast() );

            TextView txvHour;
            ImageView imvHour;
            TextView txvTemp;

            LinearLayout hourlyForecastGrid = findViewById( R.id.hourlyForecastGrid );
            hourlyForecastGrid.removeAllViews();

            for ( int i = 0; i < WeatherLionApplication.storedData.getHourlyForecast().size(); i++ )
            {
                LastWeatherData.WeatherData.HourlyForecast.HourForecast wxHourForecast =
                        WeatherLionApplication.storedData.getHourlyForecast().get( i );
                String forecastTime = null;

                View hourForecastView = View.inflate( this, R.layout.wl_hourly_weather_child, null );
                TextView txvForecastTime = hourForecastView.findViewById( R.id.txvHourForecastTime );
                ImageView imvHourWeatherIcon = hourForecastView.findViewById( R.id.imvHourForecastWeatherIcon );
                TextView txvHourlyForecastTemp = hourForecastView.findViewById( R.id.txvHourForecastTemperature );

                txvForecastTime.setText( wxHourForecast.getTime() );

                String today = new SimpleDateFormat( "MM/dd/yyyy",
                        Locale.ENGLISH ).format( new Date() );

                String hourForecast = String.format( "%s %s", today,
                        wxHourForecast.getTime() );

                SimpleDateFormat sdf = new SimpleDateFormat( "MM/dd/yyyy h a",
                        Locale.ENGLISH );
                Date onTime = null;

                try
                {
                    onTime = sdf.parse( hourForecast );
                } // end of try block
                catch ( ParseException e )
                {
                    UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE , e.getMessage(),
                    TAG + "::loadMainActivity [line: " + e.getStackTrace()[ 1 ].getLineNumber() + "]" );
                }// end of catch block

                // Load current forecast condition weather image
                StringBuilder fCondition = new StringBuilder(
                    UtilityMethod.validateCondition(
                        wxHourForecast.getCondition() ) );
                String fConditionIcon = UtilityMethod.getConditionIcon( fCondition, onTime );

                loadWeatherIcon( imvHourWeatherIcon, String.format(
                    "weather_images/%s/weather_%s", WeatherLionApplication.iconSet, fConditionIcon ) );

                if( WeatherLionApplication.storedPreferences.getUseMetric() )
                {
                    txvHourlyForecastTemp.setText( String.format( "%s%s",
                        Math.round( UtilityMethod.fahrenheitToCelsius(
                            wxHourForecast.getTemperature() ) ),
                                WeatherLionApplication.DEGREES ) );
                }// end of if block
                else
                {
                    txvHourlyForecastTemp.setText( String.format( "%s%s", wxHourForecast.getTemperature(),
                        WeatherLionApplication.DEGREES ) );
                }// end of else block

                // distribute the views equally horizontally across the parent view
                LinearLayout.LayoutParams childLayoutParams = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT );
                childLayoutParams.weight = 0.2f;

                // tag will be used to identify the view index
                hourForecastView.setTag( i );

                // apply the layout params to the child view
                hourForecastView.setLayoutParams( childLayoutParams );

                // add the newly generated view to the parent view
                hourlyForecastGrid.addView( hourForecastView );

                // listen for user touch of the hourly forecast view
                hourForecastView.setOnClickListener( new View.OnClickListener()
                {
                    @Override
                    public void onClick( View v )
                    {
                        LastWeatherData.WeatherData.HourlyForecast.HourForecast selectHourForecast =
                            WeatherLionApplication.storedData.getHourlyForecast().get(
                                Integer.parseInt( v.getTag().toString() ) );

                        String message = String.format( "Expect %s skies at %s with a high of %s%s.",
                                ( selectHourForecast.getCondition().toLowerCase().contains( "sky" ) ?
                                    selectHourForecast.getCondition().toLowerCase().replace( "sky",
                                    "" ).trim() : selectHourForecast.getCondition().toLowerCase() ),
                                selectHourForecast.getTime().toLowerCase(),
                                selectHourForecast.getTemperature(),
                                ( WeatherLionApplication.storedPreferences.getUseMetric() ?
                                        WeatherLionApplication.CELSIUS : WeatherLionApplication.FAHRENHEIT
                                ) );

                        UtilityMethod.showMessageDialog( message, "Hour Forecast", mContext );
                    }// end of method onClick
                });

                if( i == 4 )
                {
                    break;
                }// end of if block
            }// end of for loop
        }// end of if block
        else
        {
            hourlyForecast.setVisibility( View.GONE );
        }// end of else block

        fiveDayForecastList = new ArrayList<>(
                WeatherLionApplication.storedData.getDailyForecast() );

        WeeklyForecastAdapter weeklyForecastAdapter = new WeeklyForecastAdapter(
                fiveDayForecastList );
        forecastRecyclerView.setAdapter( weeklyForecastAdapter );

        detailsScroll = findViewById( R.id.scrDetails );

        detailsScroll.post(
            new Runnable()
            {
                public void run()
                {
                    // scroll to the top of the scroll view
                    detailsScroll.smoothScrollTo( 0, 0 );
                    //detailsScroll.fullScroll( View.FOCUS_UP );
                }// end of method run
            });

        // only enable the swipe refresh if the scroll view is at the top
        detailsScroll.getViewTreeObserver().addOnScrollChangedListener(
            new ViewTreeObserver.OnScrollChangedListener()
            {
                @Override
                public void onScrollChanged()
                {
                    if ( detailsScroll.getScrollY() == 0 )
                    {
                        appRefresh.setEnabled( true );
                    }// end of if block
                    else
                    {
                        appRefresh.setEnabled( false );
                    }// end of else block
                }// end of method onScrollChanged
            });

        appRefresh = findViewById( R.id.swlRefresh );
        appRefresh.setRefreshing( false );

        appRefresh.setColorSchemeResources(
                R.color.aqua,
                R.color.frosty,
                R.color.lion,
                R.color.rabalac
        );

        appRefresh.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                UtilityMethod.refreshRequestedBySystem = false;
                UtilityMethod.refreshRequestedByUser = true;
                refreshWeather();
            }
        });

        currentCity.setLength( 0 );
        currentCity.append( WeatherLionApplication.storedData.getLocation().getCity() );

        currentCountry.setLength( 0 );
        currentCountry.append( WeatherLionApplication.storedData.getLocation().getCountry() );

        currentCondition.setLength( 0 ); // reset
        currentCondition.append( UtilityMethod.validateCondition(
                WeatherLionApplication.storedData.getCurrent().getCondition() ) );

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

        txvCurrentCondition.setText( currentCondition.toString() );

        updateTemps(); // call update temps here

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

        String formalName = null;
        String[] cityBreakdown = null;
        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences( this );

        currentLocation.setLength( 0 );
        currentLocation.append( spf.getString( WeatherLionApplication.CURRENT_LOCATION_PREFERENCE,
                Preference.DEFAULT_WEATHER_LOCATION ) );

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

                formalName = String.format( "%s, %s", cityName, stateName );
            }// end of if block
            else
            {
                formalName = currentLocation.toString();
            }// end of else block
        }// end of if block

        txvWeatherLocation = findViewById( R.id.txvCurrentWeatherLocation );
        txvWeatherLocation.setText( formalName );
        txvWeatherLocation.setTypeface( WeatherLionApplication.currentTypeface );

        imvShowPreviousSearches = findViewById( R.id.imvShowList );

        imvShowPreviousSearches.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                showPreviousSearches( txvWeatherLocation );
            }
        });

        txvWeatherLocation.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                // Process the onclick event only if there is a list to be shown
                if( imvShowPreviousSearches.getVisibility() == View.VISIBLE )
                {
                    showPreviousSearches( txvWeatherLocation );
                }// end of if block
            }
        });

        loadPreviousSearches();

        // Load current condition weather image
        String currentConditionIcon = UtilityMethod.getConditionIcon( currentCondition, null );

        ImageView imvCurrentConditionImage = findViewById( R.id.imvCurrentCondition );
        String imageFile = String.format( "weather_images/%s/weather_%s",
            WeatherLionApplication.iconSet, currentConditionIcon );

        loadWeatherIcon( imvCurrentConditionImage, imageFile );

        String backdropFile = String.format( "weather_backgrounds/background_%s",
            Objects.requireNonNull( currentConditionIcon )
                .replace(".png", ".jpg" ) );

        loadWeatherBackdrop( rootView, backdropFile );

        // Update the weather provider
        ImageView imvWeatherProviderLogo = findViewById( R.id.imvWeatherProviderLogo );

        TextView txvWeatherProvider = findViewById( R.id.txvWeatherProviderName );
        txvWeatherProvider.setText( WeatherLionApplication.storedData.getProvider().getName() );
        txvWeatherProvider.setTypeface( WeatherLionApplication.currentTypeface );

        String storedProviderName = WeatherLionApplication.storedData.getProvider()
                .getName().equalsIgnoreCase( WeatherLionApplication.YAHOO_WEATHER ) ?
                WeatherLionApplication.storedData.getProvider().getName()
                        .replaceAll( "!", "" ) :
                WeatherLionApplication.storedData.getProvider()
                        .getName();

        String providerIcon = String.format( "%s%s", "wl_",
                storedProviderName.toLowerCase().replaceAll( " ", "_" ) );

        imvWeatherProviderLogo.setImageResource( UtilityMethod.getImageResourceId( providerIcon ) );

        txvLastUpdated = findViewById( R.id.txvLastUpdated );
        txvLastUpdated.setTypeface( WeatherLionApplication.currentTypeface );

        if( WeatherLionApplication.currentLocationTimeZone != null )
        {
            txcLocalTime.setTimeZone(
                    WeatherLionApplication.currentLocationTimeZone.getTimezoneId() );
        }// end of if block

        if( timeUpdated != null )
        {
            txvLastUpdated.setText( String.format( "%s%s", "Updated ",
                    UtilityMethod.getTimeSince( timeUpdated ) ) );
        }// end of if block

        if( loadingDialog != null )
        {
            stopLoading();
            loadingDialog.dismiss();
        }// end of if block

        if( !UtilityMethod.hasInternetConnection( this ) )
        {
           noInternetAlert( rootView );
        }// end of if block

        // load the applicable typeface in use
        UtilityMethod.loadCustomFont( rootView );
    }// end of method loadMainActivityWeather

    /**
     * Load a list of previous place that were searched for
     */
    private void loadPreviousSearches()
    {
        List< CityData > previousSearches = JSONHelper.importPreviousCitySearches();
        List< String > searchList = new ArrayList<>();

        // when the program is first runs there will be no previous searches so
        // this function does nothing on the first run
        if( previousSearches != null )
        {
            for ( CityData city : previousSearches )
            {
                String c;

                if( city.getRegionCode() != null && !UtilityMethod.isNumeric( city.getRegionCode() ) )
                {
                    c = city.getCityName() + ", " + city.getRegionCode();

                    if( !WeatherLionApplication.storedPreferences.getLocation().equals( c ) )
                    {
                        searchList.add( c );
                    }// end of if block
                }// end of if block
                else
                {
                    c = city.getCityName() + ", " + city.getCountryName();

                    if( !WeatherLionApplication.storedPreferences.getLocation().equals( c ) )
                    {
                        searchList.add( c );
                    }// end of if block
                }// end of else block
            }// end of for each loop

            Collections.sort( searchList );

        }// end of if block

        listItems = searchList.toArray( new String[ 0 ] );

        if( listItems.length > 0 )
        {
            String cc = txvWeatherLocation.getText().toString();

            if( listItems.length >= 1 )
            {
                /* If the only city in the list is other than the current city
                 * then show the popup list.
                 */
                if( !listItems[ 0 ].equalsIgnoreCase(
                        WeatherLionApplication.storedPreferences.getLocation() ) )
                {
                    imvShowPreviousSearches.setVisibility( View.VISIBLE );
                }// end of if block
                else
                {
                    imvShowPreviousSearches.setVisibility( View.INVISIBLE );
                }// end of else block
            }// end of if block
            else
            {
                imvShowPreviousSearches.setVisibility( View.INVISIBLE );
            }// end of else block
        }// end of if block
        else
        {
            imvShowPreviousSearches.setVisibility( View.INVISIBLE );
        }// end of else block
    }// end of method loadPreviousSearches

    /**
     * Displays a dialog alerting the user to activate the device's GPS radio.
     */
    private void noGpsAlert()
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( mContext );
        // return the use gps switch to the off setting
        settings.edit().putBoolean( WeatherLionApplication.USE_GPS_LOCATION_PREFERENCE,
                false ).apply();

        responseDialog( WeatherLionApplication.PROGRAM_NAME + " - No GPS",
                "Your GPS seems to be disabled, do you want to enable it?",
                "Yes", "No","openGPSSettings",
                null, null, null );
    }// end of method noGpsAlert

    /**
     * Displays a SnackBar alerting the user to activate the device's Wifi radio.
     */
    private void noInternetAlert( View activityView )
    {
        Snackbar internetCafe = Snackbar.make( activityView,
                "No Internet Connection", Snackbar.LENGTH_INDEFINITE ).setAction("OPEN SETTINGS",
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        showPreferenceActivity( false );
                    }// end of method onClick
                });
        internetCafeView = internetCafe.getView();
        internetCafeView.setBackgroundColor(
                UtilityMethod.addOpacity( WeatherLionApplication.systemColor.toArgb() ,
                        90 ) );
        TextView infoText = internetCafeView.findViewById(
                android.support.design.R.id.snackbar_text );
        infoText.setTextColor( Color.YELLOW );
        internetCafe.setActionTextColor( Color.RED );
        internetCafe.show();
    }// end of method noInternetAlert

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        mContext = this;
        requestWindowFeature( Window.FEATURE_NO_TITLE ); //will hide the title
        Objects.requireNonNull( getSupportActionBar() ).hide(); // hide the title bar
        this.getWindow().setStatusBarColor( WeatherLionApplication.systemColor.toArgb() );

        this.getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN ); //enable full screen

        // create and register the local application broadcast receiver
        IntentFilter appBroadcastFilter = new IntentFilter();
        appBroadcastFilter.addAction( WidgetUpdateService.WEATHER_LOADING_ERROR_MESSAGE );
        appBroadcastFilter.addAction( PrefsActivity.ICON_SWITCH );
        appBroadcastFilter.addAction( WidgetUpdateService.ASTRONOMY_CHANGE );
        appBroadcastFilter.addAction( RECYCLER_ITEM_CLICK );
        appBroadcastFilter.addAction( WeatherDataXMLService.WEATHER_XML_STORAGE_MESSAGE );
        LocalBroadcastManager.getInstance( this ).registerReceiver( appBroadcastReceiver,
                appBroadcastFilter );

        // create and register the system broadcast receiver
        IntentFilter systemFilter = new IntentFilter();
        systemFilter.addAction( Intent.ACTION_TIME_TICK );
        systemFilter.addAction( ConnectivityManager.CONNECTIVITY_ACTION );
        this.registerReceiver( systemEventsBroadcastReceiver, systemFilter );

        // Check if any previous weather data is stored locally
        if( WeatherLionApplication.firstRun &&
                !WeatherLionApplication.localWeatherDataAvailable )
        {
            initializeWelcomeWindow();

            if( checkGeoAccess() )
            {
                accessLoaded();
            }// end of if block
        }// end of if block
        else
        {
            String widBackgroundColor = WeatherLionApplication.spf.getString( WeatherLionApplication.WIDGET_BACKGROUND_PREFERENCE,
                Preference.DEFAULT_WIDGET_BACKGROUND );

            if( widBackgroundColor != null )
            {
                switch( widBackgroundColor.toLowerCase() )
                {
                    case WeatherLionApplication.AQUA_THEME:
                        setTheme( R.style.AquaTheme );
                        break;
                    case WeatherLionApplication.FROSTY_THEME:
                        setTheme( R.style.FrostyTheme );
                        break;
                    case WeatherLionApplication.RABALAC_THEME:
                        setTheme( R.style.RabalacTheme );
                        break;
                    default:
                        setTheme( R.style.LionTheme );
                        break;
                }// end of switch block
            }// end of if block

            WeatherLionApplication.callMethodByName( null, "checkForStoredWeatherData",
                    null, null );

            View keysDialogView = View.inflate( this, R.layout.wl_data_keys_layout, null);
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
                    WeatherLionApplication.currentWxLocation =
                            WeatherLionApplication.lastDataReceived.getWeatherData().getLocation().getCity();

                    // if there is no location stored in the local preferences, go to the settings activity
                    if( WeatherLionApplication.currentWxLocation.equalsIgnoreCase(
                            Preference.DEFAULT_WEATHER_LOCATION ) )
                    {
                        showPreferenceActivity( false );
                        return;
                    }// end of if block
                    else
                    {
                        initializeMainWindow();
                    }// end of if block

                    if( WeatherLionApplication.useGps && !WeatherLionApplication.gpsRadioEnabled )
                    {
                        noGpsAlert();
                    }// end of if block

                    initializeMainWindow();
                    loadMainActivityWeather();
                }// end of if block
            }// end of if block
            else if( WeatherLionApplication.storedPreferences != null )
            {

                if( !WeatherLionApplication.storedPreferences.getLocation().equals(
                        Preference.DEFAULT_WEATHER_LOCATION ) )
                {
                    /* If the preferences are stored correctly and there is no weather data
                    stored the service data service must obtain the required data based on
                    the stored preferences
                    */
                    if( UtilityMethod.hasInternetConnection( WeatherLionApplication.getAppContext() ) )
                    {
                        WeatherLionApplication.restoringWeatherData = true;
                        UtilityMethod.refreshRequestedBySystem = true;
                        UtilityMethod.refreshRequestedByUser = false;

                        String invoker = this.getClass().getSimpleName() + "::onCreate";
                        WeatherLionApplication.callMethodByName( null,"refreshWeather",
                                new Class[]{ String.class }, new Object[]{ invoker } );

                        showLoadingDialog( "Refreshing Data..." );
                        initializeWelcomeWindow();
                        // Have a loading screen displayed in the mean time
                    }// end of if block
                }// end of if block
                else
                {
                    initializeWelcomeWindow();
                }//end of else block

            }// end of else if block
            else
            {
                initializeWelcomeWindow();
            }//end of else block
        }// end of else block
    }// end of method onCreate

    /**
     * Prepare the main window for activity
     */
    private void initializeMainWindow()
    {
        setContentView( R.layout.wl_main_activity );
        View mainActivity = findViewById( R.id.main_window );

        if( !UtilityMethod.hasInternetConnection( this ) )
        {
            noInternetAlert( mainActivity );
        }// end of if block

        forecastRecyclerView = findViewById( R.id.lstDayForecast );
        forecastRecyclerView.setHasFixedSize( true );

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( this );
        forecastRecyclerView.setLayoutManager( layoutManager );

        Drawable dividerDrawable = getDrawable( R.drawable.wl_forecast_list_divider );
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration( dividerDrawable );
        forecastRecyclerView.addItemDecoration( dividerItemDecoration );

        txcLocalTime = findViewById( R.id.tcLocalTime );
        txcLocalTime.setTypeface( WeatherLionApplication.currentTypeface );

        doGlimpseRotation( findViewById( R.id.imvBlade ) );
    }// end of method initializeMainWindow

    /**
     * Prepare the welcome window for activity
     */
    private void initializeWelcomeWindow()
    {
        setContentView( R.layout.wl_welcome_activity );

        UtilityMethod.loadCustomFont( (RelativeLayout) findViewById( R.id.weather_main_container) );

        TextView txvMessage = findViewById(R.id.txvMessage);
        txvMessage.setText( HtmlCompat.fromHtml( getString( R.string.announcement ), 0 ) );
        View welcomeActivity = findViewById( R.id.weather_main_container);

        if( !UtilityMethod.hasInternetConnection( this ) )
        {
            noInternetAlert( welcomeActivity );
        }// end of if block
    }// end of method initializeWelcomeWindow

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

        this.unregisterReceiver( systemEventsBroadcastReceiver );

        LocalBroadcastManager.getInstance( WeatherLionApplication.getAppContext() )
                .unregisterReceiver( appBroadcastReceiver );
    }// end of method onDestroy

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        WeatherLionApplication.mainWindowShowing = false;
    }// end of method onPause

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onResume()
    {
        super.onResume();

        WeatherLionApplication.mainWindowShowing = true;

        //this.getWindow().setStatusBarColor( WeatherLionApplication.systemColor.toArgb() );

        removeInternetAlert();

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
                if( WeatherLionApplication.storedData.getProvider().getDate() != null )
                {
                    UtilityMethod.lastUpdated = df.parse(
                            WeatherLionApplication.storedData.getProvider().getDate() );

                    WeatherLionApplication.currentSunriseTime = new StringBuilder(
                            WeatherLionApplication.storedData.getAstronomy().getSunrise() );
                    WeatherLionApplication.currentSunsetTime = new StringBuilder(
                            WeatherLionApplication.storedData.getAstronomy().getSunset() );
                }// end of if block
            }// end of try block
            catch ( ParseException e )
            {
                UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, "Unable to parse last weather data date.",
                        TAG + "::onCreate [line: " +
                                e.getStackTrace()[1].getLineNumber()+ "]" );
            }// end of catch block
        }// end of if block

        if( WeatherLionApplication.lastDataReceived != null )
        {
            if( WeatherLionApplication.lastDataReceived.getWeatherData().getLocation().getCity() != null )
            {
                if( !findViewById( R.id.weather_main_container ).getTag().equals( "main_screen" ) )
                {
                    initializeMainWindow();
                    doGlimpseRotation( findViewById( R.id.imvBlade ) );
                    loadMainActivityWeather();
                }// end of if block
            }// end of if block
        }// end of if block

        if( txvLastUpdated != null && UtilityMethod.lastUpdated != null )
        {
            txvLastUpdated.setTypeface( WeatherLionApplication.currentTypeface );

            if( WeatherLionApplication.currentLocationTimeZone != null )
            {
                txcLocalTime.setTimeZone(
                        WeatherLionApplication.currentLocationTimeZone.getTimezoneId() );
            }// end of if block

            txvLastUpdated.setText( String.format( "%s%s", "Updated ",
                    UtilityMethod.getTimeSince( UtilityMethod.lastUpdated ) ) );
        }// end of if block

        // if an update is required but was not performed
        if( UtilityMethod.updateRequired( WeatherLionMain.this ) )
        {
            UtilityMethod.refreshRequestedBySystem = true;
            UtilityMethod.refreshRequestedByUser = false;

            String invoker = WeatherLionMain.this.getClass().getSimpleName() +
                    "::onResume";
            WeatherLionApplication.callMethodByName( null,
                    "refreshWeather",
                    new Class[]{ String.class }, new Object[]{ invoker } );
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
     * Refreshes the data in the main activity view
     */
    private void refreshWeather()
    {
        if( UtilityMethod.hasInternetConnection( WeatherLionApplication.getAppContext() ) )
        {
            if( UtilityMethod.updateRequired( this ) )
            {
                showLoadingDialog( "Refreshing widget" );

                String invoker = this.getClass().getSimpleName() + "::refreshWeather";
                WeatherLionApplication.callMethodByName( null,
            "refreshWeather", new Class[]{ String.class }, new Object[]{ invoker } );
            }// end of if block
        }// end of if block
    }// end of method refreshWeather

    /**
     * Remove the no internet connection {@code Snackbar}
     */
    private void removeInternetAlert()
    {
        if( UtilityMethod.hasInternetConnection( mContext ) )
        {
            if( internetCafeView != null )
            {
                if( internetCafeView.getVisibility() == View.VISIBLE )
                {
                    // animate the view downward before removing it from the layout
                    internetCafeView.animate()
                        .translationY( 200 )
                        .setDuration( 500 )
                        .withEndAction(
                            new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    ( (ViewGroup) internetCafeView.getParent() )
                                            .removeView( internetCafeView );
                                }// end of method run
                            });
                }// end of if block
            }// end of if block
        }// end of if block
    }// end of method removeInternetAlert

    /**
     * Animate the footer layout
     */
    private void animateFooter( int direction )
    {
        final RelativeLayout footerBar = findViewById( R.id.rlWeatherFooter );

        LinearLayout.LayoutParams params;

        // upward
        if( direction == 1 )
        {
            footerBar.setVisibility( View.VISIBLE );
            // animate the view upward
            footerBar.animate().translationY( 0 ).setDuration( 450 );
        }// end of if block
        else if( direction == 0 )
        {
            // animate the view downward
            footerBar.animate().translationY( footerBar.getHeight() ).setDuration( 450 ) .withEndAction(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        footerBar.setVisibility( View.INVISIBLE );
                    }
                });
        }// end of else if block
    }// end of method animateFooter

    /**
     * Display a dialog eliciting a response from the user
     *
     * @param prompt    The prompt to be displayed to the user
     * @param title     The title of the dialog box
     * @param posResponse   The text to be displayed on the positive response button
     * @param negResponse   The text to be displayed on the negative response button
     * @param positiveAction A string representing a method that should be called after the user click's on the positive button
     * @param negativeAction A string representing a method that should be called after the user click's on the negative button
     * @param params          An array representing the param value example new Object[]{"GeoNames"} or null can be passed.
     * @param paramClassTypes   An array representing the param type example new Class[]{String.class} or null can be passed.
     */
    private void responseDialog( String title, String prompt, String posResponse,
                                 String negResponse, final String positiveAction,
                                 final String negativeAction, final Object[] params, final Class[] paramClassTypes )
    {
        final AlertDialog responseDialog = new AlertDialog.Builder( this ).create();
        View responseDialogView = View.inflate( WeatherLionMain.this, R.layout.wl_response_dialog, null );

        UtilityMethod.loadCustomFont( (LinearLayout) responseDialogView.findViewById(
                R.id.llResponseDialog ) );

        RelativeLayout rlTitleBar = responseDialogView.findViewById( R.id.rlDialogTitleBar );
        GradientDrawable bgShape = (GradientDrawable) rlTitleBar.getBackground().getCurrent();
        bgShape.setColor( WeatherLionApplication.systemColor.toArgb() );

        TextView txvDialogTitle = responseDialogView.findViewById( R.id.txvDialogTitle );
        TextView txvDialogMessage = responseDialogView.findViewById( R.id.txvMessage );

        Button btnPositive = responseDialogView.findViewById( R.id.btnPositive );
        btnPositive.setBackground( WeatherLionApplication.systemButtonDrawable );
        Button btnNegative = responseDialogView.findViewById( R.id.btnNegative );
        btnNegative.setBackground( WeatherLionApplication.systemButtonDrawable );

        txvDialogTitle.setText( title );
        txvDialogMessage.setText( prompt );
        btnPositive.setText( posResponse );
        btnNegative.setText( negResponse );

        responseDialog.setView( responseDialogView );
        responseDialog.setCancelable( false );

        Objects.requireNonNull( responseDialog.getWindow() ).setBackgroundDrawable(
                new ColorDrawable( Color.TRANSPARENT ) );
        UtilityMethod.loadCustomFont( (LinearLayout) responseDialogView.findViewById(
                R.id.llResponseDialog ) );

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

                responseDialog.dismiss();
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

                responseDialog.dismiss();
            }
        });

        responseDialog.show();

        // adjust the layout after the window is displayed
        Window dialogWindow = responseDialog.getWindow();
        dialogWindow.setLayout( CustomPreferenceGrid.DEFAULT_DIALOG_WIDTH,
                ViewGroup.LayoutParams.WRAP_CONTENT );
        dialogWindow.setGravity( Gravity.CENTER );
    }// end of method responseDialog

    /**
     * Displays a popup menu
     *
     * @param v The view to which the popup menu should be anchored
     */
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

    /**
     * Display the preference activity view
     *
     * @param locationSet A {@code boolean} representing weather or not the user has specified a location
     *                    for the weather data.
     */
    private void showPreferenceActivity( boolean locationSet )
    {
        Intent settingsIntent = new Intent( this, PrefsActivity.class );
        settingsIntent.putExtra( LION_LOCATION_PAYLOAD, locationSet );
        startActivity( settingsIntent );
    }// end of method showPreferenceActivity

    /**
     * Displays a popup window containing previous cities searched for.
     *
     * @param anchor  The view to which the popup menu should be anchored
     */
    private void showPreviousSearches( View anchor )
    {
        ArrayAdapter arrayAdapter = new ArrayAdapter<>( this,
                R.layout.wl_popup_list_item_dark_bg, listItems );
        ImageView imvShowList = findViewById( R.id.imvShowList );
        int arrowWidth = imvShowList.getWidth();
        popupWindow = new ListPopupWindow( this );
        popupWindow.setAnchorView( anchor );
        popupWindow.setAdapter( arrayAdapter );
        popupWindow.setContentWidth( measureContentWidth( arrayAdapter, anchor ) +
                arrowWidth );  // add the width of the arrow to the total width
        popupWindow.setVerticalOffset( 6 );

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
                    popupWindow.setBackgroundDrawable( this.getDrawable( R.drawable.wl_round_list_popup_aqua ) );
                    break;
                case WeatherLionApplication.FROSTY_THEME:
                    popupWindow.setBackgroundDrawable( this.getDrawable( R.drawable.wl_round_list_popup_frosty ) );
                    break;
                case WeatherLionApplication.RABALAC_THEME:
                    popupWindow.setBackgroundDrawable( this.getDrawable( R.drawable.wl_round_list_popup_rabalac ) );
                    break;
                case WeatherLionApplication.LION_THEME:
                    popupWindow.setBackgroundDrawable( this.getDrawable( R.drawable.wl_round_list_popup_lion ) );
                    break;
            }// end of switch block
        }// end of if block

        // if the list has more than 9 elements we will set the height if the window manually
        if( listItems.length > 9 )
        {
            popupWindow.setHeight( 776 );   // random height value
        }// end of if block

        popupWindow.setOnItemClickListener( new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id )
            {
                SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences(
                        WeatherLionApplication.getAppContext() );
                String savedLocation = spf.getString( WeatherLionApplication.CURRENT_LOCATION_PREFERENCE,
                        Preference.DEFAULT_WEATHER_LOCATION );

                String selection = listItems[ + position ];
                currentLocation.setLength( 0 );
                currentLocation.append( selection );
                int i = 0;
                String formalName = null;
                String[] cityBreakdown = null;

                if( selection.contains( "," ) )
                {
                    cityBreakdown = selection.split( "," );
                }// end of if block

                if( cityBreakdown != null )
                {
                    if( UtilityMethod.usStatesByCode.get( cityBreakdown[ 1 ].trim() ) != null )
                    {
                        String cityName = cityBreakdown[ 0 ].trim();
                        String stateName = UtilityMethod.usStatesByCode.get( cityBreakdown[ 1 ].trim() );

                        formalName = String.format( "%s, %s", cityName, stateName );
                    }// end of if block
                    else
                    {
                        formalName = selection;
                    }// end of else block
                }// end of if block

                txvWeatherLocation.setText( formalName );

                if( !currentLocation.toString().equalsIgnoreCase( savedLocation ) )
                {
                    WidgetUpdateService.widgetRefreshRequired = true;
                    spf.edit().putString( WeatherLionApplication.CURRENT_LOCATION_PREFERENCE,
                            currentLocation.toString() ).apply();

                    WeatherLionApplication.storedPreferences.setLocation( currentLocation.toString() );
                    WeatherLionApplication.currentWxLocation = currentCity.toString();

                    // send out a broadcast to the widget service that the location preference has been modified
                    UtilityMethod.refreshRequestedBySystem = false;
                    UtilityMethod.refreshRequestedByUser = true;

                    String invoker = this.getClass().getSimpleName() + "::showPreviousSearches";
                    WeatherLionApplication.callMethodByName( null,
                            "refreshWeather",
                            new Class[]{ String.class }, new Object[]{ invoker } );

                    UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                            "Switching cities",
                            TAG + "::showPreviousSearches" );
                }// end of if block

                popupWindow.dismiss();
            }// end of anonymous method onItemClick
        });

        popupWindow.show();
    }// end of method showPreviousSearches

    /**
     * Measures the width of the popup window based on the size of it's content.
     *
     * @param listAdapter The list adapter to be used for the popup window
     * @return An {@code Integer} value representing the calculated width of the window
     * @author alerant
     * <br />
     * {@link 'https://stackoverflow.com/a/26814964'}
     */
    private int measureContentWidth( ListAdapter listAdapter, View anchor )
    {
        ViewGroup mMeasureParent = null;
        int maxWidth = 0;
        View itemView = null;
        int itemType = 0;

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED );
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0,
                View.MeasureSpec.UNSPECIFIED );
       int count = listAdapter.getCount();

        for ( int i = 0; i < count; i++ )
        {
            final int positionType = listAdapter.getItemViewType( i );

            if ( positionType != itemType )
            {
                itemType = positionType;
                itemView = null;
            }// end of if block

            if ( mMeasureParent == null )
            {
                mMeasureParent = new FrameLayout(mContext);
            }// end of if block

            itemView = listAdapter.getView( i, itemView, mMeasureParent );
            itemView.measure( View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED );
            int itemWidth = itemView.getMeasuredWidth() + itemView.getPaddingEnd();

            if( itemWidth > maxWidth )
            {
                maxWidth = itemWidth;
            }// end of if block
        }// end of for loop

        anchor.measure( View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED );
        int anchorWidth =  anchor.getMeasuredWidth();

        if( anchorWidth > maxWidth )
        {
            maxWidth += anchorWidth - maxWidth; // add the difference to the max width
        }// end of if block

        return maxWidth;
    }// end of method measureContentWidth

    /**
     * Displays a dialog for key entry or deletion.
     *
     * @param defaultSelection The entry which will be selected by default on open.
     */
    private void showDataKeysDialog( String defaultSelection )
    {
        final View keyDialogView = View.inflate( this, R.layout.wl_data_keys_layout,
                null );
        keyEntryDialog = new AlertDialog.Builder( mContext ).create();
        keyEntryDialog.setView( keyDialogView );

        RelativeLayout rlTitleBar = keyDialogView.findViewById( R.id.rlDialogTitleBar );
        //rlTitleBar.setBackgroundColor( WeatherLionApplication.systemColor.toArgb() );

        GradientDrawable bgShape = (GradientDrawable) rlTitleBar.getBackground().getCurrent();
        bgShape.setColor( WeatherLionApplication.systemColor.toArgb() );

        ImageView imvClose = keyDialogView.findViewById( R.id.imvCloseDialog );
        Spinner spnAccessProvider = keyDialogView.findViewById( R.id.spnAccessProvider );
        rlKeyNameParent = keyDialogView.findViewById( R.id.spnKeyNameParent );
        spnProviderKeys = keyDialogView.findViewById( R.id.spnKeyName );
        edtKeyName = keyDialogView.findViewById( R.id.edtKeyName );
        pwdKeyValue = keyDialogView.findViewById( R.id.edtKeyValue );
        CheckBox chkShowPwd = keyDialogView.findViewById( R.id.cbShowPwd );

        int[][] states = { { android.R.attr.state_checked }, {} };
        int[] colors = { WeatherLionApplication.systemColor.toArgb(),
                WeatherLionApplication.systemColor.toArgb() };
        CompoundButtonCompat.setButtonTintList( chkShowPwd,
                new ColorStateList( states, colors ) );

        ImageView imvAccessProviderDropArrow = keyDialogView.findViewById(
                R.id.imvAccessProviderDropArrow );

        imvAccessProviderDropArrow.setColorFilter( WeatherLionApplication.systemColor.toArgb() );
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

        //ArrayAdapter adapter = new ArrayAdapter( mContext,
        // R.layout.wl_access_provider_spinner_style, accessNeededProviders );

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

        spnAccessProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
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

                pwdKeyValue.setSelection( pwdKeyValue.getText().length() );
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
                String keyName;
                String keyValue = null;
                String message;

                if( edtKeyName.getVisibility() == View.VISIBLE )
                {
                    keyName = edtKeyName.getText().toString().replaceAll( "_", " " );

                    if( edtKeyName == null || edtKeyName.getText().toString().length() == 0
                            || edtKeyName.getText().toString().equals( "" ) )
                    {
                        message = String.format( "Please enter the name of the key as given by %s!",
                                WeatherLionApplication.selectedProvider );

                        UtilityMethod.butteredToast( mContext, message,2, Toast.LENGTH_LONG );
                        edtKeyName.requestFocus();
                    }// end of if block
                    else if( pwdKeyValue == null || pwdKeyValue.getText().toString().length() == 0
                            || pwdKeyValue.getText().toString().equals( "" ) )
                    {
                        message = String.format( "Please enter the %s supplied by %s!", keyName,
                            WeatherLionApplication.selectedProvider );

                        UtilityMethod.butteredToast( mContext,message,
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
                        keyName = edtKeyName.getText().toString().replaceAll( "_", " " );
                        message = String.format( "Please enter the %s supplied by %s!", keyName,
                                WeatherLionApplication.selectedProvider );

                        UtilityMethod.butteredToast( mContext, message, 2, Toast.LENGTH_LONG );
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

                switch ( WeatherLionApplication.selectedProvider )
                {
                    case WeatherLionApplication.HERE_MAPS:
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
                    case WeatherLionApplication.YAHOO_WEATHER:
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

//        keyEntryDialog.setCancelable( wxOnly.contains( WeatherLionApplication.GEO_NAMES ) ); // User is only allowed to cancel if GeoNames account exists
        keyEntryDialog.setCancelable( false ); // User is only allowed to cancel by using the close or finish buttons
        Objects.requireNonNull( keyEntryDialog.getWindow() ).setBackgroundDrawable(
                new ColorDrawable( Color.TRANSPARENT ) );
        UtilityMethod.loadCustomFont( (RelativeLayout) keyDialogView.findViewById( R.id.rlKeysDialog ) );
        keyEntryDialog.show();
        keyEntryDialog.findViewById( R.id.edtKeyName ).requestFocus();
    }// end of method showDataKeysDialog

    private void showLoadingDialog( String loadingMessage )
    {
        View loadingDialogView = View.inflate( this, R.layout.wl_loading_data_layout,
                null );
        //loadingDialogView.setBackgroundColor(Color.parseColor("#FF4A4A4A"));

        loadingDialog = new AlertDialog.Builder( mContext ).create();
        loadingDialog.setView( loadingDialogView );

        ImageView strollingLion = loadingDialogView.findViewById( R.id.imvStrollingLion );
        TextView txvLoadingData = loadingDialogView.findViewById(R.id.txvLoadingData );

        txvLoadingData.setText( String.format( "%s%s", loadingMessage, "..." ) );

        loadingAnimation = (AnimationDrawable) strollingLion.getDrawable();
        loadingDialog.setCancelable( false );

        loadingDialog.show();

        if( loadingDialog.getWindow() != null )
        {
            loadingDialog.getWindow().setLayout( 500, 500 );
            loadingDialog.getWindow().setBackgroundDrawable( new ColorDrawable(
                    Color.TRANSPARENT ) );
        }// end of if block

        startLoading();
    }// end of method showLoadingDialog

    public void startLoading()
    {
        loadingAnimation.start();
    }// end of method startLoading

    public void stopLoading()
    {
        if( loadingAnimation != null )
        {
            loadingAnimation.stop();
        }// end of if block
    }// end of method stopLoading

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
        TextView txvTitle = messageDialogView.findViewById( R.id.txvDialogTitle );
        TextView txvMessage = messageDialogView.findViewById( R.id.txvMessage );

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

        // adjust the layout after the window is displayed
        Window dialogWindow = messageDialog.getWindow();
        dialogWindow.setLayout( CustomPreferenceGrid.DEFAULT_DIALOG_WIDTH,
                ViewGroup.LayoutParams.WRAP_CONTENT );
        dialogWindow.setGravity( Gravity.CENTER );
    }// end of method showMessageDialog

    /**
     * Updates the windows appearance based on the time of day
     */
    private void updateAstronomy( String timeOfDay )
    {
        WeatherLionApplication.storedData =
            WeatherLionApplication.lastDataReceived.getWeatherData();
        String currentConditionIcon = null;

        switch( timeOfDay )
        {
            case WidgetUpdateService.SUNRISE:
                currentConditionIcon = UtilityMethod.weatherImages.get(
                    WeatherLionApplication.storedData.getCurrent().
                        getCondition().toLowerCase() );

                UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                    String.format( "Switching main activity to sunrise icon %s!",
                        currentConditionIcon ),TAG + "::updateAstronomy" );
                break;
            case WidgetUpdateService.SUNSET:
                if (  WeatherLionApplication.storedData.getCurrent().
                        getCondition().toLowerCase().contains( "(night)" ) )
                {
                    currentConditionIcon = UtilityMethod.weatherImages.get(
                        WeatherLionApplication.storedData.getCurrent().
                            getCondition().toLowerCase() );
                }// end of if block
                else
                {
                    if ( UtilityMethod.weatherImages.containsKey(
                            WeatherLionApplication.storedData.getCurrent().
                                getCondition().toLowerCase() + " (night)" ) )
                    {
                        currentConditionIcon =
                            UtilityMethod.weatherImages.get(
                                WeatherLionApplication.storedData.getCurrent().
                                    getCondition().toLowerCase() + " (night)" );

                        UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                    "Switching main activity to sunset icon to " + currentConditionIcon,
                        TAG + "::updateAstronomy" );
                    }// end of if block
                    else
                    {
                        UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                            String.format( "No night icon exists for %s!",
                                WeatherLionApplication.storedData.getCurrent().
                                            getCondition() ),
                                TAG + "::updateAstronomy" );

                        // there will most likely be a day icon but not a night one so exit here
                        return;
                    }// end of else block
                }// end of else block

                break;
        }// end of switch block

        // Load applicable icon based on the time of day
        ImageView imvCurrentConditionImage = findViewById( R.id.imvCurrentCondition );
        String imageFile = String.format( "weather_images/%s/weather_%s",
            WeatherLionApplication.iconSet, currentConditionIcon );

        loadWeatherIcon( imvCurrentConditionImage, imageFile );

        String backdropFile = String.format( "weather_backgrounds/background_%s",
            Objects.requireNonNull( currentConditionIcon )
                .replace(".png", ".jpg" ) );

        ViewGroup rootView = findViewById( R.id.weather_main_container );
        loadWeatherBackdrop( rootView, backdropFile );
    }// end of method updateAstronomy

    /***
     * Update the numerical values displayed on the widget
     */
    private void updateTemps()
    {
        WeatherLionApplication.storedData = WeatherLionApplication.lastDataReceived.getWeatherData();

        TextView txvCurrentTemperature = findViewById( R.id.txvCurrentTemperature );
        txvCurrentTemperature.setTypeface( WeatherLionApplication.currentTypeface );

        TextView txvFeelsLikeTemperature = findViewById( R.id.txvFeelsLikeTemperature );
        txvFeelsLikeTemperature.setTypeface( WeatherLionApplication.currentTypeface );

        TextView txvHighTemp  = findViewById( R.id.txvHighTemp );
        txvHighTemp.setTypeface( WeatherLionApplication.currentTypeface );

        TextView txvLowTemp  = findViewById( R.id.txvLowTemp );
        txvLowTemp.setTypeface( WeatherLionApplication.currentTypeface );

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


        currentWindDirection.setLength( 0 );
        currentWindDirection.append( WeatherLionApplication.storedData.getWind().getWindDirection() );

        TextView txvWindReading = findViewById( R.id.txvWindReading );
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

        // Display weather data in the activity
        String displayCurrentTemp;
        String displayCurrentFeelsLike;
        String displayCurrentHighTemp;
        String displayCurrentLowTemp;
        String displayCurrentWindReading;

        int inputValue;

        if( WeatherLionApplication.storedPreferences.getUseMetric() )
        {
            displayCurrentTemp = String.format( "%s%s",
                Math.round(
                    UtilityMethod.fahrenheitToCelsius( Float.parseFloat( currentTemp.toString() ) ) ),
                        WeatherLionApplication.DEGREES );

            displayCurrentFeelsLike = String.format( "%s %s%s",
                WidgetUpdateService.FEELS_LIKE,
                    Math.round(
                        UtilityMethod.fahrenheitToCelsius( Float.parseFloat(
                            currentFeelsLikeTemp.toString() ) ) ),
                                WeatherLionApplication.DEGREES );

            displayCurrentHighTemp = String.format( "%s%s",
                Math.round( UtilityMethod.fahrenheitToCelsius(
                    Float.parseFloat( currentHigh.toString() ) ) ),
                        WeatherLionApplication.DEGREES );

            displayCurrentLowTemp = String.format( "%s%s",
                Math.round(
                    UtilityMethod.fahrenheitToCelsius( Float.parseFloat( currentLow.toString() ) ) ),
                        WeatherLionApplication.DEGREES );

            displayCurrentWindReading = String.format( "%s %s %s",
                currentWindDirection, currentWindSpeed, "km/h" );

            inputValue = (int) UtilityMethod.fahrenheitToCelsius( Integer.parseInt(
                    currentTemp.toString().replaceAll( "\\D+","" ) ) );
        }// end of if block
        else
        {
            displayCurrentTemp = String.format( "%s%s",
                currentTemp, WeatherLionApplication.DEGREES );

            displayCurrentFeelsLike = String.format( "%s %s%s",
                WidgetUpdateService.FEELS_LIKE,
                    currentFeelsLikeTemp.toString(),
                        WeatherLionApplication.DEGREES );

            displayCurrentHighTemp = String.format( "%s%s",currentHigh.toString(), WeatherLionApplication.DEGREES );

            displayCurrentLowTemp = String.format( "%s%s", currentLow.toString(), WeatherLionApplication.DEGREES );

            displayCurrentWindReading = String.format( "%s %s %s",
                    currentWindDirection, currentWindSpeed, "mph" );

            inputValue = Integer.parseInt( currentTemp.toString().replaceAll( "\\D+","" ) );
        }// end of else block

        txvWindReading.setText( displayCurrentWindReading );
        txvCurrentTemperature.setText( displayCurrentTemp );
        txvFeelsLikeTemperature.setText( displayCurrentFeelsLike );
        txvHighTemp.setText( displayCurrentHighTemp );
        txvLowTemp.setText( displayCurrentLowTemp );

        // Update the color of the temperature label
        int colour = UtilityMethod.temperatureColor( inputValue );

        txvCurrentTemperature.setTypeface( WeatherLionApplication.currentTypeface );
        txvCurrentTemperature.setTextColor( colour );
    }// end of method updateTemps

    private void scrollToView( final int[] itemLocation, final int itemPosition )
    {
        detailsScroll.post(
            new Runnable()
            {
                public void run()
                {
                    ValueAnimator scrollAnimator;

                    View target = forecastRecyclerView.getChildAt( itemPosition );
                    Rect scrollViewRect = new Rect();
                    detailsScroll.getDrawingRect( scrollViewRect );

                    boolean partiallyVisible = !target.getLocalVisibleRect(
                        scrollViewRect) || scrollViewRect.height() < target.getHeight();

                    // only scroll if we can't see the entire last row in the view
                    if( partiallyVisible )
                    {
                        //detailsScroll.smoothScrollTo( 0, scrollTo );
                        //detailsScroll.setSmoothScrollingEnabled( true );
                        //target.getParent().requestChildFocus( target, target );
                        int scrollTo = target.getBottom() - 64;

                        scrollAnimator = ValueAnimator
                            .ofInt( detailsScroll.getScrollY(), scrollTo )
                                .setDuration( LIST_ANIMATION_DURATION );

                        scrollAnimator.addUpdateListener(
                            new ValueAnimator.AnimatorUpdateListener()
                            {
                                @Override
                                public void onAnimationUpdate( ValueAnimator animation )
                                {
                                    detailsScroll.setScrollY ((Integer) animation.getAnimatedValue() );
                                    detailsScroll.requestLayout();
                                }
                            });

                        AnimatorSet animationSet = new AnimatorSet();
                        animationSet.setInterpolator( new AccelerateDecelerateInterpolator() );
                        animationSet.play( scrollAnimator );
                        animationSet.start();
                    }// end of if block
                }// end of method run
            } );
    }// end of method scrollToView

    /**
     * This broadcast receiver listens for local broadcasts within the program
     */
    private class AppBroadcastReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive( Context context, Intent intent )
        {
            final String action = Objects.requireNonNull( intent.getAction() );

            switch( action )
            {
                case WidgetUpdateService.ASTRONOMY_CHANGE:
                    Bundle extras = intent.getExtras();
                    String timeOfDay;

                    if ( extras != null )
                    {
                        // the current time of day
                        if( extras.getString( WidgetUpdateService.ASTRONOMY_PAYLOAD ) != null )
                        {
                            timeOfDay = extras.getString( WidgetUpdateService.ASTRONOMY_PAYLOAD );

                            if( timeOfDay != null )
                            {
                                updateAstronomy( timeOfDay );
                            }// end of if block
                        }// end of if block
                    }// end of if block

                    break;
                case WidgetUpdateService.WEATHER_LOADING_ERROR_MESSAGE:
                    // cancel the Visual indication of a refresh
                    if( appRefresh != null )
                    {
                        // cancel the visual indication of a refresh
                        appRefresh.setRefreshing( false );
                    }// end of if block

                    stopLoading();

                    break;
                case PrefsActivity.FONT_SWITCH:
                case PrefsActivity.ICON_SWITCH:
                    if( new File( WeatherLionMain.this.getFileStreamPath( WeatherLionApplication.WEATHER_DATA_XML ).toString() ).exists() )
                    {
                        // refresh the xml data stored after the last update
                        WeatherLionApplication.lastDataReceived = LastWeatherDataXmlParser.parseXmlData(
                                UtilityMethod.readAll(
                                        context.getFileStreamPath( WeatherLionApplication.WEATHER_DATA_XML ).toString() )
                                        .replaceAll( "\t", "" ).trim() );

                        initializeMainWindow();
                        loadMainActivityWeather();
                    }// end of if block

                    break;
                case WeatherDataXMLService.WEATHER_XML_STORAGE_MESSAGE:
                    if( WeatherLionApplication.restoringWeatherData )
                    {
                        setContentView( R.layout.wl_main_activity );

                        UtilityMethod.lastUpdated = new Date();

                        if( WeatherLionApplication.useGps && !WeatherLionApplication.gpsRadioEnabled )
                        {
                            noGpsAlert();
                        }// end of if block

                        if( !UtilityMethod.hasInternetConnection( WeatherLionMain.this ) )
                        {
                            View mainActivity = findViewById( R.id.weather_main_container );
                            noInternetAlert( mainActivity );
                        }// end of if block
                        else if( internetCafeView != null )
                        {
                            if( internetCafeView.getVisibility() == View.VISIBLE )
                            {
                                removeInternetAlert();
                            }// end of if block
                        }// end of else if block

                        WeatherLionApplication.restoringWeatherData = false;
                    }  // end of if block

                    if( new File( WeatherLionMain.this.getFileStreamPath( WeatherLionApplication.WEATHER_DATA_XML ).toString() ).exists() )
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

                        initializeMainWindow();
                        // reload main activity
                        loadMainActivityWeather();
                    }// end of if block

                    if( appRefresh != null )
                    {
                        // cancel the visual indication of a refresh
                        appRefresh.setRefreshing( false );
                    }// end of if block

                    if( loadingDialog != null )
                    {
                        stopLoading();
                        loadingDialog.dismiss();
                    }// end of if block

                    break;
                case RECYCLER_ITEM_CLICK:

                    extras = intent.getExtras();
                    int[] location;
                    int position;

                    if ( extras != null )
                    {
                        // the position of the row that was clicked
                        if( extras.getIntArray( RECYCLER_ITEM_LOCATION ) != null &&
                                extras.getInt( RECYCLER_ITEM_POSITION ) != -1 )
                        {
                            location = extras.getIntArray( RECYCLER_ITEM_LOCATION );
                            position = extras.getInt( RECYCLER_ITEM_POSITION );

                           scrollToView( location, position );
                        }// end of if block
                    }// end of if block

                    break;
            }// end of switch block
        }// end of method onReceive
    }// end of class AppBroadcastReceiver

    /**
     * Receives broadcasts sent by the operating system.
     */
    private class SystemBroadcastReceiver extends BroadcastReceiver
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onReceive( Context context, Intent intent )
        {
            final String action = Objects.requireNonNull( intent.getAction() );

            if( action.equals( ConnectivityManager.CONNECTIVITY_ACTION ) )
            {
                if( UtilityMethod.hasInternetConnection( WeatherLionMain.this ) )
                {
                    if( internetCafeView != null )
                    {
                        if( internetCafeView.getVisibility() == View.VISIBLE )
                        {
                            removeInternetAlert();
                        }// end of if block
                    }// end of if block
                }// end of if block
                else
                {
                    View rootView = getWindow().getDecorView().findViewById(
                            android.R.id.content );
                    noInternetAlert( rootView );
                }// end of else block
            }// end of if block
            else if( action.equals( Intent.ACTION_TIME_TICK ) )
            {
                if( txvLastUpdated != null && UtilityMethod.lastUpdated != null )
                {
                    txvLastUpdated.setTypeface( WeatherLionApplication.currentTypeface );

                    if( WeatherLionApplication.currentLocationTimeZone != null )
                    {
                        txcLocalTime.setTimeZone(
                                WeatherLionApplication.currentLocationTimeZone.getTimezoneId() );
                    }// end of if block

                    txvLastUpdated.setText( String.format( "%s%s", "Updated ",
                            UtilityMethod.getTimeSince( UtilityMethod.lastUpdated ) ) );
                }// end of if block

                // if an update is required but was not performed
                if( UtilityMethod.updateRequired( WeatherLionMain.this ) )
                {
                    UtilityMethod.refreshRequestedBySystem = true;
                    UtilityMethod.refreshRequestedByUser = false;

                    String invoker = WeatherLionMain.this.getClass().getSimpleName() +
                            "::systemEventsBroadcastReceiver::onReceive";
                    WeatherLionApplication.callMethodByName( null,
                            "refreshWeather",
                            new Class[]{ String.class }, new Object[]{ invoker } );
                }// end of if block
            }// end of else if block
        }// end of method onReceive
    }// end of class SystemBroadcastReceiver
}// end of class WeatherLionMain