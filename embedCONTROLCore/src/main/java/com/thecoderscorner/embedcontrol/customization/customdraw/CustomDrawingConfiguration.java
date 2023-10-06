package com.thecoderscorner.embedcontrol.customization.customdraw;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.PortableColor;

import java.util.Optional;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.RenderingStatus;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType;

public interface CustomDrawingConfiguration<T> {
    NoOpCustomDrawingConfiguration NO_CUSTOM_DRAWING = new NoOpCustomDrawingConfiguration();

    boolean isSupportedFor(MenuItem item);
    String getName();
    Optional<ControlColor> getColorFor(T value);

    default ControlColor getColorFor(T value, ConditionalColoring colorSet, RenderingStatus status,
                                     ColorComponentType componentType) {
        return getColorFor(value).orElseGet(() -> new ControlColor(
                colorSet.foregroundFor(status, componentType),
                colorSet.backgroundFor(status, componentType)
        ));
    }

    record NumericColorRange(double start, double end, PortableColor fg, PortableColor bg) { }

    public class NoOpCustomDrawingConfiguration implements CustomDrawingConfiguration<Boolean> {
        public static final String CUSTOM_DRAW_NONE = "none";

        @Override
        public boolean isSupportedFor(MenuItem item) {
            return true;
        }

        @Override
        public String getName() {
            return "default";
        }

        @Override
        public String toString() {
            return "None";
        }

        @Override
        public Optional<ControlColor> getColorFor(Boolean value) {
            return Optional.empty();
        }
    }

}

