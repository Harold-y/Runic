package org.hye.service.impl;

import org.hye.entity.SqliteSequence;
import org.hye.dao.SqliteSequenceMapper;
import org.hye.service.ISqliteSequenceService;
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
public class SqliteSequenceServiceImpl extends ServiceImpl<SqliteSequenceMapper, SqliteSequence> implements ISqliteSequenceService {

}
