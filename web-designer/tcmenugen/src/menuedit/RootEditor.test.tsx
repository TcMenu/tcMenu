import React, {useState} from 'react';
import {fireEvent, render, screen} from '@testing-library/react';
import {RootEditor} from './RootEditor';
import {MenuTree} from '../domain/MenuTree';
import {ARDUINO_AVR} from '../domain/Platforms';
import {EepromSaveMode, MenuTreeWithCodeOptions, ProjectSaveLocation, RoundTripMode} from '../domain/ProjectStruct';

const createDummyProject = (projectFileName?: string): MenuTreeWithCodeOptions => ({
    menuTree: new MenuTree(() => {}),
    description: 'Root Editor Test Project Description',
    projectFileName: projectFileName,
    options: {
        embeddedPlatform: ARDUINO_AVR.boardId,
        lastDisplayUuid: '',
        lastInputUuid: '',
        lastRemoteUuids: [],
        lastThemeUuid: '',
        applicationUUID: '12345678-1234-1234-1234-1234567890ab',
        applicationName: 'Test App',
        lastProperties: [],
        namingRecursive: false,
        saveLocation: ProjectSaveLocation.ONE_SINGLE_FILE_MENU_MAIN,
        useCppMain: false,
        usingSizedEEPROMStorage: false,
        eepromSaveMode: EepromSaveMode.DYNAMIC_WRITE_BY_ID,
        eepromDefinition: '',
        authenticatorDefinition: '',
        projectIoExpanders: [],
        useDynamicMenus: true,
        menuInMenuCollection: { menuDefinitions: [] },
        i18nEnabled: false
    },
    roundTripMode: RoundTripMode.DIRECTORY_IN_BROWSER
});

function RootEditorTestWrapper({project, onHashChange}: {project: MenuTreeWithCodeOptions, onHashChange?: () => void}) {
    const [, setHash] = useState(0);
    return (
        <RootEditor
            project={project}
            onHashChange={() => {
                setHash(h => h + 1);
                if (onHashChange) onHashChange();
            }}
        />
    );
}

describe('RootEditor Component', () => {
    it('renders project settings and displays existing projectFileName', () => {
        const dummyProject = createDummyProject('My_Custom_File');
        const hashChangeMock = jest.fn();

        render(<RootEditorTestWrapper project={dummyProject} onHashChange={hashChangeMock} />);

        expect(screen.getByText('Project Settings')).toBeInTheDocument();
        expect(screen.getByLabelText('Project Name')).toHaveValue('Test App');
        const fileNameInput = screen.getByLabelText('Project File Name');
        expect(fileNameInput).toHaveValue('My_Custom_File');
        expect(screen.getByText(/TcMenu Generator defaults to creating files based on the project directory or project name in camel case/i)).toBeInTheDocument();
    });

    it('updates projectFileName and triggers onHashChange', () => {
        const dummyProject = createDummyProject();
        const hashChangeMock = jest.fn();

        render(<RootEditorTestWrapper project={dummyProject} onHashChange={hashChangeMock} />);

        const fileNameInput = screen.getByLabelText('Project File Name');
        fireEvent.change(fileNameInput, {target: {value: 'New_File_Name'}});

        expect(dummyProject.projectFileName).toBe('New_File_Name');
        expect(hashChangeMock).toHaveBeenCalled();
    });

    it('displays error warning when invalid characters are entered into projectFileName', () => {
        const dummyProject = createDummyProject();
        const hashChangeMock = jest.fn();

        render(<RootEditorTestWrapper project={dummyProject} onHashChange={hashChangeMock} />);

        const fileNameInput = screen.getByLabelText('Project File Name');
        fireEvent.change(fileNameInput, {target: {value: 'Invalid-Name Spaces'}});

        expect(screen.getByText('Project file name can only contain letters, numbers, and underscores')).toBeInTheDocument();
    });
});
