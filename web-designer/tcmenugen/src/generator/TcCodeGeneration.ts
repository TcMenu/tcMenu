import {PersistedProject, projectToPersistedJson} from "../domain/PersistedMenu";
import {generateUUID} from "../util/uuid";
import {CreatorProperty, MenuTreeWithCodeOptions, SubSystem} from "../domain/ProjectStruct";
import {EmbeddedPlatform} from "../domain/Platforms";

export const NO_REMOTE_ID = "2c101fec-1f7d-4ff3-8d2b-992ad41e7fcb";

export interface GenerationRequest {
    project: PersistedProject;
    existingProperties: Array<CreatorProperty>;
    requiredFiles: GeneratedFile[];
}

export interface GeneratedFile {
    fileName: string;
    content: string;
    alwaysOverwrite: boolean;
}

export interface GenerationResponse {
    generatedFiles: Array<GeneratedFile>;
    logLines: Array<LogEntry>;
    successful: boolean;
    buildId: string;
}

export interface LogEntry {
    log: string;
    level: string;
}

// validator modes:
// "text" (text field plain pattern)
// "variable" (text field variable pattern)
// "boolean" (checkbox)
// "pin", "optpin" (text field with pin pattern)
// "number[min,max]" (text field with min max)
// "choices[choices]" (combobox with choices populateed)
// "font" (font dialog as per now)
// rgb (use RGB picker next to text field)
// menuitem[item type or *] (allow user to select from a list of items)
// ioexpander TBD
// separator - simply shows a separator line with text.

export interface ChoiceDesc {
    choiceValue: string;
    choiceDesc: string;
}

export enum PropertyValidationMode {
    BOOLEAN, FONT, OPTIONAL_PIN, PIN, INT_WITH_RANGE, VARIABLE,
    TEXT, CHOICES, MENUITEM, IO_EXPANDER, RGB, SEPARATOR
}

export class PropValidationInfo {
    constructor(
        public mode: PropertyValidationMode,
        public min: number,
        public max: number,
        public choices: ChoiceDesc[],
        public menuItemFilter?: string,
    ) {}
}

export class PropertyDescription {
    constructor(
        public description: string,
        public validation: PropValidationInfo,
        public extendedDescription?: string,
        public applicability?: string
    ) {}
}

export enum ThemeMode { MONO = 0, COLOR = 1, ANY=2, PALETTE = 3, NONE = 4}

export enum FontMode {DEFAULT_FONT = 0, ADAFRUIT = 1, ADAFRUIT_LOCAL = 2, NUMBERED = 3, U8G2 = 4, TCUNICODE = 5, TCUNICODE_LOCAL = 6}


export class ThemeDescription {
    constructor(
        public themeMode: ThemeMode,
        public fontMode: FontMode,
        public knownPalette?: string[],
    ) {}
}

export class PublishableCodePluginItem {
    constructor(
        public pluginGroup: string,
        public license: string,
        public vendor: string,
        public id: string,
        public description: string,
        public extendedDescription: string,
        public supportedPlatforms: string[],
        public subsystem: SubSystem,
        public imageFileName: string,
        public docsLink: string,
        public themeDescription: ThemeDescription,
        public properties: CreatorProperty[],
        public propertyDescriptions: {[key: string]: PropertyDescription},
        public requiredLibraries?: string[],
    ) {}
}

export let userNeedsChooseDisplay = getDefaultPlugin(SubSystem.DISPLAY);
export let userNeedsChooseInput = getDefaultPlugin(SubSystem.INPUT);
export let userNeedsChooseTheme = getDefaultPlugin(SubSystem.THEME);

export function getDefaultPlugin(subsystem: SubSystem): PublishableCodePluginItem {
    return new PublishableCodePluginItem(
        "None",
        "Apache 2.0",
        "The Coders Corner",
        generateUUID(),
        "No plugin selected",
        "Please select a plugin from the list of available plugins before proceeding.",
        [],
        subsystem,
        "",
        "",
        new ThemeDescription(ThemeMode.NONE, FontMode.DEFAULT_FONT),
        [],
        {}
    );
}

// tcmenuapp.thecoderscorner.com/api/v1/generator/

export async function getActiveProfile(): Promise<string> {
    try {
        let resp = await fetch("/api/v1/environment/profile");
        if(resp.ok) {
            return await resp.text();
        }
    } catch (e) {
        console.error("Failed to fetch profile", e);
    }
    return "noenv";
}

function jsonifyGenerationRequest(project: MenuTreeWithCodeOptions, requiredFiles: GeneratedFile[]): string {
    return JSON.stringify({
        project: projectToPersistedJson(project),
        existingProperties: project.options.lastProperties.map(p => ({
            name: p.name,
            subsystem: SubSystem[p.subsystem],
            latestValue: p.latestValue
        })),
        requiredFiles: requiredFiles.map(f => ({fileName: f.fileName, alwaysOverwrite: f.alwaysOverwrite}))
    });
}

export async function runGenerateCode(request: MenuTreeWithCodeOptions, requiredFile: GeneratedFile[]): Promise<GenerationResponse> {
    let req = await fetch("/api/v1/generator/generate", {
        method: "POST",  headers: {"Content-Type": "application/json", "Accept": "application/json"},
        body: jsonifyGenerationRequest(request, requiredFile)
    });
    if (!req.ok) {
        throw new Error(`Unable to generate code, status was ${req.status}`);
    }
    return await req.json() as GenerationResponse;
}

export async function getPluginsByIds(ids: string[]): Promise<Array<PublishableCodePluginItem>> {
    let req = await fetch("/api/v1/generator/plugins/byIdList", {
        method: "POST", body: JSON.stringify(ids), headers: {"Content-Type": "application/json", "Accept": "application/json"}});
    if (!req.ok) {
        throw new Error(`Unable to get plugins, status was ${req.status}`);
    }
    const json = await req.json();
    return json.map((p: any) => deserializePublishablePlugin(p));
}

export async function searchPlugins(query: string, subsystem: SubSystem, platform: EmbeddedPlatform): Promise<Array<PublishableCodePluginItem>> {
    let req = await fetch(
        "/api/v1/generator/plugins/search?query=" + encodeURIComponent(query) + "&subsystem=" + subsystem + "&platform=" + platform.boardId, {
            method: "GET", headers: {"Content-Type": "application/json", "Accept": "application/json"},
        });
    if (!req.ok) {
        throw new Error(`Unable to search plugins, status was ${req.status}`);
    }
    const json = await req.json();
    return json.map((p: any) => deserializePublishablePlugin(p));
}

export function deserializePublishablePlugin(pluginData: string | any): PublishableCodePluginItem {
    const json = (typeof pluginData === 'string') ? JSON.parse(pluginData) : pluginData;

    let themeDesc: ThemeDescription;
    if(!json.themeDescription) {
        themeDesc = new ThemeDescription(ThemeMode.NONE, FontMode.DEFAULT_FONT);
    } else {
        themeDesc = new ThemeDescription(
            ThemeMode[json.themeDescription.themeMode as keyof typeof ThemeMode],
            FontMode[json.themeDescription.fontMode as keyof typeof FontMode],
            json.themeDescription.knownPalette
        );
    }

    const propDescriptions: {[key: string]: PropertyDescription} = {};
    for (const key in json.propertyDescriptions) {
        const pd = json.propertyDescriptions[key];
        if(!pd.validation) throw new Error(`Invalid plugin data, property ${key} has no validation information`)
        const valInfo = new PropValidationInfo(
            PropertyValidationMode[pd.validation.mode as keyof typeof PropertyValidationMode],
            pd.validation.min ?? 0,
            pd.validation.max ?? 255,
            pd.validation.choices ?? [],
            pd.validation.menuItemFilter ?? "",
        );
        propDescriptions[key] = new PropertyDescription(pd.description, valInfo, pd.extendedDescription, pd.applicability);
    }

    const properties: CreatorProperty[] = (json.properties || []).map((theProp: any) => ({
        name: theProp.name,
        subsystem: SubSystem[theProp.subsystem as keyof typeof SubSystem],
        latestValue: theProp.initialValue
    } as CreatorProperty));

    return new PublishableCodePluginItem(
        json.pluginGroup,
        json.license,
        json.vendor,
        json.id,
        json.description,
        json.extendedDescription,
        json.supportedPlatforms,
        SubSystem[json.subsystem as keyof typeof SubSystem],
        json.imageFileName,
        json.docsLink,
        themeDesc,
        properties,
        propDescriptions,
        json.requiredLibraries
    );
}

export function prettyPrintFont(fontType: FontMode) {
    switch(fontType) {
        case FontMode.DEFAULT_FONT: return "Default Font";
        case FontMode.ADAFRUIT: return "Adafruit";
        case FontMode.ADAFRUIT_LOCAL: return "Adafruit Local";
        case FontMode.NUMBERED: return "Numbered";
        case FontMode.U8G2: return "U8G2";
        case FontMode.TCUNICODE: return "TcUnicode";
        case FontMode.TCUNICODE_LOCAL: return "TcUnicode Local";
    }
}

function protoFontType(fontType: FontMode):string {
    switch (fontType) {
        case FontMode.DEFAULT_FONT: return  "def";
        case FontMode.TCUNICODE: return "tca";
        case FontMode.TCUNICODE_LOCAL: return "tcl";
        case FontMode.ADAFRUIT: return "ada";
        case FontMode.ADAFRUIT_LOCAL: return "adl";
        case FontMode.NUMBERED: return "num";
        case FontMode.U8G2: return "u8g2";
        default: return "avl";
    }
}

function fromShortMode(fontType: string): FontMode {
    switch (fontType) {
        case "tca": return FontMode.TCUNICODE;
        case "tcl": return FontMode.TCUNICODE_LOCAL;
        case "ada": return FontMode.ADAFRUIT;
        case "adl": return FontMode.ADAFRUIT_LOCAL;
        case "u8g2": return FontMode.U8G2;
        case "num": return FontMode.NUMBERED;
        default: return FontMode.DEFAULT_FONT;
    }
}

export class FontDescription {
    public static DEFAULT_FONT = new FontDescription(FontMode.DEFAULT_FONT, "", 1);
    public fontType: FontMode;
    public fontName: string;
    public fontMag: number;

    constructor(fontType: FontMode, fontName: string, fontMag: number) {
        this.fontType = fontType;
        this.fontName = fontName;
        this.fontMag = fontMag;
    }

    toString(): string {
        const mag = this.fontMag > 0 ? "x " + this.fontMag : "";
        return `${prettyPrintFont(this.fontType)}: ${this.fontName}${mag}`;
    }

    toProtocol(): string {
        return `${protoFontType(this.fontType)}:${this.fontName},${this.fontMag}`;
    }

    static fromProtocol(proto: string): FontDescription {
        const fontDefPattern = /^(\w+):([\w_]*),(\d+)$/;
        const matcher = proto.match(fontDefPattern);
        if (matcher && matcher.length === 4) {
            return new FontDescription(
                fromShortMode(matcher[1]),
                matcher[2],
                parseInt(matcher[3])
            );
        }
        return this.DEFAULT_FONT;
    }
}
