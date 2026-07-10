package com.thecoderscorner.menu.editorui.generator.i18n;

import com.thecoderscorner.menu.editorui.generator.logger.GeneratedFile;
import com.thecoderscorner.menu.editorui.generator.logger.UserFeedbackLogger;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import com.thecoderscorner.menu.persist.PropertiesLocaleEnabledHandler;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Map;

import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.*;

public class MultiFileI18nCodeGenerator extends I18nCodeGenerator {
    private final UserFeedbackLogger feedbackLogger;

    public MultiFileI18nCodeGenerator(UserFeedbackLogger feedbackLogger, LocaleMappingHandler localMapper) {
        super(localMapper);
        this.feedbackLogger = feedbackLogger;
    }

    @Override
    public void processLocale(Path srcDir, boolean generatedDir) throws IOException {
        feedbackLogger.debug("Starting locale processing");
        var previousLocale = localeHandler.getCurrentLocale();

        try(var baos = new ByteArrayOutputStream(8192); var langSelectWriter = new OutputStreamWriter(baos)) {
            langSelectWriter.append(GENERATED_LOCAL_HEADER).append(LINE_BREAK);
            langSelectWriter.append("// This is the header to include. Set TC_LOCAL_?? to a locale").append(LINE_BREAK);
            langSelectWriter.append("// or omit for the default language").append(TWO_LINES);
            String defaultLocaleFile = toSourceFile(srcDir, "_lang" + ".h", generatedDir);
            localeHandler.changeLocale(PropertiesLocaleEnabledHandler.DEFAULT_LOCALE);
            var defaultLocaleMap = localeHandler.getUnderlyingMap();
            writeOutFile(defaultLocaleFile, PropertiesLocaleEnabledHandler.DEFAULT_LOCALE, defaultLocaleMap);

            boolean useElIf = false;

            for (var locale : localeHandler.getEnabledLocales().stream().filter(l -> !l.getLanguage().isEmpty()).toList()) {
                String localeFile = toSourceFile(srcDir, "_lang_" + locale.toString() + ".h", generatedDir);
                localeHandler.changeLocale(locale);
                writeOutFile(localeFile, locale, defaultLocaleMap);

                if (useElIf) {
                    langSelectWriter.append("#elif");
                } else {
                    langSelectWriter.append("#if");
                    useElIf = true;
                }
                langSelectWriter.append(" defined(TC_LOCALE_").append(locale.toString().toUpperCase()).append(')')
                        .append(LINE_BREAK);
                langSelectWriter.append("# include \"").append(Paths.get(localeFile).getFileName().toString())
                        .append("\"").append(LINE_BREAK);
            }

            Path defPath = Paths.get(defaultLocaleFile);
            if (useElIf) {
                langSelectWriter.append("#else").append(LINE_BREAK).append("#include \"")
                        .append(defPath.getFileName().toString()).append("\"").append(LINE_BREAK)
                        .append("#endif").append(LINE_BREAK);
            } else {
                langSelectWriter.append("#include \"").append(defPath.getFileName().toString()).append("\"")
                        .append(LINE_BREAK);
            }
            addTcLocaleStringMethod(langSelectWriter);

            langSelectWriter.flush();
            var selFile = Paths.get(toSourceFile(srcDir, "_langSelect" + ".h", generatedDir));
            Files.writeString(selFile, baos.toString(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            feedbackLogger.fileModificiation(GeneratedFile.always(selFile, baos.toString()));
            feedbackLogger.info("Finished processing locale, Wrote out language selector - " + selFile.getFileName());
        }
        finally {
            // put back the same locale as was selected before.
            localeHandler.changeLocale(previousLocale);
        }
    }

    private void writeOutFile(String defaultLocaleFile, Locale defaultLocale, Map<String, String> defaultLocaleMap) {
        try(var writer = new OutputStreamWriter(new FileOutputStream(defaultLocaleFile))) {
            localeToCpp(writer, defaultLocale, defaultLocaleMap, I18nConvertMode.LOCALE_PER_FILE);
        }
        catch(IOException ex) {
            feedbackLogger.error("Failed to write out default locale file", ex);
        }
    }
}
