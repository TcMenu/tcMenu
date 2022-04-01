/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.domain.util;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.*;
import com.thecoderscorner.menu.remote.commands.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.thecoderscorner.menu.domain.state.AnyMenuState.StateStorageType.*;
import static java.lang.System.Logger.Level.ERROR;

/**
 * A helper class for dealing with MenuItem objects. This class provides the helper for visiting
 * menu items and returning a result. It also provides other helpers for dealing with items.
 */
public class MenuItemHelper {

    /**
     * Visits a menu item calling the appropriate function for the type and collects the result
     * that is set by calling your visitor's `setResult` method.
     * @param item the item to be visited
     * @param visitor the visitor that will be used
     * @param <T> the return type
     * @return an optional of the return type, set to empty unless setResult was called.
     */
    public static <T> Optional<T> visitWithResult(MenuItem item, AbstractMenuItemVisitor<T> visitor) {
        item.accept(visitor);
        return visitor.getResult();
    }

    /**
     * Returns the menu item as a sub menu or null
     * @param item the possible sub menu
     * @return the sub menu, or null.
     */
    public static SubMenuItem asSubMenu(MenuItem item) {
        return visitWithResult(item, new AbstractMenuItemVisitor<SubMenuItem>() {
            @Override
            public void visit(SubMenuItem item) {
                setResult(item);
            }

            @Override
            public void anyItem(MenuItem item) { /* ignored */ }
        }).orElse(null);
    }

    /**
     * Check if the item is based on a runtime item
     * @param item the item to check
     * @return true if runtime based, otherwise false.
     */
    public static boolean isRuntimeStructureNeeded(MenuItem item) {
        return visitWithResult(item, new AbstractMenuItemVisitor<Boolean>() {
            @Override
            public void visit(RuntimeListMenuItem listItem) {
                setResult(true);
            }

            @Override
            public void visit(EditableTextMenuItem item) {
                setResult(true);
            }

            @Override
            public void visit(EditableLargeNumberMenuItem item) {
                setResult(true);
            }

            @Override
            public void visit(ScrollChoiceMenuItem scrollItem) {
                setResult(true);
            }

            @Override
            public void visit(Rgb32MenuItem rgbItem) {
                setResult(true);
            }

            @Override
            public void visit(SubMenuItem item) {
                setResult(true); // needed for the back menu item
            }

            @Override
            public void anyItem(MenuItem item) {
                setResult(false);
            }
        }).orElse(false);
    }

    /**
     * creates a copy of the menu item chosen, with the ID changed to newId
     * @param selected the item to copy
     * @param newId the ID for the copy
     * @return the newly created item
     */
    public static MenuItem createFromExistingWithId(MenuItem selected, int newId) {
        return visitWithResult(selected, new AbstractMenuItemVisitor<MenuItem>() {
            @Override
            public void visit(AnalogMenuItem item) {
                setResult(AnalogMenuItemBuilder.anAnalogMenuItemBuilder()
                        .withExisting(item)
                        .withId(newId)
                        .menuItem()
                );
            }

            @Override
            public void visit(BooleanMenuItem item) {
                setResult(BooleanMenuItemBuilder.aBooleanMenuItemBuilder()
                        .withExisting(item)
                        .withId(newId)
                        .menuItem()
                );
            }

            @Override
            public void visit(EnumMenuItem item) {
                setResult(EnumMenuItemBuilder.anEnumMenuItemBuilder()
                        .withExisting(item)
                        .withId(newId)
                        .menuItem()
                );
            }

            @Override
            public void visit(SubMenuItem item) {
                setResult(SubMenuItemBuilder.aSubMenuItemBuilder()
                        .withExisting(item)
                        .withId(newId)
                        .menuItem()
                );
            }

            @Override
            public void visit(EditableTextMenuItem item) {
                setResult(EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                        .withExisting(item)
                        .withId(newId)
                        .menuItem()
                );
            }

            @Override
            public void visit(EditableLargeNumberMenuItem item) {
                setResult(EditableLargeNumberMenuItemBuilder.aLargeNumberItemBuilder()
                        .withExisting(item)
                        .withId(newId)
                        .menuItem()
                );
            }

            @Override
            public void visit(FloatMenuItem item) {
                setResult(FloatMenuItemBuilder.aFloatMenuItemBuilder()
                        .withExisting(item)
                        .withId(newId)
                        .menuItem()
                );
            }

            @Override
            public void visit(Rgb32MenuItem item) {
                setResult(new Rgb32MenuItemBuilder()
                        .withExisting(item)
                        .withId(newId)
                        .menuItem()
                );
            }

            @Override
            public void visit(CustomBuilderMenuItem item) {
                setResult(new CustomBuilderMenuItemBuilder()
                        .withExisting(item)
                        .withId(newId)
                        .menuItem()
                );
            }

            @Override
            public void visit(ScrollChoiceMenuItem item) {
                setResult(new ScrollChoiceMenuItemBuilder()
                        .withExisting(item)
                        .withId(newId)
                        .menuItem()
                );
            }

            @Override
            public void visit(ActionMenuItem item) {
                setResult(ActionMenuItemBuilder.anActionMenuItemBuilder()
                        .withExisting(item)
                        .withId(newId)
                        .menuItem()
                );
            }

            @Override
            public void visit(RuntimeListMenuItem item) {
                setResult(RuntimeListMenuItemBuilder.aRuntimeListMenuItemBuilder()
                        .withExisting(item)
                        .withId(newId)
                        .menuItem()
                );
            }
        }).orElse(null);
    }

    /**
     * Gets the size of the eeprom storage for a given element type
     * @param item the item to determine eeprom size for
     * @return the eeprom storage needed.
     */
    public static int eepromSizeForItem(MenuItem item) {
        if(item == null) return 0;
        return MenuItemHelper.visitWithResult(item, new AbstractMenuItemVisitor<Integer>() {
            @Override
            public void visit(AnalogMenuItem item) {
                setResult(2);
            }

            @Override
            public void visit(BooleanMenuItem item) {
                setResult(1);
            }

            @Override
            public void visit(EnumMenuItem item) {
                setResult(2);
            }

            @Override
            public void visit(EditableLargeNumberMenuItem numItem) {
                setResult(8);
            }

            @Override
            public void visit(Rgb32MenuItem numItem) {
                setResult(4);
            }

            @Override
            public void visit(ScrollChoiceMenuItem numItem) {
                setResult(2);
            }

            @Override
            public void visit(EditableTextMenuItem item) {
                if(item.getItemType() == EditItemType.IP_ADDRESS) {
                    setResult(4);
                }
                else if(item.getItemType() == EditItemType.PLAIN_TEXT) {
                    setResult(item.getTextLength());
                }
                else setResult(4); // all date and time types are 4 bytes long
            }

            @Override
            public void anyItem(MenuItem item) {
                setResult(0);
            }
        }).orElse(0);
    }

    public static AnyMenuState stateForMenuItem(AnyMenuState existingState, MenuItem item, Object val) {
        boolean changed = false;
        boolean active = false;
        if(existingState != null) {
            changed = existingState.isChanged();
            active = existingState.isActive();
        }
        return stateForMenuItem(item, val, changed, active);
    }

    public static AnyMenuState stateForMenuItem(AnyMenuState existingState, MenuItem item, Object val, boolean changed) {
        boolean active = false;
        if(existingState != null) {
            active = existingState.isActive();
        }
        return stateForMenuItem(item, val, changed, active);
    }

    public static Optional<AnyMenuState> applyIncrementalValueChange(MenuItem item, int delta, MenuTree tree) {
        var state = tree.getMenuState(item);
        if(state == null) state = MenuItemHelper.stateForMenuItem(item, 0, false, false);

        if(state.getStorageType() == INTEGER) {
            var intState = (IntegerMenuState) state;
            var val = intState.getValue() + delta;

            if(val < 0 || (item instanceof AnalogMenuItem && val > ((AnalogMenuItem) item).getMaxValue()) ||
                    (item instanceof EnumMenuItem && val > ((EnumMenuItem) item).getEnumEntries().size())) {
                return Optional.empty();
            }

            AnyMenuState menuState = stateForMenuItem(intState, item, intState.getValue() + delta);
            tree.changeItem(item, menuState);
            return Optional.ofNullable(menuState);
        }
        else if(state.getStorageType() == SCROLL_POSITION) {
            var scrState = (CurrentScrollPositionMenuState) state;
            var val = scrState.getValue().getPosition() + delta;
            if(val <= 0 || (item instanceof ScrollChoiceMenuItem && val >= ((ScrollChoiceMenuItem) item).getNumEntries())) {
                return Optional.empty();
            }
            var currentScrollPosition = new CurrentScrollPosition(scrState.getValue().getPosition() + delta, "");
            AnyMenuState menuState = stateForMenuItem(scrState, item, currentScrollPosition);
            tree.changeItem(item, menuState);
            return Optional.ofNullable(menuState);
        }
        return Optional.empty();
    }

    public static AnyMenuState stateForMenuItem(MenuItem item, Object v, boolean changed, boolean active) {
        if(item == null) return new BooleanMenuState(item, false, false, false);
         var val = (v!=null) ? v : defaultValueForItem(item);

        return MenuItemHelper.visitWithResult(item, new AbstractMenuItemVisitor<AnyMenuState>() {
            @Override
            public void visit(AnalogMenuItem item) {
                int res = (val instanceof String) ? Integer.parseInt(val.toString()) : ((Number)val).intValue();
                if(res < 0) res = 0;
                if(res > item.getMaxValue()) res = item.getMaxValue();
                setResult(new IntegerMenuState(item, changed, active, res));
            }

            @Override
            public void visit(BooleanMenuItem item) {
                boolean res;
                if(val instanceof String) {
                   if(((String) val).length() == 1) {
                       res = ((String) val).charAt(0) == '1' || ((String) val).charAt(0) == 'Y';
                   }
                   else {
                       res = Boolean.parseBoolean((String)val);
                   }
                }
                else if(val instanceof Number) {
                    res = ((Number) val).intValue() != 0;
                }
                else res = (boolean)val;

                setResult(new BooleanMenuState(item, changed, active, res));
            }

            @Override
            public void visit(EnumMenuItem item) {
                int res = (val instanceof String) ? Integer.parseInt(val.toString()) : ((Number)val).intValue();
                if(res < 0) res = 0;
                if(res >= item.getEnumEntries().size()) res = item.getEnumEntries().size() - 1;
                setResult(new IntegerMenuState(item, changed, active, res));
            }

            @Override
            public void visit(SubMenuItem item) {
                setResult(new BooleanMenuState(item, changed, active, false));
                super.visit(item);
            }

            @Override
            public void visit(EditableTextMenuItem item) {
                setResult(new StringMenuState(item, changed, active, val.toString()));

            }

            @Override
            public void visit(ActionMenuItem item) {
                setResult(new BooleanMenuState(item, changed, active, false));
            }

            @Override
            public void visit(FloatMenuItem item) {
                float res = (val instanceof String) ? Float.parseFloat(val.toString()) : ((Number)val).floatValue();
                setResult(new FloatMenuState(item, changed, active, res));
            }

            @SuppressWarnings("unchecked")
            @Override
            public void visit(RuntimeListMenuItem listItem) {
                setResult(new StringListMenuState(item, changed, active, (List<String>)val));
            }

            @Override
            public void visit(EditableLargeNumberMenuItem numItem) {
                BigDecimal dec;
                if(val instanceof String) {
                    var value = ((String) val).replaceAll("[\\[\\]]", "");
                    dec = new BigDecimal(value);
                }
                else dec = (BigDecimal)val;
                setResult(new BigDecimalMenuState(item, changed, active, dec));
            }

            @Override
            public void visit(ScrollChoiceMenuItem scrollItem) {
                CurrentScrollPosition pos;
                if(val instanceof Integer) pos = new CurrentScrollPosition((int)val, "");
                else if(val instanceof CurrentScrollPosition) pos = (CurrentScrollPosition) val;
                else pos = new CurrentScrollPosition(val.toString());
                if(pos.getPosition() >= 0 && pos.getPosition() < scrollItem.getNumEntries()) {
                    setResult(new CurrentScrollPositionMenuState(item, changed, active, pos));
                }
                else setResult(new CurrentScrollPositionMenuState(scrollItem, changed, active,
                        new CurrentScrollPosition(0, "No entries")));
            }

            @Override
            public void visit(Rgb32MenuItem rgbItem) {
                PortableColor res = (val instanceof String) ? new PortableColor(val.toString()) : (PortableColor) val;
                setResult(new PortableColorMenuState(item, changed, active, res));
            }

            @Override
            public void anyItem(MenuItem item) {
                setResult(new BooleanMenuState(item, changed, active, false));
            }
        }).orElseThrow();
    }

    private static Object defaultValueForItem(MenuItem item) {
        return MenuItemHelper.visitWithResult(item, new AbstractMenuItemVisitor<>() {
            @Override
            public void visit(AnalogMenuItem item) {
                setResult(0);
            }

            @Override
            public void visit(EnumMenuItem item) {
                setResult(0);
            }

            @Override
            public void visit(EditableTextMenuItem item) {
                setResult("");
            }

            @Override
            public void visit(FloatMenuItem item) {
                setResult(0.0F);
            }

            @Override
            public void visit(ScrollChoiceMenuItem scrollItem) {
                setResult(new CurrentScrollPosition(0, ""));
            }

            @Override
            public void visit(RuntimeListMenuItem listItem) {
                setResult(List.of());
            }

            @Override
            public void visit(EditableLargeNumberMenuItem numItem) {
                setResult(BigDecimal.ZERO);
            }

            @Override
            public void visit(Rgb32MenuItem rgbItem) {
                setResult(new PortableColor("#000000"));
            }

            @Override
            public void anyItem(MenuItem item) {
                setResult(false);
            }
        }).orElse(false);
    }

    public static void setMenuState(MenuItem item, Object value, MenuTree tree) {
        var oldState = tree.getMenuState(item);
        if(oldState != null) {
            tree.changeItem(item, stateForMenuItem(item, value, !value.equals(oldState.getValue()), oldState.isActive()));
        } else {
            tree.changeItem(item, stateForMenuItem(item, value, false, false));
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValueFor(MenuItem item, MenuTree tree, T def) {
        if(tree.getMenuState(item) != null) {
            try {
                return (T) tree.getMenuState(item).getValue();
            }
            catch (Exception ex) {
                System.getLogger(MenuItemHelper.class.getSimpleName()).log(ERROR, "State type incorrect", ex);
            }
        }
        tree.changeItem(item, MenuItemHelper.stateForMenuItem(item, def, false, false));
        return def;
    }

    public static Optional<BootItemMenuCommand<?, ?>> getBootMsgForItem(MenuItem item, SubMenuItem parent, MenuTree tree) {
        if(item instanceof AnalogMenuItem) {
            return Optional.of(new MenuAnalogBootCommand(parent.getId(), (AnalogMenuItem) item, getValueFor(item, tree, 0)));
        } else if(item instanceof EnumMenuItem) {
            return Optional.of(new MenuEnumBootCommand(parent.getId(), (EnumMenuItem) item, getValueFor(item, tree, 0)));
        } else if(item instanceof FloatMenuItem) {
            return Optional.of(new MenuFloatBootCommand(parent.getId(), (FloatMenuItem) item, getValueFor(item, tree, 0.0F)));
        } else if(item instanceof BooleanMenuItem) {
            return Optional.of(new MenuBooleanBootCommand(parent.getId(), (BooleanMenuItem) item, getValueFor(item, tree, false)));
        } else if(item instanceof SubMenuItem) {
            return Optional.of(new MenuSubBootCommand(parent.getId(), (SubMenuItem) item, false));
        } else if(item instanceof ActionMenuItem) {
            return Optional.of(new MenuActionBootCommand(parent.getId(), (ActionMenuItem) item, false));
        } else if(item instanceof EditableLargeNumberMenuItem) {
            return Optional.of(new MenuLargeNumBootCommand(parent.getId(), (EditableLargeNumberMenuItem) item, getValueFor(item, tree, BigDecimal.ZERO)));
        } else if(item instanceof  EditableTextMenuItem) {
            return Optional.of(new MenuTextBootCommand(parent.getId(), (EditableTextMenuItem) item, getValueFor(item, tree, "")));
        } else if(item instanceof Rgb32MenuItem) {
            return Optional.of(new MenuRgb32BootCommand(parent.getId(), (Rgb32MenuItem) item, getValueFor(item, tree, new PortableColor(0, 0, 0))));
        } else if(item instanceof  RuntimeListMenuItem) {
            return Optional.of(new MenuRuntimeListBootCommand(parent.getId(), (RuntimeListMenuItem) item, getValueFor(item, tree, List.of())));
        } else if(item instanceof  ScrollChoiceMenuItem) {
            return Optional.of(new MenuScrollChoiceBootCommand(parent.getId(), (ScrollChoiceMenuItem) item, getValueFor(item, tree, new CurrentScrollPosition(0, ""))));
        }
        return Optional.empty();
    }

    public static Object getDefaultFor(MenuItem item) {
        if(item instanceof AnalogMenuItem || item instanceof EnumMenuItem) {
            return 0;
        } else if(item instanceof FloatMenuItem) {
            return 0.0F;
        } else if(item instanceof BooleanMenuItem || item instanceof SubMenuItem || item instanceof ActionMenuItem) {
            return false;
        } else if(item instanceof EditableLargeNumberMenuItem) {
            return BigDecimal.ZERO;
        } else if(item instanceof  EditableTextMenuItem) {
            return "";
        } else if(item instanceof Rgb32MenuItem) {
            return new PortableColor(0, 0, 0);
        } else if(item instanceof  RuntimeListMenuItem) {
            return List.of();
        } else if(item instanceof  ScrollChoiceMenuItem) {
            return new CurrentScrollPosition(0, "");
        }
        else throw new IllegalArgumentException("Unknown menu type " + item + "of type " + item.getClass());
    }
}
