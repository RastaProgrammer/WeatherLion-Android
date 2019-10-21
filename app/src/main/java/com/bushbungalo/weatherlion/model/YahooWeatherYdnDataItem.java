package com.bushbungalo.weatherlion.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * @author Paul O. Patterson
 * <br />
 * <b style="margin-left:-40px">Date Created:</b>
 * <br />
 * 01/24/19
 */

// Yahoo! Weather's 2019 weather API
@SuppressWarnings({"unused"})
public class YahooWeatherYdnDataItem 
{
	static YahooWeatherYdnDataItem yahooWeatherData;
	
	private Location location;
	private CurrentObservation current_observation;
	private List<Forecast> forecasts;
		
	public Location getLocation()
	{
		return location;
	}// end of method getLocation

	public void setLocation( Location location )
	{
		this.location = location;
	}// end of method setLocation

	public CurrentObservation getCurrentObservation()
	{
		return current_observation;
	}// end of method getCurrentObservation

	public void setCurrentObservation( CurrentObservation current_observation )
	{
		this.current_observation = current_observation;
	}// end of method setCurrentObservation

	public List<Forecast> getForecast()
	{
		return forecasts;
	}// end of method getForecast

	public void setForecast( List<Forecast> forecasts )
	{
		this.forecasts = forecasts;
	}// end of method setForecast

	public class Location
	{
		private int woeid;
	    private String city;
	    private String region;
	    private String country;
	    private double lat;
	    @SerializedName("long")
	    private double lon;
	    private String timezone_id;
	    	    
		public int getWoeid() 
		{
			return woeid;
		}// end of method getWoeid
		
		public void setWoeid( int woeid )
		{
			this.woeid = woeid;
		}// end of method setWoeid
		
		public String getCity()
		{
			return city;
		}// end of method getCity
		
		public void setCity( String city )
		{
			this.city = city;
		}// end of method setCity
		
		public String getRegion()
		{
			return region;
		}// end of method getRegion
		
		public void setRegion( String region )
		{
			this.region = region;
		}// end of method setRegion
		
		public String getCountry() 
		{
			return country;
		}// end of method getCountry
		
		public void setCountry( String country )
		{
			this.country = country;
		}// end of method 
		
		public double getLat() 
		{
			return lat;
		}// end of method getLat
		
		public void setLat( double lat )
		{
			this.lat = lat;
		}// end of method setLat
		
		public double getLong()
		{
			return lon;
		}// end of method getLong
		
		public void setLong( double _long ) 
		{
			this.lon = _long;
		}// end of method setLong
		
		public String getTimezoneId()
		{
			return timezone_id;
		}// end of method getTimezoneId
		
		public void setTimezoneId( String timezoneId )
		{
			this.timezone_id = timezoneId;
		}// end of method setTimezoneId		
	}// end of class Location
	
	public class CurrentObservation
	{
	    private Wind wind;
	    private Atmosphere atmosphere;
	    private Astronomy astronomy;
	    private Condition condition;
	    private long pubDate;
	    
		public Wind getWind() 
		{
			return wind;
		}// end of method getWind

		public void setWind( Wind wind )
		{
			this.wind = wind;
		}// end of method setWind

		public Atmosphere getAtmosphere() 
		{
			return atmosphere;
		}// end of method getAtmosphere

		public void setAtmosphere( Atmosphere atmosphere )
		{
			this.atmosphere = atmosphere;
		}// end of method setAtmosphere

		public Astronomy getAstronomy()
		{
			return astronomy;
		}// end of method getAstronomy

		public void setAstronomy( Astronomy astronomy )
		{
			this.astronomy = astronomy;
		}// end of method setAstronomy

		public Condition getCondition() 
		{
			return condition;
		}// end of method getCondition

		public void setCondition( Condition condition )
		{
			this.condition = condition;
		}// end of method setCondition

		public long getPubDate()
		{
			return pubDate;
		}// end of method getPubDate

		public void setPubDate( long pubDate )
		{
			this.pubDate = pubDate;
		}// end of method setPubDate

		public class Wind
	    {
			 private double chill;
			 private int direction;
			 private double speed;
	         
			 public double getChill() 
			 {
				return chill;
			 }// end of method getChill
			
			public void setChill( double chill )
			{
				this.chill = chill;
			}// end of method setChill
			
			public int getDirection()
			{
				return direction;
			}// end of method getDirection
			
			public void setDirection( int direction )
			{
				this.direction = direction;
			}// end of method setDirection
			
			public double getSpeed()
			{
				return speed;
			}// end of method getSpeed
			
			public void setSpeed( double speed )
			{
				this.speed = speed;
			}// end of method setSpeed
	    }// end of class Wind
		
		public class Atmosphere
		{
			 private double humidity;
			 private double visibility;
			 private double pressure;
	         
			 public double getHumidity() 
			 {
				return humidity;
			 }// end of method getHumidity
			 
			 public void setHumidity( double humidity )
			 {
				this.humidity = humidity;
			 }// end of method setHumidity
			 
			 public double getVisibility()
			 {
				return visibility;
			 }// end of method getVisibility	
			
			 public void setVisibility( double visibility )
			 {
				this.visibility = visibility;
			 }// end of method setVisibility
			
			 public double getPressure()
			 {
				return pressure;
			 }// end of method getPressure
			
			 public void setPressure( double pressure )
			 {
				this.pressure = pressure;
			 }// end of method setPressure
		}// end of class  Atmosphere
		
		public class Astronomy
		{
	         private String sunrise;
	         private String sunset;
	         
	         public String getSunrise()
	         {
				return sunrise;
	         }// end of method getSunrise
	         
	         public void setSunrise( String sunrise )
	         {
				this.sunrise = sunrise;
	         }// end of method setSunrise
			
	         public String getSunset()
	         {
				return sunset;
	         }// end of method getSunset
			
	         public void setSunset( String sunset )
	         {
				this.sunset = sunset;
	         }// end of method setSunset
	    }// end of class Astronomy
		
		public class Condition
		{
	         private String text;
	         private int code;
	         private double temperature;
	         
			public String getText()
			{
				return text;
			}// end of method getText
			
			public void setText( String text )
			{
				this.text = text;
			}// end of method setText
			
			public int getCode()
			{
				return code;
			}// end of method getCode
			
			public void setCode( int code )
			{
				this.code = code;
			}// end of method setCode
			
			public double getTemperature()
			{
				return temperature;
			}// end of method getTemperature
			
			public void setTemperature( double temperature )
			{
				this.temperature = temperature;
			}// end of method setTemperature
	    }// end of class Condition
	}// end of classCurrentObservation
	
	public class Forecast
	{
		private String day;
        private long date;
        private double low;
        private double high;
        private String text;
        private int code;
        
		public String getDay() 
		{
			return day;
		}// end of method getDay
		
		public void setDay( String day ) 
		{
			this.day = day;
		}// end of method setDay
		
		public long getDate()
		{
			return date;
		}// end of method getDate
		
		public void setDate( long date )
		{
			this.date = date;
		}// end of method setDate
		
		public double getLow()
		{
			return low;
		}// end of method getLow
		
		public void setLow( double low ) 
		{
			this.low = low;
		}// end of method setLow
		
		public double getHigh() 
		{
			return high;
		}// end of method getHigh
		
		public void setHigh( double high ) 
		{
			this.high = high;
		}// end of method setHigh
		
		public String getText()
		{
			return text;
		}// end of method getText
		
		public void setText( String text ) 
		{
			this.text = text;
		}// end of method setText
		
		public int getCode() 
		{
			return code;
		}// end of method getCode
		
		public void setCode( int code ) 
		{
			this.code = code;
		}// end of method setCode
	}// end of class Forecast
	
	public static boolean DeserializeYahooJSON( String strJSON )
    {
        Gson gson = new Gson();
        yahooWeatherData = gson.fromJson( strJSON, YahooWeatherYdnDataItem.class );

		return yahooWeatherData != null;
    }// end of method DeserializeYahooJSON
	
}// end of class YahooWeatherYdnDataItem
