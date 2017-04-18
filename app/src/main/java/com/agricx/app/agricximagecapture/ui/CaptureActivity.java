package com.agricx.app.agricximagecapture.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.agricx.app.agricximagecapture.R;
import com.agricx.app.agricximagecapture.utility.UiUtility;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CaptureActivity extends AppCompatActivity {

    private static final String TAG = CaptureActivity.class.getSimpleName();

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @BindView(R.id.act_capture_iv_preview) ImageView ivPreview;
    @BindView(R.id.act_capture_b_camera) Button bOpenCamera;
    @BindView(R.id.act_capture_et_lot_id) EditText etLotId;
    @BindView(R.id.act_capture_et_sample_id) EditText etSampleId;
    @BindView(R.id.act_capture_et_image_id) EditText etImageId;
    @BindView(R.id.act_capture_b_save_next) Button bSaveAndNext;

    private boolean isImageCaptured;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupInitialView();
    }

    private void setupInitialView(){
        setContentView(R.layout.activity_capture);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        isImageCaptured = false;
    }

    @OnClick({
            R.id.act_capture_b_camera,
            R.id.act_capture_b_minus_lot_id,
            R.id.act_capture_b_plus_lot_id,
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

        if (!isImageCaptured){
            Toast.makeText(this, getString(R.string.please_capture_image), Toast.LENGTH_SHORT).show();
        } else if (lotId.length() == 0){
            UiUtility.requestFocusAndOpenKeyboard(this, R.string.enter_lot_id, etLotId);
        } else if (sampleId.length() == 0){
            UiUtility.requestFocusAndOpenKeyboard(this, R.string.enter_sample_id, etSampleId);
        } else if (imageId.length() == 0){
            UiUtility.requestFocusAndOpenKeyboard(this, R.string.enter_image_id, etImageId);
        } else {
            // TODO : Save image to sd card
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            if (imageBitmap != null){
                ivPreview.setImageBitmap(imageBitmap);
                bOpenCamera.setVisibility(View.GONE);
                isImageCaptured = true;
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
