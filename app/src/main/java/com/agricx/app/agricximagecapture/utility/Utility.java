package com.agricx.app.agricximagecapture.utility;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;

import com.agricx.app.agricximagecapture.pojo.ImageCollectionLog;
import com.agricx.app.agricximagecapture.pojo.LotInfo;
import com.agricx.app.agricximagecapture.pojo.SampleInfo;

import java.io.File;
import java.util.ArrayList;

import static android.content.Context.TELEPHONY_SERVICE;

public class Utility {

    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Nullable
    public static LotInfo getLotInfoFromLotId(String lotId, @NonNull ImageCollectionLog completeImageCollectionLog){
        for (LotInfo lotInfo: completeImageCollectionLog.getLotInfoList()){
            if (lotInfo.getLotId().equalsIgnoreCase(lotId)){
                return lotInfo;
            }
        }
        return null;
    }

    @Nullable
    public static SampleInfo getSampleInfoFromSampleId(long sampleId, @NonNull ArrayList<SampleInfo> sampleInfoList){
        for (SampleInfo sampleInfo: sampleInfoList){
            if (sampleInfo.getSampleId() == sampleId) {
                return sampleInfo;
            }
        }
        return null;
    }

    // Sample Collection Images folder
    public static File getParentDirectory(String subFolderName){
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                subFolderName);
    }


    public static String getDeviceImei(Context context){
        TelephonyManager manager = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
        return manager.getDeviceId();
    }

    public static void performFileScan(Context context, String filePath){
        MediaScannerConnection.scanFile(context, new String[] { filePath }, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                    }
                });
    }

}
