package com.bushbungalo.weatherlion;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.bushbungalo.weatherlion.services.GeoLocationService;
import com.bushbungalo.weatherlion.services.LocationTrackerService;
import com.bushbungalo.weatherlion.services.WidgetUpdateService;
import com.bushbungalo.weatherlion.utils.UtilityMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/*
 * Created by Paul O. Patterson on 11/9/17.
 */

public class PrefsActivity extends AppCompatActivity
{
    private static final String TAG = "PrefsActivity";
    //private static IconSetPreference iconSetPref;

    private static double mLatitude;
    private static double mLongitude;

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected;

    private final static int ALL_PERMISSIONS_RESULT = 101;
    private LocationTrackerService locationTrackerService;

    public Intent intentStarter;
    public Activity mActivity;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences( this );
        String widBackgroundColor = spf.getString( WeatherLionApplication.WIDGET_BACKGROUND_PREFERENCE,
                com.bushbungalo.weatherlion.Preference.DEFAULT_WIDGET_BACKGROUND );

        boolean locationSet = getIntent().getBooleanExtra( WeatherLionMain.LION_MAIN_PAYLOAD, false );

        if( WeatherLionApplication.firstRun )
        {
            UtilityMethod.showMessageDialog( "Please enter your weather location and required settings.",
                "New Installation", this );
        }// end of if block
        else if( !locationSet )
        {
            UtilityMethod.showMessageDialog( "Please set the location for the weather data.",
                "Location Required", this );
        }// end of else if block

        // ensure that the app theme is inline with the user selected background
        if( widBackgroundColor != null )
        {
            switch( widBackgroundColor.toLowerCase() )
            {
                case WeatherLionApplication.AQUA_THEME:
                    setTheme( R.style.AquaTheme );
                    break;
                case WeatherLionApplication.RABALAC_THEME:
                    setTheme( R.style.RabalacTheme );
                    break;
                default:
                    setTheme( R.style.LionTheme );
                    break;
            }// end of switch block
        }// end of if block

        super.onCreate( savedInstanceState );
        setContentView( R.layout.wl_prefs_activity );

        intentStarter = getIntent();
        mActivity = this;

        ArrayList<String> permissions = new ArrayList<>();

        permissions.add( ACCESS_FINE_LOCATION );
        permissions.add( ACCESS_COARSE_LOCATION );

        permissionsToRequest = findUnAskedPermissions( permissions );

        // 2019 update
        if ( permissionsToRequest.size() > 0 )
        {
            requestPermissions( permissionsToRequest.toArray(
                new String[ 0 ] ), ALL_PERMISSIONS_RESULT );
        }// end of if block

        // initialize the settings activity - add is important
        getFragmentManager()
                .beginTransaction()
                .add( R.id.prefs_content, new SettingsFragment() ).commit();

    }// end of method onCreate

    /**
     * Check if there are any permissions for which a request was not made.
     *
     * @param wanted   List of permissions to check
     * @return  List of permission that were not requested
     */
    private ArrayList< String > findUnAskedPermissions( ArrayList< String > wanted )
    {
        ArrayList< String > result = new ArrayList<>();

        for ( String perm : wanted )
        {
            if ( hasPermission( perm ) ) continue;

            result.add( perm );
        }// end of for loop

        return result;
    }// end of method findUnAskedPermissions

    /**
     * Check to see if a specific permission was granted.
     *
     * @param permission   The requested permission
     * @return  A {@code boolean} value indicated whether the permission has been granted.
     */
    private boolean hasPermission( String permission )
    {
        // 2019 update API version will always be above 23
        return ( checkSelfPermission( permission ) == PackageManager.PERMISSION_GRANTED );
    }// end of method hasPermission

    /**
     * Decide whether the OS version is greater that Lollipop.
     *
     * @return  True/False dependent on the outcome of the check
     */
    @SuppressLint("ObsoleteSdkInt")
    @SuppressWarnings("unused")
    private boolean canMakeSmores()
    {
        return ( Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 );
    }// end of method canMakeSmores

    /**
     * {@inheritDoc}
     */
    @TargetApi( Build.VERSION_CODES.M )
    @Override
    public void onRequestPermissionsResult( int requestCode, @NonNull String[] permissions,
                                            @NonNull int[] grantResults )
    {
        permissionsRejected = new ArrayList<>();

        if ( requestCode == ALL_PERMISSIONS_RESULT )
        {
            for ( String perms : permissionsToRequest )
            {
                if( !hasPermission( perms ) )
                {
                    permissionsRejected.add( perms );
                }// end of if block
            }// end of for loop

            if( permissionsRejected.size() > 0 )
            {
                // 2019 update API version will always be above 23
                if( shouldShowRequestPermissionRationale( permissionsRejected.get( 0 ) ) )
                {
                    showPermissionsRequiredDialog(
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick( DialogInterface dialog, int which )
                            {
                                requestPermissions( permissionsRejected.toArray(
                                    new String[ 0 ] ), ALL_PERMISSIONS_RESULT );
                            }
                        });
                }// end of first inner if block
            }// end of outer if block
        }// end of if block
    }// end of method onRequestPermissionsResult

    /**
     * Display a dialog with a message requesting access to permissions.
     *
     * @param okListener   A listener for the Ok click
     */
    private void showPermissionsRequiredDialog( DialogInterface.OnClickListener okListener )
    {
        String message = "The application requires these permissions to run.\nPlease grant access if you need to use the app.";

        new AlertDialog.Builder( WeatherLionApplication.getAppContext() )
            .setMessage( message )
            .setPositiveButton( "OK", okListener )
            .setNegativeButton( "Cancel", null )
            .create()
            .show();
    }// end of method showPermissionsRequiredDialog

    // inner class responsible for building the settings fragment
    public static class SettingsFragment extends PreferenceFragment implements
            Preference.OnPreferenceChangeListener
    {
        PrefsActivity outer = new PrefsActivity();
        private CityFinderPreference locationPref;
        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences( WeatherLionApplication.getAppContext() );

        // GPS Broadcast Receiver
        private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String geoMessage = intent.getStringExtra( GeoLocationService.GEO_LOCATION_SERVICE_PAYLOAD) ;
                setGpsLocation( geoMessage );
            }// end of method onReceive
        };

        // Location Broadcast Receiver
        private BroadcastReceiver locationPreferenceBR = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String prefMessage = intent.getStringExtra( CityFinderPreference.CITY_LOCATION_SERVICE_PAYLOAD );
                spf = PreferenceManager.getDefaultSharedPreferences( getContext() );
                String selectedLocation = spf.getString( WeatherLionApplication.CURRENT_LOCATION_PREFERENCE,
                        com.bushbungalo.weatherlion.Preference.DEFAULT_WEATHER_LOCATION );

                // update global value
                WeatherLionApplication.storedPreferences.setLocation( selectedLocation );

                refreshPreference( prefMessage );
            }// end of method onReceive
        };

        // Icon Set Broadcast Receiver
        private BroadcastReceiver iconSetPreferenceBR = new BroadcastReceiver()
        {
            @Override
            public void onReceive( Context context, Intent intent )
            {
                String prefMessage = intent.getStringExtra( IconSetPreference.ICON_SET_SERVICE_PAYLOAD );
                refreshPreference( prefMessage );
                spf = PreferenceManager.getDefaultSharedPreferences( getContext() );
                WeatherLionApplication.iconSet = spf.getString( WeatherLionApplication.ICON_SET_PREFERENCE,
                        com.bushbungalo.weatherlion.Preference.DEFAULT_ICON_SET );

                // update global value
                WeatherLionApplication.storedPreferences.setIconSet( WeatherLionApplication.iconSet );

                Intent methodIntent = new Intent( getContext(), WidgetUpdateService.class );
                methodIntent.setData( Uri.parse( WeatherLionApplication.UNIT_NOT_CHANGED ) );
                methodIntent.putExtra( WeatherLionApplication.LAUNCH_METHOD_EXTRA,
                        "loadWeatherIconSet" );
                WidgetUpdateService.enqueueWork( context, methodIntent );
            }// end of method onReceive
        };

        // Widget Background Broadcast Receiver
        private BroadcastReceiver widgetBackgroundPreferenceBR = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String prefMessage = intent.getStringExtra( WidgetBackgroundPreference.WIDGET_BACKGROUND_SERVICE_PAYLOAD );
                SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences( getContext() );
                String widBackgroundColor = spf.getString( WeatherLionApplication.WIDGET_BACKGROUND_PREFERENCE,
                        com.bushbungalo.weatherlion.Preference.DEFAULT_WIDGET_BACKGROUND );

                if( widBackgroundColor != null )
                {
                    switch( widBackgroundColor.toLowerCase() )
                    {
                        case WeatherLionApplication.AQUA_THEME:
                            WeatherLionApplication.systemColor = Color.valueOf( context.getColor( R.color.aqua ) );
                            WeatherLionApplication.systemButtonDrawable = context.getDrawable( R.drawable.wl_aqua_button );

                            break;
                        case WeatherLionApplication.RABALAC_THEME:
                            WeatherLionApplication.systemColor = Color.valueOf( context.getColor( R.color.rabalac ) );
                            WeatherLionApplication.systemButtonDrawable = context.getDrawable( R.drawable.wl_rabalac_button );

                            break;
                        case WeatherLionApplication.LION_THEME:
                            WeatherLionApplication.systemColor = Color.valueOf( context.getColor( R.color.lion ) );
                            WeatherLionApplication.systemButtonDrawable = context.getDrawable( R.drawable.wl_default_button );

                            break;
                    }// end of switch block

                    // update global value
                    WeatherLionApplication.storedPreferences.setWidgetBackground( widBackgroundColor );

                    Intent methodIntent = new Intent( getContext(), WidgetUpdateService.class );
                    methodIntent.setData( Uri.parse( WeatherLionApplication.UNIT_NOT_CHANGED ) );
                    methodIntent.putExtra( WeatherLionApplication.LAUNCH_METHOD_EXTRA,
                        WidgetUpdateService.LOAD_WIDGET_BACKGROUND );
                    WidgetUpdateService.enqueueWork( context, methodIntent );

                    // reload all activities in the stack with the updated theme
                    TaskStackBuilder.create( getActivity() )
                            .addNextIntent( new Intent( getActivity(), WeatherLionMain.class ) )
                            .addNextIntent( getActivity().getIntent() )
                            .startActivities();

                    refreshPreference( prefMessage );

                }// end of if block
            }// end of method onReceive
        };

        /**
         * {@inheritDoc}
         */
        @Override
        public void onCreate( Bundle savedInstanceState )
        {
            super.onCreate( savedInstanceState );

            // Load the preferences from an XML resource
            addPreferencesFromResource( R.xml.settings );

            SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences( getContext() );
            WeatherLionApplication.previousWeatherProvider.setLength( 0 );
            WeatherLionApplication.previousWeatherProvider.append( spf.getString( WeatherLionApplication.WEATHER_SOURCE_PREFERENCE,
                    com.bushbungalo.weatherlion.Preference.DEFAULT_WX_PROVIDER ) ); // capture the saved value

            // Bind the each setting to their values so that when the values are changed,
            // the settings activity will be updated to reflect the changes.
            bindPreferenceSummaryToValue( findPreference( getString( R.string.pref_wx_source ) ) );
            bindPreferenceSummaryToValue( findPreference( getString( R.string.pref_update_interval ) ) );
            bindPreferenceSummaryToValue( findPreference( getString( R.string.pref_location ) ) );
            bindPreferenceSummaryToValue( findPreference( getString( R.string.pref_icon_set ) ) );
            bindPreferenceSummaryToValue( findPreference( getString( R.string.pref_widget_background ) ) );
            bindPreferenceSummaryToValue( findPreference( getString( R.string.pref_ui_fonts ) ) );

            final Preference useGpsSwitch = findPreference( WeatherLionApplication.USE_GPS_LOCATION_PREFERENCE );
            ListPreference weatherProvidersSetting = (ListPreference) findPreference( WeatherLionApplication.WEATHER_SOURCE_PREFERENCE );

            // Update the weather providers list with the authorized data
            weatherProvidersSetting.setEntries( WeatherLionApplication.authorizedProviders );
            weatherProvidersSetting.setEntryValues( WeatherLionApplication.authorizedProviders );

            if( WeatherLionApplication.storedPreferences != null )
            {
                if( WeatherLionApplication.firstRun )
                {
                    weatherProvidersSetting.setDefaultValue( WeatherLionApplication.authorizedProviders[ 0 ] );
                }// end of if block
                else
                {
                    weatherProvidersSetting.setDefaultValue( WeatherLionApplication.storedPreferences.getProvider() );
                }// end of else block
            }// end of if block
            else
            {
                if( WeatherLionApplication.authorizedProviders != null )
                {
                    weatherProvidersSetting.setDefaultValue( WeatherLionApplication.authorizedProviders[ 0 ] );
                }// end of if block
            }// end of else block

            Preference useMetricSwitch = findPreference( WeatherLionApplication.USE_METRIC_PREFERENCE );

            boolean useSystemLocation = spf.getBoolean( WeatherLionApplication.USE_GPS_LOCATION_PREFERENCE,
                    com.bushbungalo.weatherlion.Preference.DEFAULT_USE_GPS );
            locationPref = (CityFinderPreference) findPreference( WeatherLionApplication.CURRENT_LOCATION_PREFERENCE );
            locationPref.setEnabled( !useSystemLocation );

            LocalBroadcastManager.getInstance( WeatherLionApplication.getAppContext() )
                .registerReceiver( mBroadcastReceiver, new IntentFilter(
                    GeoLocationService.GEO_LOCATION_SERVICE_MESSAGE ) );

            LocalBroadcastManager.getInstance( WeatherLionApplication.getAppContext() )
                .registerReceiver( locationPreferenceBR, new IntentFilter(
                    CityFinderPreference.CITY_LOCATION_SERVICE_MESSAGE ) );

            LocalBroadcastManager.getInstance( WeatherLionApplication.getAppContext() )
                .registerReceiver( iconSetPreferenceBR, new IntentFilter(
                    IconSetPreference.ICON_SET_SERVICE_MESSAGE ) );

            LocalBroadcastManager.getInstance( WeatherLionApplication.getAppContext() )
                .registerReceiver( widgetBackgroundPreferenceBR, new IntentFilter(
                    WidgetBackgroundPreference.WIDGET_BACKGROUND_SERVICE_MESSAGE ) );

            useGpsSwitch.setOnPreferenceChangeListener( new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange( Preference preference, Object newValue )
                {
                    boolean isEnabled = ( boolean ) newValue;
                    locationPref.setEnabled( !isEnabled );

                    outer.locationTrackerService = new LocationTrackerService( WeatherLionApplication.getAppContext() );
                    boolean gpsReady = outer.locationTrackerService.canGetLocation();

                    if( isEnabled )
                    {

                        if ( gpsReady )
                        {
                            mLongitude = outer.locationTrackerService.getLongitude();
                            mLatitude = outer.locationTrackerService.getLatitude();
                            retrieveGpsLocation();
                            outer.locationTrackerService.stopListener();
                        }// end of if block
                        else
                        {
                            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences( WeatherLionApplication.getAppContext() );
                            settings.edit().putBoolean( WeatherLionApplication.USE_GPS_LOCATION_PREFERENCE, false ).apply();
                        }// end of else block
                    }// end of if block
                    else
                    {
                        locationPref.setEnabled( true );
                    }// end of else block

                    // update global value
                    WeatherLionApplication.storedPreferences.setUseSystemLocation( isEnabled );

                    return gpsReady;
                }
            });

            useMetricSwitch.setOnPreferenceChangeListener( new Preference.OnPreferenceChangeListener()
            {
                @Override
                public boolean onPreferenceChange( Preference preference, Object newValue )
                {
                    boolean isEnabled = ( boolean ) newValue;

                    // update global value
                    WeatherLionApplication.storedPreferences.setUseMetric( isEnabled );

                    Intent updateIntent = new Intent( WeatherLionApplication.getAppContext(),
                            WidgetUpdateService.class );
                    updateIntent.setData( Uri.parse( WeatherLionApplication.UNIT_CHANGED ) );
                    WidgetUpdateService.enqueueWork( WeatherLionApplication.getAppContext(), updateIntent );

                    return true;
                }
            });

        }// end of method onCreate

        /**
         * {@inheritDoc}
         */
        @Override
        public void onDestroy()
        {
            LocalBroadcastManager.getInstance( WeatherLionApplication.getAppContext() )
                    .unregisterReceiver( mBroadcastReceiver );

            LocalBroadcastManager.getInstance( WeatherLionApplication.getAppContext() )
                    .unregisterReceiver( locationPreferenceBR );

            LocalBroadcastManager.getInstance( WeatherLionApplication.getAppContext() )
                    .unregisterReceiver( iconSetPreferenceBR );

            LocalBroadcastManager.getInstance( WeatherLionApplication.getAppContext() )
                    .unregisterReceiver( widgetBackgroundPreferenceBR );

            super.onDestroy();
        }// end of method onDestroy

        /**
         * Bind the new preference to the preferences activity
         * @param preference    The preference to be bound
         */
        private void bindPreferenceSummaryToValue( Preference preference )
        {
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener( this );

            // Trigger the listener immediately with the preference's
            // current value.
            String updated = PreferenceManager
                    .getDefaultSharedPreferences( preference.getContext() )
                    .getString( preference.getKey(), "" );

            if( updated != null )
            {
                onPreferenceChange( preference, updated );
            }// end of if block
        }// end of method bindPreferenceSummaryToValue

        public boolean onPreferenceChange( Preference preference, Object value )
        {
            String stringValue = value.toString();

            if ( preference instanceof ListPreference )
            {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list (since they have separate labels/values).
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue( stringValue );

                if ( prefIndex >= 0 )
                {
                    String entry = (String) listPreference.getEntries()[ prefIndex ];
                    String entryValue = (String) listPreference.getEntryValues()[ prefIndex ];
                    String preferenceTitle = (String) preference.getTitle();

                    preference.setSummary( entry );

                    // handle preferences using the built in ListPreference
                    if( preferenceTitle.equals( getString( R.string.wx_source ) ) )
                    {
                        if( !WeatherLionApplication.previousWeatherProvider.toString().equals( entry ) )
                        {
                            WeatherLionApplication.storedPreferences.setProvider( entry );

                            UtilityMethod.refreshRequested = true;
                        }// end of if block
                    }// end of if block
                    else if( preferenceTitle.equals( getString( R.string.update_interval ) ) )
                    {
                        WeatherLionApplication.storedPreferences.setInterval( entryValue );
                    }// end of else if block
                    else if( preferenceTitle.equals( getString( R.string.optional_fonts ) ) )
                    {
                        WeatherLionApplication.storedPreferences.setFont( entry );
                        updateUIFont( entry );
                    }// end of else if block
                }// end of if block
            }// end of if block
            else
            {
                // For other preferences, set the summary to the value's simple string representation.
                preference.setSummary( stringValue );
            }// end of else block

            // this means that the change was not done by a binding event
            if( UtilityMethod.refreshRequested )
            {
                WidgetUpdateService.widgetRefreshRequired = true;
                UtilityMethod.logMessage(UtilityMethod.LogLevel.INFO,
                        "Preferences requesting a data refresh...",
                        TAG + "::onPreferenceChange");

                // call the weather update service it the user selects a new weather source
                Intent updateIntent = new Intent( WeatherLionApplication.getAppContext(),
                        WidgetUpdateService.class );
                updateIntent.setData( Uri.parse( WeatherLionApplication.UNIT_NOT_CHANGED ) );
                WidgetUpdateService.enqueueWork( WeatherLionApplication.getAppContext(),
                        updateIntent );
            }// end of if block

            return true;
        }// end of method onPreferenceChange

        public void retrieveGpsLocation()
        {
            Intent intent = new Intent( WeatherLionApplication.getAppContext(),
                    GeoLocationService.class );

            String geoUrl =
                    "http://api.geonames.org/findNearbyPlaceNameJSON?" +
                            "lat=" + mLatitude +
                            "&lng=" + mLongitude +
                            "&username=" + WidgetUpdateService.geoNameAccount;
            intent.setData( Uri.parse( geoUrl ) );
            WeatherLionApplication.getAppContext().startService( intent );
        }// end of method retrieveGpsLocation

        private void setGpsLocation( String geoMessage )
        {
            String currentLocation = null;
            String city;
            String countryCode;
            String countryName;
            String regionCode;
            String regionName;

            try
            {
                Object json = new JSONTokener( geoMessage ).nextValue();

                // Check if a JSON was returned from the web service
                if ( json instanceof JSONObject)
                {
                    // Get the full HTTP Data as JSONObject
                    JSONObject geoNamesJSON = new JSONObject( geoMessage );
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
                    currentLocation = geoMessage;
                }// end of else block
            }// end of try block
            catch ( JSONException e )
            {
                UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    TAG + "::setGPSLocation [line: " +
                        UtilityMethod.getExceptionLineNumber( e ) + "]" );
            }// end of catch block

            // updated the shared preferences

            // a location without a comma indicating a specific place is invalid and will be ignored
            if ( currentLocation != null )
            {
                if( currentLocation.contains( "," ) )
                {
                    saveLocationPreference( currentLocation );

                    // bind the values so that the preference screen displays the GPS location
                    bindPreferenceSummaryToValue( findPreference( getString( R.string.pref_location ) ) );

                    Intent updateIntent = new Intent( WeatherLionApplication.getAppContext(),
                        WidgetUpdateService.class );
                    updateIntent.setData( Uri.parse( WeatherLionApplication.UNIT_NOT_CHANGED ) );
                    WidgetUpdateService.enqueueWork( WeatherLionApplication.getAppContext(), updateIntent );

                }// end of if block
                else
                {
                    UtilityMethod.butteredToast( getContext(),
                    "Incomplete city name. Perform a search an select one from the list.",
                        2, Toast.LENGTH_LONG );
                }// end of else block
            }// end of if block
        }// end of method setGpsLocation

        /**
         * Save the location to the local shared preferences file.
         *
         * @param currentLocation The current city location for the weather data.
         */
        private void saveLocationPreference( String currentLocation )
        {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(
                    WeatherLionApplication.getAppContext() );

            if( !currentLocation.equals( WeatherLionApplication.storedPreferences.getLocation() ) &&
                    currentLocation.length() > 0 )
            {
                // combine the city and the region as the current location
                final String[] location = currentLocation.split( "," );

                if( location.length > 2 )
                {
                    if( location[ 2 ].trim().equalsIgnoreCase( "US" ) ||
                            location[ 2 ].trim().equalsIgnoreCase( "United States" ) )
                    {
                        // if the state name has a length of 2 then nothing needs to be done
                        if( location[ 1 ].trim().length() > 2 )
                        {
                            currentLocation = location[ 0 ].trim() + ", " +
                                    UtilityMethod.usStatesByName.get( location[ 1 ].trim() );
                        }// end of if block
                        else
                        {
                            currentLocation = location[ 0 ].trim() + ", " + location[ 1 ].trim();
                        }// end of else block
                    }// end of if block
                    else
                    {
                        currentLocation = location[ 0 ].trim() + ", " + location[ 1 ].trim();
                    }// end of else block
                }// end of if block
                else if( location.length > 1 )
                {
                    if( location[ 1 ].trim().equalsIgnoreCase( "US" ) ||
                            location[ 1 ].trim().equalsIgnoreCase( "United States" ) )
                    {
                        if( location[ 1 ].trim().length() > 2 )
                        {
                            currentLocation = location[ 0 ].trim() + ", " +
                                    UtilityMethod.usStatesByName.get( location[ 1 ].trim() );
                        }// end of if block
                        else
                        {
                            currentLocation = location[ 0 ].trim() + ", " + location[ 1 ].trim();
                        }// end of else block
                    }// end of if block
                    else
                    {
                        currentLocation = location[ 0 ].trim() + ", " + location[ 1 ].trim();
                    }// end of else block
                }// end of else if block
                else
                {
                    currentLocation = null;
                }// end of else block
            }// end of if block

            if( currentLocation != null )
            {
                // write local preferences
                settings.edit().putString( WeatherLionApplication.CURRENT_LOCATION_PREFERENCE,
                    currentLocation ).apply();
            }// end of if block
        }// end of method saveLocationPreference

        /**
         * Refreshes the shared preferences values displayed to the user.
         *
         * @param pref  The preference value to be refreshed.
         */
        private void refreshPreference( String pref )
        {
            Preference preference = findPreference( pref );
            String stringValue = UtilityMethod.getPrefValues( pref );

            if ( preference instanceof ListPreference )
            {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list (since they have separate labels/values).
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue( stringValue );

                if ( prefIndex >= 0 )
                {
                    String entry = (String) listPreference.getEntries()[ prefIndex ];
                    String entryValue = (String) listPreference.getEntryValues()[ prefIndex ];
                    String preferenceTitle = (String) preference.getTitle();

                    preference.setSummary( entry );

                    // handle preferences using the built in ListPreference
                    if( preferenceTitle.equals( getString( R.string.wx_source ) ) )
                    {
                        WeatherLionApplication.storedPreferences.setProvider( entry );
                    }// end of if block
                    else if( preferenceTitle.equals( getString( R.string.update_interval ) ) )
                    {
                        WeatherLionApplication.storedPreferences.setInterval( entryValue );
                    }// end of else if block
                    else if( preferenceTitle.equals( getString( R.string.optional_fonts ) ) )
                    {
                        WeatherLionApplication.storedPreferences.setFont( entry );
                        updateUIFont( entry );
                    }// end of else if block
                }// end of if block
            }// end of if block
            else
            {
                // For other preferences, set the summary to the value's simple string representation.
                preference.setSummary( stringValue );
            }// end of else block
        }// end of method refreshPreference

        /**
         * Update the current font typeface used throughout the UI
         *
         * @param fontName  The name of the font selected
         */
        private void updateUIFont( String fontName )
        {
            switch( fontName )
            {
                case WeatherLionApplication.SYSTEM_FONT:
                    WeatherLionApplication.currentTypeface = null;
                    break;
                case WeatherLionApplication.HELVETICA_FONT:
                    WeatherLionApplication.currentTypeface = WeatherLionApplication.helveticaNeue;
                    break;
                case WeatherLionApplication.PRODUCT_SANS_FONT:
                    WeatherLionApplication.currentTypeface = WeatherLionApplication.productsSans;
                    break;
                case WeatherLionApplication.SAMSUNG_SANS_FONT:
                    WeatherLionApplication.currentTypeface = WeatherLionApplication.samsungSans;
                    break;
            }// end of switch block
        }// end of method updateUIFont
    }// end of inner class SettingsFragment
}// end of class PrefsActivity