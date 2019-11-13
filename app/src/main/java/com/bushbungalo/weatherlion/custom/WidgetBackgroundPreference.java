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

@SuppressWarnings({"unchecked", "WeakerAccess"})
public class WidgetBackgroundPreference extends DialogPreference
{
    public static final String CALLER_NAME = "background";

    final String TAG = "WidgetBackgroundPreference";
    private Context mContext;
    protected View dialogView;

    private String[] backgroundNames = null;
    private String[] backgroundDefaultImage = null;
    private GridView backgroundGrid;
    private StringBuilder selectedBackgroundStyle = new StringBuilder();
    private int layoutId = R.layout.wl_widget_background_preference_dialog;

    public WidgetBackgroundPreference( Context context, AttributeSet attrs )
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
        dialogView = View.inflate( getContext(), layoutId, null );


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
            backgroundNames = mContext.getAssets().list( "backgrounds" );
            List packs = new ArrayList();

            for( String background : backgroundNames )
            {
                String displayIcon = "backgrounds/" + background;

                packs.add( displayIcon );
            }// end of for each loop

            backgroundDefaultImage = (String[]) packs.toArray( new String[ 0 ] );
        }// end of try block
        catch ( IOException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    TAG + "::onCreateDialogView [line: " +
                    UtilityMethod.getExceptionLineNumber( e )  + "]" );
        }// end of catch block

        final CustomPreferenceGrid adapter = new CustomPreferenceGrid( mContext, backgroundNames,
                backgroundDefaultImage, CALLER_NAME );
        backgroundGrid = dialogView.findViewById( R.id.grdWidgetBackground );
        backgroundGrid.setAdapter( adapter );

        backgroundGrid.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id )
            {
                //int color = 0x00FFFFFF; // Transparent
                for( int i = 0; i < adapter.getCount(); i++ )
                {
                    backgroundGrid.getChildAt( i ).setBackgroundColor( 0x00FFFFFF ) ; // Transparent
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

                    if( selectedBackgroundStyle.length() != 0 )
                    {
                        editor.putString( WeatherLionApplication.WIDGET_BACKGROUND_PREFERENCE,
                                UtilityMethod.toProperCase(
                                        selectedBackgroundStyle.toString() ) );
                        editor.commit();
                    }// end of if block

                    getDialog().dismiss();
                }// end of method onClick
            });

            // Controlling width and height with random values
            getDialog().getWindow().setLayout( CustomPreferenceGrid.DEFAULT_DIALOG_WIDTH,
                CustomPreferenceGrid.DEFAULT_GRID_DIALOG_HEIGHT );
        }// end of if block
    }// end of method showDialog
}// end of class WidgetBackgroundPreference
