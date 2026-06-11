import {embeddedPlatformFromId} from "../domain/Platforms";
import {generateUUID} from "../util/uuid";
import {EepromSaveMode, MenuTreeWithCodeOptions, ProjectSaveLocation} from "../domain/ProjectStruct";
import {GeneratorOptionsEditor} from "./GeneratorOptionsEditor";

export function RootEditor({project, onHashChange}: {project: MenuTreeWithCodeOptions, onHashChange: () => void}) {
    return (
        <div className="root-editor">
            <h2>Project Settings</h2>

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