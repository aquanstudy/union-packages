package com.u8x.sdk;

import com.u8x.common.XLogger;
import com.u8x.dao.config.GlobalConfig;
import com.u8x.dao.entity.ChannelMaster;
import com.u8x.dao.entity.PluginMaster;
import com.u8x.dao.entity.PluginParamMeta;
import com.u8x.dao.entity.SDKConfigInfo;
import com.u8x.services.ChannelService;
import com.u8x.utils.FileUtils;
import com.u8x.utils.StringUtils;
import com.u8x.utils.U8XUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 本地已经接入的SDK管理
 * Created by ant on 2018/5/20.
 */
@Component
public class LocalSDKManager {

    private static final XLogger logger = XLogger.getLogger(LocalSDKManager.class);

    private Map<String, ChannelMaster> sdkMap;

    private Map<String, PluginMaster> pluginMap;

    @Autowired
    private GlobalConfig globalConfig;

    @PostConstruct
    public void init(){
        sdkMap = new ConcurrentHashMap<>();
        pluginMap = new ConcurrentHashMap<>();
    }

    public void replaceSDKMap(Map<String, ChannelMaster> newSDKMap){

        synchronized (sdkMap){
            sdkMap.clear();
            if(newSDKMap != null){
                sdkMap.putAll(newSDKMap);
            }
        }

    }

    public void replaceSDK(ChannelMaster master){
        if(master == null) return;
        sdkMap.put(master.getSdkName(), master);
    }

    public void removeSDK(String sdk){
        sdkMap.remove(sdk);
        logger.debug("remove a sdk folder :" + sdk);
    }

    public void replacePluginMap(Map<String, PluginMaster> newPluginMap){
        synchronized (pluginMap){
            pluginMap.clear();
            if(newPluginMap != null){
                pluginMap.putAll(newPluginMap);
            }
        }
    }

    public void replacePlugin(PluginMaster master){
        if(master == null) return;
        pluginMap.put(master.getSdkName(), master);
    }


    public void removePlugin(String sdk){
        pluginMap.remove(sdk);
        logger.debug("remove a plugin folder :" + sdk);
    }

    public List<ChannelMaster> getLocalSDKMasters(){
        return new ArrayList<>(sdkMap.values());
    }

    public List<PluginMaster> getLocalPlugins(){
        List<PluginMaster> plugins = new ArrayList<>(pluginMap.values());
        return plugins.stream().map(item -> new PluginMaster(item)).collect(Collectors.toList());
    }

    public ChannelMaster getMasterBySDK(String sdkName){
        if(StringUtils.isEmpty(sdkName)) return null;
        return sdkMap.get(sdkName);
    }

    public PluginMaster getPluginBySDK(String sdkName){
        if(StringUtils.isEmpty(sdkName)) return null;
        return pluginMap.get(sdkName);
    }

    //重新加载获取整个sdk配置目录下所有渠道配置
    public void reloadLocalSDKs(){

        try{
            logger.debug("begin to reload local sdk config");
            String fullPath = FileUtils.joinPath(globalConfig.getPackageBuildPath(), "config/sdk");
            File file = new File(fullPath);
            if(!file.exists() || !file.isDirectory()){
                replaceSDKMap(null);
                return;
            }

            Map<String, ChannelMaster> result = new HashMap<>();
            List<String> items = FileUtils.listSubDirectoryNames(file);
            if(items != null){
                for(String item : items){
                    ChannelMaster m = loadLocalSDKByName(item);
                    if(m != null){
                        logger.trace("load a sdk from local path:{}", item);
                        result.put(item, m);
                    }else{
                        logger.warn("{} sdk local folder parse failed. please check it.", item);
                    }
                }
            }

            replaceSDKMap(result);
            return;

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //重新加载sdk配置目录下的渠道配置
    public void reloadLocalSDKs(List<String> names){

        if(names == null || names.size() == 0) return;
        logger.debug("begin to reload local sdk config. size:"+names.size());

        try{

            String fullPath = FileUtils.joinPath(globalConfig.getPackageBuildPath(), "config/sdk");
            File file = new File(fullPath);
            if(!file.exists() || !file.isDirectory()){
                replaceSDKMap(null);
                return;
            }

            for(String item : names){
                ChannelMaster m = loadLocalSDKByName(item);
                if(m != null){
                    logger.debug("load a sdk from local path:{}", item);
                    replaceSDK(m);
                }else{
                    logger.warn("{} sdk local folder parse failed. please check it.", item);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //重新加载获取整个plugins配置目录下所有插件配置
    public void reloadLocalPlugins(){

        try{
            logger.debug("begin to reload local plugin config");
            String fullPath = FileUtils.joinPath(globalConfig.getPackageBuildPath(), "config/plugin");
            File file = new File(fullPath);
            if(!file.exists() || !file.isDirectory()){
                replacePluginMap(null);
                return;
            }

            Map<String, PluginMaster> result = new HashMap<>();
            List<String> items = FileUtils.listSubDirectoryNames(file);
            if(items != null){
                for(String item : items){
                    PluginMaster m = loadLocalPluginByName(item);
                    if(m != null){
                        logger.debug("load a plugin from local path:{}", item);
                        result.put(item, m);
                    }else{
                        logger.warn("{} plugin local folder parse failed. please check it.", item);
                    }
                }
            }

            replacePluginMap(result);
            return;

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //重新加载plugins配置目录下插件配置
    public void reloadLocalPlugins(List<String> names){
        if(names == null || names.size() == 0) return;
        logger.debug("begin to reload local plugin config.size:"+names.size());

        try{

            String fullPath = FileUtils.joinPath(globalConfig.getPackageBuildPath(), "config/plugin");
            File file = new File(fullPath);
            if(!file.exists() || !file.isDirectory()){
                replacePluginMap(null);
                return;
            }

            for(String item : names){
                PluginMaster m = loadLocalPluginByName(item);
                if(m != null){
                    logger.trace("load a plugin from local path:{}", item);
                    replacePlugin(m);
                }else{
                    logger.warn("{} plugin local folder parse failed. please check it.", item);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //加载单独一个插件配置目录信息
    public PluginMaster loadLocalPluginByName(String sdkName){

        try{

            String fullPath = FileUtils.joinPath(globalConfig.getPackageBuildPath(), "config/plugin", sdkName);

            File file = new File(fullPath);

            if(!file.exists() || !file.isDirectory()){
                removePlugin(sdkName);
                return null;
            }

            File configFile = new File(FileUtils.joinPath(fullPath, "config.xml"));
            if(!configFile.exists()){
                return null;
            }

            String configStr = FileUtils.readText(configFile);
            SDKConfigInfo sdkConfig = U8XUtils.parseXml(configStr, SDKConfigInfo.class);

            if(sdkConfig != null && sdkConfig.getVersion() != null){
                PluginMaster master = new PluginMaster();
                master.setSdkName(sdkName);
                master.setVersionName(sdkConfig.getVersion().getVersionName());
                master.setMasterName(sdkConfig.getVersion().getName());
                master.setDesc(sdkConfig.getVersion().getDesc());

                if(sdkConfig.getParams() != null){
                    logger.trace("load {} meta of plugin {}", sdkConfig.getParams().size(), sdkName);
                    for(SDKConfigInfo.SDKParamMeta meta : sdkConfig.getParams()){
                        if("0".equals(meta.getRequired())){
                            logger.trace("ignore a param meta {} of plugin {}", meta.getName(), sdkName);
                            continue;
                        }
                        PluginParamMeta pmeta = new PluginParamMeta();
                        pmeta.setParamKey(meta.getName());
                        pmeta.setShowName(meta.getShowName());
                        pmeta.setMetaDesc(meta.getDesc());
                        pmeta.setDefaultVal(meta.getValue());

                        boolean bWriteInClient = "1".equals(meta.getbWriteInClient());
                        boolean bWriteInManifest = "1".equals(meta.getbWriteInManifest());

                        if(bWriteInClient && bWriteInManifest){
                            pmeta.setPos(PluginParamMeta.POS_BOTH);
                        }else if(bWriteInClient){
                            pmeta.setPos(PluginParamMeta.POS_CONFIG);
                        }else if(bWriteInManifest){
                            pmeta.setPos(PluginParamMeta.POS_METADATA);
                        }else{
                            pmeta.setPos(PluginParamMeta.POS_NONE);
                        }

                        if(StringUtils.isEmpty(meta.getParamStyle())){
                            pmeta.setParamStyle(PluginParamMeta.STYLE_TEXT);
                        }else{
                            if("file".equalsIgnoreCase(meta.getParamStyle())){
                                pmeta.setParamStyle(PluginParamMeta.STYLE_FILE);
                            }else if("list".equalsIgnoreCase(meta.getParamStyle())){
                                pmeta.setParamStyle(PluginParamMeta.STYLE_LIST);
                            }else{
                                pmeta.setParamStyle(PluginParamMeta.STYLE_TEXT);
                            }
                        }

                        pmeta.setStyleExtra(meta.getStyleExtra());

                        master.addMeta(pmeta);

                    }
                }

                return master;
            }



        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    //加载单独一个sdk配置目录信息
    public ChannelMaster loadLocalSDKByName(String sdkName){
        try{

            String fullPath = FileUtils.joinPath(globalConfig.getPackageBuildPath(), "config/sdk", sdkName);

            File file = new File(fullPath);

            if(!file.exists() || !file.isDirectory()){
                removeSDK(sdkName);
                return null;
            }

            File configFile = new File(FileUtils.joinPath(fullPath, "config.xml"));
            if(!configFile.exists()){
                return null;
            }

            String configStr = FileUtils.readText(configFile);
            SDKConfigInfo sdkConfig = U8XUtils.parseXml(configStr, SDKConfigInfo.class);

            if(sdkConfig != null && sdkConfig.getVersion() != null){
                ChannelMaster master = new ChannelMaster();
                master.setSdkName(sdkName);
                master.setVersionName(sdkConfig.getVersion().getVersionName());
                master.setMasterSdkName(StringUtils.isEmpty(sdkConfig.getVersion().getServerChannelMaster()) ? sdkName : sdkConfig.getVersion().getServerChannelMaster());
                master.setMasterName(sdkConfig.getVersion().getName());
                master.setDesc(sdkConfig.getVersion().getDesc());

                //检查闪屏文件
                String splash = "";
                String splashPath = FileUtils.joinPath(fullPath, "splash");
                String[] splashFolders = {"11","12","21","22"};
                for(String splashFolder : splashFolders){
                    if(FileUtils.directoryExists(FileUtils.joinPath(splashPath, splashFolder))){
                        splash += "|" + splashFolder;
                    }
                }
                master.setSplash(splash);

                //检查角标
                String icons = "";
                String iconPath = FileUtils.joinPath(fullPath, "icon_marks");
                String[] iconNames = {"left-top.png", "left-bottom.png", "right-top.png", "right-bottom.png"};
                String[] iconShortNames = {"lt", "lb", "rt", "rb"};
                for(int i=0;i<iconNames.length;i++){
                    String iconName = iconNames[i];
                    if(FileUtils.fileExists(FileUtils.joinPath(iconPath, iconName))){
                        icons += "|" + iconShortNames[i];
                    }
                }
                master.setIcons(icons);

                return master;
            }



        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }


}
