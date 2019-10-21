package com.bushbungalo.weatherlion.custom;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.Uri;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListPopupWindow;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bushbungalo.weatherlion.Preference;
import com.bushbungalo.weatherlion.R;
import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.model.CityData;
import com.bushbungalo.weatherlion.services.CityDataService;
import com.bushbungalo.weatherlion.services.CityStorageService;
import com.bushbungalo.weatherlion.services.WidgetUpdateService;
import com.bushbungalo.weatherlion.utils.JSONHelper;
import com.bushbungalo.weatherlion.utils.UtilityMethod;
import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Paul O. Patterson on 11/30/17.
 */

@SuppressWarnings("unused")
public class CityFinderPreference extends DialogPreference
{
    private final String TAG = "CityFinderPreference";

    // package-private
    public static final String CITY_LOCATION_SERVICE_MESSAGE = "cityLocationServiceMessage";
    public static final String CITY_LOCATION_SERVICE_PAYLOAD = "cityLocationServicePayload";

    private static boolean popupVisible = false;
    private EditText edtCityName;
    private ImageButton imbSearch;
    private ImageButton imbWorking;
    private ImageButton imbClear;
    private View dialogView;
    private static boolean deleteKey = false;
    private static StringBuilder searchCity = new StringBuilder();

    private static ListPopupWindow popupWindow;
    private static PopupMenu popupMenu;
    private static String[] listItems;

    private int selectedIndex;

    public CityFinderPreference( Context context, AttributeSet attrs )
    {
        super( context, attrs );

        setPersistent( false );
        setDialogLayoutResource( R.layout.wl_location_preference_dialog);
        setDialogIcon( null );
        setDialogTitle( null );
        setPersistent( true );
        setPositiveButtonText( "Save" );
    }// end of two-argument constructor

    /**
     * {@inheritDoc}
     */
    @SuppressLint("InflateParams")
    @Override
    protected View onCreateDialogView()
    {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE );
        dialogView = inflater.inflate( R.layout.wl_location_preference_dialog, null );
        TextView txvDialogTitle = dialogView.findViewById( R.id.txvDialogTitle );
        TextView txvDialogMessage = dialogView.findViewById( R.id.txvDialogMessage );
        edtCityName = dialogView.findViewById( R.id.edtSearchCity );
        imbSearch = dialogView.findViewById( R.id.searchBtn );
        imbWorking = dialogView.findViewById( R.id.btnWorking );
        imbClear = dialogView.findViewById( R.id.btnClear );

        RelativeLayout rlTitleBar = dialogView.findViewById( R.id.rlDialogTitleBar );

        if( WeatherLionApplication.systemColor != null )
        {
            rlTitleBar.setBackgroundColor( WeatherLionApplication.systemColor.toArgb() );
        }// end of if block

        LocalBroadcastManager.getInstance( getContext() )
            .registerReceiver( mBroadcastReceiver,
                new IntentFilter( CityDataService.CITY_DATA_SERVICE_MESSAGE ) );

        txvDialogTitle.setText( getContext().getString( R.string.city_search_dialog_title ) );
        txvDialogTitle.setTypeface( WeatherLionApplication.currentTypeface );

        txvDialogMessage.setTypeface( WeatherLionApplication.currentTypeface );

        ImageView imvClose = dialogView.findViewById( R.id.imvCloseDialog );

        imvClose.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                getDialog().dismiss();
            }
        });

        edtCityName.setTypeface( WeatherLionApplication.currentTypeface );

        edtCityName.addTextChangedListener( new TextWatcher()
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

                if( edtCityName.getText().length() > 0 &&
                    edtCityName.getText().toString().equalsIgnoreCase( "Unknown" ) )
                {
                    edtCityName.setText( "" ); // Unknown is not a location
                }// end of if block

                if( edtCityName.getText().length() > 0 )
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

        imbSearch.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                if( edtCityName.getText().length() == 0 )
                {
                    UtilityMethod.butteredToast( getContext(), "Please enter a city to search for!",
                            2, Toast.LENGTH_LONG );
                    edtCityName.requestFocus();
                }// end of if block
                else
                {
                    performSearch();
                }// end of else block
            }// end of method onClick
        });

        imbClear.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                edtCityName.setText( "" );
            }// end of method onClick
        });

        dialogView.setClickable( true );

        return dialogView;
    }// end of method onCreateDialogView

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object onGetDefaultValue( TypedArray a, int index )
    {
        return super.onGetDefaultValue( a, index );
    }

    protected void onBindDialogView( View view )
    {
        super.onBindDialogView( view );

        SharedPreferences sharedPreferences = getSharedPreferences();
        edtCityName.setText ( sharedPreferences.getString( "pref_location", null ) );
        edtCityName.setSelection( edtCityName.getText().length() );
    }// end of method onBindDialogView

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDialogClosed( boolean positiveResult )
    {
        LocalBroadcastManager.getInstance( getContext() ).unregisterReceiver( mBroadcastReceiver );
        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences(
                WeatherLionApplication.getAppContext() );
        String savedLocation = spf.getString( WeatherLionApplication.CURRENT_LOCATION_PREFERENCE,
                Preference.DEFAULT_WEATHER_LOCATION );

        super.onDialogClosed( positiveResult );

        if ( positiveResult )
        {
            SharedPreferences.Editor editor = getEditor();

            // combine the city and the state as the current location
            String currentLocation;
            final String[] location = edtCityName.getText().toString().split( "," );

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
                    currentLocation = edtCityName.getText().toString();
                }// end of else block

                // a location without a comma indicating a specific place is invalid and will be ignored
                if( currentLocation.contains( "," ) )
                {
                    if( !currentLocation.equalsIgnoreCase( savedLocation ) )
                    {
                        WidgetUpdateService.widgetRefreshRequired = true;
                        editor.putString( WeatherLionApplication.CURRENT_LOCATION_PREFERENCE,
                                currentLocation );
                        editor.commit();

                        WeatherLionApplication.storedPreferences.setLocation( currentLocation );

                        // send out a broadcast to the preferences activity that the location preference has been modified
                        Intent messageIntent = new Intent( CITY_LOCATION_SERVICE_MESSAGE );
                        messageIntent.putExtra( CITY_LOCATION_SERVICE_PAYLOAD,
                                WeatherLionApplication.CURRENT_LOCATION_PREFERENCE );
                        LocalBroadcastManager manager =
                                LocalBroadcastManager.getInstance( WeatherLionApplication.getAppContext() );
                        manager.sendBroadcast( messageIntent );

                        // send out a broadcast to the widget service that the location preference has been modified
                        UtilityMethod.refreshRequested = true;
                        Intent updateIntent = new Intent( WeatherLionApplication.getAppContext(), WidgetUpdateService.class );
                        updateIntent.setData( Uri.parse( WeatherLionApplication.UNIT_NOT_CHANGED ) );
                        WidgetUpdateService.enqueueWork( WeatherLionApplication.getAppContext(), updateIntent );

                        // send out a broadcast to the city storage service to store the city name if it has not already been stored
                        Intent storeCityIntent = new Intent( WeatherLionApplication.getAppContext(),
                                CityStorageService.class );
                        storeCityIntent.setData( Uri.parse( selectedIndex + ":" + PreferenceManager.getDefaultSharedPreferences(
                                getContext() ).getString(  WeatherLionApplication.CURRENT_LOCATION_PREFERENCE,
                                Preference.DEFAULT_WEATHER_LOCATION ) ) );
                        WeatherLionApplication.getAppContext().startService( storeCityIntent );
                    }// end of if block

                }// end of if block
                else
                {
                    WidgetUpdateService.widgetRefreshRequired = false;
                    UtilityMethod.butteredToast( getContext(),
                        "Incomplete city name. Perform a search an select one from the list.",
                        2, Toast.LENGTH_LONG );
                }// end of else block
            }// end of if block
        }// end of if block
    }// end of method onDialogClosed

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
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

            showListMenu( edtCityName );
            imbSearch.setEnabled( true );
        }// end of anonymous method onReceive
    };// end of BroadcastReceiver

    private void performSearch()
    {
        searchCity.setLength( 0 ); // clear any previous searches
        searchCity.append( edtCityName.getText().toString() );

        if ( UtilityMethod.hasInternetConnection( getContext() ) )
        {
            if( searchCity.toString().length() != 0 )
            {
                UtilityMethod.listRequested = true;
                imbSearch.setEnabled( false );
                startSearchAnimation( getContext() );
                UtilityMethod.findGeoNamesCity( searchCity.toString(), getContext() );
            }// end of if block
        }// end of if block
        else
        {
            UtilityMethod.butteredToast( getContext(),
                    "Network not available!", 2, Toast.LENGTH_SHORT );
        }// end of else block
    }// end of method performSearch

    private void showListMenu( View anchor )
    {
        popupWindow = new ListPopupWindow( getContext() );

        popupWindow.setAnchorView( anchor );
        popupWindow.setAdapter( new ArrayAdapter<>( getContext(), R.layout.wl_popup_list_item_dark_bg, listItems ) );
        popupWindow.setWidth( anchor.getWidth() );
        popupWindow.setVerticalOffset( 6 );
        popupWindow.setBackgroundDrawable( getContext().getDrawable( R.drawable.wl_round_list_popup_blue) );

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

                CityData[] c = CityData.searchCitiesData;

                edtCityName.setText( selection );
                edtCityName.setSelection( edtCityName.length() );
                popupWindow.dismiss();
            }// end of anonymous method onItemClick
        });

        popupWindow.show();
        popupVisible = true;
    }// end of method showListMenu

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
     *
     */
    private void stopSearchAnimation()
    {
        imbWorking.setVisibility( View.INVISIBLE );
        imbClear.setVisibility( View.VISIBLE );
        imbWorking.setAnimation( null );

        final InputMethodManager edCityBox = (InputMethodManager)
                getContext().getSystemService( Context.INPUT_METHOD_SERVICE );

        edCityBox.hideSoftInputFromWindow(dialogView.getWindowToken(), 0);
    }// end of method stopSearchAnimation
}// end of class CityFinderPreference