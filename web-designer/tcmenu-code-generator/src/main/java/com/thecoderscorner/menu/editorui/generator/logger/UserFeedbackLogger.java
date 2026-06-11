package com.thecoderscorner.menu.editorui.generator.logger;

public interface UserFeedbackLogger {
    void debug(String data);
    void info(String data);
    void warn(String data);
    void error(String data);
    void error(String data, Exception ex);
    void fileModificiation(GeneratedFile generatedFile);
}
