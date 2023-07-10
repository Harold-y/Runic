package org.hye.entity;

import lombok.Data;

@Data
public class FileDecryptDTO {
    Integer fId;
    Integer mtnId;
    String absPath;
    String targetAbsPath;
    Boolean givePasswordString;
    String pubKeyAbsPath;
    Integer keyId;
    String password;
}
