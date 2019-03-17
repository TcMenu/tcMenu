When you have embedded code to go with your plugin, that does not reside in the library, then this code should
be put into a directory under this folder. There is a job as part of the build script that copies anything except
this readme file and .INO files into the META-INF/tcmenu directory. This is where the generator will look for those
files. The files should be in the form directoryName/fileName.ext. Not including the META-INF/tcmenu.