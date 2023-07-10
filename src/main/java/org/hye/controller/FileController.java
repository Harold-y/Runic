package org.hye.controller;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.lang3.ArrayUtils;
import org.hye.entity.FileDecryptDTO;
import org.hye.entity.FileEncryptDTO;
import org.hye.service.IAdminService;
import org.hye.service.IFileService;
import org.hye.util.Result;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author HaroldCI
 * @since 2023-07-08
 */
@RestController
@RequestMapping("/file")
public class FileController {
    @Resource
    IFileService fileService;

    @Resource
    IAdminService adminService;

    @PostMapping("/protectFile")
    public Result<Integer> protectFile(HttpServletRequest request, @RequestBody FileEncryptDTO fileEncryptDTO)
    {
        if (adminService.ifCredValid(request).getInfo())
            return fileService.protectFile(fileEncryptDTO);
        return new Result<>("Credential not valid.", -1);
    }

    @PostMapping("/releaseFile")
    public Result<Integer> releaseFile(HttpServletRequest request, @RequestBody FileDecryptDTO fileDecryptDTO)
    {
        if (adminService.ifCredValid(request).getInfo())
            return fileService.releaseFile(fileDecryptDTO);
        return new Result<>("Credential not valid.", -1);
    }

    @GetMapping("/getDecrypted")
    public byte[] getDecrypted(HttpServletRequest request, MultipartFile file, @RequestParam String fileDecryptDTO)
    {
        if (adminService.ifCredValid(request).getInfo())
        {
            FileDecryptDTO fileDecryptDTO1 = JSONObject.parseObject(fileDecryptDTO, new TypeReference<FileDecryptDTO>(){});
            return ArrayUtils.toPrimitive(fileService.getDecrypted(file, fileDecryptDTO1).getInfo());
        }
        return null;
    }

    @GetMapping("/getDecryptedBase64Img")
    public Result<String> getDecryptedBase64Img(HttpServletRequest request, MultipartFile file, @RequestParam String fileDecryptDTO)
    {
        if (adminService.ifCredValid(request).getInfo())
        {
            FileDecryptDTO fileDecryptDTO1 = JSONObject.parseObject(fileDecryptDTO, new TypeReference<FileDecryptDTO>(){});
            return fileService.getDecryptedBase64Img(file, fileDecryptDTO1);
        }
        return new Result<>("NA", "Credential not valid.", -1);
    }

    @GetMapping("/getDecryptedPlainText")
    public Result<String> getDecryptedPlainText(HttpServletRequest request, MultipartFile file, @RequestParam String fileDecryptDTO)
    {
        if (adminService.ifCredValid(request).getInfo())
        {
            FileDecryptDTO fileDecryptDTO1 = JSONObject.parseObject(fileDecryptDTO, new TypeReference<FileDecryptDTO>(){});
            return fileService.getDecryptedPlainText(file, fileDecryptDTO1);
        }
        return new Result<>("NA", "Credential not valid.", -1);
    }

    @PostMapping("/saveEncryptedPlainText")
    public Result<Integer> saveEncryptedPlainText(HttpServletRequest request, Integer fId, String newContent, String password)
    {
        if (adminService.ifCredValid(request).getInfo())
            return fileService.saveEncryptedPlainText(fId, newContent, password);
        return new Result<>("Credential not valid.", -1);
    }
    @PostMapping("/uploadFile")
    public Result<Integer> uploadFile(HttpServletRequest request, MultipartFile multipartFile, String absPath, String name)
    {
        if (adminService.ifCredValid(request).getInfo())
            return fileService.uploadFile(multipartFile, absPath, name);
        return new Result<>("Credential not valid.", -1);
    }

    @PostMapping("/deleteFile")
    public Result<Integer> deleteFile(HttpServletRequest request, String absPath)
    {
        if (adminService.ifCredValid(request).getInfo())
            return fileService.deleteFile(absPath);
        return new Result<>("Credential not valid.", -1);
    }

    @PostMapping("/renameFile")
    public Result<Integer> renameFile(HttpServletRequest request, String absPath, String newName)
    {
        if (adminService.ifCredValid(request).getInfo())
            return fileService.renameFile(absPath, newName);
        return new Result<>("Credential not valid.", -1);
    }

    @PostMapping("/readFile")
    public byte[] readFile(HttpServletRequest request, String absPath)
    {
        if (adminService.ifCredValid(request).getInfo())
            return ArrayUtils.toPrimitive(fileService.readFile(absPath).getInfo());
        return null;
    }

    @PostMapping("/readPlainFileStr")
    public Result<String> readPlainFileStr(HttpServletRequest request, String absPath)
    {
        if (adminService.ifCredValid(request).getInfo())
            return fileService.readPlainFileStr(absPath);
        return new Result<>("", "Credential not valid.", -1);
    }

    @PostMapping("/readBase64Img")
    public Result<String> readBase64Img(HttpServletRequest request, String absPath)
    {
        if (adminService.ifCredValid(request).getInfo())
            return fileService.readBase64Img(absPath);
        return new Result<>("Credential not valid.", -1);
    }

    @PostMapping("/savePlainFile")
    public Result<Integer> savePlainFile(HttpServletRequest request, String absPath, String content)
    {
        if (adminService.ifCredValid(request).getInfo())
            return fileService.savePlainFile(absPath, content);
        return new Result<>("Credential not valid.", -1);
    }

    @PostMapping("/createFolder")
    public Result<Integer> createFolder(HttpServletRequest request, String absPath, String folderName)
    {
        if (adminService.ifCredValid(request).getInfo())
            return fileService.createFolder(absPath, folderName);
        return new Result<>("Credential not valid.", -1);
    }

    @PostMapping("/createFile")
    public Result<Integer> createFile(HttpServletRequest request, String absPath, String name)
    {
        if (adminService.ifCredValid(request).getInfo())
            return fileService.createFile(absPath, name);
        return new Result<>("Credential not valid.", -1);
    }

}

