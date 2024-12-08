package org.qiuhua.qiuhuateamsystem.gui;

import com.daxton.unrealcore.application.UnrealCoreAPI;
import com.daxton.unrealcore.application.method.SchedulerFunction;
import com.daxton.unrealcore.common.type.MouseActionType;
import com.daxton.unrealcore.common.type.MouseButtonType;
import com.daxton.unrealcore.display.been.module.ModuleData;
import com.daxton.unrealcore.display.been.module.control.ButtonModuleData;
import com.daxton.unrealcore.display.been.module.control.ContainerModuleData;
import com.daxton.unrealcore.display.been.module.display.TextModuleData;
import com.daxton.unrealcore.display.been.module.input.InputModuleData;
import com.daxton.unrealcore.display.content.module.control.ButtonModule;
import com.daxton.unrealcore.display.content.module.control.ContainerModule;
import com.daxton.unrealcore.display.content.module.display.TextModule;
import com.daxton.unrealcore.display.controller.GUIController;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.qiuhua.UnrealGUIPro.gui.UnrealGUIContainer;
import org.qiuhua.qiuhuateamsystem.Main;
import org.qiuhua.qiuhuateamsystem.file.FileManager;
import org.qiuhua.qiuhuateamsystem.team.TeamInfoData;
import org.qiuhua.qiuhuateamsystem.team.TeamManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TeamLobbyGui extends UnrealGUIContainer{

    //队伍列表的那个容器
    private ContainerModule teamListContainer;
    //单个队伍信息的容器模板
    private ContainerModule teamInfoContainer;
    //搜索输入框
    private InputModuleData searchInput;
    //当前查看玩家对象
    private Player player;
    //队伍列表组件名称 队伍信息容器模板的id
    private List<String> removeModule = new ArrayList<>();

    public TeamLobbyGui(Player player){
        super("组队大厅", FileManager.teamLobby, player);
        this.player = player;
        //获取相关组件
        //获取搜索框
        this.searchInput = (InputModuleData) this.getMainGUIData().getModuleDataMap().get("搜索输入框");
        //获取队伍列表的那个容器
        this.teamListContainer = (ContainerModule) this.getModule("队伍列表容器");
        //获取单个队伍信息的容器
        this.teamInfoContainer = (ContainerModule) this.teamListContainer.getModule("队伍信息容器模板");
        //移除这个模版
        this.teamListContainer.removeModule("队伍信息容器模板");
        //获取刷新按钮
        ButtonModule updateButton = (ButtonModule) this.getModule("刷新按钮");
        updateButton.onButtonClick((buttonModule2, mouseButtonType, mouseActionType) -> {
            if (mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.Off) {
                //获取输入框的内容
                String searchContent = this.searchInput.getText();
                //如果输入框没内容 那就单纯刷新界面
                if(searchContent.equals("")){
                    UpdateTeamList();
                }else {
                    //否则就用输入的内容刷新
                    UpdateTeamList(TeamManager.searchTeam(searchContent));
                }
                Main.log("刷新队伍列表");
            }
        });
        //获取搜索按钮
        ButtonModule searchButton = (ButtonModule) this.getModule("搜索按钮");
        searchButton.onButtonClick((buttonModule2, mouseButtonType, mouseActionType) -> {
            if (mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.Off) {
                //获取输入框的内容
                String searchContent = this.searchInput.getText();
                //如果输入框有内容就用内容进行刷新
                if(!searchContent.equals("")){
                    UpdateTeamList(TeamManager.searchTeam(searchContent));
                }
                Main.log("尝试搜索队伍 " + searchContent);
            }
        });
        //获取创建队伍按钮
        ButtonModule createTeamButton = (ButtonModule) this.getModule("创建队伍");
        createTeamButton.onButtonClick((buttonModule2, mouseButtonType, mouseActionType) -> {
            if (mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.Off) {
                //检查玩家是否有队伍
                if(TeamManager.getTeams(player) == null){
                    TeamManager.createTeam(player);
                    GuiManager.openTeamInfo(player);
                }else{
                    GuiManager.openTeamInfo(player);
                    Main.log("你已有队伍,打开队伍界面");
                }
            }
        });
        //构建队伍信息
        BuildTeamList(TeamManager.allTeams);
    }





    //刷新当前界面的队伍列表
    public void UpdateTeamList(){
        this.removeModule.forEach(this::removeModule);
        this.upDate();
        BuildTeamList(TeamManager.allTeams);
    }

    public void UpdateTeamList(ConcurrentHashMap<Integer, TeamInfoData> allTeams){
        this.removeModule.forEach(this::removeModule);
        this.upDate();
        BuildTeamList(allTeams);
    }



    //构建队伍列表 提供队伍列表
    public void BuildTeamList(ConcurrentHashMap<Integer, TeamInfoData> allTeams){
        //刷新前先清空
        this.teamListContainer.clearModule();
        this.removeModule.clear();
        if(allTeams.isEmpty()){
            return;
        }
        //获取换行次数
        int teamListRow = FileManager.config.getInt("TeamLobby.teamListRow");
        //获取teamListXSpacing
        int teamListXSpacing = FileManager.config.getInt("TeamLobby.teamListXSpacing");
        //获取teamListYSpacing
        int teamListYSpacing = FileManager.config.getInt("TeamLobby.teamListYSpacing");
        //当前行数
        int numberOfRows = 0;
        int i = 0;
        for(Integer teamId : allTeams.keySet()){
            TeamInfoData teamInfoData = allTeams.get(teamId);
            //获取队伍队长
            UUID teamOwner = teamInfoData.getTeamOwner();
            Player teamOwnerPlayer = Bukkit.getPlayer(teamOwner);
            //如果出现队长获取失败 就跳过这个并且删除他的队伍
            if(teamOwnerPlayer == null){
                teamInfoData.dissolveTeam();
                continue;
            }
            //获取是否隐藏队伍 如果隐藏就跳过他
            if(teamInfoData.getHide()){
                continue;
            }

            //获取副本名字
            String dungeonsName = teamInfoData.getDungeonsName();
            //获取队伍名字
            String teamName = teamInfoData.getTeamName();
            //获取队内玩家列表
            ConcurrentHashMap<UUID, Boolean> teamPlayer = teamInfoData.getTeamPlayer();

            //复制一份单个队伍信息的容器模板
            //获取自定义信息模板
            ContainerModule copyTeamInfoContainer = this.teamInfoContainer.copy();
            //设置他的名字 team+teamid
            copyTeamInfoContainer.setModuleID("team_" + teamId);
            //如果需要换行就设置垂直位置
            if (i % teamListRow == 0 && i != 0) {
                numberOfRows++;
                i = 0;
            }

            //设置他的位置 如果他不是水平第一个就重设位置
            if(i != 0){
                //设置水平位置
                copyTeamInfoContainer.setX(String.valueOf(teamListXSpacing * i));
            }
            //如果他不是垂直第一个就重设位置
            if(numberOfRows != 0){
                copyTeamInfoContainer.setY(String.valueOf(teamListYSpacing * numberOfRows));
            }
            //处理自定义信息模板
            TextModule textModule = (TextModule) copyTeamInfoContainer.getModule("自定义信息模板");
            List<String> textList = textModule.getText();
            Map<String, String> replacements = new HashMap<>();
            replacements.put("<副本名字>", dungeonsName);
            replacements.put("<队伍名字>", teamName);
            replacements.put("<队伍ID>", String.valueOf(teamId));
            replacements.put("<当前人数>", String.valueOf(teamPlayer.size()));
            replacements.put("<最大人数>", String.valueOf(FileManager.config.getInt("TeamInfo.teamSize")));
            //处理审批文字
            String approveText = "";
            if(teamInfoData.getApprove()){
                approveText = FileManager.config.getString("TeamInfo.approveText");
            }
            replacements.put("<审批>", approveText);
            List<String> updatedList = textList.stream()
                    .map(s -> GuiManager.replacePlaceholders(s, replacements))
                    .toList();
            //处理一次papi变量
            updatedList = FileManager.getConvertPapiList(teamOwnerPlayer, updatedList);
            textModule.setText(updatedList);
            //处理加入按钮模板
            ButtonModule joinButtonModule = (ButtonModule) copyTeamInfoContainer.getModule("加入按钮模板");
            joinButtonModule.onButtonClick((buttonModule2, mouseButtonType, mouseActionType) -> {
                if (mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.Off) {
                    TeamManager.applyJoinTeam(this.player, teamId);
                }
            });
            //处理查看按钮模板
            ButtonModule LookButtonModule = (ButtonModule) copyTeamInfoContainer.getModule("查看按钮模板");
            LookButtonModule.onButtonClick((buttonModule2, mouseButtonType, mouseActionType) -> {
                if (mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.Off) {
                    GuiManager.observeTeamInfo(this.player, teamId);
                }
            });
            this.addModule(copyTeamInfoContainer);
            this.removeModule.add("队伍列表容器:" + copyTeamInfoContainer.getModuleID());
            i++;
        }
        //获取下部边距
        int margins = FileManager.config.getInt("TeamLobby.margins");
        this.teamListContainer.setActualHeight(teamListYSpacing * numberOfRows + margins);
        this.upDate();
    }



}
