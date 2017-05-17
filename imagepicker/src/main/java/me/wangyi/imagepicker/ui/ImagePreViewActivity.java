package me.wangyi.imagepicker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import me.wangyi.imagepicker.ImagePicker;
import me.wangyi.imagepicker.R;
import me.wangyi.imagepicker.adapter.PreViewAdapter;
import me.wangyi.imagepicker.model.Image;
import me.wangyi.imagepicker.widget.ImagePager;

/**
 * Created on 2017/5/12.
 *
 * @author WangYi
 * @since 1.0.0
 */

public class ImagePreViewActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {
    RadioButton rbFullImage;
    CheckBox cbSelect;
    RelativeLayout rlBottom;

    ImagePager imagePager;
    PreViewAdapter mAdapter;

    private ArrayList<Image> mImageList;
    private boolean mFullImageEnable = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        mImageList = getIntent().getParcelableArrayListExtra(ImagePicker.EXTRA_IMAGE_LIST);
        if (mImageList == null) {
            finish();
            Log.e(ImagePicker.LOG_TAG, "preview image list is empty!");
            return;
        }

        imagePager = (ImagePager) findViewById(R.id.imagePager);
        rbFullImage = (RadioButton) findViewById(R.id.rbFullImage);
        cbSelect = (CheckBox) findViewById(R.id.cbSelect);
        rlBottom = (RelativeLayout) findViewById(R.id.rlBottom);

        mAdapter = new PreViewAdapter(mImageList);
        imagePager.setAdapter(mAdapter);
        imagePager.addOnPageChangeListener(this);
        rbFullImage.setOnClickListener(this);
        cbSelect.setOnClickListener(this);

        calculateFullSize();
        setToolbarTitle(0);
    }

    private void setToolbarTitle(int currentIndex) {
        ActionBar actionBar = getSupportActionBar();
        //noinspection ConstantConditions
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(currentIndex + 1 + "/" + mImageList.size());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
        } else if (itemId == R.id.menu_done) {
            back(RESULT_OK);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        back(RESULT_CANCELED);
    }

    private void back(int resultCode) {
        ArrayList<Image> selectedImageList = new ArrayList<>();
        for (Image image : mImageList) {
            if (image.isSelected()) {
                selectedImageList.add(image);
            }
        }
        Intent data = new Intent();
        data.putParcelableArrayListExtra(ImagePicker.EXTRA_IMAGE_LIST, selectedImageList);
        data.putExtra(ImagePicker.EXTRA_FULL_IMAGE, rbFullImage.isChecked());
        setResult(resultCode, data);
        finish();
    }

    /**
     * 计算所有选中图片的原图尺寸
     */
    private void calculateFullSize() {
        long size = 0;
        for (Image image : mImageList) {
            if (image.isSelected()) {
                size += image.getSize();
            }
        }
        if (size <= 0) {
            rbFullImage.setText(getString(R.string.full_image_temp));
        } else {
            String strSize = Formatter.formatFileSize(this, size);
            rbFullImage.setText(getString(R.string.full_image, strSize));
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setToolbarTitle(position);
        Image image = mImageList.get(imagePager.getCurrentItem());
        cbSelect.setChecked(image.isSelected());
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.rbFullImage) {
            rbFullImage.setChecked(!mFullImageEnable);
            mFullImageEnable = !mFullImageEnable;
        } else if (id == R.id.cbSelect) {
            Image image = mImageList.get(imagePager.getCurrentItem());
            image.setSelected(cbSelect.isChecked());
            calculateFullSize();
        }
    }
}
