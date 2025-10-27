package gravel_to_sand.graveltosand.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Properties;

public class Config {
    public static final Logger LOGGER = LoggerFactory.getLogger("Gravel To Sand Config");
    private static final File CONFIG_FILE = new File("config/gravel_to_sand.properties");
    private static final Properties properties = new Properties();

    public static double WATER_DEPLEAT_CHANCE;
    public static double CONVERSION_CHANCE;
    public static int CAULDRON_TIME;
    public static int CAULDRON_TICKS;

    public static void load(){
        CONFIG_FILE.getParentFile().mkdirs();

        if(CONFIG_FILE.exists()){
            try {
                properties.load(new FileInputStream(CONFIG_FILE));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        WATER_DEPLEAT_CHANCE = Double.parseDouble(properties.getProperty("waterDepleatChance", "0.1"));
        CONVERSION_CHANCE = Double.parseDouble(properties.getProperty("conversionChance", "0.01"));
        CAULDRON_TIME = Integer.parseInt(properties.getProperty("cauldronTime", "20"));
        CAULDRON_TICKS = Integer.parseInt(properties.getProperty("cauldronTicks", "5"));
        LOGGER.info(CONFIG_FILE.getAbsolutePath());

        save();
    }

    public static void save(){
        try {
            properties.store(new FileOutputStream(CONFIG_FILE), "Gravel To Sand");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
