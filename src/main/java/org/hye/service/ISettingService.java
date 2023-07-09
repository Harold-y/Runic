package org.hye.service;

import org.hye.entity.Setting;
import com.baomidou.mybatisplus.extension.service.IService;
import org.hye.util.Result;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author HaroldCI
 * @since 2023-07-08
 */
public interface ISettingService extends IService<Setting> {
    Result<Integer> addSetting(String name, String value);
    Result<Integer> removeSetting(Integer settingId);
    Result<Integer> editSetting(Integer settingId, String name, String value);
    Result<List<Setting>> getSettings();
}
