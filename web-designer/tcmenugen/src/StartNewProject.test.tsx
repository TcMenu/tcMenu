import React from 'react';
import {act, fireEvent, render, screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router-dom';
import {StartNewProject} from './StartNewProject';
import {setCurrentlyOpenProject} from './App';
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
});
