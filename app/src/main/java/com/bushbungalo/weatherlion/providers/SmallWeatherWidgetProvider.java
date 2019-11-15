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
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.bushbungalo.weatherlion.ConfigureWidget;
import com.bushbungalo.weatherlion.R;
import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.WeatherLionMain;
import com.bushbungalo.weatherlion.utils.UtilityMethod;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;

/*
 * Created by Paul O. Patterson on 11/7/17.
 */
@SuppressWarnings({"unused", "SameParameterValue"})
public class SmallWeatherWidgetProvider extends AppWidgetProvider
{
    private static String currentCondition = null;
    private static String sunriseTime = null;
    private static String sunsetTime = null;
    private static RemoteViews smallWidgetRemoteViews;

    private static final String TAG = "SmallWeatherWidgetProvider";
    private static final String LAUNCH_MAIN = "Main";
    private static final String OFFLINE = "Offline";
    private BroadcastReceiver mSystemBroadcastReceiver;

    public static final String REFRESH_BUTTON_CLICKED = "Refresh";
    public static final String WIDGET_UPDATE_MESSAGE = "WidgetUpdateMessage";
    public static final String ICON_REFRESH_MESSAGE = "WidgetIconRefreshMessage";

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEnabled( Context context )
    {
        super.onEnabled(context);
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
        ComponentName smallWidget = new ComponentName( context, SmallWeatherWidgetProvider.class );
        smallWidgetRemoteViews = new RemoteViews( context.getPackageName(),
                R.layout.wl_small_weather_widget_activity );

        int[] allWidgetIds = appWidgetManager.getAppWidgetIds( smallWidget );

        for ( int appWidgetId : allWidgetIds )
        {
            // Provider intent
            Intent intent = new Intent( context, SmallWeatherWidgetProvider.class );
            intent.putExtra( EXTRA_APPWIDGET_ID, appWidgetId );

            // set the applicable flags that main will use to start the service
            WeatherLionApplication.changeWidgetUnit =  false;

            UtilityMethod.refreshRequestedBySystem = true;
            UtilityMethod.refreshRequestedByUser = false;

            String invoker = this.getClass().getSimpleName() + "::onUpdate";
            WeatherLionApplication.callMethodByName( null,"refreshWeather",
                    new Class[]{ String.class }, new Object[]{ invoker } );

            // set the click listener for the refresh image
            PendingIntent refreshIntent = PendingIntent.getBroadcast( context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
            smallWidgetRemoteViews.setOnClickPendingIntent( R.id.imvRefresh, refreshIntent );

            smallWidgetRemoteViews.setOnClickPendingIntent(
                    R.id.imvRefresh,
                    getPendingSelfIntent( context, REFRESH_BUTTON_CLICKED )
            );

            smallWidgetRemoteViews.setOnClickPendingIntent(
                    R.id.imvOffline,
                    getPendingSelfIntent( context, OFFLINE )
            );

            PendingIntent mainIntent = PendingIntent.getBroadcast( context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
            smallWidgetRemoteViews.setOnClickPendingIntent( R.id.imvCurrentCondition, mainIntent );

            smallWidgetRemoteViews.setOnClickPendingIntent(
                    R.id.imvCurrentCondition,
                    getPendingSelfIntent( context, LAUNCH_MAIN )
            );

            smallWidgetRemoteViews.setOnClickPendingIntent(
                    R.id.rlInvalidWidget,
                    getPendingSelfIntent( context, WeatherLionApplication.LAUNCH_CONFIG )
            );

            appWidgetManager.updateAppWidget( smallWidget, smallWidgetRemoteViews);
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive( Context context, Intent intent )
    {
        super.onReceive( context, intent );
        final Context mContext = context;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance( context );
        smallWidgetRemoteViews = new RemoteViews( context.getPackageName(),
                R.layout.wl_small_weather_widget_activity );
        String updateExtra = intent.getStringExtra( WIDGET_UPDATE_MESSAGE );
        int[] appWidgetIds = AppWidgetManager.getInstance( context )
                .getAppWidgetIds( new ComponentName( context,
                        SmallWeatherWidgetProvider.class ) );

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
                smallWidgetRemoteViews.setTextViewText( R.id.txvLastUpdated, "Refreshing..." );

                smallWidgetRemoteViews.setViewVisibility( R.id.imvRefresh, View.INVISIBLE );
                smallWidgetRemoteViews.setViewVisibility( R.id.view_flipper, View.VISIBLE );

                String invoker = this.getClass().getSimpleName() + "::onReceive";
                WeatherLionApplication.callMethodByName( null, "refreshWeather",
                        new Class[]{ String.class }, new Object[]{ invoker } );

                // update the widget
                appWidgetManager.updateAppWidget( appWidgetIds, smallWidgetRemoteViews );
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
                        UtilityMethod.butteredToast( mContext,
                                "Check internet connection!",
                                2, Toast.LENGTH_LONG );
                    }
                });
            }// end of else block
        }// end of if block
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
        else
        {
            UtilityMethod.refreshRequestedBySystem = false;
        }// end of else block

        if( updateExtra != null )
        {
            if( ICON_REFRESH_MESSAGE.equals( updateExtra ) )
            {
                ComponentName watchWidget = new ComponentName( context, SmallWeatherWidgetProvider.class );
                smallWidgetRemoteViews = new RemoteViews( context.getPackageName(),
                        R.layout.wl_small_weather_widget_activity );
                appWidgetManager.updateAppWidget( watchWidget, smallWidgetRemoteViews );
            }// end of if block
        }// end of if block
    }// end of method onReceive
}// end of class WeatherUpdater SmallWeatherWidgetProvider