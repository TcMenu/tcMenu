import React, {useEffect, useState} from 'react';
import logo from './img/logo192.png';
import tcIcon from './img/logo192.png';
import './App.css';
import {Link, NavLink, Route, Routes} from 'react-router-dom';
import {MenuTreeWithCodeOptions, RoundTripMode} from "./domain/ProjectStruct";
import {getActiveProfile} from "./generator/TcCodeGeneration";
import {parseEmfJsonToProject, projectToPersistedJson} from "./domain/PersistedMenu";
import {StartNewProject} from './StartNewProject';
import {TcMenuEditor} from "./menuedit/TcMenuEditor";
import {GenerateCodeView} from "./generator/GenerateCodeView";
import embedIcon from "./img/embedControlLogo300.png"
import {IoExpanderComponent} from "./generator/IoExpanderComponent";
import ReleaseNotes from "./releaseNotes";
import {get, set} from 'idb-keyval';
import fontEdIcon from './img/font-editor-example.jpg'

const TC_MENU_STORAGE_KEY = "tcMenuTurboProject";
const TC_MENU_POLICY_KEY = "tcMenuTurboPolicyAccepted";
let currentlyOpenProject: MenuTreeWithCodeOptions|null = null;
let globalDirectoryHandle: FileSystemDirectoryHandle|null = null;
let projectListeners: ((proj: MenuTreeWithCodeOptions | null) => void)[] = [];
let saveTimer: any = null;

export function getCurrentlyOpenProject(): MenuTreeWithCodeOptions|null {
    return currentlyOpenProject;
}

export function getDirectoryHandle(): FileSystemDirectoryHandle|null {
    return globalDirectoryHandle;
}

export const findFileWithExtension = async (directoryHandle: any, ext1: string, ext2: string = ".undef"): Promise<any | null> => {
    for await (const entry of directoryHandle.values()) {
        if (entry.kind === 'file' && (entry.name.endsWith(ext1) || entry.name.endsWith(ext2))) {
            return entry;
        }
    }
    return null;
};

export function  saveProjectToLocalStorage(proj: MenuTreeWithCodeOptions | null) {
    if (saveTimer) {
        clearTimeout(saveTimer);
        saveTimer = null;
    }

    if (proj) {
        saveTimer = setTimeout(() => {
            try {
                const json = JSON.stringify(projectToPersistedJson(proj), (key, value) => {
                    return value;
                }, 2);
                localStorage.setItem(TC_MENU_STORAGE_KEY, JSON.stringify({
                    json: json,
                    mode: proj.roundTripMode
                }));
                console.log("Project auto-saved to localStorage");
            } catch (e) {
                console.error("Failed to save project to localStorage", e);
            }
            saveTimer = null;
        }, 5000);
    } else {
        localStorage.removeItem(TC_MENU_STORAGE_KEY);
    }
}

export function setCurrentlyOpenProject(proj: MenuTreeWithCodeOptions | null, directoryHandle: any = null) {
    currentlyOpenProject = proj;
    if (proj) {
        proj.menuTree.setTreeStructureChanged((tree, id) => {
            console.log("MenuTree changed:", id, "saving project");
            saveProjectToLocalStorage(currentlyOpenProject);
            projectListeners.forEach(l => l(currentlyOpenProject));
        });
        // We also want to save when options change, so we proxy the options
        const originalOptions = proj.options;
        if (!(originalOptions as any).isProxy) {
            proj.options = new Proxy(originalOptions, {
                get(target, prop) {
                    if (prop === 'isProxy') return true;
                    return (target as any)[prop];
                },
                set(target: any, prop, value) {
                    target[prop] = value;
                    console.log("Project options changed:", prop, "saving project");
                    saveProjectToLocalStorage(currentlyOpenProject);
                    return true;
                }
            });
        }
        if(directoryHandle) {
            saveProjectToLocalStorage(currentlyOpenProject);
            globalDirectoryHandle = directoryHandle;
            set('last_project_dir', directoryHandle)
                .catch(() => {
                    alert("Failed to save last project directory, project will not be able to save");
                });
        }
    } else {
        localStorage.removeItem(TC_MENU_STORAGE_KEY);
    }
    projectListeners.forEach(l => l(proj));
}

async function rehydrateProjectDirectory(proj: MenuTreeWithCodeOptions) {
    const savedHandle = await get('last_project_dir');

    if (savedHandle) {
        // 1. Check if we still have readwrite permissions (usually "prompt" or "denied" after tab close)
        let perm = await savedHandle.queryPermission({ mode: 'readwrite' });

        console.log(`Permission status: ${perm}`);

        if (perm !== 'granted') {
            console.log(`Re-request perms`);
            // 2. Trigger a simple browser pop-up asking to restore access to that specific folder
            try {
                perm = await savedHandle.requestPermission({ mode: 'readwrite' });
            } catch (e) {
                console.error("Failed to request permission", e);
                perm = 'denied';
            }
        }

        globalDirectoryHandle = savedHandle;

        console.log(`Permission status II: ${perm}`);

        if (perm === 'granted') {
            console.log(`Successfully rehydrated folder: ${savedHandle.name}`);
            setCurrentlyOpenProject(proj, savedHandle);
            return savedHandle; // You are fully back in business without the picker!
        } else {
            console.log(`Failed to rehydrate folder: ${savedHandle.name}`);
        }
    }
    return null; // Fallback to recommend closing project
}

export function useCurrentlyOpenProject() {
    const [project, setProject] = React.useState<MenuTreeWithCodeOptions | null>(() => {
        if (currentlyOpenProject) return currentlyOpenProject;
        const saved = localStorage.getItem(TC_MENU_STORAGE_KEY);
        if (saved) {
            try {
                const parsed = JSON.parse(saved);
                const restored = parseEmfJsonToProject(parsed.json, parsed.mode as RoundTripMode);
                setCurrentlyOpenProject(restored);
                if(restored.roundTripMode === RoundTripMode.DIRECTORY_IN_BROWSER) {
                    rehydrateProjectDirectory(restored)
                        .catch(() => {
                            alert("Failed to restore last project directory, project will not be able to save");
                        });
                }
                return restored;
            } catch (e) {
                console.error("Failed to restore project from localStorage", e);
            }
        }
        return null;
    });

    React.useEffect(() => {
        const listener = (p: MenuTreeWithCodeOptions | null) => setProject(p);
        projectListeners.push(listener);

        return () => {
            projectListeners = projectListeners.filter(l => l !== listener);
        };
    }, []);
    return project;
}

function PolicyDialog({onAccept}: { onAccept: () => void }) {
    return (
        <div className="dialog-overlay" style={{
            position: 'fixed', top: 0, left: 0, width: '100%', height: '100%',
            backgroundColor: 'rgba(0,0,0,0.85)', zIndex: 10000,
            display: 'flex', justifyContent: 'center', alignItems: 'center'
        }}>
            <div className="dialog-content" style={{
                backgroundColor: 'white', padding: '30px', borderRadius: '10px',
                maxWidth: '600px', textAlign: 'center', color: 'black'
            }}>
                <h2>Fair Usage, Local Storage and Cookie Policy</h2>
                <p>This application uses local storage to save your project state and ensure you don't lose your work.
                   By using this system, you agree to our fair usage policy and the use of local storage for these purposes.</p>
                <p>When you choose the generate code option, the code is sent to our server for compilation and generation. We do not
                    keep the generated code on the server, it is deleted soon after compilation.
                </p>
                <p>During the BETA testing phase of our website, we are providing everyone with free access to this app.
                    You can use this application for your own purposes, reporting any issues you find to us so that we can attempt
                    to correct them. Given its BETA status, it goes without saying that you should not presently rely on this application
                    in production systems.
                </p>
                <p>In a subsequent update we reserve the right modify these terms. Should you disagree with these terms, tcMenu Designer desktop instead.
                </p>
                <div className="dialog-actions" style={{marginTop: '20px', justifyContent: 'center'}}>
                    <button className="primary-button" onClick={onAccept}>I Accept these terms</button>
                </div>
            </div>
        </div>
    );
}

const LandingPage = () => (
    <main style={{padding: '24px', maxWidth: '900px'}}>
        <section>
            <h1>TcMenu Turbo</h1>
            <p style={{fontSize: '1.15rem', maxWidth: '720px'}}>
                Design your embedded menu structure, configure your project, and generate TcMenu code from one place.
            </p>

            <div style={{display: 'flex', gap: '12px', flexWrap: 'wrap', marginTop: '20px'}}>
                <Link className="primary-button" to="/start-project">
                    Start a new project
                </Link>
                <Link className="secondary-button" to="/menu-edit">
                    Open an existing project
                </Link>
            </div>
        </section>

        <section style={{marginTop: '32px'}}>
            <h2>Build a menu in three steps</h2>
            <ol>
                <li>
                    <strong>Create or open a project</strong> — choose your target platform and project settings.
                </li>
                <li>
                    <strong>Design your menu</strong> — add menu items, submenus, actions, values, and callbacks.
                </li>
                <li>
                    <strong>Generate code</strong> — select display, input, theme, and remote options, then create your embedded code.
                </li>
            </ol>
        </section>

        <p><Link to="/release-notes">View Release Notes</Link></p>

        <section style={{
            marginTop: '24px',
            padding: '14px 16px',
            borderLeft: '4px solid #d98c00',
            backgroundColor: '#fff7e6'
        }}>
            <h2 style={{marginTop: 0}}>Developer snapshot</h2>
            <p style={{marginBottom: 0}}>
                TcMenu Turbo is currently a developer snapshot. It is suitable for evaluation and testing, but we do not
                recommend relying on it for production systems yet.
            </p>
        </section>

        <section style={{marginTop: '24px'}}>
            <p>
                New to TcMenu or need more detail? Read the{' '}
                <a href="https://www.thecoderscorner.com/products/apps/tcmenu-designer/">
                    tcMenu Designer documentation
                </a>.
            </p>
        </section>

        <div style={{display: 'flex', justifyContent: 'left', gap: '16px', paddingTop: '16px'}}>
            <img className="frontImg" src={tcIcon} alt="TcMenu Logo"/>
            <img className="frontImg" src={embedIcon} alt="EmbedControl Logo"/>
        </div>
    </main>
);

export const MainRoutes = () => {
    return (
        <Routes>
            <Route path="/" element={<LandingPage />} />
            <Route path="/start-project" element={<StartNewProject />} />
            <Route path="/menu-edit" element={<TcMenuEditor />} />
            <Route path="/generate-code" element={<GenerateCodeView />} />
            <Route path="/io-expanders" element={<IoExpanderComponent />} />
            <Route path="/release-notes" element={<ReleaseNotes />} />
            <Route path="/bitmap-generator" element={<div>
                <h1>Font and Bitmap Editor</h1>
                <p>You can edit fonts, title widgets and bitmaps using the new Font Bmp Editor application.</p>
                <p>The plan is to make the app available on the Windows store and MacOS app store, but even right
                    now there are nightly builds for all platforms.</p>
                <img src={fontEdIcon} alt="Font and Bitmap Editor application screen"/>
                <h2>How to get the editor</h2>
                <ul>
                    <li><a href="https://github.com/TcMenu/tcMenu/actions/workflows/build_windows.yml">Windows Nightly</a></li>
                    <li><a href="https://github.com/TcMenu/tcMenu/actions/workflows/build_mac.yml">MacOS Nightly</a></li>
                    <li><a href="https://github.com/TcMenu/tcMenu/actions/workflows/build_linux.yml">Linux Nightly</a></li>
                    <li><a href="https://github.com/TcMenu/tcMenu/">From source</a></li>
                </ul>
            </div>} />
        </Routes>
    );
}

function App() {
    const project = useCurrentlyOpenProject();
    const [profile, setProfile] = useState("noenv");
    const [policyAccepted, setPolicyAccepted] = useState(() => {
        return localStorage.getItem(TC_MENU_POLICY_KEY) === "true";
    });

    useEffect(() => {
        getActiveProfile().then(p => setProfile(p));
    }, []);

    const acceptPolicy = () => {
        localStorage.setItem(TC_MENU_POLICY_KEY, "true");
        setPolicyAccepted(true);
    };

    return (
        <div className="App">
            {!policyAccepted && <PolicyDialog onAccept={acceptPolicy}/>}
            <header className="App-header">
                <Link to="/"><img src={logo} className="App-logo" alt="logo"/></Link>
                <div className="header-content">
                    <div className="header-top-row">
                        <p>TcMenu Turbo - Designer &gt;&gt;</p>
                        <div className="active-profile"><a href="/release-notes">{profile}</a></div>
                    </div>
                    <nav>
                        <ul>
                            <li><NavLink to="/start-project">Start Project</NavLink></li>
                            <li><NavLink to="/menu-edit">Menu Edit</NavLink></li>
                            {project && <li><NavLink to="/generate-code">Generate Code</NavLink></li>}
                            {project && <li><NavLink to="/io-expanders">Io Expanders</NavLink></li>}
                            <li><NavLink to="/bitmap-generator">Font Bmp Editor</NavLink></li>
                        </ul>
                    </nav>
                </div>
            </header>
            <MainRoutes />
            <footer className="app-footer">
                <p>&copy; 2026 Nutricherry LTD and TheCodersCorner.com. All rights reserved.
                    &nbsp;&nbsp;<a href="https://www.thecoderscorner.com/legal/privacy/">Privacy</a>
                    &nbsp;&#183;&nbsp;<a href="https://www.thecoderscorner.com">Built by us</a>
                    &nbsp;&#183;&nbsp;<a href="https://www.thecoderscorner.com/support-services/">Commercial support</a>
                </p>
            </footer>
        </div>
    );
}

export default App;
