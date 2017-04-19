package com.agricx.app.agricximagecapture.pojo;

import com.google.gson.annotations.SerializedName;

/**
 * Created by rohit on 18/4/17.
 */

public class LastEnteredInfo {

    @SerializedName("lotId")
    private String lotId;

    @SerializedName("sampleId")
    private long sampleId;

    @SerializedName("imageId")
    private long imageId;

    public String getLotId() {
        return lotId;
    }

    public void setLotId(String lotId) {
        this.lotId = lotId;
    }

    public long getSampleId() {
        return sampleId;
    }

    public void setSampleId(long sampleId) {
        this.sampleId = sampleId;
    }

    public long getImageId() {
        return imageId;
    }

    public void setImageId(long imageId) {
        this.imageId = imageId;
    }
}
