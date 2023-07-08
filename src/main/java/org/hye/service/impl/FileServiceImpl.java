package org.hye.service.impl;

import org.hye.entity.File;
import org.hye.dao.FileMapper;
import org.hye.service.IFileService;
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
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements IFileService {

}
