package org.getchunky.chunkyvillage.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.config.Configuration;
import org.getchunky.chunky.util.Logging;
import org.getchunky.chunkyvillage.ChunkyVillage;

import java.util.HashMap;

public class Config {
    private static Configuration configuration;
    private static YamlConfiguration yml;

    public enum Options {

        CHUNKS_PER_INFLUENCE("settings.town.chunksPerInfluence", 0.5),
        BASE_CHUNKS("settings.town.baseChunks", 10),
        ELECTION_PERCENTAGE("settings.town.electionPercentage", 50),
        INFLUENCE_PER_VOTE("settings.town.influencePerVote", 30),
        DEATH_TOLL("settings.resident.influenceLostOnDeath", 10),
        TOWN_CHAT_FORMAT("settings.town.townChatFormat", "&3[%town%]&f %displayname%: &3%msg%"),
        MAYOR_TITLE("settings.town.defaultMayorTitle", "&6Mayor&f"),
        ASSISTANT_TITLE("settings.town.defaultAssistantTitle", "&5Assistant&f"),
        TELEPORT_WARMUP("settings.town.teleportWarmup", 10),
        NO_PRIVATE_OWNERSHIP("setting.town.residents.no_private_ownership", true),
        LANGUAGE_FILE("settings.language", "english.yml"),

        AUTOUPDATE("settings.autoUpdate", true),
        TNT_COST("settings.war.tools.46", 60),
        LADDER_COST("settings.war.tools.65",10)
        ;

        private String path;
        private Object def;

        Options(String path, Object def) {
            this.path = path;
            this.def = def;
        }

        public Object get() {
            if(configuration.getProperty(path) == null) configuration.setProperty(path, def);
            return configuration.getProperty(path);
        }

        public String getString() {
            return configuration.getString(path, def.toString());}

        public int getInt() {
            return configuration.getInt(path, (Integer)def);}

        public double getDouble() {
            return configuration.getDouble(path, (Double)def);}

        public Boolean getBoolean() {
            return configuration.getBoolean(path, (Boolean)def);
        }
    }

    public static void load() {
        if(!ChunkyVillage.getInstance().getDataFolder().exists()) ChunkyVillage.getInstance().getDataFolder().mkdir();
        configuration = ChunkyVillage.getInstance().getConfiguration();
        configuration.load();
        loadDefaults();
        configuration.save();
    }


    private static void loadDefaults() {
        for(Options option : Options.values()) {
            option.get();}
        loadWarTools();
    }

    private static HashMap<Integer, Integer> warTools = new HashMap<Integer, Integer>();

    private static void loadWarTools() {
        for(String s : configuration.getKeys("settings.war.tools")) {
            try {
                int pos = s.lastIndexOf(".") + 1;
                int id = Integer.parseInt(s.substring(pos, s.length() - pos));
                Logging.info("Added: " + id);
                warTools.put(id, configuration.getInt("settings.war.tools."+s, 0));
            } catch (Exception ex) {}
        }
    }

    public static boolean isWarTool(Integer id) {
        return warTools.containsKey(id);
    }

    public static int getWarToolCost(Integer id) {
        return warTools.get(id);
    }



}
