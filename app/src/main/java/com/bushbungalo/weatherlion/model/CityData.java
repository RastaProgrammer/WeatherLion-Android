package com.bushbungalo.weatherlion.model;

import android.support.annotation.NonNull;

import java.util.Locale;

/**
 * @author Paul O. Patterson
 * <br />
 * <b style="margin-left:-40px">Date Created:</b>
 * <br />
 * 11/28/17
 */

@SuppressWarnings({"unused"})
public class CityData
{
    public static CityData currentCityData;
    public static CityData[] searchCitiesData;
	
	private String cityName;
    private String countryName;
    private String countryCode;
    private String regionName;
	private String regionCode;
    private String timeZone;
    private float longitude;
    private float latitude;
    
    public CityData()
    {
    }// end of default constructor

    public CityData(String name, String country, float latitude, float longitude)
    {
        this.cityName = name;
        this.countryName = country;
        this.latitude = latitude;
        this.longitude = longitude;
    }// end of four-argument constructor

    public CityData(String name, String country, String countryCode, String regionCode, float latitude, float longitude)
    {
        this.cityName = name;
        this.countryName = country;
        this.countryCode = countryCode;
        this.regionCode = regionCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }// end on six-argument constructor
    
    public CityData(String name, String country, String countryCode, String region,
    		 String regionCode, float latitude, float longitude)
    {
        this.cityName = name;
        this.countryName = country;
        this.countryCode = countryCode;
        this.regionName = region;
        this.regionCode = regionCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }// end on seven-argument constructor

    public String getCityName()
    {
        return cityName;
    }

    public void setCityName(String name)
    {
        this.cityName = name;
    }

    public String getCountryName()
    {
        return countryName;
    }

    public void setCountryName(String country)
    {
        this.countryName = country;
    }

    public String getCountryCode()
    {
        return countryCode;
    }

    public void setCountryCode(String countryCode)
    {
        this.countryCode = countryCode;
    }
    
    public String getRegionName() 
	{
		return regionName;
	}
	
	public void setRegionName(String region)
	{
		this.regionName = region;
	}

    public String getRegionCode()
    {
        return regionCode;
    }

    public void setRegionCode(String stateCode)
    {
        this.regionCode = stateCode;
    }

    public String getTimeZone()
    {
        return timeZone;
    }

    public void setTimeZone( String timeZone )
    {
        this.timeZone = timeZone;
    }

    public float getLongitude()
    {
        return longitude;
    }

    public void setLongitude(float longitude)
    {
        this.longitude = longitude;
    }

    public float getLatitude()
    {
        return latitude;
    }

    public void setLatitude(float latitude)
    {
        this.latitude = latitude;
    }

    @Override
    public boolean equals(Object rhs)
    {
        boolean result = false; // assume it isn't equal

        if( this == rhs )
        {
            return true;
        }// end of if block
        else if( getClass() != rhs.getClass() )
        {
            return false;
        }// end of else if block
        else
        {
            if( rhs instanceof CityData )
            {
                // valid CityData object, check the contents
                final CityData otherCity = (CityData) rhs;

                if( (this.getCityName() == null ? otherCity.getCityName() == null :
                        this.getCityName().equals(otherCity.getCityName()))
                    && (this.getCountryName() == null ? otherCity.getCountryName() == null :
                        this.getCountryName().equals(otherCity.getCountryName()))
                    && (this.getCountryCode() == null ? otherCity.getCountryCode() == null :
                        this.getCountryCode().equals(otherCity.getCountryCode()))
                    && (this.getRegionName() == null ? otherCity.getRegionName() == null :
                        this.getRegionName().equals(otherCity.getRegionName()))
                    && (this.getRegionCode() == null ? otherCity.getRegionCode() == null :
                        this.getRegionCode().equals(otherCity.getRegionCode()))
                    && this.getLatitude() == otherCity.getLatitude()
                    && this.getLongitude() == otherCity.getLongitude())
                {
                    result = true;
                }// end of if block

            }// end of if block
        }// end of else block

        return result;
    }// end of overridden method equals

    /***
	 * {@inheritDoc}
	 */
    @Override
    public @NonNull String toString()
    {
        return String.format( Locale.ENGLISH,
        "City Name: %s, Country Name: %s, Country Code: %s," +
                " Region Code: %s, Region Name: %s, Time Zone %s, Latitude: %f, and Longitude: %f", this.cityName, this.countryName,
                this.countryCode, this.regionCode, this.regionName, this.timeZone, this.latitude, this.longitude );
    }// end of overridden method equals toString
}// end of class CityData
