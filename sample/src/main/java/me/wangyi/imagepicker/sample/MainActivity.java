package me.wangyi.imagepicker.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.wangyi.imagepicker.sample.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import me.wangyi.imagepicker.ImagePicker;
import me.wangyi.imagepicker.model.Image;

public class MainActivity extends AppCompatActivity {
    RadioButton rbSingleMode, rbMultiMode;
    EditText etSelectLimit, etOutputW, etOutputH;
    Switch swCropEnable;
    RecyclerView rbResult;

    TextView tvOutputSize;
    LinearLayout llOutputSize;

    private static final int REQUEST_PICK = 233;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etSelectLimit = (EditText) findViewById(R.id.etSelectLimit);
        etOutputW = (EditText) findViewById(R.id.etOutputW);
        etOutputH = (EditText) findViewById(R.id.etOutputH);
        swCropEnable = (Switch) findViewById(R.id.swCropEnable);
        rbSingleMode = (RadioButton) findViewById(R.id.rbSingleMode);
        rbMultiMode = (RadioButton) findViewById(R.id.rbMultiMode);
        rbSingleMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rbMultiMode.setChecked(!isChecked);
            }
        });
        rbMultiMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                rbSingleMode.setChecked(!isChecked);
            }
        });
        rbResult = (RecyclerView) findViewById(R.id.rbResult);
        rbResult.setLayoutManager(new GridLayoutManager(this, 3));
        tvOutputSize = (TextView) findViewById(R.id.tvOutputSize);
        llOutputSize = (LinearLayout) findViewById(R.id.llOutputSize);
        swCropEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                tvOutputSize.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                llOutputSize.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
    }

    public void start(View view) {
        if (rbSingleMode.isChecked()) {
            int cropOutputW = Integer.parseInt(etOutputW.getText().toString());
            int cropOutputH = Integer.parseInt(etOutputH.getText().toString());
            boolean cropEnable = swCropEnable.isChecked();
            new ImagePicker()
                    .mode(ImagePicker.MODE_SINGLE_SELECT)
                    .cropEnable(cropEnable)
                    .cropOutputX(cropOutputW)
                    .cropOutputY(cropOutputH)
                    .imageLoader(new MyImageLoader())
                    .requestCode(REQUEST_PICK)
                    .start(this);
        } else {
            int selectLimit = Integer.parseInt(etSelectLimit.getText().toString());
            new ImagePicker()
                    .mode(ImagePicker.MODE_MULTI_SELECT)
                    .imageLoader(new MyImageLoader())
                    .selectLimit(selectLimit)
                    .requestCode(REQUEST_PICK)
                    .start(this);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK) {
                ArrayList<Image> imageList = data.getParcelableArrayListExtra(ImagePicker.EXTRA_IMAGE_LIST);
                boolean fullImage = data.getBooleanExtra(ImagePicker.EXTRA_FULL_IMAGE, false);
                ResultAdapter adapter = new ResultAdapter(imageList, fullImage);
                rbResult.setAdapter(adapter);
            }
        }
    }

    class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ImageHolder> {
        private ArrayList<Image> mImageList;
        private boolean mFullImage;
        SimpleDateFormat formatter = new SimpleDateFormat("yy/MM/dd HH:mm:ss", Locale.getDefault());

        ResultAdapter(ArrayList<Image> imageList, boolean fullImage) {
            this.mImageList = imageList;
            this.mFullImage = fullImage;
        }

        @Override
        public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, parent, false);
            return new ImageHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ImageHolder holder, int position) {
            Image image = mImageList.get(position);
            ImagePicker.imageLoader.displayImage(holder.ivImage, image);
            holder.itemView.setTag(R.id.holder_tag, position);
            holder.tvName.setText(image.getName());
            if (image.getSize() > 0) {
                holder.tvSize.setText(Formatter.formatFileSize(getApplicationContext(), image.getSize()));
            }
            if (image.getDateTime() > 0) {
                Date date = new Date(image.getDateTime() * 1000);
                holder.tvDateTime.setText(formatter.format(date));
            }
        }

        @Override
        public int getItemCount() {
            return mImageList == null ? 0 : mImageList.size();
        }

        class ImageHolder extends RecyclerView.ViewHolder {
            ImageView ivImage;
            TextView tvName, tvSize, tvDateTime;
            CheckBox cbFullImage;

            ImageHolder(View itemView) {
                super(itemView);
                ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
                tvName = (TextView) itemView.findViewById(R.id.tvName);
                tvSize = (TextView) itemView.findViewById(R.id.tvSize);
                tvDateTime = (TextView) itemView.findViewById(R.id.tvDateTime);
                cbFullImage = (CheckBox) itemView.findViewById(R.id.cbFullImage);
                if (mFullImage) {
                    cbFullImage.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private class MyImageLoader implements ImagePicker.ImageLoader {
        public void displayImage(ImageView imageView, Image image) {
            Glide.with(imageView.getContext())
                    .load(image.getPath())
                    .dontAnimate()
                    .placeholder(R.drawable.place_holder)
                    .into(imageView);
        }
    }
}
