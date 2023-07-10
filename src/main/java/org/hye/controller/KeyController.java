package org.hye.controller;


import org.hye.entity.Key;
import org.hye.service.IAdminService;
import org.hye.service.IKeyService;
import org.hye.util.Result;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/key")
public class KeyController {
    @Resource
    IKeyService keyService;

    @Resource
    IAdminService adminService;

    @PostMapping("/addKey")
    public Result<Integer> addKey(HttpServletRequest request, @RequestBody Key key)
    {
        if (adminService.ifCredValid(request).getInfo())
            return keyService.addKey(key);
        return new Result<>("Credential not valid.", -1);
    }
    @PostMapping("/deleteKey")
    public Result<Integer> deleteKey(HttpServletRequest request, Integer keyId)
    {
        if (adminService.ifCredValid(request).getInfo())
            return keyService.deleteKey(keyId);
        return new Result<>("Credential not valid.", -1);
    }
    @PostMapping("/editKey")
    public Result<Integer> editKey(HttpServletRequest request, @RequestBody Key key)
    {
        if (adminService.ifCredValid(request).getInfo())
            return keyService.editKey(key);
        return new Result<>("Credential not valid.", -1);
    }
    @PostMapping("/changePass")
    public Result<Integer> changePass(HttpServletRequest request, @RequestBody Key key)
    {
        if (adminService.ifCredValid(request).getInfo())
            return keyService.changePass(key);
        return new Result<>("Credential not valid.", -1);
    }
    @GetMapping("/getKeys")
    public Result<List<Key>> getKeys(HttpServletRequest request)
    {
        if (adminService.ifCredValid(request).getInfo())
            return keyService.getKeys();
        return new Result<>("Credential not valid.", -1);
    }

}

