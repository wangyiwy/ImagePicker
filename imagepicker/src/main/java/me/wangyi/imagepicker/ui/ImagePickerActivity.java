package me.wangyi.imagepicker.ui;

import android.Manifest;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.wangyi.imagepicker.ImagePicker;
import me.wangyi.imagepicker.R;
import me.wangyi.imagepicker.adapter.FolderAdapter;
import me.wangyi.imagepicker.adapter.ImageAdapter;
import me.wangyi.imagepicker.model.Folder;
import me.wangyi.imagepicker.model.Image;

/**
 * Created on 2017/5/10.
 *
 * @author WangYi
 * @since 1.0.0
 */

public class ImagePickerActivity extends AppCompatActivity implements View.OnClickListener,
        FolderAdapter.OnFolderSelectedListener, ImageAdapter.OnImageSelectedListener,
        ImageAdapter.ImageSelectCountCallBack {
    private final int REQUEST_CAMERA = 200;
    private final int REQUEST_PREVIEW = 201;
    private final int REQUEST_CROP = 202;

    private final int REQUEST_PERMISSION_CAMERA = 300;
    private final int REQUEST_PERMISSION_SDCARD = 301;

    private ArrayList<Folder> mFolderList = new ArrayList<>();
    private ArrayList<Image> mDisplayedImageList = new ArrayList<>();
    private ArrayList<Image> mSelectedImageList = new ArrayList<>();

    GridView gvImageList;
    TextView tvPreView;
    TextView tvFolderName;
    FolderPopupWindow mPopupWindow;
    ImageAdapter mImageAdapter;

    /*使用原图*/
    private boolean mFullImageEnable = false;
    /*选择模式*/
    private int mSelectMode;
    /*最多选择图片数*/
    public static int MaxSelectSize;
    /*是否支持裁剪*/
    private boolean mCropEnable;
    /*裁剪后返回的图片宽*/
    private int mCropOutputY;
    /*裁剪后返回的图片高*/
    private int mCropOutputX;

    /*裁剪保存的文件*/
    private File mCropImageFile;
    /*拍照保存的文件*/
    private File mTakePictureFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        initParameters();

        ActionBar actionBar = getSupportActionBar();
        //noinspection ConstantConditions
        actionBar.setDisplayHomeAsUpEnabled(true);

        LinearLayout llFolder = (LinearLayout) findViewById(R.id.llFolder);
        llFolder.setOnClickListener(this);

        gvImageList = (GridView) findViewById(R.id.gvImageList);
        mImageAdapter = new ImageAdapter(mDisplayedImageList, this, mSelectMode);
        mImageAdapter.setOnImageSelectedListener(this);
        gvImageList.setAdapter(mImageAdapter);

        mPopupWindow = new FolderPopupWindow(this, mFolderList);
        mPopupWindow.setOnFolderSelectedListener(this);

        gvImageList.post(new Runnable() {
            @Override
            public void run() {
                int height = gvImageList.getHeight();
                mPopupWindow.setWidth(-1);
                mPopupWindow.setHeight(height);
            }
        });

        tvFolderName = (TextView) findViewById(R.id.tvFolderName);
        tvPreView = (TextView) findViewById(R.id.tvPreView);
        tvPreView.setOnClickListener(this);

        FloatingActionButton fabCamera = (FloatingActionButton) findViewById(R.id.fabCamera);
        fabCamera.setOnClickListener(this);

        if (mSelectMode == ImagePicker.MODE_SINGLE_SELECT) {
            actionBar.setTitle(getString(R.string.choose_image));
        } else {
            actionBar.setTitle(0 + "/" + MaxSelectSize);
            tvPreView.setVisibility(View.VISIBLE);
        }

        initData();
    }

    /**
     * 加载本地所有图片
     */
    private void initData() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            getLoaderManager().initLoader(0, null, new ImageLoaderCallback());
        } else {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_SDCARD);
        }
    }

    /**
     * 初始化参数
     */
    private void initParameters() {
        mSelectMode = getIntent().getIntExtra(ImagePicker.PARAM_SELECT_MODE,
                ImagePicker.MODE_MULTI_SELECT);
        MaxSelectSize = getIntent().getIntExtra(ImagePicker.PARAM_MAX_SELECT_SIZE, 9);
        mCropEnable = getIntent().getBooleanExtra(ImagePicker.PARAM_CROP_ENABLE, false);
        mCropOutputX = getIntent().getIntExtra(ImagePicker.PARAM_CROP_OUTPUT_X, 400);
        mCropOutputY = getIntent().getIntExtra(ImagePicker.PARAM_CROP_OUTPUT_Y, 400);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.llFolder) {
            switchPopupWindow(v);
        } else if (id == R.id.tvPreView) {
            preView();
        } else if (id == R.id.fabCamera) {
            openCamera();
        }
    }

    private void switchPopupWindow(View v) {
        if (mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        } else {
            int marginBottom;
            if (hasNavBar()) {
                marginBottom = v.getHeight() + getNavigationBarHeight();
            } else {
                marginBottom = v.getHeight();
            }
            mPopupWindow.showAtLocation(v, Gravity.BOTTOM, 0, marginBottom);
        }
    }

    /**
     * 判断是否有虚拟案件
     */
    private boolean hasNavBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.y != size.y;
        } else {
            boolean menu = ViewConfiguration.get(this).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            return !(menu || back);
        }
    }

    /**
     * 获取虚拟按键的高度
     */
    private int getNavigationBarHeight() {
        int navigationBarHeight = 0;
        int id = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (id > 0) {
            navigationBarHeight = getResources().getDimensionPixelSize(id);
        }
        Log.d(ImagePicker.LOG_TAG, "navigationBarHeight:" + navigationBarHeight);
        return navigationBarHeight;
    }

    /**
     * 预览选中的图片
     */
    private void preView() {
        Intent intent = new Intent(this, ImagePreViewActivity.class);
        intent.putParcelableArrayListExtra(ImagePicker.EXTRA_IMAGE_LIST, mSelectedImageList);
        startActivityForResult(intent, REQUEST_PREVIEW);
    }

    /**
     * 准备打开相机
     */
    private void openCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            sendOpenCameraIntent();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_PERMISSION_CAMERA);
        }
    }

    /**
     * 发送Intent 打开相机
     */
    private void sendOpenCameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mTakePictureFile = createOutputFile();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTakePictureFile));
        } else {
            Uri uri = FileProvider.getUriForFile(this, getString(R.string.file_provider_name)
                    , mTakePictureFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    public void onBackPressed() {
        if (mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mSelectMode == ImagePicker.MODE_MULTI_SELECT) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
        } else if (itemId == R.id.menu_done) {
            backWithList();
        }
        return super.onOptionsItemSelected(item);
    }

    private class ImageLoaderCallback implements LoaderManager.LoaderCallbacks<Cursor> {
        private final String[] IMAGE_PROJECTION = {
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media._ID};

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getApplicationContext(),
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION,
                    IMAGE_PROJECTION[4] + ">0 AND " + IMAGE_PROJECTION[3] + "=? OR " + IMAGE_PROJECTION[3] + "=? ",
                    new String[]{"image/jpeg", "image/png"}, IMAGE_PROJECTION[2] + " DESC");
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mFolderList.clear();
            mDisplayedImageList.clear();
            ArrayList<Image> allImageList = new ArrayList<>();
            if (data != null && data.getCount() > 0) {
                while (data.moveToNext()) {
                    String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
                    String name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
                    long dateTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
                    String mimeType = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[3]));
                    long size = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[4]));
                    long id = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[5]));

                    Image image = new Image();
                    image.setPath(path);
                    image.setName(name);
                    image.setDateTime(dateTime);
                    image.setMimeType(mimeType);
                    image.setSize(size);
                    image.setId(id);
                    allImageList.add(image);

                    File imageFile = new File(path);
                    File parentFile = imageFile.getParentFile();
                    Folder folder = new Folder();
                    folder.setName(parentFile.getName());
                    folder.setPath(parentFile.getAbsolutePath());

                    if (mFolderList.contains(folder)) {
                        List<Image> images = mFolderList.get(mFolderList.indexOf(folder)).getImageList();
                        images.add(image);
                    } else {
                        List<Image> imageList = new ArrayList<>();
                        imageList.add(image);
                        folder.setImageList(imageList);
                        folder.setCover(image);
                        mFolderList.add(folder);
                    }
                }

                Folder folder = new Folder();
                if (allImageList.size() > 0) {
                    folder.setCover(allImageList.get(0));
                }
                folder.setPath("/");
                folder.setImageList(allImageList);
                folder.setName(getString(R.string.all_images));
                mFolderList.add(0, folder);

                mDisplayedImageList.addAll(allImageList);
                mImageAdapter.notifyDataSetChanged();
                mPopupWindow.notifyDataSetChanged();
                tvFolderName.setText(folder.getName());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    /**
     * 图片选中回调
     *
     * @param image 所选中的图片
     * @param isAdd 选中或者反选
     */
    @Override
    public void onSelectedUpdate(Image image, boolean isAdd) {
        if (mSelectMode == ImagePicker.MODE_SINGLE_SELECT) {
            if (mCropEnable) {
                startCrop(image);
            } else {
                backWithImage(image);
            }
        } else {
            if (isAdd) {
                mSelectedImageList.add(image);
            } else {
                mSelectedImageList.remove(image);
            }
            updateToolbarTitle();
        }
    }

    /**
     * 调用系统裁剪图片应用
     *
     * @param image 裁剪的图片
     */
    private void startCrop(Image image) {
        Uri uri = getImageContentUri(new File(image.getPath()));
        if (uri == null) {
            return;
        }
        mCropImageFile = createOutputFile();

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", mCropOutputX);
        intent.putExtra("outputY", mCropOutputY);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCropImageFile));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, REQUEST_CROP);
    }

    /**
     * 使用File Uri会报错 需要把文件转成Content Uri
     *
     * @param imageFile 图片文件
     * @return 文件的Content Uri
     */
    public Uri getImageContentUri(File imageFile) {
        try {
            return Uri.parse(MediaStore.Images.Media.insertImage(
                    getContentResolver(),
                    imageFile.getAbsolutePath(), null, null));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 更新Toolbar 上文字提示 并设置预览按钮是否可以点击
     */
    private void updateToolbarTitle() {
        int selectedCount = mSelectedImageList.size();
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(selectedCount + "/" + MaxSelectSize);
        tvPreView.setEnabled(selectedCount > 0);
    }

    /**
     * 文件夹选中回调
     */
    @Override
    public void onFolderSelected(Folder folder) {
        mPopupWindow.dismiss();
        mDisplayedImageList.clear();
        mDisplayedImageList.addAll(folder.getImageList());
        mImageAdapter.notifyDataSetChanged();
        tvFolderName.setText(folder.getName());
    }

    /**
     * 获取已选中图片数
     */
    @Override
    public int getSelectedCount() {
        return mSelectedImageList.size();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendOpenCameraIntent();
            } else {
                Toast.makeText(this, R.string.no_camera_permission, Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_PERMISSION_SDCARD) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLoaderManager().initLoader(0, null, new ImageLoaderCallback());
            } else {
                Toast.makeText(this, R.string.no_sdcard_permission, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                updateGallery(mTakePictureFile);
                if (mSelectMode == ImagePicker.MODE_MULTI_SELECT) {
                    mSelectedImageList.clear();
                    updateToolbarTitle();
                }
            } else if (requestCode == REQUEST_PREVIEW) {
                setResult(RESULT_OK, data);
                finish();
            } else if (requestCode == REQUEST_CROP) {
                backAfterCrop();
            }
        } else if (resultCode == RESULT_CANCELED) {
            if (requestCode == REQUEST_PREVIEW) {
                mFullImageEnable = data.getBooleanExtra(ImagePicker.EXTRA_FULL_IMAGE, false);
                mSelectedImageList = data.getParcelableArrayListExtra(ImagePicker.EXTRA_IMAGE_LIST);
                for (Image image : mDisplayedImageList) {
                    image.setSelected(mSelectedImageList.contains(image));
                }
                mImageAdapter.notifyDataSetChanged();
                updateToolbarTitle();
            }
        }
    }

    /**
     * 单选模式返回
     */
    private void backWithImage(Image image) {
        ArrayList<Image> imageList = new ArrayList<>();
        imageList.add(image);
        Intent data = new Intent();
        data.putParcelableArrayListExtra(ImagePicker.EXTRA_IMAGE_LIST, imageList);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 裁剪图片返回
     */
    private void backAfterCrop() {
        Image image = new Image();
        image.setPath(mCropImageFile.getAbsolutePath());
        image.setName(mCropImageFile.getName());
        ArrayList<Image> imageList = new ArrayList<>();
        imageList.add(image);

        Intent data = new Intent();
        data.putParcelableArrayListExtra(ImagePicker.EXTRA_IMAGE_LIST, imageList);

        setResult(RESULT_OK, data);
        finish();
        updateGallery(mCropImageFile);
    }

    /**
     * 多选模式 完成图片选择
     */
    private void backWithList() {
        Intent data = new Intent();
        data.putParcelableArrayListExtra(ImagePicker.EXTRA_IMAGE_LIST, mSelectedImageList);
        data.putExtra(ImagePicker.EXTRA_FULL_IMAGE, mFullImageEnable);
        setResult(RESULT_OK, data);
        finish();
    }

    /**
     * 发送广播通知ContentProvider更新
     */
    public void updateGallery(File file) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(file));
        sendBroadcast(intent);
    }

    /**
     * 创建一个File 在拍照或者裁剪后将图片保存到file
     */
    private File createOutputFile() {
        String dirName;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            dirName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        } else {
            dirName = Environment.getDataDirectory().getAbsolutePath();
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        Date date = new Date(System.currentTimeMillis());
        String fileName = formatter.format(date) + ".jpg";
        return new File(dirName, fileName);
    }
}
