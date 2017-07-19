package com.agricx.app.agricximagecapture.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.agricx.app.agricximagecapture.R;
import com.agricx.app.agricximagecapture.camera.MainActivity;
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

    @BindView(R.id.act_capture_iv_preview)
    ImageView ivPreview;
    @BindView(R.id.act_capture_b_camera)
    Button bOpenCamera;
    @BindView(R.id.act_capture_et_lot_id)
    EditText etLotId;
    @BindView(R.id.act_capture_et_sample_id)
    EditText etSampleId;
    @BindView(R.id.act_capture_tv_image_id)
    TextView tvImageId;
    @BindView(R.id.act_capture_b_save_next)
    Button bSaveAndNext;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.act_capture_b_retake)
    ImageButton bRetake;
    @BindView(R.id.act_capture_tv_recertify_indicator)
    TextView tvRecertifyIndicator;

    private Bitmap capturedImageBitmap;
    private Uri capturedImageUri;
    private LotInfo enteredLotInfo;
    private SampleInfo enteredSampleInfo;
    private ImageCollectionLog originalLog;
    private ImageCollectionLog recertifiedLog;
    private Activity thisActivity;
    private Animation scaleUpAnimation;
    private boolean isRecertify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupInitialView();
        updateLogs();
        checkPermissions();
    }

    private void updateLogs() {
        UiUtility.showProgressBarAndDisableTouch(progressBar, getWindow());
        new LogReaderTask(getApplicationContext(), FileStorage.FILE_IMAGE_COLLECTION_LOG, new LogReaderTask.LogReadDoneListener() {
            @Override
            public void onLogReadDone(ImageCollectionLog log) {
                originalLog = log;
                new LogReaderTask(getApplicationContext(), FileStorage.FILE_IMAGE_COLLECTION_LOG_RECERTIFIED, new LogReaderTask.LogReadDoneListener() {
                    @Override
                    public void onLogReadDone(ImageCollectionLog log) {
                        UiUtility.hideProgressBarAndEnableTouch(progressBar, getWindow());
                        recertifiedLog = log;
                    }
                }).execute();
            }
        }).execute();
    }

    private void checkPermissions() {
        String[] PERMISSIONS = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE};
        if (!hasPermissions(this, PERMISSIONS)) {
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
        if (requestCode == REQUEST_PERMISSIONS_ALL) {
            if (grantResults.length > 0) {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        finish();
                    }
                }
            } else {
                finish();
            }
        }
    }

    private void setupInitialView() {
        setContentView(R.layout.activity_capture);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        thisActivity = this;
        progressBar.setVisibility(View.GONE);
        ivPreview.setVisibility(View.GONE);
        bRetake.setVisibility(View.GONE);
        tvRecertifyIndicator.setVisibility(View.GONE);
        scaleUpAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_up_anim);
    }

    @OnClick({
            R.id.act_capture_b_camera,
            R.id.act_capture_b_enter_lot_id,
            R.id.act_capture_b_enter_sample_id,
            R.id.act_capture_b_save_next,
            R.id.act_capture_iv_preview,
            R.id.act_capture_b_retake
    })
    void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.act_capture_b_camera:
                startActivityForResult(new Intent(getApplicationContext(), MainActivity.class), REQUEST_IMAGE_CAPTURE);
//                startActivity(new Intent(getApplicationContext(), MainActivity.class));
//                dispatchTakePictureIntent();
                return;
            case R.id.act_capture_b_save_next:
                saveImageToSdCardAndSaveLogFile();
                return;
            case R.id.act_capture_b_enter_lot_id:
                fillAppropriateSampleAndImageId();
                return;
            case R.id.act_capture_b_enter_sample_id:
                fillAppropriateImageId();
                return;
            case R.id.act_capture_iv_preview:
                openPreviewDialog();
                return;
            case R.id.act_capture_b_retake:
                prepareNewCapture();
        }
    }

    private void openPreviewDialog() {
        if (capturedImageUri != null) {
            FragmentManager fm = getSupportFragmentManager();
            ImagePreviewFragment imagePreviewFragment = ImagePreviewFragment.getInstance(capturedImageUri);
            imagePreviewFragment.show(fm, AppConstants.DIALOG_FRAGMENT_TAG);
        } else {
            Toast.makeText(this, R.string.image_uri_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    private void fillAppropriateSampleAndImageId() {
        final String lotId = etLotId.getText().toString().trim();
        if (lotId.length() == 0) {
            Toast.makeText(this, getString(R.string.enter_lot_id), Toast.LENGTH_SHORT).show();
            return;
        }

        if (originalLog == null) {
            isRecertify = false;
            enteredLotInfo = null;
            enteredSampleInfo = null;
            etSampleId.setText("1");
            tvImageId.setText("1");
            tvRecertifyIndicator.setVisibility(View.GONE);
        } else {
            if (recertifiedLog != null && (enteredLotInfo = Utility.getLotInfoFromLotId(lotId, recertifiedLog)) != null) {
                isRecertify = true;
                ArrayList<SampleInfo> sampleInfoList = enteredLotInfo.getSampleInfoList();
                enteredSampleInfo = Collections.max(sampleInfoList);
                etSampleId.setText(String.valueOf(enteredSampleInfo.getSampleId()));
                ArrayList<Integer> imageIdList = enteredSampleInfo.getImageIdList();
                tvImageId.setText(String.valueOf(Collections.max(imageIdList) + 1));
                tvRecertifyIndicator.setVisibility(View.VISIBLE);
            } else {
                isRecertify = false;
                enteredLotInfo = Utility.getLotInfoFromLotId(lotId, originalLog);
                if (enteredLotInfo != null) {
                    ArrayList<SampleInfo> sampleInfoList = enteredLotInfo.getSampleInfoList();
                    enteredSampleInfo = Collections.max(sampleInfoList);
                    etSampleId.setText(String.valueOf(enteredSampleInfo.getSampleId()));
                    ArrayList<Integer> imageIdList = enteredSampleInfo.getImageIdList();
                    tvImageId.setText(String.valueOf(Collections.max(imageIdList) + 1));
                } else {
                    enteredSampleInfo = null;
                    etSampleId.setText("1");
                    tvImageId.setText("1");
                }
                tvRecertifyIndicator.setVisibility(View.GONE);
            }
        }
        etSampleId.requestFocus();
    }

    private void fillAppropriateImageId() {
        final String lotId = etLotId.getText().toString().trim();
        final String sampleId = etSampleId.getText().toString().trim();
        if (lotId.length() == 0) {
            Toast.makeText(this, getString(R.string.enter_lot_id), Toast.LENGTH_SHORT).show();
            return;
        } else if (sampleId.length() == 0) {
            Toast.makeText(this, getString(R.string.enter_sample_id), Toast.LENGTH_SHORT).show();
            return;
        }

        if (originalLog == null) {
            isRecertify = false;
            enteredLotInfo = null;
            enteredSampleInfo = null;
            tvImageId.setText("1");
            tvRecertifyIndicator.setVisibility(View.GONE);
        } else {
            if (recertifiedLog != null && (enteredLotInfo = Utility.getLotInfoFromLotId(lotId, recertifiedLog)) != null) {
                isRecertify = true;
                ArrayList<SampleInfo> sampleInfoList = enteredLotInfo.getSampleInfoList();
                enteredSampleInfo = Utility.getSampleInfoFromSampleId(Long.parseLong(sampleId), sampleInfoList);
                if (enteredSampleInfo != null) {
                    ArrayList<Integer> imageIdList = enteredSampleInfo.getImageIdList();
                    tvImageId.setText(String.valueOf(Collections.max(imageIdList) + 1));
                } else {
                    tvImageId.setText("1");
                }
                tvRecertifyIndicator.setVisibility(View.VISIBLE);
            } else {
                isRecertify = false;
                enteredLotInfo = Utility.getLotInfoFromLotId(lotId, originalLog);
                if (enteredLotInfo != null) {
                    ArrayList<SampleInfo> sampleInfoList = enteredLotInfo.getSampleInfoList();
                    enteredSampleInfo = Utility.getSampleInfoFromSampleId(Long.parseLong(sampleId), sampleInfoList);
                    if (enteredSampleInfo != null) {
                        ArrayList<Integer> imageIdList = enteredSampleInfo.getImageIdList();
                        tvImageId.setText(String.valueOf(Collections.max(imageIdList) + 1));
                    } else {
                        tvImageId.setText("1");
                    }
                } else {
                    enteredSampleInfo = null;
                    tvImageId.setText("1");
                }
                tvRecertifyIndicator.setVisibility(View.GONE);
            }
        }
    }

    private File createTempImageFile() throws IOException {
        if (!Utility.isExternalStorageWritable()) {
            Toast.makeText(this, getString(R.string.external_storage_not_writable), Toast.LENGTH_SHORT).show();
            return null;
        }
        File myDir = Utility.getAgricxImagesFolderName();
        if (!myDir.exists()) {
            if (!myDir.mkdirs()) {
                UiUtility.showTaskFailedDialog(thisActivity, R.string.failed_directory_creation);
                return null;
            }
        }
        return new File(myDir, AppConstants.TEMP_IMAGE_NAME);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile;
            try {
                photoFile = createTempImageFile();
            } catch (IOException ex) {
                UiUtility.showTaskFailedDialog(thisActivity, R.string.could_not_create_image_file);
                return;
            }

            if (photoFile != null) {
                Uri photoFileUri = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFileUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            } else {
                UiUtility.showTaskFailedDialog(thisActivity, R.string.could_not_create_image_file);
            }
        } else {
            Toast.makeText(this, getString(R.string.could_not_open_camera), Toast.LENGTH_SHORT).show();
        }
    }

    @OnTextChanged(value = R.id.act_capture_et_lot_id, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onLotIdChanged() {
        if (getCurrentFocus() == etLotId) {
            etSampleId.setText("");
            tvImageId.setText("");
            tvRecertifyIndicator.setVisibility(View.GONE);
        }
    }

    @OnTextChanged(value = R.id.act_capture_et_sample_id, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onSampleIdChanged() {
        if (getCurrentFocus() == etSampleId) {
            tvImageId.setText("");
        }
    }

    private void saveImageToSdCardAndSaveLogFile() {
        String lotId = etLotId.getText().toString().trim();
        String sampleId = etSampleId.getText().toString().trim();
        final String imageId = tvImageId.getText().toString().trim();

        if (capturedImageBitmap == null) {
            Toast.makeText(this, getString(R.string.please_capture_image), Toast.LENGTH_SHORT).show();
        } else if (lotId.length() == 0) {
            Toast.makeText(this, getString(R.string.enter_lot_id), Toast.LENGTH_SHORT).show();
        } else if (sampleId.length() == 0) {
            Toast.makeText(this, getString(R.string.sample_id_missing), Toast.LENGTH_SHORT).show();
        } else if (imageId.length() == 0) {
            Toast.makeText(this, getString(R.string.image_id_missing), Toast.LENGTH_SHORT).show();
        } else if (!Utility.isExternalStorageWritable()) {
            Toast.makeText(this, getString(R.string.external_storage_not_writable), Toast.LENGTH_SHORT).show();
        } else {
            File myDir = Utility.getAgricxImagesFolderName();
            File tempPhotoFile = new File(myDir, AppConstants.TEMP_IMAGE_NAME);
            String renamedPhotoName;
            StringBuilder renamedPhotoNameBuilder = (new StringBuilder()).append(lotId).append("_").append(sampleId).append("_")
                    .append(imageId).append("_").append(Utility.getDeviceImei(thisActivity));
            if (!isRecertify) {
                renamedPhotoName = renamedPhotoNameBuilder.append(".jpg").toString();
            } else {
                renamedPhotoName = renamedPhotoNameBuilder.append("_R").append(".jpg").toString();
            }
            File finalPhotoFile = new File(myDir, renamedPhotoName);
            if (tempPhotoFile.renameTo(finalPhotoFile)) {
                saveLotInfoToCollectionLogVariable();
                UiUtility.showProgressBarAndDisableTouch(progressBar, getWindow());
                ImageCollectionLog saveLog = !isRecertify ? originalLog : recertifiedLog;
                String logFileName = !isRecertify ? FileStorage.FILE_IMAGE_COLLECTION_LOG : FileStorage.FILE_IMAGE_COLLECTION_LOG_RECERTIFIED;
                (new LogSaverTask(getApplicationContext(), saveLog, logFileName, finalPhotoFile, new LogSaverTask.LogSaveDoneListener() {
                    @Override
                    public void onLogSaveDone(Boolean saved) {
                        UiUtility.hideProgressBarAndEnableTouch(progressBar, getWindow());
                        if (saved) {
                            int newImageId = Integer.parseInt(imageId) + 1;
                            tvImageId.setText(String.valueOf(newImageId));
                            prepareNewCapture();
                            UiUtility.showSuccessAlertDialog(thisActivity);
                        } else {
                            (new AlertDialog.Builder(thisActivity))
                                    .setTitle(R.string.fail_title)
                                    .setMessage(R.string.fail_save_log_msg)
                                    .setCancelable(false)
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            fillAppropriateImageId();
                                        }
                                    })
                                    .setIcon(R.drawable.cross)
                                    .create()
                                    .show();
                        }
                    }
                })).execute();
            } else {
                Toast.makeText(getApplicationContext(), R.string.rename_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveLotInfoToCollectionLogVariable() {
        if (!isRecertify && originalLog == null) {
            enteredLotInfo = new LotInfo(etLotId.getText().toString().trim());
            enteredSampleInfo = new SampleInfo(Long.parseLong(etSampleId.getText().toString().trim()));
            enteredLotInfo.getSampleInfoList().add(enteredSampleInfo);

            originalLog = new ImageCollectionLog();
            originalLog.getLotInfoList().add(enteredLotInfo);
        } else if (isRecertify && recertifiedLog == null) {
            enteredLotInfo = new LotInfo(etLotId.getText().toString().trim());
            enteredSampleInfo = new SampleInfo(Long.parseLong(etSampleId.getText().toString().trim()));
            enteredLotInfo.getSampleInfoList().add(enteredSampleInfo);

            recertifiedLog = new ImageCollectionLog();
            recertifiedLog.getLotInfoList().add(enteredLotInfo);
        } else {
            if (enteredLotInfo == null) {
                enteredLotInfo = new LotInfo(etLotId.getText().toString().trim());
                enteredSampleInfo = new SampleInfo(Long.parseLong(etSampleId.getText().toString().trim()));
                enteredLotInfo.getSampleInfoList().add(enteredSampleInfo);

                if (!isRecertify) {
                    originalLog.getLotInfoList().add(enteredLotInfo);
                } else {
                    recertifiedLog.getLotInfoList().add(enteredLotInfo);
                }
            } else {
                if (enteredSampleInfo == null) {
                    enteredSampleInfo = new SampleInfo(Long.parseLong(etSampleId.getText().toString().trim()));

                    enteredLotInfo.getSampleInfoList().add(enteredSampleInfo);
                } else {
                    enteredSampleInfo.getImageIdList().add(Integer.valueOf(tvImageId.getText().toString().trim()));
                }
            }
        }
    }

    private void prepareNewCapture() {
        capturedImageUri = null;
        capturedImageBitmap = null;
        bOpenCamera.setVisibility(View.VISIBLE);
        bOpenCamera.startAnimation(scaleUpAnimation);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            UiUtility.hideAnim(ivPreview);
            UiUtility.hideAnim(bRetake);
        } else {
            ivPreview.setVisibility(View.GONE);
            bRetake.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                File tempFile = new File(Utility.getAgricxImagesFolderName(), AppConstants.TEMP_IMAGE_NAME);
                if (!tempFile.exists()) {
                    UiUtility.showTaskFailedDialog(thisActivity, R.string.temp_image_file_not_found);
                    return;
                }
                capturedImageUri = Uri.fromFile(tempFile);
                try {
                    capturedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), capturedImageUri);
                    if (capturedImageBitmap != null) {
                        ivPreview.setVisibility(View.VISIBLE);
                        bRetake.setVisibility(View.VISIBLE);
                        ivPreview.setImageBitmap(capturedImageBitmap);
                        bOpenCamera.setVisibility(View.GONE);
                    } else {
                        UiUtility.showTaskFailedDialog(thisActivity, R.string.could_not_capture);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    UiUtility.showTaskFailedDialog(thisActivity, R.string.could_not_capture);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        /*
        DialogFragment dialogFragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(AppConstants.DIALOG_FRAGMENT_TAG);
        if (dialogFragment != null && dialogFragment.getDialog() != null && dialogFragment.getDialog().isShowing()) {
            dialogFragment.getDialog().dismiss();
        } else {
            (new AlertDialog.Builder(thisActivity))
                    .setTitle(R.string.confirmation)
                    .setMessage(R.string.sure_exit)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            CaptureActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .create()
                    .show();
        }
        */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_recreate_lot) {
            getRectifyAlertDialog().show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private AlertDialog getRectifyAlertDialog() {
        final EditText input = new EditText(thisActivity);
        input.setHint(R.string.enter_lot_id);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        input.setLayoutParams(lp);
        AlertDialog dialog = (new AlertDialog.Builder(thisActivity))
                .setView(input)
                .setTitle(R.string.enter_lot_to_rectify)
                .setCancelable(false)
                .setPositiveButton(R.string.rectify, null)
                .setNegativeButton(R.string.cancel, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String inputLotId = input.getText().toString().trim();
                        if (TextUtils.isEmpty(inputLotId)) {
                            Toast.makeText(thisActivity, getString(R.string.lot_id_cannot_be_blank), Toast.LENGTH_SHORT).show();
                        } else {
                            if (originalLog == null || Utility.getLotInfoFromLotId(inputLotId, originalLog) == null) {
                                Toast.makeText(thisActivity, getString(R.string.lot_id_does_not_exist), Toast.LENGTH_SHORT).show();
                            } else if (recertifiedLog != null && Utility.getLotInfoFromLotId(inputLotId, recertifiedLog) != null) {
                                Toast.makeText(thisActivity, getString(R.string.lot_id_under_rectification_already), Toast.LENGTH_SHORT).show();
                            } else {
                                isRecertify = true;
                                enteredLotInfo = null;
                                enteredSampleInfo = null;
                                tvImageId.setText("1");
                                tvRecertifyIndicator.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                            }
                        }
                    }
                });
            }
        });
        return dialog;
    }

    /*
    private void saveDetails() {
        LastEnteredInfo lastEnteredInfo = new LastEnteredInfo();
        lastEnteredInfo.setLotId(etLotId.getText().toString().trim());
        lastEnteredInfo.setSampleId(Long.parseLong(etSampleId.getText().toString().trim()));
        lastEnteredInfo.setImageId(Long.parseLong(tvImageId.getText().toString().trim()));
        PreferenceStorage.saveLastEnteredInfo(thisActivity, lastEnteredInfo);
    }

    private void fillLastSavedDetails() {
        LastEnteredInfo lastEnteredInfo = PreferenceStorage.getLastEnteredInfo(thisActivity);
        if (lastEnteredInfo != null) {
            etLotId.setText(lastEnteredInfo.getLotId());
            etSampleId.setText(String.valueOf(lastEnteredInfo.getSampleId()));
            tvImageId.setText(String.valueOf(lastEnteredInfo.getImageId()));
        }
    }
    */
}
