package app.lib.crypto;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Детермінована деривація AES-ключа з системного ID без зберігання.
 */
public final class SystemDerivedKey {

    private SystemDerivedKey() {}

    /**
     * Деривація ключа AES з системного стабільного ідентифікатора.
     *
     * @param appNamespace унікальний рядок вашого застосунку (наприклад, "ua.gov.myapp" або "com.company.product")
     * @param keyBits      128 або 256
     */
    public static SecretKey deriveAesKey(String appNamespace, int keyBits) throws Exception {
        if (keyBits != 128 && keyBits != 256) {
            throw new IllegalArgumentException("keyBits must be 128 or 256");
        }

        String deviceId = deviceStableId(); // наприклад, MachineGuid / machine-id / IOPlatformUUID
        if (deviceId == null || deviceId.isBlank()) {
            throw new IllegalStateException("Cannot obtain a stable device identifier");
        }

        // Пароль для PBKDF2 = DeviceID + appNamespace (щоб ключі різних апок були різні)
        char[] password = (normalize(deviceId) + ":" + appNamespace).toCharArray();

        // Сіль фіксована (версіонуйте її, якщо колись зміните рецепт)
        byte[] salt = "SystemDerivedKey::v1".getBytes(StandardCharsets.UTF_8);

        // Сучасні значення ітерацій для PBKDF2 (чим більше – тим повільніший, але складніший для брутфорсу)
        int iterations = 310_000; // орієнтир рівня 1Password/NIST-гайдів

        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyBits);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = skf.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }

    /** Отримання стабільного ID для поточної ОС. */
    public static String deviceStableId() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        try {
            if (os.contains("win")) {
                return readWindowsMachineGuid();
            } else if (os.contains("mac")) {
                return readMacPlatformUUID();
            } else {
                return readLinuxMachineId();
            }
        } catch (Exception ex) {
            // Фолбек на MAC-адреси або hostname
            try {
                String fallback = fallbackFromNetworkOrHost();
                if (fallback != null && !fallback.isBlank()) return fallback;
            } catch (Exception ignored) {}
            // Останній шанс — просто "OS signature"
            return (System.getProperty("os.name", "unknown")
                    + "|" + System.getProperty("os.arch", "unknown")
                    + "|" + System.getProperty("user.name", "unknown")).trim();
        }
    }

    // ---------- Windows ----------
    private static String readWindowsMachineGuid() throws IOException, InterruptedException {
        Process p = new ProcessBuilder("reg", "query",
                "HKLM\\SOFTWARE\\Microsoft\\Cryptography", "/v", "MachineGuid")
                .redirectErrorStream(true).start();
        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        p.waitFor();
        for (String line : out.split("\\R")) {
            if (line.contains("MachineGuid")) {
                // формат: <Name>    <Type>    <Data>
                String[] parts = line.trim().split("\\s{2,}");
                String value = parts[parts.length - 1].trim();
                return value;
            }
        }
        throw new IOException("MachineGuid not found");
    }

    // ---------- Linux ----------
    private static String readLinuxMachineId() throws IOException {
        Path p1 = Path.of("/etc/machine-id");
        Path p2 = Path.of("/var/lib/dbus/machine-id");
        String s = null;
        if (Files.exists(p1)) s = Files.readString(p1, StandardCharsets.UTF_8).trim();
        if ((s == null || s.isBlank()) && Files.exists(p2)) {
            s = Files.readString(p2, StandardCharsets.UTF_8).trim();
        }
        if (s == null || s.isBlank()) throw new IOException("machine-id not found");
        return s;
    }

    // ---------- macOS ----------
    private static String readMacPlatformUUID() throws IOException, InterruptedException {
        Process p = new ProcessBuilder("ioreg", "-rd1", "-c", "IOPlatformExpertDevice")
                .redirectErrorStream(true).start();
        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        p.waitFor();
        Matcher m = Pattern.compile("\"IOPlatformUUID\"\\s*=\\s*\"([^\"]+)\"").matcher(out);
        if (m.find()) return m.group(1);
        throw new IOException("IOPlatformUUID not found");
    }

    // ---------- Fallback ----------
    private static String fallbackFromNetworkOrHost() throws SocketException, UnknownHostException {
        List<String> macs = new ArrayList<>();
        Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
        while (en.hasMoreElements()) {
            NetworkInterface ni = en.nextElement();
            if (ni.isLoopback() || ni.isVirtual() || !ni.isUp()) continue;
            byte[] hw = ni.getHardwareAddress();
            if (hw != null && hw.length > 0) {
                macs.add(toHex(hw));
            }
        }
        Collections.sort(macs);
        String base = String.join("-", macs);
        if (!base.isBlank()) return base;
        return InetAddress.getLocalHost().getHostName();
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }

    private static String normalize(String s) {
        return s.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
    }
}
