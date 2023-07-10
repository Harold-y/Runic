package org.hye.entity;

import lombok.Data;

@Data
public class FileDTO {
    private Integer fId;

    private String fName;

    private String fPath;

    private String absPath;

    private String fType;

    private String fSize;

    private Integer fKeyId;

    private String fEncryptedType;

}
