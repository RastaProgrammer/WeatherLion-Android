package com.bushbungalo.weatherlion.custom;

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

import com.bushbungalo.weatherlion.R;
import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.utils.UtilityMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "WeakerAccess"})
public class WidgetBackgroundPreference extends DialogPreference
{
    public static final String WIDGET_BACKGROUND_SERVICE_MESSAGE = "widgetBackgroundServiceMessage";
    public static final String WIDGET_BACKGROUND_SERVICE_PAYLOAD = "widgetBackgroundServicePayload";

    final String TAG = "WidgetBackgroundPreference";
    private Context mContext;
    View dialogView;

    private String[] backgroundNames = null;
    private String[] backgroundDefaultImage = null;
    private GridView grid;
    private StringBuilder selectedBackgroundStyle = new StringBuilder();    

    public WidgetBackgroundPreference(Context context, AttributeSet attrs )
    {
        super( context, attrs );
        this.mContext = context;
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
        dialogView = View.inflate( getContext(), R.layout.wl_widget_background_preference_dialog, null );
        mContext = getContext().getApplicationContext();

        try
        {
            backgroundNames = mContext.getAssets().list( "backgrounds" );
            List packs = new ArrayList();

            for( String background : backgroundNames )
            {
                String displayIcon = "backgrounds/" + background;

                packs.add( displayIcon );
            }// end of for each loop

            backgroundDefaultImage = (String[]) packs.toArray( new String[ 0 ] );
        }// end of try block
        catch (IOException e)
        {
            UtilityMethod.logMessage(UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    TAG + "::onCreateDialogView [line: " +
                    UtilityMethod.getExceptionLineNumber( e )  + "]" );
        }// end of catch block

        final CustomPreferenceGrid adapter = new CustomPreferenceGrid( mContext, backgroundNames,
                backgroundDefaultImage, "background" );
        grid = dialogView.findViewById( R.id.grid );
        grid.setAdapter( adapter );

        grid.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id )
            {
                //int color = 0x00FFFFFF; // Transparent
                for( int i = 0; i < adapter.getCount(); i++ )
                {
                    grid.getChildAt( i ).setBackgroundColor( 0x00FFFFFF ) ; // Transparent
                }// end of for each loop

                view.setBackgroundColor( 0xFFC2E9F8 ); // Opaque Blue

                // keep track of the user's selection
                selectedBackgroundStyle.setLength( 0 );
                selectedBackgroundStyle.append( backgroundNames[+ position].replace(".png",
                        "" ) );
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

            if( selectedBackgroundStyle.length() != 0 )
            {
                editor.putString( WeatherLionApplication.WIDGET_BACKGROUND_PREFERENCE,
                        UtilityMethod.toProperCase(
                                selectedBackgroundStyle.toString() ) );
                editor.commit();

                Intent messageIntent = new Intent( WIDGET_BACKGROUND_SERVICE_MESSAGE );
                messageIntent.putExtra( WIDGET_BACKGROUND_SERVICE_PAYLOAD,
                        WeatherLionApplication.WIDGET_BACKGROUND_PREFERENCE );
                LocalBroadcastManager manager = LocalBroadcastManager.getInstance( mContext );
                manager.sendBroadcast( messageIntent) ;
            }// end of if block
        }// end of if block
    }// end of method onDialogClosed
}// end of class WidgetBackgroundPreference
