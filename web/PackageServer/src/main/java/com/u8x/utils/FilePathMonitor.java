package com.u8x.utils;

import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ant on 2018/6/23.
 */
public class FilePathMonitor {

    private DefaultFileMonitor fileMonitor;

    private boolean running = false;

    private List<FileChangeListener> listeners = new ArrayList<>();

    public synchronized void startMonitor(FileChangeListener listener, String... paths) throws FileSystemException {
        if(paths == null || paths.length == 0){
            return;
        }

        if(!listeners.contains(listener)){
            listeners.add(listener);
        }

        FileSystemManager fsm = VFS.getManager();
        if(fileMonitor == null){
            fileMonitor = new DefaultFileMonitor(new DefaultFileListener());
            fileMonitor.setRecursive(true);
        }

        for(String path : paths){
            File file = new File(path);
            FileObject monitoredDir = fsm .resolveFile(file.getAbsolutePath());
            fileMonitor.addFile(monitoredDir);
        }

        if(!running){
            fileMonitor.start();
            running = true;
        }

    }

    public synchronized void stopMonitor(){
        if(fileMonitor != null){
            running = false;
            listeners.clear();
            fileMonitor.stop();
        }
    }

    public void onFileChanged(int type, String file){
        for(FileChangeListener listener : listeners){
            listener.onFileChanged(new ChangedFile(type, file));
        }
    }

    public class DefaultFileListener implements FileListener{

        @Override
        public void fileCreated(FileChangeEvent fileChangeEvent) throws Exception {
            onFileChanged(ChangedFile.TYPE_CREATE, fileChangeEvent.getFile().getName().getPath());
        }

        @Override
        public void fileDeleted(FileChangeEvent fileChangeEvent) throws Exception {
            onFileChanged(ChangedFile.TYPE_DELETE, fileChangeEvent.getFile().getName().getPath());
        }

        @Override
        public void fileChanged(FileChangeEvent fileChangeEvent) throws Exception {
            onFileChanged(ChangedFile.TYPE_CHANGE, fileChangeEvent.getFile().getName().getPath());
        }
    }

    public interface FileChangeListener{

        void onFileChanged(ChangedFile changedFile);


    }

    public static class ChangedFile{

        public static final int TYPE_CREATE = 1;
        public static final int TYPE_CHANGE = 2;
        public static final int TYPE_DELETE = 3;

        private String file;
        private int type;

        public ChangedFile(int type, String file){
            this.file = file;
            this.type = type;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }
}
