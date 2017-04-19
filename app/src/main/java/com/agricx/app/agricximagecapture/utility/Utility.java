package com.agricx.app.agricximagecapture.utility;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.Nullable;

import com.agricx.app.agricximagecapture.data.FileStorage;
import com.agricx.app.agricximagecapture.pojo.ImageCollectionLog;
import com.agricx.app.agricximagecapture.pojo.LotInfo;
import com.agricx.app.agricximagecapture.pojo.SampleInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by rohit on 17/4/17.
 */

public class Utility {

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Nullable
    public static LotInfo getLotInfoFromLotId(String lotId, ImageCollectionLog completeImageCollectionLog){
        if (completeImageCollectionLog != null){
            for (LotInfo lotInfo: completeImageCollectionLog.getLotInfoList()){
                if (lotInfo.getLotId().equalsIgnoreCase(lotId)){
                    return lotInfo;
                }
            }
        }
        return null;
    }

    @Nullable
    public static SampleInfo getSampleInfoFromSampleId(long sampleId, ArrayList<SampleInfo> sampleInfoList){
        for (SampleInfo sampleInfo: sampleInfoList){
            if (sampleInfo.getSampleId() == sampleId) {
                return sampleInfo;
            }
        }
        return null;
    }

    public static File getAgricxImagesFolderName(){
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                AppConstants.ARICX_IMAGES_FOLDER_NAME);
    }
}
