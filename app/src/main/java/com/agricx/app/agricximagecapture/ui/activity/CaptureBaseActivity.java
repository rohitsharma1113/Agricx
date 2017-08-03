package com.agricx.app.agricximagecapture.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.agricx.app.agricximagecapture.R;
import com.agricx.app.agricximagecapture.camera.MainActivity;
import com.agricx.app.agricximagecapture.ui.fragment.ImagePreviewFragment;
import com.agricx.app.agricximagecapture.utility.AppConstants;
import com.agricx.app.agricximagecapture.utility.ImageUtils;
import com.agricx.app.agricximagecapture.utility.UiUtility;
import com.agricx.app.agricximagecapture.utility.Utility;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public abstract class CaptureBaseActivity extends AppCompatActivity {
    public static final int REQUEST_PERMISSIONS_ALL = 100;
    protected static final int REQUEST_IMAGE_CAPTURE = 1;

    @BindView(R.id.view_capture_iv_preview)
    ImageView ivPreview;
    @BindView(R.id.view_capture_b_camera)
    Button bOpenCamera;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.act_base_capture_tv_image_id)
    TextView tvImageId;
    @BindView(R.id.act_base_capture_b_save_next)
    Button bSaveAndNext;
    @BindView(R.id.view_capture_b_retake)
    ImageButton bRetake;
    @BindView(R.id.view_capture_tv_recertify_indicator)
    TextView tvRecertifyIndicator;

    protected Bitmap capturedImageBitmap;
    protected Uri capturedImageUri;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        setupInitialView();
        setupLogs();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    protected void setupInitialView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        progressBar.setVisibility(View.GONE);
        ivPreview.setVisibility(View.GONE);
        bRetake.setVisibility(View.GONE);
        tvRecertifyIndicator.setVisibility(View.GONE);
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

    protected void openPreviewDialog() {
        if (capturedImageUri != null) {
            FragmentManager fm = getSupportFragmentManager();
            ImagePreviewFragment imagePreviewFragment = ImagePreviewFragment.getInstance(capturedImageUri);
            imagePreviewFragment.show(fm, AppConstants.DIALOG_FRAGMENT_TAG);
        } else {
            Toast.makeText(this, R.string.image_uri_not_found, Toast.LENGTH_SHORT).show();
        }
    }

    protected abstract void updateCollectionLogVariables();

    protected void dismissDialogFragment(String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            DialogFragment dialogFragment = (DialogFragment) fragment;
            dialogFragment.dismiss();
        }
    }

    protected void prepareNewCapture() {
        capturedImageUri = null;
        capturedImageBitmap.recycle();
        capturedImageBitmap = null;
        bOpenCamera.setVisibility(View.VISIBLE);

        ivPreview.setVisibility(View.GONE);
        bRetake.setVisibility(View.GONE);
    }

    @OnClick({
            R.id.view_capture_b_camera,
            R.id.view_capture_b_retake,
            R.id.act_base_capture_b_save_next,
            R.id.view_capture_iv_preview,
    })
    void baseOnClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.view_capture_b_camera:
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra(AppConstants.EXTRA_IMAGE_FOLDER_NAME, getAgricxDirectoryName());
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                break;
            case R.id.act_base_capture_b_save_next:
                saveImageToSdCardAndSaveLogFile();
                break;
            case R.id.view_capture_b_retake:
                prepareNewCapture();
                break;
            case R.id.view_capture_iv_preview:
                openPreviewDialog();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                File tempFile = getTempImageFile();
                if (!tempFile.exists()) {
                    UiUtility.showTaskFailedDialog(this, R.string.temp_image_file_not_found);
                    return;
                }
                capturedImageUri = Uri.fromFile(tempFile);
                try {
                    capturedImageBitmap = ImageUtils.decodeSampledBitmapFromUri(getApplicationContext(), capturedImageUri);
                    if (capturedImageBitmap != null) {
                        ivPreview.setVisibility(View.VISIBLE);
                        bRetake.setVisibility(View.VISIBLE);
                        ivPreview.setImageBitmap(capturedImageBitmap);
                        bOpenCamera.setVisibility(View.GONE);
                    } else {
                        UiUtility.showTaskFailedDialog(this, R.string.could_not_capture);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    UiUtility.showTaskFailedDialog(this, R.string.could_not_capture);
                }
            }
        }
    }

    protected File getTempImageFile() {
        return new File(Utility.getParentDirectory(getAgricxDirectoryName()), AppConstants.TEMP_IMAGE_NAME);
    }

    protected abstract void saveImageToSdCardAndSaveLogFile();

    protected abstract String getAgricxDirectoryName();

    protected abstract void setupLogs();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_capture_training) {
            startActivity(new Intent(this, CaptureTrainingActivity.class));
            finish();
            return true;
        } else if (id == R.id.action_capture_sampling) {
            startActivity(new Intent(this, CaptureSampleActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setMenuItemEnabled(Menu menu, MenuItem item, boolean enabled) {
        if (!enabled) {
            menu.removeItem(item.getItemId());
        }
    }

    @Override
    public void onBackPressed() {
        DialogFragment dialogFragment = (DialogFragment) getSupportFragmentManager().findFragmentByTag(AppConstants.DIALOG_FRAGMENT_TAG);
        if (dialogFragment != null && dialogFragment.getDialog() != null && dialogFragment.getDialog().isShowing()) {
            dialogFragment.getDialog().dismiss();
        } else {
            (new AlertDialog.Builder(this))
                    .setTitle(R.string.confirmation)
                    .setMessage(R.string.sure_exit)
                    .setCancelable(false)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            CaptureBaseActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(R.string.no, null)
                    .create()
                    .show();
        }
    }
}
