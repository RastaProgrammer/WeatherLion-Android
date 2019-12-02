package com.bushbungalo.weatherlion.utils;

/*
 * Created by Paul O. Patterson on 11/16/17.
 */

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.bushbungalo.weatherlion.WeatherLionApplication;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.ByteString;

/*
 * Helper class for working with a remote server
 */
public class HttpHelper
{
    public static final String WEB_SERVICE_DATA_MESSAGE = "WebServiceMessage";
    public static final String WEB_SERVICE_DATA_PAYLOAD = "WebServicePayload";

    private static void broadcastServiceResponse( String webServiceData )
    {
        Intent messageIntent = new Intent( WEB_SERVICE_DATA_MESSAGE );
        messageIntent.putExtra( WEB_SERVICE_DATA_PAYLOAD, ( webServiceData ).trim() );
        LocalBroadcastManager manager =
                LocalBroadcastManager.getInstance( WeatherLionApplication.getAppContext() );
        manager.sendBroadcast( messageIntent );
    }

    /**
     * Returns text from a URL on a web server
     *
     * @param address The address url of the web service
     * @return  A string representation of the data returned from the web service
     * @throws IOException  The exception thrown.
     */
    public static String downloadUrl( String address, boolean weatherData ) throws IOException
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

            if ( responseCode != 200 && responseCode != 401 )
            {
                throw new IOException( "Got response code " + responseCode );
            }// end of if block
            else if( responseCode == 401 )
            {
                // Calling from a Non-UI Thread
                Handler handler = new Handler( Looper.getMainLooper() );

                handler.post( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        UtilityMethod.butteredToast( WeatherLionApplication.getAppContext(),
                        "The credentials supplied for " +
                                WeatherLionApplication.storedData.getProvider() +
                                " are invalid!",
                                2, Toast.LENGTH_LONG );
                    }
                });
            }// end of else if block

            is = conn.getInputStream();
            String data = readStream( is );

            /* the weather data might take a long time to be returned so it should be
            broadcasted when received */
            if( weatherData )
            {
                broadcastServiceResponse( data );
            }// end of if block

            // return for much quicker data responses
            return data;
        }// end of try block
        catch ( IOException e )
        {
            broadcastServiceResponse( WeatherLionApplication.EMPTY_JSON );

            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
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

    /***
     * Yahoo! Developers Network 2019 documentation
     * url: https://developer.yahoo.com/weather/documentation.html#java
     */
    public static void getYahooWeatherData( String wxCity, String yahooAppId, String yahooConsumerKey, String yahooConsumerSecret ) throws Exception
    {
        wxCity = wxCity.replace( " ", "%2B" ).replace( ",", "%2C" ); // add URL Encoding for two characters
        final String url = "https://weather-ydn-yql.media.yahoo.com/forecastrss";

        long timestamp = new Date().getTime() / 1000;
        byte[] nonce = new byte[ 32 ];
        Random rand = new Random();
        rand.nextBytes( nonce );
        //String oauthNonce = new String( nonce ).replaceAll( "[^a-zA-Z0-9]", "" );
        String oauthNonce = ByteString.of( nonce ).base64().replaceAll( "\\W", "" );

        List<String> parameters = new ArrayList<>();
        parameters.add( "oauth_consumer_key=" + yahooConsumerKey );
        parameters.add( "oauth_nonce=" + oauthNonce );
        parameters.add( "oauth_signature_method=HMAC-SHA1" );
        parameters.add( "oauth_timestamp=" + timestamp );
        parameters.add( "oauth_version=1.0" );
        // Make sure value is encoded
        parameters.add( "location=" + wxCity );
        parameters.add( "format=json" );
        Collections.sort( parameters );

//        StringBuffer parametersList = new StringBuffer();
        StringBuilder parametersList = new StringBuilder();

        for ( int i = 0; i < parameters.size(); i++ )
        {
            parametersList.append( String.format( "%s%s", ( ( i > 0 ) ? "&" : "" ), parameters.get( i )  ) );
        }// end of for loop

        String signatureString = String.format( "GET&%s&%s",
                URLEncoder.encode( url, "UTF-8" ),
                URLEncoder.encode( parametersList.toString(), "UTF-8" ) );

        String signature;

        try
        {
            SecretKeySpec signingKey =
                    new SecretKeySpec( ( yahooConsumerSecret + "&" ).getBytes(), "HmacSHA1" );
            Mac mac = Mac.getInstance( "HmacSHA1" );
            mac.init( signingKey );
            byte[] rawHMAC = mac.doFinal( signatureString.getBytes() );
            Base64.Encoder encoder = Base64.getEncoder();
            signature = encoder.encodeToString( rawHMAC );
        }// end of try block
        catch ( Exception e )
        {
            broadcastServiceResponse( WeatherLionApplication.EMPTY_JSON );
            return;
        }// end of catch block

        String authorizationLine = "OAuth " +
                "oauth_consumer_key=\"" + yahooConsumerKey + "\", " +
                "oauth_nonce=\"" + oauthNonce + "\", " +
                "oauth_timestamp=\"" + timestamp + "\", " +
                "oauth_signature_method=\"HMAC-SHA1\", " +
                "oauth_signature=\"" + signature + "\", " +
                "oauth_version=\"1.0\"";

        // OkHttp approach
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(
                URI.create( url + "?location=" + wxCity + "&format=json" ).toString() )
                .header( "Authorization", authorizationLine )
                .header( "X-Yahoo-App-Id", yahooAppId )
                .header( "Content-Type", "application/json" )
                .build();
        Response response = client.newCall( request ).execute();

        String dataReturned;

        if( response.isSuccessful() )
        {
            dataReturned = Objects.requireNonNull( response.body() ).string();
        }// end of if block
        else
        {
            dataReturned = WeatherLionApplication.EMPTY_JSON;
        }// end of else block

        broadcastServiceResponse( dataReturned );
    }// end of method getYahooWeatherData

    /**
     * Reads an InputStream and converts it to a String.
     *
     * @param stream    The input data stream
     * @return  A {@code String} representing the data read from the stream
     * @throws IOException  The exception thrown if an error occurs.
     */
    private static String readStream( InputStream stream ) throws IOException
    {
        byte[] buffer = new byte[ 1024 ];
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        BufferedOutputStream out = null;

        try
        {
            int length;
            out = new BufferedOutputStream( byteArray );

            while ( ( length = stream.read( buffer ) ) > 0 )
            {
                out.write( buffer, 0, length );
            }// end of while loop

            out.flush();

            return byteArray.toString();
        }// end of try block
        catch ( IOException e )
        {
           UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
                   "HttpHelper::readStream [line: " +
                           e.getStackTrace()[1].getLineNumber()+ "]" );
            e.printStackTrace();
            return null;
        }// end of catch block
        finally
        {
            if ( out != null )
            {
                out.close();
            }// end of if block
        }// end of finally block
    }// end of method readStream
}// end of class HttpHelper