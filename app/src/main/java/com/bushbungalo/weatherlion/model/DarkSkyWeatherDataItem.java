package com.bushbungalo.weatherlion.model;

import java.util.List;

/**
 * @author Paul O. Patterson
 * <br />
 * <br />
 * <b style="margin-left:-40px">Date Created:</b>
 * <br />
 * 11/16/17
 */

@SuppressWarnings({"unused"})
public class DarkSkyWeatherDataItem
{
    private float latitude;
    private float longitude;
    private String timezone;
    private int offset;
    private Currently currently;
    private Minutely minutely;
    private Hourly hourly;
    private Daily daily;
    private List<Alert> alerts;
    private Flag flags;

    public float getLatitude()
    {
        return latitude;
    }

    public void setLatitude(float latitude)
    {
        this.latitude = latitude;
    }

    public float getLongitude()
    {
        return longitude;
    }

    public void setLongitude(float longitude)
    {
        this.longitude = longitude;
    }

    public String getTimezone()
    {
        return timezone;
    }

    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    }

    public int getOffset()
    {
        return offset;
    }

    public void setOffset(int offset)
    {
        this.offset = offset;
    }

    public Currently getCurrently()
    {
        return currently;
    }

    public void setCurrently(Currently currently)
    {
        this.currently = currently;
    }

    public Minutely getMinutely()
    {
        return minutely;
    }

    public void setMinutely(Minutely minutely)
    {
        this.minutely = minutely;
    }

    public Hourly getHourly()
    {
        return hourly;
    }

    public void setHourly(Hourly hourly)
    {
        this.hourly = hourly;
    }

    public Daily getDaily()
    {
        return daily;
    }

    public void setDaily(Daily daily)
    {
        this.daily = daily;
    }

    public List<Alert> getAlerts()
    {
        return alerts;
    }

    public void setAlerts(List<Alert> alerts)
    {
        this.alerts = alerts;
    }

    public Flag getFlags()
    {
        return flags;
    }

    public void setFlags(Flag flags)
    {
        this.flags = flags;
    }

    public class Currently
    {
        private long time;
        private String summary;
        private String icon;
        private int nearestStormDistance;
        private float precipIntensity;
        private float precipIntensityError;
        private float precipProbability;
        private String precipType;
        private float temperature;
        private float apparentTemperature;
        private float dewPoint;
        private float humidity;
        private float windSpeed;
        private float windGust;
        private float windBearing;
        private float visibility;
        private float cloudCover;
        private float pressure;
        private float ozone;
        private int uvIndex;

        public long getTime()
        {
            return time;
        }

        public void setTime(long time)
        {
            this.time = time;
        }

        public String getSummary()
        {
            return summary;
        }

        public void setSummary(String summary)
        {
            this.summary = summary;
        }

        public String getIcon()
        {
            return icon;
        }

        public void setIcon(String icon)
        {
            this.icon = icon;
        }

        public int getNearestStormDistance()
        {
            return nearestStormDistance;
        }

        public void setNearestStormDistance(int nearestStormDistance)
        {
            this.nearestStormDistance = nearestStormDistance;
        }

        public float getPrecipIntensity()
        {
            return precipIntensity;
        }

        public void setPrecipIntensity(float precipIntensity)
        {
            this.precipIntensity = precipIntensity;
        }

        public float getPrecipIntensityError()
        {
            return precipIntensityError;
        }

        public void setPrecipIntensityError(float precipIntensityError)
        {
            this.precipIntensityError = precipIntensityError;
        }

        public float getPrecipProbability()
        {
            return precipProbability;
        }

        public void setPrecipProbability(float precipProbability)
        {
            this.precipProbability = precipProbability;
        }

        public String getPrecipType()
        {
            return precipType;
        }

        public void setPrecipType(String precipType)
        {
            this.precipType = precipType;
        }

        public float getTemperature()
        {
            return temperature;
        }

        public void setTemperature(float temperature)
        {
            this.temperature = temperature;
        }

        public float getApparentTemperature()
        {
            return apparentTemperature;
        }

        public void setApparentTemperature(float apparentTemperature)
        {
            this.apparentTemperature = apparentTemperature;
        }

        public float getDewPoint()
        {
            return dewPoint;
        }

        public void setDewPoint(float dewPoint)
        {
            this.dewPoint = dewPoint;
        }

        public float getHumidity()
        {
            return humidity;
        }

        public void setHumidity(float humidity)
        {
            this.humidity = humidity;
        }

        public float getWindSpeed()
        {
            return windSpeed;
        }

        public void setWindSpeed(float windSpeed)
        {
            this.windSpeed = windSpeed;
        }

        public float getWindGust()
        {
            return windGust;
        }

        public void setWindGust(float windGust)
        {
            this.windGust = windGust;
        }

        public float getWindBearing()
        {
            return windBearing;
        }

        public void setWindBearing(float windBearing)
        {
            this.windBearing = windBearing;
        }

        public float getVisibility()
        {
            return visibility;
        }

        public void setVisibility(float visibility)
        {
            this.visibility = visibility;
        }

        public float getCloudCover()
        {
            return cloudCover;
        }

        public void setCloudCover(float cloudCover)
        {
            this.cloudCover = cloudCover;
        }

        public float getPressure()
        {
            return pressure;
        }

        public void setPressure(float pressure)
        {
            this.pressure = pressure;
        }

        public float getOzone()
        {
            return ozone;
        }

        public void setOzone(float ozone)
        {
            this.ozone = ozone;
        }

        public int getUvIndex()
        {
            return uvIndex;
        }

        public void setUvIndex(int uvIndex)
        {
            this.uvIndex = uvIndex;
        }
    }// end of class Currently

    public class Minutely
    {
        private String summary;
        private String icon;
        private List<Data> data;

        public String getSummary()
        {
            return summary;
        }

        public void setSummary(String summary)
        {
            this.summary = summary;
        }

        public String getIcon()
        {
            return icon;
        }

        public void setIcon(String icon)
        {
            this.icon = icon;
        }

        public List<Data> getData()
        {
            return data;
        }

        public void setData(List<Data> data)
        {
            this.data = data;
        }

        public class Data
        {
            private long time;
            private float precipIntensity;
            private float precipIntensityError;
            private String precipType;

            public long getTime()
            {
                return time;
            }

            public void setTime(long time)
            {
                this.time = time;
            }

            public float getPrecipIntensity()
            {
                return precipIntensity;
            }

            public void setPrecipIntensity(float precipIntensity)
            {
                this.precipIntensity = precipIntensity;
            }

            public float getPrecipIntensityError()
            {
                return precipIntensityError;
            }

            public void setPrecipIntensityError(float precipIntensityError)
            {
                this.precipIntensityError = precipIntensityError;
            }

            public String getPrecipType()
            {
                return precipType;
            }

            public void setPrecipType(String precipType)
            {
                this.precipType = precipType;
            }
        }// end of class Data
    }// end of class Minutely

    public class Hourly
    {
        private String summary;
        private String icon;
        private List<Data> data;

        public String getSummary()
        {
            return summary;
        }

        public void setSummary(String summary)
        {
            this.summary = summary;
        }

        public String getIcon()
        {
            return icon;
        }

        public void setIcon(String icon)
        {
            this.icon = icon;
        }

        public List<Data> getData()
        {
            return data;
        }

        public void setData(List<Data> data)
        {
            this.data = data;
        }

        public class Data
        {
            private String time;
            private String summary;
            private String icon;
            private float precipIntensity;
            private float precipProbability;
            private String precipType;
            private float temperature;
            private float apparentTemperature;
            private float dewPoint;
            private float humidity;
            private float windSpeed;
            private float windGust;
            private int windBearing;
            private float visibility;
            private float cloudCover;
            private float pressure;
            private float ozone;
            private int uvIndex;

            public String getTime()
            {
                return time;
            }

            public void setTime(String time)
            {
                this.time = time;
            }

            public String getSummary()
            {
                return summary;
            }

            public void setSummary(String summary)
            {
                this.summary = summary;
            }

            public String getIcon()
            {
                return icon;
            }

            public void setIcon(String icon)
            {
                this.icon = icon;
            }

            public float getPrecipIntensity()
            {
                return precipIntensity;
            }

            public void setPrecipIntensity(float precipIntensity)
            {
                this.precipIntensity = precipIntensity;
            }

            public float getPrecipProbability()
            {
                return precipProbability;
            }

            public void setPrecipProbability(float precipProbability)
            {
                this.precipProbability = precipProbability;
            }

            public String getPrecipType()
            {
                return precipType;
            }

            public void setPrecipType(String precipType)
            {
                this.precipType = precipType;
            }

            public float getTemperature()
            {
                return temperature;
            }

            public void setTemperature(float temperature)
            {
                this.temperature = temperature;
            }

            public float getApparentTemperature()
            {
                return apparentTemperature;
            }

            public void setApparentTemperature(float apparentTemperature)
            {
                this.apparentTemperature = apparentTemperature;
            }

            public float getDewPoint()
            {
                return dewPoint;
            }

            public void setDewPoint(float dewPoint)
            {
                this.dewPoint = dewPoint;
            }

            public float getHumidity()
            {
                return humidity;
            }

            public void setHumidity(float humidity)
            {
                this.humidity = humidity;
            }

            public float getWindSpeed()
            {
                return windSpeed;
            }

            public void setWindSpeed(float windSpeed)
            {
                this.windSpeed = windSpeed;
            }

            public float getWindGust()
            {
                return windGust;
            }

            public void setWindGust(float windGust)
            {
                this.windGust = windGust;
            }

            public int getWindBearing()
            {
                return windBearing;
            }

            public void setWindBearing(int windBearing)
            {
                this.windBearing = windBearing;
            }

            public float getVisibility()
            {
                return visibility;
            }

            public void setVisibility(float visibility)
            {
                this.visibility = visibility;
            }

            public float getCloudCover()
            {
                return cloudCover;
            }

            public void setCloudCover(float cloudCover)
            {
                this.cloudCover = cloudCover;
            }

            public float getPressure()
            {
                return pressure;
            }

            public void setPressure(float pressure)
            {
                this.pressure = pressure;
            }

            public float getOzone()
            {
                return ozone;
            }

            public void setOzone(float ozone)
            {
                this.ozone = ozone;
            }

            public int getUvIndex()
            {
                return uvIndex;
            }

            public void setUvIndex(int uvIndex) {
                this.uvIndex = uvIndex;

            }
        }// end of class Data
    }// end of class Hourly

    public class Daily
    {
        private String summary;
        private String icon;
        private List<Data> data;

        public String getSummary()
        {
            return summary;
        }

        public void setSummary(String summary)
        {
            this.summary = summary;
        }

        public String getIcon()
        {
            return icon;
        }

        public void setIcon(String icon)
        {
            this.icon = icon;
        }

        public List<Data> getData()
        {
            return data;
        }

        public void setData(List<Data> data)
        {
            this.data = data;
        }

        public class Data
        {
            private long time;
            private String summary;
            private String icon;
            private long sunriseTime;
            private long sunsetTime;
            private float moonPhase;
            private float precipIntensity;
            private float precipIntensityMax;
            private long precipIntensityMaxTime;
            private float precipProbability;
            private String precipType;
            private float temperatureMin;
            private long temperatureMinTime;
            private float temperatureMax;
            private long temperatureMaxTime;
            private float apparentTemperatureMin;
            private long apparentTemperatureMinTime;
            private float apparentTemperatureMax;
            private long apparentTemperatureMaxTime;
            private float dewPoint;
            private float humidity;
            private float windSpeed;
            private int windBearing;
            private float visibility;
            private float cloudCover;
            private float pressure;
            private float ozone;
            private int uvIndex;
            private long uvIndexTime;

            public long getTime()
            {
                return time;
            }

            public void setTime(long time)
            {
                this.time = time;
            }

            public String getSummary()
            {
                return summary;
            }

            public void setSummary(String summary)
            {
                this.summary = summary;
            }

            public String getIcon()
            {
                return icon;
            }

            public void setIcon(String icon)
            {
                this.icon = icon;
            }

            public long getSunriseTime()
            {
                return sunriseTime;
            }

            public void setSunriseTime(long sunriseTime)
            {
                this.sunriseTime = sunriseTime;
            }

            public long getSunsetTime()
            {
                return sunsetTime;
            }

            public void setSunsetTime(long sunsetTime)
            {
                this.sunsetTime = sunsetTime;
            }

            public float getMoonPhase()
            {
                return moonPhase;
            }

            public void setMoonPhase(float moonPhase)
            {
                this.moonPhase = moonPhase;
            }

            public float getPrecipIntensity()
            {
                return precipIntensity;
            }

            public void setPrecipIntensity(float precipIntensity)
            {
                this.precipIntensity = precipIntensity;
            }

            public float getPrecipIntensityMax()
            {
                return precipIntensityMax;
            }

            public void setPrecipIntensityMax(float precipIntensityMax)
            {
                this.precipIntensityMax = precipIntensityMax;
            }

            public long getPrecipIntensityMaxTime()
            {
                return precipIntensityMaxTime;
            }

            public void setPrecipIntensityMaxTime(long precipIntensityMaxTime)
            {
                this.precipIntensityMaxTime = precipIntensityMaxTime;
            }

            public float getPrecipProbability()
            {
                return precipProbability;
            }

            public void setPrecipProbability(float precipProbability)
            {
                this.precipProbability = precipProbability;
            }

            public String getPrecipType()
            {
                return precipType;
            }

            public void setPrecipType(String precipType)
            {
                this.precipType = precipType;
            }

            public float getTemperatureMin()
            {
                return temperatureMin;
            }

            public void setTemperatureMin(float temperatureMin)
            {
                this.temperatureMin = temperatureMin;
            }

            public long getTemperatureMinTime()
            {
                return temperatureMinTime;
            }

            public void setTemperatureMinTime(long temperatureMinTime)
            {
                this.temperatureMinTime = temperatureMinTime;
            }

            public float getTemperatureMax()
            {
                return temperatureMax;
            }

            public void setTemperatureMax(float temperatureMax)
            {
                this.temperatureMax = temperatureMax;
            }

            public long getTemperatureMaxTime()
            {
                return temperatureMaxTime;
            }

            public void setTemperatureMaxTime(long temperatureMaxTime)
            {
                this.temperatureMaxTime = temperatureMaxTime;
            }

            public float getApparentTemperatureMin()
            {
                return apparentTemperatureMin;
            }

            public void setApparentTemperatureMin(float apparentTemperatureMin)
            {
                this.apparentTemperatureMin = apparentTemperatureMin;
            }

            public long getApparentTemperatureMinTime()
            {
                return apparentTemperatureMinTime;
            }

            public void setApparentTemperatureMinTime(long apparentTemperatureMinTime)
            {
                this.apparentTemperatureMinTime = apparentTemperatureMinTime;
            }

            public float getApparentTemperatureMax()
            {
                return apparentTemperatureMax;
            }

            public void setApparentTemperatureMax(float apparentTemperatureMax)
            {
                this.apparentTemperatureMax = apparentTemperatureMax;
            }

            public long getApparentTemperatureMaxTime()
            {
                return apparentTemperatureMaxTime;
            }

            public void setApparentTemperatureMaxTime(long apparentTemperatureMaxTime)
            {
                this.apparentTemperatureMaxTime = apparentTemperatureMaxTime;
            }

            public float getDewPoint()
            {
                return dewPoint;
            }

            public void setDewPoint(float dewPoint)
            {
                this.dewPoint = dewPoint;
            }

            public float getHumidity()
            {
                return humidity;
            }

            public void setHumidity(float humidity)
            {
                this.humidity = humidity;
            }

            public float getWindSpeed()
            {
                return windSpeed;
            }

            public void setWindSpeed(float windSpeed)
            {
                this.windSpeed = windSpeed;
            }

            public int getWindBearing()
            {
                return windBearing;
            }

            public void setWindBearing(int windBearing)
            {
                this.windBearing = windBearing;
            }

            public float getVisibility()
            {
                return visibility;
            }

            public void setVisibility(float visibility)
            {
                this.visibility = visibility;
            }

            public float getCloudCover()
            {
                return cloudCover;
            }

            public void setCloudCover(float cloudCover)
            {
                this.cloudCover = cloudCover;
            }

            public float getPressure()
            {
                return pressure;
            }

            public void setPressure(float pressure)
            {
                this.pressure = pressure;
            }

            public float getOzone()
            {
                return ozone;
            }

            public void setOzone(float ozone)
            {
                this.ozone = ozone;
            }

            public int getUvIndex()
            {
                return uvIndex;
            }

            public void setUvIndex(int uvIndex)
            {
                this.uvIndex = uvIndex;
            }

            public long getUvIndexTime()
            {
                return uvIndexTime;
            }

            public void setUvIndexTime(long uvIndexTime)
            {
                this.uvIndexTime = uvIndexTime;
            }
        }// end of class Data
    }// end of class Daily

    public class Alert
    {
        private String title;
        private long time;
        private long expires;
        private String description;
        private String uri;

        public String getTitle()
        {
            return title;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        public long getTime()
        {
            return time;
        }


        public void setTime(long time)
        {
            this.time = time;
        }

        public long getExpires()
        {
            return expires;
        }

        public void setExpires(long expires)
        {
            this.expires = expires;
        }

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public String getUri()
        {
            return uri;
        }

        public void setUri(String uri)
        {
            this.uri = uri;
        }
    }// end of class Alert

    public class Flag
    {
        private List<String> sources;
        private List<String> isd_stations;
        private String units;

        public List<String> getSources()
        {
            return sources;
        }

        public void setSources(List<String> sources)
        {
            this.sources = sources;
        }

        public List<String> getIsd_stations()
        {
            return isd_stations;
        }

        public void setIsd_stations(List<String> isd_stations)
        {
            this.isd_stations = isd_stations;
        }

        public String getUnits()
        {
            return units;
        }

        public void setUnits(String units)
        {
            this.units = units;
        }
    }// end of class Flag
}// end of class DarkSkyWeatherDataItem
