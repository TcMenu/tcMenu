package com.thecoderscorner.menu.web.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EnvironmentControllerTest {
    @Test
    public void testGetActiveProfile() throws Exception {
        String original = "";
        try {
            original = System.getProperties().getProperty("spring.profiles.active");
            System.setProperty("spring.profiles.active", "dev");
            EnvironmentController controller = new EnvironmentController();
            MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

            mockMvc.perform(get("/api/v1/environment/profile"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(Matchers.matchesPattern("dev \\(.*\\)")));
        } finally {
            if(original == null) original = "";
            System.setProperty("spring.profiles.active", original);
        }
    }

    @Test
    public void testGetActiveProfileEmpty() throws Exception {
        EnvironmentController controller = new EnvironmentController();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/environment/profile"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.matchesPattern("noenv \\(.*\\)")));
    }

    @Test
    public void testGetActiveProfileNull() throws Exception {
        EnvironmentController controller = new EnvironmentController();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(get("/api/v1/environment/profile"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.matchesPattern("noenv \\(.*\\)")));
    }
}
