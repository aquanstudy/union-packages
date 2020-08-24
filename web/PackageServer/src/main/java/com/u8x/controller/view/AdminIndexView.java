package com.u8x.controller.view;

import com.u8x.dao.entity.Admin;
import com.u8x.dao.entity.AdminRole;
import com.u8x.dao.entity.SysMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ant on 2017/5/14.
 */
public class AdminIndexView extends ResponseView {

    private Admin admin;

    private List<MenuItemInfo> nodes;

    public AdminIndexView fromAdmin(Admin admin, List<SysMenu> menus){
        this.admin = admin;
        nodes = new ArrayList<MenuItemInfo>();

        for(SysMenu menu : menus){
            MenuItemInfo view = new MenuItemInfo(menu.getName(), menu.getPath(), menu.getIconClass());
            if(menu.getChildren() != null){
                view.setChildren(new ArrayList<MenuItemInfo>());
                for(SysMenu child : menu.getChildren()){
                    MenuItemInfo childView = new MenuItemInfo(child.getName(), child.getPath(), child.getIconClass());
                    view.getChildren().add(childView);
                }
            }

            nodes.add(view);

        }
        this.success();
        return this;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public List<MenuItemInfo> getNodes() {
        return nodes;
    }

    public void setNodes(List<MenuItemInfo> nodes) {
        this.nodes = nodes;
    }

    public static class MenuItemInfo{

        public MenuItemInfo(String name, String path, String iconClass){
            this.name = name;
            this.path = path;
            this.iconClass = iconClass;
        }

        private String name;
        private String path;
        private String iconClass;

        private List<MenuItemInfo> children;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getIconClass() {
            return iconClass;
        }

        public void setIconClass(String iconClass) {
            this.iconClass = iconClass;
        }

        public List<MenuItemInfo> getChildren() {
            return children;
        }

        public void setChildren(List<MenuItemInfo> children) {
            this.children = children;
        }
    }
}
