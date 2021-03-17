package de.pfannekuchen.lotas;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import de.pfannekuchen.lotas.gui.GuiConfiguration;
import rlog.RLogAPI;

public class ConfigManager {

    public static Properties props = new Properties();
    private static File configuration;

    public static void setString(String category, String key, String value) {
        props.setProperty(category + ":" + key, value);
        RLogAPI.logDebug("[ConfigManager] Category:Key " + category + ":" + key + " was set to String " + value);
    }

    public static void setInt(String category, String key, int value) {
        props.setProperty(category + ":" + key, value + "");
        RLogAPI.logDebug("[ConfigManager] Category:Key " + category + ":" + key + " was set to Integer " + value);
    }

    public static void setBoolean(String category, String key, boolean value) {
        props.setProperty(category + ":" + key, Boolean.toString(value));
        RLogAPI.logDebug("[ConfigManager] Category:Key " + category + ":" + key + " was set to Boolean " + value);
    }

    public static boolean getBoolean(String category, String key) {
        return Boolean.valueOf(props.getProperty(category + ":" + key, "false"));
    }

    public static String getString(String category, String key) {
        return props.getProperty(category + ":" + key, "null");
    }

    public static int getInt(String category, String key) {
        return Integer.valueOf(props.getProperty(category + ":" + key, "-1"));
    }

    public static void save() {
        try {
            FileWriter writer = new FileWriter(configuration);
            props.store(writer, "LoTAS Configuration File");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init(File configuration) throws IOException {
        ConfigManager.configuration = configuration;
        if (!configuration.exists()) configuration.createNewFile();
        props = new Properties();
        FileReader reader = new FileReader(configuration);
        props.load(reader);
        reader.close();

        // Defaults
        if (ConfigManager.getInt("hidden", "tickrate") <= 0) ConfigManager.setInt("hidden", "tickrate", 6);
        ConfigManager.save();

        RLogAPI.logDebug("[ConfigManager] Configuration File loaded");
        
        for (int i = 0; i < GuiConfiguration.optionsBoolean.length; i++) {
            String cat = GuiConfiguration.optionsBoolean[i].split(":")[1];
            String title = GuiConfiguration.optionsBoolean[i].split(":")[2];
            GuiConfiguration.optionsBoolean[i] = GuiConfiguration.optionsBoolean[i].replaceFirst("INSERT", getBoolean(cat, title) + "");
        }
        for (int i = 0; i < GuiConfiguration.optionsString.length; i++) {
            String cat = GuiConfiguration.optionsString[i].split(":")[1];
            String title = GuiConfiguration.optionsString[i].split(":")[2];
            GuiConfiguration.optionsString[i] = GuiConfiguration.optionsString[i].replaceFirst("INSERT", getString(cat, title) + "");
        }
        for (int i = 0; i < GuiConfiguration.optionsInteger.length; i++) {
            String cat = GuiConfiguration.optionsInteger[i].split(":")[1];
            String title = GuiConfiguration.optionsInteger[i].split(":")[2];
            GuiConfiguration.optionsInteger[i] = GuiConfiguration.optionsInteger[i].replaceFirst("INSERT", getInt(cat, title) + "");
        }
    }
}