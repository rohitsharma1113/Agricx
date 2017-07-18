package com.agricx.app.agricximagecapture.ui;

import android.content.Context;
import android.os.AsyncTask;

import com.agricx.app.agricximagecapture.data.FileStorage;
import com.agricx.app.agricximagecapture.pojo.ImageCollectionLog;
import com.agricx.app.agricximagecapture.utility.UiUtility;
import com.agricx.app.agricximagecapture.utility.Utility;

import java.io.File;

/**
 * Created by rohit on 19/4/17.
 */

public class LogSaverTask extends AsyncTask<Void, Void, Boolean> {

    public interface LogSaveDoneListener {
        void onLogSaveDone(Boolean logSaved);
    }

    private ImageCollectionLog log;
    private Context context;
    private LogSaveDoneListener logSaveDoneListener;
    private File imageFile;
    private String logFileName;

    public LogSaverTask(Context context, ImageCollectionLog log, String logFileName, File imageFile, LogSaveDoneListener logSaveDoneListener){
        this.context = context;
        this.log = log;
        this.logSaveDoneListener = logSaveDoneListener;
        this.imageFile = imageFile;
        this.logFileName = logFileName;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        boolean saveStatus = FileStorage.saveCompleteImageCollectionLog(context, log, logFileName);
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
