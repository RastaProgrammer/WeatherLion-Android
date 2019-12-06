package com.bushbungalo.weatherlion.custom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bushbungalo.weatherlion.R;
import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.WeatherLionMain;
import com.bushbungalo.weatherlion.model.LastWeatherData;
import com.bushbungalo.weatherlion.utils.UtilityMethod;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressWarnings({"unused", "WeakerAccess"})
public class WeeklyForecastAdapter extends RecyclerView.Adapter< WeeklyForecastAdapter.ViewHolder >
{
    private String TAG = "WeeklyForecastAdapter";

    private List<LastWeatherData.WeatherData.DailyForecast.DayForecast> mWeeklyForecast;
    private LastWeatherData.WeatherData.DailyForecast.DayForecast forecast;

    // field variables
    private TextView txvDayDate;
    private TextView txvDayConditions;
    private ImageView imvDayConditionImage;
    private TextView txvDayHighTemp;
    private TextView txvDayLowTemp;
    private View layout;

    private int mPosition;

    public WeeklyForecastAdapter( List<LastWeatherData.WeatherData.DailyForecast.DayForecast> forecast )
    {
        mWeeklyForecast = forecast;
    }// end of default constructor

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public ViewHolder( final View v )
        {
            super( v );
            layout = v;

            txvDayDate = v.findViewById( R.id.txvDayDate );
            txvDayConditions = v.findViewById( R.id.txvDayConditions );
            imvDayConditionImage = v.findViewById( R.id.imvDayConditionImage );
            txvDayHighTemp = v.findViewById( R.id.txvDayHighTemp );
            txvDayLowTemp = v.findViewById( R.id.txvDayLowTemp );
        }// end of default constructor
    }// end of class ViewHolder

    public void add( int position, LastWeatherData.WeatherData.DailyForecast.DayForecast item )
    {
        mWeeklyForecast.add( position, item );
        notifyItemInserted( position );
    }// end of method add

    public void remove( int position )
    {
        mWeeklyForecast.remove( position );
        notifyItemRemoved( position );
    }// end fo method remove

    @NonNull
    @Override
    public WeeklyForecastAdapter.ViewHolder onCreateViewHolder( ViewGroup parent, int viewType )
    {
        LayoutInflater inflater = LayoutInflater.from( parent.getContext() );
        View v = inflater.inflate( R.layout.wl_forecast_list_expanded, parent, false );
        UtilityMethod.loadCustomFont( (RelativeLayout) v.findViewById( R.id.dayReadings ) );

        return new ViewHolder( v );
    }

    @Override
    public void onBindViewHolder( @NonNull final ViewHolder holder, final int position )
    {
        forecast = mWeeklyForecast.get( position );

        try
        {
            Date forecastDate = null;
            SimpleDateFormat dayFormat = new SimpleDateFormat( "EEEE, MMM d",
                    Locale.ENGLISH );
            String today = dayFormat.format( new Date() );
            Calendar cal = Calendar.getInstance();
            cal.add( Calendar.DAY_OF_MONTH, 1 );
            String tomorrow = dayFormat.format( cal.getTime() );
            String fCondition = UtilityMethod.validateCondition( forecast.getCondition() );
            String fConditionIcon
                = UtilityMethod.weatherImages.get( fCondition.toLowerCase() ) == null
                    ? "na.png" : UtilityMethod.weatherImages.get( fCondition.toLowerCase() );

            try
            {
                forecastDate = new SimpleDateFormat( "EEE MMM dd HH:mm:ss z yyyy",
                        Locale.ENGLISH ).parse( forecast.getDate() );
            }// end of try block
            catch ( ParseException e )
            {
                UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE,
            "Couldn't parse the forecast date!",TAG + "::onBindViewHolder [line: " +
                        e.getStackTrace()[1].getLineNumber()+ "]" );
            }// end of catch block

            if( today.equals( dayFormat.format( forecastDate ) ) )
            {
                txvDayDate.setText( WeatherLionApplication.getAppContext().getString(R.string.today_day) );

                 fConditionIcon = UtilityMethod.getConditionIcon( new StringBuilder( fCondition ), new Date() );
            }// end of if block
            else if( tomorrow.equals( dayFormat.format( forecastDate ) ) )
            {
                txvDayDate.setText( WeatherLionApplication.getAppContext().getString(R.string.tomorrow_day) );
            }// end of else if block
            else
            {
                txvDayDate.setText( dayFormat.format( forecastDate ) );
            }// end of else block

            String wxIcon = String.format( "weather_images/%s/weather_%s",
                WeatherLionApplication.iconSet, fConditionIcon );
            InputStream is = WeatherLionApplication.getAppContext().getAssets().open( wxIcon );
            Drawable d = Drawable.createFromStream( is, null );

            txvDayConditions.setText( fCondition );
            imvDayConditionImage.setImageDrawable( d );

            if( position % 2 == 0 )
            {
                holder.itemView.setBackgroundColor(
                    UtilityMethod.addOpacity( Color.WHITE, 5 )  );
            }// end of if block

        }// end of try block
        catch ( IOException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
            TAG + "::getView [line: " +
                    e.getStackTrace()[1].getLineNumber()+ "]" );
        }// end of catch block

        if( WeatherLionApplication.storedPreferences.getUseMetric() )
        {
            txvDayHighTemp.setText( String.format( "%s%s",
                    Math.round( UtilityMethod.fahrenheitToCelsius(
                            forecast.getHighTemperature() ) ), WeatherLionApplication.DEGREES ) );
            txvDayLowTemp.setText( String.format( "%s%s",
                    Math.round( UtilityMethod.fahrenheitToCelsius(
                            forecast.getLowTemperature() ) ), WeatherLionApplication.DEGREES ) );
        }// end of if block
        else
        {
            txvDayHighTemp.setText( String.format( "%s%s", forecast.getHighTemperature(),
                    WeatherLionApplication.DEGREES ) );
            txvDayLowTemp.setText( String.format( "%s%s", forecast.getLowTemperature(),
                    WeatherLionApplication.DEGREES ) );
        }// end of else block

        // get the height of the device window because we don't have access to the
        // recycler view height before hand
        WindowManager wm = (WindowManager) WeatherLionApplication.getAppContext()
                .getSystemService( Context.WINDOW_SERVICE );
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize( size );
        final int displayHeight = size.y;
        final boolean extendedDataPresent = loadExtendedData( holder.itemView );

        // If we were using a List view, this adapter class would not be necessary.
        // We could go ahead and do something like...
//        View hiddenView = findViewById( R.id.hiddenViewId );
//        hiddenView.measure(View.MeasureSpec.makeMeasureSpec( listView.getWidth(),
//                View.MeasureSpec.AT_MOST ), View.MeasureSpec.makeMeasureSpec( 0,
//                View.MeasureSpec.UNSPECIFIED ) );
//        int hiddenViewHeight = hiddenView.getMeasuredHeight();

        // listen for user touches/clicks and show/hide the view
        // containing the additional data
        layout.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( final View v )
            {
                mPosition = holder.getAdapterPosition();

                // get the calculated height of the view that will not be visible on screen
                // but it's visibility is set to View.VISIBLE
                TableLayout extendedTable = holder.itemView.findViewById( R.id.tblExtendedDetails );
                extendedTable.measure( View.MeasureSpec.makeMeasureSpec( displayHeight,
                        View.MeasureSpec.AT_MOST ), View.MeasureSpec.makeMeasureSpec(
                        0, View.MeasureSpec.UNSPECIFIED ) );

                RelativeLayout noDetails = holder.itemView.findViewById( R.id.noExtendedDetails );
                noDetails.measure( View.MeasureSpec.makeMeasureSpec( displayHeight,
                        View.MeasureSpec.AT_MOST ), View.MeasureSpec.makeMeasureSpec(
                        0, View.MeasureSpec.UNSPECIFIED ) );
                int extendableHeight;

                if( extendedDataPresent )
                {
                    extendableHeight = extendedTable.getMeasuredHeight();
                }// end of if block
                else
                {
                    extendableHeight = noDetails.getMeasuredHeight();
                }// end of else block

                int retractedHeight = holder.itemView.findViewById( R.id.dayReadings ).getHeight();
                int expandedHeight = retractedHeight + extendableHeight;
                ValueAnimator resizeAnimator;

                // reveal the view containing the additional data
                holder.itemView.findViewById( R.id.tblExtendedDetails ).setVisibility( View.VISIBLE );

                if( v.getLayoutParams().height == LayoutParams.WRAP_CONTENT ||
                        v.getLayoutParams().height == retractedHeight )
                {
                    resizeAnimator = ValueAnimator
                        .ofInt( retractedHeight, expandedHeight )
                            .setDuration( WeatherLionMain.LIST_ANIMATION_DURATION );

                    v.getLayoutParams().height = expandedHeight;

                    if( WeatherLionApplication.storedPreferences.getProvider().equals(
                            WeatherLionApplication.YAHOO_WEATHER ) )
                    {
                        // display the view after the layout has already been rendered
                        holder.itemView.findViewById( R.id.noExtendedDetails ).setVisibility( View.VISIBLE );

                        // hide the other view
                        holder.itemView.findViewById( R.id.tblExtendedDetails ).setVisibility( View.GONE );
                    }// end of if block
                    else
                    {
                        // display the view after the layout has already been rendered
                        holder.itemView.findViewById( R.id.tblExtendedDetails ).setVisibility( View.VISIBLE );

                        // hide the other view
                        holder.itemView.findViewById( R.id.noExtendedDetails ).setVisibility( View.GONE );
                    }// end of else block
                }// end of if block
                else
                {
                    resizeAnimator = ValueAnimator
                        .ofInt( expandedHeight, retractedHeight )
                            .setDuration( WeatherLionMain.LIST_ANIMATION_DURATION );

                    v.getLayoutParams().height = retractedHeight;
                }// end of else block

                resizeAnimator.addUpdateListener(
                    new ValueAnimator.AnimatorUpdateListener()
                    {
                        @Override
                        public void onAnimationUpdate( ValueAnimator animation )
                        {
                            v.getLayoutParams().height = (Integer) animation.getAnimatedValue();
                            v.requestLayout();
                        }
                    });

                resizeAnimator.addListener( new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd( Animator animation )
                    {
                        super.onAnimationEnd( animation );

                        int[] location = new int[ 2 ];
                        holder.itemView.findViewById( R.id.txvRow4Col1 ).getLocationOnScreen( location );
                        broadcastItemClick( location );
                    }
                });

                AnimatorSet animationSet = new AnimatorSet();
                animationSet.setInterpolator( new AccelerateDecelerateInterpolator() );
                animationSet.play( resizeAnimator );
                animationSet.start();
            }// end of method  onClick
        });
    }// end of method onBindViewHolder

    private void broadcastItemClick( int[] screenCoordinates )
    {
        Intent itemClickIntent = new Intent( WeatherLionMain.RECYCLER_ITEM_CLICK );
        itemClickIntent.putExtra( WeatherLionMain.RECYCLER_ITEM_LOCATION, screenCoordinates );
        itemClickIntent.putExtra( WeatherLionMain.RECYCLER_ITEM_POSITION, mPosition );
        LocalBroadcastManager manager =
                LocalBroadcastManager.getInstance( WeatherLionApplication.getAppContext() );
        manager.sendBroadcast( itemClickIntent );
    }// end of method broadcastItemClick

    @Override
    public int getItemCount()
    {
        return mWeeklyForecast.size();
    }// end of method getItemCount

    public boolean loadExtendedData( View v )
    {
        // To determine whether extended data exists, check for a field
        // that all providers supply. If it is not equal to null,
        // then extended data was supplied. Wind direction will be used
        // because it can be set to null.

        if( forecast.getWindDirection() != null )
        {
            Context context = WeatherLionApplication.getAppContext();
            float dewPoint;
            float humidity;
            float pressure;
            float windBearing;
            float windSpeed;
            float uvIndex;
            float visibility;
            float ozone;
            String windDirection;
            Date sunrise;
            Date sunset;
            SimpleDateFormat df = new SimpleDateFormat( "h:mm a", Locale.ENGLISH );
            String tempUnit = WeatherLionApplication.storedPreferences.getUseMetric() ?
                    WeatherLionApplication.CELSIUS : WeatherLionApplication.FAHRENHEIT;

            TextView txvRow1Col1 = v.findViewById( R.id.txvRow1Col1 );
            TextView txvRow1Col2 = v.findViewById( R.id.txvRow1Col2 );
            TextView txvRow2Col1 = v.findViewById( R.id.txvRow2Col1 );
            TextView txvRow2Col2 = v.findViewById( R.id.txvRow2Col2 );
            TextView txvRow3Col1 = v.findViewById( R.id.txvRow3Col1 );
            TextView txvRow3Col2 = v.findViewById( R.id.txvRow3Col2 );
            TextView txvRow4Col1 = v.findViewById( R.id.txvRow4Col1 );
            TextView txvRow4Col2 = v.findViewById( R.id.txvRow4Col2 );

            TextView txvNoDetails = v.findViewById( R.id.txvNoDetails );

            switch( WeatherLionApplication.storedPreferences.getProvider() )
            {
                case WeatherLionApplication.DARK_SKY:

                    dewPoint = forecast.getDewPoint();
                    humidity = forecast.getHumidity();
                    pressure = forecast.getPressure();
                    windBearing = forecast.getWindBearing();
                    windSpeed = forecast.getWindSpeed();
                    windDirection = UtilityMethod.compassDirection( windBearing );

                    if( WeatherLionApplication.storedPreferences.getUseMetric() )
                    {
                        txvRow1Col1.setText( context.getString( R.string.dew_point ) );
                        txvRow1Col2.setText( String.format( Locale.ENGLISH, "%d%s",
                            Math.round( UtilityMethod.fahrenheitToCelsius( dewPoint ) ) ,
                                tempUnit ) );

                        txvRow2Col1.setText( context.getString( R.string.forecast_humidity ) );
                        txvRow2Col2.setText( String.format( Locale.ENGLISH,
                                "%d%%", Math.round( humidity ) ) );

                        txvRow3Col1.setText( context.getString( R.string.forecast_pressure ) );
                        txvRow3Col2.setText( String.format( Locale.ENGLISH, "%d hPa",
                                Math.round( UtilityMethod.inHgToHpa( pressure ) ) ) );

                        txvRow4Col1.setText( context.getString( R.string.forecast_wind ) );
                        txvRow4Col2.setText( String.format( Locale.ENGLISH, "%s %d km/h",
                            windDirection,
                                Math.round( UtilityMethod.mphToKmh( windSpeed ) ) ) );
                    }// end of if block
                    else
                    {
                        txvRow1Col1.setText( context.getString( R.string.dew_point ) );
                        txvRow1Col2.setText( String.format( Locale.ENGLISH, "%d%s",
                                Math.round( dewPoint ) , tempUnit ) );

                        txvRow2Col1.setText( context.getString( R.string.forecast_humidity ) );
                        txvRow2Col2.setText( String.format( Locale.ENGLISH,
                                "%d%%", Math.round( humidity ) ) );

                        txvRow3Col1.setText( context.getString( R.string.forecast_pressure ) );
                        txvRow3Col2.setText( String.format( Locale.ENGLISH, "%d inHg", Math.round( pressure ) ) );

                        txvRow4Col1.setText( context.getString( R.string.forecast_wind ) );
                        txvRow4Col2.setText( String.format( Locale.ENGLISH, "%s %d mph",
                                windDirection, Math.round( windSpeed ) ) );
                    }// end of else block

                    break;

                case WeatherLionApplication.HERE_MAPS:

                    dewPoint = forecast.getDewPoint();
                    humidity = forecast.getHumidity();
                    windDirection = forecast.getWindDirection();
                    windSpeed = forecast.getWindSpeed();
                    uvIndex = forecast.getUvIndex();

                    if( WeatherLionApplication.storedPreferences.getUseMetric() )
                    {
                        txvRow1Col1.setText( context.getString( R.string.dew_point ) );
                        txvRow1Col2.setText( String.format( Locale.ENGLISH, "%d%s",
                                Math.round( UtilityMethod.fahrenheitToCelsius( dewPoint ) ) ,
                                tempUnit ) );

                        txvRow2Col1.setText( context.getString( R.string.forecast_humidity ) );
                        txvRow2Col2.setText( String.format( Locale.ENGLISH,
                                "%d%%", Math.round( humidity ) ) );

                        txvRow3Col1.setText( context.getString( R.string.forecast_uv_index ) );
                        txvRow3Col2.setText( String.valueOf( Math.round( uvIndex ) ) );

                        txvRow4Col1.setText( context.getString( R.string.forecast_wind ) );
                        txvRow4Col2.setText( String.format( Locale.ENGLISH, "%s %d km/h",
                                windDirection,
                                Math.round( UtilityMethod.mphToKmh( windSpeed ) ) ) );
                    }// end of if block
                    else
                    {
                        txvRow1Col1.setText( context.getString( R.string.dew_point ) );
                        txvRow1Col2.setText( String.format( Locale.ENGLISH, "%d%s",
                                Math.round( dewPoint ) , tempUnit ) );

                        txvRow2Col1.setText( context.getString( R.string.forecast_humidity ) );
                        txvRow2Col2.setText( String.format( Locale.ENGLISH,
                                "%d%%", Math.round( humidity ) ) );

                        txvRow3Col1.setText( context.getString( R.string.forecast_uv_index ) );
                        txvRow3Col2.setText( String.valueOf( Math.round( uvIndex ) ) );

                        txvRow4Col1.setText( context.getString( R.string.forecast_wind ) );
                        txvRow4Col2.setText( String.format( Locale.ENGLISH, "%s %d mph",
                                windDirection, Math.round( windSpeed ) ) );
                    }// end of else block

                    break;

                case WeatherLionApplication.OPEN_WEATHER:

                    pressure = forecast.getPressure();
                    humidity= forecast.getHumidity();
                    windSpeed = forecast.getWindSpeed();
                    windBearing = forecast.getWindBearing();
                    windDirection = UtilityMethod.compassDirection( windBearing );

                    sunrise = null;
                    sunset = null;

                    try
                    {
                        sunrise = new SimpleDateFormat( "EEE MMM dd HH:mm:ss z yyyy",
                                Locale.ENGLISH ).parse( forecast.getSunrise() );

                        sunset = new SimpleDateFormat( "EEE MMM dd HH:mm:ss z yyyy",
                                Locale.ENGLISH ).parse( forecast.getSunset() );
                    }// end of try block
                    catch ( ParseException e )
                    {
                        UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE,
                                "Couldn't parse the forecast date!",TAG + "::loadExtendedData [line: " +
                                        e.getStackTrace()[1].getLineNumber()+ "]" );
                    }// end of catch block

                    if( WeatherLionApplication.storedPreferences.getUseMetric() )
                    {
                        txvRow1Col1.setText( context.getString( R.string.forecast_humidity ) );
                        txvRow1Col2.setText( String.format( Locale.ENGLISH,
                                "%d%%", Math.round( humidity ) ) );

                        txvRow2Col1.setText( context.getString( R.string.forecast_pressure ) );
                        txvRow2Col2.setText( String.format( Locale.ENGLISH, "%d hPa",
                                Math.round( UtilityMethod.inHgToHpa( pressure ) ) ) );

                        txvRow3Col1.setText( context.getString( R.string.forecast_wind ) );
                        txvRow3Col2.setText( String.format( Locale.ENGLISH, "%s %d mph",
                                windDirection, Math.round( windSpeed ) ) );

                        txvRow4Col1.setText( context.getString( R.string.forecast_astronomy ) );
                        txvRow4Col2.setText( String.format( Locale.ENGLISH, "%s/%s",
                                df.format( sunrise ), df.format( sunset ) ) );
                    }// end of if block
                    else
                    {
                        txvRow1Col1.setText( context.getString( R.string.forecast_humidity ) );
                        txvRow1Col2.setText( String.format( Locale.ENGLISH,
                                "%d%%", Math.round( humidity ) ) );

                        txvRow2Col1.setText( context.getString( R.string.forecast_pressure ) );
                        txvRow2Col2.setText( String.format( Locale.ENGLISH, "%d inHg", Math.round( pressure ) ) );

                        txvRow3Col1.setText( context.getString( R.string.forecast_wind ) );
                        txvRow3Col2.setText( String.format( Locale.ENGLISH, "%s %d mph",
                                windDirection, Math.round( windSpeed ) ) );

                        txvRow4Col1.setText( context.getString( R.string.forecast_astronomy ) );
                        txvRow4Col2.setText( String.format( Locale.ENGLISH, "%s/%s",
                                df.format( sunrise ), df.format( sunset ) ) );
                    }// end of else block

                    break;

                case WeatherLionApplication.WEATHER_BIT:

                    dewPoint = forecast.getDewPoint();
                    pressure = forecast.getPressure();
                    windSpeed = forecast.getWindSpeed();
                    windDirection = forecast.getWindDirection();
                    sunrise = null;
                    sunset = null;

                    try
                    {
                        sunrise = new SimpleDateFormat( "EEE MMM dd HH:mm:ss z yyyy",
                                Locale.ENGLISH ).parse( forecast.getSunrise() );

                        sunset = new SimpleDateFormat( "EEE MMM dd HH:mm:ss z yyyy",
                                Locale.ENGLISH ).parse( forecast.getSunset() );
                    }// end of try block
                    catch ( ParseException e )
                    {
                        UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE,
                                "Couldn't parse the forecast date!",TAG + "::loadExtendedData [line: " +
                                        e.getStackTrace()[1].getLineNumber()+ "]" );
                    }// end of catch block


                    if( WeatherLionApplication.storedPreferences.getUseMetric() )
                    {
                        txvRow1Col1.setText( context.getString( R.string.dew_point ) );
                        txvRow1Col2.setText( String.format( Locale.ENGLISH, "%d%s",
                                Math.round( UtilityMethod.fahrenheitToCelsius( dewPoint ) ) ,
                                tempUnit ) );

                        txvRow2Col1.setText( context.getString( R.string.forecast_pressure ) );
                        txvRow2Col2.setText( String.format( Locale.ENGLISH, "%d hPa",
                                Math.round( UtilityMethod.inHgToHpa( pressure ) ) ) );

                        txvRow3Col1.setText( context.getString( R.string.forecast_wind ) );
                        txvRow3Col2.setText( String.format( Locale.ENGLISH, "%s %d km/h",
                            windDirection,
                                Math.round( UtilityMethod.mphToKmh( windSpeed ) ) ) );

                        txvRow4Col1.setText( context.getString( R.string.forecast_astronomy ) );
                        txvRow4Col2.setText( String.format( Locale.ENGLISH, "%s/%s",
                                df.format( sunrise ), df.format( sunset ) ) );
                    }// end of if block
                    else
                    {
                        txvRow1Col1.setText( context.getString( R.string.dew_point ) );
                        txvRow1Col2.setText( String.format( Locale.ENGLISH, "%d%s",
                                Math.round( dewPoint ) , tempUnit ) );

                        txvRow2Col1.setText( context.getString( R.string.forecast_pressure ) );
                        txvRow2Col2.setText( String.format( Locale.ENGLISH, "%d inHg", Math.round( pressure ) ) );

                        txvRow3Col1.setText( context.getString( R.string.forecast_wind ) );
                        txvRow3Col2.setText( String.format( Locale.ENGLISH, "%s %d mph",
                                windDirection, Math.round( windSpeed ) ) );

                        txvRow4Col1.setText( context.getString( R.string.forecast_astronomy ) );
                        txvRow4Col2.setText( String.format( Locale.ENGLISH, "%s/%s",
                                df.format( sunrise ), df.format( sunset ) ) );
                    }// end of else block

                    break;

                case WeatherLionApplication.YR_WEATHER:

                    pressure = forecast.getPressure();
                    ozone = forecast.getOzone();
                    windSpeed = forecast.getWindSpeed();
                    windDirection = forecast.getWindDirection();

                    if( WeatherLionApplication.storedPreferences.getUseMetric() )
                    {
                        txvRow1Col1.setText( context.getString( R.string.forecast_precipitation ) );
                        txvRow1Col2.setText( String.format( Locale.ENGLISH, "%d mm",
                            Math.round( UtilityMethod.inchesToMillimeters( (int) ozone ) ) ) );

                        txvRow2Col1.setText( context.getString( R.string.forecast_pressure ) );
                        txvRow2Col2.setText( String.format( Locale.ENGLISH, "%d hPa",
                                Math.round( UtilityMethod.inHgToHpa( pressure ) ) ) );

                        txvRow3Col1.setText( context.getString( R.string.forecast_wind ) );
                        txvRow3Col2.setText( String.format( Locale.ENGLISH, "%s %d km/h",
                            windDirection,
                                Math.round( UtilityMethod.mphToKmh( windSpeed ) ) ) );
                    }// end of if block
                    else
                    {
                        txvRow1Col1.setText( context.getString( R.string.forecast_precipitation ) );
                        txvRow1Col2.setText( String.format( Locale.ENGLISH, "%d in",
                                Math.round( ozone ) ) );

                        txvRow2Col1.setText( context.getString( R.string.forecast_pressure ) );
                        txvRow2Col2.setText( String.format( Locale.ENGLISH, "%d inHg", Math.round( pressure ) ) );

                        txvRow3Col1.setText( context.getString( R.string.forecast_wind ) );
                        txvRow3Col2.setText( String.format( Locale.ENGLISH, "%s %d mph",
                            windDirection, Math.round( Math.round( windSpeed ) ) ) );
                    }// end of else block

                    // keep the last row empty
                    txvRow4Col1.setText( context.getString( R.string.no_data ) );
                    txvRow4Col2.setText( context.getString( R.string.no_data ) );

                    break;
                default:

                    txvNoDetails.setText( String.format( Locale.ENGLISH, "%s does not provide extended data.",
                        WeatherLionApplication.storedPreferences.getProvider() ) );

                    break;
            }// end of switch block

            return true;
        }// end of if block
        else
        {
            // no extended data was loaded
            return false;
        }// end of else block
    }// end of method loadExtendedData
}// end of class WeeklyForecastAdapter
