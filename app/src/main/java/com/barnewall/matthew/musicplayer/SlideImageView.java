package com.barnewall.matthew.musicplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Matthew on 4/11/2016.
 */
public class SlideImageView extends ImageView {
    private boolean beingTouched;

    public SlideImageView(Context context) {
        super(context);
        beingTouched = false;
    }


    public SlideImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        beingTouched = false;

    }

    public SlideImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        beingTouched = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            beingTouched = true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            beingTouched = false;
        }
        return super.onTouchEvent(event);
    }

    public boolean isBeingTouched(){
        if(beingTouched){
            beingTouched = false;
            return true;
        }
        return false;
    }
}
