package com.bushbungalo.weatherlion.custom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.v4.content.LocalBroadcastManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bushbungalo.weatherlion.R;
import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.utils.UtilityMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"unused", "unchecked", "WeakerAccess"})
public class IconSetPreference extends DialogPreference
{
    public static final String ICON_SET_SERVICE_MESSAGE = "iconSetServiceMessage";
    public static final String ICON_SET_SERVICE_PAYLOAD = "iconSetServicePayload";

    protected View dialogView;

    private String[] iconSetNames = null;
    private String[] iconSetDefaultImage = null;
    private GridView iconGrid;
    private StringBuilder selectedIconSet = new StringBuilder();
    private int layoutId = R.layout.wl_icon_set_preference_dialog;

    public IconSetPreference( Context context, AttributeSet attrs )
    {
        super( context, attrs );

        setPersistent( false );
        setDialogLayoutResource( layoutId );
        setDialogIcon( null );
        setDialogTitle( null );
        setPersistent( true );
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
                layoutId, null );

        RelativeLayout rlTitleBar = dialogView.findViewById( R.id.rlDialogTitleBar );

        if( WeatherLionApplication.systemColor != null )
        {
            GradientDrawable bgShape = (GradientDrawable) rlTitleBar.getBackground().getCurrent();
            bgShape.setColor( WeatherLionApplication.systemColor.toArgb() );
        }// end of if block

        TextView txvDialogTitle = dialogView.findViewById( R.id.txvDialogTitle );
        txvDialogTitle.setTypeface( WeatherLionApplication.currentTypeface );

        ImageView imvClose = dialogView.findViewById( R.id.imvCloseDialog );

        imvClose.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                getDialog().dismiss();
            }
        });

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
        catch ( IOException e )
        {
            UtilityMethod.logMessage(UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    this.getClass().getSimpleName() + "::onCreateDialogView [line: " +
                        UtilityMethod.getExceptionLineNumber( e )  + "]" );
        }// end of catch block

        final CustomPreferenceGrid adapter = new CustomPreferenceGrid(
            WeatherLionApplication.getAppContext(), iconSetNames, iconSetDefaultImage, "icons" );
        iconGrid = dialogView.findViewById( R.id.grdIconSet );
        iconGrid.setAdapter( adapter );

        iconGrid.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id)
            {
                //int color = 0x00FFFFFF; // Transparent
                for( int i = 0; i < adapter.getCount(); i++ )
                {
                    iconGrid.getChildAt( i ).setBackgroundColor( 0x00FFFFFF ); // Transparent
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
    }// end of method onDialogClosed

    /**
     * {@inheritDoc}
     */
    @Override
    protected void showDialog( Bundle state )
    {
        super.showDialog( state );

        if( getDialog() != null )
        {
            Objects.requireNonNull( getDialog().getWindow() ).setBackgroundDrawable(
                    new ColorDrawable( Color.TRANSPARENT ) );

            // Remove the default system dialog buttons from the view
            ( (AlertDialog) getDialog() ).getButton(
                    AlertDialog.BUTTON_POSITIVE ).setVisibility( View.GONE );
            ( (AlertDialog) getDialog() ).getButton(
                    AlertDialog.BUTTON_NEGATIVE ).setVisibility( View.GONE );

            // use custom buttons to track user interaction
            Button btnCancel = dialogView.findViewById( R.id.btnCancel );
            btnCancel.setBackground( WeatherLionApplication.systemButtonDrawable );
            btnCancel.setTypeface( WeatherLionApplication.currentTypeface );

            Button btnOk = dialogView.findViewById( R.id.btnOk );
            btnOk.setBackground( WeatherLionApplication.systemButtonDrawable );
            btnOk.setTypeface( WeatherLionApplication.currentTypeface );

            btnCancel.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    getDialog().dismiss();
                }// end of method onClick
            });

            btnOk.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
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
                }// end of method onClick
            });
        }// end of if block
    }// end of method showDialog
}// end of class IconSetPreference