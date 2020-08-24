package com.u8x.dao;

import com.u8x.dao.entity.Channel;
import com.u8x.dao.entity.ChannelPlugin;
import com.u8x.dao.entity.PackLog;
import com.u8x.dao.mapper.ChannelMapper;
import com.u8x.dao.mapper.PackLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by ant on 2017/10/11.
 */
@Repository
public class ChannelDao {

    @Autowired
    private ChannelMapper mapper;

    @Autowired
    private PackLogMapper packLogMapper;

    public List<Channel> getChannels(Integer gameID, String name, String channelName, Integer channelID){

        return mapper.getChannels(gameID, name, channelName, channelID);

    }


    public List<Channel> getAllChannels(){

        return mapper.getAllChannels();
    }

    public Channel getChannelByID(Integer id){
        return mapper.getChannelByID(id);
    }

    public List<Channel> getChannelsByIDs(String ids){
        return mapper.getChannelsByIDs(ids);
    }

    public Channel getChannelByName(String name){
        return mapper.getChannelByName(name);
    }

    public void createChannel(Channel channel){

        mapper.insertChannel(channel);
    }

    public void updateChannel(Channel channel){
        mapper.updateChannel(channel);
    }

    public void deleteChannel(Integer id){

        mapper.deleteChannel(id);
    }


    public void deleteGameChannel(int id){
        mapper.deleteGameChannel(id);
    }



    public void insertPackLog(PackLog log){
        packLogMapper.insertPackLog(log);
    }

    public void updatePackLog(PackLog log){
        packLogMapper.updatePackLog(log);
    }

    public List<PackLog> getPackLogs(String gameID, int startPos, int pageSize){

        return packLogMapper.getPackLogs(gameID, startPos, pageSize);
    }

    public List<PackLog> getTestPackLogs(String gameID, int startPos, int pageSize){

        return packLogMapper.getTestPackLogs(gameID, startPos, pageSize);
    }

    public int getPackLogCount(String gameID){
        return packLogMapper.getPackLogCount(gameID);
    }

    public int getTestPackLogCount(String gameID){
        return packLogMapper.getTestPackLogCount(gameID);
    }

    public void deletePackLog(int id){
        packLogMapper.deletePackLog(id);
    }

    public PackLog getPackLogByID(int id){
        return packLogMapper.getPackLogByID(id);
    }

    public List<ChannelPlugin> getChannelPlugins(int channelID){
        return mapper.getPluginsByChannel(channelID);
    }

    public void insertChannelPlugin(ChannelPlugin plugin){
        mapper.insertChannelPlugin(plugin);
    }

    public void updateChannelPluginParams(int id, String params){
        mapper.updateChannelPluginParams(id, params);
    }

    public void updateChannelPluginState(int id, int state){
        mapper.updateChannelPluginState(id, state);
    }

    public void updateChannelPlugin(ChannelPlugin plugin){
        mapper.updateChannelPlugin(plugin);
    }

    public void deleteChannelPlugin(int id){
        mapper.deleteChannelPlugin(id);
    }

    public void deleteInvalidPluginsByGameID(int gameID){
        mapper.deleteInvalidPluginsByGameID(gameID);
    }



}
