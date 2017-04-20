package com.agricx.app.agricximagecapture.utility;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewAnimationUtils;
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

    public static void showTaskFailedDialog(Activity activity, @StringRes int message){
        (new AlertDialog.Builder(activity))
                .setTitle(R.string.fail_title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.ok, null)
                .setIcon(R.drawable.cross)
                .create()
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void hideAnim(final View view){
        int cx = view.getWidth()/2;
        int cy = view.getHeight()/2;
        float initialRadius = (float) Math.hypot(cx, cy);
        Animator anim;
        anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, initialRadius, 0);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.GONE);
            }
        });
        anim.start();
    }
}
