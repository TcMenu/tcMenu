package com.thecoderscorner.menu.editorui.generator.logger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class DelegatingUserFeedbackLogger implements UserFeedbackLogger {

    private final UserFeedbackLogger delegate;

    @Override
    public void debug(String data) {
        log.debug(data);
        delegate.debug(data);
    }

    @Override
    public void info(String data) {
        log.info(data);
        delegate.info(data);
    }

    @Override
    public void warn(String data) {
        log.warn(data);
        delegate.warn(data);
    }

    @Override
    public void error(String data, Exception e) {
        log.error(data, e);
        delegate.error(data, e);
    }

    @Override
    public void fileModificiation(GeneratedFile generatedFile) {
        log.debug("File processed {}", generatedFile);
        delegate.fileModificiation(generatedFile);
    }

    @Override
    public void error(String data) {
        log.error(data);
        delegate.error(data);
    }
}
