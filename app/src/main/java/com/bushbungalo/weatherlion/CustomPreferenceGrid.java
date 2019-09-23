package com.bushbungalo.weatherlion;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bushbungalo.weatherlion.utils.UtilityMethod;

import java.io.IOException;
import java.io.InputStream;

public class CustomPreferenceGrid extends BaseAdapter
{
    private Context mContext;
    private final String[] displayImages;
    private final String[] displayPaths;
    private String requester;

    CustomPreferenceGrid( Context c, String[] imageName, String[] imagePath, String caller )
    {
        mContext = c;
        this.displayImages = imageName;
        this.displayPaths = imagePath;
        this.requester = caller;
    }// end of four-argument constructor

    @Override
    public int getCount()
    {
       return displayImages.length;
    }

    @Override
    public Object getItem( int position )
    {
        return null;
    }

    @Override
    public long getItemId( int position )
    {
        return 0;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent )
    {
        String TAG = "CustomPreferenceGrid";
        View grid;

        if ( convertView == null )
        {
            grid = View.inflate( WeatherLionApplication.getAppContext(),
                    R.layout.wl_grid_item, null );
            TextView textView = grid.findViewById( R.id.grid_text );
            ImageView imageView = grid.findViewById( R.id.grid_image );

            try
            {
                SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences( mContext );
                String imageFile = displayPaths[ position ];
                InputStream is = WeatherLionApplication.getAppContext().getAssets().open( imageFile );
                Drawable d = Drawable.createFromStream( is, null );
                String selectItem;

                imageView.setImageDrawable( d );

                switch( requester )
                {
                    case "icons":
                        selectItem = spf.getString( WeatherLionApplication.ICON_SET_PREFERENCE,
                                Preference.DEFAULT_ICON_SET );
                        textView.setText( displayImages[ position ] );

                        if ( displayImages[ position ].equalsIgnoreCase( selectItem ) )
                        {
                            grid.setBackgroundColor( 0xFFC2E9F8 ); // Opaque Blue
                        }// end of if block
                        break;
                    case "background":
                        selectItem = spf.getString( WeatherLionApplication.WIDGET_BACKGROUND_PREFERENCE,
                                "Lion" );
                        textView.setText( UtilityMethod.toProperCase(
                                displayImages[ position ].replace( ".png", "" )  ) );

                        if ( displayImages[ position ].equalsIgnoreCase( selectItem + ".png" ) )
                        {
                            grid.setBackgroundColor( 0xFFC2E9F8 ); // Opaque Blue
                        }// end of if block
                        break;
                    default:
                        break;
                }// end of switch block


            }// end of try block
            catch ( IOException e )
            {
                UtilityMethod.logMessage(UtilityMethod.LogLevel.SEVERE,
                    e.getMessage(),TAG + "::getView [line: " +
                        e.getStackTrace()[1].getLineNumber()+ "]" );
            }// end of catch block
        }// end of if block
        else
        {
            grid = convertView;
        }// end of else block

        return grid;
    }// end of method getView
}// end of class CustomPreferenceGrid