package org.qiuhua.qiuhuateamsystem.playerdata;

import com.daxton.unrealcore.application.method.SchedulerRunnable;
import org.qiuhua.UnrealGUIPro.gui.UnrealGUIContainer;
import org.qiuhua.qiuhuateamsystem.gui.TeamLobbyGui;

public class PlayerData {

    //队伍的id
    private Integer teamId = null;

    //查看的guiId
    private String guiId = "";

    //查看的界面信息
    private UnrealGUIContainer unrealGUIContainer = null;

    //玩家当前被邀请的队伍
    private Integer inviteTeamId = null;

    //玩家当前的邀请倒计时
    private SchedulerRunnable schedulerRunnable;



    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public Integer getTeamId() {
        return this.teamId;
    }

    public String getGuiId() {
        return this.guiId;
    }
    public void setGuiId(String guiId) {
        this.guiId = guiId;
    }

    public UnrealGUIContainer getUnrealGUIContainer() {
        return this.unrealGUIContainer;
    }

    public void setUnrealGUIContainer(UnrealGUIContainer unrealGUIContainer) {
        this.unrealGUIContainer = unrealGUIContainer;
    }

    public Integer getInviteTeamId() {
        return this.inviteTeamId;
    }

    public void setInviteTeamId(Integer inviteTeamId) {
        this.inviteTeamId = inviteTeamId;
    }

    public SchedulerRunnable getSchedulerRunnable() {
        return this.schedulerRunnable;
    }

    public void setSchedulerRunnable(SchedulerRunnable schedulerRunnable) {
        this.schedulerRunnable = schedulerRunnable;
    }
}
