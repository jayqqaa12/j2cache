package com.jayqqaa12.j2cache.util;

import java.util.ResourceBundle;

import static com.jayqqaa12.j2cache.CacheConstans.CONFIG_FILE;


public class ConfigUtil {
    protected static ResourceBundle resourceBundle;

    static {
        String url = CONFIG_FILE;
        url = url.replace(".properties", "");
        try {
            resourceBundle = ResourceBundle.getBundle(url);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("sourceUrl = " + url + "file load error!", e);
        }
    }

    public static String getStr(String key, String defaultValue) {
        return resourceBundle.getString(key) == null ? defaultValue : resourceBundle.getString(key);
    }


    public static String getStr(String key) {
        return resourceBundle.getString(key);
    }


    public static int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(getStr(key, "").trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        try {
            return "true".equalsIgnoreCase(getStr(key, "").trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

 

}
