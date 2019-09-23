package com.bushbungalo.weatherlion.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper
{
    private static final int DB_VERSION = 1;
    private static String currentDBFile = null;

    public DBHelper( Context context, String databaseFileName )
    {
        super(context, databaseFileName, null, DB_VERSION);
        currentDBFile = databaseFileName;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        switch(currentDBFile)
        {
            case "wak.db":
                db.execSQL(WeatherAccess.SQL_CREATE);
                break;
            case "WorldCities.db":
                db.execSQL(WorldCities.SQL_CREATE);
                break;
            case "WeatherLion.db":
                break;
        }// end of switch block
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // It probably will not do this but for now...
        db.execSQL(WeatherAccess.SQL_DELETE);
        db.execSQL(WorldCities.SQL_DELETE);
        onCreate(db);
    }
}// end of class DBHelper
