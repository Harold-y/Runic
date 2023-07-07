package org.hye.runic;

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
public class EncryptionUtil {

    @Value("${constant.encryption}")
    private static String constant;

    private static final Integer iterationCount1 = 65536;

    /*
    Iteration Count: 65536
    IV Length of bytes: 16
    Salt Length of bytes: 10
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

    public static void writeSecret(String filePath, String encryptedFilePath, String password, boolean generatePassword)
    {
        String[] content_name = FileUtil.readFileGetName(filePath);
        int iterationCount = iterationCount1;
        String saltStr = RandomGeneratorUtil.randomPassNumChars(10);
        byte[] salt = saltStr.getBytes(StandardCharsets.UTF_8);
        try {
            byte[][] encryptedMessage = encrypt(content_name[0], password, salt, iterationCount);
            byte[] allByteArray = new byte[salt.length + encryptedMessage[0].length + encryptedMessage[1].length];
            // First Salt, then IV, then encrypted content
            ByteBuffer buff = ByteBuffer.wrap(allByteArray);
            buff.put(salt);
            buff.put(encryptedMessage[0]);
            buff.put(encryptedMessage[1]);
            FileUtils.writeByteArrayToFile(new File(encryptedFilePath + content_name[1] + ".runic"), allByteArray);

            if (generatePassword) {
                byte[][] passwordEncrypted = encrypt(password, constant, salt, iterationCount);

                byte[] allByteArrayPass = new byte[salt.length + passwordEncrypted[0].length + passwordEncrypted[1].length];
                // First Salt, then IV, then encrypted content
                ByteBuffer buff2 = ByteBuffer.wrap(allByteArrayPass);
                buff2.put(salt);
                buff2.put(encryptedMessage[0]);
                buff2.put(encryptedMessage[1]);

                FileUtils.writeByteArrayToFile(new File(encryptedFilePath + content_name[1] + ".runic.pass"), allByteArrayPass);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decryptSecret(String secretPath, String password, boolean givePasswordString, String passwordPath, boolean writeToFile, String writePath)
    {
        if (!givePasswordString)
        {
            try {
                byte[] secretPassBytes = readFileToByteArray(new File(passwordPath));
                byte[] saltPassBytes = Arrays.copyOfRange(secretPassBytes, 0, 10);
                byte[] ivPassBytes = Arrays.copyOfRange(secretPassBytes, 10, 26);
                byte[] encryptedPassBytes = Arrays.copyOfRange(secretPassBytes, 26, secretPassBytes.length);

                password = decrypt(encryptedPassBytes, constant, ivPassBytes, saltPassBytes, iterationCount1);
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        try {
            File secretFile = new File(secretPath);
            String[] originalNames = secretFile.getName().split("\\.");
            String originalName = "";
            for (int i = 0; i < originalNames.length - 1; i ++)
                originalName += originalNames[i];

            byte[] secretFileBytes = readFileToByteArray(secretFile);
            byte[] saltBytes = Arrays.copyOfRange(secretFileBytes, 0, 10);
            byte[] ivBytes = Arrays.copyOfRange(secretFileBytes, 10, 26);
            byte[] encryptedFileBytes = Arrays.copyOfRange(secretFileBytes, 26, secretFileBytes.length);

            String content = decrypt(encryptedFileBytes, password, ivBytes, saltBytes, iterationCount1);
            if (writeToFile) {
                FileUtils.writeStringToFile(new File("writePath" + System.getProperty("file.separator") + originalName), content, "UTF-8");
            }
            return content;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception{
        String filePath = "D:\\OneDrives\\OneDrive - UW-Madison\\个人\\日记\\霜泽亚拉.md";
        String writePath = "D:\\OneDrives\\OneDrive - UW-Madison\\个人\\日记";
        String password = "ifyoucanhearthisvoice,youarealon";

        /*
        String content = FileUtil.readFile(filePath);
        byte[] salt = RandomGeneratorUtil.randomPassNumChars(10).getBytes(StandardCharsets.UTF_8);
        byte[][] encryptedMessage = encrypt(content, password, salt, iterationCount1);
        System.out.println(encryptedMessage[0].length);
        String original = decrypt(encryptedMessage[1], password, encryptedMessage[0], salt, iterationCount1);
        // System.out.println(original);

        */

        writeSecret(filePath, writePath, password, true);

    }
}
