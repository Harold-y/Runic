package org.hye.controller;


import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.hye.entity.Admin;
import org.hye.service.IAdminService;
import org.hye.util.Result;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author HaroldCI
 * @since 2023-07-08
 */
@RestController
@RequestMapping("/admin")
public class AdminController {
    @Resource
    IAdminService adminService;

    @PostMapping("/login")
    public Result<Admin> login(String email, String password)
    {
        return adminService.login(email, password);
    }

    @PostMapping("/signup")
    public Result<Admin> signup(String email, String name, String password)
    {
        return adminService.signup(email, name, password);
    }
    @PostMapping("/logout")
    public Result<Integer> logout(HttpSession session)
    {
        String accessKey = (String) session.getAttribute("accessKey");
        return adminService.logout(accessKey);
    }
    @GetMapping("/getInfoId")
    public Result<Admin> getInfo(Integer adminId)
    {
        return adminService.getInfo(adminId);
    }
    @GetMapping("/getInfo")
    public Result<Admin> getInfo(HttpSession session)
    {
        String accessKey = (String) session.getAttribute("accessKey");
        return adminService.getInfo(accessKey);
    }
    @PostMapping("/editInfo")
    public Result<Integer> editInfo(HttpSession session, @RequestBody Admin admin)
    {
        String accessKey = (String) session.getAttribute("accessKey");
        return adminService.editInfo(accessKey, admin);
    }
    @GetMapping("/getAvatarId")
    public byte[] getAvatar(Integer userId)
    {
        return adminService.getAvatar(userId);
    }
    @GetMapping("/getAvatar")
    public byte[] getAvatar(HttpSession session)
    {
        String accessKey = (String) session.getAttribute("accessKey");
        return adminService.getAvatar(accessKey);
    }
    @PostMapping("/updateAvatar")
    public Result<Integer> updateAvatar(HttpSession session, MultipartFile img)
    {
        String accessKey = (String) session.getAttribute("accessKey");
        return adminService.updateAvatar(accessKey, img);
    }
    @PostMapping("/changePassword")
    public Result<Integer> changePassword(HttpSession session, String oldPassword, String newPassword)
    {
        String accessKey = (String) session.getAttribute("accessKey");
        return adminService.changePassword(accessKey, oldPassword, newPassword);
    }
}

