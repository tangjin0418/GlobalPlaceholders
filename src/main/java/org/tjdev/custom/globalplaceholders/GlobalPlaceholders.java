package org.tjdev.custom.globalplaceholders;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.tjdev.util.colorful.api.defaults.MiniMessage;
import org.tjdev.util.tjpluginutil.ConfigUtil;
import org.tjdev.util.tjpluginutil.config.file.YamlConfig;
import org.tjdev.util.tjpluginutil.spigot.object.TJPlugin;
import org.tjdev.util.tjpluginutil.spigot.plugin.papi.PAPI;
import org.tjdev.util.tjpluginutil.text.Replacer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static org.tjdev.util.tjpluginutil.IConstant.DEFAULT_COLOR_API;

public final class GlobalPlaceholders extends TJPlugin {
    private PAPI papi;

    @Override
    public void enable() {
        DEFAULT_COLOR_API = new MiniMessage();

        var map = new LinkedHashMap<String, String>();

        var config = ConfigUtil.prepareResult(getDataFolder(), "config");
        var configData = YamlConfig.loadConfiguration(config.getValue());

        if (!config.getKey()) {
            configData.set(
                    "player",
                    "<gray>[<white>{vault_prefix}</white>]</gray> <white>%player_displayname%</white>"
            );
            configData.set("warn", "<#FCCF31>");
        }
        configData.options().setHeader(List.of("Process order of variables is from top to bottom."));

        ConfigUtil.save(configData, config.getValue());

        for (String key : configData.getKeys(true)) {
            if (!configData.isString(key)) continue;
            map.put(key, configData.getString(key));
        }

        papi = new PAPI() {
            @Override
            public String request(OfflinePlayer p, String s) {
                var rep = new Replacer(s, "[", "]", false);
                for (Map.Entry<String, String> stringStringEntry : map.entrySet())
                    rep.replace(stringStringEntry.getKey(), stringStringEntry.getValue());
                return DEFAULT_COLOR_API.color(PAPI.setBracket(p, PAPI.set(p, rep.toString())));
            }

            @Override
            public String request(Player p, String s) {
                return request((OfflinePlayer) p, s);
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
