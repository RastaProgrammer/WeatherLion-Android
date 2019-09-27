package com.bushbungalo.weatherlion;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.bushbungalo.weatherlion.services.WidgetUpdateService;
import com.bushbungalo.weatherlion.utils.UtilityMethod;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

/*
 * Created by Paul O. Patterson on 11/7/17.
 */
@SuppressWarnings({"unused", "SameParameterValue"})
public class WeatherWidgetProvider extends AppWidgetProvider
{
    public static String currentCondition = null;
    public static String sunriseTime = null;
    public static String sunsetTime = null;
    public static RemoteViews remoteViews;

    private static final String TAG = "WeatherWidgetProvider";
    public static final String REFRESH_BUTTON_CLICKED = "Refresh";
    private static final String LAUNCH_MAIN = "Main";
    private static final String OPEN_CLOCK_APP = "Clock";
    public static final String CLOCK_UPDATE_MESSAGE = "ClockUpdateMessage";
    public static final String WIDGET_UPDATE_MESSAGE = "WidgetUpdateMessage";
    public static final String ICON_REFRESH_MESSAGE = "WidgetIconRefreshMessage";
    private BroadcastReceiver mSystemBroadcastReceiver;

    private static AppWidgetManager mAppWidgetManager;

    public static boolean dataLoadedSuccessfully = false;

    public static boolean sunriseIconsInUse;
    public static boolean sunriseUpdatedPerformed;
    public static boolean sunsetIconsInUse;
    public static boolean sunsetUpdatedPerformed;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEnabled( Context context )
    {
        super.onEnabled( context );

        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences( context );
        String currLocation = spf.getString( "pref_location", null );
        UtilityMethod.weatherWidgetEnabled
                = currLocation != null && !currLocation.equalsIgnoreCase( "Unknown" );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDisabled( Context context )
    {
        super.onDisabled( context );
        UtilityMethod.weatherWidgetEnabled = false;
        WeatherLionApplication.running = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpdate( Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds )
    {
        mAppWidgetManager = appWidgetManager;
        UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO, "Update request received!",
                TAG + "::onUpdate" );

        ComponentName watchWidget = new ComponentName( context, WeatherWidgetProvider.class );
        remoteViews = new RemoteViews( context.getPackageName(), R.layout.wl_weather_widget_activity );
        int appWidgetId = INVALID_APPWIDGET_ID;

        if ( appWidgetIds != null )
        {
            int n = appWidgetIds.length;

            if ( n == 1 )
            {
                appWidgetId = appWidgetIds[ 0 ];
            }// end of if block
        }// end of if block

        // Provider intent
        Intent intent = new Intent( context, WeatherWidgetProvider.class );
        intent.putExtra( EXTRA_APPWIDGET_ID, appWidgetId );

        // set the applicable flags that main will use to start the service
        WeatherLionApplication.currentWidgetId = appWidgetId;
        WeatherLionApplication.changeWidgetUnit =  false;

        Intent updateIntent = new Intent( context, WidgetUpdateService.class );
        updateIntent.putExtra( EXTRA_APPWIDGET_ID, appWidgetId );
        updateIntent.setData( Uri.parse( "false" ) );
        WidgetUpdateService.enqueueWork( context, updateIntent );

        if( !WeatherLionApplication.running )
        {
            WeatherLionApplication.running = true;

            // send a broad cast to let main initiate the service
            Intent clockIntent = new Intent( CLOCK_UPDATE_MESSAGE );
            context.sendBroadcast( clockIntent );
        }// end of if block

        // set the click listener for the refresh image
        PendingIntent refreshIntent = PendingIntent.getBroadcast( context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        remoteViews.setOnClickPendingIntent( R.id.imvRefresh, refreshIntent );

        remoteViews.setOnClickPendingIntent(
                R.id.imvRefresh,
                getPendingSelfIntent( context, REFRESH_BUTTON_CLICKED )
        );

        PendingIntent mainIntent = PendingIntent.getBroadcast( context,
                0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        remoteViews.setOnClickPendingIntent( R.id.imvCurrentCondition, mainIntent );

        remoteViews.setOnClickPendingIntent(
                R.id.imvCurrentCondition,
                getPendingSelfIntent( context, LAUNCH_MAIN )
        );

        remoteViews.setOnClickPendingIntent(
                R.id.relClock,
                getPendingSelfIntent( context, OPEN_CLOCK_APP )
        );

        Intent iconRefreshIntent = new Intent( context, WeatherWidgetProvider.class );
        iconRefreshIntent.setAction( WeatherWidgetProvider.ICON_REFRESH_MESSAGE );
        PendingIntent pendingIntent = PendingIntent.getBroadcast( context, 0,
                iconRefreshIntent, 0 );

        appWidgetManager.updateAppWidget( watchWidget, remoteViews );
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

        UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO,
                intent.getAction() + " requested with action " +
                        intent.getStringExtra( WIDGET_UPDATE_MESSAGE ),
                TAG + "::onReceive" );

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance( context );
        remoteViews = new RemoteViews( context.getPackageName(),
                R.layout.wl_weather_widget_activity );
        String updateExtra = intent.getStringExtra( WIDGET_UPDATE_MESSAGE );
        int[] appWidgetIds = AppWidgetManager.getInstance( context )
                .getAppWidgetIds( new ComponentName( context,
                        WeatherWidgetProvider.class ) );

        // If the refresh button was clicked text clicked
        if ( REFRESH_BUTTON_CLICKED.equals( intent.getAction() ) )
        {
            UtilityMethod.refreshRequested = true;

            UtilityMethod.logMessage(UtilityMethod.LogLevel.INFO, "Refresh requested!",
                    TAG + "::onReceive");

            // Update the weather provider
            remoteViews.setTextViewText( R.id.txvLastUpdated, "Refreshing..." );

            Intent refreshIntent = new Intent( context, WidgetUpdateService.class );
            refreshIntent.setData( Uri.parse( WeatherLionApplication.UNIT_NOT_CHANGED ) );
            WidgetUpdateService.enqueueWork( context, refreshIntent );

            // update the widget
            appWidgetManager.updateAppWidget( appWidgetIds, remoteViews );

        }// end of if block
        else if ( LAUNCH_MAIN.equals( intent.getAction() ) )
        {
            Intent mainActivityIntent = new Intent( context, WeatherLionMain.class );
            context.startActivity( mainActivityIntent );
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
                ComponentName watchWidget = new ComponentName( context, WeatherWidgetProvider.class );
                remoteViews = new RemoteViews( context.getPackageName(), R.layout.wl_weather_widget_activity );
                appWidgetManager.updateAppWidget( watchWidget, remoteViews );
            }// end of if block
        }// end of if block
    }// end of method onReceive

    public static void checkAstronomy()
    {
        // update icons based on the time of day in relation to sunrise and sunset times
        if( WeatherLionApplication.currentSunriseTime.length() != 0 &&
                WeatherLionApplication.currentSunsetTime.length() != 0 )
        {
            // Load current condition weather image
            Calendar rightNow = Calendar.getInstance();
            Calendar nightFall = Calendar.getInstance();
            Calendar sunUp = Calendar.getInstance();
            String sunsetTwenty4HourTime = new SimpleDateFormat( "yyyy-MM-dd",
                    Locale.ENGLISH ).format( rightNow.getTime() )
                    + " " + UtilityMethod.get24HourTime( WeatherLionApplication.currentSunsetTime.toString() );
            String sunriseTwenty4HourTime = new SimpleDateFormat( "yyyy-MM-dd",
                    Locale.ENGLISH ).format( rightNow.getTime() )
                    + " " + UtilityMethod.get24HourTime( WeatherLionApplication.currentSunriseTime.toString() );
            SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm", Locale.ENGLISH );
            Date rn = null; // date time right now (rn)
            Date nf = null; // date time night fall (nf)
            Date su = null; // date time sun up (su)

            try
            {
                rn = sdf.parse(sdf.format( rightNow.getTime() ) );
                nightFall.setTime(sdf.parse( sunsetTwenty4HourTime ) );
                nightFall.set(Calendar.MINUTE,
                        Integer.parseInt(sunsetTwenty4HourTime.split( ":" )[ 1 ].trim() ) );
                sunUp.setTime( sdf.parse( sunriseTwenty4HourTime ) );

                nf = sdf.parse( sdf.format( nightFall.getTime() ) );
                su = sdf.parse( sdf.format( sunUp.getTime() ) );
            } // end of try block
            catch ( ParseException e )
            {
                UtilityMethod.logMessage(UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                TAG + "::checkAstronomy [line: " +
                            e.getStackTrace()[1].getLineNumber() + "]");
            }// end of catch block

            String currentConditionIcon;

            if ( ( Objects.requireNonNull( rn ).equals( nf ) || rn.after( nf ) || rn.before( su ) ) )
            {
                if ( WidgetUpdateService.currentCondition.toString().toLowerCase().contains( "(night)" ) )
                {
                    currentConditionIcon = UtilityMethod.weatherImages.get(
                            WidgetUpdateService.currentCondition.toString().toLowerCase() );
                }// end of if block
                else
                {
                    // Yahoo has a habit of having sunny nights
                    if ( WidgetUpdateService.currentCondition.toString().equalsIgnoreCase( "sunny" ) )
                    {
                        WidgetUpdateService.currentCondition.setLength( 0 );
                        WidgetUpdateService.currentCondition.append( "Clear" );
                    }// end of if block

                    if ( UtilityMethod.weatherImages.containsKey(
                            WidgetUpdateService.currentCondition.toString().toLowerCase() + " (night)" ) )
                    {
                        currentConditionIcon =
                                UtilityMethod.weatherImages.get(
                                        WidgetUpdateService.currentCondition.toString().toLowerCase() + " (night)" );
                    }// end of if block
                    else {
                        currentConditionIcon = UtilityMethod.weatherImages.get(
                                WidgetUpdateService.currentCondition.toString().toLowerCase() );
                    }// end of else block
                }// end of else block

                if ( !sunsetUpdatedPerformed && !sunsetIconsInUse )
                {
                    sunsetIconsInUse = true;
                    sunriseIconsInUse = false;
                    sunsetUpdatedPerformed = true;
                    sunriseUpdatedPerformed = false;
                }// end of if block
                else if ( WeatherLionApplication.iconSetSwitch)
                {
                    // reset the flag after switch is made
                    WeatherLionApplication.iconSetSwitch = false;
                }// end of else if block
            }// end of if block
            else if ( WeatherLionApplication.iconSetSwitch )
            {
                currentConditionIcon = UtilityMethod.weatherImages.get(
                        WidgetUpdateService.currentCondition.toString().toLowerCase() );

                // reset the flag after switch is made
                WeatherLionApplication.iconSetSwitch = false;
            }// end of else if block
            else
            {
                currentConditionIcon = UtilityMethod.weatherImages.get(
                        WidgetUpdateService.currentCondition.toString().toLowerCase() );

                if ( !sunriseUpdatedPerformed && !sunriseIconsInUse )
                {
                    sunriseUpdatedPerformed = true;
                    sunsetUpdatedPerformed = false;
                }// end of if block
                else if ( WeatherLionApplication.iconSetSwitch)
                {
                    currentConditionIcon = UtilityMethod.weatherImages.get(
                            WidgetUpdateService.currentCondition.toString().toLowerCase() );

                    // reset the flag after switch is made
                    WeatherLionApplication.iconSetSwitch = false;
                }// end of else if block
                else
                {
                    sunriseIconsInUse = true;
                    sunsetIconsInUse = false;
                }// end of else block
            }// end of else block

            // Load applicable icon based on the time of day
            String imageFile = String.format( "weather_images/%s/weather_%s", WeatherLionApplication.iconSet
                    , currentConditionIcon );

            remoteViews = new RemoteViews(
                    WeatherLionApplication.getAppContext().getPackageName(),
                    R.layout.wl_weather_widget_activity );

            try( InputStream is = WeatherLionApplication.getAppContext().getAssets().open( imageFile ) )
            {
                Bitmap bmp = BitmapFactory.decodeStream(is);
                remoteViews.setImageViewBitmap(R.id.imvCurrentCondition, bmp);
                // update the widget
                mAppWidgetManager.updateAppWidget( WeatherLionApplication.currentWidgetId, remoteViews );
            }// end of try block
            catch ( IOException e )
            {
                UtilityMethod.butteredToast( WeatherLionApplication.getAppContext(), e.toString(), 2, Toast.LENGTH_SHORT );
            }// end of catch block
        }// end of if block
    }// end of method checkAstronomy
}// end of class WeatherUpdater WeatherWidgetProvider