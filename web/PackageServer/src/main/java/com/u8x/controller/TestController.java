package com.u8x.controller;

import com.u8x.common.Consts;
import com.u8x.controller.view.ObjectView;
import com.u8x.controller.view.ResponseView;
import com.u8x.dao.entity.SysMenu;
import com.u8x.task.PackTaskManager;
import com.u8x.utils.FileUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.*;

/**
 * Created by ant on 2018/3/16.
 */
@Controller
@RequestMapping("/admin/test")
public class TestController {

    @RequestMapping(value = "/testCmd", method = RequestMethod.GET)
    @ResponseBody
    public ResponseView testCmd(String p){

        ResponseView view = new ResponseView();

        System.out.println(p);

        return view.success();
    }

    public static void main(String[] args){
        //PackTaskManager.getInstance().addPackTask("1", "jinli", "1001");
        //PackTaskManager.getInstance().addPackTask("1", "baidu");
    }


}
