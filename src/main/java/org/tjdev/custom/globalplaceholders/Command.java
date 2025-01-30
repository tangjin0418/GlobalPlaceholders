package org.tjdev.custom.globalplaceholders;

import org.tjdev.util.tjpluginutil.object.NewThis;
import org.tjdev.util.tjpluginutil.spigot.command.CustomCommand;
import org.tjdev.util.tjpluginutil.spigot.command.SpigotCommand;

import static org.tjdev.util.tjpluginutil.spigot.object.TJPlugin.plugin;

public class Command implements NewThis {
    public Command() {
        new CustomCommand(plugin.getName().toLowerCase()) {{
            addDefault("reload");
            addReload();

            init(new SpigotCommand(getName(), "Main command.").command);
        }};
    }
}
