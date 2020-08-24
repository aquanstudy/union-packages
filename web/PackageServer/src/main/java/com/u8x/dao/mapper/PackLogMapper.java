package com.u8x.dao.mapper;

import com.u8x.dao.entity.Keystore;
import com.u8x.dao.entity.PackLog;
import com.u8x.dao.mapper.provider.ChannelSqlProvider;
import com.u8x.dao.mapper.provider.KeystoreSqlProvider;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PackLogMapper {

    @Insert("INSERT INTO packlog(`name`,`channelID`,`channelLocalID`,`gameID`,`channelName`,`fileName`,`state`,`testState`,`testFeed`,`channelParams`,`createDate`) VALUES(#{name},#{channelID},#{channelLocalID},#{gameID},#{channelName},#{fileName},#{state},#{testState},#{testFeed},#{channelParams},#{createDate})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    public void insertPackLog(PackLog log);


    @Update("UPDATE packlog SET `name`=#{name},`channelID`=#{channelID},`channelLocalID`=#{channelLocalID},`gameID`=#{gameID},`channelName`=#{channelName},`fileName`=#{fileName},`state`=#{state},`testState`=#{testState},`channelParams`=#{channelParams},`testFeed`=#{testFeed},`createDate`=#{createDate} WHERE `id`=#{id}")
    public void updatePackLog(PackLog log);

    @SelectProvider(type = ChannelSqlProvider.class, method = "searchPackLogs")
    List<PackLog> getPackLogs(@Param("gameID") String gameID, @Param("startPos") int startPos, @Param("pageSize") int pageSize);

    @SelectProvider(type = ChannelSqlProvider.class, method = "searchPackLogCount")
    @ResultType(java.lang.Integer.class)
    int getPackLogCount(@Param("gameID") String gameID);

    @SelectProvider(type = ChannelSqlProvider.class, method = "searchTestPackLogs")
    List<PackLog> getTestPackLogs(@Param("gameID") String gameID, @Param("startPos") int startPos, @Param("pageSize") int pageSize);


    @SelectProvider(type = ChannelSqlProvider.class, method = "searchTestPackLogCount")
    @ResultType(java.lang.Integer.class)
    int getTestPackLogCount(@Param("gameID") String gameID);


    @Delete("DELETE FROM `packlog` WHERE `id`=#{id}")
    public void deletePackLog(@Param("id") Integer id);


    @Select("SELECT * FROM `packlog` WHERE `id`=#{id}")
    public PackLog getPackLogByID(@Param("id") Integer id);
}