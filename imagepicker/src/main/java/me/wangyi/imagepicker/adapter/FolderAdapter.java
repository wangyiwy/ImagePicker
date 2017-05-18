package me.wangyi.imagepicker.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;

import me.wangyi.imagepicker.ImagePicker;
import me.wangyi.imagepicker.R;
import me.wangyi.imagepicker.model.Folder;
import me.wangyi.imagepicker.model.Image;

/**
 * Created on 2017/5/9.
 *
 * @author WangYi
 * @since 1.0.0
 */

public class FolderAdapter extends BaseAdapter {
    private List<Folder> mFolderList;
    private int mSelectedIndex = 0;

    public FolderAdapter(List<Folder> folderList) {
        this.mFolderList = folderList;
    }

    @Override
    public int getCount() {
        return mFolderList == null ? 0 : mFolderList.size();
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
        FolderHolder holder;
        if (convertView == null) {
            holder = new FolderHolder(parent.getContext());
            convertView = holder.itemView;
            convertView.setTag(holder);
        } else {
            holder = (FolderHolder) convertView.getTag();
        }

        holder.itemView.setTag(R.id.holder_tag, position);
        holder.rbSelect.setChecked(mSelectedIndex == position);

        Folder folder = mFolderList.get(position);
        holder.tvFolderName.setText(folder.getName());
        holder.tvFolderPath.setText(folder.getPath());
        holder.tvFolderSize.setText(String.valueOf(folder.getSize()));
        Image cover = folder.getCover();
        if (cover != null && ImagePicker.imageLoader != null) {
            ImagePicker.imageLoader.displayImage(holder.ivCover, cover);
        }

        return convertView;
    }

    private class FolderHolder implements View.OnClickListener {
        View itemView;
        ImageView ivCover;
        TextView tvFolderName, tvFolderPath, tvFolderSize;
        RadioButton rbSelect;

        FolderHolder(Context context) {
            itemView = View.inflate(context, R.layout.item_folder, null);
            ivCover = (ImageView) itemView.findViewById(R.id.ivCover);
            tvFolderName = (TextView) itemView.findViewById(R.id.tvFolderName);
            tvFolderPath = (TextView) itemView.findViewById(R.id.tvFolderPath);
            tvFolderSize = (TextView) itemView.findViewById(R.id.tvFolderSize);
            rbSelect = (RadioButton) itemView.findViewById(R.id.rbSelect);

            itemView.setOnClickListener(this);
            rbSelect.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = (int) itemView.getTag(R.id.holder_tag);
            mSelectedIndex = position;
            notifyDataSetChanged();
            if (mListener != null) {
                mListener.onFolderSelected(mFolderList.get(position));
            }
        }
    }

    private OnFolderSelectedListener mListener;

    public void setOnFolderSelectedListener(OnFolderSelectedListener listener) {
        this.mListener = listener;
    }

    public interface OnFolderSelectedListener {
        void onFolderSelected(Folder folder);
    }
}
