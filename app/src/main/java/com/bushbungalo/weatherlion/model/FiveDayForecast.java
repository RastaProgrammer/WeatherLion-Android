package com.bushbungalo.weatherlion.model;

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

    private float dewPoint;
    private float humidity;
    private float pressure;
    private float windBearing;
    private float windSpeed;
    private float uvIndex;
    private float visibility;
    private float ozone;
    private String windDirection;
    private Date sunrise;
    private Date sunset;

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

    public FiveDayForecast( Date forecastDate, String forecastHighTemp,
                            String forecastLowTemp, String forecastCondition,
                            float dewPoint, float humidity, float pressure,
                            float windBearing, float windSpeed, String windDirection,
                            float uvIndex, float visibility, float ozone,
                            Date sunrise, Date sunset )
    {
        this.forecastDate = forecastDate;
        this.forecastHighTemp = forecastHighTemp;
        this.forecastLowTemp = forecastLowTemp;
        this.forecastCondition = forecastCondition;
        this.dewPoint = dewPoint;
        this.humidity = humidity;
        this.pressure = pressure;
        this.windBearing = windBearing;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.uvIndex = uvIndex;
        this.visibility = visibility;
        this.ozone = ozone;
        this.sunrise = sunrise;
        this.sunset = sunset;
    }// end of fifteen-argument constructor

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

    public float getDewPoint() {
        return dewPoint;
    }

    public void setDewPoint(float dewPoint) {
        this.dewPoint = dewPoint;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    public float getWindBearing() {
        return windBearing;
    }

    public void setWindBearing(float windBearing) {
        this.windBearing = windBearing;
    }

    public float getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(float windSpeed) {
        this.windSpeed = windSpeed;
    }

    public float getUvIndex() {
        return uvIndex;
    }

    public void setUvIndex(float uvIndex) {
        this.uvIndex = uvIndex;
    }

    public float getVisibility() {
        return visibility;
    }

    public void setVisibility(float visibility) {
        this.visibility = visibility;
    }

    public float getOzone() {
        return ozone;
    }

    public void setOzone(float ozone) {
        this.ozone = ozone;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(String windDirection) {
        this.windDirection = windDirection;
    }

    public Date getSunrise() {
        return sunrise;
    }

    public void setSunrise(Date sunrise) {
        this.sunrise = sunrise;
    }

    public Date getSunset() {
        return sunset;
    }

    public void setSunset(Date sunset) {
        this.sunset = sunset;
    }
}// end of class FiveDayForecast

