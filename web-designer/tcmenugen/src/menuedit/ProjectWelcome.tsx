import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {setCurrentlyOpenProject} from "../App";
import {parseEmfJsonToProject} from "../domain/PersistedMenu";

export function ProjectWelcome() {
    const navigate = useNavigate();
    const [isDragging, setIsDragging] = useState(false);

    const handleFileDrop = async (e: React.DragEvent) => {
        e.preventDefault();
        setIsDragging(false);
        const file = e.dataTransfer.files[0];
        if (file && file.name.endsWith('.emf')) {
            const text = await file.text();
            console.log("EMF File dropped:", file.name, "Length:", text.length);

            try {
                let project = parseEmfJsonToProject(text)
                setCurrentlyOpenProject(project);
                navigate('/menu-edit');
            } catch(e) {
                alert("Could not parse provided EMF file. Please make sure it is a valid TcMenu project file." + e);
                console.log((e as Error).stack);
                return;
            }
        } else {
            alert("Please drop a valid .emf file");
        }
    };

    const handleDragOver = (e: React.DragEvent) => {
        e.preventDefault();
        setIsDragging(true);
    };

    const handleDragLeave = () => {
        setIsDragging(false);
    };

    const handleOpenDirectory = async () => {
        try {
            // @ts-ignore
            const directoryHandle = await window.showDirectoryPicker();
            console.log("Directory selected:", directoryHandle.name);
            // Placeholder: logic to scan directory for EMF files would go here
            alert(`Selected directory: ${directoryHandle.name}. Directory scanning is not yet implemented.`);
        } catch (err: any) {
            if (err.name !== 'AbortError') {
                console.error("Error opening directory:", err);
                alert("Could not open directory.");
            }
        }
    };

    return <div className="menu-editor-welcome">
        <h1>Menu Editor</h1>
        <p>To begin editing, please provide a project file or directory.</p>
        
        <div className="editor-actions">
            <button className="open-dir-button" onClick={handleOpenDirectory}>
                Open Local Project Directory (preferred)
            </button>

            <div className="action-divider">
                <span>OR</span>
            </div>

            <div
                className={`drop-zone ${isDragging ? 'dragging' : ''}`}
                onDrop={handleFileDrop}
                onDragOver={handleDragOver}
                onDragLeave={handleDragLeave}
            >
                <p>Drag and drop your <strong>.emf</strong> project file here.</p>
            </div>
        </div>
        
        <p className="help-text">Navigate to "Start Project" to create a new one from scratch.</p>
        <p className="help-text">If you drag an EMF file then we'll output a zip file instead of round tripping.</p>
    </div>;
}
