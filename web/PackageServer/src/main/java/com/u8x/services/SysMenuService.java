package com.u8x.services;

import com.u8x.dao.SysMenuDao;
import com.u8x.dao.entity.Admin;
import com.u8x.dao.entity.AdminRole;
import com.u8x.dao.entity.SysMenu;
import com.u8x.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ant on 2017/5/31.
 */
@Service
public class SysMenuService {

    @Autowired
    private SysMenuDao menuDao;

    public void insertSysMenu(SysMenu menu){
        menuDao.insertSysMenu(menu);
    }

    public void updateSysMenu(SysMenu menu){
        menuDao.updateSysMenu(menu);
    }

    public List<SysMenu> getAllSysMenus(){
        return menuDao.getAllSysMenus();
    }

    public List<SysMenu> getAllTopSysMenus(){

        return menuDao.getAllTopSysMenus();
    }

    public SysMenu getSysMenuByID(int id){
        return menuDao.getSysMenuByID(id);
    }

    public List<SysMenu> getChildSysMenus(int id){

        return menuDao.getChildSysMenus(id);
    }

    public void deleteSysMenus( int id){

        menuDao.deleteSysMenus(id);
    }

    public List<SysMenu> getRoleTreeMenus(AdminRole role){

        if(role == null || role.getPermission() == null) return null;

        if(role.getTopRole() == 1){

            return getTreeMenus();
        }


        String permission = role.getPermission();
        String[] ps = permission.split(",");

        List<SysMenu> menus = getTreeMenus();

        boolean hasChild = false;
        List<SysMenu> parentRemoveable = new ArrayList<SysMenu>();
        List<SysMenu> childRemoveable = new ArrayList<SysMenu>();
        for(SysMenu menu : menus){
            hasChild = false;
            if(menu.getChildren() != null){

                for(SysMenu child : menu.getChildren()){
                    if(StringUtils.contain(ps, child.getId()+"")){
                        hasChild = true;
                    }else{
                        childRemoveable.add(child);
                    }
                }
                menu.getChildren().removeAll(childRemoveable);
                childRemoveable.clear();
            }

            if(!hasChild){
                parentRemoveable.add(menu);
            }
        }

        menus.removeAll(parentRemoveable);


        return menus;
    }

    public List<SysMenu> getTreeMenus(){

        return getTreeMenus(getAllSysMenus());
    }

    //按照树状结构重组
    public List<SysMenu> getTreeMenus(List<SysMenu> all){

        List<SysMenu> roots = new ArrayList<SysMenu>();

        if(all == null){
            return roots;
        }

        for(SysMenu m : all){
            if(m.getParentID() <= 0){
                roots.add(m);
            }
        }

        for(SysMenu m : roots){

            for(SysMenu n : all){

                if(n.getParentID() == m.getId()){
                    if(m.getChildren() == null){
                        m.setChildren(new ArrayList<SysMenu>());
                    }
                    m.getChildren().add(n);
                }
            }

        }

        return roots;
    }

}
