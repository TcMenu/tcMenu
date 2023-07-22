package com.thecoderscorner.menu.domain.util;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.CurrentScrollPosition;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;

import java.math.BigDecimal;

public class MenuItemFormatter {
    private static final MenuItemFormatter defaultInstance = new MenuItemFormatter();
    private LocaleMappingHandler localHandler = LocaleMappingHandler.NOOP_IMPLEMENTATION;

    public MenuItemFormatter(LocaleMappingHandler handler) {
        if(handler != null) localHandler = handler;
    }

    private MenuItemFormatter() {
    }

    public static MenuItemFormatter defaultInstance() {
        return defaultInstance;
    }

    public static void setDefaultLocalHandler(LocaleMappingHandler localHandler) {
        defaultInstance.localHandler = localHandler;
    }

    public String bundleIfPossible(String s) {
        return localHandler.getFromLocaleWithDefault(s, s);
    }

    public String formatToWire(MenuItem item, final String text) {
        return MenuItemHelper.visitWithResult(item, new AbstractMenuItemVisitor<String>() {
            @Override
            public void anyItem(MenuItem item) {
                throw new IllegalArgumentException(item + "not editable");
            }

            @Override
            public void visit(AnalogMenuItem item) {
                setResult(formatAnalogWire(item, text));
            }

            @Override
            public void visit(EnumMenuItem item) {
                setResult(formatEnumWire(item, text));
            }

            @Override
            public void visit(BooleanMenuItem item) {
                setResult(formatBoolWire(item, text));
            }

            @Override
            public void visit(EditableLargeNumberMenuItem item) {
                setResult(formatLargeNumWire(item, text));
            }

            @Override
            public void visit(Rgb32MenuItem item) {
                setResult(formatRgbItemWire(item, text));
            }

            @Override
            public void visit(ScrollChoiceMenuItem item) {
                setResult(formatScrollItemWire(item, text));
            }

            @Override
            public void visit(EditableTextMenuItem item) {
                setResult(formatEditableTextWire(item, text));
            }
        }).orElseThrow();
    }

    private String formatEditableTextWire(EditableTextMenuItem et, String text) {
        if (et.getItemType() == EditItemType.PLAIN_TEXT && text.length() < et.getTextLength()) {
            return text;
        } else if (et.getItemType() == EditItemType.IP_ADDRESS) {
            if (!text.matches("\\d+\\.\\d+\\.\\d+\\.\\d+")) return "0.0.0.0";
            return text;
        } else if (et.getItemType() == EditItemType.TIME_24H || et.getItemType() == EditItemType.TIME_24_HUNDREDS || et.getItemType() == EditItemType.TIME_12H) {
            // time is always sent back to the server in 24 hour format, it is always possible (but optional) to provide hundreds/sec.
            if (!text.matches("\\d+:\\d+:\\d+(.\\d*)*")) return "12:00:00";
            return text;
        } else if (et.getItemType() == EditItemType.GREGORIAN_DATE) {
            if (!text.matches("\\d+/\\d+/\\d+")) return "01/01/2000";
            return text;
        }
        return "";
    }

    private static String formatLargeNumWire(EditableLargeNumberMenuItem ln, String text) {
        BigDecimal val;
        try {
            val = new BigDecimal(text);
        } catch (Exception ex) {
            return "0";
        }
        return val.toString();
    }

    private String formatRgbItemWire(Rgb32MenuItem rgb, String text) {
        return new PortableColor(text).toString();
    }

    private String formatScrollItemWire(ScrollChoiceMenuItem scroll, String text) {
        int val = 0;
        try {
            val = Integer.parseInt(text);
        } catch (Exception ex) {
            // ignored
        }
        return new CurrentScrollPosition(val, text).toString();
    }

    private String formatBoolWire(BooleanMenuItem bi, String text) {
        text = text.toUpperCase();
        if (text.equals("ON") || text.equals("YES") || text.equals("TRUE")) return "1";
        else if (text.equals("OFF") || text.equals("NO") || text.equals("FALSE")) return "0";
        return "0";
    }

    private String formatEnumWire(EnumMenuItem en, String text) {
        return text;
    }

    private int GetActualDecimalDivisor(int divisor) {
        if (divisor < 2) return 1;
        return (divisor > 1000) ? 10000 : (divisor > 100) ? 1000 : (divisor > 10) ? 100 : 10;
    }

    private String formatAnalogWire(AnalogMenuItem an, String text) {
        return text;
    }

    public String formatForDisplay(MenuItem item, Object data) {
        if (item == null || data == null) return "";

        if (item instanceof FloatMenuItem) {
            return formatFloatForDisplay((FloatMenuItem) item, (float) data);
        } else if (item instanceof AnalogMenuItem) {
            return formatAnalogForDisplay((AnalogMenuItem) item, (int) data);
        } else if (item instanceof BooleanMenuItem) {
            return formatBoolForDisplay((BooleanMenuItem) item, (boolean) data);
        } else if (item instanceof EnumMenuItem) {
            return formatEnumForDisplay((EnumMenuItem) item, (int) data);
        } else if (item instanceof EditableLargeNumberMenuItem) {
            return formatLargeNumForDisplay((EditableLargeNumberMenuItem) item, (BigDecimal) data);
        } else if (item instanceof EditableTextMenuItem) {
            return formatTextForDisplay((EditableTextMenuItem) item, (String) data);
        } else if (item instanceof Rgb32MenuItem) {
            return formatRgbItemForDisplay((Rgb32MenuItem) item, (PortableColor) data);
        } else if (item instanceof ScrollChoiceMenuItem) {
            return formatScrollItemForDisplay((ScrollChoiceMenuItem) item, (CurrentScrollPosition) data);
        } else {
            return "";
        }
    }

    private String formatScrollItemForDisplay(ScrollChoiceMenuItem sc, CurrentScrollPosition data) {
        return data.getValue();
    }

    private String formatRgbItemForDisplay(Rgb32MenuItem rgb, PortableColor col) {
        return col.toString();
    }

    private String formatTextForDisplay(EditableTextMenuItem tm, String data) {
        return data;
    }

    private String formatLargeNumForDisplay(EditableLargeNumberMenuItem ln, BigDecimal data) {
        return data.toString();
    }

    private String formatEnumForDisplay(EnumMenuItem en, int data) {
        if (en.getEnumEntries().size() > data) {
            return en.getEnumEntries().get(data);
        }
        return "";
    }

    private String formatBoolForDisplay(BooleanMenuItem bl, boolean val) {
        switch (bl.getNaming()) {
            case ON_OFF:
                return val ? "On" : "Off";
            case YES_NO:
                return val ? "Yes" : "No";
            case TRUE_FALSE:
            default:
                return val ? "True" : "False";
        }
    }

    private String formatAnalogForDisplay(AnalogMenuItem an, int val) {
        int calcVal = val + an.getOffset();
        int divisor = an.getDivisor();

        if (divisor < 2) {
            return Integer.toString(calcVal) + bundleIfPossible(an.getUnitName());
        } else {
            int whole = calcVal / divisor;
            int fractMax = GetActualDecimalDivisor(an.getDivisor());
            int fraction = Math.abs((calcVal % divisor)) * (fractMax / divisor);

            return String.format("%d.%0" + calculateRequiredDigits(divisor) + "d%s", whole, fraction,
                    localHandler.getFromLocaleOrUseSource(an.getUnitName()));
        }
    }

    private int calculateRequiredDigits(int divisor) {
        return (divisor <= 10) ? 1 : (divisor <= 100) ? 2 : (divisor <= 1000) ? 3 : 4;
    }

    private String formatFloatForDisplay(FloatMenuItem fl, float val) {
        return String.format("%." + fl.getNumDecimalPlaces() + "f", val);
    }

    public String getItemName(MenuItem item) {
        return localHandler.getFromLocaleWithDefault(item.getName(), item.getName());
    }
}