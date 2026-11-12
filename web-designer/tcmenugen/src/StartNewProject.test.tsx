import React from 'react';
import {act, fireEvent, render, screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router-dom';
import {StartNewProject} from './StartNewProject';
import {getCurrentlyOpenProject, setCurrentlyOpenProject} from './App';
import {MenuTree} from './domain/MenuTree';
import {EepromSaveMode, MenuTreeWithCodeOptions, ProjectSaveLocation, RoundTripMode} from './domain/ProjectStruct';

const createDummyProject = (): MenuTreeWithCodeOptions => ({
    menuTree: new MenuTree(() => {}),
    description: 'Test Description',
    options: {
        embeddedPlatform: 'ARDUINO_AVR',
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
    roundTripMode: RoundTripMode.NEW_PROJECT
});

describe('StartNewProject Component', () => {
    beforeEach(() => {
        act(() => {
            setCurrentlyOpenProject(null);
        });
    });

    afterEach(() => {
        act(() => {
            setCurrentlyOpenProject(null);
        });
    });

    it('renders start project form when no project is open', () => {
        render(
            <MemoryRouter>
                <StartNewProject />
            </MemoryRouter>
        );

        expect(screen.getByText('Start New TcMenu Project')).toBeInTheDocument();
        expect(screen.getByLabelText('Project Name')).toBeInTheDocument();
        expect(screen.getByLabelText('Project File Name')).toBeInTheDocument();
        expect(screen.getByText(/TcMenu Generator defaults to creating files based on the project directory or project name in camel case/i)).toBeInTheDocument();
        expect(screen.getByText('Generate >>')).toBeInTheDocument();
    });

    it('offers to close current project when a project is already open', () => {
        act(() => {
            setCurrentlyOpenProject(createDummyProject());
        });

        render(
            <MemoryRouter>
                <StartNewProject />
            </MemoryRouter>
        );

        expect(screen.getByText('Project is already open')).toBeInTheDocument();
        const closeBtn = screen.getByRole('button', {name: /close current project/i});
        expect(closeBtn).toBeInTheDocument();

        // Clicking close button should close project and show the start new project form
        act(() => {
            fireEvent.click(closeBtn);
        });

        expect(screen.getByText('Start New TcMenu Project')).toBeInTheDocument();
        expect(screen.getByLabelText('Project Name')).toBeInTheDocument();
    });

    it('validates project file name and generates new project with custom file name', () => {
        render(
            <MemoryRouter>
                <StartNewProject />
            </MemoryRouter>
        );

        const nameInput = screen.getByLabelText('Project Name');
        const descInput = screen.getByLabelText('Description');
        const fileNameInput = screen.getByLabelText('Project File Name');
        const generateBtn = screen.getByRole('button', {name: /generate/i});

        fireEvent.change(nameInput, {target: {value: 'My App'}});
        fireEvent.change(descInput, {target: {value: 'My App Description'}});
        expect(generateBtn).not.toBeDisabled();

        // Invalid project file name with spaces/symbols
        fireEvent.change(fileNameInput, {target: {value: 'Invalid Name!'}});
        expect(screen.getByText('Project file name can only contain letters, numbers, and underscores')).toBeInTheDocument();
        expect(generateBtn).toBeDisabled();

        // Valid project file name
        fireEvent.change(fileNameInput, {target: {value: 'Valid_File_123'}});
        expect(screen.queryByText('Project file name can only contain letters, numbers, and underscores')).not.toBeInTheDocument();
        expect(generateBtn).not.toBeDisabled();

        act(() => {
            fireEvent.click(generateBtn);
        });

        const currentProject = getCurrentlyOpenProject();
        expect(currentProject).not.toBeNull();
        expect(currentProject?.options.applicationName).toBe('My App');
        expect(currentProject?.projectFileName).toBe('Valid_File_123');
    });
});
