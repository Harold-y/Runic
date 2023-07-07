package org.hye.util;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.apache.commons.io.FileUtils.readFileToByteArray;

public class EncryptionFileUtil {

    @Value("${constant.encryption}")
    private static String constant = "toallthepeopleiloved";
    private static final Integer iterationCount1 = 65536;
    private static final String ENCRYPT_ALGO = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128; // must be one of {128, 120, 112, 104, 96}
    private static final int IV_LENGTH_BYTE = 12;
    private static final int SALT_LENGTH_BYTE = 16;
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    public static byte[] getRandomNonce(int numBytes) {
        byte[] nonce = new byte[numBytes];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    // Password derived AES 256 bits secret key
    public static SecretKey getAESKeyFromPassword(char[] password, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        // iterationCount = 65536
        // keyLength = 256
        KeySpec spec = new PBEKeySpec(password, salt, iterationCount1, 256);
        SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        return secret;
    }


    public static void encrypt(byte[] content, String encryptedFilePath, String fileName, String password, boolean generatePassword) throws Exception {

        // 16 bytes salt
        byte[] salt = getRandomNonce(SALT_LENGTH_BYTE);

        // GCM recommended 12 bytes iv?
        byte[] iv = getRandomNonce(IV_LENGTH_BYTE);

        // secret key from password
        SecretKey aesKeyFromPassword = getAESKeyFromPassword(password.toCharArray(), salt);

        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);

        // ASE-GCM needs GCMParameterSpec
        cipher.init(Cipher.ENCRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

        byte[] cipherContent = cipher.doFinal(content);

        // prefix IV and Salt to cipher text
        byte[] cipherTextWithIvSalt = ByteBuffer.allocate(iv.length + salt.length + cipherContent.length)
                .put(iv)
                .put(salt)
                .put(cipherContent)
                .array();

        FileUtils.writeByteArrayToFile(new File(encryptedFilePath + System.getProperty("file.separator") + fileName + ".runic"), cipherTextWithIvSalt);

        if (generatePassword) {
            byte[][] passwordEncrypted = EncryptionTextUtil.encrypt(password, constant, salt, iterationCount1);

            byte[] allByteArrayPass = new byte[salt.length + passwordEncrypted[0].length + passwordEncrypted[1].length];
            // First Salt, then IV, then encrypted content
            ByteBuffer buff2 = ByteBuffer.wrap(allByteArrayPass);
            buff2.put(salt);
            buff2.put(passwordEncrypted[0]);
            buff2.put(passwordEncrypted[1]);

            FileUtils.writeByteArrayToFile(new File(encryptedFilePath + System.getProperty("file.separator") + fileName + ".runic.pass"), allByteArrayPass);
        }

    }

    public static void doEncrypt(String filePath, String encryptedFilePath, String password, boolean generatePassword) throws Exception
    {
        File file = new File(filePath);
        String fileName = file.getName();
        byte[] data = readFileToByteArray(file);
        encrypt(data, encryptedFilePath, fileName, password, generatePassword);
    }

    private static byte[] decrypt(byte[] decode, String fileName, String password, boolean givePasswordString, String passwordPath, boolean writeToFile, String writePath) throws Exception {
        if (!givePasswordString)
        {
            byte[] secretPassBytes = readFileToByteArray(new File(passwordPath));
            byte[] saltPassBytes = Arrays.copyOfRange(secretPassBytes, 0, 16);
            byte[] ivPassBytes = Arrays.copyOfRange(secretPassBytes, 16, 32);
            byte[] encryptedPassBytes = Arrays.copyOfRange(secretPassBytes, 32, secretPassBytes.length);

            password = EncryptionTextUtil.decrypt(encryptedPassBytes, constant, ivPassBytes, saltPassBytes, iterationCount1);
        }
        // get back the iv and salt from the cipher text
        ByteBuffer bb = ByteBuffer.wrap(decode);

        byte[] iv = new byte[IV_LENGTH_BYTE];
        bb.get(iv);

        byte[] salt = new byte[SALT_LENGTH_BYTE];
        bb.get(salt);

        byte[] cipherContent = new byte[bb.remaining()];
        bb.get(cipherContent);

        // get back the aes key from the same password and salt
        SecretKey aesKeyFromPassword = getAESKeyFromPassword(password.toCharArray(), salt);

        Cipher cipher = Cipher.getInstance(ENCRYPT_ALGO);

        cipher.init(Cipher.DECRYPT_MODE, aesKeyFromPassword, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

        byte[] decoded = cipher.doFinal(cipherContent);

        if (writeToFile) {
            FileUtils.writeByteArrayToFile(new File(writePath + System.getProperty("file.separator") + fileName), decoded);
        }

        return decoded;

    }

    public static byte[] doDecrypt(String encryptedFilePath, String password, boolean givePasswordString, String passwordPath, boolean writeToFile, String writePath) throws Exception {
        File file = new File(encryptedFilePath);
        String fileName = file.getName();
        String[] originalNames = fileName.split("\\.");
        String originalName = "";
        for (int i = 0; i < originalNames.length - 1; i ++) {
            if (i == originalNames.length - 2) {
                originalName += ".";
            }
            originalName += originalNames[i];
        }
        byte[] data = readFileToByteArray(file);
        return decrypt(data, originalName, password, givePasswordString, passwordPath, writeToFile, writePath);
    }

    public static void main(String[] args) throws Exception {
        String fileLoc = "D:\\OneDrives\\OneDrive - UW-Madison\\素材&图片\\Picture1.png";
        String secretStoreLoc = "D:\\OneDrives\\OneDrive\\Desktop\\Test";
        String encryptedFileLoc = "D:\\OneDrives\\OneDrive\\Desktop\\Test\\Picture1.png.runic";
        String encryptedPassLoc = "D:\\OneDrives\\OneDrive\\Desktop\\Test\\Picture1.png.runic.pass";
        String writeToLoc = "D:\\OneDrives\\OneDrive\\Desktop\\Test";
        String password = "this is a password";


        // doEncrypt(fileLoc, secretStoreLoc, password, true);
        // doDecrypt(encryptedFileLoc, password, true, null, true, writeToLoc);
        // doDecrypt(encryptedFileLoc, null, false, encryptedPassLoc, true, writeToLoc);
    }
}
