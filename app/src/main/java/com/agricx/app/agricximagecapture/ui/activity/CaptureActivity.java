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
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import com.agricx.app.agricximagecapture.ui.fragment.RectifyLotFragment;
import com.agricx.app.agricximagecapture.ui.fragment.SettingsFragment;
import com.agricx.app.agricximagecapture.utility.AgricxPreferenceKeys;
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

public class CaptureActivity extends BaseActivity implements RectifyLotFragment.RecertifyLotIdListener {

    private static final String TAG = CaptureActivity.class.getSimpleName();

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG_RECERTIFICATION_FRAGMENT = "tag_recertification";

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
    @BindView(R.id.container)
    FrameLayout flContainer;

    private Bitmap capturedImageBitmap;
    private Uri capturedImageUri;
    private LotInfo enteredLotInfo;
    private SampleInfo enteredSampleInfo;
    private ImageCollectionLog originalLog;
    private ImageCollectionLog recertifiedLog;
    private Activity thisActivity;
    private boolean isRecertify;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupInitialView();
        updateLogs();
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
                // Starts Custom Camera Activity
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra(AppConstants.EXTRA_IMAGE_FOLDER_NAME, AppConstants.AGRICX_IMAGES_FOLDER_NAME);
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
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


    // Now being created in the Camera Code
    /*
    private File createTempImageFile() throws IOException {
        if (!Utility.isExternalStorageWritable()) {
            Toast.makeText(this, getString(R.string.external_storage_not_writable), Toast.LENGTH_SHORT).show();
            return null;
        }
        File myDir = Utility.getAptAgricxFolderName();
        if (!myDir.exists()) {
            if (!myDir.mkdirs()) {
                UiUtility.showTaskFailedDialog(thisActivity, R.string.failed_directory_creation);
                return null;
            }
        }
        return new File(myDir, AppConstants.TEMP_IMAGE_NAME);
    }
    */


    // Used when native camera is used
    /*
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
    */

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
            File myDir = Utility.getAptAgricxFolderName(AppConstants.AGRICX_IMAGES_FOLDER_NAME);
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

        ivPreview.setVisibility(View.GONE);
        bRetake.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                File tempFile = new File(Utility.getAptAgricxFolderName(AppConstants.AGRICX_IMAGES_FOLDER_NAME), AppConstants.TEMP_IMAGE_NAME);
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
        DialogFragment dialogFragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(AppConstants.DIALOG_FRAGMENT_TAG);
        if (dialogFragment != null && dialogFragment.getDialog() != null && dialogFragment.getDialog().isShowing()) {
            dialogFragment.getDialog().dismiss();
        } else if (getFragmentManager().getBackStackEntryCount() > 0) {
            super.onBackPressed();
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
            new RectifyLotFragment().show(getSupportFragmentManager(), TAG_RECERTIFICATION_FRAGMENT);
            return true;
        } else if (id == R.id.action_settings) {
            if (getFragmentManager().getBackStackEntryCount() == 0) {
                getFragmentManager().beginTransaction().addToBackStack("").add(R.id.container, new SettingsFragment()).commit();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRecertificationLotIdEntered(String lotId) {
        if (TextUtils.isEmpty(lotId)) {
            Toast.makeText(thisActivity, getString(R.string.lot_id_cannot_be_blank), Toast.LENGTH_SHORT).show();
        } else {
            if (originalLog == null || Utility.getLotInfoFromLotId(lotId, originalLog) == null) {
                Toast.makeText(thisActivity, getString(R.string.lot_id_does_not_exist), Toast.LENGTH_SHORT).show();
            } else if (recertifiedLog != null && Utility.getLotInfoFromLotId(lotId, recertifiedLog) != null) {
                Toast.makeText(thisActivity, getString(R.string.lot_id_under_rectification_already), Toast.LENGTH_SHORT).show();
            } else {
                isRecertify = true;
                enteredLotInfo = null;
                enteredSampleInfo = null;
                etLotId.setText(lotId);
                etSampleId.setText("1");
                tvImageId.setText("1");
                tvRecertifyIndicator.setVisibility(View.VISIBLE);
                dismissDialogFragment(TAG_RECERTIFICATION_FRAGMENT);
            }
        }
    }

    private void dismissDialogFragment(String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            DialogFragment dialogFragment = (DialogFragment) fragment;
            dialogFragment.dismiss();
        }
    }
}
