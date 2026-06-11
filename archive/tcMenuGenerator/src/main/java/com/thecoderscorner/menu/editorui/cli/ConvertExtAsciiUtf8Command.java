package com.thecoderscorner.menu.editorui.cli;

import com.thecoderscorner.menu.editorui.util.StringHelper;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@CommandLine.Command(name="convert-ascii-utf8")
public class ConvertExtAsciiUtf8Command implements Callable<Integer> {

    @CommandLine.Option(names = {"-o", "--output-utf8"}, description = "the file name to write to (can be omitted)")
    private String outputFile;
    @CommandLine.Option(names = {"-i", "--input-ascii"}, description = "the ascii file to convert to UTF8", required = true)
    private String inputFile;

    @Override
    public Integer call() {
        try {
            var str = Files.readString(Paths.get(inputFile), StandardCharsets.ISO_8859_1);
            System.out.println("Loaded ASCII in file " + inputFile);
            var out = (StringHelper.isStringEmptyOrNull(outputFile)) ? inputFile : outputFile;
            Files.writeString(Paths.get(out), str, StandardCharsets.UTF_8);
            System.out.println("Converted to UTF8 in file " + out);
            return 0;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
