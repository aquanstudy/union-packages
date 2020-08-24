package com.u8x.controller;

import com.u8x.common.Consts;
import com.u8x.common.XLogger;
import com.u8x.controller.view.ListView;
import com.u8x.controller.view.PageView;
import com.u8x.controller.view.ResponseView;
import com.u8x.dao.config.GlobalConfig;
import com.u8x.dao.entity.Channel;
import com.u8x.dao.entity.Keystore;
import com.u8x.services.KeystoreService;
import com.u8x.utils.FileUtils;
import com.u8x.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

/**
 * Created by ant on 2018/3/23.
 */
@Controller
@RequestMapping("/admin/keystore")
public class KeystoreController {

    private static final XLogger logger = XLogger.getLogger(KeystoreController.class);

    @Autowired
    private KeystoreService keystoreService;

    @Autowired
    private GlobalConfig globalConfig;

    @RequestMapping(value = "/getKeystores", method = RequestMethod.GET)
    @ResponseBody
    public PageView<Keystore> getKeystores(Integer gameID,
                                           String name,
                                           int draw,
                                           int start,
                                           int length){


        PageView<Keystore> view = new PageView<Keystore>();
        try{

            int totalSize = keystoreService.getKeystoreCount(gameID, name);
            List<Keystore> keystores = keystoreService.getKeystores(gameID, name, start, length);

            view.fromList(keystores, draw, totalSize, totalSize);

        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }


        return view;

    }

    @RequestMapping(value = "/getAllKeystores", method = RequestMethod.GET)
    @ResponseBody
    public ListView<Keystore> getAllKeystores(Integer gameID){
        ListView<Keystore> view = new ListView<>();
        try{

            List<Keystore> keystores = keystoreService.getKeystores(gameID, null, 0, 0);
            view.fromList(keystores);
        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        return view;
    }

    @RequestMapping(value = "/saveKeystore", method = RequestMethod.POST)
    @ResponseBody
    ResponseView saveKeystore(@ModelAttribute Keystore keystore,
                              @RequestParam(value = "keystoreFile", required = false) MultipartFile file) {
        ResponseView view = new ResponseView();

        try{

            String path = "";
            if(file != null && !file.isEmpty()){
                //save keystore file
                String fileName = file.getOriginalFilename();

                if((!fileName.endsWith(".keystore")) && (!fileName.endsWith(".jks"))){
                    logger.error("keystore file must end with .keystore or .jks");
                    return view.failure(Consts.Tips.KeystoreFileExtInvalid);
                }

                //path = FileUtils.joinPath("config/keystore", System.currentTimeMillis() + FileUtils.getExt(file.getOriginalFilename()));
                path = FileUtils.joinPath("games/game"+keystore.getGameID(), "keystore", System.currentTimeMillis() + FileUtils.getExt(file.getOriginalFilename()));
                FileUtils.saveFileFromStream(file.getInputStream(), FileUtils.joinPath(globalConfig.getFileTempPath(), path));
            }


            if(keystore.getId() == null || keystore.getId().equals(0)){

                if(StringUtils.isEmpty(path)){
                    logger.error("keystore file not exists or save failed.");
                    return view.failure(Consts.Tips.FailMsg);
                }
                keystore.setFilePath(path);
                keystoreService.createKeystore(keystore);
            }else{
                Keystore exists = keystoreService.getKeystoreByID(keystore.getId());
                if(exists == null){
                    return view.failure(Consts.Tips.FailMsg);
                }

                exists.setName(keystore.getName());
                exists.setAliasName(keystore.getAliasName());
                exists.setAliasPwd(keystore.getAliasPwd());
                exists.setPassword(keystore.getPassword());
                if(!StringUtils.isEmpty(path)){
                    exists.setFilePath(path);
                }

                keystoreService.updateKeystore(exists);
            }

            return view.success();


        }catch (Exception e){
            e.printStackTrace();
            return view.failure(Consts.Tips.FailMsg);
        }

    }


    @RequestMapping(value = "/deleteKeystore", method = RequestMethod.POST)
    @ResponseBody
    ResponseView deleteKeystore(@RequestParam(required = true) int id){
        ResponseView view = new ResponseView();
        Keystore exists = keystoreService.getKeystoreByID(id);
        if(exists == null){
            return view.failure(Consts.Tips.ChannelNotExists);
        }
        keystoreService.deleteKeystore(exists);
        return view.success();
    }


}
