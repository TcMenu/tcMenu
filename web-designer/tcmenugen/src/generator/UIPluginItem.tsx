import React from 'react';
import {
    FontDescription,
    FontMode,
    PropertyDescription,
    PropertyValidationMode,
    PublishableCodePluginItem
} from "./TcCodeGeneration";
import {CreatorProperty, SubSystem} from "../domain/ProjectStruct";
import './UIPluginItem.css';
import nodisplay from "../img/no-display.png";
import {IntegerEditor} from "../menuedit/IntegerEditor";
import {FontDescriptionEditor} from "./FontDescriptionEditor";
import {useCurrentlyOpenProject} from "../App";
import {expanderFromString} from "./IoExpanderComponent";
import {
    ActionMenuItem,
    AnalogMenuItem,
    BooleanMenuItem,
    EnumMenuItem,
    FloatMenuItem,
    ListMenuItem,
    SubMenuItem
} from "../domain/MenuItem";

const EMPTY_DESCRIPTION = {
    validation: {
        mode: PropertyValidationMode.TEXT,
        min: 0, max: 255, choices: []
    },
    description: "Unknown property, please report to the plugin author",
} as PropertyDescription;

interface UIPluginItemProps {
    subsystem: SubSystem;
    boardId: string;
    plugin: PublishableCodePluginItem;
    allPlugins?: PublishableCodePluginItem[];
    supportedFonts?: FontMode[];
    onPropertyChange: (propertyName: string, newValue: string) => void;
    onSelectPlugin: (pluginId: string, event: React.MouseEvent) => void;
    onRemove?: () => void;
}

const UIPluginItem: React.FC<UIPluginItemProps> = ({ subsystem, boardId, plugin, allPlugins, onPropertyChange, supportedFonts, onSelectPlugin, onRemove}) => {
    const project = useCurrentlyOpenProject();
    const [expandedProps, setExpandedProps] = React.useState<Set<string>>(new Set());

    const toggleExpand = (propName: string) => {
        setExpandedProps(prev => {
            const next = new Set(prev);
            if (next.has(propName)) next.delete(propName);
            else next.add(propName);
            return next;
        });
    };

    const getFilteredMenuItems = (filter: string) => {
        if (!project) return [];
        const menuTree = project.menuTree;
        const allItems = menuTree.getAllItems();
        
        return allItems.filter(item => {
            if (item.getMenuId() === "0") return false; // Skip ROOT
            
            if (filter === "*" || filter === "") return true;
            if (filter === "sub") return item instanceof SubMenuItem;
            if (filter === "enum") return item instanceof EnumMenuItem;
            if (filter === "list") return item instanceof ListMenuItem;
            if (filter === "analog") return item instanceof AnalogMenuItem;
            if (filter === "action") return item instanceof ActionMenuItem;
            if (filter === "boolean") return item instanceof BooleanMenuItem;
            if (filter === "float") return item instanceof FloatMenuItem;
            
            return true;
        });
    };

    const isPropertyApplicable = (applicability: string | undefined): boolean => {
        if (!applicability || applicability === "always") return true;
        if (applicability === "never") return false;

        const parts = applicability.split(':');
        if (parts.length !== 3) return true;

        const [op, propName, targetValue] = parts;
        
        // Search for the property in the current plugin first, then in all plugins if available
        let targetProp = plugin.properties.find(p => p.name === propName);
        if (!targetProp && allPlugins) {
            for (const p of allPlugins) {
                targetProp = p.properties.find(pr => pr.name === propName);
                if (targetProp) break;
            }
        }
        
        if (!targetProp) return true;

        const currentValue = targetProp.latestValue;
        if (op === "eq") {
            return currentValue === targetValue;
        } else if (op === "ne") {
            return currentValue !== targetValue;
        }

        return true;
    };

    const renderPropertyEditor = (prop: CreatorProperty, propDesc: PropertyDescription, onChange: (n: string, v: string) => void) => {
        switch (propDesc.validation.mode) {
            case PropertyValidationMode.BOOLEAN:
                return (
                    <input
                        type="checkbox"
                        id={prop.name}
                        checked={prop.latestValue === "true"}
                        onChange={(e) => onChange(prop.name, e.target.checked ? "true" : "false")}
                    />
                );
            case PropertyValidationMode.INT_WITH_RANGE:
                return (
                    <IntegerEditor
                        id={prop.name}
                        min={propDesc.validation.min}
                        max={propDesc.validation.max}
                        initialValue={parseInt(prop.latestValue)}
                        onChange={(n) => onChange(prop.name, n.toString())}
                    />
                );
            case PropertyValidationMode.PIN:
            case PropertyValidationMode.OPTIONAL_PIN:
                const isPinVariable = /^[a-zA-Z_][a-zA-Z0-9_]*$/.test(prop.latestValue);
                const isPinNumber = /^-?\d+$/.test(prop.latestValue);
                let isPinValid = prop.latestValue !== "" && isPinVariable;
                if (!isPinValid && isPinNumber) {
                    const num = parseInt(prop.latestValue);
                    isPinValid = num >= -1 && num <= 32767;
                }
                return (
                    <input
                        type="text"
                        className={isPinValid ? "" : "invalid-input"}
                        id={prop.name}
                        value={prop.latestValue}
                        onChange={(e) => onChange(prop.name, e.target.value)}
                    />
                );
            case PropertyValidationMode.CHOICES:
                return (
                    <select
                        id={prop.name}
                        value={prop.latestValue}
                        onChange={(e) => onChange(prop.name, e.target.value)}
                    >
                        {propDesc.validation.choices.map(choice => (
                            <option key={choice.choiceValue} value={choice.choiceValue}>
                                {choice.choiceDesc}
                            </option>
                        ))}
                    </select>
                );
            case PropertyValidationMode.RGB:
                return (
                    <div className="rgb-property-editor">
                        <input
                            type="text"
                            value={prop.latestValue}
                            onChange={(e) => onChange(prop.name, e.target.value)}
                        />
                        <input
                            type="color"
                            id={prop.name}
                            value={prop.latestValue.startsWith("#") ? prop.latestValue : "#000000"}
                            onChange={(e) => onChange(prop.name, e.target.value)}
                        />
                    </div>
                );
            case PropertyValidationMode.VARIABLE:
                const isVarValid = prop.latestValue === "" || /^[a-zA-Z_][a-zA-Z0-9_]*$/.test(prop.latestValue);
                return (
                    <input
                        type="text"
                        className={isVarValid ? "" : "invalid-input"}
                        id={prop.name}
                        value={prop.latestValue}
                        onChange={(e) => onChange(prop.name, e.target.value)}
                    />
                );
            case PropertyValidationMode.MENUITEM:
                const items = getFilteredMenuItems(propDesc.validation.menuItemFilter || "*");
                // if the current value is not in the list, and is not empty, we might want to show it as an invalid option or just "No Item"
                // But the requirement says "it should be selected by default if available"
                return (
                    <select
                        id={prop.name}
                        className={prop.latestValue && !items.some(it => it.getMenuId() === prop.latestValue) ? "invalid-input" : ""}
                        value={prop.latestValue}
                        onChange={(e) => onChange(prop.name, e.target.value)}
                    >
                        <option value="">No Item</option>
                        {items.map(item => (
                            <option key={item.getMenuId()} value={item.getMenuId()}>
                                {item.getItemName()} [{item.getMenuId()}]
                            </option>
                        ))}
                    </select>
                );
            case PropertyValidationMode.IO_EXPANDER:
                const expanders = (project?.options.projectIoExpanders || []).map(expanderFromString);
                return (
                    <select
                        id={prop.name}
                        value={prop.latestValue || "devicePins"}
                        onChange={(e) => onChange(prop.name, e.target.value)}
                    >
                        <option value="devicePins">Internal</option>
                        {expanders.map(exp => (
                            <option key={exp.name} value={exp.name}>
                                {exp.name}
                            </option>
                        ))}
                    </select>
                );
            case PropertyValidationMode.FONT:
                return (
                    <FontDescriptionEditor
                        initialFont={FontDescription.fromProtocol(prop.latestValue)}
                        displaySupportedFonts={supportedFonts ?? [FontMode.DEFAULT_FONT, FontMode.ADAFRUIT_LOCAL, FontMode.ADAFRUIT]}
                        onSave={(font) => onChange(prop.name, font.toProtocol())}
                    />
                );
            case PropertyValidationMode.TEXT:
            default:
                return (
                    <input
                        type="text"
                        id={prop.name}
                        value={prop.latestValue}
                        onChange={(e) => onChange(prop.name, e.target.value)}
                    />
                );
        }
    }

    return (
        <div className="plugin-item-container">
            <div className="plugin-item-header">
                <strong>{plugin.description}</strong>
                {onRemove && <button type="button" className="dismiss-button" style={{float: 'right', padding: '2px 10px'}} onClick={onRemove}>Remove</button>}
            </div>
            <div className="plugin-item-body">
                <div className="plugin-item-left">
                    <div className="plugin-image-container" onClick={(e) => onSelectPlugin(plugin.id, e)}>
                        <img src={plugin.imageFileName ? plugin.imageFileName : nodisplay} alt={plugin.description} />
                        <div className="plugin-image-overlay">Change</div>
                    </div>
                </div>
                
                <div className="plugin-item-middle">
                    <p>{plugin.extendedDescription}</p>
                    <p>{plugin.pluginGroup} - {plugin.vendor}</p>
                    <div className="plugin-links">
                        <a href={plugin.docsLink} target="_blank" rel="noopener noreferrer">Click for documentation</a>
                        <p>{plugin.license}</p>
                        <a href="https://www.thecoderscorner.com" target="_blank" rel="noopener noreferrer">The Coders Corner</a>
                    </div>
                </div>

                <div className="plugin-item-right">
                    {plugin.properties.map((prop: CreatorProperty) => {
                        const propDesc = plugin.propertyDescriptions[prop.name] ?? EMPTY_DESCRIPTION;
                        if (!isPropertyApplicable(propDesc.applicability)) {
                            return null;
                        }
                        if (propDesc.validation.mode === PropertyValidationMode.SEPARATOR) {
                            return (
                                <div key={prop.name} className="plugin-property-separator">
                                    <hr />
                                    <strong>{propDesc.description}</strong>
                                </div>
                            );
                        }
                        const isBoolean = propDesc.validation.mode === PropertyValidationMode.BOOLEAN;
                        return (
                            <div key={prop.name} className={isBoolean ? "plugin-property-editor-checkbox-wrap" : "plugin-property-editor"}>
                                <div className={isBoolean ? "plugin-property-editor-checkbox" : "plugin-property-label-row"}>
                                    <label htmlFor={prop.name}>{propDesc.description}</label>
                                    {isBoolean && renderPropertyEditor(prop, propDesc, onPropertyChange)}
                                    {propDesc.extendedDescription && (
                                        <button 
                                            type="button" 
                                            className="extra-info-button" 
                                            onClick={() => toggleExpand(prop.name)}
                                            title="Toggle extra information"
                                        >
                                            {expandedProps.has(prop.name) ? "Hide Info" : "Extra Info"}
                                        </button>
                                    )}
                                </div>
                                {!isBoolean && renderPropertyEditor(prop, propDesc, onPropertyChange)}
                                {expandedProps.has(prop.name) && propDesc.extendedDescription && (
                                    <div className="property-extended-description">
                                        {propDesc.extendedDescription}
                                    </div>
                                )}
                            </div>
                        );
                    })}
                </div>
            </div>
        </div>
    );
};

export default UIPluginItem;
