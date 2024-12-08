package org.qiuhua.qiuhuateamsystem.team;

import com.daxton.unrealcore.application.UnrealCoreAPI;
import com.daxton.unrealcore.application.method.SchedulerRunnable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.qiuhua.qiuhuateamsystem.Main;
import org.qiuhua.qiuhuateamsystem.file.FileManager;
import org.qiuhua.qiuhuateamsystem.gui.GuiManager;
import org.qiuhua.qiuhuateamsystem.gui.TeamInfoGui;
import org.qiuhua.qiuhuateamsystem.gui.TeamLobbyGui;
import org.qiuhua.qiuhuateamsystem.playerdata.PlayerData;
import org.qiuhua.qiuhuateamsystem.playerdata.PlayerDataManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

public class TeamInfoData {

    //队伍的id
    private Integer teamId;
    //队伍的队长
    private UUID teamOwner;
    //队伍的全部玩家 包括他有没有准备
    private final ConcurrentHashMap<UUID, Boolean> teamPlayer = new ConcurrentHashMap<>();
    //队伍的申请列表
    private final ConcurrentHashMap<UUID, SchedulerRunnable> approveList = new ConcurrentHashMap<>();
    //当前的可邀请列表
    private CopyOnWriteArrayList<Player> invitableList = new CopyOnWriteArrayList<>();
    //队伍的名字
    private String teamName = "";
    //是否需要审批才能进入
    private Boolean isApprove = true;
    //是否锁定队伍 即为隐藏
    private Boolean isHide = false;
    //当前选择的副本
    private String dungeonsName = "";
    //邀请的玩家列表 value是下一次邀请的时间
    private final ConcurrentHashMap<UUID, Long> inviteList = new ConcurrentHashMap<>();
    //队内消息列表
    private final LinkedBlockingQueue<String> teamMessageList = new LinkedBlockingQueue<>(FileManager.config.getInt("MessageSettings.MessageMax"));

    //邀请
    public TeamInfoData(Integer teamId, Player teamOwner){
        this.teamId = teamId;
        this.teamOwner = teamOwner.getUniqueId();
        String teamDefaultName = FileManager.config.getString("TeamInfo.teamDefaultName");
        teamDefaultName = FileManager.getPapiString(teamOwner, teamDefaultName);
        this.teamName = teamDefaultName;
        teamPlayer.put(teamOwner.getUniqueId(), true);
        Main.log("创建队伍 " + this.teamId + " 成功 队长: " + teamOwner.getName());
    }

    public TeamInfoData(Integer teamId, Player teamOwner, String dungeonsName){
        this.teamId = teamId;
        this.teamOwner = teamOwner.getUniqueId();
        this.dungeonsName = dungeonsName;
        String teamDefaultName = FileManager.config.getString("TeamInfo.teamDefaultName");
        teamDefaultName = FileManager.getPapiString(teamOwner, teamDefaultName);
        this.teamName = teamDefaultName;
        this.teamPlayer.put(teamOwner.getUniqueId(), true);
        Main.log("创建队伍 " + this.teamId + " 成功 队长: " + teamOwner.getName());
    }

    //解散队伍
    public void dissolveTeam() {
        this.teamPlayer.forEach((uuid, value) -> {
            String guiId = PlayerDataManager.getPlayerData(uuid).getGuiId();
            PlayerDataManager.getPlayerData(uuid).setTeamId(null);
            Player player = Bukkit.getPlayer(uuid);
            if(player != null){
                if(guiId != null && guiId.equals("队伍信息")){
                    //如果玩家在看着队伍 那就给他关掉这个界面
                    UnrealCoreAPI.closeGUI(player);
                }
                String meg = FileManager.message.getString("dissolveTeam", "");
                if(!meg.equals("")){
                    player.sendMessage(meg.replaceAll("<teamId>", String.valueOf(this.teamId)));
                }
            }
        });
        TeamManager.allTeams.remove(this.teamId);
        TeamManager.availableIds.offer(this.teamId);
        Main.log("解散队伍 " + this.teamId + " 成功");
    }

    //退出队伍
    public void leaveTeam(Player player){
        UUID uuid = player.getUniqueId();
        //如果这个玩家在队伍内 才能退出
        if(this.teamPlayer.containsKey(uuid)){
            String guiId = PlayerDataManager.getPlayerData(uuid).getGuiId();
            if(guiId != null && guiId.equals("队伍信息")){
                //如果玩家在看着队伍 那就给他关掉这个界面
                UnrealCoreAPI.closeGUI(player);
            }
            this.teamPlayer.remove(uuid);
            PlayerDataManager.getPlayerData(uuid).setTeamId(null);
            String meg = FileManager.message.getString("leaveTeam", "");
            if(!meg.equals("")){
                player.sendMessage(meg.replaceAll("<teamId>", String.valueOf(this.teamId)));
            }
            updateTeamInfoGui();
            String meg1 = FileManager.message.getString("system_leaveTeam", "");
            if(!meg1.equals("")){
                sendSystemMessage(meg1.replaceAll("<player_name>", player.getName()));
            }
            Main.log("玩家 " + player.getName() + " 退出队伍 " + this.teamId + " 成功");

        }
    }

    //踢出玩家
    public void kickPlayer(Player player){
        UUID uuid = player.getUniqueId();
        //如果这个玩家在队伍内 才能退出
        if(this.teamPlayer.containsKey(uuid)){
            String guiId = PlayerDataManager.getPlayerData(uuid).getGuiId();
            if(guiId != null && guiId.equals("队伍信息")){
                //如果玩家在看着队伍 那就给他关掉这个界面
                UnrealCoreAPI.closeGUI(player);
            }
            this.teamPlayer.remove(uuid);
            PlayerDataManager.getPlayerData(uuid).setTeamId(null);
            String meg = FileManager.message.getString("kickTeam", "");
            if(!meg.equals("")){
                player.sendMessage(meg.replaceAll("<teamId>", String.valueOf(this.teamId)));
            }
            updateTeamInfoGui();
            String meg1 = FileManager.message.getString("system_leaveTeam", "");
            if(!meg1.equals("")){
                sendSystemMessage(meg1.replaceAll("<player_name>", player.getName()));
            }
            Main.log("玩家 " + player.getName() + " 被踢出队伍 " + this.teamId + " 成功");
        }
    }

    //加入队伍 只检测人数和玩家当前有没有队伍
    public void joinTeam(Player player){
        //检查队伍满了没有
        //最大队伍玩家数量
        int teamPlayerMax = FileManager.config.getInt("TeamInfo.teamSize");
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        UUID uuid = player.getUniqueId();
        //如果他是申请过的取消他的申请计时
        if(this.approveList.containsKey(uuid)){
            this.approveList.get(uuid).cancel();
            this.approveList.remove(uuid);
        }
        //如果他是被邀请过的 取消他的邀请记录
        if(this.inviteList.containsKey(uuid)){
            this.inviteList.remove(uuid);
            playerData.getSchedulerRunnable().cancel();
            playerData.setSchedulerRunnable(null);
            playerData.setInviteTeamId(null);
        }
        if(this.teamPlayer.size() >= teamPlayerMax){
            Main.log("玩家" + player.getName() + "无法加入已满队伍 " + this.teamId);
            String meg = FileManager.message.getString("teamFull", "");
            if(!meg.equals("")){
                player.sendMessage(meg.replaceAll("<teamId>", String.valueOf(this.teamId)));
            }
            updateTeamInfoGui();
            return;
        }
        //检查玩家有没有队伍
        if(playerData.getTeamId() != null){
            Main.log("玩家" + player.getName() + "无法加入队伍 " + this.teamId + " 已加入队伍 " + playerData.getTeamId());
            String meg = FileManager.message.getString("haveTeam", "");
            if(!meg.equals("")){
                player.sendMessage(meg.replaceAll("<teamId>", String.valueOf(this.teamId)));
            }
            updateTeamInfoGui();
            Player owner = Bukkit.getPlayer(this.teamOwner);
            if(owner != null){
                String meg1 = FileManager.message.getString("playersHaveTeams", "");
                if(!meg1.equals("")){
                    owner.sendMessage(meg1.replaceAll("<player_name>", player.getName()));
                }
            }
            return;
        }
        String meg = FileManager.message.getString("system_joinTeam", "");
        if(!meg.equals("")){
            sendSystemMessage(meg.replaceAll("<player_name>", player.getName()));
        }
        this.teamPlayer.put(uuid, false);
        playerData.setTeamId(this.teamId);
        updateTeamInfoGui();
        //玩家看着组队大厅的话 就给玩家打开队伍页面
        if((playerData.getGuiId().equals("组队大厅") && playerData.getUnrealGUIContainer() instanceof TeamLobbyGui) || (playerData.getGuiId().equals("队伍信息_观察") && playerData.getUnrealGUIContainer() instanceof TeamInfoGui)){
            GuiManager.openTeamInfo(player);
        }
        String meg1 = FileManager.message.getString("joinTeam", "");
        if(!meg1.equals("")){
            player.sendMessage(meg1.replaceAll("<teamId>", String.valueOf(this.teamId)));
        }
        Main.log("玩家" + player.getName() + " 成功加入队伍 " + this.teamId);
    }


    //全队消息
    public void teamMessage(String meg){
        this.teamPlayer.forEach((uuid, aBoolean) -> {
            Player player = Bukkit.getPlayer(uuid);
            if(player == null){
                this.teamPlayer.remove(uuid);
                PlayerDataManager.allPlayerData.remove(uuid);
            }else {
                player.sendMessage(FileManager.getPapiString(player, meg));
            }
        });
    }


    //判断是否是队长
    public Boolean isTeamOwner(UUID uuid){
        return this.teamOwner == uuid;
    }

    //获取队长
    public UUID getTeamOwner(){
        return this.teamOwner;
    }
    //设置队长
    public void setTeamOwner(UUID newTeamOwner){
        //如果新队长是成员才能继续
        if(this.teamPlayer.containsKey(newTeamOwner)){
            Player player = Bukkit.getPlayer(newTeamOwner);
            //如果玩家不在
            if(player == null){
                this.teamPlayer.remove(newTeamOwner);
                PlayerDataManager.allPlayerData.remove(newTeamOwner);
                return;
            }
            //转让前先把旧队长的状态设置成false
            this.teamPlayer.put(this.teamOwner, false);
            //新队长的状态设置成true
            this.teamPlayer.put(newTeamOwner, true);
            this.teamOwner = newTeamOwner;
            String meg = FileManager.message.getString("system_ownerSwitch", "");
            if(!meg.equals("")){
                sendSystemMessage(meg.replaceAll("<player_name>", player.getName()));
            }


            updateTeamInfoGui();
            Main.log("队伍 " + this.teamId + " 队长变更为 " + player.getName());
        }
    }

    //刷新队伍信息界面 除了聊天栏
    public void updateTeamInfoGui(){
        for(UUID uuid : this.teamPlayer.keySet()){
            Player player = Bukkit.getPlayer(uuid);
            if(player == null){
                this.teamPlayer.remove(uuid);
                PlayerDataManager.allPlayerData.remove(uuid);
                return;
            }
            PlayerData playerData = PlayerDataManager.getPlayerData(player);
            if(playerData.getGuiId() != null && playerData.getGuiId().equals("队伍信息") && (playerData.getUnrealGUIContainer() instanceof TeamInfoGui teamInfoGui)){
                teamInfoGui.Update();
                Main.log("为玩家 " + player.getName() + " 更新队伍界面");
            }
        }
    }

    //刷新聊天消息
    public void updateTeamMessageGui(){
        for(UUID uuid : this.teamPlayer.keySet()){
            Player player = Bukkit.getPlayer(uuid);
            if(player == null){
                this.teamPlayer.remove(uuid);
                PlayerDataManager.allPlayerData.remove(uuid);
                return;
            }
            PlayerData playerData = PlayerDataManager.getPlayerData(player);
            if(playerData.getGuiId() != null && playerData.getGuiId().equals("队伍信息") && (playerData.getUnrealGUIContainer() instanceof TeamInfoGui teamInfoGui)){
                teamInfoGui.UpdateMessage();
                Main.log("为玩家 " + player.getName() + " 更新聊天消息");
            }
        }
    }




    public void setDungeonsName(String dungeonsName) {
        this.dungeonsName = dungeonsName;
    }

    public String getDungeonsName() {
        return this.dungeonsName;
    }

    public Boolean getHide() {
        return this.isHide;
    }

    public void setHide(Boolean hide) {
        this.isHide = hide;
    }

    public Boolean getApprove() {
        return this.isApprove;
    }

    public void setApprove(Boolean approve) {
        this.isApprove = approve;
    }

    public String getTeamName() {
        return this.teamName;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public ConcurrentHashMap<UUID, Boolean> getTeamPlayer() {
        return this.teamPlayer;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;

    }

    public ConcurrentHashMap<UUID, SchedulerRunnable> getApproveList() {
        return this.approveList;
    }

    public Integer getTeamId() {
        return this.teamId;
    }

    public String getTeamInfo(){
        return "队伍ID: " + this.teamId + ",队长: " + Bukkit.getPlayer(this.teamOwner).getName() + ",副本: " + this.dungeonsName;
    }

    public ConcurrentHashMap<UUID, Long> getInviteList() {
        return this.inviteList;
    }

    public void setInvitableList(CopyOnWriteArrayList<Player> invitableList) {
        this.invitableList = invitableList;
    }

    public CopyOnWriteArrayList<Player> getInvitableList() {
        return this.invitableList;
    }

    public void sendTeamMessage(String meg, Player player) {
        //获取格式
        String megFormat;
        if(isTeamOwner(player.getUniqueId())){
            //如果是队长
            megFormat = FileManager.config.getString("MessageSettings.ownerMessage");
        }else {
            //如果不是队长
            megFormat = FileManager.config.getString("MessageSettings.defaultMessage");
        }
        //解析格式的变量
        megFormat = FileManager.getPapiString(player, megFormat);
        meg = megFormat.replaceAll("<message>", meg);
        // 如果队列满了，移除最早的元素
        if (this.teamMessageList.remainingCapacity() == 0) {
            this.teamMessageList.poll(); // 移除最早的元素
        }
        this.teamMessageList.offer(meg); // 添加新元素

    }
    //发送系统消息
    public void sendSystemMessage(String meg){
        if(meg.equals("")){
            return;
        }
        //获取格式
        String megFormat = FileManager.config.getString("MessageSettings.systemMessage");
        if (megFormat != null) {
            meg = megFormat.replaceAll("<message>", meg);
            // 如果队列满了，移除最早的元素
            if (this.teamMessageList.remainingCapacity() == 0) {
                this.teamMessageList.poll(); // 移除最早的元素
            }
            this.teamMessageList.offer(meg); // 添加新元素
        }
        teamMessage(meg);
        updateTeamMessageGui();
    }

    public List<String> getTeamMessage() {
        // 将队列的内容转换为列表
        List<String> list = new ArrayList<>(teamMessageList);
        // 反转列表以便最新的元素在最前面
        Collections.reverse(list);
        return list; // 返回队列的副本
    }

}
