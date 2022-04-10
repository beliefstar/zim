package org.zim.common.model;

import lombok.Data;

/**
 * @author zhenxin
 * @program 广州智灵时代研发中心
 * @date 2022/4/8 16:40
 */
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
