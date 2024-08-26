package com.thecoderscorner.menu.editorui.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CoreCommandTests {
    final PrintStream originalOut = System.out;
    final PrintStream originalErr = System.err;
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();

    @BeforeEach
    public void setUpStreams() {
        out.reset();
        err.reset();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void testVersionCommand() {
        runCommand("version");
        assertTrue(out.toString().contains("TcMenu Designer"));
        assertTrue(out.toString().contains("Built on"));
    }

    private void runCommand(String command) {
        String[] args = command.split("\\s+");
        new CommandLine(new TcMenuDesignerCmd()).execute(args);
    }
}
