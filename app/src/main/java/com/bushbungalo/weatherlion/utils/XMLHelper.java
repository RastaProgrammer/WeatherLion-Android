package com.bushbungalo.weatherlion.utils;

import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.model.CityData;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Paul O. Patterson
 * <br />
 * <b style="margin-left:-40px">Date Created:</b>
 * <br />
 * 11/21/17
 * <br />
 * <b style="margin-left:-40px">Updates:</b><br />
 * <ul>
 * 		<li>06/29/19 - Minor code fixes.</li>
 * </ul>
 */

public class XMLHelper
{
	public static String TAG = "XMLHelper";

	// package-private variable
	static final String PREVIOUSLY_FOUND_CITIES_XML =
			WeatherLionApplication.getAppContext().getFileStreamPath( "previous_cities.xml" ).toString();

	public XMLHelper()
	{
	}// default constructor

	/**
	 * Accepts a CityData object and exports it to an XML file.
	 *
	 * @param cityData	The CityData object to be written to an XML file
	 * @return	A {@code boolean} value representing success or failure
	 */
	public static boolean exportCityDataToXML( CityData cityData )
	{
		if ( cityData != null )
		{
			try
			{
				File previousCities = new File( PREVIOUSLY_FOUND_CITIES_XML );
				Element worldCities;
				Document doc;
				Element city = new Element( "City" );

				if( !previousCities.exists() )
				{
					worldCities = new Element( "WorldCities" );
					doc = new Document( worldCities );
				}// end of if block
				else
				{
					FileInputStream fis = new FileInputStream( previousCities );
					SAXBuilder builder = new SAXBuilder();
					doc = builder.build( fis );
					fis.close();
				}// end of else block

				city.addContent( new Element( "CityName" ).setText( cityData.getCityName() ) );
				city.addContent( new Element( "CountryName" ).setText( cityData.getCountryName() ) );
				city.addContent( new Element( "CountryCode" ).setText( cityData.getCountryCode() ) );
				city.addContent( new Element( "RegionName" ).setText( cityData.getRegionName() ) );
				city.addContent( new Element( "RegionCode" ).setText( cityData.getRegionCode() ) );
				city.addContent( new Element( "TimeZone" ).setText( cityData.getTimeZone() ) );
				city.addContent( new Element( "Latitude" ).setText( String.valueOf( cityData.getLatitude() ) ) );
				city.addContent( new Element( "Longitude" ).setText( String.valueOf( cityData.getLongitude() ) ) );

				doc.getRootElement().addContent( city );

				// new XMLOutputter().output(doc, System.out);
				XMLOutputter xmlOutput = new XMLOutputter();

				// display nice nice
				xmlOutput.setFormat( Format.getPrettyFormat() );
				xmlOutput.output( doc, new FileWriter( PREVIOUSLY_FOUND_CITIES_XML ) );

				return true;
			}// end of try block
			catch ( IOException | JDOMException e )
			{
				UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
						TAG + "::exportCityDataToXML [line: " + UtilityMethod.getExceptionLineNumber(e)  + "]" );
			}// end of catch block
			// end of catch block
		}// end of if block

		return false;
	}// end of method  exportCityDataToXML

	/**
	 * Converts XML data and converts them into a list of CityData objects using the native
	 * {@code XmlPullParser} class.
	 *
	 * @return	A {@code List} containing CityData objects that were converted from XML
	 */
	@SuppressWarnings("unused")
	public static List< CityData > importCityDataFromXML()
	{
		List< CityData > cd = new ArrayList<>();

		try
		{
			boolean inItemTag = false;
			String currentTagName = "";
			CityData currentCity = null;

			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();

			parser.setInput( new StringReader(  PREVIOUSLY_FOUND_CITIES_XML ) );

			int eventType = parser.getEventType();

			while( eventType != XmlPullParser.END_DOCUMENT )
			{
				switch( eventType )
				{
					case XmlPullParser.START_TAG:
						currentTagName = parser.getName();

						if( currentTagName.equals( "City" ) )
						{
							inItemTag = true;
							currentCity = new CityData();
							cd.add( currentCity );
						}// end of if block

						break;
					case XmlPullParser.END_TAG:
						if( parser.getName().equals( "City" ) )
						{
							inItemTag = false;
						}// end of if block

						currentTagName = "";

						break;
					case XmlPullParser.TEXT:
						String nodeText = parser.getText();

						if( inItemTag )
						{
							try
							{
								switch( currentTagName )
								{
									case "CityName":
										currentCity.setCityName( nodeText );
										break;
									case "CountryName":
										currentCity.setCountryName( nodeText );
										break;
									case "CountryCode":
										currentCity.setCountryCode( nodeText );

										break;
									case "RegionName":
										currentCity.setRegionName( nodeText );

										break;
									case "RegionCode":
										currentCity.setRegionCode( nodeText );
										break;
									case "Latitude":
										currentCity.setLatitude( Float.parseFloat( nodeText ) );

										break;
									case "Longitude":
										currentCity.setLongitude( Float.parseFloat( nodeText ) );
										break;

									default:
										break;
								}// end of switch block
							}// end of try block
							catch( NumberFormatException e )
							{
								UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, e.getMessage(),
										TAG + "::importCityDataFromXML [line: " +
												UtilityMethod.getExceptionLineNumber(e)  + "]" );
							}// end of catch block
						}// end of if block

						break;
					default:
						break;
				}// end of switch block

				eventType = parser.next(); // iterate through the document
			}// end of while loop
		}// end of try block
		catch ( IOException | XmlPullParserException io )
		{
			cd = null;

			UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, io.getMessage(),
					TAG + "::importCityDataFromXML [line: " + UtilityMethod.getExceptionLineNumber(io)  + "]" );
		}// end of catch block

		return cd;
	}// end of method importCityDataFromXML

	/**
	 * Converts XML data and converts them into a list of CityData objects using the
	 * jdom-2.0.6 library.
	 *
	 * @return	A {@code List} containing CityData objects that were converted from XML
	 */
	@SuppressWarnings("unused")
	public static List< CityData > importCityDataFromXMLJDOM()
	{
		SAXBuilder builder = new SAXBuilder();
		List< CityData > cd = new ArrayList<>();

		try
		{
			// just in case the document contains unnecessary white spaces
			builder.setIgnoringElementContentWhitespace( true );

			// download the document from the URL and build it
			Document document = builder.build( PREVIOUSLY_FOUND_CITIES_XML );

			// get the root node of the XML document
			Element rootNode = document.getRootElement();

			List< Element > list = rootNode.getChildren( "City" );

			for ( int i = 0; i < list.size(); i++ )
			{
				Element node = list.get( i );

				cd.add( new CityData( node.getChildText( "CityName" ), node.getChildText( "CountryCode" ),
						node.getChildText( "RegionName" ), node.getChildText( "RegionCode" ),
						node.getChildText( "CountryName" ), Float.parseFloat( node.getChildText( "Latitude" ) ),
						Float.parseFloat( node.getChildText( "Longitude") ) )
				);

			}// end of for loop

		}// end of try block
		catch ( IOException | JDOMException io )
		{
			cd = null;

			UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE, io.getMessage(),
					TAG + "::importCityDataFromXML [line: " + UtilityMethod.getExceptionLineNumber(io)  + "]" );
		}// end of catch block

		return cd;
	}// end of method importFromXMLJOM

}// end of class XMLHelper
