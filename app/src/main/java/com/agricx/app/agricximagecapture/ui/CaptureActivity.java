package com.agricx.app.agricximagecapture.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.agricx.app.agricximagecapture.R;
import com.agricx.app.agricximagecapture.utility.UiUtility;

import java.io.File;
import java.io.FileOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CaptureActivity extends AppCompatActivity {

    private static final String TAG = CaptureActivity.class.getSimpleName();

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_WRITE_PERMISSION = 2;

    @BindView(R.id.act_capture_iv_preview) ImageView ivPreview;
    @BindView(R.id.act_capture_b_camera) Button bOpenCamera;
    @BindView(R.id.act_capture_et_lot_id) EditText etLotId;
    @BindView(R.id.act_capture_et_sample_id) EditText etSampleId;
    @BindView(R.id.act_capture_et_image_id) EditText etImageId;
    @BindView(R.id.act_capture_b_save_next) Button bSaveAndNext;

    private Bitmap capturedImageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupInitialView();
        ButterKnife.bind(this);
        areReadWritePermissionsGranted();
    }

    private void areReadWritePermissionsGranted(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_WRITE_PERMISSION && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            finish();
        }
    }

    private void setupInitialView(){
        setContentView(R.layout.activity_capture);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
    }

    @OnClick({
            R.id.act_capture_b_camera,
            R.id.act_capture_b_minus_sample_id,
            R.id.act_capture_b_plus_sample_id,
            R.id.act_capture_b_save_next
    })
    void onClick(View view){
        int id = view.getId();
        switch (id){
            case R.id.act_capture_b_camera:
                dispatchTakePictureIntent();
                return;
            case R.id.act_capture_b_save_next:
                saveImageToSdCard();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, getString(R.string.could_not_open_camera), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageToSdCard(){
        String lotId = etLotId.getText().toString().trim();
        String sampleId = etSampleId.getText().toString().trim();
        String imageId = etImageId.getText().toString().trim();

        if (capturedImageBitmap != null){
            Toast.makeText(this, getString(R.string.please_capture_image), Toast.LENGTH_SHORT).show();
        } else if (lotId.length() == 0){
            UiUtility.requestFocusAndOpenKeyboard(this, R.string.enter_lot_id, etLotId);
        } else if (sampleId.length() == 0){
            UiUtility.requestFocusAndOpenKeyboard(this, R.string.enter_sample_id, etSampleId);
        } else if (imageId.length() == 0){
            UiUtility.requestFocusAndOpenKeyboard(this, R.string.enter_image_id, etImageId);
        } else if (!isExternalStorageWritable()){
            Toast.makeText(this, getString(R.string.external_storage_not_writable), Toast.LENGTH_SHORT).show();
        } else {
            File myDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "test");
            if (!myDir.exists()){
                if (!myDir.mkdirs()){
                    Toast.makeText(this, getString(R.string.failed_directory_creation), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            String filename = (new StringBuilder()).append(lotId).append("_").append(sampleId).append("_").append(imageId).append(".jpg").toString();
            File file = new File (myDir, filename);
            try {
                FileOutputStream out = new FileOutputStream(file);
                capturedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                MediaScannerConnection.scanFile(this, new String[] { file.toString() }, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                            }
                        });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            capturedImageBitmap = (Bitmap) extras.get("data");
            if (capturedImageBitmap != null){
                ivPreview.setImageBitmap(capturedImageBitmap);
                bOpenCamera.setVisibility(View.GONE);
            } else {
                Toast.makeText(this, getString(R.string.could_not_capture), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
