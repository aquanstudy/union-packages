package com.u8x.dao.entity;

import java.io.Serializable;


public class Admin implements Serializable{

    private static final long serialVersionUID = -2350043477552381422L;

    private Integer id;
    private String username;            //管理员用户名
    private String password;            //管理员密码
    private Integer adminRoleID;        //管理员所属角色
    private String adminGames;          //管理员可以管理的游戏
    private String adminRoleName;       //管理员所属角色名称


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getAdminRoleID() {
        return adminRoleID;
    }

    public void setAdminRoleID(Integer adminRoleID) {
        this.adminRoleID = adminRoleID;
    }

    public String getAdminRoleName() {
        return adminRoleName;
    }

    public void setAdminRoleName(String adminRoleName) {
        this.adminRoleName = adminRoleName;
    }

	public String getAdminGames() {
		return adminGames;
	}

	public void setAdminGames(String adminGames) {
		this.adminGames = adminGames;
	}

}
