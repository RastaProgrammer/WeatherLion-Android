package com.bushbungalo.weatherlion.model;

@SuppressWarnings({ "unused"})
public class LicenceData
{
    public LicenceData()
    {
    }

    private String lib;
    private String owner;
    private String version;
    private String licence;

    public String getLib()
    {
        return lib;
    }

    public void setLib( String lib )
    {
        this.lib = lib;
    }

    public String getOwner()
    {
        if( owner == null )
        {
            return "<html><p>No owner details supplied</p></html>";
        }// end of if block
        else
        {
            return owner;
        }// end of else block
    }

    public void setOwner( String owner )
    {
        this.owner = owner;
    }

    public String getVersion()
    {
        if( version == null )
        {
            return "<html><p>No version number supplied</p></html>";
        }// end of if block
        else
        {
            return version;
        }// end of else block
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getLicence()
    {
        if( licence == null )
        {
            return "<html><p>No licencing details supplied</p></html>";
        }// end of if block
        else
        {
            return licence;
        }// end of else block
    }

    public void setLicence( String licence )
    {
        this.licence = licence;
    }
}// end of class LicenceData
