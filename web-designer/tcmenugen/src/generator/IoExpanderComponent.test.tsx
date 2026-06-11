import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { IoExpanderComponent, expanderFromString, CustomDeviceExpander, Pcf8574DeviceExpander } from './IoExpanderComponent';
import { useCurrentlyOpenProject } from '../App';

// Mock FontAwesomeIcon
jest.mock('@fortawesome/react-fontawesome', () => ({
    FontAwesomeIcon: () => <span className="mock-icon" />
}));

jest.mock('../App', () => ({
    useCurrentlyOpenProject: jest.fn(),
}));

// Mock window.confirm
window.confirm = jest.fn(() => true);

describe('IoExpanderComponent', () => {
    let mockProject: any;

    beforeEach(() => {
        mockProject = {
            options: {
                projectIoExpanders: [
                    'pcf8574:MyExpander:32:2:true',
                    'mcp23017:AnotherExp:33:4'
                ]
            },
            setDirty: jest.fn()
        };
        (useCurrentlyOpenProject as jest.Mock).mockReturnValue(mockProject);
    });

    it('renders no project open message when project is null', () => {
        (useCurrentlyOpenProject as jest.Mock).mockReturnValue(null);
        render(<IoExpanderComponent />);
        expect(screen.getByText(/No project open/i)).toBeInTheDocument();
    });

    it('renders the list of expanders from project options', () => {
        render(<IoExpanderComponent />);

        expect(screen.getByText('MyExpander')).toBeInTheDocument();
        expect(screen.getByText('Pcf8574')).toBeInTheDocument();
        expect(screen.getByText('pcf8574:MyExpander:32:2:true')).toBeInTheDocument();

        expect(screen.getByText('AnotherExp')).toBeInTheDocument();
        expect(screen.getByText('Mcp23017')).toBeInTheDocument();
        expect(screen.getByText('mcp23017:AnotherExp:33:4')).toBeInTheDocument();

        // Internal expander is always present
        expect(screen.getByText('devicePins')).toBeInTheDocument();
        expect(screen.getByText('Internal')).toBeInTheDocument();
    });

    it('allows adding a new expander', () => {
        render(<IoExpanderComponent />);
        
        fireEvent.click(screen.getByText('Add New'));
        
        const nameInput = screen.getByLabelText('Expander Name');
        fireEvent.change(nameInput, { target: { value: 'NewExp' } });
        
        fireEvent.click(screen.getByText('Save'));

        expect(mockProject.options.projectIoExpanders).toHaveLength(3);
        expect(mockProject.options.projectIoExpanders[2]).toBe('customIO:NewExp');
    });

    it('disallows duplicate names', () => {
        render(<IoExpanderComponent />);
        
        fireEvent.click(screen.getByText('Add New'));
        
        const nameInput = screen.getByLabelText('Expander Name');
        fireEvent.change(nameInput, { target: { value: 'MyExpander' } }); // Duplicate
        
        fireEvent.click(screen.getByText('Save'));

        expect(screen.getByText(/already exists/i)).toBeInTheDocument();
        expect(mockProject.options.projectIoExpanders).toHaveLength(2); // No change
    });

    it('disallows reserved name devicePins', () => {
        render(<IoExpanderComponent />);
        
        fireEvent.click(screen.getByText('Add New'));
        
        const nameInput = screen.getByLabelText('Expander Name');
        fireEvent.change(nameInput, { target: { value: 'devicePins' } });
        
        fireEvent.click(screen.getByText('Save'));

        expect(screen.getByText(/reserved/i)).toBeInTheDocument();
    });

    it('allows editing an existing expander', () => {
        render(<IoExpanderComponent />);
        
        const editButtons = screen.getAllByTitle('Edit');
        fireEvent.click(editButtons[0]); // Edit MyExpander
        
        const nameInput = screen.getByLabelText('Expander Name');
        fireEvent.change(nameInput, { target: { value: 'RenamedExp' } });
        
        fireEvent.click(screen.getByText('Save'));

        expect(mockProject.options.projectIoExpanders[0]).toBe('pcf8574:RenamedExp:32:2:true');
    });
});

describe('IoExpander classes', () => {
    it('should support cloning', () => {
        const custom = new CustomDeviceExpander("test");
        const cloned = custom.clone();
        expect(cloned).toBeInstanceOf(CustomDeviceExpander);
        expect(cloned.name).toBe("test");
        expect(cloned).not.toBe(custom);

        const pcf = new Pcf8574DeviceExpander("pcf", 32, "2", true);
        const clonedPcf = pcf.clone() as Pcf8574DeviceExpander;
        expect(clonedPcf.i2cAddress).toBe(32);
        expect(clonedPcf.inverted).toBe(true);
    });
});

describe('IoExpanderCollection and expanderFromString', () => {
    it('should parse pcf8574 correctly', () => {
        const exp = expanderFromString('pcf8574:Exp1:32:2:true');
        expect(exp.name).toBe('Exp1');
        expect(exp.toString()).toBe('pcf8574:Exp1:32:2:true');
    });

    it('should parse mcp23017 correctly', () => {
        const exp = expanderFromString('mcp23017:Exp2:33:4');
        expect(exp.name).toBe('Exp2');
        expect(exp.toString()).toBe('mcp23017:Exp2:33:4');
    });

    it('should parse aw9523 correctly', () => {
        const exp = expanderFromString('aw9523:Exp3:34:5');
        expect(exp.name).toBe('Exp3');
        expect(exp.toString()).toBe('aw9523:Exp3:34:5');
    });

    it('should parse customIO correctly', () => {
        const exp = expanderFromString('customIO:MyCustom');
        expect(exp.name).toBe('MyCustom');
        expect(exp.toString()).toBe('customIO:MyCustom');
    });

    it('should handle internal device expander', () => {
        const exp = expanderFromString('deviceIO:');
        expect(exp.getId()).toBe('devicePins');
    });
});
