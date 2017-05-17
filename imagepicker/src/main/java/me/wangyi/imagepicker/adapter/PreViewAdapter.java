package me.wangyi.imagepicker.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import me.wangyi.imagepicker.ImagePicker;
import me.wangyi.imagepicker.R;
import me.wangyi.imagepicker.model.Image;
import uk.co.senab.photoview.PhotoView;

/**
 * Created on 2017/5/12.
 *
 * @author WangYi
 * @since 1.0.0
 */

public class PreViewAdapter extends PagerAdapter {
    private ArrayList<Image> mImageList;

    public PreViewAdapter(ArrayList<Image> imageList) {
        this.mImageList = imageList;
    }

    @Override
    public int getCount() {
        return mImageList == null ? 0 : mImageList.size();
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        PhotoView photoView = (PhotoView) LayoutInflater.from(container.getContext())
                .inflate(R.layout.item_preview, container, false);
        Image image = mImageList.get(position);
        ImagePicker.imageLoader.displayImage(photoView, image);
        container.addView(photoView);
        return photoView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }
}
