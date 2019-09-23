package com.bushbungalo.weatherlion.model;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * @author Paul O. Patterson
 * <br />
 * <b style="margin-left:-40px">Date Created:</b>
 * <br />
 * 03/20/19
 */
@SuppressWarnings({"unused"})
public class GeoNamesGeoLocation 
{
	public static GeoNamesGeoLocation cityGeographicalData;
	
	private int totalResultsCount;
	private List< GeoNames > geonames = null;
	
	public int getTotalResultsCount()
	{
		return totalResultsCount;
	}

	public void setTotalResultsCount( int totalResultsCount )
	{
		this.totalResultsCount = totalResultsCount;
	}

	public List< GeoNames > getGeoNames()
	{
		return geonames;
	}

	public void setGeoNames( List< GeoNames > geonames )
	{
		this.geonames = geonames;
	}
	
	public class GeoNames
	{
		private String adminCode1;
		private String lng;
		private Long geonameId;
		private String toponymName;
		private String countryId;
		private String fcl;
		private Long population;
		private String countryCode;
		private String name;
		private String fclName;
		private AdminCodes1 adminCodes1;
		private String countryName;
		private String fcodeName;
		private String adminName1;
		private String lat;
		private String fcode;
		
		public String getAdminCode1()
		{
			return adminCode1;
		}

		public void setAdminCode1( String adminCode1 )
		{
			this.adminCode1 = adminCode1;
		}

		public Float getLongitude()
		{
			return Float.parseFloat( lng );
		}

		public void setLongitude( Float lng )
		{
			this.lng = String.valueOf( lng );
		}

		public Long getGeonameId()
		{
			return geonameId;
		}

		public void setGeonameId( Long geonameId )
		{
			this.geonameId = geonameId;
		}

		public String getToponymName()
		{
			return toponymName;
		}

		public void setToponymName( String toponymName )
		{
			this.toponymName = toponymName;
		}

		public String getCountryId()
		{
			return countryId;
		}

		public void setCountryId( String countryId )
		{
			this.countryId = countryId;
		}

		public String getFcl()
		{
			return fcl;
		}

		public void setFcl( String fcl )
		{
			this.fcl = fcl;
		}

		public long getPopulation()
		{
			return population;
		}

		public void setPopulation( Long population )
		{
			this.population = population;
		}

		public String getCountryCode()
		{
			return countryCode;
		}

		public void setCountryCode( String countryCode )
		{
			this.countryCode = countryCode;
		}

		public String getName()
		{
			return name;
		}

		public void setName( String name )
		{
			this.name = name;
		}

		public String getFclName()
		{
			return fclName;
		}

		public void setFclName( String fclName )
		{
			this.fclName = fclName;
		}

		public AdminCodes1 getAdminCodes1()
		{
			return adminCodes1;
		}

		public void setAdminCodes1( AdminCodes1 adminCodes1 )
		{
			this.adminCodes1 = adminCodes1;
		}

		public String getCountryName()
		{
			return countryName;
		}

		public void setCountryName( String countryName )
		{
			this.countryName = countryName;
		}

		public String getFcodeName() 
		{
			return fcodeName;
		}

		public void setFcodeName( String fcodeName )
		{
			this.fcodeName = fcodeName;
		}

		public String getAdminName1()
		{
			return adminName1;
		}

		public void setAdminName1( String adminName1 )
		{
			this.adminName1 = adminName1;
		}

		public Float getLatitude() 
		{
			return Float.parseFloat( lat );
		}

		public void setLatitude( Float lat )
		{
			this.lat = String.valueOf( lat );
		}

		public String getFcode()
		{
			return fcode;
		}

		public void setFcode( String fcode )
		{
			this.fcode = fcode;
		}
		
		public class AdminCodes1
		{
			private String ISO3166_2;

			public String getISO()
			{
				return ISO3166_2;
			}

			public void setISO( String iSO3166_2 )
			{
				ISO3166_2 = iSO3166_2;
			}
		}// end of class AdminCodes1
	}// end of class GeoNames
	
	public static class GeoNamesGeoLocationDeserializer implements JsonDeserializer<GeoNamesGeoLocation>
    {
        @Override
        public GeoNamesGeoLocation deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException
        {
            GeoNamesGeoLocation bean = new Gson().fromJson( json, GeoNamesGeoLocation.class );
            JsonObject jsonObject = json.getAsJsonObject();

            if ( jsonObject.has( "geonames" ) )
            {
                JsonArray array = jsonObject.getAsJsonArray( "geonames" );

                if ( array != null && !array.isJsonNull() )
                {
                    List< GeoNames > place = new Gson().fromJson( 
                    		array, new TypeToken< ArrayList< GeoNames > >() {}.getType() );
                    bean.setGeoNames( place );
                }// end of inner if block
            }// end of outer if block
            return bean;
        }// end of method deserialize
    }// end of class GeoNamesGeoLocationDeserializer
}// end of class GeoNamesGeoLocation
