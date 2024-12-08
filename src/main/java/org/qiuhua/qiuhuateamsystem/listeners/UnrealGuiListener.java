package org.qiuhua.qiuhuateamsystem.listeners;

import com.daxton.unrealcore.display.event.gui.PlayerGUICloseEvent;
import com.daxton.unrealcore.display.event.gui.PlayerGUIOpenEvent;
import com.daxton.unrealcore.display.event.gui.module.PlayerSelectEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.qiuhua.qiuhuateamsystem.Main;
import org.qiuhua.qiuhuateamsystem.playerdata.PlayerData;
import org.qiuhua.qiuhuateamsystem.playerdata.PlayerDataManager;

public class UnrealGuiListener implements Listener {
    //当玩家关闭gui的时候
    @EventHandler
    public void onPlayerGUICloseEvent(PlayerGUICloseEvent event){
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        if(event.getGuiName().equals("组队大厅") || event.getGuiName().equals("队伍信息")){
            playerData.setUnrealGUIContainer(null);
        }
        playerData.setGuiId("");
        Main.log("玩家 " + player.getName() + "关闭界面 " + event.getGuiName());
    }


    //当玩家打开gui的时候
    @EventHandler
    public void onPlayerGUIOpenEvent(PlayerGUIOpenEvent event){
        Player player = event.getPlayer();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        playerData.setGuiId(event.getGuiName());
        Main.log("玩家 " + player.getName() + "打开界面 " + event.getGuiName());
    }

}
