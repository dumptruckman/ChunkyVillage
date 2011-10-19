package org.getchunky.chunkyvillage;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkitstats.CallHome;
import org.getchunky.chunky.Chunky;
import org.getchunky.chunky.ChunkyManager;
import org.getchunky.chunky.event.ChunkyEvent;
import org.getchunky.chunky.exceptions.ChunkyUnregisteredException;
import org.getchunky.chunky.locale.Language;
import org.getchunky.chunky.module.ChunkyCommand;
import org.getchunky.chunky.module.ChunkyPermissions;
import org.getchunky.chunkyvillage.commands.Admin.Admin;
import org.getchunky.chunkyvillage.commands.Admin.AdminAddResident;
import org.getchunky.chunkyvillage.commands.Admin.AdminKickResident;
import org.getchunky.chunkyvillage.commands.Resident.Resident;
import org.getchunky.chunkyvillage.commands.Town.List;
import org.getchunky.chunkyvillage.commands.Town.*;
import org.getchunky.chunkyvillage.commands.Town.set.*;
import org.getchunky.chunkyvillage.commands.Town.toggle.Toggle;
import org.getchunky.chunkyvillage.commands.Town.toggle.ToggleAssistant;
import org.getchunky.chunkyvillage.commands.Town.toggle.ToggleTownChat;
import org.getchunky.chunkyvillage.config.Config;
import org.getchunky.chunkyvillage.listeners.ChunkyEvents;
import org.getchunky.chunkyvillage.listeners.PlayerEvents;
import org.getchunky.chunkyvillage.locale.Strings;
import org.getchunky.chunkyvillage.objects.ChunkyTown;
import org.getchunky.chunkyvillage.util.Updater;

public class ChunkyVillage extends JavaPlugin {

    private ChunkyEvents chunkyEvents;
    private PlayerEvents playerEvents;

    private static Plugin plugin;

    public static Plugin getInstance() {
        return plugin;
    }

    public void onDisable() {
        Updater.updateCheck("http://build.blockface.org/job/ChunkyVillage/lastSuccessfulBuild/artifact/target/ChunkyVillage-SNAPSHOT.jar");
        System.out.println(this + " is now disabled!");
    }

    public void onEnable() {
        plugin = this;

        CallHome.load(this);
        if(Updater.updateCheck("http://build.blockface.org/job/ChunkyVillage/lastSuccessfulBuild/artifact/target/ChunkyVillage-SNAPSHOT.jar")) {
            Bukkit.getServer().shutdown();}

        Config.load();

        Strings.load();

        Chunky.getModuleManager().registerModule(this);

        chunkyEvents = new ChunkyEvents();
        Chunky.getModuleManager().registerEvent(ChunkyEvent.Type.PLAYER_CHUNK_CLAIM, chunkyEvents, ChunkyEvent.Priority.Normal,this);
        Chunky.getModuleManager().registerEvent(ChunkyEvent.Type.PLAYER_CHUNK_CHANGE, chunkyEvents, ChunkyEvent.Priority.Normal,this);
        Chunky.getModuleManager().registerEvent(ChunkyEvent.Type.PLAYER_BUILD, chunkyEvents, ChunkyEvent.Priority.Normal,this);

        PluginManager pm = this.getServer().getPluginManager();

        playerEvents = new PlayerEvents();

        pm.registerEvent(Event.Type.PLAYER_RESPAWN, playerEvents, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_JOIN, playerEvents, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_QUIT, playerEvents, Event.Priority.Normal, this);
        pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerEvents, Event.Priority.Highest, this);
        pm.registerEvent(Event.Type.PLAYER_CHAT, playerEvents, Event.Priority.Low, this);



        registerChunkyCommands();

        System.out.println(this + " is now enabled!");
    }


    private void registerChunkyCommands(){
        ChunkyCommand root = Chunky.getModuleManager().getCommandByName("chunky");
        try {
            ChunkyCommand town = new ChunkyCommand("town",new Town(),null).setAliases("t").setHelpLines("/town or /t").setDescription("Shows information about town");

            ChunkyCommand list = new ChunkyCommand("list",new List(),town).setAliases("l").setHelpLines("/town list or /t l").setDescription("Lists towns.");

            ChunkyCommand newTown = new ChunkyCommand("new",new NewTown(),town).setAliases("n").setHelpLines("/town new <name> or /t n <name>").setDescription("Create a new town.");

            ChunkyCommand forSale = new ChunkyCommand("forsale",new ForSale(),town).setAliases("fs").setHelpLines("/town forsale <cost> or /t fs <cost>").setDescription("Set plot for sale.");

            ChunkyCommand notForSale = new ChunkyCommand("notforsale",new NotForSale(),town).setAliases("nfs").setHelpLines("/town notforsale or /t nfs").setDescription("Removes a plot from sale");

            ChunkyCommand addResident = new ChunkyCommand("add",new AddResident(),town).setAliases("a").setDescription("Adds resident to town.").setHelpLines("/town add <player> or /t k <player>");

            ChunkyCommand kickResident = new ChunkyCommand("kick",new KickResident(),town).setAliases("k").setDescription("Removes resident from town.").setHelpLines("/town kick <player> or /t r <player>");

            ChunkyCommand tax = new ChunkyCommand("tax",new Tax(),town).setAliases("tax").setDescription("Taxes all residents.").setHelpLines("/town tax <1-100> or /t tax <1-100>");

            ChunkyCommand vote = new ChunkyCommand("vote",new Vote(),town).setAliases("v").setDescription("Votes for a mayor.").setHelpLines("/town vote <player> or /t v <player>");

            ChunkyCommand spawn = new ChunkyCommand("spawn",new Spawn(),town).setAliases("s").setDescription("Teleport to town.").setHelpLines("/town spawn or /t s");

            ChunkyCommand leave = new ChunkyCommand("leave",new Leave(),town).setAliases("lv").setDescription("Leave the town").setHelpLines("/town leave or /t lv");

            ChunkyCommand withdraw = new ChunkyCommand("withdraw",new Withdraw(),town).setAliases("w").setDescription("Withdraw money from town bank.").setHelpLines("/town withdraw <amount> or /t w <amount>");

            ChunkyCommand deposit = new ChunkyCommand("deposit",new Deposit(),town).setAliases("d").setDescription("Deposit money into town bank.").setHelpLines("/town deposit <amount> or /t d <amount>");

            ChunkyCommand set = new ChunkyCommand("set",new Set(),town).setAliases("st").setDescription("Set various options.").setHelpLines("/town set ? or /t s ?");

            ChunkyCommand setName = new ChunkyCommand("name",new SetName(),set).setAliases("n").setDescription("Set town name.").setHelpLines("/town set name <name> or /t st n <name>");

            ChunkyCommand setMayor = new ChunkyCommand("mayor",new SetMayor(),set).setAliases("m").setDescription("Set town mayor.").setHelpLines("/town set mayor <name> or /t st m <name>");

            ChunkyCommand setStance = new ChunkyCommand("stance",new SetStance(),set).setAliases("s").setDescription("Set town's stance.").setHelpLines("/town set stace <name> <neutral|enemy|ally> or /t st s <neutral|enemy|ally>");

            ChunkyCommand setTitle = new ChunkyCommand("title",new SetTitle(),set).setAliases("t").setDescription("Set a resident's or your own title.").setHelpLines("/town set title [name] <title> or /t st t [name] <title>");

            ChunkyCommand toggle = new ChunkyCommand("toggle",new Toggle(),town).setAliases("t").setDescription("Toggle various options.").setHelpLines("/town toggle ? or /t t ?");

            ChunkyCommand toggleAssistant = new ChunkyCommand("assistant",new ToggleAssistant(),toggle).setAliases("a").setDescription("Toggles an assistant.").setHelpLines("/town toggle assistant <name> or /t t a <name>");

            ChunkyCommand toggleTownChat = new ChunkyCommand("townchat",new ToggleTownChat(),toggle).setAliases("tc").setDescription("Toggles town chat.").setHelpLines("/town toggle townchat or /t t tc");

            ChunkyCommand delete = new ChunkyCommand("delete",new Delete(),town).setAliases("del").setDescription("Deletes town.").setHelpLines("/town delete or /t del");

            ChunkyCommand resident = new ChunkyCommand("resident",new Resident(),null).setAliases("r","res").setHelpLines("/resident [player] or /res [player]").setDescription("Shows information about resident");

            ChunkyCommand admin = new ChunkyCommand("admin",new Admin(),null).setAliases("admin").setHelpLines("/admin ?").setDescription("Admin commands to manage towns.");

            ChunkyCommand adminAddResident = new ChunkyCommand("add", new AdminAddResident(),admin).setAliases("a").setDescription("Adds resident to town.").setHelpLines("/admin add <town> <player> or /a a <town> <player>");

            ChunkyCommand adminKickResident = new ChunkyCommand("kick", new AdminKickResident(),admin).setAliases("k").setDescription("Kicks resident from town.").setHelpLines("/admin kick <player> or /a k <player>");

            //Town Commands
            Chunky.getModuleManager().registerCommand(town);
            Chunky.getModuleManager().registerCommand(newTown);
            Chunky.getModuleManager().registerCommand(delete);
            Chunky.getModuleManager().registerCommand(vote);
            Chunky.getModuleManager().registerCommand(list);
            Chunky.getModuleManager().registerCommand(spawn);
            Chunky.getModuleManager().registerCommand(leave);
            Chunky.getModuleManager().registerCommand(withdraw);
            Chunky.getModuleManager().registerCommand(deposit);
            Chunky.getModuleManager().registerCommand(forSale);
            Chunky.getModuleManager().registerCommand(notForSale);
            Chunky.getModuleManager().registerCommand(tax);
            Chunky.getModuleManager().registerCommand(addResident);
            Chunky.getModuleManager().registerCommand(kickResident);
            Chunky.getModuleManager().registerCommand(set);
            Chunky.getModuleManager().registerCommand(setName);
            Chunky.getModuleManager().registerCommand(setMayor);
            Chunky.getModuleManager().registerCommand(setStance);
            Chunky.getModuleManager().registerCommand(setTitle);
            Chunky.getModuleManager().registerCommand(toggle);
            Chunky.getModuleManager().registerCommand(toggleAssistant);
            Chunky.getModuleManager().registerCommand(toggleTownChat);

            //Resident Commands
            Chunky.getModuleManager().registerCommand(resident);

            //Admin Commands
            Chunky.getModuleManager().registerCommand(admin);
            Chunky.getModuleManager().registerCommand(adminAddResident);
            Chunky.getModuleManager().registerCommand(adminKickResident);

        } catch (ChunkyUnregisteredException e) {
            e.printStackTrace();
        }
    }

    public void registerChunkyPermissions() {
        ChunkyPermissions.registerPermissionFlag(ChunkyTown.ASSISTANT);
    }
}
