

export abstract class EepromDefinition {
    public abstract stringDefinition(): string;
    public abstract toString(): string;

    public static readFromProject(encoding: string): EepromDefinition {
        if (!encoding) return new NoEEPROM();

        try {
            if (encoding.startsWith("avr:")) return new AVREepromDefinition();
            else if (encoding.startsWith("eeprom:")) return new ArduinoEepromDefinition();
            else if (encoding.startsWith("prefs:")) {
                const parts = encoding.split(":");
                const ns = parts[1];
                const size = parseInt(parts[2]);
                return new PrefsEepromDefinition(ns, size);
            }
            else if (encoding.startsWith("bsp:")) {
                const memOffset = parseInt(encoding.substring(4));
                return new BspEepromDefinition(memOffset);
            } else if (encoding.startsWith("at24:")) {
                const parts = encoding.split(":");
                const addr = parseInt(parts[1]);
                const pageSize = parts[2] as At24RomSize;
                return new At24EepromDefinition(addr, pageSize);
            }
            else return new NoEEPROM();
        }
        catch (ex) {
            return new NoEEPROM();
        }
    }
}

export class NoEEPROM extends EepromDefinition {
    constructor() {
        super();
    }

    public stringDefinition() {
        return "";
    }

    public toString() {
        return "No EEPROM";
    }
}

export class ArduinoEepromDefinition extends EepromDefinition {
    constructor() {
        super();
    }

    public stringDefinition() {
        return "eeprom:";
    }

    public toString() {
        return "Arduino EEPROM";
    }
}

export class AVREepromDefinition extends EepromDefinition {
    constructor() {
        super();
    }

    public stringDefinition() {
        return "avr:";
    }

    public toString() {
        return "AVR EEPROM";
    }
}

export enum At24RomSize {
    PAGESIZE_AT24C01= "PAGESIZE_AT24C01",
    PAGESIZE_AT24C02= "PAGESIZE_AT24C02",
    PAGESIZE_AT24C04= "PAGESIZE_AT24C04",
    PAGESIZE_AT24C08= "PAGESIZE_AT24C08",
    PAGESIZE_AT24C16= "PAGESIZE_AT24C16",
    PAGESIZE_AT24C32= "PAGESIZE_AT24C32",
    PAGESIZE_AT24C64= "PAGESIZE_AT24C64",
    PAGESIZE_AT24C128= "PAGESIZE_AT24C128",
    PAGESIZE_AT24C256= "PAGESIZE_AT24C256",
    PAGESIZE_AT24C512= "PAGESIZE_AT24C512"
}

export class At24EepromDefinition extends EepromDefinition {
    public address: number = 0x50;
    public size: At24RomSize = At24RomSize.PAGESIZE_AT24C128;

    constructor(address: number, size: At24RomSize) {
        super();
        this.address = address;
        this.size = size;
    }

    public stringDefinition() {
        return `at24:${this.address}:${this.size}`;
    }

    toString() {
        return `AT24 EEPROM at 0x${this.address.toString(16)} (${this.size})`;
    }
}

export class BspEepromDefinition extends EepromDefinition {
    public offset: number = 0;
    constructor(offset: number) {
        super();
        this.offset = offset;
    }
    
    public stringDefinition() {
        return `bsp:${this.offset}`;
    }

    toString() {
        return `BSP EEPROM at 0x${this.offset.toString(16)}`;
    }
}

export class PrefsEepromDefinition extends EepromDefinition {
    public romNamespace: string = "";
    public size: number = 0;
    
    constructor(romNamespace: string, size: number) {
        super();
        this.romNamespace = romNamespace;
        this.size = size;
    }
    
    public stringDefinition() {
        return `prefs:${this.romNamespace}:${this.size}`;
    }

    toString() {
        return `Preferences EEPROM (${this.romNamespace}, ${this.size} bytes)`;
    }
}

export abstract class AuthenticationDefinition {
    public abstract stringDefinition(): string;
    public abstract toString(): string;

    public static readFromProject(encoding: string): AuthenticationDefinition {
        if (!encoding) return new NoAuthentication();

        try {
            if (encoding.startsWith("rom:")) {
                const entries = encoding.split(":");
                if (entries.length !== 3) return new NoAuthentication();
                const offset = parseInt(entries[1]);
                const numRemotes = parseInt(entries[2]);
                return new EepromAuthenticationDefinition(offset, numRemotes);
            } else if (encoding.startsWith("flash:")) {
                const remoteIds = encoding.split(":");
                let current = 2;
                const remoteList: FlashRemoteId[] = [];
                while ((current + 1) < remoteIds.length) {
                    remoteList.push(new FlashRemoteId(remoteIds[current], remoteIds[current + 1]));
                    current += 2;
                }
                return new ReadOnlyAuthenticationDefinition(remoteIds[1], remoteList);
            } else return new NoAuthentication();
        } catch (ex) {
            return new NoAuthentication();
        }
    }

}

export class NoAuthentication extends AuthenticationDefinition {
    public stringDefinition() {
        return "";
    }
    
    toString() {
        return "No authentication";
    }
}

export class FlashRemoteId {
    public name: string;
    public uuid: string;
    
    constructor(name: string, uuid: string) {
        this.name = name;
        this.uuid = uuid;
    }
}

export class ReadOnlyAuthenticationDefinition extends AuthenticationDefinition {
    public pin: string;
    public remoteIds: FlashRemoteId[];
    
    constructor(pin: string, remoteIds: FlashRemoteId[]) {
        super();
        this.pin = pin;
        this.remoteIds = remoteIds;
    }
    
    public stringDefinition() {
        return "flash:" + this.pin + ':' +
            this.remoteIds.map(rem => rem.name + ":" + rem.uuid).join(":");
    }
    
    toString() {
        return "Read-only authentication [details omitted for security]";
    }
}

export class EepromAuthenticationDefinition extends AuthenticationDefinition {
    offset: number;
    numRemotes: number;

    constructor(offset: number, numRemotes: number) {
        super();
        this.offset = offset;
        this.numRemotes = numRemotes;
    }

    public stringDefinition() {
        return `rom:${this.offset}:${this.numRemotes}`;
    }

    toString(): string {
        return "EEPROM authentication with " + this.numRemotes + " remote(s) at offset 0x" + this.offset.toString(16) + "";
    }
}