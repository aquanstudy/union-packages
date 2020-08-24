package com.u8x.dao;

import java.util.Date;
import java.util.List;

import com.u8x.dao.entity.ApkMeta;
import com.u8x.dao.entity.GamePlugin;
import com.u8x.utils.DateUtil;
import com.u8x.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Repository;

import com.u8x.dao.entity.Game;
import com.u8x.dao.mapper.GameMapper;

/**
 * Created by guofeng.qin on 2017/2/23.
 */
@Repository
@CacheConfig(cacheNames = "xgame")
public class GameDao {
	@Autowired
	private GameMapper gameMapper;

	public Game getTheGameByName(String name) {
		return gameMapper.getTheGameByName(name);
	}

	public List<Game> getAllGames(String permissionGames) {

		if(StringUtils.isEmpty(permissionGames)){
			permissionGames = "";
		}

		permissionGames = permissionGames.trim();

		if(permissionGames.endsWith(",")){
			permissionGames = permissionGames.substring(0, permissionGames.length()-1);
		}

		if(permissionGames.startsWith(",")){
			permissionGames = permissionGames.substring(1, permissionGames.length());
		}

		return gameMapper.getAllPermissionGames(permissionGames);
	}

	public List<Game> getAllGames(){
		return gameMapper.getAllGames();
	}

	public List<Game> getAllGames(String name, int startPos, int pageSize) {


		return gameMapper.getAllGamesWithPage(name, startPos, pageSize);
	}

	public int getAllGameCount(String name){
		return gameMapper.getAllGameCount(name);
	}

	public List<Game> getGameByName(String name) {
		return gameMapper.getGameByName(name);
	}

	public void insertGame(Game game) {
		game.setId(game.getAppID());
		game.setCreateTime(DateUtil.getDateTime());
		gameMapper.insertGame(game);
	}

	public void insertGames(List<Game> games){
		if(games == null || games.size() == 0) return;
		for(Game g : games){
			g.setId(g.getAppID());
			g.setCreateTime(DateUtil.getDateTime());
			gameMapper.insertGame(g);
		}
	}

	public Game getGameById(Integer id) {
		return gameMapper.getGameById(id);
	}

	public Game updateGame(Game exists) {
		gameMapper.updateGame(exists);
		return exists;
	}

	public Game deleteGame(Game exists) {
		gameMapper.deleteGame(exists.getId());
		return exists;
	}

	public void insertApkMeta(ApkMeta meta){
		gameMapper.insertApkMeta(meta);
	}

	public ApkMeta getApkMetaByID(Integer id){
		return gameMapper.getApkMetaByID(id);
	}

	public List<ApkMeta> getApkMetasByGameID(Integer gameID){
		return gameMapper.getApkMetasByGameID(gameID);
	}

	public void deleteApkMeta(Integer id){
		gameMapper.deleteApkMeta(id);
	}


	public void insertGamePlugin(GamePlugin plugin){
		gameMapper.insertGamePlugin(plugin);
	}

	public void updateGamePlugin(GamePlugin plugin){
		gameMapper.updateGamePlugin(plugin);
	}

	public void deleteGamePlugin(Integer id){
		gameMapper.deleteGamePlugin(id);
	}

	public void deleteGamePlugin(Integer gameID, String sdkName){
		gameMapper.deleteGamePluginBySDK(gameID, sdkName);
	}

	public List<GamePlugin> getPluginsByGame(Integer gameID){

		return gameMapper.getPluginsByGame(gameID);
	}

}
