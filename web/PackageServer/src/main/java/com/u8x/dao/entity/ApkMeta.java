package com.u8x.dao.entity;

import java.io.Serializable;

/**
 * apk母包信息
 * Created by ant on 2018/6/16.
 */
public class ApkMeta implements Serializable{

    private static final long serialVersionUID = -3659065292855213556L;

    private Integer id;

    private Integer gameID;     //对应Game表中的id字段

    private String name;        //应用名称

    private String bundleID;    //应用包名

    private String versionCode; //版本号

    private String versionName; //版本名称

    private String uploadTime;  //上传时间

    private String apkPath;     //apk所在路径

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getApkPath() {
        return apkPath;
    }

    public void setApkPath(String apkPath) {
        this.apkPath = apkPath;
    }

    public String getBundleID() {
        return bundleID;
    }

    public void setBundleID(String bundleID) {
        this.bundleID = bundleID;
    }

    public Integer getGameID() {
        return gameID;
    }

    public void setGameID(Integer gameID) {
        this.gameID = gameID;
    }
}
