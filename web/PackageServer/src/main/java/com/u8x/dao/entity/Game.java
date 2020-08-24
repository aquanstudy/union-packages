package com.u8x.dao.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * 游戏信息
 */
public class Game implements java.io.Serializable {

	private static final long serialVersionUID = 2426787766135204683L;

	private Integer id;		//游戏ID，目前和appID一致

	private Integer cpID;		//CP ID
	private Integer appID; 		//游戏ID和U8Server后台一致
	private String name;		//游戏名称
	private String appKey;		//游戏appKey
	private String appSecret;	//游戏appSecret
	private String orientation = "landscape";	//横竖屏(横屏：landscape;竖屏:portrait)
	private String cpuSupport;	//cpuSupport多个用|分割，比如(armeabi|armeabi-v7a)

	private String apkPath;			//母包路径
	private String iconPath;		//图标路径
	private Integer keystoreID = 0;	//当前使用的keystore

	private Integer minSdkVersion;			//AndroidManifest.xml中minSdkVersion配置
	private Integer targetSdkVersion;		//AndroidManifest.xml中targetSdkVersion配置
	private Integer maxSdkVersion;			//AndroidManifest.xml中maxSdkVersion配置

	private Integer versionCode;			//最终apk里面版本号
	private String versionName;				//最终apk里面版本名称
	private String outputApkName = "{channelName}.apk";//最终apk包名格式：{bundleID}:包名,{versionName}:版本名称,{versionCode}:版本号,{time}:时间戳(yyyyMMddmmss),{channelID}:渠道号,{channelName}:渠道名,{appName}:游戏名,{appID}:appID
	private Integer enableLog = 1;		//1:开启log;0:不开启

	private String doNotCompress;		//apktool 压缩配置

	private Integer singleGame = 0;			//0:网游或者联网游戏;1:纯单机

	private String serverBaseUrl;		//u8server根地址

	private String createTime;			//创建时间

	public Integer getCpID() {
		return cpID;
	}

	public void setCpID(Integer cpID) {
		this.cpID = cpID;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getAppID() {
		return appID;
	}

	public void setAppID(Integer appID) {
		this.appID = appID;
	}

	public String getOrientation() {
		return orientation;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public String getCpuSupport() {
		return cpuSupport;
	}

	public void setCpuSupport(String cpuSupport) {
		this.cpuSupport = cpuSupport;
	}

	public Integer getMinSdkVersion() {
		return minSdkVersion;
	}

	public void setMinSdkVersion(Integer minSdkVersion) {
		this.minSdkVersion = minSdkVersion;
	}

	public Integer getTargetSdkVersion() {
		return targetSdkVersion;
	}

	public void setTargetSdkVersion(Integer targetSdkVersion) {
		this.targetSdkVersion = targetSdkVersion;
	}

	public Integer getMaxSdkVersion() {
		return maxSdkVersion;
	}

	public void setMaxSdkVersion(Integer maxSdkVersion) {
		this.maxSdkVersion = maxSdkVersion;
	}

	public Integer getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(Integer versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getOutputApkName() {
		return outputApkName;
	}

	public void setOutputApkName(String outputApkName) {
		this.outputApkName = outputApkName;
	}

	public Integer getEnableLog() {
		return enableLog;
	}

	public void setEnableLog(Integer enableLog) {
		this.enableLog = enableLog;
	}

	public String getServerBaseUrl() {
		return serverBaseUrl;
	}

	public void setServerBaseUrl(String serverBaseUrl) {
		this.serverBaseUrl = serverBaseUrl;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getAppSecret() {
		return appSecret;
	}

	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}

	public Integer getKeystoreID() {
		return keystoreID;
	}

	public void setKeystoreID(Integer keystoreID) {
		this.keystoreID = keystoreID;
	}

	public String getApkPath() {
		return apkPath;
	}

	public void setApkPath(String apkPath) {
		this.apkPath = apkPath;
	}

	public String getIconPath() {
		return iconPath;
	}

	public void setIconPath(String iconPath) {
		this.iconPath = iconPath;
	}

	public Integer getSingleGame() {
		return singleGame;
	}

	public void setSingleGame(Integer singleGame) {
		this.singleGame = singleGame;
	}

	public String getDoNotCompress() {
		return doNotCompress;
	}

	public void setDoNotCompress(String doNotCompress) {
		this.doNotCompress = doNotCompress;
	}
}
