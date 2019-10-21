package com.bushbungalo.weatherlion.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Paul O. Patterson
 * <br />
 * <b style="margin-left:-40px">Date Created:</b>
 * <br />
 * 11/21/17
 */

@SuppressWarnings("unused")
public class YahooGeoLocation
{
    public static YahooGeoLocation cityGeographicalData;
    private Query query;

    public Query getQuery()
    {
        return query;
    }

    public void setQuery(Query query)
    {
        this.query = query;
    }

    //no-argument constructor
    public YahooGeoLocation()
    {
    }// default constructor

    public class Query
    {
        private String count;
        private String created;
        private Results results;

        public String getCount()
        {
            return count;
        }

        public void setCount(String count)
        {
            this.count = count;
        }

        public String getCreated()
        {
            return created;
        }

        public void setCreated(String created)
        {
            this.created = created;
        }

        public Results getResults()
        {
            return results;
        }

        public void setResults(Results results)
        {
            this.results = results;
        }

        public class Results
        {
            private List<Place> place = null;

            public List<Place> getPlace()
            {
                return place;
            }

            public void setPlace(List<Place> place)
            {
                this.place = place;
            }

            public class Place
            {
                private String lang;
                private String xmlns;
                private String yahoo;
                private String uri;
                private long woeid;
                private PlaceTypeName placeTypeName;
                private String name;
                private Country country;
                private Admin1 admin1;
                private Admin2 admin2;
                private Admin3 admin3;
                private Locality1 locality1;
                private Locality2 locality2;
                private Postal postal;
                private Centroid centroid;
                private BoundingBox boundingBox;
                private int areaRank;
                private int popRank;
                private TimeZone timezone;

                public String getLang()
                {
                    return lang;
                }

                public void setLang(String lang)
                {
                    this.lang = lang;
                }

                public String getXmlns()
                {
                    return xmlns;
                }

                public void setXmlns(String xmlns)
                {
                    this.xmlns = xmlns;
                }

                public String getYahoo()
                {
                    return yahoo;
                }

                public void setYahoo(String yahoo)
                {
                    this.yahoo = yahoo;
                }

                public String getUri()
                {
                    return uri;
                }

                public void setUri(String uri)
                {
                    this.uri = uri;
                }

                public long getWoeid()
                {
                    return woeid;
                }

                public void setWoeid(long woeid)
                {
                    this.woeid = woeid;
                }

                public PlaceTypeName getPlaceTypeName()
                {
                    return placeTypeName;
                }

                public void setPlaceTypeName(PlaceTypeName placeTypeName)
                {
                    this.placeTypeName = placeTypeName;
                }

                public String getName()
                {
                    return name;
                }

                public void setName(String name)
                {
                    this.name = name;
                }

                public Country getCountry()
                {
                    return country;
                }

                public void setCountry(Country country)
                {
                    this.country = country;
                }

                public Admin1 getAdmin1()
                {
                    return admin1;
                }

                public void setAdmin1(Admin1 admin1)
                {
                    this.admin1 = admin1;
                }

                public Admin2 getAdmin2()
                {
                    return admin2;
                }

                public void setAdmin2(Admin2 admin2)
                {
                    this.admin2 = admin2;
                }

                public Admin3 getAdmin3()
                {
                    return admin3;
                }

                public void setAdmin3(Admin3 admin3)
                {
                    this.admin3 = admin3;
                }

                public Locality1 getLocality1()
                {
                    return locality1;
                }

                public void setLocality1(Locality1 locality1)
                {
                    this.locality1 = locality1;
                }

                public Locality2 getLocality2()
                {
                    return locality2;
                }

                public void setLocality2(Locality2 locality2)
                {
                    this.locality2 = locality2;
                }

                public Postal getPostal()
                {
                    return postal;
                }

                public void setPostal(Postal postal)
                {
                    this.postal = postal;
                }

                public Centroid getCentroid()
                {
                    return centroid;
                }

                public void setCentroid(Centroid centroid)
                {
                    this.centroid = centroid;
                }

                public BoundingBox getBoundingBox()
                {
                    return boundingBox;
                }

                public void setBoundingBox(BoundingBox boundingBox)
                {
                    this.boundingBox = boundingBox;
                }

                public int getAreaRank()
                {
                    return areaRank;
                }

                public void setAreaRank(int areaRank)
                {
                    this.areaRank = areaRank;
                }

                public int getPopRank()
                {
                    return popRank;
                }

                public void setPopRank(int popRank)
                {
                    this.popRank = popRank;
                }

                public TimeZone getTimezone()
                {
                    return timezone;
                }

                public void setTimezone(TimeZone timezone)
                {
                    this.timezone = timezone;
                }

                public class PlaceTypeName
                {
                    private int code;
                    private String content;

                    public int getCode()
                    {
                        return code;
                    }

                    public void setCode(int code)
                    {
                        this.code = code;
                    }

                    public String getContent()
                    {
                        return content;
                    }

                    public void setContent(String content)
                    {
                        this.content = content;
                    }
                }// end of class Units

                public class Country
                {
                    private String country;
                    private String code;
                    private String type;
                    private long woeid;
                    private String content;

                    public String getCountry()
                    {
                        return country;
                    }

                    public void setCountry(String country)
                    {
                        this.country = country;
                    }

                    public String getCode()
                    {
                        return code;
                    }

                    public void setCode(String code)
                    {
                        this.code = code;
                    }

                    public String getType()
                    {
                        return type;
                    }

                    public void setType(String type)
                    {
                        this.type = type;
                    }

                    public long getWoeid()
                    {
                        return woeid;
                    }

                    public void setWoeid(long woeid)
                    {
                        this.woeid = woeid;
                    }

                    public String getContent()
                    {
                        return content;
                    }

                    public void setContent(String content)
                    {
                        this.content = content;
                    }
                }// end of class Location

                public class Admin1
                {
                    private String code;
                    private String type;
                    private long woeid;
                    private String content;

                    public String getCode()
                    {
                        return code;
                    }

                    public void setCode(String code)
                    {
                        this.code = code;
                    }

                    public String getType()
                    {
                        return type;
                    }

                    public void setType(String type)
                    {
                        this.type = type;
                    }

                    public long getWoeid()
                    {
                        return woeid;
                    }

                    public void setWoeid(long woeid)
                    {
                        this.woeid = woeid;
                    }

                    public String getContent()
                    {
                        return content;
                    }

                    public void setContent(String content)
                    {
                        this.content = content;
                    }
                }// end of class Wind

                public class Admin2
                {
                    private String code;
                    private String type;
                    private long woeid;
                    private String content;

                    public String getCode()
                    {
                        return code;
                    }

                    public void setCode(String code)
                    {
                        this.code = code;
                    }

                    public String getType()
                    {
                        return type;
                    }

                    public void setType(String type)
                    {
                        this.type = type;
                    }

                    public long getWoeid()
                    {
                        return woeid;
                    }

                    public void setWoeid(long woeid)
                    {
                        this.woeid = woeid;
                    }

                    public String getContent()
                    {
                        return content;
                    }

                    public void setContent(String content)
                    {
                        this.content = content;
                    }
                }// end of class Wind

                public class Admin3
                {
                    private String code;
                    private String type;
                    private long woeid;
                    private String content;

                    public String getCode()
                    {
                        return code;
                    }

                    public void setCode(String code)
                    {
                        this.code = code;
                    }

                    public String getType()
                    {
                        return type;
                    }

                    public void setType(String type)
                    {
                        this.type = type;
                    }

                    public long getWoeid()
                    {
                        return woeid;
                    }

                    public void setWoeid(long woeid)
                    {
                        this.woeid = woeid;
                    }

                    public String getContent()
                    {
                        return content;
                    }

                    public void setContent(String content)
                    {
                        this.content = content;
                    }
                }// end of class Wind

                public class Locality1
                {
                    private String type;
                    private long woeid;
                    private String content;

                    public String getType()
                    {
                        return type;
                    }

                    public void setType(String type)
                    {
                        this.type = type;
                    }

                    public long getWoeid()
                    {
                        return woeid;
                    }

                    public void setWoeid(long woeid)
                    {
                        this.woeid = woeid;
                    }

                    public String getContent()
                    {
                        return content;
                    }

                    public void setContent(String content)
                    {
                        this.content = content;
                    }
                }// end of class Locality1

                public class Locality2
                {
                    private String type;
                    private long woeid;
                    private String content;

                    public String getType()
                    {
                        return type;
                    }

                    public void setType(String type)
                    {
                        this.type = type;
                    }

                    public long getWoeid()
                    {
                        return woeid;
                    }

                    public void setWoeid(long woeid)
                    {
                        this.woeid = woeid;
                    }

                    public String getContent()
                    {
                        return content;
                    }

                    public void setContent(String content)
                    {
                        this.content = content;
                    }
                }// end of class Locality2

                public class Postal
                {
                    private String type;
                    private long woeid;
                    private String content;

                    public String getType()
                    {
                        return type;
                    }

                    public void setType(String type)
                    {
                        this.type = type;
                    }

                    public long getWoeid()
                    {
                        return woeid;
                    }

                    public void setWoeid(long woeid)
                    {
                        this.woeid = woeid;
                    }

                    public String getContent()
                    {
                        return content;
                    }

                    public void setContent(String content)
                    {
                        this.content = content;
                    }
                }// end of class Postal

                public class Centroid
                {
                    private String latitude;
                    private String longitude;

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
                }// end of class Centroid

                public class BoundingBox
                {
                    private SouthWest southWest;
                    private NorthWest northWest;

                    public SouthWest getSouthWest()
                    {
                        return southWest;
                    }

                    public void setSouthWest(SouthWest southWest)
                    {
                        this.southWest = southWest;
                    }

                    public NorthWest getNorthWest()
                    {
                        return northWest;
                    }

                    public void setNorthWest(NorthWest northWest)
                    {
                        this.northWest = northWest;
                    }

                    private class SouthWest
                    {
                        private String latitude;
                        private String longitude;

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
                    }// end of class SouthWest

                    private class NorthWest
                    {
                        private String latitude;
                        private String longitude;
                        
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
                    }// end of class NorthWest
                }// end of class BoundingBox

                public class TimeZone
                {
                    private String type;
                    private long woeid;
                    private String content;

                    public String getType()
                    {
                        return type;
                    }

                    public void setType(String type)
                    {
                        this.type = type;
                    }

                    public long getWoeid()
                    {
                        return woeid;
                    }

                    public void setWoeid(long woeid)
                    {
                        this.woeid = woeid;
                    }

                    public String getContent()
                    {
                        return content;
                    }

                    public void setContent(String content)
                    {
                        this.content = content;
                    }
                }// end of class AreaRank
            }// end of class Results
        }// end of class Results
    }// end of class Query

    public static class YahooGeoLocationDeserializer implements JsonDeserializer<YahooGeoLocation>
    {
        @Override
        public YahooGeoLocation deserialize( JsonElement json, Type typeOfT, JsonDeserializationContext context )
                throws JsonParseException
        {
            YahooGeoLocation bean = new Gson().fromJson( json, YahooGeoLocation.class );
            JsonObject jsonObject = json.getAsJsonObject();

            if ( jsonObject.has( "place" ) )
            {
                JsonArray array = jsonObject.getAsJsonArray( "place" );

                if ( array != null && !array.isJsonNull() )
                {
                    bean.getQuery().getResults().place = new Gson().fromJson(
                            array, new TypeToken< ArrayList< Query.Results.Place > >() {}.getType() );
                }// end of inner if block
            }// end of outer if block
            return bean;
        }// end of method deserialize
    }// end of class YahooGeoLocationDeserializer
}// end of class YahooGeoLocation
