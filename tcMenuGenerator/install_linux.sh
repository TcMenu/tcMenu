#!/bin/bash

DIRNAME="tcmenu"
BINNAME="tcmenu"

# Install system-wide if we have root privileges, or to user dirs if not
if [[ $EUID -eq 0 ]]; then
  # Running as root
  LIBDIR="/usr/share/java"
  BINDIR="/usr/bin"
else
  # Running as a user
  LIBDIR="${HOME}/.local/share/java"
  BINDIR="${HOME}/.local/bin"
fi


if [[ "$1" == "--uninstall" ]]; then
  # Uninstall
  rm -r "${LIBDIR}/${DIRNAME}"
  rm "${BINDIR}/${BINNAME}"
  echo "Uninstalled."
  exit 0
fi

if [[ ! -d target/jfx/deps ]] ; then
  echo "Application doesn't appear to be compiled. Attempting to do so (requires jdk, maven)."
  mvn clean install -f ../baseInputDisplayPlugin
  mvn clean install -f ../dfRobotCodePlugin
  mvn -DskipTests clean install
  if [[ ! -d target/jfx/deps ]] ; then
    "Compilation failed. Ensure java development kit and maven build system are installed."
    exit 1
  fi
fi

# Create directories if needed
mkdir -p "${LIBDIR}/${DIRNAME}"
mkdir -p "${BINDIR}"

# Copy jars to destination directory
cp target/jfx/deps/* "${LIBDIR}/${DIRNAME}/"
cp -r target/jfx/app/plugins "${LIBDIR}/${DIRNAME}/"

# Make a "binary" script to run the application
cat > "${BINDIR}/${BINNAME}" << ENDHEREDOC
#!/bin/bash
# No attempt to control java version...
# Arch has archlinux-java-run, but need a distribution independent method
cd "${LIBDIR}/${DIRNAME}/"
java --module-path . --add-modules com.thecoderscorner.tcmenu.menuEditorUI com.thecoderscorner.menu.editorui.MenuEditorApp
ENDHEREDOC
chmod 755 "${BINDIR}/${BINNAME}"

if [[ ! "${PATH}" =~ "${BINDIR}" ]] ; then
  echo "Installed. Note that ${BINDIR} is not in your path. Add it then run with ${BINNAME}"
else
  echo "Installed. Try running using ${BINNAME}"
fi
