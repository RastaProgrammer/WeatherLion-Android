package com.bushbungalo.weatherlion.utils;

public class LionBounceInterpolator implements android.view.animation.Interpolator
{
    private double mAmplitude;
    private double mFrequency;

    LionBounceInterpolator( double amplitude, double frequency )
    {
        mAmplitude = amplitude;
        mFrequency = frequency;
    }// end of two-argument constructor

    public float getInterpolation( float time )
    {
        return (float) ( -1 * Math.pow(Math.E, -time/ mAmplitude ) *
                Math.cos( mFrequency * time ) + 1 );
    }// end of method getInterpolation
}
