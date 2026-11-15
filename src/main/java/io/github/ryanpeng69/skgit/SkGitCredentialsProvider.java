package io.github.ryanpeng69.skgit;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class SkGitCredentialsProvider {
    private static CredentialsProvider provider = null;
    public static CredentialsProvider getProvider() {
        return provider;
    }
    public static void loadCredentials() {
        String credentialsPath = SkGit.config.getString("credentials_path", "");
        File credentialsFile = new File(credentialsPath);
        if (!credentialsFile.isFile() || !credentialsFile.toPath().endsWith(".yml")) {
            credentialsFile = new File(SkGit.getInstance().getDataFolder(), "credentials.yml");
            SkGit.config.set("credentials_path", credentialsFile.getPath());
            SkGit.getInstance().saveConfig();
        }
        if (!credentialsFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            credentialsFile.getParentFile().mkdirs();
            SkGit.getInstance().saveResourceToFile("credentials.yml", credentialsFile, false);
        }

        FileConfiguration credConfig;
        try (FileInputStream fis = new FileInputStream(credentialsFile)) {
            credConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(fis, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read credentials file: " + credentialsFile, e);
        }
        String name = credConfig.getString("name", "");
        String password = credConfig.getString("password", "");
        if (name.isEmpty() && password.isEmpty()) {
            SkGit.getInstance().getLogger().warning("Credentials was not set, configure it at "+credentialsFile);
        }
        provider = new UsernamePasswordCredentialsProvider(name, password);
    }
}
