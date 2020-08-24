package com.u8x.dao.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ant on 2018/6/15.
 */
public class PluginMaster {

    private String sdkName;             //本地插件目录名称
    private String masterName;          //插件名称
    private String versionName;         //版本信息
    private String desc;                // 配置说明

    private int state = 0;              //状态 0：未开启;1：已开启
    private int extend = 0;             //覆写 0：未复写;1：复写

    private List<PluginParamMeta> metas;      //插件参数meta信息

    public PluginMaster(){

    }

    public PluginMaster(PluginMaster master){
        this.sdkName = master.sdkName;
        this.masterName = master.masterName;
        this.versionName = master.versionName;
        this.desc = master.desc;
        this.metas = master.metas;
        this.state = 0;
        this.extend = 0;
    }


    public void addMeta(PluginParamMeta meta){

        if(meta == null) return;

        if(metas == null) {
            metas = new ArrayList<>();
        }
        metas.add(meta);
    }

    public PluginMaster resetCopy(){
        PluginMaster master = new PluginMaster();
        master.sdkName = this.sdkName;
        master.masterName = this.masterName;
        master.versionName = this.versionName;
        master.desc = this.desc;
        master.metas = this.metas;
        return master;
    }


    public String getSdkName() {
        return sdkName;
    }

    public void setSdkName(String sdkName) {
        this.sdkName = sdkName;
    }

    public String getMasterName() {
        return masterName;
    }

    public void setMasterName(String masterName) {
        this.masterName = masterName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<PluginParamMeta> getMetas() {
        return metas;
    }

    public void setMetas(List<PluginParamMeta> metas) {
        this.metas = metas;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getExtend() {
        return extend;
    }

    public void setExtend(int extend) {
        this.extend = extend;
    }
}
