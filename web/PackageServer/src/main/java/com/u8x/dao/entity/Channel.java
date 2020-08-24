package com.u8x.dao.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 渠道信息
 * Created by ant on 2017/10/11.
 */
public class Channel implements Serializable {

    private static final long serialVersionUID = 3881554586358377726L;


    private Integer id;     //自增ID
    private Integer channelID;  //渠道号
    private Integer gameID;     //游戏ID
    private String name;    //渠道配置名称，打包标识
    private String channelName; //渠道名称
    private String sdk;     //使用的SDK,本地目录名称
    private String masterSDKName;   //对应u8server端sdkName
    private String bundleID;    //包名
    private String splash;    //闪屏(0：无闪屏;1：渠道默认闪屏;2:自定义闪屏图片)
    private Integer replaceUnitySplash; //1:替换unity的splash为闪屏；0：否
    private Integer iconType;       //图标处理类型(1:打包工具自动处理;2:自定义图标)
    private String icon; //ICON角标(自动处理； rb:右下角;rt:右上角;lb:左下角;lt:左上角)
    private String gameName; //覆盖游戏名称？
    private Integer signApk;    //1:签名apk；0：不签名apk
    private String signVersion; //apk签名版本(V1或者V2)
    private Integer keystoreID; //签名ID
    private String createTime;    //创建时间

    private Integer isLocal = 0;        //是否是本地配置(0:不是，1：是本地配置)， 如果是本地配置， 渠道参数直接配置在本地。创建时，从已有渠道配置中拷贝创建，然后默认配置和该渠道一致， 可以修改渠道参数，修改的参数保留在本地。
    private String localConfig;     //本地渠道参数配置

    private Integer isConfiged = 0;     //参数是否配置 0:未配置；1：已经配置;


    private String versionName;         //版本号，不存入数据库，每次返回数据的时候， 从master中获取
    private boolean hasCustomIcon;      //是否有自定义图标,不存入数据库， 每次返回数据的时候判断
    private boolean hasCustomSplash;    //是否有自定义闪屏,不存入数据库，每次妇女会数据的时候判断

    private Integer minSdkVersion;      //minSdkVersion， 覆盖游戏配置中的配置
    private Integer targetSdkVersion;   //targetSdkVersion，覆盖游戏配置中的配置
    private Integer maxSdkVersion;      //maxSdkVersion， 覆盖游戏配置中的配置
    private String sdkLogicVersionCode; //SDK逻辑版本号， 多个版本同时存在的时候，根据这个来让服务器使用指定的版本


    private Integer autoPermission;     //是否开启自动权限， 1：开启；0：关闭
    private Integer directPermission;   //是否直接在游戏当前Activity中直接申请，1：直接申请；0：在独立Activity中申请
    private String excludePermissionGroups; //不需要自动权限处理的权限组
    private Integer autoProtocol;       //是否开启自动用户隐私展示， 1：开启；0：关闭
    private String protocolUrl;         //用户隐私协议地址

    private String serverBaseUrl;		//u8server根地址

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

    public Integer getChannelID() {
        return channelID;
    }

    public void setChannelID(Integer channelID) {
        this.channelID = channelID;
    }

    public Integer getGameID() {
        return gameID;
    }

    public void setGameID(Integer gameID) {
        this.gameID = gameID;
    }

    public String getSdk() {
        return sdk;
    }

    public void setSdk(String sdk) {
        this.sdk = sdk;
    }

    public String getBundleID() {
        return bundleID;
    }

    public void setBundleID(String bundleID) {
        this.bundleID = bundleID;
    }

    public String getSplash() {
        return splash;
    }

    public void setSplash(String splash) {
        this.splash = splash;
    }

    public Integer getReplaceUnitySplash() {
        return replaceUnitySplash;
    }

    public void setReplaceUnitySplash(Integer replaceUnitySplash) {
        this.replaceUnitySplash = replaceUnitySplash;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Integer getSignApk() {
        return signApk;
    }

    public void setSignApk(Integer signApk) {
        this.signApk = signApk;
    }

    public String getSignVersion() {
        return signVersion;
    }

    public void setSignVersion(String signVersion) {
        this.signVersion = signVersion;
    }

    public String getMasterSDKName() {
        return masterSDKName;
    }

    public void setMasterSDKName(String masterSDKName) {
        this.masterSDKName = masterSDKName;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public Integer getKeystoreID() {
        return keystoreID;
    }

    public void setKeystoreID(Integer keystoreID) {
        this.keystoreID = keystoreID;
    }

    public Integer getIsLocal() {
        return isLocal;
    }

    public void setIsLocal(Integer isLocal) {
        this.isLocal = isLocal;
    }

    public String getLocalConfig() {
        return localConfig;
    }

    public void setLocalConfig(String localConfig) {
        this.localConfig = localConfig;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }


    public Integer getIconType() {
        return iconType;
    }

    public void setIconType(Integer iconType) {
        this.iconType = iconType;
    }

    public boolean isHasCustomIcon() {
        return hasCustomIcon;
    }

    public void setHasCustomIcon(boolean hasCustomIcon) {
        this.hasCustomIcon = hasCustomIcon;
    }

    public boolean isHasCustomSplash() {
        return hasCustomSplash;
    }

    public void setHasCustomSplash(boolean hasCustomSplash) {
        this.hasCustomSplash = hasCustomSplash;
    }

    public Integer getIsConfiged() {
        return isConfiged;
    }

    public void setIsConfiged(Integer isConfiged) {
        this.isConfiged = isConfiged;
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

    public Integer getAutoPermission() {
        return autoPermission;
    }

    public void setAutoPermission(Integer autoPermission) {
        this.autoPermission = autoPermission;
    }

    public Integer getDirectPermission() {
        return directPermission;
    }

    public void setDirectPermission(Integer directPermission) {
        this.directPermission = directPermission;
    }

    public String getExcludePermissionGroups() {
        return excludePermissionGroups;
    }

    public void setExcludePermissionGroups(String excludePermissionGroups) {
        this.excludePermissionGroups = excludePermissionGroups;
    }

    public Integer getAutoProtocol() {
        return autoProtocol;
    }

    public void setAutoProtocol(Integer autoProtocol) {
        this.autoProtocol = autoProtocol;
    }

    public String getProtocolUrl() {
        return protocolUrl;
    }

    public void setProtocolUrl(String protocolUrl) {
        this.protocolUrl = protocolUrl;
    }

    public String getServerBaseUrl() {
        return serverBaseUrl;
    }

    public void setServerBaseUrl(String serverBaseUrl) {
        this.serverBaseUrl = serverBaseUrl;
    }

    public String getSdkLogicVersionCode() {
        return sdkLogicVersionCode;
    }

    public void setSdkLogicVersionCode(String sdkLogicVersionCode) {
        this.sdkLogicVersionCode = sdkLogicVersionCode;
    }
}
