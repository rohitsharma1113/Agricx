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

/**
 * Created by rohit on 18/4/17.
 */

public class FileStorage {

    private static final String FILE_IMAGE_COLLECTION_LOG = "file_collection_log";

    public static boolean saveCompleteImageCollectionLog(Context context, ImageCollectionLog imageCollectionLog){
        String data = (new Gson()).toJson(imageCollectionLog);
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(FILE_IMAGE_COLLECTION_LOG, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Nullable
    public static ImageCollectionLog getCompleteImageCollectionLog(Context context){
        try {
            FileInputStream fis = context.openFileInput(FILE_IMAGE_COLLECTION_LOG);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            String data = sb.toString();
            return (new Gson()).fromJson(data, ImageCollectionLog.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
