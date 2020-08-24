package com.u8x.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.u8x.controller.view.ObjectView;
import com.u8x.controller.view.PageView;
import com.u8x.dao.config.GlobalConfig;
import com.u8x.dao.entity.*;
import com.u8x.sdk.LocalSDKManager;
import com.u8x.services.AdminService;
import com.u8x.services.ChannelService;
import com.u8x.utils.FileUtils;
import com.u8x.utils.HttpUtils;
import com.u8x.utils.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.u8x.common.Consts;
import com.u8x.common.XLogger;
import com.u8x.controller.view.ListView;
import com.u8x.controller.view.ResponseView;
import com.u8x.services.GameService;
import com.u8x.utils.U8XUtils;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/game")
public class GameController {
	private static final XLogger logger = XLogger.getLogger(GameController.class);

	@Autowired
	private GameService gameService;

    @Autowired
    private ChannelService channelService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private GlobalConfig globalConfig;

    @Autowired
    private LocalSDKManager sdkManager;


    @RequestMapping(value = "/getAllLocalGames", method = RequestMethod.GET)
    @ResponseBody
    ListView<Game>  getAllLocalGames() {
        ListView<Game> result = new ListView<>();

        try{
            List<Game> games = gameService.getAllLocalGames();
            result.setData(games);
            return result;

        }catch (Exception e){
            ListView.buildFailure(Consts.Tips.FailMsg);
            logger.error(e.getMessage());
            e.printStackTrace();
        }


        return result;
    }


    @RequestMapping(value = "/getGameByID", method = RequestMethod.POST)
    @ResponseBody
    ObjectView<Game>  getGameByID(Integer gameID) {
        ObjectView<Game> result = new ObjectView<>();

        try{
            Game game = gameService.getGameById(gameID);

            if(game == null){
                result.failure(Consts.Tips.GameNotExists);
                return result;
            }

            result.setObject(game);
            return result;

        }catch (Exception e){
            result.failure(Consts.Tips.FailMsg);
            logger.error(e.getMessage());
            e.printStackTrace();
        }


        return result;
    }



    @RequestMapping(value = "/getApksByGame", method = RequestMethod.GET)
    @ResponseBody
    public ListView<ApkMeta> getApksByGame(Integer gameID){

        try{

            ListView<ApkMeta> view = new ListView<>();
            view.setData(gameService.getApkMetasByGameID(gameID));
            return view;


        }catch (Exception e){

            logger.error(e.getMessage());
            e.printStackTrace();
            return ListView.buildFailure(Consts.Tips.FailMsg);
        }

    }




    @RequestMapping(value = "/deleteApk", method = RequestMethod.POST)
    @ResponseBody
    public ResponseView deleteApkByID(Integer id){

        try{

            ResponseView view = new ResponseView();

            ApkMeta meta = gameService.getApkMetaByID(id);
            if(meta == null){
                logger.error("apkmeta not exists of id:"+id);
                return view.failure(Consts.Tips.FailMsg);
            }

            String apkPath = FileUtils.joinPath(globalConfig.getFileTempPath(), meta.getApkPath());
            FileUtils.deleteFile(apkPath);
            gameService.deleteApkMeta(id);

            Game game = gameService.getGameById(meta.getGameID());
            if(game != null){
                if(!StringUtils.isEmpty(meta.getApkPath()) && meta.getApkPath().equals(game.getApkPath())){
                    //当前删除的apk是母包
                    game.setApkPath("");
                    gameService.updateGame(game);
                }
            }

            return view.success();


        }catch (Exception e){

            logger.error(e.getMessage());
            e.printStackTrace();
            return ListView.buildFailure(Consts.Tips.FailMsg);
        }

    }


    @RequestMapping(value = "/getAllPlugins", method = RequestMethod.GET)
    @ResponseBody
    public ListView<PluginMaster> getAllPlugins(Integer gameID){

        try{

            List<PluginMaster> masters = sdkManager.getLocalPlugins();
            List<GamePlugin> plugins = gameService.getPluginsByGame(gameID);

            if(plugins != null && plugins.size() > 0){
                for(PluginMaster master : masters){
                    boolean exists = false;
                    for(GamePlugin plugin : plugins){
                        if(master.getSdkName().equals(plugin.getSdkName())){
                            exists = true;
                        }
                    }

                    if(exists){
                        master.setState(1);
                    }else{
                        master.setState(0);
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


    @RequestMapping(value = "/getGamePlugins", method = RequestMethod.POST)
    @ResponseBody
    public ListView<GamePlugin> getGamePlugins(Integer gameID){

        try{

            List<GamePlugin> plugins = gameService.getPluginsByGameWithMaster(gameID);

            ListView<GamePlugin> view = new ListView<>();
            view.setData(plugins);
            return view;


        }catch (Exception e){

            logger.error(e.getMessage());
            e.printStackTrace();
            return ListView.buildFailure(Consts.Tips.FailMsg);
        }

    }

    @RequestMapping(value = "/savePluginFiles", method = RequestMethod.POST)
    @ResponseBody
    public ResponseView savePluginFiles(Integer gameID,
                                         String fileIds,
                                         @RequestParam(value = "file", required = false) MultipartFile[] files){


        ResponseView view = new ResponseView();
        try{

            Game exists = gameService.getGameById(gameID);
            if(exists == null){

                return view.failure(Consts.Tips.GameNotExists);
            }

            if(files != null && files.length > 0){

                String basePath = FileUtils.joinPath(globalConfig.getFileTempPath(), "games/game"+exists.getId(), "res");

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


    @RequestMapping(value = "/tooglePlugin", method = RequestMethod.POST)
    @ResponseBody
    ResponseView tooglePlugin(Integer gameID, String sdkName, Integer checked){
        ResponseView view = new ResponseView();
        try{

            Game game = gameService.getGameById(gameID);
            if(game == null){
                view.failure(Consts.Tips.GameNotExists);
                return view;
            }

            if(StringUtils.isEmpty(sdkName)){
                logger.error("sdkName is empty");
                view.failure(Consts.Tips.FailMsg);
                return view;
            }


            List<GamePlugin> plugins = gameService.getPluginsByGame(gameID);

            boolean exists = false;
            if(plugins != null){

                for(GamePlugin plugin : plugins){
                    if(plugin.getSdkName().equals(sdkName)){
                        exists = true;
                        break;

                    }
                }
            }

            if(checked == 1){
                if(!exists){
                    GamePlugin plugin = new GamePlugin();
                    plugin.setGameID(gameID);
                    plugin.setSdkName(sdkName);
                    gameService.insertGamePlugin(plugin);
                }
            }else{
                PluginMaster master = sdkManager.getPluginBySDK(sdkName);
                if(master != null){
                    master.setState(0);
                }
                if(exists){
                    gameService.deleteGamePlugin(gameID, sdkName);
                    channelService.deleteInvalidPluginsByGameID(gameID);
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return view.success();
    }

    @RequestMapping(value = "/getAllGames2", method = RequestMethod.GET)
    @ResponseBody
    PageView<Game>  getAllGames2(String gameName,
                                 @RequestParam(value = "draw", required = false) Integer draw,
                                 @RequestParam(value = "start", required = false) Integer start,
                                 @RequestParam(value = "length", required = false) Integer length) {
        PageView<Game> result = new PageView<>();

        try{

            if(draw == null || start == null || length == null){
                draw = 1;
                start = 0;
                length = 10;
            }

            int count = gameService.getAllGameCount(gameName);
            List<Game> games = gameService.getAllGames(gameName, start, length);

            result.fromList(games, draw, count, count);



        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }


        return result;
    }


	@RequestMapping(value = "/getAllGames", method = RequestMethod.GET)
	@ResponseBody
    DeferredResult<ListView<Game>>  getAllGames(String gameName) {
        DeferredResult<ListView<Game>> result = new DeferredResult<>(30 * 60 * 1000L);

        try{

            CompletableFuture<List<Game>> future = gameService.getAllGames();

            if (future == null) {
                result.setResult(ListView.buildFailure(Consts.Tips.FailMsg));
                return result;
            }

            future.whenComplete((lst, err) -> {
                if (err != null) {
                    result.setResult(ListView.buildFailure(err.getMessage()));
                    return;
                }
                if (lst == null) {
                    result.setResult(ListView.buildFailure(Consts.Tips.FailMsg));
                    return;
                }
                ListView<Game> view = new ListView<Game>();

                if(StringUtils.isEmpty(gameName)){
                    view.setData(lst);
                }else{
                    List<Game> matched = lst.stream().filter((Game g) -> g.getName().contains(gameName)).collect(Collectors.toList());
                    view.setData(matched);
                }

                result.setResult(view);

            });
        }catch (Exception e){
            result.setResult(ListView.buildFailure(Consts.Tips.FailMsg));
            logger.error(e.getMessage());
            e.printStackTrace();
        }


		return result;
	}


    @RequestMapping(value = "/getAllPermittedGames", method = RequestMethod.GET)
    @ResponseBody
    DeferredResult<ListView<Game>> getAllPermittedGames(String gameName) {
        DeferredResult<ListView<Game>> result = new DeferredResult<>(30 * 60 * 1000L);

        try{

            Admin user = (Admin) SecurityUtils.getSubject().getPrincipal();
            if(user == null){
                result.setResult(ListView.buildFailure(Consts.Tips.SessionInvalid));
                return result;
            }

            AdminRole role = adminService.getAdminRoleById(user.getAdminRoleID());
            if(role == null){
                logger.error("admin role not exists for admin:"+user.getUsername());
                result.setResult(ListView.buildFailure(Consts.Tips.FailMsg));
                return result;
            }



            CompletableFuture<List<Game>> future = gameService.getAllGames();

            if (future == null) {
                result.setResult(ListView.buildFailure(Consts.Tips.FailMsg));
                return result;
            }

            future.whenComplete((lst, err) -> {
                if (err != null) {
                    result.setResult(ListView.buildFailure(err.getMessage()));
                    return;
                }
                if (lst == null) {
                    result.setResult(ListView.buildFailure(Consts.Tips.FailMsg));
                    return;
                }
                ListView<Game> view = new ListView<Game>();

                if(role.getTopRole() == 1){
                    //最高权限者，直接返回全部

                    if(StringUtils.isEmpty(gameName)){
                        view.setData(lst);
                    }else{
                        List<Game> matched = lst.stream().filter((Game g) -> g.getName().contains(gameName)).collect(Collectors.toList());
                        view.setData(matched);
                    }
                }else{

                    String gameIdsStr = user.getAdminGames();

                    if(!StringUtils.isEmpty(gameIdsStr)){
                        //返回拥有权限的游戏
                        List<String> appIDs = Arrays.asList(gameIdsStr.split(","));
                        List<Game> matched = lst.stream().filter((Game g) -> appIDs.contains(g.getAppID()+"")).collect(Collectors.toList());

                        if(StringUtils.isEmpty(gameName)){
                            view.setData(matched);
                        }else{
                            List<Game> searched = matched.stream().filter((Game g) -> g.getName().contains(gameName)).collect(Collectors.toList());
                            view.setData(searched);
                        }

                    }else{
                        //返回空
                        view.setData(new ArrayList<>());
                    }
                }

                result.setResult(view);

            });
        }catch (Exception e){
            result.setResult(ListView.buildFailure(Consts.Tips.FailMsg));
            logger.error(e.getMessage());
            e.printStackTrace();
        }


        return result;
    }


    @RequestMapping(value = "/uploadApk", method = RequestMethod.POST)
    @ResponseBody
    ObjectView<String> updateApk(
            @RequestParam(value = "appID", required = true) Integer appID,
	        @RequestParam(value = "file", required = false) MultipartFile file){
        ObjectView<String> view = new ObjectView<>();
        try{
            Game exists = gameService.getGameById(appID);
            if(exists == null){
                view.failure(Consts.RespCode.FAILURE, Consts.Tips.GameNotExists);
                return view;
            }

            String currTime = ""+System.nanoTime();

            String relPath = FileUtils.joinPath("games", "game"+exists.getAppID(), "apks"   , currTime+".apk");
            String path = FileUtils.joinPath(globalConfig.getFileTempPath(), relPath);

            if(FileUtils.fileExists(path)){
                logger.warn("apk file already exists. please retry");
                view.failure(Consts.RespCode.FAILURE, Consts.Tips.FailMsg);
                return view;
            }

            FileUtils.saveFileFromStream(file.getInputStream(), path);

            ApkMeta meta = gameService.generateApkMeta(appID, relPath);

            if(meta == null){
                logger.warn("apk file parse failed.");
                view.failure(Consts.RespCode.FAILURE, Consts.Tips.FailMsg);
                return view;
            }

            exists.setApkPath(relPath);
            gameService.updateGame(exists);

            return view.fromObject(relPath);

        }catch (Exception e){
            e.printStackTrace();
        }

        view.failure(Consts.Tips.FailMsg);
        return view;
    };

    @RequestMapping(value = "/getGameIcon", method = RequestMethod.GET)
    public ResponseEntity<InputStreamResource> downloadApk(@RequestParam(required = true) Integer gameID, HttpServletResponse response){

        try{
//
            Game game = gameService.getGameById(gameID);
            if(game == null){
                logger.error("game not exists for id:"+gameID);
                return null;
            }

            String path = FileUtils.joinPath(globalConfig.getFileTempPath(), "games", "game"+game.getAppID(), "icon/icon.png");

            if(!FileUtils.fileExists(path)){
                path = FileUtils.joinPath(globalConfig.getPackageBuildPath(), "config/local/default_icon.png");
            }

            return HttpUtils.generateDownloadFile(path, "icon.png");

        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }


        return null;

    }

    @RequestMapping(value = "/uploadIcon", method = RequestMethod.POST)
    @ResponseBody
    ResponseView uploadIcon(
            @RequestParam(value = "appID", required = true) Integer appID,
            @RequestParam(value = "file", required = false) MultipartFile file){
        ResponseView view = new ResponseView();
        try{
            Game exists = gameService.getGameById(appID);
            if(exists == null){
                view.failure(Consts.RespCode.FAILURE, Consts.Tips.GameNotExists);
                return view;
            }

            String path = FileUtils.joinPath(globalConfig.getFileTempPath(), "games", "game"+exists.getAppID(), "icon/icon.png");
            FileUtils.saveFileFromStream(file.getInputStream(), path);

            exists.setIconPath(path);
            gameService.updateGame(exists);

        }catch (Exception e){
            e.printStackTrace();
        }
        return view.success();
    }
	
	@RequestMapping(value = "/saveGame", method = RequestMethod.POST)
	@ResponseBody
	ResponseView saveGame(@ModelAttribute Game game,
                          String pluginParams,
                          @RequestParam(value = "iconFile", required = false) MultipartFile iconFile) {
		ResponseView view = new ResponseView();

        try{
            Game exists = gameService.getGameById(game.getId());
            if(exists == null){
                view.failure(Consts.RespCode.FAILURE, Consts.Tips.GameNotExists);
                return view;
            }

            if(iconFile != null && !iconFile.isEmpty()){
                String iconRelPath = FileUtils.joinPath("games", "game"+exists.getAppID(), "icon/icon.png");
                String path = FileUtils.joinPath(globalConfig.getFileTempPath(), iconRelPath);
                FileUtils.saveFileFromStream(iconFile.getInputStream(), path);
                exists.setIconPath(iconRelPath);
            }


            exists.setName(game.getName());
            exists.setOrientation(game.getOrientation());
            if(!StringUtils.isEmpty(game.getCpuSupport())){
                exists.setCpuSupport(game.getCpuSupport().replaceAll(",", "|"));
            }else {
                exists.setCpuSupport("");
            }
            exists.setApkPath(game.getApkPath());
            exists.setVersionCode(game.getVersionCode());
            exists.setVersionName(game.getVersionName());
//            Integer targetSdkVersion = game.getTargetSdkVersion();
//            if(targetSdkVersion != null && targetSdkVersion > 0){
//                exists.setMinSdkVersion(Math.min(15, targetSdkVersion));
//                exists.setMaxSdkVersion(Math.max(28, targetSdkVersion));
//            }
            exists.setTargetSdkVersion(game.getTargetSdkVersion());
            exists.setServerBaseUrl(game.getServerBaseUrl());
            exists.setKeystoreID(game.getKeystoreID());
            exists.setSingleGame(game.getSingleGame());
            exists.setDoNotCompress(game.getDoNotCompress());
            gameService.updateGame(exists);

            //解析插件参数
            gameService.updateGamePlugins(game.getId(), pluginParams);

            return view.success();

        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
            return view.failure(Consts.Tips.FailMsg);
        }
	}
	
	@RequestMapping(value = "/getGames", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    ListView getGames(String gameName,HttpSession session){
        ListView<Game> view = new ListView<Game>();
        List<Game> games = gameService.getGameByName(gameName);
        return view.fromList(games);
    }
	
	@RequestMapping(value = "/deleteGame", method = RequestMethod.POST)
    @ResponseBody
    ResponseView deleteGame(@RequestParam(required = true) int id){
        ResponseView view = new ResponseView();
        Game exists = gameService.getGameById(id);
        if(exists == null){
            return view.failure(Consts.Tips.GameNotExists);
        }
        gameService.deleteGame(exists);

        //清理该游戏目录
        String relPath = FileUtils.joinPath("games", "game"+exists.getAppID());
        String path = FileUtils.joinPath(globalConfig.getFileTempPath(), relPath);
        FileUtils.deleteFile(path);

        return view.success();
    }



}
