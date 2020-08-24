package com.u8x.dao;

import com.u8x.dao.entity.Admin;
import com.u8x.dao.entity.SysMenu;
import com.u8x.dao.mapper.SysMenuMapper;
import org.apache.ibatis.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by ant on 2017/5/31.
 */
@Repository
@Transactional
public class SysMenuDao {

    @Autowired
    private SysMenuMapper mapper;

    public void insertSysMenu(SysMenu menu){
        mapper.insertSysMenu(menu);
    }

    public void updateSysMenu(SysMenu menu){
        mapper.updateSysMenu(menu);
    }

    public List<SysMenu> getAllSysMenus(){
        return mapper.getAllSysMenus();
    }

    public List<SysMenu> getChildSysMenus(int id){

        return mapper.getChildSysMenus(id);
    }

    public List<SysMenu> getAllTopSysMenus(){
        return mapper.getAllTopSysMenus();
    }

    public SysMenu getSysMenuByID(int id){
        return mapper.getSysMenuByID(id);
    }

    public void deleteSysMenus( int id){

        mapper.deleteSysMenus(id);
    }

}
