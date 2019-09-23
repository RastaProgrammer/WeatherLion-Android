package com.bushbungalo.weatherlion.database;

public class WeatherAccess
{
    public static final String DATABASE_NAME = "wak.db";
    public static final String ACCESS_KEYS = "access_keys";
    public static final String KEY_PROVIDER = "KeyProvider";
    public static final String KEY_NAME = "KeyName";
    public static final String KEY_VALUE = "KeyValue";
    public static final String HEX_VALUE = "Hex";

    public static final String SQL_CREATE =
            "CREATE TABLE " + ACCESS_KEYS + "(" +
                    KEY_PROVIDER + " TEXT, " +
                    KEY_NAME + " TEXT, " +
                    KEY_VALUE + " TEXT(64), " +
                    HEX_VALUE + " TEXT);";

    public static final String[] ALL_COLUMNS = {KEY_PROVIDER,
            KEY_NAME, KEY_VALUE, HEX_VALUE};

    public static final String SQL_DELETE = "DROP TABLE " + ACCESS_KEYS;

}// end of class WeatherAccess
