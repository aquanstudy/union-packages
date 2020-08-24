package com.u8x.controller;

import com.u8x.common.Consts;
import com.u8x.common.XLogger;
import com.u8x.controller.view.AdminIndexView;
import com.u8x.controller.view.ListView;
import com.u8x.controller.view.ResponseView;
import com.u8x.dao.entity.Admin;
import com.u8x.dao.entity.AdminRole;
import com.u8x.dao.entity.ShiroToken;
import com.u8x.dao.entity.SysMenu;
import com.u8x.services.AdminService;
import com.u8x.services.SysMenuService;
import com.u8x.utils.CodeUtils;
import com.u8x.utils.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.RenderedImage;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by Administrator on 2017/5/14.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final XLogger logger = XLogger.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    @Autowired
    private SysMenuService menuService;

    @RequestMapping(value = "/doLogin", method = RequestMethod.POST)
    @ResponseBody
    ResponseView login(@RequestParam(required = true) String username,
                   @RequestParam(required = true) String password,
                   @RequestParam(required = true) boolean rememberMe,
                       @RequestParam(required = false) String vcode,
                   HttpSession session) {

        ResponseView view = new ResponseView();
        try{

            Object vcodeLocal = session.getAttribute("myVCode");
            if(StringUtils.isEmpty(vcode) || vcodeLocal == null || !vcodeLocal.toString().equalsIgnoreCase(vcode)){

                return view.failure(Consts.Tips.VCodeError);
            }

            ShiroToken token = new ShiroToken(username, password, rememberMe);
            SecurityUtils.getSubject().login(token);
            return view.success();
        }catch (AccountException e){
            e.printStackTrace();
            return view.failure(e.getMessage());
        } catch(IncorrectCredentialsException e){
            e.printStackTrace();
            return view.failure(Consts.Tips.UPError);
        }catch (Exception e){
            e.printStackTrace();
            logger.error(e.getMessage());
            return view.failure(Consts.Tips.FailMsg);
        }finally {
            session.removeAttribute("myVCode");
        }


    }

    @RequestMapping(value = "/getVCode", method = RequestMethod.GET)
    void generateVCode(
            HttpServletResponse response,
            HttpServletRequest request,
            HttpSession session) {

        try{

            CodeUtils.ImageCode code = CodeUtils.generateCodeAndPic();
            session.setAttribute("myVCode", code.getCode());

            response.setContentType("image/png");//必须设置响应内容类型为图片，否则前台不识别

            OutputStream os = response.getOutputStream(); //获取文件输出流

            ImageIO.write((RenderedImage)code.getImg(),"jpeg",os);//输出图片流

            os.flush();
            os.close();//关闭流

        }catch (Exception e){
            logger.error(e.getMessage());
            e.printStackTrace();
        }

    }
    
    @RequestMapping(value = "/changePwd", method = RequestMethod.POST)
    @ResponseBody
    ResponseView changePwd(@RequestParam(required = true) String curPwd,
                   @RequestParam(required = true) String newPwd,
                   HttpSession session) {
        ResponseView view = new ResponseView();
        //Admin user = (Admin) session.getAttribute("admin");
        Admin user = (Admin) SecurityUtils.getSubject().getPrincipal();
        if(user == null){
            view.failure(Consts.Tips.SessionInvalid);
            return view;
        }
        if(!user.getPassword().equals(curPwd)){
            view.failure(Consts.Tips.PasswordError);
            return view;
        }
        //session.setAttribute("admin", user);
        user.setPassword(newPwd);
        adminService.updateAdmin(user);
        return view.success();
    }


    @RequestMapping(value = "/getIndex", method = RequestMethod.POST)
    @ResponseBody
    AdminIndexView getIndex(HttpSession session){

        Admin user = (Admin) SecurityUtils.getSubject().getPrincipal();

        AdminIndexView resp = new AdminIndexView();
        if(user == null){
            resp.failure(Consts.RespCode.ReLogin, Consts.Tips.SessionInvalid);
            return resp;
        }

        AdminRole role = adminService.getAdminRoleById(user.getAdminRoleID());
        if(role == null){
            resp.failure(Consts.RespCode.FAILURE, Consts.Tips.NO_PERMISSION);
        }

        List<SysMenu> menus = menuService.getRoleTreeMenus(role);

        return resp.fromAdmin(user, menus);

    }
    
    @RequestMapping(value = "/all", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    ListView getAllAdmins( String username,
                           String adminRoleName,
                           HttpSession session){

        ListView<Admin> view = new ListView<Admin>();

        List<Admin> admins = adminService.getAdmins(username, adminRoleName);

        return view.fromList(admins);

    }
    
	@RequestMapping(value = "/allNotTop", method = RequestMethod.GET)
	@ResponseBody
	ListView getAllAdminsNotTop(String username, String adminRoleName, HttpSession session) {
		ListView<Admin> view = new ListView<Admin>();
		List<Admin> admins = adminService.getAdminsNotTop(username, adminRoleName);
		return view.fromList(admins);
	}
    
    @RequestMapping(value = "/roles", method = RequestMethod.POST)
    @ResponseBody
    ListView getAllAdminRoles(HttpSession session){

        ListView<AdminRole> view = new ListView<AdminRole>();
        List<AdminRole> roles = adminService.getAllAdminRoles();
        return view.fromList(roles);

    }
    
    @RequestMapping(value = "/notTopRoles", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    ListView getAdminRolesNotTop(String roleName,HttpSession session){

        ListView<AdminRole> view = new ListView<AdminRole>();
        List<AdminRole> roles = adminService.getAllAdminRolesNotTop(roleName);
        return view.fromList(roles);

    }
    
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    ResponseView saveAdmin(@ModelAttribute Admin admin){
        ResponseView view = new ResponseView();
        if(admin.getId() == null || admin.getId().equals(0)){
            //添加新管理员
            Admin exists = adminService.getAdminByName(admin.getUsername());
            if(exists != null){
                return view.failure(Consts.Tips.UserNameExists);
            }
            admin.setAdminGames("");
            adminService.insertAdmin(admin);
        }else{
            //编辑已有管理员
            Admin exists = adminService.getAdminById(admin.getId());
            if(exists == null){
                return view.failure(Consts.Tips.FailMsg);
            }
            Admin nameExists = adminService.getAdminByName(admin.getUsername());
            if(nameExists != null && !nameExists.getId().equals(admin.getId())){
                return view.failure(Consts.Tips.UserNameExists);
            }
            exists.setUsername(admin.getUsername());
            exists.setAdminRoleID(admin.getAdminRoleID());
            exists.setAdminRoleName(admin.getAdminRoleName());

            if(admin.getPassword() != null && admin.getPassword().trim().length() > 0){
                exists.setPassword(admin.getPassword());
            }

            adminService.updateAdmin(exists);
        }

        return view.success();
    }
    
    @RequestMapping(value = "/saveRole", method = RequestMethod.POST)
    @ResponseBody
    ResponseView saveRole(@ModelAttribute AdminRole role){
        ResponseView view = new ResponseView();
        Admin user = (Admin) SecurityUtils.getSubject().getPrincipal();

        if(user == null){
            return view.failure(Consts.Tips.SessionInvalid);
        }

        if(role.getId() == null || role.getId().equals(0)){
            AdminRole exists = adminService.getRoleByName(role.getRoleName());
            if(exists != null){
                return view.failure(Consts.Tips.UserNameExists);
            }
            role.setCreatorID(user.getId());
            adminService.insertRole(role);
        }else{
            AdminRole exists = adminService.getRoleById(role.getId());
            if(exists == null){
                return view.failure(Consts.Tips.FailMsg);
            }
            AdminRole nameExists = adminService.getRoleByName(role.getRoleName());
            if(nameExists != null && !nameExists.getId().equals(role.getId())){
                return view.failure(Consts.Tips.UserNameExists);
            }
            exists.setRoleName(role.getRoleName());
            exists.setRoleDesc(role.getRoleDesc());

            adminService.updateRole(exists);
        }

        return view.success();
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    @ResponseBody
    ResponseView removeAdmin(@RequestParam(required = true) int id){
        ResponseView view = new ResponseView();
        Admin exists = adminService.getAdminById(id);
        if(exists == null){
            return view.failure(Consts.Tips.AdminNotExists);
        }
        adminService.deleteAdmin(id);
        return view.success();
    }
    
    @RequestMapping(value = "/deleteRole", method = RequestMethod.POST)
    @ResponseBody
    ResponseView removeRole(@RequestParam(required = true) int id){
        ResponseView view = new ResponseView();
        AdminRole exists = adminService.getRoleById(id);
        if(exists == null){
            return view.failure(Consts.Tips.UserNotExists);
        }
        adminService.deleteRole(id);
        return view.success();
    }
    


    @RequestMapping(value = "/addAdminGame", method = RequestMethod.POST)
    @ResponseBody
    ResponseView addAdminGame(@RequestParam(required = true) String gameId,Integer adminId){
        ResponseView view = new ResponseView();
        try{

            Admin  admin = adminService.getAdminById(adminId);
            if(admin == null){
                return view.failure(Consts.Tips.AdminNotExists);
            }

            adminService.addAdminGame(gameId, admin);

        }catch(Exception e){
            return view.failure(e.toString());
        }
        return view.success();
    }

    @RequestMapping(value = "/addAdminGames", method = RequestMethod.POST)
    @ResponseBody
    ResponseView addAdminGames(@RequestParam(required = true) String gameIds,Integer adminId){
        ResponseView view = new ResponseView();
        try{

            Admin  admin = adminService.getAdminById(adminId);
            if(admin == null){
                return view.failure(Consts.Tips.AdminNotExists);
            }

            adminService.addAdminGames(gameIds, admin);

        }catch(Exception e){
            return view.failure(e.toString());
        }
        return view.success();
    }

    @RequestMapping(value = "/removeAdminGame", method = RequestMethod.POST)
    @ResponseBody
    ResponseView removeAdminGame(@RequestParam(required = true) String gameId,Integer adminId){
        ResponseView view = new ResponseView();
        try{

            Admin  admin = adminService.getAdminById(adminId);
            if(admin == null){
                return view.failure(Consts.Tips.AdminNotExists);
            }

            adminService.removeAdminGame(gameId, admin);

        }catch(Exception e){
            return view.failure(e.toString());
        }
        return view.success();
    }

    @RequestMapping(value = "/removeAdminGames", method = RequestMethod.POST)
    @ResponseBody
    ResponseView removeAdminGames(@RequestParam(required = true) String gameIds,Integer adminId){
        ResponseView view = new ResponseView();
        try{

            Admin  admin = adminService.getAdminById(adminId);
            if(admin == null){
                return view.failure(Consts.Tips.AdminNotExists);
            }

            adminService.removeAdminGames(gameIds, admin);

        }catch(Exception e){
            return view.failure(e.toString());
        }
        return view.success();
    }

}
