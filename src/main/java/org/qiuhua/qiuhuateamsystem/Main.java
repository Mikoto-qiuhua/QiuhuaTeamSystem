package org.qiuhua.qiuhuateamsystem;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.qiuhua.qiuhuateamsystem.command.QiuhuaTeamSystemCommand;
import org.qiuhua.qiuhuateamsystem.file.FileManager;
import org.qiuhua.qiuhuateamsystem.listeners.PlayerListener;
import org.qiuhua.qiuhuateamsystem.listeners.UnrealGuiListener;
import org.qiuhua.qiuhuateamsystem.listeners.UnrealKeyBoard;

public class Main extends JavaPlugin {
    private static Main mainPlugin;
    public static Main getMainPlugin(){
        return mainPlugin;
    }


    //启动时运行
    @Override
    public void onEnable(){
        mainPlugin = this;
        FileManager.saveAllConfig();
        FileManager.reload();

        new QiuhuaTeamSystemCommand().register();
        Bukkit.getPluginManager().registerEvents(new UnrealGuiListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new UnrealKeyBoard(), this);
    }



    //关闭时运行
    @Override
    public void onDisable(){
    }

    //执行重载命令时运行
    @Override
    public void reloadConfig(){
        FileManager.reload();
    }

    public static void log(String str){
        if(FileManager.config.getBoolean("Debug")){
            Main.mainPlugin.getLogger().info(str);
        }
    }
}