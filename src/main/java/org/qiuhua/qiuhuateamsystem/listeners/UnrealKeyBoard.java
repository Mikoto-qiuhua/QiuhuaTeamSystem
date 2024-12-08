package org.qiuhua.qiuhuateamsystem.listeners;

import com.daxton.unrealcore.common.event.PlayerKeyBoardEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.qiuhua.qiuhuateamsystem.file.FileManager;
import org.qiuhua.qiuhuateamsystem.playerdata.PlayerData;
import org.qiuhua.qiuhuateamsystem.playerdata.PlayerDataManager;
import org.qiuhua.qiuhuateamsystem.team.TeamManager;

public class UnrealKeyBoard  implements Listener {

    @EventHandler
    public void onPlayerKeyBoard(PlayerKeyBoardEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        if(playerData == null) return;
        if(playerData.getInviteTeamId() == null) return;
        //获取是否是即时输入模式 也就是在输入框中输入 是的话就结束
        if (event.isInputNow()) {
            return;
        }
        //获取动作类型 1是按下 2是按着 0是弹起
        int keyAction = event.getKeyAction();
        //如果是长按
        if(keyAction == 2){
            return;
        }
        //如果不是弹起
        if(keyAction != 0){
            return;
        }
        //获取按键
        String keyName = event.getKeyName();
        String confirmJoinKey = FileManager.config.getString("InviteSettings.confirmJoinKey");
        String rejectJoinKey = FileManager.config.getString("InviteSettings.rejectJoinKey");
        if(keyName.equals(confirmJoinKey)){
            //如果是同意加入
            TeamManager.confirmJoin(player);
            return;
        }
        if(keyName.equals(rejectJoinKey)){
            //如果是不同意
            TeamManager.rejectJoin(player);
        }


    }



}
