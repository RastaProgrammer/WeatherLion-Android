package com.bushbungalo.weatherlion.utils;

import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.model.CityData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paul O. Patterson on 11/28/17.
 */

@SuppressWarnings("unused")
public class JSONHelper
{
    public static String TAG = "JSONHelper";
    static List<CityData> cityDataList = new ArrayList<>();

    // package-private variable
    static final String PREVIOUSLY_FOUND_CITIES_JSON =
        WeatherLionApplication.getAppContext().getFileStreamPath( "previous_cities.json" ).toString();


    public static boolean exportToJSON(CityData dataItem)
    {
        DataItems cityData = new DataItems();
        cityData.setDataItem( dataItem );
        Gson gson = new Gson();
        String jsonString;

        if( cityDataList == null ) cityDataList = new ArrayList<>();

        FileOutputStream fileOutputStream = null;
        File previousCities = new File( PREVIOUSLY_FOUND_CITIES_JSON );

        // attempt to import data from local storage
        if( previousCities.exists() )
        {
            cityDataList = importFromJSON();
            cityDataList.add( dataItem );
            jsonString = gson.toJson( cityDataList );
        }// end of if block
        else
        {
            cityDataList.add( dataItem );
            jsonString = gson.toJson( cityDataList );
        }// end of else block

        // write a new file with the new list
        try
        {
            fileOutputStream = new FileOutputStream( PREVIOUSLY_FOUND_CITIES_JSON );
            fileOutputStream.write( jsonPrettify( jsonString ).getBytes() );

            return true;
        }// end of try block
        catch ( IOException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
        TAG + "::exportToJSON [line: " + UtilityMethod.getExceptionLineNumber( e ) + "]" );
        }// end of catch block
        finally
        {
            // close the file writer object
            if(fileOutputStream != null)
            {
                try
                {
                    fileOutputStream.close();
                } // end of try block
                catch (IOException e)
                {
                    e.printStackTrace();
                }// end of catch block
            }// end of if block
        }// end of finally block

        return false;
    }// end of method  exportToJSON

    static List< CityData > importFromJSON()
    {
        FileReader reader = null;

        try
        {
            File file = new File( PREVIOUSLY_FOUND_CITIES_JSON );

            // if the is a file present then it will contain a list with at least on object
            if( file.exists() )
            {
                reader = new FileReader( file );

                Gson gson = new Gson();

                // convert the file JSON into a list of objects
                cityDataList = gson.fromJson(reader, new TypeToken<List<CityData>>() {}.getType());
            }// end of if block

        }// end of try block
        catch ( FileNotFoundException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
        TAG + "::exportToJSON [line: " + UtilityMethod.getExceptionLineNumber( e ) + "]" );
        }// end of catch block
        finally
        {
            // close the file reader object
            if( reader != null )
            {
                try
                {
                    reader.close();
                } // end of try block
                catch (IOException e)
                {
                    e.printStackTrace();
                }// end of catch block
            }// end of if block
        }// end of finally block

        return cityDataList;
    }// end of method importFromJSON

    /***
     * Saves JSON data to a local file for quicker access later.
     *
     * @param jsonData	JSON data formatted as a {@code String}.
     * @param path The path where the data will reside locally.
     * @return	True/False depending on the success of the operation.
     */
    public static boolean saveToJSONFile( String jsonData, String path )
    {
        boolean fileSaved = false;
        FileOutputStream fileOutputStream = null;
        File storageFile = new File( path );

        if (jsonData != null)
        {
            if (!storageFile.exists())
            {
                try
                {
                    fileOutputStream = new FileOutputStream( path );
                    fileOutputStream.write( jsonPrettify( jsonData ).getBytes() );

                    fileSaved = true;
                }// end of try block
                catch ( IOException e )
                {
                    UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    TAG + "::saveToJSONFile [line: " +
                            UtilityMethod.getExceptionLineNumber( e )  + "]" );
                }// end of catch block
                finally
                {
                    // close the file writer object
                    if( fileOutputStream != null )
                    {
                        try
                        {
                            fileOutputStream.close();
                        } // end of try block
                        catch ( IOException e )
                        {
                            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                                    TAG + "::saveToJSONFile [line: " +
                                            UtilityMethod.getExceptionLineNumber( e )  + "]" );
                        }// end of catch block
                    }// end of if block
                }// end of finally block
            }// end of if block
        }// end of if block

        return fileSaved;

    }// end of method saveToJSONFile

    /**
     * Attempt to convert a string to a {@code JsonObject}.
     *
     * @param strJSON	A string representation of JSON data.
     * @return	A {@code JsonObject} or {@code null} if unsuccessful
     */
    private static JsonObject toJSONObject( String strJSON )
    {
        JsonObject json = null;
        JsonParser parser = new JsonParser();

        try
        {
            json = parser.parse( strJSON ).getAsJsonObject();
        }// end of try block
        catch ( Exception e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
        TAG + "::toJSONObject [line: " + UtilityMethod.getExceptionLineNumber( e )  + "]" );
        }// end of catch block

        return json;
    }// end of method toJSONObject

    /**
     * Attempt to convert a string to a {@code JsonObject}.
     * @param strJSON	A string representation of JSON data.
     * @return	A {@code JsonArray} or {@code null} if unsuccessful
     */
    public static JsonArray toJSONArray( String strJSON )
    {
        JsonArray json = null;
        JsonParser parser = new JsonParser();

        try
        {
            json = parser.parse( strJSON) .getAsJsonArray();
        }// end of try block
        catch ( Exception e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                    TAG + "::toJSONArray [line: " +
                            UtilityMethod.getExceptionLineNumber( e )  + "]" );
        }// end of catch block

        return json;
    }// end of method toJSONArray

    /**
     * Converts a {@code JsonArray} to a {@code List}.
     *
     * @param array The {@code JsonArray} to be converted
     * @return  The {@code List} representing the extracted JSON data.
     */
    public static ArrayList< String > toList( JsonArray array )
    {
        ArrayList< String > returnList = new ArrayList<>();

        if ( array != null )
        {
            for( int i = 0; i < array.size(); i++ )
            {
                returnList.add( array.get( i ).getAsString() );
            }// end of for loop
        }// end of if block

        return returnList;
    }// end of method toList

    /**
     * Returns JSON data to in a formatted (pretty) structure.
     *
     * @param jsonString	JSON data formatted as a {@code String}.
     * @return A formatted JSON {@code String}.
     * @author <a href="https://coderwall.com/kenlakoo" target="_top">kencoder</a>
     * @see <a href='https://coderwall.com/p/ab5qha/convert-json-string-to-pretty-print-java-gson' target="_top">Stack Overflow</a>
     */
    private static String jsonPrettify( String jsonString )
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String prettyJson = null;
        JsonArray jArray = toJSONArray( jsonString );
        JsonObject jObject = toJSONObject( jsonString );

        if( jArray != null )
        {
            prettyJson = gson.toJson( jArray );
        }// end of if block
        else if( jObject != null )
        {
            prettyJson = gson.toJson( jObject );
        }// end of else if block
        else
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, "The string passed is not valid JSON data.",
                    TAG + "::jsonPrettify" );
        }// end of else if block

        return prettyJson;
    }// end of method jsonPrettify

    public static class DataItems
    {
        CityData dataItems;

        CityData getDataItems()
        {
            return dataItems;
        }

        void setDataItem(CityData dataItems)
        {
            this.dataItems = dataItems;
        }
    }// end of class DataItems
}// end of class JSONHelper
