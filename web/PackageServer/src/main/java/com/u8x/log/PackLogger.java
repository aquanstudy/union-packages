package com.u8x.log;

import java.util.ArrayList;
import java.util.List;

/**
 * 输出打包日志到控制台和日志文件
 * Created by ant on 2018/3/20.
 */
public class PackLogger {

    private List<ILogPrinter> printers = new ArrayList<>();

    public PackLogger(String logFile){
        printers.add(new ConsoleLogPrinter());
        printers.add(new FileLogPrinter(logFile));
    }

    public void print(String msg){
        for(ILogPrinter p : printers){
            p.print(msg);
        }
    }

    public void close(){
        for(ILogPrinter p : printers){
            p.close();
        }
    }
}
