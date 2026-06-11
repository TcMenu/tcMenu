import React, {useEffect, useState} from 'react';
import './App.css';

interface ReleaseNote {
    release: string;
    qaStatus: string;
    dateReleased: string;
    notes: string[];
}

const ReleaseNotes: React.FC = () => {
    const [releaseNotes, setReleaseNotes] = useState<ReleaseNote[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        fetch('/release-notes.json')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to load release notes');
                }
                return response.json();
            })
            .then((data: ReleaseNote[]) => {
                setReleaseNotes(data);
                setLoading(false);
            })
            .catch(err => {
                setError(err.message);
                setLoading(false);
            });
    }, []);

    if (loading) {
        return (
            <div>
                <h1>Release Notes</h1>
                <p>Loading release notes...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div>
                <h1>Release Notes</h1>
                <p>Error loading release notes: {error}</p>
            </div>
        );
    }

    return (
        <div>
            <h1>Release Notes</h1>
            <p>Latest updates and improvements to TcMenu Designer</p>
            <div className="content-container">
                {releaseNotes.map((note, index) => (
                    <div key={index} className="release-note-section">
                        <div className="release-header">
                            <h2>Version {note.release} <span style={{fontSize: '0.8em'}}> released on {note.dateReleased}</span></h2>

                        </div>
                        <ul className="changes-list">
                            {note.notes.map((change, changeIndex) => (
                                <li key={changeIndex}>{change}</li>
                            ))}
                        </ul>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default ReleaseNotes;
