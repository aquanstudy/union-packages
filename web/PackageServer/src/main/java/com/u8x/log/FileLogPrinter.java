package com.u8x.log;

import com.u8x.common.XLogger;
import com.u8x.utils.FileUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by ant on 2018/3/20.
 */
public class FileLogPrinter implements ILogPrinter {
    private static final XLogger logger = XLogger.getLogger(FileLogPrinter.class);

    private String logFile;

    private BufferedWriter bfWriter = null;

    public FileLogPrinter(String logFile){
        this.logFile = logFile;
        try{
            FileUtils.makeParentDirs(logFile);
        }catch (Exception e){
            logger.error("make log file parent dir failed. {}", logFile);
            e.printStackTrace();
        }

    }

    @Override
    public void print(String msg) {
        try{

            if(bfWriter == null){
                bfWriter = new BufferedWriter(new FileWriter(logFile));
            }

            bfWriter.write(msg);
            bfWriter.newLine();
            bfWriter.flush();


        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }


    }

    public void close(){
        if(bfWriter != null){
            try {
                bfWriter.close();
            } catch (IOException e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
