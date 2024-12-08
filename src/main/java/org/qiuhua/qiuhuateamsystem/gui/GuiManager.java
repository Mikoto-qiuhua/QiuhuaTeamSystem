package org.qiuhua.qiuhuateamsystem.gui;

import com.daxton.unrealcore.application.UnrealCoreAPI;
import org.bukkit.entity.Player;
import org.qiuhua.qiuhuateamsystem.Main;
import org.qiuhua.qiuhuateamsystem.file.FileManager;
import org.qiuhua.qiuhuateamsystem.playerdata.PlayerData;
import org.qiuhua.qiuhuateamsystem.playerdata.PlayerDataManager;
import org.qiuhua.qiuhuateamsystem.team.TeamInfoData;
import org.qiuhua.qiuhuateamsystem.team.TeamManager;

import java.util.Map;

public class GuiManager {

    public static void openTeamLobbyGui(Player player){
        TeamLobbyGui teamLobbyGui = new TeamLobbyGui(player);
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        playerData.setUnrealGUIContainer(teamLobbyGui);
        UnrealCoreAPI.inst(player).getGUIHelper().openCoreGUI(teamLobbyGui);
    }

    //打开自己的队伍
    public static void openTeamInfo(Player player){
        TeamInfoData teamInfoData = TeamManager.getTeams(player);
        if(teamInfoData != null){
            TeamInfoGui teamInfoGui = new TeamInfoGui(player, teamInfoData, "队伍信息");
            PlayerData playerData = PlayerDataManager.getPlayerData(player);
            playerData.setUnrealGUIContainer(teamInfoGui);
            UnrealCoreAPI.inst(player).getGUIHelper().openCoreGUI(teamInfoGui);
        }else {
            String meg = FileManager.message.getString("noTeam", "");
            if(!meg.equals("")){
                player.sendMessage(meg);
            }
        }
    }

    //观察其他队伍
    public static void observeTeamInfo(Player player, Integer teamId){
        TeamInfoData teamInfoData = TeamManager.getTeams(teamId);
        if(teamInfoData != null){
            TeamInfoGui teamInfoGui = new TeamInfoGui(player, teamInfoData, "队伍信息_观察");
            PlayerData playerData = PlayerDataManager.getPlayerData(player);
            playerData.setUnrealGUIContainer(teamInfoGui);
            UnrealCoreAPI.inst(player).getGUIHelper().openCoreGUI(teamInfoGui);
            Main.log("玩家 " + player.getName() + " 尝试查看队伍 " + teamId);
        }
    }



    // 替换占位符
    public static String replacePlaceholders(String text, Map<String, String> replacements) {
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            text = text.replace(entry.getKey(), entry.getValue());
        }
        return text;
    }

}
