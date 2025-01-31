package org.tjdev.custom.globalplaceholders;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.tjdev.util.colorful.api.defaults.MiniMessage;
import org.tjdev.util.tjpluginutil.ConfigUtil;
import org.tjdev.util.tjpluginutil.config.file.YamlConfig;
import org.tjdev.util.tjpluginutil.spigot.locale.LangDownload;
import org.tjdev.util.tjpluginutil.spigot.object.TJPlugin;
import org.tjdev.util.tjpluginutil.spigot.plugin.papi.PAPI;
import org.tjdev.util.tjpluginutil.text.Replacer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.kyori.adventure.text.minimessage.MiniMessage.miniMessage;
import static org.tjdev.util.tjpluginutil.ConfigUtil.get;
import static org.tjdev.util.tjpluginutil.IConstant.DEFAULT_COLOR_API;

public final class GlobalPlaceholders extends TJPlugin {
    private PAPI papi;

    @Override
    public void enable() {
        DEFAULT_COLOR_API = new MiniMessage();

        var map = new LinkedHashMap<String, String>();

        var config = ConfigUtil.prepareResult(getDataFolder(), "config");
        var configData = YamlConfig.loadConfiguration(config.getValue());

        var lang = get(configData, "lang", "en_us");
        configData.setComments(
                "lang",
                List.of("If you updated this, please delete the `entity.yml` and `item.yml` in the plugin directory for re-downloading.")
        );

        var replacement = configData.getConfigurationSection("replacement");
        if (replacement == null) {
            replacement = configData.createSection("replacement");
            replacement.set(
                    "player",
                    "<gray>[<white>{vault_prefix}</white>]</gray> [wonderful-color]%player_displayname%"
            );
            replacement.set("warn", "<#FCCF31>");
            replacement.set("wonderful-color", "<#32CCBC>");
        }
        configData.setComments(
                "replacement",
                List.of(
                        "Process order of variables is from top to bottom.",
                        "For example, parse `%globalplaceholders_[player]%` with `Alex_TangJin_TW (prefix ADMIN)` will be the following:",
                        "  1. Replace [player] => `[player]` -> `<gray>[<white>{vault_prefix}</white>]</gray> [wonderful-color]%player_displayname%`",
                        "  2. Replace [wonder-color] => `<gray>[<white>{vault_prefix}</white>]</gray> [wonderful-color]%player_displayname%` -> `<gray>[<white>{vault_prefix}</white>]</gray> <#32CCBC>%player_displayname%`",
                        "  3. Apply placeholders => `<gray>[<white>{vault_prefix}</white>]</gray> <#32CCBC>%player_displayname%` -> `<gray>[<white>ADMIN</white>]</gray> <#32CCBC>Alex_TangJin_TW`",
                        "  4. Color text with MiniMessage..."
                )
        );

        ConfigUtil.save(configData, config.getValue());

        LangDownload.init(lang, () -> {
            var entity = ConfigUtil.prepare(getDataFolder(), "entity");
            var entityData = YamlConfig.loadConfiguration(entity);
            for (EntityType value : EntityType.values()) {
                if (value == EntityType.UNKNOWN) continue;
                map.put(
                        "entity." + value.name(),
                        get(entityData, value.name(), LangDownload.get(value.translationKey()))
                );
            }
            entityData.options().setHeader(
                    List.of(
                            "%globalplaceholders_[entity.ZOMBIE]% -> Zombie",
                            "You can even add custom colors or some style like `ᴢᴏᴍʙɪᴇ`!"
                    )
            );
            ConfigUtil.save(entityData, entity);

            var item = ConfigUtil.prepare(getDataFolder(), "item");
            var itemData = YamlConfig.loadConfiguration(item);
            for (Material value : Material.values())
                map.put("item." + value.name(), get(itemData, value.name(), LangDownload.get(value.translationKey())));
            itemData.options().setHeader(List.of(
                    "%globalplaceholders_[item.DIAMOND_ORE]% -> Diamond Ore",
                    "Same with above."
            ));
            ConfigUtil.save(itemData, item);

            getLogger().info("Extra translation data has been loaded.");
        });

        for (String key : replacement.getKeys(true)) {
            if (!replacement.isString(key)) continue;
            map.put(key, replacement.getString(key));
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
