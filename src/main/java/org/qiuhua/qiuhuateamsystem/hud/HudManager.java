package org.qiuhua.qiuhuateamsystem.hud;

import com.daxton.unrealcore.application.UnrealCoreAPI;
import com.daxton.unrealcore.application.method.SchedulerFunction;
import com.daxton.unrealcore.been.common.been.SlotClickBeen;
import com.daxton.unrealcore.display.been.module.ModuleData;
import com.daxton.unrealcore.display.been.module.display.TextModuleData;
import com.daxton.unrealcore.display.been.module.display.VideoModuleData;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.qiuhua.UnrealGUIPro.Main;
import org.qiuhua.UnrealGUIPro.Tool;
import org.qiuhua.UnrealGUIPro.application.PlayerFunction;
import org.qiuhua.UnrealGUIPro.gui.GUIController;
import org.qiuhua.UnrealGUIPro.gui.UnrealGUIContainer;
import org.qiuhua.UnrealGUIPro.videoPlay.VideoPlay;
import org.qiuhua.qiuhuateamsystem.file.FileManager;
import org.qiuhua.qiuhuateamsystem.gui.GuiManager;
import org.qiuhua.qiuhuateamsystem.team.TeamInfoData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HudManager {
    private static List<ModuleData> hudId;

    public static void loadHud(){
        for (Player player : Bukkit.getOnlinePlayers()) {
            UnrealCoreAPI.removeHUD(player, hudId);
        }
        FileConfiguration fileConfiguration = FileManager.config;
        hudId = UnrealCoreAPI.getHUDList("InviteSettings.tip.", fileConfiguration);

    }


    //给玩家发送一个hud 给玩家和队伍信息
    public static void sendHud(Player player, TeamInfoData teamInfoData){
        FileConfiguration fileConfiguration = FileManager.config;
        List<ModuleData> hudDataConfigList = UnrealCoreAPI.getHUDList("InviteSettings.tip.", fileConfiguration);
        if(teamInfoData != null){
            //获取自定义文本组件 进行修改
            for(ModuleData moduleData : hudDataConfigList){
                if(moduleData.getModuleID().equals("组队邀请自定义文本")){
                    TextModuleData textModule = (TextModuleData) moduleData;
                    List<String> textList = textModule.getText();
                    //处理队伍内置占位符
                    Map<String, String> replacements = new HashMap<>();
                    replacements.put("<副本名字>", teamInfoData.getDungeonsName());
                    replacements.put("<队伍名字>", teamInfoData.getTeamName());
                    replacements.put("<队伍ID>", String.valueOf(teamInfoData.getTeamId()));
                    replacements.put("<当前人数>", String.valueOf(teamInfoData.getTeamPlayer().size()));
                    replacements.put("<最大人数>", String.valueOf(FileManager.config.getInt("TeamInfo.teamSize")));
                    List<String> updatedList = textList.stream()
                            .map(s -> GuiManager.replacePlaceholders(s, replacements))
                            .toList();
                    //处理一次papi变量
                    updatedList = FileManager.getConvertPapiList(Bukkit.getPlayer(teamInfoData.getTeamOwner()), updatedList);
                    textModule.setText(updatedList);
                    break;
                }
            }
        }


        //先执行动作 在发送
        List<String> list = fileConfiguration.getStringList("InviteSettings.trigger.start");
        if (!list.isEmpty()) {
            action(player, list);
        }
        UnrealCoreAPI.setHUD(player, hudDataConfigList);
    }

    public static void removeHud(Player player){
        FileConfiguration fileConfiguration = FileManager.config;
        //先执行动作 在发送
        List<String> list = fileConfiguration.getStringList("InviteSettings.trigger.end");
        if (!list.isEmpty()) {
            action(player, list);
        }
        UnrealCoreAPI.removeHUD(player, hudId);
    }


    public static void action(Player player, List<String> list){
        for(String action: list) {
            action = Tool.getPapiString(player, action);
            String[] a = action.split("]:");
            if (a.length >= 2) {
                String type = a[0];
                String content = a[1].trim();
                switch (type) {
                    case "[console":
                        SchedulerFunction.run(Main.getMainPlugin(), ()->{
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), content);
                        });
                        break;
                    case "[op":
                        SchedulerFunction.run(Main.getMainPlugin(), ()->{
                            boolean originalOpStatus = player.isOp();
                            player.setOp(true);
                            player.performCommand(content);
                            player.setOp(originalOpStatus);
                        });
                        break;
                    case "[tell":
                        player.sendMessage(content);
                        break;
                    case "[player":
                        SchedulerFunction.run(Main.getMainPlugin(), ()->{
                            player.performCommand(content);
                        });
                        break;
                    case "[chat":
                        SchedulerFunction.run(Main.getMainPlugin(), ()->{
                            player.chat(content);
                        });
                        break;
                    case "[effects":
                        String[] effects = content.split(",");
                        Particle particleType = Particle.valueOf(effects[0]);
                        int particleCount = Integer.parseInt(effects[1]);
                        double particleSpeed = Double.parseDouble(effects[2]);
                        double x = Double.parseDouble(effects[3]);
                        double y = Double.parseDouble(effects[4]);
                        double z = Double.parseDouble(effects[5]);
                        player.spawnParticle(particleType, player.getLocation().clone().add(0, 0.5, 0), particleCount, x, y, z, particleSpeed);
                        break;
                    case "[sound":
                        String[] sound = content.split(",");
                        Sound soundType = Sound.valueOf(sound[0]);
                        float volume = Float.parseFloat(sound[2]); // 音量
                        float pitch = Float.parseFloat(sound[1]); // 音调
                        player.playSound(player.getLocation(), soundType, volume, pitch);
                        break;
                    case "[transitionValue":
                        PlayerFunction.setTransition(player, content);
                        break;
                    case "[transitionValueRemove":
                        PlayerFunction.removeTransition(player, content);
                    case "[toGui":
                        GUIController.open(player, content);
                        break;

                }

            }

        }
    }

}
