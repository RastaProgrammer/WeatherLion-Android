package com.bushbungalo.weatherlion;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author Paul O. Patterson
 * @version     1.0
 * @since       1.0
 * 
 * <p>
 * This class is responsible for preserving the user's preferences on the local disk. 
 * </p>
 * <br />
 * <b style="margin-left:-40px">Date Created:</b>
 * <br />
 * 11/21/17
 * <b style="margin-left:-40px">Updates:</b><br />
 * <ul>
 * 		<li>01/29/19 - Changed the default weather update period to 30 mins (1800000 ms).</li>
 * 		<li>05/11/19 - Removed printing stack trace errors to console for logging.</li>
 * </ul>
 */
@SuppressWarnings({"unused", "SameParameterValue", "WeakerAccess"})
public class Preference 
{
	public static final String DEFAULT_WEATHER_SOURCE = WeatherLionApplication.YR_WEATHER;
	public static final String DEFAULT_UPDATE_INTERVAL = "1800000";
	public static final boolean DEFAULT_USE_GPS = false;
	public static final String DEFAULT_WEATHER_LOCATION = "Unknown";
	public static final boolean DEFAULT_USE_METRIC = false;
	public static final String DEFAULT_ICON_SET = "MIUI";
	public static final String DEFAULT_WIDGET_BACKGROUND = "Lion";
	public static final String DEFAULT_UI_FONT = "Product Sans";
	public static final boolean DEFAULT_FIRST_RUN = true;

	private String m_provider;
	private String  m_interval;
	private String m_location;
	private boolean m_use_metric;
	private boolean m_use_system_location;	
	private String m_widget_background;	
	private String m_icon_set;
	private String m_ui_font;
	private boolean m_first_run;

	private static SharedPreferences spf;
	private static final String TAG = "Preference";

	private  WeatherLionApplication app = new WeatherLionApplication();

	Preference(){} // default constructor

	private Preference( String provider, String interval, String location,
			boolean useMetric, boolean useSystemLocation, String widgetBackground, String iconSet,
						String font, boolean firstRun )
	{
		this.m_provider = provider;
		this.m_interval = interval;
		this.m_location = location;
		this.m_use_metric = useMetric;
		this.m_use_system_location = useSystemLocation;
		this.m_widget_background = widgetBackground;
		this.m_icon_set = iconSet;
		this.m_ui_font = font;
		this.m_first_run = firstRun;
	}// end of five argument constructor

	/**
	 * @return the m_provider
	 */
	public String getProvider()
	{
		return m_provider;
	}
	
	/**
	 * @param provider the m_provider to set
	 */
	public void setProvider(String provider)
	{
		this.m_provider = provider;
	}

	/**
	 * @return the m_interval
	 */
	public String getInterval()
	{
		return m_interval;
	}

	/**
	 * @param interval the m_interval to set
	 */
	public void setInterval(String interval)
	{
		this.m_interval = interval;
	}
	
	/**
	 * @return the m_location
	 */
	public String getLocation()
	{
		return m_location;
	}
	
	/**
	 * @param location the m_location to set
	 */
	public void setLocation(String location)
	{
		this.m_location = location;
	}
	
	/**
	 * @return the m_use_metric
	 */
	public boolean getUseMetric()
	{
		return m_use_metric;
	}
	
	/**
	 * @param useMetric the m_use_metric to set
	 */
	public void setUseMetric(boolean useMetric)
	{
		this.m_use_metric = useMetric;
	}
	
	/**
	 * @return the m_system_location
	 */
	boolean getUseSystemLocation()
	{
		return m_use_system_location;
	}
	
	/**
	 * @param systemLocation the m_system_location to set
	 */
	void setUseSystemLocation(boolean systemLocation)
	{
		this.m_use_system_location = systemLocation;
	}
	
	public String getWidgetBackground() 
	{
		return m_widget_background;
	}

	public void setWidgetBackground( String m_widget_background ) 
	{
		this.m_widget_background = m_widget_background;
	}
	
	public String getIconSet() 
	{
		return m_icon_set;
	}

	public void setIconSet( String m_icon_set ) 
	{
		this.m_icon_set = m_icon_set;
	}

	public boolean getFirstRun()
	{
		return m_first_run;
	}

	public void setFirstRun( boolean m_first_run )
	{
		this.m_first_run = m_first_run;
	}

	public String getFont()
	{
		return m_ui_font;
	}

	public void setFont( String font )
	{
		this.m_ui_font = font;
	}
	
	public Preference getSavedPreferences()
	{
		spf = PreferenceManager.getDefaultSharedPreferences( WeatherLionApplication.getAppContext() );

		// get the property value and use it
		String wxProvider = spf.getString( WeatherLionApplication.WEATHER_SOURCE_PREFERENCE, DEFAULT_WEATHER_SOURCE);
		String updateInterval  = spf.getString( WeatherLionApplication.UPDATE_INTERVAL, DEFAULT_UPDATE_INTERVAL ); //default to 30 minutes
		boolean useSystemLocation = spf.getBoolean( WeatherLionApplication.USE_GPS_LOCATION_PREFERENCE, DEFAULT_USE_GPS );
		String wxLocation = spf.getString( WeatherLionApplication.CURRENT_LOCATION_PREFERENCE, DEFAULT_WEATHER_LOCATION );
		boolean useMetric = spf.getBoolean( WeatherLionApplication.USE_METRIC_PREFERENCE, DEFAULT_USE_METRIC );
		String iconSet = spf.getString( WeatherLionApplication.ICON_SET_PREFERENCE, DEFAULT_ICON_SET );
		String widgetBackground = spf.getString( WeatherLionApplication.WIDGET_BACKGROUND_PREFERENCE, DEFAULT_WIDGET_BACKGROUND );
		String uiFont = spf.getString( WeatherLionApplication.UI_FONT, DEFAULT_UI_FONT );
		boolean firstRun = spf.getBoolean( WeatherLionApplication.FIRST_RUN, DEFAULT_FIRST_RUN );
		
		return new Preference( wxProvider, updateInterval, wxLocation, useMetric, useSystemLocation,
				widgetBackground, iconSet, uiFont, firstRun );
	}// end of method getSavedPreferences
		
	/***
	 * Method that creates a default properties files
	 */
	static void createDefaultPreferencesPropertiesFile()
	{
		SharedPreferences.Editor editor =
			PreferenceManager.getDefaultSharedPreferences( WeatherLionApplication.getAppContext() ).edit();

		editor.putString( WeatherLionApplication.WEATHER_SOURCE_PREFERENCE,
				WeatherLionApplication.YR_WEATHER );
		editor.putString( WeatherLionApplication.UPDATE_INTERVAL, DEFAULT_UPDATE_INTERVAL ); //default to 30 minutes

		if( WeatherLionApplication.storedData != null )
		{
			if( WeatherLionApplication.storedData.getLocation().getCity().length() > 0 )
			{
				editor.putString( WeatherLionApplication.CURRENT_LOCATION_PREFERENCE, WeatherLionApplication.storedData.getLocation().getCity() );
			}// end of if block
		}// end of if block
		else
		{
			editor.putString( WeatherLionApplication.CURRENT_LOCATION_PREFERENCE, DEFAULT_WEATHER_LOCATION );
		}// end of else block

		editor.putBoolean( WeatherLionApplication.USE_METRIC_PREFERENCE, DEFAULT_USE_METRIC );
		editor.putBoolean( WeatherLionApplication.USE_GPS_LOCATION_PREFERENCE, DEFAULT_USE_GPS );
		editor.putString( WeatherLionApplication.ICON_SET_PREFERENCE, DEFAULT_ICON_SET );
		editor.putString( WeatherLionApplication.WIDGET_BACKGROUND_PREFERENCE, DEFAULT_WIDGET_BACKGROUND );
		editor.putString( WeatherLionApplication.UI_FONT, DEFAULT_UI_FONT );

		// since this file is now created, then first run has be accomplished and so it
		// will be set to false
		editor.putBoolean( WeatherLionApplication.FIRST_RUN, false );

		editor.apply();

		// the first run has officially been completed
		WeatherLionApplication.firstRun = false;

	}// end of method createDefaultPropertiesFile

	/**
	 * Retrieves a specific preference value.
	 *
	 * @param property	The name of the preference to be retrieved
	 */
	public String getPrefValues( String property )
	{
		spf = PreferenceManager.getDefaultSharedPreferences( WeatherLionApplication.getAppContext() );

		return spf.getString( property, null );
	}// end of method getPrefValues()

	/**
	 * Updates the value of a specific preference.
	 *
	 * @param propertyName	The name of the preference to be updated
	 * @param propertyValue The value to be assigned to the preference
	 */
	public void setPrefValues( String propertyName, String propertyValue )
	{
		SharedPreferences.Editor editor =
			PreferenceManager.getDefaultSharedPreferences( WeatherLionApplication.getAppContext() ).edit();

		// Check if propertyValue is true or false.
		// If it is none of those then it is a string value.
		if( propertyValue.equalsIgnoreCase( "true" ) )
		{
			editor.putBoolean( propertyName, true );
		}// end of if block
		else if( propertyValue.equalsIgnoreCase( "false" ) )
		{
			editor.putBoolean( propertyName, false );
		}// end of else if block
		else
		{
			editor.putString( propertyName, propertyValue );
		}// end of else block

		editor.apply();
	}// end of method setPrefValues
}// end of class Preference