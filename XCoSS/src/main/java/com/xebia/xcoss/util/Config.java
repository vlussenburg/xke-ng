package com.xebia.xcoss.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

public class Config extends Properties {

    private static Logger log = LoggerFactory.getLogger(Config.class);

    public Config() {
    	super();
    }

    public int getIntValue(String key) {
        return getIntValue(key, 0);
    }

    public int getIntValue(String key, int defaultValue) {
        String value = getProperty(key);
        if (StringUtils.isEmpty(value)) {
            return defaultValue;
        }
        int result = defaultValue;
        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Property '"+key+"' is not an integer: " + value);
        }
        return result;
    }

    public boolean getBooleanValue(String key) {
        String value = getProperty(key);
        return Boolean.parseBoolean(value);
    }

    public String[] getPropertyList(String key) {
        String value = getProperty(key);
        if (value == null) {
            return new String[0];
        }
        String[] data = value.split(",");
        String[] result = new String[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = data[i].trim();
        }
        return result;
    }
    
    public void loadFrom(String path) throws IOException {
    	InputStream is = null;
    	try {
			ClassPathResource rsc = new ClassPathResource("/" + path);
			is = rsc.getInputStream();
			load(is);
    	} finally {
    		if ( is != null ) {
    			is.close();
    		}
    	}
    }

}
