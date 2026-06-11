 import {useCurrentlyOpenProject} from "../App";
import React, {useEffect, useState} from "react";
import {ALL_PLATFORMS} from "../domain/Platforms";
import {AuthenticationDefinition, EepromDefinition} from "./EepromAndAuthSupport";
import {ChooseEepromDialog} from "./ChooseEepromDialog";
import {ChooseAuthenticatorDialog} from "./ChooseAuthenticatorDialog";
import {openModeDesc} from "../menuedit/TcMenuEditor";
import UIPluginItem from "./UIPluginItem";
import {GeneratorLogView} from "./GeneratorLogView";
import {
    FontMode,
    GeneratedFile,
    GenerationResponse,
    getDefaultPlugin,
    getPluginsByIds,
    PublishableCodePluginItem,
    runGenerateCode,
    searchPlugins,
    ThemeMode,
    userNeedsChooseDisplay,
    userNeedsChooseInput,
    userNeedsChooseTheme,
    NO_REMOTE_ID
} from "./TcCodeGeneration";
import {CreatorProperty, SubSystem} from "../domain/ProjectStruct";
import {SelectPluginDialog} from "./SelectPluginDialog";

export function GenerateCodeView() {
    const project = useCurrentlyOpenProject();
    const [isEepromDialogOpen, setIsEepromDialogOpen] = useState(false);
    const [isAuthDialogOpen, setIsAuthDialogOpen] = useState(false);
    const [isSelectPluginDialogOpen, setIsSelectPluginDialogOpen] = useState(false);
    const [plugins, setPlugins] = useState<PublishableCodePluginItem[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [searchResults, setSearchResults] = useState<PublishableCodePluginItem[]>([]);
    const [selectingSubsystem, setSelectingSubsystem] = useState<SubSystem | null>(null);
    const [selectingIndex, setSelectingIndex] = useState<number | null>(null);
    const [dialogPosition, setDialogPosition] = useState({ top: 0, left: 0 });
    const [generationResponse, setGenerationResponse] = useState<GenerationResponse | null>(null);
    const [, setHash] = useState(0);

    const getPluginIdsToFetch = () => {
        if (!project) return [];
        const ids = new Set<string>();
        if (project.options.lastInputUuid) ids.add(project.options.lastInputUuid);
        if (project.options.lastDisplayUuid) ids.add(project.options.lastDisplayUuid);
        if (project.options.lastThemeUuid) ids.add(project.options.lastThemeUuid);
        if (project.options.lastRemoteUuids) {
            project.options.lastRemoteUuids.forEach(id => {
                if (id) ids.add(id);
            });
        }

        return Array.from(ids).filter(id => id !== userNeedsChooseInput.id && id !== userNeedsChooseDisplay.id && id !== userNeedsChooseTheme.id);
    };

    const prepopulatePluginProperties = (fp: PublishableCodePluginItem): PublishableCodePluginItem => {
        if (!project) return fp;
        let modified = false;
        const newProperties = fp.properties.map(prop => {
            const existingIdx = project.options.lastProperties.findIndex(p => p.name === prop.name && p.subsystem === prop.subsystem);
            if (existingIdx !== -1) {
                const existingProp = project.options.lastProperties[existingIdx];
                if (existingProp.latestValue !== "" && existingProp.latestValue !== prop.latestValue) {
                    return {...prop, latestValue: existingProp.latestValue};
                }
            } else {
                // Property is missing from project options, so we must add it.
                project.options.lastProperties.push({
                    name: prop.name,
                    latestValue: prop.latestValue,
                    subsystem: prop.subsystem
                });
                modified = true;
            }
            return prop;
        });

        if (modified) {
            import("../App").then(m => m.saveProjectToLocalStorage(project));
        }

        return {
            ...fp,
            properties: newProperties
        };
    };

    const mergePluginsIntoState = (fetchedPlugins: PublishableCodePluginItem[]) => {
        setPlugins(prev => {
            let newPlugins = [...prev];
            fetchedPlugins.forEach(fp => {
                const updatedFp = prepopulatePluginProperties(fp);
                newPlugins = updateOnePluginInState(newPlugins, updatedFp);
            });
            return newPlugins;
        });
    };

    const updateOnePluginInState = (currentPlugins: PublishableCodePluginItem[], updatedPlugin: PublishableCodePluginItem) => {
        const newPlugins = [...currentPlugins];
        const idx = newPlugins.findIndex(p => p.id === updatedPlugin.id && p.subsystem === updatedPlugin.subsystem);
        if (idx !== -1) {
            newPlugins[idx] = updatedPlugin;
        } else {
            newPlugins.push(updatedPlugin);
        }
        return newPlugins;
    }

    useEffect(() => {
        const idsToFetch = getPluginIdsToFetch();
        if (idsToFetch.length > 0) {
            setLoading(true);
            getPluginsByIds(idsToFetch)
                .then(fetchedPlugins => {
                    mergePluginsIntoState(fetchedPlugins);
                    setHash(h => h + 1);
                })
                .catch(err => setError("Failed to load plugins from server " +  err.messages))
                .finally(() => setLoading(false));
        } else {
            setPlugins([]); // Clear if nothing to fetch
        }
    }, [project]);

    if(error) return (
        <div className="wide-project-container">
            <h1>Generate Code encountered an error</h1>
            <p className="error-indicator">{error}</p>
            <p>It is possible there's an issue with the server at the moment, please try again shortly.</p>
        </div>
    )

    if (project === null) return (
        <div className="wide-project-container">
            <h1>No project open</h1>
            <p>To use code generator open a project first.</p>
        </div>
    );

    if (loading && plugins.length === 0) return (
        <div className="wide-project-container">
            <h1>Generate Code</h1>
            <div className="loading-indicator">Loading project plugins...</div>
        </div>
    );

    if(!project.options.lastInputUuid) project.options.lastInputUuid = userNeedsChooseInput.id; 
    if(!project.options.lastDisplayUuid) project.options.lastDisplayUuid = userNeedsChooseDisplay.id; 
    if(!project.options.lastThemeUuid) project.options.lastThemeUuid = userNeedsChooseTheme.id;
    if(!project.options.lastRemoteUuids || project.options.lastRemoteUuids.length === 0) {
        project.options.lastRemoteUuids = [NO_REMOTE_ID];
    }
    
    // Create actual state for the currently displayed plugins, defaulting to the placeholder values.
    const displayPlugin = plugins.find(p => p.subsystem === SubSystem.DISPLAY && p.id === project.options.lastDisplayUuid) || userNeedsChooseDisplay;
    const inputPlugin = plugins.find(p => p.subsystem === SubSystem.INPUT && p.id === project.options.lastInputUuid) || userNeedsChooseInput;
    const themePlugin = plugins.find(p => p.subsystem === SubSystem.THEME && p.id === project.options.lastThemeUuid) || userNeedsChooseTheme;

    // Ensure that for every displayed plugin, all its properties are in the project options
    const allDisplayedPlugins = [displayPlugin, inputPlugin, themePlugin];
    project.options.lastRemoteUuids.forEach(uuid => {
        const p = plugins.find(p => p.subsystem === SubSystem.REMOTE && p.id === uuid);
        if (p) allDisplayedPlugins.push(p);
    });

    let modifiedInRender = false;
    allDisplayedPlugins.forEach(p => {
        if (p.id !== userNeedsChooseInput.id && p.id !== userNeedsChooseDisplay.id && p.id !== userNeedsChooseTheme.id && p.id !== NO_REMOTE_ID) {
            p.properties.forEach(prop => {
                const existingIdx = project.options.lastProperties.findIndex(lp => lp.name === prop.name && lp.subsystem === prop.subsystem);
                if (existingIdx === -1) {
                    project.options.lastProperties.push({
                        name: prop.name,
                        latestValue: prop.latestValue,
                        subsystem: prop.subsystem
                    });
                    modifiedInRender = true;
                }
            });
        }
    });
    if (modifiedInRender) {
        import("../App").then(m => m.saveProjectToLocalStorage(project));
    }

    const isThemeRequired = (displayPlugin.themeDescription?.themeMode ?? ThemeMode.NONE) !== ThemeMode.NONE;

    const onPlatformChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        project.options.embeddedPlatform = e.target.value;
        setHash(h => h + 1);
    }

    const eepromMode = EepromDefinition.readFromProject(project.options.eepromDefinition);
    const authMode = AuthenticationDefinition.readFromProject(project.options.authenticatorDefinition);

    const onEepromSave = (newEeprom: EepromDefinition) => {
        project.options.eepromDefinition = newEeprom.stringDefinition();
        setIsEepromDialogOpen(false);
        setHash(h => h + 1);
    }

    const onAuthSave = (newAuth: AuthenticationDefinition) => {
        project.options.authenticatorDefinition = newAuth.stringDefinition();
        setIsAuthDialogOpen(false);
        setHash(h => h + 1);
    }

    const onPropertyChange = (pluginId: string, propertyName: string, newValue: string, subsystem: SubSystem) => {
        setPlugins(prevPlugins => prevPlugins.map(p => {
            if (p.id === pluginId && p.subsystem === subsystem) {
                const updatedProperties = p.properties.map(prop =>
                    prop.name === propertyName ? {...prop, latestValue: newValue} : prop
                );

                // Also update project options so it's persisted
                if (project) {
                    const existingIdx = project.options.lastProperties.findIndex(lp => lp.name === propertyName && lp.subsystem === p.subsystem);
                    if (existingIdx !== -1) {
                        project.options.lastProperties[existingIdx].latestValue = newValue;
                    } else {
                        project.options.lastProperties.push({
                            name: propertyName,
                            latestValue: newValue,
                            subsystem: p.subsystem
                        });
                    }
                    // Trigger auto-save as we might have modified nested property or pushed to array
                    // which Proxy might not catch depending on how it's used.
                    import("../App").then(m => m.saveProjectToLocalStorage(project));
                }

                return {
                    ...p,
                    properties: updatedProperties
                };
            }
            return p;
        }));
    };

    type PluginPredicateCallback = (plugin: PublishableCodePluginItem) => boolean;

    const onSelectPlugin = (pluginId: string, subsystem: SubSystem, event: React.MouseEvent, index: number | null = null, predicateExtra?: PluginPredicateCallback) => {
        if (!project) return;

        const rect = (event.currentTarget as HTMLElement).getBoundingClientRect();
        setDialogPosition({
            top: rect.top,
            left: rect.left
        });

        setSelectingSubsystem(subsystem);
        setSelectingIndex(index);
        setLoading(true);
        const currentPlatform = ALL_PLATFORMS.find(p => p.boardId === project.options.embeddedPlatform) || ALL_PLATFORMS[0];
        searchPlugins("*", subsystem, currentPlatform)
            .then(results => {
                results = results.filter(p => predicateExtra?.(p) ?? true);
                setSearchResults(results);
                setIsSelectPluginDialogOpen(true);
            })
            .catch(err => setError("Failed to load plugins from server " +  err.messages))
            .finally(() => setLoading(false));
    };

    const onPluginSelected = (plugin: PublishableCodePluginItem) => {
        if (!project || !selectingSubsystem) return;

        // Update project options and ensure the ID is not duplicated in other slots where it doesn't belong
        if (selectingSubsystem === SubSystem.INPUT) {
            project.options.lastInputUuid = plugin.id;
        } else if (selectingSubsystem === SubSystem.DISPLAY) {
            project.options.lastDisplayUuid = plugin.id;
        } else if (selectingSubsystem === SubSystem.THEME) {
            project.options.lastThemeUuid = plugin.id;
        } else if (selectingSubsystem === SubSystem.REMOTE && selectingIndex !== null) {
            project.options.lastRemoteUuids[selectingIndex] = plugin.id;
        }

        // Safety check: remove this ID from lastRemoteUuids if it's now the main display/input/theme,
        // unless it's genuinely intended to be both (unlikely for these subsystems).
        if (selectingSubsystem === SubSystem.DISPLAY || selectingSubsystem === SubSystem.INPUT || selectingSubsystem === SubSystem.THEME) {
            const rIdx = project.options.lastRemoteUuids.indexOf(plugin.id);
            if (rIdx !== -1) {
                if (project.options.lastRemoteUuids.length > 1) {
                    project.options.lastRemoteUuids.splice(rIdx, 1);
                } else {
                    project.options.lastRemoteUuids[0] = NO_REMOTE_ID;
                }
            }
        }

        // Prepopulate properties from project options if they exist
        const updatedPlugin = prepopulatePluginProperties(plugin);

        // Add plugin to state if not there, or replace
        setPlugins(prev => updateOnePluginInState(prev, updatedPlugin));

        setIsSelectPluginDialogOpen(false);
        setSelectingSubsystem(null);
        setSelectingIndex(null);
        setHash(h => h + 1);
    };

    const onAddRemote = (event: React.MouseEvent) => {
        if (!project) return;
        project.options.lastRemoteUuids.push(NO_REMOTE_ID);
        setHash(h => h + 1);
    }

    const onRemoveRemote = (index: number) => {
        if (!project) return;
        if (project.options.lastRemoteUuids.length <= 1) {
            project.options.lastRemoteUuids = [NO_REMOTE_ID];
        } else {
            project.options.lastRemoteUuids.splice(index, 1);
        }
        setHash(h => h + 1);
    }

    function requiredFilesForBuild(): GeneratedFile[] {
        if(!project) return [];

        return [];
    }

    function onCodeGenerate() {
        if(!project) {
            console.error("Project not loaded");
            return;
        }

        setGenerationResponse(null);

        if (inputPlugin.id === userNeedsChooseInput.id) {
            alert("Please select an input plugin before generating code.");
            return;
        }
        if (displayPlugin.id === userNeedsChooseDisplay.id) {
            alert("Please select a display plugin before generating code.");
            return;
        }
        if (isThemeRequired && themePlugin.id === userNeedsChooseTheme.id) {
            alert("The selected display plugin requires a theme. Please select a theme plugin.");
            return;
        }

        if (isThemeRequired && displayPlugin.themeDescription.themeMode !== ThemeMode.ANY &&
            themePlugin.themeDescription.themeMode !== displayPlugin.themeDescription.themeMode) {
            alert(`The selected theme is not compatible with the display. The display requires a ${ThemeMode[displayPlugin.themeDescription.themeMode]} theme.`);
            return;
        }

        console.log("Validation passed, proceeding with code generation...");
        setLoading(true);

        if(!project) return;

        runGenerateCode(project, requiredFilesForBuild()).then(response => {
            console.log("Success:", response.successful);
            console.log("Generated files:", response.generatedFiles ? response.generatedFiles.length : 0);
            if (response.generatedFiles) {
                for(const f of response.generatedFiles) {
                    console.log(f.fileName);
                }
            }
            setGenerationResponse(response);
        }).catch(err => {
            console.error("Generation failed", err);
            setGenerationResponse({
                successful: false,
                buildId: "N/A",
                logLines: [{log: "Failed to communicate with generator: " + err.message, level: "ERROR"}],
                generatedFiles: []
            });
        }).finally(() => setLoading(false));
    }

    const ALL_FONTS = [ FontMode.DEFAULT_FONT, FontMode.ADAFRUIT, FontMode.ADAFRUIT_LOCAL,
        FontMode.NUMBERED, FontMode.U8G2, FontMode.TCUNICODE, FontMode.TCUNICODE_LOCAL
    ];
    const ADAFRUIT_FONTS = [ FontMode.DEFAULT_FONT, FontMode.ADAFRUIT, FontMode.ADAFRUIT_LOCAL ];
    const TCUNICODE_FONTS = [ FontMode.ADAFRUIT, FontMode.ADAFRUIT_LOCAL, FontMode.TCUNICODE, FontMode.TCUNICODE_LOCAL ];
    const U8G2_FONTS = [ FontMode.DEFAULT_FONT, FontMode.U8G2 ];

    function supportedFontsFromPlugin(displayPlugin: PublishableCodePluginItem, tcUnicodeOn: boolean): FontMode[] {
        const themeReported = displayPlugin?.themeDescription?.fontMode ?? ThemeMode.NONE;
        if(tcUnicodeOn) {
            return TCUNICODE_FONTS;
        } else if(themeReported === FontMode.ADAFRUIT || themeReported === FontMode.ADAFRUIT_LOCAL) {
            return ADAFRUIT_FONTS;
        } else if(themeReported === FontMode.U8G2) {
            return U8G2_FONTS;
        }
        return ALL_FONTS;
    }

    function getPropBool(properties: CreatorProperty[], propName: string) {
        const p = properties.find(p => p.name === propName);
        return p?.latestValue === "true";
    }

    if (generationResponse) {
        return <GeneratorLogView response={generationResponse} onDismiss={() => setGenerationResponse(null)} />;
    }

    return (
        <div className="wide-project-container">
            {isSelectPluginDialogOpen && (
                <SelectPluginDialog
                    plugins={searchResults}
                    subsystem={selectingSubsystem || ""}
                    onCancel={() => setIsSelectPluginDialogOpen(false)}
                    onSelect={onPluginSelected}
                    position={dialogPosition}
                />
            )}
            <h1>Generate Code <span
                style={{fontSize: "60%"}}> for {project.options.applicationName} - {openModeDesc(project.roundTripMode)}</span>
            </h1>
            <div className="form-group">
                <label htmlFor="identificationOfApp">Application UUID</label>
                <input type="text" readOnly value={project.options.applicationUUID}/>
            </div>
            <div className="form-group">
                <label htmlFor="embeddedPlatform">Embedded Platform</label>
                <select id="embeddedPlatform" value={project.options.embeddedPlatform} onChange={onPlatformChange}>
                    {ALL_PLATFORMS.map(p => (
                        <option key={p.boardId} value={p.boardId}>{p.friendlyName}</option>
                    ))}
                </select>
            </div>

            {loading && <div className="loading-indicator">Loading plugins...</div>}

            <div className="form-group">
                <label>EEPROM Support</label>
                <div className="input-with-button">
                    <input type="text" value={eepromMode.toString()} readOnly/>
                    <button type="button" onClick={() => setIsEepromDialogOpen(true)}>Change</button>
                </div>
            </div>
            {isEepromDialogOpen && (
                <ChooseEepromDialog
                    initialEeprom={eepromMode}
                    onCancel={() => setIsEepromDialogOpen(false)}
                    onSave={onEepromSave}
                />
            )}

            <div className="form-group">
                <label>Authenticator Support</label>
                <div className="input-with-button">
                    <input type="text" value={authMode.toString()} readOnly/>
                    <button type="button" onClick={() => setIsAuthDialogOpen(true)}>Change</button>
                </div>
            </div>
            {isAuthDialogOpen && (
                <ChooseAuthenticatorDialog
                    initialAuth={authMode}
                    onCancel={() => setIsAuthDialogOpen(false)}
                    onSave={onAuthSave}
                />
            )}

            <h3 className="subsystem-header">Choose your Input facilities</h3>
            <UIPluginItem
                subsystem={SubSystem.INPUT} boardId={project.options.embeddedPlatform}
                plugin={inputPlugin}
                allPlugins={plugins}
                onPropertyChange={(name, val) => onPropertyChange(inputPlugin.id, name, val, SubSystem.INPUT)}
                onSelectPlugin={(id, e) => onSelectPlugin(id, SubSystem.INPUT, e)}
            />

            <h3 className="subsystem-header">Select a display plugin</h3>
            <UIPluginItem
                subsystem={SubSystem.DISPLAY} boardId={project.options.embeddedPlatform}
                plugin={displayPlugin}
                allPlugins={plugins}
                onPropertyChange={(name, val) => onPropertyChange(displayPlugin.id, name, val, SubSystem.DISPLAY)}
                onSelectPlugin={(id, e) => onSelectPlugin(id, SubSystem.DISPLAY, e)}
            />

            <h3 className="subsystem-header">Remote capabilities
                <button type="button" className="dismiss-button" style={{marginLeft: '20px', padding: '2px 10px'}} onClick={onAddRemote}>Add another</button>
            </h3>
            {project.options.lastRemoteUuids.map((uuid, index) => {
                const remotePlugin = plugins.find(p => p.subsystem === SubSystem.REMOTE && p.id === uuid) || getDefaultPlugin(SubSystem.REMOTE);
                return <UIPluginItem
                    key={index}
                    subsystem={SubSystem.REMOTE} boardId={project.options.embeddedPlatform}
                    plugin={remotePlugin}
                    allPlugins={plugins}
                    onPropertyChange={(name, val) => onPropertyChange(remotePlugin.id, name, val, SubSystem.REMOTE)}
                    onSelectPlugin={(id, e) => onSelectPlugin(id, SubSystem.REMOTE, e, index)}
                    onRemove={() => onRemoveRemote(index)}
                />
            })}

            {displayPlugin?.themeDescription?.themeMode === ThemeMode.NONE ?
                <p>No theme needed</p> :
                <>
                    <h3 className="subsystem-header">Choose a theme <span style={{fontSize: "75%"}}>(display needs {ThemeMode[displayPlugin.themeDescription.themeMode]} with tcUnicode or {FontMode[displayPlugin.themeDescription.fontMode]} fonts)</span></h3>
                    <UIPluginItem
                        subsystem={SubSystem.THEME} boardId={project.options.embeddedPlatform}
                        plugin={themePlugin}
                        allPlugins={plugins}
                        supportedFonts={supportedFontsFromPlugin(displayPlugin, getPropBool(themePlugin.properties, "USE_TC_UNICODE"))}
                        onPropertyChange={(name, val) => onPropertyChange(themePlugin.id, name, val, SubSystem.THEME)}
                        onSelectPlugin={(id, e) => onSelectPlugin(id, SubSystem.THEME, e, null,
                                        plugin => displayPlugin.themeDescription?.themeMode === ThemeMode.ANY || plugin.themeDescription?.themeMode === displayPlugin.themeDescription?.themeMode)}
                    />
                </>
            }
            <div style={{marginTop: "20px"}}>
                <button type="button" className="generate-button" onClick={onCodeGenerate}>Generate Code</button>
            </div>
        </div>
    );
}