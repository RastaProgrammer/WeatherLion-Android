package com.bushbungalo.weatherlion.database;

public class WorldCities
{
    public static final String WORLD_CITIES = "world_cities";
    public static final String CITY_NAME = "CityName";
    public static final String COUNTRY_NAME = "CountryName";
    public static final String COUNTRY_CODE = "CountryCode";
    public static final String REGION_NAME = "RegionName";
    public static final String REGION_CODE = "RegionCode";
    public static final String LATITUDE = "Latitude";
    public static final String LONGITUDE = "Longitude";
    public static final String DATE_ADDED = "DateAdded";

    public static final String SQL_CREATE =
            "CREATE TABLE " + WORLD_CITIES + "(" +
                    CITY_NAME + " TEXT, " +
                    COUNTRY_NAME + " TEXT, " +
                    COUNTRY_CODE + " TEXT(2), " +
                    REGION_NAME + " TEXT, " +
                    REGION_CODE + " TEXT(2), " +
                    LATITUDE + " REAL, " +
                    LONGITUDE + " REAL, " +
                    DATE_ADDED + " TEXT);";

    public static final String SQL_DELETE = "DROP TABLE " + WORLD_CITIES;

    public static final String[] ALL_COLUMNS = {CITY_NAME, COUNTRY_NAME, COUNTRY_CODE,
            REGION_NAME, REGION_CODE, LATITUDE, LONGITUDE, DATE_ADDED};
}// end of class WorldCities
