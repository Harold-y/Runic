package org.hye.util;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import static org.apache.commons.io.FileUtils.readFileToByteArray;

@Component
public class EncryptionTextUtil {

    @Value("${constant.encryption}")
    private static String constant = "toallthepeopleiloved";

    private static final Integer iterationCount1 = 65536;

    /*
    Iteration Count: 65536
    IV Length of bytes: 16
    Salt Length of bytes: 16
     */
    public static SecretKey getSecretKey(String password, byte[] salt, int iterationCount) throws NoSuchAlgorithmException, InvalidKeySpecException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        return secret;
    }

    public static byte[][] encrypt(String message, String password, byte[] salt, int iterationCount) throws Exception {
        SecretKey secret = getSecretKey(password, salt, iterationCount);
        /* Encrypt the message. */
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        AlgorithmParameters params = cipher.getParameters();
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
        byte[] ciphertext = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));

        return new byte[][]{iv, ciphertext};
    }

    public static String decrypt(byte[] encrypted, String password, byte[] iv, byte[] salt, int iterationCount) throws Exception {
        SecretKey secret = getSecretKey(password, salt, iterationCount);
        /* Encrypt the message. */
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
    }

    /**
     * @param filePath: the path of the text file
     * @param encryptedFilePath: the folder you want to put your encrypted file in
     * @param password: password
     * @param generatePassword: if to generate a separate password key file (will be stored in encryptedFilePath named as fileName + .runic.pass)
     */
    public static void writeSecret(String filePath, String encryptedFilePath, String password, boolean generatePassword)
    {
        String[] content_name = FileUtil.readFileGetName(filePath);
        int iterationCount = iterationCount1;
        String saltStr = RandomGeneratorUtil.randomPassNumChars(16);
        byte[] salt = saltStr.getBytes(StandardCharsets.UTF_8);
        try {
            byte[][] encryptedMessage = encrypt(content_name[0], password, salt, iterationCount);
            byte[] allByteArray = new byte[salt.length + encryptedMessage[0].length + encryptedMessage[1].length];
            // First Salt, then IV, then encrypted content
            ByteBuffer buff = ByteBuffer.wrap(allByteArray);
            buff.put(salt);
            buff.put(encryptedMessage[0]);
            buff.put(encryptedMessage[1]);
            FileUtils.writeByteArrayToFile(new File(encryptedFilePath + System.getProperty("file.separator") + content_name[1] + ".rtext"), allByteArray);

            if (generatePassword) {
                byte[][] passwordEncrypted = encrypt(password, constant, salt, iterationCount);

                byte[] allByteArrayPass = new byte[salt.length + passwordEncrypted[0].length + passwordEncrypted[1].length];
                // First Salt, then IV, then encrypted content
                ByteBuffer buff2 = ByteBuffer.wrap(allByteArrayPass);
                buff2.put(salt);
                buff2.put(passwordEncrypted[0]);
                buff2.put(passwordEncrypted[1]);

                FileUtils.writeByteArrayToFile(new File(encryptedFilePath + System.getProperty("file.separator") + content_name[1] + ".runic.pass"), allByteArrayPass);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param content: the message to encrypt on
     * @param encryptedFilePath: the folder you want to put your encrypted file in
     * @param fileName: the fileName of the encrypted file (we will add .rtext in the end)
     * @param password: password
     * @param generatePassword: if to generate a separate password key file (will be stored in encryptedFilePath named as fileName + .runic.pass)
     */
    public static void writeSecretViaStr(String content, String encryptedFilePath, String fileName, String password, boolean generatePassword)
    {
        int iterationCount = iterationCount1;
        String saltStr = RandomGeneratorUtil.randomPassNumChars(16);
        byte[] salt = saltStr.getBytes(StandardCharsets.UTF_8);
        try {
            byte[][] encryptedMessage = encrypt(content, password, salt, iterationCount);
            byte[] allByteArray = new byte[salt.length + encryptedMessage[0].length + encryptedMessage[1].length];
            // First Salt, then IV, then encrypted content
            ByteBuffer buff = ByteBuffer.wrap(allByteArray);
            buff.put(salt);
            buff.put(encryptedMessage[0]);
            buff.put(encryptedMessage[1]);
            FileUtils.writeByteArrayToFile(new File(encryptedFilePath + System.getProperty("file.separator") + fileName + ".rtext"), allByteArray);

            if (generatePassword) {
                byte[][] passwordEncrypted = encrypt(password, constant, salt, iterationCount);

                byte[] allByteArrayPass = new byte[salt.length + passwordEncrypted[0].length + passwordEncrypted[1].length];
                // First Salt, then IV, then encrypted content
                ByteBuffer buff2 = ByteBuffer.wrap(allByteArrayPass);
                buff2.put(salt);
                buff2.put(passwordEncrypted[0]);
                buff2.put(passwordEncrypted[1]);

                FileUtils.writeByteArrayToFile(new File(encryptedFilePath + System.getProperty("file.separator") + fileName + ".runic.pass"), allByteArrayPass);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * @param secretPath: the path of the secret file (with .rtext appendix)
     * @param password: password
     * @param givePasswordString: if true then use the @password parameter, else use @passwordPath parameter (must be a .runic.pass file)
     * @param passwordPath: the path of the pass file (with .runic.pass appendix)
     * Note: either give password directly or pass it by filename
     * @param writeToFile: if to write decrypted content to a file
     * @param writePath: if @writeToFile param is true, then specify the path you want to write your decrypted file
     */
    public static String decryptSecret(String secretPath, String password, boolean givePasswordString, String passwordPath, boolean writeToFile, String writePath) throws Exception {
        if (!givePasswordString)
        {
            try {
                byte[] secretPassBytes = readFileToByteArray(new File(passwordPath));
                byte[] saltPassBytes = Arrays.copyOfRange(secretPassBytes, 0, 16);
                byte[] ivPassBytes = Arrays.copyOfRange(secretPassBytes, 16, 32);
                byte[] encryptedPassBytes = Arrays.copyOfRange(secretPassBytes, 32, secretPassBytes.length);

                password = decrypt(encryptedPassBytes, constant, ivPassBytes, saltPassBytes, iterationCount1);
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        File secretFile = new File(secretPath);
        String[] originalNames = secretFile.getName().split("\\.");
        String originalName = "";
        for (int i = 0; i < originalNames.length - 1; i ++) {
            if (i == originalNames.length - 2) {
                originalName += ".";
            }
            originalName += originalNames[i];
        }

        byte[] secretFileBytes = readFileToByteArray(secretFile);
        byte[] saltBytes = Arrays.copyOfRange(secretFileBytes, 0, 16);
        byte[] ivBytes = Arrays.copyOfRange(secretFileBytes, 16, 32);
        byte[] encryptedFileBytes = Arrays.copyOfRange(secretFileBytes, 32, secretFileBytes.length);

        String content = decrypt(encryptedFileBytes, password, ivBytes, saltBytes, iterationCount1);
        if (writeToFile) {
            FileUtils.writeStringToFile(new File(writePath + System.getProperty("file.separator") + originalName), content, "UTF-8");
        }
        return content;
    }

    public static void main(String[] args) throws Exception{

    }
}
