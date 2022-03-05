package com.thecoderscorner.menu.editorui.generator.ejava;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class GeneratedStatementTest {

    @Test
    public void testDefaultImplementations() {
        var hasVisited = new AtomicBoolean(false);
        var generated = new GeneratedStatement() {
            @Override
            public void generate(JavaClassBuilder cb, StringBuilder sb) {
                hasVisited.set(true);
            }
        };
        generated.generate(null, null);
        assertTrue(hasVisited.get());
        assertFalse(generated.isPatchable());
        assertThrows(IllegalStateException.class, () -> generated.getMatchingPattern(null));
        assertThrows(IllegalStateException.class, generated::getEndOfBlockPattern);
    }
}