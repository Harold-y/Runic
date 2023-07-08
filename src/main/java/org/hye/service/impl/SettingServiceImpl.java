package org.hye.service.impl;

import org.hye.entity.Setting;
import org.hye.dao.SettingMapper;
import org.hye.service.ISettingService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

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

}
