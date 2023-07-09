package org.hye.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.hye.entity.Setting;
import org.hye.dao.SettingMapper;
import org.hye.service.ISettingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.hye.util.Result;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
public class SettingServiceImpl extends ServiceImpl<SettingMapper, Setting> implements ISettingService {
    @Resource
    SettingMapper settingMapper;

    public Result<Integer> addSetting(String name, String value)
    {
        if (name == null || value == null || name.equals("") || value.equals(""))
            return new Result<>("setting name or value cannot be empty.", -1);
        Setting setting1 = new Setting();
        setting1.setSettingName(name);
        setting1.setSettingValue(value);

        return new Result<>(settingMapper.insert(setting1), "insert.", 0);
    }

    public Result<Integer> removeSetting(Integer settingId)
    {
        if (settingId == null)
            return new Result<>("empty setting id.", -1);
        return new Result<>(settingMapper.deleteById(settingId), "delete.", 0);
    }

    public Result<Integer> editSetting(Integer settingId, String name, String value)
    {
        if (settingId == null || name == null || value == null || name.equals("") || value.equals(""))
            return new Result<>("empty setting info.", -1);
        Setting setting1 = new Setting();
        setting1.setSettingName(name);
        setting1.setSettingValue(value);
        setting1.setSettingId(settingId);
        return new Result<>(settingMapper.updateById(setting1), "edit.", 0);
    }

    public Result<List<Setting>> getSettings()
    {
        QueryWrapper<Setting> queryWrapper = new QueryWrapper<>();
        return new Result<>(settingMapper.selectList(queryWrapper), "queried.", 0);
    }
}
