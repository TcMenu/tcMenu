import React from 'react';
import {afterEach, beforeEach, describe, expect, test} from 'vitest';
import {act, render, screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router-dom';
import App, {LandingPage, setCurrentlyOpenProject} from './App';
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

describe('App and LandingPage', () => {
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

    test('renders app header', () => {
        render(
            <MemoryRouter>
                <App />
            </MemoryRouter>
        );
        const headerElements = screen.getAllByText(/TcMenu Web/i);
        expect(headerElements.length).toBeGreaterThan(0);
    });

    test('renders start project and open project buttons when no project is open', () => {
        render(
            <MemoryRouter>
                <LandingPage />
            </MemoryRouter>
        );
        expect(screen.getByRole('link', { name: /start a new project/i })).toBeInTheDocument();
        expect(screen.getByRole('link', { name: /open an existing project/i })).toBeInTheDocument();
        expect(screen.queryByRole('link', { name: /continue editing project/i })).not.toBeInTheDocument();
    });

    test('renders continue editing project button when a project is already open', () => {
        act(() => {
            setCurrentlyOpenProject(createDummyProject());
        });

        render(
            <MemoryRouter>
                <LandingPage />
            </MemoryRouter>
        );

        expect(screen.getByRole('link', { name: /continue editing project/i })).toBeInTheDocument();
        expect(screen.queryByRole('link', { name: /start a new project/i })).not.toBeInTheDocument();
        expect(screen.queryByRole('link', { name: /open an existing project/i })).not.toBeInTheDocument();
    });
});
