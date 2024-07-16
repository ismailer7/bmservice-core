package com.bmservice.core.helper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import java.util.TreeMap;

@Slf4j
public class Mapper {

    public static Object getMapValue(final TreeMap map, final String key, final Object defaultValue) {
        if (map != null && !map.isEmpty() && map.containsKey(key)) {
            final Object value = map.get(key);
            if (value != null) {
                return value;
            }
        }
        return defaultValue;
    }

    public static Object getMapValue(final HashMap map, final String key, final Object defaultValue) {
        if (map != null && !map.isEmpty() && map.containsKey(key)) {
            final Object value = map.get(key);
            if (value != null) {
                return value;
            }
        }
        return defaultValue;
    }

    public static HashMap<String, String> readProperties(final String filePath) {
        final HashMap<String, String> results = new HashMap<String, String>();
        final Properties properties = new Properties();
        try {
            final InputStream in = FileUtils.openInputStream(new File(filePath));
            properties.load(in);
            properties.stringPropertyNames().forEach(key -> {
                String value = properties.getProperty(key);
                results.put(key, value);
                return;
            });
        }
        catch (IOException e) {
            log.error(e.getMessage());
        }
        return results;
    }

}
