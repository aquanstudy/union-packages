package com.u8x.dao;

import com.u8x.dao.entity.Admin;
import com.u8x.dao.entity.AdminRole;
import com.u8x.dao.mapper.AdminMapper;
import com.u8x.dao.mapper.AdminRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by Administrator on 2017/5/14.
 */
@Repository
@Transactional
public class AdminDao {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private AdminRoleMapper adminRoleMapper;

    public Admin getAdminByName(String name){
        return adminMapper.getAdminByName(name);
    }
    
    public AdminRole getRoleByName(String name){
        return adminRoleMapper.getRoleByName(name);
    }

    public Admin getAdminById(Integer id){
        return adminMapper.getAdminById(id);
    }

    public AdminRole getAdminRoleById(int id){
        return adminRoleMapper.getAdminRoleByID(id);
    }

    public List<Admin> getAdmins(String username, String adminRoleName){
        return adminMapper.getAdmins(username, adminRoleName);
    }
    
    public List<Admin> getAdminsNotTop(String username, String adminRoleName){
        return adminMapper.getAdminsNotTop(username, adminRoleName);
    }

    public List<Admin> getAllAdmins(){
        return adminMapper.getAllAdmins();
    }

    public List<AdminRole> getAllAdminRoles(){
        return adminRoleMapper.getAllAdminRoles();
    }
    
    public List<AdminRole> getAllAdminRolesNotTop(String roleName){
        return adminRoleMapper.getAllAdminRolesNotTop(roleName);
    }

    public void insertAdmin(Admin admin){
        adminMapper.insertAdmin(admin);
    }

    public void updateAdmin(Admin admin){
        adminMapper.updateAdmin(admin);
    }

    public void deleteAdmin(int id){
        adminMapper.deleteAdmin(id);
    }

	public void insertRole(AdminRole role) {
		adminRoleMapper.insertRole(role);
	}

	public AdminRole getRoleById(Integer id) {
		return adminMapper.getRoleById(id);
	}

	public void deleteRole(int id) {
		adminMapper.deleteRole(id);
	}

	public void updateRole(AdminRole exists) {
		adminRoleMapper.updateRole(exists);
	}

	public void updateAdminGames(String games, String adminId) {
		adminRoleMapper.updateAdminGames(games,adminId);
	}

}
