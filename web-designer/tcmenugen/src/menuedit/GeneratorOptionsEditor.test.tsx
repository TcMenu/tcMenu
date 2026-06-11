// GeneratorOptionsEditor.test.tsx
import React from 'react';
import {fireEvent, render, screen} from '@testing-library/react';
import {GeneratorOptionsEditor, GeneratorOptionsProps} from './GeneratorOptionsEditor';
import {EepromSaveMode, ProjectSaveLocation} from '../domain/ProjectStruct';

describe('GeneratorOptionsEditor Component', () => {
    const mockOnSaveLocationChange = jest.fn();
    const mockOnEepromSaveModeChange = jest.fn();
    const mockMenuBuilderChange = jest.fn();

    const defaultProps: GeneratorOptionsProps = {
        menuBuilderOn: false,
        saveLocation: ProjectSaveLocation.PROJECT_TO_SRC_WITH_GENERATED,
        eepromSaveMode: EepromSaveMode.WRITE_BY_POSITION_WITH_SIZE,
        onSaveLocationChange: mockOnSaveLocationChange,
        onEepromSaveModeChange: mockOnEepromSaveModeChange,
        onMenuBuilderChange: mockMenuBuilderChange
    };

    it('renders correctly with initial props', () => {
        render(<GeneratorOptionsEditor {...defaultProps} />);

        const saveLocationSelect = screen.getByLabelText('Project Structure') as HTMLSelectElement;
        const eepromSaveModeSelect = screen.getByLabelText('How to save state to EEPROM') as HTMLSelectElement;

        expect(saveLocationSelect.value).toBe(String(ProjectSaveLocation.PROJECT_TO_SRC_WITH_GENERATED));
        expect(eepromSaveModeSelect.value).toBe(String(EepromSaveMode.WRITE_BY_POSITION_WITH_SIZE));
    });

    it('calls onSaveLocationChange on save location change', () => {
        render(<GeneratorOptionsEditor {...defaultProps} />);

        const saveLocationSelect = screen.getByLabelText('Project Structure') as HTMLSelectElement;
        fireEvent.change(saveLocationSelect, {target: {value: String(ProjectSaveLocation.ALL_TO_SRC)}});

        expect(mockOnSaveLocationChange).toHaveBeenCalledWith(ProjectSaveLocation.ALL_TO_SRC);
    });

    it('calls onEepromSaveModeChange on save mode change', () => {
        render(<GeneratorOptionsEditor {...defaultProps} />);

        const eepromSaveModeSelect = screen.getByLabelText('How to save state to EEPROM') as HTMLSelectElement;
        fireEvent.change(eepromSaveModeSelect, {target: {value: String(EepromSaveMode.WRITE_BY_POSITION_WITH_SIZE)}});

        expect(mockOnEepromSaveModeChange).toHaveBeenCalledWith(EepromSaveMode.WRITE_BY_POSITION_WITH_SIZE);
    });

    it('displays the correct options for project structure', () => {
        render(<GeneratorOptionsEditor {...defaultProps} />);

        const saveLocationOptions = screen.getAllByRole('option', {name: /Plugins in one file|TcMenu files/});

        expect(saveLocationOptions).toHaveLength(6);
        expect(saveLocationOptions[0]).toHaveTextContent('Plugins in one file, menu structure managed by TcMenu');
        expect(saveLocationOptions[1]).toHaveTextContent('Plugins in one file, menu structure in sketch');
        expect(saveLocationOptions[2]).toHaveTextContent('TcMenu files in the current directory');
        expect(saveLocationOptions[3]).toHaveTextContent('TcMenu files in the "generated" directory');
        expect(saveLocationOptions[4]).toHaveTextContent('TcMenu files in the "src" directory');
        expect(saveLocationOptions[5]).toHaveTextContent('Project in "src/" TcMenu files in the "src/generated" directory');
    });

    it('displays the correct options for EEPROM save mode', () => {
        render(<GeneratorOptionsEditor {...defaultProps} />);

        const eepromSaveModeOptions = screen.getAllByRole('option', {name: /Write by position with size|Dynamic write by ID|Legacy write by position/});

        expect(eepromSaveModeOptions).toHaveLength(3);
        expect(eepromSaveModeOptions[0]).toHaveTextContent('Dynamic write by ID (Best for new projects)');
        expect(eepromSaveModeOptions[1]).toHaveTextContent('Legacy write by position');
        expect(eepromSaveModeOptions[2]).toHaveTextContent('Write by position with size');
    });
});