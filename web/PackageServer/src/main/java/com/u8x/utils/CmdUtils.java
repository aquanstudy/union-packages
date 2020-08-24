package com.u8x.utils;

import com.u8x.common.XLogger;
import com.u8x.task.PackTaskManager;

/**
 * Created by ant on 2018/6/11.
 */
public class CmdUtils {

    private static final XLogger logger = XLogger.getLogger(CmdUtils.class);

    //生成ICON预览图
    public static void generateMaskedIcon(String packPath, String gameID, String channelID){

        String buildPyPath = packPath + "/scripts/icon_utils.py";

        StringBuilder sb = new StringBuilder();
        sb.append(" python ").append(buildPyPath).append(" -g ").append(gameID).append(" -c ").append(channelID);

        exeCmdInThread(sb.toString());

    }

    //生成Splash预览图
    public static void generateCachedSplash(String packPath, String sdk){
        String buildPyPath = packPath + "/scripts/splash_utils.py";

        StringBuilder sb = new StringBuilder();
        sb.append(" python ").append(buildPyPath).append(" -s ").append(sdk);

        exeCmdInThread(sb.toString());
    }


    public static void exeCmdInThread(final String cmd){

        new Thread(new Runnable() {
            @Override
            public void run() {
                exeCmd(cmd);
            }
        }).start();
    }

    public static boolean exeCmd(String cmd){


        logger.debug("execute cmd:"+cmd);

        Process proc = null;

        try{

            proc = Runtime.getRuntime().exec(cmd);

            int exitCode = proc.waitFor();

            if(exitCode == 0){
                logger.error(cmd+" execute success");
                return true;
            }else{
                logger.error(cmd+" execute failed");
                return false;
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(proc != null){
                proc.destroy();
            }
        }
        return false;
    }
}
