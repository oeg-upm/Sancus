package sancus;

import java.io.File;
import java.io.FileReader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Stores API keys for Ethereum and Etherscan.
 * Keys are loaded from keys.json in the working directory.
 * They can be overridden via command-line arguments:
 *   --ethkey="..." --etherscankey="..."
 */
public class Keys {

    private static String ethKey = "";
    private static String etherscanKey = "";
    private static boolean demoMode = false;

    /**
     * Loads API keys from keys.json in the working directory.
     * The file must contain "ethKey" and "etherscanKey" fields.
     * Keys can also be provided via --ethkey= and --etherscankey= arguments.
     */
    public static void loadFromJson() {
        File keysFile = new File("keys.json");
        if (!keysFile.exists()) {
            System.out.println("[Keys] keys.json not found. Provide keys via keys.json or command-line arguments.");
            return;
        }
        try (FileReader reader = new FileReader(keysFile)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (json.has("ethKey") && !json.get("ethKey").getAsString().isEmpty()) {
                ethKey = json.get("ethKey").getAsString();
            }
            if (json.has("etherscanKey") && !json.get("etherscanKey").getAsString().isEmpty()) {
                etherscanKey = json.get("etherscanKey").getAsString();
            }
            System.out.println("[Keys] Loaded keys from keys.json");
        } catch (Exception e) {
            System.out.println("[Keys] Error reading keys.json: " + e.getMessage());
        }
        if (ethKey.isEmpty()) {
            System.out.println("[Keys] WARNING: ethKey is not set. Block retrieval will not work.");
        }
        if (etherscanKey.isEmpty()) {
            System.out.println("[Keys] WARNING: etherscanKey is not set. ABI/Solidity retrieval will not work.");
        }
    }

    public static String getEthKey() {
        return ethKey;
    }

    public static void setEthKey(String ethKey) {
        Keys.ethKey = ethKey;
    }

    public static String getEtherscanKey() {
        return etherscanKey;
    }

    public static void setEtherscanKey(String etherscanKey) {
        Keys.etherscanKey = etherscanKey;
    }

    public static boolean isDemoMode() {
        return demoMode;
    }

    public static void setDemoMode(boolean demoMode) {
        Keys.demoMode = demoMode;
    }
}
