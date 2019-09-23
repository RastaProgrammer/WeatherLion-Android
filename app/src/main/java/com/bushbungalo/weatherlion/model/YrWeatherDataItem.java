package com.bushbungalo.weatherlion.model;

import com.bushbungalo.weatherlion.utils.UtilityMethod;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * @author Paul O. Patterson
 * <br />
 * <b style="margin-left:-40px">Date Created:</b>
 * <br />
 * 11/21/17
 */

@SuppressWarnings("unused")
public class YrWeatherDataItem
{
    private String name;
    private String type;
    private String country;
    private String tzId;
    private float tzUtcOffsetMinutes;
    private float locAltitude;
    private float locLatitude;
    private float locLongitude;
    private String geonames;
    private long geobaseid;
    private Date lastupdate;
    private Date sunrise;
    private Date sunset;

    public static YrWeatherDataItem yrWeatherDataItem;
    private List<Forecast> forecast;   

	private YrWeatherDataItem()
    {
    }   
    
	public List<Forecast> getForecast()
    {
		return forecast;
	}
	
    public String getName() 
    {
		return name;
	}

	public void setName(String name) 
	{
		this.name = name;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getCountry() 
	{
		return country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public String getTzId()
	{
		return tzId;
	}

	public void setTzId(String tzId)
	{
		this.tzId = tzId;
	}

	public float getTzUtcOffsetMinutes()
	{
		return tzUtcOffsetMinutes;
	}

	public void setTzUtcOffsetMinutes(float tzUtcOffsetMinutes)
	{
		this.tzUtcOffsetMinutes = tzUtcOffsetMinutes;
	}

	public float getLocAltitude() 
	{
		return locAltitude;
	}

	public void setLocAltitude(float locAltitude) 
	{
		this.locAltitude = locAltitude;
	}

	public float getLocLatitude() 
	{
		return locLatitude;
	}

	public void setLocLatitude(float locLatitude)
	{
		this.locLatitude = locLatitude;
	}

	public float getLocLongitude() 
	{
		return locLongitude;
	}

	public void setLocLongitude(float locLongitude)
	{
		this.locLongitude = locLongitude;
	}

	public String getGeonames() 
	{
		return geonames;
	}

	public void setGeonames(String geonames) 
	{
		this.geonames = geonames;
	}

	public long getGeobaseid() 
	{
		return geobaseid;
	}

	public void setGeobaseid(long geobaseid)
	{
		this.geobaseid = geobaseid;
	}

	public Date getLastupdate()
	{
		return lastupdate;
	}

	public void setLastupdate(Date lastupdate)
	{
		this.lastupdate = lastupdate;
	}

	public Date getSunrise() 
	{
		return sunrise;
	}

	public void setSunrise(Date sunrise)
	{
		this.sunrise = sunrise;
	}

	public Date getSunset() 
	{
		return sunset;
	}

	public void setSunset(Date sunset) 
	{
		this.sunset = sunset;
	}	

	public void setForecast(List<Forecast> forecast)
	{
		this.forecast = forecast;
	}

	public static class Forecast
    {
        private Date timeFrom;
        private Date timeTo;
        private int timePeriod;
        private int symbolNumber;
        private int symbolNumberEx;
        private String symbolName;
        private String symbolVar;
        private float precipValue;
        private float windDirDeg;
        private String windDirCode;
        private String windDirName;
        private float windSpeedMps;
        private String windSpeedName;
        private String temperatureUnit;
        private float temperatureValue;
        private String pressureUnit;
        private float pressureValue;

        public Forecast(Date timeFrom, Date timeTo, int timePeriod,
                        int symbolNumber, int symbolNumberEx, String symbolName,
                        String symbolVar, float precipValue, float windDirDeg, String windDirCode,
                        String windDirName, float windSpeedMps, String windSpeedName,
                        String temperatureUnit, float temperatureValue, String pressureUnit, float pressureValue)
        {
            this.timeFrom = timeFrom;
            this.timeTo = timeTo;
            this.timePeriod = timePeriod;
            this.symbolNumber = symbolNumber;
            this.symbolNumberEx = symbolNumberEx;
            this.symbolName = symbolName;
            this.symbolVar = symbolVar;
            this.precipValue = precipValue;
            this.windDirDeg = windDirDeg;
            this.windDirCode = windDirCode;
            this.windDirName = windDirName;
            this.windSpeedMps = windSpeedMps;
            this.windSpeedName = windSpeedName;
            this.temperatureUnit = temperatureUnit;
            this.temperatureValue = temperatureValue;
            this.pressureUnit = pressureUnit;
            this.pressureValue = pressureValue;
        }// end of constructor

		public Date getTimeFrom()
		{
			return timeFrom;
		}

		public void setTimeFrom(Date timeFrom)
		{
			this.timeFrom = timeFrom;
		}

		public Date getTimeTo()
		{
			return timeTo;
		}

		public void setTimeTo(Date timeTo)
		{
			this.timeTo = timeTo;
		}

		public int getTimePeriod() 
		{
			return timePeriod;
		}

		public void setTimePeriod(int timePeriod)
		{
			this.timePeriod = timePeriod;
		}

		public int getSymbolNumber() 
		{
			return symbolNumber;
		}

		public void setSymbolNumber(int symbolNumber)
		{
			this.symbolNumber = symbolNumber;
		}

		public int getSymbolNumberEx()
		{
			return symbolNumberEx;
		}

		public void setSymbolNumberEx(int symbolNumberEx)
		{
			this.symbolNumberEx = symbolNumberEx;
		}

		public String getSymbolName() 
		{
			return symbolName;
		}

		public void setSymbolName(String symbolName)
		{
			this.symbolName = symbolName;
		}

		public String getSymbolVar() 
		{
			return symbolVar;
		}

		public void setSymbolVar(String symbolVar) 
		{
			this.symbolVar = symbolVar;
		}

		public float getPrecipValue() 
		{
			return precipValue;
		}

		public void setPrecipValue(float precipValue)
		{
			this.precipValue = precipValue;
		}

		public float getWindDirDeg() 
		{
			return windDirDeg;
		}

		public void setWindDirDeg(float windDirDeg)
		{
			this.windDirDeg = windDirDeg;
		}

		public String getWindDirCode()
		{
			return windDirCode;
		}

		public void setWindDirCode(String windDirCode)
		{
			this.windDirCode = windDirCode;
		}

		public String getWindDirName() 
		{
			return windDirName;
		}

		public void setWindDirName(String windDirName) 
		{
			this.windDirName = windDirName;
		}

		public float getWindSpeedMps() 
		{
			return windSpeedMps;
		}

		public void setWindSpeedMps(float windSpeedMps) 
		{
			this.windSpeedMps = windSpeedMps;
		}

		public String getWindSpeedName() 
		{
			return windSpeedName;
		}

		public void setWindSpeedName(String windSpeedName)
		{
			this.windSpeedName = windSpeedName;
		}

		public String getTemperatureUnit()
		{
			return temperatureUnit;
		}

		public void setTemperatureUnit(String temperatureUnit) 
		{
			this.temperatureUnit = temperatureUnit;
		}

		public float getTemperatureValue() 
		{
			return temperatureValue;
		}

		public void setTemperatureValue(float temperatureValue) 
		{
			this.temperatureValue = temperatureValue;
		}

		public String getPressureUnit() 
		{
			return pressureUnit;
		}

		public void setPressureUnit(String pressureUnit) 
		{
			this.pressureUnit = pressureUnit;
		}

		public float getPressureValue() 
		{
			return pressureValue;
		}

		public void setPressureValue(float pressureValue)
		{
			this.pressureValue = pressureValue;
		}
    }// end of inner class Forecast 

	public static boolean deserializeYrXML( String xmlData )
    {
		SAXBuilder builder = new SAXBuilder();
		Document weatherXML = null;
		
		try 
		{
			InputStream xmlStream = new ByteArrayInputStream( xmlData.getBytes( StandardCharsets.UTF_8 ) );
			weatherXML = builder.build( xmlStream );
		}// end of try block 
		catch ( IOException | JDOMException e )
		{
			UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
			"::deserializeYrXML [line: " +
						e.getStackTrace()[1].getLineNumber()+ "]" );
		}// end of catch block
		
    	yrWeatherDataItem = new YrWeatherDataItem();
    	
    	// just in case the document contains unnecessary white spaces
		builder.setIgnoringElementContentWhitespace( true );       		
		
		// get the root node of the XML document
		Element rootNode = Objects.requireNonNull( weatherXML ).getRootElement();
		Element xnlCurrentLocation = rootNode.getChild( "location" );
		Element meta = rootNode.getChild( "meta" );
		Element sun = rootNode.getChild( "sun" );
		Element xmlForecast = rootNode.getChild( "forecast" );
		Element tabular = xmlForecast.getChild( "tabular" );
        List< Element > elemList = tabular.getChildren("time");
        DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH );
        
        yrWeatherDataItem.name = xnlCurrentLocation.getChildText( "name" );
        yrWeatherDataItem.type = xnlCurrentLocation.getChildText( "type" );
        yrWeatherDataItem.country = xnlCurrentLocation.getChildText( "country" );
        yrWeatherDataItem.tzId = xnlCurrentLocation.getAttributeValue( "id" );
        yrWeatherDataItem.tzUtcOffsetMinutes =
        		Float.parseFloat( xnlCurrentLocation.getChild( "timezone" ).getAttributeValue( "utcoffsetMinutes" ) );
        yrWeatherDataItem.locAltitude = 
        		Float.parseFloat( xnlCurrentLocation.getChild( "location" ).getAttributeValue( "altitude" ) );
        yrWeatherDataItem.locLatitude = 
        		Float.parseFloat( xnlCurrentLocation.getChild( "location" ).getAttributeValue( "latitude" ) );
        yrWeatherDataItem.locLongitude = 
        		Float.parseFloat( xnlCurrentLocation.getChild( "location" ).getAttributeValue( "longitude" ) );
        yrWeatherDataItem.geonames = xnlCurrentLocation.getChild( "location" ).getAttributeValue( "geonames" );
        yrWeatherDataItem.geobaseid = 
        		Long.parseLong( xnlCurrentLocation.getChild( "location" ).getAttributeValue( "geobaseid" ) );
        
        yrWeatherDataItem.lastupdate = null;
        yrWeatherDataItem.sunrise = null;
        yrWeatherDataItem.sunset = null;
        
        try
        {			
			yrWeatherDataItem.lastupdate = dateFormat.parse( meta.getChildText( "lastupdate" ) );
	        yrWeatherDataItem.sunrise = dateFormat.parse( sun.getAttributeValue( "rise" ) );
	        yrWeatherDataItem.sunset = dateFormat.parse( sun.getAttributeValue( "set" ) );
		}// end of try block
        catch ( ParseException e ) 
        {
			e.printStackTrace();
		}// end of catch block 
        
        yrWeatherDataItem.forecast = new ArrayList<>();
        
        for ( int i = 0; i < elemList.size(); i++ )
        {
        	Date timeFrom = null;
			Date timeTo = null;
			
            try
            {
				timeFrom = dateFormat.parse( elemList.get( i ).getAttributeValue( "from" ) );
				timeTo = dateFormat.parse( elemList.get( i ).getAttributeValue( "to" ) );
			}// end of try block
            catch ( ParseException e ) 
            {
				e.printStackTrace();
			}// end of catch block               
            
            int timePeriod = Integer.parseInt( elemList.get( i ).getAttributeValue( "period" ) );
            int symbolNumber = Integer.parseInt( elemList.get( i ).getChild( "symbol" ).getAttributeValue( "number" ) );
            int symbolNumberEx = Integer.parseInt( elemList.get( i ).getChild( "symbol" ).getAttributeValue( "numberEx" ) );
            String symbolName = elemList.get( i ).getChild( "symbol" ).getAttributeValue( "name" );
            String symbolVar = elemList.get( i ).getChild( "symbol" ).getAttributeValue( "var" );
            float precipValue = Float.parseFloat( elemList.get( i ).getChild( "precipitation" ).getAttributeValue( "value" ) );
            float wdDeg = Float.parseFloat( elemList.get( i ).getChild( "windDirection" ).getAttributeValue( "deg") );
            String wdName = elemList.get( i ).getChild( "windDirection" ).getAttributeValue( "name" );
            String wdCode = elemList.get( i ).getChild( "windDirection" ).getAttributeValue( "code" );
            float wsMps = Float.parseFloat( elemList.get( i ).getChild( "windSpeed" ).getAttributeValue( "mps" ) );
            String wsName = elemList.get( i ).getChild( "windSpeed" ).getAttributeValue( "name" );
            String tempUnit = elemList.get( i ).getChild( "temperature" ).getAttributeValue( "unit" );
            float tempValue = Float.parseFloat( elemList.get( i ).getChild( "temperature" ).getAttributeValue( "value" ) );
            String pressureUnit = elemList.get( i ).getChild( "pressure" ).getAttributeValue( "unit" );
            float pressureValue = Float.parseFloat( elemList.get( i ).getChild( "pressure" ).getAttributeValue( "value" ) );
            
            yrWeatherDataItem.getForecast().add( new Forecast(timeFrom, timeTo, timePeriod, symbolNumber,
                                       symbolNumberEx, symbolName, symbolVar, precipValue,
                                       wdDeg, wdCode, wdName, wsMps, wsName,
                                       tempUnit, tempValue, pressureUnit, pressureValue ) );
        }// end of for loop	
    			
        return true;                  
    }// end of method DeserializeYrXML        
}// end of class yrWeatherDataItem
