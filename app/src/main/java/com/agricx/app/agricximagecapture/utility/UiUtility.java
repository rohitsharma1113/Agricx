package com.agricx.app.agricximagecapture.utility;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.IBinder;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.agricx.app.agricximagecapture.R;

/**
 * Created by rohit on 17/4/17.
 */

public class UiUtility {

    public static void requestFocusAndOpenKeyboard(Activity activity, @StringRes int stringRes, EditText editText){
        if (stringRes != AppConstants.EMPTY_STRING_RES){
            editText.setError(activity.getString(stringRes));
        }
        editText.requestFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void showProgressBarAndDisableTouch(ProgressBar progressBar, Window window){
        progressBar.setVisibility(View.VISIBLE);
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public static void hideProgressBarAndEnableTouch(ProgressBar progressBar, Window window){
        progressBar.setVisibility(View.GONE);
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public static void closeKeyboard(Context context, IBinder iBinder) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(iBinder, 0);
    }

    public static void showSuccessAlertDialog(Activity activity){
        (new AlertDialog.Builder(activity))
                .setTitle(R.string.success_title)
                .setMessage(R.string.success_msg)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, null)
                .setIcon(R.drawable.green_tick)
                .create()
                .show();
    }

    public static void showLogSaveFailedDialog(final Activity activity){
        (new AlertDialog.Builder(activity))
                .setTitle(R.string.fail_title)
                .setMessage(R.string.fail_msg)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        activity.finishAffinity();
                    }
                })
                .setIcon(R.drawable.cross)
                .create()
                .show();
    }
}
