package org.hye.controller;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hye.entity.Admin;
import org.hye.service.IAdminService;
import org.hye.util.Result;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    public Result<Integer> logout(HttpServletRequest request)
    {
        String accessKey = request.getHeader("accessKey");
        return adminService.logout(accessKey);
    }
    @GetMapping("/getInfoId")
    public Result<Admin> getInfo(Integer adminId)
    {
        return adminService.getInfo(adminId);
    }
    @GetMapping("/getInfo")
    public Result<Admin> getInfo(HttpServletRequest request)
    {
        String accessKey = request.getHeader("accessKey");
        return adminService.getInfo(accessKey);
    }
    @PostMapping("/editInfo")
    public Result<Integer> editInfo(HttpServletRequest request, @RequestBody Admin admin)
    {
        String accessKey = request.getHeader("accessKey");
        return adminService.editInfo(accessKey, admin);
    }
    @GetMapping(value = "/getAvatarId", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getAvatar(Integer userId)
    {
        return adminService.getAvatar(userId);
    }
    @GetMapping(value = "/getAvatar", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getAvatar(HttpServletRequest request)
    {
        String accessKey = request.getHeader("accessKey");
        return adminService.getAvatar(accessKey);
    }
    @PostMapping("/updateAvatar")
    public Result<Integer> updateAvatar(HttpServletRequest request, MultipartFile img)
    {
        String accessKey = request.getHeader("accessKey");
        return adminService.updateAvatar(accessKey, img);
    }
    @PostMapping("/changePassword")
    public Result<Integer> changePassword(HttpServletRequest request, String oldPassword, String newPassword)
    {
        String accessKey = request.getHeader("accessKey");
        return adminService.changePassword(accessKey, oldPassword, newPassword);
    }
    @GetMapping("/getPeople")
    public Result<List<Admin>> getPeople(HttpServletRequest request)
    {
        String accessKey = request.getHeader("accessKey");
        return adminService.getPeople(accessKey);
    }
}

