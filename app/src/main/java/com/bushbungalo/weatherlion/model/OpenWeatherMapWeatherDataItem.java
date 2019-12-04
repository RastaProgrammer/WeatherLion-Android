package com.bushbungalo.weatherlion.model;

import com.google.gson.Gson;

import java.util.List;

/**
 * @author Paul O. Patterson
 * <br />
 * <b style="margin-left:-40px">Date Created:</b>
 * <br />
 * 11/20/17
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class OpenWeatherMapWeatherDataItem 
{
	static OpenWeatherMapWeatherDataItem.WeatherData owmWxWeatherData;
	static OpenWeatherMapWeatherDataItem.ForecastData owmFxWeatherData;
	
	private OpenWeatherMapWeatherDataItem()
	{
	}

    public class WeatherData
    {
    	private Coordinate coord;
    	private List< Weather > weather;
    	private String _base;
    	private Main main;
    	private Wind wind;
    	private Cloud clouds;
    	private long dt;
    	private System sys;
    	private long id;
    	private String name;
    	private int cod;

        /**
		 * @return the coord
		 */
		public Coordinate getCoord()
		{
			return coord;
		}

		/**
		 * @param coord the coord to set
		 */
		public void setCoord(Coordinate coord) 
		{
			this.coord = coord;
		}

		/**
		 * @return the weather
		 */
		public List<Weather> getWeather()
		{
			return weather;
		}

		/**
		 * @param weather the weather to set
		 */
		public void setWeather(List<Weather> weather)
		{
			this.weather = weather;
		}

		/**
		 * @return the _base
		 */
		public String get_base() 
		{
			return _base;
		}

		/**
		 * @param _base the _base to set
		 */
		public void set_base(String _base)
		{
			this._base = _base;
		}

		/**
		 * @return the main
		 */
		public Main getMain() 
		{
			return main;
		}

		/**
		 * @param main the main to set
		 */
		public void setMain(Main main)
		{
			this.main = main;
		}

		/**
		 * @return the wind
		 */
		public Wind getWind() 
		{
			return wind;
		}

		/**
		 * @param wind the wind to set
		 */
		public void setWind(Wind wind) 
		{
			this.wind = wind;
		}

		/**
		 * @return the clouds
		 */
		public Cloud getClouds()
		{
			return clouds;
		}

		/**
		 * @param clouds the clouds to set
		 */
		public void setClouds(Cloud clouds)
		{
			this.clouds = clouds;
		}

		/**
		 * @return the dt
		 */
		public long getDt() 
		{
			return dt;
		}

		/**
		 * @param dt the dt to set
		 */
		public void setDt(long dt) 
		{
			this.dt = dt;
		}

		/**
		 * @return the sys
		 */
		public System getSys()
		{
			return sys;
		}

		/**
		 * @param sys the sys to set
		 */
		public void setSys(System sys)
		{
			this.sys = sys;
		}

		/**
		 * @return the id
		 */
		public long getId() 
		{
			return id;
		}

		/**
		 * @param id the id to set
		 */
		public void setId(long id)
		{
			this.id = id;
		}

		/**
		 * @return the name
		 */
		public String getName()
		{
			return name;
		}

		/**
		 * @param name the name to set
		 */
		public void setName(String name)
		{
			this.name = name;
		}

		/**
		 * @return the cod
		 */
		public int getCod() 
		{
			return cod;
		}

		/**
		 * @param cod the cod to set
		 */
		public void setCod(int cod)
		{
			this.cod = cod;
		}

		private class Coordinate
        {
            private float lon;
            private float lat;
            
			/**
			 * @return the lon
			 */
			public float getLon() 
			{
				return lon;
			}
			/**
			 * @param lon the lon to set
			 */
			public void setLon(float lon)
			{
				this.lon = lon;
			}
			/**
			 * @return the lat
			 */
			public float getLat()
			{
				return lat;
			}
			/**
			 * @param lat the lat to set
			 */
			public void setLat(float lat)
			{
				this.lat = lat;
			}
        }// end of class 

        public class Weather
        {
            private int id;
            private String main;
            private String description;
            private String icon;
            
			/**
			 * @return the id
			 */
			public int getId()
			{
				return id;
			}
			/**
			 * @param id the id to set
			 */
			public void setId(int id)
			{
				this.id = id;
			}
			/**
			 * @return the main
			 */
			public String getMain() 
			{
				return main;
			}
			/**
			 * @param main the main to set
			 */
			public void setMain(String main)
			{
				this.main = main;
			}
			/**
			 * @return the description
			 */
			public String getDescription()
			{
				return description;
			}
			/**
			 * @param description the description to set
			 */
			public void setDescription(String description)
			{
				this.description = description;
			}
			/**
			 * @return the icon
			 */
			public String getIcon() 
			{
				return icon;
			}
			/**
			 * @param icon the icon to set
			 */
			public void setIcon(String icon)
			{
				this.icon = icon;
			}
        }// end of class       

        public class Main
        {
            private float temp;
            private float pressure;
            private float humidity;
            private float temp_min;
            private float temp_max;
            private float sea_level;
            private float grnd_level;
            
			public float getTemp()
			{
				return temp;
			}
			
			public void setTemp(float temp)
			{
				this.temp = temp;
			}
			
			public float getPressure() 
			{
				return pressure;
			}
			
			public void setPressure(float pressure)
			{
				this.pressure = pressure;
			}
			
			public float getHumidity()
			{
				return humidity;
			}
			
			public void setHumidity(float humidity) 
			{
				this.humidity = humidity;
			}
			
			public float getTemp_min()
			{
				return temp_min;
			}
			
			public void setTemp_min(float temp_min)
			{
				this.temp_min = temp_min;
			}
			
			public float getTemp_max()
			{
				return temp_max;
			}
			
			public void setTemp_max(float temp_max)
			{
				this.temp_max = temp_max;
			}
			
			public float getSea_level()
			{
				return sea_level;
			}
			public void setSea_level(float sea_level)
			{
				this.sea_level = sea_level;
			}
			
			public float getGrnd_level() 
			{
				return grnd_level;
			}
			
			public void setGrnd_level(float grnd_level)
			{
				this.grnd_level = grnd_level;
			}
        }// end of class

        public class Wind
        {
            private float speed;
            private float deg;
            
			public float getSpeed()
			{
				return speed;
			}
			
			public void setSpeed(float speed) 
			{
				this.speed = speed;
			}
			
			public float getDeg() 
			{
				return deg;
			}
			
			public void setDeg(float deg) 
			{
				this.deg = deg;
			}
        }// end of class

        private class Cloud
        {
            private int all;

			public int getAll()
			{
				return all;
			}

			public void setAll(int all) 
			{
				this.all = all;
			}
        }// end of class

        public class System
        {
            private float message;
            private String country;
            private long sunrise;
            private long sunset;
            
			public float getMessage()
			{
				return message;
			}
			
			public void setMessage(float message)
			{
				this.message = message;
			}
			
			public String getCountry()
			{
				return country;
			}
			
			public void setCountry(String country)
			{
				this.country = country;
			}
			
			public long getSunrise()
			{
				return sunrise;
			}
			
			public void setSunrise(long sunrise)
			{
				this.sunrise = sunrise;
			}
			
			public long getSunset()
			{
				return sunset;
			}
			
			public void setSunset(long sunset)
			{
				this.sunset = sunset;
			}
        }// end of class

    }// end of inner class WeatherData

    public class ForecastData
    {
        private City city;
        private int cod;
        private String message;
        private int cnt;
        private List<Data> list;

        public City getCity() 
        {
			return city;
		}

		public void setCity(City city)
		{
			this.city = city;
		}

		public int getCod()
		{
			return cod;
		}

		public void setCod(int cod)
		{
			this.cod = cod;
		}

		public String getMessage()
		{
			return message;
		}

		public void setMessage(String message) 
		{
			this.message = message;
		}

		public int getCnt() 
		{
			return cnt;
		}

		public void setCnt(int cnt) 
		{
			this.cnt = cnt;
		}

		public List<Data> getList() 
		{
			return list;
		}

		public void setList(List<Data> list) 
		{
			this.list = list;
		}

		private class City
        {
            private long id;
            private String name;
            private Coordinate coord;
            private String country;
            private String population;

            public long getId() 
            {
				return id;
			}

			public void setId(long id) 
			{
				this.id = id;
			}

			public String getName() 
			{
				return name;
			}

			public void setName(String name) 
			{
				this.name = name;
			}

			public Coordinate getCoord() 
			{
				return coord;
			}

			public void setCoord(Coordinate coord)
			{
				this.coord = coord;
			}

			public String getCountry() 
			{
				return country;
			}

			public void setCountry(String country) 
			{
				this.country = country;
			}

			public String getPopulation()
			{
				return population;
			}

			public void setPopulation(String population) 
			{
				this.population = population;
			}

			private class Coordinate
            {
                private float lon;
                private float lat;
            }// end of class 
        }// end of class City

        public class Data
        {
            private long dt;
			private long sunrise;
            private long sunset;
			private Temperature temp;
			private List<Weather> weather;
			private float pressure;
            private float humidity;
            private float speed;
            private int deg;
            private int clouds;
            private float rain;

            public long getDt()
            {
				return dt;
			}

			public void setDt(long dt)
			{
				this.dt = dt;
			}

			public long getSunrise()
			{
				return sunrise;
			}

			public void setSunrise(long sunrise)
			{
				this.sunrise = sunrise;
			}

			public long getSunset() {
				return sunset;
			}

			public void setSunset(long sunset) {
				this.sunset = sunset;
			}

			public Temperature getTemp() 
			{
				return temp;
			}

			public void setTemp(Temperature temp)
			{
				this.temp = temp;
			}

			public List<Weather> getWeather() 
			{
				return weather;
			}

			public void setWeather(List<Weather> weather)
			{
				this.weather = weather;
			}

			public float getPressure()
			{
				return pressure;
			}

			public void setPressure( float pressure )
			{
				this.pressure = pressure;
			}

			public float getHumidity()
			{
				return humidity;
			}

			public void setHumidity( float humidity )
			{
				this.humidity = humidity;
			}

			public float getSpeed() 
			{
				return speed;
			}

			public void setSpeed(float speed) 
			{
				this.speed = speed;
			}

			public int getDeg() 
			{
				return deg;
			}

			public void setDeg(int deg) 
			{
				this.deg = deg;
			}

			public int getClouds() 
			{
				return clouds;
			}

			public void setClouds(int clouds) 
			{
				this.clouds = clouds;
			}

			public float getRain() 
			{
				return rain;
			}

			public void setRain(float rain)
			{
				this.rain = rain;
			}

			public class Temperature
            {
                private float day;
                private float min;
                private float max;
                private float night;
                private float eve;
                private float morn;
                
				public float getDay() 
				{
					return day;
				}
				
				public void setDay(float day)
				{
					this.day = day;
				}
				
				public float getMin() 
				{
					return min;
				}
				
				public void setMin(float min)
				{
					this.min = min;
				}
				
				public float getMax()
				{
					return max;
				}
				
				public void setMax(float max)
				{
					this.max = max;
				}
				
				public float getNight()
				{
					return night;
				}
				
				public void setNight(float night)
				{
					this.night = night;
				}
				
				public float getEve() 
				{
					return eve;
				}
				
				public void setEve(float eve)
				{
					this.eve = eve;
				}
				
				public float getMorn()
				{
					return morn;
				}
				
				public void setMorn(float morn)
				{
					this.morn = morn;
				}
            }// end of class Temperature

            public class Weather
            {
                private int id;
                private String main;
                private String description;
                private String icon;
                
				public int getId()
				{
					return id;
				}
				
				public void setId(int id)
				{
					this.id = id;
				}
				
				public String getMain() 
				{
					return main;
				}
				
				public void setMain(String main)
				{
					this.main = main;
				}
				
				public String getDescription() 
				{
					return description;
				}
				
				public void setDescription(String description)
				{
					this.description = description;
				}
				
				public String getIcon()
				{
					return icon;
				}
				
				public void setIcon(String icon)
				{
					this.icon = icon;
				}
            }// end of class                
        }// end of class Data
    }// end of inner class ForecastData

    public static boolean DeserializeOpenWeatherMapWxJSON( String strJSON )
    {
    	Gson gson = new Gson();
        owmWxWeatherData = gson.fromJson( strJSON, OpenWeatherMapWeatherDataItem.WeatherData.class );

		return owmWxWeatherData != null;
    }// end of method DeserializeOpenWeatherMapWxJSON

    public static boolean DeserializeOpenWeatherMapFxJSON( String strJSON )
    {
    	Gson gson = new Gson();
    	owmFxWeatherData = gson.fromJson(strJSON, OpenWeatherMapWeatherDataItem.ForecastData.class );

		return owmFxWeatherData != null;
    }// end of method DeserializeOpenWeatherMapFxJSON
}// end of class OpenWeatherMapWeatherDataItem
