package fr.ambient.command.impl;

import com.google.gson.JsonObject;
import fr.ambient.Ambient;
import fr.ambient.command.Command;
import fr.ambient.config.Config;
import fr.ambient.util.ConfigUtil;
import fr.ambient.util.player.ChatUtil;
import fr.ambient.util.system.FileUtil;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

import static fr.ambient.util.InstanceAccess.mc;

public class ConfigCommand extends Command {

    public ConfigCommand() {
        super("config", "c");
    }

    @Override
    @SneakyThrows
    public void execute(String[] args, String message) {
        try {
            String[] words = message.split(" ");

            if (words.length < 2 || words.length > 3) {
                ChatUtil.display("§cInvalid arguments!");
                ChatUtil.display("§7Usage: §f.config <save|load|list|delete> [name]");
                ChatUtil.display("§7Examples:");
                ChatUtil.display("§f  .config save myconfig");
                ChatUtil.display("§f  .config load myconfig");
                ChatUtil.display("§f  .config list");
                ChatUtil.display("§f  .config delete myconfig");
                return;
            }

            String action = words[1].toLowerCase();
            String configName = words.length == 3 ? words[2] : "default";

            switch (action) {
                case "list" -> listLocalConfigs();
                case "save" -> saveLocalConfig(configName);
                case "load" -> loadLocalConfig(configName);
                case "delete" -> deleteLocalConfig(configName);
                default -> {
                    ChatUtil.display("§cUnknown action: " + action);
                    ChatUtil.display("§7Available actions: §fsave, load, list, delete");
                }
            }
        } catch (Exception e) {
            ChatUtil.display("§cError executing config command: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void listLocalConfigs() {
        File configDir = new File(mc.mcDataDir, "/ambient/configs/");
        configDir.mkdirs();

        File[] configs = configDir.listFiles((dir, name) -> name.endsWith(".json"));

        if (configs == null || configs.length == 0) {
            ChatUtil.display("§7No configs found.");
            return;
        }

        ChatUtil.display("§aLocal Configs:");
        Arrays.stream(configs)
                .map(file -> file.getName().replace(".json", ""))
                .sorted()
                .forEach(name -> ChatUtil.display("§7  - §f" + name));

        ChatUtil.display("§7Total: §f" + configs.length + " §7configs");
    }

    private void saveLocalConfig(String configName) {
        try {
            File configDir = new File(mc.mcDataDir, "/ambient/configs/");
            configDir.mkdirs();

            File configFile = new File(configDir, configName + ".json");
            String configData = ConfigUtil.write();

            Files.write(configFile.toPath(), configData.getBytes());

            ChatUtil.display("§aConfig saved: §f" + configName);

            Config config = new Config(configName);
            Ambient.getInstance().getConfigManager().getConfigs().put(configName, config);

        } catch (Exception e) {
            ChatUtil.display("§cError saving config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadLocalConfig(String configName) {
        try {
            File configDir = new File(mc.mcDataDir, "/ambient/configs/");
            File configFile = new File(configDir, configName + ".json");

            if (!configFile.exists()) {
                ChatUtil.display("§cConfig not found: §f" + configName);
                ChatUtil.display("§7Use §f.config list §7to see available configs");
                return;
            }

            JsonObject configData = FileUtil.readJsonFromFile(configFile.getAbsolutePath());
            ConfigUtil.read(configData);

            ChatUtil.display("§aConfig loaded: §f" + configName);

            Config config = new Config(configName);
            Ambient.getInstance().getConfigManager().setActiveConfig(config);

        } catch (Exception e) {
            ChatUtil.display("§cError loading config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void deleteLocalConfig(String configName) {
        try {
            if (configName.equals("default")) {
                ChatUtil.display("§cCannot delete default config!");
                return;
            }

            File configDir = new File(mc.mcDataDir, "/ambient/configs/");
            File configFile = new File(configDir, configName + ".json");

            if (!configFile.exists()) {
                ChatUtil.display("§cConfig not found: §f" + configName);
                return;
            }

            if (configFile.delete()) {
                ChatUtil.display("§aConfig deleted: §f" + configName);

                Ambient.getInstance().getConfigManager().getConfigs().remove(configName);

                if (Ambient.getInstance().getConfigManager().getActiveConfig() != null &&
                        Ambient.getInstance().getConfigManager().getActiveConfig().getName().equals(configName)) {
                    loadLocalConfig("default");
                }
            } else {
                ChatUtil.display("§cFailed to delete config: §f" + configName);
            }

        } catch (Exception e) {
            ChatUtil.display("§cError deleting config: " + e.getMessage());
            e.printStackTrace();
        }
    }
}