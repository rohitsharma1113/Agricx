package com.agricx.app.agricximagecapture.ui;

import android.content.Context;
import android.os.AsyncTask;

import com.agricx.app.agricximagecapture.data.FileStorage;
import com.agricx.app.agricximagecapture.pojo.ImageCollectionLog;

public class LogReaderTask<T> extends AsyncTask<Void, Void, T> {

    public interface LogReadDoneListener {
        <E> void onLogReadDone(E log);
    }

    private Context context;
    private LogReadDoneListener logReadDoneListener;
    private String fileName;
    private Class<T> classType;

    public LogReaderTask(Context context,String fileName, Class<T> classType, LogReadDoneListener logReadDoneListener){
        this.context = context;
        this.logReadDoneListener = logReadDoneListener;
        this.fileName = fileName;
        this.classType = classType;
    }

    @Override
    protected T doInBackground(Void... voids) {
        return FileStorage.getCompleteImageCollectionLog(context, fileName, classType);
    }

    @Override
    protected void onPostExecute(T log) {
        logReadDoneListener.onLogReadDone(log);
    }
}
