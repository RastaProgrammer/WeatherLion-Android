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
 * 01/21/19
 */
@SuppressWarnings({"unused"})
public class HereGeoLocation
{
    public static HereGeoLocation cityGeographicalData;
    private Response response;
    
    public Response getResponse()
    {
        return response;
    }// end of method getResponse 

    public void setResponse( Response response )
    {
        this.response = response;
    }// end of method setResponse

    //no-argument constructor
    public HereGeoLocation()
    {
    }// default constructor
    
    public class Response
    {
        private MetaInfo meta;
        private View view;
        
        public MetaInfo getMeta()
        {
			return meta;
		}// end of method getMeta

		public void setMeta( MetaInfo meta ) 
		{
			this.meta = meta;
		}// end of method setMeta

		public View getView() 
		{
			return view;
		}// end of method  getView

		public void setView( View view )
		{
			this.view = view;
		}// end of method setView

		public class MetaInfo
        {
        	private String timestamp;
        	
        	public void setTimestamp( String timestamp ) 
        	{
        		this.timestamp = timestamp;
        	}// end of method setTimestamp
        	
        	public String getTimestamp() 
        	{
        		return timestamp;
        	}// end of method setTimestamp
        }// end of class MetaInfo
        
        public class View
        {
        	private String _type;
        	private int viewID;
        	private List<Result> result = null;
        	
        	public String getType() 
        	{
				return _type;
			}// end of method getType

			public void setType( String _type )
			{
				this._type = _type;
			}// end of method setType

			public int getViewID() 
			{
				return viewID;
			}// end of method getViewID

			public void setViewID( int viewID )
			{
				this.viewID = viewID;
			}// end of method setViewID

			public List<Result> getResult()
			{
				return result;
			}// end of method getResult

			public void setResult( List<Result> result )
			{
				this.result = result;
			}// end of method setResult

			public class Result
        	{
        		private int relevance;
        		private String matchLevel;
        		private MatchQuality matchQuality;
        		private Location location;       
        		
        		public int getRelevance()
        		{
					return relevance;
				}// end of method getRelevance

				public void setRelevance( int relevance ) 
				{
					this.relevance = relevance;
				}// end of method setRelevance
        		
				public String getMatchLevel()
				{
					return matchLevel;
				}// end of method getMatchLevel

				public void setMatchLevel( String matchLevel )
				{
					this.matchLevel = matchLevel;
				}// end of method setMatchLevel
				
        		public MatchQuality getMatchQuality()
        		{
					return matchQuality;
				}// end of method getMatchQuality

				public void setMatchQuality( MatchQuality matchQuality )
				{
					this.matchQuality = matchQuality;
				}// end of method setMatchQuality

				public Location getLocation() 
				{
					return location;
				}// end of method getLocation

				public void setLocation( Location location )
				{
					this.location = location;
				}// end of method setLocation	

				public class MatchQuality
        		{
        			private int state;
        			private int district;
        			
					public int getState() 
					{
						return state;
					}// end of method getState
					
					public void setState( int state ) 
					{
						this.state = state;
					}// end of method setState
					
					public int getDistrict() 
					{
						return district;
					}// end of method getDistrict
					
					public void setDistrict( int district )
					{
						this.district = district;
					}// end of method setDistrict
        			
        		}// end of class MatchQuality 
        		
        		public class Location
        		{
        			private String locationID;
        			private String locationType;
        			private DisplayPosition displayPosition;
        			private NavigationPosition navigationPosition;
            		private MapView mapView;
            		private Address address;
        			
        			public String getLocationID() 
        			{
						return locationID;
					}// end of method getLocationID

					public void setLocationID( String locationID ) 
					{
						this.locationID = locationID;
					}// end of method setLocationID

					public String getLocationType() 
					{
						return locationType;
					}// end of method getLocationType

					public void setLocationType( String locationType ) 
					{
						this.locationType = locationType;
					}// end of method setLocationType

					public DisplayPosition getDisplayPosition()
					{
						return displayPosition;
					}// end of method getDisplayPosition

					public void setDisplayPosition( DisplayPosition displayPosition )
					{
						this.displayPosition = displayPosition;
					}// end of method setDisplayPosition

					public NavigationPosition getNavigationPosition() 
					{
						return navigationPosition;
					}// end of method getNavigationPosition

					public void setNavigationPosition( NavigationPosition navigationPosition ) 
					{
						this.navigationPosition = navigationPosition;
					}// end of method setNavigationPosition

					public MapView getMapView() 
					{
						return mapView;
					}// end of method getMapView

					public void setMapView( MapView mapView ) 
					{
						this.mapView = mapView;
					}// end of method setMapView

					public Address getAddress() 
					{
						return address;
					}// end of method getAddress

					public void setAddress( Address address )
					{
						this.address = address;
					}// end of method setAddress

					public class DisplayPosition
        			{
        				private float latitude;
        				private float longitude;
        				
						public float getLatitude() 
						{
							return latitude;
						}// end of method getLatitude
						
						public void setLatitude( float latitude )
						{
							this.latitude = latitude;
						}// end of method setLatitude
						
						public float getLongitude() 
						{
							return longitude;
						}// end of method getLongitude
						
						public void setLongitude( float longitude )
						{
							this.longitude = longitude;
						}// end of method setLongitude
        				
        			}// end of class DisplayPosition
        			
        			public class NavigationPosition
        			{
        				private float latitude;
        				private float longitude;
        				
						public float getLatitude() 
						{
							return latitude;
						}// end of method getLatitude
						
						public void setLatitude( float latitude )
						{
							this.latitude = latitude;
						}// end of method setLatitude
						
						public float getLongitude() 
						{
							return longitude;
						}// end of method getLongitude
						
						public void setLongitude( float longitude )
						{
							this.longitude = longitude;
						}// end of method setLongitude
        				
        			}// end of class NavigationPosition
        			
        			public class MapView
        			{
        				public class TopLeft
        				{
        					private float latitude;
            				private float longitude;
            				
    						public float getLatitude() 
    						{
    							return latitude;
    						}// end of method getLatitude
    						
    						public void setLatitude( float latitude )
    						{
    							this.latitude = latitude;
    						}// end of method setLatitude
    						
    						public float getLongitude() 
    						{
    							return longitude;
    						}// end of method getLongitude
    						
    						public void setLongitude( float longitude )
    						{
    							this.longitude = longitude;
    						}// end of method setLongitude
            				
        				}// end of class TopLeft
        				
        				public class BottomLeft
        				{
        					private double latitude;
            				private double longitude;
            				
            				public double getLatitude() 
    						{
    							return latitude;
    						}// end of method getLatitude 
    						
    						public void setLatitude( double latitude )
    						{
    							this.latitude = latitude;
    						}// end of method setLatitude
    						
    						public double getLongitude() 
    						{
    							return longitude;
    						}// end of method getLongitude
    						
    						public void setLongitude( double longitude )
    						{
    							this.longitude = longitude;
    						}// end of method setLongitude
            				
        				}// end of class BottomLeft
        			}// end of function MapView
        			
        			public class Address
        			{
        				private String label;
        				private String country;
        				private String state;
        				private String county;
        				private String city;
        				private String district;
        				private String postalCode;
        				private List<AdditionalData> additionalData = null;
        				
						public String getLabel()
						{
							return label;
						}// end of method getLabel
						
						public void setLabel( String label ) 
						{
							this.label = label;
						}// end of method setLabel
						
						public String getCountry() 
						{
							return country;
						}// end of method getCountry
						
						public void setCountry( String country ) 
						{
							this.country = country;
						}// end of method setCountry
						
						public String getState() 
						{
							return state;
						}// end of method getState
						
						public void setState( String state ) 
						{
							this.state = state;
						}// end of method setState
						
						public String getCounty() 
						{
							return county;
						}// end of method getCounty
						
						public void setCounty( String county ) 
						{
							this.county = county;
						}// end of method setCounty
						
						public String getCity()
						{
							return city;
						}// end of method getCity
						
						public void setCity( String city )
						{
							this.city = city;
						}// end of method setCity
						
						public String getDistrict() 
						{
							return district;
						}// end of method getDistrict
						
						public void setDistrict( String district )
						{
							this.district = district;
						}// end of method setDistrict
						
						public String getPostalCode()
						{
							return postalCode;
						}// end of method getPostalCode
						
						public void setPostalCode( String postalCode )
						{
							this.postalCode = postalCode;
						}// end of method setPostalCode
						
						public List<AdditionalData> getAdditionalData()
						{
							return additionalData;
						}// end of method getAdditionalData

						public void setAdditionalData( List<AdditionalData> additionalData ) 
						{
							this.additionalData = additionalData;
						}// end of method setAdditionalData

						public class AdditionalData
						{
							private String key;
							private String value;
							
							public String getKey() 
							{
								return key;
							}// end of method getKey 
							
							public void setKey( String key )
							{
								this.key = key;
							}// end of method setKey
							
							public String getValue()
							{
								return value;
							}// end of method  getValue
							
							public void setValue( String value )
							{
								this.value = value;
							}// end of method  setValue
							
						}// end of class AdditionalData
        				
        			}// end of class Address
        		}// end of class Location
        	}// end of class Result
        	
        }// end of class View
        
    }// end of class Response

    public static class HereGeoLocationDeserializer implements JsonDeserializer<HereGeoLocation>
    {
        @Override
        public HereGeoLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException
        {
            HereGeoLocation bean = new Gson().fromJson( json, HereGeoLocation.class );
            JsonObject jsonObject = json.getAsJsonObject();

            if ( jsonObject.has( "Result" ) )
            {
                JsonArray array = jsonObject.getAsJsonArray( "Result" );

                if ( array != null && !array.isJsonNull() )
                {
                    List<Response.View.Result> place = new Gson().fromJson( 
                    		array, new TypeToken< ArrayList<Response.View.Result> >() {}.getType() );
                    bean.getResponse().getView().setResult( place );
                }// end of inner if block
            }// end of outer if block
            return bean;
        }// end of method deserialize
    }// end of class HereGeoLocationDeserializer
}// end of class HereGeoLocation
