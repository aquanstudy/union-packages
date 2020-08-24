package com.u8x.controller;

import com.u8x.common.Consts;
import com.u8x.controller.view.ListView;
import com.u8x.controller.view.ObjectView;
import com.u8x.controller.view.ResponseView;
import com.u8x.controller.view.SysMenuTreeView;
import com.u8x.dao.entity.Admin;
import com.u8x.dao.entity.AdminRole;
import com.u8x.dao.entity.SysMenu;
import com.u8x.services.AdminService;
import com.u8x.services.SysMenuService;
import com.u8x.utils.DateUtil;
import com.u8x.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Date;
import java.util.List;

/**
 * Created by ant on 2017/5/31.
 */
@Controller
@RequestMapping("/admin/sysmenu")
public class SysMenuController {

    @Autowired
    private SysMenuService menuService;

    @Autowired
    private AdminService adminService;

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    @ResponseBody
    SysMenuTreeView getAllMenus(@RequestParam(required = true) int roleID){

//        ListView<SysMenu> view = new ListView<SysMenu>();
//
//        List<SysMenu> menus = menuService.getAllSysMenus();
//
//        return view.fromList(menus);
        SysMenuTreeView view = new SysMenuTreeView();
        AdminRole role = adminService.getAdminRoleById(roleID);
        if(role == null){
            view.failure(Consts.Tips.FailMsg);
            return view;
        }



        List<SysMenu> menus = menuService.getTreeMenus();

        return view.fromSysMenuList(menus, role);

    }

    @RequestMapping(value = "/allTop", method = RequestMethod.POST)
    @ResponseBody
    ListView<SysMenu> getAllTopMenus(){

        ListView<SysMenu> view = new ListView<SysMenu>();

        List<SysMenu> menus = menuService.getAllTopSysMenus();

        return view.fromList(menus);

    }

    @RequestMapping(value = "/changePermissions", method = RequestMethod.POST)
    @ResponseBody
    public ResponseView changePermissions(@RequestParam(required = true) int roleID, @RequestParam(required = true) String permissions){

        ResponseView view = new ResponseView();

        AdminRole role = adminService.getAdminRoleById(roleID);
        if(role == null){
            view.failure(Consts.Tips.FailMsg);
            return view;
        }

        role.setPermission(permissions);
        adminService.updateRole(role);

        return view.success();

    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public ResponseView saveSysmenu(@ModelAttribute SysMenu menu, HttpSession session){

        ResponseView view = new ResponseView();

        if(menu.getId() == null || menu.getId() == 0){
            //添加新的功能
            menu.setCreateTime(DateUtil.getDateTime());
            menuService.insertSysMenu(menu);
        }else {
            //编辑已有功能
            SysMenu exists = menuService.getSysMenuByID(menu.getId());
            if(exists == null){

                return view.failure(Consts.Tips.FailMsg);
            }
            menu.setCreateTime(exists.getCreateTime());
            menuService.updateSysMenu(menu);
        }

        return view.success();

    }

    @RequestMapping(value = "/detail", method = RequestMethod.POST)
    @ResponseBody
    public ObjectView<SysMenu> getDetail(@RequestParam(required = true) int id){

        ObjectView<SysMenu> view = new ObjectView<SysMenu>();

        SysMenu menu = menuService.getSysMenuByID(id);
        if(menu == null){
            view.failure(Consts.Tips.FailMsg);
            return view;
        }

        return view.fromObject(menu);

    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    ResponseView removeAdmin(@RequestParam(required = true) int id){
        ResponseView view = new ResponseView();
        SysMenu exists = menuService.getSysMenuByID(id);
        if(exists == null){
            return view.failure(Consts.Tips.FailMsg);
        }

        List<SysMenu> children = menuService.getChildSysMenus(id);
        if(children.size() > 0){

            return view.failure(Consts.Tips.ChildrenExists);
        }

        menuService.deleteSysMenus(id);
        return view.success();
    }

}
