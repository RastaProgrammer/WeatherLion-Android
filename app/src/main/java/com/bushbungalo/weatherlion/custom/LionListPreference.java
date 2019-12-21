package com.bushbungalo.weatherlion.custom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bushbungalo.weatherlion.R;
import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.utils.UtilityMethod;

import java.util.Objects;

/**
 * Created by Paul O. Patterson on 11/11/19.
 */

public class LionListPreference extends DialogPreference
{
    private int layoutId = R.layout.wl_list_preference_dialog;
    private Context mContext;
    private View dialogView;
    private RadioGroup rdgEntries;
    private StringBuilder selectedEntry = new StringBuilder();
    private int selectedIndex = -1;
    private int mWhichButtonClicked;

    public LionListPreference( Context context, AttributeSet attrs )
    {
        super( context, attrs );
        this.mContext = context;

        setPersistent( false );
        setDialogLayoutResource( layoutId );
        setDialogIcon( null );
        setDialogTitle( null );
        setPersistent( true );
    }// end of two-argument constructor

    /**
     * {@inheritDoc}
     */
    @Override
    protected View onCreateDialogView()
    {
        dialogView = View.inflate( mContext, layoutId, null );

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

        rdgEntries = dialogView.findViewById( R.id.rdgEntries );

        RadioGroup.LayoutParams params
                = new RadioGroup.LayoutParams( mContext, null);
        params.setMargins( 0, 0, 0, 30 );

        switch( getKey() )
        {
            case WeatherLionApplication.WEATHER_SOURCE_PREFERENCE:
                for( String entry : WeatherLionApplication.authorizedProviders )
                {
                    RadioButton entryButton = new RadioButton( mContext );
                    entryButton.setTextSize( 20f );
                    entryButton.setPadding( 60, 0, 0, 0 );
                    entryButton.setLayoutParams( params );
                    entryButton.setText( entry );
                    entryButton.setTypeface( WeatherLionApplication.currentTypeface );
                    rdgEntries.addView( entryButton );
                }// end of for each loop

                if( WeatherLionApplication.storedPreferences != null )
                {
                    if( WeatherLionApplication.firstRun )
                    {
                        selectedIndex = 0;
                    }// end of if block
                    else
                    {
                        selectedIndex = getIndexOf( WeatherLionApplication.authorizedProviders,
                                WeatherLionApplication.storedPreferences.getProvider() );
                    }// end of else block
                }// end of if block
                else
                {
                    if( WeatherLionApplication.authorizedProviders != null )
                    {
                        selectedIndex = 0;
                    }// end of if block
                }// end of else block

                if( WeatherLionApplication.authorizedProviders != null )
                {
                    if ( selectedIndex != -1 && WeatherLionApplication.authorizedProviders.length > selectedIndex )
                    {
                        RadioButton selectedRadioButton = (RadioButton) rdgEntries.getChildAt( selectedIndex );
                        selectedRadioButton.setChecked( true );
                    }// end of if block
                }// end of if block

                txvDialogTitle.setText( getContext().getResources().getString( R.string.wx_source ) );

                break;
            case WeatherLionApplication.UPDATE_INTERVAL:
                String[] updateTimes = mContext.getResources().getStringArray( R.array.update_times );
                String[] updateMilliseconds = mContext.getResources().getStringArray( R.array.update_milliseconds );

                for( String entry : updateTimes )
                {
                    RadioButton entryButton = new RadioButton( mContext );
                    entryButton.setTextSize( 18f );
                    entryButton.setTypeface( WeatherLionApplication.currentTypeface );
                    entryButton.setPadding( 60, 0, 0, 0 );
                    entryButton.setLayoutParams( params );
                    entryButton.setText( entry );
                    rdgEntries.addView( entryButton );
                }// end of for each loop

                if( WeatherLionApplication.storedPreferences != null )
                {
                    if( WeatherLionApplication.firstRun )
                    {
                        selectedIndex = 0;
                    }// end of if block
                    else
                    {
                        selectedIndex = getIndexOf( updateMilliseconds,
                                WeatherLionApplication.storedPreferences.getInterval() );
                    }// end of else block
                }// end of if block
                else
                {
                    selectedIndex = 0;
                }// end of else block

                if ( selectedIndex != -1 && updateTimes.length > selectedIndex )
                {
                    RadioButton selectedRadioButton = (RadioButton) rdgEntries.getChildAt( selectedIndex );
                    selectedRadioButton.setChecked( true );
                }// end of if block

                txvDialogTitle.setText( getContext().getResources().getString( R.string.update_interval ) );
                break;
            case WeatherLionApplication.UI_FONT:
                String[] fontFaces = mContext.getResources().getStringArray( R.array.font_faces );

                for( String font : fontFaces )
                {
                    RadioButton entryButton = new RadioButton( mContext );
                    entryButton.setTextSize( 20f );
                    entryButton.setPadding( 60, 0, 0, 0 );
                    entryButton.setLayoutParams( params );
                    entryButton.setText( font );

                    if( !font.equalsIgnoreCase( "System" ) )
                    {
                        entryButton.setTypeface( WeatherLionApplication.fonts.get( font ) );
                    }// end fo if block

                    rdgEntries.addView( entryButton );
                }// end of for each loop

                if( WeatherLionApplication.storedPreferences != null )
                {
                    if( WeatherLionApplication.firstRun )
                    {
                        selectedIndex = 0;
                    }// end of if block
                    else
                    {
                        selectedIndex = getIndexOf( fontFaces,
                                WeatherLionApplication.storedPreferences.getFont() );
                    }// end of else block
                }// end of if block
                else
                {
                    selectedIndex = 0;
                }// end of else block

                if ( selectedIndex != -1 && fontFaces.length > selectedIndex )
                {
                    RadioButton selectedRadioButton = (RadioButton) rdgEntries.getChildAt( selectedIndex );
                    selectedRadioButton.setChecked( true );
                }// end of if block

                txvDialogTitle.setText( getContext().getResources().getString( R.string.optional_fonts ) );

                break;
            default:
                break;
        }// end of switch block

        rdgEntries.setOnCheckedChangeListener( new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged( RadioGroup group, int checkedId )
            {
                if( checkedId != -1 )
                {
                    View radioButton = rdgEntries.findViewById( checkedId );
                    selectedIndex = rdgEntries.indexOfChild( radioButton );
                    selectedEntry.setLength( 0 );
                    selectedEntry.append( ( (RadioButton) group.getChildAt(
                            selectedIndex ) ).getText().toString() );

                    mWhichButtonClicked = DialogInterface.BUTTON_POSITIVE;
                    getDialog().dismiss();
                }// end of if block
            }
        });

        return dialogView;
    }// end of method onCreateDialogView

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDismiss( DialogInterface dialog )
    {
       super.onDismiss( dialog );
       onDialogClosed(mWhichButtonClicked == DialogInterface.BUTTON_POSITIVE);
    }

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
    }// end of method onBindDialogView

    private static int getIndexOf( String[] array, String item )
    {
        for( int i = 0; i < array.length; i++ )
        {
            if ( item.equals( array[ i ] ) ) return i;
        }// end of if block

        return -1;
    }// end of method getIndexOf

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

            btnCancel.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    mWhichButtonClicked = DialogInterface.BUTTON_NEGATIVE;
                    getDialog().dismiss();
                }// end of method onClick
            });

            // Controlling width and height with specific values
            Window dialogWindow = getDialog().getWindow();
            dialogWindow.getAttributes().windowAnimations = R.style.ZoomAnimation;
            dialogWindow.setLayout( CustomPreferenceGrid.DEFAULT_DIALOG_WIDTH,
                    ViewGroup.LayoutParams.WRAP_CONTENT );
//            dialogWindow.setLayout( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );
            dialogWindow.setGravity( Gravity.CENTER );
        }// end of if block
    }// end of method showDialog

    @Override
    protected void onDialogClosed( boolean positiveResult )
    {
        super.onDialogClosed( positiveResult );

        SharedPreferences.Editor editor = getEditor();

        if( positiveResult && selectedEntry.length() != 0 )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
            "User selected: " + selectedEntry.toString(), "LionListPreference::onDialogClosed" );

            UtilityMethod.refreshRequestedByUser = true;
            UtilityMethod.refreshRequestedBySystem  = false;

            switch( getKey() )
            {
                case WeatherLionApplication.WEATHER_SOURCE_PREFERENCE:
                    editor.putString( WeatherLionApplication.WEATHER_SOURCE_PREFERENCE,
                            selectedEntry.toString() );
                    editor.commit();
                    break;
                case WeatherLionApplication.UPDATE_INTERVAL:
                    String[] updateTimes = mContext.getResources().getStringArray( R.array.update_times );
                    String[] updateMilliseconds = mContext.getResources().getStringArray( R.array.update_milliseconds );
                    int index = getIndexOf( updateTimes, selectedEntry.toString() );
                    String entryValue = updateMilliseconds[ index ];


                    editor.putString( WeatherLionApplication.UPDATE_INTERVAL,
                            entryValue );
                    editor.commit();
                    break;
                case WeatherLionApplication.UI_FONT:
                    editor.putString( WeatherLionApplication.UI_FONT,
                            selectedEntry.toString() );
                    editor.commit();
                    break;
                default:
                    break;
            }// end of switch block
        }// end of if block
    }// end of method onDialogClosed
}// end of class LionListPreference
