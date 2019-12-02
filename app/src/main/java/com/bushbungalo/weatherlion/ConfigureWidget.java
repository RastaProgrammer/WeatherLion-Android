package com.bushbungalo.weatherlion;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.text.HtmlCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bushbungalo.weatherlion.model.CityData;
import com.bushbungalo.weatherlion.providers.LargeWeatherWidgetProvider;
import com.bushbungalo.weatherlion.providers.SmallWeatherWidgetProvider;
import com.bushbungalo.weatherlion.services.CityDataService;
import com.bushbungalo.weatherlion.services.CityStorageService;
import com.bushbungalo.weatherlion.services.WidgetUpdateService;
import com.bushbungalo.weatherlion.utils.JSONHelper;
import com.bushbungalo.weatherlion.utils.LastWeatherDataXmlParser;
import com.bushbungalo.weatherlion.utils.UtilityMethod;
import com.bushbungalo.weatherlion.utils.WidgetHelper;
import com.google.gson.JsonArray;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;

public class ConfigureWidget extends AppCompatActivity
{
    private static final String TAG = "ConfigureWidget";
    private Context mContext;

    private EditText edtSearchCity;
    private ImageButton imbSearch;
    private ImageButton imbWorking;
    private ImageButton imbClear;

    // Data Access
    private EditText edtKeyName;
    private EditText pwdKeyValue;
    private Spinner spnProviderKeys;
    private RelativeLayout rlKeyNameParent;
    
    private static StringBuilder searchCity = new StringBuilder();

    private static ListPopupWindow popupWindow;    
    private static String[] listItems;

    private static boolean popupVisible = false;
    private int selectedIndex;

    private static SharedPreferences spf;

    private AppWidgetManager widgetManager;
    private int widgetId;
    private RemoteViews widgetView;

    private boolean largeWidget;
    private boolean smallWidget;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setResult( RESULT_CANCELED );
        setContentView( R.layout.wl_configuration_activity );

        mContext = ConfigureWidget.this;
        widgetManager = AppWidgetManager.getInstance( mContext );
        spf = PreferenceManager.getDefaultSharedPreferences(ConfigureWidget.this );

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if ( extras != null )
        {
            widgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID );
        }// end of if block

        // If they gave us an intent without the widget id, just bail.
        if ( widgetId == AppWidgetManager.INVALID_APPWIDGET_ID )
        {
            finish();
        }// end of if block

        String widgetProvider = WidgetHelper.getWidgetProviderName( widgetId );

        widgetView = null;

        if( widgetProvider.equals( WeatherLionApplication.LARGE_WIDGET ) )
        {
           widgetView = new RemoteViews( mContext.getPackageName(),
                   R.layout.wl_large_weather_widget_activity_alternate);
           largeWidget = true;
           UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                    "Using large widget layout...",
                    TAG + "::onCreate" );
        }// end of if block
        else if( widgetProvider.equals( WeatherLionApplication.SMALL_WIDGET ) )
        {
            widgetView = new RemoteViews( mContext.getPackageName(),
                    R.layout.wl_small_weather_widget_activity );
            smallWidget = true;
            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                    "Using small widget layout...",
                    TAG + "::onCreate" );
        }// end of else block
        else
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                    "Unable to decipher widget view " + widgetProvider + " !",
                    TAG + "::onCreate" );
        }// end of else block

        LocalBroadcastManager.getInstance( mContext )
                .registerReceiver(cityDetailsBroadcastReceiver,
                        new IntentFilter( CityDataService.CITY_DATA_SERVICE_MESSAGE ) );

        Button btnAddKey = findViewById( R.id.btnAddKey );
        Button btnDeleteKey = findViewById( R.id.btnDeleteKey );
        CheckBox chkShowPwd = findViewById( R.id.cbShowPwd );

        Spinner spnAccessProvider = findViewById( R.id.spnAccessProvider );
        rlKeyNameParent = findViewById( R.id.spnKeyNameParent );
        spnProviderKeys = findViewById( R.id.spnKeyName );
        edtKeyName = findViewById( R.id.edtKeyName );
        pwdKeyValue = findViewById( R.id.edtKeyValue );

        edtSearchCity = findViewById( R.id.edtSearchCity );
        imbSearch = findViewById( R.id.searchBtn );
        imbWorking = findViewById( R.id.btnWorking );
        imbClear = findViewById( R.id.btnClear );

        Button btnFinish = findViewById( R.id.btnFinish );

        // Load only the providers who require access keys
        ArrayList< String > wxOnly =
                new ArrayList<>( Arrays.asList( WeatherLionApplication.providerNames ) );

        Collections.sort( wxOnly );	// sort the list

        // Yr.no (Norwegian Meteorological Institute) does not require an access key at the moment
        wxOnly.remove( WeatherLionApplication.YR_WEATHER );

        String[] accessNeededProviders = wxOnly.toArray( new String[ 0 ] );

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
                view.setTextColor( Color.parseColor("#F9F9FA" ) );
                return view;
            }

            @NonNull
            @Override
            public View getDropDownView( int position, View convertView, @NonNull ViewGroup parent )
            {
                TextView view = (TextView) super.getDropDownView( position, convertView, parent );
                view.setTypeface( WeatherLionApplication.currentTypeface );
                view.setTextColor( Color.parseColor("#F9F9FA" ) );
                return view;
            }
        };

        spnAccessProvider.setAdapter( accessProvidersAdapter );
        spnAccessProvider.setSelection( accessProvidersAdapter.getPosition( accessNeededProviders[ 0 ] ) );

         /*
            Data Keys Area
         */

        spnAccessProvider.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected( AdapterView<?> parent, View view, int position, long id )
            {
                WeatherLionApplication.selectedProvider = parent.getItemAtPosition( position ).toString();

                switch( WeatherLionApplication.selectedProvider )
                {
                    case WeatherLionApplication.GEO_NAMES:
                        rlKeyNameParent.setVisibility( View.INVISIBLE );
                        edtKeyName.setVisibility( View.VISIBLE );
                        edtKeyName.setEnabled( false );
                        edtKeyName.setText( getString( R.string.user_name ) );
                        pwdKeyValue.requestFocus();

                        break;
                    case WeatherLionApplication.DARK_SKY:
                    case WeatherLionApplication.OPEN_WEATHER:
                    case WeatherLionApplication.WEATHER_BIT:
                        rlKeyNameParent.setVisibility( View.INVISIBLE );
                        edtKeyName.setVisibility( View.VISIBLE );
                        edtKeyName.setEnabled( false );
                        edtKeyName.setText( getString( R.string.api_key ) );
                        pwdKeyValue.requestFocus();

                        break;
                    case WeatherLionApplication.HERE_MAPS:
                        edtKeyName.setVisibility( View.INVISIBLE );
                        rlKeyNameParent.setVisibility( View.VISIBLE );

                        // create array adapter with custom fonts
                        ArrayAdapter hereMapsAdapter = new ArrayAdapter( mContext,
                                R.layout.wl_key_name_spinner_style, WeatherLionApplication.hereMapsRequiredKeys )
                        {
                            @NonNull
                            @Override
                            public View getView( int position, View convertView, @NonNull ViewGroup parent )
                            {
                                TextView view = (TextView) super.getView( position, convertView, parent );
                                view.setTypeface( WeatherLionApplication.currentTypeface );
                                view.setTextColor( Color.parseColor("#F9F9FA" ) );
                                return view;
                            }

                            @NonNull
                            @Override
                            public View getDropDownView( int position, View convertView, @NonNull ViewGroup parent )
                            {
                                TextView view = (TextView) super.getDropDownView( position, convertView, parent );
                                view.setTypeface( WeatherLionApplication.currentTypeface );
                                view.setTextColor( Color.parseColor("#F9F9FA" ) );
                                return view;
                            }
                        };

                        spnProviderKeys.setAdapter( hereMapsAdapter );
                        spnProviderKeys.setSelection( 0 );

                        pwdKeyValue.requestFocus();

                        break;
                    case WeatherLionApplication.YAHOO_WEATHER:
                        edtKeyName.setVisibility( View.INVISIBLE );
                        rlKeyNameParent.setVisibility( View.VISIBLE );

                        // create array adapter with custom fonts
                        ArrayAdapter yahooAdapter = new ArrayAdapter( mContext,
                                R.layout.wl_key_name_spinner_style, WeatherLionApplication.yahooRequiredKeys )
                        {
                            @NonNull
                            @Override
                            public View getView( int position, View convertView, @NonNull ViewGroup parent )
                            {
                                TextView view = (TextView) super.getView( position, convertView, parent );
                                view.setTypeface( WeatherLionApplication.currentTypeface );
                                view.setTextColor( Color.parseColor("#F9F9FA" ) );
                                return view;
                            }

                            @NonNull
                            @Override
                            public View getDropDownView( int position, View convertView, @NonNull ViewGroup parent )
                            {
                                TextView view = (TextView) super.getDropDownView( position, convertView, parent );
                                view.setTypeface( WeatherLionApplication.currentTypeface );
                                view.setTextColor( Color.parseColor("#F9F9FA" ) );
                                return view;
                            }
                        };

                        spnProviderKeys.setAdapter( yahooAdapter );
                        spnProviderKeys.setSelection( 0 );

                        break;
                    default:
                        rlKeyNameParent.setVisibility( View.INVISIBLE );
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
        chkShowPwd.setOnCheckedChangeListener( new CompoundButton.OnCheckedChangeListener()
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

                    final InputMethodManager edCityBox = (InputMethodManager)
                            mContext.getSystemService( Context.INPUT_METHOD_SERVICE );

                    edCityBox.hideSoftInputFromWindow( pwdKeyValue.getWindowToken(), 0 );

                    UtilityMethod.butteredToast( mContext,"The key was successfully added to the database.",
                            1, Toast.LENGTH_LONG );

                    WeatherLionApplication.callMethodByName( null,
                        "loadAccessProviders",
                        null, null );
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
                WeatherLionApplication.dataAccessKeyName = ( (EditText)  findViewById( R.id.edtKeyName ) ).getText().toString();

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

        /*
            City Search Area
         */

        imbClear.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                edtSearchCity.setText( "" );
            }// end of method onClick
        });

        edtSearchCity.setTypeface( WeatherLionApplication.currentTypeface );

        edtSearchCity.addTextChangedListener( new TextWatcher()
        {
            @Override
            public void afterTextChanged( Editable s )
            {
            }

            @Override
            public void beforeTextChanged( CharSequence s, int start, int count, int after )
            {
            }

            @Override
            public void onTextChanged( CharSequence s, int start, int before, int count )
            {

                if( edtSearchCity.getText().length() > 0 &&
                        edtSearchCity.getText().toString().equalsIgnoreCase( "Unknown" ) )
                {
                    edtSearchCity.setText( "" ); // Unknown is not a location
                }// end of if block

                if( edtSearchCity.getText().length() > 0 )
                {
                    imbClear.setVisibility( View.VISIBLE );
                }// end of if block
                else
                {
                    imbClear.setVisibility( View.INVISIBLE );
                }// end of else block

                if( popupVisible )
                {
                    popupWindow.dismiss();
                }// end of if block
            }// end of method onTextChanged
        });

        edtSearchCity.setOnEditorActionListener( new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
            {
                if ( actionId == EditorInfo.IME_ACTION_DONE )
                {
                    preSearch();
                }// end of if block

                return false;
            }
        });

        imbSearch.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                preSearch();
            }
        });

        btnFinish.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                if( !WeatherLionApplication.geoNamesAccountLoaded )
                {
                    if( pwdKeyValue.length() == 0 )
                    {
                        UtilityMethod.butteredToast( mContext,
                                "A GeoNames username is required to use this application!",
                                2, Toast.LENGTH_SHORT );

                        return;
                    }// end of if block
                }// end of if block

                if( edtSearchCity.length() == 0 && !WeatherLionApplication.locationSet )
                {
                    String msg = "Use the search feature to locate the city which the weather will be required!";

                    showMessageDialog( UtilityMethod.MsgType.TEXT, msg, "Set Weather Location",
                            null, null, null );
                }// end of if block
                else
                {
                    SharedPreferences.Editor editor =
                            PreferenceManager.getDefaultSharedPreferences( WeatherLionApplication.getAppContext() ).edit();

                    // combine the city and the state as the current location
                    String currentLocation;
                    final String[] location = edtSearchCity.getText().toString().split( "," );

                    if( location.length > 0 )
                    {
                        if( location.length > 2 )
                        {
                            if( location[ 2 ].trim().equalsIgnoreCase( "US") ||
                                    location[ 2 ].trim().equalsIgnoreCase( "United States") )
                            {
                                // if the state name has a length of 2 then nothing needs to be done
                                if( location[ 1 ].trim().length() > 2 )
                                {
                                    currentLocation = location[ 0 ].trim() + ", " +
                                            UtilityMethod.usStatesByName.get( location[ 0 ].trim() );
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
                            if( location[ 1 ].trim().equalsIgnoreCase( "US") ||
                                    location[ 1 ].trim().equalsIgnoreCase( "United States") )
                            {
                                // if the state name has a length of 2 then nothing needs to be done
                                if( location[ 1 ].trim().length() > 2 )
                                {
                                    currentLocation = location[ 0 ].trim() + ", " +
                                            UtilityMethod.usStatesByName.get( location[ 0 ].trim() );
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
                            currentLocation = edtSearchCity.getText().toString();
                        }// end of else block

                        // a location without a comma indicating a specific place is invalid and will be ignored
                        if( currentLocation.contains( "," ) )
                        {
                            WidgetUpdateService.widgetRefreshRequired = true;
                            editor.putString( WeatherLionApplication.CURRENT_LOCATION_PREFERENCE,
                                    currentLocation );
                            editor.apply();

                            WeatherLionApplication.locationSet = true;

                            WeatherLionApplication.storedPreferences.setLocation( currentLocation );

                            // send out a broadcast to the city storage service to store the city name if it has not already been stored
                            UtilityMethod.logMessage(UtilityMethod.LogLevel.INFO,
                                    TAG + " calling the city data storage service...",
                                    TAG + "::onCreate");

                            Intent storeCityIntent = new Intent( WeatherLionApplication.getAppContext(),
                                    CityStorageService.class );
                            storeCityIntent.setData( Uri.parse( selectedIndex + ":" + PreferenceManager.getDefaultSharedPreferences(
                                    mContext ).getString(  WeatherLionApplication.CURRENT_LOCATION_PREFERENCE,
                                    Preference.DEFAULT_WEATHER_LOCATION ) ) );
                            WeatherLionApplication.getAppContext().startService( storeCityIntent );
                        }// end of if block
                        else
                        {
                            WidgetUpdateService.widgetRefreshRequired = false;
                            UtilityMethod.butteredToast( mContext,
                                    "Incomplete city name. Perform a search an select one from the list.",
                                    2, Toast.LENGTH_LONG );
                        }// end of else block
                    }// end of if block
                }// end of else block

                if( WeatherLionApplication.geoNamesAccountLoaded )
                {
                    // reload shared preferences
                    spf = PreferenceManager.getDefaultSharedPreferences(
                            ConfigureWidget.this );

                   WeatherLionApplication.locationSet = !Objects.requireNonNull( spf.getString( WeatherLionApplication.CURRENT_LOCATION_PREFERENCE,
                            Preference.DEFAULT_WEATHER_LOCATION ) ).equals( Preference.DEFAULT_WEATHER_LOCATION );

                    // we have enough information so exit early
                    if( WeatherLionApplication.locationSet )
                    {
                        loadWidget();
                    }// end of if block

                }// end of if block
            }// end of method onClick
        });

        if( WeatherLionApplication.geoNamesAccountLoaded )
        {
            // reload shared preferences
            spf = PreferenceManager.getDefaultSharedPreferences(
                    ConfigureWidget.this );

            WeatherLionApplication.locationSet = !Objects.requireNonNull( spf.getString(
                WeatherLionApplication.CURRENT_LOCATION_PREFERENCE, Preference.DEFAULT_WEATHER_LOCATION )
                    ).equals( Preference.DEFAULT_WEATHER_LOCATION );

            // we have enough information so exit early
            if( WeatherLionApplication.locationSet )
            {
                loadWidget();
            }// end of if block
        }// end of if block
        else
        {
            // GeoNames should be the default selection
            spnAccessProvider.setSelection( accessProvidersAdapter.getPosition( WeatherLionApplication.GEO_NAMES ) );
        }// end of else block
    }// end of method onCreate

    /**
     * Check to see if any previous weather data was stored locally and use it if so.
     */
    private boolean checkForStoredWeatherData()
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
                        TAG + "::onCreate [line: " +
                                e.getStackTrace()[1].getLineNumber()+ "]" );
            }// end of catch block

            WeatherLionApplication.currentSunriseTime = new StringBuilder( WeatherLionApplication.storedData.getAstronomy().getSunrise() );
            WeatherLionApplication.currentSunsetTime = new StringBuilder( WeatherLionApplication.storedData.getAstronomy().getSunset() );

            return true;
        }// end of if block

        return false;
    }// end of method checkForStoredWeatherData

    /**
     * Loads the new widget with weather data
     */
    private void loadWidget()
    {
        String invoker = this.getClass().getSimpleName() + "::loadWidget";
        Bundle pendingIntentExtras;

        if( largeWidget )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                    "Creating large widget ID # " + widgetId,
                    TAG + "::loadWidget" );

            // Provider intent
            Intent intent = new Intent( mContext, LargeWeatherWidgetProvider.class );
            intent.putExtra( EXTRA_APPWIDGET_ID, widgetId );

            // set the click listener for the refresh image
            PendingIntent refreshIntent = PendingIntent.getBroadcast( mContext,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
            widgetView.setOnClickPendingIntent( R.id.imvRefresh, refreshIntent );

            PendingIntent mainIntent = PendingIntent.getBroadcast( mContext,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
            widgetView.setOnClickPendingIntent( R.id.imvCurrentCondition, mainIntent );

            // Update the weather provider
            widgetView.setTextViewText( R.id.txvLastUpdated, "Refreshing..." );

            pendingIntentExtras = new Bundle();
            pendingIntentExtras.putString( WidgetUpdateService.WEATHER_SERVICE_INVOKER, invoker );
            pendingIntentExtras.putString( WeatherLionApplication.LAUNCH_METHOD_EXTRA, null );
            pendingIntentExtras.putString( WidgetUpdateService.WEATHER_DATA_UNIT_CHANGED,
                    WeatherLionApplication.UNIT_NOT_CHANGED );

            Intent widgetRefreshIntent = new Intent( mContext, WidgetUpdateService.class );
            widgetRefreshIntent.putExtras( pendingIntentExtras );
            WidgetUpdateService.enqueueWork( mContext, widgetRefreshIntent );

            Intent offlineIntent = new Intent( mContext, WeatherLionMain.class );
            PendingIntent pOfflineIntent = PendingIntent.getActivity( mContext,
                    0, offlineIntent, 0 );

            widgetView.setOnClickPendingIntent( R.id.imvOffline, pOfflineIntent );

            // main intent
            Intent mainActivityIntent = new Intent( mContext, WeatherLionMain.class );
            PendingIntent pMainActivityIntent = PendingIntent.getActivity( mContext,
                    0, mainActivityIntent, 0 );

            widgetView.setOnClickPendingIntent( R.id.imvCurrentCondition, pMainActivityIntent );

            // when using the alternate large widget layout
            widgetView.setOnClickPendingIntent( R.id.rlWeatherData, pMainActivityIntent );

            // clock intent
            Intent openClockIntent = new Intent( AlarmClock.ACTION_SHOW_ALARMS );
            openClockIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            PendingIntent pClockIntent = PendingIntent.getActivity( mContext,
                    0, openClockIntent, 0 );

            widgetView.setOnClickPendingIntent( R.id.relClock, pClockIntent );

        }// end of if block
        else if( smallWidget )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                    "Creating small widget ID # " + widgetId,
                    TAG + "::loadWidget" );

            // Provider intent
            Intent intent = new Intent( mContext, SmallWeatherWidgetProvider.class );
            intent.putExtra( EXTRA_APPWIDGET_ID, widgetId );

            // set the click listener for the refresh image
            PendingIntent refreshIntent = PendingIntent.getBroadcast( mContext,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
            widgetView.setOnClickPendingIntent( R.id.imvRefresh, refreshIntent );

            PendingIntent mainIntent = PendingIntent.getBroadcast( mContext,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
            widgetView.setOnClickPendingIntent( R.id.imvCurrentCondition, mainIntent );

            // Update the weather provider
            widgetView.setTextViewText( R.id.txvLastUpdated, "Refreshing..." );

            pendingIntentExtras = new Bundle();
            pendingIntentExtras.putString( WidgetUpdateService.WEATHER_SERVICE_INVOKER, invoker );
            pendingIntentExtras.putString( WeatherLionApplication.LAUNCH_METHOD_EXTRA, null );
            pendingIntentExtras.putString( WidgetUpdateService.WEATHER_DATA_UNIT_CHANGED,
                    WeatherLionApplication.UNIT_NOT_CHANGED );

            Intent widgetRefreshIntent = new Intent( mContext, WidgetUpdateService.class );
            widgetRefreshIntent.putExtras( pendingIntentExtras );
            WidgetUpdateService.enqueueWork( mContext, widgetRefreshIntent );

            // main intent
            Intent mainActivityIntent = new Intent( mContext, WeatherLionMain.class );
            PendingIntent pMainActivityIntent = PendingIntent.getActivity( mContext,
                    0, mainActivityIntent, 0 );

            widgetView.setOnClickPendingIntent( R.id.imvCurrentCondition, pMainActivityIntent );
        }// end of else block

        // set the applicable flags that main will use to start the service
        WeatherLionApplication.changeWidgetUnit =  false;

        if( checkForStoredWeatherData() )
        {
            Bundle previousWeatherExtras = new Bundle();
            previousWeatherExtras.putString( WidgetUpdateService.WEATHER_SERVICE_INVOKER, invoker );
            previousWeatherExtras.putString( WeatherLionApplication.LAUNCH_METHOD_EXTRA,
                    WidgetUpdateService.LOAD_PREVIOUS_WEATHER  );
            previousWeatherExtras.putString( WidgetUpdateService.WEATHER_DATA_UNIT_CHANGED,
                    WeatherLionApplication.UNIT_NOT_CHANGED );

            // run the weather service and  call the method that loads the previous weather data
            Intent methodIntent = new Intent( this, WidgetUpdateService.class );
            methodIntent.putExtras( previousWeatherExtras );
            WidgetUpdateService.enqueueWork( this, methodIntent );
        }// end of else if block

        Bundle updateExtras = new Bundle();
        updateExtras.putString( WidgetUpdateService.WEATHER_SERVICE_INVOKER, invoker );
        updateExtras.putString( WeatherLionApplication.LAUNCH_METHOD_EXTRA, null );
        updateExtras.putString( WidgetUpdateService.WEATHER_DATA_UNIT_CHANGED,
                WeatherLionApplication.UNIT_NOT_CHANGED );

        Intent updateIntent = new Intent( mContext, WidgetUpdateService.class );
        updateIntent.putExtra( EXTRA_APPWIDGET_ID, widgetId );
        updateIntent.putExtras( updateExtras );
        WidgetUpdateService.enqueueWork( mContext, updateIntent );

        widgetManager.updateAppWidget( widgetId, widgetView );

        Intent resultValue = new Intent();
        resultValue.putExtra( AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId );
        setResult( RESULT_OK, resultValue );

        finish();
    }// end of method loadWidget

    /**
     * Ensures that all the necessary access privileges are available for performing a search
     */
    private void preSearch()
    {
        if( WeatherLionApplication.geoNamesAccountLoaded )
        {
            if( edtSearchCity.getText().length() == 0 )
            {
                UtilityMethod.butteredToast( mContext, "Please enter a city to search for!",
                        2, Toast.LENGTH_LONG );
                edtSearchCity.requestFocus();
            }// end of if block
            else
            {
                performSearch();
            }// end of else block

        }// end of if block
        else
        {
            if( pwdKeyValue.length() == 0 )
            {
                UtilityMethod.butteredToast( mContext,
                        "A GeoNames username is required to use this application!",
                        2, Toast.LENGTH_SHORT );
                pwdKeyValue.requestFocus();
            }// end of if block
            else if( edtSearchCity.getText().length() == 0 )
            {
                UtilityMethod.butteredToast( mContext, "Please enter a city to search for!",
                        2, Toast.LENGTH_LONG );
                edtSearchCity.requestFocus();
            }// end of if block
            else
            {
                String[] encryptedKey = WeatherLionApplication.encrypt( pwdKeyValue.getText().toString() );

                if( WeatherLionApplication.addSiteKeyToDatabase( WeatherLionApplication.GEO_NAMES,
                        "username", encryptedKey[ 0 ], encryptedKey[ 1 ] ) != -1 )
                {
                    // for immediate usage
                    WidgetUpdateService.geoNameAccount = pwdKeyValue.getText().toString();

                    UtilityMethod.butteredToast( mContext,"The key was successfully added to the database.",
                            1, Toast.LENGTH_LONG );

                    broadcastKeyUpdate();   // keys will be reloaded when a receiver actions the broadcast
                    performSearch();
                }// end of if block
                else
                {
                    UtilityMethod.butteredToast( mContext,"The key could not be added to the database!"
                                    + "\nPlease recheck the key and try again.",
                            2, Toast.LENGTH_SHORT );
                }// end of else block
            }// end of else block
        }// end of else block
    }// end of method preSearch

    /**
     * Send out a broadcast that the keys data must be reloaded
     */
    private void broadcastKeyUpdate()
    {
        Intent messageIntent = new Intent( WeatherLionMain.KEY_UPDATE_MESSAGE );
        messageIntent.putExtra( WeatherLionMain.KEY_UPDATE_PAYLOAD,
                WeatherLionMain.KEY_UPDATE_MESSAGE );
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

    /**
     * Receives the data returned from a webservice search performed in the background
     */
    private BroadcastReceiver cityDetailsBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive( Context context, Intent intent )
        {
            String cityResultsJSON = intent.getStringExtra( CityDataService.CITY_DATA_SERVICE_PAYLOAD );
            JsonArray jArray = JSONHelper.toJSONArray( cityResultsJSON );

            stopSearchAnimation();

            ArrayList<String> matches = new ArrayList<>( JSONHelper.toList( jArray ) );
            //ArrayList<String> unSortedMatches =  new ArrayList<>( matches );

            Collections.sort( matches ); // sort the results alphabetically

            if( matches.size() > 0 )
            {
                listItems = matches.toArray( new String[0] );
            }// end of if block
            else
            {
                listItems = new String[]{ "No matches..." };
            }// end of else block

            showListMenu( edtSearchCity );
            imbSearch.setEnabled( true );
        }// end of anonymous method onReceive
    };// end of cityDetailsBroadcastReceiver

    /**
     * Perform a city search using the data entered by the user
     */
    private void performSearch()
    {
        searchCity.setLength( 0 ); // clear any previous searches
        searchCity.append( edtSearchCity.getText().toString() );

        if ( UtilityMethod.hasInternetConnection( mContext ) )
        {
            if( searchCity.toString().length() != 0 )
            {
                UtilityMethod.listRequested = true;
                imbSearch.setEnabled( false );
                startSearchAnimation( mContext );
                UtilityMethod.findGeoNamesCity( searchCity.toString(), mContext );
            }// end of if block
        }// end of if block
        else
        {
            UtilityMethod.butteredToast( mContext,
                    "Network not available!", 2, Toast.LENGTH_SHORT );
        }// end of else block
    }// end of method performSearch

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
    @SuppressWarnings({"SameParameterValue"})
    private void responseDialog( String title, String prompt, String posResponse,
                                 String negResponse, final String positiveAction,
                                 final String negativeAction, final Object[] params, final Class[] paramClassTypes )
    {
        final AlertDialog response = new AlertDialog.Builder( this ).create();
        View dialogView = View.inflate( ConfigureWidget.this, R.layout.wl_response_dialog, null );
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
                    callMethodByName( ConfigureWidget.this, positiveAction,
                            null, null );
                }// end of if block
                else
                {
                    callMethodByName( ConfigureWidget.this,
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
                    callMethodByName( ConfigureWidget.this, negativeAction,
                            null, null );
                }// end of if block

                response.dismiss();
            }
        });

        response.show();
    }// end of method responseDialog

    /**
     * Show the popup list containing the results of the city search
     *
     * @param anchor The view which the popup window should be anchored
     */
    private void showListMenu( View anchor )
    {
        popupWindow = new ListPopupWindow( mContext );

        popupWindow.setAnchorView( anchor );
        popupWindow.setAdapter( new ArrayAdapter<>( mContext, R.layout.wl_popup_list_item_dark_theme, listItems ) );
        popupWindow.setWidth( anchor.getWidth() );
        popupWindow.setVerticalOffset( 6 );
        popupWindow.setBackgroundDrawable( mContext.getDrawable( R.drawable.wl_dark_list_background ) );

        // if the list has more than 9 elements we will set the height if the window manually
        if( listItems.length > 9 )
        {
            popupWindow.setHeight( 776 );
        }// end of if block

        popupWindow.setOnItemClickListener( new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id )
            {
                String selection = listItems[ + position ];
                int i = 0;

                for( CityData c : CityData.searchCitiesData )
                {
                    int len = selection.split( "," ).length;
                    String city;

                    if( len > 2 )
                    {
                        city = c.getCityName() + ", " + c.getRegionCode() + ", " + c.getCountryName();

                        if( selection.equals( city ) )
                        {
                            selectedIndex = i;
                        }// end of if block
                    }// end of if block
                    else
                    {
                        city = c.getCityName() + ", " + c.getCountryName();

                        if( selection.equals( city ) )
                        {
                            selectedIndex = i;
                        }// end of if block
                    }// end of else block

                    i++;
                }// end of for each loop

                edtSearchCity.setText( selection );
                edtSearchCity.setSelection( edtSearchCity.length() );
                popupWindow.dismiss();
            }// end of anonymous method onItemClick
        });

        popupWindow.show();
        popupVisible = true;
    }// end of method showListMenu

    /**
     * Display a dialog with a specific message
     *
     * @param message   The message to be displayed in the alert dialog
     * @param title   The alert dialog title
     */
    @SuppressWarnings({"SameParameterValue"})
    private void showMessageDialog( UtilityMethod.MsgType messageType, String message, String title,
                                   final String methodToCall, final Object[] params, final Class[] paramClassTypes )
    {
        final View messageDialogView = View.inflate( this,
                R.layout.wl_message_dialog, null );
        final AlertDialog messageDialog = new AlertDialog.Builder( this ).create();
        messageDialog.setView( messageDialogView );

        RelativeLayout rlTitleBar = messageDialogView.findViewById( R.id.rlDialogTitleBar );
        rlTitleBar.setBackgroundColor( WeatherLionApplication.systemColor.toArgb() );

        TextView txvTitle = messageDialogView.findViewById( R.id.txvDialogTitle );
        txvTitle.setMovementMethod( new ScrollingMovementMethod() );
        txvTitle.setTypeface( WeatherLionApplication.currentTypeface );
        txvTitle.setText( title );

        TextView txvMessage = messageDialogView.findViewById( R.id.txvMessage );
        txvMessage.setTypeface( WeatherLionApplication.currentTypeface );

        Button btnOk = messageDialogView.findViewById( R.id.btnOk );
        btnOk.setBackground( WeatherLionApplication.systemButtonDrawable );
        btnOk.setTypeface( WeatherLionApplication.currentTypeface );

        if( messageType != null && messageType.equals( UtilityMethod.MsgType.HTML ) )
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
                if( methodToCall == null )
                {
                    messageDialog.dismiss();

                    return;
                }// end of if block

                if( params == null || params.length == 0 )
                {
                    WeatherLionApplication.callMethodByName( null, methodToCall,null, null );
                }// end of if block
                else
                {
                    WeatherLionApplication.callMethodByName( null, methodToCall, paramClassTypes, params );
                }// end of else block

                messageDialog.dismiss();
            }
        });

        messageDialog.show();
    }// end of method showMessageDialog

    /**
     * Starts the rotation animation of the object
     *
     * @param c The application context
     */
    private void startSearchAnimation( Context c )
    {
        Animation rotateAnim = AnimationUtils.loadAnimation( c, R.anim.rotate );

        imbWorking.setVisibility( View.VISIBLE );
        imbClear.setVisibility( View.INVISIBLE );
        imbWorking.setAnimation( rotateAnim );
    }// end of method startSearchAnimation

    /**
     *  Stops the rotation animation of the object
     */
    private void stopSearchAnimation()
    {
        imbWorking.setVisibility( View.INVISIBLE );
        imbClear.setVisibility( View.VISIBLE );
        imbWorking.setAnimation( null );

        final InputMethodManager edCityBox = (InputMethodManager)
                mContext.getSystemService( Context.INPUT_METHOD_SERVICE );

        edCityBox.hideSoftInputFromWindow( edtSearchCity.getWindowToken(), 0 );
    }// end of method stopSearchAnimation

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        LocalBroadcastManager.getInstance( mContext ).unregisterReceiver(
                cityDetailsBroadcastReceiver);
    }// end of method onDestroy
}// end of class ConfigureWidget