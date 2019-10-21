package com.bushbungalo.weatherlion.model;

import java.util.List;

import com.google.gson.Gson;

/**
 * @author Paul O. Patterson
 * <br />
 * <b style="margin-left:-40px">Date Created:</b>
 * <br />
 * 03/21/19
 * <br />
 */
@SuppressWarnings({"unused"})
public class HereMapsWeatherDataItem
{
	static HereMapsWeatherDataItem.ForecastData hereFxWeatherData;
    static HereMapsWeatherDataItem.WeatherData hereWxWeatherData;
    static HereMapsWeatherDataItem.AstronomyData hereAxData;
    
	public class WeatherData
	{
		private Observations observations;
		
		public Observations getObservations()
		{
			return observations;
		}

		public void setObservations(Observations observations)
		{
			this.observations = observations;
		}

		public class Observations
		{
			private List< Location > location; 
			
			public List<Location> getLocation()
			{
				return location;
			}

			public void setLocation( List<Location> location )
			{
				this.location = location;
			}

			public class Location
			{
				private List< Observation > observation; 
				private String country;
				private String state;
				private String city;
				private Float latitude;
				private Float longitude;
				private Float distance;
				private int timezone;
				
				public List<Observation> getObservation()
				{
					return observation;
				}

				public void setObservation(List<Observation> observation)
				{
					this.observation = observation;
				}

				public String getCountry()
				{
					return country;
				}

				public void setCountry(String country)
				{
					this.country = country;
				}

				public String getState()
				{
					return state;
				}

				public void setState(String state)
				{
					this.state = state;
				}

				public String getCity() 
				{
					return city;
				}

				public void setCity(String city)
				{
					this.city = city;
				}

				public Float getLatitude() 
				{
					return latitude;
				}

				public void setLatitude(Float latitude)
				{
					this.latitude = latitude;
				}

				public Float getLongitude() 
				{
					return longitude;
				}

				public void setLongitude(Float longitude)
				{
					this.longitude = longitude;
				}

				public Float getDistance() 
				{
					return distance;
				}

				public void setDistance(Float distance)
				{
					this.distance = distance;
				}

				public int getTimezone() 
				{
					return timezone;
				}

				public void setTimezone(int timezone)
				{
					this.timezone = timezone;
				}

				public class Observation
				{
					private String daylight;
					private String description;
					private String skyInfo;
					private String skyDescription;
					private String temperature;
					private String temperatureDesc;
					private String comfort;
					private String highTemperature;
					private String lowTemperature;
					private String humidity;
					private String dewPoint;
					private String precipitation1H;
					private String precipitation3H;
					private String precipitation6H;
					private String precipitation12H;
					private String precipitation24H;
					private String precipitationDesc;
					private String airInfo;
					private String airDescription;
					private String windSpeed;
					private String windDirection;
					private String windDesc;
					private String windDescShort;
					private String barometerPressure;
					private String barometerTrend;
					private String visibility;
					private String snowCover;
					private String icon;
					private String iconName;
					private String iconLink;
					private String ageMinutes;
					private String activeAlerts;
					private String country;
					private String state;
					private String city;
					private Float latitude;
					private Float longitude;
					private String distance;
					private String elevation;
					private String utcTime;
					
					public String getDaylight()
					{
						return daylight;
					}
					
					public void setDaylight(String daylight)
					{
						this.daylight = daylight;
					}
					
					public String getDescription() 
					{
						return description;
					}
					
					public void setDescription(String description)
					{
						this.description = description;
					}
					
					public String getSkyInfo()
					{
						return skyInfo;
					}
					
					public void setSkyInfo(String skyInfo)
					{
						this.skyInfo = skyInfo;
					}
					
					public String getSkyDescription() 
					{
						return skyDescription;
					}
					
					public void setSkyDescription(String skyDescription)
					{
						this.skyDescription = skyDescription;
					}
					
					public Float getTemperature() 
					{
						return Float.parseFloat( temperature );
					}
					
					public void setTemperature(String temperature)
					{
						this.temperature = temperature;
					}
					
					public String getTemperatureDesc()
					{
						return temperatureDesc;
					}
					
					public void setTemperatureDesc(String temperatureDesc)
					{
						this.temperatureDesc = temperatureDesc;
					}
					
					public Float getComfort() 
					{
						return Float.parseFloat( comfort );
					}
					
					public void setComfort(String comfort)
					{
						this.comfort = comfort;
					}
					
					public Float getHighTemperature() 
					{
						return Float.parseFloat( highTemperature );
					}
					
					public void setHighTemperature(String highTemperature)
					{
						this.highTemperature = highTemperature;
					}
					
					public Float getLowTemperature()
					{
						return Float.parseFloat( lowTemperature );
					}
					
					public void setLowTemperature(String lowTemperature)
					{
						this.lowTemperature = lowTemperature;
					}
					
					public Float getHumidity() 
					{
						return Float.parseFloat( humidity );
					}
					
					public void setHumidity(String humidity) 
					{
						this.humidity = humidity;
					}
					
					public Float getDewPoint() 
					{
						return Float.parseFloat( dewPoint );
					}
					
					public void setDewPoint(String dewPoint) 
					{
						this.dewPoint = dewPoint;
					}
					
					public String getPrecipitation1H() 
					{
						return precipitation1H;
					}
					
					public void setPrecipitation1H(String precipitation1h) 
					{
						precipitation1H = precipitation1h;
					}
					
					public String getPrecipitation3H() 
					{
						return precipitation3H;
					}
					
					public void setPrecipitation3H(String precipitation3h)
					{
						precipitation3H = precipitation3h;
					}
					
					public String getPrecipitation6H()
					{
						return precipitation6H;
					}
					
					public void setPrecipitation6H(String precipitation6h) 
					{
						precipitation6H = precipitation6h;
					}
					
					public String getPrecipitation12H() 
					{
						return precipitation12H;
					}
					
					public void setPrecipitation12H(String precipitation12h)
					{
						precipitation12H = precipitation12h;
					}
					
					public String getPrecipitation24H() 
					{
						return precipitation24H;
					}
					
					public void setPrecipitation24H(String precipitation24h)
					{
						precipitation24H = precipitation24h;
					}
					
					public String getPrecipitationDesc() 
					{
						return precipitationDesc;
					}
					
					public void setPrecipitationDesc(String precipitationDesc)
					{
						this.precipitationDesc = precipitationDesc;
					}
					
					public String getAirInfo() 
					{
						return airInfo;
					}
					
					public void setAirInfo(String airInfo)
					{
						this.airInfo = airInfo;
					}
					
					public String getAirDescription() 
					{
						return airDescription;
					}
					
					public void setAirDescription(String airDescription) 
					{
						this.airDescription = airDescription;
					}
					
					public Float getWindSpeed() 
					{
						return Float.parseFloat( windSpeed );
					}
					
					public void setWindSpeed(String windSpeed)
					{
						this.windSpeed = windSpeed;
					}
					
					public String getWindDirection()
					{
						return windDirection;
					}
					
					public void setWindDirection(String windDirection)
					{
						this.windDirection = windDirection;
					}
					
					public String getWindDesc() 
					{
						return windDesc;
					}
					
					public void setWindDesc(String windDesc)
					{
						this.windDesc = windDesc;
					}
					
					public String getWindDescShort()
					{
						return windDescShort;
					}
					
					public void setWindDescShort(String windDescShort) 
					{
						this.windDescShort = windDescShort;
					}
					
					public String getBarometerPressure()
					{
						return barometerPressure;
					}
					
					public void setBarometerPressure(String barometerPressure)
					{
						this.barometerPressure = barometerPressure;
					}
					
					public String getBarometerTrend() 
					{
						return barometerTrend;
					}
					
					public void setBarometerTrend(String barometerTrend)
					{
						this.barometerTrend = barometerTrend;
					}
					
					public String getVisibility()
					{
						return visibility;
					}
					
					public void setVisibility(String visibility) 
					{
						this.visibility = visibility;
					}
					
					public String getSnowCover() 
					{
						return snowCover;
					}
					
					public void setSnowCover(String snowCover)
					{
						this.snowCover = snowCover;
					}
					
					public String getIcon()
					{
						return icon;
					}
					
					public void setIcon(String icon)
					{
						this.icon = icon;
					}
					
					public String getIconName()
					{
						return iconName;
					}
					
					public void setIconName(String iconName)
					{
						this.iconName = iconName;
					}
					
					public String getIconLink() 
					{
						return iconLink;
					}
					
					public void setIconLink(String iconLink)
					{
						this.iconLink = iconLink;
					}
					
					public String getAgeMinutes() 
					{
						return ageMinutes;
					}
					
					public void setAgeMinutes(String ageMinutes) 
					{
						this.ageMinutes = ageMinutes;
					}
					
					public String getActiveAlerts()
					{
						return activeAlerts;
					}
					
					public void setActiveAlerts(String activeAlerts) 
					{
						this.activeAlerts = activeAlerts;
					}
					
					public String getCountry() 
					{
						return country;
					}
					
					public void setCountry(String country)
					{
						this.country = country;
					}
					
					public String getState()
					{
						return state;
					}
					
					public void setState(String state) 
					{
						this.state = state;
					}
					
					public String getCity() 
					{
						return city;
					}
					
					public void setCity(String city) 
					{
						this.city = city;
					}
					
					public Float getLatitude()
					{
						return latitude;
					}
					
					public void setLatitude(Float latitude) 
					{
						this.latitude = latitude;
					}
					
					public Float getLongitude() 
					{
						return longitude;
					}
					
					public void setLongitude(Float longitude)
					{
						this.longitude = longitude;
					}
					
					public String getDistance()
					{
						return distance;
					}
					
					public void setDistance(String distance)
					{
						this.distance = distance;
					}
					
					public String getElevation()
					{
						return elevation;
					}
					
					public void setElevation(String elevation) 
					{
						this.elevation = elevation;
					}
					
					public String getUtcTime() 
					{
						return utcTime;
					}
					
					public void setUtcTime(String utcTime)
					{
						this.utcTime = utcTime;
					}
				}// end of class Observation
			}// end of class Location
		}// end of class Observations
	}// end of class WeatherData
	
	public class ForecastData
	{
		private DailyForecasts dailyForecasts;
		
		public DailyForecasts getDailyForecasts()
		{
			return dailyForecasts;
		}

		public void setDailyForecasts(DailyForecasts dailyForecasts) 
		{
			this.dailyForecasts = dailyForecasts;
		}

		public class DailyForecasts
		{
			private ForecastLocation forecastLocation;
			
			public ForecastLocation getForecastLocation() 
			{
				return forecastLocation;
			}

			public void setForecastLocation(ForecastLocation forecastLocation) 
			{
				this.forecastLocation = forecastLocation;
			}

			public class ForecastLocation
			{
				private List<Forecast> forecast;
				
				public List<Forecast> getForecast() 
				{
					return forecast;
				}

				public void setForecast(List<Forecast> forecast) 
				{
					this.forecast = forecast;
				}

				public class Forecast
				{
					private String daylight;
					private String description;
					private String skyInfo;
					private String skyDescription;
					private String temperatureDesc;
					private String comfort;
					private String highTemperature;
					private String lowTemperature;
					private String humidity;
					private String dewPoint;
					private String precipitationProbability;
					private String precipitationDesc;
					private String rainFall;
					private String snowFall;
					private String airInfo;
					private String airDescription;
					private String windSpeed;
					private String windDirection;
					private String windDesc;
					private String windDescShort;
					private String beaufortScale;
					private String beaufortDescription;
					private String uvIndex;
					private String uvDesc;
					private String barometerPressure;
					private String icon;
					private String iconName;
					private String iconLink;
					private String dayOfWeek;
					private String weekday;
					private String utcTime;
					
					public String getDaylight() 
					{
						return daylight;
					}
					
					public void setDaylight(String daylight)
					{
						this.daylight = daylight;
					}
					
					public String getDescription()
					{
						return description;
					}
					
					public void setDescription(String description)
					{
						this.description = description;
					}
					
					public String getSkyInfo() 
					{
						return skyInfo;
					}
					
					public void setSkyInfo(String skyInfo)
					{
						this.skyInfo = skyInfo;
					}
					
					public String getSkyDescription()
					{
						return skyDescription;
					}
					
					public void setSkyDescription(String skyDescription)
					{
						this.skyDescription = skyDescription;
					}
					
					public String getTemperatureDesc() 
					{
						return temperatureDesc;
					}
					
					public void setTemperatureDesc(String temperatureDesc)
					{
						this.temperatureDesc = temperatureDesc;
					}
					
					public Float getComfort() 
					{
						return Float.parseFloat( comfort );
					}
					
					public void setComfort(String comfort)
					{
						this.comfort = comfort;
					}
					
					public Float getHighTemperature() 
					{
						return Float.parseFloat( highTemperature );
					}
					
					public void setHighTemperature(String highTemperature) 
					{
						this.highTemperature = highTemperature;
					}
					
					public Float getLowTemperature()
					{
						return Float.parseFloat( lowTemperature );
					}
					
					public void setLowTemperature(String lowTemperature)
					{
						this.lowTemperature = lowTemperature;
					}
					
					public String getHumidity() 
					{
						return humidity;
					}
					
					public void setHumidity(String humidity)
					{
						this.humidity = humidity;
					}
					
					public String getDewPoint()
					{
						return dewPoint;
					}
					
					public void setDewPoint(String dewPoint)
					{
						this.dewPoint = dewPoint;
					}
					
					public String getPrecipitationProbability() 
					{
						return precipitationProbability;
					}
					
					public void setPrecipitationProbability(String precipitationProbability) 
					{
						this.precipitationProbability = precipitationProbability;
					}
					
					public String getPrecipitationDesc()
					{
						return precipitationDesc;
					}
					
					public void setPrecipitationDesc(String precipitationDesc)
					{
						this.precipitationDesc = precipitationDesc;
					}
					
					public String getRainFall() 
					{
						return rainFall;
					}
					
					public void setRainFall(String rainFall)
					{
						this.rainFall = rainFall;
					}
					
					public String getSnowFall() 
					{
						return snowFall;
					}
					
					public void setSnowFall(String snowFall)
					{
						this.snowFall = snowFall;
					}
					
					public String getAirInfo() 
					{
						return airInfo;
					}
					
					public void setAirInfo(String airInfo)
					{
						this.airInfo = airInfo;
					}
					
					public String getAirDescription()
					{
						return airDescription;
					}
					
					public void setAirDescription(String airDescription)
					{
						this.airDescription = airDescription;
					}
					
					public String getWindSpeed() 
					{
						return windSpeed;
					}
					
					public void setWindSpeed(String windSpeed) 
					{
						this.windSpeed = windSpeed;
					}
					
					public String getWindDirection()
					{
						return windDirection;
					}
					
					public void setWindDirection(String windDirection)
					{
						this.windDirection = windDirection;
					}
					
					public String getWindDesc() 
					{
						return windDesc;
					}
					
					public void setWindDesc(String windDesc) 
					{
						this.windDesc = windDesc;
					}
					
					public String getWindDescShort() 
					{
						return windDescShort;
					}
					
					public void setWindDescShort(String windDescShort)
					{
						this.windDescShort = windDescShort;
					}
					
					public String getBeaufortScale() 
					{
						return beaufortScale;
					}
					
					public void setBeaufortScale(String beaufortScale)
					{
						this.beaufortScale = beaufortScale;
					}
					
					public String getBeaufortDescription()
					{
						return beaufortDescription;
					}
					
					public void setBeaufortDescription(String beaufortDescription)
					{
						this.beaufortDescription = beaufortDescription;
					}
					
					public String getUvIndex()
					{
						return uvIndex;
					}
					
					public void setUvIndex(String uvIndex)
					{
						this.uvIndex = uvIndex;
					}
					
					public String getUvDesc()
					{
						return uvDesc;
					}
					
					public void setUvDesc(String uvDesc)
					{
						this.uvDesc = uvDesc;
					}
					
					public String getBarometerPressure() 
					{
						return barometerPressure;
					}
					
					public void setBarometerPressure(String barometerPressure)
					{
						this.barometerPressure = barometerPressure;
					}
					
					public String getIcon() 
					{
						return icon;
					}
					
					public void setIcon(String icon) 
					{
						this.icon = icon;
					}
					
					public String getIconName()
					{
						return iconName;
					}
					
					public void setIconName(String iconName)
					{
						this.iconName = iconName;
					}
					
					public String getIconLink()
					{
						return iconLink;
					}
					
					public void setIconLink(String iconLink)
					{
						this.iconLink = iconLink;
					}
					
					public String getDayOfWeek()
					{
						return dayOfWeek;
					}
					
					public void setDayOfWeek(String dayOfWeek)
					{
						this.dayOfWeek = dayOfWeek;
					}
					
					public String getWeekday()
					{
						return weekday;
					}
					
					public void setWeekday(String weekday)
					{
						this.weekday = weekday;
					}
					
					public String getUtcTime() 
					{
						return utcTime;
					}
					
					public void setUtcTime(String utcTime) 
					{
						this.utcTime = utcTime;
					}
				}// end of class Forecast
			}// end of class ForecastLocation
		}// end of class DailyForecasts
	}// end of class ForecastData
	
	public class AstronomyData
	{
		private Astronomic astronomy;
		
		public Astronomic getAstronomy() 
		{
			return astronomy;
		}

		public void setAstronomy(Astronomic astronomy) 
		{
			this.astronomy = astronomy;
		}

		public class Astronomic
		{
			private List<Astronomy> astronomy;
			private String country;
			private String state;
			private String city;
			private Float latitude;
			private Float longitude;
			private int timezone;
			
			public List<Astronomy> getAstronomy() 
			{
				return astronomy;
			}

			public void setAstronomy(List<Astronomy> astronomy)
			{
				this.astronomy = astronomy;
			}

			public String getCountry() 
			{
				return country;
			}

			public void setCountry(String country) 
			{
				this.country = country;
			}

			public String getState()
			{
				return state;
			}

			public void setState(String state)
			{
				this.state = state;
			}

			public String getCity() 
			{
				return city;
			}

			public void setCity(String city) 
			{
				this.city = city;
			}

			public Float getLatitude() 
			{
				return latitude;
			}

			public void setLatitude(Float latitude)
			{
				this.latitude = latitude;
			}

			public Float getLongitude()
			{
				return longitude;
			}

			public void setLongitude(Float longitude)
			{
				this.longitude = longitude;
			}

			public int getTimezone() 
			{
				return timezone;
			}

			public void setTimezone(int timezone) 
			{
				this.timezone = timezone;
			}

			public class Astronomy
			{
				private String sunrise;
				private String sunset;
				private String moonrise;
				private String moonset;
				private String moonPhase;
				private String moonPhaseDesc;
				private String iconName;
				private String city;
				private String latitude;
				private String longitude;
				private String utcTime;
				
				public String getSunrise() 
				{
					return sunrise;
				}
				
				public void setSunrise(String sunrise) 
				{
					this.sunrise = sunrise;
				}
				
				public String getSunset() 
				{
					return sunset;
				}
				
				public void setSunset(String sunset)
				{
					this.sunset = sunset;
				}
				
				public String getMoonrise() 
				{
					return moonrise;
				}
				
				public void setMoonrise(String moonrise)
				{
					this.moonrise = moonrise;
				}
				
				public String getMoonset() 
				{
					return moonset;
				}
				
				public void setMoonset(String moonset) 
				{
					this.moonset = moonset;
				}
				
				public String getMoonPhase() 
				{
					return moonPhase;
				}
				
				public void setMoonPhase(String moonPhase)
				{
					this.moonPhase = moonPhase;
				}
				
				public String getMoonPhaseDesc()
				{
					return moonPhaseDesc;
				}
				
				public void setMoonPhaseDesc(String moonPhaseDesc)
				{
					this.moonPhaseDesc = moonPhaseDesc;
				}
				public String getIconName()
				{
					return iconName;
				}
				
				public void setIconName(String iconName)
				{
					this.iconName = iconName;
				}
				
				public String getCity() 
				{
					return city;
				}
				
				public void setCity(String city)
				{
					this.city = city;
				}
				
				public String getLatitude()
				{
					return latitude;
				}
				
				public void setLatitude(String latitude)
				{
					this.latitude = latitude;
				}
				
				public String getLongitude() 
				{
					return longitude;
				}
				
				public void setLongitude(String longitude)
				{
					this.longitude = longitude;
				}
				
				public String getUtcTime() 
				{
					return utcTime;
				}
				
				public void setUtcTime(String utcTime)
				{
					this.utcTime = utcTime;
				}
			}// end of class AstronomyData
		}// end of class Astronomy
	}// end of class AstronomyData

	private static boolean DeserializeHereWXJSON( String strJSON )
    {
    	Gson gson = new Gson();
        hereWxWeatherData = gson.fromJson( strJSON, HereMapsWeatherDataItem.WeatherData.class );

		return hereWxWeatherData != null;
    }// end of method DeserializeHereWXJSON

    private static boolean DeserializeHereFxJSON( String strJSON )
    {
    	Gson gson = new Gson();
        hereFxWeatherData = gson.fromJson( strJSON, HereMapsWeatherDataItem.ForecastData.class );

		return hereFxWeatherData != null;
    }// end of method DeserializeHereFxJSON
    
    private static boolean DeserializeHereAxJSON( String strJSON )
    {
        Gson gson = new Gson();
        hereAxData = gson.fromJson( strJSON, HereMapsWeatherDataItem.AstronomyData.class );

		return hereAxData != null;
    }// end of method DeserializeHereAxJSON
}// end of class HereMapsWeatherDataItem
