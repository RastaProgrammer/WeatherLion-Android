package com.bushbungalo.weatherlion.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import com.bushbungalo.weatherlion.R;
import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.WeatherWidgetProvider;
import com.bushbungalo.weatherlion.utils.UtilityMethod;

/**
 * @author Paul O. Patterson
 * <br />
 * <b style="margin-left:-40px">Class Description:</b>
 * <br />
 * 		<span>Service that calls method <b><i>checkAstronomy</i></b> in the <b>WeatherLionWidget</b> class which checks the current time of day</span>
 * <br />
 * <b style="margin-left:-40px">Date Created:</b>
 * <br />
 * 10/12/18
 * <br />
 * <b style="margin-left:-40px">Updates:</b><br />
 * <ul>
 * 		<li>05/04/19 - Service now monitors Internet connectivity and updates widget accordingly.</li>
 * 		<li>05/05/19 - Service now only performs the check 1 minute before an update is due.</li>
 * </ul>
 */
public class IconUpdateService extends IntentService
{
	public IconUpdateService()
	{
		super("IconUpdateService");
	}// end of default constructor

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onHandleIntent( Intent intent )
	{
		RemoteViews remoteViews = new RemoteViews( this.getPackageName(), R.layout.wl_weather_widget_activity );

		// keep track of Internet connectivity
		if( UtilityMethod.timeForConnectivityCheck() )
		{
			WeatherLionApplication.connectedToInternet = UtilityMethod.hasInternetConnection( getApplicationContext() );
		}// end of if block

		// If the program is not connected to the Internet, wait for a connection
		if( WeatherLionApplication.connectedToInternet )
		{
			// if there was no previous Internet connection, check for a return in connectivity
			// and refresh the widget
			if( WeatherLionApplication.localWeatherDataAvailable && UtilityMethod.updateRequired( getApplicationContext() ) )
			{
				// run the weather service
				Intent updateIntent = new Intent( getApplicationContext(), WidgetUpdateService.class );
				updateIntent.setData( Uri.parse( WeatherLionApplication.UNIT_NOT_CHANGED ) );
				WidgetUpdateService.enqueueWork( getApplicationContext(), updateIntent );
			}// end of if block

			if( WeatherLionApplication.connectedToInternet )
			{
				remoteViews.setViewVisibility( R.id.imvOffline, View.INVISIBLE );
			}// end of if block
			else
			{
				remoteViews.setViewVisibility( R.id.imvOffline, View.VISIBLE );
			}// end of else block

			WeatherWidgetProvider.checkAstronomy();
		}// end of if block
	}// end of method handleWeatherData

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate()
	{
		super.onCreate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
}// end of class IconUpdateService
