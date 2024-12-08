package org.qiuhua.qiuhuateamsystem.gui;

import com.daxton.unrealcore.application.method.SchedulerRunnable;
import com.daxton.unrealcore.common.type.MouseActionType;
import com.daxton.unrealcore.common.type.MouseButtonType;
import com.daxton.unrealcore.display.content.module.control.ButtonModule;
import com.daxton.unrealcore.display.content.module.control.CheckModule;
import com.daxton.unrealcore.display.content.module.control.ContainerModule;
import com.daxton.unrealcore.display.content.module.display.EntityModule;
import com.daxton.unrealcore.display.content.module.display.ItemModule;
import com.daxton.unrealcore.display.content.module.display.TextModule;
import com.daxton.unrealcore.display.content.module.input.InputModule;
import com.daxton.unrealcore.nms.NMSItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.units.qual.C;
import org.qiuhua.UnrealGUIPro.gui.UnrealGUIContainer;
import org.qiuhua.qiuhuateamsystem.Main;
import org.qiuhua.qiuhuateamsystem.dungeon.DungeonPlusHook;
import org.qiuhua.qiuhuateamsystem.dungeonsInfo.DungeonsInfoData;
import org.qiuhua.qiuhuateamsystem.dungeonsInfo.DungeonsInfoManager;
import org.qiuhua.qiuhuateamsystem.file.FileManager;
import org.qiuhua.qiuhuateamsystem.playerdata.PlayerDataManager;
import org.qiuhua.qiuhuateamsystem.team.TeamInfoData;
import org.qiuhua.qiuhuateamsystem.team.TeamManager;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TeamInfoGui extends UnrealGUIContainer {

    //成员列表的那个容器
    private ContainerModule playerListContainer;
    //单个成员信息的容器模板
    private ContainerModule playerInfoContainer;

    //申请列表那个容器
    private ContainerModule approveListContainer;
    //申请信息那个容器模板
    private ContainerModule approveInfoContainer;

    //邀请列表那个容器
    private ContainerModule inviteListContainer;
    //邀请信息那个容器模板
    private ContainerModule inviteInfoContainer;
    //物品列表那个容器
    private ContainerModule itemListContainer;
    //单个物品那个容器模板
    private ContainerModule itemInfoContainer;
    //副本选择列表那个容器
    private ContainerModule dungeonsInfoListContainer;
    //选副本的按钮
    private ButtonModule dungeonsInfoButton;
    //地牢信息容器
    private ContainerModule dungeonsInfoContainer;
    //队伍消息的那个容器
    private ContainerModule teamMessageContainer;
    //队伍消息的输入框
    private InputModule teamMessageInputModule;

    //是否打开了副本选择
    private Boolean isDungeonsInfo = false;
    //当前查看玩家对象
    private Player player;
    //成员列表组件名称 成员信息容器模板的id
    private List<String> removePlayerInfoModule = new ArrayList<>();
    //申请列表组件名称 申请信息容器模板的id
    private List<String> removeApproveInfoModule = new ArrayList<>();
    //邀请列表组件名称 邀请信息容器模板的id
    private List<String> removeInviteInfoModule = new ArrayList<>();
    //物品列表组件名称 物品信息容器模板的id
    private List<String> removeItemInfoModule = new ArrayList<>();




    //这个gui所属的队伍信息
    private TeamInfoData teamInfoData;

    public TeamInfoGui(Player player, TeamInfoData teamInfoData, String guiId) {
        super(guiId, FileManager.teamInfo, player);
        this.player = player;
        this.teamInfoData = teamInfoData;
        //获取相关组件
        //获取成员列表的那个容器
        this.playerListContainer = (ContainerModule) this.getModule("队友列表容器");
        //获取单个成员信息的容器模板
        this.playerInfoContainer = (ContainerModule) this.playerListContainer.getModule("队友信息容器模板");
        //移除这个模版
        this.playerListContainer.removeModule("队友信息容器模板");

        //获取申请列表那个容器
        this.approveListContainer = (ContainerModule) this.getModule("申请列表容器");
        //获取申请信息那个容器模板
        this.approveInfoContainer = (ContainerModule) this.approveListContainer.getModule("申请玩家容器");
        //移除这个模版
        this.approveListContainer.removeModule("申请玩家容器");

        //获取邀请列表那个容器
        this.inviteListContainer = (ContainerModule) this.getModule("邀请列表容器");
        //获取邀请信息那个容器模板
        this.inviteInfoContainer = (ContainerModule) this.inviteListContainer.getModule("单个邀请信息容器模板");
        //移除这个模版
        this.inviteListContainer.removeModule("单个邀请信息容器模板");

        //获取物品列表那个容器
        this.itemListContainer = (ContainerModule) this.getModule("副本物品列表容器");
        //获取单个物品那个容器模板
        this.itemInfoContainer = (ContainerModule) this.itemListContainer.getModule("单个物品信息容器模板");
        //移除这个模版
        this.itemListContainer.removeModule("单个物品信息容器模板");

        //获取副本选择列表那个容器
        ContainerModule dungeonsInfoListContainer = (ContainerModule) this.getModule("副本选择容器");
        //选副本的按钮
        this.dungeonsInfoButton = (ButtonModule) dungeonsInfoListContainer.getModule("副本按钮模板").copy();
        //然后移除这个按钮后再复制 副本选择列表那个容器 这样里面就是空的
        dungeonsInfoListContainer.clearModule();
        this.dungeonsInfoListContainer = dungeonsInfoListContainer.copy();
        //将他移除 因为默认不显示
        this.removeModule("副本选择容器");

        //获取地牢信息那个容器
        this.dungeonsInfoContainer = (ContainerModule) this.getModule("副本信息容器");

        //获取队内消息输入框
        this.teamMessageInputModule = (InputModule) this.getModule("队内聊天输入框");
        //获取队内消息的容器
        this.teamMessageContainer = (ContainerModule) this.getModule("队内聊天栏容器");


        //以下是固定功能的按钮
        //设置选择副本的按钮
        ButtonModule dungeonsInfoListButtonModule = (ButtonModule) this.getModule("副本选择按钮");
        dungeonsInfoListButtonModule.onButtonClick((buttonModule2, mouseButtonType, mouseActionType) -> {
            if (mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.Off) {
                if(this.teamInfoData.isTeamOwner(this.player.getUniqueId())){
                    if(this.isDungeonsInfo){
                        this.isDungeonsInfo = false;
                        this.removeModule("副本选择容器");
                        this.upDate();
                    }else {
                        this.isDungeonsInfo = true;
                        BuildDungeonsInfoList(true);
                    }
                }
            }
        });
        //获取退出队伍按钮
        ButtonModule leaveModule = (ButtonModule) this.getModule("退出队伍按钮");
        leaveModule.onButtonClick((buttonModule2, mouseButtonType, mouseActionType) -> {
            if (mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.Off) {
                //玩家在队伍内才能退出
                if(this.teamInfoData.getTeamPlayer().containsKey(this.player.getUniqueId())){
                    TeamManager.leaveTeam(this.player);
                }
            }
        });
        //获取发送消息的按钮
        ButtonModule sendMessageModule = (ButtonModule) this.getModule("发送队内聊天按钮");
        sendMessageModule.onButtonClick((buttonModule2, mouseButtonType, mouseActionType) -> {
            if (mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.Off) {
                //玩家在队伍内才能发送消息
                if(this.teamInfoData.getTeamPlayer().containsKey(this.player.getUniqueId())){
                    //获取输入框内容
                    String meg = this.teamMessageInputModule.getText();
                    if(meg.equals("")){
                        return;
                    }
                    this.teamInfoData.sendTeamMessage(meg, this.player);
                    //清空输入框内容
                    this.teamMessageInputModule.setText("");
                    //重新添加进去才能刷新
                    this.addModule(this.teamMessageInputModule);
                    //更新聊天内容
                    this.teamInfoData.updateTeamMessageGui();
                }
            }
        });


        //如果是队长
        if(this.teamInfoData.isTeamOwner(this.player.getUniqueId())){
            //删除准备按钮
            this.removeModule("准备按钮");
            //获取进入副本按钮
            ButtonModule stateModule = (ButtonModule) this.getModule("进入副本按钮");
            stateModule.onButtonClick((buttonModule2, mouseButtonType, mouseActionType) -> {
                if (mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.Off) {
                    if((this.teamInfoData.isTeamOwner(this.player.getUniqueId()))){
                        Main.log("玩家 " + this.player.getName() + " 的队伍 "+ this.teamInfoData.getTeamId() + " 尝试启动副本 " + this.teamInfoData.getDungeonsName());
                        DungeonPlusHook.star(this.player, this.teamInfoData);
                    }
                }
            });
            //否则如果是队员
        } else if (this.teamInfoData.getTeamPlayer().containsKey(this.player.getUniqueId())) {
            //删除开始副本按钮
            this.removeModule("进入副本按钮");
            //获取准备按钮
            ButtonModule stateModule = (ButtonModule) this.getModule("准备按钮");
            stateModule.onButtonClick((buttonModule2, mouseButtonType, mouseActionType) -> {
                if (mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.Off) {
                    //获取队伍列表
                    ConcurrentHashMap<UUID, Boolean> teamPlays = this.teamInfoData.getTeamPlayer();
                    UUID uuid = this.player.getUniqueId();
                    //玩家在队伍内才能准备
                    if(teamPlays.containsKey(this.player.getUniqueId())){
                        if(teamPlays.get(uuid)){
                            teamPlays.put(uuid, false);
                        }else{
                            teamPlays.put(uuid, true);
                        }
                        this.teamInfoData.updateTeamInfoGui();
                    }
                }
            });
        }else {
            //如果都不是 那就全删了
            this.removeModule("准备按钮");
            this.removeModule("进入副本按钮");
        }



        //不是队长不给点
        if(!this.teamInfoData.isTeamOwner(this.player.getUniqueId())){
            dungeonsInfoListButtonModule.setUse(false);
        }
        //如果不是队员 那退出队伍也不能点
        if(!this.teamInfoData.getTeamPlayer().containsKey(this.player.getUniqueId())){
            leaveModule.setUse(false);
            this.teamMessageInputModule.setUse(false);
            sendMessageModule.setUse(false);
        }




        //构建成员信息
        BuildPlayerList(false);
        //构建基础信息
        BuildBaseModules(false);
        //构建邀请模块
        BuildInviteMoules(false);
        //构建副本信息
        BuildDungeonsInfoModules(false);
        //如果是成员才构建申请列表和邀请列表还有聊天信息
        if(this.teamInfoData.getTeamPlayer().containsKey(this.player.getUniqueId())){
            //构建申请列表
            BuildApproveList(false);
            //构建邀请列表
            BuildInviteList(false);
            //构建聊天信息
            BuildTeamMessageModules(false);
        }


    }


    //刷新当前界面的信息
    public void Update(){
        this.removePlayerInfoModule.forEach(this::removeModule);
        this.removeApproveInfoModule.forEach(this::removeModule);
        this.removeInviteInfoModule.forEach(this::removeModule);
        this.removeItemInfoModule.forEach(this::removeModule);
        this.upDate();
        //构建玩家列表
        BuildPlayerList(false);
        //构建申请列表
        BuildApproveList(false);
        //刷新基础信息
        BuildBaseModules(false);
        //刷新邀请列表
        BuildInviteList(false);
        //刷新副本信息
        BuildDungeonsInfoModules(false);
        this.upDate();
    }


    //刷新队内聊天消息
    public void UpdateMessage(){
        BuildTeamMessageModules(true);
    }


    //构建玩家信息
    public void BuildPlayerList(Boolean isUpdate){
        //刷新前先清空
        this.playerListContainer.clearModule();
        this.removePlayerInfoModule.clear();
        //获取teamOwnerName
        String teamOwnerName = FileManager.config.getString("TeamInfo.teamOwnerName");
        //获取准备信息
        String startState = FileManager.config.getString("TeamInfo.startState");
        String defaultState = FileManager.config.getString("TeamInfo.defaultState");
        //获取换行次数
        int playerListRow = FileManager.config.getInt("TeamInfo.playerListRow");
        //获取playerListXSpacing
        int playerListXSpacing = FileManager.config.getInt("TeamInfo.playerListXSpacing");
        //获取playerListYSpacing
        int playerListYSpacing = FileManager.config.getInt("TeamInfo.playerListYSpacing");
        //当前行数
        int numberOfRows = 0;
        int i = 0;
        for(UUID uuid : this.teamInfoData.getTeamPlayer().keySet()){
            Player player = Bukkit.getPlayer(uuid);
            if(player == null){
                this.teamInfoData.getTeamPlayer().remove(uuid);
                PlayerDataManager.allPlayerData.remove(uuid);
                continue;
            }

            //复制一份单个成员信息的容器模板
            //获取自定义信息模板
            ContainerModule copyPlayerInfoContainer = this.playerInfoContainer.copy();
            //设置他的名字 player+name
            copyPlayerInfoContainer.setModuleID("player_" + player.getName());
            //如果需要换行就设置垂直位置
            if (i % playerListRow == 0 && i != 0) {
                numberOfRows++;
                i = 0;
            }
            //设置他的位置 如果他不是水平第一个就重设位置
            if(i != 0){
                //设置水平位置
                copyPlayerInfoContainer.setX(String.valueOf(playerListXSpacing * i));
            }
            //如果他不是垂直第一个就重设位置
            if(numberOfRows != 0){
                copyPlayerInfoContainer.setY(String.valueOf(playerListYSpacing * numberOfRows));
            }
            //处理自定义信息模板
            TextModule textModule = (TextModule) copyPlayerInfoContainer.getModule("自定义信息模板");
            List<String> textList = textModule.getText();
            //处理一次papi变量 解析对象为这个玩家
            textList = FileManager.getConvertPapiList(player, textList);
            textModule.setText(textList);
            //处理踢出按钮
            ButtonModule kickButtonModule = (ButtonModule) copyPlayerInfoContainer.getModule("踢出队友按钮模板");
            kickButtonModule.onButtonClick((buttonModule2, mouseButtonType, mouseActionType) -> {
                if (mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.Off) {
                    TeamManager.kickPlayer(this.player, player, this.teamInfoData);
                }
            });
            //处理转让队长按钮模板
            ButtonModule switchOwnerButtonModule = (ButtonModule) copyPlayerInfoContainer.getModule("转让队长按钮模板");
            switchOwnerButtonModule.onButtonClick((buttonModule2, mouseButtonType, mouseActionType) -> {
                if (mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.Off) {
                    TeamManager.switchOwner(this.player, player, this.teamInfoData);
                }
            });
            //处理实体渲染
            EntityModule entityModule = (EntityModule) copyPlayerInfoContainer.getModule("玩家实体渲染模版");
            entityModule.setEntityName(player.getName());
            //处理准备信息模板
            TextModule stateModule = (TextModule) copyPlayerInfoContainer.getModule("准备信息模板");
            List<String> stateTextList = stateModule.getText();
            Map<String, String> replacements = new HashMap<>();
            replacements.put("<准备状态>", defaultState);
            //如果这个玩家是队长
            if(this.teamInfoData.isTeamOwner(player.getUniqueId())){
                replacements.put("<准备状态>", teamOwnerName);
            }else if (this.teamInfoData.getTeamPlayer().get(player.getUniqueId())){
                //如果已经准备
                replacements.put("<准备状态>", startState);
            }
            List<String> updatedList = stateTextList.stream()
                    .map(s -> GuiManager.replacePlaceholders(s, replacements))
                    .toList();
            stateModule.setText(updatedList);
            //如果不是队长 那就不给点提出和转让队长的按钮
            if(!this.teamInfoData.isTeamOwner(this.player.getUniqueId())){
                switchOwnerButtonModule.setUse(false);
                kickButtonModule.setUse(false);
            }

            this.addModule(copyPlayerInfoContainer);
            this.removePlayerInfoModule.add("队友列表容器:" + copyPlayerInfoContainer.getModuleID());
            i++;
        }
        //获取下部边距
        int margins = FileManager.config.getInt("TeamInfo.playerMargins");
        this.playerListContainer.setActualHeight(playerListYSpacing * numberOfRows + margins);

        if(isUpdate){
            this.upDate();
        }

    }

    //构建申请列表
    public void BuildApproveList(Boolean isUpdate){
        //刷新前先清空
        this.approveListContainer.clearModule();
        this.removeApproveInfoModule.clear();
        if(this.teamInfoData.getApproveList().isEmpty()){
            return;
        }
        //获取换行次数
        int approveListRow = FileManager.config.getInt("TeamInfo.approveListRow");
        //获取approveListXSpacing
        int approveListXSpacing = FileManager.config.getInt("TeamInfo.approveListXSpacing");
        //获取playerListYSpacing
        int approveListYSpacing = FileManager.config.getInt("TeamInfo.approveListYSpacing");
        //当前行数
        int numberOfRows = 0;
        int i = 0;
        ConcurrentHashMap<UUID, SchedulerRunnable> approveList = this.teamInfoData.getApproveList();
        for(UUID uuid : approveList.keySet()){
            Player player = Bukkit.getPlayer(uuid);
            if(player == null){
                approveList.get(uuid).cancel();
                approveList.remove(uuid);
                continue;
            }
            //复制一份单个邀请信息的容器模板
            //获取自定义信息模板
            ContainerModule copyApproveInfoContainer = this.approveInfoContainer.copy();
            //设置他的名字 approve+name
            copyApproveInfoContainer.setModuleID("approve_" + player.getName());
            //如果需要换行就设置垂直位置
            if (i % approveListRow == 0 && i != 0) {
                numberOfRows++;
                i = 0;
            }
            //设置他的位置 如果他不是水平第一个就重设位置
            if(i != 0){
                //设置水平位置
                copyApproveInfoContainer.setX(String.valueOf(approveListXSpacing * i));
            }
            //如果他不是垂直第一个就重设位置
            if(numberOfRows != 0){
                copyApproveInfoContainer.setY(String.valueOf(approveListYSpacing * numberOfRows));
            }
            //处理自定义信息模板
            TextModule textModule = (TextModule) copyApproveInfoContainer.getModule("自定义信息模板");
            List<String> textList = textModule.getText();
            //处理一次papi变量 解析对象为这个玩家
            textList = FileManager.getConvertPapiList(player, textList);
            textModule.setText(textList);
            //处理同意按钮
            ButtonModule approveButtonModule = (ButtonModule) copyApproveInfoContainer.getModule("同意加入按钮模板");
            approveButtonModule.onButtonClick((buttonModule2, mouseButtonType, mouseActionType) -> {
                if (mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.Off) {
                    TeamManager.approveJoin(this.player, player, this.teamInfoData);
                }
            });
            //如果玩家不是队长 那不给点这个按钮
            if(!this.teamInfoData.isTeamOwner(this.player.getUniqueId())){
                approveButtonModule.setUse(false);
            }
            this.addModule(copyApproveInfoContainer);
            this.removeApproveInfoModule.add("申请列表容器:" + copyApproveInfoContainer.getModuleID());
            i++;
        }
        //获取下部边距
        int margins = FileManager.config.getInt("TeamLobby.approveMargins");
        this.approveListContainer.setActualHeight(approveListYSpacing * numberOfRows + margins);
        if(isUpdate){
            this.upDate();
        }
    }

    //构建基础模块
    public void BuildBaseModules(Boolean isUpdate){
        //获取审批选项
        CheckModule approvalModule = (CheckModule) this.getModule("审批选项");
        approvalModule.setCheck(this.teamInfoData.getApprove());
        approvalModule.onInputChange((checkModule, mouseButtonType, mouseActionType, b) -> {
            if(mouseActionType == MouseActionType.Off){
                TeamManager.setApprove(this.player, b, this.teamInfoData);
            }
        });
        //获取隐藏选项
        CheckModule hideModule = (CheckModule) this.getModule("隐藏选项");
        hideModule.setCheck(this.teamInfoData.getHide());
        hideModule.onInputChange((checkModule, mouseButtonType, mouseActionType, b) -> {
            if(mouseActionType == MouseActionType.Off){
                TeamManager.setHide(this.player, b, this.teamInfoData);
            }
        });

        //获取队伍名称输入框
        InputModule teamNameInputModule = (InputModule) this.getModule("队伍名称输入框");
        teamNameInputModule.setText(this.teamInfoData.getTeamName());
        //获取修改队伍名称按钮
        ButtonModule setTeamNameModule = (ButtonModule) this.getModule("修改队伍名称按钮");
        setTeamNameModule.onButtonClick((buttonModule2, mouseButtonType, mouseActionType) -> {
            if (mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.Off) {
                //刷新队伍名称
                TeamManager.setTeamName(this.player, teamNameInputModule.getText(), this.teamInfoData);
            }
        });

        //如果不是队长 禁止使用审批 隐藏 队伍输入框 修改队伍名称按钮
        if(!this.teamInfoData.isTeamOwner(player.getUniqueId())){
            approvalModule.setUse(false);
            hideModule.setUse(false);
            teamNameInputModule.setUse(false);
            setTeamNameModule.setUse(false);
        }
        if(isUpdate){
            this.upDate();
        }

    }

    //构建邀请模块和刷新按钮
    public void BuildInviteMoules(Boolean isUpdate){
        //获取玩家名称输入框
        InputModule playerNameInputModule = (InputModule) this.getModule("邀请玩家名称输入框");
        //获取邀请玩家的按钮
        ButtonModule inviteModule = (ButtonModule) this.getModule("邀请玩家按钮");
        inviteModule.onButtonClick((buttonModule2, mouseButtonType, mouseActionType) -> {
            if (mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.Off) {
                String playerName = playerNameInputModule.getText();
                Player targetPlayer = Bukkit.getPlayer(playerName);
                if(targetPlayer != null){
                    TeamManager.inviteJoin(this.player, targetPlayer, this.teamInfoData);
                }else {
                    Main.log("玩家 " + this.player.getName() + " 尝试邀请 " + playerName + " 加入队伍 " + this.teamInfoData.getTeamId() + " 失败 原因:该玩家不存在");
                }
            }
        });
        //获取刷新按钮
        ButtonModule refreshModule = (ButtonModule) this.getModule("刷新玩家按钮");
        refreshModule.onButtonClick((buttonModule2, mouseButtonType, mouseActionType) -> {
            if (mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.Off) {
                //如果是队长才能点
                if(this.teamInfoData.getTeamOwner() == this.player.getUniqueId()){
                    Main.log("玩家 " + this.player.getName() + " 尝试刷新队伍 " + this.teamInfoData.getTeamId() + " 邀请列表");
                    this.teamInfoData.setInvitableList(TeamManager.getInviteList(this.teamInfoData));
                    this.teamInfoData.updateTeamInfoGui();
                }else {
                    Main.log("玩家 " + this.player.getName() + " 尝试刷新队伍 " + this.teamInfoData.getTeamId() + " 邀请列表失败 原因:不是队长");
                }
            }
        });
        //如果不是队长 那就不给点这两个组件
        if(!this.teamInfoData.isTeamOwner(this.player.getUniqueId())){
            playerNameInputModule.setUse(false);
            inviteModule.setUse(false);
            refreshModule.setUse(false);
        }
        if(isUpdate){
            this.upDate();
        }

    }

    //构建邀请列表
    public void BuildInviteList(Boolean isUpdate){
        //刷新前先清空
        this.inviteListContainer.clearModule();
        this.removeInviteInfoModule.clear();
        //获取邀请列表
        CopyOnWriteArrayList<Player> inviteList = this.teamInfoData.getInvitableList();
        if(inviteList.isEmpty()){
            return;
        }
        //获取换行次数
        int inviteListRow = FileManager.config.getInt("InviteSettings.inviteListRow");
        //获取inviteListXSpacing
        int inviteListXSpacing = FileManager.config.getInt("InviteSettings.inviteListXSpacing");
        //获取inviteListYSpacing
        int inviteListYSpacing = FileManager.config.getInt("InviteSettings.inviteListYSpacing");
        //当前行数
        int numberOfRows = 0;
        int i = 0;
        for(Player player : inviteList){
            if(player == null){
                continue;
            }
            //复制一份单个邀请信息的容器模板
            //获取自定义信息模板
            ContainerModule copyInviteInfoContainer = this.inviteInfoContainer.copy();
            //设置他的名字 Invite+name
            copyInviteInfoContainer.setModuleID("invite_" + player.getName());
            //如果需要换行就设置垂直位置
            if (i % inviteListRow == 0 && i != 0) {
                numberOfRows++;
                i = 0;
            }
            //设置他的位置 如果他不是水平第一个就重设位置
            if(i != 0){
                //设置水平位置
                copyInviteInfoContainer.setX(String.valueOf(inviteListXSpacing * i));
            }
            //如果他不是垂直第一个就重设位置
            if(numberOfRows != 0){
                copyInviteInfoContainer.setY(String.valueOf(inviteListYSpacing * numberOfRows));
            }
            //处理自定义信息模板
            TextModule textModule = (TextModule) copyInviteInfoContainer.getModule("自定义信息模板");
            List<String> textList = textModule.getText();
            //处理一次papi变量 解析对象为这个玩家
            textList = FileManager.getConvertPapiList(player, textList);
            textModule.setText(textList);
            //处理邀请按钮
            ButtonModule inviteButtonModule = (ButtonModule) copyInviteInfoContainer.getModule("邀请玩家按钮模板");
            inviteButtonModule.onButtonClick((buttonModule2, mouseButtonType, mouseActionType) -> {
                if (mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.Off) {
                    TeamManager.inviteJoin(this.player, player, this.teamInfoData);
                }
            });
            //如果玩家不是队长 那不给点这个按钮
            if(!this.teamInfoData.isTeamOwner(this.player.getUniqueId())){
                inviteButtonModule.setUse(false);
            }
            this.addModule(copyInviteInfoContainer);
            this.removeInviteInfoModule.add("邀请列表容器:" + copyInviteInfoContainer.getModuleID());
            i++;
        }
        //获取下部边距
        int margins = FileManager.config.getInt("InviteSettings.inviteMargins");
        this.inviteListContainer.setActualHeight(inviteListYSpacing * numberOfRows + margins);
        if(isUpdate){
            this.upDate();
        }

    }

    //构建副本信息模块
    public void BuildDungeonsInfoModules(Boolean isUpdate){
        //获取当前队伍的副本名称
        String dungeonsName = this.teamInfoData.getDungeonsName();
        //使用副本名字获取副本信息对象
        DungeonsInfoData dungeonsInfoData = DungeonsInfoManager.getDungeonsInfoData(dungeonsName);
        TextModule dungeonsInfoModule = (TextModule) this.dungeonsInfoContainer.getModule("副本自定义信息");
        if(dungeonsName.equals("") || dungeonsInfoData == null){
            dungeonsInfoModule.setText("");
        }else {
            dungeonsInfoModule.setText(dungeonsInfoData.infoPapi(this.player));
        }

        //副本名称信息
        TextModule dungeonsNameModule = (TextModule) this.dungeonsInfoContainer.getModule("副本名称信息");
        dungeonsNameModule.setText(dungeonsName);
        this.addModule(dungeonsNameModule);
        this.addModule(dungeonsInfoModule);
        //处理物品列表
        CopyOnWriteArrayList<ItemStack> itemStacksList = new CopyOnWriteArrayList<>();
        if (dungeonsInfoData != null) {
            itemStacksList = dungeonsInfoData.getItemList();
        }
        //刷新前先清空
        this.itemListContainer.clearModule();
        this.removeItemInfoModule.clear();
        if(itemStacksList.isEmpty()){
            return;
        }
        //获取换行次数
        int itemListRow = FileManager.config.getInt("DungeonsInfoSettings.itemListRow");
        //获取itemListXSpacing
        int itemListXSpacing = FileManager.config.getInt("DungeonsInfoSettings.itemListXSpacing");
        //获取itemListYSpacing
        int itemListYSpacing = FileManager.config.getInt("DungeonsInfoSettings.itemListYSpacing");
        //当前行数
        int numberOfRows = 0;
        int i = 0;
        int nameId = 0;
        for(ItemStack itemStack : itemStacksList){
            //复制一份单个邀请信息的容器模板
            //获取自定义信息模板
            ContainerModule copyItemInfoContainer = this.itemInfoContainer.copy();
            //设置他的名字 item+nameId
            copyItemInfoContainer.setModuleID("item_" + nameId);
            //如果需要换行就设置垂直位置
            if (i % itemListRow == 0 && i != 0) {
                numberOfRows++;
                i = 0;
            }
            //设置他的位置 如果他不是水平第一个就重设位置
            if(i != 0){
                //设置水平位置
                copyItemInfoContainer.setX(String.valueOf(itemListXSpacing * i));
            }
            //如果他不是垂直第一个就重设位置
            if(numberOfRows != 0){
                copyItemInfoContainer.setY(String.valueOf(itemListYSpacing * numberOfRows));
            }
            //获取物品材质nbt
            String nbtStr = NMSItem.itemNBTtoString(itemStack);
            //处理物品模板
            ItemModule itemModule = (ItemModule) copyItemInfoContainer.getModule("物品模板");
            itemModule.setItem(nbtStr);
            this.addModule(copyItemInfoContainer);
            this.removeItemInfoModule.add("副本物品列表容器:" + copyItemInfoContainer.getModuleID());
            i++;
            nameId++;
        }

        //获取下部边距
        int margins = FileManager.config.getInt("DungeonsInfoSettings.itemMargins");
        this.itemListContainer.setActualHeight(itemListYSpacing * numberOfRows + margins);
        if(isUpdate){
            this.upDate();
        }
    }

    //构建副本选择列表
    public void BuildDungeonsInfoList(Boolean isUpdate){
        //获取这个玩家可用的副本列表
        List<String> dungeonsInfoList = DungeonsInfoManager.getDungeonsInfoList(this.player);
        if(dungeonsInfoList.isEmpty()){
            return;
        }
        //获取换行次数
        int dungeonsInfoListRow = FileManager.config.getInt("DungeonsInfoSettings.dungeonsInfoListRow");
        //获取dungeonsInfoListXSpacing
        int dungeonsInfoListXSpacing = FileManager.config.getInt("DungeonsInfoSettings.dungeonsInfoListXSpacing");
        //获取dungeonsInfoListYSpacing
        int dungeonsInfoListYSpacing = FileManager.config.getInt("DungeonsInfoSettings.dungeonsInfoListYSpacing");
        //当前行数
        int numberOfRows = 0;
        int i = 0;
        //复制一份容器
        ContainerModule copyDungeonsInfoListContainer = this.dungeonsInfoListContainer.copy();
        //把它添加进列表内
        this.addModule(copyDungeonsInfoListContainer);
        for(String key : dungeonsInfoList){
            //复制一份里面的按钮
            ButtonModule copyDungeonsInfoButton = this.dungeonsInfoButton.copy();
            //设置他的名字 item+nameId
            copyDungeonsInfoButton.setModuleID("DungeonsInfo_" + key);
            //设置他的文本
            copyDungeonsInfoButton.setText(key);
            //如果需要换行就设置垂直位置
            if (i % dungeonsInfoListRow == 0 && i != 0) {
                numberOfRows++;
                i = 0;
            }
            //设置他的位置 如果他不是水平第一个就重设位置
            if(i != 0){
                //设置水平位置
                copyDungeonsInfoButton.setX(String.valueOf(dungeonsInfoListXSpacing * i));
            }
            //如果他不是垂直第一个就重设位置
            if(numberOfRows != 0){
                copyDungeonsInfoButton.setY(String.valueOf(dungeonsInfoListYSpacing * numberOfRows));
            }
            copyDungeonsInfoButton.onButtonClick((buttonModule2, mouseButtonType, mouseActionType) -> {
                if (mouseButtonType == MouseButtonType.Left && mouseActionType == MouseActionType.Off) {
                    this.removeModule("副本选择容器");
                    this.isDungeonsInfo = false;
                    TeamManager.setDungeons(this.player, key, this.teamInfoData);
                }
            });
            //如果玩家不是队长 那不给点这个按钮
            if(!this.teamInfoData.isTeamOwner(this.player.getUniqueId())){
                copyDungeonsInfoButton.setUse(false);
            }
            this.addModule(copyDungeonsInfoButton);
            i++;
        }
        //获取下部边距
        int margins = FileManager.config.getInt("DungeonsInfoSettings.dungeonsInfoMargins");
        copyDungeonsInfoListContainer.setActualHeight(dungeonsInfoListYSpacing * numberOfRows + margins);
        if(isUpdate){
            this.upDate();
        }
    }

    //构建队内消息
    public void BuildTeamMessageModules(Boolean isUpdate){
        //获取信息列表
        TextModule textModule = (TextModule) this.teamMessageContainer.getModule("队内聊天信息列表");
        //将队伍聊天消息设置进去
        textModule.setText(this.teamInfoData.getTeamMessage());
        this.addModule(textModule);
        if(isUpdate){
            this.upDate();
        }
    }

}
