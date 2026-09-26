package com.thecoderscorner.menu.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@RestController
@RequestMapping("/api/v1/environment")
public class EnvironmentController {

    private final AtomicReference<VersionInformation> activeProfileCached = new AtomicReference<>();

    public EnvironmentController(@Value("${hosted.versions.path:}") String hostedVersionsPath) {
        cacheActiveVersion(hostedVersionsPath);
    }

    @GetMapping("/profile")
    public String getActiveProfile() {
        return activeProfileCached.get().currentVersion();
    }

    @GetMapping("/availableProfiles")
    public List<HostedSubDomain> getHostedVersions() {
        return activeProfileCached.get().hostedSubDomains();
    }

    private void cacheActiveVersion(String hostedVersionsPath) {
        String envLoaded;
        String activeProfile = System.getProperty("spring.profiles.active");
        if(activeProfile == null || activeProfile.isBlank()) {
            envLoaded = "noenv";
        } else {
            envLoaded = activeProfile.toUpperCase(Locale.ROOT);
        }

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
        
        List<HostedSubDomain> hostedVersions = new ArrayList<>();
        if(StringUtils.hasLength(hostedVersionsPath)) {
            Path path = Paths.get(hostedVersionsPath);
            if (Files.exists(path)) {
                try(Reader reader = Files.newBufferedReader(path)) {
                    Gson gson = new Gson();
                    hostedVersions = gson.fromJson(reader, new TypeToken<List<HostedSubDomain>>() {}.getType());
                } catch (Exception e) {
                    log.error("Failed to load hosted versions", e);
                }
            }
        }

        activeProfileCached.set(new VersionInformation(envLoaded + " " + version, hostedVersions));
    }
    
    public record HostedSubDomain(String subdomain, String description, String versionPattern, String url) { }
    public record VersionInformation(String currentVersion, List<HostedSubDomain> hostedSubDomains) { }
}
