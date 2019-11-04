package com.bushbungalo.weatherlion.utils;

import com.bushbungalo.weatherlion.model.LastWeatherData;
import com.bushbungalo.weatherlion.model.LastWeatherData.WeatherData;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LastWeatherDataXmlParser
{
    private static final String PROVIDER_TAG = "Provider";
    private static final String LOCATION_TAG = "Location";
    private static final String ATMOSPHERE_TAG = "Atmosphere";
    private static final String WIND_TAG = "Wind";
    private static final String ASTRONOMY_TAG = "Astronomy";
    private static final String CURRENT_TAG = "Current";
    private static final String HOURLY_FORECAST_TAG = "HourlyForecast";
    private static final String HOUR_FORECAST_TAG = "HourForecast";
    private static final String DAILY_FORECAST_TAG = "DailyForecast";
    private static final String DAY_FORECAST_TAG = "DayForecast";

    public static LastWeatherData parseXmlData( String xmlFileData )
    {
        LastWeatherData lastDataReceived = new LastWeatherData();
        WeatherData currentWeatherData = lastDataReceived.new WeatherData();
        WeatherData.Provider lastProvider = currentWeatherData.new Provider();
        WeatherData.Location lastLocation = currentWeatherData.new Location();
        WeatherData.Atmosphere lastAtmosphere = currentWeatherData.new Atmosphere();
        WeatherData.Wind lastWind = currentWeatherData.new Wind();
        WeatherData.Astronomy lastAstronomy = currentWeatherData.new Astronomy();
        WeatherData.Current lastCurrentData = currentWeatherData.new Current();
        WeatherData.HourlyForecast lastHourlyForecast = currentWeatherData.new HourlyForecast();
        WeatherData.HourlyForecast.HourForecast hourForecast = null;
        List<WeatherData.HourlyForecast.HourForecast> lastHourlyForecastList = new ArrayList<>();
        WeatherData.DailyForecast lastDailyForecast = currentWeatherData.new DailyForecast();
        WeatherData.DailyForecast.DayForecast dayForecast = null;
        List<WeatherData.DailyForecast.DayForecast> lastForecastList = new ArrayList<>();

        List<String> dataTags = Arrays.asList(
                "WeatherData", "Provider", "Location", "Atmosphere", "Wind", "Astronomy",
                "Current", "HourlyForecast", "HourForecast", "DailyForecast", "DayForecast" );

        try
        {
            boolean inProviderTag = false;
            boolean inLocationTag = false;
            boolean inAtmosphereTag = false;
            boolean inWindTag = false;
            boolean inAstronomyTag = false;
            boolean inCurrentTag = false;
            boolean inHourlyForecastTag = false;
            boolean inHourForecastTag = false;
            boolean inDailyForecastTag = false;
            boolean inDayForecastTag = false;

            String currentTagName = "";

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput( new StringReader( xmlFileData ) );

            int eventType = parser.getEventType();

            while( eventType != XmlPullParser.END_DOCUMENT )
            {
                switch ( eventType )
                {
                    case XmlPullParser.START_TAG:
                        currentTagName = parser.getName();

                        if( dataTags.contains( currentTagName ) )
                        {
                            switch( currentTagName )
                            {
                                case PROVIDER_TAG:
                                    inProviderTag = true;
                                    break;
                                case LOCATION_TAG:
                                    inLocationTag = true;
                                    break;
                                case ATMOSPHERE_TAG:
                                    inAtmosphereTag = true;
                                    break;
                                case WIND_TAG:
                                    inWindTag = true;
                                    break;
                                case ASTRONOMY_TAG:
                                    inAstronomyTag = true;
                                    break;
                                case CURRENT_TAG:
                                    inCurrentTag = true;
                                    break;
                                case HOURLY_FORECAST_TAG:
                                    inHourlyForecastTag = true;
                                    break;
                                case HOUR_FORECAST_TAG:
                                    inHourForecastTag = true;
                                    hourForecast = lastHourlyForecast.new HourForecast();
                                    lastHourlyForecastList.add( hourForecast );
                                    break;
                                case DAILY_FORECAST_TAG:
                                    inDailyForecastTag = true;
                                    break;
                                case DAY_FORECAST_TAG:
                                    inDayForecastTag = true;
                                    dayForecast = lastDailyForecast.new DayForecast();
                                    lastForecastList.add( dayForecast );
                                    break;
                            }// end of switch block
                        }// end of if block

                        break;
                    case XmlPullParser.END_TAG:

                        switch( parser.getName() )
                        {
                            case PROVIDER_TAG:
                                inProviderTag = false;
                                break;
                            case LOCATION_TAG:
                                inLocationTag = false;
                                break;
                            case ATMOSPHERE_TAG:
                                inAtmosphereTag = false;
                                break;
                            case WIND_TAG:
                                inWindTag = false;
                                break;
                            case ASTRONOMY_TAG:
                                inAstronomyTag = false;
                                break;
                            case CURRENT_TAG:
                                inCurrentTag = false;
                                break;
                            case HOURLY_FORECAST_TAG:
                                inHourlyForecastTag = false;
                                break;
                            case HOUR_FORECAST_TAG:
                                inHourForecastTag = false;
                                break;
                            case DAILY_FORECAST_TAG:
                                inDailyForecastTag = false;
                                break;
                            case DAY_FORECAST_TAG:
                                inDayForecastTag = false;
                                break;
                        }// end of switch block

                        currentTagName = "";
                        break;
                    case XmlPullParser.TEXT:
                        String tagText = parser.getText();

                        if( inProviderTag && currentTagName != null )
                        {
                            switch ( currentTagName )
                            {
                                case "Name":
                                    lastProvider.setName( tagText );
                                    break;
                                case "Date":
                                    lastProvider.setDate( tagText );
                                    break;
                            }// end of switch block
                        }// end of if block
                        else if( inLocationTag && currentTagName != null )
                        {
                            switch ( currentTagName )
                            {
                                case "City":
                                    lastLocation.setCity( tagText );
                                    break;
                                case "Country":
                                    lastLocation.setCountry( tagText );
                                    break;
                            }// end of switch block
                        }// end of else if block
                        else if( inAtmosphereTag && currentTagName != null )
                        {
                            if( currentTagName.equals( "Humidity" ) )
                            {
                                lastAtmosphere.setHumidity( Integer.parseInt( tagText ) );
                            }// end of if block
                        }// end of else if block
                        else if( inWindTag && currentTagName != null )
                        {
                            switch ( currentTagName  )
                            {
                                case "WindDirection":
                                    lastWind.setWindDirection( tagText );
                                    break;
                                case "WindSpeed":
                                    lastWind.setWindSpeed( Float.parseFloat( tagText ) );
                                    break;
                            }// end of switch block
                        }// end of else if block
                        else if( inAstronomyTag && currentTagName != null )
                        {
                            switch ( currentTagName )
                            {
                                case "Sunrise":
                                    lastAstronomy.setSunrise( tagText );
                                    break;
                                case "Sunset":
                                    lastAstronomy.setSunset( tagText );
                                    break;
                            }// end of switch block
                        }// end of else if block
                        else if( inCurrentTag && currentTagName != null )
                        {
                            switch ( currentTagName )
                            {
                                case "Condition":
                                    lastCurrentData.setCondition( tagText );
                                    break;
                                case "Temperature":
                                    lastCurrentData.setTemperature( Math.round( Float.parseFloat( tagText )  ) );
                                    break;
                                case "FeelsLike":
                                    lastCurrentData.setFeelsLike( Math.round( Float.parseFloat( tagText )  ) );
                                    break;
                                case "HighTemperature":
                                    lastCurrentData.setHighTemperature( Math.round( Float.parseFloat( tagText )  ) );
                                    break;
                                case "LowTemperature":
                                    lastCurrentData.setLowTemperature( Math.round( Float.parseFloat( tagText )  ) );
                                    break;
                            }// end of switch block
                        }// end of else if block
                        else if( inHourlyForecastTag && inHourForecastTag && currentTagName != null )
                        {
                            switch ( currentTagName )
                            {
                                case "Time":
                                    hourForecast.setTime( tagText );
                                    break;
                                case "Condition":
                                    hourForecast.setCondition( tagText );
                                    break;
                                case "Temperature":
                                    hourForecast.setTemperature( (int) Float.parseFloat( tagText ) );
                                    break;
                            }// end of switch block
                        }// end of else if block
                        else if( inDailyForecastTag && inDayForecastTag && currentTagName != null )
                        {
                            switch ( currentTagName )
                            {
                                case "Date":
                                    dayForecast.setDate( tagText );
                                    break;
                                case "Condition":
                                    dayForecast.setCondition( tagText );
                                    break;
                                case "HighTemperature":
                                    dayForecast.setHighTemperature( (int) Float.parseFloat( tagText ) );
                                    break;
                                case "LowTemperature":
                                    dayForecast.setLowTemperature( (int) Float.parseFloat( tagText ) );
                                    break;
                            }// end of switch block
                        }// end of else if block

                        break;
                }// end of switch block

                eventType = parser.next();
            }// end of while loop

            lastDataReceived.setWeatherData( currentWeatherData );
            currentWeatherData.setProvider( lastProvider );
            currentWeatherData.setLocation( lastLocation );
            currentWeatherData.setAtmosphere( lastAtmosphere );
            currentWeatherData.setWind( lastWind );
            currentWeatherData.setAstronomy( lastAstronomy );
            currentWeatherData.setCurrent( lastCurrentData );
            currentWeatherData.setHourlyForecast( lastHourlyForecastList );
            currentWeatherData.setDailyForecast( lastForecastList );
        }// end of try block
        catch( Exception e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
        "LastWeatherDataXmlParser::parseXmlData [line: " +
                    UtilityMethod.getExceptionLineNumber( e ) + "]" );
        }// end of catch block

        return lastDataReceived;
    }// end of method parseXmlData
}// end of class LastWeatherDataXmlParser