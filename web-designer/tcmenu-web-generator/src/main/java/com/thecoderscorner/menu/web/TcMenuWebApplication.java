package com.thecoderscorner.menu.web;

import com.thecoderscorner.menu.editorui.config.MenuEditorConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(MenuEditorConfig.class)
public class TcMenuWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(TcMenuWebApplication.class, args);
    }
}
