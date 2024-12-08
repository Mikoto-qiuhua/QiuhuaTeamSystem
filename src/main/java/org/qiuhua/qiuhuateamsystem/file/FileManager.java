package org.qiuhua.qiuhuateamsystem.file;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.qiuhua.qiuhuateamsystem.Main;
import org.qiuhua.qiuhuateamsystem.dungeonsInfo.DungeonsInfoManager;
import org.qiuhua.qiuhuateamsystem.hud.HudManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileManager {

    public static FileConfiguration config;

    public static FileConfiguration teamLobby;
    public static FileConfiguration teamInfo;
    public static FileConfiguration message;


    //重新加载全部文件
    public static void reload() {
        config = loadFile(new File(Main.getMainPlugin().getDataFolder (),"Config.yml"));
        teamLobby = loadFile(new File(Main.getMainPlugin().getDataFolder (),"gui/组队大厅.yml"));
        teamInfo = loadFile(new File(Main.getMainPlugin().getDataFolder (),"gui/队伍信息.yml"));
        message = loadFile(new File(Main.getMainPlugin().getDataFolder (),"Message.yml"));

        //写入地牢数据
        ConfigurationSection dungeonsInfoSection = config.getConfigurationSection("DungeonsInfo");
        if (dungeonsInfoSection != null) {
            DungeonsInfoManager.refreshDungeonsInfoData(dungeonsInfoSection);
        }

        HudManager.loadHud();
    }


    //创建配置文件
    public static void saveAllConfig(){
        //创建一个插件文件夹路径为基础的 并追加下一层。所以此时的文件应该是Config.yml
        //exists 代表是否存在
        if (!(new File(Main.getMainPlugin().getDataFolder() ,"Config.yml").exists())){
            Main.getMainPlugin().saveResource("Config.yml", false);
        }
        if (!(new File(Main.getMainPlugin().getDataFolder() ,"Message.yml").exists())){
            Main.getMainPlugin().saveResource("Message.yml", false);
        }
        if (!(new File (Main.getMainPlugin().getDataFolder() ,"gui").exists())){
            Main.getMainPlugin().saveResource("gui/组队大厅.yml", false);
            Main.getMainPlugin().saveResource("gui/队伍信息.yml", false);
        }
    }

    // 使用 PAPI 替换占位符
    public static String getPapiString(Player player, String string){
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            return PlaceholderAPI.setPlaceholders(player, string);
        }
        return string;
    }

    public static List<String> getPapiList(Player player, List<String> list){
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            List<String>  newList = new ArrayList<>();
            list.forEach((e) -> {
                newList.add(PlaceholderAPI.setPlaceholders(player, e));
            });
            return newList;
        }
        return list;
    }

    public static List<String> getConvertPapiList(Player player, List<String> list){
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            List<String>  newList = new ArrayList<>();
            list.forEach((e) -> {
                newList.add(PlaceholderAPI.setPlaceholders(player, e.replaceAll("<", "%").replaceAll(">", "%")));
            });
            return newList;
        }
        return list;
    }

    //去除颜色代码
    public static String removeColorCode(String str){
        Pattern pattern = Pattern.compile("§[0-9a-fklmnor]");
        Matcher matcher = pattern.matcher(str);
        return matcher.replaceAll("");
    }



    public static YamlConfiguration loadFile (File file)
    {
        return YamlConfiguration.loadConfiguration(file);
    }
}
