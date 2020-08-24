package com.u8x.dao.config;

import com.u8x.common.XLogger;
import com.u8x.dao.entity.Channel;
import com.u8x.dao.entity.ChannelMaster;
import com.u8x.services.ChannelProductService;
import com.u8x.utils.FileUtils;
import com.u8x.utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.io.File;
import java.util.List;

/**
 * Created by ant on 2018/3/23.
 */
@Configuration
@Lazy
public class GlobalConfig {

    private static final XLogger logger = XLogger.getLogger(GlobalConfig.class);

    private static GlobalConfig _instance;

    @Value("${u8x.package.toolPath}")
    public String packageBuildPath;         //打包工具路径

    private String abPackageBuildPath = null;

    @Value("${u8x.package.filePath:}")
    public String fileTempPath;             //打包配置存储和打包过程临时目录

    private String abFileTempPath = null;

    @Value("${u8x.server.url}")
    public String u8serverUrl;              //u8server 根地址

    @Value("${u8x.server.apiID}")
    public String apiID;

    @Value("${u8x.server.apiKey}")
    public String apiKey;

    @Value("${datasource.primary.url}")
    public String databaseUrl;

    @Value("${datasource.primary.username}")
    public String databaseUsername;

    @Value("${datasource.primary.password}")
    public String databasePassword;

    private String dbIP = "";
    private String dbName = "";
    private int dbPort = 0;

    private void parseDBInfo(){

        try{
            String[] splits = databaseUrl.split(":");   //jdbc:mysql://121.42.144.254:3306/pdb_private?...
            dbIP = splits[2].substring(2).trim();       // splits[2]= //121.42.144.254

            String[] temp = splits[3].split("/");
            dbPort = Integer.valueOf(temp[0].trim());

            String[] temp2 = temp[1].split("\\?");
            dbName = temp2[0];

        }catch (Exception e){
            logger.error("parseDBInfo failed. " + e.getMessage());
            e.printStackTrace();
        }

    }

    public String getDBIP(){

        if(StringUtils.isNotEmpty(dbIP)){
            return dbIP;
        }

        parseDBInfo();

        return dbIP;

    }

    public String getDBName(){

        if(StringUtils.isNotEmpty(dbName)){
            return dbName;
        }

        parseDBInfo();

        return dbName;
    }

    public int getDBPort(){
        if(dbPort > 0){
            return dbPort;
        }
        parseDBInfo();
        return dbPort;
    }

    public String getDBUsername(){
        return databaseUsername;
    }

    public String getDBPassword(){
        return databasePassword;
    }

    public String u8serverBaseUrl(){
        String url = u8serverUrl;
        if(!url.endsWith("/")){
            url += "/";
        }
        return url;
    }

    //获取打包配置文件和打包临时文件存储目录
    public String getFileTempPath(){

        if(StringUtils.isNotEmpty(abFileTempPath)){
            return abFileTempPath;
        }

        String path = packageBuildPath;

        if(StringUtils.isNotEmpty(fileTempPath)){
            path = fileTempPath;
        }

        abFileTempPath = new File(path).getAbsolutePath();    //返回绝对路径

        logger.debug("getFileTempPath->path:"+abFileTempPath);

        return abFileTempPath;
    }

    public String getPackageBuildPath() {

        if(StringUtils.isNotEmpty(abPackageBuildPath)){
            return abPackageBuildPath;
        }

        abPackageBuildPath = new File(packageBuildPath).getAbsolutePath();

        logger.debug("getPackageBuildPath->path:"+abPackageBuildPath);

        return abPackageBuildPath;
    }


    public GlobalConfig(){
        _instance = this;
    }

    public static GlobalConfig getInstance(){

        return _instance;
    }

    public String gameUrl(){
        return u8serverBaseUrl() + "api/games";
    }


    public String channelmasterUrl(){

        return u8serverBaseUrl() + "api/channelmasters";
    }

    public String paramMetaUrl(){

        return u8serverBaseUrl() + "api/metas";
    }

    public String paramMetaSaveUrl(){

        return u8serverBaseUrl() + "api/saveMeta";
    }

    public String paramMetaDeleteUrl(){

        return u8serverBaseUrl() + "api/deleteMeta";
    }

    public String channelconfigUrl(){
        return u8serverBaseUrl() + "api/channelconfig";
    }

    public String channelFullConfigUrl(){
        return u8serverBaseUrl() + "api/channelfullconfig";
    }

    public String channelproductUrl(){
        return u8serverBaseUrl() + "api/channelproducts";
    }

    public String multichannelconfigUrl(){
        return u8serverBaseUrl() + "api/multichannelconfig";
    }

    public String channelSaveUrl(){
        return u8serverBaseUrl() + "api/saveChannelConfig";
    }

    public String channelProductSaveUrl(){

        return u8serverBaseUrl() + "api/saveChannelProduct";
    }

    public String channelProductDeleteUrl(){
        return u8serverBaseUrl() + "api/deleteChannelProduct";
    }


//    public String getSDKVersion(String sdkName){
//
//        String fullPath = FileUtils.joinPath(packageBuildPath, "config/sdk", sdkName);
//
//        File file = new File(fullPath);
//
//        if(!file.exists() || !file.isDirectory()){
//            return "";
//        }
//
//        File configFile = new File(FileUtils.joinPath(fullPath, "config.xml"));
//        if(!configFile.exists()){
//            return "";
//        }
//
//        File manifestFile = new File(FileUtils.joinPath(fullPath, "SDKManifest.xml"));
//        if(!manifestFile.exists()){
//            return "";
//        }
//
//        List<String> lines = FileUtils.readLines(configFile);
//        if(lines != null){
//            for(String l : lines){
//                if(l.trim().startsWith("<versionName>")){
//                    return l.replace("<versionName>", "").replace("</versionName>","").trim();
//                }
//            }
//        }
//
//        return "";
//
//    }

//    public ChannelMaster getSDKInfo(String sdkName){
//
//        String fullPath = FileUtils.joinPath(packageBuildPath, "config/sdk", sdkName);
//
//        File file = new File(fullPath);
//
//        if(!file.exists() || !file.isDirectory()){
//            return null;
//        }
//
//        File configFile = new File(FileUtils.joinPath(fullPath, "config.xml"));
//        if(!configFile.exists()){
//            return null;
//        }
//
//        File manifestFile = new File(FileUtils.joinPath(fullPath, "SDKManifest.xml"));
//        if(!manifestFile.exists()){
//            return null;
//        }
//
//        List<String> lines = FileUtils.readLines(configFile);
//        if(lines != null){
//            ChannelMaster master = new ChannelMaster();
//            master.setSdkName(sdkName);
//            for(String l : lines){
//                if(l.trim().startsWith("<versionName>")){
//                    String versionName = l.replace("<versionName>", "").replace("</versionName>","").trim();
//                    master.setVersionName(versionName);
//                }else if(l.trim().startsWith("<name>")){
//                    String name = l.replace("<name>", "").replace("</name>","").trim();
//                    master.setMasterName(name);
//                }else if(l.trim().startsWith("<serverChannelMaster>")){
//                    String serverChannelMaster = l.replace("<serverChannelMaster>", "").replace("</serverChannelMaster>","").trim();
//                    master.setMasterSdkName(serverChannelMaster);
//                }
//            }
//            return master;
//        }
//
//        return null;
//
//    }

}
