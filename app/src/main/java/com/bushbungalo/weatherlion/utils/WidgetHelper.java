package com.bushbungalo.weatherlion.utils;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;

import com.bushbungalo.weatherlion.providers.LargeWeatherWidgetProvider;
import com.bushbungalo.weatherlion.providers.SmallWeatherWidgetProvider;
import com.bushbungalo.weatherlion.WeatherLionApplication;

@SuppressWarnings({"unused"})
public class WidgetHelper
{
    /**
     * Get all the widgets associated with this application
     */
    public static void getWidgetIds()
    {
        int[] largeIds = AppWidgetManager.getInstance( WeatherLionApplication.getAppContext() )
                .getAppWidgetIds( new ComponentName( WeatherLionApplication.getAppContext(),
                        LargeWeatherWidgetProvider.class ) );

        int[] smallIds = AppWidgetManager.getInstance( WeatherLionApplication.getAppContext() )
                .getAppWidgetIds( new ComponentName( WeatherLionApplication.getAppContext(),
                        SmallWeatherWidgetProvider.class ) );

        WeatherLionApplication.largeWidgetIds = largeIds;
        WeatherLionApplication.smallWidgetIds = smallIds;
    }// end of method getWidgetIds

    /**
     * Returns a string representation of the widget provider associated with a widget id
     *
     * @param widgetId  The id of the widget in question
     * @return  A {@code String } representation of the widget provider associated with a widget id
     */
    public static String getWidgetProviderName( int widgetId )
    {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(
                WeatherLionApplication.getAppContext() );
        String fullClassName = widgetManager.getAppWidgetInfo( widgetId )
                .provider.getShortClassName();

        return fullClassName.substring( fullClassName.lastIndexOf("." ) + 1 );
    }// end of method getWidgetProviderName
}// end of class WidgetHelper