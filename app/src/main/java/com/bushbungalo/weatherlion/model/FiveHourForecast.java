package com.bushbungalo.weatherlion.model;

import java.time.LocalDateTime;

/**
 * @author Paul O. Patterson
 * @version     1.0
 * @since       1.0
 *
 * <p>
 * This class is used to maintain a object for five day forecasts.
 * </p>
 * <br />
 * <b style="margin-left:-40px">Date Created:</b>
 * <br />
 * 11/01/19
 */

@SuppressWarnings({"unused"})
public class FiveHourForecast
{
    private LocalDateTime forecastTime;
    private String forecastTemperature;
    private String forecastCondition;

    public FiveHourForecast()
    {
        this( null, null, null );
    }// end of default constructor

    public FiveHourForecast( LocalDateTime forecastDate, String forecastTemperature, String forecastCondition )
    {
        this.forecastTime = forecastDate;
        this.forecastTemperature = forecastTemperature;
        this.forecastCondition = forecastCondition;
    }// end of four-argument constructor

    public LocalDateTime getForecastTime()
    {
        return forecastTime;
    }

    public void setForecastTime( LocalDateTime forecastTime )
    {
        this.forecastTime = forecastTime;
    }

    public String getForecastTemperature()
    {
        return forecastTemperature;
    }

    public void setForecastTemperature( String forecastTemperature )
    {
        this.forecastTemperature = forecastTemperature;
    }

    public String getForecastCondition()
    {
        return forecastCondition;
    }

    public void setForecastCondition(String forecastCondition)
    {
        this.forecastCondition = forecastCondition;
    }
}// end of class FiveHourForecast

