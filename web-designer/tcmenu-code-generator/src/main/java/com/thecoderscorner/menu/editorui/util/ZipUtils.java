package com.thecoderscorner.menu.editorui.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.lang.System.Logger.Level.DEBUG;

public class ZipUtils {
    public static final System.Logger logger = System.getLogger(ZipUtils.class.getName());
    public static void extractFilesFromZip(Path outDir, InputStream inStream) throws IOException {
        try(var zipStream =  new ZipInputStream(inStream)) {
            ZipEntry entry;
            while((entry = zipStream.getNextEntry())!=null) {
                Path filePath = outDir.resolve(entry.getName());
                String fileInfo = String.format("Entry: [%s] len %d to %s", entry.getName(), entry.getSize(), filePath);
                logger.log(DEBUG, fileInfo);

                if(!Files.exists(filePath.getParent())) {
                    Files.createDirectories(filePath.getParent());
                }

                if(entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    Files.write(filePath, zipStream.readAllBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                }
            }
        }
    }

    public static byte[] createZipFileFrom(Path outputDir) throws IOException {
        try (var baos = new java.io.ByteArrayOutputStream();
             var zipOut = new ZipOutputStream(baos)) {
            Files.walk(outputDir).forEach(path -> {
                if (path.toString().endsWith(".backup") || path.toString().contains(".backup" + java.io.File.separator)) return;
                if (Files.isDirectory(path)) return;
                try {
                    var relativePath = outputDir.relativize(path);
                    ZipEntry zipEntry = new ZipEntry(relativePath.toString().replace("\\", "/"));
                    zipOut.putNextEntry(zipEntry);
                    Files.copy(path, zipOut);
                    zipOut.closeEntry();
                } catch (IOException e) {
                    logger.log(System.Logger.Level.ERROR, "Failed to zip file " + path, e);
                }
            });
            zipOut.finish();
            return baos.toByteArray();
        }
    }
}
