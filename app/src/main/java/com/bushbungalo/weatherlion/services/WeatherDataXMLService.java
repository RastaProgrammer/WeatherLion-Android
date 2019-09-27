package com.bushbungalo.weatherlion.services;

import android.app.IntentService;
import android.content.Intent;

import com.bushbungalo.weatherlion.FiveDayForecast;
import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.model.WeatherDataXML;
import com.bushbungalo.weatherlion.utils.UtilityMethod;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author Paul O. Patterson
 * <br />
 * <b style="margin-left:-40px">Date Created:</b>
 * <br />
 * 11/21/17
 */

@SuppressWarnings({"unused"})
public class WeatherDataXMLService extends IntentService
{
	private static final String TAG = "WeatherDataXMLService";
	private WeatherDataXML xmlData = new WeatherDataXML();

	public WeatherDataXMLService() 
	{
		super( TAG );
	}// end of default constructor


	@Override
	protected void onHandleIntent( Intent intent )
	{
		String weatherDataJSON = intent.getStringExtra( WidgetUpdateService.WEATHER_XML_DATA );
		Gson gson = new Gson();
		xmlData = gson.fromJson( weatherDataJSON, new TypeToken<WeatherDataXML>() {}.getType() );
		saveCurrentWeatherXML();
	}// end of method handleWeatherData

	private boolean saveCurrentWeatherXML()
	{
		String wxData = WeatherLionApplication.getAppContext().getFileStreamPath(
				WeatherLionApplication.WEATHER_DATA_XML ).toString();

		try
		{
			Element weatherData;
			Document doc;
			Element provider = new Element( "Provider" );
			Element location = new Element( "Location" );
			Element atmosphere = new Element( "Atmosphere" );
			Element wind = new Element( "Wind" );
			Element astronomy = new Element( "Astronomy" );
			Element current = new Element( "Current" );

			// Root element
			weatherData = new Element( "WeatherData" );
			doc = new Document( weatherData );

			// Provider Details
			provider.addContent( new Element( "Name" ).setText( xmlData.getProviderName() ) );
			provider.addContent( new Element( "Date" ).setText( xmlData.getDatePublished().toString() ) );
			doc.getRootElement().addContent( provider );

			// Location Readings
			location.addContent( new Element( "City" ).setText( xmlData.getCityName() ) );
			location.addContent( new Element( "Country" ).setText( String.valueOf( xmlData.getCountryName() ) ) );
			doc.getRootElement().addContent( location );

			// Atmospheric Readings
			atmosphere.addContent( new Element( "Humidity" ).setText( String.valueOf( xmlData.getCurrentHumidity() ) ) );
			doc.getRootElement().addContent( atmosphere );

			// Wind Readings
			wind.addContent( new Element( "WindSpeed" ).setText( String.valueOf( xmlData.getCurrentWindSpeed() ) ) );
			wind.addContent( new Element( "WindDirection" ).setText( String.valueOf( xmlData.getCurrentWindDirection() ) ) );
			doc.getRootElement().addContent( wind );

			// Astronomy readings
			astronomy.addContent( new Element( "Sunrise" ).setText( String.valueOf( xmlData.getSunriseTime() ) ) );
			astronomy.addContent( new Element( "Sunset" ).setText( String.valueOf( xmlData.getSunsetTime() ) ) );
			doc.getRootElement().addContent( astronomy );

			// Current Weather
			current.addContent( new Element( "Condition" ).setText(
					UtilityMethod.toProperCase( xmlData.getCurrentConditions() ) ) );
			current.addContent( new Element( "Temperature" ).setText( String.valueOf( xmlData.getCurrentTemperature() ) ) );
			current.addContent( new Element( "FeelsLike" ).setText( String.valueOf( xmlData.getCurrentFeelsLikeTemperature() ) ) );
			current.addContent( new Element( "HighTemperature" ).setText( String.valueOf( xmlData.getCurrentHigh() ) ) );
			current.addContent( new Element( "LowTemperature" ).setText( String.valueOf( xmlData.getCurrentLow() ) ) );
			doc.getRootElement().addContent( current );

			Element forecastList = new Element( "DailyForecast" );

			// Five Day Forecast
			for ( FiveDayForecast forecast : xmlData.getFiveDayForecast() )
			{

				Element forecastData = new Element( "DayForecast" );
				forecastData.addContent( new Element( "Date" ).setText( forecast.getForecastDate().toString() ) );
				forecastData.addContent( new Element( "Condition" ).setText( UtilityMethod.toProperCase(
						forecast.getForecastCondition() ) ) );
				forecastData.addContent( new Element( "LowTemperature" ).setText( forecast.getForecastLowTemp() ) );
				forecastData.addContent( new Element( "HighTemperature" ).setText( forecast.getForecastHighTemp() ) );
				forecastList.addContent( forecastData );
			}// end of for each loop

			doc.getRootElement().addContent( forecastList );

			XMLOutputter xmlOutput = new XMLOutputter();

			// display nice nice
			xmlOutput.setFormat( Format.getPrettyFormat() );
			xmlOutput.output( doc, new FileWriter( wxData ) );

			UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO, xmlData.getProviderName() + "'s weather data was stored locally!",
				TAG + "::saveCurrentWeatherXML" );

			WeatherLionApplication.previousWeatherProvider.setLength( 0 );
			WeatherLionApplication.previousWeatherProvider.append( xmlData.getProviderName() );
		}// end of try block
		catch ( IOException e )
		{
			UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
					TAG + "::saveCurrentWeatherXML [line: " +
							UtilityMethod.getExceptionLineNumber( e )  + "]" );
		}// end of catch block

		return true;
	}// end of method saveCurrentWeatherXML

	 public static boolean saveCurrentWeatherXML( String providerName, Date datePublished, String cityName,
	    		String countryName,	String currentConditions, String currentTemperature, String currentFeelsLikeTemperature,
	    		String currentHigh, String currentLow, String currentWindSpeed, String currentWindDirection, String currentHumidity, 
	    		String sunriseTime, String sunsetTime, List<FiveDayForecast> fiveDayForecast )
    {
    	 String wxData = WeatherLionApplication.getAppContext().getFileStreamPath( WeatherLionApplication.WEATHER_DATA_XML ).toString();

		 try
		 {
			 Element weatherData;
			 Document doc;
			 Element provider = new Element( "Provider" );
			 Element location = new Element( "Location" );
			 Element atmosphere = new Element( "Atmosphere" );
			 Element wind = new Element( "Wind" );
			 Element astronomy = new Element( "Astronomy" );
			 Element current = new Element( "Current" );
	    			    	
	    	 // Root element
			 weatherData = new Element( "WeatherData" ); 
			 doc = new Document( weatherData );     		     		
        	
			 	// Provider Details
			 provider.addContent( new Element( "Name" ).setText( providerName ) );
			 provider.addContent( new Element( "Date" ).setText( datePublished.toString() ) );
			 doc.getRootElement().addContent( provider );
    	
			 // Location Readings
			 location.addContent( new Element( "City" ).setText( cityName ) );
			 location.addContent( new Element( "Country" ).setText( String.valueOf( countryName ) ) );
			 doc.getRootElement().addContent( location );
    	
			 // Atmospheric Readings
			 atmosphere.addContent( new Element( "Humidity" ).setText( String.valueOf( currentHumidity ) ) ); 
			 doc.getRootElement().addContent( atmosphere );
    	
			 // Wind Readings
			 wind.addContent( new Element( "WindSpeed" ).setText( String.valueOf( currentWindSpeed ) ) ); 
			 wind.addContent( new Element( "WindDirection" ).setText( String.valueOf( currentWindDirection ) ) );
			 doc.getRootElement().addContent( wind );
    	
			 // Astronomy readings
			 astronomy.addContent( new Element( "Sunrise" ).setText( String.valueOf( sunriseTime ) ) ); 
			 astronomy.addContent( new Element( "Sunset" ).setText( String.valueOf( sunsetTime ) ) ); 
			 doc.getRootElement().addContent( astronomy );
    	
			 // Current Weather
			 current.addContent( new Element( "Condition" ).setText( 
					 UtilityMethod.toProperCase( currentConditions ) ) );
			 current.addContent( new Element( "Temperature" ).setText( String.valueOf( currentTemperature ) ) ); 
			 current.addContent( new Element( "FeelsLike" ).setText( String.valueOf( currentFeelsLikeTemperature ) ) );
			 current.addContent( new Element( "HighTemperature" ).setText( String.valueOf( currentHigh ) ) ); 
			 current.addContent( new Element( "LowTemperature" ).setText( String.valueOf( currentLow ) ) ); 
			 doc.getRootElement().addContent( current );

			 Element forecastList = new Element( "DailyForecast" );

			 // Five Day Forecast                
			 for ( FiveDayForecast forecast : fiveDayForecast )
			 {

				 Element forecastData = new Element( "DayForecast" );
					 forecastData.addContent( new Element( "Date" ).setText( forecast.getForecastDate().toString() ) );
					 forecastData.addContent( new Element( "Condition" ).setText( UtilityMethod.toProperCase(
						forecast.getForecastCondition() ) ) );
					 forecastData.addContent( new Element( "LowTemperature" ).setText( forecast.getForecastLowTemp() ) );
					 forecastData.addContent( new Element( "HighTemperature" ).setText( forecast.getForecastHighTemp() ) );
				 forecastList.addContent( forecastData );
			 }// end of for each loop

			 doc.getRootElement().addContent( forecastList );

			 XMLOutputter xmlOutput = new XMLOutputter();
    		
			 // display nice nice
			 xmlOutput.setFormat( Format.getPrettyFormat() );
			 xmlOutput.output( doc, new FileWriter( wxData ) );
			 
			 UtilityMethod.logMessage( UtilityMethod.LogLevel.INFO, providerName + "'s weather data was stored locally!",
					 TAG + "::saveCurrentWeatherXML" );
	    				
		 }// end of try block
		 catch ( IOException e )
		 {
			 UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
		        TAG + "::saveCurrentWeatherXML [line: " +
		        UtilityMethod.getExceptionLineNumber( e )  + "]" );
		 }// end of catch block
		 
		 return true;
    }// end of method saveCurrentWeatherXML	 
}// end of class WeatherDataXMLService