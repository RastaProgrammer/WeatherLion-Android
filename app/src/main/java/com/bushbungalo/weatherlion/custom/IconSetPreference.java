package com.bushbungalo.weatherlion.custom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
    public static final String CALLER_NAME = "icons";

    private Context mContext;
    protected View dialogView;

    private String[] iconSetNames = null;
    private String[] iconSetDefaultImage = null;
    private GridView iconGrid;
    private StringBuilder selectedIconSet = new StringBuilder();
    private int layoutId = R.layout.wl_icon_set_preference_dialog;

    public IconSetPreference( Context context, AttributeSet attrs )
    {
        super( context, attrs );
        this.mContext = context;

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
        dialogView = View.inflate( mContext,
                layoutId, null );
        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences( getContext() );
        String widBackgroundColor = spf.getString( WeatherLionApplication.WIDGET_BACKGROUND_PREFERENCE,
                com.bushbungalo.weatherlion.Preference.DEFAULT_WIDGET_BACKGROUND );
        RelativeLayout rlTitleBar = dialogView.findViewById( R.id.rlDialogTitleBar );
        RelativeLayout dialogBody = dialogView.findViewById( R.id.rlDialogBody );
        RelativeLayout dialogFooter = dialogView.findViewById( R.id.rlDialogFooter );

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

        if( widBackgroundColor != null )
        {
            switch ( widBackgroundColor.toLowerCase() )
            {
                case WeatherLionApplication.AQUA_THEME:
                    dialogBody.setBackgroundColor( Color.valueOf( mContext.getColor( R.color.aqua_dialog_bg ) ).toArgb() );
                    ( (GradientDrawable) dialogFooter.getBackground() ).setColor( Color.valueOf( mContext.getColor(
                            R.color.aqua_dialog_bg ) ).toArgb() );
                    break;
                case WeatherLionApplication.FROSTY_THEME:
                    dialogBody.setBackgroundColor( Color.valueOf( mContext.getColor( R.color.frosty_dialog_bg ) ).toArgb() );
                    ( (GradientDrawable) dialogFooter.getBackground() ).setColor( Color.valueOf( mContext.getColor(
                            R.color.frosty_dialog_bg ) ).toArgb() );
                    break;
                case WeatherLionApplication.RABALAC_THEME:
                    dialogBody.setBackgroundColor( Color.valueOf( mContext.getColor( R.color.rabalac_dialog_bg ) ).toArgb() );
                    ( (GradientDrawable) dialogFooter.getBackground() ).setColor( Color.valueOf( mContext.getColor(
                            R.color.rabalac_dialog_bg ) ).toArgb() );

                    break;
                case WeatherLionApplication.LION_THEME:
                    dialogBody.setBackgroundColor( Color.valueOf( mContext.getColor( R.color.lion_dialog_bg ) ).toArgb() );
                    ( (GradientDrawable) dialogFooter.getBackground() ).setColor( Color.valueOf( mContext.getColor(
                            R.color.lion_dialog_bg ) ).toArgb() );

                    break;
            }// end of switch block
        }// end of if block

        try
        {
            iconSetNames = mContext.getAssets().list( "weather_images" );
            List packs = new ArrayList();

            for( String iconSet : iconSetNames )
            {
                boolean previewExists = iconSet.equalsIgnoreCase( "mono" );

                String displayIcon =  WeatherLionApplication.WEATHER_IMAGES_ROOT + iconSet +
                        ( previewExists ? "/preview_image.png" : "/weather_10.png" );

                packs.add( displayIcon );
            }// end of for each loop

            iconSetDefaultImage = (String[]) packs.toArray( new String[0] );
        }// end of try block
        catch ( IOException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    this.getClass().getSimpleName() + "::onCreateDialogView [line: " +
                        UtilityMethod.getExceptionLineNumber( e )  + "]" );
        }// end of catch block

        final CustomPreferenceGrid adapter = new CustomPreferenceGrid(
                mContext, iconSetNames, iconSetDefaultImage, CALLER_NAME );
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
            getDialog().getWindow().getAttributes().windowAnimations = R.style.ZoomAnimation;
            UtilityMethod.zoomInView( dialogView );

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
                    }// end of if block

                    getDialog().dismiss();
                }// end of method onClick
            });

            // Controlling width and height with random values
            Window dialogWindow = getDialog().getWindow();
            dialogWindow.setLayout( CustomPreferenceGrid.DEFAULT_DIALOG_WIDTH,
                    ViewGroup.LayoutParams.WRAP_CONTENT );
//            dialogWindow.setLayout( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );
            dialogWindow.setGravity( Gravity.CENTER );
        }// end of if block
    }// end of method showDialog

}// end of class IconSetPreference