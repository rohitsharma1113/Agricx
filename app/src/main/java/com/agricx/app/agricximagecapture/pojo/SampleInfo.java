package com.agricx.app.agricximagecapture.pojo;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SampleInfo implements Comparable<SampleInfo>{

    @SerializedName("sampleId")
    private long sampleId;

    @SerializedName("imageIds")
    private ArrayList<Integer> imageIdList;

    public SampleInfo(long sampleId){
        this.sampleId = sampleId;
        this.imageIdList = new ArrayList<>();
        this.imageIdList.add(1);
    }

    public long getSampleId() {
        return sampleId;
    }

    public ArrayList<Integer> getImageIdList() {
        return imageIdList;
    }

    public void setSampleId(long sampleId) {
        this.sampleId = sampleId;
    }

    public void setImageIdList(ArrayList<Integer> imageIdList) {
        this.imageIdList = imageIdList;
    }

    @Override
    public int compareTo(@NonNull SampleInfo sampleInfo) {
        if (sampleId > sampleInfo.getSampleId()){
            return 1;
        } else if (sampleId < sampleInfo.getSampleId()){
            return -1;
        } else {
            return 0;
        }
    }
}
