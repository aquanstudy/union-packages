package com.u8x.dao.mapper.provider;

import com.u8x.utils.StringUtils;
import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * 管理员信息查询动态SQL组装
 * Created by ant on 2017/5/21.
 */
public class AdminSqlProvider {

    public String searchAdmins(Map<String, Object> params){

        Object username = params.get("username");
        Object adminRoleName = params.get("adminRoleName");

        SQL sql = new SQL();
        sql.SELECT("*");
        sql.FROM("`admin`");

        if(username != null && !StringUtils.isEmpty(username.toString())){
            sql.WHERE("`username` like '%"+username.toString()+"%'");
        };

        if(adminRoleName != null && !StringUtils.isEmpty(adminRoleName.toString())){
            sql.WHERE("`adminRoleName` like '%"+adminRoleName.toString()+"%'");
        }
        
        String sqlStr = sql.toString();

        System.out.println("the sql:"+sqlStr);

        return sqlStr;

    }
    
    public String searchAdminsNotTop(Map<String, Object> params){
        Object username = params.get("username");
        Object adminRoleName = params.get("adminRoleName");
        SQL sql = new SQL();
        sql.SELECT("*");
        sql.FROM("`admin`");
        if(username != null && !StringUtils.isEmpty(username.toString())){
            sql.WHERE("`username` like '%"+username.toString()+"%'");
        };
        if(adminRoleName != null && !StringUtils.isEmpty(adminRoleName.toString())){
            sql.WHERE("`adminRoleName` like '%"+adminRoleName.toString()+"%'");
        }
        sql.WHERE("`adminRoleID` != 1");
        String sqlStr = sql.toString();
        System.out.println("the sql:"+sqlStr);
        return sqlStr;
    }
    
    public String searchAdminRolesNotTop(Map<String, Object> params){
    	Object roleName = params.get("roleName");
        SQL sql = new SQL();
        sql.SELECT("*");
        sql.FROM("`adminrole`");
        if(roleName != null && !StringUtils.isEmpty(roleName.toString())){
            sql.WHERE("`roleName` like '%"+roleName.toString()+"%'");
        };
        sql.WHERE("`topRole` != 1");
        String sqlStr = sql.toString();
        System.out.println("the sql:"+sqlStr);
        return sqlStr;

    }

}
