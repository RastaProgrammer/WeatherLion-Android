package com.bushbungalo.weatherlion.model;

import com.google.gson.Gson;

import java.util.List;

/**
 * @author Paul O. Patterson
 * <br />
 * <b style="margin-left:-40px">Date Created:</b>
 * <br />
 * 11/16/17
 */

@SuppressWarnings({"unused"})
public class GoogleGeoLocation
{
    public static GoogleGeoLocation cityGeographicalData;

    private List<Result> results;
    private String status;

    public List<Result> getResults()
    {
        return results;
    }

    public void setResults(List<Result> results)
    {
        this.results = results;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public class Result
    {
        private List<AddressComponent> address_components;
        private String formatted_address;
        private Geometry geometry;
        private String place_id;
        private List<String> types;

        public List<AddressComponent> getAddress_components()
        {
            return address_components;
        }

        public void setAddress_components(List<AddressComponent> address_components)
        {
            this.address_components = address_components;
        }

        public String getFormatted_address()
        {
            return formatted_address;
        }

        public void setFormatted_address(String formatted_address)
        {
            this.formatted_address = formatted_address;
        }

        public Geometry getGeometry()
        {
            return geometry;
        }

        public void setGeometry(Geometry geometry)
        {
            this.geometry = geometry;
        }

        public String getPlace_id()
        {
            return place_id;
        }

        public void setPlace_id(String place_id)
        {
            this.place_id = place_id;
        }

        public List<String> getTypes()
        {
            return types;
        }

        public void setTypes(List<String> types)
        {
            this.types = types;
        }

        public class AddressComponent
        {
            private String long_name;
            private String short_name;
            private List<String> types;

            public String getLong_name()
            {
                return long_name;
            }

            public void setLong_name(String long_name)
            {
                this.long_name = long_name;
            }

            public String getShort_name()
            {
                return short_name;
            }

            public void setShort_name(String short_name)
            {
                this.short_name = short_name;
            }

            public List<String> getTypes()
            {
                return types;
            }

            public void setTypes(List<String> types)
            {
                this.types = types;
            }
        }// end of class AddressComponent

        public class Geometry
        {
            private Bound bounds;
            private Location location;
            private String location_type;
            private Viewport viewport;

            public Bound getBounds()
            {
                return bounds;
            }

            public void setBounds(Bound bounds)
            {
                this.bounds = bounds;
            }

            public Location getLocation()
            {
                return location;
            }

            public void setLocation(Location location)
            {
                this.location = location;
            }

            public String getLocation_type()
            {
                return location_type;
            }

            public void setLocation_type(String location_type)
            {
                this.location_type = location_type;
            }

            public Viewport getViewport()
            {
                return viewport;
            }

            public void setViewport(Viewport viewport)
            {
                this.viewport = viewport;
            }

            public class Bound
            {
                public class NorthEast
                {
                    private float lat;
                    private float lng;

                    public float getLat()
                    {
                        return lat;
                    }

                    public void setLat(float lat)
                    {
                        this.lat = lat;
                    }

                    public float getLng()
                    {
                        return lng;
                    }

                    public void setLng(float lng)
                    {
                        this.lng = lng;
                    }
                }// end of class North East

                public class SouthEast
                {
                    private float lat;
                    private float lng;

                    public float getLat()
                    {
                        return lat;
                    }

                    public void setLat(float lat)
                    {
                        this.lat = lat;
                    }

                    public float getLng()
                    {
                        return lng;
                    }

                    public void setLng(float lng)
                    {
                        this.lng = lng;
                    }
                }// end of class South East
            }// end of class Bound

            public class Location
            {
                private float lat;
                private float lng;

                public float getLat()
                {
                    return lat;
                }

                public void setLat(float lat)
                {
                    this.lat = lat;
                }

                public float getLng()
                {
                    return lng;
                }

                public void setLng(float lng)
                {
                    this.lng = lng;
                }
            }// end of class Location

            public class Viewport
            {
                public class NorthEast
                {
                    private float lat;
                    private float lng;

                    public float getLat()
                    {
                        return lat;
                    }

                    public void setLat(float lat)
                    {
                        this.lat = lat;
                    }

                    public float getLng()
                    {
                        return lng;
                    }

                    public void setLng(float lng)
                    {
                        this.lng = lng;
                    }
                }// end of class North East

                public class SouthEast
                {
                    private float lat;
                    private float lng;

                    public float getLat()
                    {
                        return lat;
                    }

                    public void setLat(float lat)
                    {
                        this.lat = lat;
                    }

                    public float getLng()
                    {
                        return lng;
                    }

                    public void setLng(float lng)
                    {
                        this.lng = lng;
                    }
                }// end of class South East
            }// end of class Viewport
        }// end of class Geometry
    }// end of class Result

    public static boolean DeserializeGeoJSON( String strJSON )
    {
        Gson gson = new Gson();
        cityGeographicalData = gson.fromJson( strJSON, GoogleGeoLocation.class );

        return cityGeographicalData != null;

    }// end of method DeserializeJSON
}// end of class GoogleGeoLocation