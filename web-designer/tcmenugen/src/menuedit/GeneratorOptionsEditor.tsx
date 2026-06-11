import React from 'react';
import {EepromSaveMode, ProjectSaveLocation} from '../domain/ProjectStruct';

export interface GeneratorOptionsProps {
    saveLocation: ProjectSaveLocation;
    eepromSaveMode: EepromSaveMode;
    menuBuilderOn: boolean;
    onSaveLocationChange: (location: ProjectSaveLocation) => void;
    onEepromSaveModeChange: (mode: EepromSaveMode) => void;
    onMenuBuilderChange: (chg: boolean) => void;
}

export function GeneratorOptionsEditor({
                                            saveLocation,
                                            eepromSaveMode,
                                            menuBuilderOn,
                                            onSaveLocationChange,
                                            onEepromSaveModeChange,
                                            onMenuBuilderChange
                                        }: GeneratorOptionsProps) {
    return (
        <>
            <div className="form-group-checkbox">
                <input id="menuBuilderOn" type="checkbox" checked={menuBuilderOn} onChange={() => onMenuBuilderChange(!menuBuilderOn)}/>
                <label htmlFor="menuBuilderOn">Use fluent menu building API</label>
            </div>

            <div className="form-group">
                <label htmlFor="saveLocation">Project Structure</label>
                <select id="saveLocation" value={saveLocation} onChange={(e) => onSaveLocationChange(parseInt(e.target.value))}>
                    <optgroup label="Turbo options (Best for new projects)">
                        <option value={ProjectSaveLocation.ONE_SINGLE_FILE}>Plugins in one file, menu structure managed by TcMenu</option>
                        <option value={ProjectSaveLocation.ONE_SINGLE_FILE_MENU_MAIN}>Plugins in one file, menu structure in sketch</option>
                    </optgroup>
                    <optgroup label="Legacy options (generates static menu)">
                        <option value={ProjectSaveLocation.ALL_TO_CURRENT}>TcMenu files in the current directory</option>
                        <option value={ProjectSaveLocation.PROJECT_TO_CURRENT_WITH_GENERATED}>TcMenu files in the "generated" directory</option>
                        <option value={ProjectSaveLocation.ALL_TO_SRC}>TcMenu files in the "src" directory</option>
                        <option value={ProjectSaveLocation.PROJECT_TO_SRC_WITH_GENERATED}>Project in "src/" TcMenu files in the "src/generated" directory</option>
                    </optgroup>
                </select>
            </div>

            <div className="form-group">
                <label htmlFor="eepromSaveMode">How to save state to EEPROM</label>
                <select id="eepromSaveMode" value={eepromSaveMode} onChange={(e) => onEepromSaveModeChange(parseInt(e.target.value))}>
                    <option value={EepromSaveMode.DYNAMIC_WRITE_BY_ID}>Dynamic write by ID (Best for new projects)</option>
                    <option value={EepromSaveMode.LEGACY_WRITE_BY_POSITION}>Legacy write by position</option>
                    <option value={EepromSaveMode.WRITE_BY_POSITION_WITH_SIZE}>Write by position with size</option>
                </select>
            </div>
        </>
    );
}
