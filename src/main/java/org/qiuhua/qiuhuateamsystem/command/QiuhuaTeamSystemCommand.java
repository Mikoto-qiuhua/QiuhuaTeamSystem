package org.qiuhua.qiuhuateamsystem.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.qiuhua.qiuhuateamsystem.Main;
import org.qiuhua.qiuhuateamsystem.gui.GuiManager;
import org.qiuhua.qiuhuateamsystem.hud.HudManager;
import org.qiuhua.qiuhuateamsystem.team.TeamManager;

import java.util.ArrayList;
import java.util.List;

public class QiuhuaTeamSystemCommand implements CommandExecutor, TabExecutor {

    public void register() {
        Bukkit.getPluginCommand("QiuhuaTeamSystem").setExecutor(this);
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length == 2 && args[0].equalsIgnoreCase("create")){
            Player player = Bukkit.getPlayer(args[1]);
            if(player == null){
                return true;
            }
            if(sender.hasPermission("QiuhuaTeamSystem.create")){
                TeamManager.createTeam(player);
            }
            return true;
        }
        if(args.length == 2 && args[0].equalsIgnoreCase("teamlobby")){
            Player player = Bukkit.getPlayer(args[1]);
            if(player == null){
                return true;
            }
            if(sender.hasPermission("QiuhuaTeamSystem.teamlobby")){
                GuiManager.openTeamLobbyGui(player);
            }
            return true;
        }
        if(args.length == 2 && args[0].equalsIgnoreCase("team")){
            Player player = Bukkit.getPlayer(args[1]);
            if(player == null){
                return true;
            }
            if(sender.hasPermission("QiuhuaTeamSystem.team")){
                GuiManager.openTeamInfo(player);
            }
            return true;
        }
        if(sender.hasPermission("QiuhuaTeamSystem.reload") && args.length == 1 && args[0].equalsIgnoreCase("reload")){
            Main.getMainPlugin().reloadConfig();
            if(sender instanceof Player){
                sender.sendMessage("[QiuhuaTeamSystem]文件已全部重新加载");
            }else {
                Main.getMainPlugin().getLogger().info("文件已全部重新加载");
            }
        }
        //以下为测试指令
//        if(args.length == 1 && args[0].equalsIgnoreCase("confirmJoin")){
//            if(sender instanceof Player player){
//                TeamManager.confirmJoin(player);
//            }
//        }
//        if(args.length == 1 && args[0].equalsIgnoreCase("rejectJoin")){
//            if(sender instanceof Player player){
//                TeamManager.rejectJoin(player);
//            }
//        }
//        if(args.length == 2 && args[0].equalsIgnoreCase("sendHud")){
//            if(sender instanceof Player player){
//                HudManager.sendHud(player, null);
//            }
//        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player player){
            List<String> result = new ArrayList<>();
            //当参数长度是1时
            if(args.length == 1){
                if (player.hasPermission("QiuhuaTeamSystem.reload")){
                    result.add("reload");
                }
                if (player.hasPermission("QiuhuaTeamSystem.create")){
                    result.add("create");
                }
                if (player.hasPermission("QiuhuaTeamSystem.teamlobby")){
                    result.add("teamlobby");
                }
                if (player.hasPermission("QiuhuaTeamSystem.team")){
                    result.add("team");
                }
//                result.add("confirmJoin");
//                result.add("rejectJoin");
//                result.add("sendHud");
                return result;
            }
        }
        return null;
    }

}
