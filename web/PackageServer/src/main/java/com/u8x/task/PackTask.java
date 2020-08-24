package com.u8x.task;

import com.alibaba.fastjson.JSONObject;
import com.u8x.common.XLogger;
import com.u8x.controller.AdminController;
import com.u8x.dao.config.GlobalConfig;
import com.u8x.dao.entity.Channel;
import com.u8x.dao.entity.PackLog;
import com.u8x.log.PackLogger;
import com.u8x.utils.FileUtils;
import com.u8x.utils.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 打包任务
 * Created by ant on 2018/3/17.
 */
public class PackTask implements Runnable {

    private static final XLogger logger = XLogger.getLogger(PackTask.class);

    private String buildPyPath = "";
    private String packLogPath = "";
    private String outputPath = "";
//    private String gameId;
//    private Integer channelID;  //channel表的id
//    private String channelName;
//    private String packId;

    private GlobalConfig globalConfig;
    private Channel channel;
    private PackLog packLog;

    private volatile boolean running = false;
    private PackResultListener listener;

    public PackTask(GlobalConfig globalConfig, Channel channel, PackLog packLog, PackResultListener listener){

        this.channel = channel;
        this.packLog = packLog;
        this.globalConfig = globalConfig;

        String pyPath = globalConfig.getPackageBuildPath();
        String filePath = globalConfig.getFileTempPath();

        if(pyPath.endsWith("/")){
            pyPath = pyPath.substring(0, pyPath.length()-1);
        }
        if(filePath.endsWith("/")){
            filePath = filePath.substring(0, filePath.length()-1);
        }


        this.buildPyPath = pyPath + "/scripts/package.py";
        this.packLogPath = filePath + "/log";
        this.outputPath = filePath + "/output/game" + channel.getGameID();

        this.listener = listener;
    }

    public PackLog getPackLog(){
        return packLog;
    }

    @Override
    public void run() {

        Process proc = null;
        try{


            String pyPath = FileUtils.formatRightPath(buildPyPath);
            File pyFile = new File(pyPath);
            if(!pyFile.exists()){
                logger.error("pack build task run failed. build py file not exists. {}", pyPath);
                onStateChange(PackLog.STATE_FAILED);
                return;
            }

            String currOutputPath = outputPath + "/" + channel.getName() + channel.getId() + "/" + packLog.getId() + ".apk";
            if(FileUtils.fileExists(currOutputPath)){
                FileUtils.deleteFile(currOutputPath);
            }

            running = true;
            onStateChange(PackLog.STATE_PACKING);
            String cmd = parseCmd(pyPath, channel.getGameID()+"", channel.getId(), packLog.getId()+"");

            logger.debug("pack task exe begin.cmd:{}", cmd);

            String currLogPath = FileUtils.formatRightPath(packLogPath) + "/u8sdk-"+packLog.getId()+".log";
            PackLogger packLogger = new PackLogger(currLogPath);

            proc = Runtime.getRuntime().exec(cmd);

            printOutput(proc.getErrorStream(),packLogger);
            printOutput(proc.getInputStream(),packLogger);

            int exitCode = proc.waitFor();

            if (exitCode == 0) {
                logger.debug("game:{}-channel:{} pack finished.", channel.getGameID(), channel.getName());

                File outputFile = new File(currOutputPath);
                if(outputFile.exists()){
                   //success
                    onStateChange(PackLog.STATE_SUCCESS);
                    logger.debug("game:{}-channel:{} pack success.", channel.getGameID(), channel.getName());
                }else{
                    onStateChange(PackLog.STATE_FAILED);
                    logger.error("game:{}-channel:{} pack failed.you can check it in the package log.", channel.getGameID(), channel.getName());
                }

            } else {
                onStateChange(PackLog.STATE_FAILED);
                logger.debug("game:{}-channel:{} pack failed.", channel.getGameID(), channel.getName());
            }

            packLogger.close();
            running = false;

        }catch (Exception e){
            onStateChange(PackLog.STATE_FAILED);
            running = false;
            logger.error(e.getMessage());
            e.printStackTrace();
        }finally {
            if(proc != null){
                proc.destroy();
            }
        }
    }

    private void onStateChange(int state){
        if(listener != null){
            listener.onStateChange(state);
        }
    }

    public boolean isRunning(){
        return running;
    }


    private void printOutput(InputStream stream, PackLogger packLogger) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStreamReader isr = null;
                BufferedReader br = null;
                try{

                    isr = new InputStreamReader(stream);
                    br = new BufferedReader(isr);
                    String line;

                    while((line = br.readLine()) != null ) {
                        packLogger.print(line);
                    }

                }catch (Exception e){
                    logger.error("printOutput exception:{}", e.getMessage());
                    e.printStackTrace();
                }finally {
                    if(isr != null){
                        try{
                            isr.close();
                        }catch (Exception e){
                            logger.error("printOutput exception:{}", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    if(br != null){
                        try{
                            br.close();
                        }catch (Exception e){
                            logger.error("printOutput exception:{}", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    if(stream != null){
                        try{
                            stream.close();
                        }catch (Exception e){
                            logger.error("printOutput exception:{}", e.getMessage());
                            e.printStackTrace();
                        }
                    }

                }
            }
        }).start();

    }

    private void storeTempDBConfig(){

        String dbIp = globalConfig.getDBIP();
        int dbPort = globalConfig.getDBPort();
        String dbName = globalConfig.getDBName();
        String dbUsername = globalConfig.getDBUsername();
        String dbPassword = globalConfig.getDBPassword();

        StringBuilder ext = new StringBuilder();
        ext.append("mysql.host=").append(dbIp)
                .append("\n").append("mysql.port=").append(dbPort)
                .append("\n").append("mysql.db=").append(dbName)
                .append("\n").append("mysql.user=").append(dbUsername)
                .append("\n").append("mysql.password=").append(dbPassword);

        String tempFilePath = FileUtils.joinPath(globalConfig.getFileTempPath(), "games", "game"+channel.getGameID(), "u8_temp_db_config.txt");
        FileUtils.saveFileFromStream(new ByteArrayInputStream(ext.toString().getBytes(Charset.forName("UTF-8"))), tempFilePath);

    }

    private String parseCmd(String pyPath, String gameId, Integer channelID, String packId){

        storeTempDBConfig();

        StringBuilder sb = new StringBuilder();
        sb.append(" python ").append(pyPath)
                .append(" -g ").append(gameId)
                .append(" -c ").append(channelID)
                .append(" -i ").append(packId)
                .append(" -f ").append(globalConfig.getFileTempPath());
        return sb.toString();
    }


    public static interface PackResultListener{
        public void onStateChange(int state);
    }
}
