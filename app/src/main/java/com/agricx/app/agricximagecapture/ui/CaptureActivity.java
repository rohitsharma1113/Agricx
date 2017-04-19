package com.agricx.app.agricximagecapture.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.agricx.app.agricximagecapture.R;
import com.agricx.app.agricximagecapture.data.FileStorage;
import com.agricx.app.agricximagecapture.pojo.ImageCollectionLog;
import com.agricx.app.agricximagecapture.pojo.LotInfo;
import com.agricx.app.agricximagecapture.pojo.SampleInfo;
import com.agricx.app.agricximagecapture.utility.UiUtility;
import com.agricx.app.agricximagecapture.utility.Utility;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class CaptureActivity extends AppCompatActivity {

    private static final String TAG = CaptureActivity.class.getSimpleName();

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_WRITE_PERMISSION = 2;

    @BindView(R.id.act_capture_iv_preview) ImageView ivPreview;
    @BindView(R.id.act_capture_b_camera) Button bOpenCamera;
    @BindView(R.id.act_capture_et_lot_id) EditText etLotId;
    @BindView(R.id.act_capture_et_sample_id) EditText etSampleId;
    @BindView(R.id.act_capture_tv_image_id) TextView tvImageId;
    @BindView(R.id.act_capture_b_save_next) Button bSaveAndNext;
    @BindView(R.id.progressBar) ProgressBar progressBar;

    private Bitmap capturedImageBitmap;
    private LotInfo enteredLotInfo;
    private SampleInfo enteredSampleInfo;
    private ImageCollectionLog imageCollectionLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupInitialView();
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
        progressBar.setVisibility(View.GONE);
    }

    @OnClick({
            R.id.act_capture_b_camera,
            R.id.act_capture_b_enter_lot_id,
            R.id.act_capture_b_enter_sample_id,
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
                return;
            case R.id.act_capture_b_enter_lot_id:
                fillAppropriateSampleAndImageId();
                return;
            case R.id.act_capture_b_enter_sample_id:
                fillAppropriateImageId();
        }
    }

    private void fillAppropriateSampleAndImageId(){
        final String lotId = etLotId.getText().toString().trim();
        if (lotId.length() == 0){
            UiUtility.requestFocusAndOpenKeyboard(this, R.string.enter_lot_id, etLotId);
            return;
        }

        UiUtility.showProgressBarAndDisableTouch(progressBar, getWindow());
        (new LogReaderTask(this, new LogReaderTask.LogReadDoneListener() {
            @Override
            public void onLogReadDone(ImageCollectionLog log) {
                imageCollectionLog = FileStorage.getCompleteImageCollectionLog(getApplicationContext());
                enteredLotInfo = Utility.getLotInfoFromLotId(getApplicationContext(), lotId, imageCollectionLog);
                if (enteredLotInfo != null){
                    ArrayList<SampleInfo> sampleInfoList = enteredLotInfo.getSampleInfoList();
                    enteredSampleInfo = Collections.max(sampleInfoList);
                    etSampleId.setText(String.valueOf(enteredSampleInfo.getSampleId()));
                    ArrayList<Integer> imageIdList = enteredSampleInfo.getImageIdList();
                    tvImageId.setText(String.valueOf(Collections.max(imageIdList) + 1));
                } else {
                    etSampleId.setText("1");
                    tvImageId.setText("1");
                }
                UiUtility.hideProgressBarAndEnableTouch(progressBar, getWindow());
            }
        })).execute();
    }

    private void fillAppropriateImageId(){
        String lotId = etLotId.getText().toString().trim();
        String sampleId = etSampleId.getText().toString().trim();
        if (lotId.length() == 0){
            UiUtility.requestFocusAndOpenKeyboard(this, R.string.enter_lot_id, etLotId);
        } else if (sampleId.length() == 0){
            Toast.makeText(this, getString(R.string.sample_id_missing), Toast.LENGTH_SHORT).show();
        } else if (enteredLotInfo != null){
            ArrayList<SampleInfo> sampleInfoList = enteredLotInfo.getSampleInfoList();
            enteredSampleInfo = Utility.getSampleInfoFromSampleId(Long.parseLong(sampleId), sampleInfoList);
            if (enteredSampleInfo != null){
                ArrayList<Integer> imageIdList = enteredSampleInfo.getImageIdList();
                tvImageId.setText(String.valueOf(Collections.max(imageIdList) + 1));
            } else {
                tvImageId.setText("1");
            }
        } else {
            tvImageId.setText("1");
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) !=  null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, getString(R.string.could_not_open_camera), Toast.LENGTH_SHORT).show();
        }
    }

    @OnTextChanged(value = R.id.act_capture_et_lot_id, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onLotIdChanged(){
        if (getCurrentFocus() == etLotId){
            etSampleId.setText("");
            tvImageId.setText("");
        }
    }

    @OnTextChanged(value = R.id.act_capture_et_sample_id, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onSampleIdChanged(){
        if (getCurrentFocus() == etSampleId){
            tvImageId.setText("");
        }
    }

    private void saveImageToSdCard(){
        String lotId = etLotId.getText().toString().trim();
        String sampleId = etSampleId.getText().toString().trim();
        final String imageId = tvImageId.getText().toString().trim();

        if (capturedImageBitmap == null){
            Toast.makeText(this, getString(R.string.please_capture_image), Toast.LENGTH_SHORT).show();
        } else if (lotId.length() == 0){
            UiUtility.requestFocusAndOpenKeyboard(this, R.string.enter_lot_id, etLotId);
        } else if (sampleId.length() == 0){
            Toast.makeText(this, getString(R.string.sample_id_missing), Toast.LENGTH_SHORT).show();
        } else if (imageId.length() == 0){
            Toast.makeText(this, getString(R.string.image_id_missing), Toast.LENGTH_SHORT).show();
        } else if (!Utility.isExternalStorageWritable()){
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
                UiUtility.showProgressBarAndDisableTouch(progressBar, getWindow());
                FileOutputStream out = new FileOutputStream(file);
                capturedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                MediaScannerConnection.scanFile(this, new String[] { file.toString() }, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                            }
                        });

                saveLotInfoToCollectionLogVariable();
                (new LogSaverTask(this, imageCollectionLog, new LogSaverTask.LogSaveDoneListener() {
                    @Override
                    public void onLogSaveDone(Boolean saved) {
                        if (saved){
                            capturedImageBitmap = null;
                            ivPreview.setVisibility(View.GONE);
                            bOpenCamera.setVisibility(View.VISIBLE);
                            int newImageId = Integer.parseInt(imageId) + 1;
                            tvImageId.setText(String.valueOf(newImageId));
                            Toast.makeText(getApplicationContext(), R.string.image_save_success, Toast.LENGTH_SHORT).show();
                        } else {
                            // TODO : handle image save failure
                        }
                        UiUtility.hideProgressBarAndEnableTouch(progressBar, getWindow());
                    }
                })).execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveLotInfoToCollectionLogVariable(){
        if (imageCollectionLog == null){
            enteredLotInfo = new LotInfo(etLotId.getText().toString().trim(), Long.parseLong(etSampleId.getText().toString().trim()));
            imageCollectionLog = new ImageCollectionLog();
            imageCollectionLog.getLotInfoList().add(enteredLotInfo);
        } else {
            if (enteredLotInfo == null){
                enteredLotInfo = new LotInfo(etLotId.getText().toString().trim(), Long.parseLong(etSampleId.getText().toString().trim()));
                imageCollectionLog.getLotInfoList().add(enteredLotInfo);
            } else {
                if (enteredSampleInfo == null){
                    enteredSampleInfo = new SampleInfo(Long.parseLong(etSampleId.getText().toString().trim()));
                    enteredLotInfo.getSampleInfoList().add(enteredSampleInfo);
                } else {
                    enteredSampleInfo.getImageIdList().add(Integer.valueOf(tvImageId.getText().toString().trim()));
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            capturedImageBitmap = (Bitmap) extras.get("data");
            if (capturedImageBitmap != null){
                ivPreview.setVisibility(View.VISIBLE);
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
