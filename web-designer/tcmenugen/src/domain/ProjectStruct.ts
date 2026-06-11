import {MenuTree} from "./MenuTree";

/**
 * Indicates to the code generator where the source and generated source should be generated (or where to find it)
 */
export  enum ProjectSaveLocation {
    /** All files including generated to the src directory */
    ALL_TO_SRC = 0,
    /** All files including generated to the current directory */
    ALL_TO_CURRENT = 1,
    /** All editable project files to current, generated non-editable files to generated */
    PROJECT_TO_CURRENT_WITH_GENERATED = 2,
    /** All editable project files to src, generated non-editable files to generated */
    PROJECT_TO_SRC_WITH_GENERATED = 3,
    /** The project_menu file contains all plugins along menu code and setup */
    ONE_SINGLE_FILE = 4,
    /** The project_menu file contains all plugins, but menu in main */
    ONE_SINGLE_FILE_MENU_MAIN = 5
}

export enum SubSystem {
    DISPLAY = "DISPLAY",
    INPUT = "INPUT",
    REMOTE = "REMOTE",
    THEME = "THEME"
}

export interface CreatorProperty {
    name: string;
    latestValue: string;
    subsystem: SubSystem;
}

export enum MenuInMenuReplicationMode {
    REPLICATE_ADD_STATUS_ITEM = "REPLICATE_ADD_STATUS_ITEM",
    REPLICATE_NO_STATUS_ITEM = "REPLICATE_NO_STATUS_ITEM"
}

export enum MenuInMenuConnectionType {
    SOCKET = "SOCKET",
    SERIAL = "SERIAL"
}

export enum EepromSaveMode {
    LEGACY_WRITE_BY_POSITION = 0,
    WRITE_BY_POSITION_WITH_SIZE = 1,
    DYNAMIC_WRITE_BY_ID = 2
}

export interface MenuInMenuDefinition {
    variableName: string;
    portOrIpAddress: string;
    portOrBaud: number;
    connectionType: MenuInMenuConnectionType;
    replicationMode: MenuInMenuReplicationMode;
    subMenuId: number;
    idOffset: number;
    maximumRange: number;
}

export interface MenuInMenuCollection {
    menuDefinitions: MenuInMenuDefinition[];
}

export interface CodeGeneratorOptions {
    embeddedPlatform: string;
    lastDisplayUuid: string;
    lastInputUuid: string;
    lastRemoteUuids: string[];
    lastThemeUuid: string;
    applicationUUID: string;
    applicationName: string;
    lastProperties: CreatorProperty[];
    namingRecursive: boolean;
    saveLocation: ProjectSaveLocation;
    useCppMain: boolean;
    usingSizedEEPROMStorage?: boolean; // legacy, kept to avoid parsing issues
    eepromSaveMode?: EepromSaveMode;
    eepromDefinition: string;
    authenticatorDefinition: string;
    projectIoExpanders: string[];
    useDynamicMenus?: boolean;
    menuInMenuCollection?: MenuInMenuCollection;
}

export enum RoundTripMode {
    DIRECTORY_IN_BROWSER,
    EMF_FILE_DRAGGED_IN,
    NEW_PROJECT
}

export interface MenuTreeWithCodeOptions {
     menuTree: MenuTree;
     description: string;
     options: CodeGeneratorOptions;
     roundTripMode: RoundTripMode;
}
