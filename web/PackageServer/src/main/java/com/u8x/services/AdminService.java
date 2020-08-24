package com.u8x.services;

import com.u8x.dao.AdminDao;
import com.u8x.dao.entity.Admin;
import com.u8x.dao.entity.AdminRole;
import com.u8x.utils.DateUtil;
import com.u8x.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Administrator on 2017/5/14.
 */
@Service
public class AdminService {

    @Autowired
    private AdminDao adminDao;

    public Admin getAdminByName(String name){

        return adminDao.getAdminByName(name);
    }

    public void updateAdminGamePermission(Admin admin, int gameID){

        if(admin == null || gameID <= 0){
            return;
        }

        if(StringUtils.isEmpty(admin.getAdminGames())){
            admin.setAdminGames(gameID+"");
        }else{
            String[] splits = admin.getAdminGames().split(",");
            boolean found = false;
            for(String s : splits){
                if(s.equals(gameID+"")){
                    found = true;
                    break;
                }
            }

            if(!found){
                if(admin.getAdminGames().endsWith(",")){
                    admin.setAdminGames(admin.getAdminGames()+gameID);
                }else{
                    admin.setAdminGames(admin.getAdminGames()+","+gameID);
                }
            }
        }

        adminDao.updateAdmin(admin);

    }
    
    public AdminRole getRoleByName(String name){
        return adminDao.getRoleByName(name);
    }

    public Admin getAdminById(Integer id){
        return adminDao.getAdminById(id);
    }

    public AdminRole getAdminRoleById(int id){
        return adminDao.getAdminRoleById(id);
    }

    public AdminRole getRoleById(Integer id){
        return adminDao.getRoleById(id);
    }

    public List<Admin> getAllAdmins(){

        return adminDao.getAllAdmins();
    }

    public List<Admin> getAdmins(String username, String adminRoleName){
        return adminDao.getAdmins(username, adminRoleName);
    }
    
    public List<Admin> getAdminsNotTop(String username, String adminRoleName){
        return adminDao.getAdminsNotTop(username, adminRoleName);
    }

    public void insertAdmin(Admin admin){
        adminDao.insertAdmin(admin);
    }

    public void updateAdmin(Admin admin){
        adminDao.updateAdmin(admin);
    }

    public void deleteAdmin(int id){
        adminDao.deleteAdmin(id);
    }

    public List<AdminRole> getAllAdminRoles(){
        return adminDao.getAllAdminRoles();
    }
    
    public List<AdminRole> getAllAdminRolesNotTop(String roleName){
        return adminDao.getAllAdminRolesNotTop(roleName);
    }

	public void insertRole(AdminRole role) {
        role.setCreateTime(DateUtil.getDateTime());
		adminDao.insertRole(role);
	}

	public void deleteRole(int id) {
		adminDao.deleteRole(id);
	}

	public void updateRole(AdminRole exists) {
		adminDao.updateRole(exists);
	}


	public void addAdminGame(String gameID, Admin admin){

        String games = admin.getAdminGames();

        if(games == null){
            games = "";
        }

        String[] ids = games.split(",");

        for(String id : ids){
            if(id.equals(gameID)){
                return;
            }
        }
        if(games.endsWith(",")){
            games += gameID;
        }else{
            games += "," + gameID;
        }
        adminDao.updateAdminGames(games, admin.getId()+"");
    }

    public void addAdminGames(String games, Admin admin){

        String exists = admin.getAdminGames();

        if(exists == null ){
            exists ="";
        }

        if(exists.endsWith(",")){
            exists = exists.substring(0, exists.length() -1);
        }

        String[] ids = games.split(",");
        String[] exts = exists.split(",");

        for(String id : ids){
            if(StringUtils.isEmpty(id)){
                continue;
            }
            boolean found = false;
            for(String e : exts){

                if(!StringUtils.isEmpty(e) && e.equals(id)){
                    found = true;
                    break;
                }
            }

            if(found){
                continue;
            }

            if(exists.length() == 0){
                exists = id;
            }else{
                exists += "," + id;
            }

        }

        adminDao.updateAdminGames(exists, admin.getId()+"");
    }

    public void removeAdminGame(String gameID, Admin admin){

        String games = admin.getAdminGames();
        if(games == null){
            games = "";
        }

        String[] ids = games.split(",");

        String newStr = "";

        for(String id : ids){
            if(id.equals(gameID)){
                continue;
            }else if(!StringUtils.isEmpty(id)){
                newStr += id + ",";
            }
        }

        adminDao.updateAdminGames(newStr, admin.getId()+"");
    }


    public void removeAdminGames(String games, Admin admin){

        String exists = admin.getAdminGames();

        if(exists == null ){
            exists ="";
        }

        if(exists.endsWith(",")){
            exists = exists.substring(0, exists.length() -1);
        }

        String[] ids = games.split(",");
        String[] exts = exists.split(",");
        String newStr = "";
        for(String e : exts){

            boolean found = false;
            for(String id : ids){

                if(!StringUtils.isEmpty(id) && id.equals(e)){
                    found = true;
                    break;
                }
            }

            if(found || StringUtils.isEmpty(e)){
                continue;
            }

            if(newStr.length() == 0){
                newStr = e;
            }else{
                newStr += "," + e;
            }

        }

        adminDao.updateAdminGames(newStr, admin.getId()+"");
    }
}
