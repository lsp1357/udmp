

package cn.com.git.udmp.modules.sys.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cn.com.git.udmp.common.persistence.Page;
import cn.com.git.udmp.common.persistence.Table;
import cn.com.git.udmp.common.utils.Collections3;
import cn.com.git.udmp.common.utils.StringUtils;
import cn.com.git.udmp.common.web.BaseController;
import cn.com.git.udmp.core.config.Global;
import cn.com.git.udmp.modules.sys.entity.Office;
import cn.com.git.udmp.modules.sys.entity.Role;
import cn.com.git.udmp.modules.sys.entity.User;
import cn.com.git.udmp.modules.sys.service.OfficeService;
import cn.com.git.udmp.modules.sys.service.SystemService;
import cn.com.git.udmp.modules.sys.utils.UserUtils;

/**
 * 角色Controller
 * 
 * @author Spring Cao
 * @version 2013-12-05
 */
@Controller
@RequestMapping(value = "${adminPath}/sys/role")
public class RoleController extends BaseController {

    @Autowired
    private SystemService systemService;

    @Autowired
    private OfficeService officeService;

    @ModelAttribute("role")
    public Role get(@RequestParam(required = false) String id) {
        if (StringUtils.isNotBlank(id)) {
            return systemService.getRole(id);
        } else {
            return new Role();
        }
    }

    @RequiresPermissions("sys:role:view")
    @RequestMapping(value = { "list", "" })
    public String list(Role role, Model model) {
        return "modules/sys/roleList";
    }

    @ResponseBody
    @RequiresPermissions("sys:role:view")
    @RequestMapping(value = { "listData" })
    public Table listData(Role role, HttpServletRequest request, HttpServletResponse response, Model model) {
        List<Role> list = systemService.findAllRole();
        Page<Role> page = new Page<Role>(request, response);
        page.setList(list);
        return new Table(page);
    }

    @RequiresPermissions("sys:role:view")
    @RequestMapping(value = "form")
    public String form(Role role, Model model) {
        if (role.getOffice() == null) {
            role.setOffice(UserUtils.getUser().getOffice());
        }
        model.addAttribute("role", role);
        model.addAttribute("menuList", systemService.findAllMenu());
        model.addAttribute("officeList", officeService.findAll());
        return "modules/sys/roleForm";
    }

    @ResponseBody
    @RequiresPermissions("sys:role:edit")
    @RequestMapping(value = "save")
    public Map<String, Object> save(Role role, Model model) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("success", false);
        if (!UserUtils.getUser().isAdmin() && role.getSysData().equals(Global.YES)) {
            result.put("message", "越权操作，只有超级管理员才能修改此数据！");
            return result;
        }
        if (Global.isDemoMode()) {
            result.put("message", "演示模式，不允许操作！");
            return result;
        }
        if (!beanValidator(model, role)) {
            result.put("message", model.asMap().get("message"));
            return result;
        }
        if (!"true".equals(checkName(role.getOldName(), role.getName()))) {
            result.put("message", "保存角色'" + role.getName() + "'失败，角色名已存在");
            return result;
        }
        if (!"true".equals(checkEnname(role.getOldEnname(), role.getEnname()))) {
            result.put("message", "保存角色'" + role.getName() + "'失败，英文名已存在");
            return result;
        }
        systemService.saveRole(role);
        result.put("success", true);
        result.put("message", "保存角色'" + role.getName() + "'成功");
        return result;
    }

    @ResponseBody
    @RequiresPermissions("sys:role:edit")
    @RequestMapping(value = "delete")
    public Map<String, Object> delete(Role role) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (!UserUtils.getUser().isAdmin() && role.getSysData().equals(Global.YES)) {
            result.put("message", "越权操作，只有超级管理员才能修改此数据！");
            result.put("success", false);
        } else if (Global.isDemoMode()) {
            result.put("message", "演示模式，不允许操作！");
            result.put("success", false);
        } else {
            systemService.deleteRole(role);
            result.put("message", "删除角色成功");
            result.put("success", true);
        }
        return result;
    }

    /**
     * 角色分配页面
     * 
     * @param role
     * @param model
     * @return
     */
    @RequiresPermissions("sys:role:edit")
    @RequestMapping(value = "assign")
    public String assign(Role role, Model model) {
        return "modules/sys/roleAssign";
    }

    /**
     * 获取角色分配的用户列表
     * 
     * @param role
     * @param request
     * @param response
     * @param model
     * @return
     */
    @ResponseBody
    @RequiresPermissions("sys:role:view")
    @RequestMapping(value = { "listAssignUser" })
    public Table listAssignUser(Role role, HttpServletRequest request, HttpServletResponse response, Model model) {
        List<User> list = systemService.findUser(new User(new Role(role.getId())));
        Page<User> page = new Page<User>(request, response);
        page.setList(list);
        return new Table(page);
    }

    /**
     * 角色分配 -- 打开角色分配对话框
     * 
     * @param role
     * @param model
     * @return
     */
    @RequiresPermissions("sys:role:view")
    @RequestMapping(value = "usertorole")
    public String selectUserToRole(Role role, Model model) {
        List<User> userList = systemService.findUser(new User(new Role(role.getId())));
        model.addAttribute("role", role);
        model.addAttribute("userList", userList);
        model.addAttribute("selectIds", Collections3.extractToString(userList, "name", ","));
        model.addAttribute("officeList", officeService.findAll());
        return "modules/sys/selectUserToRole";
    }

    /**
     * 角色分配 -- 根据部门编号获取用户列表
     * 
     * @param officeId
     * @param response
     * @return
     */
    @RequiresPermissions("sys:role:view")
    @ResponseBody
    @RequestMapping(value = "users")
    public List<Map<String, Object>> users(String officeId, HttpServletResponse response) {
        List<Map<String, Object>> mapList = Lists.newArrayList();
        User user = new User();
        user.setOffice(new Office(officeId));
        Page<User> page = systemService.findUser(new Page<User>(1, -1), user);
        for (User e : page.getList()) {
            Map<String, Object> map = Maps.newHashMap();
            map.put("id", e.getId());
            map.put("pId", 0);
            map.put("name", e.getName());
            mapList.add(map);
        }
        return mapList;
    }

    /**
     * 角色分配 -- 从角色中移除用户
     * 
     * @param userId
     * @param roleId
     * @return
     */
    @ResponseBody
    @RequiresPermissions("sys:role:edit")
    @RequestMapping(value = "outrole")
    public Map<String, Object> outrole(String userId, String roleId) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("success", false);
        if (Global.isDemoMode()) {
            result.put("message", "演示模式，不允许操作！");
        }
        Role role = systemService.getRole(roleId);
        User user = systemService.getUser(userId);
        if (UserUtils.getUser().getId().equals(userId)) {
            result.put("message", "无法从角色【" + role.getName() + "】中移除用户【" + user.getName() + "】自己！");
        } else {
            if (user.getRoleList().size() <= 1) {
                result.put("message", "用户【" + user.getName() + "】从角色【" + role.getName() + "】中移除失败！这已经是该用户的唯一角色，不能移除。");
            } else {
                Boolean flag = systemService.outUserInRole(role, user);
                if (!flag) {
                    result.put("message", "用户【" + user.getName() + "】从角色【" + role.getName() + "】中移除失败！");
                } else {
                    result.put("message", "用户【" + user.getName() + "】从角色【" + role.getName() + "】中移除成功！");
                    result.put("success", true);
                }
            }
        }
        return result;
    }

    /**
     * 角色分配
     * 
     * @param role
     * @param idsArr
     * @param redirectAttributes
     * @return
     */
    @ResponseBody
    @RequiresPermissions("sys:role:edit")
    @RequestMapping(value = "assignrole")
    public Map<String, Object> assignRole(Role role, String[] idsArr) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("success", false);
        if (Global.isDemoMode()) {
            result.put("message", "演示模式，不允许操作！");
            return result;
        }
        StringBuilder msg = new StringBuilder();
        int newNum = 0;
        for (int i = 0; i < idsArr.length; i++) {
            User user = systemService.assignUserToRole(role, systemService.getUser(idsArr[i]));
            if (null != user) {
                msg.append("<br/>新增用户【" + user.getName() + "】到角色【" + role.getName() + "】！");
                newNum++;
            }
        }
        result.put("message", "已成功分配 " + newNum + " 个用户" + msg);
        result.put("newNum", newNum);
        result.put("success", true);
        return result;
    }

    /**
     * 验证角色名是否有效
     * 
     * @param oldName
     * @param name
     * @return
     */
    @RequiresPermissions("user")
    @ResponseBody
    @RequestMapping(value = "checkName")
    public String checkName(String oldName, String name) {
        if (name != null && name.equals(oldName)) {
            return "true";
        } else if (name != null && systemService.getRoleByName(name) == null) {
            return "true";
        }
        return "false";
    }

    /**
     * 验证角色英文名是否有效
     * 
     * @param oldName
     * @param name
     * @return
     */
    @RequiresPermissions("user")
    @ResponseBody
    @RequestMapping(value = "checkEnname")
    public String checkEnname(String oldEnname, String enname) {
        if (enname != null && enname.equals(oldEnname)) {
            return "true";
        } else if (enname != null && systemService.getRoleByEnname(enname) == null) {
            return "true";
        }
        return "false";
    }

}
