## How tcMenu works with plugins

This includes a few forward statements of what will happen as third party plugins arrive. At the moment there are no third party plugins so we compile them all up and put then into the `~/.tcmenu/plugins` at first startup and upon the user upgrading.

The code would already read additional plugins, there is nothing hardwiring the number of plugins. If another directory for example `super-plugin` was added under the plugins directory, and it contained a valid plugin it would be loaded up.

If third party plugins appear, we'll work with the writers to work out the best way to get them into tcMenu Designer. What we think may be the easiest for most, is to have a series of contrib plugins within the xmlPlugins directory. Then we would build them automatically with regular plugin builds. This would require a little coordination, but nothing particularly difficult. Another possibility would be that the plugin vendor takes full control in their own repo, and then have their own release cycle.

For all cases, we will need to create the ability to add plugins easily, from either a custom repo, or from our own repo. This will require a few quite trivial changes to make it a reality. So small that they will probably appear over the 2.1.x cycle.

## Start up

When the designer starts it tries to load all the plugins, any that fail because they are invalid are not loaded. Once a plugin is loaded it will be available within the code generator for selection.

## An easy way to develop plugin

What we normally do is create a symlink to the plugin directories in the `.tcmenu/plugins` directory that links to your source directory. But make sure that under no circumstances you use the update button, or you could lose all the changes.
