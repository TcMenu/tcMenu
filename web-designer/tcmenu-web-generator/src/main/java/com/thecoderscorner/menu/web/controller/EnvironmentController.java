package com.thecoderscorner.menu.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/v1/environment")
public class EnvironmentController {

    private AtomicReference<String> activeProfileCached = new AtomicReference<>();

    public EnvironmentController() {
        cacheActiveVersion();
    }

    @GetMapping("/profile")
    public String getActiveProfile() {
        return activeProfileCached.get();
    }

    private void cacheActiveVersion() {
        String envLoaded;
        String activeProfile = System.getProperty("spring.profiles.active");
        if(activeProfile == null || activeProfile.isBlank()) {
            envLoaded = "noenv";
        } else {
            envLoaded = activeProfile;
        }

        envLoaded += " (";

        String version = "Unknown";
        try (InputStream is = getClass().getResourceAsStream("/version.properties")) {
            if (is != null) {
                Properties props = new Properties();
                props.load(is);
                version = props.getProperty("build.version", "Unknown");
                if(version.contains("${")) version = "Unknown";
            }
        } catch (Exception e) {
            version = "ERR";
        }

        envLoaded += version + ")";

        activeProfileCached.set(envLoaded);
    }
}
