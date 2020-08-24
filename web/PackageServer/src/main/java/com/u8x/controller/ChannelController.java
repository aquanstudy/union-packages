package com.u8x.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.u8x.common.Consts;
import com.u8x.common.XLogger;
import com.u8x.controller.view.*;
import com.u8x.dao.config.GlobalConfig;
import com.u8x.dao.entity.*;
import com.u8x.sdk.LocalSDKManager;
import com.u8x.services.ChannelService;
import com.u8x.services.GameService;
import com.u8x.task.PackTaskManager;
import com.u8x.utils.*;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Created by ant on 2017/10/11.
 */

@Controller
@RequestMapping("/admin/channel")
public class ChannelController {

    private static final XLogger logger = XLogger.getLogger(ChannelController.class);

    @Autowired
    private ChannelService channelService;

    @Autowired
    private GameService gameService;

    @Autowired
    private PackTaskManager packTaskManager;

    @Autowired
    private GlobalConfig globalConfig;

    @Autowired
    private LocalSDKManager sdkManager;


    @RequestMapping(value = "/getAllMasters", method = RequestMethod.GET)
    @ResponseBody
    public ListView<ChannelMaster> getAllMasters(){

        try{

            ListView<ChannelMaster> view = new ListView<>();
            view.setData(channelService.getAllLocalMasters());
            return view;


        }catch (Exception e){

            logger.error(e.getMessage());
            e.printStackTrace();
            return ListView.buildFailure(Consts.Tips.FailMsg);
        }

    }


    @RequestMapping(value = "/getAllPlugins", method = RequestMethod.GET)
    @ResponseBody
    public ListView<PluginMaster> getAllPlugins(Integer channelID){

        try{

            Channel exists = channelService.getChannelByID(channelID);
            if(exists == null){
                return ListView.buildFailure(Consts.Tips.ChannelNotExists);
            }

            List<PluginMaster> masters = sdkManager.getLocalPlugins();
            Map<String, ChannelPlugin> plugins = channelService.getPluginsByChannelWithGame(exists.getGameID(), exists.getId());

            if(plugins != null && plugins.size() > 0){
                for(PluginMaster master : masters){

                    if(plugins.containsKey(master.getSdkName())){
                        ChannelPlugin plugin = plugins.get(master.getSdkName());
                        master.setExtend(plugin.getExtend());
                        master.setState(plugin.getState());
                    }
                }
            }

            ListView<PluginMaster> view = new ListView<>();
            view.setData(masters);
            return view;


        }catch (Exception e){

            logger.error(e.getMessage());
            e.printStackTrace();
            return ListView.buildFailure(Consts.Tips.FailMsg);
        }

    }

    @RequestMapping(value = "/tooglePlugin", method = RequestMethod.POST)
    @ResponseBody
    ResponseView tooglePlugin(Integer channelID, String sdkName, Integer checked){
        ResponseView view = new ResponseView();
        try{

            Channel channel = channelService.getChannelByID(channelID);
            if(channel == null){
                view.failure(Consts.Tips.GameNotExists);
                return view;
            }

            if(StringUtils.isEmpty(sdkName)){
                logger.error("sdkName is empty");
                view.failure(Consts.Tips.FailMsg);
                return view;
            }


            List<ChannelPlugin> plugins = channelService.getPluginsByChannel(channelID);
            Map<String, GamePlugin> gamePlugins = gameService.getPluginMapByGame(channel.getGameID());

            ChannelPlugin channelPlugin = null;
            GamePlugin gamePlugin = null;

            if(plugins != null){
                for(ChannelPlugin plugin : plugins){
                    if(plugin.getSdkName().equals(sdkName)){
                        channelPlugin = plugin;
                        break;
                    }
                }
            }

            if(gamePlugins != null && gamePlugins.containsKey(sdkName)){
                gamePlugin = gamePlugins.get(sdkName);
            }

            if(channelPlugin == null){

                if(checked == 1){

                    //覆写或者添加
                    ChannelPlugin plugin = new ChannelPlugin();
                    plugin.setExtend(1);
                    plugin.setSdkName(sdkName);
                    plugin.setGameID(channel.getGameID());
                    plugin.setState(1);
                    plugin.setChannelID(channel.getId());
                    if(gamePlugin != null){
                        plugin.setParams(gamePlugin.getParams());
                    }
                    channelService.insertChannelPlugin(plugin);

                    return view.success();

                }else{
                    //禁用
                    if(gamePlugin == null) {
                        //非法，直接过滤
                        logger.warn("tooglePlugin failed. unchecked failed because of invalid state");
                        return view.success();
                    }

                    ChannelPlugin plugin = new ChannelPlugin();
                    plugin.setExtend(0);
                    plugin.setSdkName(sdkName);
                    plugin.setGameID(channel.getGameID());
                    plugin.setState(0);
                    plugin.setChannelID(channel.getId());
                    channelService.insertChannelPlugin(plugin);
                    return view.success();
                }


            }

            //启用的时候， 如果之前没有覆写过，那么判断如果游戏中含有该插件，那么删除本条记录；如果游戏中不含有本条记录，直接改为覆写。
            if(checked == 1){
                if(channelPlugin.getExtend() == 0){
                    if(gamePlugin != null){
                        //如果游戏中包含，那么删除该条记录
                        channelService.deletePluginByID(channelPlugin.getId());
                    }else{
                        //如果游戏中不包含，那么改为更新即可
                        channelPlugin.setState(1);
                        channelPlugin.setExtend(1);
                        channelService.updateChannelPlugin(channelPlugin);
                    }
                }else{
                    //如果之前就是覆写过的，那么直接更新即可。
                    channelPlugin.setState(1);
                    channelService.updateChannelPlugin(channelPlugin);
                }
            }else{


                if(gamePlugin == null){
                    //如果游戏中没有该条记录了， 直接删除
                    channelService.deletePluginByID(channelPlugin.getId());

                }else{
                    //禁用的话，直接将状态改为禁用即可
                    channelPlugin.setState(0);
                    channelService.updateChannelPlugin(channelPlugin);
                }


            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return view.success();
    }


    @RequestMapping(value = "/overridePlugin", method = RequestMethod.POST)
    @ResponseBody
    ResponseView overridePlugin(Integer channelID, String sdkName){
        ResponseView view = new ResponseView();
        try{

            Channel channel = channelService.getChannelByID(channelID);
            if(channel == null){
                view.failure(Consts.Tips.GameNotExists);
                return view;
            }

            if(StringUtils.isEmpty(sdkName)){
                logger.error("sdkName is empty");
                view.failure(Consts.Tips.FailMsg);
                return view;
            }


            List<ChannelPlugin> plugins = channelService.getPluginsByChannel(channelID);
            Map<String, GamePlugin> gamePlugins = gameService.getPluginMapByGame(channel.getGameID());

            ChannelPlugin channelPlugin = null;
            GamePlugin gamePlugin = null;
            if(plugins != null){
                for(ChannelPlugin plugin : plugins){
                    if(plugin.getSdkName().equals(sdkName)){
                        channelPlugin = plugin;
                        break;
                    }
                }
            }

            if(channelPlugin != null){
                logger.warn("the plugin {} already override by channel {}", sdkName, channelID);
                return view.success();
            }

            if(gamePlugins != null && gamePlugins.containsKey(sdkName)){
                gamePlugin = gamePlugins.get(sdkName);
            }

            //覆写或者添加
            ChannelPlugin plugin = new ChannelPlugin();
            plugin.setExtend(1);
            plugin.setSdkName(sdkName);
            plugin.setGameID(channel.getGameID());
            plugin.setState(1);
            plugin.setChannelID(channel.getId());
            if(gamePlugin != null){
                plugin.setParams(gamePlugin.getParams());
            }
            channelService.insertChannelPlugin(plugin);
            return view.success();

        }catch (Exception e){
            e.printStackTrace();
        }
        return view.failure(Consts.Tips.FailMsg);

    }


    @RequestMapping(value = "/saveChannel", method = RequestMethod.POST)
    @ResponseBody
    ChannelResponseView saveChannel(@ModelAttribute Channel channel){

        try{

            if(channel.getId() == null || channel.getId().equals(0)){
                channel.setSplash("0");
                channel.setIconType(1);
                channel.setSignApk(1);
                channel.setSignVersion("V1");
                channel.setIsConfiged(0);
                channel.setAutoPermission(1);
                channel.setDirectPermission(0);
                channel.setAutoProtocol(1);
                channel.setProtocolUrl("file:///android_asset/m_userprotocol.html");
//                channel.setMinSdkVersion(14);
//                channel.setTargetSdkVersion(22);
//                channel.setMaxSdkVersion(28);
                channelService.createChannel(channel);
            }else{
                Channel exists = channelService.getChannelByID(channel.getId());
                if(exists == null){
                    return ChannelResponseView.buildFail(Consts.Tips.FailMsg);
                }
                exists.setName(channel.getName());
                channelService.updateChannel(exists);
            }

            return ChannelResponseView.buildSuccess(channel.getId());

        }catch (Exception e){

            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return ChannelResponseView.buildFail(Consts.Tips.FailMsg);
    }

    @RequestMapping(value = "/getMetas", method = RequestMethod.GET)
    @ResponseBody
    public DeferredResult<ListView<ChannelParamMeta>> getMetas(String masterSdkName){
        DeferredResult<ListView<ChannelParamMeta>> result = new DeferredResult<>(30 * 60 * 1000L);

        try{

            CompletableFuture<List<ChannelParamMeta>> future = channelService.getChannelParamMetas(masterSdkName);

            if (future == null) {
                result.setResult(ListView.buildFailure(Consts.Tips.FailMsg));
                return result;
            }

            future.whenComplete((lst, err) -> {
                if (err != null) {
                    logger.error("getMetas error:"+err.getMessage());
                    result.setResult(ListView.buildFailure(err.getMessage()));
                    return;
                }
                if (lst == null) {
                    lst = new ArrayList<>();
                }
                ListView<ChannelParamMeta> view = new ListView<>();
                view.setData(lst);
                result.setResult(view);

            });


        }catch (Exception e){
            result.setResult(ListView.buildFailure(Consts.Tips.FailMsg));
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return result;

    }

    @RequestMapping(value = "/saveMeta", method = RequestMethod.POST)
    @ResponseBody
    public DeferredResult<ResponseView> saveMeta(String masterSdkName, @ModelAttribute ChannelParamMeta meta){

        DeferredResult<ResponseView> result = new DeferredResult<>(30 * 60 * 1000L);
        ResponseView view = new ResponseView();
        try{

            CompletableFuture<Boolean> future = channelService.saveChannelParamMeta(masterSdkName, meta);

            if (future == null) {
                result.setResult(view.failure(Consts.Tips.FailMsg));
                return result;
            }

            future.whenComplete((res, err) -> {
                if (err != null) {
                    logger.error("saveMeta error:"+err.getMessage());
                    result.setResult(view.failure(err.getMessage()));
                    return;
                }

                if(!res){
                    result.setResult(view.failure(Consts.Tips.FailMsg));
                }else{
                    result.setResult(view.success());
                }

            });


        }catch (Exception e){
            result.setResult(ListView.buildFailure(Consts.Tips.FailMsg));
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return result;

    }

    @RequestMapping(value = "/deleteMeta", method = RequestMethod.POST)
    @ResponseBody
    public DeferredResult<ResponseView> deleteMeta(Integer metaID){

        DeferredResult<ResponseView> result = new DeferredResult<>(30 * 60 * 1000L);
        ResponseView view = new ResponseView();
        try{

            if(metaID == null){
                result.setResult(view.failure(Consts.Tips.FailMsg));
                return result;
            }

            CompletableFuture<Boolean> future = channelService.deleteChannelParamMeta(metaID);

            if (future == null) {
                result.setResult(view.failure(Consts.Tips.FailMsg));
                return result;
            }

            future.whenComplete((res, err) -> {
                if (err != null) {
                    logger.error("deleteMeta error:"+err.getMessage());
                    result.setResult(view.failure(err.getMessage()));
                    return;
                }

                if(!res){
                    result.setResult(view.failure(Consts.Tips.FailMsg));
                }else{
                    result.setResult(view.success());
                }

            });


        }catch (Exception e){
            result.setResult(ListView.buildFailure(Consts.Tips.FailMsg));
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return result;

    }



    @RequestMapping(value = "/getChannelPlugins", method = RequestMethod.POST)
    @ResponseBody
    public ListView<ChannelPlugin> getChannelPlugins(Integer gameID, Integer channelID){

        try{

            List<ChannelPlugin> plugins = channelService.getPluginsByChannelWithMaster(gameID, channelID);
            ListView<ChannelPlugin> view = new ListView<>();
            view.setData(plugins);
            return view;


        }catch (Exception e){

            logger.error(e.getMessage());
            e.printStackTrace();
            return ListView.buildFailure(Consts.Tips.FailMsg);
        }

    }


    @RequestMapping(value = "/getChannelFullConfig", method = RequestMethod.POST)
    @ResponseBody
    public DeferredResult<ChannelView> getChannelFullConfig(String appID, Integer channelID){
        DeferredResult<ChannelView> result = new DeferredResult<>(30 * 60 * 1000L);

        try{

            Channel exists = channelService.getChannelByID(channelID);
            if(exists == null){

                result.setResult(ChannelView.buildFailure(Consts.Tips.ChannelNotExists));
                return result;
            }

            ChannelMaster master = sdkManager.getMasterBySDK(exists.getSdk());
            if(master == null){
                result.setResult(ChannelView.buildFailure(Consts.Tips.SDKNotConfig));
                return result;
            }

            //预先处理生成角标预览图
            try{
                CmdUtils.generateMaskedIcon(globalConfig.getPackageBuildPath(), appID, channelID+"");
                CmdUtils.generateCachedSplash(globalConfig.getPackageBuildPath(), master.getSdkName());
            }catch (Exception e){
                e.printStackTrace();
                logger.error(e.getMessage());
            }


            CompletableFuture<ChannelView> future = channelService.getChannelFullConfig(exists, master);

            if (future == null) {
                result.setResult(ChannelView.buildFailure(Consts.Tips.FailMsg));
                return result;
            }

            future.whenComplete((view, err) -> {
                if (err != null) {
                    result.setResult(ChannelView.buildFailure(err.getMessage()));
                    return;
                }
                if (view == null) {
                    result.setResult(ChannelView.buildFailure(Consts.Tips.FailMsg));
                    return;
                }

                if(view.code != Consts.RespCode.SUCCESS){
                    result.setResult(ChannelView.buildFailure(view.reason));
                    return;
                }

                result.setResult(view);

            });


        }catch (Exception e){
            result.setResult(ChannelView.buildFailure(Consts.Tips.FailMsg));
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return result;

    }




    @RequestMapping(value = "/saveConfig", method = RequestMethod.POST)
    @ResponseBody
    public DeferredResult<ResponseView> saveChannelConfig(Channel channel,
                                                          String channelParams,
                                                          String gameProducts,
                                                          String channelPlugins,
                                                          @RequestParam(value = "iconCustomFile", required = false) MultipartFile iconFile,
                                                          @RequestParam(value = "splashCustomFile", required = false) MultipartFile splashFile){


        DeferredResult<ResponseView> result = new DeferredResult<>(30 * 60 * 1000L);
        ResponseView view = new ResponseView();
        try{

            Channel exists = channelService.getChannelByID(channel.getId());
            if(exists == null){

                result.setResult(view.failure(Consts.Tips.ChannelNotExists));
                return result;
            }

            if(!U8XUtils.isVilidAndroidPackageName(channel.getBundleID())){
                result.setResult(view.failure(Consts.Tips.PNAMENOTVALID));
                return result;
            }

            //保存插件配置
            channelService.updateChannelPlugins(channel.getId(), channelPlugins);


            if(iconFile != null && !iconFile.isEmpty()){
                String path = FileUtils.joinPath(globalConfig.getFileTempPath(), "games/game"+channel.getGameID(), "channels", channel.getId()+"", "custom_icon/icon.png");
                FileUtils.saveFileFromStream(iconFile.getInputStream(), path);
            }

            if(splashFile != null && !splashFile.isEmpty()){
                String path = FileUtils.joinPath(globalConfig.getFileTempPath(), "games/game"+channel.getGameID(), "channels", channel.getId()+"", "custom_splash/u8_splash.png");
                FileUtils.saveFileFromStream(splashFile.getInputStream(), path);
            }

            exists.setGameName(channel.getGameName());
            exists.setBundleID(channel.getBundleID());
            exists.setSignApk(channel.getSignApk());
            exists.setSignVersion(channel.getSignVersion());
            exists.setKeystoreID(channel.getKeystoreID());
            exists.setMinSdkVersion(channel.getMinSdkVersion());
            exists.setTargetSdkVersion(channel.getTargetSdkVersion());
            exists.setMaxSdkVersion(channel.getMaxSdkVersion());

            exists.setAutoPermission(channel.getAutoPermission() == null ? 0 : channel.getAutoPermission());
            exists.setAutoProtocol(channel.getAutoProtocol() == null ? 0 : channel.getAutoProtocol());
            exists.setExcludePermissionGroups(channel.getExcludePermissionGroups());
            exists.setProtocolUrl(channel.getProtocolUrl());
            exists.setDirectPermission(channel.getDirectPermission() == null ? 0 : channel.getDirectPermission());

            exists.setIconType(channel.getIconType());
            exists.setIcon(channel.getIcon());
            exists.setSplash(channel.getSplash());
            exists.setIsConfiged(1);
            exists.setServerBaseUrl(channel.getServerBaseUrl());
            exists.setSdkLogicVersionCode(channel.getSdkLogicVersionCode());

            if(exists.getIsLocal() == 1){
                //本地配置， 直接存入本地配置；
                Map<String, String> params = JSONObject.parseObject(channelParams, new TypeReference<Map<String, String>>(){});
                List<ChannelParamMeta> metas = JSON.parseArray(exists.getLocalConfig(), ChannelParamMeta.class);
                if(metas != null){
                    JSONArray array = new JSONArray();
                    for(ChannelParamMeta meta : metas){
                        if(params.containsKey(meta.getParamKey())){
                            meta.setDefaultVal(params.get(meta.getParamKey()));
                        }
                        array.add(meta.toJSON());
                    }

                    exists.setLocalConfig(array.toString());
                    channelService.updateChannel(exists);
                }

                result.setResult(view.success());

                return result;
            }

            channelService.updateChannel(exists);

            CompletableFuture<Boolean> future = channelService.saveChannelConfig(exists.getGameID()+"", exists.getChannelID(), channelParams, gameProducts);

            if (future == null) {
                result.setResult(ListView.buildFailure(Consts.Tips.FailMsg));
                return result;
            }

            future.whenComplete((data, err) -> {
                if (err != null) {
                    result.setResult(view.failure(err.getMessage()));
                    return;
                }
                if (data == null || !data) {
                    result.setResult(view.failure(Consts.Tips.FailMsg));
                    return;
                }

                result.setResult(view.success());

            });

        }catch (Exception e){
            logger.error(e.getMessage());
            result.setResult(view.failure(Consts.Tips.FailMsg));
            e.printStackTrace();
        }
        return result;
    }

    @RequestMapping(value = "/saveChannelFiles", method = RequestMethod.POST)
    @ResponseBody
    public ResponseView saveChannelFiles(Integer channelID,
                                                          String fileIds,
                                                          @RequestParam(value = "file", required = false) MultipartFile[] files){


        ResponseView view = new ResponseView();
        try{

            Channel exists = channelService.getChannelByID(channelID);
            if(exists == null){

                return view.failure(Consts.Tips.ChannelNotExists);
            }

            if(files != null && files.length > 0){

                String basePath = FileUtils.joinPath(globalConfig.getFileTempPath(), "games/game"+exists.getGameID(), "channels", channelID+"");

                JSONArray array = JSONArray.parseArray(fileIds);

                for(int i=0; i<files.length; i++){

                    JSONObject item = array.getJSONObject(i);
                    String key = item.getString("key");
                    String path = item.getString("path");

                    String fullPath = FileUtils.joinPath(basePath, path);

                    FileUtils.saveFileFromStream(files[i].getInputStream(), fullPath);
                }

            }
            return view.success();

        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return view.failure(Consts.Tips.FailMsg);
    }


    @RequestMapping(value = "/saveChannelParams", method = RequestMethod.POST)
    @ResponseBody
    public DeferredResult<ResponseView> saveChannelParams(String appID, Integer channelID, String channelParams){
        DeferredResult<ResponseView> result = new DeferredResult<>(30 * 60 * 1000L);
        ResponseView view = new ResponseView();
        try{

            Channel exists = channelService.getChannelByID(channelID);
            if(exists == null){

                result.setResult(view.failure(Consts.Tips.ChannelNotExists));
                return result;
            }

            if(exists.getIsLocal() == 1){
                //本地配置， 直接存入本地配置
                Map<String, String> params = JSONObject.parseObject(channelParams, new TypeReference<Map<String, String>>(){});
                List<ChannelParamMeta> metas = JSON.parseArray(exists.getLocalConfig(), ChannelParamMeta.class);
                if(metas != null){
                    JSONArray array = new JSONArray();
                    for(ChannelParamMeta meta : metas){
                        if(params.containsKey(meta.getParamKey())){
                            meta.setDefaultVal(params.get(meta.getParamKey()));
                        }
                        array.add(meta.toJSON());
                    }

                    exists.setLocalConfig(array.toString());
                    channelService.updateChannel(exists);
                }

                result.setResult(view.success());

                return result;
            }

            CompletableFuture<Boolean> future = channelService.saveChannelConfig(appID, exists.getChannelID(), channelParams, null);

            if (future == null) {
                result.setResult(ListView.buildFailure(Consts.Tips.FailMsg));
                return result;
            }

            future.whenComplete((data, err) -> {
                if (err != null) {
                    result.setResult(view.failure(err.getMessage()));
                    return;
                }
                if (data == null || !data) {
                    result.setResult(view.failure(Consts.Tips.FailMsg));
                    return;
                }

                result.setResult(view.success());

            });

        }catch (Exception e){
            logger.error(e.getMessage());
            result.setResult(view.failure(Consts.Tips.FailMsg));
            e.printStackTrace();
        }
        return result;
    }


    @RequestMapping(value = "/saveLocalChannel", method = RequestMethod.POST)
    @ResponseBody
    DeferredResult<ChannelResponseView> saveLocalChannel(@ModelAttribute Channel channel, Integer parentChannelID){
        DeferredResult<ChannelResponseView> result = new DeferredResult<>(30 * 60 * 1000L);
        try{

            ChannelResponseView view = new ChannelResponseView();



            if(channel.getId() == null || channel.getId().equals(0)){
                //新增本地配置

                if(parentChannelID == null || parentChannelID == 0) {
                    view.failure(Consts.Tips.ChannelNotSpecified);
                    result.setResult(view);
                    return result;
                }

                Channel parentChannel = channelService.getChannelByID(parentChannelID);
                if(parentChannel == null){
                    view.failure(Consts.Tips.ChannelNotExists);
                    result.setResult(view);
                    return result;
                }

                if(parentChannel.getIsLocal() == 1){
                    //指定的父配置本身是一个子配置
                    view.failure(Consts.Tips.FailMsg);
                    result.setResult(view);
                    return result;

                }

                final Channel newChannel = parentChannel;
                newChannel.setName(channel.getName());

                //新增本地配置的时候， 从服务器拉取父配置的渠道参数
                CompletableFuture<CompletableResponse<List<ChannelParamMeta>>> future = channelService.getChannelConfig(channel.getGameID()+"", channel.getChannelID());

                if (future == null) {
                    result.setResult(ChannelResponseView.buildFail(Consts.Tips.FailMsg));
                    return result;
                }

                future.whenComplete((lst, err) -> {
                    if (err != null) {
                        result.setResult(ChannelResponseView.buildFail(err.getMessage()));
                        return;
                    }
                    if (lst == null) {
                        result.setResult(ChannelResponseView.buildFail(Consts.Tips.FailMsg));
                        return;
                    }

                    if(lst.code != Consts.RespCode.SUCCESS){
                        result.setResult(ChannelResponseView.buildFail(lst.reason));
                        return;
                    }

                    if(lst.data == null){
                        result.setResult(ChannelResponseView.buildFail(Consts.Tips.MetaNotConfig));
                        return;
                    }

                    JSONArray localConfig = new JSONArray();
                    for(ChannelParamMeta meta : lst.data){
                        localConfig.add(meta.toJSON());
                    }

                    newChannel.setIsLocal(1);
                    newChannel.setLocalConfig(localConfig.toString());
                    newChannel.setId(null);
                    newChannel.setIsConfiged(1);
                    channelService.createChannel(newChannel);

                    result.setResult(ChannelResponseView.buildSuccess(newChannel.getId()));

                });

                return result;

            }else{
                Channel exists = channelService.getChannelByID(channel.getId());
                if(exists == null){
                    result.setResult(ChannelResponseView.buildFail(Consts.Tips.FailMsg));
                    return result;
                }

                if(exists.getIsLocal() != 1){
                    logger.error("curr channel is not a local config. channelID:"+channel.getId());
                    result.setResult(ChannelResponseView.buildFail(Consts.Tips.FailMsg));
                    return result;
                }

                //编辑的时候， 只有唯一打包标识可以改
                exists.setName(channel.getName());
                channelService.updateChannel(exists);
                result.setResult(ChannelResponseView.buildSuccess(channel.getId()));
                return result;
            }

        }catch (Exception e){
            result.setResult(ChannelResponseView.buildFail(Consts.Tips.FailMsg));
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }


    @RequestMapping(value = "/getAllChannels", method = RequestMethod.GET)
    @ResponseBody
    public ListView<Channel> getAllChannels(Integer appID, Integer channelID, String name, String channelName){
        ListView<Channel> view = new ListView<>();
        try{

            if(appID == null){
                appID = 0;
            }

            if(channelID == null){
                channelID = 0;
            }

            List<Channel> channels = channelService.getChannels(appID, name, channelName, channelID);

            view.setData(channels);
            view.success();


        }catch (Exception e){
            logger.error(e.getMessage());
            view.failure(Consts.Tips.FailMsg);
            e.printStackTrace();
        }

        return view;
    }

    @RequestMapping(value = "/deleteChannel", method = RequestMethod.POST)
    @ResponseBody
    public ResponseView deleteChannel(Integer id){
        ResponseView view = new ResponseView();
        try{

            Channel channel = channelService.getChannelByID(id);
            if(channel == null){
                logger.error("channel not exists of id:{}", id);
                return view.failure(Consts.Tips.ChannelNotExists);
            }

            channelService.deleteChannel(id);
            return view.success();

        }catch (Exception e){
            e.printStackTrace();
            return view.failure(Consts.Tips.FailMsg);
        }
    }


    public ChannelController() {
    }

    @RequestMapping(value = "/package", method = RequestMethod.POST)
    @ResponseBody
    public DeferredResult<ResponseView> packageChannels(Integer appID, final String channelIDs){
        DeferredResult<ResponseView> result = new DeferredResult<>(30 * 60 * 1000L);
        ResponseView view = new ResponseView();
        try{


            Game game = gameService.getGameById(appID);
            if(game == null){
                logger.error("game not found of game id:"+appID);
                result.setResult(view.failure(Consts.Tips.GameNotExists));
                return result;
            }

            if(StringUtils.isEmpty(game.getApkPath()) || !FileUtils.fileExists(FileUtils.joinPath(globalConfig.getFileTempPath(), game.getApkPath()))){
                logger.error("base apk not specified of game id:"+appID);
                result.setResult(view.failure(Consts.Tips.APKNotSpecified));
                return result;
            }



            if(StringUtils.isEmpty(channelIDs)){
                logger.error("no channel selected to package.");
                result.setResult(view.failure(Consts.Tips.ChannelNotExists));
                return result;
            }
            String ids = channelIDs;
            if(ids.endsWith(",")) ids = ids.substring(0, ids.length()-1);
            if(ids.startsWith(",")) ids = ids.substring(1, ids.length());

            final List<Channel> channels = channelService.getChannelsByIDs(ids);
            if(channels == null || channels.size() <= 0){
                logger.error("there is no channel found by ids:{}", ids);
                result.setResult(view.failure(Consts.Tips.ChannelNotExists));
                return result;
            }


            int num = 0;
            String cids = "";
            for(Channel channel : channels){
                if(channel.getIsLocal() != 1){
                    //排除本地配置
                    cids = cids + "," + channel.getChannelID();
                    num++;
                }

            }
            
            if(cids.length() >= 1){
                cids = cids.substring(1, cids.length());
            }

            if(num <= 0){
                //都是本地配置
                channelService.addPackTasks(channels, null);
                result.setResult(view.success());
                return result;

            }

            //去u8server获取需要的参数配置，然后添加打包任务
            CompletableFuture<CompletableResponse<Map<Integer, List<ChannelParamMeta>>>> future = channelService.getMultiChannelConfigs(appID, cids);

            if (future == null) {
                result.setResult(view.failure(Consts.Tips.FailMsg));
                return result;
            }

            future.whenComplete((response, err) -> {
                if (err != null) {
                    result.setResult(view.failure(err.getMessage()));
                    return;
                }
                if (response == null) {
                    result.setResult(view.failure(Consts.Tips.FailMsg));
                    return;
                }

                try{

                    channelService.addPackTasks(channels, response.data);
                    view.code = response.code;
                    view.reason = response.reason;
                    result.setResult(view);

                }catch (Exception e){
                    result.setResult(view.failure(Consts.Tips.FailMsg));
                    e.printStackTrace();
                }



            });

            return result;

        }catch (Exception e){
            e.printStackTrace();
            result.setResult(view.failure(Consts.Tips.FailMsg));
            return result;
        }
    }

    @RequestMapping(value = "/deletePackLog", method = RequestMethod.POST)
    @ResponseBody
    public ResponseView deletePackLog(Integer id){
        ResponseView view = new ResponseView();
        try{

            if(id == null || id <= 0){

                return view.failure(Consts.Tips.FailMsg);
            }

            PackLog log = channelService.getPackLogByID(id);
            if(log == null){

                return view.failure(Consts.Tips.PackLogNotExists);
            }

            if(packTaskManager.isPacking(id)){
                return view.failure(Consts.Tips.PackingNotDelete);
            }

            packTaskManager.deleteTask(id);

            channelService.deletePackLog(id);

            String apkPath  = FileUtils.joinPath(globalConfig.getFileTempPath(), "output/game" + log.getGameID()+"/"+log.getName()+log.getChannelLocalID()+"/"+id+".apk");

            if(FileUtils.fileExists(apkPath)){
                FileUtils.deleteFile(apkPath);
            }

            return view.success();

        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
            return view.failure(Consts.Tips.FailMsg);
        }

    }


    @RequestMapping(value = "/getCurrLogs", method = RequestMethod.GET)
    @ResponseBody
    public ListView<PackLog> getCurrLogs(Integer appID){

        ListView<PackLog> view = new ListView<>();
        try{

            if(appID == null){
                logger.error("getCurrLogs:appID can not be null");
                return view.buildFailure(Consts.Tips.GameNotExists);
            }

            List<PackLog> logs = packTaskManager.refreshPackLogsByGameID(appID);
            view.setData(logs);
            view.success();
            return view;


        }catch (Exception e){
            e.printStackTrace();
            return view.buildFailure(Consts.Tips.FailMsg);
        }

    }

    @RequestMapping(value = "/getHistoryLogs", method = RequestMethod.GET)
    @ResponseBody
    public PageView<PackLog> getHistoryLogs(@RequestParam(required = false) Integer appID,
                                            int draw,
                                            int start,
                                            int length){


        PageView<PackLog> view = new PageView<PackLog>();
        try{

            int totalSize = channelService.getPackLogCount(appID);
            List<PackLog> logs = channelService.getPackLogs(appID, start, length);

            view.fromList(logs, draw, totalSize, totalSize);

        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }


        return view;

    }

    @RequestMapping(value = "/getTestLogs", method = RequestMethod.GET)
    @ResponseBody
    public PageView<PackLog> getTestLogs(@RequestParam(required = false) Integer appID,
                                            int draw,
                                            int start,
                                            int length){


        PageView<PackLog> view = new PageView<PackLog>();
        try{

            int totalSize = channelService.getTestPackLogCount(appID);
            List<PackLog> logs = channelService.getTestPackLogs(appID, start, length);

            view.fromList(logs, draw, totalSize, totalSize);

        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }


        return view;

    }


    @RequestMapping(value = "/customIconPath", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> getCustomIconPath(@RequestParam(required = true) Integer gameID,
                                                               @RequestParam(required = true) Integer channelID,
                                                               HttpServletResponse response){

        try{

            String iconPath = FileUtils.joinPath(globalConfig.getFileTempPath(), "games/game"+gameID, "channels", channelID+"", "custom_icon/icon.png");

            if(!FileUtils.fileExists(iconPath)){
                logger.error("icon file not exists. {}", iconPath);
                return null;
            }

            return HttpUtils.generateDownloadFile(iconPath, "icon.png");

        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }


        return null;

    }


    @RequestMapping(value = "/customSplashPath", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> getCustomSplashPath(@RequestParam(required = true) Integer gameID,
                                                                 @RequestParam(required = true) Integer channelID,
                                                                 HttpServletResponse response){

        try{

            String splashPath = FileUtils.joinPath(globalConfig.getFileTempPath(), "games/game"+gameID, "channels", channelID+"", "custom_splash/u8_splash.png");

            if(!FileUtils.fileExists(splashPath)){
                logger.error("icon file not exists. {}", splashPath);
                return null;
            }

            return HttpUtils.generateDownloadFile(splashPath, "u8_splash.png");

        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }


        return null;

    }


    @RequestMapping(value = "/tempIconPath", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> getTempIconPath(@RequestParam(required = true) Integer gameID,
                                                           @RequestParam(required = true) Integer channelID,
                                                           @RequestParam(required = true) String name,
                                                           HttpServletResponse response){

        try{


            if(!FileUtils.isValidPathName(name)){
                logger.error("tempIconPath is not valid path:{}", name);
                return null;
            }

            String path = "games/game"+gameID+"/channels/"+channelID+"/temp_icons/"+name;
            path = FileUtils.joinPath(globalConfig.getFileTempPath(), path);

            if(!FileUtils.fileExists(path)){
                logger.error("icon file not exists. {}", path);
                return null;
            }

            return HttpUtils.generateDownloadFile(path, name);

        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }


        return null;

    }

    @RequestMapping(value = "/defaultSplashPath", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> getDefaultSplashPath(@RequestParam(required = true) String sdk,
                                                               @RequestParam(required = true) String type,
                                                               HttpServletResponse response){

        try{

            String path = "config/sdk/"+sdk+"/splash/"+type+"/temp_caches/u8_splash.png";
            path = FileUtils.joinPath(globalConfig.getFileTempPath(), path);

            if(!FileUtils.fileExists(path)){
                logger.error("splash file not exists. {}", path);
                return null;
            }


            //return HttpUtils.generateDownloadFile(path, "u8_splash.png");
            FileSystemResource file = new FileSystemResource(path);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", "u8_splash.png"));
            headers.add("Pragma", "no-cache");
            headers.add("Expires", "0");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentLength(file.contentLength())
                    .contentType(MediaType.IMAGE_PNG)
                    .body(new InputStreamResource(file.getInputStream()));

        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }


        return null;

    }


    @RequestMapping(value = "/downloadLog", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> downloadLog(@RequestParam(required = true) Integer packID, HttpServletResponse response){

        try{
//
//            PackLog log = channelService.getPackLogByID(packID);
//            if(log == null){
//                logger.error("packlog not exists for id:"+packID);
//                return;
//            }

            String fileName = "u8sdk-"+packID+".log";
            String logPath = "log/" + fileName;
            logPath = FileUtils.joinPath(globalConfig.getFileTempPath(), logPath);

            if(!FileUtils.fileExists(logPath)){
                logger.error("log file not exists. {}", logPath);
                return null;
            }

            return HttpUtils.generateDownloadFile(logPath, fileName);

        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }


        return null;

    }

    @RequestMapping(value = "/downloadApk", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> downloadApk(@RequestParam(required = true) Integer packID, HttpServletResponse response){

        try{
//
            PackLog log = channelService.getPackLogByID(packID);
            if(log == null){
                logger.error("packlog not exists for id:"+packID);
                return null;
            }

            String fileName = packID+".apk";
            String apkPath = "output/game" + log.getGameID()+"/"+log.getName()+log.getChannelLocalID()+"/"+fileName;
            apkPath = FileUtils.joinPath(globalConfig.getFileTempPath(), apkPath);

            if(!FileUtils.fileExists(apkPath)){
                logger.error("apk file not exists. {}", apkPath);
                return null;
            }

            String destFileName = log.getName()+"-"+log.getChannelID()+"-"+log.getId()+".apk";

            return HttpUtils.generateDownloadFile(apkPath, destFileName);

        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }


        return null;

    }


    //是否有测试用例
    @RequestMapping(value = "/hasTestCase", method = RequestMethod.POST)
    @ResponseBody
    public ResponseView hasTestCase(@RequestParam(required = true) Integer channelID, HttpServletResponse response){

        ResponseView view = new ResponseView();
        try{
            Channel channel = channelService.getChannelByID(channelID);
            if(channel == null){
                logger.error("channel not exists for id:"+channelID);
                return view.failure(Consts.Tips.ChannelNotExists);
            }


            String sdkPath = FileUtils.joinPath(globalConfig.getPackageBuildPath(), "config/sdk", channel.getSdk(), "attachment/testcase.xls");
            if(!FileUtils.fileExists(sdkPath)){

                return view.failure(Consts.Tips.TestCaseNotExists);
            }

            return view.success();

        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
            return view.failure(Consts.Tips.TestCaseNotExists);
        }

    }

    @RequestMapping(value = "/downloadTestCase", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> downloadTestCase(@RequestParam(required = true) Integer channelID, HttpServletResponse response){

        try{
            Channel channel = channelService.getChannelByID(channelID);
            if(channel == null){
                logger.error("channel not exists for id:"+channelID);
                return null;
            }

            String sdkPath = FileUtils.joinPath(globalConfig.getPackageBuildPath(), "config/sdk", channel.getSdk(), "attachment/testcase.xls");
            if(!FileUtils.fileExists(sdkPath)){
                logger.error("channel test case file not exists for sdk:"+channel.getSdk());
                return null;
            }

            return HttpUtils.generateDownloadFile(sdkPath, "testcase-"+channel.getSdk()+".xls");

        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }


        return null;

    }


    @RequestMapping(value = "/commitTest", method = RequestMethod.POST)
    @ResponseBody
    public ResponseView commitTest(@RequestParam(required = true) Integer packID){

        ResponseView view = new ResponseView();
        try{
//

            PackLog log = channelService.getPackLogByID(packID);
            if(log == null){
                logger.error("packlog not exists for id:"+packID);
                return view.failure(Consts.Tips.PackLogNotExists);
            }

            if(log.getState() != PackLog.STATE_SUCCESS){

                logger.error("packlog state not success for id:"+packID);
                return view.failure(Consts.Tips.PackNotSuccess);
            }

            log.setTestState(PackLog.TEST_STATE_COMMIT);
            channelService.updatePackLog(log);
            return view.success();

        }catch (Exception e){
            logger.error(e.getMessage());
            view.failure(Consts.Tips.FailMsg);
            e.printStackTrace();
        }


        return view;

    }

    @RequestMapping(value = "/saveFeedback", method = RequestMethod.POST)
    @ResponseBody
    public ResponseView saveFeedback(@RequestParam(required = true) Integer packID,
                                     Integer testResult, String feedback){

        ResponseView view = new ResponseView();
        try{
//

            PackLog log = channelService.getPackLogByID(packID);
            if(log == null){
                logger.error("packlog not exists for id:"+packID);
                return view.failure(Consts.Tips.PackLogNotExists);
            }

            if(testResult == null ){
                logger.error("packlog testResult not set. id:"+packID);
                return view.failure(Consts.Tips.FailMsg);
            }

            if(testResult == 1){
                log.setTestState(PackLog.TEST_STATE_SUCCESS);
            }else{
                log.setTestState(PackLog.TEST_STATE_FAILED);
                log.setTestFeed(feedback);
            }

            channelService.updatePackLog(log);
            return view.success();

        }catch (Exception e){
            logger.error(e.getMessage());
            view.failure(Consts.Tips.FailMsg);
            e.printStackTrace();
        }


        return view;

    }
}
