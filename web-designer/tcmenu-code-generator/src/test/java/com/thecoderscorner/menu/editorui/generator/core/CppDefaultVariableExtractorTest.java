package com.thecoderscorner.menu.editorui.generator.core;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.thecoderscorner.menu.editorui.generator.core.CppDefaultVariableExtractor.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CppDefaultVariableExtractorTest {

    /**
     * Tests for the splitComponents method in MenuBuilderTreeCodeGeneratorImpl
     * <p>
     * The splitComponents method is used to split a BigDecimal into its integer
     * whole number and fractional components, separated by scale.
     */

    @Test
    public void testSplitComponents_positiveValue() {
        // Arrange
        BigDecimal input = new BigDecimal("123.45");

        // Act
        Parts result = splitComponents(input);

        // Assert
        assertEquals(123, result.whole());
        assertEquals(45, result.fraction());
        assertEquals(2, result.scale());
    }

    @Test
    public void testSplitComponents_negativeValue() {
        // Arrange
        BigDecimal input = new BigDecimal("-678.90");

        // Act
        Parts result = splitComponents(input);

        // Assert
        assertEquals(-678, result.whole());
        assertEquals(90, result.fraction());
        assertEquals(2, result.scale());
    }

    @Test
    public void testSplitComponents_zeroValue() {
        // Arrange
        BigDecimal input = BigDecimal.ZERO;

        // Act
        Parts result = splitComponents(input);

        // Assert
        assertEquals(0, result.whole());
        assertEquals(0, result.fraction());
        assertEquals(0, result.scale());
    }

    @Test
    public void testSplitComponents_largeFraction() {
        // Arrange
        BigDecimal input = new BigDecimal("999.999");

        // Act
        Parts result = splitComponents(input);

        // Assert
        assertEquals(999, result.whole());
        assertEquals(999, result.fraction());
        assertEquals(3, result.scale());
    }

    @Test
    public void testSplitComponents_noFraction() {
        // Arrange
        BigDecimal input = new BigDecimal("500");

        // Act
        Parts result = splitComponents(input);

        // Assert
        assertEquals(500, result.whole());
        assertEquals(0, result.fraction());
        assertEquals(0, result.scale());
    }

    @Test
    public void testSplitComponents_smallFraction() {
        // Arrange
        BigDecimal input = new BigDecimal("0.01");

        // Act
        Parts result = splitComponents(input);

        // Assert
        assertEquals(0, result.whole());
        assertEquals(1, result.fraction());
        assertEquals(2, result.scale());
    }

    @Test
    public void testSplitComponents_negativeSmallFraction() {
        // Arrange
        BigDecimal input = new BigDecimal("-0.001");

        // Act
        Parts result = splitComponents(input);

        // Assert
        assertEquals(0, result.whole());
        assertEquals(1, result.fraction());
        assertEquals(3, result.scale());
    }
}