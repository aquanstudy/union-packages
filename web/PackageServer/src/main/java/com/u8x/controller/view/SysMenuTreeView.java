package com.u8x.controller.view;

import com.u8x.dao.entity.AdminRole;
import com.u8x.dao.entity.SysMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ant on 2017/6/3.
 */
public class SysMenuTreeView extends ResponseView{

    public SysMenuTreeView fromSysMenuList(List<SysMenu> menus, AdminRole role){

        nodes = new ArrayList<SysMenuItemView>();

        for(SysMenu menu : menus){
            SysMenuItemView view = new SysMenuItemView(menu.getId(), menu.getName(), 0, "default", role.hasPermission(menu.getId()));

            if(menu.getChildren() != null){
                view.setNodes(new ArrayList<SysMenuItemView>());
                for(SysMenu child : menu.getChildren()){
                    SysMenuItemView childView = new SysMenuItemView(child.getId(), child.getName(), 1, "default", role.hasPermission(child.getId()));
                    view.getNodes().add(childView);
                }
            }

            nodes.add(view);

        }

        super.success();
        return this;
    }

    private List<SysMenuItemView> nodes;

    public static class SysMenuItemView{

        public SysMenuItemView(int id, String name, int level, String type, boolean selectState){
            this.id = id;
            this.name = name;
            this.level = level;
            this.type = type;
            this.selectState = selectState ? 1 : 0;
        }

        private int id;
        private String name;
        private int level;
        private String type;
        private int selectState;        //1:有该权限；0：没有权限

        private List<SysMenuItemView> nodes;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getSelectState() {
            return selectState;
        }

        public void setSelectState(int selectState) {
            this.selectState = selectState;
        }

        public List<SysMenuItemView> getNodes() {
            return nodes;
        }

        public void setNodes(List<SysMenuItemView> nodes) {
            this.nodes = nodes;
        }
    }

    public List<SysMenuItemView> getNodes() {
        return nodes;
    }

    public void setNodes(List<SysMenuItemView> nodes) {
        this.nodes = nodes;
    }


}
