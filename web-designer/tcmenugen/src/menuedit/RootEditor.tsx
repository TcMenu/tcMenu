import {embeddedPlatformFromId} from "../domain/Platforms";
import {generateUUID} from "../util/uuid";
import {EepromSaveMode, MenuTreeWithCodeOptions} from "../domain/ProjectStruct";
import {GeneratorOptionsEditor} from "./GeneratorOptionsEditor";
import {
    getInternationalization,
    InternationalizationMode,
    NO_INTERNATIONALIZATION,
    resetI18n
} from "../generator/I18nImpls";
import {useEffect, useState} from "react";

export function RootEditor({project, onHashChange}: {project: MenuTreeWithCodeOptions, onHashChange: () => void}) {
    const [counter, setCounter] = useState(0);
    const [i18n, setI18n] = useState(NO_INTERNATIONALIZATION);
    useEffect(() => {
        let isMounted = true;
        getInternationalization().then((intnl) => {
            if (isMounted) setI18n(intnl);
        });
        return () => { isMounted = false; };
    }, [project, counter]);
    return (
        <div className="root-editor">
            <div className="header-with-buttons">
                <h2>Project Settings</h2>
                <div className="buttons">
                    <button style={{backgroundColor: "#2bdc46"}} type="button" onClick={() => {}}>I18N Help</button>
                    {i18n.mode === InternationalizationMode.NONE && (
                        <button type="button" onClick={() => {}}>Enable Internationalisation</button>
                    )}
                    {i18n.mode === InternationalizationMode.I18N_DIR_PROPS && (
                        <button type="button" onClick={() => {i18n.reload().then(() => setCounter(c => c + 1));}}>Refresh Properties</button>
                    )}
                    {i18n.mode === InternationalizationMode.CREATE_ON_GEN && (
                        <button type="button" onClick={() => {resetI18n(); setCounter(c => c + 1);}}>Disable Internationalisation</button>
                    )}
                    {i18n.mode === InternationalizationMode.NOT_INIT && (<span>Initializing i18n...</span>)}
                </div>
            </div>

            <div className="form-group">
                <label htmlFor="applicationName">Project Name</label>
                <input
                    type="text"
                    id="applicationName"
                    value={project.options.applicationName}
                    onChange={(e) => {
                        project.options.applicationName = e.target.value;
                        onHashChange();
                    }}
                />
            </div>

            <div className="form-group">
                <label htmlFor="description">Description</label>
                <textarea
                    id="description"
                    value={project.description}
                    rows={3}
                    onChange={(e) => {
                        project.description = e.target.value;
                        onHashChange();
                    }}
                />
            </div>

            <div className="form-group">
                <label>Unique ID</label>
                <div className="input-with-button">
                    <input type="text" value={project.options.applicationUUID} readOnly disabled />
                    <button type="button" onClick={() => {
                        project.options.applicationUUID = generateUUID();
                        onHashChange();
                    }}>New ID</button>
                </div>
            </div>

            <div className="form-group">
                <label>Embedded Platform</label>
                <input type="text" value={embeddedPlatformFromId(project.options.embeddedPlatform).friendlyName} readOnly disabled />
            </div>

            <GeneratorOptionsEditor
                saveLocation={project.options.saveLocation}
                eepromSaveMode={project.options.eepromSaveMode ?? EepromSaveMode.DYNAMIC_WRITE_BY_ID}
                menuBuilderOn={project.options?.useDynamicMenus ?? false}
                onMenuBuilderChange={(ch) => {
                    project.options.useDynamicMenus = ch;
                    onHashChange();
                }}
                onSaveLocationChange={(loc) => {
                    project.options.saveLocation = loc;
                    onHashChange();
                }}
                onEepromSaveModeChange={(mode) => {
                    project.options.eepromSaveMode = mode;
                    onHashChange();
                }}
            />

            <div className="form-group-checkbox">
                <input
                    type="checkbox"
                    id="namingRecursive"
                    checked={project.options.namingRecursive}
                    onChange={() => {
                        project.options.namingRecursive = !project.options.namingRecursive;
                        onHashChange();
                    }}
                />
                <label htmlFor="namingRecursive">Recursive Naming (Include submenu name in item names)</label>
            </div>

            <div className="form-group-checkbox">
                <input
                    type="checkbox"
                    id="useCppMain"
                    checked={project.options.useCppMain}
                    onChange={() => {
                        project.options.useCppMain = !project.options.useCppMain;
                        onHashChange();
                    }}
                />
                <label htmlFor="useCppMain">Use CPP Main (Use a C++ main instead of INO)</label>
            </div>
        </div>
    );
}