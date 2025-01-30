package org.tjdev.custom.globalplaceholders;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.tjdev.util.colorful.api.defaults.MiniMessage;
import org.tjdev.util.tjpluginutil.ConfigUtil;
import org.tjdev.util.tjpluginutil.config.file.YamlConfig;
import org.tjdev.util.tjpluginutil.spigot.object.TJPlugin;
import org.tjdev.util.tjpluginutil.spigot.plugin.papi.PAPI;

import java.util.HashMap;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static org.tjdev.util.tjpluginutil.IConstant.DEFAULT_COLOR_API;

public final class GlobalPlaceholders extends TJPlugin {
    private PAPI papi;

    @Override
    public void enable() {
        DEFAULT_COLOR_API = new MiniMessage();

        var map = new HashMap<String, String>();

        var config = ConfigUtil.prepareResult(getDataFolder(), "config");
        var configData = YamlConfig.loadConfiguration(config.getValue());

        if (!config.getKey()) {
            configData.set(
                    "player",
                    "<gray>[<white>{vault_prefix}</white>]</gray> <white>%player_displayname%</white>"
            );
            configData.set("color.warn", "<#FCCF31>");
        }

        ConfigUtil.save(configData, config.getValue());

        for (String key : configData.getKeys(true))
            if (configData.isString(key)) map.put(key, configData.getString(key));

        papi = new PAPI() {
            @Override
            public String request(Player p, String s) {
                var str = map.get(s);
                if (str == null) return DEFAULT_COLOR_API.color("<red><white>%s</white> not exist.</red>".formatted(s));
                return DEFAULT_COLOR_API.color(escapeLegacy(PAPI.setBracket(p, str)));
            }
        };
        papi.register();
    }

    @Override
    public void disable() {
        papi.unregister();
    }

    public static String escapeLegacy(String s) {
        return miniMessage().serialize(LegacyComponentSerializer.legacySection().deserialize(s));
    }
}
