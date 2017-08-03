package com.agricx.app.agricximagecapture.data;

import android.content.Context;
import android.support.annotation.Nullable;

import com.agricx.app.agricximagecapture.pojo.ImageCollectionLog;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FileStorage {

    public static final String FILE_IMAGE_COLLECTION_LOG = "file_collection_log";
    public static final String FILE_IMAGE_COLLECTION_LOG_RECERTIFIED = "file_collection_log_rectified";
    public static final String FILE_TRAINING_COLLECTION_LOG = "file_collection_log_training";

    public static <T> boolean saveStringDataToFile(Context context, String fileName, T dataObject){
        String data = new Gson().toJson(dataObject);
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(fileName, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Nullable
    public static <T> T getCompleteImageCollectionLog(Context context, String fileName, Class<T> classType){
        try {
            FileInputStream fis = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            String data = sb.toString();
            return (new Gson()).fromJson(data, classType);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
