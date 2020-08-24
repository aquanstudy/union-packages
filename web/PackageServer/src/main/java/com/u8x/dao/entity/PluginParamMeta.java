package com.u8x.dao.entity;

/**
 * Created by ant on 2018/6/15.
 */
public class PluginParamMeta {

    public static final int POS_NONE = 0;
    public static final int POS_CONFIG = 1;
    public static final int POS_METADATA = 2;
    public static final int POS_BOTH = 3;

    public static final int STYLE_TEXT = 0;
    public static final int STYLE_LIST = 1;
    public static final int STYLE_FILE = 2;


    private String paramKey;        //参数唯一Key

    private String showName;        //参数显示名称

    private String metaDesc;        //参数配置说明

    private String defaultVal;      //当前值或者默认值

    private Integer pos;            //参数位置， 0：都不写入；1：写入assets下面配置文件；2：写入meta-data；3：都写入

    private Integer paramStyle = 0; //参数展示类别， 0：文本;1:列表(styleExtra中，用|分割多个固定值);2:文件(styleExtra中保存文件在打包工具中的相对路径)

    private String styleExtra;      //不同展示类别，如果需要设置附加信息，可以在这里设置


    public String getParamKey() {
        return paramKey;
    }

    public void setParamKey(String paramKey) {
        this.paramKey = paramKey;
    }

    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    public String getMetaDesc() {
        return metaDesc;
    }

    public void setMetaDesc(String metaDesc) {
        this.metaDesc = metaDesc;
    }

    public String getDefaultVal() {
        return defaultVal;
    }

    public void setDefaultVal(String defaultVal) {
        this.defaultVal = defaultVal;
    }

    public Integer getPos() {
        return pos;
    }

    public void setPos(Integer pos) {
        this.pos = pos;
    }

    public Integer getParamStyle() {
        return paramStyle;
    }

    public void setParamStyle(Integer paramStyle) {
        this.paramStyle = paramStyle;
    }

    public String getStyleExtra() {
        return styleExtra;
    }

    public void setStyleExtra(String styleExtra) {
        this.styleExtra = styleExtra;
    }
}
