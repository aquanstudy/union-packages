package com.u8x.dao.mapper;

import com.u8x.dao.entity.Keystore;
import com.u8x.dao.entity.SysMenu;
import com.u8x.dao.mapper.provider.KeystoreSqlProvider;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by ant on 2018/3/23.
 */
@Mapper
public interface KeystoreMapper {

    @Insert("INSERT INTO `keystore`(`gameID`,`name`,`filePath`,`password`,`aliasName`,`aliasPwd`) VALUES(#{gameID},#{name},#{filePath},#{password},#{aliasName},#{aliasPwd})")
    void insertKeystore(Keystore keystore);

    @Update("UPDATE `keystore` SET `name`=#{name},`filePath`=#{filePath},`password`=#{password},`aliasName`=#{aliasName},`aliasPwd`=#{aliasPwd} WHERE `id`=#{id}")
    void updateKeystore(Keystore keystore);

    @Delete("DELETE FROM `keystore` WHERE `id`=#{id}")
    void deleteKeystore(@Param("id") int id);

    @SelectProvider(type = KeystoreSqlProvider.class, method = "searchKeystores")
    List<Keystore> getKeystores(@Param("gameID") Integer gameID,@Param("name") String name, @Param("startPos") int startPos, @Param("pageSize") int pageSize);

    @SelectProvider(type = KeystoreSqlProvider.class, method = "searchKeystoreCount")
    @ResultType(java.lang.Integer.class)
    int getKeystoreCount(@Param("gameID") Integer gameID, @Param("name") String name);


    @Select("SELECT * FROM `keystore` WHERE `id`=#{id}")
    Keystore getKeystoreByID(@Param("id") int id);

}
