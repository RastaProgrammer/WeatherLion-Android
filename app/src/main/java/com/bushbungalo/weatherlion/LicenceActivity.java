package com.bushbungalo.weatherlion;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.bushbungalo.weatherlion.custom.LicenceGridAdapter;
import com.bushbungalo.weatherlion.model.CityData;
import com.bushbungalo.weatherlion.model.LicenceData;
import com.bushbungalo.weatherlion.utils.JSONHelper;
import com.bushbungalo.weatherlion.utils.UtilityMethod;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LicenceActivity extends AppCompatActivity
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

        setContentView( R.layout.wl_licence_activity );
        UtilityMethod.loadCustomFont( (RelativeLayout) findViewById( R.id.rlLicence ) );

        String licenseData = JSONHelper.getJSONData( WeatherLionApplication.OPEN_SOURCE_LICENCE,
                true );

        LicenceData[] licenceList = new Gson().fromJson( licenseData,
               LicenceData[].class );

        GridView licensesGrid = findViewById( R.id.grdLicences );

        LicenceGridAdapter arrayAdapter = new LicenceGridAdapter( this, licenceList );
        licensesGrid.setAdapter( arrayAdapter );
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
