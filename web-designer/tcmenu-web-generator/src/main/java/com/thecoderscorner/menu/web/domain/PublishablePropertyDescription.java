package com.thecoderscorner.menu.web.domain;

import com.thecoderscorner.menu.editorui.generator.applicability.CodeApplicability;
import com.thecoderscorner.menu.editorui.generator.applicability.EqualityApplicability;
import com.thecoderscorner.menu.editorui.generator.applicability.MatchesApplicability;
import com.thecoderscorner.menu.editorui.generator.applicability.NeverApplicable;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.validation.PropValidationInfo;
import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class PublishablePropertyDescription {
    String name;
    String description;
    String extendedDescription;
    PropValidationInfo validation;
    String applicability;

    public PublishablePropertyDescription(CreatorProperty p) {
        this.name = p.getName();
        this.description = p.getDescription();
        this.extendedDescription = p.getExtendedDescription();
        this.validation = p.getValidationRules().getValidationInfo();
        this.applicability = applicabilityToWire(p.getApplicability());
    }

    private String applicabilityToWire(CodeApplicability applicability) {
        return switch(applicability) {
            case MatchesApplicability ma -> "match:%s:%s".formatted(ma.getPropertyId(), ma.getCompiledMatch().pattern());
            case EqualityApplicability ea -> "%s:%s:%s".formatted(ea.getMode(), ea.getPropertyId(), ea.getValue());
            case NeverApplicable _ -> "never";
            case null, default -> "always";
        };
    }
}
