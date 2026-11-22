import React from 'react';
import { render, screen } from '@testing-library/react';
import { GeneratorLogView } from './GeneratorLogView';
import { GenerationResponse } from './TcCodeGeneration';
import { MenuTreeWithCodeOptions, RoundTripMode } from '../domain/ProjectStruct';
import { MenuTree } from '../domain/MenuTree';

describe('GeneratorLogView', () => {
    const mockMenuProject: MenuTreeWithCodeOptions = {
        menuTree: new MenuTree(jest.fn()),
        description: 'Test Project',
        options: {
            embeddedPlatform: 'ARDUINO_AVR',
            lastDisplayUuid: '',
            lastInputUuid: '',
            lastRemoteUuids: [],
            lastThemeUuid: '',
            applicationUUID: 'uuid',
            applicationName: 'Test App',
            lastProperties: [],
            namingRecursive: false,
            saveLocation: 0,
            useCppMain: false,
            eepromDefinition: '',
            authenticatorDefinition: '',
            projectIoExpanders: []
        },
        roundTripMode: RoundTripMode.NEW_PROJECT
    };

    it('renders SUCCESS status when response is successful and has no errors', () => {
        const response: GenerationResponse = {
            buildId: '12345',
            successful: true,
            generatedFiles: [],
            logLines: [
                { level: 'INFO', log: 'Starting generation...' },
                { level: 'WARN', log: 'This is a warning' }
            ]
        };

        render(<GeneratorLogView response={response} onDismiss={jest.fn()} menuProject={mockMenuProject} />);

        expect(screen.getByText('SUCCESS')).toBeInTheDocument();
        expect(screen.queryByText('SUCCESS WITH ERRORS')).not.toBeInTheDocument();
        expect(screen.queryByText('FAILED')).not.toBeInTheDocument();
    });

    it('renders SUCCESS WITH ERRORS status when response is successful but has ERROR log lines', () => {
        const response: GenerationResponse = {
            buildId: '12345',
            successful: true,
            generatedFiles: [],
            logLines: [
                { level: 'INFO', log: 'Starting generation...' },
                { level: 'ERROR', log: 'An error occurred during generation' }
            ]
        };

        render(<GeneratorLogView response={response} onDismiss={jest.fn()} menuProject={mockMenuProject} />);

        expect(screen.getByText('SUCCESS WITH ERRORS')).toBeInTheDocument();
        expect(screen.queryByText('SUCCESS')).not.toBeInTheDocument();
        expect(screen.queryByText('FAILED')).not.toBeInTheDocument();
    });

    it('renders SUCCESS WITH ERRORS status when response has lowercase error level', () => {
        const response: GenerationResponse = {
            buildId: '12345',
            successful: true,
            generatedFiles: [],
            logLines: [
                { level: 'error', log: 'Case insensitive error check' }
            ]
        };

        render(<GeneratorLogView response={response} onDismiss={jest.fn()} menuProject={mockMenuProject} />);

        expect(screen.getByText('SUCCESS WITH ERRORS')).toBeInTheDocument();
    });

    it('renders FAILED status when response is not successful even with or without errors', () => {
        const response: GenerationResponse = {
            buildId: '12345',
            successful: false,
            generatedFiles: [],
            logLines: [
                { level: 'ERROR', log: 'Fatal build error' }
            ]
        };

        render(<GeneratorLogView response={response} onDismiss={jest.fn()} menuProject={mockMenuProject} />);

        expect(screen.getByText('FAILED')).toBeInTheDocument();
        expect(screen.queryByText('SUCCESS WITH ERRORS')).not.toBeInTheDocument();
        expect(screen.queryByText('SUCCESS')).not.toBeInTheDocument();
    });
});
