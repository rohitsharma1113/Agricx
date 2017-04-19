package com.agricx.app.agricximagecapture.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
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
import com.agricx.app.agricximagecapture.ui.LogReaderTask;
import com.agricx.app.agricximagecapture.ui.LogSaverTask;
import com.agricx.app.agricximagecapture.ui.fragment.ImagePreviewFragment;
import com.agricx.app.agricximagecapture.utility.AppConstants;
import com.agricx.app.agricximagecapture.utility.UiUtility;
import com.agricx.app.agricximagecapture.utility.Utility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class CaptureActivity extends AppCompatActivity {

    private static final String TAG = CaptureActivity.class.getSimpleName();

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_PERMISSIONS_ALL = 100;

    @BindView(R.id.act_capture_iv_preview) ImageView ivPreview;
    @BindView(R.id.act_capture_b_camera) Button bOpenCamera;
    @BindView(R.id.act_capture_et_lot_id) EditText etLotId;
    @BindView(R.id.act_capture_et_sample_id) EditText etSampleId;
    @BindView(R.id.act_capture_tv_image_id) TextView tvImageId;
    @BindView(R.id.act_capture_b_save_next) Button bSaveAndNext;
    @BindView(R.id.progressBar) ProgressBar progressBar;

    private Bitmap capturedImageBitmap;
    private Uri capturedImageUri;
    private LotInfo enteredLotInfo;
    private SampleInfo enteredSampleInfo;
    private ImageCollectionLog imageCollectionLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupInitialView();
        checkPermissions();
    }

    private void checkPermissions(){
        String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(!hasPermissions(this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSIONS_ALL);
        }
    }

    public boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_ALL){
            if (grantResults.length > 0) {
                for (int i = 0; i<permissions.length; i++){
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                        finish();
                    }
                }
            } else {
                finish();
            }
        }
    }

    private void setupInitialView(){
        setContentView(R.layout.activity_capture);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        progressBar.setVisibility(View.GONE);
        ivPreview.setVisibility(View.GONE);
    }

    @OnClick({
            R.id.act_capture_b_camera,
            R.id.act_capture_b_enter_lot_id,
            R.id.act_capture_b_enter_sample_id,
            R.id.act_capture_b_save_next,
            R.id.act_capture_iv_preview
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
                return;
            case R.id.act_capture_iv_preview:
                openPreviewDialog();
        }
    }

    private void openPreviewDialog(){
        if (capturedImageUri != null){
            FragmentManager fm = getSupportFragmentManager();
            ImagePreviewFragment imagePreviewFragment = ImagePreviewFragment.getInstance(capturedImageUri);
            imagePreviewFragment.show(fm, "");
        } else {
            Toast.makeText(this, R.string.image_uri_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    private void fillAppropriateSampleAndImageId(){
        final String lotId = etLotId.getText().toString().trim();
        if (lotId.length() == 0){
            Toast.makeText(this, getString(R.string.enter_lot_id), Toast.LENGTH_SHORT).show();
            return;
        }

        UiUtility.showProgressBarAndDisableTouch(progressBar, getWindow());
        (new LogReaderTask(this, new LogReaderTask.LogReadDoneListener() {
            @Override
            public void onLogReadDone(ImageCollectionLog log) {
                imageCollectionLog = FileStorage.getCompleteImageCollectionLog(getApplicationContext());
                enteredLotInfo = Utility.getLotInfoFromLotId(lotId, imageCollectionLog);
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
            Toast.makeText(this, getString(R.string.enter_lot_id), Toast.LENGTH_SHORT).show();
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

    private File createTempImageFile() throws IOException{
        File myDir = Utility.getAgricxImagesFolderName();
        if (!myDir.exists()){
            if (!myDir.mkdirs()){
                Toast.makeText(this, getString(R.string.failed_directory_creation), Toast.LENGTH_SHORT).show();
                return null;
            }
        }
        return new File(myDir, AppConstants.TEMP_IMAGE_NAME);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) !=  null) {
            File photoFile;
            try {
                photoFile = createTempImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, getString(R.string.could_not_create_image_file), Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null){
                Uri photoFileUri = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFileUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                Toast.makeText(this, getString(R.string.could_not_create_image_file), Toast.LENGTH_SHORT).show();
            }
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
            Toast.makeText(this, getString(R.string.enter_lot_id), Toast.LENGTH_SHORT).show();
        } else if (sampleId.length() == 0){
            Toast.makeText(this, getString(R.string.sample_id_missing), Toast.LENGTH_SHORT).show();
        } else if (imageId.length() == 0){
            Toast.makeText(this, getString(R.string.image_id_missing), Toast.LENGTH_SHORT).show();
        } else if (!Utility.isExternalStorageWritable()){
            Toast.makeText(this, getString(R.string.external_storage_not_writable), Toast.LENGTH_SHORT).show();
        } else {
            File myDir = Utility.getAgricxImagesFolderName();
            File tempPhotoFile = new File(myDir, AppConstants.TEMP_IMAGE_NAME);
            if (!tempPhotoFile.exists()){
                Toast.makeText(getApplicationContext(), R.string.temp_file_does_not_exist, Toast.LENGTH_SHORT).show();
                return;
            }
            String renamedPhotoName = (new StringBuilder()).append(lotId).append("_").append(sampleId).append("_").append(imageId).append(".jpg").toString();
            File finalPhotoFile = new File(myDir, renamedPhotoName);
            if (tempPhotoFile.renameTo(finalPhotoFile)){
                saveLotInfoToCollectionLogVariable();
                (new LogSaverTask(this, imageCollectionLog, new LogSaverTask.LogSaveDoneListener() {
                    @Override
                    public void onLogSaveDone(Boolean saved) {
                        if (saved){
                            capturedImageUri = null;
                            capturedImageBitmap = null;
                            ivPreview.setVisibility(View.GONE);
                            bOpenCamera.setVisibility(View.VISIBLE);
                            int newImageId = Integer.parseInt(imageId) + 1;
                            tvImageId.setText(String.valueOf(newImageId));
                            Toast.makeText(getApplicationContext(), R.string.image_save_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.image_save_failed, Toast.LENGTH_SHORT).show();
                        }
                        UiUtility.hideProgressBarAndEnableTouch(progressBar, getWindow());
                    }
                })).execute();
            } else {
                Toast.makeText(getApplicationContext(), R.string.rename_failed, Toast.LENGTH_SHORT).show();
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
        if (requestCode == REQUEST_IMAGE_CAPTURE){
            if (resultCode == RESULT_OK){
                capturedImageUri = data.getData();
                if (capturedImageUri == null){
                    Toast.makeText(this, getString(R.string.image_uri_not_found), Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    capturedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), capturedImageUri);
                    if (capturedImageBitmap != null){
                        ivPreview.setVisibility(View.VISIBLE);
                        ivPreview.setImageBitmap(capturedImageBitmap);
                        bOpenCamera.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(this, getString(R.string.could_not_capture), Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, getString(R.string.could_not_capture), Toast.LENGTH_SHORT).show();
                }
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
