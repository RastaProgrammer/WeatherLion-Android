package com.bushbungalo.weatherlion;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.bushbungalo.weatherlion.utils.UtilityMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "unchecked"})
public class IconSetPreference extends DialogPreference
{
    public static final String ICON_SET_SERVICE_MESSAGE = "iconSetServiceMessage";
    public static final String ICON_SET_SERVICE_PAYLOAD = "iconSetServicePayload";

    private final String TAG = "IconSetPreference";
    protected View dialogView;

    private String[] iconSetNames = null;
    private String[] iconSetDefaultImage = null;
    private GridView grid;
    private StringBuilder selectedIconSet = new StringBuilder();

    public IconSetPreference( Context context, AttributeSet attrs )
    {
        super( context, attrs );
    }

    protected void onBindDialogView( View view )
    {
        super.onBindDialogView( view );
    }// end of method onBindDialogView

    /**
     * {@inheritDoc}
     */
    @Override
    protected View onCreateDialogView()
    {
        //LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        dialogView = View.inflate( WeatherLionApplication.getAppContext(),
                R.layout.wl_icon_set_preference_dialog, null );

        try
        {
            iconSetNames = WeatherLionApplication.getAppContext().getAssets().list( "weather_images" );
            List packs = new ArrayList();

            for( String iconSet : iconSetNames )
            {
                boolean previewExists = iconSet.equalsIgnoreCase( "mono" );

                String displayIcon = "weather_images/" + iconSet +
                        ( previewExists ? "/preview_image.png" : "/weather_10.png" );

                packs.add(displayIcon);
            }// end of for each loop

            iconSetDefaultImage = (String[]) packs.toArray( new String[0] );
        }// end of try block
        catch (IOException e)
        {
            UtilityMethod.logMessage(UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                TAG + "::onCreateDialogView [line: " +
                        UtilityMethod.getExceptionLineNumber( e )  + "]" );
        }// end of catch block

        final CustomPreferenceGrid adapter = new CustomPreferenceGrid(
            WeatherLionApplication.getAppContext(), iconSetNames, iconSetDefaultImage, "icons" );
        grid = dialogView.findViewById( R.id.grid );
        grid.setAdapter( adapter );

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                //int color = 0x00FFFFFF; // Transparent
                for( int i = 0; i < adapter.getCount(); i++ )
                {
                    grid.getChildAt( i ).setBackgroundColor( 0x00FFFFFF ); // Transparent
                }// end of for each loop

                view.setBackgroundColor( 0xFFC2E9F8 ); // Opaque Blue

                // keep track of the user's selection
                selectedIconSet.setLength( 0 );
                selectedIconSet.append( iconSetNames[ + position ] );
            }
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDialogClosed( boolean positiveResult )
    {
        super.onDialogClosed( positiveResult );

        if ( positiveResult )
        {
            SharedPreferences.Editor editor = getEditor();

            if( selectedIconSet.length() != 0 )
            {
                editor.putString( WeatherLionApplication.ICON_SET_PREFERENCE,
                        selectedIconSet.toString() );
                editor.commit();

                Intent messageIntent = new Intent(ICON_SET_SERVICE_MESSAGE);
                messageIntent.putExtra( ICON_SET_SERVICE_PAYLOAD,
                        WeatherLionApplication.ICON_SET_PREFERENCE );
                LocalBroadcastManager manager =
                        LocalBroadcastManager.getInstance( WeatherLionApplication.getAppContext() );
                manager.sendBroadcast( messageIntent );
            }// end of if block
        }// end of if block
    }// end of method onDialogClosed
}// end of class IconSetPreference