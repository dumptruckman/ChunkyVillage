package org.getchunky.chunkyvillage.objects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.getchunky.chunky.Chunky;
import org.getchunky.chunky.ChunkyManager;
import org.getchunky.chunky.locale.Language;
import org.getchunky.chunky.module.ChunkyPermissions;
import org.getchunky.chunky.object.ChunkyChunk;
import org.getchunky.chunky.object.ChunkyGroup;
import org.getchunky.chunky.object.ChunkyObject;
import org.getchunky.chunky.object.ChunkyPlayer;
import org.getchunky.chunky.permission.PermissionFlag;
import org.getchunky.chunkyvillage.config.Config;
import org.getchunky.register.payment.Method;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import static org.getchunky.chunkyvillage.config.Config.Options;
import static org.getchunky.chunkyvillage.config.Config.Options.ELECTION_PERCENTAGE;
import static org.getchunky.chunkyvillage.config.Config.Options.INFLUENCE_PER_VOTE;

public class ChunkyTown extends ChunkyGroup {

    public static PermissionFlag ASSISTANT = new PermissionFlag("Assistant", "A");

    //private static String MAYOR = "mayor";

    public ChunkyGroup getAssistantGroup() {
        ChunkyGroup assistants = (ChunkyGroup)ChunkyManager.getObject(ChunkyGroup.class.getName(), getAssistantGroupId());
        if(assistants == null) {
            assistants = new ChunkyGroup();
            assistants.setId(getAssistantGroupId()).setName(getAssistantGroupId());
            HashMap<PermissionFlag, Boolean> flags = new HashMap<PermissionFlag, Boolean>();
            flags.put(ASSISTANT, true);
            ChunkyManager.setPermissions(this, assistants, flags);}
        
        return assistants;
    }

    public ChunkyGroup getResidentGroup() {
        ChunkyGroup residents = (ChunkyGroup)ChunkyManager.getObject(ChunkyGroup.class.getName(), getAssistantGroupId());
        if(residents == null) {
            residents = new ChunkyGroup();
            residents.setId(getResidentGroupId()).setName(getResidentGroupId());
        }

        return residents;
    }

    public HashSet<ChunkyObject> getAssistants() {
        HashSet<ChunkyObject> result = getAssistantGroup().getMembers().get(ChunkyPlayer.class.getName());
        if(result != null) return result;
        return new HashSet<ChunkyObject>();
    }

    public HashSet<ChunkyObject> getResidents() {
        HashSet<ChunkyObject> result = getResidentGroup().getMembers().get(ChunkyPlayer.class.getName());
        if(result != null) return result;
        return new HashSet<ChunkyObject>();
    }

    private String getAssistantGroupId() {
        return getId() + "-assistants";
    }

    private String getResidentGroupId() {
        return getId() + "-assistants";
    }

    public boolean addAssistant(ChunkyResident chunkyResident) {
        if(chunkyResident.isAssistant()) return false;
        ChunkyGroup assistants = getAssistantGroup();
        assistants.addMember(chunkyResident.getChunkyPlayer());
        chunkyResident.setTitle(Options.ASSISTANT_TITLE.getString());
        return true;
    }

    public boolean isAssistant(ChunkyResident chunkyResident) {
        return getAssistants().contains(chunkyResident.getChunkyPlayer());
    }

    public boolean removeAssistant(ChunkyResident chunkyResident) {
        if(!isAssistant(chunkyResident)) return false;
        getAssistantGroup().removeMember(chunkyResident.getChunkyPlayer());
        chunkyResident.removeTitle();
        return true;
    }

    public ChunkyResident getMayor() {
        return new ChunkyResident(this.getOwner());
    }

    public ChunkyTown setMayor(ChunkyResident mayor) {
        ChunkyObject oldOwner = this.getOwner();
        this.setOwner(mayor.getChunkyPlayer(), true, false);
        if(oldOwner != null && oldOwner instanceof ChunkyPlayer) {
            ChunkyResident oldOwnerRes = new ChunkyResident(oldOwner);
            oldOwnerRes.removeTitle();
            oldOwnerRes.save();
        }
        //mayor.getData().put(MAYOR, this.getId());
        mayor.setTitle(Options.MAYOR_TITLE.getString());
        mayor.save();
        return this;
    }

    public ChunkyTown setHome(ChunkyChunk chunk) {
        this.getData().put("home", chunk.getFullId());
        return this;
    }

    public ChunkyChunk getHome() {
        return (ChunkyChunk)ChunkyManager.getObject(this.getData().getString("home"));
    }

    public Method.MethodAccount getAccount() {
        return Chunky.getMethod().getAccount("town-" + this.getId());
    }

    /*public HashSet<ChunkyObject> getResidents() {
        HashSet<ChunkyObject> ret = new HashSet<ChunkyObject>();
        if(this.getOwnables().get(ChunkyPlayer.class.getName())!=null) ret.addAll(this.getOwnables().get(ChunkyPlayer.class.getName()));
        ret.add(getMayor().getChunkyPlayer());
        return ret;
    }*/

    public boolean deposit(ChunkyResident buyer, double amount) {
        Method.MethodAccount source = buyer.getAccount();
        if(!source.hasEnough(amount)) {
            Language.sendBad(buyer.getChunkyPlayer(),"You cannot afford " + Chunky.getMethod().format(amount));
            return false;
        }
        source.subtract(amount);
        this.getAccount().add(amount);
        this.goodMessageTown("The town has received " + Chunky.getMethod().format(amount) + " from " + buyer.getName());
        return true;
    }

    public boolean withdraw(ChunkyResident receiver, double amount) {
        if(!receiver.pay(getAccount(), amount)) {
            Language.sendBad(receiver.getChunkyPlayer(),"The town doesn't have " + Chunky.getMethod().format(amount));
            return false;}
        this.goodMessageTown(receiver.getName() + " has received " + Chunky.getMethod().format(amount) + " from the town bank.");
        return true;
    }

    public boolean isResident(ChunkyResident player) {
        if(player.getChunkyPlayer() == null) return false;
        return getResidents().contains(player.getChunkyPlayer());
    }

    public ChunkyTown addResident(ChunkyResident chunkyResident) {
        ChunkyGroup residents = getResidentGroup();
        residents.addMember(chunkyResident.getChunkyPlayer());
        if (Options.NO_PRIVATE_OWNERSHIP.getBoolean()) {
            // Transfer ownership of player's chunks to the town and give owner permissions to the player
            for (ChunkyObject obj : chunkyResident.getChunkyPlayer().getOwnables().get(ChunkyChunk.class.getName())) {
                obj.setOwner(this, true, false);
                chunkyResident.getChunkyPlayer().setPerm(obj, ChunkyPermissions.OWNER, true);
                TownChunk townChunk = new TownChunk((ChunkyChunk)obj);
                townChunk.setResidentOwner(chunkyResident);
            }
        }
        chunkyResident.setPlayTime(0);
        return this;
    }

    public ChunkyTown kickResident(ChunkyResident chunkyResident) {
        getResidentGroup().removeMember(chunkyResident.getChunkyPlayer());
        chunkyResident.removeTitle();
        return this;
    }

    public int maxChunks() {
        return (int)(getAverageInfluence() * Options.CHUNKS_PER_INFLUENCE.getDouble()) + Options.BASE_CHUNKS.getInt();
    }

    public int claimedChunkCount() {
        int i =0;
        i+= this.getOwnables().get(ChunkyChunk.class.getName()).size();
        for(ChunkyObject chunkyPlayer : getResidents()) {
            HashSet<ChunkyObject> chunks = chunkyPlayer.getOwnables().get(ChunkyChunk.class.getName());
            if(chunks != null) i+= chunks.size();
        }
        return i;
    }

    public double taxPlayers(double tax) {
        Method.MethodAccount account = getAccount();
        double sum=0;

        for(ChunkyObject chunkyObject : getResidents()) {
            ChunkyResident chunkyResident = new ChunkyResident(chunkyObject);
            Method.MethodAccount res = chunkyResident.getAccount();
            double amount = res.balance()*tax;
            sum+=amount;
            res.subtract(amount);
            Language.sendMessage(chunkyResident.getChunkyPlayer(), ChatColor.AQUA + "You paid " + Chunky.getMethod().format(amount) + " in taxes.");
        }
        account.add(sum);
        return sum;
    }

    public JSONObject getVotes() {
        if(this.getData().has("votes"))return this.getData().getJSONObject("votes");
        JSONObject jsonObject = new JSONObject();
        this.getData().put("votes",jsonObject);
        return jsonObject;
    }
    public int addVote(ChunkyResident chunkyResident, ChunkyResident candidate) {
        JSONObject votes = getVotes();
        votes.put(chunkyResident.getName(), candidate.getName());
        save();
        int i=0;
        Iterator keys = votes.keys();
        String name = candidate.getName();
        while(keys.hasNext()) {
            String key = keys.next().toString();
            if(votes.getString(key).equals(name)){
                ChunkyResident voter = new ChunkyResident(key);
                i += voter.getVotingPower();
            };}
        return i;
    }

    public void clearVotes() {
        this.getData().remove("votes");
    }

    public void printVotes(ChunkyResident chunkyResident) {
        ChunkyPlayer chunkyPlayer = chunkyResident.getChunkyPlayer();
        HashMap<String, Long> standings = new HashMap<String, Long>();
        JSONObject votes = getVotes();
        Iterator keys = votes.keys();
        int required = getTotalInfluence()/ INFLUENCE_PER_VOTE.getInt() * ELECTION_PERCENTAGE.getInt()/100;
        while (keys.hasNext()) {
            String voter = keys.next().toString();
            String candidate = null;
            candidate = votes.getString(voter);
            if(!standings.containsKey(candidate)) standings.put(candidate,0L);
            Long v = standings.get(candidate);
            v+= new ChunkyResident(voter).getVotingPower();
            standings.put(candidate,v);
        }
        Language.sendMessage(chunkyPlayer,ChatColor.GRAY + "|-------------------" +ChatColor.GREEN + "[Votes]" + ChatColor.GRAY + "-------------------|");
        Language.sendMessage(chunkyPlayer,ChatColor.GREEN + "Required to Win: " + ChatColor.YELLOW + required + " votes");
        for(String candidate : standings.keySet()) {
            Language.sendMessage(chunkyPlayer,ChatColor.GREEN + candidate + ": " + ChatColor.YELLOW + standings.get(candidate) + " votes");
        }
    }

    public void deleteTown() {
        for(HashSet<ChunkyObject> chunkyObjects : ((HashMap<String, HashSet<ChunkyObject>>)(this.getOwnables().clone())).values()) {
            for(ChunkyObject chunkyObject : (HashSet<ChunkyObject>)chunkyObjects.clone()) {
                if(chunkyObject instanceof ChunkyGroup) {
                    chunkyObject.delete();
                    continue;
                }
                chunkyObject.setOwner(null,true,false);
            }}
        //getData().remove(MAYOR);
        this.setOwner(null,false,true);
        save();
        delete();
    }

    public void goodMessageTown(String message) {
        for(Player player : Bukkit.getServer().getOnlinePlayers()) {
            ChunkyResident chunkyResident = new ChunkyResident(player);
            if(this.isResident(chunkyResident)) Language.sendGood(chunkyResident.getChunkyPlayer(),message);
        }
    }

    public int getAverageInfluence() {
        return getTotalInfluence()/getResidents().size();
    }

    public int getTotalInfluence() {
        int influence = 0;
        HashSet<ChunkyObject> residents = getResidents();
        for(ChunkyObject chunkyObject : residents) {
            influence+= new ChunkyResident(chunkyObject.getName()).getPlayTime();}
        return influence;
    }

    public enum Stance {
        ENEMY,
        NEUTRAL,
        ALLY;

        @Override
        public String toString() {
            switch (this) {
                case ENEMY:
                    return ChatColor.RED + "Enemy";
                case ALLY:
                    return ChatColor.DARK_GREEN + "Ally";
                case NEUTRAL:
                    return ChatColor.WHITE + "Neutral";
            }
            return this.name();
        }
    }

    public Stance getStanceFromString(String string) {
        for(Stance stance : Stance.values()) {
            if(stance.name().equalsIgnoreCase(string)) return stance;
        }
        return null;
    }

    public Stance setStance(ChunkyTown chunkyTown, String stance) {
        return setStance(chunkyTown, getStanceFromString(stance));
    }

    public Stance setStance(ChunkyTown chunkyTown, Stance stance) {
        if(getStance(chunkyTown).equals(stance)) return null;
        getDiplomacy().put(chunkyTown.getId(),stance.name());
        save();
        return stance;
    }

    public Stance getStance(ChunkyTown chunkyTown) {
        if(chunkyTown == this) return Stance.ALLY;
        if(chunkyTown == null) return Stance.NEUTRAL;
        if(getDiplomacy().has(chunkyTown.getId())) return getStanceFromString(getDiplomacy().getString(chunkyTown.getId()));
        return Stance.NEUTRAL;
    }


    public Stance getEffectiveStance(ChunkyTown otherTown) {
        if(otherTown == this) return Stance.ALLY;
        if(otherTown == null) return Stance.NEUTRAL;
        Stance myStance = getStance(otherTown);
        Stance theirStance = otherTown.getStance(this);
        if(myStance.equals(Stance.ENEMY) || theirStance.equals(Stance.ENEMY)) return Stance.ENEMY;
        if(myStance.equals(Stance.ALLY) && theirStance.equals(Stance.ALLY)) return Stance.ALLY;
        return Stance.NEUTRAL;
    }

    public JSONObject getDiplomacy() {
        JSONObject ret = this.getData().optJSONObject("diplomacy");
        if(ret==null) {
            ret =  new JSONObject();
            this.getData().put("diplomacy",ret);
        }
        return ret;
    }


}

