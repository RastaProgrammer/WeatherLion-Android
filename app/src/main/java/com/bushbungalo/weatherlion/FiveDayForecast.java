package com.bushbungalo.weatherlion;

import java.util.Date;

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
 * 11/16/17
 */

@SuppressWarnings("unused")
public class FiveDayForecast
{
    private Date forecastDate;
    private String forecastHighTemp;
    private String forecastLowTemp;
    private String forecastCondition;

    public FiveDayForecast()
    {
        this( null, null, null, null );
    }// end of default constructor

    public FiveDayForecast( Date forecastDate, String forecastHighTemp,
                            String forecastLowTemp, String forecastCondition )
    {
        this.forecastDate = forecastDate;
        this.forecastHighTemp = forecastHighTemp;
        this.forecastLowTemp = forecastLowTemp;
        this.forecastCondition = forecastCondition;
    }// end of four-argument constructor

    public Date getForecastDate()
    {
        return forecastDate;
    }

    public void setForecastDate(Date forecastDate)
    {
        this.forecastDate = forecastDate;
    }

    public String getForecastHighTemp()
    {
        return forecastHighTemp;
    }

    public void setForecastHighTemp(String forecastHighTemp)
    {
        this.forecastHighTemp = forecastHighTemp;
    }

    public String getForecastLowTemp()
    {
        return forecastLowTemp;
    }

    public void setForecastLowTemp(String forecastLowTemp)
    {
        this.forecastLowTemp = forecastLowTemp;
    }

    public String getForecastCondition()
    {
        return forecastCondition;
    }

    public void setForecastCondition(String forecastCondition)
    {
        this.forecastCondition = forecastCondition;
    }
}// end of class FiveDayForecast

