package me.wangyi.imagepicker.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created on 2017/5/12.
 *
 * @author WangYi
 * @since 1.0.0
 */

public class ImagePager extends ViewPager {
    public ImagePager(Context context) {
        super(context);
    }

    public ImagePager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        try {
            return super.onInterceptTouchEvent(ev);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }
}