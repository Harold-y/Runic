package org.hye.service.impl;

import org.hye.entity.Admin;
import org.hye.dao.AdminMapper;
import org.hye.service.IAdminService;
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
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {

}
