package com.u8x.dao.entity;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Created by ant on 2018/5/22.
 */

@XmlRootElement(name = "config")
public class SDKConfigInfo{


    private List<SDKParamMeta> params;

    private SDKVersionInfo version;

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class SDKParamMeta{

        @XmlAttribute(name = "name")
        private String name;                //参数名称

        @XmlAttribute(name = "value")
        private String value;               //参数默认值

        @XmlAttribute(name = "required")
        private String required;            //required为0，不需要加载，打包直接读取值。

        @XmlAttribute(name = "showName")
        private String showName;            //参数显示名称（无用）

        @XmlAttribute(name = "desc")
        private String desc;                //参数描述(无用)

        @XmlAttribute(name = "bWriteInManifest")
        private String bWriteInManifest;    //1：写入AndroidManifest.xml的meta-data中

        @XmlAttribute(name = "bWriteInClient")
        private String bWriteInClient;      //1：写入到assets下面developer_config.properties中

        @XmlAttribute(name = "paramStyle")
        private String paramStyle;          //参数样式

        @XmlAttribute(name = "styleExtra")
        private String styleExtra;          //参数样式对应的格式


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getRequired() {
            return required;
        }

        public void setRequired(String required) {
            this.required = required;
        }

        public String getShowName() {
            return showName;
        }

        public void setShowName(String showName) {
            this.showName = showName;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getbWriteInManifest() {
            return bWriteInManifest;
        }

        public void setbWriteInManifest(String bWriteInManifest) {
            this.bWriteInManifest = bWriteInManifest;
        }

        public String getbWriteInClient() {
            return bWriteInClient;
        }

        public void setbWriteInClient(String bWriteInClient) {
            this.bWriteInClient = bWriteInClient;
        }

        public String getParamStyle() {
            return paramStyle;
        }

        public void setParamStyle(String paramStyle) {
            this.paramStyle = paramStyle;
        }

        public String getStyleExtra() {
            return styleExtra;
        }

        public void setStyleExtra(String styleExtra) {
            this.styleExtra = styleExtra;
        }
    }

    public static class SDKVersionInfo {

        private String name;                //SDK名称(本地sdk目录名称)

        private String serverChannelMaster;     //该SDK对应server端渠道商里面的名称

        private String versionCode;             //当前SDK的版本号

        private String versionName;             //当前SDK的版本名称

        private String desc;                    //当前SDK的配置说明

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getServerChannelMaster() {
            return serverChannelMaster;
        }

        public void setServerChannelMaster(String serverChannelMaster) {
            this.serverChannelMaster = serverChannelMaster;
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

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }

    public SDKVersionInfo getVersion() {
        return version;
    }

    public void setVersion(SDKVersionInfo version) {
        this.version = version;
    }

    @XmlElementWrapper(name = "params")
    @XmlElement(name = "param")
    public List<SDKParamMeta> getParams() {
        return params;
    }

    public void setParams(List<SDKParamMeta> params) {
        this.params = params;
    }
}


