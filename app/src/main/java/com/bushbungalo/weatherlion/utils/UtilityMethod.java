package com.bushbungalo.weatherlion.utils;

/*
  Created by Paul O. Patterson on 11/21/17.
 */

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bushbungalo.weatherlion.R;
import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.database.DBHelper;
import com.bushbungalo.weatherlion.database.WorldCities;
import com.bushbungalo.weatherlion.model.CityData;
import com.bushbungalo.weatherlion.model.HereGeoLocation;
import com.bushbungalo.weatherlion.model.TimeZoneInfo;
import com.bushbungalo.weatherlion.services.CityDataService;
import com.bushbungalo.weatherlion.services.GeoLocationService;
import com.bushbungalo.weatherlion.services.LocationTrackerService;
import com.bushbungalo.weatherlion.services.WidgetUpdateService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

@SuppressWarnings({"unused", "WeakerAccess", "null", "unchecked"})
public abstract class UtilityMethod
{
    private static final String TAG = "UtilityMethod";

    public static String[] yahooWeatherCodes =
            {
                    "tornado", "tropical storm", "hurricane", "severe thunderstorms",
                    "thunderstorms", "mixed rain and snow", "mixed rain and sleet",
                    "mixed snow and sleet", "freezing drizzle", "drizzle", "freezing rain",
                    "showers", "showers", "snow flurries", "light snow showers", "blowing snow",
                    "snow", "hail", "sleet", "dust", "foggy", "haze", "smoky", "blustery", "windy",
                    "cold", "cloudy", "mostly cloudy (night)", "mostly cloudy (day)",
                    "partly cloudy (night)", "partly cloudy (day)", "clear (night)", "sunny",
                    "fair (night)", "fair (day)", "mixed rain and hail", "hot",
                    "isolated thunderstorms", "scattered thunderstorms", "scattered thunderstorms",
                    "scattered showers", "heavy snow", "scattered snow showers", "heavy snow",
                    "partly cloudy", "thundershowers", "snow showers", "isolated thundershowers"
            };

    // Array of compass sectors
    public static String[] compassSectors =
            {
                    "N", "NNE", "NE", "ENE", "E", "ESE",
                    "SE", "SSE", "S", "SSW", "SW", "WSW",
                    "W", "WNW", "NW", "NNW"
            };

    // Maps a cardinal point to its name
    public static LinkedHashMap<String, String> cardinalPoints;
    static
    {
        cardinalPoints = new LinkedHashMap<>();
        cardinalPoints.put( "E", "East" );
        cardinalPoints.put( "N", "North" );
        cardinalPoints.put( "S", "South" );
        cardinalPoints.put( "W", "West" );
    }

    // Maps a US state's two-letter code to its full name
    public static LinkedHashMap<String, String> usStatesByCode;
    static
    {
        usStatesByCode = new LinkedHashMap<>();

        usStatesByCode.put("AL", "Alabama");
        usStatesByCode.put("AK", "Alaska");
        usStatesByCode.put("AZ", "Arizona");
        usStatesByCode.put("AR", "Arkansas");
        usStatesByCode.put("CA", "California");
        usStatesByCode.put("CO", "Colorado");
        usStatesByCode.put("CT", "Connecticut");
        usStatesByCode.put("DE", "Delaware");
        usStatesByCode.put("FL", "Florida");
        usStatesByCode.put("GA", "Georgia");
        usStatesByCode.put("HI", "Hawaii");
        usStatesByCode.put("ID", "Idaho");
        usStatesByCode.put("IL", "Illinois");
        usStatesByCode.put("IN", "Indiana");
        usStatesByCode.put("IA", "Iowa");
        usStatesByCode.put("KS", "Kansas");
        usStatesByCode.put("KY", "Kentucky");
        usStatesByCode.put("LA", "Louisiana");
        usStatesByCode.put("ME", "Maine");
        usStatesByCode.put("MD", "Maryland");
        usStatesByCode.put("MA", "Massachusetts");
        usStatesByCode.put("MI", "Michigan");
        usStatesByCode.put("MN", "Minnesota");
        usStatesByCode.put("MS", "Mississippi");
        usStatesByCode.put("MO", "Missouri");
        usStatesByCode.put("MT", "Montana");
        usStatesByCode.put("NE", "Nebraska");
        usStatesByCode.put("NV", "Nevada");
        usStatesByCode.put("NH", "New Hampshire");
        usStatesByCode.put("NJ", "New Jersey");
        usStatesByCode.put("NM", "New Mexico");
        usStatesByCode.put("NY", "New York");
        usStatesByCode.put("NC", "North Carolina");
        usStatesByCode.put("ND", "North Dakota");
        usStatesByCode.put("OH", "Ohio");
        usStatesByCode.put("OK", "Oklahoma");
        usStatesByCode.put("OR", "Oregon");
        usStatesByCode.put("PA", "Pennsylvania");
        usStatesByCode.put("RI", "Rhode Island");
        usStatesByCode.put("SC", "South Carolina");
        usStatesByCode.put("SD", "South Dakota");
        usStatesByCode.put("TN", "Tennessee");
        usStatesByCode.put("TX", "Texas");
        usStatesByCode.put("UT", "Utah");
        usStatesByCode.put("VT", "Vermont");
        usStatesByCode.put("VA", "Virginia");
        usStatesByCode.put("WA", "Washington");
        usStatesByCode.put("WV", "West Virginia");
        usStatesByCode.put("WI", "Wisconsin");
        usStatesByCode.put("WY", "Wyoming");
    }

    // Maps a US state's full name to its two-letter code
    public static LinkedHashMap<String, String> usStatesByName;
    static
    {
        usStatesByName = new LinkedHashMap<>();

        usStatesByName.put("Alabama", "AL");
        usStatesByName.put("Alaska", "AK");
        usStatesByName.put("Arizona", "AZ");
        usStatesByName.put("Arkansas", "AR");
        usStatesByName.put("California", "CA");
        usStatesByName.put("Colorado", "CO");
        usStatesByName.put("Connecticut", "CT");
        usStatesByName.put("Delaware", "DE");
        usStatesByName.put("Florida", "FL");
        usStatesByName.put("Georgia", "GA");
        usStatesByName.put("Hawaii", "HI");
        usStatesByName.put("Idaho", "ID");
        usStatesByName.put("Illinois", "IL");
        usStatesByName.put("Indiana", "IN");
        usStatesByName.put("Iowa", "IA");
        usStatesByName.put("Kansas", "KS");
        usStatesByName.put("Kentucky", "KY");
        usStatesByName.put("Louisiana", "LA");
        usStatesByName.put("Maine", "ME");
        usStatesByName.put("Maryland", "MD");
        usStatesByName.put("Massachusetts", "MA");
        usStatesByName.put("Michigan", "MI");
        usStatesByName.put("Minnesota", "MN");
        usStatesByName.put("Mississippi", "MS");
        usStatesByName.put("Missouri", "MO");
        usStatesByName.put("Montana", "MT");
        usStatesByName.put("Nebraska", "NE");
        usStatesByName.put("Nevada", "NV");
        usStatesByName.put("New Hampshire", "NH");
        usStatesByName.put("New Jersey", "NJ");
        usStatesByName.put("New Mexico", "NM");
        usStatesByName.put("New York", "NY");
        usStatesByName.put("North Carolina", "NC");
        usStatesByName.put("North Dakota", "ND");
        usStatesByName.put("Ohio", "OH");
        usStatesByName.put("Oklahoma", "OK");
        usStatesByName.put("Oregon", "OR");
        usStatesByName.put("Pennsylvania", "PA");
        usStatesByName.put("Rhode Island", "RI");
        usStatesByName.put("South Carolina", "SC");
        usStatesByName.put("South Dakota", "SD");
        usStatesByName.put("Tennessee", "TN");
        usStatesByName.put("Texas", "TX");
        usStatesByName.put("Utah", "UT");
        usStatesByName.put("Vermont", "VT");
        usStatesByName.put("Virginia", "VA");
        usStatesByName.put("Washington", "WA");
        usStatesByName.put("West Virginia", "WV");
        usStatesByName.put("Wisconsin", "WI");
        usStatesByName.put("Wyoming", "WY");
    }

    // Maps a country's two-letter code to its full name
    public static LinkedHashMap<String, String> worldCountries;
    static
    {
        worldCountries = new LinkedHashMap<>();

        worldCountries.put("AF", "Afghanistan");
        worldCountries.put("AX", "Aland Islands");
        worldCountries.put("AL", "Albania");
        worldCountries.put("DZ", "Algeria");
        worldCountries.put("AS", "American Samoa");
        worldCountries.put("AD", "Andorra");
        worldCountries.put("AO", "Angola");
        worldCountries.put("AI", "Anguilla");
        worldCountries.put("AQ", "Antarctica");
        worldCountries.put("AG", "Antigua and Barbuda");
        worldCountries.put("AR", "Argentina");
        worldCountries.put("AM", "Armenia");
        worldCountries.put("AW", "Aruba");
        worldCountries.put("AU", "Australia");
        worldCountries.put("AT", "Austria");
        worldCountries.put("AZ", "Azerbaijan");
        worldCountries.put("BS", "Bahamas");
        worldCountries.put("BH", "Bahrain");
        worldCountries.put("BD", "Bangladesh");
        worldCountries.put("BB", "Barbados");
        worldCountries.put("BY", "Belarus");
        worldCountries.put("BE", "Belgium");
        worldCountries.put("BZ", "Belize");
        worldCountries.put("BJ", "Benin");
        worldCountries.put("BM", "Bermuda");
        worldCountries.put("BT", "Bhutan");
        worldCountries.put("BO", "Bolivia");
        worldCountries.put("BA", "Bosnia and Herzegovina");
        worldCountries.put("BW", "Botswana");
        worldCountries.put("BV", "Bouvet Island");
        worldCountries.put("BR", "Brazil");
        worldCountries.put("VG", "British Virgin Islands");
        worldCountries.put("IO", "British Indian Ocean Territory");
        worldCountries.put("BN", "Brunei Darussalam");
        worldCountries.put("BG", "Bulgaria");
        worldCountries.put("BF", "Burkina Faso");
        worldCountries.put("BI", "Burundi");
        worldCountries.put("KH", "Cambodia");
        worldCountries.put("CM", "Cameroon");
        worldCountries.put("CA", "Canada");
        worldCountries.put("CV", "Cape Verde");
        worldCountries.put("KY", "Cayman Islands");
        worldCountries.put("CF", "Central African Republic");
        worldCountries.put("TD", "Chad");
        worldCountries.put("CL", "Chile");
        worldCountries.put("CN", "China");
        worldCountries.put("HK", "Hong Kong, SAR China");
        worldCountries.put("MO", "Macao, SAR China");
        worldCountries.put("CX", "Christmas Island");
        worldCountries.put("CC", "Cocos (Keeling) Islands");
        worldCountries.put("CO", "Colombia");
        worldCountries.put("KM", "Comoros");
        worldCountries.put("CG", "Congo (Brazzaville)");
        worldCountries.put("CD", "Congo, (Kinshasa)");
        worldCountries.put("CK", "Cook Islands");
        worldCountries.put("CR", "Costa Rica");
        worldCountries.put("CI", "Côte d'Ivoire");
        worldCountries.put("HR", "Croatia");
        worldCountries.put("CU", "Cuba");
        worldCountries.put("CY", "Cyprus");
        worldCountries.put("CZ", "Czech Republic");
        worldCountries.put("DK", "Denmark");
        worldCountries.put("DJ", "Djibouti");
        worldCountries.put("DM", "Dominica");
        worldCountries.put("DO", "Dominican Republic");
        worldCountries.put("EC", "Ecuador");
        worldCountries.put("EG", "Egypt");
        worldCountries.put("SV", "El Salvador");
        worldCountries.put("GQ", "Equatorial Guinea");
        worldCountries.put("ER", "Eritrea");
        worldCountries.put("EE", "Estonia");
        worldCountries.put("ET", "Ethiopia");
        worldCountries.put("FK", "Falkland Islands (Malvinas)");
        worldCountries.put("FO", "Faroe Islands");
        worldCountries.put("FJ", "Fiji");
        worldCountries.put("FI", "Finland");
        worldCountries.put("FR", "France");
        worldCountries.put("GF", "French Guiana");
        worldCountries.put("PF", "French Polynesia");
        worldCountries.put("TF", "French Southern Territories");
        worldCountries.put("GA", "Gabon");
        worldCountries.put("GM", "Gambia");
        worldCountries.put("GE", "Georgia");
        worldCountries.put("DE", "Germany");
        worldCountries.put("GH", "Ghana");
        worldCountries.put("GI", "Gibraltar");
        worldCountries.put("GR", "Greece");
        worldCountries.put("GL", "Greenland");
        worldCountries.put("GD", "Grenada");
        worldCountries.put("GP", "Guadeloupe");
        worldCountries.put("GU", "Guam");
        worldCountries.put("GT", "Guatemala");
        worldCountries.put("GG", "Guernsey");
        worldCountries.put("GN", "Guinea");
        worldCountries.put("GW", "Guinea-Bissau");
        worldCountries.put("GY", "Guyana");
        worldCountries.put("HT", "Haiti");
        worldCountries.put("HM", "Heard and Mcdonald Islands");
        worldCountries.put("VA", "Holy SeeÂ (Vatican City State)");
        worldCountries.put("HN", "Honduras");
        worldCountries.put("HU", "Hungary");
        worldCountries.put("IS", "Iceland");
        worldCountries.put("IN", "India");
        worldCountries.put("ID", "Indonesia");
        worldCountries.put("IR", "Iran, Islamic Republic of");
        worldCountries.put("IQ", "Iraq");
        worldCountries.put("IE", "Ireland");
        worldCountries.put("IM", "Isle of Man");
        worldCountries.put("IL", "Israel");
        worldCountries.put("IT", "Italy");
        worldCountries.put("JM", "Jamaica");
        worldCountries.put("JP", "Japan");
        worldCountries.put("JE", "Jersey");
        worldCountries.put("JO", "Jordan");
        worldCountries.put("KZ", "Kazakhstan");
        worldCountries.put("KE", "Kenya");
        worldCountries.put("KI", "Kiribati");
        worldCountries.put("KP", "KoreaÂ (North)");
        worldCountries.put("KR", "KoreaÂ (South)");
        worldCountries.put("KW", "Kuwait");
        worldCountries.put("KG", "Kyrgyzstan");
        worldCountries.put("LA", "Lao PDR");
        worldCountries.put("LV", "Latvia");
        worldCountries.put("LB", "Lebanon");
        worldCountries.put("LS", "Lesotho");
        worldCountries.put("LR", "Liberia");
        worldCountries.put("LY", "Libya");
        worldCountries.put("LI", "Liechtenstein");
        worldCountries.put("LT", "Lithuania");
        worldCountries.put("LU", "Luxembourg");
        worldCountries.put("MK", "Macedonia, Republic of");
        worldCountries.put("MG", "Madagascar");
        worldCountries.put("MW", "Malawi");
        worldCountries.put("MY", "Malaysia");
        worldCountries.put("MV", "Maldives");
        worldCountries.put("ML", "Mali");
        worldCountries.put("MT", "Malta");
        worldCountries.put("MH", "Marshall Islands");
        worldCountries.put("MQ", "Martinique");
        worldCountries.put("MR", "Mauritania");
        worldCountries.put("MU", "Mauritius");
        worldCountries.put("YT", "Mayotte");
        worldCountries.put("MX", "Mexico");
        worldCountries.put("FM", "Micronesia, Federated States of");
        worldCountries.put("MD", "Moldova");
        worldCountries.put("MC", "Monaco");
        worldCountries.put("MN", "Mongolia");
        worldCountries.put("ME", "Montenegro");
        worldCountries.put("MS", "Montserrat");
        worldCountries.put("MA", "Morocco");
        worldCountries.put("MZ", "Mozambique");
        worldCountries.put("MM", "Myanmar");
        worldCountries.put("NA", "Namibia");
        worldCountries.put("NR", "Nauru");
        worldCountries.put("NP", "Nepal");
        worldCountries.put("NL", "Netherlands");
        worldCountries.put("AN", "Netherlands Antilles");
        worldCountries.put("NC", "New Caledonia");
        worldCountries.put("NZ", "New Zealand");
        worldCountries.put("NI", "Nicaragua");
        worldCountries.put("NE", "Niger");
        worldCountries.put("NG", "Nigeria");
        worldCountries.put("NU", "Niue");
        worldCountries.put("NF", "Norfolk Island");
        worldCountries.put("MP", "Northern Mariana Islands");
        worldCountries.put("NO", "Norway");
        worldCountries.put("OM", "Oman");
        worldCountries.put("PK", "Pakistan");
        worldCountries.put("PW", "Palau");
        worldCountries.put("PS", "Palestinian Territory");
        worldCountries.put("PA", "Panama");
        worldCountries.put("PG", "Papua New Guinea");
        worldCountries.put("PY", "Paraguay");
        worldCountries.put("PE", "Peru");
        worldCountries.put("PH", "Philippines");
        worldCountries.put("PN", "Pitcairn");
        worldCountries.put("PL", "Poland");
        worldCountries.put("PT", "Portugal");
        worldCountries.put("PR", "Puerto Rico");
        worldCountries.put("QA", "Qatar");
        worldCountries.put("RE", "Reunion");
        worldCountries.put("RO", "Romania");
        worldCountries.put("RU", "Russian Federation");
        worldCountries.put("RW", "Rwanda");
        worldCountries.put("BL", "Saint-BarthÃ©lemy");
        worldCountries.put("SH", "Saint Helena");
        worldCountries.put("KN", "Saint Kitts and Nevis");
        worldCountries.put("LC", "Saint Lucia");
        worldCountries.put("MF", "Saint-Martin (French part)");
        worldCountries.put("PM", "Saint Pierre and Miquelon");
        worldCountries.put("VC", "Saint Vincent and Grenadines");
        worldCountries.put("WS", "Samoa");
        worldCountries.put("SM", "San Marino");
        worldCountries.put("ST", "Sao Tome and Principe");
        worldCountries.put("SA", "Saudi Arabia");
        worldCountries.put("SN", "Senegal");
        worldCountries.put("RS", "Serbia");
        worldCountries.put("SC", "Seychelles");
        worldCountries.put("SL", "Sierra Leone");
        worldCountries.put("SG", "Singapore");
        worldCountries.put("SK", "Slovakia");
        worldCountries.put("SI", "Slovenia");
        worldCountries.put("SB", "Solomon Islands");
        worldCountries.put("SO", "Somalia");
        worldCountries.put("ZA", "South Africa");
        worldCountries.put("GS", "South Georgia and the South Sandwich Islands");
        worldCountries.put("SS", "South Sudan");
        worldCountries.put("ES", "Spain");
        worldCountries.put("LK", "Sri Lanka");
        worldCountries.put("SD", "Sudan");
        worldCountries.put("SR", "Suriname");
        worldCountries.put("SJ", "Svalbard and Jan Mayen Islands");
        worldCountries.put("SZ", "Swaziland");
        worldCountries.put("SE", "Sweden");
        worldCountries.put("CH", "Switzerland");
        worldCountries.put("SY", "Syrian Arab RepublicÂ (Syria)");
        worldCountries.put("TW", "Taiwan, Republic of China");
        worldCountries.put("TJ", "Tajikistan");
        worldCountries.put("TZ", "Tanzania, United Republic of");
        worldCountries.put("TH", "Thailand");
        worldCountries.put("TL", "Timor-Leste");
        worldCountries.put("TG", "Togo");
        worldCountries.put("TK", "Tokelau");
        worldCountries.put("TO", "Tonga");
        worldCountries.put("TT", "Trinidad and Tobago");
        worldCountries.put("TN", "Tunisia");
        worldCountries.put("TR", "Turkey");
        worldCountries.put("TM", "Turkmenistan");
        worldCountries.put("TC", "Turks and Caicos Islands");
        worldCountries.put("TV", "Tuvalu");
        worldCountries.put("UG", "Uganda");
        worldCountries.put("UA", "Ukraine");
        worldCountries.put("AE", "United Arab Emirates");
        worldCountries.put("GB", "United Kingdom");
        worldCountries.put("US", "United States");
        worldCountries.put("UM", "US Minor Outlying Islands");
        worldCountries.put("UY", "Uruguay");
        worldCountries.put("UZ", "Uzbekistan");
        worldCountries.put("VU", "Vanuatu");
        worldCountries.put("VE", "VenezuelaÂ (Bolidouble ian Republic)");
        worldCountries.put("VN", "Viet Nam");
        worldCountries.put("VI", "Virgin Islands, US");
        worldCountries.put("WF", "Wallis and Futuna Islands");
        worldCountries.put("EH", "Western Sahara");
        worldCountries.put("YE", "Yemen");
        worldCountries.put("ZM", "Zambia");
        worldCountries.put("ZW", "Zimbabwe");
    }

    // Maps a country's full name to its two-letter code
    public static LinkedHashMap<String, String> worldCountryCodes;
    static
    {
        worldCountryCodes = new LinkedHashMap<>();

        worldCountryCodes.put( "Afghanistan", "AF" );
        worldCountryCodes.put( "Aland Islands", "AX" );
        worldCountryCodes.put( "Albania", "AL" );
        worldCountryCodes.put( "Algeria", "DZ" );
        worldCountryCodes.put( "American Samoa", "AS" );
        worldCountryCodes.put( "Andorra", "AD" );
        worldCountryCodes.put( "Angola", "AO" );
        worldCountryCodes.put( "Anguilla", "AI" );
        worldCountryCodes.put( "Antarctica", "AQ" );
        worldCountryCodes.put( "Antigua and Barbuda", "AG" );
        worldCountryCodes.put( "Antigua & Barbuda", "AG" );
        worldCountryCodes.put( "Argentina", "AR" );
        worldCountryCodes.put( "Armenia", "AM" );
        worldCountryCodes.put( "Aruba", "AW" );
        worldCountryCodes.put( "Australia", "AU" );
        worldCountryCodes.put( "Austria", "AT" );
        worldCountryCodes.put( "Azerbaijan", "AZ" );
        worldCountryCodes.put( "Bahamas", "BS" );
        worldCountryCodes.put( "Bahrain", "BH" );
        worldCountryCodes.put( "Bangladesh", "BD" );
        worldCountryCodes.put( "Barbados", "BB" );
        worldCountryCodes.put( "Belarus", "BY" );
        worldCountryCodes.put( "Belgium", "BE" );
        worldCountryCodes.put( "Belize", "BZ" );
        worldCountryCodes.put( "Benin", "BJ" );
        worldCountryCodes.put( "Bermuda", "BM" );
        worldCountryCodes.put( "Bhutan", "BT" );
        worldCountryCodes.put( "Bolivia", "BO" );
        worldCountryCodes.put( "Bosnia and Herzegovina", "BA" );
        worldCountryCodes.put( "Bosnia & Herzegovina", "BA" );
        worldCountryCodes.put( "Botswana", "BW" );
        worldCountryCodes.put( "Bouvet Island", "BV" );
        worldCountryCodes.put( "Brazil", "BR" );
        worldCountryCodes.put( "British Virgin Islands", "VG" );
        worldCountryCodes.put( "British Indian Ocean Territory", "IO" );
        worldCountryCodes.put( "Brunei Darussalam", "BN" );
        worldCountryCodes.put( "Bulgaria", "BG" );
        worldCountryCodes.put( "Burkina Faso", "BF" );
        worldCountryCodes.put( "Burundi", "BI" );
        worldCountryCodes.put( "Cambodia", "KH" );
        worldCountryCodes.put( "Cameroon", "CM" );
        worldCountryCodes.put( "Canada", "CA" );
        worldCountryCodes.put( "Cape Verde", "CV" );
        worldCountryCodes.put( "Cayman Islands", "KY" );
        worldCountryCodes.put( "Central African Republic", "CF" );
        worldCountryCodes.put( "Chad", "TD" );
        worldCountryCodes.put( "Chile", "CL" );
        worldCountryCodes.put( "China", "CN" );
        worldCountryCodes.put( "Hong Kong, SAR China", "HK" );
        worldCountryCodes.put( "Hong Kong", "HK" );
        worldCountryCodes.put( "Macao, SAR China", "MO" );
        worldCountryCodes.put( "Macao", "MO" );
        worldCountryCodes.put( "Christmas Island", "CX" );
        worldCountryCodes.put( "Cocos (Keeling) Islands", "CC" );
        worldCountryCodes.put( "Cocos Islands", "CC" );
        worldCountryCodes.put( "Colombia", "CO" );
        worldCountryCodes.put( "Comoros", "KM" );
        worldCountryCodes.put( "Congo (Brazzaville)", "CG" );
        worldCountryCodes.put( "Congo (Kinshasa)", "CD" );
        worldCountryCodes.put( "Cook Islands", "CK" );
        worldCountryCodes.put( "Costa Rica", "CR" );
        worldCountryCodes.put( "Côte d'Ivoire", "CI" );
        worldCountryCodes.put( "Croatia", "HR" );
        worldCountryCodes.put( "Cuba", "CU" );
        worldCountryCodes.put( "Cyprus", "CY" );
        worldCountryCodes.put( "Czech Republic", "CZ" );
        worldCountryCodes.put( "Denmark", "DK" );
        worldCountryCodes.put( "Djibouti", "DJ" );
        worldCountryCodes.put( "Dominica", "DM" );
        worldCountryCodes.put( "Dominican Republic", "DO" );
        worldCountryCodes.put( "Ecuador", "EC" );
        worldCountryCodes.put( "Egypt", "EG" );
        worldCountryCodes.put( "El Salvador", "SV" );
        worldCountryCodes.put( "Equatorial Guinea", "GQ" );
        worldCountryCodes.put( "Eritrea", "ER" );
        worldCountryCodes.put( "Estonia", "EE" );
        worldCountryCodes.put( "Ethiopia", "ET" );
        worldCountryCodes.put( "Falkland Islands (Malvinas)", "FK" );
        worldCountryCodes.put( "Faroe Islands", "FO" );
        worldCountryCodes.put( "Fiji", "FJ" );
        worldCountryCodes.put( "Finland", "FI" );
        worldCountryCodes.put( "France", "FR" );
        worldCountryCodes.put( "French Guiana", "GF" );
        worldCountryCodes.put( "French Polynesia", "PF" );
        worldCountryCodes.put( "French Southern Territories", "TF" );
        worldCountryCodes.put( "Gabon", "GA" );
        worldCountryCodes.put( "Gambia", "GM" );
        worldCountryCodes.put( "Georgia", "GE" );
        worldCountryCodes.put( "Germany", "DE" );
        worldCountryCodes.put( "Ghana", "GH" );
        worldCountryCodes.put( "Gibraltar", "GI" );
        worldCountryCodes.put( "Greece", "GR" );
        worldCountryCodes.put( "Greenland", "GL" );
        worldCountryCodes.put( "Grenada", "GD" );
        worldCountryCodes.put( "Guadeloupe", "GP" );
        worldCountryCodes.put( "Guam", "GU" );
        worldCountryCodes.put( "Guatemala", "GT" );
        worldCountryCodes.put( "Guernsey", "GG" );
        worldCountryCodes.put( "Guinea", "GN" );
        worldCountryCodes.put( "Guinea-Bissau", "GW" );
        worldCountryCodes.put( "Guyana", "GY" );
        worldCountryCodes.put( "Haiti", "HT" );
        worldCountryCodes.put( "Heard and Mcdonald Islands", "HM" );
        worldCountryCodes.put( "Heard & Mcdonald Islands", "HM" );
        worldCountryCodes.put( "Vatican City", "VA" );
        worldCountryCodes.put( "Vatican", "VA" );
        worldCountryCodes.put( "Honduras", "HN" );
        worldCountryCodes.put( "Hungary", "HU" );
        worldCountryCodes.put( "Iceland", "IS" );
        worldCountryCodes.put( "India", "IN" );
        worldCountryCodes.put( "Indonesia", "ID" );
        worldCountryCodes.put( "Iran, Islamic Republic of", "IR" );
        worldCountryCodes.put( "Iran", "IR" );
        worldCountryCodes.put( "Iraq", "IQ" );
        worldCountryCodes.put( "Ireland", "IE" );
        worldCountryCodes.put( "Isle of Man", "IM" );
        worldCountryCodes.put( "Israel", "IL" );
        worldCountryCodes.put( "Italy", "IT" );
        worldCountryCodes.put( "Jamaica", "JM" );
        worldCountryCodes.put( "Japan", "JP" );
        worldCountryCodes.put( "Jersey", "JE" );
        worldCountryCodes.put( "Jordan", "JO" );
        worldCountryCodes.put( "Kazakhstan", "KZ" );
        worldCountryCodes.put( "Kenya", "KE" );
        worldCountryCodes.put( "Kiribati", "KI" );
        worldCountryCodes.put( "Korea (North)", "KP" );
        worldCountryCodes.put( "North Korea", "KP" );
        worldCountryCodes.put( "Korea (South)", "KR" );
        worldCountryCodes.put( "South Korea", "KR" );
        worldCountryCodes.put( "Kuwait", "KW" );
        worldCountryCodes.put( "Kyrgyzstan", "KG" );
        worldCountryCodes.put( "Lao PDR", "LA" );
        worldCountryCodes.put( "Lao", "LA" );
        worldCountryCodes.put( "Latvia", "LV" );
        worldCountryCodes.put( "Lebanon", "LB" );
        worldCountryCodes.put( "Lesotho", "LS" );
        worldCountryCodes.put( "Liberia", "LR" );
        worldCountryCodes.put( "Libya", "LY" );
        worldCountryCodes.put( "Liechtenstein", "LI" );
        worldCountryCodes.put( "Lithuania", "LT" );
        worldCountryCodes.put( "Luxembourg", "LU" );
        worldCountryCodes.put( "Macedonia, Republic of", "MK" );
        worldCountryCodes.put( "Macedonia", "MK" );
        worldCountryCodes.put( "Madagascar", "MG" );
        worldCountryCodes.put( "Malawi", "MW" );
        worldCountryCodes.put( "Malaysia", "MY" );
        worldCountryCodes.put( "Maldives", "MV" );
        worldCountryCodes.put( "Mali", "ML" );
        worldCountryCodes.put( "Malta", "MT" );
        worldCountryCodes.put( "Marshall Islands", "MH" );
        worldCountryCodes.put( "Martinique", "MQ" );
        worldCountryCodes.put( "Mauritania", "MR" );
        worldCountryCodes.put( "Mauritius", "MU" );
        worldCountryCodes.put( "Mayotte", "YT" );
        worldCountryCodes.put( "Mexico", "MX" );
        worldCountryCodes.put( "Micronesia, Federated States of", "FM" );
        worldCountryCodes.put( "Micronesia", "FM" );
        worldCountryCodes.put( "Moldova", "MD" );
        worldCountryCodes.put( "Monaco", "MC" );
        worldCountryCodes.put( "Mongolia", "MN" );
        worldCountryCodes.put( "Montenegro", "ME" );
        worldCountryCodes.put( "Montserrat", "MS" );
        worldCountryCodes.put( "Morocco", "MA" );
        worldCountryCodes.put( "Mozambique", "MZ" );
        worldCountryCodes.put( "Myanmar", "MM" );
        worldCountryCodes.put( "Namibia", "NA" );
        worldCountryCodes.put( "Nauru", "NR" );
        worldCountryCodes.put( "Nepal", "NP" );
        worldCountryCodes.put( "Netherlands", "NL" );
        worldCountryCodes.put( "Netherlands Antilles", "AN" );
        worldCountryCodes.put( "New Caledonia", "NC" );
        worldCountryCodes.put( "New Zealand", "NZ" );
        worldCountryCodes.put( "Nicaragua", "NI" );
        worldCountryCodes.put( "Niger", "NE" );
        worldCountryCodes.put( "Nigeria", "NG" );
        worldCountryCodes.put( "Niue", "NU" );
        worldCountryCodes.put( "Norfolk Island", "NF" );
        worldCountryCodes.put( "Northern Mariana Islands", "MP" );
        worldCountryCodes.put( "Norway", "NO" );
        worldCountryCodes.put( "Oman", "OM" );
        worldCountryCodes.put( "Pakistan", "PK" );
        worldCountryCodes.put( "Palau", "PW" );
        worldCountryCodes.put( "Palestinian Territory", "PS" );
        worldCountryCodes.put( "Panama", "PA" );
        worldCountryCodes.put( "Papua New Guinea", "PG" );
        worldCountryCodes.put( "Paraguay", "PY" );
        worldCountryCodes.put( "Peru", "PE" );
        worldCountryCodes.put( "Philippines", "PH" );
        worldCountryCodes.put( "Pitcairn", "PN" );
        worldCountryCodes.put( "Poland", "PL" );
        worldCountryCodes.put( "Portugal", "PT" );
        worldCountryCodes.put( "Puerto Rico", "PR" );
        worldCountryCodes.put( "Qatar", "QA" );
        worldCountryCodes.put( "Reunion", "RE" );
        worldCountryCodes.put( "Romania", "RO" );
        worldCountryCodes.put( "Russian Federation", "RU" );
        worldCountryCodes.put( "Rwanda", "RW" );
        worldCountryCodes.put( "Saint Barthélemy", "BL" );
        worldCountryCodes.put( "Saint Helena", "SH" );
        worldCountryCodes.put( "Saint Kitts and Nevis", "KN" );
        worldCountryCodes.put( "Saint Kitts & Nevis", "KN" );
        worldCountryCodes.put( "Saint Lucia", "LC" );
        worldCountryCodes.put( "Saint-Martin (French part)", "MF" );
        worldCountryCodes.put( "Saint-Martin", "MF" );
        worldCountryCodes.put( "Saint Pierre and Miquelon", "PM" );
        worldCountryCodes.put( "Saint Pierre & Miquelon", "PM" );
        worldCountryCodes.put( "Saint Vincent and Grenadines", "VC" );
        worldCountryCodes.put( "Saint Vincent & Grenadines", "VC" );
        worldCountryCodes.put( "Samoa", "WS" );
        worldCountryCodes.put( "San Marino", "SM" );
        worldCountryCodes.put( "Sao Tome and Principe", "ST" );
        worldCountryCodes.put( "Sao Tome & Principe", "ST" );
        worldCountryCodes.put( "Saudi Arabia", "SA" );
        worldCountryCodes.put( "Senegal", "SN" );
        worldCountryCodes.put( "Serbia", "RS" );
        worldCountryCodes.put( "Seychelles", "SC" );
        worldCountryCodes.put( "Sierra Leone", "SL" );
        worldCountryCodes.put( "Singapore", "SG" );
        worldCountryCodes.put( "Slovakia", "SK" );
        worldCountryCodes.put( "Slovenia", "SI" );
        worldCountryCodes.put( "Solomon Islands", "SB" );
        worldCountryCodes.put( "Somalia", "SO" );
        worldCountryCodes.put( "South Africa", "ZA" );
        worldCountryCodes.put( "South Georgia and the South Sandwich Islands", "GS" );
        worldCountryCodes.put( "South Georgia & the South Sandwich Islands", "GS" );
        worldCountryCodes.put( "South Sudan", "SS" );
        worldCountryCodes.put( "Spain", "ES" );
        worldCountryCodes.put( "Sri Lanka", "LK" );
        worldCountryCodes.put( "Sudan", "SD" );
        worldCountryCodes.put( "Suriname", "SR" );
        worldCountryCodes.put( "Svalbard and Jan Mayen Islands", "SJ" );
        worldCountryCodes.put( "Svalbard & Jan Mayen Islands", "SJ" );
        worldCountryCodes.put( "Swaziland", "SZ" );
        worldCountryCodes.put( "Sweden", "SE" );
        worldCountryCodes.put( "Switzerland", "CH" );
        worldCountryCodes.put( "Syrian Arab Republic (Syria)", "SY" );
        worldCountryCodes.put( "Syria", "SY" );
        worldCountryCodes.put( "Taiwan, Republic of China", "TW" );
        worldCountryCodes.put( "Taiwan", "TW" );
        worldCountryCodes.put( "Tajikistan", "TJ" );
        worldCountryCodes.put( "Tanzania, United Republic of", "TZ" );
        worldCountryCodes.put( "Tanzania", "TZ" );
        worldCountryCodes.put( "Thailand", "TH" );
        worldCountryCodes.put( "Timor-Leste", "TL" );
        worldCountryCodes.put( "Togo", "TG" );
        worldCountryCodes.put( "Tokelau", "TK" );
        worldCountryCodes.put( "Tonga", "TO" );
        worldCountryCodes.put( "Trinidad and Tobago", "TT" );
        worldCountryCodes.put( "Trinidad & Tobago", "TT" );
        worldCountryCodes.put( "Tunisia", "TN" );
        worldCountryCodes.put( "Turkey", "TR" );
        worldCountryCodes.put( "Turkmenistan", "TM" );
        worldCountryCodes.put( "Turks and Caicos Islands", "TC" );
        worldCountryCodes.put( "Turks & Caicos Islands", "TC" );
        worldCountryCodes.put( "Tuvalu", "TV" );
        worldCountryCodes.put( "Uganda", "UG" );
        worldCountryCodes.put( "Ukraine", "UA" );
        worldCountryCodes.put( "United Arab Emirates", "AE" );
        worldCountryCodes.put( "United Kingdom", "GB" );
        worldCountryCodes.put( "United States", "US" );
        worldCountryCodes.put( "US Minor Outlying Islands", "UM" );
        worldCountryCodes.put( "Uruguay", "UY" );
        worldCountryCodes.put( "Uzbekistan", "UZ" );
        worldCountryCodes.put( "Vanuatu", "VU" );
        worldCountryCodes.put( "Venezuela (Bolidouble ian Republic)", "VE" );
        worldCountryCodes.put( "Venezuela", "VE" );
        worldCountryCodes.put( "Viet Nam", "VN" );
        worldCountryCodes.put( "Virgin Islands, US", "VI" );
        worldCountryCodes.put( "Wallis and Futuna Islands", "WF" );
        worldCountryCodes.put( "Western Sahara", "EH" );
        worldCountryCodes.put( "Yemen", "YE" );
        worldCountryCodes.put( "Zambia", "ZM" );
        worldCountryCodes.put( "Zimbabwe", "ZW" );
    }

    public static LinkedHashMap<String, String> worldCountriesByName;
    static
    {
        worldCountriesByName = new LinkedHashMap<>();

        worldCountriesByName.put( "Afghanistan", "AF" );
        worldCountriesByName.put( "Aland Islands", "AX" );
        worldCountriesByName.put( "Albania", "AL" );
        worldCountriesByName.put( "Algeria", "DZ" );
        worldCountriesByName.put( "American Samoa", "AS" );
        worldCountriesByName.put( "Andorra", "AD" );
        worldCountriesByName.put( "Angola", "AO" );
        worldCountriesByName.put( "Anguilla", "AI" );
        worldCountriesByName.put( "Antarctica", "AQ" );
        worldCountriesByName.put( "Antigua and Barbuda", "AG" );
        worldCountriesByName.put( "Argentina", "AR" );
        worldCountriesByName.put( "Armenia", "AM" );
        worldCountriesByName.put( "Aruba", "AW" );
        worldCountriesByName.put( "Australia", "AU" );
        worldCountriesByName.put( "Austria", "AT" );
        worldCountriesByName.put( "Azerbaijan", "AZ" );
        worldCountriesByName.put( "Bahamas", "BS" );
        worldCountriesByName.put( "Bahrain", "BH" );
        worldCountriesByName.put( "Bangladesh", "BD" );
        worldCountriesByName.put( "Barbados", "BB" );
        worldCountriesByName.put( "Belarus", "BY" );
        worldCountriesByName.put( "Belgium", "BE" );
        worldCountriesByName.put( "Belize", "BZ" );
        worldCountriesByName.put( "Benin", "BJ" );
        worldCountriesByName.put( "Bermuda", "BM" );
        worldCountriesByName.put( "Bhutan", "BT" );
        worldCountriesByName.put( "Bolivia", "BO" );
        worldCountriesByName.put( "Bosnia and Herzegovina", "BA" );
        worldCountriesByName.put( "Botswana", "BW" );
        worldCountriesByName.put( "Bouvet Island", "BV" );
        worldCountriesByName.put( "Brazil", "BR" );
        worldCountriesByName.put( "British Virgin Islands", "VG" );
        worldCountriesByName.put( "British Indian Ocean Territory", "IO" );
        worldCountriesByName.put( "Brunei Darussalam", "BN" );
        worldCountriesByName.put( "Bulgaria", "BG" );
        worldCountriesByName.put( "Burkina Faso", "BF" );
        worldCountriesByName.put( "Burundi", "BI" );
        worldCountriesByName.put( "Cambodia", "KH" );
        worldCountriesByName.put( "Cameroon", "CM" );
        worldCountriesByName.put( "Canada", "CA" );
        worldCountriesByName.put( "Cape Verde", "CV" );
        worldCountriesByName.put( "Cayman Islands", "KY" );
        worldCountriesByName.put( "Central African Republic", "CF" );
        worldCountriesByName.put( "Chad", "TD" );
        worldCountriesByName.put( "Chile", "CL" );
        worldCountriesByName.put( "China", "CN" );
        worldCountriesByName.put( "Hong Kong", "HK" );
        worldCountriesByName.put( "Macao", "MO" );
        worldCountriesByName.put( "Christmas Island", "CX" );
        worldCountriesByName.put( "Cocos (Keeling) Islands", "CC" );
        worldCountriesByName.put( "Colombia", "CO" );
        worldCountriesByName.put( "Comoros", "KM" );
        worldCountriesByName.put( "Congo (Brazzaville)", "CG" );
        worldCountriesByName.put( "Congo", "CD" );
        worldCountriesByName.put( "Cook Islands", "CK" );
        worldCountriesByName.put( "Costa Rica", "CR" );
        worldCountriesByName.put( "Côte d'Ivoire", "CI" );
        worldCountriesByName.put( "Croatia", "HR" );
        worldCountriesByName.put( "Cuba", "CU" );
        worldCountriesByName.put( "Cyprus", "CY" );
        worldCountriesByName.put( "Czech Republic", "CZ" );
        worldCountriesByName.put( "Denmark", "DK" );
        worldCountriesByName.put( "Djibouti", "DJ" );
        worldCountriesByName.put( "Dominica", "DM" );
        worldCountriesByName.put( "Dominican Republic", "DO" );
        worldCountriesByName.put( "Ecuador", "EC" );
        worldCountriesByName.put( "Egypt", "EG" );
        worldCountriesByName.put( "El Salvador", "SV" );
        worldCountriesByName.put( "Equatorial Guinea", "GQ" );
        worldCountriesByName.put( "Eritrea", "ER" );
        worldCountriesByName.put( "Estonia", "EE" );
        worldCountriesByName.put( "Ethiopia", "ET" );
        worldCountriesByName.put( "Falkland Islands (Malvinas)", "FK" );
        worldCountriesByName.put( "Faroe Islands", "FO" );
        worldCountriesByName.put( "Fiji", "FJ" );
        worldCountriesByName.put( "Finland", "FI" );
        worldCountriesByName.put( "France", "FR" );
        worldCountriesByName.put( "French Guiana", "GF" );
        worldCountriesByName.put( "French Polynesia", "PF" );
        worldCountriesByName.put( "French Southern Territories", "TF" );
        worldCountriesByName.put( "Gabon", "GA" );
        worldCountriesByName.put( "Gambia", "GM" );
        worldCountriesByName.put( "Georgia", "GE" );
        worldCountriesByName.put( "Germany", "DE" );
        worldCountriesByName.put( "Ghana", "GH" );
        worldCountriesByName.put( "Gibraltar", "GI" );
        worldCountriesByName.put( "Greece", "GR" );
        worldCountriesByName.put( "Greenland", "GL" );
        worldCountriesByName.put( "Grenada", "GD" );
        worldCountriesByName.put( "Guadeloupe", "GP" );
        worldCountriesByName.put( "Guam", "GU" );
        worldCountriesByName.put( "Guatemala", "GT" );
        worldCountriesByName.put( "Guernsey", "GG" );
        worldCountriesByName.put( "Guinea", "GN" );
        worldCountriesByName.put( "Guinea-Bissau", "GW" );
        worldCountriesByName.put( "Guyana", "GY" );
        worldCountriesByName.put( "Haiti", "HT" );
        worldCountriesByName.put( "Heard and Mcdonald Islands", "HM" );
        worldCountriesByName.put( "Holy See (Vatican City State)", "VA" );
        worldCountriesByName.put( "Honduras", "HN" );
        worldCountriesByName.put( "Hungary", "HU" );
        worldCountriesByName.put( "Iceland", "IS" );
        worldCountriesByName.put( "India", "IN" );
        worldCountriesByName.put( "Indonesia", "ID" );
        worldCountriesByName.put( "Iran", "IR" );
        worldCountriesByName.put( "Iraq", "IQ" );
        worldCountriesByName.put( "Ireland", "IE" );
        worldCountriesByName.put( "Isle of Man", "IM" );
        worldCountriesByName.put( "Israel", "IL" );
        worldCountriesByName.put( "Italy", "IT" );
        worldCountriesByName.put( "Jamaica", "JM" );
        worldCountriesByName.put( "Japan", "JP" );
        worldCountriesByName.put( "Jersey", "JE" );
        worldCountriesByName.put( "Jordan", "JO" );
        worldCountriesByName.put( "Kazakhstan", "KZ" );
        worldCountriesByName.put( "Kenya", "KE" );
        worldCountriesByName.put( "Kiribati", "KI" );
        worldCountriesByName.put( "Korea (North)", "KP" );
        worldCountriesByName.put( "Korea (South)", "KR" );
        worldCountriesByName.put( "Kuwait", "KW" );
        worldCountriesByName.put( "Kyrgyzstan", "KG" );
        worldCountriesByName.put( "Lao PDR", "LA" );
        worldCountriesByName.put( "Latvia", "LV" );
        worldCountriesByName.put( "Lebanon", "LB" );
        worldCountriesByName.put( "Lesotho", "LS" );
        worldCountriesByName.put( "Liberia", "LR" );
        worldCountriesByName.put( "Libya", "LY" );
        worldCountriesByName.put( "Liechtenstein", "LI" );
        worldCountriesByName.put( "Lithuania", "LT" );
        worldCountriesByName.put( "Luxembourg", "LU" );
        worldCountriesByName.put( "Macedonia", "MK" );
        worldCountriesByName.put( "Madagascar", "MG" );
        worldCountriesByName.put( "Malawi", "MW" );
        worldCountriesByName.put( "Malaysia", "MY" );
        worldCountriesByName.put( "Maldives", "MV" );
        worldCountriesByName.put( "Mali", "ML" );
        worldCountriesByName.put( "Malta", "MT" );
        worldCountriesByName.put( "Marshall Islands", "MH" );
        worldCountriesByName.put( "Martinique", "MQ" );
        worldCountriesByName.put( "Mauritania", "MR" );
        worldCountriesByName.put( "Mauritius", "MU" );
        worldCountriesByName.put( "Mayotte", "YT" );
        worldCountriesByName.put( "Mexico", "MX" );
        worldCountriesByName.put( "Micronesia", "FM" );
        worldCountriesByName.put( "Moldova", "MD" );
        worldCountriesByName.put( "Monaco", "MC" );
        worldCountriesByName.put( "Mongolia", "MN" );
        worldCountriesByName.put( "Montenegro", "ME" );
        worldCountriesByName.put( "Montserrat", "MS" );
        worldCountriesByName.put( "Morocco", "MA" );
        worldCountriesByName.put( "Mozambique", "MZ" );
        worldCountriesByName.put( "Myanmar", "MM" );
        worldCountriesByName.put( "Namibia", "NA" );
        worldCountriesByName.put( "Nauru", "NR" );
        worldCountriesByName.put( "Nepal", "NP" );
        worldCountriesByName.put( "Netherlands", "NL" );
        worldCountriesByName.put( "Netherlands Antilles", "AN" );
        worldCountriesByName.put( "New Caledonia", "NC" );
        worldCountriesByName.put( "New Zealand", "NZ" );
        worldCountriesByName.put( "Nicaragua", "NI" );
        worldCountriesByName.put( "Niger", "NE" );
        worldCountriesByName.put( "Nigeria", "NG" );
        worldCountriesByName.put( "Niue", "NU" );
        worldCountriesByName.put( "Norfolk Island", "NF" );
        worldCountriesByName.put( "Northern Mariana Islands", "MP" );
        worldCountriesByName.put( "Norway", "NO" );
        worldCountriesByName.put( "Oman", "OM" );
        worldCountriesByName.put( "Pakistan", "PK" );
        worldCountriesByName.put( "Palau", "PW" );
        worldCountriesByName.put( "Palestinian Territory", "PS" );
        worldCountriesByName.put( "Panama", "PA" );
        worldCountriesByName.put( "Papua New Guinea", "PG" );
        worldCountriesByName.put( "Paraguay", "PY" );
        worldCountriesByName.put( "Peru", "PE" );
        worldCountriesByName.put( "Philippines", "PH" );
        worldCountriesByName.put( "Pitcairn", "PN" );
        worldCountriesByName.put( "Poland", "PL" );
        worldCountriesByName.put( "Portugal", "PT" );
        worldCountriesByName.put( "Puerto Rico", "PR" );
        worldCountriesByName.put( "Qatar", "QA" );
        worldCountriesByName.put( "Réunion", "RE" );
        worldCountriesByName.put( "Romania", "RO" );
        worldCountriesByName.put( "Russian Federation", "RU" );
        worldCountriesByName.put( "Rwanda", "RW" );
        worldCountriesByName.put( "Saint-Barthélemy", "BL" );
        worldCountriesByName.put( "Saint Helena", "SH" );
        worldCountriesByName.put( "Saint Kitts and Nevis", "KN" );
        worldCountriesByName.put( "Saint Lucia", "LC" );
        worldCountriesByName.put( "Saint-Martin (French part)", "MF" );
        worldCountriesByName.put( "Saint Pierre and Miquelon", "PM" );
        worldCountriesByName.put( "Saint Vincent and Grenadines", "VC" );
        worldCountriesByName.put( "Samoa", "WS" );
        worldCountriesByName.put( "San Marino", "SM" );
        worldCountriesByName.put( "Sao Tome and Principe", "ST" );
        worldCountriesByName.put( "Saudi Arabia", "SA" );
        worldCountriesByName.put( "Senegal", "SN" );
        worldCountriesByName.put( "Serbia", "RS" );
        worldCountriesByName.put( "Seychelles", "SC" );
        worldCountriesByName.put( "Sierra Leone", "SL" );
        worldCountriesByName.put( "Singapore", "SG" );
        worldCountriesByName.put( "Slovakia", "SK" );
        worldCountriesByName.put( "Slovenia", "SI" );
        worldCountriesByName.put( "Solomon Islands", "SB" );
        worldCountriesByName.put( "Somalia", "SO" );
        worldCountriesByName.put( "South Africa", "ZA" );
        worldCountriesByName.put( "South Georgia and the South Sandwich Islands", "GS" );
        worldCountriesByName.put( "South Sudan", "SS" );
        worldCountriesByName.put( "Spain", "ES" );
        worldCountriesByName.put( "Sri Lanka", "LK" );
        worldCountriesByName.put( "Sudan", "SD" );
        worldCountriesByName.put( "Suriname", "SR" );
        worldCountriesByName.put( "Svalbard and Jan Mayen Islands", "SJ" );
        worldCountriesByName.put( "Swaziland", "SZ" );
        worldCountriesByName.put( "Sweden", "SE" );
        worldCountriesByName.put( "Switzerland", "CH" );
        worldCountriesByName.put( "Syrian Arab Republic (Syria)", "SY" );
        worldCountriesByName.put( "Taiwan", "TW" );
        worldCountriesByName.put( "Tajikistan", "TJ" );
        worldCountriesByName.put( "Tanzania", "TZ" );
        worldCountriesByName.put( "Thailand", "TH" );
        worldCountriesByName.put( "Timor-Leste", "TL" );
        worldCountriesByName.put( "Togo", "TG" );
        worldCountriesByName.put( "Tokelau", "TK" );
        worldCountriesByName.put( "Tonga", "TO" );
        worldCountriesByName.put( "Trinidad and Tobago", "TT" );
        worldCountriesByName.put( "Tunisia", "TN" );
        worldCountriesByName.put( "Turkey", "TR" );
        worldCountriesByName.put( "Turkmenistan", "TM" );
        worldCountriesByName.put( "Turks and Caicos Islands", "TC" );
        worldCountriesByName.put( "Tuvalu", "TV" );
        worldCountriesByName.put( "Uganda", "UG" );
        worldCountriesByName.put( "Ukraine", "UA" );
        worldCountriesByName.put( "United Arab Emirates", "AE" );
        worldCountriesByName.put( "United Kingdom", "GB" );
        worldCountriesByName.put( "USA", "US" );
        worldCountriesByName.put( "United States", "US" );
        worldCountriesByName.put( "United States of America", "US" );
        worldCountriesByName.put( "US Minor Outlying Islands", "UM" );
        worldCountriesByName.put( "Uruguay", "UY" );
        worldCountriesByName.put( "Uzbekistan", "UZ" );
        worldCountriesByName.put( "Vanuatu", "VU" );
        worldCountriesByName.put( "Venezuela (Bolivarian Republic)", "VE" );
        worldCountriesByName.put( "Viet Nam", "VN" );
        worldCountriesByName.put( "Virgin Islands", "VI" );
        worldCountriesByName.put( "Wallis and Futuna Islands", "WF" );
        worldCountriesByName.put( "Western Sahara", "EH" );
        worldCountriesByName.put( "Yemen", "YE" );
        worldCountriesByName.put( "Zambia", "ZM" );
        worldCountriesByName.put( "Zimbabwe", "ZW" );
    }

    // Maps a weather reading to a specific asset icon file
    public static LinkedHashMap<String, String> weatherImages;
    static
    {
        weatherImages = new LinkedHashMap<>();

        weatherImages.put("tornado", "0.png");
        weatherImages.put("tropical storm", "1.png");
        weatherImages.put("hurricane", "1.png");
        weatherImages.put("severe thunderstorm", "1.png");
        weatherImages.put("severe thunderstorms", "1.png");
        weatherImages.put("thunderstorm", "1.png");
        weatherImages.put("thunderstorms", "1.png");
        weatherImages.put("tstorms", "1.png");
        weatherImages.put("t-storms", "1.png");
        weatherImages.put("freezing rain", "2.png");
        weatherImages.put("mixed rain and hail", "2.png");
        weatherImages.put("mixed rain and snow", "2.png");
        weatherImages.put("mixed rain and sleet", "2.png");
        weatherImages.put("sleet", "2.png");
        weatherImages.put("light snow showers", "2.png");
        weatherImages.put("freezing drizzle", "2.png");
        weatherImages.put("blowing snow", "3.png");
        weatherImages.put("heavy snow", "3.png");
        weatherImages.put("mixed snow and sleet", "3.png");
        weatherImages.put("scattered snow showers", "3.png");
        weatherImages.put("snow", "3.png");
        weatherImages.put("snow showers", "3.png");
        weatherImages.put("light rain", "4.png");
        weatherImages.put("moderate rain", "4.png");
        weatherImages.put("sprinkles", "4.png");
        weatherImages.put("heavy intensity rain", "5.png");
        weatherImages.put("heavy rain", "5.png");
        weatherImages.put("heavy rain showers", "5.png");
        weatherImages.put("rain", "5.png");
        weatherImages.put("rain showers", "5.png");
        weatherImages.put("showers", "5.png");
        weatherImages.put("snow flurries", "5.png");
        weatherImages.put("hail", "5.png");
        weatherImages.put("dust", "6.png");
        weatherImages.put("smoky", "6.png");
        weatherImages.put("fog", "7.png");
        weatherImages.put("foggy", "7.png");
        weatherImages.put("haze", "7.png");
        weatherImages.put("mist", "7.png");
        weatherImages.put("misty", "7.png");
        weatherImages.put("clouds", "8.png");
        weatherImages.put("cloudy", "8.png");
        weatherImages.put("overcast", "8.png");
        weatherImages.put("broken clouds", "8.png");
        weatherImages.put("overcast clouds", "8.png");
        weatherImages.put("scattered clouds", "8.png");
        weatherImages.put("cold", "9.png");
        weatherImages.put("mostly cloudy", "10.png");
        weatherImages.put("mostly cloudy (night)", "11.png");
        weatherImages.put("few clouds", "12.png");
        weatherImages.put("humid", "12.png");
        weatherImages.put("humid and partly cloudy", "12.png");
        weatherImages.put("mostly clear", "12.png");
        weatherImages.put("partly cloudy", "12.png");
        weatherImages.put("mostly sunny", "12.png");
        weatherImages.put("few clouds (night)", "13.png");
        weatherImages.put("humid (night)", "13.png");
        weatherImages.put("mostly sunny (night)", "13.png");
        weatherImages.put("mostly clear (night)", "13.png");
        weatherImages.put("partly cloudy (night)", "13.png");
        weatherImages.put("clear (night)", "14.png");
        weatherImages.put("clear sky (night)", "14.png");
        weatherImages.put("fair (night)", "14.png");
        weatherImages.put("sunny (night)", "14.png");
        weatherImages.put("clear", "15.png");
        weatherImages.put("clear sky", "15.png");
        weatherImages.put("fair", "15.png");
        weatherImages.put("sunny", "15.png");
        weatherImages.put("hot", "15.png");
        weatherImages.put("sky is clear", "15.png");
        weatherImages.put("chance of a thunderstorm", "16.png");
        weatherImages.put("isolated t-storms", "16.png");
        weatherImages.put("isolated thunderstorms", "16.png");
        weatherImages.put("isolated thundershowers", "16.png");
        weatherImages.put("isolated t-showers", "16.png");
        weatherImages.put("scattered thunderstorms", "16.png");
        weatherImages.put("scattered t-storms", "16.png");
        weatherImages.put("scattered tstorms", "16.png");
        weatherImages.put("thundershowers", "16.png");
        weatherImages.put("thunderstorm with rain", "16.png");
        weatherImages.put("drizzle", "17.png");
        weatherImages.put("scattered showers", "17.png");
        weatherImages.put("isolated showers", "17.png");
        weatherImages.put("chance of rain", "17.png");
        weatherImages.put("light rain showers", "17.png");
        weatherImages.put("light shower rain", "17.png");
        weatherImages.put("chance of a thunderstorm (night)", "18.png");
        weatherImages.put("isolated thunderstorms (night)", "18.png");
        weatherImages.put("isolated t-storms (night)", "18.png");
        weatherImages.put("isolated thundershowers (night)", "18.png");
        weatherImages.put("isolated t-showers (night)", "18.png");
        weatherImages.put("scattered thunderstorms (night)", "18.png");
        weatherImages.put("scattered t-storms (night)", "18.png");
        weatherImages.put("thundershowers (night)", "18.png");
        weatherImages.put("t-showers (night)", "18.png");
        weatherImages.put("drizzle (night)", "19.png");
        weatherImages.put("scattered showers (night)", "19.png");
        weatherImages.put("isolated showers (night)", "19.png");
        weatherImages.put("chance of rain (night)", "19.png");
        weatherImages.put("light rain showers (night)", "19.png");
        weatherImages.put("light shower rain (night)", "19.png");
        weatherImages.put("dust (night)", "20.png");
        weatherImages.put("smoky (night)", "20.png");
        weatherImages.put("blustery (night)", "20.png");
        weatherImages.put("breezy (night)", "20.png");
        weatherImages.put("blustery", "20.png");
        weatherImages.put("windy (night)", "21.png");
        weatherImages.put("breez", "21.png");
        weatherImages.put("breeze", "21.png");
        weatherImages.put("breezy", "21.png");
        weatherImages.put("wind", "21.png");
        weatherImages.put("windy", "21.png");
        weatherImages.put("not available", "na.png");
    }

    public enum LogLevel
    {
        SEVERE,
        INFO,
        WARNING
    }

    public enum MsgType
    {
        HTML,
        TEXT
    }

    public static Date lastUpdated;
    public static boolean refreshRequestedBySystem;
    public static boolean refreshRequestedByUser;

    public static ArrayList<String> subDirectoriesFound = new ArrayList<>();
    private static File[] files;

    private static CityData cd = null;
    public static boolean listRequested; // Should the method return a list or single data

    /**
     *
     * @return The application context
     */
    private static Context getAppContext()
    {
        return WeatherLionApplication.getAppContext();
    }// end of method getAppContext

    /**
     * Displays a custom Toast message to the user.
     *
     * @param context   The context of the caller.
     * @param message   The message to be displayed to the user.
     * @param type      The type should be an int value 1 for normal and 2 for error.
     * @param duration  The length of time in which the message is to be displayed.
     */
    public static void butteredToast( Context context, String message, int type, int duration )
    {
        View layout;

        switch ( type )
        {
            case 1: default:
            layout = View.inflate( context, R.layout.wl_buttered_toast, null );

            // ensure that the toast background is consistent with the overall UI theme
            if( WeatherLionApplication.widBackgroundColor != null )
            {
                switch( WeatherLionApplication.widBackgroundColor.toLowerCase() )
                {
                    case "aqua":
                        layout.setBackground( context.getDrawable( R.drawable.wl_aqua_toast_background ) );
                        break;
                    case "rabalac":
                        layout.setBackground( context.getDrawable( R.drawable.wl_rabalac_toast_background ) );
                        break;
                    default:
                        layout.setBackground( context.getDrawable( R.drawable.wl_lion_toast_background ) );
                        break;
                }// end of switch block
            }// end of if block
            break;
            case 2:
                layout = View.inflate( context, R.layout.wl_burnt_toast, null );
                break;
        }// end of switch block

        TextView txvMessage = layout.findViewById( R.id.toastMessage );
        txvMessage.setText( message );

        Toast toast = new Toast( context );
        toast.setDuration( duration );
        toast.setGravity( Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,
                0, 180 );
        toast.setView( layout );
        toast.show();
    }// end of method butteredToast

    /* Weather Calculations */

    /***
     * Calculate the wind chill for temperatures at or below 50° F and wind speeds above 3 mph
     *
     * @param fTemp A temperature measured in Fahrenheit
     * @param mphWind A wind speed measure in miles per hour (mph)
     * @return A integer value representing the calculated wind chill.
     */
    public static int calculateWindChill( int fTemp, int mphWind )
    {
        // The wind chill calculator only works for temperatures at or below 50 ° F
        // and wind speeds above 3 mph.
        if( fTemp > 50 || mphWind < 3 )
        {
            return fTemp;
        }// end of if block
        else
        {
            return (int) ( 35.74 + ( 0.6215 * fTemp ) - ( 35.75 * Math.pow( mphWind, 0.16 ) )
                    + (0.4275 * fTemp * Math.pow( mphWind, 0.16 ) ) );
        }// end of else block
    }// end of method calculateWindChill

    /**
     * Check to see if an {@code int} array contains a number.
     *
     * @param array     The array to be searched.
     * @param key       The number to find
     * @return          True/False dependent on the outcome of the search
     */
    public static boolean containsInt( final int[] array, final int key )
    {
        Arrays.sort( array );
        return Arrays.binarySearch( array, key ) >= 0;
    }// end of method containsInt

    /**
     * Returns a drawable using its name
     *
     * @param name The name of the drawable
     * @return  The drawable resource
     */
    public static Drawable getDrawable( String name )
    {
        int resourceId = getAppContext().getResources().getIdentifier( name, "drawable",
                getAppContext().getPackageName() );
        return ContextCompat.getDrawable( getAppContext(), resourceId );
    }// end of method getDrawable

    /**
     * Returns a drawable using its name
     *
     * @param name The name of the drawable
     * @return  The drawable resource
     */
    public static int getImageResourceId( String name )
    {
        return getAppContext().getResources().getIdentifier( name, "drawable",
                getAppContext().getPackageName() );
    }// end of method getDrawable

    /***
     * Heat index computed using air temperature F and relative humidity
     *
     * @param airTemp	The current air temperature reading
     * @param relativeHumidity The current relative humidity reading
     * @return A {@code double} representing the heat index value
     * @author Kevin Sharp and Mark Klein
     * <br />
     * {@link 'https://www.wpc.ncep.noaa.gov/html/heatindex.shtml'}
     */
    public static double heatIndex( double airTemp, double relativeHumidity )
    {
        double hi;
        double vaporPressure = 0;
        double satVaporPressure = 0;
        double airTempInFahrenheit  = 0;
        double hiTemp;
        double fpTemp = 0;
        double hiFinal;
        double adj1;
        double adj2;
        double adj;

        if( relativeHumidity > 100 )
        {
            return 0;
        }// end of if block
        else if( relativeHumidity < 0 )
        {
            return 0;
        }//end of else if block
        else if( airTemp <= 40.0 )
        {
            hi = airTemp;
        }//end of else if block
        else
        {
            hiTemp = 61.0 + ( ( airTemp - 68.0 ) * 1.2 ) + ( relativeHumidity * 0.094 );
            hiFinal = 0.5* ( airTemp + hiTemp );

            if( hiFinal > 79.0 )
            {
                hi = -42.379 + 2.04901523
                        * airTemp + 10.14333127
                        * relativeHumidity - 0.22475541
                        * airTemp * relativeHumidity
                        - 6.83783 * ( Math.pow( 10, -3 ) )
                        * ( Math.pow( airTemp, 2 ) ) - 5.481717
                        * ( Math.pow( 10, -2 ) ) * ( Math.pow( relativeHumidity, 2 ) )
                        + 1.22874 * ( Math.pow( 10, -3 ) ) * ( Math.pow( airTemp, 2 ) )
                        * relativeHumidity + 8.5282 * ( Math.pow( 10, -4 ) )
                        * airTemp * ( Math.pow( relativeHumidity, 2 ) )
                        - 1.99 * ( Math.pow( 10, -6 ) ) * ( Math.pow( airTemp, 2 ) )
                        * ( Math.pow( relativeHumidity, 2 ) );

                if( ( relativeHumidity <= 13 ) && ( airTemp >= 80.0 )
                        && ( airTemp <= 112.0 ) )
                {
                    adj1 = ( 13.0 - relativeHumidity ) / 4.0;
                    adj2 = Math.sqrt( ( 17.0 - Math.abs( airTemp - 95.0 ) ) / 17.0 );
                    adj = adj1 * adj2;
                    hi = hi - adj;
                }// end of if block
                else if( ( relativeHumidity > 85.0 ) && ( airTemp >= 80.0 )
                        && ( airTemp <= 87.0 ) )
                {
                    adj1 = ( relativeHumidity - 85.0 ) / 10.0;
                    adj2 = ( 87.0 - airTemp ) / 5.0;
                    adj = adj1 * adj2;
                    hi = hi + adj;
                }// end of else if block
            }// end of if block
            else
            {
                hi = hiFinal;
            }// end of else block
        }// end of else block

        double  tempc2 = ( airTemp - 32 ) * .556;
        double  rh2 = 1 - relativeHumidity/100;
        double  tdpc2 = tempc2 - ( ( ( 14.55 + .114* tempc2 ) * rh2 )
                + ( Math.pow( ( ( 2.5 + .007 * tempc2 ) * rh2 ), 3 ) )
                + ( ( 15.9 + .117 * tempc2 ) ) * ( Math.pow( rh2, 14 ) ) );

        return Math.round( hi );
    }// end of method heatIndex

    /***
     * Heat index computed using air temperature and dew point temperature. Degrees F
     * <br />
     * Steps to calculate:
     * <ol>
     *		<li>Convert T and Td to degrees C</li>
     * 		<li>Using T and Td, calculate the vapor pressure and saturation vapor pressure.</li>
     * 		<li>Calculate RH = (E/Es) * 100</li>
     * </ol>
     *
     * @param airTemp	The current air temperature reading
     * @param dewPoint The current dew point reading
     * @return A {@code double} representing the heat index value
     * @author Kevin Sharp and Mark Klein
     * <br />
     * {@link 'https://www.wpc.ncep.noaa.gov/html/heatindex.shtml'}
     */
    public static double heatIndexDew( double airTemp, double dewPoint )
    {
        double hi;
        double vaporPressure;
        double satVaporPressure;
        double airTempInFahrenheit  = 0;
        double relativeHumidity = 0;
        double hiTemp;
        double fpTemp = 0;
        double hiFinal;
        double adj1;
        double adj2;
        double adj;

        double tc2 = ( airTemp - 32) * .556;
        double tdc2 = ( dewPoint -32 )* .556;

        if ( tc2 < tdc2 )
        {
            return 0;
        }// end of if block
        else if( airTemp <= 40.0 )
        {
            hi = airTemp;
        }// end of else if block
        else
        {
            vaporPressure = 6.11 * ( Math.pow( 10, 7.5 * ( tdc2 / ( 237.7 + tdc2 ) ) ) );
            satVaporPressure = 6.11 * ( Math.pow( 10, 7.5 *( tc2 / ( 237.7 + tc2 ) ) ) );
            relativeHumidity = Math.round( 100.0 * ( vaporPressure / satVaporPressure ) );
            hiTemp = 61.0 + ( ( airTemp - 68.0 ) * 1.2) + ( relativeHumidity *0.094 );
            hiFinal = 0.5 * ( airTemp + hiTemp );

            if( hiFinal > 79.0 )
            {
                hi = -42.379 + 2.04901523 * airTemp
                        + 10.14333127 * relativeHumidity
                        - 0.22475541 * airTemp
                        * relativeHumidity - 6.83783
                        * ( Math.pow( 10, -3 ) ) * ( Math.pow( airTemp, 2 ) )
                        - 5.481717 * ( Math.pow( 10, -2 ) )
                        * ( Math.pow( relativeHumidity, 2 ) )
                        + 1.22874 * ( Math.pow( 10, -3 ) )
                        * ( Math.pow( airTemp, 2 ) ) * relativeHumidity
                        + 8.5282 * ( Math.pow( 10, -4 ) )
                        * airTemp * ( Math.pow( relativeHumidity, 2 ) )
                        - 1.99 * ( Math.pow( 10, -6 ) )
                        * ( Math.pow( airTemp, 2 ) ) * ( Math.pow( relativeHumidity, 2 ) );

                if( ( relativeHumidity <= 13.0 ) && ( airTemp >= 80.0 )
                        && ( airTemp <= 112.0 ) )
                {
                    adj1 = ( 13.0 - relativeHumidity ) / 4.0;
                    adj2 = Math.sqrt( ( 17.0 - Math.abs( airTemp - 95.0 ) ) / 17.0 );
                    adj = adj1 * adj2;
                    hi = hi - adj;
                }// end of if block
                else if( ( relativeHumidity > 85.0 ) && ( airTemp >= 80.0 )
                        && ( airTemp <= 87.0 ) )
                {
                    adj1 = ( relativeHumidity - 85.0 ) /10.0;
                    adj2 = ( 87.0 - airTemp ) / 5.0;
                    adj = adj1 * adj2;
                    hi = hi + adj;
                }// end of else if block
            }// end of if block
            else
            {
                hi = hiFinal;
            }// end of else block
        }// end of else block

        String answer = Math.round( hi ) + " F" +  " / "
                + Math.round( ( hi - 32 ) * .556 ) + " C";
        String relativeHumidityS = relativeHumidity + "%";

        return  Math.round( hi );
    }// end of method heatIndexDew

    /***
     * Heat index computed using air temperature C and relative humidity
     *
     * @param airTempCelsius	The current air temperature reading
     * @param relativeHumidity The current relative humidity reading
     * @return A {@code double} representing the heat index value
     * @author Kevin Sharp and Mark Klein
     * <br />
     * {@link 'https://www.wpc.ncep.noaa.gov/html/heatindex.shtml'}
     */
    public static double heatIndexCelsius( double airTempCelsius, double relativeHumidity )
    {
        double hi;
        double tempAirInFahrenheit;
        double hiTemp;
        double fpTemp;
        double hiFinal;
        double adj1;
        double adj2;
        double adj; // adj1 * adj2;

        if( relativeHumidity > 100 )
        {
            return 0;
        }// end of if block
        else if ( relativeHumidity < 0 )
        {
            return 0;
        }// end of else if block
        else if ( airTempCelsius <= 4.44 )
        {
            hi = airTempCelsius;
        }// end of else if block
        else
        {
            tempAirInFahrenheit = 1.80 * airTempCelsius+ 32.0;
            hiTemp = 61.0 + ( ( tempAirInFahrenheit - 68.0) * 1.2 ) + ( relativeHumidity * 0.094 );
            fpTemp = airTempCelsius;
            hiFinal = 0.5 * ( fpTemp + hiTemp );

            if( hiFinal > 79.0 )
            {
                hi = -42.379 + 2.04901523
                        * tempAirInFahrenheit + 10.14333127
                        * relativeHumidity - 0.22475541
                        * tempAirInFahrenheit * relativeHumidity
                        - 6.83783 * ( Math.pow( 10, -3 ) )
                        * ( Math.pow( tempAirInFahrenheit, 2 ) )
                        - 5.481717 * ( Math.pow( 10, -2 ) )
                        * ( Math.pow( relativeHumidity, 2 ) )
                        + 1.22874 * ( Math.pow( 10, -3 ) )
                        * ( Math.pow( tempAirInFahrenheit, 2 ) )
                        * relativeHumidity + 8.5282
                        * ( Math.pow( 10, -4 ) ) * tempAirInFahrenheit
                        * ( Math.pow( relativeHumidity, 2 ) )
                        - 1.99 * ( Math.pow( 10, -6 ) )
                        * ( Math.pow( tempAirInFahrenheit, 2 ) )
                        * ( Math.pow( relativeHumidity, 2 ) );

                if( ( relativeHumidity <= 13 ) && ( tempAirInFahrenheit >= 80.0 )
                        && ( tempAirInFahrenheit <= 112.0 ) )
                {
                    adj1 = ( 13.0 - relativeHumidity ) / 4.0;
                    adj2 = Math.sqrt( ( 17.0 - Math.abs( tempAirInFahrenheit - 95.0 ) ) /17.0 );
                    adj = adj1 * adj2;
                    hi = hi - adj;
                }// end of if block
                else if( ( relativeHumidity > 85.0 ) && ( tempAirInFahrenheit >= 80.0 )
                        && ( tempAirInFahrenheit <= 87.0 ) )
                {
                    adj1 = ( relativeHumidity - 85.0 ) / 10.0;
                    adj2 = ( 87.0 - tempAirInFahrenheit ) / 5.0;
                    adj = adj1 * adj2;
                    hi = hi + adj;
                }// end of else if block
            }// end of if block
            else
            {
                hi = hiFinal;
            }// end of else block
        }

        String heatIndexS = Math.round( hi ) + " F"
                + " / " + Math.round( ( hi - 32 ) * .556 ) + " C";
        double rh3 = 1 - relativeHumidity / 100;
        double tdpc3 = airTempCelsius - ( ( ( 14.55 + .114 * airTempCelsius ) * rh3 )
                + ( Math.pow( ( ( 2.5 + .007 * airTempCelsius ) * rh3 ), 3 ) )
                + ( (15.9 + .117 * airTempCelsius ) ) * ( Math.pow( rh3, 14 ) ) );
        String dewpt = Math.round( 1.80 * tdpc3 + 32.0 )
                + " F" + " / " + Math.round( tdpc3 ) + " C";

        return Math.round( ( hi - 32 ) * .556 );
    }// end of method heatIndexCelsius

    /***
     * Heat index computed using air temperature and dew point temperature. Degrees C
     *
     * @param airTempCelsius	The current air temperature reading
     * @param dewPointCelsius The current dew point reading
     * @return A {@code double} representing the heat index value
     * @author Kevin Sharp and Mark Klein
     * <br />
     * {@link 'https://www.wpc.ncep.noaa.gov/html/heatindex.shtml'}
     */
    public static double heatIndexDewCelsius( double airTempCelsius, double dewPointCelsius )
    {
        double hi;
        double vaporPressure;
        double satVaporPressure;
        double relativeHumidity = 0;
        double airTempInFahrenheit;
        double hiTemp;
        double fpTemp;
        double hiFinal;
        double adj1;
        double adj2;
        double adj;

        if ( airTempCelsius <  dewPointCelsius )
        {
            return 0;
        }// end of if block
        else if( airTempCelsius <= 4.44 )
        {
            hi = airTempCelsius;
        }// end of else if block
        else
        {
            vaporPressure = 6.11 * ( Math.pow( 10, 7.5 *
                    ( dewPointCelsius / ( 237.7 + dewPointCelsius ) ) ) );
            satVaporPressure = 6.11 * ( Math.pow( 10, 7.5 *
                    ( airTempCelsius / ( 237.7 + airTempCelsius ) ) ) );
            relativeHumidity = Math.round( 100.0 * ( vaporPressure / satVaporPressure ) );
            airTempInFahrenheit = 1.80 * airTempCelsius + 32.0;
            hiTemp = 61.0 + ( ( airTempInFahrenheit - 68.0 ) * 1.2 )
                    + ( relativeHumidity * 0.094 );
            fpTemp = airTempInFahrenheit;
            hiFinal = 0.5 * ( fpTemp + hiTemp );

            if( hiFinal > 79.0 )
            {
                hi = -42.379 + 2.04901523 * airTempInFahrenheit
                        + 10.14333127 * relativeHumidity - 0.22475541
                        * airTempInFahrenheit * relativeHumidity
                        - 6.83783 * ( Math.pow( 10, -3 ) )
                        * ( Math.pow( airTempInFahrenheit, 2 ) )
                        - 5.481717 * ( Math.pow( 10, -2 ) )
                        * ( Math.pow( relativeHumidity, 2 ) )
                        + 1.22874 * ( Math.pow( 10, -3 ) )
                        * ( Math.pow( airTempInFahrenheit, 2 ) )
                        * relativeHumidity + 8.5282
                        * ( Math.pow( 10, -4 ) )
                        * airTempInFahrenheit * ( Math.pow( relativeHumidity, 2) )
                        - 1.99 * ( Math.pow( 10, -6 ) ) * ( Math.pow( airTempInFahrenheit, 2 ) )
                        * ( Math.pow( relativeHumidity, 2 ) );

                if( ( relativeHumidity <= 13.0 ) && ( airTempInFahrenheit >= 80.0 )
                        && ( airTempInFahrenheit <= 112.0 ) )
                {
                    adj1 = ( 13.0 - relativeHumidity ) / 4.0;
                    adj2 = Math.sqrt( ( 17.0 - Math.abs( airTempInFahrenheit -95.0 ) ) /17.0 );
                    adj = adj1 * adj2;
                    hi = hi - adj;
                }// end of if block
                else if( ( relativeHumidity > 85.0 ) && ( airTempInFahrenheit >= 80.0 )
                        && ( airTempInFahrenheit <= 87.0 ) )
                {
                    adj1 = ( relativeHumidity - 85.0 ) / 10.0;
                    adj2 = ( 87.0 - airTempInFahrenheit ) / 5.0;
                    adj = adj1 * adj2;
                    hi = hi + adj;
                }// end of else if block
            }// end of if block
            else
            {
                hi = hiFinal;
            }// end of else block
        }// end of else block

        String heatDewCelsius =
                Math.round( hi ) + " F" +  " / " + Math.round( ( hi - 32 ) * .556 ) + " C";
        String heatDewCelsiusRelativeHumidity = relativeHumidity + "%";

        return Math.round( ( hi - 32 ) * .556 );
    }// end of method heatIndexDewPointCelsius

    /* Unit Conversions */

    /**
     * Accepts a numeric value of type float that represents
     * a temperature in Celsius and converts it to Fahrenheit.
     *
     * @param celsius   The temperature in Celsius
     * @return  The converted value in Fahrenheit.
     */
    public static float celsiusToFahrenheit( float celsius )
    {
        return (float)( celsius * 1.8 + 32 );
    }// end of method celsiusToFahrenheit

    /**
     * Accepts a numeric value of type float that represents
     * a temperature in Celsius and converts it to Kelvin.
     *
     * @param celsius  The temperature in Celsius
     * @return  The converted value in Kelvin.
     */
    public static double celsiusToKelvin( float celsius )
    {
        return celsius + 273.15;
    }// end of method celsiusToKelvin

    /**
     * Accepts a numeric value of type float that represents
     * a temperature in Fahrenheit and converts it to Celsius.
     *
     * @param fahrenheit The temperature in Fahrenheit.
     * @return  The converted value in Celsius.
     */
    public static float fahrenheitToCelsius( float fahrenheit )
    {
        float celsius = Math.round( ( fahrenheit - 32 ) / 1.8 );

        return Float.parseFloat( new DecimalFormat( "##.00" ).format( celsius ) );
    }// end of method fahrenheitToCelsius

    /**
     * Accepts a numeric value of type float that represents
     * a temperature in Fahrenheit and converts it to Kelvin.
     *
     * @param fahrenheit  The temperature in Fahrenheit.
     * @return  The converted value in Kelvin.
     */
    public static float fahrenheitToKelvin( float fahrenheit )
    {
        float kelvin = Math.round( ( fahrenheit + 459.67 ) * 0.5555555555555556 );

        return Float.parseFloat( new DecimalFormat( "##.00" ).format( kelvin ) );
    }// end of method fahrenheitToKelvin

    /**
     * Accepts a numeric value of type float that represents
     * a temperature in Kelvin and converts it to Celsius.
     *
     * @param kelvin  The temperature in Fahrenheit.
     * @return  The converted value in Celsius.
     */
    public static float kelvinToCelsius( float kelvin )
    {
        float celsius = Math.round( kelvin - 273.15 );

        return Float.parseFloat( new DecimalFormat( "##.00" ).format( celsius ) );
    }// end of method kelvinToCelsius

    /**
     * Accepts a numeric value of type float that represents
     * a temperature in Kelvin and converts it to Fahrenheit.
     *
     * @param kelvin  The temperature in Fahrenheit.
     * @return  The converted value in Fahrenheit.
     */
    public static float kelvinToFahrenheit( float kelvin )
    {
        float fahrenheit = Math.round( kelvin * 1.8 - 459.67 );

        return Float.parseFloat( new DecimalFormat( "##.00" ).format( fahrenheit ) );
    }// end of method kelvinToFahrenheit

    /**
     * Converts milliseconds to minutes.
     *
     * @param milliseconds  The number of milliseconds to be converted.
     * @return  The converted time value.
     */
    public static int millisecondsToMinutes( int milliseconds )
    {
        return milliseconds / 60000;
    }// end of method millisecondsToMinutes

    /**
     * Converts a minute time value to milliseconds.
     *
     * @param minutes  The number of minutes to be converted.
     * @return  The converted time value.
     */
    public static int minutesToMilliseconds( int minutes )
    {
        return minutes * 60000;
    }// end of method minutesToMilliseconds

    /**
     * Converts a double value into Km/h unit measurement
     *
     * @param mps	The value to be converted in mps
     * @return	The value after the conversion.
     */
    public static double mpsToKmh( double mps )
    {
        return mps * 3.6;
    }// end of method mpsToKmh

    /**
     * Accepts a numeric value of type double that represents
     * a rate of speed in mph (Miles per hour) and converts it to km/h (Kilometers per hour).
     *
     * @param mph  The rate of speed in mph (Miles per hour).
     * @return  The converted rate of speed value in km/h (Kilometers per hour).
     */
    public static double mphToKmh( double mph )
    {
        return mph * 1.60934;
    }// end of method mphToKmh

    /**
     *  Accepts a numeric value of type double that represents
     *  a rate of speed in kmh (Kilometers per hour) and converts it to mph (Miles per hour).
     *
     * @param kmh The rate of speed in kmh (Kilometers per hour).
     * @return  The converted rate of speed value in mph (Miles per hour).
     */
    public static double kmhToMph( double kmh )
    {
        return kmh * 0.621371;
    }// end of method kmhToMph

    /**
     * Converts a value in kilometers per hour to meters per second
     *
     * @param kmh The value in kilometers per hour
     * @return The converted value in meters per second
     */
    public static double kmhToMps( double kmh )
    {
        return kmh * 0.277778;
    }// end of method kmhToMps

    /**
     * Accepts a numeric value of type float that represents
     * a rate of speed in Mps (Meters per second) and converts it to Mph (Miles per hour).
     *
     * @param mps  The rate of speed in Mps (Meters per second).
     * @return  The converted rate of speed value in Mph (Miles per hour).
     */
    public static float mpsToMph( float mps )
    {
        float mph = Math.round( mps * 2.23694 );

        return Float.parseFloat( new DecimalFormat( "##.00" ).format( mph ) );
    }// end of method mpsToMph

    /**
     * Accepts a numeric value of type float that represents
     * a rate of speed in Mph (Miles per hour) and converts it to Mph (Meters per seconds).
     *
     * @param mph  The rate of speed in Mph (Miles per hour).
     * @return  The converted rate of speed value in Mph (Meters per seconds).
     */
    public static float mphToMps( float mph )
    {
        float mps = Math.round( mph * 0.44704 );

        return Float.parseFloat( new DecimalFormat( "##.00" ).format( mps ) );
    }// end of method mphToMps

    /* Database Methods */

    /**
     * Uses data received from the GeoNames web service in a {@code String} JSON format
     * and converts it to a {@code CityData} {@code Object}.
     *
     * @param cityJSON The city data in a JSON formatted {@code String}
     * @return	An {@code Object} of the {@code CityData} custom class
     */
    public static CityData createGeoNamesCityData( String cityJSON )
    {
        CityData currentCityData = new CityData();

        try
        {
            if( cityJSON != null )
            {
                Object json = new JSONTokener( cityJSON ).nextValue();
                String cityName;
                //String localCityName = null;
                String countyCityName = null;
                String countryName;
                String countryCode;
                String regionCode;
                String regionName;
                float Latitude;
                float Longitude;

                // Check if a JSON was returned from the web service
                if ( json instanceof JSONObject )
                {
                    // Get the full HTTP Data as JSONObject
                    JSONObject geoNamesJSON = new JSONObject( cityJSON );
                    // Get the JSONObject "geonames"
                    JSONArray geoNames = geoNamesJSON.optJSONArray( "geonames" );
                    int matchCount = geoNamesJSON.getInt( "totalResultsCount" );

                    // if the place array only contains one object, then only one
                    // match was found
                    if ( matchCount > 0 )
                    {
                        JSONObject place = geoNames.getJSONObject( 0 );

                        cityName = place.getString( "name" );
                        countryName =  place.getString( "countryName" );
                        countryCode = place.getString( "countryCode" );
                        //localCityName = place.getString( "toponymName" );
                        regionCode = place.getString( "adminCode1" );
                        regionName = countryCode.equalsIgnoreCase( "US" ) ?
                                UtilityMethod.usStatesByCode.get( regionCode ) :
                                null;
                        Latitude = Float.parseFloat( place.getString( "lat" ) );
                        Longitude = Float.parseFloat( place.getString( "lng" ) );

                        currentCityData.setCityName( cityName );
                        currentCityData.setCountryName( countryName );
                        currentCityData.setCountryCode( countryCode );
                        currentCityData.setRegionCode( regionCode );
                        currentCityData.setRegionName( regionName );
                        currentCityData.setLatitude( Latitude );
                        currentCityData.setLongitude( Longitude );

                    }// end of if block
                }// end of if block
            }// end of if block
        }// end of try block
        catch ( JSONException e )
        {
            logMessage( LogLevel.WARNING, e.getMessage(),
                    TAG + "::createGeoNamesCityData [line: " + getExceptionLineNumber( e ) + "]" );
        }// end of catch block

        return currentCityData;

    }// end of method createGeoNamesCityData

    /**
     * Uses data received from the Here Maps web service in a {@code String} JSON format
     * and converts it to a {@code CityData} {@code Object}.
     *
     * @param cityJSON The city data in a JSON formatted {@code String}
     * @return	An {@code Object} of the {@code CityData} custom class
     */
    public static CityData createHereCityData( String cityJSON )
    {
        CityData currentCityData = new CityData();

        try
        {
            if( cityJSON != null )
            {
                Object json = new JSONTokener( cityJSON ).nextValue();
                String localCityName;
                String countryName;
                String countryCode;
                String regionName;
                float Latitude;
                float Longitude;

                // Check if a JSON was returned from the web service
                if ( json instanceof JSONObject )
                {
                    // Get the full HTTP Data as JSONObject
                    JSONObject reader = new JSONObject( cityJSON );
                    // Get the JSONObject "query"
                    JSONObject response = reader.getJSONObject( "Response" );
                    JSONObject view = response.getJSONArray( "View" ).getJSONObject( 0 );
                    JSONArray places = view.optJSONArray( "Result" );
                    int matchCount = places.length();

                    // if the place array only contains one object, then only one
                    // match was found
                    if ( matchCount == 1 )
                    {
                        JSONObject place = places.getJSONObject( 0 );
                        JSONObject location = place.getJSONObject( "Location" );
                        JSONObject displayPosition = location.getJSONObject( "DisplayPosition" );
                        JSONObject navigationPosition = location.getJSONArray( "NavigationPosition" ).getJSONObject( 0 );
                        JSONObject address = location.getJSONObject( "Address" );
                        JSONArray additionalData = address.getJSONArray( "AdditionalData" );

                        countryName = UtilityMethod.toProperCase(
                                additionalData.getJSONObject( 0 ).getString( "value" ) );
                        countryCode = Objects.requireNonNull( worldCountryCodes.get( countryName ) ).toUpperCase();
                        regionName = additionalData.getJSONObject( 1 ).getString( "value" ); // "key": "StateName"

                        localCityName = countryName.equalsIgnoreCase( "USA" ) ?
                                UtilityMethod.toProperCase( address.getString( "District" ) ) :
                                UtilityMethod.toProperCase( address.getString( "City" ) );
                        Latitude = (float) navigationPosition.getDouble( "Latitude" );
                        Longitude = (float) navigationPosition.getDouble( "Longitude" );

                        currentCityData.setCityName( localCityName );
                        currentCityData.setCountryName( countryName );
                        currentCityData.setCountryCode( countryCode );
                        currentCityData.setRegionName( regionName );
                        currentCityData.setLatitude( Latitude );
                        currentCityData.setLongitude( Longitude );

                    }// end of if block
                    else
                    {
                        // Multiple matches were found
                        // Store the data in local storage
                        Gson gson = new GsonBuilder()
                                .registerTypeAdapter(
                                        HereGeoLocation.class,
                                        new HereGeoLocation.HereGeoLocationDeserializer() )
                                .create();
                        HereGeoLocation.cityGeographicalData = gson.fromJson(
                                cityJSON, HereGeoLocation.class );

                        HereGeoLocation.Response.View.Result place = HereGeoLocation.cityGeographicalData
                                .getResponse()
                                .getView()
                                .getResult().get( 0 );

                        localCityName = UtilityMethod.toProperCase( place.getLocation().getAddress().getDistrict() );
                        countryName = UtilityMethod.toProperCase( place.getLocation().getAddress().getCountry() );
                        countryCode = Objects.requireNonNull( worldCountryCodes.get( place.getLocation().getAddress().getCountry() ) ).toUpperCase();
                        regionName = place.getLocation().getAddress().getAdditionalData().get( 1 ).getValue(); // "key": "StateName"
                        Latitude = place.getLocation().getNavigationPosition().getLatitude();
                        Longitude = place.getLocation().getNavigationPosition().getLongitude();

                        currentCityData.setCityName( localCityName );
                        currentCityData.setCountryName( countryName );
                        currentCityData.setCountryCode( countryCode );
                        currentCityData.setRegionName( regionName );
                        currentCityData.setLatitude( Latitude );
                        currentCityData.setLongitude( Longitude );

                    }// end of else block
                }// end of if block
            }// end of if block
        }// end of try block
        catch ( JSONException e )
        {
            logMessage( LogLevel.SEVERE, e.getMessage(),
                    TAG + "::createHereCityData [line: " + getExceptionLineNumber( e ) + "]" );
        }// end of catch block

        return currentCityData;

    }// end of method createHereCityData

    /**
     * Saves a city to a local SQLite 3 database.
     *
     * @param cityName		The name of the city
     * @param countryName	The name of the country
     * @param countryCode	The country's corresponding two-letter country code
     * @param regionName	The name of the region in which the city is located
     * @param regionCode	The region's corresponding two-letter region code
     * @param latitude		The line of latitude value
     * @param longitude		The line of longitude value
     * @return				An {@code int} value indicating success or failure.<br /> 1 for success and 0 for failure.
     */
    public static int addCityToDatabase( String cityName, String countryName, String countryCode,
                                         String regionName, String regionCode, String timeZone,
                                         float latitude, float longitude )
    {
        SQLiteOpenHelper dbHelper = new DBHelper(getAppContext(),
                WeatherLionApplication.CITIES_DATABASE_NAME);
        SQLiteDatabase worldCitiesDB = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues( 8 );
        values.put( WorldCities.CITY_NAME, cityName );
        values.put( WorldCities.COUNTRY_NAME, countryName );
        values.put( WorldCities.COUNTRY_CODE, countryCode );
        values.put( WorldCities.REGION_NAME, regionName );
        values.put( WorldCities.REGION_CODE, regionCode );
        values.put( WorldCities.TIME_ZONE, timeZone );
        values.put( WorldCities.LATITUDE, latitude );
        values.put( WorldCities.LONGITUDE, longitude );
        values.put( WorldCities.DATE_ADDED,
                new SimpleDateFormat( "E, MMM, dd, yyyy h:mm a",
                        Locale.ENGLISH ).format( new Date() ) );

        int success = (int) worldCitiesDB.insertWithOnConflict( WorldCities.WORLD_CITIES,
                null, values, SQLiteDatabase.CONFLICT_IGNORE );
        worldCitiesDB.close();

        return success;
    }// end of method addCityToDatabase

    /**
     * Retrieves a city from a local SQLite 3 database
     *
     * @param cityName		The name of the city
     * @param regionCode	The region's corresponding two-letter region code
     * @param countryName	The name of the country
     * @return				An {@code Object} of the {@code CityData} custom class
     */
    public static CityData getCityDataFromDatabase( String cityName, String regionCode, String countryName )
    {
        SQLiteOpenHelper dbHelper = new DBHelper( getAppContext(), WeatherLionApplication.CITIES_DATABASE_NAME );
        SQLiteDatabase worldCitiesDB = dbHelper.getReadableDatabase();

        String[] columns = new String[]{ "CityName", "CountryName", "CountryCode",
                "RegionCode", "Latitude", "Longitude" };
        String selection;
        String[] selectionArgs;

        if( regionCode != null && countryName != null )
        {
            selection = "CityName = ? AND RegionCode = ? AND CountryName = ?";
            selectionArgs = new String[]{ cityName, regionCode, countryName };
        }// end of if block
        else if( regionCode != null )
        {
            selection = "CityName = ? AND RegionCode = ?";
            selectionArgs = new String[]{ cityName, regionCode };
        }// end of else if block
        else if( countryName != null )
        {
            selection = "CityName = ? AND CountryName = ?";
            selectionArgs = new String[]{ cityName, countryName };
        }// end of else if block
        else
        {
            selection = "CityName = ?";
            selectionArgs = new String[]{ cityName };
        }// end of else block

        try
        {
            Cursor cursor = worldCitiesDB.query( WorldCities.WORLD_CITIES, WorldCities.ALL_COLUMNS,
                    selection, selectionArgs, null, null, null );
            int found = 0;

            while ( cursor.moveToNext() )
            {
                found++;
            }// end of while loop

            cursor.close();
            worldCitiesDB.close();

            if( found > 0 )
            {
                return new CityData( cursor.getString( 1 ), cursor.getString( 2 ),
                        cursor.getString( 3 ), cursor.getString( 4 ),
                        cursor.getFloat( 5 ), cursor.getFloat( 6 ) );
            }// end of if block
            else
            {
                return null;
            }// end of else block
        }// end of try block
        catch( Exception e )
        {
            UtilityMethod.logMessage( LogLevel.SEVERE, e.getMessage(),
                    TAG + "::getCityDataFromDatabase [line: " +
                            UtilityMethod.getExceptionLineNumber( e )  + "]" );

            return null;
        }// end of catch block
    }// end of method getCityDataFromDatabase

    /* End of database methods */

    /* Miscellaneous Methods */

    /**
     * Round a value to 2 decimal places
     *
     * @param value The value to be rounded
     * @return  The rounded value to 2 decimal places
     */
    public static float roundValue( double value )
    {
        float rounded = Math.round( value );

        return Float.parseFloat( new DecimalFormat( "##.00" ).format( rounded ) );
    }// end of method roundValue

    /**
     * Accepts a numeric value of type float that represents
     * an angle of degree representing a compass direction.
     *
     * @param degrees  The angle of the direction
     * @return  The converted value
     */
    public static String compassDirection( float degrees )
    {
        int index = (int)( ( degrees / 22.5 ) + 0.5 );

        return compassSectors[ index % 16 ];
    }// end of method compassDirection

    /**
     * Accepts a numeric value of type long that represents
     * a Unix time value.
     *
     * @param unixTimeValue  The numerical time value
     * @return  The {@code Date} object.
     */
    public static Date getDateTime( long unixTimeValue )
    {
        return new Date( unixTimeValue * 1000L ); // *1000 is to convert seconds to milliseconds
    }// end of method getDateTime

    /**
     * Returns a {@code Date} object from a {@code String} representation of a date
     *
     * @param date	A {@code String} representation of a date
     * @return	A {@code Date} object
     */
    public static Date getDate(String date)
    {
        Date startDate = null;

        try
        {
            startDate = new SimpleDateFormat( "MM/dd/yyyy" , Locale.ENGLISH ).parse( date );
        }// end of try block
        catch (ParseException e)
        {
            butteredToast( getAppContext(), e.getMessage(), 2,Toast.LENGTH_SHORT );
        }// end of catch block

        return startDate;
    }// end of method getDateTime

    /**
     * Accepts a 24hr time and converts it to a 12hr time.
     *
     * @param hour  The 24hr clock hour time value
     * @param minute  The minute time value
     * @return  Formatted 12hr time. Example:  00:00 file return 12:00 AM.
     */
    public static String get12HourTime( int hour, int minute )
    {
        // 24 hour times might return a negative if the time-zone
        // offset is subtracted from 00 or 24hrs
        if( hour < 0 ) hour = 24 + hour;

        return String.format( Locale.ENGLISH, "%d:%s %s",
                ( hour > 12 ? hour - 12 : ( hour == 0 ? 12 : hour ) ),
                ( minute < 10 ? minute == 0 ? "00" :( "0" + minute )
                        : String.valueOf( minute ) ),
                (hour > 12 ? "PM" : "AM") );
    }// end of method get12HourTime

    /**
     * Converts a {@code String} representation of a time in 12hr format
     * to a time in 24hr format.
     *
     * @param time	A {@code String} representation of a time in 12hr format
     * @return		A {@code String} representation of a time in 24hr format
     */
    public static String get24HourTime( String time )
    {
        StringBuilder realTime = new StringBuilder( time );

        if( !realTime.toString().contains( " " ) )
        {
            int insertionPoint = time.indexOf( ":" ) + 2;

            realTime = new StringBuilder( time ).insert( time.length() - 2, " " );
        }// end of if block

        int hour = Integer.parseInt( realTime.toString().split( ":" )[ 0 ].trim() );
        int minute = Integer.parseInt( realTime.toString().split( ":" )[ 1 ].trim().split( " " )[ 0 ].trim() );
        String meridian = realTime.toString().split( " " )[ 1 ].trim();
        String t = null;

        if( meridian.equalsIgnoreCase( "am" ) )
        {
            t = String.format( Locale.ENGLISH,"%s:%d", hour < 10 ? "0" + hour : hour == 12 ? "00" : hour, minute );
        }// end of if block
        else if( meridian.equalsIgnoreCase( "pm" ) )
        {
            t = String.format( Locale.ENGLISH,"%s:%d", hour < 12 ? 12 + hour : hour, minute );
        }// end of else if block

        return t;
    }// end of method get24HourTime

    /**
     * Returns the square a number.
     *
     * @param value The value to be squared.
     * @return  The value multiplied by itself.
     */
    public static double square( double value )
    {
        return value * value;
    }// end of method square

    public static int getWindRotationSpeed( int windSpeed, String unit )
    {
        // convert to mph if necessary
        if( unit.equals( "kph" ) )
        {
            windSpeed = (int) kmhToMph( windSpeed );
        }// end of if block

        //double knots = windSpeed * 0.868974;
        int rotation = 0;

        if( windSpeed > 0 && windSpeed <= 6 )
        {
            rotation = 2200;
        }// end of if block
        else if( windSpeed >= 7 && windSpeed <= 10 )
        {
            rotation = 1800;
        }// end of else if block
        else if( windSpeed >= 7 && windSpeed <= 12 )
        {
            rotation = 1200;
        }// end of else if block
        else if( windSpeed >= 13 && windSpeed <= 19 )
        {
            rotation = 1100;
        }// end of else if block
        else if( windSpeed >= 20 && windSpeed <= 30 )
        {
            rotation = 1000;
        }// end of else if block
        else if( windSpeed >= 31 && windSpeed <= 39 )
        {
            rotation = 900;
        }// end of else if block
        else if( windSpeed >= 40 && windSpeed <= 50 )
        {
            rotation = 800;
        }// end of else if block
        else if( windSpeed >= 51 && windSpeed <= 62 )
        {
            rotation = 7000;
        }// end of else if block
        else if( windSpeed >= 63 && windSpeed <= 74 )
        {
            rotation = 600;
        }// end of else if block
        else if( windSpeed >= 75 && windSpeed <= 87 )
        {
            rotation = 500;
        }// end of else if block
        else if( windSpeed >= 88 && windSpeed <= 102 )
        {
            rotation = 400;
        }// end of else if block
        else if( windSpeed >= 103 && windSpeed <= 117)
        {
            rotation = 300;
        }// end of else if block
        else if( windSpeed >= 118)
        {
            rotation = 200;
        }// end of else if block

        return rotation;

    }// end of method getWindRotationSpeed

    /**
     * Ensures that the city entered by the user is correctly formatted.
     *
     * @param cityName A {@code String} representing the of a city.
     * @return  A boolean value of True/False dependent on the result of the test.
     */
    public static boolean isValidCityName(String cityName )
    {
        return cityName.contains( "," );
    }// end of method isValidCityName

    /***
     * Returns the number of files found in a specific path
     *
     * @param path The location to search for files
     * @return The number of files found which may include directories
     */
    public static int getFileCount( String path )
    {
        File[] files = new File( path ).listFiles();

        return files.length;
    }// end of method getFileCount

    /***
     * Locate any subdirectories found in a specific directory
     *
     * @param path A directory path which may contain subdirectories
     * @return An {@code ArrayList} containing the names of all subdirectories found in the given path
     */
    private static ArrayList<String> getSubdirectories( String path )
    {
        files = files == null ? new File( path ).listFiles() : files;

        if( files == null ) return null;

        for ( File file : files )
        {
            if ( file.isDirectory() )
            {
                subDirectoriesFound.add( file.getName() );
                files = file.listFiles();
                final ArrayList<String> subdirectories = getSubdirectories(path);// recursive call.
            }// end of if block
        }// end of for loop

        return subDirectoriesFound;
    }// end of method getSubdirectories

    /**
     * Converts a sequence to alphabetic characters to sentence case.
     *
     * @param text The {@code String} value to be converted.
     * @return		A {@code String} representation of text in a sentence case format.
     */
    public static String toProperCase( String text )
    {
        String[] sep = { " ", "-", "/", "'" };
        int cycle = text.length();
        StringBuilder sequence = new StringBuilder( text.toLowerCase() );

        for ( int i = 0; i <= cycle; i++ )
        {
            if ( i == 0 && Character.isAlphabetic( sequence.charAt( i ) ) )
            {
                sequence.replace( sequence.indexOf( Character.toString( sequence.charAt( i ) ) ),
                        sequence.indexOf( Character.toString( sequence.charAt( i ) ) ) + 1,
                        Character.toString( Character.toUpperCase( sequence.charAt( i ) ) ) );
            }// end of if block
            else if ( ( i < cycle ) && Character.isAlphabetic( sequence.charAt( i ) ) &&
                    ( sequence.charAt( i - 1 ) == 'c' && sequence.charAt( i - 2 ) == 'M' )  )
            {
                sequence.replace( sequence.indexOf( Character.toString( sequence.charAt( i ) ) ),
                        sequence.indexOf( Character.toString( sequence.charAt( i ) ) ) + 1,
                        Character.toString( sequence.charAt( i ) ).toUpperCase() );
            }// end of else if block
            else if ( ( i < cycle ) && Character.isAlphabetic( sequence.charAt( i ) ) &&
                    ( Character.toString( sequence.charAt( i - 1 ) ).equals( sep[ 0 ] ) ||
                            Character.toString( sequence.charAt( i - 1 ) ).equals( sep [ 1 ] ) ) )
            {
                sequence.replace( i, i + 1, Character.toString( sequence.charAt( i ) ).toUpperCase() );
            }// end of else if block
        }// end of for loop

        return sequence.toString();

    }// end of method toProperCase

    /**
     * Checks if a string contains a whole word on its own
     * @param input	A search {@code String}
     * @param word	A {@code String} being searched for
     * @return	True if the whole word is found, otherwise False.
     */
    public static boolean containsWholeWord( String input, String word )
    {
        return Pattern.compile( String.format( "\b%s\b", word ) ).matcher( input ).find();
    }// end of method containsWholeWord

    /**
     * Format a Uri so that is is compatible with a valid standard Uri {@code String}
     * @param uri The Uri {@code String} to be formatted
     * @return  A valid formatted Uri {@code String}
     */
    public static String escapeUriString(String uri)
    {
        String encodedString = null;

        try
        {
            encodedString = URLEncoder.encode( uri, "UTF-8" );
        }// end of try block
        catch ( UnsupportedEncodingException e )
        {
            logMessage( LogLevel.SEVERE, e.getMessage(),
                    TAG + "::escapeUriString [line: " + getExceptionLineNumber( e ) + "]" );
        }// end of catch block

        return encodedString;
    }// end of method escapeUriString

    /**
     * Determine if the device is connected to the Internet
     *
     * @param context The calling context.
     * @return True/False depending on the connection state.
     */
    public static boolean hasInternetConnection( Context context )
    {
        return NetworkHelper.hasNetworkAccess( context );
    }// end of method hasInternetConnection

    /**
     * Determines if a city was previously stored to the local storage
     *
     * @param cityName	The name of the city
     * @return	True/False dependent on the outcome of the check.
     */
    public static boolean isKnownCity( String cityName )
    {
        if( cityName.contains( "," ) )
        {
            return cityFoundInXMLStorage( cityName );
        }// end of if block

        return false;
    }// end of method isKnownCity

    /**
     * Determines if a city has been previously stored a a local SQLite 3 database.
     *
     * @param cityName	The name of the city
     * @return	True/False dependent on the outcome of the check.
     */
    public static boolean cityFoundInDatabase(String cityName )
    {
        // Check SQLite Database
        String[] city = cityName.split( "," );
        String[] columns = new String[]{"CityName", "CountryName", "CountryCode",
                "RegionCode", "Latitude", "Longitude"};
        String selection = city[ 1 ].trim().length() == 2
                ? "CityName = ? AND RegionCode = ? AND typeof(RegionCode) = 'integer'"
                : "CityName = ? AND CountryName = ?";
        String[] selectionArgs = new String[]{ city[ 0 ].trim(), city[ 1 ].trim() };

        SQLiteOpenHelper dbHelper = new DBHelper( getAppContext(),
                WeatherLionApplication.CITIES_DATABASE_NAME );
        SQLiteDatabase worldCitiesDB = dbHelper.getReadableDatabase();
        int found = 0;

        try
        {
            // return the number of rows affected if a whereClause is passed in, 0 otherwise.
            Cursor cursor = worldCitiesDB.query(true, WorldCities.WORLD_CITIES, columns,
                    selection, selectionArgs, null, null, null, null );

            while ( cursor.moveToNext() )
            {
                found++;
            }// end of while loop

            cursor.close();
            worldCitiesDB.close();
        }// end of try block
        catch( Exception e )
        {
            logMessage( LogLevel.SEVERE, e.getMessage(),
                    TAG + "::cityFoundInDatabase [line: " +
                            getExceptionLineNumber( e )  + "]" );
        }// end of catch block

        return found > 0;
    }// end of method cityFoundInDatabase

    /***
     * Test if a {@code String} value is a number
     *
     * @param value A {@code String} value to be tested
     * @return A {@code boolean} true/false depending on the result of the test
     */
    public static boolean isNumeric( String value )
    {
        return value != null && value.matches( "[-+]?\\d*\\.?\\d+" );
    }// end of method isNumeric

    /**
     * Uses the Geo Names web service to determine if a city actually exists.
     *
     * @param cityName The name of the city to be checked
     * @param context   The calling context.
     */
    public static void findGeoNamesCity( String cityName, Context context )
    {
        int maxRows = 100;

        // All spaces must be replaced with the + symbols for the HERE Maps web service
        if( cityName.contains( " " ) )
        {
            cityName = cityName.replace( " ", "+" );
        }// end of if block

        // All commas must be replaced with the + symbols for the HERE Maps web service
        if( cityName.contains( "," ) )
        {
            cityName = cityName.replace( ",", "+" );
        }// end of if block

        String cityUrl =
                "http://api.geonames.org/searchJSON?name_equals=" +
                        escapeUriString( cityName.toLowerCase() ) +
                        "&maxRows=" + maxRows +
                        "&username=" + WidgetUpdateService.geoNameAccount;

        if ( hasInternetConnection( getAppContext() ) )
        {
            //run an background service
            Intent intent = new Intent( context, CityDataService.class );
            intent.setData( Uri.parse( cityUrl ) );
            context.startService( intent );
        }// end of if block
        else
        {
            butteredToast( getAppContext(), "No Internet Connection.", 2, Toast.LENGTH_SHORT );
        }// end of else block
    }// end of method findGeoNamesCity

    /**
     * Uses the Here Maps web service to determine if a city actually exists.
     *
     * @param cityName The name of the city to be checked
     * @param context   The calling context.
     */
    public static void findHereCity( String cityName, Context context )
    {
        // All spaces must be replaced with the + symbols for the HERE Maps web service
        if( cityName.contains( " " ) )
        {
            cityName = cityName.replace( " ", "+" );
        }// end of if block

        // All commas must be replaced with the + symbols for the HERE Maps web service
        if( cityName.contains( "," ) )
        {
            cityName = cityName.replace( ",", "+" );
        }// end of if block

        String cityUrl =
                "https://geocoder.api.here.com/6.2/geocode.json?"
                        + "app_id=" + WidgetUpdateService.hereAppId
                        + "&app_code=" + WidgetUpdateService.hereAppCode
                        + "&searchtext=" + escapeUriString( cityName.toLowerCase() );

        if ( hasInternetConnection( getAppContext() ) )
        {
            //run an background service
            Intent intent = new Intent( context, CityDataService.class );
            intent.setData( Uri.parse( cityUrl ) );
            context.startService( intent );
        }// end of if block
        else
        {
            butteredToast( getAppContext(), "No Internet Connection.", 2, Toast.LENGTH_SHORT );
        }// end of else block
    }// end of method findHereCity

    /**
     * Returns a specified paramater from a URL string
     *
     * @param field The parameter to be found
     * @param url   The URL to search
     * @return  A string value representing the parameter value
     */

    public static String getUrlParameter( String field, String url )
    {
        int len = url.length();
        String query = url.substring( url.indexOf( "?" ) + 1, len );
        String[] queryParams = query.split( "&" );
        HashMap<String, String> map = new HashMap<>();

        for ( String param : queryParams )
        {
            String[] paramsMap = param.split( "=" );
            String name = paramsMap[ 0 ];

            if( paramsMap.length > 1 )
            {
                map.put( name, paramsMap[ 1 ] );
            }// end of if block
        }// end of for loop

        return map.get( field );
    }// end of method getUrlParameter

    /**
     * Uses the GeoNames web service to return the geographical location of a city using it's name.
     *
     * @param wxLocation The location of the city to be found.
     * @return  A {@code String} representation of a JSON {@code Object} returned from the web service.
     */
    public static String retrieveGeoNamesGeoLocationUsingAddress( String wxLocation )
    {
        int maxRows = 10;
        String strJSON = null;
        String ps;
        StringBuilder fileData = new StringBuilder();

        if ( wxLocation != null )
        {
            wxLocation = wxLocation.contains( "," ) ?
                    wxLocation.substring( 0, wxLocation.indexOf( ",") ).toLowerCase() :
                    wxLocation;
            ps = String.format( "%s%s%s", "gn_sd_", wxLocation.replaceAll( " ", "_" ), ".json" );
            WeatherLionApplication.previousCitySearchFile = getAppContext().getFileStreamPath( ps );

            if( WeatherLionApplication.previousCitySearchFile.exists() )
            {
                try(
                        FileReader fr = new FileReader( WeatherLionApplication.previousCitySearchFile );	// declare and initialize the file reader object
                        BufferedReader br = new BufferedReader( fr ) 	// declare and initialize the buffered reader object
                )
                {
                    String line;

                    while( ( line = br.readLine() ) != null )
                    {
                        fileData.append( line );
                    }// end of while loop

                    strJSON = fileData.toString();
                }// end of try block
                catch ( IOException e )
                {
                    logMessage( LogLevel.SEVERE, e.getMessage(),
                            TAG + "::handleWeatherData [line: " +
                                    getExceptionLineNumber( e )  + "]" );
                }// end of catch block
            }// end of if block
            else
            {
                String geoUrl =
                        "http://api.geonames.org/searchJSON?" +
                                "name_equals=" + wxLocation.toLowerCase() +
                                "&maxRows=" + maxRows +
                                "&username=" + WidgetUpdateService.geoNameAccount;

                if ( hasInternetConnection( getAppContext() ) )
                {
                    try
                    {
                        strJSON = HttpHelper.downloadUrl( geoUrl, false );
                    }// end of try block
                    catch ( IOException e )
                    {
                        logMessage( LogLevel.SEVERE, e.getMessage(),
                                TAG + "::retrieveGeoNamesGeoLocationUsingAddress [line: " +
                                        getExceptionLineNumber(e) + "]" );
                    }// end of catch block

                }// end of if block
                else
                {
                    butteredToast( getAppContext(), "No Internet Connection.", 2, Toast.LENGTH_SHORT );
                }// end of else block
            }// end of else block
        }// end of if block

        // Return the data from specified url
        return strJSON;

    }// end of method retrieveGeoNamesGeoLocationUsingAddress

    /**
     * Uses the Here Maps web service to return the geographical location of a city using it's name.
     *
     * @param wxLocation The location of the city to be found.
     * @return  A {@code String} representation of a JSON {@code Object} returned from the web service.
     */
    public static String retrieveHereGeoLocationUsingAddress( String wxLocation )
    {
        // All spaces must be replaced with the + symbols for the HERE Maps web service
        if( wxLocation.contains( " " ) )
        {
            wxLocation =  wxLocation.replace( " ", "+" );
        }// end of if block

        // All commas must be replaced with the + symbols for the HERE Maps web service
        if( wxLocation.contains( "," ) )
        {
            wxLocation = wxLocation.replace( ",", "+" );
        }// end of if block

        String strJSON = null;
        String geoUrl =
                "https://geocoder.api.here.com/6.2/geocode.json?"
                        + "app_id=" + WidgetUpdateService.hereAppId
                        + "&app_code=" + WidgetUpdateService.hereAppCode
                        + "&searchtext=" + escapeUriString( wxLocation.toLowerCase() );

        if ( hasInternetConnection( getAppContext()) )
        {
            try
            {
                strJSON = HttpHelper.downloadUrl( geoUrl, false );
            }// end of try block
            catch ( IOException e )
            {
                logMessage( LogLevel.SEVERE, e.getMessage(),
                        TAG + "::retrieveHereGeoLocationUsingAddress [line: " +
                                getExceptionLineNumber( e ) + "]" );
            }// end of catch block

        }// end of if block
        else
        {
            butteredToast( getAppContext(), "No Internet Connection.",
                    2, Toast.LENGTH_SHORT );
        }// end of else block

        // Return the data from specified url
        return strJSON;

    }// end of method retrieveHereGeoLocationUsingAddress

    /**
     * Uses the GeoNames web service to return the timezone details of a city using it's coordinates.
     *
     * @param lat The latitude coordinates.
     * @param lng The longitude coordinates.
     * @return  A {@code TimeZoneInfo} representation of a XML {@code Object} returned from the web service.
     */
    public static TimeZoneInfo retrieveGeoNamesTimeZoneInfo( float lat, float lng )
    {
        String strXML;
        TimeZoneInfo timeZoneInfo = null;

        String geoUrl =
                "http://api.geonames.org/timezone?" +
                        "lat=" + lat +
                        "&lng=" + lng +
                        "&username=" + WidgetUpdateService.geoNameAccount;

        if ( hasInternetConnection( getAppContext() ) )
        {
            try
            {
                strXML = HttpHelper.downloadUrl( geoUrl, false );
                timeZoneInfo = TimeZoneInfo.deserializeTimeZoneXML(
                        Objects.requireNonNull( strXML ) );
            }// end of try block
            catch ( IOException e )
            {
                logMessage( LogLevel.SEVERE, e.getMessage(),
                        TAG + "::retrieveGeoNamesTimeZoneInfo [line: " +
                                getExceptionLineNumber(e) + "]" );
            }// end of catch block

        }// end of if block
        else
        {
            butteredToast( getAppContext(), "No Internet Connection.", 2, Toast.LENGTH_SHORT );
        }// end of else block

        return timeZoneInfo;
    }// end of method retrieveGeoNamesTimeZoneInfo

    /**
     * Returns the line number were the exception occurred in the code.
     *
     * @param e		The exception that was thrown by the compiler.
     * @return		The line number at which the exception was thrown.
     */
    public static int getExceptionLineNumber( Exception e )
    {
        return e.getStackTrace()[ 1 ].getLineNumber();
    }// end of method getExceptionLineNumber

    /***
     * Use the logger to log messages locally
     *
     * @param level	Level of the log
     * @param message	Message to be logged
     * @param inMethod	The method in which the data required logging
     */
    public static void logMessage( LogLevel level, String message, String inMethod )
    {
        // log based on the specified log level
        switch ( level )
        {
            case INFO:
                Log.i( TAG, inMethod + " -> "  + message );
                break;
            case SEVERE:
                Log.e( TAG, inMethod + " -> "  + message );
                break;
            case WARNING:
                Log.w( TAG, inMethod + " -> "  + message );
                break;
            default:
                Log.v( TAG, inMethod + " -> "  + message );
                break;
        }// end of switch block
    }// end of method logMessage

    /**
     * Uses a webservice to return the geographical location of a city using it's name.
     *
     * @param wxLocation The location of the city to be found.
     * @return  A {@code String} representation of a JSON {@code Object} returned from the web service.
     */
    public static String retrieveGoogleGeoLocationUsingAddress(String wxLocation)
    {
        String strJSON = null;

        String geoUrl =
                "http://maps.googleapis.com/maps/api/geocode/json?address="+
                        escapeUriString(wxLocation.toLowerCase()) + "&sensor=false";

        if ( hasInternetConnection( getAppContext() ) )
        {
            try
            {
                strJSON = HttpHelper.downloadUrl( geoUrl, false );
            }// end of try block
            catch (IOException e)
            {
                e.printStackTrace();
            }// end of catch block

        }// end of if block
        else
        {
            butteredToast( getAppContext(),
                    "Network not available!", 2, Toast.LENGTH_SHORT );

        }// end of else block

        // Return the data from specified url
        return strJSON;

    }// end of method retrieveGoogleGeoLocationUsingAddress

    /**
     * Uses a webservice to return the geographical location of a city using it's name.
     *
     * @param wxLocation The location of the city to be found.
     * @return  A {@code String} representation of a JSON {@code Object} returned from the web service.
     */
    public static String retrieveYahooGeoLocationUsingAddress(String wxLocation)
    {
        String strJSON = null;

        String geoUrl =
                "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20geo.places%20where%20text%3D%22"+
                        escapeUriString(wxLocation.toLowerCase()) +
                        "%22&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";

        if ( hasInternetConnection( getAppContext() ) )
        {
            try
            {
                strJSON = HttpHelper.downloadUrl( geoUrl, false );
            }// end of try block
            catch ( IOException e )
            {
                logMessage( LogLevel.SEVERE, e.getMessage(), TAG +
                        "::retrieveYahooGeoLocationUsingAddress" );
            }// end of catch block

        }// end of if block
        else
        {
            butteredToast( getAppContext(),
                    "Network not available!", 2, Toast.LENGTH_SHORT );

        }// end of else block

        // Return the data from specified url
        return strJSON;

    }// end of method retrieveGoogleGeoLocationUsingAddress

    /**
     * Uses a webservice to return the geographical location of a city using its coordinates.
     *
     * @param lat   The line of latitude that the city is located
     * @param lng   The line of longitude that the city is located
     * @return  A {@code String} representation of a JSON {@code Object} returned from the web service.
     */
    public static String retrieveGoogleGeoLocationUsingCoordinates(double lat, double lng)
    {
        String strJSON = null;

        String geoUrl =
                "http://maps.googleapis.com/maps/api/geocode/json?latlng="+lat+","+lng+"&sensor=false";

        if ( hasInternetConnection( getAppContext() ) )
        {
            try
            {
                strJSON = HttpHelper.downloadUrl( geoUrl, false );
            }// end of try block
            catch ( IOException e )
            {
                logMessage( LogLevel.SEVERE, e.getMessage(), TAG +
                        "::retrieveGoogleGeoLocationUsingCoordinates" );
            }// end of catch block

        }// end of if block
        else
        {
            butteredToast( getAppContext(),
                    "Network not available!", 2, Toast.LENGTH_SHORT );

        }// end of else block

        // Return the data from specified url
        return strJSON;

    }// end of method retrieveGoogleGeoLocationUsingCoordinates

    /**
     * Determine if the widget needs to be refreshed based on the specified refresh period.
     *
     * @param context The calling context.
     * @return  True/False depending on the result of the check.
     */
    public static boolean updateRequired( Context context )
    {
        if( lastUpdated == null )
        {
            return true;
        }// end of if block

        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences( context );
        int interval = Integer.parseInt( Objects.requireNonNull(
                spf.getString( WeatherLionApplication.UPDATE_INTERVAL,
                        context.getString( R.string.default_update_interval ) ) ) );

        //milliseconds
        long difference = new Date().getTime() - lastUpdated.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;

        long elapsedMinutes = difference / minutesInMilli;
        //difference = difference % minutesInMilli;

        if( refreshRequestedBySystem && elapsedMinutes < 1 )
        {
            return false;
        }// end of if block
        else
        {
            return elapsedMinutes >= millisecondsToMinutes( interval );
        }// end of else block
    }// end of method updateRequired

    /**
     * Retrieve a city information from the GeoNames web service using
     * the devices GPS radio coordinates.
     *
     * @param lat   The latitude coordinate.
     * @param lng   The longitude coordinate.
     */
    private static void retrieveGpsLocation( String lat, String lng )
    {
        Intent intent = new Intent( getAppContext(), CityDataService.class );

        String geoUrl =
                "http://api.geonames.org/findNearbyPlaceNameJSON?" +
                        "lat=" + lat +
                        "&lng=" + lng +
                        "&username=" + WidgetUpdateService.geoNameAccount;
        intent.setData( Uri.parse( geoUrl ) );
        getAppContext().startService( intent );
    }// end of method retrieveGpsLocation

    /**
     * Uses device's GPS radio to determine the current city location of the connection.
     */
    public static void getGPSCityLocation(boolean listRequired)
    {
        double latitude;
        double longitude;

        /* This just set a flag so that the CityDataService does not build a list
         * for the usual city search.
         */
        listRequested = listRequired;

        LocationTrackerService locationTrackerService = new LocationTrackerService(getAppContext());

        if (locationTrackerService.canGetLocation())
        {
            latitude = locationTrackerService.getLatitude();
            longitude = locationTrackerService.getLongitude();
            retrieveGpsLocation( String.valueOf( latitude ), String.valueOf( longitude ) );
            locationTrackerService.stopListener();

            String geoUrl =
                    "http://api.geonames.org/findNearbyPlaceNameJSON?" +
                            "lat=" + latitude +
                            "&lng=" + longitude +
                            "&username=" + WidgetUpdateService.geoNameAccount;

            Intent intent = new Intent( getAppContext(), GeoLocationService.class);
            intent.setData(Uri.parse(geoUrl));
            getAppContext().startService(intent);
        }// end of if block
    }// end of method getGPSCityLocation

    /***
     * Searches for the number of times that a {@code char} is found in a {@code String}
     *
     * @param c			   	The {@code char} to look for in a {@code String}
     * @param checkString 	The {@code String} that contains the specified {@code char}
     * @return				An {@code int} representing the number of occurrences of the search character
     */
    public static int numberOfCharacterOccurrences(char c, String checkString )
    {
        int cn = 0;

        for ( int i = 0; i < checkString.length(); i++ )
        {
            if ( c == checkString.charAt( i ) )
            {
                cn++;
            }// end of if block
        }// end of for each loop

        return cn;
    }// end of method numberOfCharacterOccurrences

    /**
     * Checks to ensure that calls to each service provider does not exceed a limit
     *
     * @param provider  The selected weather provider
     * @return  A value of true/false dependent on the outcome of the check
     */

    public static boolean okToUseService( String provider )
    {
        boolean ok = false;

        Map<String, Object> importedServiceLog = JSONHelper.importPreviousLogs(
                getAppContext().getFileStreamPath(
                        WeatherLionApplication.SERVICE_CALL_LOG ).toString() );
        String date = (String) importedServiceLog.get( "Date" );
        Date logDate = null;
        Map importedServiceMap = (LinkedTreeMap) Objects.requireNonNull( importedServiceLog )
                .get( "Service" );

        SimpleDateFormat ldf = new SimpleDateFormat( "MMM dd, yyyy hh:mm:ss a",
                Locale.ENGLISH );
        SimpleDateFormat sdf = new SimpleDateFormat( "MMM dd, yyyy", Locale.ENGLISH );

        int callCount;

        try
        {
            logDate = ldf.parse( date );
        } // end of try block
        catch ( ParseException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE , e.getMessage(),
                    TAG + "::okToUseService [line: " +
                            e.getStackTrace()[ 1 ].getLineNumber() + "]" );
        }// end of catch block

        // if we are working with today's log which we should be
        if( sdf.format( logDate ).equals( sdf.format( new Date() ) ) )
        {
            if( importedServiceMap != null )
            {
                callCount = (int) (double) importedServiceMap.get( provider );
                ok = callCount < WeatherLionApplication.DAILY_CALL_LIMIT;
            }// end of if block
        }// end of if block
        else
        {
            WeatherLionApplication.callMethodByName( WeatherLionApplication.class,
                    "createServiceCallLog",
                    null, null );

            ok = true;
        }// end of else block

        return ok;
    }// end of method okToUseService

    /**
     * Updates a call made to a provider
     *
     * @param provider The selected weather provider
     */
    public static void serviceCall( String provider )
    {
        Map<String, Object> importedServiceLog = JSONHelper.importPreviousLogs(
                getAppContext().getFileStreamPath(
                        WeatherLionApplication.SERVICE_CALL_LOG ).toString() );
        String date = (String) importedServiceLog.get( "Date" );
        Date logDate = null;
        Map importedServiceMap = (LinkedTreeMap) Objects.requireNonNull( importedServiceLog )
                .get( "Service" );

        SimpleDateFormat ldf = new SimpleDateFormat( "MMM dd, yyyy hh:mm:ss a",
                Locale.ENGLISH );
        SimpleDateFormat sdf = new SimpleDateFormat( "MMM dd, yyyy", Locale.ENGLISH );

        int callCount;

        try
        {
            logDate = ldf.parse( date );
        } // end of try block
        catch ( ParseException e )
        {
            UtilityMethod.logMessage( UtilityMethod.LogLevel.SEVERE , e.getMessage(),
                    TAG + "::serviceCall [line: " +
                            e.getStackTrace()[ 1 ].getLineNumber() + "]" );
        }// end of catch block

        Map<String, Object> exportServiceLog = new HashMap<>();

        // if we are working with today's log which we should be
        if( sdf.format( logDate ).equals( sdf.format( new Date() ) ) )
        {
            if( importedServiceMap != null )
            {
                exportServiceLog.put( "Date", new Date() );
                exportServiceLog.put( "Service", importedServiceMap );

                callCount = (int) (double) importedServiceMap.get( provider );

                if( callCount < WeatherLionApplication.DAILY_CALL_LIMIT )
                {
                    importedServiceMap.put( provider, ++callCount );

                    Gson gson = new GsonBuilder().create();

                    // return the JSON string array as a string
                    String json = gson.toJson( exportServiceLog );

                    if( JSONHelper.updateJSONFile( json,
                            getAppContext().getFileStreamPath(
                                    WeatherLionApplication.SERVICE_CALL_LOG ).toString() ) )
                    {
                        logMessage( LogLevel.INFO,
                                "Service log updated!",
                                TAG + "::serviceCall" );
                    }// end of if block
                    else
                    {
                        logMessage( LogLevel.SEVERE,
                                "Service log could not be updated!",
                                TAG + "::serviceCall" );
                    }// end of else block
                }// end of if block
            }// end of if block
        }// end of if block
        else
        {
            WeatherLionApplication.callMethodByName( WeatherLionApplication.class,
                    "createServiceCallLog",
                    null, null );
        }// end of else block
    }// end of method serviceCall

    /***
     * Replace the last occurrence of a {@code String} contained in another {@code String}
     *
     * @param find		The {@code String} to look for in another {@code String}
     * @param replace	The replacement {@code String}
     * @param string	The {@code String} that contains another {@code String}
     * @return			A modified {@code String} reflecting the requested change
     */
    public static String replaceLast( String find, String replace, String string )
    {
        int lastIndex = string.lastIndexOf( find );

        if ( lastIndex == -1 )
        {
            return string;
        }// end of if block

        String beginString = string.substring( 0, lastIndex );
        String endString = string.substring( lastIndex + find.length() );

        return beginString + replace + endString;
    }// end of method replaceLast

    /***
     * Displays a message box prompt to the user
     *
     * @param message Additional {@code String} representing the missing asset
     */
    public static void missingRequirementsPrompt( String message )
    {
        // prompt the user
        butteredToast( getAppContext(), message, 2, Toast.LENGTH_LONG );

        // log message
        logMessage( LogLevel.SEVERE, "Missing: " + message, TAG + "::missingAssetPrompt" );
    }// end of method missingAssetPrompt

    /**
     * Display a dialog with a specific message
     * @param message   The message to be displayed in the alert dialog
     */
    public static void showMessageDialog( String message, String title, Context c )
    {
        final View messageDialogView = View.inflate( c, R.layout.wl_message_dialog, null );
        final AlertDialog messageDialog = new AlertDialog.Builder( c ).create();
        messageDialog.setView( messageDialogView );
        TextView txvTitle = messageDialogView.findViewById( R.id.txvDialogTitle );
        TextView txvMessage = messageDialogView.findViewById( R.id.txvMessage );

        txvTitle.setText( title );
        txvMessage.setText( message );

        RelativeLayout rlTitleBar = messageDialogView.findViewById( R.id.rlDialogTitleBar );
        rlTitleBar.setBackgroundColor( WeatherLionApplication.systemColor.toArgb() );

        messageDialogView.findViewById( R.id.btnOk ).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                messageDialog.dismiss();
            }
        });

        loadCustomFont( (RelativeLayout) messageDialogView.findViewById( R.id.rlMessageDialog ) );

        messageDialog.show();
    }// end of method showMessageDialog

    /**
     * Returns an RGB Color which corresponds with a temperature
     *
     * @param t The temperature to be tested
     * @return A new {@code Color}
     */
    public static int temperatureColor(int t )
    {
        int c;
        t = WeatherLionApplication.storedPreferences.getUseMetric()
                ? (int) celsiusToFahrenheit( t )
                : t;

        if( t <= 40 )
        {
            c = Color.rgb( 45, 99, 252 ); // Cold
        }// end of if block
        else if( Math.abs( t - 60 ) + Math.abs( 41 - t ) == Math.abs( 41 - 60 ) )
        {
            c = Color.rgb( 151, 205, 251 ); // Chilly
        }// end of else if block
        else if( Math.abs( t - 70 ) + Math.abs( 84 - t ) == Math.abs( 84 - 70 ) )
        {
            c = Color.rgb( 152, 211, 0 ); // Warm
        }// end of else if block
        else if( t > 85 )
        {
            c = Color.rgb( 253, 0, 3 ); // Hot
        }// end of else if block
        else
        {
            c = Color.rgb( 0, 172, 74 ); // Normal
        }// end of else block

        return c;
    }// end of method temperatureColor

    /**
     * Retrieves a specific preference value.
     *
     * @param property	The name of the preference to be retrieved
     */
    public static String getPrefValues(String property )
    {
        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences( getAppContext() );

        // get the preference value and use it

        return spf.getString( property, null );
    }// end of method getPrefValues()

    /**
     * Determines if a city has been previously stored a a local XML file.
     *
     * @param cityName	The name of the city
     * @return	True/False dependent on the outcome of the check.
     */
    public static boolean cityFoundInXMLStorage(String cityName )
    {
        boolean found = false;

        if( new File( XMLHelper.PREVIOUSLY_FOUND_CITIES_XML ).exists() )
        {
            try
            {
                FileInputStream fis = new FileInputStream( XMLHelper.PREVIOUSLY_FOUND_CITIES_XML );
                //XML file search
                SAXBuilder builder = new SAXBuilder();

                // just in case the document contains unnecessary white spaces
                builder.setIgnoringElementContentWhitespace( true );

                // download the document from the URL and build it
                Document document = builder.build( fis );

                fis.close();

                // get the root node of the XML document
                Element rootNode = document.getRootElement();

                List< Element > list = rootNode.getChildren( "City" );

                for ( int i = 0; i < list.size(); i++ )
                {
                    Element node = list.get( i );
                    String cCityName = node.getChildText( "CityName" );
                    String cRegionName = node.getChildText( "RegionName" );
                    String cRegionCode = node.getChildText( "RegionCode" );
                    String cCountryName = node.getChildText( "CountryName" );
                    boolean containsNumber = isNumeric( cRegionCode );

                    if( cityName.equalsIgnoreCase( cCityName + ", " + cCountryName ) ||
                            !containsNumber && cityName.equalsIgnoreCase( cCityName + ", " + cRegionCode ) )
                    {
                        found = true;
                        logMessage( LogLevel.INFO,  cityName + " was found in the XML storage.",
                                TAG + "::cityFoundInXMLStorage" );
                    }// end of if block
                }// end of for loop

            }// end of try block
            catch ( IOException | JDOMException io )
            {
                logMessage( LogLevel.SEVERE, io.getMessage(),
                        TAG + "::cityFoundInXMLStorage [line: " + getExceptionLineNumber( io ) + "]" );
            }// end of catch block
        }// end of if block

        return found;
    }// end of method cityFoundInXMLStorage

    /**
     * Determines if a city has been previously stored a a local JSON file.
     *
     * @param cityName	The name of the city
     * @return	True/False dependent on the outcome of the check.
     */
    public static CityData cityFoundInJSONStorage(String cityName )
    {
        String[] city = cityName.split( "," );

        //JSON File Search
        if( new File( JSONHelper.PREVIOUSLY_FOUND_CITIES_JSON ).exists() )
        {
            Gson gson = new Gson();
            JSONHelper.cityDataList = JSONHelper.importPreviousCitySearches();
            //convert the list to a JSON string
            String jsonString = gson.toJson( JSONHelper.cityDataList );

            try
            {
                jsonString =
                        new String( Files.readAllBytes( Paths.get( JSONHelper.PREVIOUSLY_FOUND_CITIES_JSON ) ) );
            }// end of try block
            catch ( IOException e )
            {
                logMessage( LogLevel.SEVERE, e.getMessage(),
                        TAG + "::cityFoundInJSONStorage [line: " + getExceptionLineNumber( e ) + "]" );
            }// end of catch block

            if ( jsonString != null )
            {
                // convert the file JSON into a list of objects
                List< CityData > cityDataList = gson.fromJson( jsonString, new TypeToken<List<CityData >>() {}.getType() );

                for ( CityData c : cityDataList )
                {
                    String cCityName = c.getCityName();
                    String cRegionName = c.getRegionName();
                    String cRegionCode = c.getRegionCode();
                    String cCountryName = c.getCountryName();
                    boolean containsNumber = isNumeric( cRegionCode );

                    if( cityName.equalsIgnoreCase( cCityName + ", " + cCountryName ) ||
                            !containsNumber && cityName.equalsIgnoreCase( cCityName + ", " + cRegionCode ) )
                    {
                        logMessage( LogLevel.INFO,  cityName + " was found in the JSON storage.",
                                TAG + "::cityFoundInJSONStorage" );

                        return c;
                    }// end of if block
                }// end of for each loop
            }// end of if block
        }// end of if block

        return null;
    }// end of method cityFoundInJSONStorage

    /**
     * Determines whether a file if stored internally.
     *
     * @param fileName The name of the file in question.
     * @return  A value of true/false dependent on the outcome of the test.
     */
    public static boolean getInternalFile(String fileName)
    {
        return getAppContext().getFileStreamPath(fileName).exists();
    }// end of method getInternalFile

    /**
     * Read all contents from a file and return it as a string.
     *
     * @param filePath	The path to the file to be read.
     * @return	A {@code String} representation of the file contents.
     */
    public static String readAll( String filePath )
    {
        StringBuilder fileContents = new StringBuilder();

        try(
                FileReader fr = new FileReader( filePath );    // declare and initialize the file reader object
                BufferedReader br = new BufferedReader( fr )    // declare and initialize the buffered reader object
        )
        {
            String line;	// variable which will store the contents of each line read

            try
            {
                while( ( line = br.readLine() ) != null )	// loop and read each line in the file until we have no more
                {
                    fileContents.append( line );
                }// end of while block
            }// end of try block
            catch( IOException e )
            {
                logMessage( LogLevel.SEVERE, e.getMessage(),
                        TAG + "::readAll [line: " +
                                UtilityMethod.getExceptionLineNumber( e )  + "]" );
            }// end of catch block

        }// end of the try with resources block
        // end of catch block
        catch( IOException e )
        {
            logMessage( LogLevel.SEVERE, e.getMessage(),
                    TAG + "::readAll [line: " +
                            UtilityMethod.getExceptionLineNumber( e )  + "]" );
        }// end of catch block

        return fileContents.toString().trim();
    }// end of method readAll

    /**
     * Get the duration of time that has elapsed since a certain date.
     *
     * @param pastDate The date in the past to be compared to.
     * @return  A {@code String} representing the time frame that has passed.
     */
    public static String getTimeSince( Date pastDate )
    {
        //milliseconds
        long difference = new Date().getTime() - pastDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;

        long elapsedMinutes = difference / minutesInMilli;
        String timeElapsed;

        if( elapsedMinutes >= 60 && elapsedMinutes < 1440 )
        {
            // an hour or more
            int hours = (int) elapsedMinutes / 60;
            timeElapsed = hours > 1 ? hours + " hours ago" : hours + " hour ago";
        }// end of if block
        else if( elapsedMinutes >= 1440 && elapsedMinutes < 10080 )
        {
            // a day or more
            int days = (int) elapsedMinutes / 1440;
            timeElapsed = days > 1 ? days + " days ago" : days + " day ago";
        }// end of if block
        else if( elapsedMinutes >= 10080 && elapsedMinutes < 43830)
        {
            // a week or more
            int weeks = (int) elapsedMinutes / 10080;
            timeElapsed = weeks > 1 ? weeks + " weeks ago" : weeks + " week ago";
        }// end of if block
        else if( elapsedMinutes >= 43830 && elapsedMinutes < 525960 )
        {
            // a month or more
            int months = (int) elapsedMinutes / 1440;
            //timeElapsed = months > 1 ? (months + " months ago") : (months + " month ago");
            timeElapsed = months + " months ago";
        }// end of if block
        else if( elapsedMinutes >= 525960 )
        {
            // a year or more
            int years = (int) elapsedMinutes / 525960;
            timeElapsed = years > 1 ? years + " years ago" : years + " year ago";
        }// end of if block
        else
        {
            int seconds = (int) elapsedMinutes / 60;

            if( elapsedMinutes < 1 )
            {
                // time in seconds
                timeElapsed = seconds > 1 ? seconds + " seconds ago" : " just now";
            }// end of if block
            else
            {
                // time in minutes
                timeElapsed = elapsedMinutes > 1 ? elapsedMinutes + " minutes ago" : elapsedMinutes + " minute ago";
            }// end of else block
        }// end of else block

        return timeElapsed;
    }// end of method getTimeSince

    public static void loadCustomFont( ViewGroup view )
    {
        for ( int i = 0; i < view.getChildCount(); i++ )
        {
            View v = view.getChildAt( i );

            if ( v instanceof TextView )
            {
                if( WeatherLionApplication.currentTypeface != null )
                {
                    ( (TextView) v ).setTypeface( WeatherLionApplication.currentTypeface );
                }// end of if block

            }// end of if block
            else if ( v instanceof ViewGroup )
            {
                if( v instanceof RelativeLayout )
                {
                    for ( int j = 0; j < ((RelativeLayout) v).getChildCount(); j++ )
                    {
                        View v1 = ((RelativeLayout) v).getChildAt( j );

                        if ( v1 instanceof TextView )
                        {
                            if( WeatherLionApplication.currentTypeface != null )
                            {
                                ( (TextView) v1 ).setTypeface( WeatherLionApplication.currentTypeface );
                            }// end of if block
                        }// end of if block
                    }// end of for loop
                }// end of if block
                else
                {
                    loadCustomFont( (ViewGroup) v );
                }// end of else block
            }// end of else if block
        }// end of for loop
    }// end of method loadCustomFont

    /***
     * Determines whether or not a connectivity check needs to be performed
     *
     * @return	A {@code boolean} value of true/false dependent on the outcome of the test.
     */
    public static boolean timeForConnectivityCheck()
    {
        int interval = Integer.parseInt( WeatherLionApplication.storedPreferences.getInterval() );
        long minutesToGo = millisecondsToMinutes( interval );
        boolean ready = false;

        if( lastUpdated != null && !updateRequired(getAppContext()) )
        {
            Calendar cal = Calendar.getInstance();
            cal.setTime( lastUpdated );
            cal.add( Calendar.MINUTE, (int) minutesToGo );
            Date nextUpdateDue = cal.getTime();

            //milliseconds
            long difference = nextUpdateDue.getTime() - new Date().getTime();

            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            minutesToGo = difference / minutesInMilli;

            ready = minutesToGo <= 1;
        }// end of if block
        else if( updateRequired(getAppContext()) || lastUpdated != null )
        {
            ready = true;
        }// end of else if block

        return ready;
    }// end of method timeForConnectivityCheck
}// end of class UtilityMethod