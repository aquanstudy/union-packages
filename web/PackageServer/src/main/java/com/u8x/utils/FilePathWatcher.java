package com.u8x.utils;

import com.sun.nio.file.ExtendedWatchEventModifier;
import com.u8x.common.XLogger;

import java.io.File;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 目录文件改变监听服务
 * Created by ant on 2018/5/20.
 */
public class FilePathWatcher extends Thread {

    private static final XLogger logger = XLogger.getLogger(FilePathWatcher.class);

    private String path;
    private FileChangeListener changeListener;

    private volatile boolean running = false;

    public FilePathWatcher(String path, FileChangeListener listener){
        this.path = path;
        this.changeListener = listener;
        this.running = false;
    }

    public void run(){
        try{

            running = true;

            Path myDir = Paths.get(path);
            WatchService watcher = myDir.getFileSystem().newWatchService();
            myDir.register(watcher, new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY}, ExtendedWatchEventModifier.FILE_TREE);


            while(running){
                WatchKey watchKey = watcher.take();

                List<WatchEvent<?>> events = watchKey.pollEvents();

                List<String> created = new ArrayList<>();
                List<String> modified = new ArrayList<>();
                List<String> removed = new ArrayList<>();

                for (WatchEvent event : events) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        logger.trace("{} : create a new file: {}", path, event.context().toString());
                        created.add(event.context().toString());
                    }
                    if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                        logger.trace("{} : delete a file: {}", path, event.context().toString());
                        removed.add(event.context().toString());
                    }
                    if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        logger.trace("{} : file changed: {}", path, event.context().toString());
                        modified.add(event.context().toString());
                    }
                }
                watchKey.reset();

                if(changeListener != null){
                    changeListener.onFileChanged(created, modified, removed);
                }

            }


        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public interface FileChangeListener{

        void onFileChanged(List<String> createdFiles, List<String> modifiedFiles, List<String> removedFiles);


    }

    public void close(){
        running = false;
    }

}
