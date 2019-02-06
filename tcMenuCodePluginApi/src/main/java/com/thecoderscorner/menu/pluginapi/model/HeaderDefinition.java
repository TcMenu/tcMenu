package com.thecoderscorner.menu.pluginapi.model;

import java.util.Objects;

import static com.thecoderscorner.menu.pluginapi.AbstractCodeCreator.LINE_BREAK;

/**
 * Used internally by the variable builder to store header requirements.
 */
public class HeaderDefinition {
        private final String headerName;
        private final char startQuote, endQuote;

        public HeaderDefinition(String headerName, boolean useQuotes) {
            this.headerName = headerName;
            if(useQuotes) {
                startQuote = endQuote = '\"';
            }
            else {
                startQuote = '<';
                endQuote = '>';
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            HeaderDefinition that = (HeaderDefinition) o;
            return Objects.equals(headerName, that.headerName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(headerName);
        }

        public String getHeaderCode() {
            return "#include " + startQuote + headerName + endQuote;
        }
    }