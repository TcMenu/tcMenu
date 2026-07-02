import {getCurrentlyOpenProject, getDirectoryHandle, waitForDirectoryHandle} from "../App";
import {RoundTripMode} from "../domain/ProjectStruct";

export enum InternationalizationMode {
    NONE, NOT_INIT,
    I18N_DIR_PROPS,
    CREATE_ON_GEN
}

export interface I18nHandler {
    mode: InternationalizationMode;
    valueForKey(key: string): string;
    reload(): Promise<void>;
}

export const NO_INTERNATIONALIZATION: I18nHandler = {
    mode: InternationalizationMode.NONE,
    valueForKey: (key: string) => key,
    reload(): Promise<void> { return Promise.resolve();}
}

export const NOT_INITIALIZED_I18N: I18nHandler = {
    mode: InternationalizationMode.NOT_INIT,
    valueForKey: (key: string) => key,
    reload(): Promise<void> { return Promise.resolve();}
}

export const CREATE_ONLY_I18N: I18nHandler = {
    mode: InternationalizationMode.CREATE_ON_GEN,
    valueForKey: (key: string) => key,
    reload(): Promise<void> { return Promise.resolve();}
}

let globalI18nHandler: I18nHandler = NOT_INITIALIZED_I18N;
let i18nRehydrationPromise: Promise<I18nHandler> | null = null;

export async function getInternationalization(): Promise<I18nHandler> {
    if(globalI18nHandler && globalI18nHandler !== NOT_INITIALIZED_I18N) {
        return globalI18nHandler;
    } else if (i18nRehydrationPromise) {
        return i18nRehydrationPromise;
    } else {
        i18nRehydrationPromise = determineInternationalization().then(() => {
            i18nRehydrationPromise = null;
            return globalI18nHandler;
        });
        return i18nRehydrationPromise;
    }
}

export function resetI18n() {
    console.log("Remove I18N support for a new project");
    let currentlyOpenProject = getCurrentlyOpenProject();
    if(currentlyOpenProject && globalI18nHandler.mode === InternationalizationMode.CREATE_ON_GEN) {
        globalI18nHandler = NO_INTERNATIONALIZATION;
        currentlyOpenProject.options.i18nEnabled = false;
    }
}

export function i18nStateHasChanged() {
    globalI18nHandler = NOT_INITIALIZED_I18N;
    i18nRehydrationPromise = null;
}

async function determineInternationalization(): Promise<void> {
    let currentlyOpenProject = getCurrentlyOpenProject();
    let globalDirectoryHandle = getDirectoryHandle();
    console.log("Determine I18N for project", currentlyOpenProject?.options.applicationName, "mode", currentlyOpenProject?.roundTripMode);
    if (currentlyOpenProject?.roundTripMode === RoundTripMode.DIRECTORY_IN_BROWSER && !globalDirectoryHandle) {
        console.log("Waiting for directory handle...");
        globalDirectoryHandle = await waitForDirectoryHandle();
        console.log("Directory handle received", globalDirectoryHandle?.name);
    }

    if(currentlyOpenProject?.roundTripMode === RoundTripMode.DIRECTORY_IN_BROWSER && globalDirectoryHandle) {
        try {
            const dir = await globalDirectoryHandle.getDirectoryHandle("i18n", {create: false});
            console.log("DIr in browser with i18n dir, turned on support");
            const handler = new PropertiesI18nHandler(dir);
            await handler.reload();
            globalI18nHandler = handler;
        } catch (e) {
            console.log("DIr in browser without i18n dir, turned off support");
            globalI18nHandler = NO_INTERNATIONALIZATION;
        }
    } else if(currentlyOpenProject?.options?.i18nEnabled) {
        console.log("A menu with i18n support was loaded by drag/drop, just remembering that");
        globalI18nHandler = CREATE_ONLY_I18N;
    } else {
        globalI18nHandler = NO_INTERNATIONALIZATION;
    }
}


export class PropertiesI18nHandler implements I18nHandler {
    mode: InternationalizationMode;
    dict: Map<string, string>;
    i18nDir: FileSystemDirectoryHandle;

    constructor(i18nDir: FileSystemDirectoryHandle) {
        this.mode = InternationalizationMode.I18N_DIR_PROPS;
        this.dict = new Map<string, string>();
        this.i18nDir = i18nDir;
    }

    private async loadProperties(): Promise<void> {try {

            const fileHandle = await this.i18nDir.getFileHandle("project-lang.properties");
            const file = await fileHandle.getFile();
            const content = await file.text();

            const lines = content.split('\n');
            for (const line of lines) {
                const trimmedLine = line.trim();
                // Skip empty lines and comments
                if (trimmedLine === '' || trimmedLine.startsWith('#') || trimmedLine.startsWith('!')) {
                    continue;
                }

                const equalIndex = trimmedLine.indexOf('=');
                if (equalIndex > 0) {
                    const key = trimmedLine.substring(0, equalIndex).trim();
                    const value = trimmedLine.substring(equalIndex + 1).trim();
                    this.dict.set(key, value);
                }
            }
            console.log(`Properties file loaded with ${this.dict.size} elements`);
        } catch (e) {
            console.error("Failed to load properties file:", e);
        }
    }

    async reload(): Promise<void> {
        this.dict.clear();
        await this.loadProperties();
    }

    valueForKey(key: string): string {
        if(key.startsWith('%')) {
            key = key.substring(1);
        }
        return this.dict.get(key) ?? key;
    }

}
