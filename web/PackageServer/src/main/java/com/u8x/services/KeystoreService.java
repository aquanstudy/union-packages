package com.u8x.services;

import com.u8x.dao.KeystoreDao;
import com.u8x.dao.entity.Keystore;
import com.u8x.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

/**
 * Created by ant on 2018/3/23.
 */
@Service
public class KeystoreService {

    @Autowired
    private KeystoreDao keystoreDao;

    public void createKeystore(Keystore keystore){
        keystoreDao.insertKeystore(keystore);
    }

    public void updateKeystore(Keystore keystore){
        keystoreDao.updateKeystore(keystore);
    }

    public void deleteKeystore(Keystore keystore){
        keystoreDao.deleteKeystore(keystore);
    }

    public List<Keystore> getKeystores(Integer gameID, String name, int startPos, int pageSize){

        return keystoreDao.getKeystores(gameID, name, startPos, pageSize);
    }


    public int getKeystoreCount(Integer gameID, String name){

        return keystoreDao.getKeystoreCount(gameID, name);
    }


    public Keystore getKeystoreByID(int id){

        return keystoreDao.getKeystoreByID(id);
    }

}
