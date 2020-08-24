package com.u8x.sdk;

import com.u8x.common.XLogger;
import com.u8x.dao.config.GlobalConfig;
import com.u8x.utils.FilePathMonitor;
import com.u8x.utils.FilePathWatcher;
import com.u8x.utils.FileUtils;
import com.u8x.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 本地SDK配置目录文件变化监听和重新加载配置文件
 * Created by ant on 2018/5/20.
 */
@Component
@Order(value = 100)
public class LocalSDKReloader implements ApplicationRunner {

    private static final String SDK_REL_PATH = "/config/sdk/";
    private static final String PLUGIN_REL_PATH = "/config/plugin/";

    private static final XLogger logger = XLogger.getLogger(LocalSDKReloader.class);

    private static final int CHECK_INTERVAL_MILLIS = 60000;  //一分钟执行一次重载检查

    private FilePathMonitor filePathMonitor;

    @Autowired
    private GlobalConfig globalConfig;

    @Autowired
    private LocalSDKManager sdkManager;

//    private volatile boolean sdkDirty = false;
//    private volatile boolean pluginDirty = false;

    private volatile boolean reloadTaskRunning = false;

    private Thread reloadTask;

    private List<String> changedSDKList;
    private List<String> changedPluginList;

    @PostConstruct
    public void init(){

        changedSDKList = Collections.synchronizedList(new ArrayList<String>());
        changedPluginList = Collections.synchronizedList(new ArrayList<String>());

        filePathMonitor = new FilePathMonitor();

        reloadTask = new Thread(new Runnable() {
            @Override
            public void run() {
                reloadTaskRunning = true;
                while(reloadTaskRunning){
                    tryReloadSDKs();
                    trReloadPlugins();

                    try {
                        Thread.sleep(CHECK_INTERVAL_MILLIS);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    //sdk 目录下文件有变化， 在这里我们重新加载SDK配置信息
     private void onSDKFileChanged(int type, String path){
        //标记需要重新加载本地配置文件，因为更新SDK时，可能会频繁触发该回调，所以收到该文件变化回调先不立即更新
        //另起一个更新任务，每分钟检查一次，如果有需要更新，则重新读取本地SDK配置文件
        //logger.debug("sdk file changed. type:"+type+"; path:"+path);

        if(StringUtils.isEmpty(path) || (path.contains("splash") && path.contains("temp_caches"))){
            return;
        }
        if(path.contains(SDK_REL_PATH)){
            //sdkDirty = true
            String sdkName = parseSDKName(path, SDK_REL_PATH);
            if(!StringUtils.isEmpty(sdkName) && !changedSDKList.contains(sdkName)){
                changedSDKList.add(sdkName);
            }
        }else if(path.contains(PLUGIN_REL_PATH)){
            //pluginDirty = true;
            String sdkName = parseSDKName(path, PLUGIN_REL_PATH);
            if(!StringUtils.isEmpty(sdkName) && changedPluginList.contains(sdkName)){
                changedPluginList.add(sdkName);
            }
        }
    }

    private void tryReloadSDKs(){
        synchronized (changedSDKList){
            if(changedSDKList.size() > 0){
                List<String> sdks = new ArrayList<>(changedSDKList);
                sdkManager.reloadLocalSDKs(sdks);  //reload changed sdk
                changedSDKList.clear();
            }
        }
    }

    private void trReloadPlugins(){
        synchronized (changedPluginList){
            if(changedPluginList.size() > 0){
                List<String> sdks = new ArrayList<>(changedPluginList);
                sdkManager.reloadLocalPlugins(sdks);//reload changed plugin
                changedPluginList.clear();
            }
        }
    }

    public void start(){

        if(filePathMonitor != null){
            String sdkPath = FileUtils.joinPath(globalConfig.getPackageBuildPath(), "config/sdk");
            String pluginPath = FileUtils.joinPath(globalConfig.getPackageBuildPath(), "config/plugin");
            try{
                filePathMonitor.startMonitor(new FilePathMonitor.FileChangeListener() {
                    @Override
                    public void onFileChanged(FilePathMonitor.ChangedFile changedFile) {
                        onSDKFileChanged(changedFile.getType(), changedFile.getFile());
                    }
                }, sdkPath, pluginPath);
            }catch (Exception e){
                logger.error("start file monitor failed.", e);
                e.printStackTrace();
            }

        }

        if(reloadTask != null){
            reloadTask.start();
        }
    }

    public void stop(){

        reloadTaskRunning = false;

        if(filePathMonitor != null){
            filePathMonitor.stopMonitor();
        }

    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        sdkManager.reloadLocalSDKs();       //启动的时候，加载一次本地SDK
        sdkManager.reloadLocalPlugins();   //启动的时候， 加载一次本地插件
        start();

    }

    @PreDestroy
    public void onDestroy(){
        stop();
    }

    public String parseSDKName(String path, String relPath){
        if(StringUtils.isEmpty(path)) return null;
        int index = path.lastIndexOf(relPath);
        if(index <= 0) return null;
        path = path.substring(index+relPath.length());
        index = path.indexOf("/");

        if(index <= -1){
            return path;
        }
        String sdkName = path.substring(0, index);
        //logger.debug("the sdk name is :"+sdkName);
        return sdkName;
    }
}
