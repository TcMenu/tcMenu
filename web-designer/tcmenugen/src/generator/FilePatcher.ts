import {GeneratedFile, LogEntry} from "./TcCodeGeneration";
import {MenuTreeWithCodeOptions, RoundTripMode} from "../domain/ProjectStruct";
import {getDirectoryHandle} from "../App";

export async function filePatcher(files: GeneratedFile[], project: MenuTreeWithCodeOptions,
                                  onLineLogged: (line: LogEntry) => void) {
    onLineLogged({level: "INFO", log: "Starting local file patching"});
    if(project.roundTripMode !== RoundTripMode.DIRECTORY_IN_BROWSER) {
        throw new Error("Round trip mode must be enabled for file patching");
    }

    const rootDirectory = getDirectoryHandle();
    if(rootDirectory == null) {
        onLineLogged({level: "ERROR", log: "The project directory is not resolvable. Please use the zip and patch manually"});
        throw new Error("The project directory is not resolvable. Please use the zip and patch manually");
    }

    const permission = await (rootDirectory as any).queryPermission({ mode: "readwrite" });
    if(permission !== "granted") {
        const requestedPermission = await (rootDirectory as any).requestPermission({ mode: "readwrite" });
        if(requestedPermission !== "granted") {
            onLineLogged({level: "ERROR", log: "Cannot write into directory, please use zip download instead"});
            throw new Error("Write permission was not granted. Please use the zip and patch manually");
        }
    }

    const resolvedFiles = files.map(file => {
        const pathSegments = file.fileName.split("/");

        if(
            file.fileName.startsWith("/") ||
            file.fileName.includes("\\") ||
            pathSegments.some(segment => segment === "" || segment === "." || segment === "..")
        ) {
            onLineLogged({level: "ERROR", log: "Invalid generated file path: " + file.fileName});
            throw new Error("Invalid generated file path: " + file.fileName);
        }

        const directorySegments = pathSegments.slice(0, -1);
        const fileName = pathSegments[pathSegments.length - 1];

        if(directorySegments.length > 3) {
            onLineLogged({level: "ERROR", log: "Invalid generated file path - too many levels: " + file.fileName});
            throw new Error("Too many levels in the returned zip. Path=" + file.fileName);
        }

        return {
            file,
            directorySegments,
            fileName
        };
    });

    for(const resolvedFile of resolvedFiles) {
        let directoryHandle = rootDirectory;

        for(const segment of resolvedFile.directorySegments) {
            directoryHandle = await directoryHandle.getDirectoryHandle(segment, { create: true });
        }

        if(!resolvedFile.file.alwaysOverwrite) {
            try {
                await directoryHandle.getFileHandle(resolvedFile.fileName, { create: false });
                onLineLogged({level: "INFO", log: `File ${resolvedFile.fileName} is not overwritable and already exists, skipping`});
                continue;
            } catch(error) {
                onLineLogged({level: "INFO", log: `File ${resolvedFile.fileName} does not exist and will be created`});
            }
        } else {
            onLineLogged({level: "INFO", log: `File ${resolvedFile.fileName} will be written out`});
        }

        const fileHandle = await directoryHandle.getFileHandle(resolvedFile.fileName, { create: true });
        const writable = await (fileHandle as any).createWritable();

        try {
            await writable.write(resolvedFile.file.content);
        } finally {
            await writable.close();
        }
    }
}