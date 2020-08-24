package com.u8x.dao.mapper;

import java.util.List;

import com.u8x.dao.entity.ApkMeta;
import com.u8x.dao.entity.GamePlugin;
import org.apache.ibatis.annotations.*;

import com.u8x.dao.entity.Game;
import com.u8x.dao.mapper.provider.GameSqlProvider;

/**
 * Created by guofeng.qin on 2017/2/23.
 */
public interface GameMapper {
	@Select("SELECT * FROM `game` WHERE `name` = #{name}")
	Game getTheGameByName(@Param("name") String name);

	//@Select("SELECT * FROM `game` WHERE `id` IN (#{permissionGames})")
	@SelectProvider(type = GameSqlProvider.class, method = "searchPermissionGames")
	List<Game> getAllPermissionGames(@Param("gameIds") String permissionGames);

	@Select("SELECT * FROM `game`")
	List<Game> getAllGames();

	@SelectProvider(type = GameSqlProvider.class, method = "searchGamesWithPage")
	List<Game> getAllGamesWithPage(@Param("gameName") String gameName, @Param("startPos") int startPos, @Param("pageSize") int pageSize);

	@SelectProvider(type = GameSqlProvider.class, method = "searchGamesCount")
	@ResultType(java.lang.Integer.class)
	int getAllGameCount(@Param("gameName") String gameName);
	
	@SelectProvider(type = GameSqlProvider.class, method = "searchGames")
	List<Game> getGameByName(@Param("gameName") String gameName);

	@Select("SELECT * FROM `apkmeta` WHERE `gameID` = #{gameID} order by `id` desc")
	List<ApkMeta> getApkMetasByGameID(@Param("gameID") Integer gameID);

	@Select("SELECT * FROM `apkmeta` WHERE `id`=#{id}")
	ApkMeta getApkMetaByID(@Param("id") Integer id);

	@Insert("INSERT INTO `game`(`id`,`cpID`,`appID`,`name`,`appKey`,`appSecret`,`orientation`,`cpuSupport`, `keystoreID`, `minSdkVersion`, `targetSdkVersion`,`maxSdkVersion`,`versionCode`,`versionName`,`outputApkName`,`doNotCompress`, `enableLog`,`singleGame`,`serverBaseUrl`,`createTime`) VALUES(#{id},#{cpID}, #{appID}, #{name},#{appKey},#{appSecret},#{orientation},#{cpuSupport}, #{keystoreID}, #{minSdkVersion}, #{targetSdkVersion}, #{maxSdkVersion}, #{versionCode}, #{versionName}, #{outputApkName},#{doNotCompress}, #{enableLog},#{singleGame}, #{serverBaseUrl}, #{createTime})")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	void insertGame(Game game);

	@Insert("INSERT INTO `apkmeta`(`gameID`,`name`,`bundleID`,`versionCode`,`versionName`,`uploadTime`,`apkPath`) VALUES(#{gameID},#{name},#{bundleID},#{versionCode},#{versionName},#{uploadTime},#{apkPath})")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	void insertApkMeta(ApkMeta meta);

	@Insert("INSERT INTO `gameplugin`(`gameID`,`sdkName`,`params`) VALUES(#{gameID},#{sdkName},#{params})")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	void insertGamePlugin(GamePlugin plugin);
	
	@Select("SELECT * FROM `game` WHERE `id` = #{id}")
	Game getGameById(Integer id);

	@Select("SELECT * FROM `gameplugin` WHERE `gameID` = #{gameID}")
	List<GamePlugin> getPluginsByGame(Integer gameID);

	@Update("UPDATE `game` SET `cpID`=#{cpID},`appID`=#{appID},`name`=#{name},`appKey`=#{appKey},`appSecret`=#{appSecret},`orientation`=#{orientation},`cpuSupport`=#{cpuSupport} ,`apkPath`=#{apkPath},`iconPath`=#{iconPath}, `keystoreID`=#{keystoreID} ,`minSdkVersion`=#{minSdkVersion} ,`targetSdkVersion`=#{targetSdkVersion},`maxSdkVersion`=#{maxSdkVersion},`versionCode`=#{versionCode},`versionName`=#{versionName},`outputApkName`=#{outputApkName},`doNotCompress`=#{doNotCompress},`enableLog`=#{enableLog},`singleGame`=#{singleGame},`serverBaseUrl`=#{serverBaseUrl} WHERE `id`=#{id}")
	void updateGame(Game exists);

	@Update("UPDATE `gameplugin` SET `params`=#{params} WHERE `id`=#{id}")
	void updateGamePlugin(GamePlugin plugin);
	
	@Delete("DELETE FROM `game` WHERE `id`=#{id}")
	void deleteGame(int id);

	@Delete("DELETE FROM `gameplugin` WHERE `id`=#{id}")
	void deleteGamePlugin(int id);

	@Delete("DELETE FROM `gameplugin` WHERE `gameID`=#{gameID} AND `sdkName`=#{sdkName}")
	void deleteGamePluginBySDK(@Param("gameID") Integer gameID, @Param("sdkName") String sdkName);

	@Delete("DELETE FROM `apkmeta` WHERE `id`=#{id}")
	void deleteApkMeta(int id);
	
}
