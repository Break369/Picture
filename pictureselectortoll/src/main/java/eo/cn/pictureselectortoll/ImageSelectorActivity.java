package eo.cn.pictureselectortoll;


import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import eo.cn.pictureselectortoll.utils.MPermissionUtils;
import eo.cn.pictureselectortoll.utils.Utils;

public class ImageSelectorActivity extends FragmentActivity implements ImageSelectorFragment.Callback {


    public static final String EXTRA_RESULT = "select_result";

    private ArrayList<String> pathList = new ArrayList<String>();

    private ImageConfig imageConfig;

    private TextView title_text;
    private TextView submitButton;
    private RelativeLayout imageselector_title_bar_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.imageselector_activity);

        imageConfig = ImageSelector.getImageConfig();

        Utils.hideTitleBar(this, R.id.imageselector_activity_layout, imageConfig.getSteepToolBarColor());

        getSupportFragmentManager().beginTransaction()
                .add(R.id.image_grid, Fragment.instantiate(this, ImageSelectorFragment.class.getName(), null))
                .commit();

        submitButton = (TextView) super.findViewById(R.id.title_right);
        title_text = (TextView) super.findViewById(R.id.title_text);
        imageselector_title_bar_layout = (RelativeLayout) super.findViewById(R.id.imageselector_title_bar_layout);

        init();

    }

    private void init() {
        title_text.setTextColor(imageConfig.getTitleTextColor());
        imageselector_title_bar_layout.setBackgroundColor(imageConfig.getTitleBgColor());

        pathList = imageConfig.getPathList();


        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });


        if (pathList == null || pathList.size() <= 0) {
            submitButton.setText(getResources().getString(R.string.finish));
            submitButton.setEnabled(false);
        } else {
            submitButton.setText(getResources().getString(R.string.finish) + "(" + pathList.size() + "/" + imageConfig.getMaxSize() + ")");
            submitButton.setEnabled(true);
        }
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pathList != null && pathList.size() > 0) {
                    SelectedFinish();
                }
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //裁剪返回
        if (requestCode == ImageSelector.IMAGE_CROP_CODE && resultCode == RESULT_OK) {
            pathList.add(cropImagePath);
            SelectedFinish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void SelectedFinish() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra(EXTRA_RESULT, pathList);
        setResult(RESULT_OK, intent);

        //改变gridview的内容
        if (imageConfig.getContainerAdapter() != null) {
            imageConfig.getContainerAdapter().refreshData(pathList, imageConfig.getImageLoader());
        }
        finish();
    }

    private String cropImagePath;

    private void crop(String imagePath, int aspectX, int aspectY, int outputX, int outputY) {

        File file;
        if (Utils.existSDCard()) {
            file = new File(Environment.getExternalStorageDirectory() + imageConfig.getFilePath(), Utils.getImageName());
        } else {
            file = new File(getCacheDir(), Utils.getImageName());
        }
        cropImagePath = file.getAbsolutePath();
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(Uri.fromFile(new File(imagePath)), "image/*");
        Uri outputUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //TODO:访问相册需要被限制，需要通过FileProvider创建一个content类型的Uri
            ContentValues contentValues = new ContentValues(1);
            contentValues.put(MediaStore.Images.Media.DATA,file.getAbsolutePath());
            outputUri = this.getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            //TODO:裁剪整个流程，估计授权一次就好outputUri不需要ContentUri,否则失败
        } else
        {
            outputUri = Uri.fromFile(file);
        }
        /*Uri uri = FileProvider.getUriForFile(this,
                "eo.cn.pictureselectortoll.fileprovider", new File(imagePath));//通过FileProvider创建一个content类型的Uri
        intent.setDataAndType(uri,"image");*/
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", aspectX);
        intent.putExtra("aspectY", aspectY);
        intent.putExtra("outputX", outputX);
        intent.putExtra("outputY", outputY);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
        startActivityForResult(intent, ImageSelector.IMAGE_CROP_CODE);
    }


    @Override
    public void onChangeAlbum(String albumName) {
        title_text.setText(albumName);
    }

    @Override
    public void onSingleImageSelected(final String path) {
        if (imageConfig.isCrop()) {
            MPermissionUtils.requestPermissionsResult(this, 4,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}
                    , new MPermissionUtils.OnPermissionListener() {
                        @Override
                        public void onPermissionGranted() {
                            crop(path, imageConfig.getAspectX(), imageConfig.getAspectY(), imageConfig.getOutputX(), imageConfig.getOutputY());
                            //  Toast.makeText(MainActivity.this, "授权成功,执行拨打电话操作!", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onPermissionDenied() {
                            MPermissionUtils.showTipsDialog(ImageSelectorActivity.this);
                        }
                    });
        } else {
            pathList.add(path);
            SelectedFinish();
        }
    }
    //6.0权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        MPermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onImageSelected(String path) {
        if (!pathList.contains(path)) {
            pathList.add(path);
        }
        if (pathList.size() > 0) {
            submitButton.setText(getResources().getString(R.string.finish) + "(" + pathList.size() + "/" + imageConfig.getMaxSize() + ")");
            if (!submitButton.isEnabled()) {
                submitButton.setEnabled(true);
            }
        }
    }

    @Override
    public void onImageUnselected(String path) {
        if (pathList.contains(path)) {
            pathList.remove(path);
            submitButton.setText(getResources().getString(R.string.finish) + "(" + pathList.size() + "/" + imageConfig.getMaxSize() + ")");
        } else {
            submitButton.setText(getResources().getString(R.string.finish) + "(" + pathList.size() + "/" + imageConfig.getMaxSize() + ")");
        }
        if (pathList.size() == 0) {
            submitButton.setText(getResources().getString(R.string.finish));
            submitButton.setEnabled(false);
        }
    }

    @Override
    public void onCameraShot(final File imageFile) {
        if (imageFile != null) {
            if (imageConfig.isCrop()) {
                MPermissionUtils.requestPermissionsResult(this, 4,
                        new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}
                        , new MPermissionUtils.OnPermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                crop(imageFile.getAbsolutePath(), imageConfig.getAspectX(), imageConfig.getAspectY(), imageConfig.getOutputX(), imageConfig.getOutputY());
                            }
                            @Override
                            public void onPermissionDenied() {
                                MPermissionUtils.showTipsDialog(ImageSelectorActivity.this);
                            }
                        });
            } else {
                pathList.add(imageFile.getAbsolutePath());
                SelectedFinish();
            }
        }
    }

}
