package org.qiuhua.qiuhuateamsystem.dungeonsInfo;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.qiuhua.qiuhuateamsystem.playerdata.PlayerData;
import pers.neige.neigeitems.manager.ItemManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DungeonsInfoManager {

    //记录已加载的地牢信息数据
    public static final ConcurrentHashMap<String, DungeonsInfoData> allDungeonsInfoData = new ConcurrentHashMap<>();

    //读取一个地牢数据
    public static DungeonsInfoData getDungeonsInfoData(String name){
        if(allDungeonsInfoData.containsKey(name)){
            return allDungeonsInfoData.get(name);
        }
        return null;
    }

    //写入地牢数据
    public static void refreshDungeonsInfoData(ConfigurationSection dungeonsSection){
        allDungeonsInfoData.clear();
        for(String dungeonsName : dungeonsSection.getKeys(false)){
            ConfigurationSection dungeonsInfoSection = dungeonsSection.getConfigurationSection(dungeonsName);
            if(dungeonsInfoSection != null){
                String name = dungeonsInfoSection.getString("name");
                List<String> info = dungeonsInfoSection.getStringList("info");
                String condition = dungeonsInfoSection.getString("condition");
                String starCondition = dungeonsInfoSection.getString("starCondition");
                String megCondition = dungeonsInfoSection.getString("megCondition");
                ConfigurationSection itemListSection = dungeonsInfoSection.getConfigurationSection("itemList");
                List<ItemStack> itemList = new ArrayList<>();
                if(itemListSection != null){
                    itemList = spawnItemList(itemListSection);
                }
                DungeonsInfoData dungeonsInfoData = new DungeonsInfoData(name, info, condition, starCondition, megCondition, itemList);
                allDungeonsInfoData.put(dungeonsName, dungeonsInfoData);
            }
        }
    }

    //获取物品列表
    public static List<ItemStack> spawnItemList(ConfigurationSection itemListSection){
        List<ItemStack> itemStackList = new ArrayList<>();
        for(String key : itemListSection.getKeys(false)){
            ConfigurationSection itemSection = itemListSection.getConfigurationSection(key);
            String type = itemSection.getString("type");
            String name = itemSection.getString("name");
            List<String> lore = itemSection.getStringList("lore");
            int model = itemSection.getInt("model");
            ItemStack itemStack;
            switch (type){
                case "NeigeItems":
                    itemStack = ItemManager.INSTANCE.getItemStack(name);
                    break;
                default:
                    itemStack = new ItemStack(Material.valueOf(type));
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.setLore(lore);
                    itemMeta.setDisplayName(name);
                    if(model != -1){
                        itemMeta.setCustomModelData(model);
                    }
                    itemStack.setItemMeta(itemMeta);
            }
            itemStackList.add(itemStack);
        }
        return itemStackList;
    }


    //获取玩家可用的副本列表
    public static List<String> getDungeonsInfoList(Player player){
        List<String> dungeonsInfoList = new ArrayList<>();
        for(String key : allDungeonsInfoData.keySet()){
            DungeonsInfoData dungeonsInfoData = allDungeonsInfoData.get(key);
            if(dungeonsInfoData.spawnConditionResults(player)){
                dungeonsInfoList.add(key);
            }
        }
        return dungeonsInfoList;
    }

}
