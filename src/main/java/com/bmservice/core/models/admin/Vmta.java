package com.bmservice.core.models.admin;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class Vmta {
    private int id;
    private int statusId;
    private int serverId;
    private int ipId;
    private String name;
    private String type;
    private String ipValue;
    private String domain;
    private String username;
    private String password;
    private String smtphost;
    private int createdBy;
    private int lastUpdatedBy;
    private Date createdAt;
    private Date lastUpdatedAt;
}
