package org.getchunky.chunkyvillage.objects;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.getchunky.chunky.Chunky;
import org.getchunky.chunky.ChunkyManager;
import org.getchunky.chunky.exceptions.ChunkyPlayerOfflineException;
import org.getchunky.chunky.module.ChunkyPermissions;
import org.getchunky.chunky.object.ChunkyChunk;
import org.getchunky.chunky.object.ChunkyObject;
import org.getchunky.chunky.object.ChunkyPlayer;
import org.getchunky.chunkyvillage.ChunkyTownManager;
import org.getchunky.chunkyvillage.config.Config;
import org.getchunky.register.payment.Method;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;


public class ChunkyResident {

    private ChunkyPlayer chunkyPlayer = null;

    private static String LAST_JOIN = "lastJoin";
    private static String PLAY_TIME = "playTime";
    private static String TITLE = "title";
    private static String TOWN = "town";

    public ChunkyResident(ChunkyObject chunkyObject) {
        chunkyPlayer = (ChunkyPlayer)chunkyObject;
    }

    public ChunkyResident(CommandSender sender) {
        this(sender.getName());
    }

    public ChunkyResident(String player) {
        this(ChunkyManager.getChunkyPlayer(player));
    }

    public ChunkyResident(Player player) {
        this(ChunkyManager.getChunkyPlayer(player));

    }

    public ChunkyResident(ChunkyPlayer chunkyPlayer) {
        this.chunkyPlayer = chunkyPlayer;
    }

    public ChunkyTown getTown() {
        String townName = getData().optString(TOWN);
        if (townName.isEmpty()) return null;
        return ChunkyTownManager.getTown(townName);
    }

    public boolean hasTown() {
        return getData().has(TOWN);
    }

    protected ChunkyResident setTown(ChunkyTown town) {
        if (town == null) getData().remove(TOWN);
        else getData().put(TOWN, town.getName());
        return this;
    }

    public ChunkyPlayer getChunkyPlayer() {
        return this.chunkyPlayer;
    }

    public void logout() {
        setPlayTime(getPlayTime());
        getData().remove(LAST_JOIN);
        save();
    }

    public void login() {
        getData().put(LAST_JOIN, System.currentTimeMillis());
        save();
    }

    public void setPlayTime(long playTime) {
        getData().put(PLAY_TIME, playTime);
        save();
    }

    public void subtractPlayTime(long playTime) {
        long time = (getPlayTime()-Math.abs(playTime));
        setPlayTime( time < 0 ? 0 : time);
    }

    public long getVotingPower() {
        return this.getPlayTime() / Config.Options.INFLUENCE_PER_VOTE.getInt();
    }


    public long getPlayTime() {
        long curTime = System.currentTimeMillis();
        long joinTime = curTime;
        if(getData().has(LAST_JOIN)) joinTime = getData().getLong(LAST_JOIN);
        long playTime = 0;
        if(getData().has(PLAY_TIME)) playTime = getData().getLong(PLAY_TIME);
        return playTime + (curTime-joinTime)/(1000*60);

    }

    public void setTitle(String title) {
        getData().put(TITLE, title);
        save();
        applyTitle();
    }

    public void applyTitle() {
        if(!hasTitle()) return;
        try {chunkyPlayer.getPlayer().setDisplayName(getTitle() + " " + chunkyPlayer.getName());
        } catch (ChunkyPlayerOfflineException e) {}}

    public void removeTitle() {
        getData().remove(TITLE);
        save();
    }

    public boolean hasTitle() {
        return getData().has(TITLE);
    }

    public String getTitle() {
        return getData().getString(TITLE);}

    public boolean isMayor() {
        ChunkyTown chunkyTown = getTown();
        return chunkyTown != null && chunkyTown.getMayor().equals(this);
    }

    public boolean isAssistant() {
        ChunkyTown chunkyTown = getTown();
        if(chunkyTown == null) return false;
        return chunkyTown.isAssistant(this);
    }

    public boolean isAssistantOrMayor() {
        return (isMayor() || isAssistant());
    }

    public String getName() {
        return getChunkyPlayer().getName();
    }

    public Method.MethodAccount getAccount() {
        return Chunky.getMethod().getAccount(this.getName());
    }

    public boolean pay(Method.MethodAccount fromAccount, double amount) {
        if(!fromAccount.hasEnough(amount)) return false;
        fromAccount.subtract(amount);
        getAccount().add(amount);
        return true;
    }

    public boolean owns(ChunkyObject chunkyObject) {
        return getChunkyPlayer().isOwnerOf(chunkyObject) || getChunkyPlayer().hasPerm(chunkyObject, ChunkyPermissions.OWNER);
    }

    /**
     * Adds a town chunk to the residents list of owned chunks.  This is called on TownChunk.setResidentOwner()
     *
     * @param townChunk Chunk to add to "ownership"
     * @return true if the resident did not already own the chunk
     */
    protected boolean addOwnedTownChunk(TownChunk townChunk) {
        if (getOwnedTownChunks().contains(townChunk)) return false;
        getTownChunkData().put(townChunk.getChunkyChunk().getId());
        save();
        return true;
    }

    /**
     * Removes a town chunk from the residents list of owned chunks.  This is called on TownChunk.setResidentOwner()
     *
     * @param townChunk Chunky to remove from "ownership"
     * @return true if the chunk was found in the list and removed
     */
    protected boolean removeOwnedTownChunk(TownChunk townChunk) {
        for (int i = 0; i < getTownChunkData().length(); i++) {
            if (getTownChunkData().get(i).toString().equals(townChunk.getChunkyChunk().getId())) {
                getTownChunkData().remove(i);
                save();
                return true;
            }
        }
        return false;
    }

    public HashSet<TownChunk> getOwnedTownChunks() {
        HashSet<TownChunk> chunks = new HashSet<TownChunk>();
        for (int i = 0; i < getTownChunkData().length(); i++) {
            chunks.add(new TownChunk((ChunkyChunk)ChunkyManager.getObject(ChunkyChunk.class.getName(), getTownChunkData().get(i).toString())));
        }
        return chunks;
    }

    public ChunkyTown.Stance getEffectiveStance(ChunkyResident chunkyResident) {
        return ChunkyTownManager.getStance(this.getChunkyPlayer(), chunkyResident.getChunkyPlayer());
    }


    @Override
    public boolean equals(Object obj) {
        return obj instanceof ChunkyResident && ((ChunkyResident) obj).getName().equals(getName());
    }

    public JSONObject getData() {
        JSONObject data = this.getChunkyPlayer().getData().optJSONObject("ChunkyVillage");
        if (data == null) {
            data = new JSONObject();
            this.getChunkyPlayer().getData().put("ChunkyVillage", data);
        }
        return data;
    }

    public JSONArray getTownChunkData() {
        JSONArray data = this.getData().optJSONArray("Town Chunks");
        if (data == null) {
            data = new JSONArray();
            this.getChunkyPlayer().getData().put("Town Chunks", data);
        }
        return data;
    }

    public ChunkyResident save() {
        getChunkyPlayer().save();
        return this;
    }

}
