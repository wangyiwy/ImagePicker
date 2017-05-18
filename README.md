# ImagePicker
Android 图片选择工具，支持单选/多选/裁剪/预览/相机拍摄，图片加载可以自由选择Gilde、picasso、ImageLoader等工具。UI色彩来自App的**colorPrimary**和**colorAccent**

[Sample.apk](/demo/sample-debug.apk)

![](/screenshots/20170518151420.png =540*960)
![](/screenshots/20170518151409.png)
![](/screenshots/20170518151427.png)
![](/screenshots/20170518151821.png)
![](/screenshots/20170518151433.png)
![](/screenshots/20170518151437.png)

#### 添加依赖
~~~
 compile 'me.wangyi:imagepicker:+'
~~~
#### 使用
1. 在AndroidManifest.xml中添加权限：
~~~
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
~~~
2. 在application节点下注册activity
~~~
    <activity android:name="me.wangyi.imagepicker.ui.ImagePickerActivity" />
    <activity android:name="me.wangyi.imagepicker.ui.ImagePreViewActivity" />
~~~
3.实现自己的ImageLoder,在displayImage方法中可以使用项目中使用的图片加载工具来加载图片
~~~
    private class MyImageLoader implements ImagePicker.ImageLoader {
        public void displayImage(ImageView imageView, Image image) {
            Glide.with(imageView.getContext())
                    .load(image.getPath())
                    .dontAnimate()
                    .placeholder(R.drawable.place_holder)
                    .into(imageView);
        }
    }
~~~
4.开始图片选择

单选
~~~
    new ImagePicker()
             .mode(ImagePicker.MODE_SINGLE_SELECT)
             .cropEnable(true)//是否开启图片裁剪
             .cropOutputX(400)//图片裁剪后保存的宽度
             .cropOutputY(400)//图片裁剪后保存的高度
             .imageLoader(new MyImageLoader())
             .requestCode(REQUEST_PICK)
             .start(this);
~~~
多选
~~~
   new ImagePicker()
            .mode(ImagePicker.MODE_MULTI_SELECT)
            .imageLoader(new MyImageLoader())
            .selectLimit(selectLimit)//最多选择图片数量
            .requestCode(REQUEST_PICK)
            .start(this);
~~~
5.在onActivityResult方法中接收返回的
~~~
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PICK) {
                ArrayList<Image> imageList = data.getParcelableArrayListExtra(ImagePicker.EXTRA_IMAGE_LIST);
                //是否使用原图
                boolean fullImage = data.getBooleanExtra(ImagePicker.EXTRA_FULL_IMAGE, false);
            }
        }
    }
~~~
#### 注意事项
ImagePicker的两个Activity的Theme必须有ActionBar，例如
~~~
    <style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
        <!-- Customize your theme here. -->
        <item name="colorPrimary">@color/colorPrimary</item>
        <item name="colorPrimaryDark">@color/colorPrimaryDark</item>
        <item name="colorAccent">@color/colorAccent</item>
    </style>
~~~
