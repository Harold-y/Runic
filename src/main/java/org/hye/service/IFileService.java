package org.hye.service;

import org.hye.entity.File;
import com.baomidou.mybatisplus.extension.service.IService;
import org.hye.entity.FileDecryptDTO;
import org.hye.entity.FileEncryptDTO;
import org.hye.util.Result;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author HaroldCI
 * @since 2023-07-08
 */
public interface IFileService extends IService<File> {
    Result<Integer> protectFile(FileEncryptDTO fileEncryptDTO);
    Result<Integer> releaseFile(FileDecryptDTO fileDecryptDTO);
    Result<Byte[]> getDecrypted(MultipartFile multipartFile, FileDecryptDTO fileDecryptDTO);
    Result<String> getDecryptedBase64Img(MultipartFile multipartFile, FileDecryptDTO fileDecryptDTO);
    Result<String> getDecryptedPlainText(MultipartFile multipartFile, FileDecryptDTO fileDecryptDTO);
    // password only used if fId is not using a simple key.
    Result<Integer> saveEncryptedPlainText(Integer fId, String newContent, String password);
    Result<Integer> uploadFile(MultipartFile multipartFile, String absPath, String name);
    Result<Integer> deleteFile(String absPath);
    Result<Integer> renameFile(String absPath, String newName);
    Result<Byte[]> readFile(String absPath);
    Result<String> readPlainFileStr(String absPath);
    Result<String> readBase64Img(String absPath);
    Result<Integer> savePlainFile(String absPath, String content);
    Result<Integer> createFolder(String absPath, String folderName);
    Result<Integer> createFile(String absPath, String name);
}
