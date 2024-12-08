package org.qiuhua.qiuhuateamsystem.team;

import com.daxton.unrealcore.application.method.SchedulerRunnable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.qiuhua.qiuhuateamsystem.Main;
import org.qiuhua.qiuhuateamsystem.file.FileManager;
import org.qiuhua.qiuhuateamsystem.hud.HudManager;
import org.qiuhua.qiuhuateamsystem.playerdata.PlayerData;
import org.qiuhua.qiuhuateamsystem.playerdata.PlayerDataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class TeamManager {

    //存储全部大厅的map
    public static final ConcurrentHashMap<Integer, TeamInfoData> allTeams = new ConcurrentHashMap<>();
    // 已解散的队伍 ID 列表
    public static final ConcurrentLinkedQueue<Integer> availableIds = new ConcurrentLinkedQueue<>();
    // 生成队伍 ID 的计数器
    private static final AtomicInteger nextId = new AtomicInteger(1);


    //通过指定id获取队伍信息
    public static TeamInfoData getTeams(Integer id){
        return allTeams.get(id);
    }

    //获取玩家队伍信息
    public static TeamInfoData getTeams(Player player){
        UUID uuid = player.getUniqueId();
        PlayerData playerData = PlayerDataManager.getPlayerData(player);

        if(playerData.getTeamId() != null){
            Integer teamId = playerData.getTeamId();
            return allTeams.get(teamId);
        }
        return null;
    }

    //创建一个队伍 提供队伍队长
    public static TeamInfoData createTeam(Player player){
        Integer teamId;
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        //检查这个人有没有队伍
        if(playerData.getTeamId() != null){
            //如果有队伍 则返回已经创建的队伍
            teamId = playerData.getTeamId();
            Main.log("你已有队伍 创建失败");
            return allTeams.get(teamId);
        }
        if (availableIds.isEmpty()) {
            // 没有可用的 ID，则生成新的 ID
            teamId = nextId.getAndIncrement();
        } else {
            // 从可用 ID 列表中获取 ID
            teamId = availableIds.poll();
        }
        TeamInfoData teamInfoData = new TeamInfoData(teamId, player);
        allTeams.put(teamId, teamInfoData);
        playerData.setTeamId(teamId);
        return teamInfoData;
    }


    //解散一个队伍 会检查玩家是否是队长
    public static void dissolveTeam(Player player) {
        TeamInfoData teamInfoData = getTeams(player);
        if(teamInfoData != null){
            UUID uuid = player.getUniqueId();
            if(teamInfoData.isTeamOwner(uuid)){
                teamInfoData.dissolveTeam();
            }else {
                Main.log("玩家 " + player.getName() + " 尝试解散队伍 " + teamInfoData.getTeamId() + " 失败 原因:不是队长");
            }
        }
    }

    //退出队伍 如果是队长 则解散队伍
    public static void leaveTeam(Player player){
        TeamInfoData teamInfoData = getTeams(player);
        if(teamInfoData != null){
            UUID uuid = player.getUniqueId();
            if(teamInfoData.isTeamOwner(uuid)){
                Main.log("玩家 " + player.getName() + " 尝试解散队伍 " + teamInfoData.getTeamId());
                teamInfoData.dissolveTeam();
            }else {
                Main.log("玩家 " + player.getName() + " 尝试离开队伍 " + teamInfoData.getTeamId());
                teamInfoData.leaveTeam(player);
            }
        }
    }

    //申请加入队伍
    public static void applyJoinTeam(Player player, Integer teamId){
        //检查队伍是否存在
        TeamInfoData teamInfoData = getTeams(teamId);
        if(teamInfoData == null){
            Main.log("玩家 " + player.getName() + " 无法加入队伍 " + teamId + " 队伍不存在");
            String meg = FileManager.message.getString("teamNull", "");
            if(!meg.equals("")){
                player.sendMessage(meg.replaceAll("<teamId>", String.valueOf(teamId)));
            }
            return;
        }
        //检查玩家有没有队伍
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        if(playerData.getTeamId() != null){
            Main.log("玩家 " + player.getName() + " 无法加入队伍 " + teamId + " 已加入队伍 " + playerData.getTeamId());
            String meg = FileManager.message.getString("haveTeam", "");
            if(!meg.equals("")){
                player.sendMessage(meg.replaceAll("<teamId>", String.valueOf(teamId)));
            }
            return;
        }
        //检查队伍是否是私有的
        if(teamInfoData.getHide()){
            Main.log("玩家 " + player.getName() + " 无法加入队伍 " + teamId + " 该队伍已设置隐藏");
            String meg = FileManager.message.getString("teamHide", "");
            if(!meg.equals("")){
                player.sendMessage(meg.replaceAll("<teamId>", String.valueOf(teamId)));
            }
            return;
        }
        //检查队伍满了没有
        int teamPlayerMax = FileManager.config.getInt("TeamInfo.teamSize");
        if(teamInfoData.getTeamPlayer().size() >= teamPlayerMax){
            Main.log("玩家" + player.getName() + "无法加入已满队伍 " + teamId);
            String meg = FileManager.message.getString("teamFull", "");
            if(!meg.equals("")){
                player.sendMessage(meg.replaceAll("<teamId>", String.valueOf(teamId)));
            }
            return;
        }
        UUID uuid = player.getUniqueId();
        //检测玩家是否申请过
        if(teamInfoData.getApproveList().containsKey(uuid)){
            Main.log("玩家 " + player.getName() + " 已申请加入队伍 " + teamId);
            String meg = FileManager.message.getString("applyRepeatedlyJoin", "");
            if(!meg.equals("")){
                player.sendMessage(meg.replaceAll("<teamId>", String.valueOf(teamId)));
            }
            return;
        }
        //检查是否是需要审批队伍
        if(teamInfoData.getApprove()){
            //如果需要审批 那就添加进审批列表

            //获取申请时间
            int approveTime = FileManager.config.getInt("TeamInfo.approveTime");
            //申请时间到了就移除申请
            SchedulerRunnable schedulerRunnable = new SchedulerRunnable() {
                @Override
                public void run() {
                    teamInfoData.getApproveList().remove(player.getUniqueId());
                    //获取队伍主人 给队伍主人发消息
                    Player teamOwner = Bukkit.getPlayer(teamInfoData.getTeamOwner());
                    if (teamOwner == null) {
                        //如果队长不在就解散这个队伍
                        teamInfoData.dissolveTeam();
                    }else {
                        teamInfoData.updateTeamInfoGui();
                    }
                    Main.log("玩家 " + player.getName() + " 申请加入队伍 " + teamId + " 到期");
                }
            };
            schedulerRunnable.runLaterAsync(Main.getMainPlugin(), approveTime);

            teamInfoData.getApproveList().put(player.getUniqueId(), schedulerRunnable);
            teamInfoData.updateTeamInfoGui();
            Main.log("玩家 " + player.getName() + " 申请加入队伍 " + teamId);
            //获取队伍主人 给队伍主人发消息
            Player teamOwner = Bukkit.getPlayer(teamInfoData.getTeamOwner());
            if (teamOwner == null) {
                String meg = FileManager.message.getString("teamNull", "");
                if(!meg.equals("")){
                    player.sendMessage(meg.replaceAll("<teamId>", String.valueOf(teamId)));
                }
                //如果队长不在就解散这个队伍
                teamInfoData.dissolveTeam();
            }else {
                String meg = FileManager.message.getString("joinApplication", "");
                if(!meg.equals("")){
                    teamOwner.sendMessage(meg.replaceAll("<player_name>", player.getName()));
                }
            }
        }else{
            //如果不需要审批那就直接加入
            teamInfoData.joinTeam(player);
        }
    }




    //踢出队伍
    public static void kickPlayer(Player triggerPlayer, Player targetPlayer, TeamInfoData teamInfoData){
        if(teamInfoData == null){
            return;
        }
        //如果目标是队长
        if(teamInfoData.isTeamOwner(targetPlayer.getUniqueId())){
            return;
        }
        //如果目标不是成员
        if(!teamInfoData.getTeamPlayer().containsKey(targetPlayer.getUniqueId())){
            return;
        }
        //检测触发者是不是队长
        if(teamInfoData.isTeamOwner(triggerPlayer.getUniqueId())){
            Main.log("玩家 " + triggerPlayer.getName() + " 尝试踢出 " + targetPlayer.getName());
            //如果是队长
            teamInfoData.kickPlayer(targetPlayer);
        }else {
            Main.log("玩家 " + triggerPlayer.getName() + " 尝试踢出 " + targetPlayer.getName() + " 失败 原因:不是队长");
        }
    }

    //邀请加入一个队伍
    public static void inviteJoin(Player triggerPlayer, Player targetPlayer, TeamInfoData teamInfoData){
        if(teamInfoData == null){
            return;
        }
        //如果目标是成员
        if(teamInfoData.getTeamPlayer().containsKey(targetPlayer.getUniqueId())){
            return;
        }
        //检测触发者是不是队长
        if(teamInfoData.isTeamOwner(triggerPlayer.getUniqueId())){
            Integer teamId = teamInfoData.getTeamId();
            Main.log("玩家 " + triggerPlayer.getName() + " 尝试邀请 " + targetPlayer.getName() + " 加入队伍 " + teamId);

            //邀请代码
            UUID uuid = targetPlayer.getUniqueId();
            Long time = System.currentTimeMillis();
            ConcurrentHashMap<UUID, Long> inviteList = teamInfoData.getInviteList();
            //如果玩家已解决被邀请过 并且下一次邀请的时间小于当前时间 那就结束
            if(inviteList.containsKey(uuid) && inviteList.get(uuid) > time){
                Main.log("玩家 " + targetPlayer.getName() + " 已经被邀请过加入队伍 " + teamId);
                return;
            }
            //计算下一次邀请的时间
            Long inviteCd = time + (FileManager.config.getLong("InviteSettings.inviteTime") * 50);
            inviteList.put(uuid, inviteCd);
            //设置玩家数据
            PlayerData playerData = PlayerDataManager.getPlayerData(targetPlayer);
            //设置当前的邀请队伍id
            playerData.setInviteTeamId(teamId);
            SchedulerRunnable schedulerRunnable = playerData.getSchedulerRunnable();
            if(schedulerRunnable != null){
                schedulerRunnable.cancel();
            }
            //设置邀请倒计时
            schedulerRunnable = new SchedulerRunnable() {
                @Override
                public void run() {
                    playerData.setInviteTeamId(null);
                    Main.log("玩家 " + targetPlayer.getName() + " 被邀请加入队伍 " + teamId + " 到期");
                    String meg = FileManager.message.getString("acceptSearchInvitationTimeout", "");
                    if(!meg.equals("")){
                        targetPlayer.sendMessage(meg.replaceAll("<teamId>", String.valueOf(teamId)));
                    }

                    //清理hud
                    HudManager.removeHud(targetPlayer);

                }
            };
            schedulerRunnable.runLaterAsync(Main.getMainPlugin(), FileManager.config.getLong("InviteSettings.inviteTime"));
            playerData.setSchedulerRunnable(schedulerRunnable);
            String meg = FileManager.message.getString("acceptSearchInvitation", "");
            if(!meg.equals("")){
                targetPlayer.sendMessage(meg.replaceAll("<teamId>", String.valueOf(teamId)).replaceAll("<player_name>", triggerPlayer.getName()));
            }

            //构建hud
            HudManager.sendHud(targetPlayer, teamInfoData);

            Main.log("成功邀请玩家" + targetPlayer.getName() + " 加入队伍 " + teamId);
        }else {
            Main.log("玩家 " + triggerPlayer.getName() + " 尝试邀请 " + targetPlayer.getName() + " 加入队伍 " + teamInfoData.getTeamId() + " 失败 原因:不是队长");
        }

    }

    //同意加入当前邀请的队伍
    public static void confirmJoin(Player player){
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Integer teamId = playerData.getInviteTeamId();
        if(teamId == null){
            return;
        }
        TeamInfoData teamInfoData = TeamManager.getTeams(playerData.getInviteTeamId());
        if(teamInfoData == null){
            Main.log("玩家 " + player.getName() + " 无法加入队伍 " + teamId + " 队伍不存在");
            String meg = FileManager.message.getString("teamNull", "");
            if(!meg.equals("")){
                player.sendMessage(meg.replaceAll("<teamId>", String.valueOf(teamId)));
            }
            return;
        }
        Main.log("玩家 " + player.getName() + " 同意加入队伍 " + teamId);
        teamInfoData.joinTeam(player);
        //清理hud
        HudManager.removeHud(player);
    }

    //拒绝加入当前的队伍
    public static void rejectJoin(Player player){
        PlayerData playerData = PlayerDataManager.getPlayerData(player);
        Integer teamId = playerData.getInviteTeamId();
        if(teamId == null){
            return;
        }
        TeamInfoData teamInfoData = TeamManager.getTeams(playerData.getInviteTeamId());
        if(teamInfoData == null){
            return;
        }
        Main.log("玩家 " + player.getName() + " 拒绝加入队伍 " + teamId);
        UUID uuid = player.getUniqueId();
        long time = System.currentTimeMillis();
        ConcurrentHashMap<UUID, Long> inviteList = teamInfoData.getInviteList();
        //计算下一次邀请的时间
        Long inviteCd = time + (FileManager.config.getLong("InviteSettings.inviteCd") * 50);
        inviteList.put(uuid, inviteCd);
        //设置当前的邀请队伍id
        playerData.setInviteTeamId(null);
        SchedulerRunnable schedulerRunnable = playerData.getSchedulerRunnable();
        if(schedulerRunnable != null){
            schedulerRunnable.cancel();
        }
        //清理hud
        HudManager.removeHud(player);


    }


    

    //批准一个玩家加入
    public static void approveJoin(Player triggerPlayer, Player targetPlayer, TeamInfoData teamInfoData){
        if(teamInfoData == null){
            return;
        }
        //如果目标是成员
        if(teamInfoData.getTeamPlayer().containsKey(targetPlayer.getUniqueId())){
            return;
        }
        //检测触发者是不是队长 并且目标在申请列表内
        if(teamInfoData.isTeamOwner(triggerPlayer.getUniqueId())){
            //并且目标在申请列表内
            if(teamInfoData.getApproveList().containsKey(targetPlayer.getUniqueId())){
                Main.log("玩家 " + triggerPlayer.getName() + " 尝试同意 " + targetPlayer.getName() + " 加入队伍 " + teamInfoData.getTeamId());
                teamInfoData.joinTeam(targetPlayer);
            }else {
                Main.log("玩家 " + triggerPlayer.getName() + " 尝试同意 " + targetPlayer.getName() + " 加入队伍 " + teamInfoData.getTeamId() + " 失败 原因:邀请已过期");
            }
        }else {
            Main.log("玩家 " + triggerPlayer.getName() + " 尝试同意 " + targetPlayer.getName() + " 加入队伍 " + teamInfoData.getTeamId() + " 失败 原因:不是队长");
        }
    }

    //转让队长
    public static void switchOwner(Player triggerPlayer, Player targetPlayer, TeamInfoData teamInfoData){
        if(teamInfoData == null){
            return;
        }
        //如果目标是队长
        if(teamInfoData.isTeamOwner(targetPlayer.getUniqueId())){
            return;
        }
        //如果目标不是成员
        if(!teamInfoData.getTeamPlayer().containsKey(targetPlayer.getUniqueId())){
            return;
        }
        //检测触发者是不是队长
        if(teamInfoData.isTeamOwner(triggerPlayer.getUniqueId())){
            //如果是队长
            Main.log("玩家 " + triggerPlayer.getName() + " 尝试转让队长 " + targetPlayer.getName());
            teamInfoData.setTeamOwner(targetPlayer.getUniqueId());
        }else {
            Main.log("玩家 " + triggerPlayer.getName() + " 尝试转让队长 " + targetPlayer.getName() + " 失败 原因:不是队长");
        }
    }


    //设置队伍名称
    public static void setTeamName(Player player, String teamName, TeamInfoData teamInfoData){
        if(teamInfoData.isTeamOwner(player.getUniqueId())){
            if(teamInfoData.getTeamName().equals(teamName) || teamName.equals("")){
                return;
            }
            Main.log("玩家 " + player.getName() + " 尝试修改队伍  " + teamInfoData.getTeamId() + " 名称为 " + teamName);
            teamInfoData.setTeamName(teamName);
            teamInfoData.updateTeamInfoGui();
        }else {
            Main.log("玩家 " + player.getName() + " 尝试修改队伍  " + teamInfoData.getTeamId() + " 名称为 " + teamName + " 失败 原因:不是队长");
        }
    }

    //设置队伍是否需要审批
    public static void setApprove(Player player, Boolean isApprove, TeamInfoData teamInfoData){
        //只有队长可以设置
        if(teamInfoData.isTeamOwner(player.getUniqueId())){
            Main.log("玩家 " + player.getName() + " 尝试修改队伍  " + teamInfoData.getTeamId() + " 审批功能为 " + isApprove);
            teamInfoData.setApprove(isApprove);
            teamInfoData.updateTeamInfoGui();
            String meg = FileManager.message.getString("system_Approval", "");
            if(!meg.equals("")){
                teamInfoData.sendSystemMessage(meg.replaceAll("<approval>", String.valueOf(isApprove)));
            }
        }else{
            Main.log("玩家 " + player.getName() + " 尝试修改队伍  " + teamInfoData.getTeamId() + " 审批功能失败 原因:不是队长");
        }
    }
    //设置队伍隐藏
    public static void setHide(Player player, Boolean isHide, TeamInfoData teamInfoData){
        //只有队长可以设置
        if(teamInfoData.isTeamOwner(player.getUniqueId())){
            Main.log("队伍 " + teamInfoData.getTeamId() + " 队伍隐藏修改为 " + isHide);
            teamInfoData.setHide(isHide);
            teamInfoData.updateTeamInfoGui();
            String meg = FileManager.message.getString("system_hide", "");
            if(!meg.equals("")){
                teamInfoData.sendSystemMessage(meg.replaceAll("<hide>", String.valueOf(isHide)));
            }

        }else{
            Main.log("玩家 " + player.getName() + " 尝试修改队伍  " + teamInfoData.getTeamId() + " 隐藏功能失败 原因:不是队长");
        }
    }
    //设置队伍副本
    public static void setDungeons(Player player, String dungeonsName, TeamInfoData teamInfoData){
        //只有队长可以设置
        if(teamInfoData.isTeamOwner(player.getUniqueId())){
            Main.log("队伍 " + teamInfoData.getTeamId() + " 副本修改为 " + dungeonsName);
            teamInfoData.setDungeonsName(dungeonsName);
            teamInfoData.updateTeamInfoGui();
            String meg = FileManager.message.getString("system_dungeonsName", "");
            if(!meg.equals("")){
                teamInfoData.sendSystemMessage(meg.replaceAll("<dungeonsName>", dungeonsName));
            }
        }else{
            Main.log("玩家 " + player.getName() + " 尝试修改队伍  " + teamInfoData.getTeamId() + " 副本失败 原因:不是队长");
        }
    }



    //搜索指定队伍 可以使用队伍名字 副本名字 队伍id 队长名字   原理是吧这些全部拼接起来看搜索条件
    //返回符合条件的队伍列表
    public static ConcurrentHashMap<Integer, TeamInfoData> searchTeam(String name){
        ConcurrentHashMap<Integer, TeamInfoData> teams = new ConcurrentHashMap<>();
        allTeams.forEach((teamId, teamInfoData) -> {
            //获取队伍信息
            String teamInfo = teamInfoData.getTeamInfo();
            //如果队伍信息包含了搜索的字符 就添加进队伍列表
            if(teamInfo.contains(name)){
                teams.put(teamId, teamInfoData);
                Main.log("队伍 " + teamId + " 符合搜索条件 -> " + teamInfo);
            }
        });
        return teams;
    }


    //获取指定类型的可邀请玩家列表
    //会排当前队伍玩家
    public static CopyOnWriteArrayList<Player> getInviteList(TeamInfoData teamInfoData){
        CopyOnWriteArrayList<Player> playerList = new CopyOnWriteArrayList<>();
        //获取当前的邀请列表类型
        String inviteListType = FileManager.config.getString("InviteSettings.inviteList.type","all");
        switch (inviteListType){
            case "all":
                playerList.addAll(getAllOnlinePlayers(teamInfoData));
                break;
            case "random":
                playerList.addAll(getRandomPlayers(teamInfoData));
                break;
            case "range":
                playerList.addAll(getRangePlayers(teamInfoData));
        }
        return playerList;
    }

    public static List<Player> getAllOnlinePlayers(TeamInfoData teamInfoData) {
        //获取队伍的玩家列表
        ConcurrentHashMap<UUID, Boolean> teamPlayer = teamInfoData.getTeamPlayer();
        List<Player> playerList = new ArrayList<>();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if(teamPlayer.containsKey(uuid)){
                continue;
            }
            playerList.add(player);
        }
        return playerList;
    }


    public static List<Player> getRandomPlayers(TeamInfoData teamInfoData) {
        List<Player> randomPlayers = new ArrayList<>();
        List<Player> allPlayers = new ArrayList<>(Bukkit.getServer().getOnlinePlayers());
        //获取队伍的玩家列表
        ConcurrentHashMap<UUID, Boolean> teamPlayer = teamInfoData.getTeamPlayer();
        // 遍历所有在线玩家，将不在队伍中的玩家添加到列表中
        for (Player player : allPlayers) {
            if (!teamPlayer.containsKey(player.getUniqueId())) {
                randomPlayers.add(player);
            }
        }
        List<Player> selectedPlayers = new ArrayList<>();
        Random random = new Random();
        // 从不在队伍中的玩家列表中随机选择指定数量的玩家
        while (selectedPlayers.size() < FileManager.config.getInt("InviteSettings.inviteList.value") && !randomPlayers.isEmpty()) {
            int randomIndex = random.nextInt(randomPlayers.size());
            Player selectedPlayer = randomPlayers.remove(randomIndex);
            selectedPlayers.add(selectedPlayer);
        }
        return selectedPlayers;
    }

    public static List<Player> getRangePlayers(TeamInfoData teamInfoData){
        List<Player> rangePlayers = new ArrayList<>();
        //获取队伍的玩家列表
        ConcurrentHashMap<UUID, Boolean> teamPlayer = teamInfoData.getTeamPlayer();
        //获取队长
        Player player = Bukkit.getPlayer(teamInfoData.getTeamOwner());
        int radius = FileManager.config.getInt("InviteSettings.inviteList.value",10);
        List<Entity> nearbyEntities = new ArrayList<>(player.getNearbyEntities(radius, radius, radius));
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Player nearbyPlayer) {
                if (!teamPlayer.containsKey(nearbyPlayer.getUniqueId())) {
                    rangePlayers.add(nearbyPlayer);
                }
            }
        }
        return rangePlayers;
    }

}

