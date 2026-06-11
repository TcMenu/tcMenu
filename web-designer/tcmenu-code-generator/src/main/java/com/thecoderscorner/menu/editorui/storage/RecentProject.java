package com.thecoderscorner.menu.editorui.storage;

import java.time.LocalDateTime;

public record RecentProject(String file, LocalDateTime lastOpened) { }
