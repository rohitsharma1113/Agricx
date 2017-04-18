package com.agricx.app.agricximagecapture.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by rohit on 18/4/17.
 */

public class SampleInfo {

    @SerializedName("sampleId")
    private long sampleId;

    @SerializedName("imageIds")
    private ArrayList<Integer> imageIdList;

    public long getSampleId() {
        return sampleId;
    }

    public ArrayList<Integer> getImageIdList() {
        return imageIdList;
    }
}
