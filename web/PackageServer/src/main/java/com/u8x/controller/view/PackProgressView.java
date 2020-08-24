package com.u8x.controller.view;

/**
 *
 * Created by ant on 2018/5/3.
 */
public class PackProgressView extends ResponseView{

    private int progress;   //当前进度
    private int packLogID;  //packID
    private int state;      //打包状态



    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getPackLogID() {
        return packLogID;
    }

    public void setPackLogID(int packLogID) {
        this.packLogID = packLogID;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
