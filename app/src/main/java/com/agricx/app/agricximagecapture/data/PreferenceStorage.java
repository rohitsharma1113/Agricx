package com.agricx.app.agricximagecapture.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.agricx.app.agricximagecapture.pojo.LastEnteredInfo;
import com.agricx.app.agricximagecapture.pojo.LotInfo;
import com.google.gson.Gson;

/**
 * Created by rohit on 18/4/17.
 */

public class PreferenceStorage {

    private static final String PREF_CURRENT_INFO = "pref_current_info";

    public static void saveLastEnteredInfo(Context context, LastEnteredInfo lastEnteredInfo){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String data = (new Gson()).toJson(lastEnteredInfo);
        sharedPreferences.edit().putString(PREF_CURRENT_INFO, data).apply();
    }

    @Nullable
    public static LastEnteredInfo getLastEnteredInfo(Context context){
        String data = PreferenceManager.getDefaultSharedPreferences(context).getString(PREF_CURRENT_INFO, null);
        return (new Gson()).fromJson(data, LastEnteredInfo.class);
    }
}
