package com.thecoderscorner.menu.web.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

@Configuration
@Profile("!local")
public class StaticWebConfig implements WebMvcConfigurer {
    private final String staticDir;

    public StaticWebConfig(@Value("${tcmenu.web.static-dir:/opt/tcmenu/web}") String staticDir) {
        this.staticDir = staticDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = staticDir;
        if (!location.startsWith("file:") && !location.startsWith("classpath:")) {
            location = "file:" + location;
        }
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/**")
                .addResourceLocations(location)
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = super.getResource(resourcePath, location);
                        if (requestedResource != null && requestedResource.isReadable()) {
                            return requestedResource;
                        }
                        String normalized = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
                        if (!normalized.startsWith("api/") && !normalized.equals("api") && !normalized.contains(".")) {
                            return super.getResource("index.html", location);
                        }
                        return null;
                    }
                });
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}