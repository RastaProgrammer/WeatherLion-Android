package com.bushbungalo.weatherlion.utils;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

@SuppressWarnings({"unused"})
public class DividerItemDecoration extends RecyclerView.ItemDecoration
{
    private Drawable mDivider;

    public DividerItemDecoration( Drawable divider )
    {
        mDivider = divider;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent,
                               @NonNull RecyclerView.State state )
    {
        super.getItemOffsets( outRect, view, parent, state );

        if ( parent.getChildAdapterPosition( view ) == 0 )
        {
            return;
        }// end of if block

        outRect.top = mDivider.getIntrinsicHeight();
    }

    @Override
    public void onDraw(@NonNull Canvas canvas, RecyclerView parent,
                       @NonNull RecyclerView.State state )
    {
        int dividerLeft = parent.getPaddingLeft();
        int dividerRight = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();

        for ( int i = 0; i < childCount - 1; i++ )
        {
            View child = parent.getChildAt( i );

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int dividerTop = child.getBottom() + params.bottomMargin;
            int dividerBottom = dividerTop + mDivider.getIntrinsicHeight();

            mDivider.setBounds( dividerLeft, dividerTop, dividerRight, dividerBottom );
            mDivider.draw( canvas );
        }// end of for loop
    }
}// end of class DividerItemDecoration

