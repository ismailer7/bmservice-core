package com.bmservice.core.models.admin;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class Server {
    private int id;
    private int statusId;
    private int providerId;
    private int serverTypeId;
    private String name;
    private String hostName;
    private String mainIp;
    private String username;
    private String password;
    private int createdBy;
    private int lastUpdatedBy;
    private Date createdAt;
    private Date lastUpdatedAt;
    private int sshPort;
    private String authorizedUsers;
    private Date expirationDate;
}
