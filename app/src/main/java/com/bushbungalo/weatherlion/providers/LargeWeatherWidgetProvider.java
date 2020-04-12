package com.bushbungalo.weatherlion.providers;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.provider.AlarmClock;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.bushbungalo.weatherlion.ConfigureWidget;
import com.bushbungalo.weatherlion.R;
import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.WeatherLionMain;
import com.bushbungalo.weatherlion.utils.UtilityMethod;

import java.text.SimpleDateFormat;
import java.util.Locale;

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
    private static final String OFFLINE = "Offline";
    private BroadcastReceiver mSystemBroadcastReceiver;

    public static final String REFRESH_BUTTON_CLICKED = "Refresh";
    public static final String CANCEL_REFRESH_BUTTON_CLICKED = "CancelRefresh";
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
                R.layout.wl_large_weather_widget_activity_alternate );

        int[] allWidgetIds = appWidgetManager.getAppWidgetIds( largeWidget );

        for ( int appWidgetId : allWidgetIds )
        {
            // Provider intent
            Intent intent = new Intent( context, LargeWeatherWidgetProvider.class );
            intent.putExtra( EXTRA_APPWIDGET_ID, appWidgetId );

            // set the applicable flags that main will use to start the service
            WeatherLionApplication.changeWidgetUnit =  false;

            UtilityMethod.refreshRequestedBySystem = true;
            UtilityMethod.refreshRequestedByUser = false;

            String invoker = this.getClass().getSimpleName() + "::onUpdate";
            WeatherLionApplication.refreshWeather( invoker );

            // set the click listener for the refresh image
            PendingIntent refreshIntent = PendingIntent.getBroadcast( context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
            largeWidgetRemoteViews.setOnClickPendingIntent( R.id.imvRefresh, refreshIntent );

            largeWidgetRemoteViews.setOnClickPendingIntent(
                    R.id.imvRefresh,
                    getPendingSelfIntent( context, REFRESH_BUTTON_CLICKED )
            );

            largeWidgetRemoteViews.setOnClickPendingIntent(
                    R.id.imvOffline,
                    getPendingSelfIntent( context, OFFLINE )
            );

            // set the click listener for the refreshing image
            PendingIntent cancelRefreshIntent = PendingIntent.getBroadcast( context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
            largeWidgetRemoteViews.setOnClickPendingIntent( R.id.imvRefreshing, cancelRefreshIntent );

            largeWidgetRemoteViews.setOnClickPendingIntent(
                    R.id.imvRefreshing,
                    getPendingSelfIntent( context, CANCEL_REFRESH_BUTTON_CLICKED )
            );

            largeWidgetRemoteViews.setOnClickPendingIntent(
                    R.id.imvOffline,
                    getPendingSelfIntent( context, OFFLINE )
            );

            // display the main window on click
            PendingIntent mainIntent = PendingIntent.getBroadcast( context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
            largeWidgetRemoteViews.setOnClickPendingIntent( R.id.imvCurrentCondition, mainIntent );

            largeWidgetRemoteViews.setOnClickPendingIntent(
                    R.id.imvCurrentCondition,
                    getPendingSelfIntent( context, LAUNCH_MAIN )
            );

            // when using the alternate large widget layout
            largeWidgetRemoteViews.setOnClickPendingIntent( R.id.rlWeatherData, mainIntent );

            largeWidgetRemoteViews.setOnClickPendingIntent(
                    R.id.rlWeatherData,
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
        final Context mContext = context;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance( context );
        largeWidgetRemoteViews = new RemoteViews( context.getPackageName(),
                R.layout.wl_large_weather_widget_activity_alternate );
        String updateExtra = intent.getStringExtra( WIDGET_UPDATE_MESSAGE );
        int[] appWidgetIds = AppWidgetManager.getInstance( context )
                .getAppWidgetIds( new ComponentName( context,
                        LargeWeatherWidgetProvider.class ) );

        // If the refresh button was clicked text clicked
        if ( REFRESH_BUTTON_CLICKED.equals( intent.getAction() ) )
        {
            UtilityMethod.refreshRequestedByUser = true;
            UtilityMethod.refreshRequestedBySystem = false;

            if(  UtilityMethod.hasInternetConnection( context ) )
            {
                UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO, "Refresh requested!",
                        TAG + "::onReceive" );

                // Update the weather provider
                largeWidgetRemoteViews.setTextViewText( R.id.txvLastUpdated, "Refreshing..." );

                largeWidgetRemoteViews.setViewVisibility( R.id.imvRefresh, View.INVISIBLE );
                largeWidgetRemoteViews.setViewVisibility( R.id.view_flipper, View.VISIBLE );

                String invoker = this.getClass().getSimpleName() + "::onReceive";
                WeatherLionApplication.refreshWeather( invoker );

                // update the widget
                appWidgetManager.updateAppWidget( appWidgetIds, largeWidgetRemoteViews );
            }// end of if block
            else
            {
                // Calling from a Non-UI Thread
                Handler handler = new Handler( Looper.getMainLooper() );

                handler.post( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        UtilityMethod.butteredToast(  mContext,
                                "Check internet connection!",
                                2, Toast.LENGTH_LONG );
                    }
                });
            }// end of else block      

        }// end of if block
        else if ( CANCEL_REFRESH_BUTTON_CLICKED.equals( intent.getAction() ) )
        {
            UtilityMethod.refreshRequestedByUser = false;
            UtilityMethod.refreshRequestedBySystem = false;

            UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO, "Cancelling refresh request...",
                    TAG + "::onReceive" );

            // Update the current location and update time stamp
            String ts = new SimpleDateFormat( "MMM dd, h:mm a", Locale.ENGLISH ).format(
                    UtilityMethod.lastUpdated );

            // ensure that refreshing animations are removed from the widget
            largeWidgetRemoteViews.setTextViewText( R.id.txvLastUpdated, ts );
            largeWidgetRemoteViews.setViewVisibility( R.id.imvRefresh, View.VISIBLE );
            largeWidgetRemoteViews.setViewVisibility( R.id.view_flipper, View.INVISIBLE );

            // update the widget
            appWidgetManager.updateAppWidget( appWidgetIds, largeWidgetRemoteViews );
        }// end of else if block
        else if ( OFFLINE.equals( intent.getAction() ) )
        {
            // Calling from a Non-UI Thread
            Handler handler = new Handler( Looper.getMainLooper() );

            handler.post( new Runnable()
            {
                @Override
                public void run()
                {
                    UtilityMethod.butteredToast(  mContext,
                            "Check internet connection!",
                            2, Toast.LENGTH_LONG );
                }
            });
        }// end of else if block
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
            UtilityMethod.refreshRequestedBySystem = false;
        }// end of else block

        if( updateExtra != null )
        {
            if( ICON_REFRESH_MESSAGE.equals( updateExtra ) )
            {
                ComponentName watchWidget = new ComponentName( context, LargeWeatherWidgetProvider.class );
                largeWidgetRemoteViews = new RemoteViews( context.getPackageName(),
                        R.layout.wl_large_weather_widget_activity_alternate );
                appWidgetManager.updateAppWidget( watchWidget, largeWidgetRemoteViews );
            }// end of if block
        }// end of if block
    }// end of method onReceive
}// end of class WeatherUpdater LargeWeatherWidgetProvider