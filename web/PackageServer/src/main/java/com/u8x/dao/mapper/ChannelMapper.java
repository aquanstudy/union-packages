package com.u8x.dao.mapper;

import com.u8x.dao.entity.Channel;
import com.u8x.dao.entity.ChannelPlugin;
import com.u8x.dao.entity.GamePlugin;
import com.u8x.dao.mapper.provider.ChannelSqlProvider;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 *
 * Created by ant on 2017/10/11.
 */
@Mapper
public interface ChannelMapper {

    @SelectProvider(type = ChannelSqlProvider.class, method = "searchChannels")
    List<Channel> getChannels(
                          @Param("gameID") Integer gameID,
                          @Param("name") String name,
                          @Param("channelName") String channelName,
                          @Param("channelID") Integer channelID);



    @Select("SELECT * FROM `channel` WHERE `name` = #{name}")
    public Channel getChannelByName(@Param("name") String name);

    @Select("SELECT * FROM `channel` WHERE `id` = #{id}")
    public Channel getChannelByID(@Param("id") Integer id);

    //@Select("SELECT * FROM `channel` WHERE `id` in (#{ids})")
    @SelectProvider(type = ChannelSqlProvider.class, method = "searchChannelsByIDs")
    public List<Channel> getChannelsByIDs(@Param("ids") String ids);

    @Select("SELECT * FROM `channel`")
    public List<Channel> getAllChannels();


    @Insert("INSERT INTO `channel`(`channelID`,`gameID`,`name`,`channelName`,`sdk`,`masterSDKName`,`bundleID`,`splash`,`replaceUnitySplash`,`iconType`,`icon`,`gameName`,`signApk`,`signVersion`,`keystoreID`,`isLocal`,`localConfig`,`isConfiged`,`createTime`,`minSdkVersion`,`targetSdkVersion`,`maxSdkVersion`, `autoPermission`, `directPermission`, `excludePermissionGroups`, `autoProtocol`, `protocolUrl`, `serverBaseUrl`, `sdkLogicVersionCode`) VALUES(#{channelID},#{gameID},#{name},#{channelName},#{sdk},#{masterSDKName},#{bundleID},#{splash},#{replaceUnitySplash},#{iconType},#{icon},#{gameName},#{signApk},#{signVersion},#{keystoreID},#{isLocal},#{localConfig},#{isConfiged},#{createTime},#{minSdkVersion},#{targetSdkVersion},#{maxSdkVersion}, #{autoPermission}, #{directPermission}, #{excludePermissionGroups}, #{autoProtocol}, #{protocolUrl}, #{serverBaseUrl}, #{sdkLogicVersionCode})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    public void insertChannel(Channel channel);

    @Select("SELECT * FROM `channelplugin` WHERE `channelID` = #{channelID}")
    List<ChannelPlugin> getPluginsByChannel(Integer channelID);

    @Insert("INSERT INTO `channelplugin`(`channelID`,`gameID`,`sdkName`,`params`,`state`,`extend`) VALUES(#{channelID},#{gameID},#{sdkName},#{params},#{state},#{extend})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insertChannelPlugin(ChannelPlugin plugin);

    @Update("UPDATE `channel` SET `channelID`=#{channelID},`gameID`=#{gameID},`name`=#{name},`channelName`=#{channelName},`sdk`=#{sdk},`masterSDKName`=#{masterSDKName},`bundleID`=#{bundleID},`splash`=#{splash},`replaceUnitySplash`=#{replaceUnitySplash},`iconType`=#{iconType},`icon`=#{icon},`gameName`=#{gameName},`signApk`=#{signApk},`signVersion`=#{signVersion},`keystoreID`=#{keystoreID},`localConfig`=#{localConfig},`isConfiged`=#{isConfiged},`minSdkVersion`=#{minSdkVersion},`targetSdkVersion`=#{targetSdkVersion},`maxSdkVersion`=#{maxSdkVersion},`autoPermission`=#{autoPermission},`directPermission`=#{directPermission},`excludePermissionGroups`=#{excludePermissionGroups},`autoProtocol`=#{autoProtocol},`protocolUrl`=#{protocolUrl},`serverBaseUrl`=#{serverBaseUrl}, `sdkLogicVersionCode`=#{sdkLogicVersionCode}  WHERE `id`=#{id}")
    public void updateChannel(Channel channel);

    @Update("UPDATE `channelplugin` SET `params`=#{params} WHERE `id`=#{id}")
    public void updateChannelPluginParams(@Param("id") Integer id, @Param("params") String params);

    @Update("UPDATE `channelplugin` SET `state`=#{state} WHERE `id`=#{id}")
    public void updateChannelPluginState(@Param("id") Integer id, @Param("state") Integer state);

    @Update("UPDATE `channelplugin` SET `state`=#{state},`params`=#{params},`extend`=#{extend} WHERE `id`=#{id}")
    public void updateChannelPlugin(ChannelPlugin plugin);


    @Delete("DELETE FROM `channel` WHERE `id`=#{id}")
    void deleteChannel(int id);

    @Delete("DELETE FROM `gamechannel` WHERE `id`=#{id}")
    void deleteGameChannel(int id);

    @Delete("DELETE FROM `channelplugin` WHERE `id`=#{id}")
    void deleteChannelPlugin(int id);

    @Delete("DELETE FROM `channelplugin` WHERE `gameID`=#{id} AND `state` = 0")
    void deleteInvalidPluginsByGameID(int id);
}
