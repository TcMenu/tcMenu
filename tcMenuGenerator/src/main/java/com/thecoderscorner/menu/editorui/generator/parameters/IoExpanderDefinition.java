package com.thecoderscorner.menu.editorui.generator.parameters;

import com.thecoderscorner.menu.editorui.generator.parameters.expander.*;
import com.thecoderscorner.menu.editorui.util.StringHelper;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static java.lang.System.Logger.Level.ERROR;

public abstract class IoExpanderDefinition implements CodeGeneratorCapable {
    private final static System.Logger logger = System.getLogger(IoExpanderDefinition.class.getSimpleName());

    public abstract String getNicePrintableName();

    public abstract String getVariableName();

    public abstract String getId();

    public static Optional<IoExpanderDefinition> fromString(String currentSel) {
        try {
            if (StringHelper.isStringEmptyOrNull(currentSel) || currentSel.equals("deviceIO:")) {
                return Optional.of(new InternalDeviceExpander());
            } else if (currentSel.startsWith("customIO:")) {
                var parts = currentSel.split(":");
                return Optional.of(new CustomDeviceExpander(parts[1]));
            } else if (currentSel.startsWith("pcf8574:")) {
                var parts = currentSel.split(":");
                var invert = parts.length > 4 && Boolean.parseBoolean(parts[4]);
                return Optional.of(new Pcf8574DeviceExpander(parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), invert));
            } else if(currentSel.startsWith("pcf8575:")) {
                var parts = currentSel.split(":");
                var invert = parts.length > 4 && Boolean.parseBoolean(parts[4]);
                return Optional.of(new Pcf8575DeviceExpander(parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]), invert));
            } else if (currentSel.startsWith("mcp23017:")) {
                var parts = currentSel.split(":");
                return Optional.of(new Mcp23017DeviceExpander(parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3])));
            }
            else if(!currentSel.contains(":")){
                return Optional.of(new CustomDeviceExpander(currentSel));
            }
            else {
                throw new IOException("Invalid string format " + currentSel);
            }
        } catch (Exception ex) {
            logger.log(ERROR, "Did not convert " + currentSel, ex);
            return Optional.empty();
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;

        if(obj instanceof IoExpanderDefinition iod) {
            return Objects.equals(getId(), iod.getId());
        }
        else return false;
    }

    @Override
    public Optional<String> generateExport() {
        return Optional.empty();
    }
}
