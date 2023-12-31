package org.hye.service.impl;

import com.alibaba.fastjson2.util.UUIDUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
import org.springframework.beans.factory.annotation.Autowired;
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

    public Result<Boolean> ifCredValid(HttpServletRequest request)
    {
        String accessKey = request.getHeader("accessKey");
        QueryWrapper<Credential> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cred_access_key", accessKey);
        Result<Admin> res1 = getAdminHelper(queryWrapper);
        Admin admin1 = res1.getInfo();
        if (admin1 == null || admin1.getAdminId() == null)
        {
            return new Result<>(false, "Cannot locate user.", -1);
        }
        return new Result<>(true, "Success.", 0);
    }

    public Result<Admin> login(String email, String password) {
        try {

            QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("admin_email", email);
            Admin admin = adminMapper.selectOne(queryWrapper);
            if (admin == null)
            {
                return new Result<>("Email incorrect.", -1);
            }
            String decryptedPass = EncryptionPassUtil.doDecrypt(admin.getAdminPassword());
            if (!decryptedPass.equals(password)) {
                return new Result<>("Password incorrect.", -1);
            }

            String accessCred = credHelper(admin);
            admin.setAdminPassword(accessCred);
            return new Result<>(admin, "queried", 1);
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
            return new Result<>("Email already registered.", -1);
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

            admin = adminMapper.selectOne(queryWrapper);
            int adminId = admin.getAdminId();
            admin1.setAdminId(adminId);
            String accessKey = credHelper(admin1);
            admin1.setAdminPassword(accessKey);

            /*
            File file = new File(uploadFolder + "/" + uuid);
            if (!file.exists())
                file.mkdir();
             */
            return new Result<>(admin1, "Success.", 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public Result<Integer> logout(String accessKey)
    {
        QueryWrapper<Credential> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cred_access_key", accessKey);
        Credential credential = credentialMapper.selectOne(queryWrapper);
        if (credential == null || credential.getCredAdminId() == null)
            return new Result<>("error: wrong credential", -1);
        int adminId = credential.getCredAdminId();
        QueryWrapper<Credential> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("cred_admin_id", adminId);
        return new Result<>(credentialMapper.delete(queryWrapper2), "logout", 0);
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
            return new Result<>(unknown, "Cannot find this user's info.", -1);
        }
        admin.setAdminPassword("");
        return new Result<>(admin, "queried.", 0);
    }

    public Result<Admin> getInfo(String accessKey)
    {
        QueryWrapper<Credential> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cred_access_key", accessKey);
        return getAdminHelper(queryWrapper);
    }

    public Result<Admin> getAdminHelper(QueryWrapper<Credential> queryWrapper) {
        Credential credential = credentialMapper.selectOne(queryWrapper);

        if (credential == null || credential.getCredAdminId() == null || credential.getCredIssueTime() == null)
        {
            return new Result<>("Credential do not exist.", -1);
        }

        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime before = LocalDateTime.parse(credential.getCredIssueTime(), df);
        Duration duration = Duration.between(before, now);
        long hours = duration.toHours();
        if (hours > credDurationHour)
        {
            return new Result<>("Credential expired.", -1);
        }
        QueryWrapper<Admin> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("admin_id", credential.getCredAdminId());
        Admin admin = adminMapper.selectOne(queryWrapper2);
        if (admin != null)
            admin.setAdminPassword("");

        return new Result<>(admin, "queried.", 0);
    }

    public Result<Integer> editInfo(String accessKey, Admin admin)
    {
        QueryWrapper<Credential> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cred_access_key", accessKey);
        Result<Admin> res1 = getAdminHelper(queryWrapper);
        Admin admin1 = res1.getInfo();
        if (admin1 == null || admin1.getAdminId() == null)
        {
            return new Result<>("Fail to locate user.", -1);
        }
        UpdateWrapper<Admin> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("admin_id", admin1.getAdminId());
        if ( admin.getAdminName() != null && !admin.getAdminName().equals("") )
            updateWrapper.set("admin_name", admin.getAdminName());
        if ( admin.getAdminEmail() != null && !admin.getAdminEmail().equals("") )
        {
            QueryWrapper<Admin> queryWrapper2 = new QueryWrapper<>();
            queryWrapper2.eq("admin_email", admin.getAdminEmail());
            Admin admin2 = adminMapper.selectOne(queryWrapper2);
            if (admin2 != null && !admin2.getAdminId().equals(admin1.getAdminId())) {
                return new Result<>("Email already in use.", -1);
            }
            updateWrapper.set("admin_email", admin.getAdminEmail());
        }
        if ( admin.getAdminNote() != null )
            updateWrapper.set("admin_note", admin.getAdminNote());

        return new Result<>(adminMapper.update(null, updateWrapper), "updated.", 0);
    }
    public byte[] getAvatarHelper(String userUUID)
    {
        File file = new File(uploadFolder + "/" + "userImg" + "/" + userUUID + ".jpg");
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
        Admin selected = adminMapper.selectById(userId);
        if (selected == null)
            return new byte[]{};
        String uuid = selected.getAdminUuid();
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
            return new Result<>("Cannot locate user.", -1);
        }

        String uuid = admin1.getAdminUuid();
        String folder = uploadFolder + "/" + "userImg";
        File folderDir = new File(folder);
        if (!folderDir.exists())
            folderDir.mkdir();
        File file = new File(uploadFolder + "/" + "userImg" + "/" + uuid + ".jpg");
        try {
            img.transferTo(file);
            return new Result<>("Updated.", 0);
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
            QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("admin_id", adminId);
            Admin admin = adminMapper.selectOne(queryWrapper);
            if (admin == null)
            {
                return new Result<>("admin error.", -1);
            }
            String decryptedOldPassword = EncryptionPassUtil.doDecrypt(admin.getAdminPassword());
            if (!decryptedOldPassword.equals(oldPassword))
            {
                return new Result<>("incorrect old password.", -1);
            }
        } catch (Exception e) {
            return new Result<>("encryption error.", -1);
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
        QueryWrapper<Credential> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cred_access_key", accessKey);
        Result<Admin> res1 = getAdminHelper(queryWrapper);
        Admin admin1 = res1.getInfo();
        if (admin1 == null || admin1.getAdminId() == null)
        {
            return new Result<>("Cannot locate user.", -1);
        }
        int userId = admin1.getAdminId();
        return changePassword(userId, oldPassword, newPassword);
    }

    public Result<List<Admin>> getPeople(String accessKey)
    {
        QueryWrapper<Credential> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("cred_access_key", accessKey);
        Result<Admin> res1 = getAdminHelper(queryWrapper2);
        Admin admin1 = res1.getInfo();
        if (admin1 == null || admin1.getAdminId() == null)
        {
            return new Result<>("incorrect or outdated credential.", -1);
        }

        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("admin_id", "admin_name", "admin_email", "admin_note", "admin_uuid");
        return new Result<>(adminMapper.selectList(queryWrapper), "queried.", 0);
    }
}
