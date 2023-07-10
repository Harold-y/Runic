package org.hye.entity;

import lombok.Data;

@Data
public class FileEncryptDTO {
    Integer fId;
    Integer mtnId;
    String absPath;
    Boolean selectFromKeys;
    String keyPassword;
    Boolean generatePubKey; // if not select from keys, whether to have a keyId
    Integer keyId;
    String password; // if select from keys then keyId must be a valid key Id, and password will be used
    // to verify that key if the protection is "password", if simple then password may not need to be contained.
    // if not selectFromKeys then password will be used to enable EncryptionFileUtil protection.

}
