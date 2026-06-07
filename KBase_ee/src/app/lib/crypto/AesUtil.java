package app.lib.crypto;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Два методи: "закодувати" (encrypt) та "розкодувати" (decrypt) рядок.
 * Використовує AES/GCM/NoPadding та ключ
 */
public class AesUtil {

    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int IV_LEN = 12;      // 96 біт, стандарт для GCM
    private static final int TAG_LEN = 128;    // 128 біт тег автентичності

    /**
     * Шифрування тексту; IV генерується випадково та префіксується до шифртексту.
     * @param plaintext вихідний текст (UTF‑8)
     * @param key AES‑ключ з SystemDerivedKey.deriveAesKey(...)
     * @return Base64( IV_len(4 байти) || IV || cipherText )
     */
    public static String encrypt(String plaintext, SecretKey key) throws Exception {
        byte[] iv = new byte[IV_LEN];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LEN, iv));
        byte[] ct = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        byte[] packed = ByteBuffer.allocate(4 + iv.length + ct.length)
                .putInt(iv.length).put(iv).put(ct).array();

        return Base64.getEncoder().encodeToString(packed);
    }

    /**
     * Розшифрування рядка, отриманого від encrypt(...).
     */
    public static String decrypt(String encoded, SecretKey key) throws Exception {
        byte[] data = Base64.getDecoder().decode(encoded);
        ByteBuffer bb = ByteBuffer.wrap(data);
        int ivLen = bb.getInt();
        byte[] iv = new byte[ivLen]; bb.get(iv);
        byte[] ct = new byte[bb.remaining()]; bb.get(ct);

        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LEN, iv));
        byte[] pt = cipher.doFinal(ct);
        return new String(pt, StandardCharsets.UTF_8);
    }
}
