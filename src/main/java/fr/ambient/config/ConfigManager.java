package fr.ambient.config;

import fr.ambient.Ambient;
import fr.ambient.util.ConfigUtil;
import fr.ambient.util.InstanceAccess;
import fr.ambient.util.system.FileUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class ConfigManager implements InstanceAccess {
    private static final Map<String, Config> configs = new HashMap<>();
    private Config activeConfig;

    public void init() {
        File configDir = new File(mc.mcDataDir, "/ambient/configs/");
        configDir.mkdirs();

        loadExistingConfigs();

        if (getConfig("default") == null) {
            Config defaultConfig = new Config("default");
            configs.put("default", defaultConfig);
            setActiveConfig(defaultConfig);
            saveConfig("default");
        } else {
            setActiveConfig(getConfig("default"));
        }
    }

    public void stop() {
        if (activeConfig != null) {
            saveConfig(activeConfig.getName());
        } else if (getConfig("default") == null) {
            Config config = new Config("default");
            configs.put("default", config);
            saveConfig("default");
        } else {
            saveConfig("default");
        }
    }

    private void loadExistingConfigs() {
        File configDir = new File(mc.mcDataDir, "/ambient/configs/");
        if (!configDir.exists()) return;

        File[] configFiles = configDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (configFiles == null) return;

        for (File configFile : configFiles) {
            String configName = configFile.getName().replace(".json", "");
            Config config = new Config(configName);
            configs.put(configName, config);
        }
    }

    public Config getConfig(String name) {
        return configs.get(name);
    }

    public Map<String, Config> getConfigs() {
        return configs;
    }

    public String saveConfig() {
        String randomName = UUID.randomUUID().toString();
        Config config = new Config(randomName);
        configs.put(randomName, config);
        saveConfig(randomName);
        return randomName;
    }

    public void saveConfig(String configName) {
        try {
            File configDir = new File(mc.mcDataDir, "/ambient/configs/");
            configDir.mkdirs();

            File configFile = new File(configDir, configName + ".json");
            String configData = ConfigUtil.write();

            java.nio.file.Files.write(configFile.toPath(), configData.getBytes());

            if (!configs.containsKey(configName)) {
                Config config = new Config(configName);
                configs.put(configName, config);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean loadConfig(String configName) {
        try {
            File configDir = new File(mc.mcDataDir, "/ambient/configs/");
            File configFile = new File(configDir, configName + ".json");

            if (!configFile.exists()) {
                return false;
            }

            com.google.gson.JsonObject configData = FileUtil.readJsonFromFile(configFile.getAbsolutePath());
            ConfigUtil.read(configData);

            Config config = configs.get(configName);
            if (config == null) {
                config = new Config(configName);
                configs.put(configName, config);
            }
            setActiveConfig(config);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteConfig(String configName) {
        if (configName.equals("default")) {
            return false;
        }

        try {
            File configDir = new File(mc.mcDataDir, "/ambient/configs/");
            File configFile = new File(configDir, configName + ".json");

            boolean deleted = configFile.delete();
            if (deleted) {
                configs.remove(configName);

                if (activeConfig != null && activeConfig.getName().equals(configName)) {
                    setActiveConfig(getConfig("default"));
                }
            }

            return deleted;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String[] getConfigNames() {
        return configs.keySet().toArray(new String[0]);
    }

    public int getConfigCount() {
        return configs.size();
    }
}