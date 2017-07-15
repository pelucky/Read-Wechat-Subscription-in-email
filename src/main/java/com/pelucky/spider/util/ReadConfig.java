package com.pelucky.spider.util;

import java.io.File;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.convert.DefaultListDelimiterHandler;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadConfig {
    private String fileName = "config.properties";
    private String url;
    private int lastDays;
    private String[] wxSubscription;
    private String[] toMail;
    private String fromMail;
    private String hostMail;
    private String password;
    private Logger logger = LoggerFactory.getLogger(ReadConfig.class);

    public ReadConfig() {
        try {
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<PropertiesConfiguration> builder = new FileBasedConfigurationBuilder<PropertiesConfiguration>(
                    PropertiesConfiguration.class)
                            .configure(params.fileBased().setListDelimiterHandler(new DefaultListDelimiterHandler(','))
                                    .setFile(new File(fileName)));
            PropertiesConfiguration configuration = builder.getConfiguration();

            url = configuration.getString("url");
            wxSubscription = configuration.getStringArray("wxSubscription");
            lastDays = configuration.getInt("lastDays");
            toMail = configuration.getStringArray("toMail");
            fromMail = configuration.getString("fromMail");
            hostMail = configuration.getString("hostMail");
            password = configuration.getString("password");
        } catch (ConfigurationException e) {
            logger.info("Read {} file failed!", fileName);
            e.printStackTrace();
        }
    }

    public String getFileName() {
        return fileName;
    }

    public String getUrl() {
        return url;
    }

    public int getLastDays() {
        return lastDays;
    }

    public String[] getWxSubscription() {
        return wxSubscription;
    }

    public String[] getToMail() {
        return toMail;
    }

    public String getFromMail() {
        return fromMail;
    }

    public String getHostMail() {
        return hostMail;
    }

    public String getPassword() {
        return password;
    }
}