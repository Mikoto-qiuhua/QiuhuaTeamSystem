package org.qiuhua.qiuhuateamsystem.dungeonsInfo;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.qiuhua.UnrealGUIPro.JavaScripts.Scripts;
import org.qiuhua.UnrealGUIPro.Main;
import org.qiuhua.UnrealGUIPro.Tool;
import org.qiuhua.UnrealGUIPro.application.PlayerFunction;
import org.qiuhua.qiuhuateamsystem.file.FileManager;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DungeonsInfoData {

    private String name;

    private List<String> info = new ArrayList<>();

    private String condition;

    private String starCondition;

    private String megCondition;

    private final CopyOnWriteArrayList<ItemStack> itemList = new CopyOnWriteArrayList<>();

    public DungeonsInfoData(String name, List<String> info, String condition, String starCondition, String megCondition,List<ItemStack> itemStackList){
        this.name = name;
        this.info.addAll(info);
        this.condition = condition;
        this.itemList.addAll(itemStackList);
        this.starCondition = starCondition;
        this.megCondition = megCondition;
    }

    public String getCondition() {
        return this.condition;
    }

    public List<String> getInfo() {
        return this.info;
    }

    public String getName() {
        return this.name;
    }

    public CopyOnWriteArrayList<ItemStack> getItemList() {
        return this.itemList;
    }

    public String getMegCondition() {
        return this.megCondition;
    }

    //将信息进行papi解析
    public List<String> infoPapi(Player player){
        List<String> newInfo = new ArrayList<>(this.info);
        newInfo = FileManager.getConvertPapiList(player, newInfo);
        return newInfo;
    }

    //解析条件结果
    public Boolean spawnConditionResults(Player player){
        if(this.condition == null || this.condition.equals("")){
            return true;
        }
        String newCondition = String.copyValueOf(this.condition.toCharArray());
        newCondition = FileManager.getPapiString(player, newCondition);
        try {
            Boolean b;
            //如果装了那个Kether的扩展
            if(Main.getIsKetherFactory()){
                b = (Boolean) PlayerFunction.parseKether(newCondition, player , new HashMap<>()).getNow(null);
            }else{
                b = (Boolean) Scripts.main(newCondition);
            }
            return b;
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    //解析启动条件
    public Boolean spawnStarConditionResults(Player player){
        if(this.starCondition == null || this.starCondition.isEmpty()){
            return true;
        }
        String newCondition = String.copyValueOf(this.starCondition.toCharArray());
        newCondition = FileManager.getPapiString(player, newCondition);
        try {
            Boolean b;
            //如果装了那个Kether的扩展
            if(Main.getIsKetherFactory()){
                b = (Boolean) PlayerFunction.parseKether(newCondition, player , new HashMap<>()).getNow(null);
            }else{
                b = (Boolean) Scripts.main(newCondition);
            }
            return b;
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }


}
