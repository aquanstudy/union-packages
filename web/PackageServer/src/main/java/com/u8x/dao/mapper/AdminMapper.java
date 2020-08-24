package com.u8x.dao.mapper;

import com.u8x.dao.entity.Admin;
import com.u8x.dao.entity.AdminRole;
import com.u8x.dao.mapper.provider.AdminSqlProvider;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by Administrator on 2017/5/14.
 */
@Mapper
public interface AdminMapper {

    @Insert("INSERT INTO `admin`(`username`,`password`,`adminRoleID`,`adminRoleName`,`adminGames`) VALUES(#{username},#{password},#{adminRoleID},#{adminRoleName},#{adminGames})")
    void insertAdmin(Admin admin);

    @Update("UPDATE `admin` SET username=#{username},password=#{password},adminRoleID=#{adminRoleID},adminRoleName=#{adminRoleName} ,adminGames=#{adminGames} WHERE `id`=#{id}")
    void updateAdmin(Admin admin);

    @Select("SELECT * FROM `admin` WHERE username = #{username}")
    Admin getAdminByName(@Param("username") String username);

    @Select("SELECT * FROM `admin` WHERE id = #{id}")
    Admin getAdminById(@Param("id") Integer id);

    @Select("SELECT * FROM `admin` ")
    List<Admin> getAllAdmins();

    @SelectProvider(type = AdminSqlProvider.class, method = "searchAdmins")
    List<Admin> getAdmins(@Param("username") String username, @Param("adminRoleName") String adminRoleName);
    
    @SelectProvider(type = AdminSqlProvider.class, method = "searchAdminsNotTop")
    List<Admin> getAdminsNotTop(@Param("username") String username, @Param("adminRoleName") String adminRoleName);

    @Delete("DELETE FROM `admin` WHERE `id`=#{id}")
    void deleteAdmin(@Param("id") int id);
    
    @Select("SELECT * FROM `adminrole` WHERE id = #{id}")
	AdminRole getRoleById(Integer id);
    
    @Delete("DELETE FROM `adminrole` WHERE `id`=#{id}")
	void deleteRole(int id);

}
