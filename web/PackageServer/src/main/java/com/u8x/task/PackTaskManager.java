package com.u8x.task;

import com.u8x.common.XLogger;
import com.u8x.dao.config.GlobalConfig;
import com.u8x.dao.entity.Channel;
import com.u8x.dao.entity.Game;
import com.u8x.dao.entity.PackLog;
import com.u8x.services.ChannelService;
import com.u8x.utils.DateUtil;
import com.u8x.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * 应用宝SDK相关支付逻辑
 * Created by ant on 2015/10/14.
 */
@Component("packTaskManager")
public class PackTaskManager {

    private static final XLogger logger = XLogger.getLogger(PackTaskManager.class);

    private BlockingQueue<PackTask> tasks;
    private PackTask currTask;

    private Object lock = new Object();

    @Autowired
    private GlobalConfig globalConfig;


    private ExecutorService executor;

    private volatile boolean isRunning = false;

    @Autowired
    private ChannelService channelService;

    public PackTaskManager(){
        logger.debug("create a new pack task manager...");
        this.tasks = new LinkedBlockingDeque<PackTask>();
        executor = Executors.newFixedThreadPool(1);
    }

//
//    public static PackTaskManager getInstance(){
//        if(instance == null){
//            instance = new PackTaskManager();
//        }
//        return instance;
//    }

    //获取当前执行或者等待的所有打包任务
    public List<PackLog> getAllPackLogsByGameID(int gameID){

        List<PackLog> result = new ArrayList<>();

        synchronized (lock){

            if(currTask != null && currTask.getPackLog().getGameID() == gameID){
                result.add(currTask.getPackLog());
            }
        }

        for(PackTask t : this.tasks){
            if(t.getPackLog().getGameID() == gameID){
                result.add(t.getPackLog());
            }

        }

        return result;


    }

    public boolean isPacking(int packLogID){

        synchronized (lock){

            if(currTask != null && currTask.getPackLog().getId() == packLogID) return true;

            return false;
        }

    }

    public PackTask getTaskByPackID(int packLogID){

        Iterator<PackTask> itor = tasks.iterator();
        while(itor.hasNext()){
            PackTask task = itor.next();
            if(task.getPackLog().getId() == packLogID) return task;
        }

        return null;


    }

    //获取当前执行或者等待的所有打包任务
    public List<PackLog> refreshPackLogsByGameID(int gameID){

        List<PackLog> result = new ArrayList<>();

        synchronized (lock){

            if(currTask != null){
                PackLog currLog = currTask.getPackLog();
                if(currLog.getGameID() == gameID){
                    currLog.setProgress(getCurrProgress(currLog.getGameID(), currLog.getName(), currLog.getChannelLocalID()));
                    logger.debug("curr progress:"+currLog.getProgress());
                    result.add(currLog);
                }

            }
        }

        for(PackTask t : this.tasks){
            PackLog log = t.getPackLog();
            if(log.getGameID() == gameID){
                log.setProgress(0);
                result.add(t.getPackLog());
            }

        }

        return result;


    }

    //获取当前打包任务进度
    public int getCurrProgress(int gameID, String name, int channelLocalID){

        String path = "/workspace/game"+gameID+"/"+name+channelLocalID+"/progress_temp/progress.txt";
        path = FileUtils.joinPath(globalConfig.getFileTempPath(), path);

        if(!FileUtils.fileExists(path)){
            logger.debug("progress file is not exists.{}", path);
            return 0;
        }

        List<String> lines = FileUtils.readLines(new File(path));

        if(lines!= null && lines.size() >= 1){
            try{
                int progress = Integer.valueOf(lines.get(0));
                return progress;
            }catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }
        return 0;

    }

    public boolean deleteTask(int packLogID){

        synchronized (lock){
            if(currTask != null){
                PackLog currLog = currTask.getPackLog();
                if(currLog != null && currLog.getId() == packLogID) return false;
            }

        }

        PackTask task = getTaskByPackID(packLogID);

        if(task != null){
            tasks.remove(task);
        }

        return true;


    }

    //添加一个新请求到队列中
    public void addPackTask(Channel channel, final PackLog packLog){

        PackTask task = new PackTask(globalConfig, channel, packLog, new PackTask.PackResultListener() {
            @Override
            public void onStateChange(int state) {
                packLog.setState(state);
                packLog.setCreateDate(DateUtil.getDateTime());
                channelService.updatePackLog(packLog);
            }
        });

        this.tasks.offer(task);

        if(!isRunning){
            isRunning = true;
            execute();
        }
    }

    public void execute(){
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try{

                    PackTask localTask = null;

                    while(isRunning){

                        if(localTask != null && localTask.isRunning()) continue;

                        synchronized (lock){
                            currTask = null;
                        }

                        try{
                            Thread.sleep(1500);     //单个渠道打包之后， 延迟1.5秒继续下一个，让前端状态变化可见
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                        if(localTask != null){
                            //deleteWorkTempPath(localTask.getPackLog());
                        }

                        localTask = tasks.take();

                        synchronized (lock){
                            currTask = localTask;
                        }

                        currTask.run();

                    }

                }catch (Exception e){
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @PreDestroy
    public void destory(){
        this.isRunning = false;
        if(executor != null){
            executor.shutdownNow();
            executor = null;
        }
    }


    private void deleteWorkTempPath(PackLog log){

        if(log == null) return;
        try{
            String path = FileUtils.joinPath(globalConfig.getFileTempPath(), "workspace/game"+log.getGameID(),log.getName()+log.getChannelLocalID());

            if(FileUtils.directoryExists(path)){
                FileUtils.deleteFile(path);
            }


            logger.debug("clear pack temp path:"+path);
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e.getMessage());
        }


    }

}
