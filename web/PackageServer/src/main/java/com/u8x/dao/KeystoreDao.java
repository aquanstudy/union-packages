package com.u8x.dao;

import com.u8x.dao.entity.Keystore;
import com.u8x.dao.mapper.KeystoreMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by ant on 2018/3/23.
 */
@Repository
@Transactional
public class KeystoreDao {

    @Autowired
    private KeystoreMapper mapper;

    public void insertKeystore(Keystore keystore){
        mapper.insertKeystore(keystore);
    }

    public void updateKeystore(Keystore keystore){
        mapper.updateKeystore(keystore);
    }

    public void deleteKeystore(Keystore keystore){
        mapper.deleteKeystore(keystore.getId());
    }

    public List<Keystore> getKeystores(Integer gameID, String name, int startPos, int pageSize){
        return mapper.getKeystores(gameID, name, startPos, pageSize);
    }

    public int getKeystoreCount(Integer gameID, String name){

        return mapper.getKeystoreCount(gameID, name);
    }

    public Keystore getKeystoreByID(int id){

        return mapper.getKeystoreByID(id);
    }
}
