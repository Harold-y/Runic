package org.hye.service;

import org.hye.entity.FileDTO;
import org.hye.entity.Mount;
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
public interface IMountService extends IService<Mount> {
    Result<Integer> addMount(Mount mount);
    Result<Integer> deleteMount(Integer mountId, Boolean deleteDiskFiles);
    Result<Integer> editMount(Mount mount);
    Result<List<FileDTO>> getFiles(Integer mountId);

    Result<List<FileDTO>> getFiles(String dirPath, Integer mountId, String mtnPath);
    Result<List<Mount>> getMounts();
}
