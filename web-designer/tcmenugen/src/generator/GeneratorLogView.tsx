import React from "react";
import {GenerationResponse, LogEntry} from "./TcCodeGeneration";
import "./GeneratorLogView.css";

export interface CodeGeneratedProperties {
    response: GenerationResponse;
    onDismiss: () => void;
}

export const GeneratorLogView: React.FC<CodeGeneratedProperties> = ({response, onDismiss})  => {
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

    return (
        <div className="generator-log-container">
            <h3>We need your feedback on where to take things next!</h3>
            <p><a href="https://github.com/TcMenu/tcMenu/discussions/572">
                Share your thoughts on our GitHub web designer discussion
            </a></p>
            <div className="generator-log-header">
                <h3>Code Generation Results</h3>
                <div className="log-header-controls">
                    <p>Build ID: {response.buildId}</p>
                    <p>Status: <span className={response.successful ? "status-success" : "status-fail"}>
                        {response.successful ? "SUCCESS" : "FAILED"}
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
                    <button type="button" className="dismiss-button" onClick={onDismiss}>Dismiss</button>
                </div>
            </div>
            <table className="log-table">
                <thead>
                    <tr>
                        <th style={{width: "100px"}}>Level</th>
                        <th>Message</th>
                    </tr>
                </thead>
                <tbody>
                    {response.logLines && response.logLines.map((entry: LogEntry, index: number) => (
                        <tr key={index}>
                            <td className={getLevelClass(entry.level)}>{entry.level}</td>
                            <td>{entry.log}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </div>
    );
}