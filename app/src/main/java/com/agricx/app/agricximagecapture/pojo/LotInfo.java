package com.agricx.app.agricximagecapture.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by rohit on 18/4/17.
 */

public class LotInfo {

    @SerializedName("lotId")
    private String lotId;

    @SerializedName("samples")
    private ArrayList<SampleInfo> sampleInfoList;

    public LotInfo(String lotId, long sampleId){
        this.lotId = lotId;
        this.sampleInfoList = new ArrayList<>();
        this.sampleInfoList.add(new SampleInfo(sampleId));
    }

    public String getLotId() {
        return lotId;
    }

    public ArrayList<SampleInfo> getSampleInfoList() {
        return sampleInfoList;
    }

    public void setLotId(String lotId) {
        this.lotId = lotId;
    }

    public void setSampleInfoList(ArrayList<SampleInfo> sampleInfoList) {
        this.sampleInfoList = sampleInfoList;
    }
}
