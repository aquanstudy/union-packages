package com.u8x.dao.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * 单次打包信息
 * Created by ant on 2018/3/15.
 */
public class PackLog implements Serializable {
    private static final long serialVersionUID = -6050653164879930071L;

    public static int STATE_FAILED = -1;
    public static int STATE_WAIT = 0;
    public static int STATE_PACKING = 1;
    public static int STATE_SUCCESS = 2;

    public static int TEST_STATE_FAILED = -1;   // 测试不通过
    public static int TEST_STATE_WAIT = 0;      // 等待测试
    public static int TEST_STATE_COMMIT = 1;    // 已提交测试
    public static int TEST_STATE_SUCCESS = 2;   // 测试通过

    private Integer id;         //唯一ID

    private String name;        //打包标识
    private Integer channelID;  //渠道号，和u8server后台渠道管理中渠道号一致
    private Integer channelLocalID; //channel表的id字段
    private Integer gameID;     //游戏ID

    private String channelName; //渠道名称

    private String fileName;    //文件名称

    private Integer state;      //打包状态(-1：打包失败;0:等待打包;1:正在打包;2:打包完成)

    private Integer testState;  //测试状态(-1:测试不通过；0:等待测试;1:已提交测试;2:测试通过)

    private String testFeed;    //测试反馈

    private  String channelParams;  //当前打包使用的参数配置

    private String createDate;    //时间


    private int progress;           //进度值， 不存数据库

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getChannelID() {
        return channelID;
    }

    public void setChannelID(Integer channelID) {
        this.channelID = channelID;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getTestState() {
        return testState;
    }

    public void setTestState(Integer testState) {
        this.testState = testState;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getChannelParams() {
        return channelParams;
    }

    public void setChannelParams(String channelParams) {
        this.channelParams = channelParams;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTestFeed() {
        return testFeed;
    }

    public void setTestFeed(String testFeed) {
        this.testFeed = testFeed;
    }

    public Integer getGameID() {
        return gameID;
    }

    public void setGameID(Integer gameID) {
        this.gameID = gameID;
    }

    public Integer getChannelLocalID() {
        return channelLocalID;
    }

    public void setChannelLocalID(Integer channelLocalID) {
        this.channelLocalID = channelLocalID;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
