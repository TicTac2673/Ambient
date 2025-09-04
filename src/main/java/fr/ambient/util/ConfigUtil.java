package fr.ambient.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.ambient.Ambient;
import fr.ambient.module.Module;
import fr.ambient.property.Property;
import fr.ambient.property.impl.*;
import fr.ambient.theme.Theme;
import fr.ambient.util.player.ChatUtil;
import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@UtilityClass
public class ConfigUtil {

    public void read(JsonObject object) {
        try {
            ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());

            String savedate = getStringSafe(object, "savedate", "0");
            String version = getStringSafe(object, "version", Ambient.getInstance().getVersion());
            String theme = getStringSafe(object, "theme", "Aqua");

            if (!Objects.equals(version, Ambient.getInstance().getVersion())) {
                ChatUtil.display("Loading outdated config...");
            }

            Theme dogTheme = Ambient.getInstance().getThemeManager().getThemeByName(theme);
            if (dogTheme == null) dogTheme = Ambient.getInstance().getThemeManager().getThemeByName("Aqua");
            if (dogTheme != null) {
                Ambient.getInstance().getHud().setCurrentTheme(dogTheme);
            }

            if (!object.has("moduleData") || object.get("moduleData").isJsonNull()) {
                ChatUtil.display("§cConfig has no module data!");
                return;
            }

            JsonObject modulesData = object.get("moduleData").getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : modulesData.entrySet()) {
                try {
                    Module module = Ambient.getInstance().getModuleManager().getModule(entry.getKey());
                    if (module == null) {
                        continue;
                    }

                    JsonElement moduleElement = modulesData.get(entry.getKey());
                    if (moduleElement.isJsonNull()) {
                        continue;
                    }

                    JsonObject moduleData = moduleElement.getAsJsonObject();

                    if (!module.getName().equals("ClickGUI")) {
                        module.setEnabled(getBooleanSafe(moduleData, "enabled", false));
                    }

                    module.setCustomName(getStringSafe(moduleData, "customname", module.getName()));
                    module.setKeyBind(getIntSafe(moduleData, "bind", 0));

                    try {
                        module.setShown(getBooleanSafe(moduleData, "showModule", true));
                    } catch (Exception e) {
                        // fuck ts shit
                    }

                    if (module.isDraggable() && moduleData.has("dragging") && !moduleData.get("dragging").isJsonNull()) {
                        JsonObject dragMoData = moduleData.get("dragging").getAsJsonObject();
                        int x = getIntSafe(dragMoData, "x", 10);
                        int y = getIntSafe(dragMoData, "y", 10);

                        module.setX(x);
                        module.setY(y);

                        if (module.getX() > sr.getScaledWidth()) {
                            module.setX((int) (Math.min(module.getX(), sr.getScaledWidth()) - module.getWidth()));
                        }
                        if (module.getY() > sr.getScaledHeight()) {
                            module.setY((int) (Math.min(module.getY(), sr.getScaledHeight()) - module.getHeight()));
                        }
                    }

                    if (moduleData.has("properties") && !moduleData.get("properties").isJsonNull()) {
                        JsonObject moduleSetting = moduleData.get("properties").getAsJsonObject();
                        applySetting(module.getPropertyList(), moduleSetting);
                    }
                } catch (Exception e) {
                    ChatUtil.display("§cError loading module: " + entry.getKey());
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            ChatUtil.display("§cError reading config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void applySetting(List<Property<?>> settings, JsonObject object) {
        for (Property<?> property : settings) {
            try {
                String label = property.getLabel();
                if (!object.has(label) || object.get(label).isJsonNull()) {
                    continue;
                }

                if (property instanceof ModeProperty modeProperty) {
                    String value = getStringSafe(object, label, modeProperty.getValue());
                    modeProperty.setValue(value);
                } else if (property instanceof NumberProperty numberProperty) {
                    float value = getFloatSafe(object, label, numberProperty.getValue());
                    numberProperty.setValue(value);
                } else if (property instanceof BooleanProperty booleanProperty) {
                    boolean value = getBooleanSafe(object, label, booleanProperty.getValue());
                    booleanProperty.setValue(value);
                } else if (property instanceof ColorProperty colorProperty) {
                    if (object.get(label).isJsonObject()) {
                        JsonObject colorProp = object.get(label).getAsJsonObject();
                        int red = getIntSafe(colorProp, "red", 255);
                        int green = getIntSafe(colorProp, "green", 255);
                        int blue = getIntSafe(colorProp, "blue", 255);
                        int alpha = getIntSafe(colorProp, "alpha", 255);
                        colorProperty.setValue(new Color(red, green, blue, alpha));
                    }
                } else if (property instanceof MultiProperty multiProperty) {
                    if (object.get(label).isJsonArray()) {
                        JsonArray multiArray = object.get(label).getAsJsonArray();
                        multiProperty.clearAll();
                        for (JsonElement element : multiArray) {
                            if (!element.isJsonNull()) {
                                multiProperty.setValueOF(element.getAsString(), true);
                            }
                        }
                    }
                } else if (property instanceof CompositeProperty compositeProperty) {
                    if (object.get(label).isJsonObject()) {
                        JsonObject compObj = object.get(label).getAsJsonObject();
                        applySetting(compositeProperty.getChildren(), compObj);
                    }
                }
            } catch (Exception e) {
                ChatUtil.display("§cError loading property: " + property.getLabel());
                e.printStackTrace();
            }
        }
    }

    private String getStringSafe(JsonObject object, String key, String defaultValue) {
        try {
            if (!object.has(key) || object.get(key).isJsonNull()) {
                return defaultValue;
            }
            return object.get(key).getAsString();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private int getIntSafe(JsonObject object, String key, int defaultValue) {
        try {
            if (!object.has(key) || object.get(key).isJsonNull()) {
                return defaultValue;
            }
            return object.get(key).getAsInt();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private float getFloatSafe(JsonObject object, String key, float defaultValue) {
        try {
            if (!object.has(key) || object.get(key).isJsonNull()) {
                return defaultValue;
            }
            return object.get(key).getAsFloat();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private boolean getBooleanSafe(JsonObject object, String key, boolean defaultValue) {
        try {
            if (!object.has(key) || object.get(key).isJsonNull()) {
                return defaultValue;
            }
            return object.get(key).getAsBoolean();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public String write() {
        try {
            JsonObject object = new JsonObject();

            object.addProperty("savedate", System.currentTimeMillis());
            object.addProperty("version", Ambient.getInstance().getVersion());
            object.addProperty("author", Ambient.getInstance().getUid());

            if (Ambient.getInstance().getThemeManager().getCurrentTheme() != null) {
                object.addProperty("theme", Ambient.getInstance().getThemeManager().getCurrentTheme().getName());
            } else {
                object.addProperty("theme", "Aqua");
            }

            JsonObject modulesObject = new JsonObject();

            for (Module module : Ambient.getInstance().getModuleManager().getObjects()) {
                try {
                    JsonObject moduleObject = new JsonObject();
                    moduleObject.addProperty("enabled", module.isEnabled());
                    moduleObject.addProperty("customname", module.getCustomName());
                    moduleObject.addProperty("bind", module.getKeyBind());
                    moduleObject.addProperty("showModule", module.isShown());

                    if (module.isDraggable()) {
                        JsonObject draggable = new JsonObject();
                        draggable.addProperty("x", module.getX());
                        draggable.addProperty("y", module.getY());
                        moduleObject.add("dragging", draggable);
                    }

                    JsonObject settingObject = new JsonObject();
                    addModuleSettingToJson(module.getPropertyList(), settingObject);
                    moduleObject.add("properties", settingObject);

                    modulesObject.add(module.getName(), moduleObject);
                } catch (Exception e) {
                    ChatUtil.display("§cError saving module: " + module.getName());
                    e.printStackTrace();
                }
            }

            object.add("moduleData", modulesObject);
            return object.toString();
        } catch (Exception e) {
            ChatUtil.display("§cError writing config: " + e.getMessage());
            e.printStackTrace();
            return "{}";
        }
    }

    public void addModuleSettingToJson(List<Property<?>> properties, JsonObject object) {
        for (Property<?> property : properties) {
            try {
                if (property instanceof ModeProperty modeProperty) {
                    object.addProperty(modeProperty.getLabel(), modeProperty.getValue());
                } else if (property instanceof NumberProperty numberProperty) {
                    object.addProperty(numberProperty.getLabel(), numberProperty.getValue());
                } else if (property instanceof BooleanProperty booleanProperty) {
                    object.addProperty(booleanProperty.getLabel(), booleanProperty.getValue());
                } else if (property instanceof ColorProperty colorProperty) {
                    JsonObject colObj = new JsonObject();
                    Color color = colorProperty.getValue();
                    if (color != null) {
                        colObj.addProperty("red", color.getRed());
                        colObj.addProperty("green", color.getGreen());
                        colObj.addProperty("blue", color.getBlue());
                        colObj.addProperty("alpha", color.getAlpha());
                    } else {
                        colObj.addProperty("red", 255);
                        colObj.addProperty("green", 255);
                        colObj.addProperty("blue", 255);
                        colObj.addProperty("alpha", 255);
                    }
                    object.add(colorProperty.getLabel(), colObj);
                } else if (property instanceof MultiProperty multiProperty) {
                    JsonArray multObj = new JsonArray();
                    for (String s : multiProperty.getValue()) {
                        multObj.add(s);
                    }
                    object.add(multiProperty.getLabel(), multObj);
                } else if (property instanceof CompositeProperty compositeProperty) {
                    JsonObject compObj = new JsonObject();
                    addModuleSettingToJson(compositeProperty.getChildren(), compObj);
                    object.add(compositeProperty.getLabel(), compObj);
                }
            } catch (Exception e) {
                ChatUtil.display("§cError saving property: " + property.getLabel());
                e.printStackTrace();
            }
        }
    }
}
