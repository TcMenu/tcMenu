import React, {useState} from "react";
import {
    AuthenticationDefinition,
    EepromAuthenticationDefinition,
    FlashRemoteId,
    NoAuthentication,
    ReadOnlyAuthenticationDefinition
} from "./EepromAndAuthSupport";
import {IntegerEditor} from "../menuedit/IntegerEditor";
import {StringListEditor} from "../menuedit/StringListEditor";

interface ChooseAuthenticatorDialogProps {
    initialAuth: AuthenticationDefinition;
    onCancel: () => void;
    onSave: (newAuth: AuthenticationDefinition) => void;
}

export function ChooseAuthenticatorDialog({initialAuth, onCancel, onSave}: ChooseAuthenticatorDialogProps) {
    const [auth, setAuth] = useState<AuthenticationDefinition>(initialAuth);
    const [, setHash] = useState(0);

    const onTypeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const type = e.target.value;
        let newAuth: AuthenticationDefinition;
        switch (type) {
            case "rom":
                newAuth = new EepromAuthenticationDefinition(0, 3);
                break;
            case "flash":
                newAuth = new ReadOnlyAuthenticationDefinition("1234", []);
                break;
            default:
                newAuth = new NoAuthentication();
        }
        setAuth(newAuth);
        setHash(h => h + 1);
    }

    const getSelectedType = () => {
        if (auth instanceof EepromAuthenticationDefinition) return "rom";
        if (auth instanceof ReadOnlyAuthenticationDefinition) return "flash";
        return "none";
    }

    const onListChange = (newList: string[]) => {
        if (auth instanceof ReadOnlyAuthenticationDefinition) {
            auth.remoteIds = newList.map(s => {
                const parts = s.split("|");
                return new FlashRemoteId(parts[0], parts[1] || "");
            });
            setHash(h => h + 1);
        }
    }

    return (
        <div className="dialog-overlay">
            <div className="dialog-content" style={{maxWidth: '600px'}}>
                <h2>Choose Authenticator Support</h2>
                <div className="form-group">
                    <label htmlFor="authType">Authenticator Type</label>
                    <select id="authType" value={getSelectedType()} onChange={onTypeChange}>
                        <option value="none">No Authentication</option>
                        <option value="rom">EEPROM Authenticator</option>
                        <option value="flash">Read-only (Flash) Authenticator</option>
                    </select>
                </div>

                {auth instanceof EepromAuthenticationDefinition && (
                    <>
                        <div className="form-group">
                            <label htmlFor="romOffset">EEPROM Offset</label>
                            <IntegerEditor
                                id="romOffset" min={0} max={65535}
                                initialValue={auth.offset}
                                onChange={(n) => {
                                    auth.offset = n;
                                    setHash(h => h + 1);
                                }}
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="numRemotes">Number of Remotes</label>
                            <IntegerEditor
                                id="numRemotes" min={1} max={10}
                                initialValue={auth.numRemotes}
                                onChange={(n) => {
                                    auth.numRemotes = n;
                                    setHash(h => h + 1);
                                }}
                            />
                        </div>
                    </>
                )}

                {auth instanceof ReadOnlyAuthenticationDefinition && (
                    <>
                        <div className="form-group">
                            <label htmlFor="pin">Authentication PIN</label>
                            <input
                                id="pin"
                                type="text"
                                maxLength={4}
                                value={auth.pin}
                                onChange={(e) => {
                                    auth.pin = e.target.value;
                                    setHash(h => h + 1);
                                }}
                            />
                        </div>
                        <StringListEditor
                            list={auth.remoteIds.map(rem => `${rem.name}|${rem.uuid}`)}
                            onListChange={onListChange}
                            label="Remote Name|UUID pairs"
                        />
                    </>
                )}

                <div className="dialog-actions">
                    <button className="secondary-button" onClick={onCancel}>Cancel</button>
                    <button className="primary-button" onClick={() => onSave(auth)}>Save Changes</button>
                </div>
            </div>
        </div>
    );
}
