package com.agricx.app.agricximagecapture.ui;

import android.content.Context;
import android.os.AsyncTask;

import com.agricx.app.agricximagecapture.data.FileStorage;
import com.agricx.app.agricximagecapture.pojo.ImageCollectionLog;

/**
 * Created by rohit on 19/4/17.
 */

public class LogReaderTask extends AsyncTask<Void, Void, ImageCollectionLog> {

    public interface LogReadDoneListener {
        void onLogReadDone(ImageCollectionLog log);
    }

    private Context context;
    private LogReadDoneListener logReadDoneListener;

    public LogReaderTask(Context context, LogReadDoneListener logReadDoneListener){
        this.context = context;
        this.logReadDoneListener = logReadDoneListener;
    }

    @Override
    protected ImageCollectionLog doInBackground(Void... voids) {
        return FileStorage.getCompleteImageCollectionLog(context);
    }

    @Override
    protected void onPostExecute(ImageCollectionLog imageCollectionLog) {
        logReadDoneListener.onLogReadDone(imageCollectionLog);
    }
}
