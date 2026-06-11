package com.thecoderscorner.menu.editorui.storage;

public enum TcConfigEntryType {
        DEFAULT_FONT_IMPORT_DIR("FONT_IMPORT_DIR", 1),
        DEFAULT_BMP_IMPORT_DIR("BMP_IMPORT_DIR", 2);

        private final String key;
        private final int pk;

        TcConfigEntryType(String key, int pk) {
            this.key = key;
            this.pk = pk;
        }

        public String getKey() {
            return key;
        }

        public int getPk() {
            return pk;
        }
    }