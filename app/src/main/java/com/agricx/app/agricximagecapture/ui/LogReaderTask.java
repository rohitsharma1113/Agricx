package com.agricx.app.agricximagecapture.ui;

import android.content.Context;
import android.os.AsyncTask;

import com.agricx.app.agricximagecapture.data.FileStorage;
import com.agricx.app.agricximagecapture.pojo.ImageCollectionLog;

public class LogReaderTask extends AsyncTask<Void, Void, ImageCollectionLog> {

    public interface LogReadDoneListener {
        void onLogReadDone(ImageCollectionLog log);
    }

    private Context context;
    private LogReadDoneListener logReadDoneListener;
    private String fileName;

    public LogReaderTask(Context context,String fileName, LogReadDoneListener logReadDoneListener){
        this.context = context;
        this.logReadDoneListener = logReadDoneListener;
        this.fileName = fileName;
    }

    @Override
    protected ImageCollectionLog doInBackground(Void... voids) {
        return FileStorage.getCompleteImageCollectionLog(context, fileName);
    }

    @Override
    protected void onPostExecute(ImageCollectionLog imageCollectionLog) {
        logReadDoneListener.onLogReadDone(imageCollectionLog);
    }
}
