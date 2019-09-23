package com.bushbungalo.weatherlion.utils;

/*
 * Created by Paul O. Patterson on 11/16/17.
 */

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/*
 * Helper class for working with a remote server
 */
public class HttpHelper
{

    /**
     * Returns text from a URL on a web server
     *
     * @param address The address url of the web service
     * @return  A string representation of the data returned from the web service
     * @throws IOException  The exception thrown.
     */
    public static String downloadUrl( String address ) throws IOException
    {
        InputStream is = null;

        try
        {
            URL url = new URL( address );
            HttpURLConnection conn = ( HttpURLConnection ) url.openConnection();
            conn.setReadTimeout( 10000 );
            conn.setConnectTimeout( 15000 );
            conn.setRequestMethod( "GET" );
            conn.setDoInput( true );
            conn.connect();

            int responseCode = conn.getResponseCode();

            if (responseCode != 200)
            {
                throw new IOException( "Got response code " + responseCode );
            }// end of if block

            is = conn.getInputStream();
            return readStream(is);

        }// end of try block
        catch ( IOException e )
        {
            UtilityMethod.logMessage(UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                "HttpHelper::downloadUrl [line: " +
                    e.getStackTrace()[1].getLineNumber()+ "]" );
        }// end of catch block
        finally
        {
            if ( is != null )
            {
                is.close();
            }// end of if block
        }// end of finally block

        return null;
    }// end of method downloadUrl

    /**
     * Reads an InputStream and converts it to a String.
     *
     * @param stream    The input data stream
     * @return  A {@code String} representing the data read from the stream
     * @throws IOException  The exception thrown if an error occurs.
     */
    private static String readStream( InputStream stream ) throws IOException
    {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        BufferedOutputStream out = null;

        try
        {
            int length;
            out = new BufferedOutputStream(byteArray);

            while ( ( length = stream.read( buffer ) ) > 0 )
            {
                out.write( buffer, 0, length );
            }// end of while loop

            out.flush();

            return byteArray.toString();
        }// end of try block
        catch ( IOException e )
        {
           UtilityMethod.logMessage(UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                   "HttpHelper::readStream [line: " +
                           e.getStackTrace()[1].getLineNumber()+ "]" );
            e.printStackTrace();
            return null;
        }// end of catch block
        finally
        {
            if (out != null)
            {
                out.close();
            }// end of if block
        }// end of finally block
    }// end of method downloadUrl
}// end of class HttpHelper