package fr.ambient.config;

import fr.ambient.util.ConfigUtil;
import fr.ambient.util.InstanceAccess;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;

@Getter
@Setter
public class Config implements InstanceAccess {

    private final String name;

    public Config(String name) {
        this.name = name;
    }

    public File getConfigFile() {
        File configDir = new File(mc.mcDataDir, "/ambient/configs/");
        configDir.mkdirs();
        return new File(configDir, name + ".json");
    }

    @SneakyThrows
    public void write() {
        String configData = ConfigUtil.write();
        Files.write(getConfigFile().toPath(), configData.getBytes());
    }

    public boolean exists() {
        return getConfigFile().exists();
    }

    public long getSize() {
        File configFile = getConfigFile();
        return configFile.exists() ? configFile.length() : 0;
    }

    public long getLastModified() {
        File configFile = getConfigFile();
        return configFile.exists() ? configFile.lastModified() : 0;
    }

    @Override
    public String toString() {
        return "Config{name='" + name + "', exists=" + exists() + "}";
    }
}