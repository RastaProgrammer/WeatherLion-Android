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
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

@SuppressWarnings({"unused", "WeakerAccess"})
public class TimeZoneInfo
{
    private String countryCode;
    private String countryName;
    private float longitude;
    private float latitude;
    private String timezoneId;
    private float dstOffset;
    private float gmtOffset;
    private float rawOffset;
    private Date time;
    private Date sunrise;
    private Date sunset;

    public String getCountryCode()
    {
        return countryCode;
    }

    public void setCountryCode( String countryCode )
    {
        this.countryCode = countryCode;
    }

    public String getCountryName()
    {
        return countryName;
    }

    public void setCountryName( String countryName )
    {
        this.countryName = countryName;
    }

    public float getLongitude()
    {
        return longitude;
    }

    public void setLongitude( float longitude )
    {
        this.longitude = longitude;
    }

    public float getLatitude()
    {
        return latitude;
    }

    public void setLatitude( float latitude )
    {
        this.latitude = latitude;
    }

    public String getTimezoneId()
    {
        return timezoneId;
    }

    public void setTimezoneId( String timezoneId )
    {
        this.timezoneId = timezoneId;
    }

    public float getDstOffset()
    {
        return dstOffset;
    }

    public void setDstOffset( float dstOffset )
    {
        this.dstOffset = dstOffset;
    }

    public float getGmtOffset()
    {
        return gmtOffset;
    }

    public void setGmtOffset( float gmtOffset )
    {
        this.gmtOffset = gmtOffset;
    }

    public float getRawOffset()
    {
        return rawOffset;
    }

    public void setRawOffset( float rawOffset )
    {
        this.rawOffset = rawOffset;
    }

    public Date getTime()
    {
        return time;
    }

    public void setTime( Date time )
    {
        this.time = time;
    }

    public Date getSunrise()
    {
        return sunrise;
    }

    public void setSunrise( Date sunrise )
    {
        this.sunrise = sunrise;
    }

    public Date getSunset()
    {
        return sunset;
    }

    public void setSunset( Date sunset )
    {
        this.sunset = sunset;
    }

    public static TimeZoneInfo deserializeTimeZoneXML( String xmlData )
    {
        SAXBuilder builder = new SAXBuilder();
        Document timeZoneXML = null;

        try
        {
            InputStream xmlStream = new ByteArrayInputStream( xmlData.getBytes( StandardCharsets.UTF_8 ) );
            timeZoneXML = builder.build( xmlStream );
        }// end of try block
        catch ( IOException | JDOMException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    "::deserializeTimeZoneXML [line: " +
                            e.getStackTrace()[1].getLineNumber()+ "]" );
        }// end of catch block

        TimeZoneInfo timeZoneInfo = new TimeZoneInfo();

        // just in case the document contains unnecessary white spaces
        builder.setIgnoringElementContentWhitespace( true );

        // get the root node of the XML document
        Element rootNode = Objects.requireNonNull( timeZoneXML ).getRootElement();
        Element timezone = rootNode.getChild( "timezone" );
        Element countryCode = timezone.getChild( "countryCode" );
        Element countryName = timezone.getChild( "countryName" );
        Element lat = timezone.getChild( "lat" );
        Element lng = timezone.getChild( "lng" );
        Element timezoneId = timezone.getChild( "timezoneId" );
        Element dstOffset = timezone.getChild( "dstOffset" );
        Element gmtOffset = timezone.getChild( "gmtOffset" );
        Element rawOffset = timezone.getChild( "rawOffset" );
        Element time = timezone.getChild( "time" );
        Element sunrise = timezone.getChild( "sunrise" );
        Element sunset = timezone.getChild( "sunset" );
        DateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm",
                Locale.ENGLISH );

        timeZoneInfo.setCountryCode( countryCode.getText() );
        timeZoneInfo.setCountryName( countryName.getText() );
        timeZoneInfo.setLatitude( Float.parseFloat( lat.getText() ) );
        timeZoneInfo.setLongitude( Float.parseFloat( lng.getText() ) );
        timeZoneInfo.setTimezoneId( timezoneId.getText() );
        timeZoneInfo.setDstOffset( Float.parseFloat( dstOffset.getText() ) );
        timeZoneInfo.setGmtOffset( Float.parseFloat( gmtOffset.getText() ) );
        timeZoneInfo.setRawOffset( Float.parseFloat( rawOffset.getText() ) );

        timeZoneInfo.time = null;
        timeZoneInfo.sunrise = null;
        timeZoneInfo.sunset = null;

        try
        {
            timeZoneInfo.setTime( dateFormat.parse( time.getText() ) );
            timeZoneInfo.setSunrise( dateFormat.parse( sunrise.getText() ) );
            timeZoneInfo.setSunset( dateFormat.parse( sunset.getText() ) );
        }// end of try block
        catch ( ParseException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, "Bad Yahoo data: " + e.getMessage(),
                    "TimeZoneInfo::deserializeTimeZoneXML [line: " +
                            e.getStackTrace()[1].getLineNumber()+ "]" );
        }// end of catch block

        return timeZoneInfo;
    }// end of method deserializeTimeZoneXML
}// end of class TimeZoneInfo
