package com.agricx.app.agricximagecapture.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.agricx.app.agricximagecapture.R;
import com.agricx.app.agricximagecapture.data.FileStorage;
import com.agricx.app.agricximagecapture.pojo.ImageCollectionLog;
import com.agricx.app.agricximagecapture.pojo.LotInfo;
import com.agricx.app.agricximagecapture.pojo.SampleInfo;
import com.agricx.app.agricximagecapture.ui.LogReaderTask;
import com.agricx.app.agricximagecapture.ui.LogSaverTask;
import com.agricx.app.agricximagecapture.ui.fragment.RectifyLotFragment;
import com.agricx.app.agricximagecapture.utility.AppConstants;
import com.agricx.app.agricximagecapture.utility.UiUtility;
import com.agricx.app.agricximagecapture.utility.Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class CaptureSampleActivity extends CaptureBaseActivity implements RectifyLotFragment.RecertifyLotIdListener {

    private static final String TAG = CaptureSampleActivity.class.getSimpleName();
    private static final String TAG_RECERTIFICATION_FRAGMENT = "tag_recertification";

    @BindView(R.id.act_sample_capture_et_lot_id)
    EditText etLotId;
    @BindView(R.id.act_capture_sample_et_sample_id)
    EditText etSampleId;

    private LotInfo enteredLotInfo;
    private SampleInfo enteredSampleInfo;
    private ImageCollectionLog originalLog;
    private ImageCollectionLog recertifiedLog;
    private boolean isRecertify;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_sample);
    }

    @Override
    protected void setupLogs() {
        UiUtility.showProgressBarAndDisableTouch(progressBar, getWindow());
        new LogReaderTask<>(getApplicationContext(), FileStorage.FILE_IMAGE_COLLECTION_LOG, ImageCollectionLog.class,
                new LogReaderTask.LogReadDoneListener() {
                    @Override
                    public <T> void onLogReadDone(T log) {
                        originalLog = (ImageCollectionLog) log;
                        new LogReaderTask<>(getApplicationContext(), FileStorage.FILE_IMAGE_COLLECTION_LOG_RECERTIFIED, ImageCollectionLog.class,
                                new LogReaderTask.LogReadDoneListener() {
                                    @Override
                                    public <E> void onLogReadDone(E log) {
                                        UiUtility.hideProgressBarAndEnableTouch(progressBar, getWindow());
                                        recertifiedLog = (ImageCollectionLog) log;
                                    }
                                }).execute();
                    }
                }).execute();
    }

    @OnClick({
            R.id.act_sample_capture_b_enter_lot_id,
            R.id.act_sample_capture_b_enter_sample_id,
    })
    void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.act_sample_capture_b_enter_lot_id:
                fillAppropriateSampleAndImageId();
                break;
            case R.id.act_sample_capture_b_enter_sample_id:
                fillAppropriateImageId();
                break;
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
        UiUtility.closeKeyboard(getApplicationContext(), etSampleId.getWindowToken());
    }

    @OnTextChanged(value = R.id.act_sample_capture_et_lot_id, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onLotIdChanged() {
        if (getCurrentFocus() == etLotId) {
            etSampleId.setText("");
            tvImageId.setText("");
            tvRecertifyIndicator.setVisibility(View.GONE);
        }
    }

    @OnTextChanged(value = R.id.act_capture_sample_et_sample_id, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onSampleIdChanged() {
        if (getCurrentFocus() == etSampleId) {
            tvImageId.setText("");
        }
    }

    @Override
    protected void saveImageToSdCardAndSaveLogFile() {
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
            File tempPhotoFile = getTempImageFile();
            String renamedPhotoName;
            StringBuilder renamedPhotoNameBuilder = (new StringBuilder()).append(lotId).append("_").append(sampleId).append("_")
                    .append(imageId).append("_").append(Utility.getDeviceImei(this));
            if (!isRecertify) {
                renamedPhotoName = renamedPhotoNameBuilder.append(".jpg").toString();
            } else {
                renamedPhotoName = renamedPhotoNameBuilder.append("_R").append(".jpg").toString();
            }
            File finalPhotoFile = new File(Utility.getParentDirectory(getAgricxDirectoryName()), renamedPhotoName);
            if (tempPhotoFile.renameTo(finalPhotoFile)) {
                updateCollectionLogVariables();
                UiUtility.showProgressBarAndDisableTouch(progressBar, getWindow());
                ImageCollectionLog saveLog = !isRecertify ? originalLog : recertifiedLog;
                String logFileName = !isRecertify ? FileStorage.FILE_IMAGE_COLLECTION_LOG : FileStorage.FILE_IMAGE_COLLECTION_LOG_RECERTIFIED;
                (new LogSaverTask<>(getApplicationContext(), saveLog, logFileName, finalPhotoFile,
                        new LogSaverTask.LogSaveDoneListener() {
                            @Override
                            public void onLogSaveDone(Boolean saved) {
                                UiUtility.hideProgressBarAndEnableTouch(progressBar, getWindow());
                                if (saved) {
                                    int newImageId = Integer.parseInt(imageId) + 1;
                                    tvImageId.setText(String.valueOf(newImageId));
                                    prepareNewCapture();
                                    UiUtility.showSuccessAlertDialog(CaptureSampleActivity.this);
                                } else {
                                    (new AlertDialog.Builder(CaptureSampleActivity.this))
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

    @Override
    public void updateCollectionLogVariables() {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_recreate_lot) {
            new RectifyLotFragment().show(getSupportFragmentManager(), TAG_RECERTIFICATION_FRAGMENT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean returnValue = super.onCreateOptionsMenu(menu);
        setMenuItemEnabled(menu, menu.findItem(R.id.action_capture_sampling), false);
        return returnValue;
    }

    @Override
    protected String getAgricxDirectoryName() {
        return AppConstants.AGRICX_SAMPLING_IMAGES_DIRECTORY_NAME;
    }

    @Override
    public void onRecertificationLotIdEntered(String lotId) {
        if (TextUtils.isEmpty(lotId)) {
            Toast.makeText(this, getString(R.string.lot_id_cannot_be_blank), Toast.LENGTH_SHORT).show();
        } else {
            if (originalLog == null || Utility.getLotInfoFromLotId(lotId, originalLog) == null) {
                Toast.makeText(this, getString(R.string.lot_id_does_not_exist), Toast.LENGTH_SHORT).show();
            } else if (recertifiedLog != null && Utility.getLotInfoFromLotId(lotId, recertifiedLog) != null) {
                Toast.makeText(this, getString(R.string.lot_id_under_rectification_already), Toast.LENGTH_SHORT).show();
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
}
