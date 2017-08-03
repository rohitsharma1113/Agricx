package com.agricx.app.agricximagecapture.ui.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.agricx.app.agricximagecapture.BuildConfig;
import com.agricx.app.agricximagecapture.R;
import com.agricx.app.agricximagecapture.data.FileStorage;
import com.agricx.app.agricximagecapture.data.TrainingDataUtil;
import com.agricx.app.agricximagecapture.pojo.ImageCollectionLog;
import com.agricx.app.agricximagecapture.pojo.LotInfo;
import com.agricx.app.agricximagecapture.pojo.SampleInfo;
import com.agricx.app.agricximagecapture.ui.LogReaderTask;
import com.agricx.app.agricximagecapture.ui.LogSaverTask;
import com.agricx.app.agricximagecapture.ui.fragment.DefectVarietySelectionFragment;
import com.agricx.app.agricximagecapture.utility.AgricxPreferenceKeys;
import com.agricx.app.agricximagecapture.utility.AppConstants;
import com.agricx.app.agricximagecapture.utility.UiUtility;
import com.agricx.app.agricximagecapture.utility.Utility;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class CaptureTrainingActivity extends CaptureBaseActivity implements
        DefectVarietySelectionFragment.SelectionActionListener {

    @BindView(R.id.act_train_b_select_defect)
    Button bSelectDefect;
    @BindView(R.id.act_train_b_select_variety)
    Button bSelectVariety;
    @BindView(R.id.act_train_rg_c_u)
    RadioGroup rgSetupConditions;
    @BindView(R.id.act_train_rb_controlled)
    RadioButton rbControlled;
    @BindView(R.id.act_train_rb_uncontrolled)
    RadioButton rbUncontrolled;
    @BindView(R.id.act_train_et_sample_id)
    EditText etSampleId;

    private LotInfo enteredLotInfo;
    private SampleInfo enteredSampleInfo;
    private ImageCollectionLog originalLog;

    private TrainingDataUtil.BaseDefect defectSelected;
    private TrainingDataUtil.BaseVariety varietySelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_training);
    }

    @Override
    protected void setupLogs() {
        UiUtility.showProgressBarAndDisableTouch(progressBar, getWindow());
        new LogReaderTask<>(getApplicationContext(), FileStorage.FILE_TRAINING_COLLECTION_LOG, ImageCollectionLog.class, new LogReaderTask.LogReadDoneListener() {
            @Override
            public <E> void onLogReadDone(E log) {
                UiUtility.hideProgressBarAndEnableTouch(progressBar, getWindow());
                originalLog = (ImageCollectionLog) log;
            }
        }).execute();
    }

    @OnClick({
            R.id.act_train_b_select_defect,
            R.id.act_train_b_select_variety,
            R.id.act_train_b_enter_sample_id
    })
    void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.act_train_b_select_defect:
                DefectVarietySelectionFragment.
                        newInstance(DefectVarietySelectionFragment.EXTRA_DEFECTS_SELECTION).
                        show(getSupportFragmentManager(), "");
                break;
            case R.id.act_train_b_select_variety:
                DefectVarietySelectionFragment.
                        newInstance(DefectVarietySelectionFragment.EXTRA_VARIETY_SELECTION).
                        show(getSupportFragmentManager(), "");
                break;
            case R.id.act_train_b_enter_sample_id:
                fillAppropriateImageId();
                break;
        }
    }

    @OnTextChanged(value = R.id.act_train_et_sample_id, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onSampleIdChanged() {
        if (getCurrentFocus() == etSampleId) {
            tvImageId.setText("");
        }
    }

    @Override
    public <T> void onDefectVarietyItemSelected(int selectionType, T item) {
        if (item != null) {
            if (selectionType == DefectVarietySelectionFragment.EXTRA_DEFECTS_SELECTION) {
                defectSelected = (TrainingDataUtil.BaseDefect) item;
                bSelectDefect.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.defect_variety_selected));
                bSelectDefect.setText(getString(R.string.defect_value, defectSelected.getDefectFullName()));
            } else {
                varietySelected = (TrainingDataUtil.BaseVariety) item;
                bSelectVariety.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.defect_variety_selected));
                bSelectVariety.setText(getString(R.string.variety_value, varietySelected.getVarietyFullName()));
            }
        }
        if (defectSelected != null && varietySelected != null) {
            fillAppropriateSampleAndImageId();
        } else {
            etSampleId.setText("");
            tvImageId.setText("");
        }
    }

    private void fillAppropriateSampleAndImageId() {
        if (originalLog == null) {
            enteredLotInfo = null;
            enteredSampleInfo = null;
            etSampleId.setText("1");
            tvImageId.setText("1");
        } else {
            String lotId = getLotId();
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
        }
        etSampleId.requestFocus();
    }

    private void fillAppropriateImageId() {
        String sampleId = etSampleId.getText().toString().trim();
        if (defectSelected == null) {
            Toast.makeText(this, getString(R.string.defect_not_selected), Toast.LENGTH_SHORT).show();
            return;
        } else if (varietySelected == null) {
            Toast.makeText(this, getString(R.string.variety_not_selected), Toast.LENGTH_SHORT).show();
            return;
        } else if (TextUtils.isEmpty(sampleId)) {
            Toast.makeText(this, getString(R.string.enter_sample_id), Toast.LENGTH_SHORT).show();
            return;
        }

        if (originalLog == null){
            enteredLotInfo = null;
            enteredSampleInfo = null;
            tvImageId.setText("1");
        } else {
            String lotId = getLotId();
            enteredLotInfo = Utility.getLotInfoFromLotId(lotId, originalLog);
            if (enteredLotInfo != null){
                ArrayList<SampleInfo> sampleInfoList = enteredLotInfo.getSampleInfoList();
                enteredSampleInfo = Utility.getSampleInfoFromSampleId(Long.parseLong(sampleId), sampleInfoList);
                if (enteredSampleInfo != null){
                    ArrayList<Integer> imageIdList = enteredSampleInfo.getImageIdList();
                    tvImageId.setText(String.valueOf(Collections.max(imageIdList) + 1));
                } else {
                    tvImageId.setText("1");
                }
            } else {
                enteredSampleInfo = null;
                tvImageId.setText("1");
            }
        }
        UiUtility.closeKeyboard(getApplicationContext(), etSampleId.getWindowToken());
    }

    @Override
    protected void saveImageToSdCardAndSaveLogFile() {
        String sampleId = etSampleId.getText().toString().trim();
        final String imageId = tvImageId.getText().toString().trim();

        if (capturedImageBitmap == null) {
            Toast.makeText(this, getString(R.string.please_capture_image), Toast.LENGTH_SHORT).show();
        } else if (defectSelected == null) {
            Toast.makeText(this, getString(R.string.defect_not_selected), Toast.LENGTH_SHORT).show();
        } else if (varietySelected == null) {
            Toast.makeText(this, getString(R.string.variety_not_selected), Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(sampleId)) {
            Toast.makeText(this, getString(R.string.sample_id_missing), Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(imageId)) {
            Toast.makeText(this, getString(R.string.image_id_missing), Toast.LENGTH_SHORT).show();
        } else if (!Utility.isExternalStorageWritable()) {
            Toast.makeText(this, getString(R.string.external_storage_not_writable), Toast.LENGTH_SHORT).show();
        } else {
            File tempPhotoFile = getTempImageFile();
            // Get Renamed photo name
            String condition;
            if (rgSetupConditions.getCheckedRadioButtonId() == rbControlled.getId()) {
                condition = "C";
            } else {
                condition = "U";
            }
            String markerType = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).
                    getString(AgricxPreferenceKeys.PF_MARKER_TYPE, AppConstants.DEFAULT_MARKER_TYPE);
            StringBuilder renamedPhotoNameBuilder = (new StringBuilder()).
                    append(defectSelected.getDefectId()).append("_").
                    append(defectSelected.getDefectShortName()).append("_").
                    append(varietySelected.getVarietyId()).append("_").
                    append(varietySelected.getVarietyShortName()).append("_").
                    append(sampleId).append("_").
                    append(imageId).append("_").
                    append(condition).append("_").
                    append(markerType).append("_").
                    append(Utility.getDeviceImei(this));
            String renamedPhotoName = renamedPhotoNameBuilder.append(".jpg").toString();
            // Rename File Logic
            File finalPhotoFile = new File(Utility.getParentDirectory(getAgricxDirectoryName()), renamedPhotoName);
            if (tempPhotoFile.renameTo(finalPhotoFile)) {
                updateCollectionLogVariables();
                UiUtility.showProgressBarAndDisableTouch(progressBar, getWindow());
                ImageCollectionLog saveLog = originalLog;
                String logFileName = FileStorage.FILE_TRAINING_COLLECTION_LOG;
                (new LogSaverTask<>(getApplicationContext(), saveLog, logFileName, finalPhotoFile,
                        new LogSaverTask.LogSaveDoneListener() {
                            @Override
                            public void onLogSaveDone(Boolean saved) {
                                UiUtility.hideProgressBarAndEnableTouch(progressBar, getWindow());
                                if (saved) {
                                    int newImageId = Integer.parseInt(imageId) + 1;
                                    tvImageId.setText(String.valueOf(newImageId));
                                    prepareNewCapture();
                                    UiUtility.showSuccessAlertDialog(CaptureTrainingActivity.this);
                                } else {
                                    (new AlertDialog.Builder(CaptureTrainingActivity.this))
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
    protected void updateCollectionLogVariables() {
        if (originalLog == null){
            originalLog = new ImageCollectionLog();
            enteredLotInfo = new LotInfo(getLotId());
            enteredSampleInfo = new SampleInfo(Long.parseLong(etSampleId.getText().toString().trim()));
            enteredLotInfo.getSampleInfoList().add(enteredSampleInfo);
            originalLog.getLotInfoList().add(enteredLotInfo);
        } else {
            if (enteredLotInfo == null){
                enteredLotInfo = new LotInfo(getLotId());
                enteredSampleInfo = new SampleInfo(Long.parseLong(etSampleId.getText().toString().trim()));
                enteredLotInfo.getSampleInfoList().add(enteredSampleInfo);
                originalLog.getLotInfoList().add(enteredLotInfo);
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
    protected String getAgricxDirectoryName() {
        return AppConstants.AGRICX_TRAINING_IMAGES_DIRECTORY_NAME;
    }

    private String getLotId() {
        return defectSelected.getDefectId() + "_" + varietySelected.getVarietyId();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean returnValue = super.onCreateOptionsMenu(menu);
        setMenuItemEnabled(menu, menu.findItem(R.id.action_capture_training), false);
        setMenuItemEnabled(menu, menu.findItem(R.id.action_recreate_lot), false);
        if (BuildConfig.FLAVOR.equalsIgnoreCase(AppConstants.FLAVOUR_TRAINING)) {
            setMenuItemEnabled(menu, menu.findItem(R.id.action_capture_sampling), false);
        }
        return returnValue;
    }
}
