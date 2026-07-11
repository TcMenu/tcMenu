import React from "react";
import {GenerationResponse, LogEntry} from "./TcCodeGeneration";
import "./GeneratorLogView.css";
import {MenuTreeWithCodeOptions, RoundTripMode} from "../domain/ProjectStruct";
import {filePatcher} from "./FilePatcher";

export interface CodeGeneratedProperties {
    response: GenerationResponse;
    onDismiss: () => void;
    menuProject: MenuTreeWithCodeOptions;
}

export const GeneratorLogView: React.FC<CodeGeneratedProperties> = ({response, onDismiss, menuProject})  => {
    const [logLines, setLogLines] = React.useState<LogEntry[]>(response.logLines);
    const [genStatus, setGenStatus] = React.useState<string>(response.successful ? "SUCCESS" : "FAILED");
    const getLevelClass = (level: string) => {
        switch (level.toUpperCase()) {
            case 'ERROR': return 'log-level-error';
            case 'WARN':
            case 'WARNING': return 'log-level-warn';
            case 'INFO': return 'log-level-info';
            case 'DEBUG': return 'log-level-debug';
            default: return '';
        }
    };

    const onLineLogged = (line: LogEntry) => {
        setLogLines(prevLines => [...prevLines, line]);
    };

    async function onPatchFiles() {
        try {
            setGenStatus("PATCHING IN PROGRESS");
            await filePatcher(response.generatedFiles, menuProject, onLineLogged);
            onLineLogged({ level: "INFO", log: "All files patched successfully!"});
            setGenStatus("PATCHED FILES");
        } catch (error) {
            alert("Failed to patch files: " + error);
        }
    }

    return (
        <div className="generator-log-container">
            <div className="generator-log-header">
                <h3>Code Generation Results</h3>
                <div className="log-header-controls">
                    <p>Build ID: {response.buildId}</p>
                    <p>Status: <span className={response.successful ? "status-success" : "status-fail"}>
                        {genStatus}
                    </span></p>
                </div>
                <div className="log-header-buttons">
                    {response.successful && (
                        <a href={`/api/v1/generator/generate/zip/${response.buildId}`} 
                           className="download-button" 
                           download={`tcmenu-build-${response.buildId}.zip`}>
                            Download Zip
                        </a>
                    )}
                    {response.successful && menuProject.roundTripMode === RoundTripMode.DIRECTORY_IN_BROWSER &&
                    <button type="button" className="download-button" style={{fontSize: "100%"}} onClick={onPatchFiles}>Apply Directly</button>}
                    <button type="button" className="dismiss-button" onClick={onDismiss}>Dismiss</button>
                </div>
            </div>
            <div className="generator-log-terminal">
                {logLines && logLines.map((entry: LogEntry, index: number) => (
                    <div key={index} className="log-line">
                        <span className={`log-level ${getLevelClass(entry.level)}`}>
                            {entry.level.toUpperCase().padEnd(5)}
                        </span>
                        <span className="log-message">{entry.log}</span>
                    </div>
                ))}
            </div>
            <h4><a href="https://github.com/TcMenu/tcMenu/discussions/572">
                Please help us improve the web designer by sharing your feedback on GitHub!</a></h4>
        </div>
    );
}