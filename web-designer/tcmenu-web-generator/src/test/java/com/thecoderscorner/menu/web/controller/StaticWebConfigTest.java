package com.thecoderscorner.menu.web.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class StaticWebConfigTest {

    @TempDir
    Path tempDir;

    static class TestResourceHandlerRegistry extends ResourceHandlerRegistry {
        public TestResourceHandlerRegistry(GenericWebApplicationContext context, MockServletContext servletContext) {
            super(context, servletContext);
        }

        @Override
        public AbstractHandlerMapping getHandlerMapping() {
            return super.getHandlerMapping();
        }
    }

    static class TestViewControllerRegistry extends ViewControllerRegistry {
        public TestViewControllerRegistry(GenericWebApplicationContext context) {
            super(context);
        }

        @Override
        public SimpleUrlHandlerMapping buildHandlerMapping() {
            return super.buildHandlerMapping();
        }
    }

    private MockHttpServletResponse executeRequest(SimpleUrlHandlerMapping handlerMapping, String uri) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE, uri);
        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerExecutionChain chain = handlerMapping.getHandler(request);
        assertNotNull(chain);
        ResourceHttpRequestHandler handler = (ResourceHttpRequestHandler) chain.getHandler();
        handler.handleRequest(request, response);
        return response;
    }

    @Test
    public void testStaticResourceServing() throws Exception {
        Files.writeString(tempDir.resolve("index.html"), "<html><body>Hello Static</body></html>");
        Path subDir = Files.createDirectory(tempDir.resolve("sub"));
        Files.writeString(subDir.resolve("sample.js"), "console.log('sample');");

        var config = new StaticWebConfig(tempDir.toAbsolutePath().toString());
        var servletContext = new MockServletContext();
        var context = new GenericWebApplicationContext(servletContext);
        context.refresh();

        var registry = new TestResourceHandlerRegistry(context, servletContext);
        config.addResourceHandlers(registry);

        var handlerMapping = (SimpleUrlHandlerMapping) registry.getHandlerMapping();
        assertNotNull(handlerMapping);
        handlerMapping.setApplicationContext(context);
        handlerMapping.initApplicationContext();

        // Test root index.html
        MockHttpServletResponse response = executeRequest(handlerMapping, "/index.html");
        assertEquals(200, response.getStatus());
        assertEquals("<html><body>Hello Static</body></html>", response.getContentAsString());

        // Test subfolder sample.js
        MockHttpServletResponse subResponse = executeRequest(handlerMapping, "/sub/sample.js");
        assertEquals(200, subResponse.getStatus());
        assertEquals("console.log('sample');", subResponse.getContentAsString());

        // Test not found
        assertThrows(org.springframework.web.servlet.resource.NoResourceFoundException.class, () -> {
            executeRequest(handlerMapping, "/missing.txt");
        });
    }

    @Test
    public void testLocationWithFilePrefixAndTrailingSlash() throws Exception {
        Files.writeString(tempDir.resolve("test.txt"), "test content");

        var config = new StaticWebConfig("file:" + tempDir.toAbsolutePath() + "/");
        var servletContext = new MockServletContext();
        var context = new GenericWebApplicationContext(servletContext);
        context.refresh();

        var registry = new TestResourceHandlerRegistry(context, servletContext);
        config.addResourceHandlers(registry);

        var handlerMapping = (SimpleUrlHandlerMapping) registry.getHandlerMapping();
        assertNotNull(handlerMapping);
        handlerMapping.setApplicationContext(context);
        handlerMapping.initApplicationContext();

        MockHttpServletResponse response = executeRequest(handlerMapping, "/test.txt");
        assertEquals(200, response.getStatus());
        assertEquals("test content", response.getContentAsString());
    }

    @Test
    public void testDefaultIndexViewController() throws Exception {
        var config = new StaticWebConfig("/some/dir");
        var servletContext = new MockServletContext();
        var context = new GenericWebApplicationContext(servletContext);
        context.refresh();

        var registry = new TestViewControllerRegistry(context);
        config.addViewControllers(registry);

        var handlerMapping = registry.buildHandlerMapping();
        assertNotNull(handlerMapping);
        handlerMapping.setApplicationContext(context);
        handlerMapping.initApplicationContext();

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        HandlerExecutionChain chain = handlerMapping.getHandler(request);
        assertNotNull(chain);
        assertTrue(chain.getHandler() instanceof ParameterizableViewController);
        ParameterizableViewController vc = (ParameterizableViewController) chain.getHandler();
        assertEquals("forward:/index.html", vc.getViewName());
    }
}
