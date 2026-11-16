package io.github.ryanpeng69.skgit;

import io.github.ryanpeng69.skgit.features.Updater;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bstats.bukkit.Metrics;
import com.github.shanebeee.skr.Registration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class SkGit extends JavaPlugin {

    private static SkGit instance;
    public static FileConfiguration config;
    Registration registration;

    @Override
    public void onEnable() {
        instance = this;

        registration = new Registration("skgit", false);
        SkGitRegistration.register(registration);

        new Metrics(this, 34187);

        loadConfig();

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            io.papermc.paper.command.brigadier.Commands commands = event.registrar();
            commands.register(SkGitCmd.createCommandTree().build());
        });

        getLogger().info("SkGit enabled");

        Updater.checkUpdate().thenAccept(update -> {
            if (update == null) {
                getLogger().severe("Error occurred while checking update");
            } else if (update.equals("latest")) {
                getLogger().info("Up to date");
            } else {
                getLogger().warning("New release: "+update);
            }
        });
    }

    public static SkGit getInstance() {
        return instance;
    }

    public static void loadConfig() {
        instance.saveDefaultConfig();
        instance.reloadConfig();
        config = instance.getConfig();
        config.options().copyDefaults(true);
        instance.saveConfig();
        SkGitCredentialsProvider.loadCredentials();
    }

    public void saveResourceToFile(String resourcePath, File targetFile, boolean replace) {
        if (targetFile.exists() && !replace) {
            return;
        }
        try (InputStream in = getResource(resourcePath)) {
            if (in == null) {
                getLogger().warning("Resource not found in jar: " + resourcePath);
                return;
            }
            Files.copy(in, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            getLogger().severe("Could not copy resource to "+targetFile.getPath()+"\nCause: "+e.getMessage());
        }
    }

    public static void logAction(String message) {
        if (config.getBoolean("log.actions", false)) instance.getLogger().info(message);
    }
    public static void throwError(Exception e) {
        throwError(e.getMessage());
    }
    public static void throwError(String e) {
        if (config.getBoolean("send_error.console")) instance.getLogger().severe(e);
        if (config.getBoolean("send_error.ops")) instance.getServer().broadcast(
                Component.text("[SkGit] ERROR: "+e).color(NamedTextColor.RED), "op");
    }
}