package org.example.init;

import java.io.*;
import java.util.Properties;

/**
 * @Classname Config
 * @Description 读取配置文件
 * @Date 2022/4/14 13:10
 * @author mavenr
 */
public class Config {

    // 配置文件
    private static File configFile = new File(System.getProperty("user.dir") + File.separator + "init.ini");

    private static Properties properties;

    /**
     * 读取配置文件
     */
    public static void createProperties() {
        // 加载配置文件
        properties = new Properties();

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(configFile);
            properties.load(new InputStreamReader(fileInputStream, "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询配置文件信息
     * @param key
     * @return
     */
    public static String get(String key) {
        return properties.getProperty(key);
    }

    /**
     * 将值存入到配置文件
     * @param key
     * @param value
     */
    public static void set(String key, String value) {
        properties.setProperty(key, value);
        try {
            properties.store(new FileOutputStream(configFile), "重写"+key+"参数");
        } catch (IOException e) {
            System.out.println("未读取到配置文件！");
        }
    }
}
