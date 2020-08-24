package com.u8x.dao.mapper;

import com.u8x.dao.entity.Admin;
import com.u8x.dao.entity.SysMenu;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by ant on 2017/5/31.
 */
@Mapper
public interface SysMenuMapper {

    @Insert("INSERT INTO `sysmenu`(`name`,`path`,`parentID`,`createTime`,`iconClass`) VALUES(#{name},#{path},#{parentID},#{createTime},#{iconClass})")
    void insertSysMenu(SysMenu menu);

    @Update("UPDATE `sysmenu` SET `name`=#{name},`path`=#{path},`parentID`=#{parentID},`createTime`=#{createTime},`iconClass`=#{iconClass} WHERE `id`=#{id}")
    void updateSysMenu(SysMenu menu);

    @Select("SELECT * FROM `sysmenu` ")
    List<SysMenu> getAllSysMenus();

    @Select("SELECT * FROM `sysmenu` WHERE `parentID`=0 ")
    List<SysMenu> getAllTopSysMenus();

    @Select("SELECT * FROM `sysmenu` WHERE `parentID`=#{id} ")
    List<SysMenu> getChildSysMenus(@Param("id") int id);

    @Select("SELECT * FROM `sysmenu` WHERE `id`=#{id}")
    SysMenu getSysMenuByID(@Param("id") int id);

    @Delete("DELETE FROM `sysmenu` WHERE `id`=#{id}")
    void deleteSysMenus(@Param("id") int id);

}
