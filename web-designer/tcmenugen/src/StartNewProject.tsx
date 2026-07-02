import React, {useEffect, useState} from 'react';
import {generateUUID} from "./util/uuid";
import {ALL_PLATFORMS} from './domain/Platforms';
import {
    CodeGeneratorOptions,
    EepromSaveMode,
    MenuTreeWithCodeOptions,
    ProjectSaveLocation,
    RoundTripMode
} from './domain/ProjectStruct';
import {getCurrentlyOpenProject, setCurrentlyOpenProject} from "./App";
import {MenuTree} from "./domain/MenuTree";

import {useNavigate} from 'react-router-dom';
import {GeneratorOptionsEditor} from "./menuedit/GeneratorOptionsEditor";
import {counterStarter} from "./menuedit/BulkAddTemplates";
import {JsonMenuItemSerializer} from "./domain/JsonMenuItemSerializer";

export function StartNewProject() {
    const navigate = useNavigate();
    const [projectName, setProjectName] = useState('');
    const [projectDescription, setProjectDescription] = useState('');
    const [projectUuid, setProjectUuid] = useState('00000000-0000-0000-0000-000000000000');
    const [platform, setPlatform] = useState(ALL_PLATFORMS[0].boardId);
    const [saveLocation, setSaveLocation] = useState(ProjectSaveLocation.ONE_SINGLE_FILE_MENU_MAIN);
    const [eepromSaveMode, setEepromSaveMode] = useState(EepromSaveMode.DYNAMIC_WRITE_BY_ID);
    const [namingRecursive, setNamingRecursive] = useState(false);
    const [useCppMain, setUseCppMain] = useState(false);
    const [menuBuilderOn, setMenuBuilderOn] = useState(true);

    useEffect(() => {
        // Basic placeholder for UUID generation if needed, but for now it's just a default
        setProjectUuid(generateUUID());
    }, []);

    const handleGenerateUuid = () => {
        setProjectUuid(generateUUID());
    };

    const handleGenerate = () => {
        const options: CodeGeneratorOptions = {
            embeddedPlatform: platform,
            lastDisplayUuid: "",
            lastInputUuid: "",
            lastRemoteUuids: [],
            lastThemeUuid: "",
            applicationUUID: projectUuid,
            applicationName: projectName,
            lastProperties: [],
            namingRecursive: namingRecursive,
            saveLocation: saveLocation,
            useCppMain: useCppMain,
            usingSizedEEPROMStorage: eepromSaveMode === EepromSaveMode.WRITE_BY_POSITION_WITH_SIZE,
            eepromSaveMode: eepromSaveMode,
            eepromDefinition: "",
            authenticatorDefinition: "",
            projectIoExpanders: [],
            useDynamicMenus: menuBuilderOn,
            menuInMenuCollection: { menuDefinitions: [] },
            i18nEnabled: false
        };
        const menuProject: MenuTreeWithCodeOptions = {
            menuTree: new MenuTree(menuTree => {}),
            description: projectDescription,
            options: options,
            roundTripMode: RoundTripMode.NEW_PROJECT
        };

        const serializer = new JsonMenuItemSerializer();
        serializer.putItemsIntoMenuTree(counterStarter, menuProject.menuTree, "0");

        setCurrentlyOpenProject(menuProject);
        navigate('/menu-edit');
    };

    if(getCurrentlyOpenProject()) {
        return <div>
            <h1>Project is already open</h1>
            <p>A project is already open. Close it from "Menu Edit" to use quick start!</p>
        </div>;
    }

    return (
        <div className="start-project-container">
            <div className="form-header">
                <h2>Start New TcMenu Project</h2>
                <button
                    type="button"
                    className="generate-button"
                    onClick={handleGenerate}
                    disabled={!projectName.trim() || !projectDescription.trim()}
                >
                    Generate &gt;&gt;
                </button>
            </div>
            <div className="form-group">
                <label htmlFor="projectName">Project Name</label>
                <input
                    type="text"
                    id="projectName"
                    value={projectName}
                    onChange={(e) => setProjectName(e.target.value)}
                    placeholder="Enter project name"
                />
            </div>
            <div className="form-group">
                <label htmlFor="projectDescription">Description</label>
                <textarea
                    id="projectDescription"
                    value={projectDescription}
                    onChange={(e) => setProjectDescription(e.target.value)}
                    placeholder="Enter project description"
                    rows={3}
                />
            </div>
            <div className="form-group">
                <label htmlFor="projectUuid">Project Unique ID (UUID)</label>
                <div className="input-with-button">
                    <input
                        type="text"
                        id="projectUuid"
                        value={projectUuid}
                        readOnly
                    />
                    <button type="button" onClick={handleGenerateUuid}>New ID</button>
                </div>
            </div>
            <div className="form-group">
                <label htmlFor="platform">Embedded Platform</label>
                <select id="platform" value={platform} onChange={(e) => setPlatform(e.target.value)}>
                    {ALL_PLATFORMS.map(p => (
                        <option key={p.boardId} value={p.boardId}>{p.friendlyName}</option>
                    ))}
                </select>
            </div>
            <GeneratorOptionsEditor
                saveLocation={saveLocation}
                eepromSaveMode={eepromSaveMode}
                menuBuilderOn={menuBuilderOn}
                onSaveLocationChange={setSaveLocation}
                onEepromSaveModeChange={setEepromSaveMode}
                onMenuBuilderChange={setMenuBuilderOn}
            />
            <div className="form-group-checkbox">
                <input
                    type="checkbox"
                    id="namingRecursive"
                    checked={namingRecursive}
                    onChange={(e) => setNamingRecursive(e.target.checked)}
                />
                <label htmlFor="namingRecursive">Include submenu name in item names</label>
            </div>
            <div className="form-group-checkbox">
                <input
                    type="checkbox"
                    id="useCppMain"
                    checked={useCppMain}
                    onChange={(e) => setUseCppMain(e.target.checked)}
                />
                <label htmlFor="useCppMain">Use a C++ main instead of INO</label>
            </div>
        </div>
    );
}