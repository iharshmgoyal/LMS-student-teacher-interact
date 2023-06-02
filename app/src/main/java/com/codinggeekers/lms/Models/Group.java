package com.codinggeekers.lms.Models;

public class Group {
    String GroupName, GroupId, GroupImg;

    public Group(String groupName, String groupImg) {
        GroupName = groupName;
        GroupImg = groupImg;
    }

    public String getGroupName() {
        return GroupName;
    }

    public void setGroupName(String groupName) {
        GroupName = groupName;
    }

    public String getGroupImg() {
        return GroupImg;
    }

    public void setGroupImg(String groupImg) {
        GroupImg = groupImg;
    }
}
