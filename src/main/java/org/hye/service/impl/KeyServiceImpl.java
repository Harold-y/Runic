package org.hye.service.impl;

import org.hye.entity.Key;
import org.hye.dao.KeyMapper;
import org.hye.service.IKeyService;
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
public class KeyServiceImpl extends ServiceImpl<KeyMapper, Key> implements IKeyService {

}
