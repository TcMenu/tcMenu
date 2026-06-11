import React from 'react';
import {PublishableCodePluginItem} from "./TcCodeGeneration";
import nodisplay from "../img/no-display.png";
import './SelectPluginDialog.css';

interface SelectPluginDialogProps {
    plugins: PublishableCodePluginItem[];
    onCancel: () => void;
    onSelect: (plugin: PublishableCodePluginItem) => void;
    subsystem: string;
    position?: { top: number, left: number };
}

export function SelectPluginDialog({plugins, onCancel, onSelect, subsystem, position}: SelectPluginDialogProps) {
    const dialogRef = React.useRef<HTMLDivElement>(null);
    const [adjustedStyle, setAdjustedStyle] = React.useState<React.CSSProperties>({});
    const [searchTerm, setSearchTerm] = React.useState("");
    const [showDeprecated, setShowDeprecated] = React.useState(false);

    const filteredPlugins = React.useMemo(() => {
        let result = plugins;
        if (!showDeprecated) {
            result = result.filter(p => !p.description.toUpperCase().startsWith("DEPRECATED"));
        }

        if (!searchTerm) return result;
        const term = searchTerm.toLowerCase();
        return result.filter(p => 
            p.description.toLowerCase().includes(term) || 
            p.extendedDescription.toLowerCase().includes(term) ||
            p.vendor.toLowerCase().includes(term) ||
            p.pluginGroup.toLowerCase().includes(term)
        );
    }, [plugins, searchTerm, showDeprecated]);

    React.useLayoutEffect(() => {
        if (position && dialogRef.current) {
            const rect = dialogRef.current.getBoundingClientRect();
            let { top, left } = position;

            // Check if it goes off-screen horizontally
            if (left + rect.width > window.innerWidth) {
                left = window.innerWidth - rect.width - 20;
            }
            if (left < 0) left = 10;

            // Check if it goes off-screen vertically
            if (top + rect.height > window.innerHeight) {
                top = window.innerHeight - rect.height - 20;
            }
            if (top < 0) top = 10;

            setAdjustedStyle({
                position: 'fixed',
                top: top,
                left: left,
                zIndex: 1000,
                margin: 0
            });
        } else if (!position) {
            setAdjustedStyle({});
        }
    }, [position]);

    return (
        <div className={position ? "plugin-select-floating" : "dialog-overlay"} onClick={onCancel}>
            <div ref={dialogRef} className="dialog-content plugin-select-dialog" style={adjustedStyle} onClick={e => e.stopPropagation()}>
                <div className="dialog-header-with-search">
                    <h2>Select {subsystem} Plugin</h2>
                    <div className="header-search-container">
                        <div className="deprecated-toggle">
                            <input
                                type="checkbox"
                                id="show-deprecated"
                                checked={showDeprecated}
                                onChange={(e) => setShowDeprecated(e.target.checked)}
                            />
                            <label htmlFor="show-deprecated">Show Deprecated</label>
                        </div>
                        <input
                            type="text"
                            placeholder="Search plugins..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="plugin-search-input"
                            autoFocus
                        />
                    </div>
                </div>
                <div className="plugin-list-container">
                    {filteredPlugins.length === 0 ? (
                        <p style={{padding: "20px"}}>No plugins found for this subsystem and platform.</p>
                    ) : (
                        filteredPlugins.map(plugin => (
                            <div key={plugin.id} className="plugin-select-item" onClick={() => onSelect(plugin)}>
                                <div className="plugin-select-image">
                                    <img src={plugin.imageFileName ? plugin.imageFileName : nodisplay} alt={plugin.description} />
                                </div>
                                <div className="plugin-select-info">
                                    <strong>{plugin.description}</strong>
                                    <p className="plugin-vendor">{plugin.vendor} - {plugin.pluginGroup}</p>
                                    <p className="plugin-description-text">{plugin.extendedDescription}</p>
                                </div>
                            </div>
                        ))
                    )}
                </div>
                <div className="dialog-actions">
                    <button className="secondary-button" onClick={onCancel}>Cancel</button>
                </div>
            </div>
        </div>
    );
}
