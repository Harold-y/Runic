package org.hye.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.commons.io.FileUtils;
import org.hye.dao.FileMapper;
import org.hye.entity.FileDTO;
import org.hye.entity.Mount;
import org.hye.dao.MountMapper;
import org.hye.service.IMountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.hye.util.Result;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author HaroldCI
 * @since 2023-07-08
 */
@Service
public class MountServiceImpl extends ServiceImpl<MountMapper, Mount> implements IMountService {
    @Resource
    MountMapper mountMapper;

    @Resource
    FileMapper fileMapper;

    private Result<Integer> checkMount(Mount mount)
    {
        if (mount.getMtnPath() == null || mount.getMtnPath().equals(""))
            return new Result<>("Mount path cannot be empty.", -1);
        File dir = new File(mount.getMtnPath());
        if (dir.exists() && !dir.isDirectory())
            return new Result<>("Mount must be a folder.", -1);
        if (!dir.exists())
            if (!dir.mkdirs())
                return new Result<>("Error creating a folder.", -1);
        return new Result<>(1, dir.getName(), 0);
    }
    @Override
    public Result<Integer> addMount(Mount mount) {
        Result<Integer> check = checkMount(mount);
        if (check.getCode() == -1)
            return check;
        if (mount.getMtnName() == null || mount.getMtnName().equals(""))
            mount.setMtnName(check.getMsg());
        mount.setMtnId(null);
        String mtnPath = mount.getMtnPath().replaceAll("\\\\", "/");
        mount.setMtnPath(mtnPath);
        return new Result<>(mountMapper.insert(mount), "insert.", 0);
    }

    @Override
    public Result<Integer> deleteMount(Integer mountId, Boolean deleteDiskFiles) {
        Mount mount = mountMapper.selectById(mountId);
        if (mount == null)
            return new Result<>("No such mount.", -1);
        String mountPath = mount.getMtnPath();
        int info = mountMapper.deleteById(mountId);
        if (info > 0 && deleteDiskFiles)
        {
            try {
                FileUtils.deleteDirectory(new File(mountPath));
            } catch (IOException e) {
                System.out.println(e.getMessage());
                return new Result<>("Error deleting the folder.", -1);
            }
        }
        return new Result<>(mountMapper.deleteById(mountId), "delete.", 0);
    }

    @Override
    public Result<Integer> editMount(Mount mount) {
        if (mount.getMtnId() == null)
            return new Result<>("Mount id cannot be empty.", -1);
        UpdateWrapper<Mount> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("mtn_id", mount.getMtnId());

        String newName = "folderX";
        if (mount.getMtnPath() != null && !mount.getMtnPath().equals(""))
        {
            Result<Integer> check = checkMount(mount);
            if (check.getCode() == -1)
                return check;
            newName = check.getMsg();
            updateWrapper.set("mtn_path", mount.getMtnPath().replaceAll("\\\\", "/"));
        }
        if (mount.getMtnName() != null && !mount.getMtnName().equals(""))
            updateWrapper.set("mtn_name", mount.getMtnName());
        else
            updateWrapper.set("mtn_name", newName);
        return new Result<>(mountMapper.update(null, updateWrapper), "update.", 0);
    }

    @Override
    public Result<List<FileDTO>> getFiles(Integer mountId) {
        Mount mount = mountMapper.selectById(mountId);
        if (mount == null)
            return new Result<>("No such mount.", -1);
        String mountPath = mount.getMtnPath();
        return getFiles(mountPath, mountId, mountPath);
    }
    private org.hye.entity.File ifContained(String childPath, Integer mountId, String mtnPath)
    {
        QueryWrapper<org.hye.entity.File> queryWrapper = new QueryWrapper<>();
        String confirmPath = childPath.split(mtnPath)[1];
        queryWrapper.eq("f_mtn_id", mountId);
        queryWrapper.eq("f_path", confirmPath);
        return fileMapper.selectOne(queryWrapper);
    }
    private String convertLength(long byteLength)
    {
        if (byteLength < 1024)
            return byteLength + " Byte";
        double numKb = (double) byteLength / 1024;
        if (numKb < 1024)
            return String.format("%.2f", numKb) + " KB";
        double numMb = numKb / 1024;
        if (numMb < 1024)
            return String.format("%.2f", numMb) + " MB";
        double numGb = numMb / 1024;
        return String.format("%.2f", numGb) + " GB";
    }
    private String getRepresentedSize(File file)
    {
        long byteLength = file.length();
        return convertLength(byteLength);
    }

    @Override
    public Result<List<FileDTO>> getFiles(String dirPath, Integer mountId, String mtnPath) {
        dirPath = dirPath.replaceAll("\\\\", "/");
        mtnPath = mtnPath.replaceAll("\\\\", "/");

        File folder = new File(dirPath);
        if (!folder.exists())
            return new Result<>("Error of accessing un-existing folder.", -1);
        if (!folder.isDirectory())
            return new Result<>("Cannot traverse a file for a folder.", -1);
        List<FileDTO> res = new ArrayList<>();
        File[] files = folder.listFiles();
        if (files == null)
            return new Result<>("Error of getting list of files contained in a folder.", -1);
        for (final File fileEntry : files)
        {
            FileDTO current = new FileDTO();

            String childPath = fileEntry.getAbsolutePath().replaceAll("\\\\", "/");
            org.hye.entity.File file = ifContained(childPath, mountId, mtnPath);
            if (file != null) {
                current.setFId(file.getFId());
                current.setFName(file.getFName());
                current.setFKeyId(file.getFKeyId());
                current.setFEncryptedType(file.getFEncryptedType());
                current.setFPath(file.getFPath());
            }else {
                current.setFName(fileEntry.getName());
            }
            if (fileEntry.isDirectory())
            {
                current.setFType("dir");
                current.setFSize(convertLength(FileUtils.sizeOfDirectory(fileEntry)));
            }else {
                String[] extensionLists = fileEntry.getPath().split("\\.");
                String extension = "";
                if (extensionLists.length > 2)
                    extension = extensionLists[extensionLists.length - 2];
                else
                    extension = extensionLists[extensionLists.length - 1];

                current.setFType(extension);
                current.setFSize(getRepresentedSize(fileEntry));
            }
            current.setAbsPath(fileEntry.getAbsolutePath());
            res.add(current);
        }
        return new Result<>(res, "queried.", 0);
    }

    @Override
    public Result<List<Mount>> getMounts() {
        QueryWrapper<Mount> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("mtn_id", "mtn_name", "mtn_path");
        return new Result<>(mountMapper.selectList(queryWrapper), "select.", 0);
    }
}
