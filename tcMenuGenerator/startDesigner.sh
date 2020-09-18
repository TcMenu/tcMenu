#!/bin/bash

echo "==================================================="
echo "Starting TcMenu Designer"
echo "Ensure that you have at least openJDK 11 installed."
echo "==================================================="

pushd app

java --module-path ../deps --add-modules com.thecoderscorner.tcmenu.menuEditorUI com.thecoderscorner.menu.editorui.MenuEditorApp

popd

