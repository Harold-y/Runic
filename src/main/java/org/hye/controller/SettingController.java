package org.hye.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.hye.entity.Admin;
import org.hye.entity.Credential;
import org.hye.entity.Setting;
import org.hye.service.IAdminService;
import org.hye.service.ISettingService;
import org.hye.util.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
@RequestMapping("/setting")
public class SettingController {
    @Resource
    ISettingService settingService;

    @Resource
    IAdminService adminService;


    @PostMapping("/addSetting")
    public Result<Integer> addSetting(HttpServletRequest request, String name, String value)
    {
        if (adminService.ifCredValid(request).getInfo())
            return settingService.addSetting(name, value);
        return new Result<>(-1, "credential not valid.", -1);
    }

    @PostMapping("/removeSetting")
    public Result<Integer> removeSetting(HttpServletRequest request, Integer settingId)
    {
        if (adminService.ifCredValid(request).getInfo())
            return settingService.removeSetting(settingId);
        return new Result<>(-1, "credential not valid.", -1);
    }

    @PostMapping("/editSetting")
    public Result<Integer> editSetting(HttpServletRequest request, Integer settingId, String name, String value)
    {
        if (adminService.ifCredValid(request).getInfo())
            return settingService.editSetting(settingId, name, value);
        return new Result<>(-1, "credential not valid.", -1);
    }

    @GetMapping("/getSettings")
    public Result<List<Setting>> getSettings(HttpServletRequest request)
    {
        if (adminService.ifCredValid(request).getInfo())
            return settingService.getSettings();
        return new Result<>(null, "credential not valid.", -1);
    }
}

