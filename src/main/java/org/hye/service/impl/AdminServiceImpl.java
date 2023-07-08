package org.hye.service.impl;

import com.alibaba.fastjson2.util.UUIDUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import jakarta.annotation.Resource;
import org.hye.dao.CredentialMapper;
import org.hye.dao.SettingMapper;
import org.hye.entity.Admin;
import org.hye.dao.AdminMapper;
import org.hye.entity.Credential;
import org.hye.entity.Setting;
import org.hye.service.IAdminService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.hye.util.EncryptionPassUtil;
import org.hye.util.Result;
import org.hye.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author HaroldCI
 * @since 2023-07-08
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {
    @Resource
    AdminMapper adminMapper;

    @Resource
    CredentialMapper credentialMapper;

    @Resource
    SettingMapper settingMapper;

    @Value("${file.uploadFolder}")
    String uploadFolder;

    @Value("${cred.duration.hour}")
    Integer credDurationHour;

    private String credHelper(Admin admin) {
        String accessCred = UUIDUtil.generateUUID();

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime time = LocalDateTime.now();
        String localTime = df.format(time);
        Credential credential = new Credential();
        credential.setCredAdminId(admin.getAdminId());
        credential.setCredAccessKey(accessCred);
        credential.setCredIssueTime(localTime);

        credentialMapper.insert(credential);

        return accessCred;
    }

    public Result<Admin> login(String email, String password) {
        try {
            String encryptedPass = EncryptionPassUtil.doEncrypt(password);
            QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("admin_email", email);
            Admin admin = adminMapper.selectOne(queryWrapper);
            if (admin == null)
            {
                return new Result<>("Email incorrect.");
            }else if (!admin.getAdminPassword().equals(encryptedPass)) {
                return new Result<>("Password incorrect.");
            }

            String accessCred = credHelper(admin);
            admin.setAdminPassword(accessCred);
            return new Result<>(admin, "queried");
        } catch (Exception e) {
            return null;
        }
    }

    public Result<Admin> signup(String email, String name, String password)
    {
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("admin_email", email);
        Admin admin = adminMapper.selectOne(queryWrapper);
        if (admin != null)
        {
            return new Result<>("Email already registered.");
        }
        try {
            String encryptedPass = EncryptionPassUtil.doEncrypt(password);
            Admin admin1 = new Admin();
            admin1.setAdminPassword(encryptedPass);
            admin1.setAdminName(name);
            admin1.setAdminEmail(email);
            String uuid = UUIDUtil.generateUUID();
            admin1.setAdminUuid(uuid);

            adminMapper.insert(admin1);
            String accessKey = credHelper(admin1);
            admin1.setAdminPassword(accessKey);

            File file = new File(uploadFolder + System.getProperty("file.separator") + uuid);
            if (!file.exists())
                file.mkdir();
            return new Result<>(admin1, "Success.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public Result<Integer> logout(String accessKey)
    {
        QueryWrapper<Credential> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cred_access_key", accessKey);
        return new Result<>(credentialMapper.delete(queryWrapper), "logout");
    }

    public Result<Admin> getInfo(Integer adminId)
    {
        QueryWrapper<Admin> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("admin_id", adminId);
        Admin admin = adminMapper.selectOne(queryWrapper2);
        if (admin == null)
        {
            Admin unknown = new Admin();
            unknown.setAdminName("Runic Person");
            unknown.setAdminNote("Cannot Find This person's Info");
            return new Result<>(unknown, "Cannot find this user's info.");
        }
        admin.setAdminPassword("");
        return new Result<>(admin, "queried.");
    }

    public Result<Admin> getInfo(String accessKey)
    {
        QueryWrapper<Credential> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cred_access_key", accessKey);
        return getAdminHelper(queryWrapper);
    }

    private Result<Admin> getAdminHelper(QueryWrapper<Credential> queryWrapper) {
        Credential credential = credentialMapper.selectOne(queryWrapper);

        if (credential == null || credential.getCredAdminId() == null || credential.getCredIssueTime() == null)
        {
            return new Result<>("Credential do not exist.");
        }

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime before = LocalDateTime.parse(credential.getCredIssueTime(), df);
        Duration duration = Duration.between(before, now);
        long hours = duration.toHours();
        if (hours > credDurationHour)
        {
            return new Result<>("Credential expired.");
        }
        QueryWrapper<Admin> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("admin_id", credential.getCredAdminId());

        return new Result<>(adminMapper.selectOne(queryWrapper2), "queried.");
    }

    public Result<Integer> editInfo(String accessKey, Admin admin)
    {
        QueryWrapper<Credential> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cred_access_key", accessKey);
        Result<Admin> res1 = getAdminHelper(queryWrapper);
        Admin admin1 = res1.getInfo();
        if (admin1 == null || admin1.getAdminId() == null || !admin1.getAdminId().equals(admin.getAdminId()))
        {
            return new Result<>("Fail to locate user.");
        }
        UpdateWrapper<Admin> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("admin_id", admin.getAdminId());
        if ( admin.getAdminName() != null && !admin.getAdminName().equals("") )
            updateWrapper.set("admin_name", admin.getAdminName());
        if ( admin.getAdminEmail() != null && !admin.getAdminEmail().equals("") )
        {
            QueryWrapper<Admin> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.eq("admin_email", admin.getAdminEmail());
            Admin admin2 = adminMapper.selectOne(queryWrapper2);
            if (admin2 != null && !admin2.getAdminId().equals(admin1.getAdminId())) {
                return new Result<>("Email already in use.");
            }
            updateWrapper.set("admin_email", admin.getAdminEmail());
        }
        if ( admin.getAdminNote() != null )
            updateWrapper.set("admin_note", admin.getAdminNote());

        return new Result<>(adminMapper.update(admin1, updateWrapper), "updated.");
    }
    public byte[] getAvatarHelper(String userUUID)
    {
        File file = new File(uploadFolder + System.getProperty("file.separator") + "userImg" + System.getProperty("file.separator") + userUUID + ".jpg");
        if (!file.exists())
            return new byte[]{};
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes, 0, inputStream.available());
            return bytes;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public byte[] getAvatar(Integer userId)
    {
        String uuid = adminMapper.selectById(userId).getAdminUuid();
        return getAvatarHelper(uuid);
    }

    public byte[] getAvatar(String accessKey)
    {
        QueryWrapper<Credential> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cred_access_key", accessKey);
        Result<Admin> res1 = getAdminHelper(queryWrapper);
        Admin admin1 = res1.getInfo();
        if (admin1 == null || admin1.getAdminId() == null)
        {
            return new byte[]{};
        }
        return getAvatarHelper(admin1.getAdminUuid());
    }

    public Result<Integer> updateAvatar(String accessKey, MultipartFile img)
    {
        QueryWrapper<Credential> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cred_access_key", accessKey);
        Result<Admin> res1 = getAdminHelper(queryWrapper);
        Admin admin1 = res1.getInfo();
        if (admin1 == null || admin1.getAdminId() == null)
        {
            return new Result<>("Cannot locate user.");
        }

        String uuid = admin1.getAdminUuid();
        String folder = uploadFolder + System.getProperty("file.separator") + uuid;
        File folderDir = new File(folder);
        if (!folderDir.exists())
            folderDir.mkdir();
        File file = new File(uploadFolder + System.getProperty("file.separator") + "userImg" + System.getProperty("file.separator") + uuid + ".jpg");
        try {
            img.transferTo(file);
            return new Result<>("Updated.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Change password
     * @param adminId the id of admin
     * @param oldPassword old password
     * @param newPassword new password
     * @return DB # of changed rows (normally 1).
     */
    public Result<Integer> changePassword(Integer adminId, String oldPassword, String newPassword)
    {
        int retStatus;
        try {
            String encryptedOldPass = EncryptionPassUtil.doEncrypt(oldPassword);
            QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("admin_id", adminId);
            Admin admin = adminMapper.selectOne(queryWrapper);
            if (admin == null || !admin.getAdminPassword().equals(encryptedOldPass))
            {
                return new Result<>("Incorrect old password.");
            }
        } catch (Exception e) {
            return new Result<>("Encryption error.");
        }

        try {
            String encrypted = EncryptionPassUtil.doEncrypt(newPassword);
            UpdateWrapper<Admin> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("admin_id", adminId);
            updateWrapper.set("admin_password", encrypted);
            retStatus = adminMapper.update(null, updateWrapper);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new Result<>(retStatus, "updated.");
    }

    public Result<Integer> changePassword(String accessKey, String oldPassword, String newPassword)
    {
        int retStatus;
        QueryWrapper<Credential> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cred_access_key", accessKey);
        Result<Admin> res1 = getAdminHelper(queryWrapper);
        Admin admin1 = res1.getInfo();
        if (admin1 == null || admin1.getAdminId() == null)
        {
            return new Result<>("Cannot locate user.");
        }
        int userId = admin1.getAdminId();
        return changePassword(userId, oldPassword, newPassword);
    }
}
