package org.hye.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.hye.dao.KeyMapper;
import org.hye.dao.MountMapper;
import org.hye.entity.*;
import org.hye.dao.FileMapper;
import org.hye.entity.File;
import org.hye.service.IFileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.hye.util.EncryptionFileUtil;
import org.hye.util.EncryptionPassUtil;
import org.hye.util.Result;
import org.hye.util.UUIDUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author HaroldCI
 * @since 2023-07-08
 */
@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements IFileService {

    @Resource
    FileMapper fileMapper;

    @Resource
    KeyMapper keyMapper;

    @Resource
    MountMapper mountMapper;

    @Override
    public Result<Integer> protectFile(FileEncryptDTO fileEncryptDTO) {
        String absPath = fileEncryptDTO.getAbsPath();
        if (absPath == null || absPath.equals(""))
            return new Result<>(-1, "Empty path.", -1);
        java.io.File file = new java.io.File(absPath);
        if (!file.exists())
            return new Result<>(-1, "File not exist.", -1);
        try {
            // Select Key from DB
            if (fileEncryptDTO.getSelectFromKeys())
            {
                Key key = keyMapper.selectById(fileEncryptDTO.getKeyId());
                if (key == null)
                    return new Result<>(-1, "Error: key is not present.", -1);
                if (key.getKeyEncryptMethod().equals("simple"))
                    fileEncryptDTO.setPassword(EncryptionPassUtil.doDecrypt(key.getKeyEncrypted()));
                else
                    fileEncryptDTO.setPassword(EncryptionPassUtil.doDecryptWithKey(key.getKeyEncrypted(), fileEncryptDTO.getKeyPassword()));
                fileEncryptDTO.setKeyId(key.getKeyId());
            }else
                fileEncryptDTO.setKeyId(-1);
            Mount mount = mountMapper.selectById(fileEncryptDTO.getMtnId());
            if (mount == null)
                return new Result<>(-1, "Error: mount is not present.", -1);
            Path path = Paths.get(absPath);
            String directory = path.getParent().toString();
            File addFile = new File();
            if (file.isDirectory())
            {
                EncryptionFileUtil.doEncryptFolder(absPath, directory, fileEncryptDTO.getPassword(), fileEncryptDTO.getGeneratePubKey());
                addFile.setFEncryptedType("dir");
            }else {
                EncryptionFileUtil.doEncrypt(absPath, directory, fileEncryptDTO.getPassword(), fileEncryptDTO.getGeneratePubKey());
                addFile.setFEncryptedType("file");
            }
            int startPos = absPath.indexOf(mount.getMtnPath());
            int endingPos = startPos + mount.getMtnPath().length();

            addFile.setFId(null);
            addFile.setFName(file.getName());
            addFile.setFPath(absPath.substring(endingPos).replaceAll("\\\\", "/") + ".runic");
            addFile.setFKeyId(fileEncryptDTO.getKeyId());
            addFile.setFMtnId(fileEncryptDTO.getMtnId());
            return new Result<>(fileMapper.insert(addFile), "Insert.", 0);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result<>(0, "Error encrypting the file.", -1);
        }
    }

    private Result<Byte[]> decryptHelper(MultipartFile multipartFile, FileDecryptDTO fileDecryptDTO, boolean write, String writeAddr)
    {
        Integer fId = fileDecryptDTO.getFId();
        try {
            if (fId == null)
            {
                uploadFile(multipartFile, "tmp", multipartFile.getName());
                byte[] res = EncryptionFileUtil.doDecrypt("tmp/" + multipartFile.getName(), fileDecryptDTO.getPassword(),
                        fileDecryptDTO.getGivePasswordString(), fileDecryptDTO.getPubKeyAbsPath(), write, writeAddr);
                java.io.File delF = new java.io.File("tmp/" + multipartFile.getName());
                delF.delete();
                return new Result<>(ArrayUtils.toObject(res), "helper.", 0);
            }else {
                File file = fileMapper.selectById(fId);
                Integer mtnId = file.getFMtnId();
                Mount mount = mountMapper.selectById(mtnId);
                String absPath = mount.getMtnPath() + "/" + file.getFPath();
                String suffix = file.getFEncryptedType();
                byte[] res = new byte[0];
                if (suffix.equals("dir"))
                {
                    EncryptionFileUtil.doDecryptFolder(absPath, fileDecryptDTO.getPassword(),
                            fileDecryptDTO.getGivePasswordString(), fileDecryptDTO.getPubKeyAbsPath(), write, writeAddr);
                }else {
                    res = EncryptionFileUtil.doDecrypt(absPath, fileDecryptDTO.getPassword(),
                            fileDecryptDTO.getGivePasswordString(), fileDecryptDTO.getPubKeyAbsPath(), write, writeAddr);
                }
                return new Result<>(ArrayUtils.toObject(res), "helper.", 0);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            return new Result<>(null, "error occurred.", -1);
        }
    }

    @Override
    public Result<Integer> releaseFile(FileDecryptDTO fileDecryptDTO) {
        if (fileDecryptDTO.getFId() == null)
            return new Result<>(0, "No file Id provided.", -1);
        java.io.File file = new java.io.File(fileDecryptDTO.getTargetAbsPath());
        if (file.isFile())
            return new Result<>(0, "Cannot choose a file target.", -1);
        Result<Byte[]> res = decryptHelper(null, fileDecryptDTO, true, fileDecryptDTO.getTargetAbsPath());
        if (res.getCode() != -1)
            return new Result<>(1, "release.", 0);
        return new Result<>(0, res.getMsg(), -1);
    }

    @Override
    public Result<Byte[]> getDecrypted(MultipartFile multipartFile, FileDecryptDTO fileDecryptDTO) {
        return decryptHelper(multipartFile, fileDecryptDTO, false, "");
    }

    @Override
    public Result<String> getDecryptedBase64Img(MultipartFile multipartFile, FileDecryptDTO fileDecryptDTO) {
        Result<Byte[]> res = decryptHelper(multipartFile, fileDecryptDTO, false, "");
        if (res.getCode() != -1)
            return new Result<>(Base64.encodeBase64String(ArrayUtils.toPrimitive(res.getInfo())), "get base64 image encrypted.", 0);
        return new Result<>("NA", res.getMsg(), -1);
    }

    @Override
    public Result<String> getDecryptedPlainText(MultipartFile multipartFile, FileDecryptDTO fileDecryptDTO) {
        Result<Byte[]> res = decryptHelper(multipartFile, fileDecryptDTO, false, "");
        if (res.getCode() != -1)
            return new Result<>(new String(ArrayUtils.toPrimitive(res.getInfo()), StandardCharsets.UTF_8), "get base64 image encrypted.", 0);
        return new Result<>("NA", res.getMsg(), -1);
    }

    @Override
    public Result<Integer> saveEncryptedPlainText(Integer fId, String newContent, String password) {
        File file = fileMapper.selectById(fId);
        if (file == null)
            return new Result<>(-1, "file is not contained in DB.", -1);
        try {
            Mount mount = mountMapper.selectById(file.getFMtnId());
            String prefixPath = mount.getMtnPath();
            String randomPlace = UUIDUtil.generateUUID();

            java.io.File tempFile = new java.io.File("tmp/" + randomPlace);
            if (!tempFile.exists())
                tempFile.mkdirs();
            if (!file.getFKeyId().equals(-1))
            {
                Key key = keyMapper.selectById(file.getFKeyId());
                if (key.getKeyEncryptMethod().equals("simple"))
                {
                    password = EncryptionPassUtil.doDecrypt(key.getKeyEncrypted());
                }else {
                    password = EncryptionPassUtil.doDecryptWithKey(key.getKeyEncrypted(), password);
                }
            }
            EncryptionFileUtil.doEncrypt(prefixPath + "/" + file.getFPath(), "tmp/" + randomPlace, password, false);
            java.io.File originalFile = new java.io.File(prefixPath + "/" + file.getFPath());
            originalFile.delete();
            java.io.File tempFile2 = new java.io.File("tmp/" + randomPlace + "/" + file.getFName());
            FileUtils.copyFile(tempFile2, new java.io.File(prefixPath + "/" + file.getFPath()));

            return new Result<>(1, "save.", 0);
        } catch (Exception e)
        {
            e.printStackTrace();
            return new Result<>(-1, "Encryption error.", -1);
        }
    }

    @Override
    public Result<Integer> uploadFile(MultipartFile multipartFile, String absPath, String name) {
        java.io.File folderDir = new java.io.File(absPath);
        if (!folderDir.exists())
            folderDir.mkdirs();
        java.io.File file = new java.io.File(absPath + "/" + name);
        try {
            multipartFile.transferTo(file);
            return new Result<>(1, "Upload.", 0);
        } catch (IOException e) {
            e.printStackTrace();
            return new Result<>(0, "Error uploading the file.", -1);
        }
    }

    @Override
    public Result<Byte[]> readFile(String absPath) {
        java.io.File file = new java.io.File(absPath);
        try {
            byte[] res = FileUtils.readFileToByteArray(file);
            return new Result<>(ArrayUtils.toObject(res), "read file", 0);
        } catch (IOException e) {
            e.printStackTrace();
            return new Result<>(null, "Error reading the file.", -1);
        }
    }

    @Override
    public Result<String> readPlainFileStr(String absPath) {
        try {
            java.io.File file = new java.io.File(absPath);
            if(file.exists() && file.isFile())
            {
                InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuilder builder = new StringBuilder();
                String text = null;
                while ((text = bufferedReader.readLine()) != null)
                    builder.append(text).append("\n");
                bufferedReader.close();
                reader.close();
                return new Result<>(builder.toString(), "read plain.", -1);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            return new Result<>("NA", "error of reading the file.", -1);
        }
        return new Result<>("NA", "error of reading the file.", -1);
    }

    @Override
    public Result<Integer> deleteFile(String absPath) {
        java.io.File f = new java.io.File(absPath);
        if(f.exists() && f.isDirectory()) {
            try {
                FileUtils.deleteDirectory(f);
            } catch (IOException e) {
                e.printStackTrace();
                return new Result<>(0, "error of deleting the folder.", -1);
            }
            return new Result<>(1, "delete.", 0);
        }else
        {
            int res = f.delete() ? 1 : 0;
            int code = res == 1 ? 0 : -1;
            return new Result<>(res, "delete.", code);
        }
    }

    @Override
    public Result<String> readBase64Img(String absPath) {
        try {
            java.io.File file = new java.io.File(absPath);
            if(file.exists() && file.isFile())
            {
                InputStream fInput = new FileInputStream(file);
                byte[] imageBytes = new byte[(int)file.length()];
                fInput.read(imageBytes, 0, imageBytes.length);
                fInput.close();
                return new Result<>(Base64.encodeBase64String(imageBytes), "Read Base64 Image.", -1);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            return new Result<>("NA", "Error reading image.", -1);
        }
        return new Result<>("NA", "Error reading image.", -1);
    }

    @Override
    public Result<Integer> renameFile(String absPath, String newName) {
        java.io.File f = new java.io.File(absPath);
        Path path1 = Paths.get(absPath);
        Path parentPath = path1.getParent();
        String parentName = parentPath.toString();
        if(!f.exists())
            return new Result<>(0, "error of locating the file.", -1);
        int res = f.renameTo(new java.io.File(parentName + "/" + newName)) ? 1 : 0;
        int code = res == 1 ? 0 : -1;

        return new Result<>(res, "rename.", code);
    }

    @Override
    public Result<Integer> savePlainFile(String absPath, String content) {
        try {
            java.io.File file = new java.io.File(absPath);
            FileUtils.writeStringToFile(file, content, "UTF-8", false);
            return new Result<>(1, "success", 0);
        } catch (Exception e)
        {
            e.printStackTrace();
            return new Result<>(0, "error of writing to file.", -1);
        }
    }

    @Override
    public Result<Integer> createFolder(String absPath, String folderName) {
        java.io.File f = new java.io.File(absPath + "/" + folderName);
        if(f.exists() && f.isDirectory())
            return new Result<>(0, "folder already exist.", -1);
        int res = f.mkdirs() ? 1 : 0;
        int code = res == 1 ? 0 : -1;
        return new Result<>(res, "create folder.", code);
    }

    @Override
    public Result<Integer> createFile(String absPath, String name) {
        java.io.File f = new java.io.File(absPath + "/" + name);
        if(f.exists())
            return new Result<>(0, "file already exist.", -1);
        try {
            int res = f.createNewFile() ? 1 : 0;
            int code = res == 1 ? 0 : -1;
            return new Result<>(res, "create file.", code);
        } catch (IOException e)
        {
            e.printStackTrace();
            return new Result<>(0, "error creating file.", -1);
        }
    }
}
