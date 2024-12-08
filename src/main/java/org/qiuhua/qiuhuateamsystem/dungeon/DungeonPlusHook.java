package org.qiuhua.qiuhuateamsystem.dungeon;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.qiuhua.qiuhuateamsystem.dungeonsInfo.DungeonsInfoData;
import org.qiuhua.qiuhuateamsystem.dungeonsInfo.DungeonsInfoManager;
import org.qiuhua.qiuhuateamsystem.file.FileManager;
import org.qiuhua.qiuhuateamsystem.team.TeamInfoData;
import org.serverct.ersha.dungeon.DungeonPlus;
import org.serverct.ersha.dungeon.common.dungeon.DungeonContent;
import org.serverct.ersha.dungeon.common.team.Team;
import org.serverct.ersha.dungeon.common.team.type.TeamOperationType;
import org.serverct.ersha.dungeon.internal.dungeon.Dungeon;
import org.serverct.ersha.dungeon.internal.manager.TeamManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DungeonPlusHook {
    public static void star(Player player, TeamInfoData teamInfoData){
        ConcurrentHashMap<UUID, Boolean> teamPlayer = teamInfoData.getTeamPlayer();
        DungeonsInfoData dungeonsInfoData = DungeonsInfoManager.getDungeonsInfoData(teamInfoData.getDungeonsName());
        if(dungeonsInfoData == null){
            return;
        }
        String dungeonsName = dungeonsInfoData.getName();
        for(UUID uuid : teamPlayer.keySet()){
            Player player1 = Bukkit.getPlayer(uuid);
            if(player1 == null){
                continue;
            }
            if(!teamPlayer.get(uuid)){
                String meg = FileManager.message.getString("system_PlayersAreNotPrepared", "");
                if(!meg.isEmpty()){
                    teamInfoData.sendSystemMessage(meg.replaceAll("<player_name>", player1.getName()));
                }
                return;
            }

            //检查全队条件
            if(!dungeonsInfoData.spawnStarConditionResults(player1)){
                String meg = dungeonsInfoData.getMegCondition();
                if(meg != null && !meg.isEmpty()){
                    teamInfoData.sendSystemMessage(FileManager.getPapiString(player1, meg));
                }
                return;
            }
        }
        //创建队伍列表 拉玩家进入地牢
        List<UUID> players = new ArrayList<>(teamPlayer.keySet());
        Team team = new TeamManager().getTeam(player);
        if(team != null){
            team.playerOperation(player, TeamOperationType.DISBAND);
        }
        team = Team.Companion.create(player, players);

        DungeonContent dungeonContent = DungeonPlus.contentManager.getContent(dungeonsName, player);
        Dungeon builder = Dungeon.Companion.builder(
                team, dungeonContent, new String[]{}, false
        );
        assert builder != null;
        DungeonPlus.dungeonManager.addDungeon(builder);
        builder.start(false);
        //将玩家的准备都取消
        for(UUID uuid : teamPlayer.keySet()){
            //如果该玩家不是这个地牢的创建者 就取消他的准备 正常来说 地牢创建者是队伍队长
            if(uuid != player.getUniqueId()){
                teamPlayer.put(uuid, false);
            }
        }
    }
}
