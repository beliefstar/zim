package org.zim.common.model;

import lombok.Data;

@Data
public class ClientInfo {
    private Long userId;
    private String userName;


    public static ClientInfo of(Long userId, String userName) {
        ClientInfo clientInfo = new ClientInfo();
        clientInfo.setUserId(userId);
        clientInfo.setUserName(userName);
        return clientInfo;
    }
}
