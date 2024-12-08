package org.qiuhua.qiuhuateamsystem.playerdata;

import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    //记录玩家数据
    public static final ConcurrentHashMap<UUID, PlayerData> allPlayerData = new ConcurrentHashMap<>();

    //获取玩家数据 没有就创建
    public static PlayerData getPlayerData(Player player){
        UUID uuid = player.getUniqueId();
        if(!allPlayerData.containsKey(uuid)){
            allPlayerData.put(uuid, new PlayerData());
        }
        return allPlayerData.get(uuid);
    }

    public static PlayerData getPlayerData(UUID uuid){
        if(!allPlayerData.containsKey(uuid)){
            allPlayerData.put(uuid, new PlayerData());
        }
        return allPlayerData.get(uuid);
    }




}
