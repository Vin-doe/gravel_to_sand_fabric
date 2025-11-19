package gravel_to_sand.graveltosand.config;

import com.moandjiezana.toml.TomlWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import com.moandjiezana.toml.Toml;

public class Config {
    public static final Logger LOGGER = LoggerFactory.getLogger("Gravel To Sand Config");
    private static final String CONFIG_FILE_PATH = "config/gravel_to_sand.toml";

    public static Double WATER_DEPLETE_CHANCE; // 0.1
    public static Double CONVERSION_CHANCE; // 0.01
    public static Integer CAULDRON_TIME; // 20
    public static Integer CAULDRON_TICKS; // 5

    public static void load(){
        try (InputStream stream = Files.newInputStream(Paths.get(CONFIG_FILE_PATH))){
            Toml toml = new Toml().read(stream);
            Double water_deplete_chance = toml.getDouble("waterDepleteChance");
            Double conversion_chance = toml.getDouble("conversionChance");
            Long cauldron_time = toml.getLong("cauldronTime");
            Long cauldron_ticks = toml.getLong("cauldronTicks");

            WATER_DEPLETE_CHANCE = water_deplete_chance == null ? 0.1 : clampValue(water_deplete_chance, 0.0, 1.0);
            CONVERSION_CHANCE = conversion_chance == null ? 0.01 : clampValue(conversion_chance, 0.0, 1.0);
            CAULDRON_TIME = cauldron_time == null ? 20 : clampValue(Math.toIntExact(cauldron_time), 1, 100000);
            CAULDRON_TICKS = cauldron_ticks == null ? 5 : clampValue(Math.toIntExact(cauldron_ticks), 1, 100000);

            if (water_deplete_chance == null || conversion_chance == null || cauldron_time == null || cauldron_ticks == null){
                LOGGER.warn("Config file damaged. Fixing broken keys with default values.");
                fixConfig();
            }
        }
         catch (IOException e){
            LOGGER.warn("Config file does not exist. Creating default file.");
            createConfig();
         }
    }

    /// lower: lower bound inclusive
    /// upper: upper bound exclusive
    private static Integer clampValue(Integer value, int lower, int upper){
        if (value == null){
            return null;
        }
        if (value < lower){
            return lower;
        }
        else if (value >= upper){
            return upper;
        }
        else{
            return value;
        }
    }

    private static Double clampValue(Double value, double lower, double upper){
        if (value == null){
            return null;
        }

        if (value < lower){
            return lower;
        }
        else if (value > upper){
            return upper;
        }
        else{
            return value;
        }
    }

    private static void createConfig(){
        TomlWriter tomlWriter = new TomlWriter();
        HashMap<String, Object> root = new HashMap<>();

        WATER_DEPLETE_CHANCE = 0.1;
        CONVERSION_CHANCE = 0.01;
        CAULDRON_TIME = 20;
        CAULDRON_TICKS = 5;
        root.put("waterDepleteChance", 0.1);
        root.put("conversionChance", 0.01);
        root.put("cauldron_time", 20);
        root.put("cauldronTicks", 5);

        try {
            tomlWriter.write(root, new File(CONFIG_FILE_PATH));
        } catch (IOException e) {
            LOGGER.warn("Config creation was not successful.");
            // shouldn't ever happen
        }
    }

    private static void fixConfig() {
        if (!Files.exists(Path.of(CONFIG_FILE_PATH))){
            createConfig();
            return;
        }

        TomlWriter tomlWriter = new TomlWriter();
        HashMap<String, Object> root = new HashMap<>();

        if (WATER_DEPLETE_CHANCE == null){
            root.put("waterDepleteChance", 0.1);
            WATER_DEPLETE_CHANCE = 0.1;
        }
        else{
            root.put("waterDepleteChance", WATER_DEPLETE_CHANCE);
        }

        if (CONVERSION_CHANCE == null){
            root.put("conversionChance", 0.01);
            CONVERSION_CHANCE = 0.01;
        }
        else{
            root.put("conversionChance", CONVERSION_CHANCE);
        }

        if (CAULDRON_TIME == null){
            root.put("cauldron_time", 20);
            CAULDRON_TIME = 20;
        }
        else{
            root.put("cauldron_time", CAULDRON_TIME);
        }

        if (CAULDRON_TICKS == null){
            root.put("cauldronTicks", 5);
            CAULDRON_TICKS = 5;
        }
        else{
            root.put("cauldronTicks", CAULDRON_TICKS);
        }

        try {
            tomlWriter.write(root, new File(CONFIG_FILE_PATH));
        } catch (IOException e) {
            LOGGER.warn("Config fixing was not successful.");
        }
    }

    /// if in-game config editing should be implemented
    public static void save(){

    }
}
