package com.thecoderscorner.embedcontrol.jfxapp;

import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.ManualLanConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.Rs232ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.SimulatorConnectionCreator;
import com.thecoderscorner.embedcontrol.core.rs232.Rs232SerialFactory;
import com.thecoderscorner.embedcontrol.core.serial.PlatformSerialFactory;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.util.StringHelper;
import com.thecoderscorner.embedcontrol.customization.ScreenLayoutPersistence;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;
import com.thecoderscorner.menu.persist.XMLDOMHelper;
import org.w3c.dom.Element;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

import static java.lang.System.Logger.Level.INFO;

public class RemoteAppScreenLayoutPersistence extends ScreenLayoutPersistence {
    private final PlatformSerialFactory serialFactory;
    private final ScheduledExecutorService executorService;
    private ConnectionCreator connectionCreator;
    private String panelName;

    public RemoteAppScreenLayoutPersistence(MenuTree tree, GlobalSettings settings, UUID appUuid, Path path, int defFontSize,
                                            PlatformSerialFactory serialFactory, ScheduledExecutorService executor) {
        super(tree, settings, appUuid, path, defFontSize);
        this.executorService = executor;
        this.serialFactory = serialFactory;
    }

    public RemoteAppScreenLayoutPersistence(MenuTree tree, GlobalSettings settings, UUID appUuid, Path appDataDir, int defFontSize, Rs232SerialFactory serialFactory, ScheduledExecutorService coreExecutor, ConnectionCreator connectionCreator) {
        this(tree, settings, appUuid, appDataDir, defFontSize, serialFactory, coreExecutor);
        this.connectionCreator = connectionCreator;
    }

    public void loadApplicationSpecific(Element rootElement) {
        var ecRemote = XMLDOMHelper.elementWithName(rootElement, "EcRemote");
        if (ecRemote == null) return;
        remoteUuid = UUID.fromString(XMLDOMHelper.getAttributeOrDefault(ecRemote, "uuid", UUID.randomUUID()));
        panelName = XMLDOMHelper.getAttributeOrDefault(ecRemote, "name", "Unknown");
        var creatorType = XMLDOMHelper.getAttributeOrDefault(ecRemote, "conType", ManualLanConnectionCreator.MANUAL_LAN_JSON_TYPE);

        connectionCreator = switch (creatorType) {
            case Rs232ConnectionCreator.MANUAL_RS232_CREATOR_TYPE -> makeRs232Connection(ecRemote);
            case ManualLanConnectionCreator.MANUAL_LAN_JSON_TYPE -> makeManualLanConnection(ecRemote);
            case SimulatorConnectionCreator.SIMULATED_CREATOR_TYPE -> makeSimulatorConnection(ecRemote);
            default -> throw new UnsupportedOperationException("No such creator as " + creatorType);
        };

        logger.log(INFO, "Loaded panel UUID='" + remoteUuid + "' type='" + creatorType);

    }

    private ConnectionCreator makeSimulatorConnection(Element ecRemote) {
        var data = XMLDOMHelper.elementWithName(ecRemote, "simData");
        String json = null;
        if(data != null) {
            json = data.getTextContent();
        }
        return new SimulatorConnectionCreator(json, ecRemote.getAttribute("name"), remoteUuid, executorService,
                new JsonMenuItemSerializer());
    }

    private void writeSimulatorConnection(Element ecRemote, SimulatorConnectionCreator scc) {
        ecRemote.setAttribute("name", scc.getName());
        ecRemote.setAttribute("conType", SimulatorConnectionCreator.SIMULATED_CREATOR_TYPE);
        if(!StringHelper.isStringEmptyOrNull(scc.getJsonForTree())) {
            var child = XMLDOMHelper.appendElementWithNameValue(ecRemote, "simData", null);
            var cdata = ecRemote.getOwnerDocument().createCDATASection(scc.getJsonForTree());
            child.appendChild(cdata);
        }
    }

    private ConnectionCreator makeManualLanConnection(Element ecRemote) {
        return new ManualLanConnectionCreator(globalSettings, executorService, ecRemote.getAttribute("name"),
                ecRemote.getAttribute("host"),
                Integer.parseInt(XMLDOMHelper.getAttributeOrDefault(ecRemote, "port", 3333)));
    }

    private void writeManualLanConnection(Element ecRemote, ManualLanConnectionCreator lan) {
        ecRemote.setAttribute("name", lan.getName());
        ecRemote.setAttribute("conType", ManualLanConnectionCreator.MANUAL_LAN_JSON_TYPE);
        ecRemote.setAttribute("host", lan.getIpAddr());
        ecRemote.setAttribute("port", String.valueOf(lan.getPort()));
    }

    private ConnectionCreator makeRs232Connection(Element ecRemote) {
        return new Rs232ConnectionCreator(serialFactory,
                ecRemote.getAttribute("name"), ecRemote.getAttribute("port"),
                Integer.parseInt(XMLDOMHelper.getAttributeOrDefault(ecRemote, "baud", 9600)));
    }

    private void writeRs232Connection(Element ecRemote, Rs232ConnectionCreator rs232) {
        ecRemote.setAttribute("name", rs232.getName());
        ecRemote.setAttribute("conType", Rs232ConnectionCreator.MANUAL_RS232_CREATOR_TYPE);
        ecRemote.setAttribute("port", rs232.getPortId());
        ecRemote.setAttribute("baud", String.valueOf(rs232.getBaudRate()));
    }

    public void saveApplicationSpecific(Element rootElement) {
        var ecRemote = XMLDOMHelper.appendElementWithNameValue(rootElement, "EcRemote", null);
        ecRemote.setAttribute("uuid", remoteUuid.toString());
        ecRemote.setAttribute( "name", panelName);
        if(connectionCreator instanceof SimulatorConnectionCreator sim) {
            writeSimulatorConnection(ecRemote, sim);
        } else if(connectionCreator instanceof Rs232ConnectionCreator rs232) {
            writeRs232Connection(ecRemote, rs232);
        } else if(connectionCreator instanceof ManualLanConnectionCreator lan) {
            writeManualLanConnection(ecRemote, lan);
        } else {
            throw new UnsupportedOperationException("Unknown creator type ");
        }

    }

    public UUID getRemoteUuid() {
        return remoteUuid;
    }

    public ConnectionCreator getConnectionCreator() {
        return connectionCreator;
    }

    public String getPanelName() {
        return panelName;
    }

    public ScheduledExecutorService getExecutorService() {
        return executorService;
    }
}
