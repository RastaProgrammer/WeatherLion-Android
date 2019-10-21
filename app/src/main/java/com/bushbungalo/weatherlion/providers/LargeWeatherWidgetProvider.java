package com.bushbungalo.weatherlion.providers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.AlarmClock;
import android.widget.RemoteViews;

import com.bushbungalo.weatherlion.ConfigureWidget;
import com.bushbungalo.weatherlion.R;
import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.WeatherLionMain;
import com.bushbungalo.weatherlion.services.WidgetUpdateService;
import com.bushbungalo.weatherlion.utils.UtilityMethod;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;

/*
 * Created by Paul O. Patterson on 11/7/17.
 */
@SuppressWarnings({"unused", "SameParameterValue"})
public class LargeWeatherWidgetProvider extends AppWidgetProvider
{
    private static RemoteViews largeWidgetRemoteViews;

    private static final String TAG = "LargeWeatherWidgetProvider";
    private static final String LAUNCH_MAIN = "Main";
    private static final String OPEN_CLOCK_APP = "Clock";
    private BroadcastReceiver mSystemBroadcastReceiver;

    public static final String REFRESH_BUTTON_CLICKED = "Refresh";
    public static final String CLOCK_UPDATE_MESSAGE = "ClockUpdateMessage";
    public static final String WIDGET_UPDATE_MESSAGE = "WidgetUpdateMessage";
    public static final String ICON_REFRESH_MESSAGE = "WidgetIconRefreshMessage";

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEnabled( Context context )
    {
        super.onEnabled( context );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisabled( Context context )
    {
        super.onDisabled( context );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdate( Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds )
    {
        ComponentName largeWidget = new ComponentName( context, LargeWeatherWidgetProvider.class );
        largeWidgetRemoteViews = new RemoteViews( context.getPackageName(),
                R.layout.wl_large_weather_widget_activity );

        int[] allWidgetIds = appWidgetManager.getAppWidgetIds( largeWidget );

        for ( int appWidgetId : allWidgetIds )
        {
            // Provider intent
            Intent intent = new Intent( context, LargeWeatherWidgetProvider.class );
            intent.putExtra( EXTRA_APPWIDGET_ID, appWidgetId );

            // set the applicable flags that main will use to start the service
            WeatherLionApplication.changeWidgetUnit =  false;

            Intent updateIntent = new Intent( context, WidgetUpdateService.class );
            updateIntent.putExtra( EXTRA_APPWIDGET_ID, appWidgetId );
            updateIntent.setData( Uri.parse( "false" ) );
            WidgetUpdateService.enqueueWork( context, updateIntent );

            // set the click listener for the refresh image
            PendingIntent refreshIntent = PendingIntent.getBroadcast( context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
            largeWidgetRemoteViews.setOnClickPendingIntent( R.id.imvRefresh, refreshIntent );

            largeWidgetRemoteViews.setOnClickPendingIntent(
                    R.id.imvRefresh,
                    getPendingSelfIntent( context, REFRESH_BUTTON_CLICKED )
            );

            PendingIntent mainIntent = PendingIntent.getBroadcast( context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
            largeWidgetRemoteViews.setOnClickPendingIntent( R.id.imvCurrentCondition, mainIntent );

            largeWidgetRemoteViews.setOnClickPendingIntent(
                    R.id.imvCurrentCondition,
                    getPendingSelfIntent( context, LAUNCH_MAIN )
            );

            largeWidgetRemoteViews.setOnClickPendingIntent(
                    R.id.relClock,
                    getPendingSelfIntent( context, OPEN_CLOCK_APP )
            );

            largeWidgetRemoteViews.setOnClickPendingIntent(
                    R.id.rlInvalidWidget,
                    getPendingSelfIntent( context, WeatherLionApplication.LAUNCH_CONFIG )
            );

            appWidgetManager.updateAppWidget( largeWidget, largeWidgetRemoteViews );
        }// end of for each loop
    }// end of method onUpdate

    @Override
    public void onDeleted( Context context, int[] appWidgetIds )
    {
      super.onDeleted( context, appWidgetIds );
    }

    // Catch the click on widget views
    protected PendingIntent getPendingSelfIntent( Context context, String action )
    {
        Intent intent = new Intent( context, getClass() );
        intent.setAction( action );

        return PendingIntent.getBroadcast( context, 0, intent, 0 );
    }// end of method getPendingSelfIntent

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive( Context context, Intent intent )
    {
        super.onReceive( context, intent );

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance( context );
        largeWidgetRemoteViews = new RemoteViews( context.getPackageName(),
                R.layout.wl_large_weather_widget_activity );
        String updateExtra = intent.getStringExtra( WIDGET_UPDATE_MESSAGE );
        int[] appWidgetIds = AppWidgetManager.getInstance( context )
                .getAppWidgetIds( new ComponentName( context,
                        LargeWeatherWidgetProvider.class ) );

        // If the refresh button was clicked text clicked
        if ( REFRESH_BUTTON_CLICKED.equals( intent.getAction() ) )
        {
            UtilityMethod.refreshRequested = true;

            UtilityMethod.logMessage(UtilityMethod.LogLevel.INFO, "Refresh requested!",
                    TAG + "::onReceive");

            // Update the weather provider
            largeWidgetRemoteViews.setTextViewText( R.id.txvLastUpdated, "Refreshing..." );

            Intent refreshIntent = new Intent( context, WidgetUpdateService.class );
            refreshIntent.setData( Uri.parse( WeatherLionApplication.UNIT_NOT_CHANGED ) );
            WidgetUpdateService.enqueueWork( context, refreshIntent );

            // update the widget
            appWidgetManager.updateAppWidget( appWidgetIds, largeWidgetRemoteViews);

        }// end of if block
        else if ( LAUNCH_MAIN.equals( intent.getAction() ) )
        {
            Intent mainActivityIntent = new Intent( context, WeatherLionMain.class );
            context.startActivity( mainActivityIntent );
        }// end of else if block
        else if ( WeatherLionApplication.LAUNCH_CONFIG.equals( intent.getAction() ) )
        {
            Intent configActivityIntent = new Intent( context, ConfigureWidget.class );
            context.startActivity( configActivityIntent );
        }// end of else if block
        else if ( OPEN_CLOCK_APP.equals( intent.getAction() ) )
        {
            Intent openClockIntent = new Intent( AlarmClock.ACTION_SHOW_ALARMS );
            openClockIntent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
            context.startActivity( openClockIntent );
        }// end of else if block
        else
        {
            UtilityMethod.refreshRequested = false;
        }// end of else block

        if( updateExtra != null )
        {
            if( ICON_REFRESH_MESSAGE.equals( updateExtra ) )
            {
                ComponentName watchWidget = new ComponentName( context, LargeWeatherWidgetProvider.class );
                largeWidgetRemoteViews = new RemoteViews( context.getPackageName(), R.layout.wl_large_weather_widget_activity);
                appWidgetManager.updateAppWidget( watchWidget, largeWidgetRemoteViews);
            }// end of if block
        }// end of if block
    }// end of method onReceive
}// end of class WeatherUpdater LargeWeatherWidgetProvider