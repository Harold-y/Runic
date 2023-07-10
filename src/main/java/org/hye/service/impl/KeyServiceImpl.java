package org.hye.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.hye.entity.Admin;
import org.hye.entity.Key;
import org.hye.dao.KeyMapper;
import org.hye.service.IKeyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.hye.util.EncryptionPassUtil;
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
public class KeyServiceImpl extends ServiceImpl<KeyMapper, Key> implements IKeyService {
    @Resource
    KeyMapper keyMapper;

    private Boolean ifKeyNameExist(String keyName) {
        QueryWrapper<Key> wrapper = new QueryWrapper<>();
        wrapper.eq("key_name", keyName);
        Key key = keyMapper.selectOne(wrapper);
        return key != null;
    }

    private Boolean ifKeyNameExist(String keyName, Integer keyId) {
        QueryWrapper<Key> wrapper = new QueryWrapper<>();
        wrapper.eq("key_name", keyName);
        Key key = keyMapper.selectOne(wrapper);
        return (key != null && !key.getKeyId().equals(keyId));
    }
    @Override
    public Result<Integer> addKey(Key key) {
        if (key == null || key.getKeyName() == null || key.getKeyName().equals(""))
            return new Result<>("Key is null or key name is null.", -1);
        if (key.getKeyEncryptMethod() == null || (!key.getKeyEncryptMethod().equals("password") && !key.getKeyEncryptMethod().equals("simple")))
            return new Result<>("Wrong encryption method", -1);
        if (ifKeyNameExist(key.getKeyName()))
            return new Result<>("Key name already exist.", -1);
        key.setKeyId(null);
        String password = key.getKeyEncrypted();
        String encrypted = "";
        try {
            if (key.getKeyEncryptMethod().equals("password")) {
                encrypted = EncryptionPassUtil.doEncryptWithKey(password, password);
            }else if (key.getKeyEncryptMethod().equals("simple")) {
                encrypted = EncryptionPassUtil.doEncrypt(password);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new Result<>("Key encryption error.", -1);
        }
        key.setKeyEncrypted(encrypted);
        return new Result<>(keyMapper.insert(key), "insert.", 0);
    }

    @Override
    public Result<Integer> deleteKey(int keyId) {
        return new Result<>(keyMapper.deleteById(keyId), "delete.", 0);
    }

    @Override
    public Result<Integer> editKey(Key key) {
        UpdateWrapper<Key> updateWrapper = new UpdateWrapper<>();
        if (key == null || key.getKeyId() == null || key.getKeyName() == null || key.getKeyName().equals(""))
            return new Result<>("Key or key id or key name is empty.", -1);
        if (ifKeyNameExist(key.getKeyName(), key.getKeyId()))
            return new Result<>("Key name already exist.", -1);
        updateWrapper.eq("key_id", key.getKeyId());
        updateWrapper.set("key_name", key.getKeyName());
        if (key.getKeyTips() != null && !key.getKeyTips().equals(""))
            updateWrapper.set("key_tips", key.getKeyTips());

        return new Result<>(keyMapper.update(null, updateWrapper), "update.", 0);
    }

    @Override
    public Result<Integer> changePass(Key key) {
        return new Result<>("functionality not enabled.", -1);
    }

    @Override
    public Result<List<Key>> getKeys() {
        QueryWrapper<Key> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("key_id", "key_name", "key_tips", "key_encrypt_method");
        return new Result<>(keyMapper.selectList(queryWrapper), "select.", 0);
    }
}
