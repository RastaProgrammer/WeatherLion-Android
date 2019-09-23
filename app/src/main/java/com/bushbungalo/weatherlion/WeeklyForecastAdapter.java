package com.bushbungalo.weatherlion;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bushbungalo.weatherlion.model.LastWeatherData;
import com.bushbungalo.weatherlion.utils.UtilityMethod;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@SuppressWarnings({"unused", "WeakerAccess"})
public class WeeklyForecastAdapter extends RecyclerView.Adapter< WeeklyForecastAdapter.ViewHolder >
{
    private List<LastWeatherData.WeatherData.DailyForecast.DayForecast> mWeeklyForecast;

    public WeeklyForecastAdapter( List<LastWeatherData.WeatherData.DailyForecast.DayForecast> forecast )
    {
        mWeeklyForecast = forecast;
    }// end of default constructor

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        // each data item is just a string in this case
        public TextView txvDayDate;
        public TextView txvDayConditions;
        public ImageView imvDayConditionImage;
        public TextView txvDayHighTemp;
        public TextView txvDayLowTemp;
        public View layout;

        public ViewHolder( View v )
        {
            super( v );
            layout = v;

            txvDayDate = v.findViewById( R.id.txvDayDate );
            txvDayConditions = v.findViewById( R.id.txvDayConditions );
            imvDayConditionImage = v.findViewById( R.id.imvDayConditionImage );
            txvDayHighTemp = v.findViewById( R.id.txvDayHighTemp );
            txvDayLowTemp = v.findViewById( R.id.txvDayLowTemp );
        }
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
        View v = inflater.inflate( R.layout.wl_forecast_list, parent, false );
        UtilityMethod.loadCustomFont( (RelativeLayout) v.findViewById( R.id.dayReadings ) );

        return new ViewHolder( v );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position)
    {
        String TAG = "WeeklyForecastAdapter";

        final LastWeatherData.WeatherData.DailyForecast.DayForecast forecast =
                mWeeklyForecast.get( position );

        try
        {
            String fCondition = forecast.getCondition();

            if( fCondition.toLowerCase().contains( "(day)" ) )
            {
                fCondition = fCondition.replace( "(day)", "" ).trim();
            }// end of if block
            else if( fCondition.toLowerCase().contains( "(night)" ) )
            {
                fCondition = fCondition.replace( "(night)", "" ).trim();
            }// end of if block

            String fConditionIcon
                    = UtilityMethod.weatherImages.get( fCondition.toLowerCase() ) == null
                    ? "na.png" : UtilityMethod.weatherImages.get( fCondition.toLowerCase() );
            String wxIcon = "weather_images/" + WeatherLionApplication.iconSet + "/weather_" + fConditionIcon;

            InputStream is = WeatherLionApplication.getAppContext().getAssets().open( wxIcon );
            Drawable d = Drawable.createFromStream( is, null );

            Date forecastDate = null;

            try
            {
                forecastDate = new SimpleDateFormat( "EEE MMM dd HH:mm:ss z yyyy",
                        Locale.ENGLISH ).parse( forecast.getDate() );
            }// end of try block
            catch ( ParseException e )
            {
                UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE,
            "Couldn't parse the forecast date!",TAG + "::loadPreviousWeather [line: " +
                        e.getStackTrace()[1].getLineNumber()+ "]" );
            }// end of catch block

            holder.txvDayDate.setText( new SimpleDateFormat( "EEEE, MMM d",
                    Locale.ENGLISH ).format( forecastDate ) );
            holder.txvDayConditions.setText( fCondition );
            holder.imvDayConditionImage.setImageDrawable( d );
        }// end of try block
        catch ( IOException e )
        {
            UtilityMethod.logMessage(UtilityMethod.LogLevel.SEVERE, e.getMessage(),
            TAG + "::getView [line: " +
                    e.getStackTrace()[1].getLineNumber()+ "]" );
        }// end of catch block

        if( WeatherLionApplication.storedPreferences.getUseMetric() )
        {
            holder.txvDayHighTemp.setText( String.format( "%s%s",
                    Math.round( UtilityMethod.fahrenheitToCelsius(
                            forecast.getHighTemperature() ) ), WeatherLionApplication.DEGREES ) );
            holder.txvDayLowTemp.setText( String.format( "%s%s",
                    Math.round( UtilityMethod.fahrenheitToCelsius(
                            forecast.getLowTemperature() ) ), WeatherLionApplication.DEGREES ) );
        }// end of if block
        else
        {
            holder.txvDayHighTemp.setText( String.format( "%s%s", forecast.getHighTemperature(),
                    WeatherLionApplication.DEGREES ) );
            holder.txvDayLowTemp.setText( String.format( "%s%s", forecast.getLowTemperature(),
                    WeatherLionApplication.DEGREES ) );
        }// end of else block
    }// end of method onBindViewHolder

    @Override
    public int getItemCount()
    {
        return mWeeklyForecast.size();
    }// end of method getItemCount

}// end of class WeeklyForecastAdapter
