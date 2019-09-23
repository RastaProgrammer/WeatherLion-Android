package com.bushbungalo.weatherlion.utils;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;

import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.WeatherWidgetProvider;

@SuppressWarnings({"unused"})
public class WidgetHelper
{
    public static int getWidgetId()
    {
        int[] ids = AppWidgetManager.getInstance( WeatherLionApplication.getAppContext() )
                .getAppWidgetIds( new ComponentName( WeatherLionApplication.getAppContext(),
                        WeatherWidgetProvider.class ) );

        return ids.length > 0 ? ids[ 0 ] : 0; // return the first widget id if there are multiple
    }// end of method getWidgetId
}// end of class WidgetHelper