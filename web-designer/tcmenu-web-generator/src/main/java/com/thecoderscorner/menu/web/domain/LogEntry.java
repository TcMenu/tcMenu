package com.thecoderscorner.menu.web.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.event.Level;

@Data
@AllArgsConstructor
public class LogEntry {
    private String log;
    private Level level;
}
