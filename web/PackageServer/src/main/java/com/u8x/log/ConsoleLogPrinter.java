package com.u8x.log;

/**
 * Created by ant on 2018/3/20.
 */
public class ConsoleLogPrinter implements ILogPrinter {
    @Override
    public void print(String msg) {
        System.out.println(msg);
        System.out.flush();
    }

    @Override
    public void close() {

    }
}
