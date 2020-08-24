package com.u8x.dao.mapper;

import com.u8x.dao.entity.Admin;
import com.u8x.dao.entity.AdminRole;
import com.u8x.dao.mapper.provider.AdminSqlProvider;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Created by Administrator on 2017/5/20.
 */
@Mapper
public interface AdminRoleMapper {

    @Select("SELECT * FROM adminrole")
    List<AdminRole> getAllAdminRoles();

    @Select("SELECT * FROM adminrole WHERE `id` = #{id}")
    AdminRole getAdminRoleByID(@Param("id") int id);

    @SelectProvider(type = AdminSqlProvider.class, method = "searchAdminRolesNotTop")
    List<AdminRole> getAllAdminRolesNotTop(@Param("roleName") String roleName );

    @Select("SELECT * FROM `adminrole` WHERE rolename = #{rolename}")
    AdminRole getRoleByName(@Param("rolename") String rolename);
    
    @Insert("INSERT INTO `adminrole`(`roleName`,`roleDesc`,`createTime`,`creatorID`,`topRole`) VALUES(#{roleName},#{roleDesc},#{createTime},#{creatorID},0)")
	void insertRole(AdminRole role);
   
    @Update("UPDATE `adminrole` SET roleName=#{roleName},roleDesc=#{roleDesc},`permission`=#{permission} WHERE `id`=#{id}")
	void updateRole(AdminRole exists);
    
    @Update("UPDATE `admin` SET adminGames=#{games} WHERE `id`=#{adminId}")
	void updateAdminGames(@Param("games")String games, @Param("adminId")String adminId);
}
