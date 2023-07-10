package org.hye.service;

import org.hye.entity.Key;
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
public interface IKeyService extends IService<Key> {
    Result<Integer> addKey(Key key);
    Result<Integer> deleteKey(int keyId);
    Result<Integer> editKey(Key key);
    Result<Integer> changePass(Key key);
    Result<List<Key>> getKeys();
}
