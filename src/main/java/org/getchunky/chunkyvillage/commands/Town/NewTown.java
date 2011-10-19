package org.getchunky.chunkyvillage.commands.Town;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.getchunky.chunky.ChunkyManager;
import org.getchunky.chunky.locale.Language;
import org.getchunky.chunky.module.ChunkyCommand;
import org.getchunky.chunky.module.ChunkyCommandExecutor;
import org.getchunky.chunky.object.ChunkyChunk;
import org.getchunky.chunky.object.ChunkyPlayer;
import org.getchunky.chunkyvillage.ChunkyTownManager;
import org.getchunky.chunkyvillage.objects.ChunkyResident;
import org.getchunky.chunkyvillage.objects.ChunkyTown;
import org.getchunky.chunkyvillage.permissions.Permissions;

public class NewTown implements ChunkyCommandExecutor{

    public void onCommand(CommandSender sender, ChunkyCommand chunkyCommand, String s, String[] strings) {
        if(!(sender instanceof Player)) {
            Language.IN_GAME_ONLY.bad(sender);
            return;
        }
        if(!Permissions.CREATE_TOWN.has(sender)) {
            Language.NO_COMMAND_PERMISSION.bad(sender);
            return;
        }
        Player player = (Player)sender;
        ChunkyResident chunkyResident = new ChunkyResident(player);
        if(chunkyResident.getTown() != null) {
            Language.sendBad(chunkyResident.getChunkyPlayer(), "You are already part of a town.");
            return;
        }
        if(strings.length < 1) {
            Language.sendBad(chunkyResident.getChunkyPlayer(),"Please specify the town name.");
            return;
        }
        ChunkyChunk chunkyChunk = chunkyResident.getChunkyPlayer().getCurrentChunk();
        if(chunkyChunk.isOwned()) {
            Language.sendBad(chunkyResident.getChunkyPlayer(),"This chunk is already owned.");
            return;
        }
        String id = strings[0];
        ChunkyTown chunkyTown = new ChunkyTown();
        chunkyTown.setId(ChunkyManager.getUniqueId());
        chunkyTown.setMayor(chunkyResident).setHome(chunkyChunk).addResident(chunkyResident).setName(id);
        chunkyChunk.setOwner(chunkyTown, true,true);
        chunkyChunk.save();
        Language.sendGood(chunkyResident.getChunkyPlayer(),"You've just created a town called " + chunkyTown.getName());

    }
}
