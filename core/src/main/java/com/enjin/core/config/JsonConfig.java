package com.enjin.core.config;

import com.enjin.core.Enjin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class JsonConfig {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static <T extends JsonConfig> T load(File file, Class<T> clazz) throws Exception {
        JsonConfig config = null;

        if (!file.exists() || file.length() == 0) {
            config = loadNew(clazz);
            config.save(file);
        } else {
            config = loadExisting(file, clazz);
        }

        if (config == null) {
            throw new Exception("Could not load config of type " + clazz.getName() + " at " + file.getPath());
        }

        return clazz.cast(config);
    }

    private static <T extends JsonConfig> T loadNew(Class<T> clazz) throws Exception {
        return clazz.newInstance();
    }

    private static <T extends JsonConfig> T loadExisting(File file, Class<T> clazz) throws Exception {
        return gson.fromJson(new FileReader(file), clazz);
    }

    public boolean save(File file) {
        try {
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file);
            fw.write(gson.toJson(this));
            fw.close();
        } catch (IOException e) {
            Enjin.getLogger().warning("Could not save the config to " + file.getName());
            return false;
        }

        return true;
    }

    public boolean update(File file, Object data) {
        JsonElement old     = gson.toJsonTree(this);
        JsonElement updates = gson.toJsonTree(data);

        if (!old.isJsonObject() && !updates.isJsonObject()) {
            Enjin.getLogger()
                 .warning("Could not update the config at " + file.getName() + " as it or the updated data is not an object.");
            return false;
        }

        JsonObject oldObj     = old.getAsJsonObject();
        JsonObject updatesObj = updates.getAsJsonObject();

        update(oldObj, updatesObj);

        try {
            FileWriter fw = new FileWriter(file);
            fw.write(gson.toJson(oldObj));
            fw.close();
        } catch (IOException e) {
            Enjin.getLogger().warning("Could not save the updated config to " + file.getName());
            return false;
        }

        return true;
    }

    private void update(JsonObject oldObj, JsonObject update) {
        for (Map.Entry<String, JsonElement> entry : update.getAsJsonObject().entrySet()) {
            if (!oldObj.has(entry.getKey())) {
                Enjin.getLogger().debug(entry.getKey() + " does not exists, updating value.");
                oldObj.add(entry.getKey(), entry.getValue());
                continue;
            }

            JsonElement element = oldObj.get(entry.getKey());
            if (entry.getValue().isJsonObject()) {
                Enjin.getLogger().debug(entry.getKey() + " is an object, processing object fields.");
                update(element.getAsJsonObject(), element.getAsJsonObject());
            } else {
                Enjin.getLogger().debug("Setting " + entry.getKey() + " to " + entry.getValue().toString());
                oldObj.add(entry.getKey(), entry.getValue());
            }
        }
    }
}