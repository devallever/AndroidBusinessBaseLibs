package com.allever.video.editor.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.viewpager.widget.ViewPager;

public class GalleryViewPager extends ViewPager {

	private boolean mCanScroll = true;
	
	public GalleryViewPager(Context context) {
		super(context);
	}
	
	public GalleryViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void setCanScroll(boolean canScroll) {
		this.mCanScroll = canScroll;
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent arg0) {
    	try{
    		if (mCanScroll) {
    			return super.onTouchEvent(arg0);
    		} else {
    			return false;
    		}
    	}
    	catch (Exception e) {
			return false;
		}
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
    	try{
    		if (mCanScroll) {
    			return super.onInterceptTouchEvent(arg0);
    		} else {
    			return false;
    		}
    	}
    	catch (Exception e) {
			return false;
		}
    }
}
