package org.hye.controller;


import org.hye.entity.FileDTO;
import org.hye.entity.Mount;
import org.hye.service.IAdminService;
import org.hye.service.IMountService;
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
@RequestMapping("/mount")
public class MountController {
    @Resource
    IMountService mountService;

    @Resource
    IAdminService adminService;

    @PostMapping("/addMount")
    public Result<Integer> addMount(HttpServletRequest request, @RequestBody Mount mount)
    {
        if (adminService.ifCredValid(request).getInfo())
            return mountService.addMount(mount);
        return new Result<>("Credential not valid.", -1);
    }
    @PostMapping("/deleteMount")
    public Result<Integer> deleteMount(HttpServletRequest request, Integer mountId, Boolean deleteDiskFiles)
    {
        if (adminService.ifCredValid(request).getInfo())
            return mountService.deleteMount(mountId, deleteDiskFiles);
        return new Result<>("Credential not valid.", -1);
    }
    @PostMapping("/editMount")
    public Result<Integer> editMount(HttpServletRequest request, @RequestBody Mount mount)
    {
        if (adminService.ifCredValid(request).getInfo())
            return mountService.editMount(mount);
        return new Result<>("Credential not valid.", -1);
    }
    @GetMapping("/getFilesMtnId")
    public Result<List<FileDTO>> getFiles(HttpServletRequest request, Integer mountId)
    {
        if (adminService.ifCredValid(request).getInfo())
            return mountService.getFiles(mountId);
        return new Result<>("Credential not valid.", -1);
    }
    @GetMapping("/getFilesPath")
    public Result<List<FileDTO>> getFiles(HttpServletRequest request, String dirPath, Integer mountId, String mtnPath)
    {
        if (adminService.ifCredValid(request).getInfo())
            return mountService.getFiles(dirPath, mountId, mtnPath);
        return new Result<>("Credential not valid.", -1);
    }
    @GetMapping("/getMounts")
    public Result<List<Mount>> getMounts(HttpServletRequest request)
    {
        if (adminService.ifCredValid(request).getInfo())
            return mountService.getMounts();
        return new Result<>("Credential not valid.", -1);
    }
}

