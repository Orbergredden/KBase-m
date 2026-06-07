package app.lib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import app.model.Params;

/**
 * Робота з ключами файла KeyStore
 */
public class KeyStorePrg {
	private static final String FILE_NAME = "keystore.p12";
	private static final String KEYSTORE_TYPE = "PKCS12";
	private static final String KEY_ALIAS = "kbase-secrets-key";
	
	private static final String XFORM = "AES/GCM/NoPadding";
    private static final int IV_BYTES = 12;
    private static final int TAG_BITS = 128;

    public static class EncBlob {
        public byte[] iv;
        public byte[] ct;
        public String keyAlias;  // для версіонування ключа (наприклад, "...-v1")
        public String dbId;      // AAD, з яким шифрували
        
        public EncBlob() {
        	// Поля залишаються null за замовчуванням
        }

        public EncBlob(byte[] iv, byte[] ct, String keyAlias, String dbId) {
            this.iv = iv; this.ct = ct; this.keyAlias = keyAlias; this.dbId = dbId;
        }
    }
    
    /**
     * 
     * @param password
     * @param masterKey
     * @param keyAlias
     * @param dbId
     * @return
     * @throws GeneralSecurityException
     */
    public static EncBlob encrypt(Params params, char[] password, String keyAlias, String dbId)
            throws GeneralSecurityException, Exception {
    	SecretKey masterKey = loadSecretKey(params);

        byte[] iv = new byte[IV_BYTES];
        SecureRandom rnd = new SecureRandom(); // неблокуючий варіант
        rnd.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(XFORM);
        cipher.init(Cipher.ENCRYPT_MODE, masterKey, new GCMParameterSpec(TAG_BITS, iv));
        if (dbId != null) cipher.updateAAD(dbId.getBytes(StandardCharsets.UTF_8));

        // Без створення String із пароля:
        ByteBuffer bb = StandardCharsets.UTF_8.encode(CharBuffer.wrap(password));
        byte[] pt = new byte[bb.remaining()];
        bb.get(pt);
        Arrays.fill(bb.array(), (byte)0); // якщо bb.hasArray()

        byte[] ct = cipher.doFinal(pt);
        Arrays.fill(pt, (byte)0);
        return new EncBlob(iv, ct, keyAlias, dbId);
    }
    
    /**
     * 
     * @param blob
     * @param masterKey
     * @return
     * @throws GeneralSecurityException
     */
    public static char[] decrypt(Params params, EncBlob blob) 
    		throws GeneralSecurityException, Exception {
    	SecretKey masterKey = loadSecretKey(params);
        Cipher cipher = Cipher.getInstance(XFORM);
        cipher.init(Cipher.DECRYPT_MODE, masterKey, new GCMParameterSpec(TAG_BITS, blob.iv));
        if (blob.dbId != null) cipher.updateAAD(blob.dbId.getBytes(StandardCharsets.UTF_8));

        byte[] pt = cipher.doFinal(blob.ct);
        // Перетворення без проміжного String, якщо хочете, можна й так:
        char[] out = StandardCharsets.UTF_8.decode(ByteBuffer.wrap(pt)).array().clone();
        Arrays.fill(pt, (byte)0);
        return out;
    }
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public static SecretKey loadSecretKey(Params params) throws Exception {
		
		checkAndCreate(params);
		
		String filename = FileUtil.getFullUserFileName(params, FILE_NAME); // шлях до KeyStore
		Path keystorePath = Path.of(filename);
		String keystoreType = System.getenv().getOrDefault("KBASE_KEYSTORE_TYPE", KEYSTORE_TYPE);
		String alias = System.getenv().getOrDefault("KBASE_KEY_ALIAS", KEY_ALIAS);
        char[] storePassword = System.getenv().getOrDefault("KBASE_KEYSTORE_PASSWORD", "kbase").toCharArray();
        char[] keyPassword = System.getenv().getOrDefault("KBASE_KEY_PASSWORD", new String(storePassword)).toCharArray();

        KeyStore ks = KeyStore.getInstance(keystoreType);
        try (FileInputStream fis = new FileInputStream(keystorePath.toFile())) {
            ks.load(fis, storePassword);
        }

        KeyStore.ProtectionParameter prot = new KeyStore.PasswordProtection(keyPassword);
        KeyStore.Entry entry = ks.getEntry(alias, prot);
        if (!(entry instanceof KeyStore.SecretKeyEntry)) {
            throw new IllegalStateException("Елемент за alias не є SecretKeyEntry (тип: " +
                    (entry == null ? "null" : entry.getClass()) + ")");
        }

        return ((KeyStore.SecretKeyEntry) entry).getSecretKey();
    }
	
	/**
	 * 
	 * @param params
	 */
	public static void checkAndCreate (Params params) {
		String filename = FileUtil.getFullUserFileName(params, FILE_NAME); // шлях до KeyStore
		File file = new File(filename);
		
        String keystoreType = System.getenv().getOrDefault("KBASE_KEYSTORE_TYPE", KEYSTORE_TYPE);
        char[] keystorePassword = System.getenv().getOrDefault("KBASE_KEYSTORE_PASSWORD", "kbase").toCharArray();
        String alias = KEY_ALIAS;
        
		// check
		if (file.exists()) {
			try (FileInputStream fis = new FileInputStream(filename)) {
	            KeyStore ks = KeyStore.getInstance(keystoreType);
	            ks.load(fis, keystorePassword);
	
	            if (ks.containsAlias(alias)) {
	            	Arrays.fill(keystorePassword, '\0');
	                return;
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
		
		// create file 
        char[] keyPassword = null;
		
        try {
            // Згенерувати паролі (рандомні, URL-safe Base64)
        	keystorePassword = randomPassword(32).toCharArray();
            keyPassword = randomPassword(32).toCharArray();
            
            createOrUpdateKeyStore(filename, keystoreType, alias, keystorePassword, keyPassword);
            
            // Прописати системні властивості JVM (діють у межах поточного процесу)
            System.setProperty("KBASE_KEYSTORE_TYPE", keystoreType);
            System.setProperty("KBASE_KEYSTORE_PASSWORD", new String(keystorePassword));
            System.setProperty("KBASE_KEY_ALIAS", alias);
            System.setProperty("KBASE_KEY_PASSWORD", new String(keyPassword));

            // Зберегти у змінні середовища Windows (поточний користувач)
            if (isWindows()) {
                Map<String, String> envPairs = new LinkedHashMap<>();
                envPairs.put("KBASE_KEYSTORE_TYPE", keystoreType);
                envPairs.put("KBASE_KEYSTORE_PASSWORD", new String(keystorePassword));
                envPairs.put("KBASE_KEY_ALIAS", alias);
                envPairs.put("KBASE_KEY_PASSWORD", new String(keyPassword));

                persistEnvWindowsUser(envPairs);
            }
        } catch (Exception e) {
            //e.printStackTrace(System.err);
        	e.printStackTrace();
			ShowAppMsg.showAlert(
        			"ERROR", 
        			"Ошибка", 
        			"Неможливо створити KeyStore файл",
					e.getMessage());
        } finally {
            // Затираємо паролі у пам'яті
            if (keystorePassword != null) Arrays.fill(keystorePassword, '\0');
            if (keyPassword != null)      Arrays.fill(keyPassword, '\0');
        }
	}
	
	/**
	 * 
	 * @param bytes
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	private static String randomPassword(int bytes) throws NoSuchAlgorithmException {
        byte[] buf = new byte[bytes];
        SecureRandom.getInstanceStrong().nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
	
	/**
	 * Створює новий KeyStore або відкриває існуючий і записує AES-ключ під заданим alias.
	 * @param keystorePath
	 * @param keystoreType
	 * @param alias
	 * @param storePass
	 * @param keyPass
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
    public static void createOrUpdateKeyStore(
            String keystorePath,
            String keystoreType,
            String alias,
            char[] storePass,
            char[] keyPass
    ) throws GeneralSecurityException, IOException {

        KeyStore ks = KeyStore.getInstance(keystoreType);

        Path path = Path.of(keystorePath);
        if (Files.exists(path)) {
            // Завантажуємо існуючий KeyStore
            try (var in = Files.newInputStream(path)) {
                ks.load(in, storePass);
            }
        } else {
            // Створюємо новий порожній KeyStore
            ks.load(null, storePass);
        }

        // Генеруємо 256-бітний AES ключ (на сучасних JDK ліміти крипто вже зняті)
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256, SecureRandom.getInstanceStrong());
        SecretKey secretKey = kg.generateKey();

        // Записуємо як SecretKeyEntry з власним паролем keyPass
        KeyStore.ProtectionParameter prot = new KeyStore.PasswordProtection(keyPass);
        KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secretKey);
        ks.setEntry(alias, entry, prot);

        // Зберігаємо KeyStore на диск
        try (FileOutputStream fos = new FileOutputStream(keystorePath)) {
            ks.store(fos, storePass);
        }
    }
    
    /**
     * 
     * @return
     */
    private static boolean isWindows() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        return os.contains("win");
    }
    
    /**
     * Зберігає пари key=value у змінні середовища Windows поточного користувача через `setx`.
     * ПРИМІТКА: значення будуть видимі лише у НОВИХ процесах після виконання (нове вікно терміналу/перелогін).
     */
    private static void persistEnvWindowsUser(Map<String, String> keyValues) throws IOException, InterruptedException {
        for (Map.Entry<String, String> kv : keyValues.entrySet()) {
            String name = kv.getKey();
            String value = kv.getValue();

            // setx має обмеження на довжину (~1024-2047 символів залежно від версії). Перевіримо базово:
            if (value.length() > 1500) {
                throw new IllegalArgumentException("Занадто довге значення для " + name + " (" + value.length() + " символів).");
            }

            // Викликаємо: cmd /c setx NAME "VALUE"
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "setx", name, quoteWindows(value));
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.waitFor(30, TimeUnit.SECONDS);

            if (p.exitValue() != 0) {
                throw new RuntimeException("Не вдалося встановити змінну середовища: " + name);
            }
        }
    }

    private static String quoteWindows(String v) {
        // Огорнемо лапками, і екрануємо внутрішні лапки
        return "\"" + v.replace("\"", "\\\"") + "\"";
    }
}
