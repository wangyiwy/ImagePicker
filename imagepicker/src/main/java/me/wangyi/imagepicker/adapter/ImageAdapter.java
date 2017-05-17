package me.wangyi.imagepicker.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import me.wangyi.imagepicker.ImagePicker;
import me.wangyi.imagepicker.R;
import me.wangyi.imagepicker.model.Image;
import me.wangyi.imagepicker.ui.ImagePickerActivity;

/**
 * Created on 2017/5/17.
 *
 * @author WangYi
 * @since 1.0.0
 */

public class ImageAdapter extends BaseAdapter {
    private ArrayList<Image> mImageList;
    private OnImageSelectedListener mListener;
    private ImageSelectCountCallBack mCountCallBack;
    private int mSelectMode;

    public ImageAdapter(ArrayList<Image> imageList, ImageSelectCountCallBack callBack, int selectMode) {
        this.mImageList = imageList;
        this.mCountCallBack = callBack;
        this.mSelectMode = selectMode;
    }

    @Override
    public int getCount() {
        return mImageList == null ? 0 : mImageList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageHolder holder;
        if (convertView == null) {
            holder = new ImageHolder(parent.getContext());
            convertView = holder.itemView;
            convertView.setTag(holder);
        } else {
            holder = (ImageHolder) convertView.getTag();
        }
        Image image = mImageList.get(position);
        ImagePicker.imageLoader.displayImage(holder.ivImage, image);
        holder.itemView.setTag(R.id.holder_tag, position);

        holder.masker.setVisibility(image.isSelected() ? View.VISIBLE : View.GONE);
        holder.cbSelect.setChecked(image.isSelected());
        return convertView;
    }

    private class ImageHolder implements View.OnClickListener {
        View itemView;
        ImageView ivImage;
        CheckBox cbSelect;
        View masker;

        ImageHolder(Context context) {
            itemView = View.inflate(context, R.layout.item_image, null);
            ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
            cbSelect = (CheckBox) itemView.findViewById(R.id.cbSelect);
            masker = itemView.findViewById(R.id.masker);
            itemView.setOnClickListener(this);
            cbSelect.setOnClickListener(this);
            if (mSelectMode == ImagePicker.MODE_MULTI_SELECT) {
                cbSelect.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onClick(View v) {
            int position = (int) itemView.getTag(R.id.holder_tag);
            Image image = mImageList.get(position);

            if (mSelectMode == ImagePicker.MODE_SINGLE_SELECT) {
                if (mListener != null) {
                    mListener.onSelectedUpdate(image, true);
                }
                return;
            }

            int selectedCount = mCountCallBack.getSelectedCount();
            if (selectedCount >= ImagePickerActivity.MaxSelectSize && !image.isSelected()) {
                if (cbSelect.isChecked()) {
                    cbSelect.setChecked(false);
                }
                String tip = v.getContext().getString(R.string.select_limit_tip, ImagePickerActivity.MaxSelectSize);
                Toast.makeText(v.getContext(), tip, Toast.LENGTH_SHORT).show();
                return;
            } else {
                if (v.getId() != R.id.cbSelect) {
                    cbSelect.setChecked(!cbSelect.isChecked());
                }
                masker.setVisibility(cbSelect.isChecked() ? View.VISIBLE : View.GONE);
            }

            image.setSelected(cbSelect.isChecked());

            if (mListener != null) {
                mListener.onSelectedUpdate(image, cbSelect.isChecked());
            }
        }
    }

    public void setOnImageSelectedListener(OnImageSelectedListener listener) {
        this.mListener = listener;
    }

    public interface OnImageSelectedListener {
        void onSelectedUpdate(Image image, boolean isAdd);
    }

    public interface ImageSelectCountCallBack {
        int getSelectedCount();
    }
}
