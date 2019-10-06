package com.qaperf.ai.utils;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

public class ConfigLoader {
    private static ConfigLoader loader;
    private static Config config;

    private ConfigLoader(){

    }

    public static Config loadConfig(){
        if(loader==null){
            loader=new ConfigLoader();
            config= loader.loadYaml();
        }
    return config;
    }

    private Config loadYaml(){
        Yaml yaml = new Yaml();
        InputStream inputStream = ConfigLoader.class
                .getResourceAsStream("/config.yml");
        config = yaml.loadAs(inputStream, Config.class);
        System.out.println(config);
        return config;
    }

}
