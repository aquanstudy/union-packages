package com.u8x.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.u8x.common.Consts;
import com.u8x.common.XLogger;
import com.u8x.controller.view.ChannelParamsView;
import com.u8x.controller.view.ChannelView;
import com.u8x.controller.view.CompletableResponse;
import com.u8x.controller.view.ListView;
import com.u8x.dao.ChannelDao;
import com.u8x.dao.GameDao;
import com.u8x.dao.config.GlobalConfig;
import com.u8x.dao.entity.*;
import com.u8x.sdk.LocalSDKManager;
import com.u8x.task.PackTaskManager;
import com.u8x.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Created by ant on 2017/10/11.
 */
@Service
@Transactional
public class ChannelService {
    private static final XLogger logger = XLogger.getLogger(ChannelService.class);

    @Autowired
    private ChannelDao channelDao;

    @Autowired
    private GameDao gameDao;

    @Autowired
    private PackTaskManager packTaskManager;

    @Autowired
    private GlobalConfig globalConfig;

    @Autowired
    private LocalSDKManager sdkManager;


    public List<Channel> getChannels(Integer gameID, String name, String channelName, Integer channelID){

        List<Channel> channels = channelDao.getChannels(gameID, name, channelName, channelID);

        if(channels != null){
            for(Channel channel : channels){
                ChannelMaster master = sdkManager.getMasterBySDK(channel.getSdk());
                if(master != null){
                    channel.setVersionName(master.getVersionName());
                }
            }
        }

        return channels;

    }

    public List<ChannelMaster> getAllLocalMasters(){

        return sdkManager.getLocalSDKMasters();
    }


    //获取指定渠道商的参数meta信息
    public CompletableFuture<List<ChannelParamMeta>> getChannelParamMetas(String masterSdkName){

        CompletableFuture<List<ChannelParamMeta>> future = new CompletableFuture<>();

        try{

            String time = System.currentTimeMillis()+"";
            StringBuilder sb = new StringBuilder();
            sb.append("apiID=").append(globalConfig.apiID)
                    .append("&masterSdkName=").append(masterSdkName).append("&time=").append(time).append(globalConfig.apiKey);

            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();

            Map<String,String> params = new HashMap<>();
            params.put("apiID", globalConfig.apiID);
            params.put("masterSdkName", masterSdkName);
            params.put("time", time);
            params.put("sign",sign);

            HttpUtils.post(globalConfig.paramMetaUrl(), params, new HttpUtils.IHttpCallback() {
                @Override
                public void completed(String data) {
                    logger.debug("post result:{}", data);
                    try {
                        JSONObject jsonResult = JSON.parseObject(data);
                        if (jsonResult == null || jsonResult.getIntValue("code") == 0) {
                            if(jsonResult != null){
                                logger.error("get channel param metas from u8server manager failed. state code:{};msg:{}", jsonResult.getIntValue("code"), jsonResult.getString("msg"));
                            }

                            future.complete(null);
                            return;
                        }

                        List<ChannelParamMeta> metas = null;
                        JSONArray jsonArray = jsonResult.getJSONArray("data");
                        if (jsonArray != null && jsonArray.size() > 0) {
                            metas = JSON.parseArray(jsonArray.toString(), ChannelParamMeta.class);
                            logger.debug("get channel param metas from u8server manager success. localize {} channel metes.", metas.size());
                        } else {
                            logger.debug("get channel param metas from u8server manager success. there is no channel masters.");
                        }


                        future.complete(metas);

                    } catch (Exception e) {
                        future.complete(null);
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }

                }

                @Override
                public void failed(String msg) {
                    logger.debug("post result failed:{}", msg);
                    future.complete(null);
                }
            });


        }catch (Exception e){
            future.completeExceptionally(e);
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return future;

    }

    //保存渠道商的参数meta信息
    public CompletableFuture<Boolean> saveChannelParamMeta(String masterSdkName, ChannelParamMeta meta){

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try{

            String time = System.currentTimeMillis()+"";

            StringBuilder sb = new StringBuilder();
            sb.append("apiID=" + globalConfig.apiID);

            if(!StringUtils.isEmpty(meta.getDefaultVal())){
                sb.append("&defaultVal="+meta.getDefaultVal());
            }

            sb.append("&masterSdkName="+masterSdkName);

            if(!StringUtils.isEmpty(meta.getMetaDesc())){
                sb.append("&metaDesc="+meta.getMetaDesc());
            }

            if(meta.getId() == null){
                meta.setId(0);
            }
            sb.append("&metaID="+meta.getId());
            sb.append("&paramKey="+meta.getParamKey())
                    .append("&paramStyle="+meta.getParamStyle())
                    .append("&paramType="+meta.getParamType())
                    .append("&pos="+meta.getPos())
                    .append("&showName="+meta.getShowName());

            if(!StringUtils.isEmpty(meta.getStyleExtra())){
                sb.append("&styleExtra="+meta.getStyleExtra());
            }

            sb.append("&time=" + time).append(globalConfig.apiKey);

            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();

            Map<String,String> params = new HashMap<>();
            params.put("apiID", globalConfig.apiID);
            params.put("masterSdkName", masterSdkName);
            params.put("metaID", meta.getId()+"");
            params.put("paramKey", meta.getParamKey());
            params.put("showName", meta.getShowName());
            params.put("metaDesc", meta.getMetaDesc());
            params.put("defaultVal", meta.getDefaultVal());
            params.put("pos", meta.getPos()+"");
            params.put("paramType", meta.getParamType()+"");
            params.put("paramStyle", meta.getParamStyle()+"");
            params.put("styleExtra", StringUtils.isEmpty(meta.getStyleExtra()) ? "" : meta.getStyleExtra());
            params.put("time", time);
            params.put("sign",sign);

            HttpUtils.post(globalConfig.paramMetaSaveUrl(), params, new HttpUtils.IHttpCallback() {
                @Override
                public void completed(String data) {
                    logger.debug("post result:{}", data);
                    try {
                        JSONObject jsonResult = JSON.parseObject(data);
                        if (jsonResult == null || jsonResult.getIntValue("code") == 0) {
                            if(jsonResult != null){
                                logger.error("save channel param meta from u8server manager failed. state code:{};msg:{}", jsonResult.getIntValue("code"), jsonResult.getString("msg"));
                            }

                            future.complete(false);
                            return;
                        }

                        future.complete(true);

                    } catch (Exception e) {
                        future.complete(false);
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }

                }

                @Override
                public void failed(String msg) {
                    logger.debug("post result failed:{}", msg);
                    future.complete(false);
                }
            });


        }catch (Exception e){
            future.completeExceptionally(e);
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return future;

    }

    //删除渠道商的参数meta信息
    public CompletableFuture<Boolean> deleteChannelParamMeta(int metaID){

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try{

            String time = System.currentTimeMillis()+"";

            StringBuilder sb = new StringBuilder();
            sb.append("apiID=" + globalConfig.apiID);
            sb.append("&metaID="+metaID);
            sb.append("&time=" + time).append(globalConfig.apiKey);

            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();

            Map<String,String> params = new HashMap<>();
            params.put("apiID", globalConfig.apiID);
            params.put("metaID", metaID+"");
            params.put("time", time);
            params.put("sign",sign);

            HttpUtils.post(globalConfig.paramMetaDeleteUrl(), params, new HttpUtils.IHttpCallback() {
                @Override
                public void completed(String data) {
                    logger.debug("post result:{}", data);
                    try {
                        JSONObject jsonResult = JSON.parseObject(data);
                        if (jsonResult == null || jsonResult.getIntValue("code") == 0) {
                            if(jsonResult != null){
                                logger.error("delete channel param meta from u8server manager failed. state code:{};msg:{}", jsonResult.getIntValue("code"), jsonResult.getString("msg"));
                            }

                            future.complete(false);
                            return;
                        }

                        future.complete(true);

                    } catch (Exception e) {
                        future.complete(false);
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }

                }

                @Override
                public void failed(String msg) {
                    logger.debug("post result failed:{}", msg);
                    future.complete(false);
                }
            });


        }catch (Exception e){
            future.completeExceptionally(e);
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return future;

    }



    //从u8server同步渠道参数配置
    public CompletableFuture<ChannelView> getChannelFullConfig(Channel channel, ChannelMaster master){

        CompletableFuture<ChannelView> future = new CompletableFuture<>();
        ChannelView response = new ChannelView();
        response.setChannel(channel);
        response.setMaster(master);



        try{



            String iconPath = FileUtils.joinPath(globalConfig.getFileTempPath(), "games/game"+channel.getGameID(), "channels", channel.getId()+"", "custom_icon/icon.png");
            if(FileUtils.fileExists(iconPath)){
                channel.setHasCustomIcon(true);
            }else{
                channel.setHasCustomIcon(false);
            }

            String splashPath = FileUtils.joinPath(globalConfig.getFileTempPath(), "games/game"+channel.getGameID(), "channels", channel.getId()+"", "custom_splash/u8_splash.png");
            if(FileUtils.fileExists(splashPath)){
                channel.setHasCustomSplash(true);
            }else{
                channel.setHasCustomSplash(false);
            }


            Game game = gameDao.getGameById(channel.getGameID());
            if(game != null){
                response.setGame(game);
            }

            if(channel.getIsLocal() == 1){
                //本地配置
                String localConfigStr = channel.getLocalConfig();
                List<ChannelParamMeta> metas = JSON.parseArray(localConfigStr, ChannelParamMeta.class);

                if(metas != null && metas.size() > 0){
                    response.setMetas(metas);
                }
                response.success();
                future.complete(response);

                return future;
            }



            String time = System.currentTimeMillis()+"";
            StringBuilder sb = new StringBuilder();
            sb.append("apiID=").append(globalConfig.apiID)
                    .append("&appID=").append(channel.getGameID())
                    .append("&channelID=").append(channel.getChannelID()).append("&time=").append(time).append(globalConfig.apiKey);

            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();

            Map<String,String> params = new HashMap<>();
            params.put("apiID", globalConfig.apiID);
            params.put("appID", channel.getGameID()+"");
            params.put("channelID", channel.getChannelID()+"");
            params.put("time", time);
            params.put("sign",sign);

            HttpUtils.post(globalConfig.channelFullConfigUrl(), params, new HttpUtils.IHttpCallback() {
                @Override
                public void completed(String data) {
                    logger.debug("post result:{}", data);
                    try {
                        JSONObject jsonResult = JSON.parseObject(data);
                        if (jsonResult == null || jsonResult.getIntValue("code") == 0) {
                            if(jsonResult != null){
                                response.failure(jsonResult.getString("msg"));
                                logger.error("get channel full config from u8server manager failed. state code:{};msg:{}", jsonResult.getIntValue("code"), jsonResult.getString("msg"));
                            }else{
                                response.failure(Consts.Tips.FailMsg);
                            }

                            future.complete(response);
                            return;
                        }


                        JSONObject fullData = jsonResult.getJSONObject("data");
                        if(fullData != null){
                            JSONArray metaArray = fullData.getJSONArray("metas");
                            if(metaArray != null && metaArray.size() > 0){
                                List<ChannelParamMeta> metas = JSON.parseArray(metaArray.toString(), ChannelParamMeta.class);
                                response.setMetas(metas);
                            }else{
                                response.setMetas(new ArrayList<>());
                            }

                            JSONArray productArray = fullData.getJSONArray("products");
                            if(productArray != null && productArray.size() > 0){
                                List<ChannelProduct> products = JSON.parseArray(productArray.toString(), ChannelProduct.class);
                                response.setProducts(products);
                            }else{
                                response.setProducts(new ArrayList<>());
                            }
                        }
                        response.success();
                        future.complete(response);

                    } catch (Exception e) {
                        future.complete(null);
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }

                }

                @Override
                public void failed(String msg) {
                    logger.debug("post result failed:{}", msg);
                    future.complete(null);
                }
            });


        }catch (Exception e){
            future.completeExceptionally(e);
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return future;

    }


    //从u8server同步渠道参数配置
    public CompletableFuture<CompletableResponse<List<ChannelParamMeta>>> getChannelConfig(String appID, Integer channelID){

        CompletableFuture<CompletableResponse<List<ChannelParamMeta>>> future = new CompletableFuture<>();
        CompletableResponse<List<ChannelParamMeta>> response = new CompletableResponse<>();
        try{

            String time = System.currentTimeMillis()+"";
            StringBuilder sb = new StringBuilder();
            sb.append("apiID=").append(globalConfig.apiID)
                    .append("&appID=").append(appID)
                    .append("&channelID=").append(channelID).append("&time=").append(time).append(globalConfig.apiKey);

            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();

            Map<String,String> params = new HashMap<>();
            params.put("apiID", globalConfig.apiID);
            params.put("appID", appID);
            params.put("channelID", channelID+"");
            params.put("time", time);
            params.put("sign",sign);

            HttpUtils.post(globalConfig.channelconfigUrl(), params, new HttpUtils.IHttpCallback() {
                @Override
                public void completed(String data) {
                    logger.debug("post result:{}", data);
                    try {
                        JSONObject jsonResult = JSON.parseObject(data);
                        if (jsonResult == null || jsonResult.getIntValue("code") == 0) {
                            if(jsonResult != null){
                                response.failure(jsonResult.getString("msg"));
                                logger.error("get channel config from u8server manager failed. state code:{};msg:{}", jsonResult.getIntValue("code"), jsonResult.getString("msg"));
                            }else{
                                response.failure(Consts.Tips.FailMsg);
                            }

                            future.complete(response);
                            return;
                        }

                        List<ChannelParamMeta> metas = null;
                        JSONArray jsonArray = jsonResult.getJSONArray("data");
                        if (jsonArray != null && jsonArray.size() > 0) {
                            metas = JSON.parseArray(jsonArray.toString(), ChannelParamMeta.class);
                            response.success(metas);
                            logger.debug("get channel config from u8server manager success. localize {} channel metes.", metas.size());
                        } else {
                            response.failure(Consts.Tips.MetaNotConfig);
                            logger.debug("get channel config from u8server manager success. there is no channel masters.");
                        }


                        future.complete(response);

                    } catch (Exception e) {
                        future.complete(null);
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }

                }

                @Override
                public void failed(String msg) {
                    logger.debug("post result failed:{}", msg);
                    future.complete(null);
                }
            });


        }catch (Exception e){
            future.completeExceptionally(e);
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return future;

    }

    //从u8server同步多个渠道参数配置
    public CompletableFuture<CompletableResponse<Map<Integer,List<ChannelParamMeta>>>> getMultiChannelConfigs(Integer appID, String channelIDs){

        CompletableFuture<CompletableResponse<Map<Integer,List<ChannelParamMeta>>>> future = new CompletableFuture<>();

        try{
            CompletableResponse<Map<Integer,List<ChannelParamMeta>>> response = new CompletableResponse<>();
            String time = System.currentTimeMillis()+"";
            StringBuilder sb = new StringBuilder();
            sb.append("apiID=").append(globalConfig.apiID)
                    .append("&appID=").append(appID)
                    .append("&channelIDs=").append(channelIDs).append("&time=").append(time).append(globalConfig.apiKey);

            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();

            Map<String,String> params = new HashMap<>();
            params.put("apiID", globalConfig.apiID);
            params.put("appID", appID+"");
            params.put("channelIDs", channelIDs);
            params.put("time", time);
            params.put("sign",sign);

            HttpUtils.post(globalConfig.multichannelconfigUrl(), params, new HttpUtils.IHttpCallback() {
                @Override
                public void completed(String data) {
                    logger.debug("post result:{}", data);
                    try {
                        JSONObject jsonResult = JSON.parseObject(data);
                        if (jsonResult.getIntValue("code") == 0) {
                            logger.error("get channel config from u8server manager failed. state code:{};msg:{}", jsonResult.getIntValue("code"), jsonResult.getString("msg"));
                            future.complete(response.failure(jsonResult.getString("msg")));
                            return;
                        }

                        if(jsonResult.getIntValue("code") == 2){
                            response.code = Consts.RespCode.SUCWITHWARN;
                        }else{
                            response.code = Consts.RespCode.SUCCESS;
                        }

                        Map<Integer, List<ChannelParamMeta>> result = new HashMap<Integer, List<ChannelParamMeta>>();
                        JSONArray jsonArray = jsonResult.getJSONArray("data");
                        if (jsonArray != null && jsonArray.size() > 0) {
                            for(int i=0; i<jsonArray.size(); i++){
                                JSONObject json = jsonArray.getJSONObject(i);
                                if(json.containsKey("channelID")){
                                    Integer channelID = json.getInteger("channelID");
                                    if(json.containsKey("metas")){
                                        List<ChannelParamMeta> metas = JSON.parseArray(json.getJSONArray("metas").toString(), ChannelParamMeta.class);
                                        result.put(channelID, metas);
                                    }
                                }
                            }

                            logger.debug("get channel config from u8server manager success.");
                        } else {
                            logger.debug("get channel config from u8server manager success. fetch zero record.");
                        }

                        response.data = result;
                        response.reason = jsonResult.getString("msg");

                        future.complete(response);

                    } catch (Exception e) {
                        future.complete(null);
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }

                }

                @Override
                public void failed(String msg) {
                    logger.debug("post result failed:{}", msg);
                    future.complete(null);
                }
            });


        }catch (Exception e){
            future.completeExceptionally(e);
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return future;

    }

    //同步渠道参数配置到u8server
    public CompletableFuture<Boolean> saveChannelConfig(String appID, Integer channelID, String channelParams, String products){

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        try{

            String time = System.currentTimeMillis()+"";
            StringBuilder sb = new StringBuilder();
            sb.append("apiID=").append(globalConfig.apiID)
                    .append("&appID=").append(appID)
                    .append("&channelID=").append(channelID)
                    .append("&channelParams=").append(channelParams)
                    .append("&products=").append(StringUtils.isEmpty(products) ? "" : products)
                    .append("&time=").append(time).append(globalConfig.apiKey);

            String sign = EncryptUtils.md5(sb.toString()).toLowerCase();

            logger.debug("sign str:{};sign:{}", sb.toString(), sign);

            Map<String,String> params = new HashMap<>();
            params.put("apiID", globalConfig.apiID);
            params.put("appID", appID);
            params.put("channelID", channelID+"");
            params.put("channelParams", channelParams);
            params.put("products", StringUtils.isEmpty(products) ? "" : products);
            params.put("time", time);
            params.put("sign",sign);

            HttpUtils.post(globalConfig.channelSaveUrl(), params, new HttpUtils.IHttpCallback() {
                @Override
                public void completed(String data) {
                    logger.debug("post result:{}", data);
                    try {
                        JSONObject jsonResult = JSON.parseObject(data);
                        if ( jsonResult.getIntValue("code") == 0) {
                            logger.error("save channel config to u8server manager failed. state code:{};msg:{}", jsonResult.getIntValue("code"), jsonResult.getString("msg"));
                            future.complete(null);
                            return;
                        }

                        future.complete(true);

                    } catch (Exception e) {
                        future.complete(false);
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }

                }

                @Override
                public void failed(String msg) {
                    logger.debug("post result failed:{}", msg);
                    future.complete(false);
                }
            });


        }catch (Exception e){
            future.completeExceptionally(e);
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return future;

    }

    public void deletePluginByID(int id){
        channelDao.deleteChannelPlugin(id);
    }

    public void deleteInvalidPluginsByGameID(int gameID){
        channelDao.deleteInvalidPluginsByGameID(gameID);
    }

    public void insertChannelPlugin(ChannelPlugin plugin){
        channelDao.insertChannelPlugin(plugin);
    }

    public void updateChannelPlugin(ChannelPlugin plugin){
        channelDao.updateChannelPlugin(plugin);
    }

    public List<ChannelPlugin> getPluginsByChannel(Integer channelID){

        return channelDao.getChannelPlugins(channelID);
    }


    public Map<String, ChannelPlugin> getPluginsByChannelWithGame(Integer gameID, Integer channelID){
        List<ChannelPlugin> channelPlugins = channelDao.getChannelPlugins(channelID);

        List<GamePlugin> gamePlugins = gameDao.getPluginsByGame(gameID);        //游戏级别的插件

        Map<String, ChannelPlugin> result = new HashMap<>();
        if(channelPlugins != null){
            for(ChannelPlugin plugin : channelPlugins){
                result.put(plugin.getSdkName(), plugin);
            }
        }

        if(gamePlugins != null){
            for(GamePlugin plugin : gamePlugins){

                if(result.containsKey(plugin.getSdkName())){

                    continue;
                }

                ChannelPlugin cp = new ChannelPlugin();
                cp.setSdkName(plugin.getSdkName());
                cp.setGameID(plugin.getGameID());
                cp.setParams(plugin.getParams());
                cp.setState(1);
                cp.setExtend(0);
                result.put(plugin.getSdkName(), cp);
            }


        }

        return result;

    }

    public List<ChannelPlugin> getPluginsByChannelWithMaster(Integer gameID, Integer channelID){

        try{

            List<ChannelPlugin> channelPlugins = channelDao.getChannelPlugins(channelID);

            Map<String, ChannelPlugin> result = new LinkedHashMap<>();
            Map<String, ChannelPlugin> forbidden = new HashMap<>();
            if(channelPlugins != null){

                for(ChannelPlugin plugin : channelPlugins){

                    if(plugin.getState() == 0){

                        forbidden.put(plugin.getSdkName(), plugin);
                        continue;
                    }

                    PluginMaster master = sdkManager.getPluginBySDK(plugin.getSdkName());
                    if(master != null){
                        plugin.setMaster(master);
                        result.put(plugin.getSdkName(), plugin);

                        String jsonParams = plugin.getParams();
                        if(!StringUtils.isEmpty(jsonParams)){
                            Map<String, String> params = JSONObject.parseObject(jsonParams, new TypeReference<Map<String, String>>(){});
                            if(params == null){
                                continue;
                            }
                            if(master.getMetas() != null){
                                for(PluginParamMeta m : master.getMetas()){
                                    if(params.containsKey(m.getParamKey())){
                                        m.setDefaultVal(params.get(m.getParamKey()));
                                    }
                                }
                            }
                        }

                    }
                }

            }

            List<GamePlugin> gamePlugins = gameDao.getPluginsByGame(gameID);        //游戏级别的插件

            if(gamePlugins != null){
                for(GamePlugin plugin : gamePlugins){

                    if(result.containsKey(plugin.getSdkName()) || forbidden.containsKey(plugin.getSdkName())){

                        continue;
                    }

                    ChannelPlugin cp = new ChannelPlugin();
                    cp.setSdkName(plugin.getSdkName());
                    cp.setGameID(plugin.getGameID());
                    cp.setParams(plugin.getParams());
                    cp.setState(1);
                    cp.setExtend(0);

                    PluginMaster master = sdkManager.getPluginBySDK(plugin.getSdkName());
                    if(master != null){
                        cp.setMaster(master);
                        result.put(plugin.getSdkName(), cp);

                        String jsonParams = plugin.getParams();
                        if(!StringUtils.isEmpty(jsonParams)){
                            Map<String, String> params = JSONObject.parseObject(jsonParams, new TypeReference<Map<String, String>>(){});
                            if(params == null){
                                continue;
                            }
                            if(master.getMetas() != null){
                                for(PluginParamMeta m : master.getMetas()){
                                    if(params.containsKey(m.getParamKey())){
                                        m.setDefaultVal(params.get(m.getParamKey()));
                                    }
                                }
                            }
                        }

                    }
                }


            }

            List<ChannelPlugin> lst = new ArrayList<ChannelPlugin>(result.values());
            return lst;
        }catch (Exception e){
            logger.error("get plugins failed.", e);
            e.printStackTrace();
        }

        return null;

    }

    public void updateChannelPlugins(Integer channelID, String channelPluginJsonArray){

        if(StringUtils.isEmpty(channelPluginJsonArray)) return;

        try{

            List<ChannelPlugin> plugins = getPluginsByChannel(channelID);

            JSONArray jsonArray = JSON.parseArray(channelPluginJsonArray);
            if(jsonArray != null && jsonArray.size() > 0){
                for(int i=0; i<jsonArray.size(); i++){
                    JSONObject json = jsonArray.getJSONObject(i);
                    String sdkName = json.getString("sdkName");
                    for(ChannelPlugin currP : plugins){
                        if(currP.getSdkName().equals(sdkName)){
                            JSONObject params = json.getJSONObject("params");
                            if(params != null){
                                currP.setParams(params.toString());
                                updateChannelPlugin(currP);
                            }

                        }
                    }
                }
            }


        }catch (Exception e){
            e.printStackTrace();
        }

    }


    //添加单个打包任务
    public void addPackTask(Channel c, List<ChannelParamMeta> metas){

        JSONObject json = new JSONObject();

        if(metas != null){
            for(ChannelParamMeta meta : metas){
                JSONObject item = new JSONObject();
                item.put("value", meta.getDefaultVal());
                switch(meta.getPos()){
                    case ChannelParamMeta.POS_NONE:
                        item.put("toMetaData", "0");
                        item.put("toConfig", "0");
                        break;
                    case ChannelParamMeta.POS_CONFIG:
                        item.put("toMetaData", "0");
                        item.put("toConfig", "1");
                        break;
                    case ChannelParamMeta.POS_METADATA:
                        item.put("toMetaData", "1");
                        item.put("toConfig", "0");
                        break;
                    case ChannelParamMeta.POS_BOTH:
                        item.put("toMetaData", "1");
                        item.put("toConfig", "1");
                        break;
                }

                json.put(meta.getParamKey(), item);
            }
        }

        String channelParams = json.toString();

        PackLog log = new PackLog();
        log.setName(c.getName());
        log.setChannelID(c.getChannelID());
        log.setChannelLocalID(c.getId());
        log.setChannelName(c.getChannelName());
        log.setGameID(c.getGameID());
        log.setFileName("");
        log.setState(PackLog.STATE_WAIT);
        log.setTestState(PackLog.TEST_STATE_WAIT);
        log.setTestFeed("");
        log.setChannelParams(channelParams);
        log.setCreateDate(DateUtil.getDateTime());

        insertPackLog(log);
        logger.debug("add a new package task. id:{}; channelID:{}, channel params:{}", log.getId(), c.getChannelID(), channelParams);
        packTaskManager.addPackTask(c, log);
    }

    //添加多个打包任务
    public void addPackTasks(List<Channel> channels, Map<Integer, List<ChannelParamMeta>> remoteParams){

        for(Channel c : channels){

            if(c.getIsLocal() == 1){
                //本地配置
                List<ChannelParamMeta> metas = JSON.parseArray(c.getLocalConfig(), ChannelParamMeta.class);
                addPackTask(c, metas);
            }else{
                if(remoteParams != null && remoteParams.containsKey(c.getChannelID())){
                    List<ChannelParamMeta> metas = remoteParams.get(c.getChannelID());
                    addPackTask(c, metas);
                }else{
                    addPackTask(c, null);
                }
            }

        }
    }

    public List<PackLog> getPackLogs(int gameID, int startPos, int pageSize){

        return channelDao.getPackLogs(gameID+"", startPos, pageSize);
    }

    public int getPackLogCount(int gameID){
        return channelDao.getPackLogCount(gameID+"");
    }

    public List<PackLog> getTestPackLogs(int gameID, int startPos, int pageSize){

        return channelDao.getTestPackLogs(gameID+"", startPos, pageSize);
    }

    public int getTestPackLogCount(int gameID){
        return channelDao.getTestPackLogCount(gameID+"");
    }

    public void deletePackLog(int id){
        channelDao.deletePackLog(id);
    }

    public void insertPackLog(PackLog log){
        channelDao.insertPackLog(log);
    }

    public void updatePackLog(PackLog log){
        channelDao.updatePackLog(log);
    }

    public PackLog getPackLogByID(int id){
        return channelDao.getPackLogByID(id);
    }

    public Channel getChannelByID(Integer id){
        return channelDao.getChannelByID(id);
    }

    public List<Channel> getChannelsByIDs(String ids){
        if(StringUtils.isEmpty(ids)) return null;
        return channelDao.getChannelsByIDs(ids);
    }

    public Channel getChannelByName(String name){
        return channelDao.getChannelByName(name);
    }

    public List<Channel> getAllChannels(){

        return channelDao.getAllChannels();
    }

    public void createChannel(Channel channel){

        channelDao.createChannel(channel);
    }

    public void updateChannel(Channel channel){
        channelDao.updateChannel(channel);
    }

    public void deleteChannel(Integer id){
        channelDao.deleteChannel(id);
    }

    public void deleteGameChannel(int id){
        channelDao.deleteGameChannel(id);
    }
}
