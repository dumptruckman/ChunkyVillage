package org.getchunky.chunkyvillage.objects;

import org.getchunky.chunky.locale.Language;
import org.getchunky.chunky.object.ChunkyChunk;
import org.getchunky.chunky.object.ChunkyObject;
import org.getchunky.chunky.object.ChunkyPlayer;
import org.getchunky.chunky.util.Logging;
import org.getchunky.chunkyvillage.ChunkyTownManager;
import org.json.JSONObject;

public class TownChunk {

    private ChunkyChunk chunkyChunk;

    private static String COST = "cost";
    private static String OWNER = "resident owner";

    public TownChunk(ChunkyChunk chunkyChunk) {
        this.chunkyChunk = chunkyChunk;
    }

    public ChunkyTown getTown() {
        if(getChunkyChunk().getOwner() == null) return null;
        if(getChunkyChunk().getOwner() instanceof ChunkyTown) return (ChunkyTown)chunkyChunk.getOwner();
        return null;
    }

    public ChunkyChunk getChunkyChunk() {
        return chunkyChunk;
    }

    public JSONObject getData() {
        JSONObject data = this.getChunkyChunk().getData().optJSONObject("ChunkyVillage");
        if (data == null) {
            data = new JSONObject();
            this.getChunkyChunk().getData().put("ChunkyVillage", data);
        }
        return data;
    }

    public TownChunk save() {
        getChunkyChunk().save();
        return this;
    }

    public TownChunk setResidentOwner(ChunkyResident owner) {
        ChunkyResident prevOwner = getResidentOwner();
        if (prevOwner != null)
            if (!prevOwner.removeOwnedTownChunk(this))
                Logging.warning("Tried to remove chunk '" + this.getChunkyChunk().getId() + "' from '" + prevOwner.getName() + "' but they did not own it!");
        if (owner == null) getData().remove(OWNER);
        else {
            getData().put(OWNER, owner.getChunkyPlayer().getName());
            owner.addOwnedTownChunk(this);
        }
        save();
        return this;
    }

    public Boolean hasResidentOwner() {
        return getData().has(OWNER);
    }

    public ChunkyResident getResidentOwner() {
        String name = getData().optString(OWNER);
        if (name.isEmpty()) return null;
        return new ChunkyResident(name);
    }

    public void setForSale(double cost) {
        getData().put(COST, cost);
        save();
    }

    public void setNotForSale() {
        getData().remove(COST);
        save();
    }

    public boolean isForSale() {
        return getData().has(COST);
    }

    public double getCost() {
        return getData().getDouble(COST);
    }

    public boolean buyChunk(ChunkyResident buyer) {
        if (!hasResidentOwner()) {
            ChunkyTown chunkyTown = this.getTown();
            if(!chunkyTown.deposit(buyer, getCost())) return false;
            getChunkyChunk().setOwner(buyer.getChunkyPlayer(), false, true);
            setNotForSale();
            Language.sendGood(buyer.getChunkyPlayer(), "You have purchased this chunk!");
            return true;
        } else {
            ChunkyResident seller = getResidentOwner();
            if(!seller.pay(buyer.getAccount(), getCost())) return false;
            getChunkyChunk().setOwner(buyer.getChunkyPlayer(), false, true);
            setNotForSale();
            Language.sendGood(buyer.getChunkyPlayer(),"You have purchased this chunk!");
            return true;
        }
    }
}
