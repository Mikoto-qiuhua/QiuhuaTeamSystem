package org.qiuhua.qiuhuateamsystem.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.qiuhua.qiuhuateamsystem.playerdata.PlayerData;
import org.qiuhua.qiuhuateamsystem.playerdata.PlayerDataManager;
import org.qiuhua.qiuhuateamsystem.team.TeamInfoData;
import org.qiuhua.qiuhuateamsystem.team.TeamManager;

public class PlayerListener implements Listener {
    //玩家离开服务器清理数据
    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event){
        Player player = event.getPlayer();
        //获取玩家队伍
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        if(playerData.getTeamId() != null){
            TeamInfoData teamInfoData = TeamManager.getTeams(player);
            if(teamInfoData != null){
                //如果玩家是队长
                if(teamInfoData.isTeamOwner(player.getUniqueId())){
                    //解散队伍
                    teamInfoData.dissolveTeam();
                }else {
                    //退出队伍
                    teamInfoData.leaveTeam(player);
                }
            }
        }
        PlayerDataManager.allPlayerData.remove(player.getUniqueId());
    }
}
