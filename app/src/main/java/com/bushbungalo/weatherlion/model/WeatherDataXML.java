package com.bushbungalo.weatherlion.model;

import java.util.Date;
import java.util.List;

/**
 * @author Paul O. Patterson
 * <br />
 * <b style="margin-left:-40px">Date Created:</b>
 * <br />
 * 09/27/19
 */

@SuppressWarnings({"unused"})
public class WeatherDataXML
{
    private static final String TAG = "WeatherDataXML";

    private String providerName;
    private Date datePublished;
    private String cityName;
    private String countryName;
    private String currentConditions;
    private String currentTemperature;
    private String currentFeelsLikeTemperature;
    private String currentHigh;
    private String currentLow;
    private String currentWindSpeed;
    private String currentWindDirection;
    private String currentHumidity;
    private String sunriseTime;
    private String sunsetTime;
    private List<FiveDayForecast> fiveDayForecast;

    public WeatherDataXML(){}// end of default constructor

    public WeatherDataXML( String providerName, Date datePublished,
                          String cityName, String countryName, String currentConditions,
                          String currentTemperature, String feelsLikeTemperature, String currentHigh,
                          String currentLow, String currentWindSpeed, String currentWindDirection,
                          String currentHumidity, String sunriseTime, String sunsetTime,
                          List<FiveDayForecast> fiveDayForecast )
    {
        this.providerName = providerName;
        this.datePublished = datePublished;
        this.cityName = cityName;
        this.countryName = countryName;
        this.currentConditions = currentConditions;
        this.currentTemperature = currentTemperature;
        this.currentFeelsLikeTemperature = feelsLikeTemperature;
        this.currentHigh = currentHigh;
        this.currentLow = currentLow;
        this.currentWindSpeed = currentWindSpeed;
        this.currentWindDirection = currentWindDirection;
        this.currentHumidity = currentHumidity;
        this.sunriseTime = sunriseTime;
        this.sunsetTime = sunsetTime;
        this.fiveDayForecast = fiveDayForecast;
    }// end of fifteen-argument constructor

    public String getProviderName()
    {
        return providerName;
    }

    public void setProviderName( String providerName )
    {
        this.providerName = providerName;
    }

    public Date getDatePublished()
    {
        return datePublished;
    }

    public void setDatePublished( Date datePublished )
    {
        this.datePublished = datePublished;
    }

    public String getCityName()
    {
        return cityName;
    }

    public void setCityName( String cityName )
    {
        this.cityName = cityName;
    }

    public String getCountryName()
    {
        return countryName;
    }

    public void setCountryName( String countryName )
    {
        this.countryName = countryName;
    }

    public String getCurrentConditions()
    {
        return currentConditions;
    }

    public void setCurrentConditions( String currentConditions )
    {
        this.currentConditions = currentConditions;
    }

    public String getCurrentTemperature()
    {
        return currentTemperature;
    }

    public void setCurrentTemperature( String currentTemperature )
    {
        this.currentTemperature = currentTemperature;
    }

    public String getCurrentFeelsLikeTemperature()
    {
        return this.currentFeelsLikeTemperature;
    }

    public void setCurrentFeelsLikeTemperature( String currentFeelsLikeTemperature )
    {
        this.currentFeelsLikeTemperature = currentFeelsLikeTemperature;
    }

    public String getCurrentHigh()
    {
        return currentHigh;
    }

    public void setCurrentHigh( String currentHigh )
    {
        this.currentHigh = currentHigh;
    }

    public String getCurrentLow()
    {
        return currentLow;
    }

    public void setCurrentLow( String currentLow )
    {
        this.currentLow = currentLow;
    }

    public String getCurrentWindSpeed()
    {
        return currentWindSpeed;
    }

    public void setCurrentWindSpeed( String currentWindSpeed )
    {
        this.currentWindSpeed = currentWindSpeed;
    }

    public String getCurrentWindDirection()
    {
        return currentWindDirection;
    }

    public void setCurrentWindDirection( String currentWindDirection )
    {
        this.currentWindDirection = currentWindDirection;
    }

    public String getCurrentHumidity()
    {
        return currentHumidity;
    }

    public void setCurrentHumidity( String currentHumidity )
    {
        this.currentHumidity = currentHumidity;
    }

    public String getSunriseTime()
    {
        return sunriseTime;
    }

    public void setSunriseTime( String sunriseTime )
    {
        this.sunriseTime = sunriseTime;
    }

    public String getSunsetTime()
    {
        return sunsetTime;
    }

    public void setSunsetTime( String sunsetTime )
    {
        this.sunsetTime = sunsetTime;
    }

    public List<FiveDayForecast> getFiveDayForecast()
    {
        return fiveDayForecast;
    }

    public void setFiveDayForecast( List<FiveDayForecast> fiveDayForecast )
    {
        this.fiveDayForecast = fiveDayForecast;
    }
}// end of class WeatherDataXML
