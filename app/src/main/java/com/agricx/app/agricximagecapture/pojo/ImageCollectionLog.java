package com.agricx.app.agricximagecapture.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by rohit on 18/4/17.
 */

public class ImageCollectionLog {

    @SerializedName("collectionLog")
    private ArrayList<LotInfo> lotInfoList;

    public ImageCollectionLog(){
        this.lotInfoList = new ArrayList<>();
    }

    public ArrayList<LotInfo> getLotInfoList() {
        return lotInfoList;
    }

    public void setLotInfoList(ArrayList<LotInfo> lotInfoList) {
        this.lotInfoList = lotInfoList;
    }
}
