package org.hye.service;

import org.hye.entity.Admin;
import com.baomidou.mybatisplus.extension.service.IService;
import org.hye.util.Result;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author HaroldCI
 * @since 2023-07-08
 */
public interface IAdminService extends IService<Admin> {
    Result<Admin> login(String email, String password);
    Result<Admin> signup(String email, String name, String password);
    Result<Integer> logout(String accessKey);
    Result<Admin> getInfo(Integer adminId);
    Result<Admin> getInfo(String accessKey);
    Result<Integer> editInfo(String accessKey, Admin admin);
    byte[] getAvatar(Integer userId);
    byte[] getAvatar(String accessKey);
    Result<Integer> updateAvatar(String accessKey, MultipartFile img);
    Result<Integer> changePassword(Integer adminId, String oldPassword, String newPassword);
    Result<Integer> changePassword(String accessKey, String oldPassword, String newPassword);
}
