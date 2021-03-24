package com.thecoderscorner.menu.editorui.generator.validation;

import java.util.Objects;

public class ChoiceDescription {
    private final String choiceValue;
    private final String choiceDesc;

    public ChoiceDescription(String choiceValue) {
        this.choiceDesc = choiceValue;
        this.choiceValue = choiceValue;
    }

    public ChoiceDescription(String choiceValue, String choiceDesc) {
        this.choiceValue = choiceValue;
        this.choiceDesc = choiceDesc;
    }

    public String getChoiceValue() {
        return choiceValue;
    }

    public String getChoiceDesc() {
        return choiceDesc;
    }

    @Override
    public String toString() {
        if(choiceDesc.equals(choiceValue)) {
            return choiceValue;
        }
        return choiceDesc + " (" + choiceValue + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChoiceDescription that = (ChoiceDescription) o;
        return Objects.equals(choiceValue, that.choiceValue) && Objects.equals(choiceDesc, that.choiceDesc);
    }

    @Override
    public int hashCode() {
        return Objects.hash(choiceValue, choiceDesc);
    }
}
