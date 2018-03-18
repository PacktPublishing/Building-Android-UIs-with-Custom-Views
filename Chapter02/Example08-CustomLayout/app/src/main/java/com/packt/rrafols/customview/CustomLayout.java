package com.packt.rrafols.customview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class CustomLayout extends ViewGroup {

    public CustomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int left = l + getPaddingLeft();
        int top = t + getPaddingTop();

        // keeps track of maximum row height
        int rowHeight = 0;

        for(int i = 0; i < count; i++) {
            View child = getChildAt(i);

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            // if child fits in this row put it there
            if(left + childWidth < r - getPaddingRight()) {
                child.layout(left, top, left + childWidth, top + childHeight);
                left += childWidth;
            } else {
                // otherwise put it on next row
                left = l + getPaddingLeft();
                top += rowHeight;
                rowHeight = 0;

                child.layout(left, top, left + childWidth, top + childHeight);
                left += childWidth;
            }

            // update maximum row height
            if(childHeight > rowHeight) rowHeight = childHeight;
        }
    }

    private int getMeasure(int spec, int desired) {
        switch(MeasureSpec.getMode(spec)) {
            case MeasureSpec.EXACTLY:
                return MeasureSpec.getSize(spec);

            case MeasureSpec.AT_MOST:
                return Math.min(MeasureSpec.getSize(spec), desired);

            case MeasureSpec.UNSPECIFIED:
            default:
                return desired;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if(getWidth() == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        int count = getChildCount();

        int rowHeight = 0;
        int maxWidth = 0;
        int maxHeight = 0;
        int left = 0;
        int top = 0;

        for(int i = 0; i < count; i++) {
            View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            // if child fits in this row put it there
            if(left + childWidth < getWidth()) {
                left += childWidth;
            } else {
                // otherwise put it on next row
                if(left > maxWidth) maxWidth = left;
                left = 0;
                top += rowHeight;
                rowHeight = 0;
            }

            // update maximum row height
            if(childHeight > rowHeight) rowHeight = childHeight;
        }

        if(left > maxWidth) maxWidth = left;
        maxHeight = top + rowHeight;

        setMeasuredDimension(getMeasure(widthMeasureSpec, maxWidth), getMeasure(heightMeasureSpec, maxHeight));
    }
}
