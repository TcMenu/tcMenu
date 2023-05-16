package com.thecoderscorner.embedcontrol.core.simulator;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.*;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.domain.util.MenuItemVisitor;
import com.thecoderscorner.menu.remote.*;
import com.thecoderscorner.menu.remote.commands.*;
import com.thecoderscorner.menu.remote.protocol.ApiPlatform;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.thecoderscorner.menu.domain.state.MenuTree.ROOT;
import static com.thecoderscorner.menu.remote.commands.MenuChangeCommand.ChangeType;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;

public class SimulatedRemoteConnection implements RemoteConnector {
    private final System.Logger logger = System.getLogger(SimulatedRemoteConnection.class.getSimpleName());
    private final ScheduledExecutorService executor;
    private final MenuTree tree;
    private final UUID uuid;
    private final String simName;
    private final int latencyMillis;
    private final Map<Integer, Object> valuesById;
    private final RuntimeListMenuItem simUpdateList;
    private final List<String> recentUpdates = new ArrayList<>();
    public AuthStatus authStatus;
    public RemoteInformation remoteInfo;
    private RemoteConnectorListener connectorListener;
    private ConnectionChangeListener connectionChangeListener;

    public SimulatedRemoteConnection(MenuTree tree, String simName, UUID uuid, int latencyMillis,
                                     Map<Integer, Object> valuesById, ScheduledExecutorService executor) {
        this.tree = tree;
        this.executor = executor;
        this.simName = simName;
        this.latencyMillis = latencyMillis;
        this.valuesById = valuesById;
        this.uuid = uuid;
        this.remoteInfo = new RemoteInformation(simName, uuid, 1234, 1, 1, ApiPlatform.JAVA_API);
        var simulatorMenu = new SubMenuItemBuilder()
                .withId(60001)
                .withName("Simulator Options")
                .menuItem();
        tree.addMenuItem(ROOT, simulatorMenu);

        simUpdateList = new RuntimeListMenuItemBuilder()
                .withId(60002)
                .withName("Recent Updates")
                .withInitialRows(10)
                .menuItem();
        tree.addMenuItem(simulatorMenu, simUpdateList);

        connectorListener = (connector, data) -> logger.log(DEBUG, "Nothing attached, data = " + data);
        connectionChangeListener = (connector, authStatus) -> logger.log(DEBUG, "Nothing attached, status = " + authStatus);
    }

    @Override
    public String getUserName() {
        return simName;
    }

    @Override
    public String getConnectionName() {
        return simName;
    }

    @Override
    public boolean isDeviceConnected() {
        return authStatus == AuthStatus.CONNECTION_READY;
    }

    @Override
    public RemoteInformation getRemoteParty() {
        return remoteInfo;
    }

    @Override
    public AuthStatus getAuthenticationStatus() {
        return authStatus;
    }

    @Override
    public void registerConnectorListener(RemoteConnectorListener listener) {
        connectorListener = listener;
    }

    @Override
    public void registerConnectionChangeListener(ConnectionChangeListener listener) {
        connectionChangeListener = listener;
    }

    @Override
    public void close() {
        executor.schedule(this::start, 1, TimeUnit.SECONDS);
    }

    @Override
    public void sendMenuCommand(MenuCommand command) {
        if (authStatus == AuthStatus.NOT_STARTED) {
            logger.log(DEBUG, "Msg before start called");
        }

        if (command instanceof MenuChangeCommand) {
            processChange((MenuChangeCommand) command);
        } else if (command instanceof MenuDialogCommand) {
            processDialog((MenuDialogCommand) command);
        }
    }

    private void processDialog(MenuDialogCommand dc) {
        executor.schedule(() -> {
            if (dc.getDialogMode() == DialogMode.ACTION && dc.getCorrelationId() != null) {
                sendDialogAction(DialogMode.HIDE, "", "", MenuButtonType.NONE, MenuButtonType.NONE, CorrelationId.EMPTY_CORRELATION);
                connectorListener.onCommand(this, new MenuAcknowledgementCommand(dc.getCorrelationId(), AckStatus.SUCCESS));
            }
        }, latencyMillis, TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("unchecked")
    private void processChange(MenuChangeCommand ch) {
        executor.schedule(() -> {

            var item = tree.getMenuById(ch.getMenuItemId()).orElseThrow();
            if (ch.getChangeType() == ChangeType.DELTA) {
                logger.log(DEBUG, "Delta change on id " + item.getId());
                var state = (MenuState<Integer>) tree.getMenuState(item);
                var prevVal = state.getValue() != null ? state.getValue() : 0;
                int newVal = prevVal + Integer.parseInt(ch.getValue());

                if (item instanceof AnalogMenuItem) {
                    AnalogMenuItem analog = (AnalogMenuItem) item;
                    if (newVal < 0 || newVal > analog.getMaxValue()) {
                        acknowledgeChange(item, ch.getValue(), ch.getCorrelationId(), AckStatus.VALUE_RANGE_WARNING);
                        return;
                    }
                } else if (item instanceof EnumMenuItem) {
                    EnumMenuItem en = (EnumMenuItem) item;
                    if (newVal < 0 || newVal >= en.getEnumEntries().size()) {
                        acknowledgeChange(item, ch.getValue(), ch.getCorrelationId(), AckStatus.VALUE_RANGE_WARNING);
                        return;
                    }
                }
                tree.changeItem(item, MenuItemHelper.stateForMenuItem(item, newVal, true, false));
                connectorListener.onCommand(this, new MenuChangeCommand(CorrelationId.EMPTY_CORRELATION, item.getId(), ChangeType.ABSOLUTE, Integer.toString(newVal)));
                acknowledgeChange(item, ch.getValue(), ch.getCorrelationId(), AckStatus.SUCCESS);
            } else if (ch.getChangeType() == ChangeType.ABSOLUTE) {
                if (item instanceof AnalogMenuItem) {
                    AnalogMenuItem analog = (AnalogMenuItem) item;
                    logger.log(DEBUG, "Analog absolute update on id " + item.getId());
                    var state = (MenuState<Integer>) tree.getMenuState(item);
                    tree.changeItem(item, MenuItemHelper.stateForMenuItem(state, item, ch.getValue(), true));
                    connectorListener.onCommand(this, new MenuChangeCommand(CorrelationId.EMPTY_CORRELATION, item.getId(), ChangeType.ABSOLUTE, ch.getValue()));
                    acknowledgeChange(item, ch.getValue(), ch.getCorrelationId(), AckStatus.SUCCESS);
                } else if (item instanceof BooleanMenuItem) {
                    logger.log(DEBUG, "Boolean change on id " + item.getId());
                    tree.changeItem(item, MenuItemHelper.stateForMenuItem(tree.getMenuState(item), item, ch.getValue(), true));
                    connectorListener.onCommand(this, new MenuChangeCommand(CorrelationId.EMPTY_CORRELATION, item.getId(), ChangeType.ABSOLUTE, ch.getValue()));
                    acknowledgeChange(item, ch.getValue(), ch.getCorrelationId(), AckStatus.SUCCESS);
                } else if (item instanceof EditableTextMenuItem) {
                    logger.log(DEBUG, "Text change on id " + item.getId());
                    tree.changeItem(item, MenuItemHelper.stateForMenuItem(tree.getMenuState(item), item, ch.getValue()));
                    connectorListener.onCommand(this, new MenuChangeCommand(CorrelationId.EMPTY_CORRELATION, item.getId(), ChangeType.ABSOLUTE, ch.getValue()));
                    acknowledgeChange(item, ch.getValue(), ch.getCorrelationId(), AckStatus.SUCCESS);
                } else if (item instanceof EditableLargeNumberMenuItem) {
                    logger.log(DEBUG, "Large number change on id " + item.getId());
                    tree.changeItem(item, MenuItemHelper.stateForMenuItem(tree.getMenuState(item), item, ch.getValue(), true));
                    connectorListener.onCommand(this, new MenuChangeCommand(CorrelationId.EMPTY_CORRELATION, item.getId(), ChangeType.ABSOLUTE, ch.getValue()));
                    acknowledgeChange(item, ch.getValue(), ch.getCorrelationId(), AckStatus.SUCCESS);
                } else if (item instanceof ActionMenuItem) {
                    logger.log(DEBUG, "Action event change on id " + item.getId());
                    acknowledgeChange(item, ch.getValue(), ch.getCorrelationId(), AckStatus.SUCCESS);
                    sendDialogAction(DialogMode.SHOW, item.getName(), "Action performed", MenuButtonType.NONE, MenuButtonType.OK, CorrelationId.EMPTY_CORRELATION);
                } else if (item instanceof ScrollChoiceMenuItem) {
                    logger.log(DEBUG, "Scroll choice event change on id " + item.getId());
                    var pos = new CurrentScrollPosition(ch.getValue());
                    var posToSend = new CurrentScrollPosition(pos.getPosition(), Integer.toString(pos.getPosition()));
                    connectorListener.onCommand(this, new MenuChangeCommand(CorrelationId.EMPTY_CORRELATION, item.getId(), ChangeType.ABSOLUTE, posToSend.toString()));
                    acknowledgeChange(item, posToSend.toString(), ch.getCorrelationId(), AckStatus.SUCCESS);
                } else if(item instanceof Rgb32MenuItem) {
                    logger.log(DEBUG, "Scroll choice event change on id " + item.getId());
                    var col = new PortableColor(ch.getValue());
                    connectorListener.onCommand(this, new MenuChangeCommand(CorrelationId.EMPTY_CORRELATION, item.getId(), ChangeType.ABSOLUTE, col.toString()));
                    acknowledgeChange(item, col.toString(), ch.getCorrelationId(), AckStatus.SUCCESS);
                }
            }
            else if(ch.getChangeType() == ChangeType.LIST_STATE_CHANGE) {
                Optional<ListResponse> resp = ListResponse.fromString(ch.getValue());
                var row = resp.orElseThrow().getRow();
                var responseType = resp.orElseThrow().getResponseType();
                sendDialogAction(DialogMode.SHOW, "List Activation", responseType + " on row " + row, MenuButtonType.NONE, MenuButtonType.CLOSE, CorrelationId.EMPTY_CORRELATION);
            }
        }, latencyMillis, TimeUnit.MILLISECONDS);
    }

    private void acknowledgeChange(MenuItem item, String value, CorrelationId correlation, AckStatus status) {
        if (recentUpdates.size() > 20) recentUpdates.remove(0);
        recentUpdates.add(String.format("%s %s - %s", item.getName(), value, status));
        connectorListener.onCommand(this, new MenuChangeCommand(
                CorrelationId.EMPTY_CORRELATION,
                simUpdateList.getId(),
                recentUpdates));
        connectorListener.onCommand(this, new MenuAcknowledgementCommand(correlation, status));
    }

    public void sendDialogAction(DialogMode mode, String title, String desc, MenuButtonType button1, MenuButtonType button2,
                                 CorrelationId correlation) {
        executor.schedule(() -> connectorListener.onCommand(this, new MenuDialogCommand(mode, title, desc, button1, button2, correlation))
                , latencyMillis, TimeUnit.MILLISECONDS);
    }

    private void sendCommandFor(MenuItem item) {
        logger.log(DEBUG, "Send " + item);

        var parId = tree.findParent(item).getId();

        item.accept(new MenuItemVisitor() {
            @Override
            public void visit(AnalogMenuItem item) {
                connectorListener.onCommand(SimulatedRemoteConnection.this, new MenuAnalogBootCommand(parId, item,
                        MenuItemHelper.getValueFor(item, tree, 0)));
            }

            @Override
            public void visit(BooleanMenuItem item) {
                connectorListener.onCommand(SimulatedRemoteConnection.this, new MenuBooleanBootCommand(parId, item,
                        MenuItemHelper.getValueFor(item, tree, false)));
            }

            @Override
            public void visit(EnumMenuItem item) {
                connectorListener.onCommand(SimulatedRemoteConnection.this, new MenuEnumBootCommand(parId, item,
                        MenuItemHelper.getValueFor(item, tree, 0)));
            }

            @Override
            public void visit(SubMenuItem item) {
                connectorListener.onCommand(SimulatedRemoteConnection.this, new MenuSubBootCommand(parId, item,
                        MenuItemHelper.getValueFor(item, tree, false)));
            }

            @Override
            public void visit(EditableTextMenuItem item) {
                connectorListener.onCommand(SimulatedRemoteConnection.this, new MenuTextBootCommand(parId, item,
                        MenuItemHelper.getValueFor(item, tree, "")));
            }

            @Override
            public void visit(FloatMenuItem item) {
                connectorListener.onCommand(SimulatedRemoteConnection.this, new MenuFloatBootCommand(parId, item,
                        MenuItemHelper.getValueFor(item, tree, 0.0F)));
            }

            @Override
            public void visit(ActionMenuItem item) {
                connectorListener.onCommand(SimulatedRemoteConnection.this, new MenuActionBootCommand(parId, item,
                        MenuItemHelper.getValueFor(item, tree, false)));
            }

            @Override
            public void visit(RuntimeListMenuItem item) {
                connectorListener.onCommand(SimulatedRemoteConnection.this, new MenuRuntimeListBootCommand(parId, item,
                        MenuItemHelper.getValueFor(item, tree, List.of())));
            }

            @Override
            public void visit(ScrollChoiceMenuItem item) {
                connectorListener.onCommand(SimulatedRemoteConnection.this, new MenuScrollChoiceBootCommand(parId, item,
                        MenuItemHelper.getValueFor(item, tree, new CurrentScrollPosition(0, ""))));
            }

            @Override
            public void visit(Rgb32MenuItem item) {
                connectorListener.onCommand(SimulatedRemoteConnection.this, new MenuRgb32BootCommand(parId, item,
                        MenuItemHelper.getValueFor(item, tree, ControlColor.BLACK)));
            }

            @Override
            public void visit(EditableLargeNumberMenuItem item) {
                connectorListener.onCommand(SimulatedRemoteConnection.this, new MenuLargeNumBootCommand(parId, item,
                        MenuItemHelper.getValueFor(item, tree, BigDecimal.ZERO)));
            }

            @Override
            public void visit(CustomBuilderMenuItem customItem) {
                // ignored
            }
        });
    }

    private void stateChanged(AuthStatus status) {
        authStatus = status;
        connectionChangeListener.connectionChange(this, status);
    }

    @Override
    public void start() {
        logger.log(DEBUG, "Start called");

        executor.schedule(() -> {
            try {
                stateChanged(AuthStatus.ESTABLISHED_CONNECTION);
                Thread.sleep(50);
                stateChanged(AuthStatus.AUTHENTICATED);
                Thread.sleep(50);
                stateChanged(AuthStatus.BOOTSTRAPPING);
                connectorListener.onCommand(this, new MenuBootstrapCommand(MenuBootstrapCommand.BootType.START));

                recurseSendingItems(ROOT);
                connectorListener.onCommand(this, new MenuBootstrapCommand(MenuBootstrapCommand.BootType.END));
                stateChanged(AuthStatus.CONNECTION_READY);
            } catch (InterruptedException e) {
                logger.log(ERROR, "Interrupted");
            }
        }, latencyMillis, TimeUnit.MILLISECONDS);
    }

    private void recurseSendingItems(SubMenuItem currentRoot) {
        var items = tree.getMenuItems(currentRoot);
        for (var item : items) {
            sendCommandFor(item);
            var subMenuItem = MenuItemHelper.asSubMenu(item);
            if (subMenuItem != null) {
                recurseSendingItems(subMenuItem);
            }
        }
    }

    @Override
    public void stop() {
        logger.log(DEBUG, "Stop called");
        stateChanged(AuthStatus.AWAITING_CONNECTION);
        stateChanged(AuthStatus.NOT_STARTED);
    }
}