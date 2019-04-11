package me.bed0.jWynn.api.v1.guild;

import me.bed0.jWynn.api.common.GuildRank;

import java.util.Date;

public class WynncraftGuildMember {

    private String name;
    private String uuid;
    private GuildRank rank;
    private int contributed;
    private Date joined;

    public WynncraftGuildMember(String name, String uuid, GuildRank rank, int contributed, Date joined) {
        this.name = name;
        this.uuid = uuid;
        this.rank = rank;
        this.contributed = contributed;
        this.joined = joined;
    }

    public String getName() {
        return name;
    }

    public String getUuid() {
        return uuid;
    }

    public GuildRank getRank() {
        return rank;
    }

    public int getContributed() {
        return contributed;
    }

    public Date getJoined() {
        return joined;
    }
}