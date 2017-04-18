package com.agricx.app.agricximagecapture.utility;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.StringRes;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

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
}
