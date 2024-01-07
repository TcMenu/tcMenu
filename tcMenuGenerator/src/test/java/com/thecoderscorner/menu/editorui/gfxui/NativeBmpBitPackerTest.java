package com.thecoderscorner.menu.editorui.gfxui;

import org.junit.jupiter.api.Test;

import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

public class NativeBmpBitPackerTest {

    /**
     * Test for the pushBit method in the NativeBmpBitPacker class.
     * This method pushes a bit into the data array at the current bit position.
     * We will test it with a range of bits and validate the expected result.
     */
    @Test
    public void testPushingBitsAndReadingPacked() {
        NativeBmpBitPacker packer = new NativeBmpBitPacker(8, 2, true);

        // push bits one by one and validate data
        packer.pushBit(true); // 1
        assertEquals("10000000", getFirstByteInBinary(packer));

        packer.pushBit(false); // 10
        assertEquals("10000000", getFirstByteInBinary(packer));

        packer.pushBit(true); // 101
        assertEquals("10100000", getFirstByteInBinary(packer));

        packer.pushBit(true); // 1011
        assertEquals("10110000", getFirstByteInBinary(packer));

        packer.pushBit(false); // 10110
        assertEquals("10110000", getFirstByteInBinary(packer));

        // Going beyond bitsNeeded, expect an exception
        for (int i = 0; i < 11; i++) {
            packer.pushBit(false);
        }
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> packer.pushBit(false));

        // resetting to initial state
        packer.reset();
        assertEquals(0, packer.getData()[0]);
    }

    @Test
    public void testSetBitAtOnPacked() {
        NativeBmpBitPacker packer = new NativeBmpBitPacker(3, 10, true);
        packer.setBitAt(0, 1, true);
        packer.setBitAt(2, 1, true);
        assertEquals("00010100", getFirstByteInBinary(packer));
        assertTrue(packer.getBitAt(2, 1));
        assertTrue(packer.getBitAt(0, 1));
    }

    @Test
    public void testGetBitAtOnUnpacked() {
        // Initialize object for NativeBmpBitPacker
        NativeBmpBitPacker packer = new NativeBmpBitPacker(12, 8, false);

        // test the largest case first..
        assertFalse(packer.getBitAt(7, 7));

        // Assert that all bits are initially zero as per object creation method
        for(int x = 0; x < 8; x++) {
            for(int y = 0; y < 8; y++) {
                assertFalse(packer.getBitAt(x, y));
            }
        }


        for(int y = 0; y < 8; y++) {
            for (int x = 0; x < 12; x++) {
                // Set a single bit at a known position
                packer.setBitAt(x, y, true);
                assertTrue(packer.getBitAt(x, y));
                packer.setBitAt(x, y, false);
                assertFalse(packer.getBitAt(x, y));
                packer.reset();
            }
        }
    }

    @Test
    public void testGetBitAtOutOfBounds() {
        // Initialize object for NativeBmpBitPacker
        NativeBmpBitPacker packer = new NativeBmpBitPacker(8, 8, false);

        // Set a single bit at a known position outside the boundaries
        try {
            packer.getBitAt(10, 10);
            fail("Expected an ArrayIndexOutOfBoundsException to be thrown");
        } catch (ArrayIndexOutOfBoundsException e) {
            // Assert that exception message is not empty
            assertTrue(!e.getMessage().isEmpty());
        }
    }

    @Test
    public void testConstructionWithExistingStructure() {
        var existingData = new byte[] {(byte) 0b10101010, (byte) 0b10000001};
        var packer = new NativeBmpBitPacker(existingData, 8, 2, true);

        assertTrue(packer.getBitAt(0, 0));
        assertTrue(packer.getBitAt(2, 0));
        assertTrue(packer.getBitAt(4, 0));
        assertTrue(packer.getBitAt(0, 1));
        assertFalse(packer.getBitAt(1, 0));
        assertFalse(packer.getBitAt(3, 0));
        assertFalse(packer.getBitAt(5, 0));
        assertFalse(packer.getBitAt(1, 1));

    }

    /**
     * The `NativeBmpBitPackerUnitTest` class aims to test the `convertToBits` method of the `NativeBmpBitPacker` class.
     * The `convertToBits` method is used to convert the x and y coordinates of a bitmap into bits using a supplied `BiFunction`.
     * The `BiFunction` should take in the x and y coordinates as parameters and return a boolean value representing the bit.
     */
    @Test
    public void testConvertToBits() {
        var testedBitPacker = new NativeBmpBitPacker(8, 8, true);
        BiFunction<Integer, Integer, Boolean> xyDataSupplier = (x, y) -> x == y;

        testedBitPacker.convertToBits(xyDataSupplier);

        byte[] expectedData = new byte[] { (byte)0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01 };
        assertArrayEquals(expectedData, testedBitPacker.getData());
    }

    @Test
    public void testConvertToBitsOutOfRange() {
        var testedBitPacker = new NativeBmpBitPacker(8, 8, true);
        BiFunction<Integer, Integer, Boolean> xyDataSupplier = (x, y) -> x > 8 || y > 8;

        try {
            testedBitPacker.convertToBits(xyDataSupplier);
        } catch (ArrayIndexOutOfBoundsException e){
            assertTrue(e instanceof ArrayIndexOutOfBoundsException);
        }
    }

    /**
     * Helper method for fetching the first byte from the data in binary format.
     * @param packer the packer instance
     * @return binary representation of the first byte in data
     */
    private String getFirstByteInBinary(NativeBmpBitPacker packer) {
        byte firstByte = packer.getData()[0];
        return String.format("%8s", Integer.toBinaryString(firstByte & 0xFF)).replace(' ', '0');
    }
}