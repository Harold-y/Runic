package org.hye.dao;

import org.apache.ibatis.annotations.Mapper;
import org.hye.entity.File;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author HaroldCI
 * @since 2023-07-08
 */
@Mapper
public interface FileMapper extends BaseMapper<File> {

}
