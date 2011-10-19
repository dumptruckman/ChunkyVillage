package org.getchunky.chunkyvillage;

import org.bukkit.entity.Player;
import org.getchunky.chunky.Chunky;
import org.getchunky.chunky.ChunkyManager;
import org.getchunky.chunky.object.ChunkyObject;
import org.getchunky.chunky.object.ChunkyPlayer;
import org.getchunky.chunkyvillage.objects.ChunkyResident;
import org.getchunky.chunkyvillage.objects.ChunkyTown;
import org.getchunky.register.payment.Method;
import org.json.JSONException;

import java.util.HashMap;

public class ChunkyTownManager {

    public static ChunkyTown getTown(String name) {
        return (ChunkyTown)ChunkyManager.getObject(ChunkyTown.class.getName(),name);
    }

    public static ChunkyTown getTown(Player player) {
        return getTown(ChunkyManager.getChunkyPlayer(player));
    }

    public static ChunkyTown getTown(ChunkyPlayer chunkyPlayer) {
        /**if(chunkyPlayer.getOwner() != null && chunkyPlayer.getOwner() instanceof ChunkyTown) return (ChunkyTown)chunkyPlayer.getOwner();
        ChunkyTown chunkyTown = isMayor(chunkyPlayer);
        if(chunkyTown != null) return chunkyTown;
        return null;**/
        return new ChunkyResident(chunkyPlayer).getTown();
    }

    public static ChunkyTown matchTown(String input) {
        String n = input.toLowerCase();
        for(ChunkyObject chunkyObject : ChunkyTownManager.getTowns().values()) {
            if(chunkyObject.getName().toLowerCase().contains(n)) return (ChunkyTown)chunkyObject;}
        return null;
    }

    public static Method.MethodAccount getAccount(ChunkyObject chunkyObject) {
        return Chunky.getMethod().getAccount(chunkyObject.getName());
    }

    public static HashMap<String, ChunkyObject> getTowns() {
        return ChunkyManager.getObjectsOfType(ChunkyTown.class.getName());
    }

    public static ChunkyTown.Stance getStance(ChunkyPlayer a, ChunkyPlayer b) {
        ChunkyTown aTown = getTown(a);
        ChunkyTown bTown = getTown(b);
        if(aTown == null || bTown == null) return ChunkyTown.Stance.NEUTRAL;
        return aTown.getEffectiveStance(bTown);
    }


}
