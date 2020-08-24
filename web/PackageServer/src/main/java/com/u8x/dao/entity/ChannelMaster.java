package com.u8x.dao.entity;

import java.io.Serializable;

/**
 * 渠道商
 * Created by ant on 2018/4/8.
 */
public class ChannelMaster implements Serializable {

    private static final long serialVersionUID = 7364383481552655539L;

    private Integer masterID;
    private String sdkName;             //本地SDK目录名称
    private String masterSdkName;       //对应u8server后台渠道商中sdkName
    private String masterName;
    private String versionName;

    private String splash;              //闪屏组合。11:横白;12:横黑;21:竖白;22:竖黑。比如：11|12|21|22
    private String icons;               //含有的角标简写组合。 rt:右上角;rb:右下角;lt:左上角;lb:左下角。 比如rt|rb|lt|lb，说明4个角标都有

    private Integer state;               // 0:正常状态; 1:本地配置有问题

    private String desc;                // 配置说明

    public Integer getMasterID() {
        return masterID;
    }

    public void setMasterID(Integer masterID) {
        this.masterID = masterID;
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

    public String getMasterSdkName() {
        return masterSdkName;
    }

    public void setMasterSdkName(String masterSdkName) {
        this.masterSdkName = masterSdkName;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getSplash() {
        return splash;
    }

    public void setSplash(String splash) {
        this.splash = splash;
    }

    public String getIcons() {
        return icons;
    }

    public void setIcons(String icons) {
        this.icons = icons;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
