package com.bushbungalo.weatherlion;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bushbungalo.weatherlion.utils.UtilityMethod;

import java.util.Calendar;
import java.util.Locale;

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
                    setTheme( R.style.AquaThemeDark );
                    break;
                case "frosty":
                    setTheme( R.style.FrostyThemeDark );
                    break;
                case "rabalac":
                    setTheme( R.style.RabalacThemeDark );
                    break;
                default:
                    setTheme( R.style.LionThemeDark );
                    break;
            }// end of switch block
        }// end of if block

        super.onCreate( savedInstanceState );
        this.getWindow().setStatusBarColor( WeatherLionApplication.systemColor.toArgb() );

        setContentView( R.layout.wl_about_activity );
        UtilityMethod.loadCustomFont( (RelativeLayout) findViewById( R.id.rlAbout ) );

        TextView company = findViewById( R.id.txvCompanyName );
        Button viewLicences = findViewById( R.id.btnViewLicences );

        // obtain the current year
        Calendar cal = Calendar.getInstance();
        company.setText( String.format( Locale.ENGLISH, "%s%d",
                "BushBungalo Productions™ 2005–", cal.get( Calendar.YEAR ) ) );

        viewLicences.setOnClickListener ( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                Intent licenceIntent = new Intent( AboutActivity.this, LicenceActivity.class );
                startActivity( licenceIntent );
            }
        });

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
