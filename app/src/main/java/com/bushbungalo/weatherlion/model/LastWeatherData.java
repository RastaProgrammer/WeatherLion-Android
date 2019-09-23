package com.bushbungalo.weatherlion.model;

import java.util.List;

@SuppressWarnings("unused")
public class LastWeatherData
{
    private WeatherData weatherData;

    public LastWeatherData(){}// default constructor

    public WeatherData getWeatherData()
    {
        return weatherData;
    }

    public void setWeatherData( WeatherData weatherData )
    {
        this.weatherData = weatherData;
    }

    public class WeatherData
    {
        private Provider provider;
        private Location location;
        private Atmosphere atmosphere;
        private Wind wind;
        private Astronomy astronomy;
        private Current current;
        private List<DailyForecast.DayForecast> dailyForecast;

        public Wind getWind ()
        {
            return wind;
        }

        public void setWind ( Wind wind )
        {
            this.wind = wind;
        }

        public Atmosphere getAtmosphere ()
        {
            return atmosphere;
        }

        public void setAtmosphere ( Atmosphere atmosphere )
        {
            this.atmosphere = atmosphere;
        }

        public List<DailyForecast.DayForecast> getDailyForecast()
        {
            return dailyForecast;
        }

        public void setDailyForecast( List<DailyForecast.DayForecast> dailyForecast )
        {
            this.dailyForecast = dailyForecast;
        }

        public Current getCurrent ()
        {
            return current;
        }

        public void setCurrent ( Current current )
        {
            this.current = current;
        }

        public Astronomy getAstronomy ()
        {
            return astronomy;
        }

        public void setAstronomy ( Astronomy astronomy )
        {
            this.astronomy = astronomy;
        }

        public Provider getProvider ()
        {
            return provider;
        }

        public void setProvider ( Provider provider )
        {
            this.provider = provider;
        }

        public Location getLocation ()
        {
            return location;
        }

        public void setLocation ( Location location )
        {
            this.location = location;
        }

        public class Provider
        {
            private String name;
            private String date;

            public String getName()
            {
                return this.name;
            }

            public void setName( String name )
            {
                this.name = name;
            }

            public String getDate()
            {
                return this.date;
            }

            public void setDate( String date )
            {
                this.date = date;
            }
        }// end of class Provider

        public class Location
        {
            private String city;
            private String country;

            public String getCity()
            {
                return this.city;
            }

            public void setCity( String city )
            {
                this.city = city;
            }

            public String getCountry()
            {
                return  this.country;
            }

            public void setCountry( String country )
            {
                this.country = country;
            }
        }// end of class Location

        public class Atmosphere
        {
            private int humidity;

            public int getHumidity()
            {
                return this.humidity;
            }

            public void setHumidity( int humidity )
            {
                this.humidity = humidity;
            }
        }// end of class Atmosphere

        public class Wind
        {
            private float windSpeed;
            private String windDirection;

            public float getWindSpeed()
            {
                return windSpeed;
            }

            public void setWindSpeed( float windSpeed )
            {
                this.windSpeed = windSpeed;
            }

            public String getWindDirection()
            {
                return windDirection;
            }

            public void setWindDirection( String windDirection )
            {
                this.windDirection = windDirection;
            }
        }// end of class Wind

        public class Astronomy
        {
            private String sunrise;
            private String sunset;

            public String getSunrise()
            {
                return sunrise;
            }

            public void setSunrise( String sunrise )
            {
                this.sunrise = sunrise;
            }

            public String getSunset()
            {
                return sunset;
            }

            public void setSunset( String sunset )
            {
                this.sunset = sunset;
            }
        }// end of class Astronomy

        public class Current
        {
            private String condition;
            private int temperature;
            private int feelsLike;
            private int highTemperature;
            private int lowTemperature;

            public String getCondition()
            {
                return condition;
            }

            public void setCondition( String condition )
            {
                this.condition = condition;
            }

            public int getTemperature()
            {
                return temperature;
            }

            public void setTemperature( int temperature )
            {
                this.temperature = temperature;
            }

            public int getFeelsLike()
            {
                return feelsLike;
            }

            public void setFeelsLike( int feelsLike )
            {
                this.feelsLike = feelsLike;
            }

            public int getHighTemperature()
            {
                return highTemperature;
            }

            public void setHighTemperature( int highTemperature )
            {
                this.highTemperature = highTemperature;
            }

            public int getLowTemperature()
            {
                return lowTemperature;
            }

            public void setLowTemperature( int lowTemperature )
            {
                this.lowTemperature = lowTemperature;
            }
        }// end of class Current

        public class DailyForecast
        {
            private DayForecast dayForecast;

            public DayForecast getDayForecast()
            {
                return dayForecast;
            }

            public void setDayForecast( DayForecast dayForecast )
            {
                this.dayForecast = dayForecast;
            }

            public List< DayForecast > getDailyForecast()
            {
                return dailyForecast;
            }

            public class DayForecast
            {
                private String date;
                private String condition;
                private int highTemperature;
                private int lowTemperature;

                public String getDate()
                {
                    return this.date;
                }

                public void setDate( String date )
                {
                    this.date = date;
                }

                public String getCondition()
                {
                    return condition;
                }

                public void setCondition( String condition )
                {
                    this.condition = condition;
                }

                public int getHighTemperature()
                {
                    return highTemperature;
                }

                public void setHighTemperature( int highTemperature )
                {
                    this.highTemperature = highTemperature;
                }

                public int getLowTemperature()
                {
                    return lowTemperature;
                }

                public void setLowTemperature( int lowTemperature )
                {
                    this.lowTemperature = lowTemperature;
                }
            }// end of class DayForecast
        }// end of class DailyForecast
    }// end of class WeatherData
}// end of class LastWeatherData