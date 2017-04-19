package com.agricx.app.agricximagecapture.ui;

import android.content.Context;
import android.os.AsyncTask;

import com.agricx.app.agricximagecapture.data.FileStorage;
import com.agricx.app.agricximagecapture.pojo.ImageCollectionLog;

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

    public LogSaverTask(Context context, ImageCollectionLog log, LogSaveDoneListener logSaveDoneListener){
        this.context = context;
        this.log = log;
        this.logSaveDoneListener = logSaveDoneListener;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        return FileStorage.saveCompleteImageCollectionLog(context, log);
    }

    @Override
    protected void onPostExecute(Boolean logSaved) {
        logSaveDoneListener.onLogSaveDone(logSaved);
    }
}
