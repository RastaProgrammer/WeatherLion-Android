package com.bushbungalo.weatherlion.custom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bushbungalo.weatherlion.R;
import com.bushbungalo.weatherlion.WeatherLionApplication;

public class CustomPopupMenuAdapter extends ArrayAdapter<String>
{
    private LayoutInflater mInflater;
    private final String[] menuItems;

    public CustomPopupMenuAdapter( Context context, String[] values )
    {
        super( context,  R.layout.wl_popup_list_item_dark_bg, values );
        mInflater = LayoutInflater.from( context );
        menuItems = values;
    }// end of default constructor

    @NonNull
    @Override
    public View getView( int position, View convertView, @NonNull ViewGroup parent )
    {

        if ( convertView == null )
        {
            convertView = mInflater.inflate( R.layout.wl_popup_list_item_dark_bg,
                    parent, false );
        }// end of if block

        TextView menuItem = convertView.findViewById( R.id.txvListItem );

        if( WeatherLionApplication.currentTypeface != null )
        {
            menuItem.setTypeface( WeatherLionApplication.currentTypeface );
            menuItem.setText( menuItems[ position ] );
        }// end of if block

        return convertView;
    }// end of method getView
}// end of class CustomPopupMenuAdapter
