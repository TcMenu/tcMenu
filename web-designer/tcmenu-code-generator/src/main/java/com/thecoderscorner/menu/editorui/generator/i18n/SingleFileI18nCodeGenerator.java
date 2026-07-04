package com.thecoderscorner.menu.editorui.generator.i18n;

import com.thecoderscorner.menu.editorui.generator.logger.GeneratedFile;
import com.thecoderscorner.menu.editorui.generator.logger.UserFeedbackLogger;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import com.thecoderscorner.menu.persist.PropertiesLocaleEnabledHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.LINE_BREAK;
import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.toSourceFile;

public class SingleFileI18nCodeGenerator extends I18nCodeGenerator {
    private static final String SINGLE_FILE_LOCALE_HEADER = """
            // TcMenu Generated locale header file containing all locale definitions.
            // To enable a particular language set build flag TC_LOCALE_<LANG>
            """;
    UserFeedbackLogger feedbackLogger;

    public SingleFileI18nCodeGenerator(UserFeedbackLogger feedbackLogger, LocaleMappingHandler localeHandler) {
        super(localeHandler);
        this.feedbackLogger = feedbackLogger;
    }

    @Override
    public void processLocale(Path srcDir, boolean generatedDir) throws IOException {
        feedbackLogger.info("Start locale processing");
        var previousLocale = localeHandler.getCurrentLocale();
        var selFile = Paths.get(toSourceFile(srcDir, "_langSelect" + ".h", generatedDir));
        try (var baos = new ByteArrayOutputStream(32768); var writer = new OutputStreamWriter(baos)) {
            writer.append(SINGLE_FILE_LOCALE_HEADER).append(LINE_BREAK);
            boolean writtenAnything = false;
            for(var locale : localeHandler.getEnabledLocales().stream().filter(l -> !l.getLanguage().isEmpty()).toList()) {
                localeHandler.changeLocale(locale);
                I18nConvertMode convertMode = writtenAnything ? I18nConvertMode.PROCESSING_NON_DEFAULT : I18nConvertMode.PROCESSING_FIRST_NON_DEFAULT;
                localeToCpp(writer, locale, localeHandler.getUnderlyingMap(), convertMode);
                writtenAnything = true;
            }
            if(!writtenAnything) {
                writer.append("#if defined(NOT_USING_I18N)").append(LINE_BREAK);
            }
            localeHandler.changeLocale(PropertiesLocaleEnabledHandler.DEFAULT_LOCALE);
            localeToCpp(writer, localeHandler.getCurrentLocale(), localeHandler.getUnderlyingMap(), I18nConvertMode.PROCESSING_DEFAULT_LOCALE);
            addTcLocaleStringMethod(writer);
            feedbackLogger.fileModificiation(GeneratedFile.always(selFile, writer.toString()));
            writer.flush();
            Files.write(selFile, baos.toString().getBytes());
            feedbackLogger.info("Wrote all locales to single file - " + selFile.getFileName());
            feedbackLogger.debug("Finished locale processing");
        } finally {
            localeHandler.changeLocale(previousLocale);
        }
    }
}
