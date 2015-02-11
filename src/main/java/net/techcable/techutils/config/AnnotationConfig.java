package net.techcable.techutils.config;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import lombok.*;

public abstract class AnnotationConfig {
    @Getter(AccessLevel.PRIVATE)
    private FileConfiguration config;
    
    /**
     * Retreive the path of the configuration file
     * 
     * @return the configuration file
     */
    public abstract File getFile();
    
    /**
     * Retreive the path from witch to fetch the default configuration
     * 
     * @return the default config path
     */
    public URL getDefaultConfigResource() {
        return Resources.getResource(getFile().getName());
    }
    
    public void loadDefault() {
        CharSource source = Resources.asCharSource(getDefaultConfigResource(), Charsets.UTF_8);
        CharSink sink = Files.asCharSink(getFile(), Charsets.UTF_8);
        try {
			source.copyTo(sink);
		} catch (Exception e) {
			Throwables.propagate(e);
		}
    }
    
    public void loadValues() {
        for (Field field : getClass().getFields()) {
            if (!field.isAnnotationPresent(Setting.class)) continue;
            String key = field.getAnnotation(Setting.class).value();
            if (!getConfig().contains(key)) continue;
            Object value = getConfig().get(key);
            try {
                field.setAccessible(true);
                
                field.set(this, value);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "Unable to set configuration value", e);
            }
        }
    }
    
    public void saveValues() {
        for (Field field : getClass().getFields()) {
            if (!field.isAnnotationPresent(Setting.class)) continue;
            String key = field.getAnnotation(Setting.class).value();
            Object value = null;
            try {
                field.setAccessible(true);
                
                value = field.get(this);
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "Unable to retreive configuration value", e);
            }
            if (value != null) getConfig().set(key, value);
        }
    }
}