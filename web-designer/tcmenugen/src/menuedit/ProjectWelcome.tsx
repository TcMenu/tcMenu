import React, {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {findFileWithExtension, setCurrentlyOpenProject} from "../App";
import {parseEmfJsonToProject} from "../domain/PersistedMenu";
import {RoundTripMode} from "../domain/ProjectStruct";

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
            const directoryHandle = await window.showDirectoryPicker({mode: 'readwrite'});
            console.log("Directory selected:", directoryHandle.name);

            const emfFileHandle = await findFileWithExtension(directoryHandle, 'emf');

            if (emfFileHandle) {
                const file = await emfFileHandle.getFile();
                const text = await file.text();
                console.log("EMF File found:", emfFileHandle.name, "Length:", text.length);

                try {
                    let project = parseEmfJsonToProject(text, RoundTripMode.DIRECTORY_IN_BROWSER);
                    setCurrentlyOpenProject(project, directoryHandle);
                    navigate('/menu-edit');
                } catch (e) {
                    alert("Could not parse found EMF file. Please make sure it is a valid TcMenu project file." + e);
                    console.log((e as Error).stack);
                    return;
                }
            } else {
                alert(`No .emf file found in directory: ${directoryHandle.name}`);
            }
        } catch (err: any) {
            if (err.name !== 'AbortError') {
                console.error("Error opening directory:", err);
                alert("Could not open directory.");
            }
        }
    };

    function isFileSystemAccessSupported() {
        // 1. Direct feature check for the directory picker method
        const hasPicker = 'showDirectoryPicker' in window;

        // 2. Ensure we aren't on mobile (Android/iOS don't expose the local disk picker)
        const isMobile = /Mobi|Android|iPhone|iPad/i.test(navigator.userAgent);

        return hasPicker && !isMobile;
    }

    return <div className="menu-editor-welcome">
        <h1>Menu Editor</h1>
        <p>To begin editing, please provide a project file or directory.</p>
        
        <div className="editor-actions">

            {isFileSystemAccessSupported() ?
                <button className="open-dir-button" onClick={handleOpenDirectory}>Open Local Project Directory (round trip)</button> :
                <p>Round trip mode needs (Google Chrome or Microsoft Edge).</p>
            }

            <div className="action-divider">
                <span>OR</span>
            </div>

            <div
                className={`drop-zone ${isDragging ? 'dragging' : ''}`}
                onDrop={handleFileDrop}
                onDragOver={handleDragOver}
                onDragLeave={handleDragLeave}
            >
                <p>Drag and drop your <strong>.emf</strong> project file here (outputs project as zip).</p>
            </div>
        </div>
        
        <p className="help-text">Navigate to "Start Project" to create a new one from scratch.</p>
        <p className="help-text">If you drag an EMF file then we'll output a zip file instead of round tripping.</p>
    </div>;
}
