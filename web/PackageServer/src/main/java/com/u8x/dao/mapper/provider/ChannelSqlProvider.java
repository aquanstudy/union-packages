package com.u8x.dao.mapper.provider;

import com.u8x.utils.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * Created by ant on 2017/10/11.
 */
public class ChannelSqlProvider {


    public String searchPackLogs(Map<String, Object> params){

        String gameID = (String)params.get("gameID");

        Object startPos = null;

        if(params.containsKey("startPos")){
            startPos = params.get("startPos");
        }
        Object pageSize = null;
        if(params.containsKey("pageSize")){
            pageSize = params.get("pageSize");
        }

        String sql = "select * from `packlog` where `gameID` = " + gameID +" order by `id` desc";

        if(startPos != null && pageSize !=null && Integer.valueOf(startPos.toString()) >= 0 && Integer.valueOf(pageSize.toString()) > 0){
            sql += " limit " + startPos + "," + pageSize;
        }

        return sql;

    }

    public String searchPackLogCount(Map<String, Object> params){

        String gameID = (String)params.get("gameID");


        String sql = "select count(`id`) from `packlog` where `gameID` = " + gameID;

        return sql;

    }


    public String searchTestPackLogs(Map<String, Object> params){

        String gameID = (String)params.get("gameID");

        Object startPos = null;

        if(params.containsKey("startPos")){
            startPos = params.get("startPos");
        }
        Object pageSize = null;
        if(params.containsKey("pageSize")){
            pageSize = params.get("pageSize");
        }

        String sql = "select * from `packlog` where `gameID` = " + gameID + " and `testState` != 0 " + " order by `id` desc";

        if(startPos != null && pageSize !=null && Integer.valueOf(startPos.toString()) >= 0 && Integer.valueOf(pageSize.toString()) > 0){
            sql += " limit " + startPos + "," + pageSize;
        }

        return sql;

    }

    public String searchTestPackLogCount(Map<String, Object> params){

        String gameID = (String)params.get("gameID");


        String sql = "select count(`id`) from `packlog` where `gameID` = " + gameID + " and `testState` != 0 ";

        return sql;

    }

    public String searchChannels(Map<String, Object> params){
        return searchSql(params, false);
    }

    public String searchChannelCount(Map<String, Object> params){

        return searchSql(params, true);
    }

    public String searchChannelsByIDs(Map<String, Object> params){

        String channelIDs = (String)params.get("ids");

        String sql = "select * from `channel` where id in ("+channelIDs+")";

        return sql;

    }

    public String searchSql(Map<String, Object> params, boolean selectCount) {
        Integer gameID = (Integer)params.get("gameID");
        String name = (String) params.get("name");
        String channelName = (String) params.get("channelName");
        Integer channelID = (Integer) params.get("channelID");

        SQL sql = new SQL();
        if(selectCount){
            sql.SELECT("count(`id`)");
        }else{
            sql.SELECT("*");
        }
        sql.FROM("`channel`");

        if(gameID > 0){
            sql.WHERE("`gameID` = " + gameID);
        }

        if (StringUtils.isNotEmpty(name)) {
            sql.WHERE("`name` like '" + name + "%'");
        }

        if (StringUtils.isNotEmpty(channelName)) {
            sql.WHERE("`channelName` like '%" + channelName + "%'");
        }

        if (channelID > 0) {
            sql.WHERE("`channelID` = " + channelID);
        }


        String sqlStr = sql.toString() + " order by `id` desc";

        System.out.println("the sql:" + sqlStr);

        return sqlStr;

    }

    public String searchGameChannels(Map<String,Object> params){

        return searchGameChannelSql(params, false);
    }

    public String searchGameChannelCount(Map<String,Object> params){

        return searchGameChannelSql(params, true);
    }


    public String searchGameChannelSql(Map<String, Object> params, boolean selectCount) {
        Object appID = params.get("appID");
        Object channelID = params.get("channelID");


        Object startPos = null;

        if(params.containsKey("startPos")){
            startPos = params.get("startPos");
        }
        Object pageSize = null;
        if(params.containsKey("pageSize")){
            pageSize = params.get("pageSize");
        }

        SQL sql = new SQL();
        if(selectCount){
            sql.SELECT("count(g.`id`)");
            sql.FROM("`gamechannel` g");
        }else{
            sql.SELECT("g.*,c.name");
            sql.FROM("`gamechannel` g left join `channel` c on g.`channelID` = c.`id`");
        }


        if (appID != null && StringUtils.isNotEmpty(appID.toString())) {
            sql.WHERE("g.`appID` = '" + appID.toString() + "'");
        }

        if (channelID != null && StringUtils.isNotEmpty(channelID.toString())) {
            sql.WHERE("g.`channelID` = " + channelID);
        }


        String sqlStr = sql.toString();

        if(startPos != null && pageSize !=null && Integer.valueOf(startPos.toString()) >= 0 && Integer.valueOf(pageSize.toString()) > 0){
            sqlStr += " limit " + startPos + "," + pageSize;
        }

        System.out.println("the sql:" + sqlStr);

        return sqlStr;

    }

}
