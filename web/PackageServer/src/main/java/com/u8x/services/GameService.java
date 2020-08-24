package com.u8x.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.u8x.common.XLogger;
import com.u8x.dao.config.GlobalConfig;
import com.u8x.dao.entity.*;
import com.u8x.sdk.LocalSDKManager;
import com.u8x.utils.*;
import net.dongliu.apk.parser.ApkFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.u8x.dao.GameDao;

/**
 * Created by guofeng.qin on 2017/2/23.
 */
@Service
@Transactional
public class GameService {
	private static final XLogger logger = XLogger.getLogger(GameService.class);

	@Autowired
	private GameDao gameDao;

	@Autowired
	private GlobalConfig globalConfig;

	@Autowired
	private LocalSDKManager sdkManager;


	public Game getTheGameByName(String name) {
		return gameDao.getTheGameByName(name);
	}

	public List<Game> getAllLocalGames() {

		return gameDao.getAllGames();
	}

	public List<Game> getAllGames(String name, int startPos, int pageSize){

		return gameDao.getAllGames(name, startPos, pageSize);
	}

	public int getAllGameCount(String name){
		return gameDao.getAllGameCount(name);
	}

	public CompletableFuture<List<Game>> getAllGames() {
		CompletableFuture<List<Game>> future = new CompletableFuture<>();
		try{

			List<Game> localGameLst = gameDao.getAllGames();
			String existAppIDs = "";
			if(localGameLst != null && localGameLst.size() > 0){
				for(int i=0; i<localGameLst.size(); i++){
					if(i > 0){
						existAppIDs += ",";
					}
					existAppIDs += localGameLst.get(i).getAppID();
				}
			}

			String time = System.currentTimeMillis()+"";
			StringBuilder sb = new StringBuilder();
			sb.append("apiID=").append(globalConfig.apiID).append("&excludeAppIDs=").append(existAppIDs).append("&time=").append(time).append(globalConfig.apiKey);

			String sign = EncryptUtils.md5(sb.toString()).toLowerCase();

			Map<String,String> params = new HashMap<>();
			params.put("apiID", globalConfig.apiID);
			params.put("excludeAppIDs", existAppIDs);
			params.put("time", time);
			params.put("sign",sign);

			HttpUtils.post(globalConfig.gameUrl(), params, new HttpUtils.IHttpCallback() {
				@Override
				public void completed(String data) {
					logger.debug("post result:{}", data);
					try{
						JSONObject jsonResult = JSON.parseObject(data);
						if(jsonResult.getIntValue("code") == 0){
							logger.error("synchronize from u8server manager failed. state code:{};msg:{}",jsonResult.getIntValue("code"), jsonResult.getString("msg"));
							future.complete(localGameLst);
							return;
						}
						JSONArray jsonArray = jsonResult.getJSONArray("data");
						if(jsonArray != null && jsonArray.size() > 0){
							List<Game> addedGameLst = JSON.parseArray(jsonArray.toString(), Game.class);
							gameDao.insertGames(addedGameLst);
							localGameLst.addAll(addedGameLst);
							logger.debug("synchronize from u8server manager success. localize {} new games.", addedGameLst.size());
						}else{
							logger.debug("synchronize from u8server manager success. there is no new game to localize");
						}

						future.complete(localGameLst);

					}catch (Exception e){
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
			logger.error(e.getMessage());
			future.completeExceptionally(e);
			e.printStackTrace();
		}

		return future;
	}

	public List<Game> getGameByName(String name) {
		return gameDao.getGameByName(name);
	}

	public void insertGame(Game game) {
		gameDao.insertGame(game);
	}

	public Game getGameById(Integer id) {
		return gameDao.getGameById(id);
	}

	public List<ApkMeta> getApkMetasByGameID(Integer gameID){
		return gameDao.getApkMetasByGameID(gameID);
	}

	public ApkMeta getApkMetaByID(Integer id){

		return gameDao.getApkMetaByID(id);
	}

	public void deleteApkMeta(Integer id){
		gameDao.deleteApkMeta(id);
	}

	public void updateGame(Game exists) {
		gameDao.updateGame(exists);
	}

	public void deleteGame(Game game) {
		gameDao.deleteGame(game);
	}

	public ApkMeta generateApkMeta(Integer gameID, String apkPath){

		String fullApkPath = FileUtils.joinPath(globalConfig.getFileTempPath(), apkPath);
		if(!FileUtils.fileExists(fullApkPath)){
			logger.error("apk parse failed. apk not exists.%s", fullApkPath);
			return null;
		}

		ApkFile apkParser = null;
		try {
			apkParser = new ApkFile(new File(fullApkPath));
			String xml = apkParser.getManifestXml();
			net.dongliu.apk.parser.bean.ApkMeta apkInfo = apkParser.getApkMeta();

			String name = apkInfo.getName();
			String versionCode = "" + apkInfo.getVersionCode();
			String versionName = apkInfo.getVersionName();
			String bundleID = apkInfo.getPackageName();
			String time = DateUtil.getDateTime();

			ApkMeta meta = new ApkMeta();
			meta.setName(name);
			meta.setVersionCode(versionCode);
			meta.setVersionName(versionName);
			meta.setBundleID(bundleID);
			meta.setUploadTime(time);
			meta.setApkPath(apkPath);
			meta.setGameID(gameID);

			gameDao.insertApkMeta(meta);

			return meta;
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			if(apkParser != null){
				try {
					apkParser.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return null;

	}

	public void insertGamePlugin(GamePlugin plugin){
		gameDao.insertGamePlugin(plugin);
	}

	public void updateGamePlugin(GamePlugin plugin){
		try{
			gameDao.updateGamePlugin(plugin);
		}catch (Exception e){
			logger.error("updateGamePlugin failed.", e);
			e.printStackTrace();
		}

	}

	public void deleteGamePlugin(Integer id){
		gameDao.deleteGamePlugin(id);
	}

	public void deleteGamePlugin(Integer gameID, String sdkName){
		gameDao.deleteGamePlugin(gameID, sdkName);
	}

	public List<GamePlugin> getPluginsByGame(Integer gameID){
		return gameDao.getPluginsByGame(gameID);
	}

	public Map<String, GamePlugin> getPluginMapByGame(Integer gameID){
		List<GamePlugin> plugins = getPluginsByGame(gameID);


		if(plugins != null){
			Map<String, GamePlugin> pluginMap = new HashMap<>();
			for(GamePlugin p : plugins){
				pluginMap.put(p.getSdkName(), p);
			}
			return pluginMap;
		}

		return null;


	}

	public void updateGamePlugins(Integer gameID, String gamePluginJsonArray){

		if(StringUtils.isEmpty(gamePluginJsonArray)) return;

		try{

			List<GamePlugin> plugins = getPluginsByGame(gameID);

			JSONArray jsonArray = JSON.parseArray(gamePluginJsonArray);
			if(jsonArray != null && jsonArray.size() > 0){
				for(int i=0; i<jsonArray.size(); i++){
					JSONObject json = jsonArray.getJSONObject(i);
					int id = json.getInteger("id");
					for(GamePlugin currP : plugins){
						if(currP.getId() == id){
							JSONObject params = json.getJSONObject("params");
							if(params != null){
								currP.setParams(params.toString());
								updateGamePlugin(currP);
							}

						}
					}
				}
			}


		}catch (Exception e){
			e.printStackTrace();
		}

	}

	public List<GamePlugin> getPluginsByGameWithMaster(Integer gameID){

		List<GamePlugin> plugins = gameDao.getPluginsByGame(gameID);

		if(plugins != null){
			List<GamePlugin> result = new ArrayList<>();
			for(GamePlugin plugin : plugins){
				PluginMaster master = sdkManager.getPluginBySDK(plugin.getSdkName());
				if(master != null){
					plugin.setMaster(master);
					result.add(plugin);

					String jsonParams = plugin.getParams();
					if(!StringUtils.isEmpty(jsonParams)){
						Map<String, String> params = JSONObject.parseObject(jsonParams, new TypeReference<Map<String, String>>(){});
						if(params != null && master.getMetas() != null){
							for(PluginParamMeta m : master.getMetas()){
								if(params.containsKey(m.getParamKey())){
									m.setDefaultVal(params.get(m.getParamKey()));
								}
							}
						}
					}

				}
			}
			return plugins;

		}

		return null;

	}

}
