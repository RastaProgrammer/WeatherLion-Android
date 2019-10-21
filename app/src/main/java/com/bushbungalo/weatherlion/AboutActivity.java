package com.bushbungalo.weatherlion;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;

import com.bushbungalo.weatherlion.utils.UtilityMethod;

public class AboutActivity extends AppCompatActivity
{
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {

        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences( this );
        String widBackgroundColor = spf.getString( "pref_widget_background", null );

        // ensure that the app theme is inline with the user selected background
        if( widBackgroundColor != null )
        {
            switch( widBackgroundColor.toLowerCase() )
            {
                case "aqua":
                    setTheme( R.style.AquaTheme );
                    break;
                case "rabalac":
                    setTheme( R.style.RabalacTheme );
                    break;
                default:
                    setTheme( R.style.LionTheme );
                    break;
            }// end of switch block
        }// end of if block

        super.onCreate( savedInstanceState );

        setContentView( R.layout.wl_about_activity );
        UtilityMethod.loadCustomFont( (RelativeLayout) findViewById( R.id.rlAbout ) );

    }// end of method onCreate

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }// end of method onDestroy

}// end of class AboutActivity
