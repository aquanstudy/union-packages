package com.u8x.dao.mapper.provider;

import com.u8x.utils.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * Created by ant on 2018/3/23.
 */
public class KeystoreSqlProvider {

    public String searchKeystores(Map<String, Object> params){

        Object name = params.get("name");
        Object gameID = params.get("gameID");

        Object startPos = null;

        if(params.containsKey("startPos")){
            startPos = params.get("startPos");
        }
        Object pageSize = null;
        if(params.containsKey("pageSize")){
            pageSize = params.get("pageSize");
        }


        SQL sql = new SQL();
        sql.SELECT("*");
        sql.FROM("`keystore`");
        if(name != null && !StringUtils.isEmpty(name.toString())){
            sql.WHERE("`name` like '%"+name.toString()+"%'");
        }

        sql.WHERE("`gameID`="+gameID);

        String sqlStr = sql.toString();

        if(startPos != null && pageSize !=null && Integer.valueOf(startPos.toString()) >= 0 && Integer.valueOf(pageSize.toString()) > 0){
            sqlStr += " limit " + startPos + "," + pageSize;
        }

        System.out.println("the sql:"+sqlStr);

        return sqlStr;

    }

    public String searchKeystoreCount(Map<String, Object> params){
        Object name = params.get("name");
        Object gameID = params.get("gameID");

        SQL sql = new SQL();
        sql.SELECT("count(id)");
        sql.FROM("`keystore`");
        if(name != null && !StringUtils.isEmpty(name.toString())){
            sql.WHERE("`name` like '%"+name.toString()+"%'");
        }
        sql.WHERE("`gameID`="+gameID);

        String sqlStr = sql.toString();

        System.out.println("the sql:"+sqlStr);

        return sqlStr;
    }

}
