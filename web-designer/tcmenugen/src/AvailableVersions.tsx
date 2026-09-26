import {useState, useEffect} from 'react';

export interface AvailableVersion {
    subdomain: string,
    description: string,
    url: string,
    versionPattern: string
}

const availProfilesUrl: string = "/api/v1/environment/availableProfiles"

export default function AvailableVersions() {
    const [versions, setVersions] = useState<AvailableVersion[]>([]);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        fetch(availProfilesUrl)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Failed to fetch available versions');
                }
                return response.json();
            })
            .then((data: AvailableVersion[]) => {
                setVersions(data);
            })
            .catch(err => {
                setError(err.message);
            });
    }, []);

    return <>
        <h1>Choosing between available versions</h1>
        <p>
            We intend to host three versions of tcMenu Web Designer to give you time to move between releases:
            “designer-next”, “designer”, and “designer-prev”. New releases are first published to “designer-next” (next
            will always have the latest features and bug fixes). With every minor or major release, the versions will
            shift downward through "designer" and "designer-prev".
        </p>
        <p>
            If your workflow is particularly sensitive to version changes, you can run your own local copy of
            Designer on your own hardware. We only ask that you do not make that instance publicly available.
            From 4.5.10 onwards, <a href="https://hub.docker.com/r/davetcc/tcmenu-web-generator">every release is
            available as a Docker image</a>.
        </p>
        <p>
            <b>IMPORTANT:</b> We recommend that you save and close your project before switching versions.
        </p>

        {error && <div style={{color: 'red'}}>Error: {error}</div>}

        {versions.length == 0 && <h2>No other versions available</h2> }

        {versions.length > 0 && (
            <div>
                <h2>Available Versions:</h2>
                <ul>
                    {versions.map((version, index) => (
                        <li key={index}>
                            <a href={version.url}><strong>{version.subdomain}</strong> ({version.description}) - Tracking V{version.versionPattern}</a>
                        </li>
                    ))}
                </ul>
            </div>
        )}

    </>;
}