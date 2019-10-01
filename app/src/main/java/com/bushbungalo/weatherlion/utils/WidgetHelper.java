package com.bushbungalo.weatherlion.utils;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;

import com.bushbungalo.weatherlion.LargeWeatherWidgetProvider;
import com.bushbungalo.weatherlion.SmallWeatherWidgetProvider;
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
}// end of class WidgetHelper