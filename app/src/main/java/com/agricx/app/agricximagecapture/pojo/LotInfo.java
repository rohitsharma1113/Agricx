package com.agricx.app.agricximagecapture.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by rohit on 18/4/17.
 */

public class LotInfo {

    @SerializedName("lotId")
    private long lotId;

    @SerializedName("samples")
    private ArrayList<SampleInfo> sampleInfoList;

    public long getLotId() {
        return lotId;
    }

    public ArrayList<SampleInfo> getSampleInfoList() {
        return sampleInfoList;
    }
}
