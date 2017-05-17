package me.wangyi.imagepicker;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageView;

import me.wangyi.imagepicker.model.Image;
import me.wangyi.imagepicker.ui.ImagePickerActivity;

/**
 * Created on 2017/5/10.
 *
 * @author WangYi
 * @since 1.0.0
 */

public class ImagePicker {
    public static final String LOG_TAG = "ImagePicker";

    public static final String EXTRA_IMAGE_LIST = "extra_image_list";
    public static final String EXTRA_FULL_IMAGE = "extra_full_image";

    public static final String PARAM_SELECT_MODE = "param_select_mode";
    public static final String PARAM_MAX_SELECT_SIZE = "param_max_select_size";
    public static final String PARAM_CROP_ENABLE = "param_crop_enable";
    public static final String PARAM_CROP_OUTPUT_X = "param_crop_output_x";
    public static final String PARAM_CROP_OUTPUT_Y = "param_crop_output_y";

    public static final int MODE_SINGLE_SELECT = 0;
    public static final int MODE_MULTI_SELECT = 1;

    /*parameters and default values */
    private int mSelectMode = MODE_MULTI_SELECT;
    private int mMaxSelectSize = 9;
    private boolean mCropImageEnable = false;
    private int mCropOutputX = 400;
    private int mCropOutputY = 400;
    private int mRequestCode = 999;

    public static ImageLoader imageLoader;

    public ImagePicker mode(int mode) {
        this.mSelectMode = mode;
        return this;
    }

    public ImagePicker selectLimit(int size) {
        this.mMaxSelectSize = size;
        return this;
    }

    public ImagePicker cropEnable(boolean enable) {
        this.mCropImageEnable = enable;
        return this;
    }

    public ImagePicker cropOutputX(int size) {
        this.mCropOutputX = size;
        return this;
    }

    public ImagePicker cropOutputY(int size) {
        this.mCropOutputY = size;
        return this;
    }

    public ImagePicker requestCode(int requestCode) {
        this.mRequestCode = requestCode;
        return this;
    }

    public ImagePicker imageLoader(ImageLoader loader) {
        imageLoader = loader;
        return this;
    }

    public void start(Activity activity) {
        if (imageLoader == null) {
            throw new RuntimeException("please provide your image loader!");
        }

        Intent intent = new Intent(activity, ImagePickerActivity.class);
        intent.putExtra(PARAM_SELECT_MODE, mSelectMode);
        intent.putExtra(PARAM_MAX_SELECT_SIZE, mMaxSelectSize);
        intent.putExtra(PARAM_CROP_ENABLE, mCropImageEnable);
        intent.putExtra(PARAM_CROP_OUTPUT_X, mCropOutputX);
        intent.putExtra(PARAM_CROP_OUTPUT_Y, mCropOutputY);
        activity.startActivityForResult(intent, mRequestCode);
    }

    public interface ImageLoader {
        void displayImage(ImageView imageView, Image image);
    }
}
