package com.u8x.utils;

import com.u8x.common.XLogger;
import com.u8x.task.PackTask;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ant on 2018/3/16.
 */
public class FileUtils {

    private static final XLogger logger = XLogger.getLogger(FileUtils.class);

    public static boolean fileExists(String filePath){

        File f = new File(filePath);
        return f.exists() && !f.isDirectory();
    }

    /**
     * 合法的文件路径名称，不能非法访问上级目录
     * @param name
     * @return
     */
    public static boolean isValidPathName(String name){

        if(StringUtils.isEmpty(name)) return false;
        if(name.contains("..") || name.contains("/") || name.contains("\\")){

            return false;
        }

        return true;
    }

    public static boolean deleteFile(String filePath){
        try {
            org.apache.commons.io.FileUtils.forceDelete(new File(filePath));
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    public static boolean directoryExists(String filePath){
        File f = new File((filePath));
        return f.exists() && f.isDirectory();
    }


    public static String getExt(String filePath){

        if(filePath == null || filePath.trim().length() == 0) return "";
        int index = filePath.lastIndexOf(".");
        if(index >= 0){
            return filePath.substring(index);
        }

        return "";
    }

    public static List<String> readLines(File file){

        try {
            return org.apache.commons.io.FileUtils.readLines(file, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public static String readText(File file){

        try {
            return org.apache.commons.io.FileUtils.readFileToString(file, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String formatRightPath(String path){

        if(StringUtils.isEmpty(path)) return path;

        path = path.replaceAll("\\\\", "/");

        while(path.endsWith("/")){
            path = path.substring(0, path.length()-1);
        }

        while(path.startsWith("/") && path.length() > 1 && path.charAt(1) == '/'){
            path = path.substring(0, path.length()-1);
        }

        return path;

    }

    public static String formatPath(String path){

        if(StringUtils.isEmpty(path)) return path;

        path = path.replaceAll("\\\\", "/");

        while(path.endsWith("/")){
            path = path.substring(0, path.length()-1);
        }

        while(path.startsWith("/")){
            path = path.substring(1, path.length());
        }

        return path;

    }

    public static String joinPath(String...paths){

        if(paths == null || paths.length == 0) return "";

        StringBuilder sb = new StringBuilder();
        for(int i=0; i<paths.length; i++){

            if(i > 0 && i <= paths.length -1 ){
                sb.append("/");
            }

            String p = paths[i];
            sb.append(p);
        }

        String path = formatRightPath(sb.toString());

        return path;
    }

    public static void makeParentDirs(String file){
        File f = new File(file);
        File fd = f.getParentFile();
        if(!fd.exists()){
            fd.mkdirs();
        }
    }

    public static void saveFileFromStream(InputStream inputStream, String targetFile){

        logger.debug("saveFileFromStream:{}", targetFile);

        try{
            makeParentDirs(targetFile);
            org.apache.commons.io.FileUtils.copyInputStreamToFile(inputStream, new File(targetFile));
        }catch (Exception e){
            e.printStackTrace();
        }
//        BufferedWriter bfWriter = null;
//        BufferedReader bfReader =null;
//
//        try{
//
//            makeParentDirs(targetFile);
//
//            bfWriter = new BufferedWriter(new FileWriter(targetFile));
//            bfReader = new BufferedReader(new InputStreamReader(inputStream));
//
//            String line = null;
//            while ((line = bfReader.readLine())!=null) {
//                bfWriter.write(line);
//                bfWriter.newLine(); //因为每次是读取一行内容，所以显示一行后就得换行
//                bfWriter.flush(); //刷新内容
//            }
//
//        }catch (IOException e) {
//            e.printStackTrace();
//            logger.error(e.getMessage());
//        }finally {
//            if(bfWriter != null){
//                try {
//                    bfWriter.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    logger.error(e.getMessage());
//                }
//            }
//
//            if(bfReader != null){
//                try {
//                    bfReader.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    logger.error(e.getMessage());
//                }
//            }
//        }

    }


    //列出当前目录下所有的子目录名称
    public static List<String> listSubDirectoryNames(File dir){

        List<File> files = listSubDirectories(dir);
        if(files != null){
            List<String> result = new ArrayList<>();
            for(File f : files){
                result.add(f.getName());
            }
            return result;
        }
        return null;
    }

    //列出当前目录下所有的子目录，不会递归
    public static List<File> listSubDirectories(File dir){
        try{
            if(!dir.exists() || !dir.isDirectory()){
                logger.error("dir {} is not a valid directory", dir.getAbsolutePath());
                return null;
            }
            List<File> result = new ArrayList<>();
            File[] found = dir.listFiles();
            if(found != null){
                for(int i=0; i<found.length; i++){
                    File item = found[i];
                    if(item.isDirectory()){
                        result.add(item);
                    }
                }
            }

            return result;

        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public static void cloneFile(String sourceFile, String targetFile){

        try {
            org.apache.commons.io.FileUtils.copyFile(new File(sourceFile), new File(targetFile));
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

    }

}
