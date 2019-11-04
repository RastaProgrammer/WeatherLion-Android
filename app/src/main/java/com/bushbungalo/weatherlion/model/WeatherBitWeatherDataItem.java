package com.bushbungalo.weatherlion.model;

import com.google.gson.Gson;

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
 * 		<li>01/22/19</li> 
 * 			<ol>
 * 				<li>Renamed class {@code ForecastData} to {@code FiveDayForecastData}</li>
 * 				<li>Added class {@code SixteenDayForecastData}</li>
 * 			</ol>
 * </ul>
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class WeatherBitWeatherDataItem
{
//	private static WeatherBitWeatherDataItem.FiveDayForecastData wbFxWeatherData; // Deprecated
	static WeatherBitWeatherDataItem.SixteenDayForecastData wbFxWeatherData;
	static WeatherBitWeatherDataItem.FortyEightHourForecastData wbHxWeatherData;
	static WeatherBitWeatherDataItem.WeatherData wbWxWeatherData;

    private WeatherData wx;
    private FiveDayForecastData fx;
	private FortyEightHourForecastData hx;
    
    public WeatherBitWeatherDataItem()
    {
    }
    
    public WeatherData getWx()
    {
		return wx;
	}

	public void setWx(WeatherData wx)
	{
		this.wx = wx;
	}

	public FiveDayForecastData getFx() 
	{
		return fx;
	}

	public void setFx(FiveDayForecastData fx) 
	{
		this.fx = fx;
	}

	public FortyEightHourForecastData getHx()
	{
		return hx;
	}

	public void setHx( FortyEightHourForecastData hx )
	{
		this.hx = hx;
	}

    public class WeatherData
    {
        private List<Data> data;
        private int count;

        public List<Data> getData()
        {
			return data;
		}

		public void setData(List<Data> data)
		{
			this.data = data;
		}

		public int getCount() 
		{
			return count;
		}

		public void setCount(int count)
		{
			this.count = count;
		}

		public class Data
        {
            private String wind_cdir;
            private int rh;
            private String pod;
            private String lon;
            private double pres;
            private String timezone;
            private String ob_time;
            private String country_code;
            private int clouds;
            private double vis;
            private double wind_spd;
            private String wind_cdir_full;
            private double appTemp;
            private String state_code;
            private int ts;
            private double h_angle;
            private double dewpt;
            private Weather weather;
            private double uv;
            private String station;
            private int windDir;
            private int elevAngle;
            private String datetime;
            private String precip;
            private double dhi;
            private String cityName;
            private String sunrise;
            private String sunset;
            private double temp;
            private String lat;
            private double slp;
            
			public String getWind_cdir() 
			{
				return wind_cdir;
			}
			
			public void setWindCDir(String wind_cdir)
			{
				this.wind_cdir = wind_cdir;
			}
			
			public int getRh() 
			{
				return rh;
			}
			
			public void setRh(int rh) 
			{
				this.rh = rh;
			}
			
			public String getPod()
			{
				return pod;
			}
			
			public void setPod(String pod)
			{
				this.pod = pod;
			}
			
			public String getLon()
			{
				return lon;
			}
			
			public void setLon(String lon) 
			{
				this.lon = lon;
			}
			
			public double getPres()
			{
				return pres;
			}
			
			public void setPres(double pres)
			{
				this.pres = pres;
			}
			
			public String getTimezone() 
			{
				return timezone;
			}
			
			public void setTimezone(String timezone)
			{
				this.timezone = timezone;
			}
			
			public String getOb_time()
			{
				return ob_time;
			}
			
			public void setObTime(String ob_time)
			{
				this.ob_time = ob_time;
			}
			
			public String getCountrCode()
			{
				return country_code;
			}
			
			public void setCountryCode(String country_code)
			{
				this.country_code = country_code;
			}
			
			public int getClouds()
			{
				return clouds;
			}
			
			public void setClouds(int clouds) 
			{
				this.clouds = clouds;
			}
			
			public double getVis()
			{
				return vis;
			}
			
			public void setVis(double vis)
			{
				this.vis = vis;
			}
			
			public double getWindSpeed()
			{
				return wind_spd;
			}
			
			public void setWindSpeed(double wind_spd)
			{
				this.wind_spd = wind_spd;
			}
			
			public String geWindCdirFull()
			{
				return wind_cdir_full;
			}
			
			public void setWindCdirFull(String wind_cdir_full)
			{
				this.wind_cdir_full = wind_cdir_full;
			}
			
			public double getAppTemp()
			{
				return appTemp;
			}
			
			public void setAppTemp(double app_temp)
			{
				this.appTemp = app_temp;
			}
			
			public String getStateCode()
			{
				return state_code;
			}
			
			public void setStateCode(String state_code)
			{
				this.state_code = state_code;
			}
			
			public int getTs()
			{
				return ts;
			}
			
			public void setTs(int ts) 
			{
				this.ts = ts;
			}
			
			public double getHAngle()
			{
				return h_angle;
			}
			
			public void setHAngle(double h_angle)
			{
				this.h_angle = h_angle;
			}
			
			public double getDewpt()
			{
				return dewpt;
			}
			
			public void setDewpt(double dewpt)
			{
				this.dewpt = dewpt;
			}
			
			public Weather getWeather()
			{
				return weather;
			}
			
			public void setWeather(Weather weather) 
			{
				this.weather = weather;
			}
			
			public double getUv() 
			{
				return uv;
			}
			
			public void setUv(double uv)
			{
				this.uv = uv;
			}
			
			public String getStation()
			{
				return station;
			}
			
			public void setStation(String station)
			{
				this.station = station;
			}
			
			public int getWindDir()
			{
				return windDir;
			}
			
			public void setWindDir(int windDir)
			{
				this.windDir = windDir;
			}
			
			public int getElevAngle()
			{
				return elevAngle;
			}
			
			public void setElevAngle(int elevAngle) 
			{
				this.elevAngle = elevAngle;
			}
			
			public String getDatetime() 
			{
				return datetime;
			}
			
			public void setDatetime(String datetime)
			{
				this.datetime = datetime;
			}
			
			public String getPrecip()
			{
				return precip;
			}
			
			public void setPrecip(String precip)
			{
				this.precip = precip;
			}
			
			public double getDhi()
			{
				return dhi;
			}
			
			public void setDhi(double dhi)
			{
				this.dhi = dhi;
			}
			
			public String getCityName() 
			{
				return cityName;
			}
			
			public void setCityName(String cityName)
			{
				this.cityName = cityName;
			}
			
			public String getSunrise()
			{
				return sunrise;
			}
			public void setSunrise(String sunrise) {
				this.sunrise = sunrise;
			}
			
			public String getSunset()
			{
				return sunset;
			}
			
			public void setSunset(String sunset)
			{
				this.sunset = sunset;
			}
			
			public double getTemp() 
			{
				return temp;
			}
			
			public void setTemp(double temp) 
			{
				this.temp = temp;
			}
			
			public String getLat()
			{
				return lat;
			}
			
			public void setLat(String lat)
			{
				this.lat = lat;
			}
			
			public double getSlp()
			{
				return slp;
			}
			
			public void setSlp(double slp)
			{
				this.slp = slp;
			}

        }// end of class Data

        public class Weather
        {
            private String icon;
            private String code;
            private String description;
            
			public String getIcon()
			{
				return icon;
			}
			
			public void setIcon(String icon)
			{
				this.icon = icon;
			}
			
			public String getCode()
			{
				return code;
			}
			
			public void setCode(String code)
			{
				this.code = code;
			}
			
			public String getDescription()
			{
				return description;
			}
			
			public void setDescription(String description) 
			{
				this.description = description;
			}

        }// end of class Weather
    }// end of class WeatherData

	public class FortyEightHourForecastData
	{
		private List<Data> data;

		public List<Data> getData()
		{
			return data;
		}

		public void setData(List<Data> data)
		{
			this.data = data;
		}

		public class Data
		{
			private String wind_cdir;
			private double rh;
			private String pod;
			private String timestamp_utc;
			private double pres;
			private double solar_rad;
			private double ozone;
			private Weather weather;
			private double wind_gust_spd;
			private String timestamp_local;
			private int snow_depth;
			private int clouds;
			private int ts;
			private double wind_spd;
			private int pop;
			private String wind_cdir_full;
			private double slp;
			private double dni;
			private double dewpt;
			private int snow;
			private double uv;
			private int wind_dir;
			private int clouds_hi;
			private double precip;
			private double vis;
			private double dhi;
			private double app_temp;
			private String datetime;
			private double temp;
			private double ghi;
			private int clouds_mid;
			private int clouds_low;

			public String getWindCDir()
			{
				return wind_cdir;
			}

			public void setWindCDir( String wind_cdir )
			{
				this.wind_cdir = wind_cdir;
			}

			public double getRh()
			{
				return rh;
			}

			public void setRh( double rh )
			{
				this.rh = rh;
			}

			public String getPod()
			{
				return pod;
			}

			public void setPod( String pod )
			{
				this.pod = pod;
			}

			public String getTimestampUtc()
			{
				return timestamp_utc;
			}

			public void setTimestampUtc( String timestamp_utc )
			{
				this.timestamp_utc = timestamp_utc;
			}

			public double getPres()
			{
				return pres;
			}

			public void setPres( double pres )
			{
				this.pres = pres;
			}

			public double getSolarRad()
			{
				return solar_rad;
			}

			public void setSolar_rad( double solar_rad )
			{
				this.solar_rad = solar_rad;
			}

			public double getOzone()
			{
				return ozone;
			}

			public void setOzone( double ozone )
			{
				this.ozone = ozone;
			}

			public Weather getWeather()
			{
				return weather;
			}

			public void setWeather( Weather weather )
			{
				this.weather = weather;
			}

			public double getWindGustSpd()
			{
				return wind_gust_spd;
			}

			public void setWindGustSpd( double wind_gust_spd )
			{
				this.wind_gust_spd = wind_gust_spd;
			}

			public String getTimestampLocal()
			{
				return timestamp_local;
			}

			public void setTimestampLocal( String timestamp_local )
			{
				this.timestamp_local = timestamp_local;
			}

			public int getSnowDepth()
			{
				return snow_depth;
			}

			public void setSnowDepth( int snow_depth )
			{
				this.snow_depth = snow_depth;
			}

			public int getClouds()
			{
				return clouds;
			}

			public void setClouds( int clouds )
			{
				this.clouds = clouds;
			}

			public int getTs()
			{
				return ts;
			}

			public void setTs( int ts )
			{
				this.ts = ts;
			}

			public double getWindSpd()
			{
				return wind_spd;
			}

			public void setWindSpd( double wind_spd )
			{
				this.wind_spd = wind_spd;
			}

			public int getPop()
			{
				return pop;
			}

			public void setPop( int pop )
			{
				this.pop = pop;
			}

			public String getWindCDirFull()
			{
				return wind_cdir_full;
			}

			public void setWindCDirFull( String wind_cdir_full )
			{
				this.wind_cdir_full = wind_cdir_full;
			}

			public double getSlp()
			{
				return slp;
			}

			public void setSlp( double slp )
			{
				this.slp = slp;
			}

			public double getDni()
			{
				return dni;
			}

			public void setDni( double dni )
			{
				this.dni = dni;
			}

			public double getDewpt()
			{
				return dewpt;
			}

			public void setDewpt( double dewpt )
			{
				this.dewpt = dewpt;
			}

			public int getSnow()
			{
				return snow;
			}

			public void setSnow( int snow )
			{
				this.snow = snow;
			}

			public double getUv()
			{
				return uv;
			}

			public void setUv( double uv )
			{
				this.uv = uv;
			}

			public int getWindDir()
			{
				return wind_dir;
			}

			public void setWindDir( int wind_dir )
			{
				this.wind_dir = wind_dir;
			}

			public int getCloudsHi() {
				return clouds_hi;
			}

			public void setCloudsHi( int clouds_hi )
			{
				this.clouds_hi = clouds_hi;
			}

			public double getPrecip()
			{
				return precip;
			}

			public void setPrecip( double precip )
			{
				this.precip = precip;
			}

			public double getVis()
			{
				return vis;
			}

			public void setVis( double vis )
			{
				this.vis = vis;
			}

			public double getDhi()
			{
				return dhi;
			}

			public void setDhi( double dhi )
			{
				this.dhi = dhi;
			}

			public double getAppTemp()
			{
				return app_temp;
			}

			public void setAppTemp( double app_temp )
			{
				this.app_temp = app_temp;
			}

			public String getDatetime()
			{
				return datetime;
			}

			public void setDatetime( String datetime )
			{
				this.datetime = datetime;
			}

			public double getTemp()
			{
				return temp;
			}

			public void setTemp( double temp )
			{
				this.temp = temp;
			}

			public double getGhi()
			{
				return ghi;
			}

			public void setGhi( double ghi )
			{
				this.ghi = ghi;
			}

			public int getCloudsMid()
			{
				return clouds_mid;
			}

			public void setCloudsMid( int clouds_mid )
			{
				this.clouds_mid = clouds_mid;
			}

			public int getCloudsLow()
			{
				return clouds_low;
			}

			public void setCloudsLow( int clouds_low )
			{
				this.clouds_low = clouds_low;
			}

			public class Weather
			{
				private String icon;
				private String code;
				private String description;

				public String getIcon()
				{
					return icon;
				}
				public void setIcon(String icon)
				{
					this.icon = icon;
				}

				public String getCode()
				{
					return code;
				}
				public void setCode(String code)
				{
					this.code = code;
				}

				public String getDescription()
				{
					return description;
				}

				public void setDescription(String description)
				{
					this.description = description;
				}

			}// end of class Weather

		}// end of class Data
	}// end of inner class FortyEightHourForecastData

    public class FiveDayForecastData
    {
        private List<Data> data;
        private String city_name;
        private String lon;
        private String timezone;
        private String lat;
        private String country_code;
        private String state_code;

        public List<Data> getData() 
        {
			return data;
		}

		public void setData(List<Data> data)
		{
			this.data = data;
		}

		public String getCityName() 
		{
			return city_name;
		}

		public void setCityName(String city_name)
		{
			this.city_name = city_name;
		}

		public String getLon() 
		{
			return lon;
		}

		public void setLon(String lon)
		{
			this.lon = lon;
		}

		public String getTimezone()
		{
			return timezone;
		}

		public void setTimezone(String timezone) 
		{
			this.timezone = timezone;
		}

		public String getLat()
		{
			return lat;
		}

		public void setLat(String lat) 
		{
			this.lat = lat;
		}

		public String getCountry_code()
		{
			return country_code;
		}

		public void setCountryCode(String country_code) 
		{
			this.country_code = country_code;
		}

		public String getStateCode() 
		{
			return state_code;
		}

		public void setStateCode(String state_code) 
		{
			this.state_code = state_code;
		}

		public class Data
        {
            private String wind_cdir;
            private double rh;
            private double wind_spd;
            private int pop;
            private String wind_cdir_full;
            private double app_temp;
            private int snow6h;
            private String pod;
            private double dewpt;
            private int snow;
            private double uv;
            private int ts;
            private int wind_dir;
            private Weather weather;
            private int snow_depth;
            private double dhi;
            private String precip6h;
            private String precip;
            private double pres;
            private String datetime;
            private double temp;
            private double slp;
            private int clouds;
            private double vis;

            public String getWind_cdir()
            {
				return wind_cdir;
			}

			public void setWindCDir(String wind_cdir) 
			{
				this.wind_cdir = wind_cdir;
			}

			public double getRh() 
			{
				return rh;
			}

			public void setRh(double rh) 
			{
				this.rh = rh;
			}

			public double getWindSpeed() 
			{
				return wind_spd;
			}

			public void setWindSpeed(double wind_spd) 
			{
				this.wind_spd = wind_spd;
			}

			public int getPop() 
			{
				return pop;
			}

			public void setPop(int pop) 
			{
				this.pop = pop;
			}

			public String getWindCDirFull()
			{
				return wind_cdir_full;
			}

			public void setWindCDirFull(String wind_cdir_full) 
			{
				this.wind_cdir_full = wind_cdir_full;
			}

			public double getAppTemp()
			{
				return app_temp;
			}

			public void setAppTemp(double app_temp)
			{
				this.app_temp = app_temp;
			}

			public int getSnow6h() 
			{
				return snow6h;
			}

			public void setSnow6h(int snow6h) 
			{
				this.snow6h = snow6h;
			}

			public String getPod()
			{
				return pod;
			}

			public void setPod(String pod)
			{
				this.pod = pod;
			}

			public double getDewpt() 
			{
				return dewpt;
			}

			public void setDewpt(double dewpt)
			{
				this.dewpt = dewpt;
			}

			public int getSnow() 
			{
				return snow;
			}

			public void setSnow(int snow)
			{
				this.snow = snow;
			}

			public double getUv()
			{
				return uv;
			}

			public void setUv(double uv)
			{
				this.uv = uv;
			}

			public int getTs() 
			{
				return ts;
			}

			public void setTs(int ts)
			{
				this.ts = ts;
			}

			public int getWindDir() 
			{
				return wind_dir;
			}

			public void setWindDir(int wind_dir)
			{
				this.wind_dir = wind_dir;
			}

			public Weather getWeather() 
			{
				return weather;
			}

			public void setWeather(Weather weather)
			{
				this.weather = weather;
			}

			public int getSnowDepth()
			{
				return snow_depth;
			}

			public void setSnowDepth(int snow_depth) 
			{
				this.snow_depth = snow_depth;
			}

			public double getDhi() 
			{
				return dhi;
			}

			public void setDhi(double dhi)
			{
				this.dhi = dhi;
			}

			public String getPrecip6h()
			{
				return precip6h;
			}

			public void setPrecip6h(String precip6h)
			{
				this.precip6h = precip6h;
			}

			public String getPrecip() 
			{
				return precip;
			}

			public void setPrecip(String precip)
			{
				this.precip = precip;
			}

			public double getPres() 
			{
				return pres;
			}

			public void setPres(double pres) 
			{
				this.pres = pres;
			}

			public String getDatetime()
			{
				return datetime;
			}

			public void setDatetime(String datetime) 
			{
				this.datetime = datetime;
			}

			public double getTemp() 
			{
				return temp;
			}

			public void setTemp(double temp) 
			{
				this.temp = temp;
			}

			public double getSlp() 
			{
				return slp;
			}

			public void setSlp(double slp)
			{
				this.slp = slp;
			}

			public int getClouds()
			{
				return clouds;
			}

			public void setClouds(int clouds)
			{
				this.clouds = clouds;
			}

			public double getVis() 
			{
				return vis;
			}

			public void setVis(double vis)
			{
				this.vis = vis;
			}

			public class Weather
            {
                private String icon;
                private String code;
                private String description;
                
				public String getIcon()
				{
					return icon;
				}
				public void setIcon(String icon) 
				{
					this.icon = icon;
				}
				
				public String getCode() 
				{
					return code;
				}
				public void setCode(String code)
				{
					this.code = code;
				}
				
				public String getDescription()
				{
					return description;
				}
				
				public void setDescription(String description)
				{
					this.description = description;
				}
                
            }// end of class Weather
            
        }// end of class Data
    }// end of inner class FiveDayForecastData
    
    public class SixteenDayForecastData
    {
    	private List<Data> data;
        private String city_name;
        private String lon;
        private String timezone;
        private String lat;
        private String country_code;
        private String state_code;

        public List<Data> getData()
        {
			return data;
		}

		public void setData( List<Data> data )
		{
			this.data = data;
		}

		public String getCityName() 
		{
			return city_name;
		}

		public void setCityName( String city_name ) 
		{
			this.city_name = city_name;
		}

		public String getLon()
		{
			return lon;
		}

		public void setLon( String lon )
		{
			this.lon = lon;
		}

		public String getTimezone() 
		{
			return timezone;
		}

		public void setTimezone( String timezone ) 
		{
			this.timezone = timezone;
		}

		public String getLat() 
		{
			return lat;
		}

		public void setLat( String lat )
		{
			this.lat = lat;
		}

		public String getCountryCode()
		{
			return country_code;
		}

		public void setCountryCode( String country_code )
		{
			this.country_code = country_code;
		}

		public String getStateCode() 
		{
			return state_code;
		}

		public void setStateCode( String state_code )
		{
			this.state_code = state_code;
		}
		
		public class Data
        {
			private int moonrise_ts;
	        private String wind_cdir;
	        private int rh;
	        private double pres;
	        private int sunset_ts;
	        private double ozone;
	        private double moon_phase;
	        private double wind_gust_spd;
	        private int snow_depth;
	        private int clouds;
	        private int ts;
	        private int sunrise_ts;
	        private double app_min_temp;
	        private double wind_spd;
	        private int pop;
	        private String wind_cdir_full;
	        private double slp;
	        private double app_max_temp;
	        private double vis;
	        private double dewpt;
	        private int snow;
	        private double uv;
	        private String valid_date;
	        private int wind_dir;
	        private double max_dhi;
	        private int clouds_hi;
	        private double precip;
	        private Weather weather;
	        private double max_temp;
	        private int moonset_ts;
	        private String  datetime;
	        private double temp;
	        private double min_temp;
	        private int clouds_mid;
	        private int clouds_low;        
	       

			public int getMoonriseTs()
			{
				return moonrise_ts;
			}

			public void setMoonriseTs( int moonrise_ts ) 
			{
				this.moonrise_ts = moonrise_ts;
			}

			public String getWindCdir()
			{
				return wind_cdir;
			}

			public void setWindCdir( String wind_cdir ) 
			{
				this.wind_cdir = wind_cdir;
			}

			public int getRh() 
			{
				return rh;
			}

			public void setRh( int rh )
			{
				this.rh = rh;
			}

			public double getPres() 
			{
				return pres;
			}

			public void setPres( double pres ) 
			{
				this.pres = pres;
			}

			public int getSunsetTs() 
			{
				return sunset_ts;
			}

			public void setSunsetTs( int sunset_ts )
			{
				this.sunset_ts = sunset_ts;
			}

			public double getOzone() 
			{
				return ozone;
			}

			public void setOzone( double ozone ) 
			{
				this.ozone = ozone;
			}

			public double getMoonPhase() 
			{
				return moon_phase;
			}

			public void setMoonPhase( double moon_phase )
			{
				this.moon_phase = moon_phase;
			}

			public double getWindGustSpd()
			{
				return wind_gust_spd;
			}

			public void setWindGustSpd( double wind_gust_spd ) 
			{
				this.wind_gust_spd = wind_gust_spd;
			}

			public int getSnowDepth()
			{
				return snow_depth;
			}

			public void setSnowDepth( int snow_depth ) 
			{
				this.snow_depth = snow_depth;
			}

			public int getClouds()
			{
				return clouds;
			}

			public void setClouds( int clouds )
			{
				this.clouds = clouds;
			}

			public int getTs()
			{
				return ts;
			}

			public void setTs( int ts )
			{
				this.ts = ts;
			}

			public int getSunriseTs() 
			{
				return sunrise_ts;
			}

			public void setSunriseTs( int sunrise_ts )
			{
				this.sunrise_ts = sunrise_ts;
			}

			public double getAppMinTemp() 
			{
				return app_min_temp;
			}

			public void setAppMinTemp( double app_min_temp )
			{
				this.app_min_temp = app_min_temp;
			}

			public double getWindSpd()
			{
				return wind_spd;
			}

			public void setWindSpd( double wind_spd )
			{
				this.wind_spd = wind_spd;
			}

			public int getPop()
			{
				return pop;
			}

			public void setPop( int pop ) 
			{
				this.pop = pop;
			}

			public String getWindCdirFull()
			{
				return wind_cdir_full;
			}

			public void setWindCdirFull( String wind_cdir_full )
			{
				this.wind_cdir_full = wind_cdir_full;
			}

			public double getSlp() 
			{
				return slp;
			}

			public void setSlp( double slp ) 
			{
				this.slp = slp;
			}

			public double getAppMaxTemp() 
			{
				return app_max_temp;
			}

			public void setAppMaxTemp( double app_max_temp )
			{
				this.app_max_temp = app_max_temp;
			}

			public double getVis()
			{
				return vis;
			}

			public void setVis( double vis )
			{
				this.vis = vis;
			}

			public double getDewpt()
			{
				return dewpt;
			}

			public void setDewpt( double dewpt )
			{
				this.dewpt = dewpt;
			}

			public int getSnow()
			{
				return snow;
			}

			public void setSnow( int snow ) 
			{
				this.snow = snow;
			}

			public double getUv() 
			{
				return uv;
			}

			public void setUv( double uv ) 
			{
				this.uv = uv;
			}

			public String getValidDate()
			{
				return valid_date;
			}

			public void setValidDate( String valid_date ) 
			{
				this.valid_date = valid_date;
			}

			public int getWindDir()
			{
				return wind_dir;
			}

			public void setWindDir( int wind_dir ) 
			{
				this.wind_dir = wind_dir;
			}

			public double getMaxDhi()
			{
				return max_dhi;
			}

			public void setMaxDhi( double max_dhi )
			{
				this.max_dhi = max_dhi;
			}

			public int getCloudsHi()
			{
				return clouds_hi;
			}

			public void setCloudsHi( int clouds_hi ) 
			{
				this.clouds_hi = clouds_hi;
			}

			public double getPrecip()
			{
				return precip;
			}

			public void setPrecip( double precip ) 
			{
				this.precip = precip;
			}

			public double getMaxTemp() 
			{
				return max_temp;
			}

			public void setMaxTemp( double max_temp )
			{
				this.max_temp = max_temp;
			}

			public int getMoonsetTs()
			{
				return moonset_ts;
			}

			public void setMoonsetTs( int moonset_ts )
			{
				this.moonset_ts = moonset_ts;
			}

			public String getDatetime()
			{
				return datetime;
			}

			public void setDatetime( String datetime )
			{
				this.datetime = datetime;
			}

			public double getTemp() 
			{
				return temp;
			}

			public void setTemp( double temp ) 
			{
				this.temp = temp;
			}

			public double getMinTemp()
			{
				return min_temp;
			}

			public void setMinTemp( double min_temp )
			{
				this.min_temp = min_temp;
			}

			public int getCloudsMid() 
			{
				return clouds_mid;
			}

			public void setCloudsMid( int clouds_mid )
			{
				this.clouds_mid = clouds_mid;
			}

			public int getCloudsLow()
			{
				return clouds_low;
			}

			public void setCloudsLow( int clouds_low )
			{
				this.clouds_low = clouds_low;
			}
			
			public Weather getWeather()
			{
				return weather;
			}

			public void setWeather( Weather weather ) 
			{
				this.weather = weather;
			}

			public class Weather
            {
                private String icon;
                private String code;
                private String description;
                
				public String getIcon()
				{
					return icon;
				}
				public void setIcon( String icon ) 
				{
					this.icon = icon;
				}
				
				public String getCode() 
				{
					return code;
				}
				public void setCode( String code )
				{
					this.code = code;
				}
				
				public String getDescription()
				{
					return description;
				}
				
				public void setDescription( String description )
				{
					this.description = description;
				}
                
            }// end of class Weather
            
        }// end of class Data        
        
    }// end of inner class SixteenDayForecastData

    private static boolean deserializeWeatherBitWxJSON( String strJSON )
    {
    	Gson gson = new Gson();
        wbWxWeatherData = gson.fromJson( strJSON, WeatherBitWeatherDataItem.WeatherData.class );

		return wbWxWeatherData != null;
    }// end of method deserializeWeatherBitWxJSON

	private static boolean deserializeWeatherBitHxJSON( String strJSON )
	{
		Gson gson = new Gson();
		wbHxWeatherData = gson.fromJson( strJSON, WeatherBitWeatherDataItem.FortyEightHourForecastData.class );

		return wbHxWeatherData != null;
	}// end of method deserializeWeatherBitHxJSON

    private static boolean deserializeWeatherBitFxJSON( String strJSON )
    {
    	Gson gson = new Gson();
        wbFxWeatherData = gson.fromJson( strJSON, WeatherBitWeatherDataItem.SixteenDayForecastData.class );

		return wbFxWeatherData != null;
    }// end of method deserializeWeatherBitFxJSON
}// end of class WeatherBitWeatherDataItem
