package com.bushbungalo.weatherlion.custom;

import android.content.Context;
import android.support.v4.text.HtmlCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bushbungalo.weatherlion.R;
import com.bushbungalo.weatherlion.WeatherLionApplication;
import com.bushbungalo.weatherlion.model.LicenceData;

import java.util.Objects;

public class LicenceGridAdapter extends BaseAdapter
{
    private Context mContext;
    private LicenceData[] mLicenceDataDetails;

    public LicenceGridAdapter( Context c, LicenceData[] licences )
    {
        mContext = c;
        mLicenceDataDetails = licences;
    }// end of two-argument constructor

    @Override
    public int getCount()
    {
       return mLicenceDataDetails.length;
    }

    @Override
    public Object getItem( int position )
    {
        return null;
    }

    @Override
    public long getItemId( int position )
    {
        return 0;
    }

    @Override
    public View getView( int position, View convertView, ViewGroup parent )
    {
        View grid;

        if( convertView == null )
        {
            grid = View.inflate( mContext, R.layout.wl_licence_item, null );
        }// end of if block
        else
        {
            grid = convertView;
        }// end of else block

        LicenceData licenceData = mLicenceDataDetails[ position ];

        TextView libraryName = grid.findViewById( R.id.txvLibName );
        libraryName.setTypeface( WeatherLionApplication.currentTypeface );
        libraryName.setText( licenceData.getLib() );

        TextView libraryOwner = grid.findViewById( R.id.txvLibOwner );
        libraryOwner.setTypeface( WeatherLionApplication.currentTypeface );
        libraryOwner.setText( licenceData.getOwner() );

        TextView libraryVersion = grid.findViewById( R.id.txvLibVersion );
        libraryVersion.setTypeface( WeatherLionApplication.currentTypeface );
        libraryVersion.setText( licenceData.getVersion() );

        TextView libraryLicence = grid.findViewById( R.id.txvLibLicence );
        libraryLicence.setTypeface( WeatherLionApplication.currentTypeface );
        libraryLicence.setText( HtmlCompat.fromHtml(
                Objects.requireNonNull( licenceData.getLicence() ), 0 ) );

        String lastLicenceOwner = mLicenceDataDetails[  mLicenceDataDetails.length - 1 ].getLib();

        if( !libraryName.getText().toString().equals( lastLicenceOwner ) )
        {
            grid.setBackground( mContext.getDrawable( R.drawable.wl_light_bottom_border ) );
        }// end of if block
        else
        {
            // last item must not have a bottom border
            grid.setBackground( null );
        }// end of else block

        return grid;
    }// end of method getView
}// end of class LicenceGridAdapter