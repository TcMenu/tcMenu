/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PersistedProject {
    private String version = "1.00";
    private String projectName;
    private String author;
    private Instant lastEdited;
    private ArrayList<PersistedMenu> items;
    private CodeGeneratorOptions codeOptions;

    public PersistedProject() {
    }

    public PersistedProject(String projectName, String author, Instant lastEdited, List<PersistedMenu> items,
                            CodeGeneratorOptions generatorOptions) {
        this.projectName = projectName;
        this.author = author;
        this.lastEdited = lastEdited;
        this.items = new ArrayList<>(items);
        this.codeOptions = generatorOptions;
    }

    public CodeGeneratorOptions getCodeOptions() {
        return codeOptions;
    }

    public String getVersion() {
        return version;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getAuthor() {
        return author;
    }

    public Instant getLastEdited() {
        return lastEdited;
    }

    public List<PersistedMenu> getItems() {
        return items;
    }


}
