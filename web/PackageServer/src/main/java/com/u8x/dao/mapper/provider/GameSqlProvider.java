package com.u8x.dao.mapper.provider;

import java.util.Map;

import org.apache.ibatis.jdbc.SQL;

import com.u8x.utils.StringUtils;

public class GameSqlProvider {


        public String searchGames(Map<String, Object> params) {


                Object gameName = params.get("gameName");
                SQL sql = new SQL();
                sql.SELECT("*");
                sql.FROM("`game`");
                if(gameName != null && !StringUtils.isEmpty(gameName.toString())){
                    sql.WHERE("`name` like '%"+gameName.toString()+"%'");
                };
                String sqlStr = sql.toString();
                System.out.println("the sql:"+sqlStr);
                return sqlStr;
        }

        public String searchPermissionGames(Map<String, Object> params) {

                Object gameIds = params.get("gameIds");
                SQL sql = new SQL();
                sql.SELECT("*");
                sql.FROM("`game`");
                if(gameIds != null && !StringUtils.isEmpty(gameIds.toString())){
                        sql.WHERE("`id` IN ("+gameIds.toString()+")");
                };
                String sqlStr = sql.toString();
                System.out.println("the sql:"+sqlStr);
                return sqlStr;
        }

        public String searchGamesWithPage(Map<String, Object> params) {


                Object gameName = params.get("gameName");

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
                sql.FROM("`game`");
                if(gameName != null && !StringUtils.isEmpty(gameName.toString())){
                        sql.WHERE("`name` like '%"+gameName.toString()+"%'");
                };
                String sqlStr = sql.toString();

                if(startPos != null && pageSize !=null && Integer.valueOf(startPos.toString()) >= 0 && Integer.valueOf(pageSize.toString()) > 0){
                        sqlStr += " limit " + startPos + "," + pageSize;
                }

                return sqlStr;
        }


        public String searchGamesCount(Map<String, Object> params) {


                Object gameName = params.get("gameName");


                SQL sql = new SQL();
                sql.SELECT("count(`id`)");
                sql.FROM("`game`");
                if(gameName != null && !StringUtils.isEmpty(gameName.toString())){
                        sql.WHERE("`name` like '%"+gameName.toString()+"%'");
                };
                String sqlStr = sql.toString();
                return sqlStr;
        }
}
