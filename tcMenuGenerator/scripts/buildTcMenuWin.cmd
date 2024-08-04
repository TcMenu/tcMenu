echo off
echo "copy over icon"
copy classes\img\tcMenuDesigner.ico .

jpackage --type app-image -n tcmenu -p jfx/deps --input jfx/app --win-console --resource-dir .\classes\img\ --icon tcMenuDesigner.ico --app-version 4.3.0 --verbose --java-options "-Dprism.lcdtext=false --enable-preview -Djava.library.path=$APPDIR/win" --add-modules "jdk.crypto.cryptoki" -m com.thecoderscorner.tcmenu.menuEditorUI/com.thecoderscorner.menu.editorui.cli.TcMenuDesignerCmd

jpackage --type app-image -n tcMenuDesigner -p jfx/deps --input jfx/app  --resource-dir .\classes\img\ --icon tcMenuDesigner.ico --app-version 4.3.0 --verbose --java-options "-Dprism.lcdtext=false --enable-preview -Djava.library.path=$APPDIR/win" --add-modules "jdk.crypto.cryptoki" -m com.thecoderscorner.tcmenu.menuEditorUI/com.thecoderscorner.menu.editorui.cli.TcMenuDesignerCmd

echo "packaging complete, make one package into tcmenu"

copy tcMenuDesigner\app\tcMenuDesigner.cfg tcmenu\app
copy tcMenuDesigner\tcMenuDesigner.exe tcmenu

echo -----------------------------------------------------------
echo the tcmenu package is now ready for packaging using designerscript.iss in Inno Script Studio
