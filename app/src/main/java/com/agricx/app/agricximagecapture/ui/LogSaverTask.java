package com.agricx.app.agricximagecapture.ui;

import android.content.Context;
import android.os.AsyncTask;

import com.agricx.app.agricximagecapture.data.FileStorage;
import com.agricx.app.agricximagecapture.pojo.ImageCollectionLog;
import com.agricx.app.agricximagecapture.utility.Utility;
import com.google.gson.Gson;

import java.io.File;

public class LogSaverTask<T> extends AsyncTask<Void, Void, Boolean> {

    public interface LogSaveDoneListener {
        void onLogSaveDone(Boolean logSaved);
    }

    private T log;
    private Context context;
    private LogSaveDoneListener logSaveDoneListener;
    private File imageFile;
    private String logFileName;

    public LogSaverTask(Context context, T log, String logFileName, File imageFile, LogSaveDoneListener logSaveDoneListener){
        this.context = context;
        this.log = log;
        this.logSaveDoneListener = logSaveDoneListener;
        this.imageFile = imageFile;
        this.logFileName = logFileName;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        boolean saveStatus = FileStorage.saveStringDataToFile(context, logFileName, log);
        if (saveStatus){
            Utility.performFileScan(context, imageFile.toString());
        }
        return saveStatus;
    }

    @Override
    protected void onPostExecute(Boolean logSaved) {
        logSaveDoneListener.onLogSaveDone(logSaved);
    }
}
